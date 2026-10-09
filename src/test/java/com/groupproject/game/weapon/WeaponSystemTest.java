package com.groupproject.game.weapon;

import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.entity.weapon.AssaultRifle;
import com.groupproject.game.entity.weapon.CombatKnife;
import com.groupproject.game.entity.weapon.MeleeSlash;
import com.groupproject.game.entity.weapon.Pistol;
import com.groupproject.game.entity.weapon.Shotgun;
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
 * <p>WHY: Guarantees that pistol ammo counting (exactly 7 rounds), AR continuous full-auto
 * (30 rounds, 700 RPM), Shotgun 6-pellet close range spread (8 shells, short lifetime dissipation),
 * melee hitbox generation, and 4-slot weapon switching conform strictly to design requirements.
 */
public class WeaponSystemTest {

    private AssaultRifle ar;
    private Shotgun shotgun;
    private Pistol pistol;
    private CombatKnife knife;
    private ProjectileManager projectileManager;

    @BeforeEach
    void setUp() {
        ar = new AssaultRifle();
        shotgun = new Shotgun();
        pistol = new Pistol();
        knife = new CombatKnife();
        projectileManager = new ProjectileManager();
    }

    @Test
    @DisplayName("Verify Assault Rifle (AR) properties: Slot 1, 30 rounds, 700 RPM (~0.0857s)")
    void testAssaultRifleAttributes() {
        assertEquals(1, ar.getSlotIndex(), "AR must be mapped to slot 1");
        assertEquals(30, ar.getMagazineSize(), "AR magazine capacity must be 30 rounds");
        assertEquals(30, ar.getCurrentAmmo(), "AR must start fully loaded with 30 rounds");
        assertEquals(16, ar.getDamage(), "AR damage per bullet must be 16");
        assertFalse(ar.isReloading(), "AR should not start in reloading state");

        // Verify fire rate corresponds to ~700 RPM
        double expectedCooldown = 60.0 / 700.0;
        assertTrue(ar.attack(100, 100, 0, projectileManager, null));
        assertEquals(29, ar.getCurrentAmmo());

        // Immediately subsequent attack within cooldown window should fail
        assertFalse(ar.attack(100, 100, 0, projectileManager, null),
                "AR cannot fire faster than cooldown interval");

        // Advance time just past cooldown
        ar.update(expectedCooldown + 0.005);
        assertTrue(ar.attack(100, 100, 0, projectileManager, null),
                "AR should fire second round after ~0.0857s cooldown elapsed");
        assertEquals(28, ar.getCurrentAmmo());
    }

    @Test
    @DisplayName("Verify AR expending all 30 rounds automatically initiates reload")
    void testAssaultRifleAutoReload() {
        for (int i = 0; i < 30; i++) {
            ar.update(0.1); // Clear cooldown
            boolean fired = ar.attack(100, 100, 0, projectileManager, null);
            assertTrue(fired, "Shot #" + (i + 1) + " should succeed");
        }

        assertEquals(0, ar.getCurrentAmmo(), "All 30 bullets must be spent");
        assertTrue(ar.isReloading(), "AR must trigger reload after 30th shot");

        // Simulate reload duration (1.8s)
        ar.update(1.9);
        assertFalse(ar.isReloading(), "AR reload should complete");
        assertEquals(30, ar.getCurrentAmmo(), "AR magazine replenished to 30");
    }

    @Test
    @DisplayName("Verify Shotgun properties: Slot 2, 8 shells, 6 pellets per blast")
    void testShotgunAttributesAndPelletSpread() {
        assertEquals(2, shotgun.getSlotIndex(), "Shotgun must be mapped to slot 2");
        assertEquals(8, shotgun.getMagazineSize(), "Shotgun magazine must hold 8 shells");
        assertEquals(8, shotgun.getCurrentAmmo(), "Shotgun must start with 8 shells");
        assertEquals(14, shotgun.getDamage(), "Each pellet deals 14 damage");

        boolean fired = shotgun.attack(100, 100, 0, projectileManager, null);
        assertTrue(fired, "Shotgun must fire when loaded and ready");
        assertEquals(7, shotgun.getCurrentAmmo(), "1 shell consumed");

        // Update PM to integrate pellets from staging buffer
        projectileManager.update(0.016, null);
        assertEquals(Shotgun.PELLETS_PER_SHOT, projectileManager.getCount(),
                "A single shotgun blast must spawn exactly 6 pellets in PM");
    }

