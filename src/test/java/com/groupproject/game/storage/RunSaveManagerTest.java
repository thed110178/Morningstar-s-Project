package com.groupproject.game.storage;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.entity.buff.CharacterBuff;
import com.groupproject.game.entity.item.AmmoType;
import com.groupproject.game.entity.weapon.AssaultRifle;
import com.groupproject.game.entity.weapon.Shotgun;
import com.groupproject.game.state.GameStateManager;
import com.groupproject.game.state.MenuState;
import com.groupproject.game.state.PlayingState;
import java.io.File;
import java.io.FileWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử toàn diện Hệ thống Lưu Tiến Trình Ván Đấu & Nút Tiếp Tục Chơi (Run Checkpoint & Resume).
 */
public class RunSaveManagerTest {

    private static final String TEST_SAVE_PATH = "target/test_save_checkpoint.dat";
    private String originalSavePath;

    @BeforeEach
    void setUp() {
        originalSavePath = RunSaveManager.getSaveFilePath();
        RunSaveManager.setSaveFilePath(TEST_SAVE_PATH);
        RunSaveManager.deleteSave();
    }

    @AfterEach
    void tearDown() {
        RunSaveManager.deleteSave();
        RunSaveManager.setSaveFilePath(originalSavePath);
    }

    @Test
    @DisplayName("Xác thực tính toàn vẹn khi Lưu và Khôi phục dữ liệu tiến trình ván đấu")
    void testSaveAndLoadRunDataIntegrity() {
        RunProgressData data = new RunProgressData();
        data.setCurrentStage(2);
        data.setPlayerHealth(85);
        data.setPlayerMaxHealth(120);
        data.setPlayerCredits(350);
        data.setActiveWeaponSlot(1);

        // Thêm Buff
        data.getBuffLevels().put(CharacterBuff.MAX_HEALTH.name(), 2);
        data.getBuffLevels().put(CharacterBuff.DEFENSE.name(), 1);

        // Thêm Đạn dự trữ
        data.getAmmoReserves().put(AmmoType.AR.name(), 90);
        data.getAmmoReserves().put(AmmoType.SHOTGUN.name(), 25);

        // Thêm Vũ khí
        data.getWeapons().add(new WeaponSaveData(1, "Assault Rifle", 2, 2, 1, 1, 0.20, 2.0, 28, false));
        data.getWeapons().add(new WeaponSaveData(2, "Shotgun", 1, 1, 1, 1, 0.15, 2.0, 8, true));
        data.getWeapons().add(new WeaponSaveData(3, "Pistol", 1, 1, 1, 1, 0.15, 2.0, 7, false));
        data.getWeapons().add(new WeaponSaveData(4, "Combat Knife", 1, 1, 1, 1, 0.25, 2.5, 0, false));

        boolean saved = RunSaveManager.saveRunData(data);
        assertTrue(saved, "Ghi file lưu checkpoint phải thành công");
        assertTrue(RunSaveManager.hasValidSave(), "Phải phát hiện có bản lưu hợp lệ");
        assertEquals(2, RunSaveManager.getSavedStage(), "Stage lưu phải là Stage 2");

        RunProgressData loaded = RunSaveManager.loadRun();
        assertNotNull(loaded, "Dữ liệu nạp lại không được null");
        assertEquals(GameConfig.GAME_BUILD_VERSION, loaded.getGameVersion());
        assertEquals(2, loaded.getCurrentStage());
        assertEquals(85, loaded.getPlayerHealth());
        assertEquals(120, loaded.getPlayerMaxHealth());
        assertEquals(350, loaded.getPlayerCredits());
        assertEquals(1, loaded.getActiveWeaponSlot());

        // Kiểm tra Buffs
        assertEquals(2, loaded.getBuffLevels().get(CharacterBuff.MAX_HEALTH.name()));
        assertEquals(1, loaded.getBuffLevels().get(CharacterBuff.DEFENSE.name()));

        // Kiểm tra Đạn
        assertEquals(90, loaded.getAmmoReserves().get(AmmoType.AR.name()));
        assertEquals(25, loaded.getAmmoReserves().get(AmmoType.SHOTGUN.name()));

        // Kiểm tra Vũ khí
        assertEquals(4, loaded.getWeapons().size());
        WeaponSaveData ar = loaded.getWeapons().stream().filter(w -> w.getSlotIndex() == 1).findFirst().orElse(null);
        assertNotNull(ar);
        assertEquals("Assault Rifle", ar.getWeaponName());
        assertEquals(2, ar.getLevel());
        assertEquals(2, ar.getDamageStars());
        assertEquals(28, ar.getCurrentAmmo());

        WeaponSaveData sg = loaded.getWeapons().stream().filter(w -> w.getSlotIndex() == 2).findFirst().orElse(null);
        assertNotNull(sg);
        assertTrue(sg.isSTier());
    }

