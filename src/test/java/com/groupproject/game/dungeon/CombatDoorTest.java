package com.groupproject.game.dungeon;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.entity.enemy.TargetDummy;
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
}
