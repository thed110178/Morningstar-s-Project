package com.groupproject.game.dungeon;

import com.groupproject.game.core.GameConfig;
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
        this.type = type;
    }

    /**
     * Checks if this tile blocks movement and projectile trajectories.
     */
    public boolean isSolid() {
        return type.isSolid();
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
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

        // Subtle geometric outline styling
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
