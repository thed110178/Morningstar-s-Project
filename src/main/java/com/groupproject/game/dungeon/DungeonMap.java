package com.groupproject.game.dungeon;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the discrete 2D grid matrix of terrain tiles, collision checks, rooms, and lockdown doors.
 *
 * <p>WHY: Centralizes map generation, spatial queries, and door state transitions. Implementing
 * dynamic door tiles allows combat chambers to seal the operative inside during hostile encounters
 * and unlock once all threats are eliminated, authentic to the Soul Knight gameplay loop.
 */
public class DungeonMap {

    public static final int COLS = GameConfig.WORLD_WIDTH / GameConfig.TILE_SIZE; // 80 columns
    public static final int ROWS = GameConfig.WORLD_HEIGHT / GameConfig.TILE_SIZE; // 45 rows

    private final Tile[][] tiles;
    private final List<Room> rooms = new ArrayList<>();

    private Room spawnRoom;
    private Room combatRoom;
    private Room extractRoom;

    /**
     * Constructs the dungeon matrix and builds the 3-room layout (Spawn, Combat, Extract) with blast doors.
     */
    public DungeonMap() {
        this.tiles = new Tile[COLS][ROWS];
        initializeEmptyWorld();
        buildThreeRoomLayout();
    }

    /**
     * Initializes all grid slots to impassable Void terrain by default.
     */
    private void initializeEmptyWorld() {
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS; r++) {
                tiles[c][r] = new Tile(c, r, TileType.VOID);
            }
        }
    }

    /**
     * Constructs a structured Roguelite 3-chamber facility connected by tactical corridors:
     * 1. Spawn Room (West staging ground)
     * 2. Combat Arena (Central chamber with blast doors and cover pillars)
     * 3. Extract Chamber (East evacuation portal)
     */
    private void buildThreeRoomLayout() {
        // --- 1. Spawn Room (Staging Area) ---
        spawnRoom = new Room(6, 16, 14, 12, Room.RoomType.SPAWN, "Deployment Staging Room");
        rooms.add(spawnRoom);
        carveRoom(spawnRoom);

        // Mark 4x4 central pad as SPAWN_ZONE
        int spawnPadStartX = spawnRoom.getGridX() + 5;
        int spawnPadStartY = spawnRoom.getGridY() + 4;
        for (int c = spawnPadStartX; c < spawnPadStartX + 4; c++) {
            for (int r = spawnPadStartY; r < spawnPadStartY + 4; r++) {
                tiles[c][r].setType(TileType.SPAWN_ZONE);
            }
        }

        // --- 2. Combat Room (Central Battle Arena) ---
        combatRoom = new Room(28, 11, 24, 22, Room.RoomType.COMBAT, "Central Combat Arena");
        rooms.add(combatRoom);
        carveRoom(combatRoom);

        // Add 4 tactical cover pillars inside the combat arena
        int cx = combatRoom.getGridX();
        int cy = combatRoom.getGridY();
        carvePillar(cx + 5, cy + 5, 2, 2);
        carvePillar(cx + 17, cy + 5, 2, 2);
        carvePillar(cx + 5, cy + 15, 2, 2);
        carvePillar(cx + 17, cy + 15, 2, 2);

        // --- 3. Extract Room (Evacuation Zone) ---
        extractRoom = new Room(60, 16, 14, 12, Room.RoomType.EXTRACT, "Evacuation Landing Zone");
        rooms.add(extractRoom);
        carveRoom(extractRoom);

        // Mark 4x4 central pad as EXTRACT_ZONE
        int extractPadStartX = extractRoom.getGridX() + 5;
        int extractPadStartY = extractRoom.getGridY() + 4;
        for (int c = extractPadStartX; c < extractPadStartX + 4; c++) {
            for (int r = extractPadStartY; r < extractPadStartY + 4; r++) {
                tiles[c][r].setType(TileType.EXTRACT_ZONE);
            }
        }

        // --- 4. Connecting Corridors ---
        // Corridor 1: Spawn -> Combat
        carveHorizontalCorridor(spawnRoom.getGridX() + spawnRoom.getWidthInTiles() - 1,
                combatRoom.getGridX(), 20, 4);

        // Corridor 2: Combat -> Extract
        carveHorizontalCorridor(combatRoom.getGridX() + combatRoom.getWidthInTiles() - 1,
                extractRoom.getGridX(), 20, 4);

        // --- 5. Install Combat Room Lockdown Doors ---
        // West Entrance Door (col = 28, rows 21 and 22)
        installDoorTile(combatRoom, combatRoom.getGridX(), 21);
        installDoorTile(combatRoom, combatRoom.getGridX(), 22);

        // East Exit Door (col = 51, rows 21 and 22)
        int eastDoorCol = combatRoom.getGridX() + combatRoom.getWidthInTiles() - 1;
        installDoorTile(combatRoom, eastDoorCol, 21);
        installDoorTile(combatRoom, eastDoorCol, 22);
    }

    /**
     * Installs an open door tile at the given grid coordinates and registers it with the room.
     */
    private void installDoorTile(Room room, int col, int row) {
        if (isValidGrid(col, row)) {
            Tile doorTile = tiles[col][row];
            doorTile.setType(TileType.DOOR_OPEN); // Default open until lockdown triggers
            room.addDoorTile(doorTile);
        }
    }

    /**
     * Carves a rectangular room by setting boundary walls and interior floor tiles.
     */
    private void carveRoom(Room room) {
        int x = room.getGridX();
        int y = room.getGridY();
        int w = room.getWidthInTiles();
        int h = room.getHeightInTiles();

        for (int c = x; c < x + w; c++) {
            for (int r = y; r < y + h; r++) {
                if (c == x || c == x + w - 1 || r == y || r == y + h - 1) {
                    tiles[c][r].setType(TileType.WALL);
                } else {
                    tiles[c][r].setType(TileType.FLOOR);
                }
            }
        }
    }

    /**
     * Carves a solid cover barrier within a room.
     */
    private void carvePillar(int startCol, int startRow, int width, int height) {
        for (int c = startCol; c < startCol + width; c++) {
            for (int r = startRow; r < startRow + height; r++) {
                if (isValidGrid(c, r)) {
                    tiles[c][r].setType(TileType.COVER);
                }
            }
        }
    }

    /**
     * Carves a horizontal corridor with top and bottom walls.
     */
    private void carveHorizontalCorridor(int startCol, int endCol, int startRow, int corridorHeight) {
        for (int c = startCol; c <= endCol; c++) {
            for (int r = startRow; r < startRow + corridorHeight; r++) {
                if (!isValidGrid(c, r)) continue;

                if (r == startRow || r == startRow + corridorHeight - 1) {
                    // Only place wall if not already an open floor from a room interior
                    if (tiles[c][r].getType() == TileType.VOID) {
                        tiles[c][r].setType(TileType.WALL);
                    }
                } else {
                    // Open pathway
                    tiles[c][r].setType(TileType.FLOOR);
                }
            }
        }
    }

    /**
     * Validates whether a grid index falls within the map matrix boundaries.
     */
    public boolean isValidGrid(int col, int row) {
        return col >= 0 && col < COLS && row >= 0 && row < ROWS;
    }

    /**
     * Returns the Tile object located at specific grid column and row.
     */
    public Tile getTile(int col, int row) {
        if (isValidGrid(col, row)) {
            return tiles[col][row];
        }
        return null;
    }

    /**
     * Performs an ultra-fast $O(1)$ spatial check to determine if an axis-aligned bounding box
     * (AABB) can move to the specified world position without colliding with walls, cover, or closed doors.
     */
    public boolean isAreaWalkable(double worldX, double worldY, double width, double height) {
        int minCol = (int) Math.floor(worldX / GameConfig.TILE_SIZE);
        int maxCol = (int) Math.floor((worldX + width - 0.001) / GameConfig.TILE_SIZE);
        int minRow = (int) Math.floor(worldY / GameConfig.TILE_SIZE);
        int maxRow = (int) Math.floor((worldY + height - 0.001) / GameConfig.TILE_SIZE);

        for (int c = minCol; c <= maxCol; c++) {
            for (int r = minRow; r <= maxRow; r++) {
                if (!isValidGrid(c, r)) {
                    // Outside world bounds is impassable
                    return false;
                }
                if (tiles[c][r].isSolid()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Renders visible tiles using Viewport Culling.
     */
    public void render(Graphics2D g, Camera camera) {
        int startCol = Math.max(0, (int) Math.floor(camera.getX() / GameConfig.TILE_SIZE));
        int endCol = Math.min(COLS - 1, (int) Math.ceil((camera.getX() + camera.getViewportWidth()) / GameConfig.TILE_SIZE));

        int startRow = Math.max(0, (int) Math.floor(camera.getY() / GameConfig.TILE_SIZE));
        int endRow = Math.min(ROWS - 1, (int) Math.ceil((camera.getY() + camera.getViewportHeight()) / GameConfig.TILE_SIZE));

        for (int c = startCol; c <= endCol; c++) {
            for (int r = startRow; r <= endRow; r++) {
                Tile tile = tiles[c][r];
                int screenX = camera.worldToScreenX(tile.getWorldX());
                int screenY = camera.worldToScreenY(tile.getWorldY());
                tile.render(g, screenX, screenY);
            }
        }

        // Render Room Label Indicators when camera views them
        renderRoomLabels(g, camera);
    }

    /**
     * Displays floating tactical room banners inside each chamber with lockdown warnings.
     */
    private void renderRoomLabels(Graphics2D g, Camera camera) {
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        for (Room room : rooms) {
            int screenX = camera.worldToScreenX(room.getCenterWorldX() - 110);
            int screenY = camera.worldToScreenY(room.getGridY() * GameConfig.TILE_SIZE + 28);

            if (screenX >= -180 && screenX <= camera.getViewportWidth() + 180
                    && screenY >= -50 && screenY <= camera.getViewportHeight() + 50) {

                if (room.getRoomType() == Room.RoomType.SPAWN) {
                    g.setColor(new Color(60, 220, 160, 200));
                    g.drawString("[ " + room.getName() + " ]", screenX, screenY);
                } else if (room.getRoomType() == Room.RoomType.COMBAT) {
                    if (room.isLocked()) {
                        g.setColor(new Color(255, 60, 60, 240));
                        g.drawString("[ ! COMBAT LOCKDOWN ! ]", screenX, screenY);
                    } else if (room.isCleared()) {
                        g.setColor(new Color(60, 220, 120, 200));
                        g.drawString("[ CLEARED - DOORS UNLOCKED ]", screenX, screenY);
                    } else {
                        g.setColor(new Color(255, 120, 60, 200));
                        g.drawString("[ " + room.getName() + " ]", screenX, screenY);
                    }
                } else {
                    g.setColor(new Color(190, 100, 255, 200));
                    g.drawString("[ " + room.getName() + " ]", screenX, screenY);
                }
            }
        }
    }

    public Room getSpawnRoom() {
        return spawnRoom;
    }

    public Room getCombatRoom() {
        return combatRoom;
    }

    public Room getExtractRoom() {
        return extractRoom;
    }

    public List<Room> getRooms() {
        return rooms;
    }
}
