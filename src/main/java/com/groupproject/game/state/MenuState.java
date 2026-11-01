package com.groupproject.game.state;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.core.GameFontManager;
import com.groupproject.game.core.GameSoundManager;
import com.groupproject.game.core.GameSoundManager.SoundEffect;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import com.groupproject.game.storage.RunProgressData;
import com.groupproject.game.storage.RunSaveManager;
import com.groupproject.game.ui.PauseMenuUI;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

/**
 * Main Menu State providing an interactive entry screen with dual Keyboard and
 * Mouse navigation, dynamic Save Checkpoint detection, and New Game
 * confirmation dialog.
 *
 * <p>
 * WHY: Allows players to resume a previously saved campaign run, start fresh,
 * challenge the standalone Titan Tank boss, adjust keybindings, or exit
 * cleanly.
 */
public class MenuState extends GameState {

    private boolean hasSave = false;
    private int savedStage = 1;
    private String[] options = {
            "BẮT ĐẦU CHƠI",
            "BOSS FIGHT: TITAN TANK",
            "CÀI ĐẶT PHÍM (SETTINGS)",
            "HOW TO PLAY",
            "EXIT"
    };
    private Rectangle[] optionBounds = new Rectangle[5];
    private int selectedOption = 0;

    private boolean showHowToPlay = false;
    private boolean showSettings = false;
    private final PauseMenuUI settingsUI = new PauseMenuUI();

    // --- Hộp thoại xác nhận bắt đầu ván mới khi đang có bản lưu dở ---
    private boolean showConfirmDialog = false;
    private int confirmSelection = 1; // 0 = Xác nhận (Xóa & Chơi mới), 1 = Hủy bỏ (Mặc định 1 để an toàn)
    private final Rectangle confirmBtnBounds = new Rectangle();
    private final Rectangle cancelBtnBounds = new Rectangle();

    // Typography styling
    private final Font titleFont = GameFontManager.getBoldFont(54.0f);
    private final Font subtitleFont = GameFontManager.getFont(Font.ITALIC, 22.0f);
    private final Font menuFont = GameFontManager.getBoldFont(26.0f);
    private final Font infoFont = GameFontManager.getFont(18.0f);
    private final Font dialogTitleFont = GameFontManager.getBoldFont(22.0f);
    private final Font dialogBodyFont = GameFontManager.getBoldFont(17.0f);
    private final Font dialogSubFont = GameFontManager.getFont(Font.ITALIC, 14.0f);
    private final Font btnFont = GameFontManager.getBoldFont(16.0f);

    public MenuState(GameStateManager gsm) {
        super(gsm);
        for (int i = 0; i < optionBounds.length; i++) {
            optionBounds[i] = new Rectangle();
        }
    }

    /**
     * Tự động quét và cập nhật danh sách tùy chọn Menu dựa trên sự hiện diện của
     * bản lưu checkpoint.
     */
    public void refreshMenuOptions() {
        this.hasSave = RunSaveManager.hasValidSave();
        if (hasSave) {
            this.savedStage = RunSaveManager.getSavedStage();
            this.options = new String[] {
                    "★ TIẾP TỤC CHƠI (STAGE " + savedStage + ")",
                    "BẮT ĐẦU CHƠI MỚI",
                    "BOSS FIGHT: TITAN TANK",
                    "CÀI ĐẶT PHÍM (SETTINGS)",
                    "HOW TO PLAY",
                    "EXIT"
            };
        } else {
            this.options = new String[] {
                    "BẮT ĐẦU CHƠI",
                    "BOSS FIGHT: TITAN TANK",
                    "CÀI ĐẶT PHÍM (SETTINGS)",
                    "HOW TO PLAY",
                    "EXIT"
            };
        }

        this.optionBounds = new Rectangle[options.length];
        for (int i = 0; i < options.length; i++) {
            optionBounds[i] = new Rectangle();
        }

        if (selectedOption >= options.length) {
            selectedOption = 0;
        }
    }

    @Override
    public void init() {
        refreshMenuOptions();
        selectedOption = 0;
        showHowToPlay = false;
        showSettings = false;
        showConfirmDialog = false;
        confirmSelection = 1;
        GameSoundManager.playBgm(GameSoundManager.BgmTrack.MENU);
    }

