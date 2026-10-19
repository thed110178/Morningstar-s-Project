package com.groupproject.game.dungeon;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.entity.station.StatReforgeStation;
import com.groupproject.game.entity.weapon.AssaultRifle;
import com.groupproject.game.entity.weapon.CombatKnife;
import com.groupproject.game.entity.weapon.Pistol;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.state.GameStateManager;
import com.groupproject.game.state.PlayingState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying the Reset Stat Room, Reforge Zone, and StatReforgeStation progressive reroll mechanics.
 *
 * <p>WHY: Guarantees that:
 * 1. Procedural dungeon generation places a dedicated RESET_STAT chamber with DOOR_RESET_STAT and REFORGE_ZONE.
 * 2. Stage 4 (Boss Stage) maintains a symmetrical RESET_STAT chamber South of the Spawn room.
 * 3. RESET_STAT room is permanently open and is immune to combat lockdown.
 * 4. Progressive reroll cost strictly adheres to the exponential formula: 40 -> 65 (+25) -> 95 (+30) -> 135 (+40) -> 185 (+50).
 * 5. Player credits are deducted correctly on success and preserved on failure (insufficient credits).
 * 6. Reforge rerolls weapon star rating (1★ - 5★) and critical strike rate (5% - 50%) for all weapon slots.
 */
public class StatReforgeRoomTest {

    private DungeonMap map;

    @BeforeEach
    void setUp() {
        map = new DungeonMap();
    }

    @Test
    @DisplayName("Verify RESET_STAT room is generated in procedural dungeon and contains REFORGE_ZONE pad")
    void testResetStatRoomGeneratedInProceduralDungeon() {
        Room resetRoom = map.getResetStatRoom();
        assertNotNull(resetRoom, "Reset Stat Room must be generated in procedural layout");
        assertEquals(Room.RoomType.RESET_STAT, resetRoom.getRoomType());

        // Verify the 2x2 central pad is marked as REFORGE_ZONE
        int cx = resetRoom.getGridX() + resetRoom.getWidthInTiles() / 2;
        int cy = resetRoom.getGridY() + resetRoom.getHeightInTiles() / 2;

        Tile centerTile = map.getTile(cx, cy);
        assertNotNull(centerTile);
        assertEquals(TileType.REFORGE_ZONE, centerTile.getType(), "Center of Reset Stat room must be REFORGE_ZONE");
        assertFalse(centerTile.isSolid(), "Reforge zone platform must be walkable");

        // Verify door is DOOR_RESET_STAT and walkable
        assertFalse(resetRoom.getDoorTiles().isEmpty(), "Reset Stat room must have registered entrance door");
        Tile doorTile = resetRoom.getDoorTiles().get(0);
        assertEquals(TileType.DOOR_RESET_STAT, doorTile.getType(), "Entrance door must be DOOR_RESET_STAT");
        assertFalse(doorTile.isSolid(), "Doorway to reforge lab must always be open and non-solid");
    }

    @Test
    @DisplayName("Verify RESET_STAT room never locks down during combat (always open)")
    void testResetStatRoomNeverLocksDown() {
        Room resetRoom = map.getResetStatRoom();
        assertNotNull(resetRoom);
        assertFalse(resetRoom.isLocked());

        // Attempt to lockdown room
        resetRoom.lockdown();

        assertFalse(resetRoom.isLocked(), "RESET_STAT room must be immune to combat lockdown");
        assertEquals(Room.RoomStatus.UNVISITED, resetRoom.getStatus(),
                "Status should not switch to ACTIVE_LOCKDOWN for RESET_STAT room");

        for (Tile door : resetRoom.getDoorTiles()) {
            assertEquals(TileType.DOOR_RESET_STAT, door.getType(), "Door must remain DOOR_RESET_STAT");
            assertFalse(door.isSolid(), "Door must remain non-solid");
        }
    }

