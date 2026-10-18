package com.groupproject.game.dungeon;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.entity.enemy.TargetDummy;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying the Combat Room lockdown doors and hostile target damage lifecycle.
 *
 * <p>WHY: Guarantees that blast doors seal shut during hostile combat encounters and correctly
 * disengage upon room clearance, preventing exploits where players could leave locked chambers.
 */
public class CombatDoorTest {

    private DungeonMap map;
    private Room combatRoom;

    @BeforeEach
    void setUp() {
        map = new DungeonMap();
        combatRoom = map.getCombatRoom();
    }

    @Test
    @DisplayName("Verify doors are initially installed in open state")
    void testInitialDoorsOpen() {
        assertNotNull(combatRoom);
        assertFalse(combatRoom.getDoorTiles().isEmpty(), "Combat room must have registered door tiles");

        for (Tile door : combatRoom.getDoorTiles()) {
            assertEquals(TileType.DOOR_OPEN, door.getType(), "Door must start in DOOR_OPEN state");
            assertFalse(door.isSolid(), "Open door must not be solid");
        }
        assertFalse(combatRoom.isLocked(), "Room should not be locked initially");
    }

    @Test
    @DisplayName("Verify lockdown seals all doors and solidifies them against movement")
    void testRoomLockdown() {
        combatRoom.lockdown();

        assertTrue(combatRoom.isLocked(), "Room must be marked as locked");
        assertEquals(Room.RoomStatus.ACTIVE_LOCKDOWN, combatRoom.getStatus());

        for (Tile door : combatRoom.getDoorTiles()) {
            assertEquals(TileType.DOOR_CLOSED, door.getType(), "Door must change to DOOR_CLOSED");
            assertTrue(door.isSolid(), "Closed door must become solid");

            // Verify collision check confirms impassable barrier
            double doorX = door.getWorldX();
            double doorY = door.getWorldY();
            assertFalse(map.isAreaWalkable(doorX + 2, doorY + 2, 20, 20),
                    "Closed door must block entity traversal");
        }
    }

    @Test
    @DisplayName("Verify unlock clears lockdown and restores passable pathway")
    void testRoomUnlock() {
        combatRoom.lockdown();
        combatRoom.unlock();

        assertFalse(combatRoom.isLocked(), "Room must no longer be locked");
        assertTrue(combatRoom.isCleared(), "Room must be marked as cleared");

        for (Tile door : combatRoom.getDoorTiles()) {
            assertEquals(TileType.DOOR_OPEN, door.getType(), "Door must return to DOOR_OPEN");
            assertFalse(door.isSolid(), "Unlocked door must be passable");

            // Verify collision check confirms walkable pathway
            double doorX = door.getWorldX();
            double doorY = door.getWorldY();
            assertTrue(map.isAreaWalkable(doorX + 2, doorY + 2, 20, 20),
                    "Open door must allow entity traversal");
        }
    }

    @Test
    @DisplayName("Verify open door absorbs projectiles while allowing player walking")
    void testOpenDoorAbsorbsBulletsWhileAllowingWalk() {
        for (Tile door : combatRoom.getDoorTiles()) {
            assertEquals(TileType.DOOR_OPEN, door.getType());
            assertTrue(door.blocksProjectiles(), "Open door must block/absorb projectiles");
            assertFalse(door.isSolid(), "Open door must not block player movement");

            double doorX = door.getWorldX();
            double doorY = door.getWorldY();

            // Player can walk through
            assertTrue(map.isAreaWalkable(doorX + 2, doorY + 2, 20, 20));

            // But projectiles are blocked
            assertTrue(map.isAreaBlockingProjectiles(doorX + 2, doorY + 2, 4, 4));
        }

        // Test with ProjectileManager simulation
        com.groupproject.game.entity.projectile.ProjectileManager pm =
                new com.groupproject.game.entity.projectile.ProjectileManager();

        Tile firstDoor = combatRoom.getDoorTiles().get(0);
        com.groupproject.game.entity.projectile.Projectile p =
                new com.groupproject.game.entity.projectile.Projectile(
                        firstDoor.getWorldX() + 5, firstDoor.getWorldY() + 5,
                        0, 600, 25, 1.0, true);
        pm.spawn(p);
        pm.update(0.016, map);

        assertFalse(p.isActive(), "Bullet must be absorbed and deactivated upon hitting open door");
    }

