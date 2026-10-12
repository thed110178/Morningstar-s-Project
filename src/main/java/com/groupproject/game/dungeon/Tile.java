package com.groupproject.game.dungeon;

import com.groupproject.game.core.GameConfig;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Represents an individual square terrain unit positioned on the discrete dungeon grid.
 *
 * <p>WHY: Encapsulating grid coordinates, world-space bounds, and tile classifications into
 * a dedicated object ensures the physics system can rapidly query whether an entity's hitbox
 * intersects impassable geography without performing expensive arbitrary polygon checks.
 */
public class Tile {

    private final int gridX;
    private final int gridY;
    private final int worldX;
    private final int worldY;
    private TileType type;
    private int health = 0;
    private int maxHealth = 0;

    /**
     * Constructs a tile at the designated grid coordinates.
     *
     * @param gridX Horizontal column index on the map grid.
     * @param gridY Vertical row index on the map grid.
     * @param type  The classification determining collision behavior and visual style.
     */
    public Tile(int gridX, int gridY, TileType type) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.worldX = gridX * GameConfig.TILE_SIZE;
        this.worldY = gridY * GameConfig.TILE_SIZE;
        setType(type);
    }

    /**
     * Checks if this tile blocks movement (physical obstruction).
     */
    public boolean isSolid() {
        return type.isSolid();
    }

    /**
     * Checks if this tile blocks or absorbs projectile trajectories.
     *
     * <p>WHY: Walls, cover barriers, and closed doors block projectiles physically.
     * Open doors (DOOR_OPEN) also absorb bullets to prevent operatives from cheesing
     * combat encounters by eliminating hostiles from the outside corridor before entering.
     */
    public boolean blocksProjectiles() {
        return isSolid() || type == TileType.DOOR_OPEN;
    }

    public int getGridX() {
        return gridX;
    }

    public int getCol() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public int getRow() {
        return gridY;
    }

    public int getWorldX() {
        return worldX;
    }

    public int getWorldY() {
        return worldY;
    }

    public TileType getType() {
        return type;
    }

    public void setType(TileType type) {
        this.type = type;
        if (type == TileType.DESTRUCTIBLE_COVER) {
            this.maxHealth = 40;
            this.health = 40;
        } else {
            this.health = 0;
            this.maxHealth = 0;
        }
    }

    /**
     * Xác định xem ô này có phải là cover tạm thời có thể phá hủy được hay không.
     */
    public boolean isDestructible() {
        return type == TileType.DESTRUCTIBLE_COVER;
    }

    /**
     * Gây sát thương lên ô cover tạm thời. Khi máu giảm về 0 hoặc thấp hơn,
     * ô này sẽ bị phá hủy và tự động chuyển hóa thành ô sàn thông thường (FLOOR).
     *
     * @param damage Lượng sát thương từ đạn bắn hoặc nhát chém cận chiến.
     * @return true nếu ô cover đã bị phá hủy hoàn toàn.
     */
    public boolean takeDamage(int damage) {
        if (!isDestructible() || damage <= 0) {
            return false;
        }
        this.health -= damage;
        if (this.health <= 0) {
            setType(TileType.FLOOR);
            return true;
        }
        return false;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Renders the tile surface at the given screen-space coordinates.
     *
     * <p>WHY: Decouples tile drawing from absolute world coordinates by accepting pre-calculated
     * screen coordinates from the camera offset, supporting efficient viewport culling.
     *
     * @param g       2D Graphics context.
     * @param screenX Pixel X position on the canvas.
     * @param screenY Pixel Y position on the canvas.
     */
    public void render(Graphics2D g, int screenX, int screenY) {
        int size = GameConfig.TILE_SIZE;

        // Base surface fill
        g.setColor(type.getDefaultColor());
        g.fillRect(screenX, screenY, size, size);

        // Visual ornamentation based on tile classification
        if (type == TileType.WALL) {
            // Beveled 3D top-down wall border
            g.setColor(new Color(65, 75, 100));
            g.drawRect(screenX, screenY, size - 1, size - 1);
            g.setColor(new Color(30, 35, 48));
            g.fillRect(screenX + 4, screenY + 4, size - 8, size - 8);
        } else if (type == TileType.COVER) {
            // Tactical cover block with cross-hatch pattern
            g.setColor(new Color(90, 105, 135));
            g.drawRect(screenX + 2, screenY + 2, size - 5, size - 5);
            g.drawLine(screenX + 4, screenY + 4, screenX + size - 5, screenY + size - 5);
            g.drawLine(screenX + size - 5, screenY + 4, screenX + 4, screenY + size - 5);
        } else if (type == TileType.DESTRUCTIBLE_COVER) {
            // Thùng gỗ che chắn chiến thuật phá hủy được (Tactical Wooden Crate)
            g.setColor(new Color(85, 55, 30));
            g.drawRect(screenX + 1, screenY + 1, size - 3, size - 3);

            // Nan gỗ chéo chữ X gia cố
            g.setColor(new Color(105, 75, 40));
            g.drawLine(screenX + 4, screenY + 4, screenX + size - 5, screenY + size - 5);
            g.drawLine(screenX + size - 5, screenY + 4, screenX + 4, screenY + size - 5);

            // 4 Nẹp kim loại ở các góc
            g.setColor(new Color(70, 75, 85));
            int bSize = 6;
            g.fillRect(screenX + 2, screenY + 2, bSize, bSize);
            g.fillRect(screenX + size - 2 - bSize, screenY + 2, bSize, bSize);
            g.fillRect(screenX + 2, screenY + size - 2 - bSize, bSize, bSize);
            g.fillRect(screenX + size - 2 - bSize, screenY + size - 2 - bSize, bSize, bSize);

            // Hiệu ứng rạn nứt khi bị nhận sát thương
            if (health < maxHealth) {
                g.setColor(new Color(25, 15, 10, 220));
                g.drawLine(screenX + 14, screenY + 8, screenX + 26, screenY + 24);
                g.drawLine(screenX + 26, screenY + 24, screenX + 20, screenY + 38);
                if (health <= maxHealth / 2) {
                    g.drawLine(screenX + 34, screenY + 12, screenX + 28, screenY + 28);
                    g.drawLine(screenX + 10, screenY + 30, screenX + 22, screenY + 32);
                }

                // Thanh HP nhỏ màu vàng cam phía trên đỉnh thùng
                int barW = 30;
                int barH = 4;
                int barX = screenX + (size - barW) / 2;
                int barY = screenY + 4;
                g.setColor(new Color(0, 0, 0, 180));
                g.fillRect(barX, barY, barW, barH);
                int fillW = (int) Math.round((barW * health) / (double) maxHealth);
                g.setColor(new Color(255, 160, 40));
                g.fillRect(barX, barY, fillW, barH);
                g.setColor(new Color(255, 255, 255, 140));
                g.drawRect(barX, barY, barW, barH);
            }
        } else if (type == TileType.DOOR_CLOSED) {
            // Heavy lockdown blast gate with bright red warning barrier
            g.setColor(new Color(255, 60, 60));
            g.drawRect(screenX + 2, screenY + 2, size - 5, size - 5);
            g.setStroke(new BasicStroke(2.5f));
            g.drawLine(screenX + 6, screenY + size / 2, screenX + size - 6, screenY + size / 2);
            g.drawLine(screenX + size / 2, screenY + 6, screenX + size / 2, screenY + size - 6);
            g.setStroke(new BasicStroke(1.0f));
            // Warning beacon dot
            g.setColor(new Color(255, 220, 50));
            g.fillOval(screenX + size / 2 - 3, screenY + size / 2 - 3, 6, 6);
        } else if (type == TileType.DOOR_OPEN) {
            // Unlocked green transition threshold
            g.setColor(new Color(50, 220, 120, 140));
            g.drawRect(screenX + 2, screenY + 2, size - 5, size - 5);
            g.drawLine(screenX + 6, screenY + 8, screenX + size - 6, screenY + 8);
            g.drawLine(screenX + 6, screenY + size - 8, screenX + size - 6, screenY + size - 8);
        } else if (type == TileType.SPAWN_ZONE) {
            // Glowing cyan deployment grid pattern
            g.setColor(new Color(50, 160, 120, 80));
            g.drawRect(screenX + 2, screenY + 2, size - 5, size - 5);
        } else if (type == TileType.EXTRACT_ZONE) {
            // Glowing purple extraction portal pattern
            g.setColor(new Color(160, 80, 220, 90));
            g.drawRect(screenX + 2, screenY + 2, size - 5, size - 5);
            g.drawOval(screenX + 8, screenY + 8, size - 16, size - 16);
        } else if (type == TileType.FLOOR) {
            // Subtle floor tile separation line
            g.setColor(new Color(35, 42, 58));
            g.drawRect(screenX, screenY, size - 1, size - 1);
        }
    }
}
