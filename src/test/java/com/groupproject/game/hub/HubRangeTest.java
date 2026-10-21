package com.groupproject.game.hub;

import com.groupproject.game.entity.enemy.TargetDummy;
import com.groupproject.game.entity.item.AmmoType;
import com.groupproject.game.entity.weapon.AssaultRifle;
import com.groupproject.game.entity.weapon.SMG;
import com.groupproject.game.entity.weapon.SemiCarbineRifle;
import com.groupproject.game.entity.weapon.Shotgun;
import com.groupproject.game.entity.weapon.SniperRifle;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.entity.weapon.WeaponManager;
import com.groupproject.game.state.GameStateManager;
import com.groupproject.game.state.PlayingState;
import java.awt.Color;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động toàn diện cho hệ thống Hub The Range và các phân hệ tích hợp.
 */
class HubRangeTest {

    private GameStateManager gsm;
    private HubRangeState hub;

    @BeforeEach
    void setUp() {
        gsm = new GameStateManager();
        hub = new HubRangeState(gsm);
        gsm.registerState(GameStateManager.StateType.THE_RANGE, hub);
        gsm.registerState(GameStateManager.StateType.PLAYING, new PlayingState(gsm));
        hub.init();
    }

    @Test
    @DisplayName("Kiểm tra khởi tạo The Range: Ô vuông trung tâm, 3 Dummy phải, 4 Dummy trái và các trạm")
    void testHubInitialization() {
        assertNotNull(hub.getPlayer(), "Operative phải được khởi tạo");
        double px = hub.getPlayer().getCenterX();
        double py = hub.getPlayer().getCenterY();

        // Operative nằm trong ô vuông trung tâm
        assertTrue(px >= HubRangeState.CENTER_BOUND_MIN_X && px <= HubRangeState.CENTER_BOUND_MAX_X,
                "Operative phải sinh trong giới hạn X của ô vuông trung tâm");
        assertTrue(py >= HubRangeState.CENTER_BOUND_MIN_Y && py <= HubRangeState.CENTER_BOUND_MAX_Y,
                "Operative phải sinh trong giới hạn Y của ô vuông trung tâm");

        // Đúng 3 Dummy đứng yên ở trường bắn phải
        assertEquals(3, hub.getRightRangeDummies().size(), "Phải có đúng 3 Dummy ở trường bắn phải");
        assertEquals(1640.0, hub.getRightRangeDummies().get(0).getX(), 0.01, "Dummy gần");
        assertEquals(1840.0, hub.getRightRangeDummies().get(1).getX(), 0.01, "Dummy vừa");
        assertEquals(2090.0, hub.getRightRangeDummies().get(2).getX(), 0.01, "Dummy xa");

        // Đúng 4 Dummy di chuyển lạng lách ở trường bắn trái
        assertEquals(4, hub.getLeftRangeDummies().size(), "Phải có 4 Dummy ở trường bắn trái");

        // Các trạm tương tác phải có đủ 10 trạm
        assertTrue(hub.getStations().size() >= 10, "Phải có đầy đủ các trạm vũ khí, nâng cấp, reroll, thùng rác, HP, cổng");
    }

    @Test
    @DisplayName("Kiểm tra vệt đường đạn Recoil Tracer tồn tại 3.0 giây sáng rõ rồi mờ dần và tự hủy sau 3.6s")
    void testRecoilTracerManagerLifespan() {
        RecoilTracerManager tm = hub.getTracerManager();
        assertEquals(0, tm.getActiveCount());

        tm.addTracer(100, 100, 500, 100, new Color(255, 215, 60));
        assertEquals(1, tm.getActiveCount());

        // Sau 2.5s (trong phạm vi 3.0s giữ sáng rõ) -> vẫn còn hoạt động
        tm.update(2.5);
        assertEquals(1, tm.getActiveCount());

        // Sau thêm 0.8s (tổng 3.3s, đang trong pha mờ dần 0.6s) -> vẫn còn hoạt động
        tm.update(0.8);
        assertEquals(1, tm.getActiveCount());

        // Sau thêm 0.4s (tổng 3.7s > 3.6s) -> đã hết hạn và tự động bị hủy
        tm.update(0.4);
        assertEquals(0, tm.getActiveCount(), "Vệt đạn phải bị loại bỏ sau khi hết 3.6 giây");
    }

