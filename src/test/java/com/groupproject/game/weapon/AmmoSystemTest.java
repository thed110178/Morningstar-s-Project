package com.groupproject.game.weapon;

import com.groupproject.game.entity.item.AmmoInventory;
import com.groupproject.game.entity.item.AmmoPickup;
import com.groupproject.game.entity.item.AmmoType;
import com.groupproject.game.entity.weapon.AssaultRifle;
import com.groupproject.game.entity.weapon.Pistol;
import com.groupproject.game.entity.weapon.SMG;
import com.groupproject.game.entity.weapon.Shotgun;
import com.groupproject.game.entity.weapon.WeaponManager;
import com.groupproject.game.state.GameStateManager;
import com.groupproject.game.state.PlayingState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động toàn diện cho Hệ thống Đạn dược Dự trữ (Reserve Ammo System).
 *
 * <p>WHY: Đảm bảo tính toán chính xác 100% dung lượng kho đạn (AR: 150, SMG: 180, Shotgun: 50),
 * stack rơi thưởng (AR: 30, SMG: 35, Shotgun: 10), cơ chế súng lục vô hạn đạn, súng chính
 * phụ thuộc đạn dự trữ, cơ chế nạp từng viên của Shotgun, và hộp đạn pickup hút tự động.
 */
public class AmmoSystemTest {

    private AmmoInventory inventory;
    private com.groupproject.game.entity.projectile.ProjectileManager pm;

    @BeforeEach
    void setUp() {
        inventory = new AmmoInventory();
        pm = new com.groupproject.game.entity.projectile.ProjectileManager();
    }

    @Test
    @DisplayName("Verify starting ammo reserves are 0 and capacities match design specification")
    void testInitialCapacitiesAndStacks() {
        assertEquals(0, inventory.getAmmo(AmmoType.AR));
        assertEquals(0, inventory.getAmmo(AmmoType.SMG));
        assertEquals(0, inventory.getAmmo(AmmoType.SHOTGUN));

        assertEquals(150, inventory.getMaxCapacity(AmmoType.AR));
        assertEquals(180, inventory.getMaxCapacity(AmmoType.SMG));
        assertEquals(50, inventory.getMaxCapacity(AmmoType.SHOTGUN));

        assertEquals(30, AmmoType.AR.getStackAmount());
        assertEquals(35, AmmoType.SMG.getStackAmount());
        assertEquals(10, AmmoType.SHOTGUN.getStackAmount());
    }

    @Test
    @DisplayName("Verify adding ammo respects max capacities and clamps properly")
    void testAddAmmoClamping() {
        int added1 = inventory.addAmmo(AmmoType.AR, 100);
        assertEquals(100, added1);
        assertEquals(100, inventory.getAmmo(AmmoType.AR));
        assertFalse(inventory.isFull(AmmoType.AR));

        int added2 = inventory.addAmmo(AmmoType.AR, 80);
        assertEquals(50, added2); // Chỉ thêm được 50 để đạt trần 150
        assertEquals(150, inventory.getAmmo(AmmoType.AR));
        assertTrue(inventory.isFull(AmmoType.AR));

        // Thêm tiếp khi đã đầy -> 0 viên được thêm
        assertEquals(0, inventory.addAmmo(AmmoType.AR, 20));
        assertEquals(150, inventory.getAmmo(AmmoType.AR));
    }

    @Test
    @DisplayName("Verify consuming ammo draws correct amount and clamps to 0")
    void testConsumeAmmo() {
        inventory.setAmmo(AmmoType.SMG, 100);

        int drawn = inventory.consumeAmmo(AmmoType.SMG, 35);
        assertEquals(35, drawn);
        assertEquals(65, inventory.getAmmo(AmmoType.SMG));

        int drawnAll = inventory.consumeAmmo(AmmoType.SMG, 200);
        assertEquals(65, drawnAll);
        assertEquals(0, inventory.getAmmo(AmmoType.SMG));
    }

