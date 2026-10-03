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
}