    @Test
    @DisplayName("Verify TargetDummy takes damage and dies upon reaching 0 HP")
    void testTargetDummyDamage() {
        TargetDummy dummy = new TargetDummy(500, 500, 50);
        assertTrue(dummy.isAlive());
        assertEquals(50, dummy.getCurrentHealth());

        dummy.takeDamage(20);
        assertEquals(30, dummy.getCurrentHealth());
        assertTrue(dummy.isAlive());

        dummy.takeDamage(40); // Overkill damage
        assertEquals(0, dummy.getCurrentHealth());
        assertFalse(dummy.isAlive(), "Dummy should be eliminated when HP reaches 0");
    }

    @Test
    @DisplayName("Verify room reset restores UNVISITED status and opens all doors")
    void testRoomReset() {
        combatRoom.lockdown();
        assertTrue(combatRoom.isLocked());

        combatRoom.reset();
        assertEquals(Room.RoomStatus.UNVISITED, combatRoom.getStatus());
        assertFalse(combatRoom.isLocked());

        for (Tile door : combatRoom.getDoorTiles()) {
            assertEquals(TileType.DOOR_OPEN, door.getType());
            assertFalse(door.isSolid());
        }
    }

    @Test
    @DisplayName("Verify hasEntityEntered accurately detects crossing doorway threshold")
    void testHasEntityEnteredDetection() {
        double doorWorldX = combatRoom.getGridX() * GameConfig.TILE_SIZE; // 1344
        double py = 21.5 * GameConfig.TILE_SIZE; // Corridor center Y
        double size = 28.0;

        // Outside room (corridor, col 27)
        assertFalse(combatRoom.hasEntityEntered(doorWorldX - 20, py, size, size));

        // Inside doorway tile (col 28)
        assertFalse(combatRoom.hasEntityEntered(doorWorldX + 2, py, size, size));

        // Stepped onto room floor (col 29)
        double interiorX = (combatRoom.getGridX() + 1.0) * GameConfig.TILE_SIZE + 2; // 1394
        assertTrue(combatRoom.hasEntityEntered(interiorX, py, size, size));
    }

    @Test
    @DisplayName("Simulate player entering combat room frame by frame and verify lockdown trigger in PlayingState")
    void testPlayingStateLockdownOnEntrance() {
        com.groupproject.game.state.GameStateManager gsm = new com.groupproject.game.state.GameStateManager();
        com.groupproject.game.state.PlayingState state = new com.groupproject.game.state.PlayingState(gsm);
        gsm.registerState(com.groupproject.game.state.GameStateManager.StateType.PLAYING, state);
        gsm.setState(com.groupproject.game.state.GameStateManager.StateType.PLAYING);

        Room stateArena = state.getDungeonMap().getCombatRoom();
        assertNotNull(stateArena);
        assertEquals(Room.RoomStatus.UNVISITED, stateArena.getStatus());

        com.groupproject.game.input.KeyboardHandler keyH = new com.groupproject.game.input.KeyboardHandler();
        com.groupproject.game.input.MouseHandler mouseH = new com.groupproject.game.input.MouseHandler();

        // Simulate pressing D (moving right toward Combat Arena)
        java.awt.Canvas dummyComponent = new java.awt.Canvas();
        java.awt.event.KeyEvent pressD = new java.awt.event.KeyEvent(
                dummyComponent, java.awt.event.KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_D, 'D');
        keyH.keyPressed(pressD);
        keyH.update();

        // Update frames until player enters and locks the arena
        for (int i = 0; i < 300; i++) {
            state.update(0.016, keyH, mouseH);
            if (stateArena.isLocked()) {
                break;
            }
        }

        assertTrue(stateArena.isLocked(), "Combat arena must be locked upon operative entrance");
        assertEquals(Room.RoomStatus.ACTIVE_LOCKDOWN, stateArena.getStatus());

        for (Tile door : stateArena.getDoorTiles()) {
            assertEquals(TileType.DOOR_CLOSED, door.getType(), "Door must be closed");
            assertTrue(door.isSolid(), "Door must be solid impassable barrier");
        }

        // Verify PlayingState init resets the dungeon properly
        state.init();
        assertEquals(Room.RoomStatus.UNVISITED, stateArena.getStatus(),
                "init() must reset combat room back to UNVISITED");
        assertFalse(stateArena.isLocked());
    }

