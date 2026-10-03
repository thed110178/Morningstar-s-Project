package com.groupproject.game.dungeon;

import com.groupproject.game.core.GameConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying the DungeonMap structure, room layout, and collision logic.
 *
 * <p>WHY: Satisfies the course's automated unit testing criteria (JUnit 5). Validates that
 * procedural layout generation creates valid playable chambers, open corridors, and impenetrable
 * boundary walls before gameplay runtime.
 */
public class DungeonMapTest {

    private DungeonMap map;

    @BeforeEach
    void setUp() {
        map = new DungeonMap();
    }

    @Test
    @DisplayName("Verify map matrix grid dimensions match world configuration")
    void testMapDimensions() {
        assertEquals(80, DungeonMap.COLS, "Map should have exactly 80 columns");
        assertEquals(45, DungeonMap.ROWS, "Map should have exactly 45 rows");
    }

    @Test
    @DisplayName("Verify all three required chambers exist with correct types")
    void testThreeRoomsExist() {
        assertNotNull(map.getSpawnRoom(), "Spawn Room must be instantiated");
        assertEquals(Room.RoomType.SPAWN, map.getSpawnRoom().getRoomType());

        assertNotNull(map.getCombatRoom(), "Combat Room must be instantiated");
        assertEquals(Room.RoomType.COMBAT, map.getCombatRoom().getRoomType());

        assertNotNull(map.getExtractRoom(), "Extract Room must be instantiated");
        assertEquals(Room.RoomType.EXTRACT, map.getExtractRoom().getRoomType());

        assertEquals(3, map.getRooms().size(), "There should be exactly 3 rooms registered");
    }

    @Test
    @DisplayName("Verify Spawn Room center point is walkable and not blocked by walls")
    void testSpawnRoomCenterIsWalkable() {
        Room spawn = map.getSpawnRoom();
        double cx = spawn.getCenterWorldX();
        double cy = spawn.getCenterWorldY();

        boolean walkable = map.isAreaWalkable(cx - 10, cy - 10, 20, 20);
        assertTrue(walkable, "Center of Spawn Room must be walkable for player deployment");
    }

    @Test
    @DisplayName("Verify boundary walls of rooms are solid and block movement")
    void testRoomBoundaryWallsAreSolid() {
        Room spawn = map.getSpawnRoom();
        int wallCol = spawn.getGridX(); // Leftmost wall column
        int wallRow = spawn.getGridY(); // Topmost wall row

        Tile wallTile = map.getTile(wallCol, wallRow);
        assertNotNull(wallTile);
        assertEquals(TileType.WALL, wallTile.getType());
        assertTrue(wallTile.isSolid(), "Boundary wall tile must be solid");

        // Attempting to move into this wall position must return false
        double wallWorldX = wallCol * GameConfig.TILE_SIZE;
        double wallWorldY = wallRow * GameConfig.TILE_SIZE;
        assertFalse(map.isAreaWalkable(wallWorldX, wallWorldY, 16, 16),
                "Bounding box intersecting wall must NOT be walkable");
    }

    @Test
    @DisplayName("Verify tactical cover pillars in Combat Arena are solid")
    void testCombatCoverPillars() {
        Room combat = map.getCombatRoom();
        int pillarCol = combat.getGridX() + 5;
        int pillarRow = combat.getGridY() + 5;

        Tile pillarTile = map.getTile(pillarCol, pillarRow);
        assertNotNull(pillarTile);
        assertEquals(TileType.COVER, pillarTile.getType());
        assertTrue(pillarTile.isSolid(), "Cover pillar must be solid to block bullets and movement");
    }

    @Test
    @DisplayName("Verify out-of-bounds coordinates are considered impassable")
    void testOutOfBoundsAreImpassable() {
        assertFalse(map.isAreaWalkable(-100, -100, 30, 30), "Negative coordinates must be impassable");
        assertFalse(map.isAreaWalkable(GameConfig.WORLD_WIDTH + 50, 100, 30, 30),
                "Coordinates exceeding world width must be impassable");
    }
}
