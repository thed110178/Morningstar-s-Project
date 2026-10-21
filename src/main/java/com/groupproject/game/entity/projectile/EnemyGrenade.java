package com.groupproject.game.entity.projectile;

import com.groupproject.game.core.Camera;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.state.PlayingState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Tactical timed fragmentation grenade discharged by heavy grenadier hostile units (GrenadierEnemy).
 *
 * <p>WHY: Provides zoning / area-denial pressure against the operative. Unlike standard high-speed bullets
 * that impact immediately, {@code EnemyGrenade} is lobbed towards the operative's position, lands on the floor,
 * and arms a strict 2.0-second delay fuse. During this 2.0s window, a high-visibility pulsing hazard blast
 * radius and beeping beacon project onto the terrain, forcing the player to evade or reposition away from cover.
 */
public class EnemyGrenade extends Projectile {

    private final double startX;
    private final double startY;
    private final double targetX;
    private final double targetY;

    // Strict 2.0 second detonation fuse as specified by combat design
    private double fuseTimer = 2.0;
    private final double totalFuse = 2.0;

    // Flight / toss arc mechanics
    private final double tossDuration = 0.50; // 0.50s airborne travel time
    private double tossTimer = 0.0;
    private boolean landed = false;

    // Area-of-Effect detonation parameters
    private final double blastRadius;
    private boolean hasExploded = false;

    // Visual blinking timer for floor danger warning
    private double animTimer = 0.0;

    /**
     * Constructs a hostile timed grenade thrown towards designated coordinates.
     *
     * @param originX     Starting world X coordinate of the grenadier.
     * @param originY     Starting world Y coordinate of the grenadier.
     * @param targetX     Target landing world X coordinate.
     * @param targetY     Target landing world Y coordinate.
     * @param damage      Detonation explosive damage (e.g. 24 HP).
     * @param blastRadius Detonation AoE radius in pixels (e.g. 78 px).
     */
    public EnemyGrenade(double originX, double originY, double targetX, double targetY,
                        int damage, double blastRadius) {
        super(originX, originY, Math.atan2(targetY - originY, targetX - originX),
                0.0, damage, 2.5, false);
        this.startX = originX;
        this.startY = originY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.blastRadius = blastRadius;
    }

    /**
     * Advances grenade flight kinematics and floor countdown fuse.
     *
     * @param deltaTime Elapsed frame time in seconds.
     */
    @Override
    public void update(double deltaTime) {
        if (!isActive()) return;

        animTimer += deltaTime;

        // 1. Airborne toss arc phase
        if (!landed) {
            tossTimer += deltaTime;
            double progress = Math.min(1.0, tossTimer / tossDuration);
            // Linear horizontal interpolation towards target
            double curX = startX + (targetX - startX) * progress;
            double curY = startY + (targetY - startY) * progress;
            setX(curX);
            setY(curY);

            if (progress >= 1.0) {
                landed = true;
                setX(targetX);
                setY(targetY);
            }
        }

        // 2. Strict 2.0-second countdown fuse
        fuseTimer -= deltaTime;
        if (fuseTimer <= 0.0) {
            fuseTimer = 0.0;
            deactivate(); // Marked for detonation in ProjectileManager / PlayingState
        }
    }

    /**
     * Triggers the high-explosive fragmentation payload at current world coordinates.
     * Damages player, destroys destructible cover, and triggers screen shake and explosion visual.
     *
     * @param playingState Active gameplay state.
     * @param dungeonMap   Collision terrain map.
     */
    public void explode(PlayingState playingState, DungeonMap dungeonMap) {
        if (hasExploded) return;
        hasExploded = true;
        deactivate();

        double blastX = getX();
        double blastY = getY();

        if (playingState != null) {
            playingState.triggerAreaExplosion(blastX, blastY, blastRadius, getDamage());
        }

        if (dungeonMap != null) {
            dungeonMap.damageDestructibleCoverInArea(
                    blastX - blastRadius, blastY - blastRadius,
                    blastRadius * 2, blastRadius * 2,
                    getDamage());
        }
    }

    /**
     * Handles deflection if the grenade impacts an impenetrable dungeon wall during flight.
     */
    public void handleWallCollision(DungeonMap dungeonMap) {
        // If it collides with a wall during throw, drop it down at current point immediately
        landed = true;
    }

    public boolean hasExploded() {
        return hasExploded;
    }

    public double getFuseTimer() {
        return fuseTimer;
    }

    public double getTotalFuse() {
        return totalFuse;
    }

    public double getBlastRadius() {
        return blastRadius;
    }

    public boolean isLanded() {
        return landed;
    }

