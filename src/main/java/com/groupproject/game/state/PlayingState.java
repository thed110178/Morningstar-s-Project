package com.groupproject.game.state;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.dungeon.Room;
import com.groupproject.game.entity.enemy.TargetDummy;
import com.groupproject.game.entity.projectile.Projectile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.entity.weapon.MeleeSlash;
import com.groupproject.game.entity.weapon.WeaponManager;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Gameplay State managing real-time combat, weapons, projectiles, map collision, camera,
 * and the combat chamber lockdown/unlock lifecycle.
 *
 * <p>WHY: Encapsulates all real-time combat systems, rendering of the tile map, camera tracking,
 * player entity updates, firearm discharges, hostile target elimination, and wall/door collision.
 * When the operative enters the central combat arena, blast doors seal shut (DOOR_CLOSED) until
 * all hostiles are eradicated, unlocking passage to the extraction zone (DOOR_OPEN).
 */
public class PlayingState extends GameState {

    private final Camera camera;
    private final DungeonMap dungeonMap;
    private final ProjectileManager projectileManager;
    private final WeaponManager weaponManager;

    private final List<TargetDummy> combatTargets = new ArrayList<>();

    // Player operative spatial variables
    private double playerX;
    private double playerY;
    private final double playerSize = 28.0; // Hitbox width and height in pixels

    /**
     * Angle in radians from player center to cursor in world space: atan2(dy, dx).
     */
    private double aimAngle = 0.0;

    // Tactical announcement banner timer
    private String bannerMessage = "";
    private Color bannerColor = Color.WHITE;
    private double bannerTimer = 0.0;

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
     * Deploys the player operative in the center of the Spawn Room and populates the Combat Arena.
     */
    @Override
    public void init() {
        // Reset dungeon rooms and doors so new mission starts with unvisited/open state
        dungeonMap.reset();

        Room spawnRoom = dungeonMap.getSpawnRoom();
        if (spawnRoom != null) {
            this.playerX = spawnRoom.getCenterWorldX() - (playerSize / 2.0);
            this.playerY = spawnRoom.getCenterWorldY() - (playerSize / 2.0);
        } else {
            this.playerX = GameConfig.WORLD_WIDTH / 2.0;
            this.playerY = GameConfig.WORLD_HEIGHT / 2.0;
        }

        projectileManager.clear();
        spawnCombatTargets();

        showBanner("MISSION START - PROCEED TO COMBAT ARENA", new Color(60, 220, 160), 3.5);

        // Center camera on operative immediately on mission start
        camera.update(playerX + playerSize / 2.0, playerY + playerSize / 2.0);
    }

    /**
     * Spawns training targets inside the Combat Arena.
     */
    private void spawnCombatTargets() {
        combatTargets.clear();
        Room arena = dungeonMap.getCombatRoom();
        if (arena != null) {
            double cx = arena.getCenterWorldX();
            double cy = arena.getCenterWorldY();

            // 3 hostile test targets positioned tactically around the cover pillars
            combatTargets.add(new TargetDummy(cx, cy, 75));
            combatTargets.add(new TargetDummy(cx + 190, cy - 140, 75));
            combatTargets.add(new TargetDummy(cx + 190, cy + 140, 75));
        }
    }

    /**
     * Displays an animated tactical HUD announcement banner.
     */
    private void showBanner(String message, Color color, double duration) {
        this.bannerMessage = message;
        this.bannerColor = color;
        this.bannerTimer = duration;
    }