    @Test
    @DisplayName("Verify Shotgun pellets dissipate at limited range (lifetime 0.32s)")
    void testShotgunPelletsDissipateAtRange() {
        shotgun.attack(100, 100, 0, projectileManager, null);
        projectileManager.update(0.016, null);
        assertEquals(6, projectileManager.getCount());

        // Advance time past pellet lifetime (0.32s)
        projectileManager.update(0.35, null);
        assertEquals(0, projectileManager.getCount(),
                "All shotgun pellets must expire and dissipate past their close-range lifetime");
    }

    @Test
    @DisplayName("Verify Shotgun shell-by-shell reload and reload cancel upon shooting")
    void testShotgunShellByShellReloadAndCancel() {
        // Bắn 3 viên (từ 8 xuống 5 viên)
        for (int i = 0; i < 3; i++) {
            shotgun.update(0.8); // Cooldown 0.75s
            assertTrue(shotgun.attack(100, 100, 0, projectileManager, null));
        }
        assertEquals(5, shotgun.getCurrentAmmo(), "Shotgun should have 5 shells left");

        // Sau phát bắn thứ 3, người chơi chờ hết cooldown rồi mới bấm R nạp đạn
        shotgun.update(0.8);
        shotgun.reload();
        assertTrue(shotgun.isReloading(), "Shotgun should start reloading");

        // Sau 0.5s (> 0.45s của 1 viên): Đã nạp thêm 1 viên lên 6 viên, và vẫn đang tiếp tục nạp
        shotgun.update(0.5);
        assertEquals(6, shotgun.getCurrentAmmo(), "Shotgun should now have 6 shells");
        assertTrue(shotgun.isReloading(), "Should continue reloading next shell towards 8");

        // Người chơi bấm bắn trong khi đang nạp viên tiếp theo
        // -> Hủy nạp ngay lập tức và bắn thành công!
        boolean fired = shotgun.attack(100, 100, 0, projectileManager, null);
        assertTrue(fired, "Shooting during reload should cancel reload and fire immediate shot");
        assertFalse(shotgun.isReloading(), "Reloading state must be cancelled immediately");
        assertEquals(5, shotgun.getCurrentAmmo(), "Shells should decrement from 6 to 5 after shot");
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
    @DisplayName("Verify semi-auto vs full-auto: AR is automatic, Pistol and Shotgun are semi-automatic")
    void testWeaponFiringModes() {
        assertTrue(ar.isAutomatic(), "Assault Rifle must be full-auto (continuous fire)");
        assertFalse(pistol.isAutomatic(), "Pistol must be semi-auto (click-per-shot)");
        assertFalse(shotgun.isAutomatic(), "Shotgun must be semi-auto");
        assertFalse(knife.isAutomatic(), "Knife must be semi-auto");
    }

    @Test
    @DisplayName("Verify WeaponChest auto-opens when player approaches and collects on interaction [F]")
    void testWeaponChestMechanics() {
        com.groupproject.game.entity.item.WeaponChest chest = new com.groupproject.game.entity.item.WeaponChest(200, 200);
        assertFalse(chest.isOpened(), "Chest starts closed");
        assertFalse(chest.isCollected(), "Chest starts uncollected");

        // Player range checks
        assertTrue(chest.isPlayerInRange(200, 200, 28), "Player on chest should be in range");
        assertFalse(chest.isPlayerInRange(500, 500, 28), "Player far away should be out of range");

        // Khi người chơi chưa đến gần: update ở xa không mở hòm
        chest.update(0.016, 500, 500, 28);
        assertFalse(chest.isOpened(), "Chest remains closed when player is far");

        // Khi người chơi đi lại gần hòm: Hòm TỰ ĐỘNG MỞ và lộ diện vũ khí
        chest.update(0.016, 200, 200, 28);
        assertTrue(chest.isOpened(), "Chest must auto-open when player steps within interaction range");
        assertFalse(chest.isCollected(), "Weapon is revealed but not yet collected into backpack");
        assertNotNull(chest.getDroppedWeapon(), "Weapon inside must be revealed");

        // Người chơi bấm phím tương tác [F] để nhặt vào túi đồ
        com.groupproject.game.entity.weapon.Weapon reward = chest.collect();
        assertTrue(chest.isCollected(), "Chest must be marked as collected after interaction");
        assertNotNull(reward, "Reward weapon must not be null");
        assertTrue(reward.getSlotIndex() == 1 || reward.getSlotIndex() == 2,
                "Reward must be either AR (slot 1) or Shotgun (slot 2)");

        // Nhặt lần 2 sẽ trả về null (tránh dupe súng)
        assertNull(chest.collect(), "Cannot collect multiple times from same chest");
    }

    @Test
    @DisplayName("Verify swapping Primary Weapon slots [1] ⇄ [2] in Backpack")
    void testSwapPrimarySlots() {
        WeaponManager wm = new WeaponManager();

        // 1. Khi cả 2 slot 1 và 2 đều trống, swap trả về false
        assertFalse(wm.swapPrimarySlots(), "Cannot swap when both primary slots are empty");

        // 2. Trang bị AR vào Slot 1 và Shotgun vào Slot 2
        wm.equipWeapon(ar);      // Slot 1
        wm.equipWeapon(shotgun); // Slot 2
        assertEquals(1, ar.getSlotIndex(), "AR should be at slot 1");
        assertEquals(2, shotgun.getSlotIndex(), "Shotgun should be at slot 2");
        assertSame(ar, wm.getWeaponInSlot(1));
        assertSame(shotgun, wm.getWeaponInSlot(2));

        // 3. Thực hiện hoán đổi vị trí Slot 1 và Slot 2
        boolean swapped = wm.swapPrimarySlots();
        assertTrue(swapped, "Swap operation should succeed");

        // Sau khi hoán đổi:
        // Slot 1 giờ chứa Shotgun (slotIndex cập nhật thành 1)
        // Slot 2 giờ chứa AR (slotIndex cập nhật thành 2)
        assertSame(shotgun, wm.getWeaponInSlot(1), "Slot 1 must now hold Shotgun");
        assertSame(ar, wm.getWeaponInSlot(2), "Slot 2 must now hold AR");
        assertEquals(1, shotgun.getSlotIndex(), "Shotgun slotIndex must be updated to 1");
        assertEquals(2, ar.getSlotIndex(), "AR slotIndex must be updated to 2");

        // Kiểm tra phím chọn slot hoạt động chính xác với cấu hình mới
        wm.selectSlot(1);
        assertSame(shotgun, wm.getActiveWeapon(), "Selecting slot 1 must now equip Shotgun");
        wm.selectSlot(2);
        assertSame(ar, wm.getActiveWeapon(), "Selecting slot 2 must now equip AR");

        // 4. Hoán đổi ngược lại trở về ban đầu
        assertTrue(wm.swapPrimarySlots());
        assertSame(ar, wm.getWeaponInSlot(1));
        assertSame(shotgun, wm.getWeaponInSlot(2));
        assertEquals(1, ar.getSlotIndex());
        assertEquals(2, shotgun.getSlotIndex());
    }

    @Test
    @DisplayName("Verify WeaponManager starting loadout (Slot 3 Pistol, Slot 4 Knife) and chest equip")
    void testWeaponManagerSwitching() {
        WeaponManager wm = new WeaponManager();

        // Khởi đầu người chơi chỉ có súng lục (Slot 3) và dao găm (Slot 4)
        assertEquals(3, wm.getActiveWeapon().getSlotIndex(), "Khởi đầu người chơi cầm Súng lục (Slot 3)");
        assertNull(wm.getWeaponInSlot(1), "Chưa sở hữu vũ khí chính Slot 1 (AR)");
        assertNull(wm.getWeaponInSlot(2), "Chưa sở hữu vũ khí chính Slot 2 (Shotgun)");

        // Chuyển sang dao găm (Slot 4)
        wm.selectSlot(4);
        assertEquals(4, wm.getActiveWeapon().getSlotIndex(), "Active weapon should be Knife (Slot 4)");

        // Quick swap về súng lục (Slot 3)
        wm.quickSwap();
        assertEquals(3, wm.getActiveWeapon().getSlotIndex(), "Quick swap should return to Pistol (Slot 3)");

        // Khi nhặt hòm tiếp tế rơi ra AR
        wm.equipWeapon(ar);
        assertEquals(1, wm.getActiveWeapon().getSlotIndex(), "Vũ khí nhặt được lập tức được trang bị (Slot 1 AR)");
        assertNotNull(wm.getWeaponInSlot(1), "Slot 1 AR đã được đăng ký vào kho");

        // Khi nhặt thêm Shotgun
        wm.equipWeapon(shotgun);
        assertEquals(2, wm.getActiveWeapon().getSlotIndex(), "Vũ khí nhặt tiếp được trang bị (Slot 2 Shotgun)");

        // Đổi qua lại giữa các slot đã sở hữu
        wm.selectSlot(1);
        assertEquals(1, wm.getActiveWeapon().getSlotIndex());
        wm.selectSlot(3);
        assertEquals(3, wm.getActiveWeapon().getSlotIndex());
        wm.selectSlot(4);
        assertEquals(4, wm.getActiveWeapon().getSlotIndex());
    }
}