    @Test
    @DisplayName("Verify Stage 4 (Boss Stage) includes RESET_STAT room South of Spawn Room")
    void testResetStatRoomInBossStage() {
        map.generateBossStage();

        Room bossResetRoom = map.getResetStatRoom();
        assertNotNull(bossResetRoom, "Boss stage must include a RESET_STAT chamber");
        assertEquals(Room.RoomType.RESET_STAT, bossResetRoom.getRoomType());

        Room spawnRoom = map.getSpawnRoom();
        assertNotNull(spawnRoom);

        // Verify RESET_STAT is positioned South of Spawn Room (Y coordinate is strictly greater)
        assertTrue(bossResetRoom.getCenterWorldY() > spawnRoom.getCenterWorldY(),
                "In Boss Stage, Reset Stat room must be located South of Spawn Room");

        // Verify center tile is REFORGE_ZONE
        int cx = bossResetRoom.getGridX() + bossResetRoom.getWidthInTiles() / 2;
        int cy = bossResetRoom.getGridY() + bossResetRoom.getHeightInTiles() / 2;
        Tile center = map.getTile(cx, cy);
        assertNotNull(center);
        assertEquals(TileType.REFORGE_ZONE, center.getType());
    }

    @Test
    @DisplayName("Verify progressive credit cost progression formula: 40 -> 65 -> 95 -> 135 -> 185 -> 245")
    void testStatReforgeStationCostProgression() {
        StatReforgeStation station = new StatReforgeStation(100.0, 100.0);

        // Initial cost (0 resets)
        assertEquals(0, station.getReforgeCount());
        assertEquals(40, station.getCurrentCost(), "Initial reforge cost must be 40 Credits");

        // Simulate 1st reset
        station.setReforgeCount(1);
        assertEquals(65, station.getCurrentCost(), "2nd reset cost must be 65 Credits (+25)");

        // Simulate 2nd reset
        station.setReforgeCount(2);
        assertEquals(95, station.getCurrentCost(), "3rd reset cost must be 95 Credits (+30)");

        // Simulate 3rd reset
        station.setReforgeCount(3);
        assertEquals(135, station.getCurrentCost(), "4th reset cost must be 135 Credits (+40)");

        // Simulate 4th reset
        station.setReforgeCount(4);
        assertEquals(185, station.getCurrentCost(), "5th reset cost must be 185 Credits (+50)");

        // Simulate 5th reset
        station.setReforgeCount(5);
        assertEquals(245, station.getCurrentCost(), "6th reset cost must be 245 Credits (+60)");
    }

    @Test
    @DisplayName("Verify reforge interaction deducts credits, advances price, and rerolls star & crit")
    void testReforgeInteractionWithPlayingState() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();

        StatReforgeStation station = state.getStatReforgeStation();
        assertNotNull(station, "PlayingState must initialize StatReforgeStation");

        // Give player 200 Credits
        state.setPlayerCredits(200);
        assertEquals(200, state.getPlayerCredits());

        Weapon activeWeapon = state.getWeaponManager().getActiveWeapon();
        assertNotNull(activeWeapon, "Player must have an active weapon in hand");

        // First reforge: Cost is 40
        boolean success1 = station.interact(state);
        assertTrue(success1, "Reforge with 200 credits should succeed");
        assertEquals(160, state.getPlayerCredits(), "Credits must be 200 - 40 = 160");
        assertEquals(1, station.getReforgeCount(), "Reforge count should now be 1");
        assertEquals(65, station.getCurrentCost(), "Next reforge cost must be 65");

        // Verify rerolled weapon stats
        assertTrue(activeWeapon.getLevel() >= 1 && activeWeapon.getLevel() <= 5,
                "Weapon level must be within 1 to 5 stars, got: " + activeWeapon.getLevel());
        assertTrue(activeWeapon.getCritChance() >= 0.05 && activeWeapon.getCritChance() <= 0.50,
                "Weapon crit chance must be within 5% to 50%, got: " + activeWeapon.getCritChance());

        // Second reforge: Cost is 65
        boolean success2 = station.interact(state);
        assertTrue(success2, "Second reforge with 160 credits should succeed");
        assertEquals(95, state.getPlayerCredits(), "Credits must be 160 - 65 = 95");
        assertEquals(2, station.getReforgeCount(), "Reforge count should now be 2");
        assertEquals(95, station.getCurrentCost(), "Next reforge cost must be 95");

