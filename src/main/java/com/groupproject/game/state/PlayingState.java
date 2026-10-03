package com.groupproject.game.state;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.dungeon.Room;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.entity.weapon.WeaponManager;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * Main Gameplay State managing real-time combat, weapons, projectiles, map collision, and camera.
 *
 * <p>WHY: Encapsulates all real-time combat systems, rendering of the tile map, camera tracking,
 * player entity updates, firearm discharges, and wall collision resolution. Decoupling this from
 * the Window and Panel ensures that game logic can be restarted, paused, or transitioned into an
 * upgrade screen without restarting the application.
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

    /**
     * Manager orchestrating all active projectiles in flight and wall impacts.
     */
    private final ProjectileManager projectileManager;

    /**
     * Manager coordinating equipped weapon slots, ammunition, reloading, and melee strikes.
     */
    private final WeaponManager weaponManager;

    // Player operative spatial variables
    private double playerX;
    private double playerY;
    private final double playerSize = 28.0; // Hitbox width and height in pixels

    /**
     * Angle in radians from player center to cursor in world space: atan2(dy, dx).
     */
    private double aimAngle = 0.0;

    /**
     * Constructs the playing state, builds the dungeon facility, camera, and combat managers.
     *
     * @param gsm The parent state manager.
     */
    public PlayingState(GameStateManager gsm) {
        super(gsm);
        this.camera = new Camera(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        this.dungeonMap = new DungeonMap();
        this.projectileManager = new ProjectileManager();
        this.weaponManager = new WeaponManager();
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

        projectileManager.clear();

        // Center camera on operative immediately on mission start
        camera.update(playerX + playerSize / 2.0, playerY + playerSize / 2.0);
    }

    /**
     * Updates locomotion, wall collision sliding, camera tracking, weapon swapping, and combat attacks.
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

        // --- 1. Weapon Hotkey Switching & Reloading ---
        if (keyH.isKeyPressedOnce(KeyEvent.VK_3) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD3)) {
            weaponManager.selectSlot(3); // Equip 7-round Tactical Pistol
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_4) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD4)) {
            weaponManager.selectSlot(4); // Equip Combat Knife
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_Q)) {
            weaponManager.quickSwap(); // Quick swap between last 2 weapons
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_R)) {
            weaponManager.reload(); // Manual magazine reload
        }

        // --- 2. Compute Desired Locomotion Vector ---
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

        // --- 3. Decoupled Axis Wall Collision (Wall Sliding) ---
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

        double playerCenterX = playerX + (playerSize / 2.0);
        double playerCenterY = playerY + (playerSize / 2.0);

        // --- 4. Smooth Camera Tracking ---
        camera.update(playerCenterX, playerCenterY);

        // --- 5. 360-Degree Aim Angle Calculation ---
        double mouseWorldX = camera.screenToWorldX(mouseH.getScreenX());
        double mouseWorldY = camera.screenToWorldY(mouseH.getScreenY());
        aimAngle = Math.atan2(mouseWorldY - playerCenterY, mouseWorldX - playerCenterX);

        // --- 6. Weapon Attack Execution (Mouse Left Click or Hold) ---
        if (mouseH.isLeftPressed()) {
            weaponManager.attack(playerCenterX, playerCenterY, aimAngle, projectileManager);
        }

        // --- 7. Advance Weapons & Projectiles ---
        weaponManager.update(deltaTime);
        projectileManager.update(deltaTime, dungeonMap);
    }

    /**
     * Renders dungeon terrain, flying bullets, melee slashes, operative, and tactical HUDs.
     */
    @Override
    public void render(Graphics2D g) {
        // 1. Render Visible Dungeon Map (with Viewport Culling)
        dungeonMap.render(g, camera);

        // 2. Render Active Flying Projectiles
        projectileManager.render(g, camera);

        // 3. Render Player Operative (Screen Space)
        double playerCenterX = playerX + (playerSize / 2.0);
        double playerCenterY = playerY + (playerSize / 2.0);
        int screenPX = camera.worldToScreenX(playerCenterX);
        int screenPY = camera.worldToScreenY(playerCenterY);

        // Operative shadow
        g.setColor(new Color(0, 0, 0, 100));
        g.fillOval(screenPX - 14, screenPY - 8, 28, 16);

        // Operative body (Cyan cyber tactical suit)
        g.setColor(new Color(40, 180, 220));
        g.fillOval(screenPX - 15, screenPY - 15, 30, 30);
        g.setColor(new Color(255, 255, 255, 200));
        g.drawOval(screenPX - 15, screenPY - 15, 30, 30);

        // Operative visor/helmet indicator pointing toward aim angle
        int visorX = (int) (screenPX + Math.cos(aimAngle) * 6);
        int visorY = (int) (screenPY + Math.sin(aimAngle) * 6);
        g.setColor(new Color(255, 230, 80));
        g.fillOval(visorX - 4, visorY - 4, 8, 8);

        // 4. Render Equipped Weapon & Melee Slash VFX
        weaponManager.render(g, camera, playerCenterX, playerCenterY, aimAngle);

        // 5. Render Top Sector Information HUD
        renderSectorHUD(g, playerCenterX, playerCenterY);

        // 6. Render Bottom Weapon & Ammunition HUD
        weaponManager.renderHUD(g, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
    }

    /**
     * Displays current operative location, active chamber title, and key tips.
     */
    private void renderSectorHUD(Graphics2D g, double pcx, double pcy) {
        g.setColor(new Color(15, 20, 30, 200));
        g.fillRoundRect(20, 20, 440, 85, 12, 12);
        g.setColor(new Color(60, 80, 110));
        g.drawRoundRect(20, 20, 440, 85, 12, 12);

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
        g.drawString(String.format("Operative Pos: (%.1f, %.1f) | Active Bullets: %d",
                pcx, pcy, projectileManager.getCount()), 35, 68);
        g.setColor(new Color(140, 160, 185));
        g.drawString("Move: [WASD] | Aim/Shoot: [Mouse 360°] | Weapons: [3] [4] [Q]", 35, 88);
    }

    public DungeonMap getDungeonMap() {
        return dungeonMap;
    }

    public WeaponManager getWeaponManager() {
        return weaponManager;
    }

    public ProjectileManager getProjectileManager() {
        return projectileManager;
    }

    public double getPlayerX() {
        return playerX;
    }

    public double getPlayerY() {
        return playerY;
    }
}
