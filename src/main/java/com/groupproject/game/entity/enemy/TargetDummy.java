package com.groupproject.game.entity.enemy;

import com.groupproject.game.core.Camera;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.state.PlayingState;
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

    protected double x;
    protected double y;
    protected double size;
    protected int maxHealth;
    protected int currentHealth;
    protected boolean alive = true;
    protected boolean isBoss;
    protected String name;

    // Flash white when hit, or radiant gold when struck critically
    protected double hitFlashTimer = 0.0;
    protected double critFlashTimer = 0.0;
    // Animation timer for boss pulsing core
    protected double animTimer = 0.0;
    // Directional knockback impulse velocity
    protected double knockbackVx = 0.0;
    protected double knockbackVy = 0.0;

    // Spawn Telegraph (Warning Indicator) mechanics
    // WHY: When an enemy spawns at the start of a combat encounter or reinforcement wave,
    // it materializes over a short window (e.g. 1.25s). During this window, an animated holographic summoning ring,
    // contracting radar focus circle, and hazard badge display at the spawn point so the operative can anticipate
    // incoming threats and reposition safely rather than being instantly ambushed or overrun.
    protected double spawnTelegraphTimer = 0.0;
    protected double spawnTelegraphTotal = 0.0;

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
        takeDamage(damage, false);
    }

    /**
     * Applies damage to this target, flashing white for standard hit or radiant gold for critical strike.
     *
     * @param damage   Amount of hit points to deduct.
     * @param critical True if this damage was a critical strike.
     */
    public void takeDamage(int damage, boolean critical) {
        if (!alive || isSpawning()) return;

        currentHealth -= damage;
        if (critical) {
            critFlashTimer = 0.18; // Flash fiery radiant gold for 180ms
            hitFlashTimer = 0.18;
        } else {
            hitFlashTimer = 0.10; // Flash white for 100ms
        }

        if (currentHealth <= 0) {
            currentHealth = 0;
            alive = false;
        }
    }

    /**
     * Applies damage to this target, flashes visual feedback, and imparts directional knockback.
     *
     * @param damage         Amount of hit points to deduct.
     * @param knockbackAngle Angle of impact in radians.
     * @param knockbackForce Impulse magnitude in pixels per second.
     */
    public void takeDamage(int damage, double knockbackAngle, double knockbackForce) {
        takeDamage(damage, knockbackAngle, knockbackForce, false);
    }

    /**
     * Applies damage to this target, triggers hit/crit flash, and imparts directional knockback.
     *
     * @param damage         Amount of hit points to deduct.
     * @param knockbackAngle Angle of impact in radians.
     * @param knockbackForce Impulse magnitude in pixels per second.
     * @param critical       True if this damage was a critical strike.
     */
    public void takeDamage(int damage, double knockbackAngle, double knockbackForce, boolean critical) {
        if (!alive || isSpawning()) return;
        takeDamage(damage, critical);
        applyKnockback(knockbackAngle, knockbackForce);
    }

    /**
     * Imparts directional knockback velocity onto this enemy entity.
     * Boss units have high poise armor, absorbing 85% of knockback impulse.
     *
     * @param angle   Direction of impact in radians.
     * @param impulse Velocity impulse in pixels per second.
     */
    public void applyKnockback(double angle, double impulse) {
        if (!alive || isSpawning()) return;
        if (isBoss) {
            impulse *= 0.15; // Titan Boss absorbs 85% of knockback impulse
        }
        this.knockbackVx += Math.cos(angle) * impulse;
        this.knockbackVy += Math.sin(angle) * impulse;
    }

    /**
     * Updates knockback displacement and rapidly dampens impulse velocity with wall collision checking.
     *
     * @param deltaTime  Frame delta time in seconds.
     * @param dungeonMap Collision map for terrain walkability.
     */
    public void updateKnockback(double deltaTime, DungeonMap dungeonMap) {
        if (Math.abs(knockbackVx) > 0.01 || Math.abs(knockbackVy) > 0.01) {
            double stepX = knockbackVx * deltaTime;
            double stepY = knockbackVy * deltaTime;
            double half = size / 2.0;

            if (dungeonMap == null || dungeonMap.isAreaWalkable(x + stepX - half, y - half, size, size)) {
                x += stepX;
            } else {
                knockbackVx = 0.0;
            }

            if (dungeonMap == null || dungeonMap.isAreaWalkable(x - half, y + stepY - half, size, size)) {
                y += stepY;
            } else {
                knockbackVy = 0.0;
            }

            // High friction damping: knockback dissipates within ~0.10 - 0.15 seconds
            double friction = Math.max(0.0, 1.0 - 14.0 * deltaTime);
            knockbackVx *= friction;
            knockbackVy *= friction;

            if (Math.hypot(knockbackVx, knockbackVy) < 5.0) {
                knockbackVx = 0.0;
                knockbackVy = 0.0;
            }
        }
    }

    public void update(double deltaTime) {
        animTimer += deltaTime;
        if (hitFlashTimer > 0) {
            hitFlashTimer -= deltaTime;
            if (hitFlashTimer < 0) hitFlashTimer = 0.0;
        }
        if (critFlashTimer > 0) {
            critFlashTimer -= deltaTime;
            if (critFlashTimer < 0) critFlashTimer = 0.0;
        }
        if (spawnTelegraphTimer > 0) {
            spawnTelegraphTimer -= deltaTime;
            if (spawnTelegraphTimer < 0) {
                spawnTelegraphTimer = 0.0;
            }
        }
    }

    public double getHitFlashTimer() {
        return hitFlashTimer;
    }

    public double getCritFlashTimer() {
        return critFlashTimer;
    }

    /**
     * Updates AI behaviors (movement, aiming, attacking).
     *
     * <p>Subclasses (e.g. {@code MeleeEnemy}, {@code RangedPistolEnemy}) override this method
     * to implement autonomous pursuit, tactical kiting, and weapon discharge.
     *
     * @param deltaTime          Frame delta time in seconds.
     * @param playerCenterX      Target player operative X coordinate.
     * @param playerCenterY      Target player operative Y coordinate.
     * @param playerSize         Player hitbox dimension.
     * @param dungeonMap         Active dungeon collision grid.
     * @param projectileManager  Manager for spawning hostile bullets.
     * @param playingState       Active playing state callback (e.g. for dealing damage).
     */
    public void updateAI(double deltaTime, double playerCenterX, double playerCenterY, double playerSize,
                         DungeonMap dungeonMap, ProjectileManager projectileManager, PlayingState playingState) {
        update(deltaTime);
        updateKnockback(deltaTime, dungeonMap);
    }

    /**
     * Renders a tactical holographic summoning circle, pulsating warning ring, contracting focus circle,
     * and entity badge label on the floor while this enemy is materializing into the combat arena.
     *
     * @param g      Graphics context.
     * @param camera Active world camera for coordinate translation.
     */
    public void renderSpawnTelegraph(Graphics2D g, Camera camera) {
        int screenX = camera.worldToScreenX(x);
        int screenY = camera.worldToScreenY(y);

        double progress = (spawnTelegraphTotal > 0.0)
                ? Math.min(1.0, Math.max(0.0, 1.0 - (spawnTelegraphTimer / spawnTelegraphTotal)))
                : 1.0;

        // Visual telegraph styling: Clean, minimalist warning portal mark
        Color themeColor = isBoss ? new Color(255, 185, 35) : new Color(255, 60, 55);

        // 1. Pulsing ground summoning portal
        double pulse = Math.sin(animTimer * 16.0) * 2.5;
        int baseRadius = (int) (size / 2.0 + 10.0);
        int ringRadius = (int) (baseRadius + pulse);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        // Translucent filled ground portal glow
        g2.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 50));
        g2.fillOval(screenX - ringRadius, screenY - ringRadius / 2, ringRadius * 2, ringRadius);

        // Outer warning portal ring
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 210));
        g2.drawOval(screenX - ringRadius, screenY - ringRadius / 2, ringRadius * 2, ringRadius);

        // Contracting focus ring (shrinks towards entity size as progress -> 1.0)
        int focusRadius = (int) (ringRadius * (1.0 - progress * 0.70));
        g2.setColor(new Color(255, 255, 255, 220));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(screenX - focusRadius, screenY - focusRadius / 2, focusRadius * 2, focusRadius);

        // 4 Cardinal tick crosshairs marking exact spawn epicenter
        int tickLen = 5;
        g2.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 230));
        g2.drawLine(screenX - ringRadius - tickLen, screenY, screenX - ringRadius + tickLen, screenY);
        g2.drawLine(screenX + ringRadius - tickLen, screenY, screenX + ringRadius + tickLen, screenY);
        g2.drawLine(screenX, screenY - ringRadius / 2 - tickLen, screenX, screenY - ringRadius / 2 + tickLen);
        g2.drawLine(screenX, screenY + ringRadius / 2 - tickLen, screenX, screenY + ringRadius / 2 + tickLen);

        // 2. Faint holographic silhouette fading in gradually
        int half = (int) (size / 2.0);
        int alpha = (int) (progress * 110 + 25);
        g2.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), alpha));
        g2.fillRoundRect(screenX - half, screenY - half, (int) size, (int) size, 6, 6);
        g2.setColor(new Color(255, 255, 255, (int) (progress * 140 + 30)));
        g2.drawRoundRect(screenX - half, screenY - half, (int) size, (int) size, 6, 6);

        g2.dispose();
    }

    /**
     * Renders the hostile entity and its overhead health bar.
     * Regular enemies render standard red body with optic sensor.
     * Boss enemies render an imposing Titan chassis with pulsing hazard aura, composite armor, and core reactor.
     */
    public void render(Graphics2D g, Camera camera) {
        if (!alive) return;
        if (isSpawning()) {
            renderSpawnTelegraph(g, camera);
            return;
        }

        int screenCX = camera.worldToScreenX(x);
        int screenCY = camera.worldToScreenY(y);
        int half = (int) (size / 2.0);

        if (!isBoss) {
            // Shadow
            g.setColor(new Color(0, 0, 0, 90));
            g.fillOval(screenCX - half, screenCY, (int) size, 14);

            // Entity body: flashes searing red when critically struck, white when damaged, otherwise dark crimson
            if (critFlashTimer > 0) {
                g.setColor(new Color(255, 45, 45)); // Chớp đỏ rực khi chịu đòn chí mạng
            } else if (hitFlashTimer > 0) {
                g.setColor(Color.WHITE);
            } else {
                g.setColor(new Color(220, 50, 50));
            }
            g.fillRoundRect(screenCX - half, screenCY - half, (int) size, (int) size, 8, 8);
            if (critFlashTimer > 0) {
                g.setColor(new Color(255, 140, 140));
                g.setStroke(new BasicStroke(2.0f));
                g.drawRoundRect(screenCX - half, screenCY - half, (int) size, (int) size, 8, 8);
                g.setStroke(new BasicStroke(1.0f));
            } else {
                g.setColor(new Color(255, 120, 120));
                g.drawRoundRect(screenCX - half, screenCY - half, (int) size, (int) size, 8, 8);
            }

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

            if (critFlashTimer > 0) {
                g.setColor(new Color(255, 45, 45)); // Chớp đỏ rực khi chịu đòn chí mạng
                g.fillRoundRect(bodyX, bodyY, bSize, bSize, 14, 14);
            } else if (hitFlashTimer > 0) {
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

    public void setAlive(boolean alive) {
        this.alive = alive;
        if (!alive) {
            this.currentHealth = 0;
        }
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

    public double getKnockbackVx() {
        return knockbackVx;
    }

    public double getKnockbackVy() {
        return knockbackVy;
    }

    public boolean isSpawning() {
        return spawnTelegraphTimer > 0;
    }

    public double getSpawnTelegraphTimer() {
        return spawnTelegraphTimer;
    }

    public double getSpawnTelegraphTotal() {
        return spawnTelegraphTotal;
    }

    public void setSpawnTelegraph(double duration) {
        this.spawnTelegraphTimer = Math.max(0.0, duration);
        this.spawnTelegraphTotal = this.spawnTelegraphTimer;
    }
}
