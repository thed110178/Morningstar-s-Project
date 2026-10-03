package com.groupproject.game.entity.enemy;

import com.groupproject.game.core.Camera;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Combat training target / Hostile enemy prototype placed inside the Combat Arena.
 *
 * <p>WHY: Provides an interactive target with a hit point (HP) pool to demonstrate combat
 * mechanics, projectile hit detection, melee knife slicing, and the automatic door unlock trigger
 * upon room clearance.
 */
public class TargetDummy {

    private final double x;
    private final double y;
    private final double size = 30.0;
    private final int maxHealth;
    private int currentHealth;
    private boolean alive = true;

    // Flash white when hit
    private double hitFlashTimer = 0.0;

    /**
     * Constructs a combat target at the designated world position.
     *
     * @param x         Center X coordinate in world space.
     * @param y         Center Y coordinate in world space.
     * @param maxHealth Total hit points.
     */
    public TargetDummy(double x, double y, int maxHealth) {
        this.x = x;
        this.y = y;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
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
        if (hitFlashTimer > 0) {
            hitFlashTimer -= deltaTime;
        }
    }

    /**
     * Renders the hostile entity and its overhead health bar.
     */
    public void render(Graphics2D g, Camera camera) {
        if (!alive) return;

        int screenCX = camera.worldToScreenX(x);
        int screenCY = camera.worldToScreenY(y);
        int half = (int) (size / 2.0);

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

    public int getCurrentHealth() {
        return currentHealth;
    }
}
