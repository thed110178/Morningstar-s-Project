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
 *
 * <p>WHY: Encapsulates all real-time combat systems, rendering of the tile map, camera tracking,
 * and player entity updates. Decoupling this from the Window and Panel ensures that game logic
 * can be restarted, paused, or transitioned into an upgrade screen without restarting the application.
 */
public class PlayingState extends GameState {

    /**
     * Dedicated 2D camera tracking the player across the large world coordinate space.
     */
    private final Camera camera;

    // Temporary player coordinates for prototype movement testing
    private double testPlayerX = GameConfig.WORLD_WIDTH / 2.0;
    private double testPlayerY = GameConfig.WORLD_HEIGHT / 2.0;

    /**
     * Angle in radians from player center to cursor in world space: atan2(dy, dx).
     */
    private double aimAngle = 0.0;

    /**
     * Constructs the playing state and binds the camera to the default screen dimensions.
     *
     * @param gsm The parent state manager.
     */
    public PlayingState(GameStateManager gsm) {
        super(gsm);
        this.camera = new Camera(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
    }

    /**
     * Resets player coordinates to the center of the world when entering a new combat session.
     */
    @Override
    public void init() {
        testPlayerX = GameConfig.WORLD_WIDTH / 2.0;
        testPlayerY = GameConfig.WORLD_HEIGHT / 2.0;
    }

    /**
     * Updates locomotion, camera tracking, and computes the 360-degree aiming angle.
     *
     * @param deltaTime Elapsed delta time in seconds.
     * @param keyH      Keyboard input handler.
     * @param mouseH    Mouse input handler.
     */
    @Override
    public void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH) {
        // Handle pause request: returns to Main Menu
        if (keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
            gsm.setState(GameStateManager.StateType.MENU);
            return;
        }

        // --- 1. Framerate-Independent WASD Locomotion ---
        // WHY: Multiplying speed by deltaTime ensures uniform movement speed across any FPS
        double speed = GameConfig.PLAYER_DEFAULT_SPEED * deltaTime;
        if (keyH.isKeyDown(KeyEvent.VK_W)) testPlayerY -= speed;
        if (keyH.isKeyDown(KeyEvent.VK_S)) testPlayerY += speed;
        if (keyH.isKeyDown(KeyEvent.VK_A)) testPlayerX -= speed;
        if (keyH.isKeyDown(KeyEvent.VK_D)) testPlayerX += speed;

        // --- 2. Smooth Camera Following ---
        camera.update(testPlayerX, testPlayerY);

        // --- 3. 360-Degree Aim Angle Calculation ---
        // WHY: The mouse position is in screen pixels, but the player moves in world coordinates.
        // We must translate mouse screen coordinates to world coordinates via the camera before
        // computing the directional angle using atan2(deltaY, deltaX).
        double mouseWorldX = camera.screenToWorldX(mouseH.getScreenX());
        double mouseWorldY = camera.screenToWorldY(mouseH.getScreenY());
        aimAngle = Math.atan2(mouseWorldY - testPlayerY, mouseWorldX - testPlayerX);
    }

    /**
     * Renders world terrain, player operative, and gun aiming indicator.
     */
    @Override
    public void render(Graphics2D g) {
        // --- 1. World Backdrop ---
        g.setColor(new Color(24, 28, 38));
        g.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        // Render visible tile grid based on current camera offset
        renderWorldGrid(g);

        // --- 2. Player Entity (Screen Space) ---
        int screenPX = camera.worldToScreenX(testPlayerX);
        int screenPY = camera.worldToScreenY(testPlayerY);

        // Draw Operative circular body
        g.setColor(new Color(50, 150, 255));
        g.fillOval(screenPX - 18, screenPY - 18, 36, 36);
        g.setColor(Color.WHITE);
        g.drawOval(screenPX - 18, screenPY - 18, 36, 36);

        // --- 3. 360-Degree Firearm Barrel ---
        // WHY: Visual feedback showing precisely where bullets will emerge
        int gunLength = 26;
        int gunEndX = (int) (screenPX + Math.cos(aimAngle) * gunLength);
        int gunEndY = (int) (screenPY + Math.sin(aimAngle) * gunLength);
        g.setColor(new Color(255, 200, 50));
        g.drawLine(screenPX, screenPY, gunEndX, gunEndY);

        // --- 4. Diagnostic HUD ---
        g.setColor(new Color(255, 255, 255, 200));
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("MISSION ACTIVE | WASD to move | Mouse to aim 360° | ESC to Menu", 25, 35);
        g.drawString(String.format("Player World Pos: (%.1f, %.1f) | Camera: (%.1f, %.1f)",
                testPlayerX, testPlayerY, camera.getX(), camera.getY()), 25, 60);
    }

    /**
     * Efficiently renders only the tiles intersecting the camera viewport.
     *
     * <p>WHY: Culling off-screen tiles prevents wasting CPU cycles rendering thousands of invisible tiles.
     */
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
}