        // Third reforge: Cost is 95
        boolean success3 = station.interact(state);
        assertTrue(success3, "Third reforge with exactly 95 credits should succeed");
        assertEquals(0, state.getPlayerCredits(), "Credits must be 95 - 95 = 0");
        assertEquals(3, station.getReforgeCount(), "Reforge count should now be 3");
        assertEquals(135, station.getCurrentCost(), "Next reforge cost must be 135");
    }

    @Test
    @DisplayName("Verify reforge fails when player has insufficient credits")
    void testReforgeFailsWhenInsufficientCredits() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();

        StatReforgeStation station = state.getStatReforgeStation();
        assertNotNull(station);

        // Player has 25 credits (cost is 40)
        state.setPlayerCredits(25);

        boolean success = station.interact(state);
        assertFalse(success, "Reforge must fail when player has less than 40 credits");
        assertEquals(25, state.getPlayerCredits(), "Credits should not be deducted upon failure");
        assertEquals(0, station.getReforgeCount(), "Reforge count must remain 0");
        assertEquals(40, station.getCurrentCost(), "Current cost must remain 40");
    }

    @Test
    @DisplayName("Verify reforge works across secondary weapon slots (Knife & Pistol)")
    void testReforgeKnifeAndPistol() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();

        StatReforgeStation station = state.getStatReforgeStation();
        state.setPlayerCredits(1000);

        // Test with Slot 4 (Combat Knife)
        state.getWeaponManager().selectSlot(4);
        Weapon knife = state.getWeaponManager().getActiveWeapon();
        assertTrue(knife instanceof CombatKnife, "Slot 4 must be CombatKnife");

        int oldKnifeLevel = knife.getLevel();
        boolean knifeSuccess = station.interact(state);
        assertTrue(knifeSuccess, "Reforging CombatKnife should succeed");
        assertEquals(oldKnifeLevel, knife.getLevel(), "Cấp độ CombatKnife phải được giữ nguyên sau khi hiệu chỉnh");
        assertTrue(knife.getCritChance() >= 0.05 && knife.getCritChance() <= 0.50);

        // Test with Slot 3 (Pistol)
        state.getWeaponManager().selectSlot(3);
        Weapon pistol = state.getWeaponManager().getActiveWeapon();
        assertTrue(pistol instanceof Pistol, "Slot 3 must be Pistol");

        int oldPistolLevel = pistol.getLevel();
        boolean pistolSuccess = station.interact(state);
        assertTrue(pistolSuccess, "Reforging Pistol should succeed");
        assertEquals(oldPistolLevel, pistol.getLevel(), "Cấp độ Pistol phải được giữ nguyên sau khi hiệu chỉnh");
        assertTrue(pistol.getCritChance() >= 0.05 && pistol.getCritChance() <= 0.50);
    }

    @Test
    @DisplayName("Verify reforgeStats method preserves weapon level, redistributes stars and rerolls crit bounds")
    void testWeaponReforgeStatsMethodBounds() {
        Weapon weapon = new AssaultRifle();
        weapon.configureLevel(3); // Level 3 -> 2 sao thặng dư
        int expectedLevel = 3;
        int expectedExtraStars = 2;

        for (int i = 0; i < 50; i++) {
            Weapon.ReforgeResult result = weapon.reforgeStats();
            assertNotNull(result);
            assertEquals(expectedLevel, weapon.getLevel(), "Cấp độ vũ khí (Level) phải được bảo toàn tuyệt đối không đổi");
            assertEquals(expectedLevel, result.level(), "Record result.level() phải trùng với cấp độ vũ khí");

            // Kiểm tra tổng số sao thặng dư sau khi tái phân bổ phải bằng đúng số sao ban đầu
            int extraStars = (weapon.getDamageStars() - 1) + (weapon.getFireRateStars() - 1) + (weapon.getAccuracyStars() - 1);
            assertEquals(expectedExtraStars, extraStars, "Tổng số sao thặng dư phải được bảo toàn sau khi tái phân bổ (Rearrange)");

            // Kiểm tra tỉ lệ chí mạng nằm trong dải [5%, 50%]
            assertTrue(result.newCritPercent() >= 5 && result.newCritPercent() <= 50,
                    "Rerolled crit percent must be 5..50%, got: " + result.newCritPercent());
            assertEquals(result.newCritPercent() / 100.0, weapon.getCritChance(), 0.001);
        }
    }
}
