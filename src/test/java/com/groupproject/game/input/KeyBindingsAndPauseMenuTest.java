package com.groupproject.game.input;

import com.groupproject.game.input.KeyBindings.GameAction;
import com.groupproject.game.state.GameStateManager;
import com.groupproject.game.state.PlayingState;
import com.groupproject.game.ui.PauseMenuUI;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử đơn vị cho Hệ thống Tùy chỉnh Phím (Keybindings) và Menu Tạm dừng (Pause Menu).
 *
 * <p>Kiểm tra:
 * 1. Giá trị phím mặc định của toàn bộ 16 hành động tác chiến (bao gồm Bắn súng, Cận chiến nhanh, Mở Menu).
 * 2. Đổi phím (Rebinding), phát hiện xung đột trùng phím, và khôi phục mặc định.
 * 3. Chuyển đổi mã phím/nút chuột thành chuỗi hiển thị rõ nghĩa (Key Name).
 * 4. Truy vấn trạng thái phím và chuột (isActionDown, isActionPressedOnce) qua KeyboardHandler và MouseHandler.
 * 5. Điều hướng Menu Tạm dừng với 3 mục (Tiếp tục, Cài đặt, Thoát ra menu) bằng phím và ESC.
 * 6. Tính năng mở Pause Menu bằng phím tùy chỉnh (PAUSE_MENU) và đóng băng gameplay trong PlayingState khi tạm dừng.
 */
public class KeyBindingsAndPauseMenuTest {

    private final Component dummyComponent = new Canvas();
    private KeyboardHandler keyH;
    private MouseHandler mouseH;

    @BeforeEach
    void setUp() {
        keyH = new KeyboardHandler();
        mouseH = new MouseHandler();
        KeyBindings.resetToDefaults();
    }

    @AfterEach
    void tearDown() {
        KeyBindings.resetToDefaults();
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
        keyH.update();
    }

    private void clickMouse(int button, int x, int y) {
        MouseEvent press = new MouseEvent(dummyComponent, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, x, y, 1, false, button);
        mouseH.mousePressed(press);
    }

    @Test
    @DisplayName("Kiểm tra cấu hình phím mặc định của toàn bộ 16 hành động tác chiến")
    void testDefaultKeyBindings() {
        // Tác chiến & Bắn súng
        assertEquals(KeyBindings.MOUSE_LEFT, KeyBindings.getKeyCode(GameAction.FIRE));
        assertEquals(KeyBindings.MOUSE_RIGHT, KeyBindings.getKeyCode(GameAction.QUICK_MELEE));

        // Di chuyển
        assertEquals(KeyEvent.VK_W, KeyBindings.getKeyCode(GameAction.MOVE_UP));
        assertEquals(KeyEvent.VK_S, KeyBindings.getKeyCode(GameAction.MOVE_DOWN));
        assertEquals(KeyEvent.VK_A, KeyBindings.getKeyCode(GameAction.MOVE_LEFT));
        assertEquals(KeyEvent.VK_D, KeyBindings.getKeyCode(GameAction.MOVE_RIGHT));

        // Kỹ năng & Tương tác
        assertEquals(KeyEvent.VK_SPACE, KeyBindings.getKeyCode(GameAction.DODGE));
        assertEquals(KeyEvent.VK_F, KeyBindings.getKeyCode(GameAction.INTERACT));
        assertEquals(KeyEvent.VK_R, KeyBindings.getKeyCode(GameAction.RELOAD));
        assertEquals(KeyEvent.VK_Q, KeyBindings.getKeyCode(GameAction.QUICK_SWAP));

        // Hệ thống & Balo
        assertEquals(KeyEvent.VK_ESCAPE, KeyBindings.getKeyCode(GameAction.PAUSE_MENU));
        assertEquals(KeyEvent.VK_TAB, KeyBindings.getKeyCode(GameAction.BACKPACK));

        // Slot Vũ khí
        assertEquals(KeyEvent.VK_1, KeyBindings.getKeyCode(GameAction.SLOT_1));
        assertEquals(KeyEvent.VK_2, KeyBindings.getKeyCode(GameAction.SLOT_2));
        assertEquals(KeyEvent.VK_3, KeyBindings.getKeyCode(GameAction.SLOT_3));
        assertEquals(KeyEvent.VK_4, KeyBindings.getKeyCode(GameAction.SLOT_4));

        // Xác nhận tên hiển thị Slot 1 và 2 không bị gán cứng súng cụ thể
        assertEquals("Vũ khí Slot 1", GameAction.SLOT_1.getDisplayNameVi());
        assertEquals("Vũ khí Slot 2", GameAction.SLOT_2.getDisplayNameVi());
    }

