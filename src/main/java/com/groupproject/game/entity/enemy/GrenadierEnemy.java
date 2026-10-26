package com.groupproject.game.entity.enemy;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameSpriteManager;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.entity.projectile.EnemyGrenade;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.state.PlayingState;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Heavy ordnance grenadier operative designed to zone and flush the player out of cover.
 *
 * <p>WHY: Introduced starting from Stage 3 to combat static camping and cover hugging.
 * The grenadier keeps medium combat distance and periodically lobs timed fragmentation grenades
 * ({@link EnemyGrenade}) toward the operative's position. The grenades detonate after exactly 2.0 seconds,
 * dealing heavy area-of-effect explosive damage and destroying surrounding cover, forcing the player to stay mobile.
 */
public class GrenadierEnemy extends TargetDummy {

    private double speed;
    private double minRange;
    private double maxRange;
    private double grenadeCooldown;
    private double grenadeTimer = 1.0;
    private int grenadeDamage;
    private double blastRadius;
    private double facingAngle = 0.0;

    // Tactical strafing & throw animation
    private double strafeTimer;
    private int strafeDirection; // 1: right / clockwise, -1: left / counter-clockwise
    private double throwAnimTimer = 0.0;

    /**
     * Constructs a standard grenadier with default attributes.
     *
     * @param x         Starting world X coordinate.
     * @param y         Starting world Y coordinate.
     * @param maxHealth Hit points.
     */
    public GrenadierEnemy(double x, double y, int maxHealth) {
        this(x, y, maxHealth, 95.0, 24, 78.0, 3.0, 175.0, 400.0, "Heavy Grenadier [Demolitions]");
    }

    /**
     * Constructs a customizable grenadier with explicit combat parameters.
     *
     * @param x               Starting world X coordinate.
     * @param y               Starting world Y coordinate.
     * @param maxHealth       Hit points.
     * @param speed           Movement speed in pixels per second.
     * @param grenadeDamage   Explosive damage dealt by grenade detonation.
     * @param blastRadius     Explosion area radius.
     * @param grenadeCooldown Frequency in seconds between successive grenade throws.
     * @param minRange        Distance threshold below which enemy retreats (kiting).
     * @param maxRange        Distance threshold above which enemy advances to close the gap.
     * @param name            Tactical display name.
     */
    public GrenadierEnemy(double x, double y, int maxHealth, double speed, int grenadeDamage,
                          double blastRadius, double grenadeCooldown, double minRange, double maxRange,
                          String name) {
        super(x, y, maxHealth, 30.0, false, name);
        this.speed = speed;
        this.grenadeDamage = grenadeDamage;
        this.blastRadius = blastRadius;
        this.grenadeCooldown = grenadeCooldown;
        this.grenadeTimer = 1.0 + ThreadLocalRandom.current().nextDouble() * 0.8;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.strafeTimer = 1.2 + ThreadLocalRandom.current().nextDouble() * 1.0;
        this.strafeDirection = ThreadLocalRandom.current().nextBoolean() ? 1 : -1;
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

        if (throwAnimTimer > 0) {
            throwAnimTimer -= deltaTime;
            if (throwAnimTimer < 0) throwAnimTimer = 0.0;
        }

        if (isSpawning()) {
            return;
        }

        double dx = playerCenterX - x;
        double dy = playerCenterY - y;
        double dist = Math.hypot(dx, dy);

        if (dist > 0.001) {
            facingAngle = Math.atan2(dy, dx);
        }

        // --- 1. TACTICAL MEDIUM-RANGE REPOSITIONING & STRAFING ---
        strafeTimer -= deltaTime;
        if (strafeTimer <= 0) {
            strafeTimer = 1.2 + ThreadLocalRandom.current().nextDouble() * 1.5;
            strafeDirection = ThreadLocalRandom.current().nextBoolean() ? 1 : -1;
        }

        double moveVx = 0.0;
        double moveVy = 0.0;

        if (dist < minRange) {
            // Player is too close: Backpedal away to maintain standoff grenade distance
            if (dist > 0.001) {
                moveVx = -(dx / dist) * speed;
                moveVy = -(dy / dist) * speed;
            }
        } else if (dist > maxRange) {
            // Player is too far: Advance to bring operative within grenade lob range
            if (dist > 0.001) {
                moveVx = (dx / dist) * speed;
                moveVy = (dy / dist) * speed;
            }
        } else {
            // In comfortable standoff range: Strafe perpendicular to player sightline
            double perpX = -dy / dist * strafeDirection;
            double perpY = dx / dist * strafeDirection;
            moveVx = perpX * (speed * 0.75);
            moveVy = perpY * (speed * 0.75);
        }

        moveWithCollision(moveVx * deltaTime, moveVy * deltaTime, dungeonMap);

        // --- 2. ORDNANCE DISCHARGE (GRENADE LOBBING) ---
        // WHY: Forces player out of cover by placing 2.0s ticking hazard zones at their position
        if (grenadeTimer > 0) {
            grenadeTimer -= deltaTime;
        }

        if (grenadeTimer <= 0 && projectileManager != null) {
            // Check if player is within effective throwing envelope [100px - 460px]
            if (dist >= 90.0 && dist <= 460.0) {
                // Throw grenade directly toward player operative position
                EnemyGrenade grenade = new EnemyGrenade(x, y, playerCenterX, playerCenterY, grenadeDamage, blastRadius);
                projectileManager.spawn(grenade);

                throwAnimTimer = 0.35; // Trigger arm throw pose
                grenadeTimer = grenadeCooldown + ThreadLocalRandom.current().nextDouble(-0.35, 0.45);
            }
        }
    }

