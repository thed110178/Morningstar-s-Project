package com.groupproject.game.entity.enemy;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameSoundManager;
import com.groupproject.game.core.GameSoundManager.SoundEffect;
import com.groupproject.game.core.GameSpriteManager;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.state.PlayingState;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Cybernetic combat field medic / support drone specializing in allied regeneration and tactical evasion.
 *
 * <p>WHY: Introduces non-offensive support dynamics into combat rooms starting from Stage 2.
 * The medic possesses no offensive attacks; instead, it projects a continuous emerald healing aura centered
 * on itself that periodically restores health to injured squadmates (raiders, scouts, grenadiers, and bosses).
 * Its autonomous AI actively seeks out and approaches wounded allies while prioritizing personal survival
 * by kiting away from the operative whenever the player draws near.
 */
public class MedicEnemy extends TargetDummy {

    private double speed;
    private double healRadius;
    private int healAmount;
    private double healInterval;
    private double healTimer = 0.0;
    private double pulseAnimTimer = 0.0;
    private double fleeDistance;
    private double facingAngle = 0.0;

    // Current ally being approached for medical triage
    private TargetDummy healingTarget = null;

    /**
     * Constructs a standard combat field medic with default parameters.
     *
     * @param x         Starting world X coordinate.
     * @param y         Starting world Y coordinate.
     * @param maxHealth Hit points.
     */
    public MedicEnemy(double x, double y, int maxHealth) {
        this(x, y, maxHealth, 115.0, 135.0, 6, 0.80, 210.0, "Combat Field Medic [Support]");
    }

    /**
     * Constructs a customizable combat field medic with configurable attributes.
     *
     * @param x             Starting world X coordinate.
     * @param y             Starting world Y coordinate.
     * @param maxHealth     Hit points.
     * @param speed         Locomotion speed in pixels per second.
     * @param healRadius    Radius of the restorative aura centered on this medic.
     * @param healAmount    Amount of HP restored per pulse to each ally within the aura.
     * @param healInterval  Frequency in seconds between successive restorative pulses.
     * @param fleeDistance  Proximity threshold triggering evasive maneuvers away from the player.
     * @param name          Tactical display name.
     */
    public MedicEnemy(double x, double y, int maxHealth, double speed, double healRadius,
                      int healAmount, double healInterval, double fleeDistance, String name) {
        super(x, y, maxHealth, 30.0, false, name);
        this.speed = speed;
        this.healRadius = healRadius;
        this.healAmount = healAmount;
        this.healInterval = healInterval;
        this.healTimer = healInterval;
        this.fleeDistance = fleeDistance;
    }

    /**
     * Moves this entity with decoupled axis terrain collision checking.
     */
    private void moveWithCollision(double moveX, double moveY, DungeonMap dungeonMap) {
        double half = size / 2.0;
        if (dungeonMap == null || dungeonMap.isAreaWalkable(x + moveX - half, y - half, size, size)) {
            x += moveX;
        }
        if (dungeonMap == null || dungeonMap.isAreaWalkable(x - half, y + moveY - half, size, size)) {
            y += moveY;
        }
    }

