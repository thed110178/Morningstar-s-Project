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
        assertEquals(0, inventory.getAmmo(AmmoType.SR));

        assertEquals(150, inventory.getMaxCapacity(AmmoType.AR));
        assertEquals(180, inventory.getMaxCapacity(AmmoType.SMG));
        assertEquals(50, inventory.getMaxCapacity(AmmoType.SHOTGUN));
        assertEquals(30, inventory.getMaxCapacity(AmmoType.SR));

        assertEquals(30, AmmoType.AR.getStackAmount());
        assertEquals(35, AmmoType.SMG.getStackAmount());
        assertEquals(10, AmmoType.SHOTGUN.getStackAmount());
        assertEquals(5, AmmoType.SR.getStackAmount());

        assertEquals("Đạn súng trường (Rifle ammo)", AmmoType.AR.getDisplayName());
        assertEquals("Đạn bắn tỉa SR (Sniper ammo)", AmmoType.SR.getDisplayName());
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
        assertTrue(com.groupproject.game.core.GameSoundManager.isSoundLoaded(com.groupproject.game.core.GameSoundManager.SoundEffect.AMMO_PICKUP),
                "AMMO_PICKUP sound must be loaded and ready");
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
            assertTrue(p.getAmount() == 30 || p.getAmount() == 35 || p.getAmount() == 10 || p.getAmount() == 5);
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
        assertEquals(0, wm.getAmmoInventory().getAmmo(AmmoType.SR));

        // Bắn 1 phát mỗi khẩu
        ar.attack(0, 0, 0, pm, null);
        smg.attack(0, 0, 0, pm, null);

        assertEquals(29, ar.getCurrentAmmo());
        assertEquals(24, smg.getCurrentAmmo());

        // Gọi tiếp tế quầy đạn tại Shop (+2 stack cho mỗi loại: AR=60, SMG=70, SG=20, SR=10)
        wm.resupplyAllWeapons();

        // Kho dự trữ nhận đúng 2 stack mỗi loại
        assertEquals(60, wm.getAmmoInventory().getAmmo(AmmoType.AR));
        assertEquals(70, wm.getAmmoInventory().getAmmo(AmmoType.SMG));
        assertEquals(20, wm.getAmmoInventory().getAmmo(AmmoType.SHOTGUN));
        assertEquals(10, wm.getAmmoInventory().getAmmo(AmmoType.SR));

        // Băng đạn của các súng cũng được nạp đầy
        assertEquals(ar.getMagazineSize(), ar.getCurrentAmmo());
        assertEquals(smg.getMagazineSize(), smg.getCurrentAmmo());
    }

    @Test
    @DisplayName("Verify AmmoType standard colors match AmmoPickup glow colors across all calibers")
    void testAmmoTypeColorsAndGlowAlignment() {
        assertEquals(new java.awt.Color(80, 220, 100), AmmoType.AR.getColor(), "AR ammo must be military green");
        assertEquals(new java.awt.Color(0, 210, 255), AmmoType.SMG.getColor(), "SMG ammo must be cyan");
        assertEquals(new java.awt.Color(255, 90, 70), AmmoType.SHOTGUN.getColor(), "Shotgun ammo must be fiery red-orange");
        assertEquals(new java.awt.Color(185, 80, 255), AmmoType.SR.getColor(), "SR ammo must be electric violet");

        AmmoPickup pAR = new AmmoPickup(0, 0, AmmoType.AR, 30);
        AmmoPickup pSMG = new AmmoPickup(0, 0, AmmoType.SMG, 35);
        AmmoPickup pSG = new AmmoPickup(0, 0, AmmoType.SHOTGUN, 10);
        AmmoPickup pSR = new AmmoPickup(0, 0, AmmoType.SR, 5);

        assertEquals(AmmoType.AR.getColor(), pAR.getGlowColor());
        assertEquals(AmmoType.SMG.getColor(), pSMG.getGlowColor());
        assertEquals(AmmoType.SHOTGUN.getColor(), pSG.getGlowColor());
        assertEquals(AmmoType.SR.getColor(), pSR.getGlowColor());
    }

    @Test
    @DisplayName("Verify isOutOfAmmo logic on Weapon, WeaponManager, and Player out of ammo warning")
    void testOutOfAmmoWarningMechanics() {
        WeaponManager wm = new WeaponManager();
        AssaultRifle ar = new AssaultRifle();
        wm.equipWeapon(ar); // Trang bị vào Slot 1

        // 1. Ban đầu: Băng đạn đầy (30 viên), kho dự trữ = 0 -> chưa hết sạch đạn (vẫn còn đạn trong băng)
        assertEquals(30, ar.getCurrentAmmo());
        assertEquals(0, wm.getAmmoInventory().getAmmo(AmmoType.AR));
        assertFalse(ar.isOutOfAmmo(), "Weapon with rounds in magazine must not be out of ammo");
        assertFalse(wm.isCurrentWeaponOutOfAmmo(), "WeaponManager must reflect active weapon ammo status");

        // 2. Bắn hết đạn trong băng (30 viên) và kho dự trữ = 0 -> HẾT SẠCH ĐẠN!
        for (int i = 0; i < 30; i++) {
            ar.attack(0, 0, 0, pm, wm.getActiveSlashes());
            ar.update(0.1); // hoàn thành cooldown bắn
        }
        assertEquals(0, ar.getCurrentAmmo());
        assertEquals(0, wm.getAmmoInventory().getAmmo(AmmoType.AR));
        assertTrue(ar.isOutOfAmmo(), "Weapon with 0 magazine and 0 reserve must be out of ammo");
        assertTrue(wm.isCurrentWeaponOutOfAmmo(), "WeaponManager must report current weapon is out of ammo");

        // 3. Nhặt đạn hoặc nhận tiếp tế -> Ngay lập tức không còn hết sạch đạn
        wm.getAmmoInventory().addAmmo(AmmoType.AR, 30);
        assertEquals(30, wm.getAmmoInventory().getAmmo(AmmoType.AR));
        assertFalse(ar.isOutOfAmmo(), "Weapon with reserve ammo available must not be considered out of ammo");
        assertFalse(wm.isCurrentWeaponOutOfAmmo());

        // 4. Đổi sang Pistol (vô hạn đạn) hoặc CombatKnife (cận chiến) -> không bao giờ hết sạch đạn
        Pistol pistol = new Pistol();
        wm.equipWeapon(pistol);
        assertFalse(pistol.isOutOfAmmo(), "Pistol with infinite ammo reserve must never be out of ammo");

        com.groupproject.game.entity.weapon.CombatKnife knife = new com.groupproject.game.entity.weapon.CombatKnife();
        wm.equipWeapon(knife);
        assertFalse(knife.isOutOfAmmo(), "Melee weapon must never be out of ammo");

        // 5. Kiểm tra Player.renderOutOfAmmoWarning không ném ngoại lệ khi vẽ
        com.groupproject.game.entity.player.Player player = new com.groupproject.game.entity.player.Player(100, 100);
        com.groupproject.game.core.Camera camera = new com.groupproject.game.core.Camera(800, 600);
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(800, 600, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = img.createGraphics();

        assertDoesNotThrow(() -> player.renderOutOfAmmoWarning(g2d, camera, true));
        assertDoesNotThrow(() -> player.renderOutOfAmmoWarning(g2d, camera, false));
        g2d.dispose();
    }

    @Test
    @DisplayName("Verify Sniper Rifle consumes AmmoType.SR with 30 max capacity and 5 mag capacity")
    void testSniperRifleAmmoConsumptionAndBackpackCapacity() {
        WeaponManager wm = new WeaponManager();
        com.groupproject.game.entity.weapon.SniperRifle sr = new com.groupproject.game.entity.weapon.SniperRifle();
        wm.equipWeapon(sr);

        assertEquals(AmmoType.SR, sr.getAmmoType());
        assertEquals(5, sr.getMagazineSize());
        assertEquals(5, sr.getCurrentAmmo());
        assertEquals(0, wm.getAmmoInventory().getAmmo(AmmoType.SR));
        assertEquals(30, wm.getAmmoInventory().getMaxCapacity(AmmoType.SR));

        // Thêm đạn SR vào kho balô
        wm.getAmmoInventory().addAmmo(AmmoType.SR, 20);
        assertEquals(20, wm.getAmmoInventory().getAmmo(AmmoType.SR));

        // Bắn hết 5 viên
        for (int i = 0; i < 5; i++) {
            sr.update(1.0); // hoàn thành cooldown kéo quy lát
            assertTrue(sr.attack(0, 0, 0, pm, null));
        }
        assertEquals(0, sr.getCurrentAmmo());
        assertTrue(sr.isReloading());

        // Nạp đạn xong -> rút đúng 5 viên từ kho balô (20 - 5 = 15)
        sr.update(2.5);
        assertEquals(5, sr.getCurrentAmmo());
        assertEquals(15, wm.getAmmoInventory().getAmmo(AmmoType.SR));
        assertFalse(sr.isReloading());
    }

    @Test
    @DisplayName("Verify AmmoPickup leaves surplus ammo on ground when backpack reaches capacity")
    void testAmmoPickupLeavesSurplusWhenInventoryFull() {
        // Player backpack có sức chứa AR tối đa là 150
        // Set ammo inventory gần đầy: 140/150 (chỉ còn chỗ chứa 10 viên)
        inventory.setAmmo(AmmoType.AR, 140);

        // Tạo hộp đạn AR 30 viên ở vị trí người chơi
        AmmoPickup pickup = new AmmoPickup(100, 100, AmmoType.AR, 30);
        assertFalse(pickup.isCollected());
        assertEquals(30, pickup.getAmount());

        // Cập nhật pickup với vị trí player trùng khớp
        pickup.update(0.016, 100.0, 100.0, 32.0, inventory, null);

        // Kho balô nhận 10 viên để đạt tối đa 150
        assertEquals(150, inventory.getAmmo(AmmoType.AR));
        assertTrue(inventory.isFull(AmmoType.AR));

        // Hộp đạn KHÔNG biến mất (collected = false), và số lượng còn lại là 20 viên
        assertFalse(pickup.isCollected(), "Hộp đạn thừa không được biến mất");
        assertEquals(20, pickup.getAmount(), "Hộp đạn phải giữ lại 20 viên thừa trên mặt đất");

        // Khi người chơi quay lại chạm vào hộp đạn một lần nữa khi kho vẫn đầy 150/150:
        pickup.update(0.016, 100.0, 100.0, 32.0, inventory, null);
        assertFalse(pickup.isCollected());
        assertEquals(20, pickup.getAmount());
        assertEquals(150, inventory.getAmmo(AmmoType.AR));

        // Nếu người chơi tiêu thụ bớt 15 viên (còn 135/150) rồi chạm lại:
        inventory.consumeAmmo(AmmoType.AR, 15);
        assertEquals(135, inventory.getAmmo(AmmoType.AR));
        pickup.update(0.016, 100.0, 100.0, 32.0, inventory, null);

        // Kho balô nhận 15 viên (đạt 150/150), hộp đạn còn lại 5 viên thừa trên mặt đất
        assertEquals(150, inventory.getAmmo(AmmoType.AR));
        assertFalse(pickup.isCollected());
        assertEquals(5, pickup.getAmount());

        // Tiêu thụ tiếp 10 viên (còn 140/150) rồi nhặt:
        inventory.consumeAmmo(AmmoType.AR, 10);
        pickup.update(0.016, 100.0, 100.0, 32.0, inventory, null);

        // Hộp đạn 5 viên được thu nạp toàn bộ, hộp đạn biến mất (collected = true)
        assertEquals(145, inventory.getAmmo(AmmoType.AR));
        assertTrue(pickup.isCollected(), "Khi nhặt hết sạch đạn thừa thì hộp đạn mới biến mất");
    }

    @Test
    @DisplayName("Verify weapon replacement reclaims magazine ammo into backpack and drops surplus onto ground")
    void testReplacedWeaponAmmoReturnedToBackpackAndSurplusDropped() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);

        // Ban đầu người chơi trang bị Slot 1 (AR) và Slot 2 (Shotgun)
        WeaponManager wm = state.getWeaponManager();
        AssaultRifle ar = new AssaultRifle();
        Shotgun sg = new Shotgun();
        wm.equipWeapon(ar); // slot 1
        wm.equipWeapon(sg); // slot 2
        wm.selectSlot(1);

        // Băng đạn của AR có 30 viên.
        assertEquals(30, ar.getCurrentAmmo());

        // Đặt kho dự trữ AR gần đầy: 140/150 viên (chỉ còn chỗ chứa 10 viên)
        wm.getAmmoInventory().setAmmo(AmmoType.AR, 140);
        int initialPickupsCount = state.getAmmoPickups().size();

        // Thu hồi đạn khi AR bị thay thế
        PlayingState.ReclaimedAmmoResult result =
                state.reclaimReplacedWeaponAmmo(ar, 300.0, 400.0);

        assertEquals(AmmoType.AR, result.type);
        assertEquals(10, result.addedToBackpack, "Kho balô chỉ nhận được 10 viên để đạt 150/150");
        assertEquals(20, result.droppedSurplus, "20 viên đạn thừa trong băng phải rơi xuống đất");

        assertEquals(150, wm.getAmmoInventory().getAmmo(AmmoType.AR));
        assertEquals(0, ar.getCurrentAmmo(), "Băng đạn súng cũ phải bị rút về 0 để tránh nhân bản đạn");

        // Kiểm tra xem hộp đạn thừa đã được sinh ra dưới đất đúng tọa độ và số lượng không
        assertEquals(initialPickupsCount + 1, state.getAmmoPickups().size());
        AmmoPickup droppedPickup = state.getAmmoPickups().get(state.getAmmoPickups().size() - 1);
        assertEquals(300.0, droppedPickup.getX(), 0.001);
        assertEquals(400.0, droppedPickup.getY(), 0.001);
        assertEquals(AmmoType.AR, droppedPickup.getType());
        assertEquals(20, droppedPickup.getAmount(), "Hộp đạn rơi dưới đất phải chứa đúng 20 viên thừa");
        assertFalse(droppedPickup.isCollected());

        // Kiểm tra tính Idempotent: gọi lại không được rơi thêm
        PlayingState.ReclaimedAmmoResult secondCall =
                state.reclaimReplacedWeaponAmmo(ar, 300.0, 400.0);
        assertEquals(0, secondCall.addedToBackpack);
        assertEquals(0, secondCall.droppedSurplus);
        assertEquals(initialPickupsCount + 1, state.getAmmoPickups().size());
    }

    @Test
    @DisplayName("Verify spawnClearRoomAmmoStacks intelligently prioritizes equipped weapons in Slot 1 and 2")
    void testClearRoomAmmoStacksPrioritizesEquippedWeapons() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);

        // Trang bị AR ở Slot 1 và Shotgun ở Slot 2
        AssaultRifle ar = new AssaultRifle(1);
        Shotgun sg = new Shotgun();
        sg.setSlotIndex(2);
        state.getWeaponManager().addWeapon(ar);
        state.getWeaponManager().addWeapon(sg);

        state.getAmmoPickups().clear();
        state.spawnClearRoomAmmoStacks(500.0, 500.0);

        assertEquals(2, state.getAmmoPickups().size());
        AmmoPickup p1 = state.getAmmoPickups().get(0);
        AmmoPickup p2 = state.getAmmoPickups().get(1);

        // 2 hộp đạn rơi ra phải chứa đúng loại đạn của 2 khẩu súng đang cầm (AR và Shotgun)
        java.util.Set<AmmoType> droppedTypes = new java.util.HashSet<>();
        droppedTypes.add(p1.getType());
        droppedTypes.add(p2.getType());

        assertTrue(droppedTypes.contains(AmmoType.AR), "Must drop AR ammo for equipped Assault Rifle");
        assertTrue(droppedTypes.contains(AmmoType.SHOTGUN), "Must drop Shotgun ammo for equipped Shotgun");
    }
}
