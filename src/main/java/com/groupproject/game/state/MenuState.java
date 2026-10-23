package com.groupproject.game.state;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import com.groupproject.game.ui.PauseMenuUI;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

/**
 * Main Menu State providing an interactive entry screen with dual Keyboard and
 * Mouse navigation.
 *
 * <p>
 * WHY: Allows players to start the game, review control bindings, or exit
 * gracefully.
 * Implementing both keyboard arrow navigation and mouse hover/click ensures
 * accessibility
 * and creates a responsive, polished user interface experience.
 */
public class MenuState extends GameState {

    /**
     * Primary menu choices available to the user.
     */
    private final String[] options = {
            "BẮT ĐẦU CHƠI",
            "BOSS FIGHT: TITAN TANK",
            "CÀI ĐẶT PHÍM (SETTINGS)",
            "HOW TO PLAY",
            "EXIT"
    };
    private int selectedOption = 0;
    private boolean showHowToPlay = false;
    private boolean showSettings = false;
    private final PauseMenuUI settingsUI = new PauseMenuUI();

    /**
     * Cached bounding boxes for mouse hover/click collision detection.
     */
    private final Rectangle[] optionBounds = new Rectangle[5];

    // Typography styling
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 54);
    private final Font subtitleFont = new Font("SansSerif", Font.ITALIC, 22);
    private final Font menuFont = new Font("SansSerif", Font.BOLD, 26);
    private final Font infoFont = new Font("SansSerif", Font.PLAIN, 18);

    public MenuState(GameStateManager gsm) {
        super(gsm);
    }

    @Override
    public void init() {
        selectedOption = 0;
        showHowToPlay = false;
        showSettings = false;
    }

    @Override
    public void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH) {
        // Xử lý màn hình Cài đặt phím (Keybind Settings)
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

        // Handle Dismissal of the 'How To Play' briefing overlay
        if (showHowToPlay) {
            if (keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE) || keyH.isKeyPressedOnce(KeyEvent.VK_ENTER)
                    || mouseH.isLeftClickedOnce()) {
                showHowToPlay = false;
            }
            return;
        }

        // --- Keyboard Navigation (WASD / Arrow Keys) ---
        if (keyH.isKeyPressedOnce(KeyEvent.VK_UP) || keyH.isKeyPressedOnce(KeyEvent.VK_W)) {
            selectedOption--;
            if (selectedOption < 0) {
                selectedOption = options.length - 1; // Wrap around to bottom
            }
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_DOWN) || keyH.isKeyPressedOnce(KeyEvent.VK_S)) {
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
                    selectCurrentOption();
                    return;
                }
            }
        }

        // --- Confirm Selection via Keyboard ---
        if (keyH.isKeyPressedOnce(KeyEvent.VK_ENTER) || keyH.isKeyPressedOnce(KeyEvent.VK_SPACE)) {
            selectCurrentOption();
        }
    }

    /**
     * Executes the action corresponding to the currently highlighted menu index.
     */
    private void selectCurrentOption() {
        switch (selectedOption) {
            case 0 -> {
                gsm.transitionTo(GameStateManager.StateType.THE_RANGE, "CĂN CỨ TIỀN PHƯƠNG",
                        "THE RANGE");
            }
            case 1 -> {
                PlayingState.setSpawnDirectlyInBossRoom(true);
                gsm.transitionTo(GameStateManager.StateType.PLAYING, "HIGH THREAT ARENA",
                        "PHÁT HIỆN KẺ ĐỊCH NGUY HIỂM");
            }
            case 2 -> {
                showSettings = true;
                settingsUI.setCurrentSubMenu(PauseMenuUI.SubMenu.SETTINGS);
            }
            case 3 -> showHowToPlay = true;
            case 4 -> System.exit(0);
        }
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
        String title = "DontKnow";
        int titleX = (GameConfig.SCREEN_WIDTH - fmTitle.stringWidth(title)) / 2;
        int titleY = 175;

        // Shadow / Glow effect
        g.setColor(new Color(255, 75, 43, 80));
        g.drawString(title, titleX + 3, titleY + 3);
        // Primary title text
        g.setColor(new Color(255, 120, 50));
        g.drawString(title, titleX, titleY);

        // Subtitle
        g.setFont(subtitleFont);
        FontMetrics fmSub = g.getFontMetrics();
        String subtitle = "An unplanned top-down roguelite shooter";
        g.setColor(new Color(160, 180, 210));
        g.drawString(subtitle, (GameConfig.SCREEN_WIDTH - fmSub.stringWidth(subtitle)) / 2, titleY + 38);

        // --- 3. Interactive Menu Buttons ---
        g.setFont(menuFont);
        FontMetrics fmMenu = g.getFontMetrics();
        int startY = 295;
        int spacing = 52;

        for (int i = 0; i < options.length; i++) {
            String opt = options[i];
            int textWidth = fmMenu.stringWidth(opt);
            int optX = (GameConfig.SCREEN_WIDTH - textWidth) / 2;
            int optY = startY + i * spacing;

            int boxPadX = 35;
            int boxPadY = 12;
            Rectangle bounds = new Rectangle(optX - boxPadX, optY - fmMenu.getAscent() - boxPadY / 2,
                    textWidth + boxPadX * 2, fmMenu.getHeight() + boxPadY);
            optionBounds[i] = bounds;

            if (i == selectedOption) {
                // Highlighted button styling
                g.setColor(new Color(255, 100, 30, 40));
                g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
                g.setColor(new Color(255, 120, 50));
                g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);

                g.setColor(new Color(255, 230, 80));
                g.drawString(">  " + opt + "  <",
                        (GameConfig.SCREEN_WIDTH - fmMenu.stringWidth(">  " + opt + "  <")) / 2, optY);
            } else {
                // Normal unselected button
                g.setColor(new Color(180, 195, 220));
                g.drawString(opt, optX, optY);
            }
        }

        // --- 4. Footer Help Tip ---
        g.setFont(infoFont);
        g.setColor(new Color(100, 115, 140));
        String footer = "Use W/S or Arrow Keys to navigate, Enter to Select | F11: Full Screen | Mouse Click supported";
        FontMetrics fmFoot = g.getFontMetrics();
        g.drawString(footer, (GameConfig.SCREEN_WIDTH - fmFoot.stringWidth(footer)) / 2, GameConfig.SCREEN_HEIGHT - 40);
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
}
