package com.groupproject.game.entity.enemy;

import com.groupproject.game.core.Camera;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;

/**
 * Combat training target / Hostile enemy prototype placed inside the Combat Arena.
 *
 * <p>WHY: Provides an interactive target with a hit point (HP) pool to demonstrate combat
 * mechanics, projectile hit detection, melee knife slicing, and the automatic door unlock trigger
 * upon room clearance.
 *
 * <p>Supports regular combat targets (size 30px) and Grand Arena Titan Boss Dummies (size 64px,
 * heavy composite armor, pulsating core reactor, and massive HP capacity).
 */
public class TargetDummy {

    private final double x;
    private final double y;
    private final double size;
    private final int maxHealth;
    private int currentHealth;
    private boolean alive = true;
    private final boolean isBoss;
    private final String name;

    // Flash white when hit
    private double hitFlashTimer = 0.0;
    // Animation timer for boss pulsing core
    private double animTimer = 0.0;

    /**
     * Constructs a standard combat target at the designated world position.
     *
     * @param x         Center X coordinate in world space.
     * @param y         Center Y coordinate in world space.
     * @param maxHealth Total hit points.
     */
    public TargetDummy(double x, double y, int maxHealth) {
        this(x, y, maxHealth, 30.0, false, "Target Dummy");
    }

    /**
     * Constructs a combat target or boss entity with customizable dimensions and semantic classification.
     *
     * @param x         Center X coordinate in world space.
     * @param y         Center Y coordinate in world space.
     * @param maxHealth Total hit points.
     * @param size      Bounding box dimension across width and height.
     * @param isBoss    True if this entity represents the stage boss.
     * @param name      Display name for tactical status cards and boss health bar.
     */
    public TargetDummy(double x, double y, int maxHealth, double size, boolean isBoss, String name) {
        this.x = x;
        this.y = y;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.size = size;
        this.isBoss = isBoss;
        this.name = name != null ? name : (isBoss ? "TITAN MECHA DUMMY [BOSS]" : "Target Dummy");
    }

    /**
     * Applies damage to this target and flashes visual feedback.
     *
     * @param damage Amount of hit points to deduct.
     */
    public void takeDamage(int damage) {
        if (!alive) return;

        currentHealth -= damage;
        hitFlashTimer = 0.1; // Flash white for 100ms

        if (currentHealth <= 0) {
            currentHealth = 0;
            alive = false;
        }
    }

    public void update(double deltaTime) {
        animTimer += deltaTime;
        if (hitFlashTimer > 0) {
            hitFlashTimer -= deltaTime;
        }
    }