    @Test
    @DisplayName("Verify pressing [F] on Extract Pad triggers victory screen and transitions to Menu")
    void testExtractInteractionTriggersBlackScreenVictory() {
        com.groupproject.game.state.GameStateManager gsm = new com.groupproject.game.state.GameStateManager();
        com.groupproject.game.state.PlayingState state = new com.groupproject.game.state.PlayingState(gsm);
        com.groupproject.game.state.MenuState menu = new com.groupproject.game.state.MenuState(gsm);

        gsm.registerState(com.groupproject.game.state.GameStateManager.StateType.MENU, menu);
        gsm.registerState(com.groupproject.game.state.GameStateManager.StateType.PLAYING, state);
        gsm.setState(com.groupproject.game.state.GameStateManager.StateType.PLAYING);

        Room extract = state.getDungeonMap().getExtractRoom();
        assertNotNull(extract);

        // Teleport operative onto the extraction pad
        state.setPlayerPosition(extract.getCenterWorldX() - 14, extract.getCenterWorldY() - 14);

        com.groupproject.game.input.KeyboardHandler keyH = new com.groupproject.game.input.KeyboardHandler();
        com.groupproject.game.input.MouseHandler mouseH = new com.groupproject.game.input.MouseHandler();

        // 1. Update one frame: operative is standing on extract pad
        state.update(0.016, keyH, mouseH);
        assertTrue(state.isStandingOnExtract(), "Operative must be recognized standing on EXTRACT_ZONE");
        assertFalse(state.isExtracting(), "Extraction victory sequence should not trigger without pressing [F]");

        // 2. Press interaction key [F] at Stage 1 -> should advance to Stage 2
        java.awt.Canvas dummy = new java.awt.Canvas();
        java.awt.event.KeyEvent pressF = new java.awt.event.KeyEvent(
                dummy, java.awt.event.KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_F, 'F');
        keyH.keyPressed(pressF);
        keyH.update();

        state.update(0.016, keyH, mouseH);
        assertEquals(2, state.getCurrentStage(), "Pressing [F] on extraction pad in Stage 1 must advance to Stage 2");

        // 3. Set to Stage 3 (Final Bossfight Stage) and place on extract pad
        state.setCurrentStage(com.groupproject.game.state.PlayingState.MAX_STAGES);
        state.getDungeonMap().generateStage(com.groupproject.game.state.PlayingState.MAX_STAGES);
        Room bossRoom = state.getDungeonMap().getCombatRoom();
        assertNotNull(bossRoom);
        state.getDungeonMap().spawnExtractZoneInRoom(bossRoom);
        state.setPlayerPosition(bossRoom.getCenterWorldX() - 14, bossRoom.getCenterWorldY() - 14);

        // Fresh keyboard handler for second key press
        com.groupproject.game.input.KeyboardHandler keyH2 = new com.groupproject.game.input.KeyboardHandler();
        keyH2.keyPressed(pressF);
        keyH2.update();

        state.update(0.016, keyH2, mouseH);
        assertTrue(state.isExtracting(), "Pressing [F] on extraction pad in Final Stage must activate victory sequence");

        // 4. Skip black screen by pressing [ENTER]
        java.awt.event.KeyEvent pressEnter = new java.awt.event.KeyEvent(
                dummy, java.awt.event.KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_ENTER, '\n');
        keyH2.keyPressed(pressEnter);
        keyH2.update();

        state.update(0.016, keyH2, mouseH);
        assertEquals(com.groupproject.game.state.GameStateManager.StateType.MENU,
                gsm.getCurrentStateType(), "Victory debriefing must return player to Main Menu");
    }

