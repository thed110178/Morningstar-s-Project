package com.groupproject.game.entity.weapon;

import com.groupproject.game.core.Camera;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Represents an instantaneous melee strike hitbox and visual slash arc effect.
 *
 * <p>WHY: Melee attacks in top-down games (like knife slices or sword swings) do not shoot
 * prolonged projectiles. Instead, they project a transient damage zone in front of the operative
 * along the aiming vector for a fraction of a second. This class encapsulates both the geometric
 * damage volume and the crisp kinetic slash animation.
 */
public class MeleeSlash {

    private final double centerX;
    private final double centerY;
    private final double angle;
    private final double hitboxSize;
    private final int damage;

    private double lifetime;
    private final double maxLifetime;
    private boolean active = true;

    /**
     * Constructs a melee slash hitbox.
     *
     * @param originX     Operative center X coordinate.
     * @param originY     Operative center Y coordinate.
     * @param aimAngle    Direction in radians towards the mouse pointer.
     * @param reachOffset Distance from operative center to hitbox center (pixels).
     * @param hitboxSize  Width and height of the damage square (approximately 1 tile: ~44px).
     * @param damage      Damage dealt to targets in range.
     * @param duration    Visual persistence duration in seconds (e.g. 0.12s).
     */
    public MeleeSlash(double originX, double originY, double aimAngle, double reachOffset,
                      double hitboxSize, int damage, double duration) {
        this.angle = aimAngle;
        this.hitboxSize = hitboxSize;
        this.damage = damage;
        this.maxLifetime = duration;
        this.lifetime = duration;

        // Position the hitbox center forward along the aim vector
        this.centerX = originX + Math.cos(aimAngle) * reachOffset;
        this.centerY = originY + Math.sin(aimAngle) * reachOffset;
    }

    /**
     * Decrements the active strike timer.
     */
    public void update(double deltaTime) {
        lifetime -= deltaTime;
        if (lifetime <= 0) {
            active = false;
        }
    }

    /**
     * Renders a sweeping silver-cyan slash arc in the direction of the strike.
     */
    public void render(Graphics2D g, Camera camera) {
        if (!active) return;

        int screenCX = camera.worldToScreenX(centerX);
        int screenCY = camera.worldToScreenY(centerY);
        int halfSize = (int) (hitboxSize / 2.0);

        // Alpha fade-out based on remaining lifetime
        float progress = (float) (lifetime / maxLifetime);
        int alpha = Math.clamp((int) (progress * 220), 40, 220);

        // Draw translucent tactical damage sector
        g.setColor(new Color(255, 230, 100, alpha / 3));
        g.fillRect(screenCX - halfSize, screenCY - halfSize, (int) hitboxSize, (int) hitboxSize);
        g.setColor(new Color(255, 240, 150, alpha));
        g.drawRect(screenCX - halfSize, screenCY - halfSize, (int) hitboxSize, (int) hitboxSize);

        // Draw swift kinetic slash line along the perpendicular tangent
        double perpAngle = angle + Math.PI / 2.0;
        int slashLength = (int) (hitboxSize * 0.75);
        int x1 = (int) (screenCX + Math.cos(perpAngle) * slashLength);
        int y1 = (int) (screenCY + Math.sin(perpAngle) * slashLength);
        int x2 = (int) (screenCX - Math.cos(perpAngle) * slashLength);
        int y2 = (int) (screenCY - Math.sin(perpAngle) * slashLength);

        g.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 255, 255, alpha));
        g.drawLine(x1, y1, x2, y2);
        g.setStroke(new BasicStroke(1.0f)); // Reset stroke
    }

    /**
     * Returns the rectangular bounding hitbox for damage overlap queries.
     */
    public Rectangle getBounds() {
        return new Rectangle(
                (int) (centerX - hitboxSize / 2.0),
                (int) (centerY - hitboxSize / 2.0),
                (int) hitboxSize,
                (int) hitboxSize
        );
    }

    public boolean isActive() {
        return active;
    }

    public int getDamage() {
        return damage;
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }
}
