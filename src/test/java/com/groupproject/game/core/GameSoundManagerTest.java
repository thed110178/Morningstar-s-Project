package com.groupproject.game.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.groupproject.game.core.GameSoundManager.BgmTrack;
import com.groupproject.game.core.GameSoundManager.SoundEffect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Kiểm thử toàn diện hệ thống hiệu ứng âm thanh (GameSoundManager).
 */
public class GameSoundManagerTest {

    @BeforeEach
    public void setUp() {
        GameSoundManager.init();
        GameSoundManager.setMuted(false);
    }

    @AfterEach
    public void tearDown() {
        GameSoundManager.setMuted(false);
    }

    @Test
    @DisplayName("Tất cả hiệu ứng âm thanh SFX đều được nạp thành công")
    public void testAllSoundEffectsLoaded() {
        assertEquals(51, SoundEffect.values().length, "Phải có đúng 51 loại âm thanh SoundEffect");

        for (SoundEffect sfx : SoundEffect.values()) {
            assertNotNull(sfx.getFilename(), "Tên file âm thanh không được null: " + sfx.name());
            if (!sfx.isOptional()) {
                assertTrue(
                        GameSoundManager.isSoundLoaded(sfx),
                        "Hiệu ứng âm thanh phải được nạp thành công: " + sfx.name() + " (" + sfx.getFilename() + ")"
                );
            }
            assertTrue(sfx.getPoolSize() >= 2, "Pool size phải ít nhất là 2 cho: " + sfx.name());
        }

        // Xác thực các hiệu ứng mới của Sniper, Shotgun và 7 hiệu ứng tương tác / gameplay / UI mới
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.GUN_SCR_SHOOT), "Tiếng bắn Semi-Carbine SCR phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.AMMO_PICKUP), "Tiếng nhặt hộp đạn Ammo Pickup phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.CRITICAL_HIT), "Tiếng sát thương chí mạng Critical Hit phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.PLAYER_GOT_HIT), "Tiếng nhân vật bị trúng đạn Player Got Hit phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.GUN_SCR_RELOAD), "Tiếng nạp đạn Semi-Carbine SCR phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.GUN_EMPTY_SHOT), "Tiếng bắn khi hết đạn/đang nạp đạn (GUN_EMPTY_SHOT) phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.GUN_SCR_DRAW), "Tiếng rút súng Semi-Carbine SCR phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.GUN_SNIPER_SHOOT), "Tiếng bắn Sniper phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.GUN_SNIPER_LOADING_AMMO), "Tiếng lên đạn Sniper phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.GUN_SHOTGUN_LOADING_AMMO), "Tiếng lên đạn từng phát của Shotgun phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.GUN_SAS_DRAW), "Tiếng rút súng Semi-auto Shotgun SAS phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.GUN_SAS_RELOAD), "Tiếng nạp đạn Semi-auto Shotgun SAS phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.GUN_SAS_SHOOT), "Tiếng bắn Semi-auto Shotgun SAS phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.INTERACT), "Tiếng tương tác Interact phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.HEALING), "Tiếng hồi máu Healing phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.COIN_PICK_UP), "Tiếng nhặt xu Coin-pick-up phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.GAME_OVER), "Tiếng kết thúc trận Game Over phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.BUY_COMPLETE), "Tiếng mua hàng Buy Complete phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.BUY_FAILED), "Tiếng mua thất bại Buy Failed phải nạp thành công");
                assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.UI_INTERACT), "Tiếng tương tác giao diện UI Interact phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.ENEMY_DEAD_1), "Tiếng chết kẻ địch 1 phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.ENEMY_DEAD_2), "Tiếng chết kẻ địch 2 phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.ENEMY_DEAD_3), "Tiếng chết kẻ địch 3 phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.BOMB_BEEP), "Tiếng đếm ngược bom Grenadier phải nạp thành công");
        assertTrue(GameSoundManager.isSoundLoaded(SoundEffect.GRENADIER_EXPLOSION), "Tiếng nổ bom Grenadier phải nạp thành công");

        // Kiểm tra kích thước pool riêng cho súng auto và shotgun nạp từng shell
        assertTrue(SoundEffect.GUN_SMG_SHOOT.getPoolSize() >= 10, "SMG auto sấy liên thanh phải có pool lớn >= 10");
        assertTrue(SoundEffect.GUN_RIFLE_SHOOT.getPoolSize() >= 8, "AR auto sấy liên thanh phải có pool lớn >= 8");
        assertTrue(SoundEffect.GUN_SHOTGUN_RELOAD.getPoolSize() >= 6, "Shotgun nạp từng shell phải có pool >= 6");
    }

    @Test
    @DisplayName("Gọi phát âm thanh play(), playReload(), playChamber() và playShotgunShellReload() phải an toàn và không bao giờ ném Exception")
    public void testPlayAllSoundsWithoutException() {
        for (SoundEffect sfx : SoundEffect.values()) {
            // Phát âm thanh với âm lượng mặc định
            GameSoundManager.play(sfx);
            // Phát âm thanh với hệ số âm lượng tùy chỉnh
            GameSoundManager.play(sfx, 0.5f);
        }

        // Test kênh reload, lên đạn từng phát (chamber) và dừng reload
        GameSoundManager.playReload(SoundEffect.GUN_PISTOL_RELOAD);
        GameSoundManager.playShotgunShellReload();
        GameSoundManager.playChamber(SoundEffect.GUN_SNIPER_LOADING_AMMO);
        GameSoundManager.playChamber(SoundEffect.GUN_SHOTGUN_LOADING_AMMO);
        GameSoundManager.stopReload();
        GameSoundManager.playRandomEnemyDead();
        GameSoundManager.stopEffect(SoundEffect.BOMB_BEEP);
        GameSoundManager.playCriticalHit();
    }

    @Test
    @DisplayName("Kiểm tra âm lượng hiệu ứng chí mạng CRITICAL_HIT được tăng cường đanh giòn")
    public void testCriticalHitVolumeBoost() {
        assertEquals(1.35f, SoundEffect.CRITICAL_HIT.getDefaultVolume(), 0.001f, "Âm lượng chí mạng phải được tăng nhẹ lên 1.35x (+35%)");
        assertEquals(1.0f, SoundEffect.HIT_TARGET.getDefaultVolume(), 0.001f, "Âm lượng đòn đánh thường là 1.0x");
        GameSoundManager.playCriticalHit();
    }

    @Test
    @DisplayName("Kiểm tra chức năng tắt/bật âm thanh Mute")
    public void testMuteControl() {
        GameSoundManager.setMuted(true);
        assertTrue(GameSoundManager.isMuted(), "Sau khi setMuted(true) phải là true");

        // Gọi play khi đang mute vẫn an toàn
        GameSoundManager.play(SoundEffect.GUN_PISTOL_SHOOT);
        GameSoundManager.playReload(SoundEffect.GUN_PISTOL_RELOAD);

        GameSoundManager.setMuted(false);
        assertFalse(GameSoundManager.isMuted(), "Sau khi setMuted(false) phải là false");
    }

    @Test
    @DisplayName("Kiểm tra thiết lập Master Volume và kẹp ngưỡng an toàn [0.0 .. 1.0]")
    public void testMasterVolumeClamping() {
        GameSoundManager.setMasterVolume(0.8f);
        assertEquals(0.8f, GameSoundManager.getMasterVolume(), 0.001f);

        // Kẹp cận dưới 0.0
        GameSoundManager.setMasterVolume(-0.5f);
        assertEquals(0.0f, GameSoundManager.getMasterVolume(), 0.001f);

        // Kẹp cận trên 1.0
        GameSoundManager.setMasterVolume(1.8f);
        assertEquals(1.0f, GameSoundManager.getMasterVolume(), 0.001f);
    }

    @Test
    @DisplayName("Kiểm tra lưu và nạp cài đặt âm thanh (Persistence)")
    public void testSaveAndLoadSettings() {
        String testPath = "target/test_audio_settings.properties";
        GameSoundManager.setSettingsFilePath(testPath);

        // Lưu cài đặt 65% và Muted = true
        GameSoundManager.setMasterVolume(0.65f);
        GameSoundManager.setMuted(true);

        // Đọc lại từ file
        GameSoundManager.loadSettings();
        assertEquals(0.65f, GameSoundManager.getMasterVolume(), 0.01f);
        assertTrue(GameSoundManager.isMuted());

        // Thay đổi sang 40% và Unmute
        GameSoundManager.setMasterVolume(0.40f);
        GameSoundManager.setMuted(false);

        // Tải lại
        GameSoundManager.loadSettings();
        assertEquals(0.40f, GameSoundManager.getMasterVolume(), 0.01f);
        assertFalse(GameSoundManager.isMuted());

        // Khôi phục lại đường dẫn chuẩn
        GameSoundManager.setSettingsFilePath("save/audio_settings.properties");
        GameSoundManager.loadSettings();
    }

    @Test
    @DisplayName("Kiểm tra tách biệt cài đặt âm lượng và tắt tiếng giữa SFX và BGM")
    public void testSfxAndBgmVolumeSeparation() {
        String testPath = "target/test_separated_audio_settings.properties";
        GameSoundManager.setSettingsFilePath(testPath);

        // 1. Kiểm tra độc lập giá trị âm lượng SFX và BGM
        GameSoundManager.setSfxVolume(0.85f);
        GameSoundManager.setBgmVolume(0.25f);
        assertEquals(0.85f, GameSoundManager.getSfxVolume(), 0.001f, "Âm lượng SFX phải là 85%");
        assertEquals(0.25f, GameSoundManager.getBgmVolume(), 0.001f, "Âm lượng BGM phải là 25%");

        // 2. Kiểm tra kẹp ngưỡng giá trị hợp lệ [0.0 .. 1.0]
        GameSoundManager.setSfxVolume(-0.4f);
        assertEquals(0.0f, GameSoundManager.getSfxVolume(), 0.001f, "SFX volume kẹp cận dưới 0.0");
        GameSoundManager.setSfxVolume(2.0f);
        assertEquals(1.0f, GameSoundManager.getSfxVolume(), 0.001f, "SFX volume kẹp cận trên 1.0");

        GameSoundManager.setBgmVolume(-0.4f);
        assertEquals(0.0f, GameSoundManager.getBgmVolume(), 0.001f, "BGM volume kẹp cận dưới 0.0");
        GameSoundManager.setBgmVolume(2.0f);
        assertEquals(1.0f, GameSoundManager.getBgmVolume(), 0.001f, "BGM volume kẹp cận trên 1.0");

        // 3. Kiểm tra bật/tắt tiếng độc lập (Independent Mute)
        GameSoundManager.setSfxMuted(false);
        GameSoundManager.setBgmMuted(false);
        assertFalse(GameSoundManager.isSfxMuted(), "SFX ban đầu không tắt tiếng");
        assertFalse(GameSoundManager.isBgmMuted(), "BGM ban đầu không tắt tiếng");

        GameSoundManager.setSfxMuted(true);
        assertTrue(GameSoundManager.isSfxMuted(), "SFX đã tắt tiếng");
        assertFalse(GameSoundManager.isBgmMuted(), "BGM vẫn phát bình thường");

        GameSoundManager.setSfxMuted(false);
        GameSoundManager.setBgmMuted(true);
        assertFalse(GameSoundManager.isSfxMuted(), "SFX vẫn phát bình thường");
        assertTrue(GameSoundManager.isBgmMuted(), "BGM đã tắt tiếng");

        // 4. Kiểm tra lưu và nạp cài đặt tách biệt từ file properties
        GameSoundManager.setSfxVolume(0.70f);
        GameSoundManager.setBgmVolume(0.35f);
        GameSoundManager.setSfxMuted(true);
        GameSoundManager.setBgmMuted(false);

        // Nạp lại cài đặt từ file đã lưu
        GameSoundManager.loadSettings();
        assertEquals(0.70f, GameSoundManager.getSfxVolume(), 0.01f, "Nạp lại SFX Volume 70%");
        assertEquals(0.35f, GameSoundManager.getBgmVolume(), 0.01f, "Nạp lại BGM Volume 35%");
        assertTrue(GameSoundManager.isSfxMuted(), "Nạp lại trạng thái SFX Muted = true");
        assertFalse(GameSoundManager.isBgmMuted(), "Nạp lại trạng thái BGM Muted = false");

        // Thay đổi sang cấu hình khác: SFX 10%, BGM 90%, SFX Unmute, BGM Mute
        GameSoundManager.setSfxVolume(0.10f);
        GameSoundManager.setBgmVolume(0.90f);
        GameSoundManager.setSfxMuted(false);
        GameSoundManager.setBgmMuted(true);

        GameSoundManager.loadSettings();
        assertEquals(0.10f, GameSoundManager.getSfxVolume(), 0.01f, "Nạp lại SFX Volume 10%");
        assertEquals(0.90f, GameSoundManager.getBgmVolume(), 0.01f, "Nạp lại BGM Volume 90%");
        assertFalse(GameSoundManager.isSfxMuted(), "Nạp lại trạng thái SFX Muted = false");
        assertTrue(GameSoundManager.isBgmMuted(), "Nạp lại trạng thái BGM Muted = true");

        // Khôi phục đường dẫn chuẩn
        GameSoundManager.setSettingsFilePath("save/audio_settings.properties");
        GameSoundManager.loadSettings();
    }

    @Test
    @DisplayName("Tất cả 7 bản nhạc nền BGM và luồng chuyển đổi trạng thái (Menu, Stage 1-3, The Range, Game Over, Boss Fight) hoạt động chính xác")
    public void testAllBgmTracksAndStateTransitions() {
        assertEquals(7, BgmTrack.values().length, "Phải có đầy đủ 7 bản nhạc nền BGM");

        for (BgmTrack track : BgmTrack.values()) {
            assertTrue(
                    GameSoundManager.isBgmLoaded(track),
                    "Bản nhạc nền BGM phải tồn tại và nạp thành công: " + track.name() + " (" + track.getResourcePath() + ")"
            );
        }

        // 1. Menu BGM
        GameSoundManager.playBgm(BgmTrack.MENU);
        assertEquals(BgmTrack.MENU, GameSoundManager.getCurrentBgmTrack());
        assertTrue(BgmTrack.MENU.isLoop());

        // 2. Stage 1-3 BGM (Survival Mode)
        GameSoundManager.playStageBgm(1);
        assertEquals(BgmTrack.STAGE_1_3, GameSoundManager.getCurrentBgmTrack());
        GameSoundManager.playStageBgm(2);
        assertEquals(BgmTrack.STAGE_1_3, GameSoundManager.getCurrentBgmTrack());
        GameSoundManager.playStageBgm(3);
        assertEquals(BgmTrack.STAGE_1_3, GameSoundManager.getCurrentBgmTrack());

        // 3. Khi bước sang Stage 4 (trước khi gặp Boss): ngắt nhạc Stage 1-3, chờ Cinematic Boss
        GameSoundManager.playStageBgm(4);
        assertNull(GameSoundManager.getCurrentBgmTrack());

        // 4. Bắt đầu Cinematic giới thiệu Boss (2.8s): phát BOSS_INTRO và xếp hàng nối tiếp BOSS_LOOP
        GameSoundManager.startBossIntroBgm(2.8);
        assertEquals(BgmTrack.BOSS_INTRO, GameSoundManager.getCurrentBgmTrack());
        assertEquals(BgmTrack.BOSS_LOOP, GameSoundManager.getQueuedNextBgmTrack());

        // 5. Khi kết thúc hoặc bỏ qua Cinematic Boss -> chuyển sang BOSS_LOOP
        GameSoundManager.transitionBossIntroToLoop();
        assertEquals(BgmTrack.BOSS_LOOP, GameSoundManager.getCurrentBgmTrack());

        // 6. Khi màn Boss được Clear -> chuyển sang BOSS_END
        GameSoundManager.playBossClearBgm();
        assertEquals(BgmTrack.BOSS_END, GameSoundManager.getCurrentBgmTrack());
        assertFalse(BgmTrack.BOSS_END.isLoop());

        // 7. Khi người chơi thua (Game Over) -> phát GAME_OVER, kể cả khi chuyển về The Range vẫn giữ GAME_OVER
        //    và xếp hàng THE_RANGE chạy ngay sau khi GAME_OVER kết thúc
        GameSoundManager.playGameOverBgm();
        assertEquals(BgmTrack.GAME_OVER, GameSoundManager.getCurrentBgmTrack());
        assertEquals(BgmTrack.THE_RANGE, GameSoundManager.getQueuedNextBgmTrack());

        // Chuyển cảnh về The Range trong lúc nhạc GAME_OVER đang chạy
        GameSoundManager.playRangeBgm();
        assertEquals(BgmTrack.GAME_OVER, GameSoundManager.getCurrentBgmTrack(), "Nhạc Game Over phải tiếp tục chạy khi chuyển về The Range");
        assertEquals(BgmTrack.THE_RANGE, GameSoundManager.getQueuedNextBgmTrack(), "Nhạc The Range phải được xếp hàng phát ngay khi hết bài Game Over");

        // Dọn dẹp BGM sau test
        GameSoundManager.stopBgm();
        assertNull(GameSoundManager.getCurrentBgmTrack());
    }
}