    @Test
    @DisplayName("Xác thực bản lưu cũ tự động bị xóa bỏ khi Game cập nhật phiên bản")
    void testOldVersionSaveAutoDeleted() throws Exception {
        // Tạo file lưu với mã phiên bản cũ (ví dụ bản v0.9.0-ALPHA)
        File file = new File(TEST_SAVE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (FileWriter fw = new FileWriter(file)) {
            fw.write("# DONTKNOW RUN CHECKPOINT\n");
            fw.write("version=0.9.0-ALPHA\n");
            fw.write("stage=3\n");
            fw.write("playerHealth=100\n");
        }
        assertTrue(file.exists());

        // Khi kiểm tra tính hợp lệ hoặc nạp file: Hệ thống phải tự động xóa bỏ file lưu cũ
        assertFalse(RunSaveManager.hasValidSave(), "Bản lưu phiên bản cũ không được coi là hợp lệ");
        assertNull(RunSaveManager.loadRun(), "Nạp bản lưu cũ phải trả về null");
        assertFalse(file.exists(), "File lưu phiên bản cũ phải bị xóa vĩnh viễn khỏi đĩa");
    }

    @Test
    @DisplayName("Xác thực PlayingState xuất và khôi phục ván đấu dở dang an toàn 100%")
    void testPlayingStateExportAndResumeFromSave() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.setCurrentStage(2);
        state.setPlayerCredits(280);
        state.getPlayer().setMaxHealth(140);
        state.getPlayer().setCurrentHealth(95);

        // Thêm súng vào slot 1 và slot 2
        AssaultRifle ar = new AssaultRifle();
        ar.setLevel(2);
        ar.setStars(2, 1, 1);
        state.getWeaponManager().addWeapon(ar);
        state.getWeaponManager().selectSlot(1);

        Shotgun sg = new Shotgun();
        sg.setSlotIndex(2);
        state.getWeaponManager().addWeapon(sg);

        // Lưu qua RunSaveManager
        boolean saved = RunSaveManager.saveRun(state);
        assertTrue(saved, "Lưu PlayingState Campaign phải thành công");

        // Tạo một PlayingState mới hoàn toàn và khôi phục
        PlayingState newState = new PlayingState(gsm);
        RunProgressData loaded = RunSaveManager.loadRun();
        assertNotNull(loaded);
        newState.resumeFromSave(loaded);

        assertEquals(2, newState.getCurrentStage(), "Stage khôi phục phải là 2");
        assertEquals(280, newState.getPlayerCredits(), "Credits khôi phục phải là 280");
        assertEquals(140, newState.getPlayer().getMaxHealth(), "Max Health khôi phục phải là 140");
        assertEquals(95, newState.getPlayer().getCurrentHealth(), "Current Health khôi phục phải là 95");
        assertNotNull(newState.getWeaponManager().getWeapon(1), "Slot 1 phải có Assault Rifle");
        assertEquals("Assault Rifle (AR)", newState.getWeaponManager().getWeapon(1).getRawName());
        assertEquals(2, newState.getWeaponManager().getWeapon(1).getLevel());
    }