    @Test
    @DisplayName("Kiểm tra đổi phím, phát hiện xung đột và khôi phục mặc định")
    void testRebindingAndConflictDetection() {
        // Đổi phím né đòn từ SPACE sang SHIFT
        KeyBindings.setKeyCode(GameAction.DODGE, KeyEvent.VK_SHIFT);
        assertEquals(KeyEvent.VK_SHIFT, KeyBindings.getKeyCode(GameAction.DODGE));

        // Đổi phím bắn từ Chuột trái sang phím J
        KeyBindings.setKeyCode(GameAction.FIRE, KeyEvent.VK_J);
        assertEquals(KeyEvent.VK_J, KeyBindings.getKeyCode(GameAction.FIRE));

        // Đổi phím mở menu sang phím P
        KeyBindings.setKeyCode(GameAction.PAUSE_MENU, KeyEvent.VK_P);
        assertEquals(KeyEvent.VK_P, KeyBindings.getKeyCode(GameAction.PAUSE_MENU));

        // Kiểm tra phát hiện xung đột: Nếu gán W cho DODGE, sẽ xung đột với MOVE_UP
        assertEquals(GameAction.MOVE_UP, KeyBindings.findConflict(GameAction.DODGE, KeyEvent.VK_W));
        // Gán phím mới chưa dùng (Z) thì không xung đột
        assertNull(KeyBindings.findConflict(GameAction.DODGE, KeyEvent.VK_Z));

        // Khôi phục về mặc định
        KeyBindings.resetToDefaults();
        assertEquals(KeyEvent.VK_SPACE, KeyBindings.getKeyCode(GameAction.DODGE));
        assertEquals(KeyBindings.MOUSE_LEFT, KeyBindings.getKeyCode(GameAction.FIRE));
        assertEquals(KeyEvent.VK_ESCAPE, KeyBindings.getKeyCode(GameAction.PAUSE_MENU));
    }

    @Test
    @DisplayName("Kiểm tra định dạng tên phím và nút chuột thân thiện cho giao diện người dùng")
    void testKeyNames() {
        assertEquals("CHUỘT TRÁI", KeyBindings.getKeyName(KeyBindings.MOUSE_LEFT));
        assertEquals("CHUỘT PHẢI", KeyBindings.getKeyName(KeyBindings.MOUSE_RIGHT));
        assertEquals("SPACE", KeyBindings.getKeyName(KeyEvent.VK_SPACE));
        assertEquals("ESC", KeyBindings.getKeyName(KeyEvent.VK_ESCAPE));
        assertEquals("ENTER", KeyBindings.getKeyName(KeyEvent.VK_ENTER));
        assertEquals("TAB", KeyBindings.getKeyName(KeyEvent.VK_TAB));
        assertEquals("W", KeyBindings.getKeyName(KeyEvent.VK_W));
        assertEquals("F", KeyBindings.getKeyName(KeyEvent.VK_F));
    }

    @Test
    @DisplayName("Kiểm tra truy vấn phím isActionDown và isActionPressedOnce cho cả chuột và phím")
    void testActionQueriesWithKeyboardAndMouseHandler() {
        // 1. Kiểm tra phím bàn phím
        pressKey(KeyEvent.VK_W, 'w');
        assertTrue(KeyBindings.isActionDown(keyH, mouseH, GameAction.MOVE_UP));
        assertTrue(KeyBindings.isActionPressedOnce(keyH, mouseH, GameAction.MOVE_UP));

        keyH.update();
        assertFalse(KeyBindings.isActionPressedOnce(keyH, mouseH, GameAction.MOVE_UP));
        assertTrue(KeyBindings.isActionDown(keyH, mouseH, GameAction.MOVE_UP));

        releaseKey(KeyEvent.VK_W, 'w');
        assertFalse(KeyBindings.isActionDown(keyH, mouseH, GameAction.MOVE_UP));

        // 2. Kiểm tra nút chuột cho FIRE và QUICK_MELEE
        clickMouse(MouseEvent.BUTTON1, 100, 100);
        assertTrue(KeyBindings.isActionDown(keyH, mouseH, GameAction.FIRE));
        assertTrue(KeyBindings.isActionPressedOnce(keyH, mouseH, GameAction.FIRE));

        clickMouse(MouseEvent.BUTTON3, 100, 100);
        assertTrue(KeyBindings.isActionDown(keyH, mouseH, GameAction.QUICK_MELEE));
        assertTrue(KeyBindings.isActionPressedOnce(keyH, mouseH, GameAction.QUICK_MELEE));
    }

