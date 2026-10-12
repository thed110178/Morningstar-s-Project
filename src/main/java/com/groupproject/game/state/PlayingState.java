package com.groupproject.game.state;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.dungeon.Room;
import com.groupproject.game.entity.enemy.TargetDummy;
import com.groupproject.game.entity.item.WeaponChest;
import com.groupproject.game.entity.projectile.Projectile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.entity.weapon.MeleeSlash;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.entity.weapon.WeaponManager;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import com.groupproject.game.ui.BackpackUI;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import com.groupproject.game.dungeon.Tile;
import com.groupproject.game.dungeon.TileType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    private final List<TargetDummy> combatTargets = new CopyOnWriteArrayList<>();

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

    // Extraction zone interaction and victory transition
    private boolean isStandingOnExtract = false;
    private boolean isExtracting = false;
    private double extractTransitionTimer = 0.0;
    private static final double EXTRACT_DURATION = 4.0; // 4.0 seconds black screen before auto return

    // Hòm tiếp tế vũ khí xuất hiện tại các phòng chiến đấu đã dọn sạch
    private final List<WeaponChest> weaponChests = new CopyOnWriteArrayList<>();
    private Room activeCombatRoom = null;

    // Tiến trình Stage trong Biome (1 -> 2 -> 3 Boss Arena)
    private int currentStage = 1;
    public static final int MAX_STAGES = 3;

    // Giao diện Balo chiến thuật (Tab)
    private final BackpackUI backpackUI = new BackpackUI();
    private boolean isBackpackOpen = false;

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
     * Deploys the player operative in the center of the Spawn Room and initializes the procedural run.
     */
    @Override
    public void init() {
        this.currentStage = 1;
        // Reset dungeon rooms and doors so new mission starts with procedural layout
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
        combatTargets.clear();
        weaponChests.clear();
        activeCombatRoom = null;

        this.isStandingOnExtract = false;
        this.isExtracting = false;
        this.extractTransitionTimer = 0.0;
        this.isBackpackOpen = false;
        this.weaponManager.reset();

        int numCombat = dungeonMap.getCombatRooms().size();
        showBanner(String.format("BIOME 1 - STAGE 1/%d: TIÊU DIỆT %d PHÒNG CHIẾN ĐẤU ĐỂ SƠ TÁN", MAX_STAGES, numCombat),
                new Color(60, 220, 160), 4.0);

        // Center camera on operative immediately on mission start
        camera.update(playerX + playerSize / 2.0, playerY + playerSize / 2.0);
    }

    /**
     * Chuyển tiếp người chơi sang stage tiếp theo trong Biome mà vẫn bảo lưu trang bị / balo.
     */
    private void advanceToNextStage() {
        currentStage++;
        dungeonMap.generateStage(currentStage);

        Room spawnRoom = dungeonMap.getSpawnRoom();
        if (spawnRoom != null) {
            this.playerX = spawnRoom.getCenterWorldX() - (playerSize / 2.0);
            this.playerY = spawnRoom.getCenterWorldY() - (playerSize / 2.0);
        } else {
            this.playerX = GameConfig.WORLD_WIDTH / 2.0;
            this.playerY = GameConfig.WORLD_HEIGHT / 2.0;
        }

        projectileManager.clear();
        combatTargets.clear();
        weaponChests.clear();
        activeCombatRoom = null;
        isStandingOnExtract = false;

        camera.update(playerX + playerSize / 2.0, playerY + playerSize / 2.0);

        if (currentStage == 3) {
            showBanner("★ TIẾN VÀO STAGE 3/3: GRAND BOSS ARENA - CHIẾN ĐẤU ĐỈNH ĐIỂM! ★",
                    new Color(255, 70, 70), 5.0);
        } else {
            int numCombat = dungeonMap.getCombatRooms().size();
            showBanner(String.format("★ TIẾN VÀO STAGE %d/%d: TIÊU DIỆT %d PHÒNG CHIẾN ĐẤU ★",
                    currentStage, MAX_STAGES, numCombat),
                    new Color(60, 220, 160), 4.0);
        }
    }

    /**
     * Kiểm tra xem một vị trí trong phòng có hoàn toàn an toàn và hợp lệ để sinh kẻ địch hay không.
     * Đảm bảo:
     * 1. Bounding box của enemy (30x30 kèm đệm an toàn) hoàn toàn đi lại được (không dính tường, cover cứng, cover tạm thời).
     * 2. Nằm hoàn toàn bên trong phòng (cách biên tường).
     * 3. Không trùng hoặc quá gần người chơi (cách ít nhất 70px).
     * 4. Không trùng hoặc quá gần các enemy khác đã sinh (cách ít nhất 40px).
     * 5. Không nằm trên hoặc quá gần các ô cửa (cách ít nhất 55px).
     */
    private boolean isSafeEnemySpawnPoint(Room arena, double x, double y, List<TargetDummy> currentList) {
        if (arena == null) return false;

        // 1. Kiểm tra nằm trong phạm vi lòng phòng (cách biên tường ít nhất 32px)
        double minX = arena.getGridX() * GameConfig.TILE_SIZE + 32;
        double maxX = (arena.getGridX() + arena.getWidthInTiles()) * GameConfig.TILE_SIZE - 32;
        double minY = arena.getGridY() * GameConfig.TILE_SIZE + 32;
        double maxY = (arena.getGridY() + arena.getHeightInTiles()) * GameConfig.TILE_SIZE - 32;

        if (x < minX || x > maxX || y < minY || y > maxY) {
            return false;
        }

        // 2. Bounding box 34x34 quanh tâm (x, y) phải hoàn toàn walkable (không tường, không cover cứng, không cover tạm thời)
        if (!dungeonMap.isAreaWalkable(x - 17, y - 17, 34, 34)) {
            return false;
        }

        // 3. Không sinh đè lên người chơi (khoảng cách an toàn ít nhất 70px)
        double distToPlayer = Math.hypot(x - playerX, y - playerY);
        if (distToPlayer < 70.0) {
            return false;
        }

        // 4. Không đè lên các enemy khác đã được thêm vào phòng (khoảng cách ít nhất 40px)
        for (TargetDummy other : currentList) {
            double dist = Math.hypot(x - other.getX(), y - other.getY());
            if (dist < 40.0) {
                return false;
            }
        }

        // 5. Không nằm quá gần các ô cửa của phòng (khoảng cách an toàn ít nhất 55px)
        for (Tile door : arena.getDoorTiles()) {
            double doorCenterX = door.getWorldX() + GameConfig.TILE_SIZE / 2.0;
            double doorCenterY = door.getWorldY() + GameConfig.TILE_SIZE / 2.0;
            if (Math.hypot(x - doorCenterX, y - doorCenterY) < 55.0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tìm vị trí an toàn nhất trong phòng để sinh kẻ địch, ưu tiên vị trí lý tưởng (preferredX, preferredY).
     * Nếu vị trí lý tưởng bị vướng cover hoặc vật cản, tự động quét bán kính xung quanh để tìm ô sàn trống.
     */
    private Point2D.Double findSafeSpawnLocation(Room arena, double preferredX, double preferredY, List<TargetDummy> currentList) {
        // Nếu vị trí mong muốn đã hoàn toàn an toàn, sử dụng ngay
        if (isSafeEnemySpawnPoint(arena, preferredX, preferredY, currentList)) {
            return new Point2D.Double(preferredX, preferredY);
        }

        // Quét bán kính xung quanh vị trí mong muốn theo các vòng tròn bán kính tăng dần (spiral search)
        double[] radiuses = {24, 48, 72, 96, 120, 150, 180};
        for (double r : radiuses) {
            for (int deg = 0; deg < 360; deg += 30) {
                double rad = Math.toRadians(deg);
                double testX = preferredX + r * Math.cos(rad);
                double testY = preferredY + r * Math.sin(rad);
                if (isSafeEnemySpawnPoint(arena, testX, testY, currentList)) {
                    return new Point2D.Double(testX, testY);
                }
            }
        }

        // Nếu vẫn chưa tìm được qua spiral search, quét tất cả các ô FLOOR trong phòng
        int rx = arena.getGridX();
        int ry = arena.getGridY();
        int rw = arena.getWidthInTiles();
        int rh = arena.getHeightInTiles();

        for (int c = rx + 1; c < rx + rw - 1; c++) {
            for (int r = ry + 1; r < ry + rh - 1; r++) {
                Tile tile = dungeonMap.getTile(c, r);
                if (tile != null && tile.getType() == TileType.FLOOR) {
                    double testX = tile.getWorldX() + GameConfig.TILE_SIZE / 2.0;
                    double testY = tile.getWorldY() + GameConfig.TILE_SIZE / 2.0;
                    if (isSafeEnemySpawnPoint(arena, testX, testY, currentList)) {
                        return new Point2D.Double(testX, testY);
                    }
                }
            }
        }

        // Fallback an toàn: Tâm phòng
        return new Point2D.Double(arena.getCenterWorldX(), arena.getCenterWorldY());
    }

    /**
     * Sinh một kẻ địch một cách an toàn vào danh sách, đảm bảo 100% không bị kẹt vào cover hay tường.
     */
    private void spawnEnemySafely(Room arena, double preferredX, double preferredY, int health) {
        Point2D.Double safePos = findSafeSpawnLocation(arena, preferredX, preferredY, combatTargets);
        combatTargets.add(new TargetDummy(safePos.x, safePos.y, health));
    }

    /**
     * Sinh các mục tiêu chiến đấu an toàn cho từng phòng combat khi bị phong tỏa.
     */
    private void spawnTargetsForRoom(Room arena) {
        combatTargets.clear();
        if (arena != null) {
            double cx = arena.getCenterWorldX();
            double cy = arena.getCenterWorldY();

            if (currentStage == 3) {
                // Stage 3 Boss Arena: Thử thách lớn với 9 mục tiêu chiến đấu, dummy trung tâm có lượng máu cao
                spawnEnemySafely(arena, cx, cy, 150);
                spawnEnemySafely(arena, cx + 220, cy, 100);
                spawnEnemySafely(arena, cx - 220, cy, 100);
                spawnEnemySafely(arena, cx, cy + 180, 100);
                spawnEnemySafely(arena, cx, cy - 180, 100);
                spawnEnemySafely(arena, cx + 160, cy + 130, 80);
                spawnEnemySafely(arena, cx - 160, cy + 130, 80);
                spawnEnemySafely(arena, cx + 160, cy - 130, 80);
                spawnEnemySafely(arena, cx - 160, cy - 130, 80);
            } else {
                // Regular Combat Room: 3 đến 4 mục tiêu phân bố an toàn
                spawnEnemySafely(arena, cx, cy, 75);
                spawnEnemySafely(arena, cx + 140, cy - 80, 75);
                spawnEnemySafely(arena, cx + 140, cy + 80, 75);
                if (arena.getWidthInTiles() >= 13) {
                    spawnEnemySafely(arena, cx - 140, cy, 75);
                }
            }
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
        // --- Handle Extraction Victory Transition Sequence ---
        // WHY: Freezes all combat updates and presents the black debriefing screen
        if (isExtracting) {
            extractTransitionTimer += deltaTime;

            // Allow skipping black screen by pressing ENTER, SPACE, ESC, or F
            if (extractTransitionTimer >= EXTRACT_DURATION
                    || keyH.isKeyPressedOnce(KeyEvent.VK_ENTER)
                    || keyH.isKeyPressedOnce(KeyEvent.VK_SPACE)
                    || keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)
                    || keyH.isKeyPressedOnce(KeyEvent.VK_F)) {
                gsm.setState(GameStateManager.StateType.MENU);
            }
            return;
        }

        // --- Handle Tactical Backpack (TAB / B) ---
        // WHY: Cho phép xem kho trang bị và hoán đổi vị trí vũ khí 1 và 2 trong không gian an toàn
        if (keyH.isKeyPressedOnce(KeyEvent.VK_TAB) || keyH.isKeyPressedOnce(KeyEvent.VK_B)) {
            isBackpackOpen = !isBackpackOpen;
        }

        if (isBackpackOpen) {
            boolean closeRequested = backpackUI.update(keyH, mouseH, weaponManager, deltaTime);
            if (closeRequested) {
                isBackpackOpen = false;
            }
            return; // Đóng băng vòng lặp chiến đấu khi đang xem Balo
        }

        // Handle pause request: returns to Main Menu
        if (keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
            gsm.setState(GameStateManager.StateType.MENU);
            return;
        }

        if (bannerTimer > 0) {
            bannerTimer -= deltaTime;
        }

        // --- 1. Weapon Hotkey Switching & Reloading ---
        if (keyH.isKeyPressedOnce(KeyEvent.VK_1) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD1)) {
            weaponManager.selectSlot(1); // Trang bị Assault Rifle (Súng trường tấn công AR)
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_2) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD2)) {
            weaponManager.selectSlot(2); // Trang bị Shotgun (Súng săn cự ly gần)
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_3) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD3)) {
            weaponManager.selectSlot(3); // Trang bị Tactical Pistol (Súng lục)
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_4) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD4)) {
            weaponManager.selectSlot(4); // Trang bị Combat Knife (Dao găm chiến thuật)
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

        // --- 4. Multi-Chamber Combat Lockdown & Detection ---
        if (activeCombatRoom == null) {
            for (Room cr : dungeonMap.getCombatRooms()) {
                if (cr.getStatus() == Room.RoomStatus.UNVISITED
                        && cr.hasEntityEntered(playerX, playerY, playerSize, playerSize)) {

                    activeCombatRoom = cr;

                    double minInteriorX = (cr.getGridX() + 1.0) * GameConfig.TILE_SIZE;
                    double maxInteriorX = (cr.getGridX() + cr.getWidthInTiles() - 1.0) * GameConfig.TILE_SIZE;
                    double minInteriorY = (cr.getGridY() + 1.0) * GameConfig.TILE_SIZE;
                    double maxInteriorY = (cr.getGridY() + cr.getHeightInTiles() - 1.0) * GameConfig.TILE_SIZE;

                    if (playerX < minInteriorX) playerX = minInteriorX;
                    if (playerX + playerSize > maxInteriorX) playerX = maxInteriorX - playerSize;
                    if (playerY < minInteriorY) playerY = minInteriorY;
                    if (playerY + playerSize > maxInteriorY) playerY = maxInteriorY - playerSize;

                    cr.lockdown();
                    spawnTargetsForRoom(cr);

                    int idx = dungeonMap.getCombatRooms().indexOf(cr) + 1;
                    int total = dungeonMap.getCombatRooms().size();
                    showBanner(String.format("! PHÒNG CHIẾN ĐẤU %d/%d PHONG TỎA - TIÊU DIỆT KẺ ĐỊCH !", idx, total),
                            new Color(255, 60, 60), 3.5);
                    break;
                }
            }
        }

        // --- 5. Smooth Camera Tracking ---
        camera.update(playerCenterX, playerCenterY);

        // --- 6. 360-Degree Aim Angle Calculation ---
        double mouseWorldX = camera.screenToWorldX(mouseH.getScreenX());
        double mouseWorldY = camera.screenToWorldY(mouseH.getScreenY());
        aimAngle = Math.atan2(mouseWorldY - playerCenterY, mouseWorldX - playerCenterX);

        // --- 7. Weapon Attack Execution ---
        // Súng tự động (AR) cho phép giữ chuột liên thanh (isLeftPressed)
        // Súng bán tự động (Pistol/Shotgun/Dao) yêu cầu bấm chuột từng phát một (isLeftClickedOnce)
        Weapon currentWeapon = weaponManager.getActiveWeapon();
        boolean shouldTriggerAttack = false;
        if (currentWeapon != null) {
            if (currentWeapon.isAutomatic()) {
                shouldTriggerAttack = mouseH.isLeftPressed();
            } else {
                shouldTriggerAttack = mouseH.isLeftClickedOnce();
            }
        }
        if (shouldTriggerAttack) {
            weaponManager.attack(playerCenterX, playerCenterY, aimAngle, projectileManager);
        }

        // --- 8. Advance Weapons & Projectiles ---
        weaponManager.update(deltaTime);
        projectileManager.update(deltaTime, dungeonMap);

        // --- 9. Check Projectile and Melee Hits on Hostile Targets ---
        updateCombatDamageResolution();

        // --- 10. Check Room Clear Condition and Unlock Doors ---
        if (activeCombatRoom != null && activeCombatRoom.isLocked() && combatTargets.isEmpty()) {
            activeCombatRoom.unlock();

            long clearedCount = dungeonMap.getCombatRooms().stream().filter(Room::isCleared).count();
            int totalCount = dungeonMap.getCombatRooms().size();

            // Rơi ra hòm tiếp tế chứa ngẫu nhiên vũ khí chính tại phòng chiến đấu vừa dọn sạch.
            // Ở Stage 3 (Boss Arena): Đặt hòm ở phía Bắc của tâm phòng (cách bệ extract 180px)
            // để hoàn toàn tách bạch, tránh chồng đè vị trí và xung đột tương tác phím [F] với bệ sơ tán.
            double chestX = activeCombatRoom.getCenterWorldX() - 19;
            double chestY = (currentStage == 3)
                    ? activeCombatRoom.getCenterWorldY() - 180
                    : activeCombatRoom.getCenterWorldY() - 19;
            weaponChests.add(new WeaponChest(chestX, chestY));

            if (currentStage == 3) {
                // Stage 3 Boss Arena: Kích hoạt bệ sơ tán ngay tại tâm phòng combat!
                dungeonMap.spawnExtractZoneInRoom(activeCombatRoom);
                showBanner("★ ĐÃ DỌN SẠCH BOSS ARENA - BỆ SƠ TÁN ĐÃ MỞ TẠI TÂM PHÒNG ★",
                        new Color(60, 240, 140), 5.0);
            } else {
                if (clearedCount >= totalCount) {
                    showBanner("★ TOÀN BỘ PHÒNG ĐÃ DỌN SẠCH - CỬA SƠ TÁN ĐÃ MỞ ★",
                            new Color(60, 240, 140), 4.0);
                } else {
                    showBanner(String.format("★ PHÒNG %d/%d DỌN SẠCH: CỬA TIẾP TỤC ĐÃ MỞ ★", clearedCount, totalCount),
                            new Color(60, 220, 120), 3.5);
                }
            }

            activeCombatRoom = null;
        }

        // --- 11. Handle Weapon Chests Auto-Open & Pickup Interaction [F] ---
        boolean chestInteracted = false;
        for (WeaponChest chest : weaponChests) {
            // Hòm tự động mở khi người chơi đi lại gần (bán kính ~58px) và lộ diện khẩu súng
            chest.update(deltaTime, playerX, playerY, playerSize);

            // Khi hòm đã mở và súng chưa được nhặt, bấm phím tương tác chính [F] để nhặt súng vào trang bị
            if (chest.isPlayerInRange(playerX, playerY, playerSize) && chest.isOpened() && !chest.isCollected()) {
                if (keyH.isKeyPressedOnce(KeyEvent.VK_F)) {
                    Weapon dropped = chest.collect();
                    if (dropped != null) {
                        weaponManager.equipWeapon(dropped);
                        showBanner("★ ĐÃ TRANG BỊ: " + dropped.getName().toUpperCase() + " (SLOT " + dropped.getSlotIndex() + ") ★",
                                new Color(255, 215, 60), 3.5);
                        chestInteracted = true;
                    }
                }
            }
        }

        // --- 12. Check Extraction Zone Interaction [F] ---
        // WHY: Intersecting the 4x4 extract pad prompts the operative to press [F] to extract or advance stage
        isStandingOnExtract = dungeonMap.isIntersectsExtractZone(playerX, playerY, playerSize, playerSize);
        if (!chestInteracted && isStandingOnExtract && keyH.isKeyPressedOnce(KeyEvent.VK_F)) {
            if (currentStage < MAX_STAGES) {
                advanceToNextStage();
            } else {
                isExtracting = true;
                extractTransitionTimer = 0.0;
            }
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

            // Gây sát thương và phá hủy ô cover tạm thời trong tầm chém dao găm
            java.awt.geom.Rectangle2D slashBounds = slash.getBounds();
            dungeonMap.damageDestructibleCoverInArea(
                    slashBounds.getX(), slashBounds.getY(),
                    slashBounds.getWidth(), slashBounds.getHeight(),
                    slash.getDamage());
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

        // 3.5 Render Danh sách Hòm tiếp tế vũ khí (Weapon Chests)
        for (WeaponChest chest : weaponChests) {
            boolean inRange = chest.isPlayerInRange(playerX, playerY, playerSize);
            chest.render(g, camera, inRange);
        }

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

        // 9. Floating Extract Interaction Prompt if standing on extract pad
        if (isStandingOnExtract && !isExtracting) {
            renderExtractPrompt(g, screenPX, screenPY);
        }

        // 10. Extraction Victory Black Screen Overlay (Report Stage 0-1 Completed)
        if (isExtracting) {
            renderExtractionVictoryScreen(g);
        }

        // 11. Giao diện Balo chiến thuật (TAB Overlay)
        if (isBackpackOpen) {
            backpackUI.render(g, weaponManager, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        }
    }

    /**
     * Displays current operative location, active chamber title, remaining targets, and key tips.
     */
    private void renderSectorHUD(Graphics2D g, double pcx, double pcy) {
        g.setColor(new Color(15, 20, 30, 200));
        g.fillRoundRect(20, 20, 480, 88, 12, 12);
        g.setColor(new Color(60, 80, 110));
        g.drawRoundRect(20, 20, 480, 88, 12, 12);

        String currentRoomName = "Connecting Corridor";
        for (Room r : dungeonMap.getRooms()) {
            if (r.containsWorldPoint(pcx, pcy)) {
                currentRoomName = r.getName();
                break;
            }
        }

        long clearedRooms = dungeonMap.getCombatRooms().stream().filter(Room::isCleared).count();
        int totalRooms = dungeonMap.getCombatRooms().size();

        String stageLabel = currentStage == 3 ? "STAGE 3/3 [BOSS ARENA]" : String.format("STAGE %d/%d", currentStage, MAX_STAGES);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        if (activeCombatRoom != null && activeCombatRoom.isLocked()) {
            g.setColor(new Color(255, 60, 60));
            g.drawString(String.format("BIOME 1 | %s | %s [PHONG TỎA: %d MỤC TIÊU]",
                    stageLabel, activeCombatRoom.getName().toUpperCase(), combatTargets.size()), 35, 45);
        } else {
            g.setColor(new Color(255, 200, 50));
            g.drawString(String.format("BIOME 1 | %s | %s [TIẾN ĐỘ: %d/%d PHÒNG]",
                    stageLabel, currentRoomName.toUpperCase(), clearedRooms, totalRooms), 35, 45);
        }

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(200, 215, 235));
        g.drawString(String.format("Operative: (%.0f, %.0f) | Bullets: %d | Hostiles: %d",
                pcx, pcy, projectileManager.getCount(), combatTargets.size()), 35, 68);
        g.setColor(new Color(140, 160, 185));
        g.drawString("Di chuyển: [WASD] | [TAB] Balo | [F] Tương tác | Slots: [1] [2] [3] [4] [Q]", 35, 88);
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

    /**
     * Displays a tactical prompt prompting the operative to press [F] to extract.
     */
    private void renderExtractPrompt(Graphics2D g, int screenPX, int screenPY) {
        String prompt;
        if (currentStage == 1) {
            prompt = "[F] TIẾN VÀO STAGE 2";
        } else if (currentStage == 2) {
            prompt = "[F] TIẾN VÀO STAGE 3 (BOSS ARENA)";
        } else {
            prompt = "[F] SƠ TÁN (HOÀN THÀNH BIOME)";
        }
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(prompt);
        int pad = 12;

        int boxW = textW + pad * 2;
        int boxH = 32;
        int boxX = screenPX - boxW / 2;
        int boxY = screenPY - 50;

        // Background box with glowing border
        g.setColor(new Color(15, 25, 35, 230));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setColor(new Color(160, 80, 255)); // Purple portal glow
        g.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        // Keycap [F] indicator
        g.setColor(new Color(255, 220, 60));
        g.drawString("[F]", boxX + pad, boxY + 21);

        g.setColor(Color.WHITE);
        g.drawString(prompt.substring(3), boxX + pad + fm.stringWidth("[F] "), boxY + 21);
    }

    /**
     * Renders a solid black screen with tactical debriefing reporting Stage 0-1 completion.
     */
    private void renderExtractionVictoryScreen(Graphics2D g) {
        int width = GameConfig.SCREEN_WIDTH;
        int height = GameConfig.SCREEN_HEIGHT;

        // 1. Solid Black Screen
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        // 2. Cyber grid decorative border
        g.setColor(new Color(30, 45, 60));
        g.drawRect(40, 40, width - 80, height - 80);
        g.drawRect(44, 44, width - 88, height - 88);

        // Corner accents
        g.setColor(new Color(60, 220, 160));
        int cSize = 24;
        g.drawLine(40, 40, 40 + cSize, 40);
        g.drawLine(40, 40, 40, 40 + cSize);
        g.drawLine(width - 40, 40, width - 40 - cSize, 40);
        g.drawLine(width - 40, 40, width - 40, 40 + cSize);
        g.drawLine(40, height - 40, 40 + cSize, height - 40);
        g.drawLine(40, height - 40, 40, height - 40 - cSize);
        g.drawLine(width - 40, height - 40, width - 40 - cSize, height - 40);
        g.drawLine(width - 40, height - 40, width - 40, height - 40 - cSize);

        // 3. Victory Title: ★ BIOME 1 HOÀN THÀNH ★
        g.setFont(new Font("SansSerif", Font.BOLD, 42));
        FontMetrics fmTitle = g.getFontMetrics();
        String title = "★ BIOME 1 HOÀN THÀNH ★";
        int titleX = (width - fmTitle.stringWidth(title)) / 2;
        int titleY = 210;

        g.setColor(new Color(20, 60, 40));
        g.drawString(title, titleX + 2, titleY + 2);
        g.setColor(new Color(60, 240, 140));
        g.drawString(title, titleX, titleY);

        // Subtitle
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics fmSub = g.getFontMetrics();
        String subtitle = "CHIẾN DỊCH TOÀN THẮNG - SƠ TÁN THÀNH CÔNG";
        int subX = (width - fmSub.stringWidth(subtitle)) / 2;
        g.setColor(new Color(255, 215, 60));
        g.drawString(subtitle, subX, 260);

        // 4. Mission Debriefing Tactical Card
        int cardW = 660;
        int cardH = 180;
        int cardX = (width - cardW) / 2;
        int cardY = 310;
        g.setColor(new Color(15, 20, 30, 220));
        g.fillRoundRect(cardX, cardY, cardW, cardH, 12, 12);
        g.setColor(new Color(60, 80, 110));
        g.drawRoundRect(cardX, cardY, cardW, cardH, 12, 12);

        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.setColor(new Color(200, 220, 245));
        g.drawString("• Khu vực: Biome 1 (Alpha Sector) - Hoàn thành xuất sắc 3/3 Stages", cardX + 30, cardY + 45);
        g.drawString("• Stage 1 & 2: Dọn sạch toàn bộ các phòng chiến đấu liên kết", cardX + 30, cardY + 80);
        g.drawString("• Stage 3: Chinh phục Grand Boss Arena & kích hoạt sơ tán", cardX + 30, cardY + 115);
        g.drawString("• Trang bị & Balo: Giữ nguyên toàn bộ vũ khí và chiến lợi phẩm", cardX + 30, cardY + 150);

        // 5. User prompt at bottom
        g.setFont(new Font("SansSerif", Font.ITALIC, 16));
        FontMetrics fmPrompt = g.getFontMetrics();
        String prompt = "[ Nhấn ENTER hoặc F để quay về Menu ngay ]";
        int promptX = (width - fmPrompt.stringWidth(prompt)) / 2;
        g.setColor(new Color(160, 180, 210));
        g.drawString(prompt, promptX, 550);
    }

    public boolean isStandingOnExtract() {
        return isStandingOnExtract;
    }

    public boolean isExtracting() {
        return isExtracting;
    }

    public void setPlayerPosition(double x, double y) {
        this.playerX = x;
        this.playerY = y;
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

    public WeaponChest getWeaponChest() {
        return weaponChests.isEmpty() ? null : weaponChests.get(0);
    }

    public void setWeaponChest(WeaponChest chest) {
        weaponChests.clear();
        if (chest != null) {
            weaponChests.add(chest);
        }
    }

    public List<WeaponChest> getWeaponChests() {
        return weaponChests;
    }

    public Room getActiveCombatRoom() {
        return activeCombatRoom;
    }

    public boolean isBackpackOpen() {
        return isBackpackOpen;
    }

    public void setBackpackOpen(boolean backpackOpen) {
        this.isBackpackOpen = backpackOpen;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(int stage) {
        this.currentStage = stage;
    }
}
