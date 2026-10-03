package com.groupproject.game.weapon;

import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.entity.weapon.CombatKnife;
import com.groupproject.game.entity.weapon.MeleeSlash;
import com.groupproject.game.entity.weapon.Pistol;
import com.groupproject.game.entity.weapon.WeaponManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying Weapon attributes, firing mechanics, ammunition, and slot switching.
 *
 * <p>WHY: Guarantees that pistol ammo counting (exactly 7 rounds), auto-reloading, melee hitbox
 * generation, and hotkey switching conform strictly to design requirements without edge-case regressions.
 */
public class WeaponSystemTest {

    private Pistol pistol;
    private CombatKnife knife;
    private ProjectileManager projectileManager;

    @BeforeEach
    void setUp() {
        pistol = new Pistol();
        knife = new CombatKnife();
        projectileManager = new ProjectileManager();
    }

    @Test
    @DisplayName("Verify Pistol initializes with exactly 7 rounds in magazine")
    void testPistolInitialAmmo() {
        assertEquals(7, pistol.getMagazineSize(), "Pistol magazine must hold 7 rounds");
        assertEquals(7, pistol.getCurrentAmmo(), "Pistol must start fully loaded");
        assertFalse(pistol.isReloading(), "Pistol should not be reloading at start");
        assertEquals(3, pistol.getSlotIndex(), "Pistol must be mapped to slot 3");
    }

    @Test
    @DisplayName("Verify firing Pistol spawns bullet and decrements ammo count")
    void testPistolFiring() {
        boolean fired = pistol.attack(100, 100, 0, projectileManager, null);
        assertTrue(fired, "Pistol must fire successfully when loaded and off cooldown");
        assertEquals(6, pistol.getCurrentAmmo(), "Ammo should decrement to 6");

        // Update PM to integrate bullet from staging buffer
        projectileManager.update(0.016, null);
        assertEquals(1, projectileManager.getCount(), "1 bullet should be active in projectile manager");
    }

    @Test
    @DisplayName("Verify Pistol triggers automatic reload after expending all 7 rounds")
    void testPistolAutoReloadAfterSevenShots() {
        // Fire 7 rounds consecutively (fast-forwarding cooldown)
        for (int i = 0; i < 7; i++) {
            pistol.update(0.3); // Clear fire rate cooldown
            boolean fired = pistol.attack(100, 100, 0, projectileManager, null);
            assertTrue(fired, "Shot #" + (i + 1) + " should be successful");
        }

        assertEquals(0, pistol.getCurrentAmmo(), "Ammo should be exhausted after 7 shots");
        assertTrue(pistol.isReloading(), "Pistol must automatically begin reload when reaching 0 rounds");

        // Cannot attack while reloading
        boolean firedDuringReload = pistol.attack(100, 100, 0, projectileManager, null);
        assertFalse(firedDuringReload, "Should not be able to fire while reloading");

        // Simulate reload completion (reload time is 1.2s)
        pistol.update(1.3);
        assertFalse(pistol.isReloading(), "Reload should be finished");
        assertEquals(7, pistol.getCurrentAmmo(), "Magazine should be replenished to 7 rounds");
    }

    @Test
    @DisplayName("Verify Combat Knife is melee, requires no ammo, and spawns slash hitbox")
    void testCombatKnifeMechanics() {
        assertTrue(knife.isMelee(), "Knife must be categorized as melee");
        assertEquals(0, knife.getMagazineSize(), "Knife has no magazine");
        assertEquals(4, knife.getSlotIndex(), "Knife must be mapped to slot 4");

        List<MeleeSlash> slashes = new ArrayList<>();
        boolean attacked = knife.attack(200, 200, Math.PI / 4, projectileManager, slashes);

        assertTrue(attacked, "Knife attack should succeed");
        assertEquals(1, slashes.size(), "Attack must spawn exactly 1 melee slash hitbox");

        MeleeSlash slash = slashes.get(0);
        assertTrue(slash.isActive(), "Slash hitbox must be initially active");
        assertTrue(slash.getDamage() > 0, "Slash must carry damage payload");
    }

    @Test
    @DisplayName("Verify WeaponManager slot switching and Quick-Swap (Q)")
    void testWeaponManagerSwitching() {
        WeaponManager wm = new WeaponManager();

        // Default active weapon is Pistol (Slot 3)
        assertEquals(3, wm.getActiveWeapon().getSlotIndex());

        // Select Knife (Slot 4)
        wm.selectSlot(4);
        assertEquals(4, wm.getActiveWeapon().getSlotIndex());

        // Quick swap (Q) back to Pistol
        wm.quickSwap();
        assertEquals(3, wm.getActiveWeapon().getSlotIndex());

        // Quick swap again back to Knife
        wm.quickSwap();
        assertEquals(4, wm.getActiveWeapon().getSlotIndex());
    }
}
