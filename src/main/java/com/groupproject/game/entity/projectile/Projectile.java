package com.groupproject.game.entity.projectile;

import com.groupproject.game.core.Camera;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Represents an active bullet or projectile in flight within the 2D world.
 *
 * <p>WHY: Encapsulates flight kinematics, time-to-live expiration, damage payloads, and collision
 * bounds into a decoupled, lightweight object. This ensures projectile behavior can be reused
 * across pistols, rifles, shotguns, and even enemy bullets without duplicating motion logic.
 */
public class Projectile {

    private double x;
    private double y;
    private final double vx;
    private final double vy;
    private final int damage;
    private final double radius;
    private final boolean playerBullet;
    private final boolean critical;

    private double lifetime;
    private boolean active = true;

    /**
     * Constructs a flying projectile with standard critical flag set to false.
     */
    public Projectile(double originX, double originY, double angle, double speed,
                      int damage, double maxLifetime, boolean playerBullet) {
        this(originX, originY, angle, speed, damage, maxLifetime, playerBullet, false);
    }

    /**
     * Constructs a flying projectile with directional velocity, damage payload, and critical hit flag.
     *
     * @param originX      Starting X coordinate in world space (tip of gun barrel).
     * @param originY      Starting Y coordinate in world space.
     * @param angle        Direction of flight in radians.
     * @param speed        Linear speed in pixels per second.
     * @param damage       Damage dealt upon impact.
     * @param maxLifetime  Maximum duration in seconds before self-dissipating.
     * @param playerBullet True if fired by operative; false if fired by hostile enemy.
     * @param critical     True if this projectile is an empowered critical hit.
     */
    public Projectile(double originX, double originY, double angle, double speed,
                      int damage, double maxLifetime, boolean playerBullet, boolean critical) {
        this.x = originX;
        this.y = originY;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.damage = damage;
        this.lifetime = maxLifetime;
        this.playerBullet = playerBullet;
        this.critical = critical;
        this.radius = critical ? 5.5 : 4.0;
    }

    /**
     * Advances bullet position and decrements lifetime timer.
     *
     * @param deltaTime Elapsed frame time in seconds.
     */
    public void update(double deltaTime) {
        if (!active) return;

        x += vx * deltaTime;
        y += vy * deltaTime;

        lifetime -= deltaTime;
        if (lifetime <= 0) {
            active = false;
        }
    }

    /**
     * Renders the glowing bullet tracer on the canvas.
     */
    public void render(Graphics2D g, Camera camera) {
        if (!active) return;

        int screenX = camera.worldToScreenX(x);
        int screenY = camera.worldToScreenY(y);
        int r = (int) radius;

        // Glowing outer halo
        if (critical) {
            g.setColor(new Color(255, 65, 20, 170)); // Rực lửa đỏ cam kim sắc khi nổ đòn chí mạng
        } else {
            g.setColor(playerBullet ? new Color(255, 200, 50, 100) : new Color(255, 60, 60, 100));
        }
        g.fillOval(screenX - r - 2, screenY - r - 2, (r + 2) * 2, (r + 2) * 2);

        // Solid core bullet
        if (critical) {
            g.setColor(new Color(255, 255, 210)); // Lõi siêu nhiệt ánh vàng trắng
        } else {
            g.setColor(playerBullet ? new Color(255, 240, 120) : new Color(255, 100, 100));
        }
        g.fillOval(screenX - r, screenY - r, r * 2, r * 2);
    }

    /**
     * Returns an AABB bounding rectangle for rapid collision detection.
     */
    public Rectangle getBounds() {
        return new Rectangle((int) (x - radius), (int) (y - radius), (int) (radius * 2), (int) (radius * 2));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getDamage() {
        return damage;
    }

    public boolean isCritical() {
        return critical;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isPlayerBullet() {
        return playerBullet;
    }

    public double getAngle() {
        return Math.atan2(vy, vx);
    }
}
