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
    @DisplayName("Verify procedural generation instantiates 3-5 combat chambers plus spawn, extract and shop")
    void testProceduralRoomsExist() {
        assertNotNull(map.getSpawnRoom(), "Spawn Room must be instantiated");
        assertEquals(Room.RoomType.SPAWN, map.getSpawnRoom().getRoomType());

        assertNotNull(map.getCombatRoom(), "Combat Room must be instantiated");
        assertEquals(Room.RoomType.COMBAT, map.getCombatRoom().getRoomType());

        assertNotNull(map.getExtractRoom(), "Extract Room must be instantiated");
        assertEquals(Room.RoomType.EXTRACT, map.getExtractRoom().getRoomType());

        assertNotNull(map.getShopRoom(), "Shop Room must be instantiated");
        assertEquals(Room.RoomType.SHOP, map.getShopRoom().getRoomType());

        int combatCount = map.getCombatRooms().size();
        assertTrue(combatCount >= 3 && combatCount <= 5,
                "Procedural dungeon must generate between 3 and 5 combat rooms, got: " + combatCount);
        assertEquals(combatCount + 3, map.getRooms().size(),
                "Total rooms must match combat rooms plus spawn, extract, and shop");
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
    @DisplayName("Verify destructible cover tiles are breakable and no solid cover surrounds room doorways")
    void testCombatCoverPillars() {
        Room combat = map.getCombatRoom();
        assertNotNull(combat);

        // 1. Kiểm tra không có bất kỳ ô cover cứng nào nằm trong vùng lối ra vào của phòng
        int rx = combat.getGridX();
        int ry = combat.getGridY();
        int rw = combat.getWidthInTiles();
        int rh = combat.getHeightInTiles();

        for (int c = rx; c < rx + rw; c++) {
            for (int r = ry; r < ry + rh; r++) {
                if (map.isNearDoorway(combat, c, r)) {
                    Tile t = map.getTile(c, r);
                    assertNotEquals(TileType.COVER, t.getType(),
                            String.format("Doorway clearance area at (%d, %d) must NOT contain solid COVER", c, r));
                }
            }
        }

        // 2. Kiểm tra phòng combat có CẢ HAI loại cover: Cover Cứng (COVER) và Cover Tạm Thời (DESTRUCTIBLE_COVER)
        Tile solidCoverTile = null;
        Tile destructibleTile = null;
        for (int c = rx + 1; c < rx + rw - 1; c++) {
            for (int r = ry + 1; r < ry + rh - 1; r++) {
                Tile t = map.getTile(c, r);
                if (t.getType() == TileType.COVER) {
                    solidCoverTile = t;
                } else if (t.getType() == TileType.DESTRUCTIBLE_COVER) {
                    destructibleTile = t;
                }
            }
        }

        assertNotNull(solidCoverTile, "Combat room must contain permanent solid COVER pillars");
        assertTrue(solidCoverTile.isSolid(), "Permanent cover must be solid");
        assertFalse(solidCoverTile.isDestructible(), "Permanent cover must NOT be destructible");

        assertNotNull(destructibleTile, "Combat room must contain destructible cover crates");
        assertTrue(destructibleTile.isSolid(), "Destructible cover must be solid initially");
        assertTrue(destructibleTile.blocksProjectiles(), "Destructible cover must block bullets initially");
        assertEquals(40, destructibleTile.getHealth(), "Initial health of destructible cover must be 40");

        // 3. Bắn hoặc chém gây sát thương làm vỡ ô cover tạm thời
        boolean destroyed = destructibleTile.takeDamage(40);
        assertTrue(destroyed, "Taking 40 damage must completely destroy the cover");
        assertEquals(TileType.FLOOR, destructibleTile.getType(), "Destroyed cover must turn into walkable FLOOR");
        assertFalse(destructibleTile.isSolid(), "Destroyed cover must no longer be solid");
        assertFalse(destructibleTile.blocksProjectiles(), "Destroyed cover must no longer block bullets");
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

            assertEquals(combatCount + 3, map.getRooms().size(),
                    "Total room count must equal combat rooms + 3 (Spawn, Extract, and Shop)");
            assertNotNull(map.getShopRoom(), "Shop room must be present in every run");

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

    @Test
    @DisplayName("Verify adjacent combat chambers are always connected by walkable corridors")
    void testAdjacentCombatRoomsAreAlwaysConnected() {
        for (int run = 0; run < 15; run++) {
            map.reset();
            List<Room> combats = map.getCombatRooms();

            for (int i = 0; i < combats.size(); i++) {
                Room r1 = combats.get(i);
                for (int j = i + 1; j < combats.size(); j++) {
                    Room r2 = combats.get(j);

                    // Kiểm tra nếu r1 và r2 kề nhau theo trục hoành (hướng 3h/9h, cách nhau CORRIDOR_LEN_X = 6 tiles)
                    boolean horizontallyAdjacent = (r1.getGridY() == r2.getGridY())
                            && (Math.abs(r1.getGridX() - r2.getGridX()) == DungeonMap.ROOM_WIDTH + DungeonMap.CORRIDOR_LEN_X);

                    // Kiểm tra nếu r1 và r2 kề nhau theo trục tung (hướng 12h/6h, cách nhau CORRIDOR_LEN_Y = 4 tiles)
                    boolean verticallyAdjacent = (r1.getGridX() == r2.getGridX())
                            && (Math.abs(r1.getGridY() - r2.getGridY()) == DungeonMap.ROOM_HEIGHT + DungeonMap.CORRIDOR_LEN_Y);

                    if (horizontallyAdjacent) {
                        Room left = r1.getGridX() < r2.getGridX() ? r1 : r2;
                        int midCol = left.getGridX() + DungeonMap.ROOM_WIDTH + 1;
                        int corridorRow = left.getGridY() + 4;
                        Tile midCorridorTile = map.getTile(midCol, corridorRow);
                        assertNotNull(midCorridorTile);
                        assertEquals(TileType.FLOOR, midCorridorTile.getType(),
                                "Corridor between horizontally adjacent combat rooms must be FLOOR");
                    }

                    if (verticallyAdjacent) {
                        Room top = r1.getGridY() < r2.getGridY() ? r1 : r2;
                        int midRow = top.getGridY() + DungeonMap.ROOM_HEIGHT + 1;
                        int corridorCol = top.getGridX() + 6;
                        Tile midCorridorTile = map.getTile(corridorCol, midRow);
                        assertNotNull(midCorridorTile);
                        assertEquals(TileType.FLOOR, midCorridorTile.getType(),
                                "Corridor between vertically adjacent combat rooms must be FLOOR");
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("Verify Stage 3 generates Grand Boss Arena layout and allows dynamic extract pad spawn")
    void testStageProgressionAndBossStageLayout() {
        // Stage 1
        map.generateStage(1);
        assertEquals(1, map.getCurrentStage());
        assertTrue(map.getCombatRooms().size() >= 3 && map.getCombatRooms().size() <= 5);
        assertNotNull(map.getExtractRoom());

        // Stage 2
        map.generateStage(2);
        assertEquals(2, map.getCurrentStage());
        assertTrue(map.getCombatRooms().size() >= 3 && map.getCombatRooms().size() <= 5);
        assertNotNull(map.getExtractRoom());

        // Stage 3 - Bossfight Stage
        map.generateStage(3);
        assertEquals(3, map.getCurrentStage());
        assertNotNull(map.getSpawnRoom(), "Stage 3 must have a Spawn Room");
        assertNotNull(map.getShopRoom(), "Stage 3 must have a Tactical Shop Room");
        assertEquals(1, map.getCombatRooms().size(), "Stage 3 must have exactly 1 big combat room (Grand Arena)");

        Room bossRoom = map.getCombatRoom();
        assertNotNull(bossRoom, "Boss room must exist");
        assertEquals(DungeonMap.BOSS_ROOM_WIDTH, bossRoom.getWidthInTiles(), "Boss room width must match BOSS_ROOM_WIDTH");
        assertEquals(DungeonMap.BOSS_ROOM_HEIGHT, bossRoom.getHeightInTiles(), "Boss room height must match BOSS_ROOM_HEIGHT");
        assertNull(map.getExtractRoom(), "Initial Stage 3 must not have a pre-existing separate extract room");

        // Center should not be extract pad before clearing
        double cx = bossRoom.getCenterWorldX();
        double cy = bossRoom.getCenterWorldY();
        assertFalse(map.isOnExtractZone(cx, cy), "Before clearing, boss room center must NOT be extract zone");

        // Spawn extract pad dynamically upon room clear
        map.spawnExtractZoneInRoom(bossRoom);
        assertNotNull(map.getExtractRoom(), "Extract room should be assigned after pad activation");
        assertTrue(map.isOnExtractZone(cx, cy), "After clearing, boss room center must be EXTRACT_ZONE");
        assertTrue(map.isIntersectsExtractZone(cx - 10, cy - 10, 20, 20),
                "Bounding box at center should intersect extract zone");
    }

    @Test
    @DisplayName("Verify reset() restores dungeon progression to Stage 1")
    void testResetRestoresStageOne() {
        map.generateStage(3);
        assertEquals(3, map.getCurrentStage());
        assertEquals(1, map.getCombatRooms().size());

        map.reset();
        assertEquals(1, map.getCurrentStage());
        assertTrue(map.getCombatRooms().size() >= 3 && map.getCombatRooms().size() <= 5);
        assertNotNull(map.getExtractRoom());
    }

    @Test
    @DisplayName("Verify damageDestructibleCoverInArea damages and breaks covers, updating walkable physics")
    void testDestructibleCoverAreaDamage() {
        Room combat = map.getCombatRoom();
        int testCol = combat.getGridX() + 4;
        int testRow = combat.getGridY() + 4;
        Tile tile = map.getTile(testCol, testRow);
        tile.setType(TileType.DESTRUCTIBLE_COVER);

        double wx = tile.getWorldX();
        double wy = tile.getWorldY();

        assertFalse(map.isAreaWalkable(wx, wy, 20, 20), "Destructible cover must obstruct movement");
        assertTrue(map.isAreaBlockingProjectiles(wx, wy, 20, 20), "Destructible cover must block bullets");

        // Partial damage (25 dmg from pistol)
        boolean damaged = map.damageDestructibleCoverInArea(wx + 2, wy + 2, 10, 10, 25);
        assertTrue(damaged, "Area damage should hit the destructible cover");
        assertEquals(15, tile.getHealth(), "Remaining health should be 40 - 25 = 15");
        assertEquals(TileType.DESTRUCTIBLE_COVER, tile.getType(), "Tile should still be DESTRUCTIBLE_COVER");

        // Lethal damage (25 dmg again) -> breaks cover
        damaged = map.damageDestructibleCoverInArea(wx + 2, wy + 2, 10, 10, 25);
        assertTrue(damaged);
        assertEquals(TileType.FLOOR, tile.getType(), "Cover should break into FLOOR");
        assertTrue(map.isAreaWalkable(wx, wy, 20, 20), "Broken cover must allow movement");
        assertFalse(map.isAreaBlockingProjectiles(wx, wy, 20, 20), "Broken cover must allow bullets");
    }

    @Test
    @DisplayName("Verify no solid cover surrounds room doorways across multiple dungeon runs and stages")
    void testDoorwayClearanceAcrossMultipleStages() {
        for (int stage = 1; stage <= 3; stage++) {
            map.generateStage(stage);
            for (Room room : map.getRooms()) {
                if (room.getRoomType() == Room.RoomType.COMBAT) {
                    int rx = room.getGridX();
                    int ry = room.getGridY();
                    int rw = room.getWidthInTiles();
                    int rh = room.getHeightInTiles();

                    for (int c = rx; c < rx + rw; c++) {
                        for (int r = ry; r < ry + rh; r++) {
                            if (map.isNearDoorway(room, c, r)) {
                                Tile t = map.getTile(c, r);
                                assertNotEquals(TileType.COVER, t.getType(),
                                        String.format("Stage %d: Room %s doorway at (%d, %d) must NOT contain solid COVER",
                                                stage, room.getName(), c, r));
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("Verify Shop Room can spawn across diverse locations in the stage and not exclusively adjacent to spawn")
    void testShopRoomDiversePlacementAcrossRuns() {
        boolean sawNonSpawnAdjacent = false;

        for (int i = 0; i < 30; i++) {
            map.generateProceduralDungeon();
            Room shop = map.getShopRoom();
            Room spawn = map.getSpawnRoom();
            assertNotNull(shop, "Shop Room must be present");
            assertNotNull(spawn, "Spawn Room must be present");

            // Khoảng cách Manhattan giữa Shop Room và Spawn Room theo ô phòng
            int dx = Math.abs(shop.getGridX() - spawn.getGridX());
            int dy = Math.abs(shop.getGridY() - spawn.getGridY());

            // Nếu khoảng cách lớn hơn 1 ô phòng (ROOM_WIDTH + CORRIDOR_LEN_X = 20),
            // tức Shop Room kết nối với Combat Room hoặc Extract Room ở xa
            if (dx > DungeonMap.ROOM_WIDTH + DungeonMap.CORRIDOR_LEN_X + 2
                    || dy > DungeonMap.ROOM_HEIGHT + DungeonMap.CORRIDOR_LEN_Y + 2) {
                sawNonSpawnAdjacent = true;
            }
        }

        assertTrue(sawNonSpawnAdjacent, "Shop Room must appear across diverse locations, not only next to spawn room");
    }
}