    @Override
    public void render(Graphics2D g, Camera camera) {
        if (!isActive()) return;

        int screenX = camera.worldToScreenX(getX());
        int screenY = camera.worldToScreenY(getY());

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        double fuseProgress = Math.max(0.0, Math.min(1.0, 1.0 - (fuseTimer / totalFuse))); // 0.0 -> 1.0

        // --- 1. RENDER AREA-OF-EFFECT DANGER ZONE ON GROUND ---
        // WHY: Visual clarity is paramount. Operative must clearly perceive the 78px hazard zone
        // to make split-second tactical decisions (e.g. Combat Dash [SPACE] roll out of blast radius).
        int r = (int) blastRadius;

        // Blinking frequency accelerates as fuse timer runs out (2Hz at start -> 14Hz near detonation)
        double blinkFreq = 3.0 + fuseProgress * 12.0;
        float blinkAlphaFactor = (float) (Math.sin(animTimer * blinkFreq * Math.PI * 2.0) * 0.5 + 0.5);

        // Danger zone translucent floor fill
        int fillAlpha = (int) (20 + fuseProgress * 30 + blinkAlphaFactor * 25);
        g2.setColor(new Color(255, 45, 25, Math.min(255, fillAlpha)));
        g2.fillOval(screenX - r, screenY - r, r * 2, r * 2);

        // Dashed pulsing danger perimeter outline
        float[] dashPattern = {8.0f, 6.0f};
        float strokeW = (float) (1.8 + fuseProgress * 1.4);
        g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, (float) (animTimer * 20.0)));
        int outlineAlpha = (int) (140 + blinkAlphaFactor * 105);
        g2.setColor(new Color(255, 60, 30, Math.min(255, outlineAlpha)));
        g2.drawOval(screenX - r, screenY - r, r * 2, r * 2);

        // Solid inner warning ring contracting towards epicenter
        int innerRadius = (int) (r * (1.0 - fuseProgress * 0.85));
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(new Color(255, 200, 50, (int) (100 + blinkAlphaFactor * 120)));
        g2.drawOval(screenX - innerRadius, screenY - innerRadius, innerRadius * 2, innerRadius * 2);

        // Crosshairs tick marks at perimeter
        int tick = 6;
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(255, 75, 40, (int) (180 + blinkAlphaFactor * 75)));
        g2.drawLine(screenX - r - tick, screenY, screenX - r + tick, screenY);
        g2.drawLine(screenX + r - tick, screenY, screenX + r + tick, screenY);
        g2.drawLine(screenX, screenY - r - tick, screenX, screenY - r + tick);
        g2.drawLine(screenX, screenY + r - tick, screenX, screenY + r + tick);

        // --- 2. RENDER GRENADE CANISTER ---
        // If airborne, calculate parabolic arc elevation offset
        int renderY = screenY;
        if (!landed) {
            double arcProgress = Math.min(1.0, tossTimer / tossDuration);
            double arcOffset = Math.sin(arcProgress * Math.PI) * 28.0;
            renderY = (int) (screenY - arcOffset);

            // Ground shadow underneath airborne grenade
            int shadowW = (int) (12 * (1.0 - arcOffset / 35.0));
            g2.setColor(new Color(0, 0, 0, 75));
            g2.fillOval(screenX - shadowW / 2, screenY - 3, shadowW, 6);
        }

        // Heavy ordnance grenade body (size 11x11 px)
        int gSize = 10;
        int gx = screenX - gSize / 2;
        int gy = renderY - gSize / 2;

        // Shadow on ground when landed
        if (landed) {
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillOval(gx - 2, gy + gSize - 2, gSize + 4, 6);
        }

        // Dark matte charcoal canister body
        g2.setColor(new Color(45, 52, 60));
        g2.fillRoundRect(gx, gy, gSize, gSize, 4, 4);

        // Metallic reinforced rim
        g2.setColor(new Color(85, 95, 105));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(gx, gy, gSize, gSize, 4, 4);

        // Central orange hazard band
        g2.setColor(new Color(255, 140, 20));
        g2.fillRect(gx + 1, gy + 3, gSize - 2, 4);

        // Blinking red detonation LED indicator at center
        boolean ledOn = blinkAlphaFactor > 0.4;
        g2.setColor(ledOn ? new Color(255, 30, 20) : new Color(120, 20, 15));
        g2.fillOval(screenX - 2, renderY - 2, 4, 4);
        if (ledOn) {
            // Bright LED bloom glow
            g2.setColor(new Color(255, 60, 40, 160));
            g2.drawOval(screenX - 3, renderY - 3, 6, 6);
        }

        // Small countdown badge above grenade: "2.0s", "1.4s", "0.5s"
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        String timeStr = String.format("%.1fs", fuseTimer);
        int textW = fm.stringWidth(timeStr);
        int badgeX = screenX - textW / 2;
        int badgeY = gy - 6;

        g2.setColor(new Color(15, 15, 20, 210));
        g2.fillRoundRect(badgeX - 3, badgeY - 9, textW + 6, 12, 4, 4);
        g2.setColor(new Color(255, 80, 50, (int) (180 + blinkAlphaFactor * 75)));
        g2.drawString(timeStr, badgeX, badgeY);

        g2.dispose();
    }
}