    @Test
    @DisplayName("Verify Pistol has infinite reserve ammo and reloads without consuming inventory")
    void testPistolInfiniteReserveAmmo() {
        Pistol pistol = new Pistol();
        pistol.bindAmmoInventory(inventory);

        assertTrue(pistol.hasInfiniteAmmo());
        assertEquals(-1, pistol.getReserveAmmo());

        // Bắn hết 7 viên
        for (int i = 0; i < 7; i++) {
            pistol.update(0.15);
            assertTrue(pistol.attack(0, 0, 0, pm, null));
        }
        assertEquals(0, pistol.getCurrentAmmo());
        assertTrue(pistol.isReloading());

        // Sau 1.2s nạp đạn, súng hồi 7 viên mà không tiêu tốn đạn kho
        pistol.update(1.25);
        assertEquals(7, pistol.getCurrentAmmo());
        assertFalse(pistol.isReloading());
        assertEquals(0, inventory.getAmmo(AmmoType.AR));
        assertEquals(0, inventory.getAmmo(AmmoType.SMG));
        assertEquals(0, inventory.getAmmo(AmmoType.SHOTGUN));
    }

    @Test
    @DisplayName("Verify Assault Rifle cannot reload when reserve is empty, and draws from reserve on reload")
    void testAssaultRifleReloadWithReserve() {
        AssaultRifle ar = new AssaultRifle();
        ar.bindAmmoInventory(inventory);

        assertEquals(AmmoType.AR, ar.getAmmoType());
        assertFalse(ar.hasInfiniteAmmo());

        // 1. Khi kho dự trữ = 0: Hết đạn trong băng không thể reload
        for (int i = 0; i < 30; i++) {
            ar.update(0.1);
            assertTrue(ar.attack(0, 0, 0, pm, null));
        }
        assertEquals(0, ar.getCurrentAmmo());
        // Do kho = 0, reload() bị từ chối
        assertFalse(ar.isReloading());

        // 2. Bổ sung 40 viên AR vào kho dự trữ
        inventory.addAmmo(AmmoType.AR, 40);
        assertEquals(40, inventory.getAmmo(AmmoType.AR));
        assertEquals(40, ar.getReserveAmmo());

        // Kích hoạt reload
        ar.reload();
        assertTrue(ar.isReloading());

        // Hoàn thành reload sau 1.8s
        ar.update(1.85);
        assertFalse(ar.isReloading());
        assertEquals(30, ar.getCurrentAmmo()); // Nạp đầy băng 30 viên
        assertEquals(10, inventory.getAmmo(AmmoType.AR)); // Còn lại 40 - 30 = 10 viên trong kho
    }

    @Test
    @DisplayName("Verify Assault Rifle partial reload when reserve ammo is less than full magazine")
    void testAssaultRiflePartialReload() {
        AssaultRifle ar = new AssaultRifle();
        ar.bindAmmoInventory(inventory);

        // Kho chỉ có 12 viên AR
        inventory.setAmmo(AmmoType.AR, 12);

        // Súng đang có 0 viên
        for (int i = 0; i < 30; i++) {
            ar.update(0.1);
            ar.attack(0, 0, 0, pm, null);
        }
        assertEquals(0, ar.getCurrentAmmo());

        ar.reload();
        assertTrue(ar.isReloading());

        ar.update(1.85);
        assertEquals(12, ar.getCurrentAmmo()); // Nạp được 12 viên
        assertEquals(0, inventory.getAmmo(AmmoType.AR)); // Kho về 0
    }

    @Test
    @DisplayName("Verify Shotgun shell-by-shell reload consumes 1 reserve shell per cycle and stops when empty")
    void testShotgunShellByShellReload() {
        Shotgun sg = new Shotgun();
        sg.bindAmmoInventory(inventory);

        assertEquals(AmmoType.SHOTGUN, sg.getAmmoType());

        // 1. Khi kho = 0, không thể reload
        for (int i = 0; i < 8; i++) {
            sg.update(0.8);
            sg.attack(0, 0, 0, pm, null);
        }
        assertEquals(0, sg.getCurrentAmmo());
        assertFalse(sg.isReloading());

        // 2. Thêm đúng 2 viên Shotgun vào kho
        inventory.setAmmo(AmmoType.SHOTGUN, 2);

        sg.reload();
        assertTrue(sg.isReloading());

        // Sau 0.45s viên 1 nạp xong -> súng có 1 viên, kho còn 1 viên
        sg.update(0.46);
        assertEquals(1, sg.getCurrentAmmo());
        assertEquals(1, inventory.getAmmo(AmmoType.SHOTGUN));
        assertTrue(sg.isReloading()); // Tiếp tục nạp viên 2

        // Sau 0.45s tiếp theo viên 2 nạp xong -> súng có 2 viên, kho về 0 viên
        sg.update(0.46);
        assertEquals(2, sg.getCurrentAmmo());
        assertEquals(0, inventory.getAmmo(AmmoType.SHOTGUN));
        // Kho đã hết -> Tự động dừng nạp đạn
        assertFalse(sg.isReloading());
    }