    @Override
    public void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH) {
        // 1. Xử lý Hộp thoại xác nhận Bắt đầu ván mới (New Game Confirmation Dialog)
        if (showConfirmDialog) {
            updateConfirmDialog(keyH, mouseH);
            return;
        }

        // 2. Xử lý màn hình Cài đặt phím (Keybind Settings)
        if (showSettings) {
            if (keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
                settingsUI.handleEsc();
                if (settingsUI.getCurrentSubMenu() == PauseMenuUI.SubMenu.MAIN) {
                    showSettings = false;
                }
                return;
            }

            settingsUI.update(keyH, mouseH, deltaTime);
            if (settingsUI.getCurrentSubMenu() == PauseMenuUI.SubMenu.MAIN) {
                showSettings = false;
            }
            return;
        }

        // 3. Xử lý màn hình Hướng dẫn (How To Play)
        if (showHowToPlay) {
            if (keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE) || keyH.isKeyPressedOnce(KeyEvent.VK_ENTER)
                    || mouseH.isLeftClickedOnce()) {
                GameSoundManager.play(SoundEffect.UI_INTERACT);
                showHowToPlay = false;
            }
            return;
        }

        // --- Keyboard Navigation (WASD / Arrow Keys) ---
        if (keyH.isKeyPressedOnce(KeyEvent.VK_UP) || keyH.isKeyPressedOnce(KeyEvent.VK_W)) {
            GameSoundManager.play(SoundEffect.UI_INTERACT);
            selectedOption--;
            if (selectedOption < 0) {
                selectedOption = options.length - 1; // Wrap around to bottom
            }
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_DOWN) || keyH.isKeyPressedOnce(KeyEvent.VK_S)) {
            GameSoundManager.play(SoundEffect.UI_INTERACT);
            selectedOption++;
            if (selectedOption >= options.length) {
                selectedOption = 0; // Wrap around to top
            }
        }

        // --- Mouse Hover & Click Detection ---
        int mouseX = mouseH.getScreenX();
        int mouseY = mouseH.getScreenY();
        for (int i = 0; i < optionBounds.length; i++) {
            if (optionBounds[i] != null && optionBounds[i].contains(mouseX, mouseY)) {
                selectedOption = i;
                if (mouseH.isLeftClickedOnce()) {
                    GameSoundManager.play(SoundEffect.UI_INTERACT);
                    selectCurrentOption();
                    return;
                }
            }
        }

        // --- Confirm Selection via Keyboard ---
        if (keyH.isKeyPressedOnce(KeyEvent.VK_ENTER) || keyH.isKeyPressedOnce(KeyEvent.VK_SPACE)) {
            GameSoundManager.play(SoundEffect.UI_INTERACT);
            selectCurrentOption();
        }
    }

    /**
     * Xử lý điều hướng và xác nhận trên Hộp thoại cảnh báo ghi đè bản lưu.
     */
    private void updateConfirmDialog(KeyboardHandler keyH, MouseHandler mouseH) {
        // Phím điều hướng Trái / Phải hoặc A / D
        if (keyH.isKeyPressedOnce(KeyEvent.VK_LEFT) || keyH.isKeyPressedOnce(KeyEvent.VK_A)
                || keyH.isKeyPressedOnce(KeyEvent.VK_RIGHT) || keyH.isKeyPressedOnce(KeyEvent.VK_D)) {
            GameSoundManager.play(SoundEffect.UI_INTERACT);
            confirmSelection = 1 - confirmSelection;
        }

        // Phím ESC: Hủy bỏ ngay lập tức
        if (keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
            GameSoundManager.play(SoundEffect.UI_INTERACT);
            showConfirmDialog = false;
            return;
        }

        // Phím Enter / Space: Thực thi lựa chọn
        if (keyH.isKeyPressedOnce(KeyEvent.VK_ENTER) || keyH.isKeyPressedOnce(KeyEvent.VK_SPACE)) {
            GameSoundManager.play(SoundEffect.UI_INTERACT);
            executeConfirmAction();
            return;
        }

        // Tương tác chuột Hover & Click
        int mouseX = mouseH.getScreenX();
        int mouseY = mouseH.getScreenY();
        if (confirmBtnBounds.contains(mouseX, mouseY)) {
            confirmSelection = 0;
            if (mouseH.isLeftClickedOnce()) {
                GameSoundManager.play(SoundEffect.UI_INTERACT);
                executeConfirmAction();
                return;
            }
        } else if (cancelBtnBounds.contains(mouseX, mouseY)) {
            confirmSelection = 1;
            if (mouseH.isLeftClickedOnce()) {
                GameSoundManager.play(SoundEffect.UI_INTERACT);
                executeConfirmAction();
                return;
            }
        }
    }

    private void executeConfirmAction() {
        if (confirmSelection == 0) {
            // Người chơi xác nhận xóa bản lưu dở và bắt đầu ván mới
            RunSaveManager.deleteSave();
            hasSave = false;
            showConfirmDialog = false;
            refreshMenuOptions();
            gsm.transitionTo(GameStateManager.StateType.THE_RANGE, "CĂN CỨ TIỀN PHƯƠNG", "THE RANGE");
        } else {
            // Hủy bỏ và giữ nguyên bản lưu
            showConfirmDialog = false;
        }
    }

    /**
     * Thực thi hành động tương ứng với tùy chọn menu đang được chọn.
     */
    private void selectCurrentOption() {
        if (hasSave) {
            // Khi có bản lưu tiến trình dở dang
            switch (selectedOption) {
                case 0 -> resumeSavedGame();
                case 1 -> {
                    // Hỏi xác nhận trước khi xóa save
                    showConfirmDialog = true;
                    confirmSelection = 1; // Mặc định trỏ vào Hủy bỏ để bảo vệ an toàn dữ liệu
                }
                case 2 -> startStandaloneBossFight();
                case 3 -> openSettingsMenu();
                case 4 -> showHowToPlay = true;
                case 5 -> System.exit(0);
            }
        } else {
            // Khi không có bản lưu
            switch (selectedOption) {
                case 0 -> gsm.transitionTo(GameStateManager.StateType.THE_RANGE, "CĂN CỨ TIỀN PHƯƠNG", "THE RANGE");
                case 1 -> startStandaloneBossFight();
                case 2 -> openSettingsMenu();
                case 3 -> showHowToPlay = true;
                case 4 -> System.exit(0);
            }
        }
    }

    /**
     * Tải lại ván đấu từ file save checkpoint.
     */
    private void resumeSavedGame() {
        RunProgressData data = RunSaveManager.loadRun();
        if (data != null) {
            PlayingState.setSpawnDirectlyInBossRoom(false);
            PlayingState.setPendingResumeData(data);
            gsm.transitionTo(GameStateManager.StateType.PLAYING, "TIẾP TỤC CHIẾN DỊCH", "STAGE " + savedStage);
            return;
        }
        // Nếu file save bị lỗi hoặc không nạp được -> làm mới menu
        refreshMenuOptions();
    }

    private void startStandaloneBossFight() {
        PlayingState.setSpawnDirectlyInBossRoom(true);
        gsm.transitionTo(GameStateManager.StateType.PLAYING, "HIGH THREAT ARENA", "PHÁT HIỆN KẺ ĐỊCH NGUY HIỂM");
    }

    private void openSettingsMenu() {
        showSettings = true;
        settingsUI.setCurrentSubMenu(PauseMenuUI.SubMenu.SETTINGS);
    }

    @Override
    public void render(Graphics2D g) {
        // --- 1. Background Grid Styling ---
        g.setColor(new Color(15, 18, 28));
        g.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        // Draw ambient sci-fi grid lines
        g.setColor(new Color(28, 35, 54));
        for (int x = 0; x < GameConfig.SCREEN_WIDTH; x += 40) {
            g.drawLine(x, 0, x, GameConfig.SCREEN_HEIGHT);
        }
        for (int y = 0; y < GameConfig.SCREEN_HEIGHT; y += 40) {
            g.drawLine(0, y, GameConfig.SCREEN_WIDTH, y);
        }

        // If settings screen is active, render it and skip main menu
        if (showSettings) {
            settingsUI.render(g, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
            return;
        }

        // If briefing popup is visible, render it on top and skip main options
        if (showHowToPlay) {
            renderHowToPlay(g);
            return;
        }

        // --- 2. Title & Glowing Subtitle ---
        g.setFont(titleFont);
        FontMetrics fmTitle = g.getFontMetrics();
        String title = "Alone in the Zone";
        int titleX = (GameConfig.SCREEN_WIDTH - fmTitle.stringWidth(title)) / 2;
        int titleY = 160;

        // Shadow / Glow effect
        g.setColor(new Color(255, 75, 43, 80));
        g.drawString(title, titleX + 3, titleY + 3);
        // Primary title text
        g.setColor(new Color(255, 120, 50));
        g.drawString(title, titleX, titleY);

        // Subtitle
        g.setFont(subtitleFont);
        FontMetrics fmSub = g.getFontMetrics();
        String subtitle = "Top-Down Roguelite Shooter";
        g.setColor(new Color(160, 180, 210));
        g.drawString(subtitle, (GameConfig.SCREEN_WIDTH - fmSub.stringWidth(subtitle)) / 2, titleY + 36);

        // --- 3. Interactive Menu Buttons ---
        g.setFont(menuFont);
        FontMetrics fmMenu = g.getFontMetrics();
        int startY = hasSave ? 260 : 285;
        int spacing = hasSave ? 48 : 52;

        for (int i = 0; i < options.length; i++) {
            String opt = options[i];
            int textWidth = fmMenu.stringWidth(opt);
            int optX = (GameConfig.SCREEN_WIDTH - textWidth) / 2;
            int optY = startY + i * spacing;

            int boxPadX = 35;
            int boxPadY = 10;
            Rectangle bounds = new Rectangle(optX - boxPadX, optY - fmMenu.getAscent() - boxPadY / 2,
                    textWidth + boxPadX * 2, fmMenu.getHeight() + boxPadY);
            if (i < optionBounds.length) {
                optionBounds[i] = bounds;
            }

            boolean isResumeOption = (hasSave && i == 0);

            if (i == selectedOption) {
                // Highlighted button styling
                if (isResumeOption) {
                    g.setColor(new Color(255, 205, 50, 55));
                    g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
                    g.setColor(new Color(255, 215, 60));
                    g.setStroke(new BasicStroke(2.0f));
                    g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
                    g.setStroke(new BasicStroke(1.0f));

                    g.setColor(new Color(255, 245, 120));
                    g.drawString(">  " + opt + "  <",
                            (GameConfig.SCREEN_WIDTH - fmMenu.stringWidth(">  " + opt + "  <")) / 2, optY);
                } else {
                    g.setColor(new Color(255, 100, 30, 40));
                    g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
                    g.setColor(new Color(255, 120, 50));
                    g.setStroke(new BasicStroke(1.5f));
                    g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
                    g.setStroke(new BasicStroke(1.0f));

                    g.setColor(new Color(255, 230, 80));
                    g.drawString(">  " + opt + "  <",
                            (GameConfig.SCREEN_WIDTH - fmMenu.stringWidth(">  " + opt + "  <")) / 2, optY);
                }
            } else {
                // Normal unselected button
                if (isResumeOption) {
                    g.setColor(new Color(255, 215, 75)); // Vàng kim nổi bật cho nút Tiếp tục chơi
                    g.drawString(opt, optX, optY);
                } else {
                    g.setColor(new Color(180, 195, 220));
                    g.drawString(opt, optX, optY);
                }
            }
        }

        // --- 4. Footer Help Tip ---
        g.setFont(infoFont);
        g.setColor(new Color(100, 115, 140));
        String footer = "Use W/S or Arrow Keys to navigate, Enter to Select | F11: Full Screen | Mouse Click supported";
        FontMetrics fmFoot = g.getFontMetrics();
        g.drawString(footer, (GameConfig.SCREEN_WIDTH - fmFoot.stringWidth(footer)) / 2, GameConfig.SCREEN_HEIGHT - 30);

        // --- 5. Render Hộp Thoại Xác Nhận Bắt Đầu Chơi Mới (Modal Dialog) ---
        if (showConfirmDialog) {
            renderConfirmDialog(g);
        }
    }

    /**
     * Hiển thị hộp thoại Modal xác nhận ghi đè bản lưu theo phản hồi người dùng.
     */
    private void renderConfirmDialog(Graphics2D g) {
        // Lớp mờ nền đen bao trùm
        g.setColor(new Color(8, 12, 18, 225));
        g.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        int boxW = 660;
        int boxH = 260;
        int boxX = (GameConfig.SCREEN_WIDTH - boxW) / 2;
        int boxY = (GameConfig.SCREEN_HEIGHT - boxH) / 2;

        // Khung Modal công nghệ cao
        g.setColor(new Color(22, 28, 42));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 16, 16);
        g.setColor(new Color(255, 120, 50));
        g.setStroke(new BasicStroke(2.0f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 16, 16);
        g.setStroke(new BasicStroke(1.0f));

        // Tiêu đề cảnh báo
        g.setFont(dialogTitleFont);
        FontMetrics fmDT = g.getFontMetrics();
        String title = "⚠ TIẾN TRÌNH LƯỢT CHƠI ĐANG TỒN TẠI ⚠";
        g.setColor(new Color(255, 205, 60));
        g.drawString(title, (GameConfig.SCREEN_WIDTH - fmDT.stringWidth(title)) / 2, boxY + 45);

        // Nội dung thông báo đúng chuẩn yêu cầu người dùng
        g.setFont(dialogBodyFont);
        FontMetrics fmDB = g.getFontMetrics();
        String msg1 = "Bạn có chắc không? Tiến trình lượt chơi trước đó sẽ bị xóa!";
        g.setColor(Color.WHITE);
        g.drawString(msg1, (GameConfig.SCREEN_WIDTH - fmDB.stringWidth(msg1)) / 2, boxY + 95);

        g.setFont(dialogSubFont);
        FontMetrics fmDS = g.getFontMetrics();
        String msg2 = "Bản lưu tại Stage " + savedStage + " cùng trang bị và Buff hiện tại sẽ bị xóa vĩnh viễn.";
        g.setColor(new Color(185, 200, 225));
        g.drawString(msg2, (GameConfig.SCREEN_WIDTH - fmDS.stringWidth(msg2)) / 2, boxY + 128);

        // --- 2 Nút bấm tương tác ---
        int btnW = 270;
        int btnH = 46;
        int btnY = boxY + 175;
        int btnGap = 26;
        int startBtnX = (GameConfig.SCREEN_WIDTH - (btnW * 2 + btnGap)) / 2;

        confirmBtnBounds.setBounds(startBtnX, btnY, btnW, btnH);
        cancelBtnBounds.setBounds(startBtnX + btnW + btnGap, btnY, btnW, btnH);

        g.setFont(btnFont);
        FontMetrics fmBtn = g.getFontMetrics();

        // 1. Nút Xác nhận (Confirm)
        boolean isConfirm = (confirmSelection == 0);
        g.setColor(isConfirm ? new Color(220, 45, 45) : new Color(45, 30, 36));
        g.fillRoundRect(confirmBtnBounds.x, confirmBtnBounds.y, btnW, btnH, 8, 8);
        g.setColor(isConfirm ? new Color(255, 120, 120) : new Color(130, 60, 65));
        g.setStroke(new BasicStroke(isConfirm ? 2.0f : 1.0f));
        g.drawRoundRect(confirmBtnBounds.x, confirmBtnBounds.y, btnW, btnH, 8, 8);

        String confirmText = isConfirm ? "> XÓA & CHƠI MỚI <" : "XÓA & CHƠI MỚI";
        g.setColor(isConfirm ? Color.WHITE : new Color(210, 190, 195));
        g.drawString(confirmText, confirmBtnBounds.x + (btnW - fmBtn.stringWidth(confirmText)) / 2, btnY + 28);

        // 2. Nút Hủy bỏ (Cancel - Khuyến nghị)
        boolean isCancel = (confirmSelection == 1);
        g.setColor(isCancel ? new Color(40, 180, 120) : new Color(26, 45, 42));
        g.fillRoundRect(cancelBtnBounds.x, cancelBtnBounds.y, btnW, btnH, 8, 8);
        g.setColor(isCancel ? new Color(100, 255, 180) : new Color(50, 115, 95));
        g.setStroke(new BasicStroke(isCancel ? 2.0f : 1.0f));
        g.drawRoundRect(cancelBtnBounds.x, cancelBtnBounds.y, btnW, btnH, 8, 8);

        String cancelText = isCancel ? "> HỦY BỎ (QUAY LẠI) <" : "HỦY BỎ (QUAY LẠI)";
        g.setColor(isCancel ? Color.WHITE : new Color(180, 220, 205));
        g.drawString(cancelText, cancelBtnBounds.x + (btnW - fmBtn.stringWidth(cancelText)) / 2, btnY + 28);

        g.setStroke(new BasicStroke(1.0f));
    }

    /**
     * Renders a translucent briefing popup explaining controls and roguelite
     * progression.
     */
    private void renderHowToPlay(Graphics2D g) {
        int boxW = 800;
        int boxH = 460;
        int boxX = (GameConfig.SCREEN_WIDTH - boxW) / 2;
        int boxY = (GameConfig.SCREEN_HEIGHT - boxH) / 2;

        // Semi-transparent dark backdrop
        g.setColor(new Color(22, 27, 40, 240));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 20, 20);
        g.setColor(new Color(255, 120, 50));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 20, 20);

        g.setFont(menuFont);
        g.setColor(new Color(255, 200, 50));
        g.drawString("MISSION BRIEFING & CONTROLS", boxX + 40, boxY + 50);

        g.setFont(infoFont);
        g.setColor(Color.WHITE);
        int lineY = boxY + 100;
        int gap = 30;

        g.drawString("• [W, A, S, D]: Move Operative (Up, Left, Down, Right)", boxX + 50, lineY);
        g.drawString("• [MOUSE AIM 360°]: Aim crosshair freely around operative", boxX + 50, lineY += gap);
        g.drawString("• [LEFT CLICK]: Fire Equipped Weapon | [4]: Combat Knife", boxX + 50, lineY += gap);
        g.drawString("• [R]: Reload Weapon magazine", boxX + 50, lineY += gap);
        g.drawString("• [1, 2, Q]: Swap between primary and secondary firearms", boxX + 50, lineY += gap);
        g.drawString("• [SPACEBAR]: Tactical Dash / Roll (brief invulnerability)", boxX + 50, lineY += gap);
        g.drawString("• [F11 / ALT+ENTER]: Toggle Full Screen (Bật / Tắt Toàn Màn Hình)", boxX + 50, lineY += gap);
        g.drawString("• [ESC]: Pause Mission / Options | [TAB]: Open Tactical Backpack", boxX + 50, lineY += gap);
        g.drawString("• ROGUELITE UPGRADES: Defeat enemies, gather EXP, pick 1 of 3 Perks!", boxX + 50, lineY += gap);

        g.setColor(new Color(255, 120, 50));
        g.drawString("Click anywhere or press [ENTER] / [ESC] to return to Main Menu", boxX + 50, boxY + boxH - 24);
    }

    public boolean isShowSettings() {
        return showSettings;
    }

    public void setShowSettings(boolean showSettings) {
        this.showSettings = showSettings;
        if (showSettings) {
            settingsUI.setCurrentSubMenu(PauseMenuUI.SubMenu.SETTINGS);
        }
    }

    public PauseMenuUI getSettingsUI() {
        return settingsUI;
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public boolean isShowConfirmDialog() {
        return showConfirmDialog;
    }

    public void setShowConfirmDialog(boolean show) {
        this.showConfirmDialog = show;
    }

    public int getConfirmSelection() {
        return confirmSelection;
    }

    public void setConfirmSelection(int selection) {
        this.confirmSelection = selection;
    }

    public boolean hasSave() {
        return hasSave;
    }
}