    @Override
    public void updateAI(double deltaTime, double playerCenterX, double playerCenterY, double playerSize,
                         DungeonMap dungeonMap, ProjectileManager projectileManager, PlayingState playingState) {
        if (!alive) return;

        update(deltaTime);
        updateKnockback(deltaTime, dungeonMap);

        if (pulseAnimTimer > 0) {
            pulseAnimTimer -= deltaTime;
            if (pulseAnimTimer < 0) pulseAnimTimer = 0.0;
        }

        if (isSpawning()) {
            return;
        }

        // --- 1. SCAN FOR WOUNDED ALLIED SQUADMATES ---
        // WHY: The medic's primary objective is squad sustainment. It continually identifies
        // the most severely wounded ally (lowest HP percentage) to close distance and provide triage.
        TargetDummy bestWoundedAlly = null;
        double lowestHpRatio = 1.0;

        List<TargetDummy> targets = (playingState != null) ? playingState.getCombatTargets() : null;
        if (targets != null) {
            for (TargetDummy ally : targets) {
                if (ally != this && ally.isAlive() && !ally.isSpawning()) {
                    if (ally.getCurrentHealth() < ally.getMaxHealth()) {
                        double ratio = (double) ally.getCurrentHealth() / ally.getMaxHealth();
                        if (ratio < lowestHpRatio) {
                            lowestHpRatio = ratio;
                            bestWoundedAlly = ally;
                        }
                    }
                }
            }
        }
        this.healingTarget = bestWoundedAlly;

        // --- 2. PERIODIC HEALING AURA PULSE ---
        // Restores health to all allied units within healRadius centered on this medic
        healTimer -= deltaTime;
        if (healTimer <= 0.0) {
            healTimer = healInterval;
            pulseAnimTimer = 0.35; // Trigger visual emerald pulse ring

            if (targets != null) {
                boolean healedAnyAlly = false;
                for (TargetDummy ally : targets) {
                    if (ally != this && ally.isAlive() && !ally.isSpawning()) {
                        double distToAlly = Math.hypot(ally.getCenterX() - x, ally.getCenterY() - y);
                        if (distToAlly <= healRadius && ally.getCurrentHealth() < ally.getMaxHealth()) {
                            ally.heal(healAmount);
                            healedAnyAlly = true;
                        }
                    }
                }
                if (healedAnyAlly) {
                    GameSoundManager.play(SoundEffect.HEALING, 0.55f);
                }
            }
        }

        // --- 3. AUTONOMOUS TRIAGE LOCOMOTION & PLAYER EVASION ---
        double dxPlayer = playerCenterX - x;
        double dyPlayer = playerCenterY - y;
        double distToPlayer = Math.hypot(dxPlayer, dyPlayer);

        double moveVx = 0.0;
        double moveVy = 0.0;

        if (distToPlayer < fleeDistance) {
            // PRIORITY 1: PLAYER EVASION (Flee from operative to preserve survival)
            if (distToPlayer > 0.001) {
                moveVx = -(dxPlayer / distToPlayer) * speed;
                moveVy = -(dyPlayer / distToPlayer) * speed;
                facingAngle = Math.atan2(moveVy, moveVx);
            }
        } else if (healingTarget != null) {
            // PRIORITY 2: APPROACH WOUNDED SQUADMATE
            double dxAlly = healingTarget.getCenterX() - x;
            double dyAlly = healingTarget.getCenterY() - y;
            double distToAlly = Math.hypot(dxAlly, dyAlly);

            if (distToAlly > healRadius * 0.50) {
                // Advance toward wounded ally to bring them inside the healing aura
                moveVx = (dxAlly / distToAlly) * speed;
                moveVy = (dyAlly / distToAlly) * speed;
                facingAngle = Math.atan2(moveVy, moveVx);
            } else {
                // Already in comfortable healing proximity: gently strafe/orbit to avoid clustering
                facingAngle = Math.atan2(dyAlly, dxAlly);
            }
        } else {
            // PRIORITY 3: NO WOUNDED ALLIES - Maintain safe staging distance behind vanguard allies
            if (distToPlayer < 300.0 && distToPlayer > 0.001) {
                moveVx = -(dxPlayer / distToPlayer) * (speed * 0.65);
                moveVy = -(dyPlayer / distToPlayer) * (speed * 0.65);
                facingAngle = Math.atan2(moveVy, moveVx);
            } else if (distToPlayer > 420.0 && distToPlayer > 0.001) {
                moveVx = (dxPlayer / distToPlayer) * (speed * 0.50);
                moveVy = (dyPlayer / distToPlayer) * (speed * 0.50);
                facingAngle = Math.atan2(moveVy, moveVx);
            }
        }

        moveWithCollision(moveVx * deltaTime, moveVy * deltaTime, dungeonMap);
    }

    public double getHealRadius() {
        return healRadius;
    }

    public int getHealAmount() {
        return healAmount;
    }

    public double getHealInterval() {
        return healInterval;
    }

    public double getSpeed() {
        return speed;
    }

    public double getFleeDistance() {
        return fleeDistance;
    }

    public TargetDummy getHealingTarget() {
        return healingTarget;
    }