    /**
     * Renders the hostile entity and its overhead health bar.
     * Regular enemies render standard red body with optic sensor.
     * Boss enemies render an imposing Titan chassis with pulsing hazard aura, composite armor, and core reactor.
     */
    public void render(Graphics2D g, Camera camera) {
        if (!alive) return;

        int screenCX = camera.worldToScreenX(x);
        int screenCY = camera.worldToScreenY(y);
        int half = (int) (size / 2.0);

        if (!isBoss) {
            // Shadow
            g.setColor(new Color(0, 0, 0, 90));
            g.fillOval(screenCX - half, screenCY, (int) size, 14);

            // Entity body: flashes white when damaged, otherwise dark crimson with glowing core
            if (hitFlashTimer > 0) {
                g.setColor(Color.WHITE);
            } else {
                g.setColor(new Color(220, 50, 50));
            }
            g.fillRoundRect(screenCX - half, screenCY - half, (int) size, (int) size, 8, 8);
            g.setColor(new Color(255, 120, 120));
            g.drawRoundRect(screenCX - half, screenCY - half, (int) size, (int) size, 8, 8);

            // Glowing red optic eye
            g.setColor(new Color(255, 240, 60));
            g.fillOval(screenCX - 4, screenCY - 4, 8, 8);

            // Overhead health bar
            int barW = 34;
            int barH = 5;
            int barX = screenCX - barW / 2;
            int barY = screenCY - half - 10;

            g.setColor(new Color(30, 30, 40));
            g.fillRect(barX, barY, barW, barH);
            float hpPercent = (float) currentHealth / maxHealth;
            g.setColor(hpPercent > 0.4f ? new Color(50, 220, 90) : new Color(255, 60, 60));
            g.fillRect(barX, barY, (int) (barW * hpPercent), barH);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(barX, barY, barW, barH);

        } else {
            // --- BOSS RENDERING: TITAN MECHA DUMMY ---
            // 1. Heavy Pulsing Shadow
            int shadowW = (int) (size * 1.35);
            int shadowH = 22;
            g.setColor(new Color(0, 0, 0, 130));
            g.fillOval(screenCX - shadowW / 2, screenCY + half - 10, shadowW, shadowH);

            // 2. Glowing Hazard Pulse Ring under the Boss
            float pulse = (float) (Math.sin(animTimer * 4.0) * 0.5 + 0.5);
            int auraRadius = (int) (half + 12 + pulse * 6);
            g.setColor(new Color(255, 45, 45, (int) (35 + pulse * 35)));
            g.fillOval(screenCX - auraRadius, screenCY - auraRadius, auraRadius * 2, auraRadius * 2);
            g.setColor(new Color(255, 60, 60, (int) (60 + pulse * 60)));
            g.drawOval(screenCX - auraRadius, screenCY - auraRadius, auraRadius * 2, auraRadius * 2);

            // 3. Heavy Armored Chassis (Titan Mecha Body)
            int bodyX = screenCX - half;
            int bodyY = screenCY - half;
            int bSize = (int) size;

            if (hitFlashTimer > 0) {
                g.setColor(Color.WHITE);
                g.fillRoundRect(bodyX, bodyY, bSize, bSize, 14, 14);
            } else {
                // Gradient giáp đỏ sẫm titan
                GradientPaint bodyGp = new GradientPaint(
                        bodyX, bodyY, new Color(165, 25, 35),
                        bodyX + bSize, bodyY + bSize, new Color(90, 15, 22)
                );
                Paint oldPaint = g.getPaint();
                g.setPaint(bodyGp);
                g.fillRoundRect(bodyX, bodyY, bSize, bSize, 14, 14);
                g.setPaint(oldPaint);

                // Tấm giáp bảo vệ bên trong (Inner obsidian armor plates)
                g.setColor(new Color(30, 20, 25));
                g.fillRoundRect(bodyX + 8, bodyY + 8, bSize - 16, bSize - 16, 8, 8);

                // Đường viền năng lượng vàng cam (Hazard Cyber Trims)
                g.setColor(new Color(255, 180, 40));
                g.setStroke(new BasicStroke(2.0f));
                g.drawRoundRect(bodyX, bodyY, bSize, bSize, 14, 14);

                // Góc giáp vát gia cố 4 góc
                g.setColor(new Color(255, 215, 60));
                g.drawLine(bodyX, bodyY + 12, bodyX + 12, bodyY);
                g.drawLine(bodyX + bSize - 12, bodyY, bodyX + bSize, bodyY + 12);
                g.drawLine(bodyX, bodyY + bSize - 12, bodyX + 12, bodyY + bSize);
                g.drawLine(bodyX + bSize - 12, bodyY + bSize, bodyX + bSize, bodyY + bSize - 12);
                g.setStroke(new BasicStroke(1.0f));
            }

            // 4. Giáp vai Pauldrons nhô ra 2 bên
            g.setColor(hitFlashTimer > 0 ? Color.WHITE : new Color(120, 20, 28));
            g.fillRoundRect(bodyX - 8, bodyY + 12, 10, 24, 4, 4);
            g.fillRoundRect(bodyX + bSize - 2, bodyY + 12, 10, 24, 4, 4);
            g.setColor(new Color(255, 215, 60));
            g.drawRoundRect(bodyX - 8, bodyY + 12, 10, 24, 4, 4);
            g.drawRoundRect(bodyX + bSize - 2, bodyY + 12, 10, 24, 4, 4);

            // Đèn cảnh báo trên 2 vai
            g.setColor(new Color(255, 230, 60));
            g.fillOval(bodyX - 5, bodyY + 16, 4, 6);
            g.fillOval(bodyX + bSize + 1, bodyY + 16, 4, 6);

            // 5. Lõi lò phản ứng năng lượng trung tâm (Glowing Reactor Core / Optic Eye)
            int coreR = 15;
            g.setColor(new Color(40, 10, 15));
            g.fillOval(screenCX - coreR, screenCY - coreR, coreR * 2, coreR * 2);

            // Lõi phát quang đỏ rực rỡ
            g.setColor(hitFlashTimer > 0 ? Color.WHITE : new Color(255, 40, 50));
            int innerR = (int) (coreR - 3 + pulse * 2);
            g.fillOval(screenCX - innerR, screenCY - innerR, innerR * 2, innerR * 2);

            // Tâm vàng optic rực sáng
            g.setColor(new Color(255, 245, 120));
            g.fillOval(screenCX - 4, screenCY - 4, 8, 8);

            // 6. Overhead Health Bar rộng (68px) với viền vàng
            int barW = 68;
            int barH = 7;
            int barX = screenCX - barW / 2;
            int barY = bodyY - 16;

            g.setColor(new Color(20, 24, 32, 220));
            g.fillRoundRect(barX - 2, barY - 2, barW + 4, barH + 4, 4, 4);
            g.setColor(new Color(255, 215, 60));
            g.drawRoundRect(barX - 2, barY - 2, barW + 4, barH + 4, 4, 4);

            float hpPercent = Math.max(0f, (float) currentHealth / maxHealth);
            g.setColor(hpPercent > 0.4f ? new Color(255, 60, 60) : new Color(255, 25, 25));
            g.fillRect(barX, barY, (int) (barW * hpPercent), barH);
        }
    }

    /**
     * Returns an AABB collision box for projectile and melee overlap queries.
     */
    public Rectangle getBounds() {
        return new Rectangle((int) (x - size / 2.0), (int) (y - size / 2.0), (int) size, (int) size);
    }

    public boolean isAlive() {
        return alive;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSize() {
        return size;
    }

    public boolean isBoss() {
        return isBoss;
    }

    public String getName() {
        return name;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }
}
