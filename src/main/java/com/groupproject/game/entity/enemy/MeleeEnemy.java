package com.groupproject.game.entity.enemy;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.dungeon.Tile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.state.PlayingState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * Autonomous melee combat operative / cybernetic drone that actively pursues the player.
 *
 * <p>WHY: Provides dynamic close-quarters threat pressure. Unlike stationary target dummies,
 * {@code MeleeEnemy} continuously navigates towards the player's world coordinates, slides along
 * solid terrain without clipping through walls, and deals contact melee damage with an internal
 * cooldown timer to ensure balanced pacing.
 */
public class MeleeEnemy extends TargetDummy {

    private double speed;
    private int meleeDamage;
    private double attackCooldown;
    private double attackTimer = 0.0;
    private double attackAnimTimer = 0.0;
    private double facingAngle = 0.0;

    // Navigation & obstacle avoidance state
    private double pathTimer = 0.0;
    private int[] nextPathTile = null;

    /**
     * Constructs a standard melee chaser enemy with default combat parameters.
     *
     * @param x         Starting world X coordinate.
     * @param y         Starting world Y coordinate.
     * @param maxHealth Hit points.
     */
    public MeleeEnemy(double x, double y, int maxHealth) {
        this(x, y, maxHealth, 125.0, 12, 0.9);
    }

    /**
     * Constructs a customized melee chaser enemy with configurable speed, damage, and attack rate.
     *
     * @param x              Starting world X coordinate.
     * @param y              Starting world Y coordinate.
     * @param maxHealth      Hit points.
     * @param speed          Movement speed in pixels per second.
     * @param meleeDamage    Damage dealt upon striking the operative.
     * @param attackCooldown Cooldown in seconds between successive strikes.
     */
    public MeleeEnemy(double x, double y, int maxHealth, double speed, int meleeDamage, double attackCooldown) {
        this(x, y, maxHealth, speed, meleeDamage, attackCooldown, "Cyber Swarmer [Melee]");
    }

    /**
     * Constructs a customized melee chaser enemy with configurable speed, damage, attack rate, and display name.
     *
     * @param x              Starting world X coordinate.
     * @param y              Starting world Y coordinate.
     * @param maxHealth      Hit points.
     * @param speed          Movement speed in pixels per second.
     * @param meleeDamage    Damage dealt upon striking the operative.
     * @param attackCooldown Cooldown in seconds between successive strikes.
     * @param name           Display name reflecting tactical tier (e.g. MK-2, MK-3).
     */
    public MeleeEnemy(double x, double y, int maxHealth, double speed, int meleeDamage, double attackCooldown, String name) {
        super(x, y, maxHealth, 30.0, false, name);
        this.speed = speed;
        this.meleeDamage = meleeDamage;
        this.attackCooldown = attackCooldown;
    }

    /**
     * Finds the next walkable grid tile towards the player when direct line-of-sight is obstructed by cover or walls.
     * Uses lightweight Breadth-First Search (BFS) bounded tightly around the combat area.
     */
    private int[] findNextTileBFS(DungeonMap map, int startCol, int startRow, int targetCol, int targetRow) {
        if (startCol == targetCol && startRow == targetRow) {
            return new int[]{targetCol, targetRow};
        }

        int minCol = Math.max(0, Math.min(startCol, targetCol) - 6);
        int maxCol = Math.min(DungeonMap.COLS - 1, Math.max(startCol, targetCol) + 6);
        int minRow = Math.max(0, Math.min(startRow, targetRow) - 6);
        int maxRow = Math.min(DungeonMap.ROWS - 1, Math.max(startRow, targetRow) + 6);

        int cols = maxCol - minCol + 1;
        int rows = maxRow - minRow + 1;
        int total = cols * rows;

        int[] parent = new int[total];
        Arrays.fill(parent, -1);

        int startIndex = (startCol - minCol) * rows + (startRow - minRow);
        int targetIndex = (targetCol - minCol) * rows + (targetRow - minRow);

        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(startIndex);
        parent[startIndex] = startIndex;

        int[] dc = {0, 0, 1, -1};
        int[] dr = {1, -1, 0, 0};

        boolean found = false;
        while (!queue.isEmpty()) {
            int curr = queue.poll();
            if (curr == targetIndex) {
                found = true;
                break;
            }

            int currC = minCol + (curr / rows);
            int currR = minRow + (curr % rows);

            for (int i = 0; i < 4; i++) {
                int nc = currC + dc[i];
                int nr = currR + dr[i];

                if (nc >= minCol && nc <= maxCol && nr >= minRow && nr <= maxRow) {
                    Tile tile = map.getTile(nc, nr);
                    if (tile != null && !tile.isSolid()) {
                        int nIndex = (nc - minCol) * rows + (nr - minRow);
                        if (parent[nIndex] == -1) {
                            parent[nIndex] = curr;
                            queue.add(nIndex);
                        }
                    }
                }
            }
        }

        if (!found) {
            return new int[]{targetCol, targetRow};
        }

        // Trace back from targetIndex to find the immediate next step after startIndex
        int step = targetIndex;
        while (parent[step] != startIndex && parent[step] != step) {
            step = parent[step];
        }

        int nextC = minCol + (step / rows);
        int nextR = minRow + (step % rows);
        return new int[]{nextC, nextR};
    }