    @Test
    @DisplayName("Kiểm tra Menu Tạm dừng: Khởi tạo, xử lý ESC tiếp tục chơi")
    void testPauseMenuOpenAndResumeViaEsc() {
        PauseMenuUI menu = new PauseMenuUI();
        menu.open();

        assertEquals(PauseMenuUI.SubMenu.MAIN, menu.getCurrentSubMenu());
        assertEquals(0, menu.getSelectedMainIndex()); // Mục 0: TIẾP TỤC

        // Bấm ESC từ menu chính sẽ trả về lệnh RESUME
        PauseMenuUI.PauseAction action = menu.handleEsc();
        assertEquals(PauseMenuUI.PauseAction.RESUME, action);
    }

    @Test
    @DisplayName("Kiểm tra điều hướng 3 mục Menu Tạm dừng (Tiếp tục, Cài đặt, Thoát ra menu)")
    void testPauseMenuNavigation() {
        PauseMenuUI menu = new PauseMenuUI();
        menu.open();

        // 1. Mục 0: TIẾP TỤC -> Bấm ENTER -> RESUME
        pressKey(KeyEvent.VK_ENTER, '\n');
        PauseMenuUI.PauseAction action0 = menu.update(keyH, mouseH, 0.016);
        assertEquals(PauseMenuUI.PauseAction.RESUME, action0);
        releaseKey(KeyEvent.VK_ENTER, '\n');

        // 2. Chuyển xuống Mục 1: CÀI ĐẶT
        pressKey(KeyEvent.VK_DOWN, ' ');
        menu.update(keyH, mouseH, 0.016);
        releaseKey(KeyEvent.VK_DOWN, ' ');
        assertEquals(1, menu.getSelectedMainIndex());

        // Bấm ENTER vào CÀI ĐẶT -> Chuyển sang SubMenu SETTINGS
        pressKey(KeyEvent.VK_ENTER, '\n');
        PauseMenuUI.PauseAction action1 = menu.update(keyH, mouseH, 0.016);
        assertEquals(PauseMenuUI.PauseAction.NONE, action1);
        assertEquals(PauseMenuUI.SubMenu.SETTINGS, menu.getCurrentSubMenu());
        releaseKey(KeyEvent.VK_ENTER, '\n');

        // Bấm ESC từ CÀI ĐẶT -> Trở về Menu chính
        PauseMenuUI.PauseAction escFromSettings = menu.handleEsc();
        assertEquals(PauseMenuUI.PauseAction.NONE, escFromSettings);
        assertEquals(PauseMenuUI.SubMenu.MAIN, menu.getCurrentSubMenu());

        // 3. Chuyển xuống Mục 2: THOÁT RA MENU
        pressKey(KeyEvent.VK_DOWN, ' ');
        menu.update(keyH, mouseH, 0.016);
        releaseKey(KeyEvent.VK_DOWN, ' ');
        assertEquals(2, menu.getSelectedMainIndex());

        pressKey(KeyEvent.VK_ENTER, '\n');
        PauseMenuUI.PauseAction action2 = menu.update(keyH, mouseH, 0.016);
        assertEquals(PauseMenuUI.PauseAction.EXIT_TO_MENU, action2);
    }

    @Test
    @DisplayName("Kiểm tra quy trình gán phím mới và nút chuột trong Menu Cài đặt")
    void testRebindingFlowInSettingsMenu() {
        PauseMenuUI menu = new PauseMenuUI();
        menu.open();
        menu.setCurrentSubMenu(PauseMenuUI.SubMenu.SETTINGS);

        // Kích hoạt trạng thái gán phím cho DODGE
        menu.setRebindingAction(GameAction.DODGE);
        assertEquals(GameAction.DODGE, menu.getRebindingAction());

        // Bấm ESC để hủy gán phím
        PauseMenuUI.PauseAction escCancel = menu.handleEsc();
        assertEquals(PauseMenuUI.PauseAction.NONE, escCancel);
        assertNull(menu.getRebindingAction());
        assertEquals(KeyEvent.VK_SPACE, KeyBindings.getKeyCode(GameAction.DODGE));

        // Kích hoạt lại và bấm phím C để gán phím mới
        menu.setRebindingAction(GameAction.DODGE);
        pressKey(KeyEvent.VK_C, 'c');
        menu.update(keyH, mouseH, 0.016);
        releaseKey(KeyEvent.VK_C, 'c');

        // Phím né đòn đã được gán sang phím C
        assertEquals(KeyEvent.VK_C, KeyBindings.getKeyCode(GameAction.DODGE));
        assertNull(menu.getRebindingAction());

        // Gán nút Chuột Phải cho FIRE qua click chuột phải
        menu.setRebindingAction(GameAction.FIRE);
        clickMouse(MouseEvent.BUTTON3, 200, 200);
        menu.update(keyH, mouseH, 0.016);
        assertEquals(KeyBindings.MOUSE_RIGHT, KeyBindings.getKeyCode(GameAction.FIRE));
    }

