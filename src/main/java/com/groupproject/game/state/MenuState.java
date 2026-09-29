package com.groupproject.game.state;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

/**
 * Main Menu State with keyboard and mouse navigation.
 */
public class MenuState extends GameState {

    private final String[] options = {"START MISSION", "HOW TO PLAY", "EXIT"};
    private int selectedOption = 0;
    private boolean showHowToPlay = false;

    private final Rectangle[] optionBounds = new Rectangle[3];
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
    }

    @Override
    public void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH) {
        if (showHowToPlay) {
            if (keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE) || keyH.isKeyPressedOnce(KeyEvent.VK_ENTER) || mouseH.isLeftClickedOnce()) {
                showHowToPlay = false;
            }
            return;
        }

        // Keyboard navigation
        if (keyH.isKeyPressedOnce(KeyEvent.VK_UP) || keyH.isKeyPressedOnce(KeyEvent.VK_W)) {
            selectedOption--;
            if (selectedOption < 0) selectedOption = options.length - 1;
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_DOWN) || keyH.isKeyPressedOnce(KeyEvent.VK_S)) {
            selectedOption++;
            if (selectedOption >= options.length) selectedOption = 0;
        }

        // Mouse hover detection
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

        if (keyH.isKeyPressedOnce(KeyEvent.VK_ENTER) || keyH.isKeyPressedOnce(KeyEvent.VK_SPACE)) {
            selectCurrentOption();
        }
    }

    private void selectCurrentOption() {
        switch (selectedOption) {
            case 0 -> gsm.setState(GameStateManager.StateType.PLAYING);
            case 1 -> showHowToPlay = true;
            case 2 -> System.exit(0);
        }
    }

    @Override
    public void render(Graphics2D g) {
        // Background - Dark Cybernetic Grid
        g.setColor(new Color(15, 18, 28));
        g.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        // Decorative grid lines
        g.setColor(new Color(28, 35, 54));
        for (int x = 0; x < GameConfig.SCREEN_WIDTH; x += 40) {
            g.drawLine(x, 0, x, GameConfig.SCREEN_HEIGHT);
        }
        for (int y = 0; y < GameConfig.SCREEN_HEIGHT; y += 40) {
            g.drawLine(0, y, GameConfig.SCREEN_WIDTH, y);
        }

        if (showHowToPlay) {
            renderHowToPlay(g);
            return;
        }

        // Main Title with glow effect
        g.setFont(titleFont);
        FontMetrics fmTitle = g.getFontMetrics();
        String title = "SOUL KNIGHT: SURVIVOR";
        int titleX = (GameConfig.SCREEN_WIDTH - fmTitle.stringWidth(title)) / 2;
        int titleY = 180;

        // Shadow / Glow
        g.setColor(new Color(255, 75, 43, 80));
        g.drawString(title, titleX + 3, titleY + 3);
        // Primary title text
        g.setColor(new Color(255, 120, 50));
        g.drawString(title, titleX, titleY);

        // Subtitle
        g.setFont(subtitleFont);
        FontMetrics fmSub = g.getFontMetrics();
        String subtitle = "POBG Top-Down Roguelite Shooter Edition";
        g.setColor(new Color(160, 180, 210));
        g.drawString(subtitle, (GameConfig.SCREEN_WIDTH - fmSub.stringWidth(subtitle)) / 2, titleY + 40);

        // Menu Options
        g.setFont(menuFont);
        FontMetrics fmMenu = g.getFontMetrics();
        int startY = 360;
        int spacing = 65;

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
                // Highlight box
                g.setColor(new Color(255, 100, 30, 40));
                g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
                g.setColor(new Color(255, 120, 50));
                g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);

                g.setColor(new Color(255, 230, 80));
                g.drawString(">  " + opt + "  <", (GameConfig.SCREEN_WIDTH - fmMenu.stringWidth(">  " + opt + "  <")) / 2, optY);
            } else {
                g.setColor(new Color(180, 195, 220));
                g.drawString(opt, optX, optY);
            }
        }

        // Footer info
        g.setFont(infoFont);
        g.setColor(new Color(100, 115, 140));
        String footer = "Use W/S or Arrow Keys to navigate, Enter to Select | Mouse Click supported";
        FontMetrics fmFoot = g.getFontMetrics();
        g.drawString(footer, (GameConfig.SCREEN_WIDTH - fmFoot.stringWidth(footer)) / 2, GameConfig.SCREEN_HEIGHT - 40);
    }

    private void renderHowToPlay(Graphics2D g) {
        int boxW = 800;
        int boxH = 460;
        int boxX = (GameConfig.SCREEN_WIDTH - boxW) / 2;
        int boxY = (GameConfig.SCREEN_HEIGHT - boxH) / 2;

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
        int gap = 32;

        g.drawString("• [W, A, S, D]: Move Operative (Up, Left, Down, Right)", boxX + 50, lineY);
        g.drawString("• [MOUSE AIM 360°]: Aim crosshair freely around operative", boxX + 50, lineY += gap);
        g.drawString("• [LEFT CLICK]: Fire Equipped Weapon", boxX + 50, lineY += gap);
        g.drawString("• [R]: Reload Weapon magazine", boxX + 50, lineY += gap);
        g.drawString("• [1, 2, Q]: Swap between primary and secondary firearms", boxX + 50, lineY += gap);
        g.drawString("• [SPACEBAR]: Tactical Dash / Roll (brief invulnerability)", boxX + 50, lineY += gap);
        g.drawString("• [ESC]: Pause Mission / Options", boxX + 50, lineY += gap);
        g.drawString("• ROGUELITE UPGRADES: Defeat enemies, gather EXP, pick 1 of 3 Perks!", boxX + 50, lineY += gap);

        g.setColor(new Color(255, 120, 50));
        g.drawString("Click anywhere or press [ENTER] / [ESC] to return to Main Menu", boxX + 50, boxY + boxH - 30);
    }
}
