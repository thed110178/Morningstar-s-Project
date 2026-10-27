package com.groupproject.game.ui;

import com.groupproject.game.input.KeyBindings;
import com.groupproject.game.input.KeyBindings.GameAction;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Giao diện Menu Tạm dừng (Pause Menu UI) và Trình tùy chỉnh phím điều khiển (Keybind Settings).
 *
 * <p>WHY: Cho phép người chơi bấm phím ESC để tạm dừng trò chơi với 3 tùy chọn trực quan:
 * 1. "TIẾP TỤC" (Resume / Tiếp tục chiến đấu).
 * 2. "CÀI ĐẶT" (Settings / Tùy chỉnh phím điều khiển Keybind).
 * 3. "THOÁT RA MENU" (Exit to Menu / Thoát về màn hình chính).
 *
 * <p>Hỗ trợ đầy đủ tương tác chuột (Hover & Click) cùng phím điều hướng (Mũi tên / WASD / Enter / ESC).
 */
public class PauseMenuUI {

    public enum PauseAction {
        NONE,
        RESUME,
        EXIT_TO_MENU
    }

    public enum SubMenu {
        MAIN,
        SETTINGS
    }

    private SubMenu currentSubMenu = SubMenu.MAIN;

    // Các tùy chọn chính của Menu Tạm dừng
    private static final String[] MAIN_OPTIONS = {
        "TIẾP TỤC",
        "CÀI ĐẶT",
        "THOÁT RA MENU"
    };

    private int selectedMainIndex = 0;
    private final Rectangle[] mainOptionBounds = new Rectangle[3];

    // Cài đặt phím (Keybind Settings) & Hiển thị
    private GameAction rebindingAction = null;
    private final Map<GameAction, Rectangle> keybindBounds = new HashMap<>();
    private final Rectangle resetDefaultsBtnBounds = new Rectangle();
    private final Rectangle backFromSettingsBtnBounds = new Rectangle();
    private final Rectangle aimGuidelineToggleBounds = new Rectangle();
    private final Rectangle fpsUpsToggleBounds = new Rectangle();

    private boolean isResetHovered = false;
    private boolean isBackHovered = false;
    private boolean isAimGuidelineHovered = false;
    private boolean isFpsUpsHovered = false;
    private GameAction hoveredAction = null;

    private String feedbackMessage = "";
    private double feedbackTimer = 0.0;
    private double pulseTimer = 0.0;

    public PauseMenuUI() {
        for (int i = 0; i < mainOptionBounds.length; i++) {
            mainOptionBounds[i] = new Rectangle();
        }
    }

    /**
     * Mở menu tạm dừng từ trạng thái ban đầu.
     */
    public void open() {
        this.currentSubMenu = SubMenu.MAIN;
        this.selectedMainIndex = 0;
        this.rebindingAction = null;
        this.feedbackMessage = "";
        this.feedbackTimer = 0.0;
    }

    /**
     * Xử lý khi người chơi bấm phím ESC.
     *
     * @return {@code PauseAction.RESUME} nếu bấm ESC tại màn hình tạm dừng chính để tiếp tục game,
     *         hoặc {@code PauseAction.NONE} nếu quay lại từ menu cài đặt / hủy gán phím.
     */
    public PauseAction handleEsc() {
        if (currentSubMenu == SubMenu.SETTINGS) {
            if (rebindingAction != null) {
                // Hủy gán phím
                rebindingAction = null;
                feedbackMessage = "ĐÃ HỦY GÁN PHÍM";
                feedbackTimer = 1.5;
                return PauseAction.NONE;
            } else {
                // Thoát menu cài đặt trở về menu tạm dừng chính
                currentSubMenu = SubMenu.MAIN;
                selectedMainIndex = 1;
                return PauseAction.NONE;
            }
        }
        return PauseAction.RESUME;
    }

    /**
     * Cập nhật các tương tác chuột và bàn phím trong menu tạm dừng.
     */
    public PauseAction update(KeyboardHandler keyH, MouseHandler mouseH, double deltaTime) {
        pulseTimer += deltaTime;
        if (feedbackTimer > 0) {
            feedbackTimer -= deltaTime;
            if (feedbackTimer <= 0) {
                feedbackMessage = "";
            }
        }

        if (currentSubMenu == SubMenu.MAIN) {
            return updateMainMenu(keyH, mouseH);
        } else {
            return updateSettingsMenu(keyH, mouseH);
        }
    }

