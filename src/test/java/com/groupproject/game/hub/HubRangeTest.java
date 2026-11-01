package com.groupproject.game.hub;

import com.groupproject.game.entity.enemy.TargetDummy;
import com.groupproject.game.entity.weapon.AssaultRifle;
import com.groupproject.game.entity.weapon.SMG;
import com.groupproject.game.entity.weapon.SemiCarbineRifle;
import com.groupproject.game.entity.weapon.Shotgun;
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

        // Các trạm tương tác phải có đủ 9 trạm (5 kệ súng, bàn nâng cấp, bàn reroll, cầu thang xuất kích, bảng HP)
        assertTrue(hub.getStations().size() >= 9, "Phải có đầy đủ các trạm vũ khí, nâng cấp, reroll, HP, cầu thang xuất kích");
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

        // Kiểm tra tương tác mở Modal UI
        upgradeStation.onInteract(hub);
        assertTrue(hub.getWeaponUpgradeModalUI().isVisible(), "Tương tác với bàn nâng cấp phải mở Modal UI");

        // Nâng cấp liên tục 9 lần lên Lv 10 bằng cách chọn nhánh Sát Thương
        for (int i = 2; i <= 10; i++) {
            upgradeStation.executeUpgrade(hub, ar, Weapon.UpgradeStatType.DAMAGE);
            assertEquals(i, ar.getLevel(), "Cấp độ súng phải tăng lên " + i);
        }

        assertEquals(10, ar.getLevel());

        // Cố gắng nâng cấp lần thứ 11 -> Bị chặn lại ở Lv 10
        upgradeStation.executeUpgrade(hub, ar, Weapon.UpgradeStatType.DAMAGE);
        assertEquals(10, ar.getLevel(), "Không được phép vượt quá cấp độ tối đa Lv 10");
    }

    @Test
    @DisplayName("Kiểm tra Trạm tái rèn (HubRerollStation) mở Modal và hỗ trợ Reroll Sao / Reroll Chí Mạng riêng biệt")
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

        // Kiểm tra tương tác mở Modal UI
        rerollStation.onInteract(hub);
        assertTrue(hub.getStatReforgeModalUI().isVisible(), "Tương tác với trạm tái rèn phải mở Modal UI");

        // 1. Kiểm tra Reroll toàn diện (tương thích ngược)
        int oldTotalStars = scr.getDamageStars() + scr.getFireRateStars() + scr.getAccuracyStars();
        rerollStation.executeReforge(hub, scr);

        int newTotalStars = scr.getDamageStars() + scr.getFireRateStars() + scr.getAccuracyStars();
        assertEquals(oldTotalStars, newTotalStars, "Tổng số sao phải được bảo toàn nguyên vẹn sau khi tái rèn");
        assertEquals(5, scr.getLevel(), "Cấp độ phải được bảo toàn nguyên vẹn");

        // 2. Kiểm tra Reroll Sao Chỉ Số độc lập (bảo lưu chí mạng)
        double fixedCrit = scr.getCritChance();
        rerollStation.executeReforgeStars(hub, scr);
        assertEquals(fixedCrit, scr.getCritChance(), 0.0001, "Reroll Sao phải bảo lưu tỉ lệ chí mạng");

        // 3. Kiểm tra Reroll Chí Mạng độc lập (bảo lưu sao)
        int fixedStars = scr.getDamageStars() + scr.getFireRateStars() + scr.getAccuracyStars();
        rerollStation.executeReforgeCrit(hub, scr);
        assertEquals(fixedStars, scr.getDamageStars() + scr.getFireRateStars() + scr.getAccuracyStars(), "Reroll Chí Mạng phải bảo lưu sao");
    }

    @Test
    @DisplayName("Kiểm tra Thùng rác (TrashBinStation): Không cho vứt Pistol/Dao, cho phép vứt súng Slot 1 và 2")
    void testTrashBinStationProtectionAndDiscard() {
        WeaponManager wm = hub.getWeaponManager();
        TrashBinStation bin = new TrashBinStation(0, 0);
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

    @Test
    @DisplayName("Kiểm tra dải tường gạch Bắc/Nam thay thế tường vô hình, xóa tile ngoài biên và Surface Baking trong The Range")
    void testHubWallBoundariesAndSurfaceBaking() {
        com.groupproject.game.dungeon.Tile[][] tiles = hub.getHubTiles();
        assertNotNull(tiles, "Lưới gạch The Range phải được khởi tạo");

        int cols = tiles.length;
        int rows = tiles[0].length;

        // 1. Kiểm tra các hàng phía trên hàng 4 (y < 4) và phía dưới hàng 21 (y > 21) bị xóa sạch hoàn toàn (null)
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < HubRangeState.WALL_ROW_TOP; y++) {
                assertNull(tiles[x][y], "Ô tile hàng " + y + " phía trên tường Bắc phải bị xóa (null) để tiết kiệm tài nguyên");
            }
            for (int y = HubRangeState.WALL_ROW_BOT + 1; y < rows; y++) {
                assertNull(tiles[x][y], "Ô tile hàng " + y + " phía dưới tường Nam phải bị xóa (null) để tiết kiệm tài nguyên");
            }
        }

        // 2. Kiểm tra hàng WALL_ROW_TOP (hàng 4) và WALL_ROW_BOT (hàng 21) là tường gạch WALL kiên cố bao quanh
        for (int x = 0; x < cols; x++) {
            assertNotNull(tiles[x][HubRangeState.WALL_ROW_TOP], "Tường Bắc phải tồn tại");
            assertEquals(com.groupproject.game.dungeon.TileType.WALL, tiles[x][HubRangeState.WALL_ROW_TOP].getType(),
                    "Bờ tường Bắc hàng " + HubRangeState.WALL_ROW_TOP + " phải là dải tường gạch WALL");

            assertNotNull(tiles[x][HubRangeState.WALL_ROW_BOT], "Tường Nam phải tồn tại");
            assertEquals(com.groupproject.game.dungeon.TileType.WALL, tiles[x][HubRangeState.WALL_ROW_BOT].getType(),
                    "Bờ tường Nam hàng " + HubRangeState.WALL_ROW_BOT + " phải là dải tường gạch WALL");
        }

        // 3. Kiểm tra Surface Baking đã nướng xong ảnh nền và render 1 lệnh trơn tru
        java.awt.image.BufferedImage baked = hub.getBakedHubSurface();
        assertNotNull(baked, "Ảnh nướng nền The Range (bakedHubSurface) phải được tạo thành công");
        assertTrue(baked.getWidth() > 0 && baked.getHeight() > 0, "Kích thước surface nướng phải hợp lệ");

        java.awt.image.BufferedImage screen = new java.awt.image.BufferedImage(1280, 720, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = screen.createGraphics();
        assertDoesNotThrow(() -> hub.render(g), "Vẽ The Range với Surface Baking không được ném ngoại lệ");
        g.dispose();
    }

    @Test
    @DisplayName("Kiểm tra cơ chế Viewport Culling trên Station và Dummy khi nằm ngoài tầm nhìn camera")
    void testHubAndEntityViewportCulling() {
        com.groupproject.game.core.Camera camera = new com.groupproject.game.core.Camera(1280, 720);
        camera.snapTo(2000.0, 500.0);

        // Dummy ở rất xa bên trái (x = 100) -> screenX = 100 - (2000 - 640) = -1260 (nằm ngoài camera)
        TargetDummy offscreenDummy = new TargetDummy(100.0, 500.0, 100);
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1280, 720, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();

        assertDoesNotThrow(() -> offscreenDummy.render(g, camera), "Render Dummy ngoài viewport phải được culling trơn tru");

        // Strafing Dummy ở rất xa
        StrafingTargetDummy offscreenStrafing = new StrafingTargetDummy(100.0, 500.0, 100, 50.0, 150.0, 400.0, 600.0);
        assertDoesNotThrow(() -> offscreenStrafing.render(g, camera), "Render Strafing Dummy ngoài viewport phải được culling trơn tru");

        g.dispose();
    }

    @Test
    @DisplayName("Kiểm tra đạn Sniper xuyên qua mục tiêu không bị nhân lên nhiều tracer từ người chơi")
    void testSniperPiercingSingleTracerIntegrity() {
        RecoilTracerManager tm = hub.getTracerManager();
        assertEquals(0, tm.getActiveCount(), "Ban đầu không có tracer");

        // Tạo 1 viên đạn xuyên phá (Sniper) với 1 lượt xuyên (xuyên Dummy 1 và dừng ở Dummy 2)
        com.groupproject.game.entity.projectile.Projectile piercingBullet =
                new com.groupproject.game.entity.projectile.Projectile(
                        1000.0, 500.0, 0.0, 800.0, 100, 2.0, true, false, 1, 0.2
                );
        assertEquals(1000.0, piercingBullet.getOriginX(), 0.01, "OriginX ban đầu phải được lưu trữ chính xác");
        assertEquals(500.0, piercingBullet.getOriginY(), 0.01, "OriginY ban đầu phải được lưu trữ chính xác");

        TargetDummy dummy1 = hub.getRightRangeDummies().get(0); // x = 1640
        TargetDummy dummy2 = hub.getRightRangeDummies().get(1); // x = 1840

        // Khi đạn bắn trúng Dummy 1 lần đầu (còn 1 lượt xuyên -> tiêu hao lượt xuyên và bay tiếp)
        piercingBullet.onHitTarget(dummy1);
        assertTrue(piercingBullet.isActive(), "Đạn sniper vẫn tiếp tục bay sau khi xuyên Dummy 1");

        // Khi đạn bắn trúng Dummy 2 (đã hết lượt xuyên -> dừng lại và deactivate)
        piercingBullet.onHitTarget(dummy2);
        assertFalse(piercingBullet.isActive(), "Đạn sniper dừng lại sau khi xuyên hết số lần cho phép");

        // Thêm duy nhất 1 tracer từ điểm bắn ban đầu đến điểm kết thúc cuối
        tm.addTracer(piercingBullet.getOriginX(), piercingBullet.getOriginY(), dummy2.getX(), dummy2.getY(), Color.YELLOW);
        assertEquals(1, tm.getActiveCount(), "Chỉ có duy nhất 1 đường tracer kết thúc tại dummy cuối cùng, không bị nhân 3 tia");
    }

    @Test
    @DisplayName("Kiểm tra Render Cầu Thang Xuất Kích, Bàn Quân Khí và Bàn Bio-Tech không gây lỗi")
    void testTacticalStationsRenderIntegrity() {
        com.groupproject.game.core.Camera camera = new com.groupproject.game.core.Camera(1280, 720);
        camera.snapTo(1050.0, 360.0);

        DepartureGateStation stairs = new DepartureGateStation(1050, 360);
        HubUpgradeStation upgrade = new HubUpgradeStation(880, 370);
        HubRerollStation reroll = new HubRerollStation(1220, 370);

        stairs.update(0.1);
        upgrade.update(0.1);
        reroll.update(0.1);

        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1280, 720, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();

        assertDoesNotThrow(() -> stairs.render(g, camera, true), "Render Cầu thang xuất kích (có player gần)");
        assertDoesNotThrow(() -> stairs.render(g, camera, false), "Render Cầu thang xuất kích (không có player gần)");
        assertDoesNotThrow(() -> upgrade.render(g, camera, true), "Render Bàn Nâng Cấp Bio-Tech");
        assertDoesNotThrow(() -> reroll.render(g, camera, true), "Render Bàn Tái Rèn Quân Khí Dã Chiến");

        // Kiểm tra WeaponRackStation (hiển thị vũ khí drop lơ lửng và bảng chỉ số chi tiết)
        WeaponRackStation rack = new WeaponRackStation(760, 860, "AR", "Assault Rifle",
                com.groupproject.game.entity.weapon.WeaponSpriteManager.SPRITE_AR, AssaultRifle::new);
        rack.update(0.1);
        assertNotNull(rack.getPreviewWeapon(), "Preview weapon must be initialized");
        assertDoesNotThrow(() -> rack.render(g, camera, false), "Render Weapon Rack đứng xa với nhãn nổi");
        assertDoesNotThrow(() -> rack.render(g, camera, true), "Render Weapon Rack lại gần với thẻ chỉ số chi tiết");

        // Kiểm tra Banner FIGHT! và CLEAR
        com.groupproject.game.ui.PlayingHUD hud = new com.groupproject.game.ui.PlayingHUD();
        assertDoesNotThrow(() -> hud.renderBanner(g, "FIGHT!", new Color(255, 65, 65), false), "Render banner FIGHT!");
        assertDoesNotThrow(() -> hud.renderBanner(g, "CLEAR", new Color(60, 235, 130), false), "Render banner CLEAR");

        g.dispose();
    }

    @Test
    @DisplayName("Kiểm tra Đường Ngắm Nét Đứt (Aim Guideline) tại The Range render chính xác theo chuột và cài đặt")
    void testHubRangeAimGuidelineRendering() {
        com.groupproject.game.input.KeyboardHandler keyH = new com.groupproject.game.input.KeyboardHandler();
        com.groupproject.game.input.MouseHandler mouseH = new com.groupproject.game.input.MouseHandler();

        // 1. Cập nhật vị trí chuột cách xa Operative
        mouseH.mouseMoved(new java.awt.event.MouseEvent(
                new java.awt.Component() {}, 0, 0, 0, 800, 300, 0, false));
        hub.update(0.016, keyH, mouseH);

        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1280, 720, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();

        // 2. Khi bật GameConfig.showAimGuideline = true -> render trơn tru không lỗi
        com.groupproject.game.core.GameConfig.showAimGuideline = true;
        assertDoesNotThrow(() -> hub.render(g), "Hub render với đường ngắm aim guideline bật phải chạy mượt mà");

        // 3. Khi tắt GameConfig.showAimGuideline = false -> render trơn tru không lỗi
        com.groupproject.game.core.GameConfig.showAimGuideline = false;
        assertDoesNotThrow(() -> hub.render(g), "Hub render khi tắt đường ngắm phải chạy mượt mà");

        // Khôi phục mặc định
        com.groupproject.game.core.GameConfig.showAimGuideline = true;
        g.dispose();
    }
    @Test
    @DisplayName("Kiểm tra thảm cỏ và hệ thống autotiling 30 biến thể hoa văn ranh giới trong The Range")
    void testHubGrassAutotilingAndPatterns() {
        com.groupproject.game.dungeon.Tile[][] tiles = hub.getHubTiles();
        assertNotNull(tiles);

        int coreGrass = 0;
        int transitionGrass = 0;
        int pureDirt = 0;

        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                com.groupproject.game.dungeon.Tile t = tiles[x][y];
                if (t != null && t.getType() == com.groupproject.game.dungeon.TileType.FLOOR) {
                    int p = t.getGrassPattern();
                    assertTrue(p >= 0 && p <= 30, "Mẫu hoa văn ranh giới The Range phải nằm trong khoảng [0..30], thực tế: " + p);
                    if (t.hasGrass() && p == 0) {
                        coreGrass++;
                    } else if (p >= 1 && p <= 30) {
                        transitionGrass++;
                    } else {
                        pureDirt++;
                    }
                }
            }
        }

        assertTrue(coreGrass > 0, "The Range phải có thảm cỏ lõi dày (Grass.png)");
        assertTrue(transitionGrass > 0, "The Range phải có các ô ranh giới hoa văn autotiling (Grass_pattern 1-30)");
        assertTrue(pureDirt > 0, "The Range phải có nền đất thuần (Dirt.png)");
    }

    @Test
    @DisplayName("Kiểm tra âm thanh Critical Hit đã sẵn sàng và được nạp thành công")
    void testCriticalHitSoundEffectLoaded() {
        assertTrue(com.groupproject.game.core.GameSoundManager.isSoundLoaded(com.groupproject.game.core.GameSoundManager.SoundEffect.CRITICAL_HIT),
                "CRITICAL_HIT sound must be loaded and ready");
    }

    @Test
    @DisplayName("Kiem tra duong dan tracer ket thuc tai dung toa do thuc te cua vien dan thay vi bi hut ve tam dummy")
    void testBulletTracerEndsAtExactBulletHitCoordinatesInsteadOfDummyCenter() {
        TargetDummy dummy = hub.getRightRangeDummies().get(0);
        double dummyCenterY = dummy.getY();
        assertEquals(620.0, dummyCenterY, 0.01);

        double bulletOriginX = 1500.0;
        double bulletOriginY = 605.0;
        com.groupproject.game.entity.projectile.Projectile bullet =
                new com.groupproject.game.entity.projectile.Projectile(
                        bulletOriginX, bulletOriginY, 0.0, 2000.0, 25, 1.0, true, false, 0, 0.0
                );

        hub.getTracerManager().clear();
        hub.getProjectileManager().spawn(bullet);

        com.groupproject.game.input.KeyboardHandler keyH = new com.groupproject.game.input.KeyboardHandler();
        com.groupproject.game.input.MouseHandler mouseH = new com.groupproject.game.input.MouseHandler();
        hub.update(0.08, keyH, mouseH);

        assertFalse(bullet.isActive(), "Dan phai dung lai khi va cham dummy");
        assertEquals(1, hub.getTracerManager().getActiveCount(), "Phai ghi nhan dung 1 vet tracer");

        RecoilTracerManager.RecoilTracer tracer = hub.getTracerManager().getActiveTracers().get(0);
        assertEquals(bulletOriginY, tracer.getEndY(), 0.1,
                "Diem ket thuc Y cua tracer phai bang Y cua vien dan, khong bi hut ve tam dummy (620.0)");
        assertNotEquals(dummyCenterY, tracer.getEndY(),
                "Diem ket thuc Y cua tracer khong duoc la tam dummy");
    }
}
