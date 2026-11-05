package com.groupproject.game.weapon;

import com.groupproject.game.state.GameStateManager;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.entity.weapon.WeaponManager;
import com.groupproject.game.state.PlayingState;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying the Boss Fight weapon loadout mechanics:
 * Spawns 2 distinct primary weapons at Level 3 without duplicates.
 */
class BossFightLoadoutTest {

    private GameStateManager gsm;
    private PlayingState playingState;

    @BeforeEach
    void setUp() {
        gsm = new GameStateManager();
        playingState = new PlayingState(gsm);
        playingState.init();
    }

    @Test
    @DisplayName("Verify Boss Fight equips 2 distinct primary weapons at Level 3 in Slot 1 and Slot 2")
    void testBossFightEquipsTwoDistinctWeaponsAtLevel3() {
        playingState.loadBossStageDirectly();
        playingState.skipBossIntro();

        WeaponManager wm = playingState.getWeaponManager();
        assertNotNull(wm, "WeaponManager must not be null");

        Weapon w1 = wm.getWeapon(1);
        Weapon w2 = wm.getWeapon(2);

        assertNotNull(w1, "Primary weapon in Slot 1 must be present");
        assertNotNull(w2, "Primary weapon in Slot 2 must be present");

        assertEquals(3, w1.getLevel(), "Slot 1 weapon must be configured at Level 3");
        assertEquals(3, w2.getLevel(), "Slot 2 weapon must be configured at Level 3");

        assertNotEquals(w1.getClass(), w2.getClass(),
                "Slot 1 and Slot 2 weapons must be of different types (no duplicate weapons)");

        assertEquals(1, w1.getSlotIndex(), "Slot 1 weapon must have slotIndex = 1");
        assertEquals(2, w2.getSlotIndex(), "Slot 2 weapon must have slotIndex = 2");

        assertSame(w1, wm.getActiveWeapon(), "Slot 1 weapon should be actively selected on start");
        assertTrue(w1.getCurrentAmmo() > 0, "Slot 1 weapon must have loaded magazine");
        assertTrue(w2.getCurrentAmmo() > 0, "Slot 2 weapon must have loaded magazine");
    }

    @Test
    @DisplayName("Verify randomness and zero duplicates across 100 consecutive Boss Fight generations")
    void testRandomnessAndZeroDuplicatesAcrossMultipleRuns() {
        Set<Class<? extends Weapon>> observedTypes = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            List<Weapon> weapons = playingState.generateRandomBossFightWeapons();
            assertNotNull(weapons, "Generated weapon list must not be null");
            assertEquals(2, weapons.size(), "Must generate exactly 2 weapons");

            Weapon w1 = weapons.get(0);
            Weapon w2 = weapons.get(1);

            assertEquals(3, w1.getLevel(), "Weapon 1 must be Level 3");
            assertEquals(3, w2.getLevel(), "Weapon 2 must be Level 3");

            assertEquals(1, w1.getSlotIndex(), "Weapon 1 must be assigned to Slot 1");
            assertEquals(2, w2.getSlotIndex(), "Weapon 2 must be assigned to Slot 2");

            assertNotEquals(w1.getClass(), w2.getClass(),
                    "Iteration " + i + ": Weapon types must never be identical");

            observedTypes.add(w1.getClass());
            observedTypes.add(w2.getClass());
        }

        // Với 6 loại súng (AR, SG, SMG, Sniper, SAS, SCR), qua 100 lượt quay ngẫu nhiên phải xuất hiện đa dạng chủng loại (ít nhất 5 loại)
        assertTrue(observedTypes.size() >= 5,
                "Random generation should yield diverse weapon types across runs. Observed: " + observedTypes.size());
    }

    @Test
    @DisplayName("Verify full ammo reserves are resupplied for both generated weapons")
    void testResupplyAmmoPackageProvidesReserves() {
        playingState.loadBossStageDirectly();
        playingState.skipBossIntro();

        WeaponManager wm = playingState.getWeaponManager();
        Weapon w1 = wm.getWeapon(1);
        Weapon w2 = wm.getWeapon(2);

        assertTrue(w1.getReserveAmmo() > 0, "Slot 1 weapon should have positive reserve ammunition");
        assertTrue(w2.getReserveAmmo() > 0, "Slot 2 weapon should have positive reserve ammunition");
    }
}