    private PauseAction updateMainMenu(KeyboardHandler keyH, MouseHandler mouseH) {
        // 1. Phím mũi tên / WASD điều hướng
        if (keyH.isKeyPressedOnce(KeyEvent.VK_UP) || keyH.isKeyPressedOnce(KeyEvent.VK_W)) {
            selectedMainIndex--;
            if (selectedMainIndex < 0) selectedMainIndex = MAIN_OPTIONS.length - 1;
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_DOWN) || keyH.isKeyPressedOnce(KeyEvent.VK_S)) {
            selectedMainIndex++;
            if (selectedMainIndex >= MAIN_OPTIONS.length) selectedMainIndex = 0;
        }

        // 2. Tương tác chuột Hover & Click
        int mouseX = mouseH.getScreenX();
        int mouseY = mouseH.getScreenY();
        for (int i = 0; i < mainOptionBounds.length; i++) {
            if (mainOptionBounds[i].contains(mouseX, mouseY)) {
                selectedMainIndex = i;
                if (mouseH.isLeftClickedOnce()) {
                    return executeMainOption(i);
                }
            }
        }

        // 3. Phím Enter / Space xác nhận
        if (keyH.isKeyPressedOnce(KeyEvent.VK_ENTER) || keyH.isKeyPressedOnce(KeyEvent.VK_SPACE)) {
            return executeMainOption(selectedMainIndex);
        }

