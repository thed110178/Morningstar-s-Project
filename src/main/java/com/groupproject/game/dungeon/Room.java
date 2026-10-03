package com.groupproject.game.dungeon;

import com.groupproject.game.core.GameConfig;
import java.awt.Rectangle;

/**
 * Represents a discrete rectangular chamber within the dungeon layout.
 *
 * <p>WHY: Breaking the dungeon into distinct rooms enables game designers and algorithms
 * to categorize play spaces (e.g. peaceful armory, high-intensity combat arena, extraction point).
 * It also simplifies enemy wave spawning logic (spawning only when the player enters a room)
 * and spatial door locking mechanisms.
 */
public class Room {

    /**
     * Functional role of the room.
     */
    public enum RoomType {
        SPAWN,
        COMBAT,
        EXTRACT
    }

    private final int gridX;
    private final int gridY;
    private final int widthInTiles;
    private final int heightInTiles;
    private final RoomType roomType;
    private final String name;

    /**
     * Constructs a chamber with discrete grid dimensions and a functional role.
     *
     * @param gridX         Leftmost column on the map grid.
     * @param gridY         Topmost row on the map grid.
     * @param widthInTiles  Width spanning across columns.
     * @param heightInTiles Height spanning across rows.
     * @param roomType      Category defining gameplay behavior (Spawn, Combat, Extract).
     * @param name          Descriptive room title.
     */
    public Room(int gridX, int gridY, int widthInTiles, int heightInTiles, RoomType roomType, String name) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.widthInTiles = widthInTiles;
        this.heightInTiles = heightInTiles;
        this.roomType = roomType;
        this.name = name;
    }

    /**
     * Returns the horizontal center of the room in world pixel coordinates.
     * Ideal for placing player spawn points, treasure chests, or boss entities.
     */
    public double getCenterWorldX() {
        return (gridX + widthInTiles / 2.0) * GameConfig.TILE_SIZE;
    }

    /**
     * Returns the vertical center of the room in world pixel coordinates.
     */
    public double getCenterWorldY() {
        return (gridY + heightInTiles / 2.0) * GameConfig.TILE_SIZE;
    }

    /**
     * Checks if a world coordinate (X, Y) falls within the interior bounds of this room.
     */
    public boolean containsWorldPoint(double worldX, double worldY) {
        double minX = gridX * GameConfig.TILE_SIZE;
        double minY = gridY * GameConfig.TILE_SIZE;
        double maxX = (gridX + widthInTiles) * GameConfig.TILE_SIZE;
        double maxY = (gridY + heightInTiles) * GameConfig.TILE_SIZE;

        return worldX >= minX && worldX <= maxX && worldY >= minY && worldY <= maxY;
    }

    /**
     * Returns a rectangle bounding the room in world pixel coordinates.
     */
    public Rectangle getWorldBounds() {
        return new Rectangle(
                gridX * GameConfig.TILE_SIZE,
                gridY * GameConfig.TILE_SIZE,
                widthInTiles * GameConfig.TILE_SIZE,
                heightInTiles * GameConfig.TILE_SIZE
        );
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public int getWidthInTiles() {
        return widthInTiles;
    }

    public int getHeightInTiles() {
        return heightInTiles;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public String getName() {
        return name;
    }
}
