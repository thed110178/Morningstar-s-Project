package com.groupproject.game.state;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.dungeon.Room;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * Main Gameplay State managing the active combat session within the Dungeon Map.
 *
 * <p>WHY: Encapsulates all real-time combat systems, rendering of the tile map, camera tracking,
 * player entity updates, and wall collision resolution. Decoupling this from the Window and Panel
 * ensures that game logic can be restarted, paused, or transitioned into an upgrade screen without
 * restarting the application.
 */
public class PlayingState extends GameState {

    /**
     * Dedicated 2D camera tracking the player across the large world coordinate space.
     */
    private final Camera camera;

    /**
     * Structured 3-chamber facility map (Spawn, Combat Arena, Extraction Zone).
     */
    private final DungeonMap dungeonMap;

    // Player operative spatial variables
    private double playerX;
    private double playerY;
    private final double playerSize = 28.0; // Hitbox width and height in pixels

    /**
     * Angle in radians from player center to cursor in world space: atan2(dy, dx).
     */
    private double aimAngle = 0.0;

    /**
     * Constructs the playing state, builds the dungeon facility, and initializes the camera.
     *
     * @param gsm The parent state manager.
     */
    public PlayingState(GameStateManager gsm) {
        super(gsm);
        this.camera = new Camera(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        this.dungeonMap = new DungeonMap();
    }

    /**
     * Deploys the player operative in the center of the Spawn Room upon entering a new mission.
     */
    @Override
    public void init() {
        Room spawnRoom = dungeonMap.getSpawnRoom();
        if (spawnRoom != null) {
            this.playerX = spawnRoom.getCenterWorldX() - (playerSize / 2.0);
            this.playerY = spawnRoom.getCenterWorldY() - (playerSize / 2.0);
        } else {
            this.playerX = GameConfig.WORLD_WIDTH / 2.0;
            this.playerY = GameConfig.WORLD_HEIGHT / 2.0;
        }
        // Center the camera on the player immediately on mission start
        camera.update(playerX + playerSize / 2.0, playerY + playerSize / 2.0);
    }

    /**
     * Updates locomotion with wall collision sliding, camera tracking, and 360-degree aiming.
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

        // --- 1. Compute Desired Velocity Vector ---
        double moveStep = GameConfig.PLAYER_DEFAULT_SPEED * deltaTime;
        double moveX = 0;
        double moveY = 0;

        if (keyH.isKeyDown(KeyEvent.VK_W)) moveY -= moveStep;
        if (keyH.isKeyDown(KeyEvent.VK_S)) moveY += moveStep;
        if (keyH.isKeyDown(KeyEvent.VK_A)) moveX -= moveStep;
        if (keyH.isKeyDown(KeyEvent.VK_D)) moveX += moveStep;

        // Diagonal movement normalization (prevents moving 1.414x faster when pressing W+D)
        if (moveX != 0 && moveY != 0) {
            double invSqrt = 1.0 / Math.sqrt(2.0);
            moveX *= invSqrt;
            moveY *= invSqrt;
        }

        // --- 2. Decoupled Axis Wall Collision (Wall Sliding) ---
        // WHY: Resolving collision along the X and Y axes independently allows the operative
        // to slide smoothly along walls when moving diagonally into obstacles instead of coming
        // to an abrupt, frustrating halt.
        if (moveX != 0) {
            double nextX = playerX + moveX;
            if (dungeonMap.isAreaWalkable(nextX, playerY, playerSize, playerSize)) {
                playerX = nextX;
            }
        }
        if (moveY != 0) {
            double nextY = playerY + moveY;
            if (dungeonMap.isAreaWalkable(playerX, nextY, playerSize, playerSize)) {
                playerY = nextY;
            }
        }

        // --- 3. Smooth Camera Tracking ---
        double playerCenterX = playerX + (playerSize / 2.0);
        double playerCenterY = playerY + (playerSize / 2.0);
        camera.update(playerCenterX, playerCenterY);

        // --- 4. 360-Degree Aim Angle Calculation ---
        double mouseWorldX = camera.screenToWorldX(mouseH.getScreenX());
        double mouseWorldY = camera.screenToWorldY(mouseH.getScreenY());
        aimAngle = Math.atan2(mouseWorldY - playerCenterY, mouseWorldX - playerCenterX);
    }

    /**
     * Renders dungeon terrain tiles, operative body, firearm, and tactical status HUD.
     */
    @Override
    public void render(Graphics2D g) {
        // --- 1. Render Visible Dungeon Map (with Viewport Culling) ---
        dungeonMap.render(g, camera);

        // --- 2. Render Player Operative (Screen Space) ---
        double playerCenterX = playerX + (playerSize / 2.0);
        double playerCenterY = playerY + (playerSize / 2.0);
        int screenPX = camera.worldToScreenX(playerCenterX);
        int screenPY = camera.worldToScreenY(playerCenterY);

        // Operative shadow
        g.setColor(new Color(0, 0, 0, 100));
        g.fillOval(screenPX - 14, screenPY - 8, 28, 16);

        // Operative body (Cyan cyber suit)
        g.setColor(new Color(40, 180, 220));
        g.fillOval(screenPX - 15, screenPY - 15, 30, 30);
        g.setColor(new Color(255, 255, 255, 200));
        g.drawOval(screenPX - 15, screenPY - 15, 30, 30);

        // Operative visor/helmet indicator
        int visorX = (int) (screenPX + Math.cos(aimAngle) * 6);
        int visorY = (int) (screenPY + Math.sin(aimAngle) * 6);
        g.setColor(new Color(255, 230, 80));
        g.fillOval(visorX - 4, visorY - 4, 8, 8);

        // --- 3. 360-Degree Firearm Barrel ---
        int gunLength = 24;
        int gunEndX = (int) (screenPX + Math.cos(aimAngle) * gunLength);
        int gunEndY = (int) (screenPY + Math.sin(aimAngle) * gunLength);
        g.setColor(new Color(255, 140, 40));
        g.drawLine(screenPX, screenPY, gunEndX, gunEndY);

        // --- 4. Tactical Status HUD Overlay ---
        renderHUD(g, playerCenterX, playerCenterY);
    }

    /**
     * Displays current operative location, active room name, and navigation help.
     */
    private void renderHUD(Graphics2D g, double pcx, double pcy) {
        // HUD container box
        g.setColor(new Color(15, 20, 30, 200));
        g.fillRoundRect(20, 20, 420, 85, 12, 12);
        g.setColor(new Color(60, 80, 110));
        g.drawRoundRect(20, 20, 420, 85, 12, 12);

        // Identify current room
        String currentRoomName = "Connecting Corridor";
        for (Room r : dungeonMap.getRooms()) {
            if (r.containsWorldPoint(pcx, pcy)) {
                currentRoomName = r.getName();
                break;
            }
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.setColor(new Color(255, 200, 50));
        g.drawString("SECTOR: " + currentRoomName.toUpperCase(), 35, 45);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(200, 215, 235));
        g.drawString(String.format("Operative Pos: (%.1f, %.1f) | Camera: (%.1f, %.1f)",
                pcx, pcy, camera.getX(), camera.getY()), 35, 68);
        g.setColor(new Color(140, 160, 185));
        g.drawString("Move: [WASD] | Aim: [Mouse 360°] | Menu: [ESC]", 35, 88);
    }

    public DungeonMap getDungeonMap() {
        return dungeonMap;
    }

    public double getPlayerX() {
        return playerX;
    }

    public double getPlayerY() {
        return playerY;
    }
}
