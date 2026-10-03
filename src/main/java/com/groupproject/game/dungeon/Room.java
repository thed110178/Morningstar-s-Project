package com.groupproject.game.dungeon;

import com.groupproject.game.core.GameConfig;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Combat progression state of the room.
     */
    public enum RoomStatus {
        UNVISITED,
        ACTIVE_LOCKDOWN,
        CLEARED
    }

    private final int gridX;
    private final int gridY;
    private final int widthInTiles;
    private final int heightInTiles;
    private final RoomType roomType;
    private final String name;

    private RoomStatus status = RoomStatus.UNVISITED;
    private final List<Tile> doorTiles = new ArrayList<>();

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
     * Registers a door tile belonging to the entrance or exit of this room.
     */
    public void addDoorTile(Tile doorTile) {
        if (doorTile != null && !doorTiles.contains(doorTile)) {
            doorTiles.add(doorTile);
        }
    }

    /**
     * Engages combat lockdown by closing and solidifying all entrance/exit blast gates.
     */
    public void lockdown() {
        if (status == RoomStatus.CLEARED) return; // Do not relock cleared rooms

        this.status = RoomStatus.ACTIVE_LOCKDOWN;
        for (Tile door : doorTiles) {
            door.setType(TileType.DOOR_CLOSED);
        }
    }

    /**
     * Disengages lockdown once all hostiles are defeated, opening all blast gates.
     */
    public void unlock() {
        this.status = RoomStatus.CLEARED;
        for (Tile door : doorTiles) {
            door.setType(TileType.DOOR_OPEN);
        }
    }

    /**
     * Resets the room lifecycle status to UNVISITED and re-opens all registered blast doors.
     *
     * <p>WHY: When starting a new mission or restarting the gameplay loop via
     * {@link com.groupproject.game.state.PlayingState#init()}, rooms must return to their
     * pristine state so that subsequent lockdown encounters trigger reliably.
     */
    public void reset() {
        this.status = RoomStatus.UNVISITED;
        for (Tile door : doorTiles) {
            door.setType(TileType.DOOR_OPEN);
        }
    }

    public boolean isLocked() {
        return status == RoomStatus.ACTIVE_LOCKDOWN;
    }

    public boolean isCleared() {
        return status == RoomStatus.CLEARED;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public List<Tile> getDoorTiles() {
        return doorTiles;
    }

    /**
     * Returns the horizontal center of the room in world pixel coordinates.
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
     * Determines whether an entity's center point has entered the room's interior floor area
     * beyond the perimeter doorway threshold.
     *
     * <p>WHY: Used by PlayingState to detect when an operative has crossed into the combat arena.
     * Checking against the interior floor boundaries (1 tile inward from outer perimeter walls/doors)
     * ensures detection triggers reliably as soon as the operative steps into the chamber.
     *
     * @param x      Entity top-left X in world space.
     * @param y      Entity top-left Y in world space.
     * @param width  Entity hitbox width in pixels.
     * @param height Entity hitbox height in pixels.
     * @return true if the entity's center point is situated within the interior floor bounds.
     */
    public boolean hasEntityEntered(double x, double y, double width, double height) {
        double centerX = x + (width / 2.0);
        double centerY = y + (height / 2.0);

        double minInteriorX = (gridX + 1.0) * GameConfig.TILE_SIZE;
        double maxInteriorX = (gridX + widthInTiles - 1.0) * GameConfig.TILE_SIZE;
        double minInteriorY = (gridY + 1.0) * GameConfig.TILE_SIZE;
        double maxInteriorY = (gridY + heightInTiles - 1.0) * GameConfig.TILE_SIZE;

        return centerX >= minInteriorX && centerX <= maxInteriorX
                && centerY >= minInteriorY && centerY <= maxInteriorY;
    }

    /**
     * Checks if a world coordinate (X, Y) is inside the interior floor region (excluding perimeter walls).
     */
    public boolean containsWorldPointInterior(double worldX, double worldY) {
        double minX = (gridX + 1.0) * GameConfig.TILE_SIZE;
        double minY = (gridY + 1.0) * GameConfig.TILE_SIZE;
        double maxX = (gridX + widthInTiles - 1.0) * GameConfig.TILE_SIZE;
        double maxY = (gridY + heightInTiles - 1.0) * GameConfig.TILE_SIZE;

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