    /**
     * Updates locomotion, wall/door collision sliding, camera tracking, combat, and room lockdown logic.
     */
    @Override
    public void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH) {
        // Handle pause request: returns to Main Menu
        if (keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
            gsm.setState(GameStateManager.StateType.MENU);
            return;
        }

        if (bannerTimer > 0) {
            bannerTimer -= deltaTime;
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

        // Diagonal movement normalization
        if (moveX != 0 && moveY != 0) {
            double invSqrt = 1.0 / Math.sqrt(2.0);
            moveX *= invSqrt;
            moveY *= invSqrt;
        }

        // --- 3. Decoupled Axis Wall Collision (Wall & Closed Door Sliding) ---
        // WHY: isAreaWalkable automatically blocks movement through DOOR_CLOSED tiles
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

        // --- 4. Combat Room Lockdown & Trigger Detection ---
        Room combatRoom = dungeonMap.getCombatRoom();
        if (combatRoom != null) {
            // Trigger lockdown as soon as operative enters the combat arena while hostiles remain
            if (combatRoom.getStatus() == Room.RoomStatus.UNVISITED
                    && combatRoom.hasEntityEntered(playerX, playerY, playerSize, playerSize)
                    && !combatTargets.isEmpty()) {

                // WHY: Gently nudge operative forward if still slightly overlapping doorway tile
                // so the operative hitbox is fully contained on interior floor tiles and is never
                // clipped, trapped, or blocked inside the solidifying blast gate.
                double westDoorBoundary = (combatRoom.getGridX() + 1.0) * GameConfig.TILE_SIZE;
                if (playerX < westDoorBoundary) {
                    playerX = westDoorBoundary;
                }
                double eastDoorBoundary = (combatRoom.getGridX() + combatRoom.getWidthInTiles() - 1.0) * GameConfig.TILE_SIZE;
                if (playerX + playerSize > eastDoorBoundary) {
                    playerX = eastDoorBoundary - playerSize;
                }

                combatRoom.lockdown();
                showBanner("! COMBAT LOCKDOWN: DOORS SEALED - ELIMINATE ALL TARGETS !",
                        new Color(255, 60, 60), 3.5);
            }
        }

        // --- 5. Smooth Camera Tracking ---
        camera.update(playerCenterX, playerCenterY);

        // --- 6. 360-Degree Aim Angle Calculation ---
        double mouseWorldX = camera.screenToWorldX(mouseH.getScreenX());
        double mouseWorldY = camera.screenToWorldY(mouseH.getScreenY());
        aimAngle = Math.atan2(mouseWorldY - playerCenterY, mouseWorldX - playerCenterX);

        // --- 7. Weapon Attack Execution ---
        if (mouseH.isLeftPressed()) {
            weaponManager.attack(playerCenterX, playerCenterY, aimAngle, projectileManager);
        }

        // --- 8. Advance Weapons & Projectiles ---
        weaponManager.update(deltaTime);
        projectileManager.update(deltaTime, dungeonMap);

        // --- 9. Check Projectile and Melee Hits on Hostile Targets ---
        updateCombatDamageResolution();

        // --- 10. Check Room Clear Condition and Unlock Doors ---
        if (combatRoom != null && combatRoom.isLocked() && combatTargets.isEmpty()) {
            combatRoom.unlock();
            showBanner("★ ROOM CLEARED: DOORS UNLOCKED ★",
                    new Color(60, 220, 120), 4.0);
        }
    }

    /**
     * Resolves collision detection between active bullets / knife slashes and hostile targets.
     */
    private void updateCombatDamageResolution() {
        // 1. Projectile hits
        for (Projectile p : projectileManager.getActiveProjectiles()) {
            if (!p.isActive() || !p.isPlayerBullet()) continue;

            for (TargetDummy dummy : combatTargets) {
                if (dummy.isAlive() && p.getBounds().intersects(dummy.getBounds())) {
                    dummy.takeDamage(p.getDamage());
                    p.deactivate(); // Bullet spent
                    break;
                }
            }
        }

        // 2. Melee knife slash hits
        for (MeleeSlash slash : weaponManager.getActiveSlashes()) {
            if (!slash.isActive()) continue;

            for (TargetDummy dummy : combatTargets) {
                if (dummy.isAlive() && slash.getBounds().intersects(dummy.getBounds())) {
                    dummy.takeDamage(slash.getDamage());
                }
            }
        }

        // 3. Update targets and remove eliminated hostiles
        for (TargetDummy dummy : combatTargets) {
            dummy.update(0.016);
        }
        combatTargets.removeIf(d -> !d.isAlive());
    }

    /**
     * Renders dungeon terrain, doors, flying bullets, melee slashes, operative, targets, and tactical HUDs.
     */
    @Override
    public void render(Graphics2D g) {
        // 1. Render Visible Dungeon Map (with Viewport Culling & Door tiles)
        dungeonMap.render(g, camera);

        // 2. Render Hostile Combat Targets
        for (TargetDummy dummy : combatTargets) {
            dummy.render(g, camera);
        }

        // 3. Render Active Flying Projectiles
        projectileManager.render(g, camera);

        // 4. Render Player Operative (Screen Space)
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

        // 5. Render Equipped Weapon & Melee Slash VFX
        weaponManager.render(g, camera, playerCenterX, playerCenterY, aimAngle);

        // 6. Render Top Sector Information HUD
        renderSectorHUD(g, playerCenterX, playerCenterY);

        // 7. Render Bottom Weapon & Ammunition HUD
        weaponManager.renderHUD(g, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        // 8. Render Tactical Announcement Banner if active
        if (bannerTimer > 0 && !bannerMessage.isEmpty()) {
            renderBanner(g);
        }
    }

    /**
     * Displays current operative location, active chamber title, remaining targets, and key tips.
     */
    private void renderSectorHUD(Graphics2D g, double pcx, double pcy) {
        g.setColor(new Color(15, 20, 30, 200));
        g.fillRoundRect(20, 20, 440, 88, 12, 12);
        g.setColor(new Color(60, 80, 110));
        g.drawRoundRect(20, 20, 440, 88, 12, 12);

        String currentRoomName = "Connecting Corridor";
        for (Room r : dungeonMap.getRooms()) {
            if (r.containsWorldPoint(pcx, pcy)) {
                currentRoomName = r.getName();
                break;
            }
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        if (dungeonMap.getCombatRoom().isLocked()) {
            g.setColor(new Color(255, 60, 60));
            g.drawString("SECTOR: COMBAT LOCKDOWN (" + combatTargets.size() + " HOSTILES REMAIN)", 35, 45);
        } else {
            g.setColor(new Color(255, 200, 50));
            g.drawString("SECTOR: " + currentRoomName.toUpperCase(), 35, 45);
        }

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(200, 215, 235));
        g.drawString(String.format("Operative: (%.0f, %.0f) | Bullets: %d | Hostiles: %d",
                pcx, pcy, projectileManager.getCount(), combatTargets.size()), 35, 68);
        g.setColor(new Color(140, 160, 185));
        g.drawString("Move: [WASD] | Aim/Shoot: [Mouse 360°] | Weapons: [3] [4] [Q]", 35, 88);
    }

    /**
     * Renders a glowing centered banner when doors lock or unlock.
     */
    private void renderBanner(Graphics2D g) {
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(bannerMessage);
        int pad = 24;

        int boxW = textW + pad * 2;
        int boxH = 46;
        int boxX = (GameConfig.SCREEN_WIDTH - boxW) / 2;
        int boxY = 125;

        g.setColor(new Color(15, 18, 28, 220));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 10, 10);
        g.setColor(bannerColor);
        g.drawRoundRect(boxX, boxY, boxW, boxH, 10, 10);

        g.setColor(bannerColor);
        g.drawString(bannerMessage, boxX + pad, boxY + 28);
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

    public List<TargetDummy> getCombatTargets() {
        return combatTargets;
    }
}