    @Test
    @DisplayName("Kiểm tra StrafingTargetDummy di chuyển lạng lách, đổi HP và tự động hồi sinh")
    void testStrafingDummyMovementAndRespawn() {
        StrafingTargetDummy dummy = hub.getLeftRangeDummies().get(0);
        double initialX = dummy.getX();
        double initialY = dummy.getY();

        // Cho thời gian trôi qua để dummy di chuyển
        dummy.update(0.5);
        assertNotEquals(initialX + initialY, dummy.getX() + dummy.getY(), "Dummy phải di chuyển lạng lách");

        // Điều chỉnh mức máu
        dummy.configureHealth(500);
        assertEquals(500, dummy.getMaxHealth());
        assertEquals(500, dummy.getCurrentHealth());

        // Gây sát thương tiêu diệt dummy
        dummy.takeDamage(600);
        assertFalse(dummy.isAlive(), "Dummy phải chết khi nhận sát thương vượt HP");

        // Sau thời gian chờ hồi sinh 0.70s -> dummy tự động hồi sinh lại đầy máu
        dummy.update(0.70);
        assertTrue(dummy.isAlive(), "Dummy phải tự động hồi sinh thay thế");
        assertEquals(500, dummy.getCurrentHealth());
    }

    @Test
    @DisplayName("Kiểm tra kệ vũ khí (WeaponRackStation): Trang bị miễn phí các dòng súng vào Slot 1 và 2")
    void testWeaponRackStations() {
        WeaponManager wm = hub.getWeaponManager();

        // Tìm trạm kệ Semi-Carbine Rifle (SCR)
        WeaponRackStation scrRack = (WeaponRackStation) hub.getStations().stream()
                .filter(s -> s instanceof WeaponRackStation wr && "SCR".equals(wr.getWeaponCode()))
                .findFirst().orElse(null);
        assertNotNull(scrRack);

        // Tương tác lấy súng
        scrRack.onInteract(hub);
        Weapon equipped = wm.getActiveWeapon();
        assertNotNull(equipped);
        assertTrue(equipped instanceof SemiCarbineRifle, "Vũ khí trang bị phải là SemiCarbineRifle");
        assertEquals(20, equipped.getMagazineSize());

        // Tìm trạm kệ Shotgun và lấy súng tiếp
        WeaponRackStation shotgunRack = (WeaponRackStation) hub.getStations().stream()
                .filter(s -> s instanceof WeaponRackStation wr && "Shotgun".equals(wr.getWeaponCode()))
                .findFirst().orElse(null);
        assertNotNull(shotgunRack);

        shotgunRack.onInteract(hub);
        Weapon equippedShotgun = wm.getActiveWeapon();
        assertTrue(equippedShotgun instanceof Shotgun);
        assertNotNull(wm.getWeapon(1));
        assertNotNull(wm.getWeapon(2));
    }

    @Test
    @DisplayName("Kiểm tra Bàn nâng cấp miễn phí (HubUpgradeStation) nâng súng lên tối đa Cấp 10")
    void testHubUpgradeStationMaxLevel10() {
        WeaponManager wm = hub.getWeaponManager();
        Weapon ar = new AssaultRifle();
        ar.setSlotIndex(1);
        wm.addWeapon(ar);
        wm.selectSlot(1);

        HubUpgradeStation upgradeStation = (HubUpgradeStation) hub.getStations().stream()
                .filter(s -> s instanceof HubUpgradeStation)
                .findFirst().orElse(null);
        assertNotNull(upgradeStation);

        assertEquals(1, ar.getLevel());

        // Nâng cấp liên tục 9 lần lên Lv 10
        for (int i = 2; i <= 10; i++) {
            upgradeStation.onInteract(hub);
            assertEquals(i, ar.getLevel(), "Cấp độ súng phải tăng lên " + i);
        }

        assertEquals(10, ar.getLevel());

        // Cố gắng nâng cấp lần thứ 11 -> Bị chặn lại ở Lv 10
        upgradeStation.onInteract(hub);
        assertEquals(10, ar.getLevel(), "Không được phép vượt quá cấp độ tối đa Lv 10");
    }

    @Test
    @DisplayName("Kiểm tra Trạm tái rèn (HubRerollStation) tái phân bổ sao và roll crit miễn phí")
    void testHubRerollStation() {
        WeaponManager wm = hub.getWeaponManager();
        Weapon scr = new SemiCarbineRifle();
        scr.setSlotIndex(1);
        scr.configureLevel(5); // Cấp 5
        wm.addWeapon(scr);
        wm.selectSlot(1);

        HubRerollStation rerollStation = (HubRerollStation) hub.getStations().stream()
                .filter(s -> s instanceof HubRerollStation)
                .findFirst().orElse(null);
        assertNotNull(rerollStation);

        int oldTotalStars = scr.getDamageStars() + scr.getFireRateStars() + scr.getAccuracyStars();
        rerollStation.onInteract(hub);

        int newTotalStars = scr.getDamageStars() + scr.getFireRateStars() + scr.getAccuracyStars();
        assertEquals(oldTotalStars, newTotalStars, "Tổng số sao phải được bảo toàn nguyên vẹn sau khi tái rèn");
        assertEquals(5, scr.getLevel(), "Cấp độ phải được bảo toàn nguyên vẹn");
    }