        // 4. Nếu người chơi bấm phím PAUSE_MENU đã tùy chỉnh -> cũng kích hoạt tiếp tục game (Resume)
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.PAUSE_MENU)) {
            return PauseAction.RESUME;
        }

        return PauseAction.NONE;
    }

    private PauseAction executeMainOption(int index) {
        return switch (index) {
            case 0 -> PauseAction.RESUME;
            case 1 -> {
                currentSubMenu = SubMenu.SETTINGS;
                rebindingAction = null;
                yield PauseAction.NONE;
            }
            case 2 -> PauseAction.EXIT_TO_MENU;
            default -> PauseAction.NONE;
        };
    }

    private PauseAction updateSettingsMenu(KeyboardHandler keyH, MouseHandler mouseH) {
        int mouseX = mouseH.getScreenX();
        int mouseY = mouseH.getScreenY();

        // 1. Nếu đang chờ gán phím mới cho hành động
        if (rebindingAction != null) {
            // Kiểm tra phím bấm từ bàn phím
            int pressedCode = keyH.getAnyKeyPressedOnce();
            if (pressedCode > 0) {
                if (pressedCode == KeyEvent.VK_ESCAPE) {
                    rebindingAction = null;
                    feedbackMessage = "ĐÃ HỦY GÁN PHÍM";
                    feedbackTimer = 1.5;
                } else {
                    applyRebind(rebindingAction, pressedCode);
                    rebindingAction = null;
                }
                return PauseAction.NONE;
            }

            // Kiểm tra bấm chuột phải -> Gán nút Chuột Phải
            if (mouseH.isRightClickedOnce()) {
                applyRebind(rebindingAction, KeyBindings.MOUSE_RIGHT);
                rebindingAction = null;
                return PauseAction.NONE;
            }

            // Kiểm tra click chuột trái -> Gán nút Chuột Trái hoặc Hủy
            if (mouseH.isLeftClickedOnce()) {
                Rectangle actionBox = keybindBounds.get(rebindingAction);
                if (actionBox != null && actionBox.contains(mouseX, mouseY)) {
                    applyRebind(rebindingAction, KeyBindings.MOUSE_LEFT);
                    rebindingAction = null;
                } else if (backFromSettingsBtnBounds.contains(mouseX, mouseY)) {
                    rebindingAction = null;
                    feedbackMessage = "ĐÃ HỦY GÁN PHÍM";
                    feedbackTimer = 1.5;
                } else {
                    // Nếu click vào ô của một hành động khác -> chuyển sang gán cho hành động đó
                    GameAction clickedOther = null;
                    for (Map.Entry<GameAction, Rectangle> entry : keybindBounds.entrySet()) {
                        if (entry.getValue().contains(mouseX, mouseY)) {
                            clickedOther = entry.getKey();
                            break;
                        }
                    }
                    if (clickedOther != null) {
                        rebindingAction = clickedOther;
                        feedbackMessage = "BẤM PHÍM HOẶC CHUỘT ĐỂ GÁN CHO [" + rebindingAction.getDisplayNameVi().toUpperCase() + "]";
                        feedbackTimer = 5.0;
                    } else {
                        // Click vào khoảng trống -> Gán chuột trái cho hành động hiện tại
                        applyRebind(rebindingAction, KeyBindings.MOUSE_LEFT);
                        rebindingAction = null;
                    }
                }
                return PauseAction.NONE;
            }

            return PauseAction.NONE;
        }

        // 2. Hover chuột
        isResetHovered = resetDefaultsBtnBounds.contains(mouseX, mouseY);
        isBackHovered = backFromSettingsBtnBounds.contains(mouseX, mouseY);
        isAimGuidelineHovered = aimGuidelineToggleBounds.contains(mouseX, mouseY);
        isFpsUpsHovered = fpsUpsToggleBounds.contains(mouseX, mouseY);
        hoveredAction = null;
        for (Map.Entry<GameAction, Rectangle> entry : keybindBounds.entrySet()) {
            if (entry.getValue().contains(mouseX, mouseY)) {
                hoveredAction = entry.getKey();
                break;
            }
        }

        // 3. Click chuột
        if (mouseH.isLeftClickedOnce()) {
            if (isBackHovered) {
                currentSubMenu = SubMenu.MAIN;
                selectedMainIndex = 1;
                return PauseAction.NONE;
            }

            if (isResetHovered) {
                KeyBindings.resetToDefaults();
                feedbackMessage = "ĐÃ KHÔI PHỤC TOÀN BỘ PHÍM VỀ MẶC ĐỊNH!";
                feedbackTimer = 2.0;
                return PauseAction.NONE;
            }

            if (isAimGuidelineHovered) {
                com.groupproject.game.core.GameConfig.showAimGuideline = !com.groupproject.game.core.GameConfig.showAimGuideline;
                feedbackMessage = com.groupproject.game.core.GameConfig.showAimGuideline
                        ? "ĐÃ BẬT ĐƯỜNG NGẮM CON TRỎ CHUỘT (AIM GUIDELINE: ON)"
                        : "ĐÃ TẮT ĐƯỜNG NGẮM CON TRỎ CHUỘT (AIM GUIDELINE: OFF)";
                feedbackTimer = 2.0;
                return PauseAction.NONE;
            }

            if (isFpsUpsHovered) {
                com.groupproject.game.core.GameConfig.showFpsUps = !com.groupproject.game.core.GameConfig.showFpsUps;
                feedbackMessage = com.groupproject.game.core.GameConfig.showFpsUps
                        ? "ĐÃ BẬT HIỂN THỊ CHỈ SỐ FPS & UPS (PERFORMANCE HUD: ON)"
                        : "ĐÃ TẮT HIỂN THỊ CHỈ SỐ FPS & UPS (PERFORMANCE HUD: OFF)";
                feedbackTimer = 2.0;
                return PauseAction.NONE;
            }

            if (hoveredAction != null) {
                rebindingAction = hoveredAction;
                feedbackMessage = "BẤM PHÍM BẤT KỲ HOẶC CHUỘT TRÁI/PHẢI ĐỂ GÁN (ESC ĐỂ HỦY)";
                feedbackTimer = 5.0;
                return PauseAction.NONE;
            }
        }

        return PauseAction.NONE;
    }

    private void applyRebind(GameAction action, int keyCode) {
        GameAction conflict = KeyBindings.findConflict(action, keyCode);
        KeyBindings.setKeyCode(action, keyCode);
        if (conflict != null) {
            feedbackMessage = String.format("GÁN: [%s] (TRÙNG PHÍM VỚI %s)",
                    KeyBindings.getKeyName(keyCode), conflict.getDisplayNameVi());
        } else {
            feedbackMessage = String.format("ĐÃ GÁN: [%s] CHO %s",
                    KeyBindings.getKeyName(keyCode), action.getDisplayNameVi().toUpperCase());
        }
        feedbackTimer = 2.2;
    }

    /**
     * Kết xuất toàn bộ giao diện menu tạm dừng hoặc bảng cài đặt phím.
     */
    public void render(Graphics2D g, int screenW, int screenH) {
        // 1. Phủ màn đen mờ dã chiến (Dim backdrop)
        g.setColor(new Color(5, 10, 18, 225));
        g.fillRect(0, 0, screenW, screenH);

        if (currentSubMenu == SubMenu.MAIN) {
            renderMainPauseMenu(g, screenW, screenH);
        } else {
            renderSettingsMenu(g, screenW, screenH);
        }
    }

    private void renderMainPauseMenu(Graphics2D g, int screenW, int screenH) {
        int panelW = 480;
        int panelH = 380;
        int panelX = (screenW - panelW) / 2;
        int panelY = (screenH - panelH) / 2;

        // Khung nền Sci-Fi cao cấp bo góc
        g.setColor(new Color(14, 20, 30, 245));
        g.fillRoundRect(panelX, panelY, panelW, panelH, 12, 12);
        g.setColor(new Color(0, 210, 255, 160));
        g.setStroke(new BasicStroke(1.8f));
        g.drawRoundRect(panelX, panelY, panelW, panelH, 12, 12);
        g.setStroke(new BasicStroke(1.0f));

        // Tiêu đề
        g.setFont(com.groupproject.game.core.GameFontManager.getBoldFont(24.0f));
        FontMetrics fmTitle = g.getFontMetrics();
        String title = "TẠM DỪNG TRÒ CHƠI";
        int titleX = panelX + (panelW - fmTitle.stringWidth(title)) / 2;
        int titleY = panelY + 54;

        g.setColor(new Color(0, 0, 0, 200));
        g.drawString(title, titleX + 2, titleY + 2);
        g.setColor(new Color(0, 235, 255));
        g.drawString(title, titleX, titleY);

        // Phụ đề
        g.setFont(com.groupproject.game.core.GameFontManager.getSmallFont(11.0f));
        FontMetrics fmSub = g.getFontMetrics();
        String sub = "CHIẾN TRƯỜNG ĐANG TẠM DỪNG - PAUSE MENU";
        int subX = panelX + (panelW - fmSub.stringWidth(sub)) / 2;
        int subY = titleY + 22;
        g.setColor(new Color(160, 185, 215));
        g.drawString(sub, subX, subY);

        // Đường kẻ ngăn cách neon
        g.setColor(new Color(50, 75, 110, 180));
        g.drawLine(panelX + 30, subY + 16, panelX + panelW - 30, subY + 16);

        // 3 Nút lựa chọn
        int btnW = 340;
        int btnH = 50;
        int btnX = panelX + (panelW - btnW) / 2;
        int startBtnY = subY + 40;
        int spacing = 62;

        g.setFont(com.groupproject.game.core.GameFontManager.getBoldFont(15.0f));
        FontMetrics fmBtn = g.getFontMetrics();

        for (int i = 0; i < MAIN_OPTIONS.length; i++) {
            int btnY = startBtnY + (i * spacing);
            mainOptionBounds[i].setBounds(btnX, btnY, btnW, btnH);

            boolean isSelected = (i == selectedMainIndex);

            // Nền nút
            if (isSelected) {
                g.setColor(new Color(0, 140, 200, 210));
                g.fillRoundRect(btnX, btnY, btnW, btnH, 8, 8);
                g.setColor(new Color(150, 245, 255));
                g.setStroke(new BasicStroke(2.0f));
                g.drawRoundRect(btnX, btnY, btnW, btnH, 8, 8);
                g.setStroke(new BasicStroke(1.0f));

                // Mũi tên con trỏ neon
                g.setColor(new Color(255, 255, 255));
                g.drawString(">", btnX + 16, btnY + 31);
            } else {
                g.setColor(new Color(20, 28, 42, 190));
                g.fillRoundRect(btnX, btnY, btnW, btnH, 8, 8);
                g.setColor(new Color(55, 75, 105));
                g.drawRoundRect(btnX, btnY, btnW, btnH, 8, 8);
            }

            // Chữ hiển thị
            String text = MAIN_OPTIONS[i];
            int textX = panelX + (panelW - fmBtn.stringWidth(text)) / 2;
            int textY = btnY + 31;

            if (isSelected) {
                g.setColor(new Color(0, 0, 0, 200));
                g.drawString(text, textX + 1, textY + 1);
                g.setColor(Color.WHITE);
                g.drawString(text, textX, textY);
            } else {
                g.setColor(new Color(190, 210, 235));
                g.drawString(text, textX, textY);
            }
        }

        // Lời nhắc phím ở đáy
        g.setFont(com.groupproject.game.core.GameFontManager.getSmallFont(11.0f));
        g.setColor(new Color(130, 155, 185));
        String tip = "Dùng chuột hoặc phím [W/S] [LÊN/XUỐNG] [ENTER] - [ESC] để tiếp tục";
        int tipX = panelX + (panelW - g.getFontMetrics().stringWidth(tip)) / 2;
        g.drawString(tip, tipX, panelY + panelH - 16);
    }

    private void renderSettingsMenu(Graphics2D g, int screenW, int screenH) {
        int panelW = 900;
        int panelH = 650;
        int panelX = (screenW - panelW) / 2;
        int panelY = (screenH - panelH) / 2;

        // Khung nền
        g.setColor(new Color(12, 18, 28, 245));
        g.fillRoundRect(panelX, panelY, panelW, panelH, 12, 12);
        g.setColor(new Color(0, 200, 240, 170));
        g.setStroke(new BasicStroke(1.8f));
        g.drawRoundRect(panelX, panelY, panelW, panelH, 12, 12);
        g.setStroke(new BasicStroke(1.0f));

        // Tiêu đề
        g.setFont(com.groupproject.game.core.GameFontManager.getBoldFont(18.0f));
        FontMetrics fmTitle = g.getFontMetrics();
        String title = "CÀI ĐẶT PHÍM ĐIỀU KHIỂN (KEYBIND SETTINGS)";
        int titleX = panelX + (panelW - fmTitle.stringWidth(title)) / 2;
        int titleY = panelY + 40;
        g.setColor(new Color(0, 235, 255));
        g.drawString(title, titleX, titleY);

        // Hướng dẫn / Thông điệp phản hồi
        g.setFont(com.groupproject.game.core.GameFontManager.getSmallFont(11.0f));
        String sub;
        Color subColor;
        if (!feedbackMessage.isEmpty()) {
            sub = feedbackMessage;
            subColor = feedbackMessage.contains("TRÙNG") ? new Color(255, 180, 50) : new Color(80, 250, 150);
        } else if (rebindingAction != null) {
            sub = "ĐANG CHỜ GÁN: Bấm phím bất kỳ - Chuột phải gán [CHUỘT PHẢI] - Click gán [CHUỘT TRÁI] - [ESC] Hủy";
            subColor = new Color(255, 220, 60);
        } else {
            sub = "Click chuột vào ô phím muốn đổi, sau đó bấm phím trên bàn phím hoặc nút chuột";
            subColor = new Color(165, 190, 220);
        }
        int subX = panelX + (panelW - g.getFontMetrics().stringWidth(sub)) / 2;
        g.setColor(subColor);
        g.drawString(sub, subX, titleY + 22);

        // Đường kẻ
        g.setColor(new Color(50, 75, 105, 160));
        g.drawLine(panelX + 25, titleY + 32, panelX + panelW - 25, titleY + 32);

        // Bố cục 2 cột (Dual Column Keybind List)
        GameAction[] allActions = GameAction.values();
        int halfCount = (allActions.length + 1) / 2; // 8 hàng mỗi cột cho 16 hành động
        int colW = 410;
        int col1X = panelX + 30;
        int col2X = panelX + panelW - colW - 30;
        int startRowY = titleY + 44;
        int rowH = 32;
        int rowSpacing = 38;

        for (int i = 0; i < allActions.length; i++) {
            GameAction action = allActions[i];
            boolean isLeftCol = (i < halfCount);
            int rowIdx = isLeftCol ? i : (i - halfCount);

            int rx = isLeftCol ? col1X : col2X;
            int ry = startRowY + (rowIdx * rowSpacing);

            // Lưu Bounding box
            Rectangle bounds = keybindBounds.computeIfAbsent(action, k -> new Rectangle());
            bounds.setBounds(rx, ry, colW, rowH);

            boolean isHovered = (hoveredAction == action);
            boolean isRebindingThis = (rebindingAction == action);

            // Nền hàng
            if (isRebindingThis) {
                // Nhấp nháy màu vàng cam / cyan
                float pulse = (float) (0.5 + 0.5 * Math.sin(pulseTimer * 12.0));
                g.setColor(new Color(180, 110, 20, 210));
                g.fillRoundRect(rx, ry, colW, rowH, 6, 6);
                g.setColor(new Color(255, 215, 60, (int) (180 + 75 * pulse)));
                g.setStroke(new BasicStroke(1.6f));
                g.drawRoundRect(rx, ry, colW, rowH, 6, 6);
                g.setStroke(new BasicStroke(1.0f));
            } else if (isHovered) {
                g.setColor(new Color(22, 35, 52, 220));
                g.fillRoundRect(rx, ry, colW, rowH, 6, 6);
                g.setColor(new Color(0, 190, 240));
                g.drawRoundRect(rx, ry, colW, rowH, 6, 6);
            } else {
                g.setColor(new Color(16, 22, 34, 180));
                g.fillRoundRect(rx, ry, colW, rowH, 6, 6);
                g.setColor(new Color(45, 60, 85));
                g.drawRoundRect(rx, ry, colW, rowH, 6, 6);
            }

            // Tên hành động bên trái hàng
            g.setFont(com.groupproject.game.core.GameFontManager.getSmallBoldFont(11.0f));
            g.setColor(isRebindingThis ? Color.WHITE : (isHovered ? new Color(240, 250, 255) : new Color(195, 215, 235)));
            g.drawString(action.getDisplayNameVi(), rx + 12, ry + 21);

            // Nút hiển thị phím bên phải hàng
            int keyBoxW = 140;
            int keyBoxH = 24;
            int keyBoxX = rx + colW - keyBoxW - 8;
            int keyBoxY = ry + (rowH - keyBoxH) / 2;

            if (isRebindingThis) {
                g.setColor(new Color(240, 150, 20));
                g.fillRoundRect(keyBoxX, keyBoxY, keyBoxW, keyBoxH, 4, 4);
                g.setColor(Color.WHITE);
                g.drawRoundRect(keyBoxX, keyBoxY, keyBoxW, keyBoxH, 4, 4);

                g.setFont(com.groupproject.game.core.GameFontManager.getSmallBoldFont(10.0f));
                String pressTip = "BẤM PHÍM MỚI";
                int ptw = g.getFontMetrics().stringWidth(pressTip);
                g.drawString(pressTip, keyBoxX + (keyBoxW - ptw) / 2, keyBoxY + 16);
            } else {
                g.setColor(new Color(10, 15, 24, 230));
                g.fillRoundRect(keyBoxX, keyBoxY, keyBoxW, keyBoxH, 4, 4);
                g.setColor(isHovered ? new Color(0, 210, 255) : new Color(70, 95, 130));
                g.drawRoundRect(keyBoxX, keyBoxY, keyBoxW, keyBoxH, 4, 4);

                String keyName = KeyBindings.getKeyName(KeyBindings.getKeyCode(action));
                g.setFont(com.groupproject.game.core.GameFontManager.getSmallBoldFont(11.0f));
                int kw = g.getFontMetrics().stringWidth(keyName);
                g.setColor(new Color(0, 220, 255));
                g.drawString(keyName, keyBoxX + (keyBoxW - kw) / 2, keyBoxY + 16);
            }
        }

        // Tùy chọn giao diện & Hiệu năng: 2 Hàng Toggle Box
        int botBtnW = 220;
        int botBtnH = 38;
        int botBtnY = panelY + panelH - botBtnH - 18;

        int toggleBoxW = panelW - 60;
        int toggleBoxH = 34;
        int toggleBoxX = panelX + 30;

        int toggle2Y = botBtnY - toggleBoxH - 10;
        int toggle1Y = toggle2Y - toggleBoxH - 8;

        aimGuidelineToggleBounds.setBounds(toggleBoxX, toggle1Y, toggleBoxW, toggleBoxH);
        fpsUpsToggleBounds.setBounds(toggleBoxX, toggle2Y, toggleBoxW, toggleBoxH);

        // --- HÀNG 1: ĐƯỜNG NGẮM CHUỘT ---
        Color toggle1Bg = isAimGuidelineHovered ? new Color(22, 34, 50, 230) : new Color(15, 22, 34, 190);
        g.setColor(toggle1Bg);
        g.fillRoundRect(toggleBoxX, toggle1Y, toggleBoxW, toggleBoxH, 6, 6);
        g.setColor(isAimGuidelineHovered ? new Color(0, 210, 255) : new Color(45, 65, 95));
        g.drawRoundRect(toggleBoxX, toggle1Y, toggleBoxW, toggleBoxH, 6, 6);

        g.setFont(com.groupproject.game.core.GameFontManager.getSmallBoldFont(11.0f));
        g.setColor(new Color(230, 245, 255));
        g.drawString("[NGẮM] ĐƯỜNG NGẮM CHUỘT (AIM GUIDELINE)", toggleBoxX + 14, toggle1Y + 22);

        g.setFont(com.groupproject.game.core.GameFontManager.getSmallFont(10.0f));
        g.setColor(new Color(145, 175, 205));
        g.drawString("Đường kẻ đứt mờ nối từ người chơi đến con trỏ chuột hỗ trợ ngắm bắn", toggleBoxX + 310, toggle1Y + 22);

        int statusBtnW = 125;
        int statusBtnH = 24;
        int statusBtnX = toggleBoxX + toggleBoxW - statusBtnW - 8;
        int statusBtn1Y = toggle1Y + (toggleBoxH - statusBtnH) / 2;

        boolean isAimOn = com.groupproject.game.core.GameConfig.showAimGuideline;
        if (isAimOn) {
            g.setColor(new Color(25, 110, 70));
            g.fillRoundRect(statusBtnX, statusBtn1Y, statusBtnW, statusBtnH, 4, 4);
            g.setColor(new Color(60, 240, 140));
            g.drawRoundRect(statusBtnX, statusBtn1Y, statusBtnW, statusBtnH, 4, 4);
            g.setFont(com.groupproject.game.core.GameFontManager.getSmallBoldFont(10.0f));
            String onText = "[ ĐANG BẬT ]";
            int otw = g.getFontMetrics().stringWidth(onText);
            g.setColor(Color.WHITE);
            g.drawString(onText, statusBtnX + (statusBtnW - otw) / 2, statusBtn1Y + 16);
        } else {
            g.setColor(new Color(45, 50, 60));
            g.fillRoundRect(statusBtnX, statusBtn1Y, statusBtnW, statusBtnH, 4, 4);
            g.setColor(new Color(110, 115, 130));
            g.drawRoundRect(statusBtnX, statusBtn1Y, statusBtnW, statusBtnH, 4, 4);
            g.setFont(com.groupproject.game.core.GameFontManager.getSmallBoldFont(10.0f));
            String offText = "[ ĐANG TẮT ]";
            int oftw = g.getFontMetrics().stringWidth(offText);
            g.setColor(new Color(180, 185, 195));
            g.drawString(offText, statusBtnX + (statusBtnW - oftw) / 2, statusBtn1Y + 16);
        }

        // --- HÀNG 2: CHỈ SỐ HIỆU NĂNG FPS & UPS ---
        Color toggle2Bg = isFpsUpsHovered ? new Color(22, 34, 50, 230) : new Color(15, 22, 34, 190);
        g.setColor(toggle2Bg);
        g.fillRoundRect(toggleBoxX, toggle2Y, toggleBoxW, toggleBoxH, 6, 6);
        g.setColor(isFpsUpsHovered ? new Color(0, 210, 255) : new Color(45, 65, 95));
        g.drawRoundRect(toggleBoxX, toggle2Y, toggleBoxW, toggleBoxH, 6, 6);

        g.setFont(com.groupproject.game.core.GameFontManager.getSmallBoldFont(11.0f));
        g.setColor(new Color(230, 245, 255));
        g.drawString("[FPS] CHỈ SỐ FPS & UPS (PERFORMANCE HUD)", toggleBoxX + 14, toggle2Y + 22);

        g.setFont(com.groupproject.game.core.GameFontManager.getSmallFont(10.0f));
        g.setColor(new Color(145, 175, 205));
        g.drawString("Hiển thị số khung hình (FPS) và nhịp cập nhật logic (UPS) thời gian thực", toggleBoxX + 310, toggle2Y + 22);

        int statusBtn2Y = toggle2Y + (toggleBoxH - statusBtnH) / 2;
        boolean isFpsOn = com.groupproject.game.core.GameConfig.showFpsUps;
        if (isFpsOn) {
            g.setColor(new Color(25, 110, 70));
            g.fillRoundRect(statusBtnX, statusBtn2Y, statusBtnW, statusBtnH, 4, 4);
            g.setColor(new Color(60, 240, 140));
            g.drawRoundRect(statusBtnX, statusBtn2Y, statusBtnW, statusBtnH, 4, 4);
            g.setFont(com.groupproject.game.core.GameFontManager.getSmallBoldFont(10.0f));
            String onText = "[ ĐANG BẬT ]";
            int otw = g.getFontMetrics().stringWidth(onText);
            g.setColor(Color.WHITE);
            g.drawString(onText, statusBtnX + (statusBtnW - otw) / 2, statusBtn2Y + 16);
        } else {
            g.setColor(new Color(45, 50, 60));
            g.fillRoundRect(statusBtnX, statusBtn2Y, statusBtnW, statusBtnH, 4, 4);
            g.setColor(new Color(110, 115, 130));
            g.drawRoundRect(statusBtnX, statusBtn2Y, statusBtnW, statusBtnH, 4, 4);
            g.setFont(com.groupproject.game.core.GameFontManager.getSmallBoldFont(10.0f));
            String offText = "[ ĐANG TẮT ]";
            int oftw = g.getFontMetrics().stringWidth(offText);
            g.setColor(new Color(180, 185, 195));
            g.drawString(offText, statusBtnX + (statusBtnW - oftw) / 2, statusBtn2Y + 16);
        }

        // 2 Nút ở đáy: [ ĐẶT LẠI MẶC ĐỊNH ] và [ QUAY LẠI ]
        int resetBtnX = panelX + (panelW / 2) - botBtnW - 15;
        int backBtnX = panelX + (panelW / 2) + 15;

        resetDefaultsBtnBounds.setBounds(resetBtnX, botBtnY, botBtnW, botBtnH);
        backFromSettingsBtnBounds.setBounds(backBtnX, botBtnY, botBtnW, botBtnH);

        // Nút Đặt lại mặc định
        if (isResetHovered) {
            g.setColor(new Color(150, 80, 20, 210));
            g.fillRoundRect(resetBtnX, botBtnY, botBtnW, botBtnH, 8, 8);
            g.setColor(new Color(255, 180, 50));
            g.setStroke(new BasicStroke(1.6f));
            g.drawRoundRect(resetBtnX, botBtnY, botBtnW, botBtnH, 8, 8);
            g.setStroke(new BasicStroke(1.0f));
        } else {
            g.setColor(new Color(30, 24, 20, 190));
            g.fillRoundRect(resetBtnX, botBtnY, botBtnW, botBtnH, 8, 8);
            g.setColor(new Color(110, 75, 45));
            g.drawRoundRect(resetBtnX, botBtnY, botBtnW, botBtnH, 8, 8);
        }
        g.setFont(com.groupproject.game.core.GameFontManager.getBoldFont(12.0f));
        String resetText = "[R] ĐẶT LẠI MẶC ĐỊNH";
        int rtw = g.getFontMetrics().stringWidth(resetText);
        g.setColor(isResetHovered ? Color.WHITE : new Color(230, 180, 120));
        g.drawString(resetText, resetBtnX + (botBtnW - rtw) / 2, botBtnY + 24);

        // Nút Quay lại
        if (isBackHovered) {
            g.setColor(new Color(0, 130, 190, 210));
            g.fillRoundRect(backBtnX, botBtnY, botBtnW, botBtnH, 8, 8);
            g.setColor(new Color(120, 235, 255));
            g.setStroke(new BasicStroke(1.6f));
            g.drawRoundRect(backBtnX, botBtnY, botBtnW, botBtnH, 8, 8);
            g.setStroke(new BasicStroke(1.0f));
        } else {
            g.setColor(new Color(18, 28, 42, 190));
            g.fillRoundRect(backBtnX, botBtnY, botBtnW, botBtnH, 8, 8);
            g.setColor(new Color(55, 80, 115));
            g.drawRoundRect(backBtnX, botBtnY, botBtnW, botBtnH, 8, 8);
        }
        g.setFont(com.groupproject.game.core.GameFontManager.getBoldFont(12.0f));
        String backText = "[OK] HOÀN TẤT (QUAY LẠI)";
        int btw = g.getFontMetrics().stringWidth(backText);
        g.setColor(isBackHovered ? Color.WHITE : new Color(180, 215, 245));
        g.drawString(backText, backBtnX + (botBtnW - btw) / 2, botBtnY + 24);
    }

    public SubMenu getCurrentSubMenu() {
        return currentSubMenu;
    }

    public void setCurrentSubMenu(SubMenu subMenu) {
        this.currentSubMenu = subMenu;
    }

    public int getSelectedMainIndex() {
        return selectedMainIndex;
    }

    public void setSelectedMainIndex(int index) {
        this.selectedMainIndex = Math.max(0, Math.min(MAIN_OPTIONS.length - 1, index));
    }

    public GameAction getRebindingAction() {
        return rebindingAction;
    }

    public void setRebindingAction(GameAction action) {
        this.rebindingAction = action;
    }

    public Rectangle getAimGuidelineToggleBounds() {
        return aimGuidelineToggleBounds;
    }

    public Rectangle getFpsUpsToggleBounds() {
        return fpsUpsToggleBounds;
    }
}
