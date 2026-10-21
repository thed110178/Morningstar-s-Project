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
    private int damage;
    private final double radius;
    private final boolean playerBullet;
    private final boolean critical;

    // Cơ chế đạn xuyên phá mục tiêu và cover tạm thời
    private int remainingPierces = 0;
    private final double pierceDamageFalloff;
    private final boolean piercing;
    private final java.util.Set<Object> hitTargets = new java.util.HashSet<>();

    private double lifetime;
    private boolean active = true;

    /**
     * Constructs a flying projectile with standard critical flag set to false.
     */
    public Projectile(double originX, double originY, double angle, double speed,
                      int damage, double maxLifetime, boolean playerBullet) {
        this(originX, originY, angle, speed, damage, maxLifetime, playerBullet, false, 0, 0.0);
    }

    /**
     * Constructs a flying projectile with directional velocity, damage payload, and critical hit flag.
     */
    public Projectile(double originX, double originY, double angle, double speed,
                      int damage, double maxLifetime, boolean playerBullet, boolean critical) {
        this(originX, originY, angle, speed, damage, maxLifetime, playerBullet, critical, 0, 0.0);
    }

    /**
     * Constructs a flying projectile with penetration capabilities across multiple enemies and covers.
     *
     * @param originX             Starting X coordinate in world space.
     * @param originY             Starting Y coordinate in world space.
     * @param angle               Direction of flight in radians.
     * @param speed               Linear speed in pixels per second.
     * @param damage              Damage dealt upon impact.
     * @param maxLifetime         Maximum duration in seconds before self-dissipating.
     * @param playerBullet        True if fired by operative; false if fired by hostile enemy.
     * @param critical            True if this projectile is an empowered critical hit.
     * @param remainingPierces    Number of enemies/covers this bullet can pierce through before expiring.
     * @param pierceDamageFalloff Fraction of damage reduced per penetration (e.g. 0.25 = -25% damage per pierce).
     */
    public Projectile(double originX, double originY, double angle, double speed,
                      int damage, double maxLifetime, boolean playerBullet, boolean critical,
                      int remainingPierces, double pierceDamageFalloff) {
        this.x = originX;
        this.y = originY;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.damage = damage;
        this.lifetime = maxLifetime;
        this.playerBullet = playerBullet;
        this.critical = critical;
        this.remainingPierces = Math.max(0, remainingPierces);
        this.piercing = (remainingPierces > 0);
        this.pierceDamageFalloff = Math.max(0.0, Math.min(0.95, pierceDamageFalloff));
        this.radius = critical ? 5.5 : (piercing ? 4.8 : 4.0);
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

        // Vệt đạn xé gió sắc nét kéo dài cho đạn bắn tỉa xuyên phá
        if (piercing) {
            double angle = getAngle();
            int tailLength = 18;
            int tailX = screenX - (int) (Math.cos(angle) * tailLength);
            int tailY = screenY - (int) (Math.sin(angle) * tailLength);

            g.setColor(new Color(185, 80, 255, 140)); // Vệt đuôi tím điện quang
            g.setStroke(new java.awt.BasicStroke(critical ? 3.5f : 2.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g.drawLine(tailX, tailY, screenX, screenY);
        }

        // Glowing outer halo
        if (piercing) {
            g.setColor(critical ? new Color(255, 60, 220, 200) : new Color(185, 80, 255, 160));
        } else if (critical) {
            g.setColor(new Color(255, 65, 20, 170)); // Rực lửa đỏ cam kim sắc khi nổ đòn chí mạng
        } else {
            g.setColor(playerBullet ? new Color(255, 200, 50, 100) : new Color(255, 60, 60, 100));
        }
        g.fillOval(screenX - r - 2, screenY - r - 2, (r + 2) * 2, (r + 2) * 2);

        // Solid core bullet
        if (piercing) {
            g.setColor(new Color(255, 245, 255)); // Lõi thạch anh ánh trắng tím
        } else if (critical) {
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

    /**
     * Kiểm tra xem viên đạn có thể tác động sát thương lên mục tiêu chỉ định hay không.
     * Tránh việc một kẻ địch nhận nhiều lần sát thương từ cùng một viên đạn khi viên đạn bay xuyên qua hitbox.
     */
    public boolean canHit(Object target) {
        return active && target != null && !hitTargets.contains(target);
    }

    /**
     * Ghi nhận tác động lên một mục tiêu (kẻ địch hoặc người chơi).
     * Nếu đạn có số lần xuyên còn lại, giảm số lần xuyên và chiết khấu sát thương cho mục tiêu tiếp theo;
     * ngược lại nếu hết lần xuyên thì tiêu biến đạn (deactivate).
     */
    public void onHitTarget(Object target) {
        if (target != null) {
            hitTargets.add(target);
        }
        if (remainingPierces > 0) {
            remainingPierces--;
            this.damage = Math.max(1, (int) Math.round(this.damage * (1.0 - pierceDamageFalloff)));
        } else {
            deactivate();
        }
    }

    /**
     * Ghi nhận tác động lên ô cover tạm thời tại tọa độ lưới (col, row).
     * @return true nếu đạn vẫn còn tiếp tục bay xuyên qua cover; false nếu đạn bị chặn và tiêu biến.
     */
    public boolean onHitCover(int col, int row) {
        String key = col + "," + row;
        if (!hitTargets.contains(key)) {
            hitTargets.add(key);
            if (remainingPierces > 0) {
                remainingPierces--;
                this.damage = Math.max(1, (int) Math.round(this.damage * (1.0 - pierceDamageFalloff)));
                return true;
            } else {
                deactivate();
                return false;
            }
        }
        return active;
    }

    public int getRemainingPierces() {
        return remainingPierces;
    }

    public double getPierceDamageFalloff() {
        return pierceDamageFalloff;
    }

    public boolean isPiercing() {
        return piercing;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
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