    @Test
    @DisplayName("Kiểm tra Thùng rác (TrashBinStation): Không cho vứt Pistol/Dao, cho phép vứt súng Slot 1 và 2")
    void testTrashBinStationProtectionAndDiscard() {
        WeaponManager wm = hub.getWeaponManager();
        TrashBinStation bin = (TrashBinStation) hub.getStations().stream()
                .filter(s -> s instanceof TrashBinStation)
                .findFirst().orElse(null);
        assertNotNull(bin);

        // 1. Thử vứt súng lục Slot 3 -> Bị chặn
        wm.selectSlot(3);
        bin.onInteract(hub);
        assertNotNull(wm.getWeapon(3), "Súng lục phòng thân không bao giờ bị xóa");

        // 2. Thử vứt dao Slot 4 -> Bị chặn
        wm.selectSlot(4);
        bin.onInteract(hub);
        assertNotNull(wm.getWeapon(4), "Dao găm cận chiến không bao giờ bị xóa");

        // 3. Trang bị SMG vào Slot 1 rồi vứt vào thùng rác -> Thành công
        Weapon smg = new SMG();
        smg.setSlotIndex(1);
        wm.addWeapon(smg);
        wm.selectSlot(1);
        assertEquals(smg, wm.getActiveWeapon());

        bin.onInteract(hub);
        assertNull(wm.getWeapon(1), "Súng Slot 1 phải được dọn khỏi balo vào thùng rác");
    }

    @Test
    @DisplayName("Kiểm tra Bảng điều chỉnh HP Dummy (DummyHPControllerStation): 100 -> 250 -> 500 -> 1000")
    void testDummyHPControllerStationCycle() {
        DummyHPControllerStation hpStation = (DummyHPControllerStation) hub.getStations().stream()
                .filter(s -> s instanceof DummyHPControllerStation)
                .findFirst().orElse(null);
        assertNotNull(hpStation);

        assertEquals(100, hpStation.getCurrentHealthPreset());

        hpStation.onInteract(hub);
        assertEquals(250, hpStation.getCurrentHealthPreset());
        assertEquals(250, hub.getLeftRangeDummies().get(0).getMaxHealth());

        hpStation.onInteract(hub);
        assertEquals(500, hpStation.getCurrentHealthPreset());

        hpStation.onInteract(hub);
        assertEquals(1000, hpStation.getCurrentHealthPreset());

        hpStation.onInteract(hub);
        assertEquals(100, hpStation.getCurrentHealthPreset(), "Quay vòng về 100 HP");
    }

    @Test
    @DisplayName("Kiểm tra Cổng xuất kích (DepartureGateStation) xóa sạch trang bị The Range khi vào Stage 1")
    void testDepartureGateClearsHubWeaponsOnEnteringStage1() {
        WeaponManager wm = hub.getWeaponManager();
        Weapon scr = new SemiCarbineRifle();
        scr.setSlotIndex(1);
        scr.setLevel(10);
        wm.addWeapon(scr);

        Weapon shotgun = new Shotgun();
        shotgun.setSlotIndex(2);
        shotgun.setLevel(10);
        wm.addWeapon(shotgun);

        DepartureGateStation gate = (DepartureGateStation) hub.getStations().stream()
                .filter(s -> s instanceof DepartureGateStation)
                .findFirst().orElse(null);
        assertNotNull(gate);

        // Kích hoạt xuất kích vào trận
        gate.onInteract(hub);

        // Sau khi stateManager.setState(PLAYING) chạy thì playingState.init() xóa sạch vũ khí The Range
        PlayingState ps = (PlayingState) gsm.getState(GameStateManager.StateType.PLAYING);
        assertNotNull(ps);
        Weapon pW1 = ps.getWeaponManager().getWeapon(1);
        Weapon pW2 = ps.getWeaponManager().getWeapon(2);
        Weapon pW3 = ps.getWeaponManager().getWeapon(3);
        Weapon pW4 = ps.getWeaponManager().getWeapon(4);

        assertNull(pW1, "Slot 1 trong chiến trường Stage 1 KHÔNG được mang súng từ Hub");
        assertNull(pW2, "Slot 2 trong chiến trường Stage 1 KHÔNG được mang súng từ Hub");
        assertNotNull(pW3, "Slot 3 phải là súng lục tiêu chuẩn xuất phát của Operative");
        assertNotNull(pW4, "Slot 4 phải là dao găm cận chiến tiêu chuẩn của Operative");
    }