    @Test
    @DisplayName("Verify AmmoPickup auto-collects when player moves within proximity")
    void testAmmoPickupProximityCollection() {
        AmmoPickup pickup = new AmmoPickup(100.0, 100.0, AmmoType.SMG, 35);
        assertFalse(pickup.isCollected());

        // Người chơi ở xa (500, 500) -> Không nhặt
        pickup.update(0.016, 500.0, 500.0, 28.0, inventory, null);
        assertFalse(pickup.isCollected());
        assertEquals(0, inventory.getAmmo(AmmoType.SMG));

        // Người chơi bước vào phạm vi (105, 105) (khoảng cách ~7px < 38px) -> Tự động nhặt
        pickup.update(0.016, 105.0, 105.0, 28.0, inventory, null);
        assertTrue(pickup.isCollected());
        assertEquals(35, inventory.getAmmo(AmmoType.SMG));
    }

    @Test
    @DisplayName("Verify room clearance spawns exactly 2 ammo stack pickups with valid stack amounts")
    void testRoomClearSpawnsTwoAmmoStacks() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);

        assertEquals(0, state.getAmmoPickups().size());

        // Kích hoạt rơi 2 stack khi clear phòng tại tọa độ (200, 200)
        state.spawnClearRoomAmmoStacks(200.0, 200.0);

        assertEquals(2, state.getAmmoPickups().size());

        AmmoPickup p1 = state.getAmmoPickups().get(0);
        AmmoPickup p2 = state.getAmmoPickups().get(1);
        assertEquals(200.0 - 58.0, p1.getX(), 0.001);
        assertEquals(200.0 + 58.0, p2.getX(), 0.001);
        assertEquals(200.0, p1.getY(), 0.001);
        assertEquals(200.0, p2.getY(), 0.001);
        assertEquals(200.0, (p1.getX() + p2.getX()) / 2.0, 0.001,
                "Chest center must be exactly in the middle of both ammo stacks");

        for (AmmoPickup p : state.getAmmoPickups()) {
            assertNotNull(p.getType());
            assertTrue(p.getAmount() == 30 || p.getAmount() == 35 || p.getAmount() == 10);
            assertEquals(p.getType().getStackAmount(), p.getAmount());
        }
    }

    @Test
    @DisplayName("Verify WeaponManager resupplyAllWeapons provides 2 stacks for all ammo types and refills magazines")
    void testShopResupplyAllProvidesTwoStacks() {
        WeaponManager wm = new WeaponManager();
        AssaultRifle ar = new AssaultRifle();
        SMG smg = new SMG(2); // Slot 2

        wm.addWeapon(ar);
        wm.addWeapon(smg);

        // Ban đầu kho = 0
        assertEquals(0, wm.getAmmoInventory().getAmmo(AmmoType.AR));
        assertEquals(0, wm.getAmmoInventory().getAmmo(AmmoType.SMG));
        assertEquals(0, wm.getAmmoInventory().getAmmo(AmmoType.SHOTGUN));

        // Bắn 1 phát mỗi khẩu
        ar.attack(0, 0, 0, pm, null);
        smg.attack(0, 0, 0, pm, null);

        assertEquals(29, ar.getCurrentAmmo());
        assertEquals(24, smg.getCurrentAmmo());

        // Gọi tiếp tế quầy đạn tại Shop (+2 stack cho mỗi loại: AR=60, SMG=70, SG=20)
        wm.resupplyAllWeapons();

        // Kho dự trữ nhận đúng 2 stack mỗi loại
        assertEquals(60, wm.getAmmoInventory().getAmmo(AmmoType.AR));
        assertEquals(70, wm.getAmmoInventory().getAmmo(AmmoType.SMG));
        assertEquals(20, wm.getAmmoInventory().getAmmo(AmmoType.SHOTGUN));

        // Băng đạn của các súng cũng được nạp đầy
        assertEquals(ar.getMagazineSize(), ar.getCurrentAmmo());
        assertEquals(smg.getMagazineSize(), smg.getCurrentAmmo());
    }
}