    @Override
    public void updateAI(double deltaTime, double playerCenterX, double playerCenterY, double playerSize,
                         DungeonMap dungeonMap, ProjectileManager projectileManager, PlayingState playingState) {
        if (!alive) return;

        // Base animation & hit flash update, along with directional knockback physics
        update(deltaTime);
        updateKnockback(deltaTime, dungeonMap);

        if (attackTimer > 0) {
            attackTimer -= deltaTime;
        }
        if (attackAnimTimer > 0) {
            attackAnimTimer -= deltaTime;
        }

        // While in spawn telegraph materialization period, hold position and do not strike
        if (isSpawning()) {
            return;
        }

        // Vector to player
        double dx = playerCenterX - x;
        double dy = playerCenterY - y;
        double dist = Math.hypot(dx, dy);

        if (dist > 0.001) {
            facingAngle = Math.atan2(dy, dx);
        }

        // Determine navigation target point (direct pursuit if LOS clear, or waypoint around obstacles if obstructed)
        double navX = playerCenterX;
        double navY = playerCenterY;

        boolean hasLOS = (dungeonMap == null) || dungeonMap.hasLineOfSight(x, y, playerCenterX, playerCenterY);
        if (!hasLOS && dungeonMap != null) {
            pathTimer -= deltaTime;
            int currentTileCol = (int) Math.floor(x / GameConfig.TILE_SIZE);
            int currentTileRow = (int) Math.floor(y / GameConfig.TILE_SIZE);
            int playerTileCol = (int) Math.floor(playerCenterX / GameConfig.TILE_SIZE);
            int playerTileRow = (int) Math.floor(playerCenterY / GameConfig.TILE_SIZE);

            if (pathTimer <= 0 || nextPathTile == null
                    || (currentTileCol == nextPathTile[0] && currentTileRow == nextPathTile[1])) {
                nextPathTile = findNextTileBFS(dungeonMap, currentTileCol, currentTileRow, playerTileCol, playerTileRow);
                pathTimer = 0.15; // Re-calculate path every 150ms
            }

            if (nextPathTile != null) {
                navX = (nextPathTile[0] + 0.5) * GameConfig.TILE_SIZE;
                navY = (nextPathTile[1] + 0.5) * GameConfig.TILE_SIZE;
            }
        } else {
            nextPathTile = null;
            pathTimer = 0.0;
        }

        double toNavX = navX - x;
        double toNavY = navY - y;
        double navDist = Math.hypot(toNavX, toNavY);

        // Locomotion: Chase operative until within physical contact distance
        double minDistance = (size / 2.0) + (playerSize / 2.0) - 2.0;
        if (dist > minDistance) {
            double desiredAngle = (navDist > 0.001) ? Math.atan2(toNavY, toNavX) : facingAngle;
            double stepDist = speed * deltaTime;
            double half = size / 2.0;

            // Multi-angle obstacle steering feelers: try straight, then +/- 25°, +/- 50°, +/- 75°, +/- 90°, +/- 115°
            double[] feelerOffsets = {0.0, 0.44, -0.44, 0.87, -0.87, 1.31, -1.31, 1.57, -1.57, 2.0, -2.0};
            boolean moved = false;

            for (double offset : feelerOffsets) {
                double testAngle = desiredAngle + offset;
                double stepX = Math.cos(testAngle) * stepDist;
                double stepY = Math.sin(testAngle) * stepDist;

                if (dungeonMap == null || dungeonMap.isAreaWalkable(x + stepX - half, y + stepY - half, size, size)) {
                    x += stepX;
                    y += stepY;
                    moved = true;
                    break;
                }
            }

            // Fallback: If diagonal test failed, try decoupled axis sliding along desired angle
            if (!moved) {
                double stepX = Math.cos(desiredAngle) * stepDist;
                double stepY = Math.sin(desiredAngle) * stepDist;

                if (dungeonMap == null || dungeonMap.isAreaWalkable(x + stepX - half, y - half, size, size)) {
                    x += stepX;
                }
                if (dungeonMap == null || dungeonMap.isAreaWalkable(x - half, y + stepY - half, size, size)) {
                    y += stepY;
                }
            }
        }

        // Melee Strike Check: Execute strike if within physical reach
        double strikeReach = (size / 2.0) + (playerSize / 2.0) + 8.0;
        if (dist <= strikeReach && attackTimer <= 0) {
            if (playingState != null) {
                playingState.damagePlayer(meleeDamage);
            }
            attackTimer = attackCooldown;
            attackAnimTimer = 0.22; // Trigger kinetic claw slash arc visual
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

        // 2. Chassis body (Aggressive spiky cyber-swarmer)
        if (hitFlashTimer > 0) {
            g.setColor(Color.WHITE);
        } else {
            g.setColor(new Color(190, 35, 45)); // Crimson chassis
        }
        g.fillRoundRect(screenCX - half, screenCY - half, (int) size, (int) size, 8, 8);
        g.setColor(new Color(255, 90, 90));
        g.drawRoundRect(screenCX - half, screenCY - half, (int) size, (int) size, 8, 8);

        // 3. Forward kinetic mandibles / blades pointing along facingAngle
        int bladeLen = 14;
        int bx1 = (int) (screenCX + Math.cos(facingAngle - 0.4) * (half + bladeLen));
        int by1 = (int) (screenCY + Math.sin(facingAngle - 0.4) * (half + bladeLen));
        int bx2 = (int) (screenCX + Math.cos(facingAngle + 0.4) * (half + bladeLen));
        int by2 = (int) (screenCY + Math.sin(facingAngle + 0.4) * (half + bladeLen));

        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(2.5f));
        g.setColor(hitFlashTimer > 0 ? Color.WHITE : new Color(255, 180, 50));
        g.drawLine(screenCX, screenCY, bx1, by1);
        g.drawLine(screenCX, screenCY, bx2, by2);
        g.setStroke(oldStroke);

        // 4. Predatory glowing amber optic sensor
        g.setColor(new Color(255, 220, 40));
        int eyeX = (int) (screenCX + Math.cos(facingAngle) * 4);
        int eyeY = (int) (screenCY + Math.sin(facingAngle) * 4);
        g.fillOval(eyeX - 4, eyeY - 4, 8, 8);

        // 5. Kinetic claw slash arc effect when active strike occurs
        if (attackAnimTimer > 0) {
            g.setStroke(new BasicStroke(3.0f));
            g.setColor(new Color(255, 60, 60, 220));
            int arcRadius = (int) (half + 16);
            int startAngle = (int) Math.toDegrees(-facingAngle) - 45;
            g.drawArc(screenCX - arcRadius, screenCY - arcRadius, arcRadius * 2, arcRadius * 2, startAngle, 90);
            g.setStroke(oldStroke);
        }

        // 6. Overhead Health Bar
        int barW = 34;
        int barH = 5;
        int barX = screenCX - barW / 2;
        int barY = screenCY - half - 10;

        g.setColor(new Color(30, 30, 40));
        g.fillRect(barX, barY, barW, barH);
        float hpPercent = Math.max(0f, (float) currentHealth / maxHealth);
        g.setColor(hpPercent > 0.4f ? new Color(255, 80, 80) : new Color(220, 20, 20));
        g.fillRect(barX, barY, (int) (barW * hpPercent), barH);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(barX, barY, barW, barH);
    }

    public double getSpeed() {
        return speed;
    }

    public int getMeleeDamage() {
        return meleeDamage;
    }

    public double getAttackTimer() {
        return attackTimer;
    }

    public void setAttackTimer(double timer) {
        this.attackTimer = Math.max(0.0, timer);
    }

    public double getAttackCooldown() {
        return attackCooldown;
    }
}