    @Test
    @DisplayName("Kiểm tra PlayingState mở Pause Menu bằng phím tùy chỉnh PAUSE_MENU và đóng băng khi tạm dừng")
    void testPlayingStatePauseIntegration() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();

        assertFalse(state.isPaused());

        // 1. Tùy chỉnh phím mở menu sang phím P
        KeyBindings.setKeyCode(GameAction.PAUSE_MENU, KeyEvent.VK_P);

        // Bấm phím P trong trận chiến -> Kích hoạt tạm dừng
        pressKey(KeyEvent.VK_P, 'p');
        state.update(0.016, keyH, mouseH);
        releaseKey(KeyEvent.VK_P, 'p');

        assertTrue(state.isPaused(), "Phím P tùy chỉnh phải mở được Pause Menu");

        // 2. Khi đang tạm dừng: Người chơi bấm phím di chuyển W -> Vị trí KHÔNG thay đổi vì gameplay bị đóng băng
        double initialY = state.getPlayerY();
        pressKey(KeyEvent.VK_W, 'w');
        state.update(0.1, keyH, mouseH);
        releaseKey(KeyEvent.VK_W, 'w');

        assertEquals(initialY, state.getPlayerY(), 0.001, "Người chơi không được di chuyển khi game đang tạm dừng");

        // 3. Bấm phím P lần nữa -> Tiếp tục trò chơi (Unpause)
        pressKey(KeyEvent.VK_P, 'p');
        state.update(0.016, keyH, mouseH);
        releaseKey(KeyEvent.VK_P, 'p');

        assertFalse(state.isPaused(), "Bấm phím P lần nữa phải đóng Pause Menu và tiếp tục game");
    }

    @Test
    @DisplayName("Kiểm tra MenuState màn hình chính có mục CÀI ĐẶT PHÍM và mở được Settings trước khi vào chơi")
    void testMenuStateSettingsIntegration() {
        GameStateManager gsm = new GameStateManager();
        com.groupproject.game.state.MenuState menuState = new com.groupproject.game.state.MenuState(gsm);
        menuState.init();

        assertFalse(menuState.isShowSettings());

        // Điều hướng xuống mục 2: CÀI ĐẶT PHÍM (SETTINGS)
        pressKey(KeyEvent.VK_DOWN, ' ');
        menuState.update(0.016, keyH, mouseH);
        releaseKey(KeyEvent.VK_DOWN, ' ');

        pressKey(KeyEvent.VK_DOWN, ' ');
        menuState.update(0.016, keyH, mouseH);
        releaseKey(KeyEvent.VK_DOWN, ' ');
        assertEquals(2, menuState.getSelectedOption());

        // Bấm ENTER để mở màn hình Cài đặt phím
        pressKey(KeyEvent.VK_ENTER, '\n');
        menuState.update(0.016, keyH, mouseH);
        releaseKey(KeyEvent.VK_ENTER, '\n');

        assertTrue(menuState.isShowSettings(), "Mục 2 phải mở được màn hình Cài đặt phím trước khi vào trận");
        assertEquals(PauseMenuUI.SubMenu.SETTINGS, menuState.getSettingsUI().getCurrentSubMenu());

        // Bấm ESC để đóng Cài đặt phím và quay lại Main Menu
        pressKey(KeyEvent.VK_ESCAPE, (char) 27);
        menuState.update(0.016, keyH, mouseH);
        releaseKey(KeyEvent.VK_ESCAPE, (char) 27);

        assertFalse(menuState.isShowSettings(), "Bấm ESC phải đóng Cài đặt phím và quay về Main Menu");
    }
}
