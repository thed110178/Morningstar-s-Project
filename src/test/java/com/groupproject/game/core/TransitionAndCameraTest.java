package com.groupproject.game.core;

import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import com.groupproject.game.state.GameStateManager;
import com.groupproject.game.state.PlayingState;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.event.KeyEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying camera instant snapping upon stage transitions
 * and screen transition effects when exiting from playing state to main menu.
 */
class TransitionAndCameraTest {

    private GameStateManager gsm;
    private PlayingState playingState;
    private KeyboardHandler keyH;
    private MouseHandler mouseH;
    private final Component dummyComponent = new Canvas();

    @BeforeEach
    void setUp() {
        gsm = new GameStateManager();
        playingState = new PlayingState(gsm);
        gsm.registerState(GameStateManager.StateType.PLAYING, playingState);
        gsm.registerState(GameStateManager.StateType.MENU, new com.groupproject.game.state.MenuState(gsm));
        playingState.init();

        keyH = new KeyboardHandler();
        mouseH = new MouseHandler();
    }

    private void pressKey(int keyCode, char keyChar) {
        KeyEvent press = new KeyEvent(dummyComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, keyChar);
        keyH.keyPressed(press);
        keyH.update();
    }

    private void releaseKey(int keyCode, char keyChar) {
        KeyEvent release = new KeyEvent(dummyComponent, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, keyCode, keyChar);
        keyH.keyReleased(release);
        keyH.update();
    }

    @Test
    @DisplayName("Verify camera immediately snaps to player spawn position on advanceToNextStage without Lerp lag")
    void testCameraSnapsImmediatelyOnStageAdvance() {
        Camera camera = playingState.getCamera();
        assertNotNull(camera);

        // Đặt camera ở góc (0, 0)
        camera.snapTo(0.0, 0.0);
        assertEquals(0.0, camera.getX(), 0.001);
        assertEquals(0.0, camera.getY(), 0.001);

        // Kích hoạt chuyển tầng
        playingState.advanceToNextStage();

        // Kiểm tra camera phải ngay lập tức snap vào trọng tâm người chơi ở tầng mới
        double playerCenterX = playingState.getPlayer().getCenterX();
        double playerCenterY = playingState.getPlayer().getCenterY();

        double expectedCamTargetX = playerCenterX - (GameConfig.SCREEN_WIDTH / 2.0);
        double expectedCamTargetY = playerCenterY - (GameConfig.SCREEN_HEIGHT / 2.0);

        // Giới hạn clamp trong world
        double clampedX = Math.max(0, Math.min(GameConfig.WORLD_WIDTH - GameConfig.SCREEN_WIDTH, expectedCamTargetX));
        double clampedY = Math.max(0, Math.min(GameConfig.WORLD_HEIGHT - GameConfig.SCREEN_HEIGHT, expectedCamTargetY));

        assertEquals(clampedX, camera.getX(), 1.0,
                "Camera X must immediately equal the clamped target coordinate without smooth lerp lag");
        assertEquals(clampedY, camera.getY(), 1.0,
                "Camera Y must immediately equal the clamped target coordinate without smooth lerp lag");
    }

    @Test
    @DisplayName("Verify camera stays locked on player throughout stage transition phases until completion")
    void testCameraLockedOnPlayerDuringStageTransition() {
        Camera camera = playingState.getCamera();

        // Bắt đầu quá trình chuyển cảnh
        playingState.requestAdvanceToNextStage();
        assertTrue(playingState.isStageTransitionActive(), "Stage transition must be active");

        // 1. Giai đoạn Fading Out (0.45s)
        playingState.update(0.25, keyH, mouseH);
        assertTrue(playingState.isStageTransitionActive());

        // 2. Chuyển sang HOLD và thực thi callback advanceToNextStage (sau 0.45s)
        playingState.update(0.25, keyH, mouseH);
        assertTrue(playingState.isStageTransitionActive());

        // Sau khi map mới được tạo, camera phải ở ngay vị trí player mới
        double newPlayerX = playingState.getPlayer().getCenterX();
        double newPlayerY = playingState.getPlayer().getCenterY();
        double clampedX = Math.max(0, Math.min(GameConfig.WORLD_WIDTH - GameConfig.SCREEN_WIDTH, newPlayerX - (GameConfig.SCREEN_WIDTH / 2.0)));
        double clampedY = Math.max(0, Math.min(GameConfig.WORLD_HEIGHT - GameConfig.SCREEN_HEIGHT, newPlayerY - (GameConfig.SCREEN_HEIGHT / 2.0)));

        assertEquals(clampedX, camera.getX(), 1.0);
        assertEquals(clampedY, camera.getY(), 1.0);

        // 3. Giai đoạn Fading In (0.45s)
        playingState.update(0.40, keyH, mouseH);
        assertEquals(clampedX, camera.getX(), 1.0, "Camera must remain securely snapped during FADING_IN");

        // 4. Kết thúc transition
        playingState.update(0.50, keyH, mouseH);
        assertFalse(playingState.isStageTransitionActive(), "Transition should now be finished");
        assertEquals(clampedX, camera.getX(), 1.0, "Camera must already be at the player's coordinate when transition completes");
    }

    @Test
    @DisplayName("Verify exit to main menu from PauseMenu triggers screen transition instead of abruptly changing state")
    void testExitToMenuTriggersScreenTransition() {
        // Mở Menu Tạm dừng
        playingState.setPaused(true);
        assertTrue(playingState.isPaused());

        // Đặt mục chọn là số 2: THOÁT RA MENU
        playingState.getPauseMenuUI().setSelectedMainIndex(2);
        assertEquals(2, playingState.getPauseMenuUI().getSelectedMainIndex(), "Must be selecting option 2 (EXIT TO MENU)");

        // Nhấn ENTER để kích hoạt thoát ra Menu
        pressKey(KeyEvent.VK_ENTER, '\n');
        playingState.update(0.016, keyH, mouseH);
        releaseKey(KeyEvent.VK_ENTER, '\n');

        // Kiểm tra: GameStateManager phải đang chạy transition mờ dần
        assertTrue(gsm.getScreenTransition().isActive(),
                "Exiting to Main Menu must trigger a screen transition instead of abruptly snapping to Menu");
    }
}
