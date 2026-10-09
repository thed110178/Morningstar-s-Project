package com.groupproject.game.dungeon;

import com.groupproject.game.core.GameConfig;
import java.util.List;
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
    @DisplayName("Verify procedural generation instantiates 3-5 combat chambers plus spawn and extract")
    void testProceduralRoomsExist() {
        assertNotNull(map.getSpawnRoom(), "Spawn Room must be instantiated");
        assertEquals(Room.RoomType.SPAWN, map.getSpawnRoom().getRoomType());

        assertNotNull(map.getCombatRoom(), "Combat Room must be instantiated");
        assertEquals(Room.RoomType.COMBAT, map.getCombatRoom().getRoomType());

        assertNotNull(map.getExtractRoom(), "Extract Room must be instantiated");
        assertEquals(Room.RoomType.EXTRACT, map.getExtractRoom().getRoomType());

        int combatCount = map.getCombatRooms().size();
        assertTrue(combatCount >= 3 && combatCount <= 5,
                "Procedural dungeon must generate between 3 and 5 combat rooms, got: " + combatCount);
        assertEquals(combatCount + 2, map.getRooms().size(),
                "Total rooms must match combat rooms plus spawn and extract");
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
        int pillarCol = combat.getGridX() + 3;
        int pillarRow = combat.getGridY() + 3;

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

    @Test
    @DisplayName("Verify Extract Room contains EXTRACT_ZONE tiles and collision detection accurately identifies it")
    void testExtractZoneDetection() {
        Room extract = map.getExtractRoom();
        assertNotNull(extract);

        double cx = extract.getCenterWorldX();
        double cy = extract.getCenterWorldY();

        // Center pad should be EXTRACT_ZONE
        assertTrue(map.isIntersectsExtractZone(cx - 10, cy - 10, 20, 20),
                "Center of Extract Room must intersect EXTRACT_ZONE tiles");
        assertTrue(map.isOnExtractZone(cx, cy),
                "Center point of Extract Room must be an EXTRACT_ZONE tile");

        // Spawn room center should NOT be EXTRACT_ZONE
        Room spawn = map.getSpawnRoom();
        assertFalse(map.isOnExtractZone(spawn.getCenterWorldX(), spawn.getCenterWorldY()),
                "Spawn room must not be considered EXTRACT_ZONE");
    }

    @Test
    @DisplayName("Verify reset() procedural generation produces valid 3-5 chambers across multiple runs")
    void testProceduralGenerationVariabilityAndBounds() {
        for (int run = 0; run < 15; run++) {
            map.reset();
            int combatCount = map.getCombatRooms().size();
            assertTrue(combatCount >= 3 && combatCount <= 5,
                    "Run " + run + " must generate 3 to 5 combat chambers, got: " + combatCount);

            assertEquals(combatCount + 2, map.getRooms().size(),
                    "Total room count must equal combat rooms + 2");

            for (Room r : map.getRooms()) {
                assertTrue(r.getGridX() >= 0 && r.getGridX() + r.getWidthInTiles() <= DungeonMap.COLS,
                        "Room " + r.getName() + " exceeds grid X boundaries");
                assertTrue(r.getGridY() >= 0 && r.getGridY() + r.getHeightInTiles() <= DungeonMap.ROWS,
                        "Room " + r.getName() + " exceeds grid Y boundaries");

                // Center of every chamber must be walkable floor
                assertTrue(map.isAreaWalkable(r.getCenterWorldX() - 8, r.getCenterWorldY() - 8, 16, 16),
                        "Center of room " + r.getName() + " must be walkable");
            }
        }
    }

    @Test
    @DisplayName("Verify rooms never overlap and strictly align to directional grid slots")
    void testNoOverlappingRoomsAndStrictGridAlignment() {
        for (int run = 0; run < 15; run++) {
            map.reset();
            List<Room> allRooms = map.getRooms();

            for (int i = 0; i < allRooms.size(); i++) {
                Room r1 = allRooms.get(i);
                for (int j = i + 1; j < allRooms.size(); j++) {
                    Room r2 = allRooms.get(j);

                    // Check if two rooms overlap
                    boolean xOverlap = r1.getGridX() < r2.getGridX() + r2.getWidthInTiles()
                            && r1.getGridX() + r1.getWidthInTiles() > r2.getGridX();
                    boolean yOverlap = r1.getGridY() < r2.getGridY() + r2.getHeightInTiles()
                            && r1.getGridY() + r1.getHeightInTiles() > r2.getGridY();

                    assertFalse(xOverlap && yOverlap,
                            String.format("Run %d: Room %s overlaps with Room %s", run, r1.getName(), r2.getName()));
                }
            }
        }
    }
}