    /**
     * Renders the restorative healing aura perimeter onto the dungeon floor.
     * <p>WHY: Rendered on the ground layer UNDER all entities so the healing field
     * does not obscure player, enemies, or obstacles while clearly demarcating hostile recovery zone.
     * Uses red / crimson styling to clearly distinguish as a hostile enemy threat.
     */
    public void renderAura(Graphics2D g, Camera camera) {
        if (!alive || isSpawning()) return;

        int screenCX = camera.worldToScreenX(x);
        int screenCY = camera.worldToScreenY(y);
        int hr = (int) healRadius;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        // Translucent hostile crimson ground fill
        g2.setColor(new Color(220, 35, 45, 24));
        g2.fillOval(screenCX - hr, screenCY - hr, hr * 2, hr * 2);

        // Dashed rotating perimeter line in warning red
        float[] dashPattern = {6.0f, 6.0f};
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, (float) (animTimer * 12.0)));
        g2.setColor(new Color(255, 60, 60, 135));
        g2.drawOval(screenCX - hr, screenCY - hr, hr * 2, hr * 2);

        // 4 Cardinal medical cross badges '+' on the perimeter ring in crimson
        g2.setStroke(new BasicStroke(1.6f));
        g2.setColor(new Color(255, 75, 75, 185));
        int crossArm = 4;
        // North
        g2.drawLine(screenCX, screenCY - hr - crossArm, screenCX, screenCY - hr + crossArm);
        g2.drawLine(screenCX - crossArm, screenCY - hr, screenCX + crossArm, screenCY - hr);
        // South
        g2.drawLine(screenCX, screenCY + hr - crossArm, screenCX, screenCY + hr + crossArm);
        g2.drawLine(screenCX - crossArm, screenCY + hr, screenCX + crossArm, screenCY + hr);
        // West
        g2.drawLine(screenCX - hr - crossArm, screenCY, screenCX - hr + crossArm, screenCY);
        g2.drawLine(screenCX - hr, screenCY - crossArm, screenCX - hr, screenCY + crossArm);
        // East
        g2.drawLine(screenCX + hr - crossArm, screenCY, screenCX + hr + crossArm, screenCY);
        g2.drawLine(screenCX + hr, screenCY - crossArm, screenCX + hr, screenCY + crossArm);

        // Expanding pulse wave when a healing cycle executes (crimson ripple)
        if (pulseAnimTimer > 0) {
            double pulseProgress = 1.0 - (pulseAnimTimer / 0.35); // 0.0 -> 1.0
            int pulseRadius = (int) (hr * pulseProgress);
            int pulseAlpha = (int) (180 * (1.0 - pulseProgress));
            g2.setStroke(new BasicStroke((float) (3.2 * (1.0 - pulseProgress * 0.5))));
            g2.setColor(new Color(255, 50, 50, pulseAlpha));
            g2.drawOval(screenCX - pulseRadius, screenCY - pulseRadius, pulseRadius * 2, pulseRadius * 2);
        }

        g2.dispose();
    }

    @Override
    public void render(Graphics2D g, Camera camera) {
        if (!alive) return;
        if (isSpawning()) {
            renderSpawnTelegraph(g, camera);
            return;
        }

        int screenCX = camera.worldToScreenX(x);
        int screenCY = camera.worldToScreenY(y);
        int half = (int) (size / 2.0);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        // Floor shadow
        g2.setColor(new Color(0, 0, 0, 95));
        g2.fillOval(screenCX - half, screenCY + 2, (int) size, 12);

        // 2. Thử nạp và hiển thị Sprite Quân Y (Combat Field Medic) xoay theo hướng di chuyển
        BufferedImage sprite = GameSpriteManager.getEnemyMedicSprite();
        if (sprite != null) {
            Graphics2D gSprite = (Graphics2D) g2.create();
            if (critFlashTimer > 0) {
                gSprite.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            } else if (hitFlashTimer > 0) {
                gSprite.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
            }
            int spriteDrawSize = (int) Math.round(size * 1.35);
            GameSpriteManager.renderRotatedSprite(gSprite, sprite, screenCX, screenCY, facingAngle, spriteDrawSize, spriteDrawSize);
            gSprite.dispose();
        } else {
            // --- Dự phòng Vector Fallback ---
            // Armor body - Dark Military Gray with Hostile Red Trims
            int bx = screenCX - half;
            int by = screenCY - half;
            int bSize = (int) size;

            if (critFlashTimer > 0) {
                g2.setColor(new Color(255, 45, 45));
                g2.fillRoundRect(bx, by, bSize, bSize, 8, 8);
            } else if (hitFlashTimer > 0) {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(bx, by, bSize, bSize, 8, 8);
            } else {
                // Dark military gray chassis (phù hợp màu mặc định của kẻ địch)
                g2.setColor(new Color(68, 74, 84));
                g2.fillRoundRect(bx, by, bSize, bSize, 8, 8);

                // Sub-armor carbon plating
                g2.setColor(new Color(48, 53, 62));
                g2.fillRoundRect(bx + 4, by + 4, bSize - 8, bSize - 8, 5, 5);

                // Rear twin thruster nubs
                g2.setColor(new Color(55, 60, 70));
                g2.fillRoundRect(bx + 4, by - 4, bSize - 8, 5, 3, 3);
                g2.setColor(new Color(220, 40, 45));
                g2.fillRect(bx + bSize / 2 - 2, by - 4, 4, 5);

                // Crimson hostile hazard trims
                g2.setColor(new Color(195, 45, 50));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(bx, by, bSize, bSize, 8, 8);
            }

            // Hostile red medical cross '+' emblem at center chest
            g2.setColor(new Color(230, 40, 40));
            g2.fillRect(screenCX - 2, screenCY - 6, 4, 12);
            g2.fillRect(screenCX - 6, screenCY - 2, 12, 4);

            // Glowing center core of the cross
            g2.setColor(new Color(255, 140, 140));
            g2.fillRect(screenCX - 2, screenCY - 2, 4, 4);

            // Glowing red optic sensor eye
            g2.setColor(new Color(255, 60, 60));
            g2.fillOval(screenCX - 3, screenCY - 3, 6, 6);
        }

        // Overhead health bar - luôn luôn màu đỏ
        int barW = 34;
        int barH = 5;
        int barX = screenCX - barW / 2;
        int barY = screenCY - half - 10;

        g2.setColor(new Color(30, 30, 40));
        g2.fillRect(barX, barY, barW, barH);
        float hpPercent = (float) currentHealth / maxHealth;
        g2.setColor(new Color(230, 45, 45)); // Luôn luôn là màu đỏ
        g2.fillRect(barX, barY, (int) (barW * hpPercent), barH);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(barX, barY, barW, barH);

        g2.dispose();
    }
}
