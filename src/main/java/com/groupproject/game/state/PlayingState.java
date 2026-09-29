package com.groupproject.game.state;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * Main Gameplay State running the active combat session.
 */
public class PlayingState extends GameState {

    private final Camera camera;
    private double testPlayerX = GameConfig.WORLD_WIDTH / 2.0;
    private double testPlayerY = GameConfig.WORLD_HEIGHT / 2.0;
    private double aimAngle = 0.0;

    public PlayingState(GameStateManager gsm) {
        super(gsm);
        this.camera = new Camera(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
    }

    @Override
    public void init() {
        testPlayerX = GameConfig.WORLD_WIDTH / 2.0;
        testPlayerY = GameConfig.WORLD_HEIGHT / 2.0;
    }

    @Override
    public void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH) {
        // Check pause
        if (keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
            gsm.setState(GameStateManager.StateType.MENU);
            return;
        }

        // Test Movement
        double speed = GameConfig.PLAYER_DEFAULT_SPEED * deltaTime;
        if (keyH.isKeyDown(KeyEvent.VK_W)) testPlayerY -= speed;
        if (keyH.isKeyDown(KeyEvent.VK_S)) testPlayerY += speed;
        if (keyH.isKeyDown(KeyEvent.VK_A)) testPlayerX -= speed;
        if (keyH.isKeyDown(KeyEvent.VK_D)) testPlayerX += speed;

        // Smooth Camera Follow
        camera.update(testPlayerX, testPlayerY);

        // 360 Degree Aim Angle calculation
        double mouseWorldX = camera.screenToWorldX(mouseH.getScreenX());
        double mouseWorldY = camera.screenToWorldY(mouseH.getScreenY());
        aimAngle = Math.atan2(mouseWorldY - testPlayerY, mouseWorldX - testPlayerX);
    }

    @Override
    public void render(Graphics2D g) {
        // World Background
        g.setColor(new Color(24, 28, 38));
        g.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        // Render World Tile Grid (using Camera offset)
        renderWorldGrid(g);

        // Render Player placeholder
        int screenPX = camera.worldToScreenX(testPlayerX);
        int screenPY = camera.worldToScreenY(testPlayerY);

        // Draw Player body
        g.setColor(new Color(50, 150, 255));
        g.fillOval(screenPX - 18, screenPY - 18, 36, 36);
        g.setColor(Color.WHITE);
        g.drawOval(screenPX - 18, screenPY - 18, 36, 36);

        // Draw Gun pointing at 360 degree mouse direction
        int gunLength = 26;
        int gunEndX = (int) (screenPX + Math.cos(aimAngle) * gunLength);
        int gunEndY = (int) (screenPY + Math.sin(aimAngle) * gunLength);
        g.setColor(new Color(255, 200, 50));
        g.drawLine(screenPX, screenPY, gunEndX, gunEndY);

        // Render Crosshair at mouse position
        renderCrosshair(g, camera.worldToScreenX(camera.screenToWorldX(screenPX)), camera.worldToScreenY(camera.screenToWorldY(screenPY)));

        // HUD overlay
        g.setColor(new Color(255, 255, 255, 200));
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("MISSION ACTIVE | WASD to move | Mouse to aim 360° | ESC to Menu", 25, 35);
        g.drawString(String.format("Player World Pos: (%.1f, %.1f) | Camera: (%.1f, %.1f)",
                testPlayerX, testPlayerY, camera.getX(), camera.getY()), 25, 60);
    }

    private void renderWorldGrid(Graphics2D g) {
        int startX = (int) (camera.getX() / GameConfig.TILE_SIZE) * GameConfig.TILE_SIZE;
        int startY = (int) (camera.getY() / GameConfig.TILE_SIZE) * GameConfig.TILE_SIZE;
        int endX = startX + GameConfig.SCREEN_WIDTH + GameConfig.TILE_SIZE * 2;
        int endY = startY + GameConfig.SCREEN_HEIGHT + GameConfig.TILE_SIZE * 2;

        g.setColor(new Color(38, 44, 60));
        for (int x = startX; x <= endX; x += GameConfig.TILE_SIZE) {
            int sx = camera.worldToScreenX(x);
            g.drawLine(sx, 0, sx, GameConfig.SCREEN_HEIGHT);
        }
        for (int y = startY; y <= endY; y += GameConfig.TILE_SIZE) {
            int sy = camera.worldToScreenY(y);
            g.drawLine(0, sy, GameConfig.SCREEN_WIDTH, sy);
        }
    }

    private void renderCrosshair(Graphics2D g, int cx, int cy) {
        // Draw small precision crosshair
    }
}