    @Test
    @DisplayName("Xác thực MenuState hiển thị nút Tiếp tục chơi và Hộp thoại xác nhận khi bắt đầu ván mới")
    void testMenuStateResumeAndConfirmNewGameDialog() {
        GameStateManager gsm = new GameStateManager();
        PlayingState playingState = new PlayingState(gsm);
        gsm.registerState(GameStateManager.StateType.PLAYING, playingState);

        MenuState menuState = new MenuState(gsm);
        gsm.registerState(GameStateManager.StateType.MENU, menuState);

        // 1. Khi chưa có save: Không có nút tiếp tục
        menuState.refreshMenuOptions();
        assertFalse(menuState.hasSave());

        // 2. Tạo bản lưu Stage 3
        RunProgressData data = new RunProgressData();
        data.setCurrentStage(3);
        data.setPlayerCredits(400);
        RunSaveManager.saveRunData(data);

        menuState.refreshMenuOptions();
        assertTrue(menuState.hasSave());

        // 3. Người chơi chọn Bắt đầu chơi mới (Index 1) -> Mở Hộp thoại xác nhận
        // Phải hỏi: 'Bạn có chắc không? Tiến trình lượt chơi trước đó sẽ bị xóa!'
        assertFalse(menuState.isShowConfirmDialog());
        com.groupproject.game.input.KeyboardHandler keyH = new com.groupproject.game.input.KeyboardHandler();
        com.groupproject.game.input.MouseHandler mouseH = new com.groupproject.game.input.MouseHandler();

        // Di chuyển con trỏ xuống tùy chọn 1: Bắt đầu chơi mới
        keyH.setKeyPressed(java.awt.event.KeyEvent.VK_DOWN, true);
        menuState.update(0.016, keyH, mouseH);
        keyH.setKeyPressed(java.awt.event.KeyEvent.VK_DOWN, false);

        // Nhấn Enter
        keyH.setKeyPressed(java.awt.event.KeyEvent.VK_ENTER, true);
        menuState.update(0.016, keyH, mouseH);
        keyH.setKeyPressed(java.awt.event.KeyEvent.VK_ENTER, false);

        assertTrue(menuState.isShowConfirmDialog(), "Phải bật hộp thoại xác nhận khi chọn Chơi mới lúc có save dở");
        assertEquals(1, menuState.getConfirmSelection(), "Mặc định phải trỏ vào Hủy bỏ để bảo vệ an toàn");

        // Người chơi chọn Hủy bỏ (Nhấn ESC hoặc Enter tại lựa chọn 1)
        keyH.setKeyPressed(java.awt.event.KeyEvent.VK_ESCAPE, true);
        menuState.update(0.016, keyH, mouseH);
        keyH.setKeyPressed(java.awt.event.KeyEvent.VK_ESCAPE, false);

        assertFalse(menuState.isShowConfirmDialog(), "Hộp thoại phải đóng sau khi nhấn Hủy bỏ");
        assertTrue(RunSaveManager.hasValidSave(), "Bản lưu vẫn phải được bảo lưu nguyên vẹn khi Hủy");

        // Người chơi mở lại hộp thoại và chọn Xác nhận xóa (Phím Trái để chuyển sang index 0 -> Enter)
        menuState.setShowConfirmDialog(true);
        menuState.setConfirmSelection(0); // 0 = Xóa & Chơi mới
        keyH.setKeyPressed(java.awt.event.KeyEvent.VK_ENTER, true);
        menuState.update(0.016, keyH, mouseH);
        keyH.setKeyPressed(java.awt.event.KeyEvent.VK_ENTER, false);

        assertFalse(menuState.isShowConfirmDialog());
        assertFalse(RunSaveManager.hasValidSave(), "Bản lưu phải bị xóa vĩnh viễn khi người chơi xác nhận");
    }