    @Test
    @DisplayName("Verify hostile combat targets never spawn inside solid cover, destructible crates, or walls")
    void testEnemySpawnNeverOverlapsCoverOrSolidTiles() {
        for (int stage = 1; stage <= 3; stage++) {
            com.groupproject.game.state.GameStateManager gsm = new com.groupproject.game.state.GameStateManager();
            com.groupproject.game.state.PlayingState state = new com.groupproject.game.state.PlayingState(gsm);
            state.setCurrentStage(stage);
            state.getDungeonMap().generateStage(stage);

            com.groupproject.game.input.KeyboardHandler keyH = new com.groupproject.game.input.KeyboardHandler();
            com.groupproject.game.input.MouseHandler mouseH = new com.groupproject.game.input.MouseHandler();

            for (Room room : state.getDungeonMap().getCombatRooms()) {
                // Đưa người chơi vào phòng combat để kích hoạt lockdown và sinh enemies
                state.setPlayerPosition(room.getCenterWorldX() - 14, room.getCenterWorldY() - 14);
                state.update(0.016, keyH, mouseH);

                List<TargetDummy> targets = state.getCombatTargets();
                assertFalse(targets.isEmpty(), "Combat targets must be spawned when room locks down");

                for (TargetDummy dummy : targets) {
                    double dx = dummy.getX();
                    double dy = dummy.getY();

                    // 1. Toàn bộ hitbox của dummy (30x30) phải nằm trên ô có thể di chuyển (isAreaWalkable)
                    boolean walkable = state.getDungeonMap().isAreaWalkable(dx - 15, dy - 15, 30, 30);
                    assertTrue(walkable, String.format("Stage %d: Enemy at (%.1f, %.1f) must be on 100%% walkable floor",
                            stage, dx, dy));

                    // 2. Ô gạch tại tâm của dummy không được phép là COVER, DESTRUCTIBLE_COVER hoặc WALL
                    int col = (int) (dx / GameConfig.TILE_SIZE);
                    int row = (int) (dy / GameConfig.TILE_SIZE);
                    Tile centerTile = state.getDungeonMap().getTile(col, row);
                    assertNotNull(centerTile);
                    assertNotEquals(TileType.COVER, centerTile.getType(),
                            String.format("Stage %d: Enemy must NOT spawn on solid COVER at grid (%d, %d)", stage, col, row));
                    assertNotEquals(TileType.DESTRUCTIBLE_COVER, centerTile.getType(),
                            String.format("Stage %d: Enemy must NOT spawn on DESTRUCTIBLE_COVER at grid (%d, %d)", stage, col, row));
                    assertNotEquals(TileType.WALL, centerTile.getType(),
                            String.format("Stage %d: Enemy must NOT spawn on WALL at grid (%d, %d)", stage, col, row));
                    assertFalse(centerTile.isSolid(), "Center tile of enemy must NOT be solid");
                }
            }
        }
    }

