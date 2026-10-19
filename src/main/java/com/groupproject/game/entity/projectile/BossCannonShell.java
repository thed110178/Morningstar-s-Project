package com.groupproject.game.entity.projectile;

import com.groupproject.game.core.Camera;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.state.PlayingState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Heavy explosive cannon shell discharged by the Battle Tank Boss (BossTank).
 *
 * <p>WHY: Upon colliding with solid terrain, destructible cover, or reaching the operative,
 * the shell detonates into a high-damage area-of-effect (AoE) explosion that penetrates
 * surrounding cover barriers, forcing the player to evade rather than hide.
 */
public class BossCannonShell extends Projectile {

    private final double explosionRadius;
    private boolean exploded = false;

    /**
     * Constructs a heavy cannon shell.
     *
     * @param originX         Starting world X coordinate.
     * @param originY         Starting world Y coordinate.
     * @param angle           Direction of flight in radians.
     * @param speed           Flight velocity (e.g. 420 px/s).
     * @param damage          Impact and explosion damage (e.g. 30 HP).
     * @param maxLifetime     Flight duration in seconds before self-detonation.
     * @param explosionRadius Detonation radius in pixels (e.g. 75 px).
     */
    public BossCannonShell(double originX, double originY, double angle, double speed,
                           int damage, double maxLifetime, double explosionRadius) {
        super(originX, originY, angle, speed, damage, maxLifetime, false);
        this.explosionRadius = explosionRadius;
    }

    /**
     * Triggers the explosive detonation payload at current coordinates.
     * Deals AoE damage to operative, destroys nearby destructible cover, and spawns visual explosion.
     *
     * @param playingState Active gameplay state.
     * @param dungeonMap   Collision terrain map.
     */
    public void explode(PlayingState playingState, DungeonMap dungeonMap) {
        if (exploded) return;
        exploded = true;
        deactivate();

        double blastX = getX();
        double blastY = getY();

        if (playingState != null) {
            playingState.triggerAreaExplosion(blastX, blastY, explosionRadius, getDamage());
        }

        if (dungeonMap != null) {
            dungeonMap.damageDestructibleCoverInArea(
                    blastX - explosionRadius, blastY - explosionRadius,
                    explosionRadius * 2, explosionRadius * 2,
                    getDamage());
        }
    }

    public boolean hasExploded() {
        return exploded;
    }

    public double getExplosionRadius() {
        return explosionRadius;
    }

    @Override
    public void render(Graphics2D g, Camera camera) {
        if (!isActive()) return;

        int screenX = camera.worldToScreenX(getX());
        int screenY = camera.worldToScreenY(getY());
        double angle = getAngle();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Fiery propulsion tail / smoke trail
        int tailLen = 16;
        int tailX = (int) (screenX - Math.cos(angle) * tailLen);
        int tailY = (int) (screenY - Math.sin(angle) * tailLen);
        g2.setColor(new Color(255, 120, 20, 160));
        g2.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(tailX, tailY, screenX, screenY);

        // 2. Heavy armored shell projectile body (size 14x10 px rotated along trajectory)
        g2.translate(screenX, screenY);
        g2.rotate(angle);

        // Outer glow
        g2.setColor(new Color(255, 60, 40, 120));
        g2.fillOval(-9, -7, 18, 14);

        // Metallic ordnance shell
        g2.setColor(new Color(70, 75, 80));
        g2.fillRoundRect(-7, -5, 14, 10, 4, 4);

        // Glowing warhead tip
        g2.setColor(new Color(255, 220, 50));
        g2.fillPolygon(new int[]{7, 12, 7}, new int[]{-4, 0, 4}, 3);

        g2.dispose();
    }
}