    public double getSpeed() {
        return speed;
    }

    public double getGrenadeCooldown() {
        return grenadeCooldown;
    }

    public int getGrenadeDamage() {
        return grenadeDamage;
    }

    public double getBlastRadius() {
        return blastRadius;
    }

    public double getGrenadeTimer() {
        return grenadeTimer;
    }

    public void setGrenadeTimer(double timer) {
        this.grenadeTimer = timer;
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

        // 1. Shadow
        g2.setColor(new Color(0, 0, 0, 95));
        g2.fillOval(screenCX - half, screenCY + 2, (int) size, 12);

        // 2. Thử nạp và hiển thị Sprite Lính Ném Lựu (Heavy Grenadier) xoay theo hướng đối mặt
        BufferedImage sprite = GameSpriteManager.getEnemyGrenadierSprite();
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

            // Tư thế vung tay ném lựu đạn khi kích hoạt kỹ năng
            if (throwAnimTimer > 0) {
                int armLen = 14;
                int armEndX = (int) (screenCX + Math.cos(facingAngle) * armLen);
                int armEndY = (int) (screenCY + Math.sin(facingAngle) * armLen);
                g2.setColor(new Color(255, 140, 30));
                g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(screenCX, screenCY, armEndX, armEndY);
            }
        } else {
            // --- Dự phòng Vector Fallback ---
            // 2. Chassis body
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
                // Heavy ordnance matte armor: Charcoal slate
                g2.setColor(new Color(55, 65, 75));
                g2.fillRoundRect(bx, by, bSize, bSize, 8, 8);

                // Heavy explosive bandolier belt diagonally across chest
                g2.setColor(new Color(30, 35, 40));
                g2.fillRect(bx + 4, by + 4, bSize - 8, 5);

                // 3 Grenade canisters on chest bandolier
                g2.setColor(new Color(255, 120, 20));
                g2.fillRoundRect(bx + 6, by + 4, 4, 5, 2, 2);
                g2.fillRoundRect(bx + 13, by + 4, 4, 5, 2, 2);
                g2.fillRoundRect(bx + 20, by + 4, 4, 5, 2, 2);

                // Ordnance hazard orange trims
                g2.setColor(new Color(255, 130, 30));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(bx, by, bSize, bSize, 8, 8);
            }

            // Arm throw indicator pose
            if (throwAnimTimer > 0) {
                int armLen = 14;
                int armEndX = (int) (screenCX + Math.cos(facingAngle) * armLen);
                int armEndY = (int) (screenCY + Math.sin(facingAngle) * armLen);
                g2.setColor(new Color(255, 140, 30));
                g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(screenCX, screenCY, armEndX, armEndY);
            }

            // Glowing orange demolition optic eye
            g2.setColor(new Color(255, 175, 40));
            g2.fillOval(screenCX - 4, screenCY - 4, 8, 8);
            g2.setColor(new Color(255, 245, 160));
            g2.fillOval(screenCX - 2, screenCY - 2, 4, 4);
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