    @Test
    @DisplayName("Xác thực chu trình: Thoát ở Stage 2 -> Tiếp tục chơi Stage 2 -> Thoát lần nữa vẫn bảo lưu Stage 2 chuẩn xác 100%")
    void testResumeGameMultiStageCycleKeepsStageConsistent() {
        GameStateManager gsm = new GameStateManager();
        PlayingState playingState = new PlayingState(gsm);
        gsm.registerState(GameStateManager.StateType.PLAYING, playingState);

        MenuState menuState = new MenuState(gsm);
        gsm.registerState(GameStateManager.StateType.MENU, menuState);

        // 1. Tạo bản lưu đang ở Stage 2 với 350 Credits
        RunProgressData stage2Data = new RunProgressData();
        stage2Data.setCurrentStage(2);
        stage2Data.setPlayerCredits(350);
        stage2Data.setPlayerHealth(80);
        stage2Data.setPlayerMaxHealth(120);
        RunSaveManager.saveRunData(stage2Data);

        // Menu kiểm tra có save Stage 2
        menuState.refreshMenuOptions();
        assertTrue(menuState.hasSave());
        assertEquals(2, RunSaveManager.getSavedStage());

        // 2. Người chơi tại Menu bấm 'TIẾP TỤC CHƠI' (Option 0)
        com.groupproject.game.input.KeyboardHandler keyH = new com.groupproject.game.input.KeyboardHandler();
        com.groupproject.game.input.MouseHandler mouseH = new com.groupproject.game.input.MouseHandler();
        keyH.setKeyPressed(java.awt.event.KeyEvent.VK_ENTER, true);
        menuState.update(0.016, keyH, mouseH);
        keyH.setKeyPressed(java.awt.event.KeyEvent.VK_ENTER, false);

        // Xác nhận đã đặt pendingResumeData cho PlayingState
        assertTrue(PlayingState.hasPendingResumeData(), "Phải có pendingResumeData sẵn sàng cho PlayingState");

        // Giả lập chuyển cảnh hoàn tất: GameStateManager gọi setState(PLAYING) -> kích hoạt playingState.init()
        gsm.setState(GameStateManager.StateType.PLAYING);

        // Xác nhận sau khi init(), PlayingState phải ở đúng Stage 2 và 350 Credits (không bị tụt về Stage 1)
        assertEquals(2, playingState.getCurrentStage(), "PlayingState sau khi resume phải ở đúng Stage 2");
        assertEquals(350, playingState.getPlayerCredits(), "Credits phải là 350");
        assertFalse(PlayingState.hasPendingResumeData(), "pendingResumeData phải được giải phóng sau khi nạp");

        // 3. Người chơi lại bấm Thoát ra Menu từ Stage 2 lần nữa
        RunSaveManager.saveRun(playingState);

        // 4. Kiểm tra file save trên đĩa: Vẫn phải là Stage 2!
        RunProgressData reloadedData = RunSaveManager.loadRun();
        assertNotNull(reloadedData);
        assertEquals(2, reloadedData.getCurrentStage(), "File save sau khi thoát lần 2 vẫn phải là Stage 2");
        assertEquals(350, reloadedData.getPlayerCredits());

        // 5. Menu hiển thị lại: Phải tiếp tục là Stage 2
        menuState.refreshMenuOptions();
        assertTrue(menuState.hasSave());
        assertEquals(2, RunSaveManager.getSavedStage(), "Menu lần 2 vẫn phải hiển thị tiếp tục chơi Stage 2");
    }

    @Test
    @DisplayName("Khởi động lại game (GamePanel mới) rồi bấm Tiếp tục chơi không bao giờ bị đẩy nhầm vào phòng Boss Tank")
    void testColdStartResumeNeverEntersStandaloneBossFight() {
        // 1. Giả lập có bản lưu ở Stage 2 trước khi bật game
        RunProgressData saved = new RunProgressData();
        saved.setCurrentStage(2);
        saved.setPlayerCredits(200);
        saved.setPlayerHealth(90);
        saved.setPlayerMaxHealth(100);
        RunSaveManager.saveRunData(saved);

        // 2. Khởi tạo mới GamePanel giống hệt lúc người chơi vừa bật lại game sau khi tắt
        com.groupproject.game.core.GamePanel panel = new com.groupproject.game.core.GamePanel();
        assertFalse(PlayingState.isSpawnDirectlyInBossRoom(), "Mặc định khi bật GamePanel không được bật cờ spawnDirectlyInBossRoom");

        GameStateManager gsm = panel.getStateManager();
        MenuState menuState = (MenuState) gsm.getState(GameStateManager.StateType.MENU);
        PlayingState playingState = (PlayingState) gsm.getState(GameStateManager.StateType.PLAYING);

        // Kể cả khi cờ spawnDirectlyInBossRoom vô tình bị bật trước đó, bấm Tiếp tục chơi vẫn phải ưu tiên nạp bản lưu
        PlayingState.setSpawnDirectlyInBossRoom(true);
        menuState.refreshMenuOptions();
        assertTrue(menuState.hasSave());

        com.groupproject.game.input.KeyboardHandler keyH = new com.groupproject.game.input.KeyboardHandler();
        com.groupproject.game.input.MouseHandler mouseH = new com.groupproject.game.input.MouseHandler();
        keyH.setKeyPressed(java.awt.event.KeyEvent.VK_ENTER, true);
        menuState.update(0.016, keyH, mouseH);
        keyH.setKeyPressed(java.awt.event.KeyEvent.VK_ENTER, false);

        gsm.setState(GameStateManager.StateType.PLAYING);

        assertFalse(playingState.isStandaloneBossFight(), "Bấm Tiếp tục chơi tuyệt đối không được vào chế độ Standalone Boss Fight");
        assertEquals(2, playingState.getCurrentStage(), "Phải nạp đúng Stage 2 từ bản lưu");
        assertEquals(200, playingState.getPlayerCredits(), "Phải khôi phục đúng 200 Credits từ bản lưu");
    }
}

