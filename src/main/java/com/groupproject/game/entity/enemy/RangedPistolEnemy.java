package com.groupproject.game.entity.enemy;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameSpriteManager;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.entity.projectile.Projectile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.state.PlayingState;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Autonomous tactical ranged enemy equipped with a semi-automatic tactical pistol.
 *
 * <p>WHY: Provides sustained ranged projectile pressure. Implements tactical kiting (backpedaling
 * when the player approaches within 190px, advancing when beyond 360px) and discharges bursts of
 * 1 to 3 slow-moving, dodgeable pistol bullets with deliberate slow-pacing delays to reward
 * player tactical maneuvering and cover usage.
 */
public class RangedPistolEnemy extends TargetDummy {

    private double speed;
    private double minRange;
    private double maxRange;
    private double facingAngle = 0.0;

    // Burst pistol firing mechanics
    private int burstTotal = 0;
    private int burstRemaining = 0;
    private double shotCooldownTimer = 0.0;
    private double burstCooldownTimer = 1.8;
    private double muzzleFlashTimer = 0.0;
    private int bulletDamage;
    private double bulletSpeed;

    // Tactical strafing & dynamic cover flanking mechanics
    // WHY: In safe engagement distance or when the operative takes cover behind obstacles,
    // the scout avoids standing still. It actively strafes laterally and aggressively flanks around
    // obstructions to flush hiding operatives out and establish unobstructed shooting angles.
    private double strafeTimer;
    private int strafeDirection; // 1: clockwise / right flank, -1: counter-clockwise / left flank

    /**
     * Constructs a standard tactical pistol scout enemy.
     *
     * @param x         Starting world X coordinate.
     * @param y         Starting world Y coordinate.
     * @param maxHealth Hit points.
     */
    public RangedPistolEnemy(double x, double y, int maxHealth) {
        this(x, y, maxHealth, 105.0, 10, 190.0, 360.0);
    }

    /**
     * Constructs a customizable tactical pistol scout enemy with explicit range and weapon parameters.
     *
     * @param x            Starting world X coordinate.
     * @param y            Starting world Y coordinate.
     * @param maxHealth    Hit points.
     * @param speed        Locomotion speed in pixels per second.
     * @param bulletDamage Damage dealt per projectile impact.
     * @param minRange     Distance threshold below which enemy retreats (kiting).
     * @param maxRange     Distance threshold above which enemy advances to close the gap.
     */
    public RangedPistolEnemy(double x, double y, int maxHealth, double speed, int bulletDamage,
                             double minRange, double maxRange) {
        this(x, y, maxHealth, speed, bulletDamage, minRange, maxRange, "Recon Scout [Pistol]");
    }

    /**
     * Constructs a customizable tactical pistol scout enemy with explicit range, weapon parameters, and display name.
     *
     * @param x            Starting world X coordinate.
     * @param y            Starting world Y coordinate.
     * @param maxHealth    Hit points.
     * @param speed        Locomotion speed in pixels per second.
     * @param bulletDamage Damage dealt per projectile impact.
     * @param minRange     Distance threshold below which enemy retreats (kiting).
     * @param maxRange     Distance threshold above which enemy advances to close the gap.
     * @param name         Display name reflecting tactical tier (e.g. MK-2, MK-3).
     */
    public RangedPistolEnemy(double x, double y, int maxHealth, double speed, int bulletDamage,
                             double minRange, double maxRange, String name) {
        super(x, y, maxHealth, 30.0, false, name);
        this.speed = speed;
        this.bulletDamage = bulletDamage;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.bulletSpeed = 380.0; // Moderate velocity for dodgeable gameplay
        this.strafeTimer = 1.0 + ThreadLocalRandom.current().nextDouble() * 1.5;
        this.strafeDirection = ThreadLocalRandom.current().nextBoolean() ? 1 : -1;
    }

