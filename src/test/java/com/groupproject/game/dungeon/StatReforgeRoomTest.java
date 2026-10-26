package com.groupproject.game.dungeon;

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
    @DisplayName("Verify progressive credit cost formula: Lv.1 weapon (15 -> 25 -> 40 -> 60) and Lv.2+ weapon (30 -> 50 -> 75 -> 105)")
    void testStatReforgeStationCostProgression() {
        StatReforgeStation station = new StatReforgeStation(100.0, 100.0);
        Pistol lv1Pistol = new Pistol();
        lv1Pistol.configureLevel(1);

        AssaultRifle lv2Rifle = new AssaultRifle();
        lv2Rifle.configureLevel(2);

        // --- 1. Súng Cấp 1 (Chỉ roll lại tỉ lệ chí mạng): Giá ưu đãi khởi đầu 15 Credits ---
        assertEquals(0, station.getReforgeCount());
        assertEquals(15, station.getCurrentCost(lv1Pistol), "Initial reforge cost for Lv.1 weapon must be 15 Credits");

        station.setReforgeCount(1);
        assertEquals(25, station.getCurrentCost(lv1Pistol), "2nd reforge for Lv.1 must be 25 Credits (+10)");

        station.setReforgeCount(2);
        assertEquals(40, station.getCurrentCost(lv1Pistol), "3rd reforge for Lv.1 must be 40 Credits (+15)");

        station.setReforgeCount(3);
        assertEquals(60, station.getCurrentCost(lv1Pistol), "4th reforge for Lv.1 must be 60 Credits (+20)");

        // --- 2. Súng Cấp 2+ (Tái phân bổ sao và roll chí mạng): Khởi đầu 30 Credits ---
        station.setReforgeCount(0);
        assertEquals(30, station.getCurrentCost(lv2Rifle), "Initial reforge cost for Lv.2+ weapon must be 30 Credits");

        station.setReforgeCount(1);
        assertEquals(50, station.getCurrentCost(lv2Rifle), "2nd reforge for Lv.2+ must be 50 Credits (+20)");

        station.setReforgeCount(2);
        assertEquals(75, station.getCurrentCost(lv2Rifle), "3rd reforge for Lv.2+ must be 75 Credits (+25)");

        station.setReforgeCount(3);
        assertEquals(105, station.getCurrentCost(lv2Rifle), "4th reforge for Lv.2+ must be 105 Credits (+30)");
    }

    @Test
    @DisplayName("Verify reforge interaction opens modal, and executeReforge deducts credits and advances price")
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
        assertEquals(1, activeWeapon.getLevel(), "Default starting weapon in hand must be Level 1");

        // Interacting with station opens the StatReforgeModalUI
        boolean opened = station.interact(state);
        assertTrue(opened, "Interact should succeed and open modal");
        assertTrue(state.getStatReforgeModalUI().isVisible(), "StatReforgeModalUI must be visible after interact");

        // First reforge: Cost for Lv.1 weapon is 15 Credits
        boolean success1 = station.executeReforge(state, activeWeapon);
        assertTrue(success1, "Reforge with 200 credits should succeed");
        assertEquals(185, state.getPlayerCredits(), "Credits must be 200 - 15 = 185");
        assertEquals(1, station.getReforgeCount(), "Reforge count should now be 1");
        assertEquals(25, station.getCurrentCost(activeWeapon), "Next reforge cost for Lv.1 must be 25");

        // Verify rerolled weapon stats
        assertTrue(activeWeapon.getLevel() >= 1 && activeWeapon.getLevel() <= 5,
                "Weapon level must be preserved within valid range, got: " + activeWeapon.getLevel());
        assertTrue(activeWeapon.getCritChance() >= 0.05 && activeWeapon.getCritChance() <= 0.50,
                "Weapon crit chance must be within 5% to 50%, got: " + activeWeapon.getCritChance());

        // Second reforge: Cost is 25 Credits
        boolean success2 = station.executeReforge(state, activeWeapon);
        assertTrue(success2, "Second reforge with 185 credits should succeed");
        assertEquals(160, state.getPlayerCredits(), "Credits must be 185 - 25 = 160");
        assertEquals(2, station.getReforgeCount(), "Reforge count should now be 2");
        assertEquals(40, station.getCurrentCost(activeWeapon), "Next reforge cost for Lv.1 must be 40");

        // Third reforge: Cost is 40 Credits
        boolean success3 = station.executeReforge(state, activeWeapon);
        assertTrue(success3, "Third reforge with 160 credits should succeed");
        assertEquals(120, state.getPlayerCredits(), "Credits must be 160 - 40 = 120");
        assertEquals(3, station.getReforgeCount(), "Reforge count should now be 3");
        assertEquals(60, station.getCurrentCost(activeWeapon), "Next reforge cost for Lv.1 must be 60");
    }

    @Test
    @DisplayName("Verify reforge fails when player has insufficient credits")
    void testReforgeFailsWhenInsufficientCredits() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();

        StatReforgeStation station = state.getStatReforgeStation();
        assertNotNull(station);

        // Player has 10 credits (cost for Lv.1 weapon is 15 credits)
        state.setPlayerCredits(10);

        Weapon activeWeapon = state.getWeaponManager().getActiveWeapon();
        boolean success = station.executeReforge(state, activeWeapon);
        assertFalse(success, "Reforge must fail when player has less than 15 credits for Lv.1 weapon");
        assertEquals(10, state.getPlayerCredits(), "Credits should not be deducted upon failure");
        assertEquals(0, station.getReforgeCount(), "Reforge count must remain 0");
        assertEquals(15, station.getCurrentCost(activeWeapon), "Current cost for Lv.1 weapon must remain 15");
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
        boolean knifeSuccess = station.executeReforge(state, knife);
        assertTrue(knifeSuccess, "Reforging CombatKnife should succeed");
        assertEquals(oldKnifeLevel, knife.getLevel(), "Cấp độ CombatKnife phải được giữ nguyên sau khi hiệu chỉnh");
        assertTrue(knife.getCritChance() >= 0.05 && knife.getCritChance() <= 0.50);

        // Test with Slot 3 (Pistol)
        state.getWeaponManager().selectSlot(3);
        Weapon pistol = state.getWeaponManager().getActiveWeapon();
        assertTrue(pistol instanceof Pistol, "Slot 3 must be Pistol");

        int oldPistolLevel = pistol.getLevel();
        boolean pistolSuccess = station.executeReforge(state, pistol);
        assertTrue(pistolSuccess, "Reforging Pistol should succeed");
        assertEquals(oldPistolLevel, pistol.getLevel(), "Cấp độ Pistol phải được giữ nguyên sau khi hiệu chỉnh");
        assertTrue(pistol.getCritChance() >= 0.05 && pistol.getCritChance() <= 0.50);
    }

    @Test
    @DisplayName("Verify independent Reroll Stars Only and Reroll Crit Only preserve unrelated stats")
    void testIndependentReforgeStarsAndCrit() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();

        StatReforgeStation station = state.getStatReforgeStation();
        state.setPlayerCredits(1000);

        Weapon activeWeapon = state.getWeaponManager().getActiveWeapon();
        activeWeapon.configureLevel(3); // Level 3: tổng 2 sao thặng dư

        // Ghi nhận chỉ số chí mạng trước khi Reroll Sao
        double fixedCritChance = activeWeapon.getCritChance();
        double fixedCritMult = activeWeapon.getCritMultiplier();

        // 1. Thực thi Reroll Sao Chỉ Số
        Weapon.ReforgeResult starsResult = station.executeReforgeStars(state, activeWeapon);
        assertNotNull(starsResult);
        assertEquals(fixedCritChance, activeWeapon.getCritChance(), 0.0001, "Reroll Sao phải bảo lưu 100% tỉ lệ chí mạng");
        assertEquals(fixedCritMult, activeWeapon.getCritMultiplier(), 0.0001, "Reroll Sao phải bảo lưu 100% hệ số sát thương chí mạng");
        assertEquals(3, activeWeapon.getLevel());
        assertEquals(2, (activeWeapon.getDamageStars() - 1) + (activeWeapon.getFireRateStars() - 1) + (activeWeapon.getAccuracyStars() - 1));

        // Ghi nhận sao trước khi Reroll Chí Mạng
        int fixedDmgStars = activeWeapon.getDamageStars();
        int fixedFrStars = activeWeapon.getFireRateStars();
        int fixedAccStars = activeWeapon.getAccuracyStars();

        // 2. Thực thi Reroll Chí Mạng
        Weapon.ReforgeResult critResult = station.executeReforgeCrit(state, activeWeapon);
        assertNotNull(critResult);
        assertEquals(fixedDmgStars, activeWeapon.getDamageStars(), "Reroll Chí Mạng phải bảo lưu 100% sao Sát thương");
        assertEquals(fixedFrStars, activeWeapon.getFireRateStars(), "Reroll Chí Mạng phải bảo lưu 100% sao Tốc bắn");
        assertEquals(fixedAccStars, activeWeapon.getAccuracyStars(), "Reroll Chí Mạng phải bảo lưu 100% sao Độ chính xác");
        assertTrue(activeWeapon.getCritChance() >= 0.05 && activeWeapon.getCritChance() <= 0.50);
        assertTrue(activeWeapon.getCritMultiplier() >= 1.50 && activeWeapon.getCritMultiplier() <= 2.00);
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