    @Test
    @DisplayName("Verify Shop Room has dedicated DOOR_SHOP gateway that is always open and passable")
    void testShopGatewayDoorAlwaysOpen() {
        Room shop = map.getShopRoom();
        assertNotNull(shop, "Dungeon must have a shop room");
        assertFalse(shop.getDoorTiles().isEmpty(), "Shop room must have registered gateway door tiles");

        for (Tile door : shop.getDoorTiles()) {
            assertEquals(TileType.DOOR_SHOP, door.getType(), "Shop door must be DOOR_SHOP");
            assertFalse(door.isSolid(), "Shop door must never be solid");
            assertTrue(map.isAreaWalkable(door.getWorldX() + 2, door.getWorldY() + 2, 20, 20),
                    "Shop door must allow traversal");
        }

        // Lockdown all combat rooms and verify shop door remains open
        for (Room cr : map.getCombatRooms()) {
            cr.lockdown();
        }
        shop.lockdown(); // Even if lockdown is invoked on shop directly

        for (Tile door : shop.getDoorTiles()) {
            assertEquals(TileType.DOOR_SHOP, door.getType(), "Shop door must remain DOOR_SHOP during combat lockdown");
            assertFalse(door.isSolid(), "Shop door must remain passable during combat lockdown");
        }
    }

    @Test
    @DisplayName("Verify Stage 3 spawns Titan Mecha Boss Dummy with 1000 HP and large size 64px")
    void testStage3BossDummyPropertiesAndLifecycle() {
        com.groupproject.game.state.GameStateManager gsm = new com.groupproject.game.state.GameStateManager();
        com.groupproject.game.state.PlayingState state = new com.groupproject.game.state.PlayingState(gsm);
        state.setCurrentStage(3);
        state.getDungeonMap().generateStage(3);

        com.groupproject.game.input.KeyboardHandler keyH = new com.groupproject.game.input.KeyboardHandler();
        com.groupproject.game.input.MouseHandler mouseH = new com.groupproject.game.input.MouseHandler();

        Room bossRoom = state.getDungeonMap().getCombatRooms().get(0);
        assertNotNull(bossRoom);
        assertEquals("Grand Boss Arena", bossRoom.getName());

        // Di chuyển người chơi vào phòng Grand Boss Arena để kích hoạt lockdown
        state.setPlayerPosition(bossRoom.getCenterWorldX() - 14, bossRoom.getCenterWorldY() - 14);
        state.update(0.016, keyH, mouseH);

        // Kiểm tra bossDummy được sinh ra
        TargetDummy boss = state.getBossDummy();
        assertNotNull(boss, "Stage 3 Grand Boss Arena must spawn a designated Boss Dummy");
        assertTrue(boss.isBoss(), "Boss Dummy must have isBoss == true");
        assertEquals(1000, boss.getMaxHealth(), "Titan Boss Dummy must have 1000 Max HP");
        assertEquals(1000, boss.getCurrentHealth(), "Titan Boss Dummy must start with full 1000 HP");
        assertEquals(64.0, boss.getSize(), "Titan Boss Dummy must be 64px large");
        assertTrue(boss.getName().contains("TITAN MECHA DUMMY"), "Boss name should be Titan Mecha Dummy");
        assertTrue(boss.isAlive(), "Boss must start alive");

        // Gây sát thương một phần cho Boss
        boss.takeDamage(250);
        assertEquals(750, boss.getCurrentHealth());
        assertTrue(boss.isAlive());

        // Kiểm tra hạ gục Boss bằng sát thương chí mạng
        boss.takeDamage(750);
        assertEquals(0, boss.getCurrentHealth());
        assertFalse(boss.isAlive());
    }

    @Test
    @DisplayName("Verify TargetDummy Boss constructor and damage handling")
    void testTargetDummyBossConstructor() {
        TargetDummy boss = new TargetDummy(500, 500, 1000, 64.0, true, "TITAN MECHA DUMMY [BOSS]");
        assertTrue(boss.isBoss());
        assertEquals(64.0, boss.getSize());
        assertEquals(1000, boss.getMaxHealth());
        assertEquals(1000, boss.getCurrentHealth());
        assertEquals("TITAN MECHA DUMMY [BOSS]", boss.getName());
        assertEquals(64, boss.getBounds().width);
        assertEquals(64, boss.getBounds().height);

        boss.takeDamage(300);
        assertEquals(700, boss.getCurrentHealth());
        assertTrue(boss.isAlive());

        boss.takeDamage(800);
        assertEquals(0, boss.getCurrentHealth());
        assertFalse(boss.isAlive());
    }
}