    /**
     * Di chuyển kẻ địch có xét va chạm với địa hình bản đồ (tường, vật cản cứng và thùng gỗ phá hủy).
     * Cho phép trượt (sliding) theo từng trục độc lập nếu một trục bị chặn bởi chướng ngại vật.
     *
     * @param moveX      Bước di chuyển theo trục X.
     * @param moveY      Bước di chuyển theo trục Y.
     * @param dungeonMap Bản đồ địa hình hiện tại (nếu null thì bỏ qua kiểm tra va chạm, dùng cho unit test).
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

        // Base animation & hit flash update, along with directional knockback physics
        update(deltaTime);
        updateKnockback(deltaTime, dungeonMap);

        if (shotCooldownTimer > 0) {
            shotCooldownTimer -= deltaTime;
        }
        if (burstCooldownTimer > 0) {
            burstCooldownTimer -= deltaTime;
        }
        if (muzzleFlashTimer > 0) {
            muzzleFlashTimer -= deltaTime;
        }

        // Vector to player
        double dx = playerCenterX - x;
        double dy = playerCenterY - y;
        double dist = Math.hypot(dx, dy);

        if (dist > 0.001) {
            facingAngle = Math.atan2(dy, dx);
        }

        // While in spawn telegraph materialization period, hold position and do not fire
        if (isSpawning()) {
            return;
        }

        // --- 1. Tactical Locomotion & Angle Flanking System ---
        // WHY: Solves the static dummy problem. In safe combat range (minRange <= dist <= maxRange),
        // the scout actively strafes to evade incoming bullets, and when the operative breaks Line of Sight
        // by crouching behind cover, aggressively flanks around obstacles to reopen shooting angles and flush them out.
        boolean hasLineOfSight = (dungeonMap == null || dungeonMap.hasLineOfSight(x, y, playerCenterX, playerCenterY));
        double oldX = x;
        double oldY = y;

        if (dist < minRange) {
            // Player is too close: Backpedal away from player to preserve safe engagement distance
            double dirX = -dx / dist;
            double dirY = -dy / dist;
            double stepX = dirX * speed * deltaTime;
            double stepY = dirY * speed * deltaTime;

            moveWithCollision(stepX, stepY, dungeonMap);

            // If enemy is cornered against a wall while retreating, slide perpendicularly to escape corner trap
            if (Math.hypot(x - oldX, y - oldY) < 0.2 * speed * deltaTime) {
                double perpX = -dy / dist * strafeDirection;
                double perpY = dx / dist * strafeDirection;
                moveWithCollision(perpX * speed * deltaTime, perpY * speed * deltaTime, dungeonMap);
            }
        } else if (dist > maxRange) {
            // Player is too far: Advance to bring operative within pistol weapon range
            double dirX = dx / dist;
            double dirY = dy / dist;

            // If line of sight is blocked by distant cover, bias movement obliquely to flank the obstruction
            if (!hasLineOfSight) {
                double perpX = -dy / dist * strafeDirection;
                double perpY = dx / dist * strafeDirection;
                dirX = dirX * 0.7 + perpX * 0.7;
                dirY = dirY * 0.7 + perpY * 0.7;
                double len = Math.hypot(dirX, dirY);
                if (len > 0.001) {
                    dirX /= len;
                    dirY /= len;
                }
            }

            double stepX = dirX * speed * deltaTime;
            double stepY = dirY * speed * deltaTime;

            moveWithCollision(stepX, stepY, dungeonMap);
        } else {
            // --- Safe Engagement Range (minRange <= dist <= maxRange) ---
            // Update strafe timer to periodically alternate lateral pacing direction
            strafeTimer -= deltaTime;
            if (strafeTimer <= 0) {
                strafeDirection = -strafeDirection;
                strafeTimer = 1.6 + ThreadLocalRandom.current().nextDouble() * 1.4; // 1.6s - 3.0s
            }

            double perpX = -dy / dist * strafeDirection;
            double perpY = dx / dist * strafeDirection;

            double moveDirX;
            double moveDirY;
            double currentMoveSpeed;

            if (!hasLineOfSight) {
                // Case A: Operative is hiding behind cover (no Line of Sight).
                // Aggressively flank around the obstruction with tangential momentum and forward bias
                // to force the operative out of hiding and reopen clear shooting lines.
                double forwardBias = 0.40;
                moveDirX = perpX + (dx / dist) * forwardBias;
                moveDirY = perpY + (dy / dist) * forwardBias;
                currentMoveSpeed = speed * 1.10; // Rapid tactical flanking pace
            } else {
                // Case B: Clear Line of Sight.
                // Maintain tactical strafe to evade player fire while gently regulating distance
                // towards the optimal combat sweet spot (~275px).
                double idealRange = (minRange + maxRange) / 2.0;
                double rangeCorrection = 0.0;
                if (dist > idealRange + 30.0) {
                    rangeCorrection = 0.25; // Close the gap slightly
                } else if (dist < idealRange - 30.0) {
                    rangeCorrection = -0.25; // Yield ground slightly
                }

                moveDirX = perpX + (dx / dist) * rangeCorrection;
                moveDirY = perpY + (dy / dist) * rangeCorrection;
                currentMoveSpeed = speed * 0.75; // Measured combat strafing speed while aiming/firing
            }

            double moveLen = Math.hypot(moveDirX, moveDirY);
            if (moveLen > 0.001) {
                moveDirX /= moveLen;
                moveDirY /= moveLen;
            }

            double stepX = moveDirX * currentMoveSpeed * deltaTime;
            double stepY = moveDirY * currentMoveSpeed * deltaTime;

            moveWithCollision(stepX, stepY, dungeonMap);

            // If the dominant axis of strafe/flank movement is blocked by an obstacle or wall,
            // immediately invert direction so the scout orbits away from the obstruction
            boolean dominantBlocked;
            if (Math.abs(moveDirX) >= Math.abs(moveDirY)) {
                dominantBlocked = Math.abs(stepX) > 0.001 && Math.abs(x - oldX) < 0.25 * Math.abs(stepX);
            } else {
                dominantBlocked = Math.abs(stepY) > 0.001 && Math.abs(y - oldY) < 0.25 * Math.abs(stepY);
            }

            if (dominantBlocked) {
                strafeDirection = -strafeDirection;
                strafeTimer = 1.2 + ThreadLocalRandom.current().nextDouble() * 1.0;
            }
        }

        // --- 2. Pistol Weapon Discharge & 1-3 Round Burst Mechanics ---

        if (burstRemaining > 0) {
            // Currently discharging a burst
            if (shotCooldownTimer <= 0 && hasLineOfSight && projectileManager != null) {
                // Fire single bullet towards operative
                double barrelX = x + Math.cos(facingAngle) * 16.0;
                double barrelY = y + Math.sin(facingAngle) * 16.0;
                double spread = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.08;

                projectileManager.spawn(new Projectile(
                        barrelX, barrelY, facingAngle + spread,
                        bulletSpeed, bulletDamage, 2.5, false // Hostile enemy bullet
                ));

                muzzleFlashTimer = 0.09;
                burstRemaining--;
                shotCooldownTimer = 0.35; // Deliberate 350ms gap between shots in the burst

                if (burstRemaining <= 0) {
                    // Burst complete: Enter slow fire rate resting period (2.0s - 2.6s)
                    burstCooldownTimer = 2.0 + ThreadLocalRandom.current().nextDouble() * 0.6;
                }
            }
        } else {
            // Waiting for burst cooldown to expire
            if (burstCooldownTimer <= 0 && hasLineOfSight && dist <= maxRange + 60.0) {
                // Initiate new burst of 1 to 3 rounds
                burstTotal = 1 + ThreadLocalRandom.current().nextInt(3); // 1, 2, or 3 rounds
                burstRemaining = burstTotal;
                shotCooldownTimer = 0.12; // Slight aiming pause before first trigger pull
            }
        }
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

        // 1. Drop shadow
        g.setColor(new Color(0, 0, 0, 95));
        g.fillOval(screenCX - half, screenCY + half - 5, (int) size, 10);

        // 2. Thử nạp và hiển thị Sprite Enemy Gun xoay theo hướng đối mặt
        BufferedImage sprite = GameSpriteManager.getEnemyGunSprite();
        if (sprite != null) {
            // Nòng súng lục nhô ra trước (vẽ ở layer dưới thân nhân vật kẻ địch)
            int barrelLen = 14;
            int gunStartX = (int) (screenCX + Math.cos(facingAngle + 0.3) * 8);
            int gunStartY = (int) (screenCY + Math.sin(facingAngle + 0.3) * 8);
            int gunEndX = (int) (gunStartX + Math.cos(facingAngle) * barrelLen);
            int gunEndY = (int) (gunStartY + Math.sin(facingAngle) * barrelLen);

            Stroke oldStroke = g.getStroke();
            g.setStroke(new BasicStroke(3.0f));
            g.setColor(new Color(25, 30, 38)); // Gunmetal
            g.drawLine(gunStartX, gunStartY, gunEndX, gunEndY);
            g.setStroke(oldStroke);

            Graphics2D g2 = (Graphics2D) g.create();
            if (critFlashTimer > 0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            } else if (hitFlashTimer > 0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
            }
            int spriteDrawSize = (int) Math.round(size * 1.35);
            GameSpriteManager.renderRotatedSprite(g2, sprite, screenCX, screenCY, facingAngle, spriteDrawSize, spriteDrawSize);
            g2.dispose();

            // Muzzle Flash Star upon firing
            if (muzzleFlashTimer > 0) {
                g.setColor(new Color(255, 200, 50, 220));
                g.fillOval(gunEndX - 5, gunEndY - 5, 10, 10);
                g.setColor(new Color(255, 255, 150));
                g.fillOval(gunEndX - 2, gunEndY - 2, 4, 4);
            }
        } else {
            // --- Dự phòng Vector Fallback ---
            // 2. Thân người mặc áo giáp trinh sát dã chiến (Tactical Urban Plate Carrier)
            if (critFlashTimer > 0) {
                g.setColor(new Color(255, 45, 45)); // Chớp đỏ rực khi chịu sát thương chí mạng
            } else if (hitFlashTimer > 0) {
                g.setColor(Color.WHITE);
            } else {
                g.setColor(new Color(42, 48, 52)); // Xám than quân sự dã chiến
            }
            g.fillRoundRect(screenCX - half, screenCY - half, (int) size, (int) size, 8, 8);
            if (critFlashTimer > 0) {
                g.setColor(new Color(255, 140, 140));
                g.setStroke(new BasicStroke(2.0f));
                g.drawRoundRect(screenCX - half, screenCY - half, (int) size, (int) size, 8, 8);
                g.setStroke(new BasicStroke(1.0f));
            } else {
                g.setColor(new Color(78, 88, 95));
                g.drawRoundRect(screenCX - half, screenCY - half, (int) size, (int) size, 8, 8);
            }

            // 3. Mũ bảo hộ & kính ngắm quang học tác chiến màu hổ phách (Tactical Recon Visor)
            g.setColor(new Color(245, 185, 40)); // Kính ngắm màu hổ phách quân sự
            int eyeX = (int) (screenCX + Math.cos(facingAngle) * 5);
            int eyeY = (int) (screenCY + Math.sin(facingAngle) * 5);
            g.fillOval(eyeX - 4, eyeY - 4, 8, 8);

            // 4. Equipped Tactical Pistol
            int barrelLen = 15;
            int gunStartX = (int) (screenCX + Math.cos(facingAngle + 0.3) * 10);
            int gunStartY = (int) (screenCY + Math.sin(facingAngle + 0.3) * 10);
            int gunEndX = (int) (gunStartX + Math.cos(facingAngle) * barrelLen);
            int gunEndY = (int) (gunStartY + Math.sin(facingAngle) * barrelLen);

            Stroke oldStroke = g.getStroke();
            g.setStroke(new BasicStroke(3.5f));
            g.setColor(new Color(25, 30, 38)); // Gunmetal
            g.drawLine(gunStartX, gunStartY, gunEndX, gunEndY);
            g.setStroke(oldStroke);

            // 5. Muzzle Flash Star upon firing
            if (muzzleFlashTimer > 0) {
                g.setColor(new Color(255, 200, 50, 220));
                g.fillOval(gunEndX - 5, gunEndY - 5, 10, 10);
                g.setColor(new Color(255, 255, 150));
                g.fillOval(gunEndX - 2, gunEndY - 2, 4, 4);
            }
        }

        // 6. Overhead Health Bar
        int barW = 34;
        int barH = 5;
        int barX = screenCX - barW / 2;
        int barY = screenCY - half - 10;

        g.setColor(new Color(30, 30, 40));
        g.fillRect(barX, barY, barW, barH);
        float hpPercent = Math.max(0f, (float) currentHealth / maxHealth);
        g.setColor(new Color(230, 45, 45)); // Luôn luôn là màu đỏ
        g.fillRect(barX, barY, (int) (barW * hpPercent), barH);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(barX, barY, barW, barH);
    }

    public double getSpeed() {
        return speed;
    }

    public int getBurstRemaining() {
        return burstRemaining;
    }

    public int getBurstTotal() {
        return burstTotal;
    }

    public double getBurstCooldownTimer() {
        return burstCooldownTimer;
    }

    public double getMinRange() {
        return minRange;
    }

    public double getMaxRange() {
        return maxRange;
    }

    public int getBulletDamage() {
        return bulletDamage;
    }

    public int getStrafeDirection() {
        return strafeDirection;
    }

    public void setStrafeDirection(int dir) {
        this.strafeDirection = (dir >= 0) ? 1 : -1;
    }

    public double getStrafeTimer() {
        return strafeTimer;
    }

    public void setStrafeTimer(double timer) {
        this.strafeTimer = timer;
    }
}