    @Test
    @DisplayName("Kiểm tra tích hợp Giao diện trong trận: Balo TAB, Menu Pause ESC và HUD")
    void testInGameUITechnologyIntegration() {
        assertNotNull(hub.getPlayingHUD(), "PlayingHUD phải được tích hợp vào The Range");
        assertNotNull(hub.getBackpackUI(), "BackpackUI phải được tích hợp vào The Range");
        assertNotNull(hub.getPauseMenuUI(), "PauseMenuUI phải được tích hợp vào The Range");

        assertFalse(hub.isBackpackOpen(), "Ban đầu Balo phải đóng");
        assertFalse(hub.isPaused(), "Ban đầu game không bị tạm dừng");

        // Bật Balo
        hub.setBackpackOpen(true);
        assertTrue(hub.isBackpackOpen());

        // Đóng Balo
        hub.setBackpackOpen(false);
        assertFalse(hub.isBackpackOpen());

        // Bật Pause Menu
        hub.setPaused(true);
        assertTrue(hub.isPaused());

        // Tiếp tục (Resume)
        hub.setPaused(false);
        assertFalse(hub.isPaused());

        // Kiểm tra render không ném ngoại lệ
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1280, 720, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = img.createGraphics();
        assertDoesNotThrow(() -> hub.render(g2), "Render Hub cùng hệ thống HUD trong trận không được văng Exception");
        g2.dispose();
    }

    @Test
    @DisplayName("Kiểm tra đạn Sniper Rifle xuyên qua nhiều bia Dummy và render súng/thanh nạp đạn trên người chơi")
    void testSniperPiercingAndWeaponRenderInTheRange() {
        // 1. Trang bị Sniper Rifle vào Slot 1
        com.groupproject.game.entity.weapon.SniperRifle sniper = new com.groupproject.game.entity.weapon.SniperRifle(1);
        hub.getWeaponManager().addWeapon(sniper);
        hub.getWeaponManager().selectSlot(1);
        assertEquals(sniper, hub.getWeaponManager().getActiveWeapon());

        // 2. Lấy 2 Dummy thẳng hàng ở trường bắn bên phải (cùng Y=620, X1=1640, X2=1840)
        List<TargetDummy> rightDummies = hub.getRightRangeDummies();
        assertTrue(rightDummies.size() >= 2);
        TargetDummy d1 = rightDummies.get(0);
        TargetDummy d2 = rightDummies.get(1);

        // Bắn 1 phát đạn Sniper thẳng hàng ngang từ X=1600, Y=620 hướng 0 độ (sang phải)
        com.groupproject.game.entity.projectile.Projectile sniperBullet = new com.groupproject.game.entity.projectile.Projectile(
                1600, 620, 0.0, 1500.0, 75, 1.0, true, false,
                sniper.getMaxPierces(), sniper.getPierceDamageFalloff()
        );
        assertTrue(sniperBullet.isPiercing(), "Đạn Sniper phải mang đặc tính xuyên phá");
        hub.getProjectileManager().spawn(sniperBullet);

        // Giả lập chuỗi frame 60 FPS (mỗi frame 16ms) mô phỏng chính xác đường đạn bay thực tế
        for (int i = 0; i < 20; i++) {
            hub.update(0.016);
        }

        // Cả 2 Dummy trên đường đạn đều phải bị trúng sát thương nhờ khả năng xuyên phá của Sniper
        assertTrue(d1.getCurrentHealth() < d1.getMaxHealth(), "Dummy 1 phải nhận sát thương từ phát bắn Sniper đầu tiên");
        assertTrue(d2.getCurrentHealth() < d2.getMaxHealth(), "Dummy 2 phải nhận sát thương do đạn Sniper xuyên qua Dummy 1");

        // 3. Bắn 1 viên tiêu hao đạn rồi kiểm tra hiển thị thanh nạp đạn ở chân và súng trên tay
        sniper.attack(0, 0, 0, hub.getProjectileManager(), new java.util.ArrayList<>());
        sniper.reload();
        assertTrue(sniper.isReloading(), "Súng đang trong trạng thái nạp đạn");

        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1280, 720, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = img.createGraphics();
        assertDoesNotThrow(() -> hub.render(g2), "Render súng trên tay người chơi và thanh nạp đạn ở chân không được ném Exception");
        g2.dispose();
    }
}
