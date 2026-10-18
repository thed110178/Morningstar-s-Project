package com.groupproject.game.state;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.dungeon.Room;
import com.groupproject.game.entity.enemy.TargetDummy;
import com.groupproject.game.entity.item.AmmoPickup;
import com.groupproject.game.entity.item.AmmoType;
import com.groupproject.game.entity.item.HealthPickup;
import com.groupproject.game.entity.item.WeaponChest;
import com.groupproject.game.entity.projectile.Projectile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.entity.shop.ShopItem;
import com.groupproject.game.entity.weapon.AssaultRifle;
import com.groupproject.game.entity.weapon.MeleeSlash;
import com.groupproject.game.entity.weapon.SMG;
import com.groupproject.game.entity.weapon.Shotgun;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.entity.weapon.WeaponManager;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import com.groupproject.game.ui.BackpackUI;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import com.groupproject.game.dungeon.Tile;
import com.groupproject.game.dungeon.TileType;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

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
    private TargetDummy bossDummy = null;

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
    // Danh sách các hộp đạn chiến thuật rơi trên sàn (Ammo Pickups)
    private final List<AmmoPickup> ammoPickups = new CopyOnWriteArrayList<>();
    private Room activeCombatRoom = null;
    private boolean stage1FreeWeaponGranted = false; // Đảm bảo ở Stage 1 luôn có 1 cây súng free

    // Tiến trình Stage trong Biome (1 -> 2 -> 3 Boss Arena)
    private int currentStage = 1;
    public static final int MAX_STAGES = 3;

    // Hệ thống Sinh Lực (HP) của Chiến binh Operative
    private int playerMaxHealth = 100;
    private int playerCurrentHealth = 100;
    private double invulnerableTimer = 0.0; // Thời gian bất tử (i-frames: 0.8s) khi dính đòn
    private double screenShakeTimer = 0.0;  // Rung màn hình khi bị thương (0.2s)
    private boolean isPlayerDead = false;   // Trạng thái tử trận (HP = 0)
    private final List<HealthPickup> healthPickups = new CopyOnWriteArrayList<>();

    // Giao diện Balo chiến thuật (Tab)
    private final BackpackUI backpackUI = new BackpackUI();
    private boolean isBackpackOpen = false;

    // Hệ thống tiền tệ chiến thuật (Credits) và quầy giao dịch Shop
    private int playerCredits = 0;
    private final List<ShopItem> shopItems = new CopyOnWriteArrayList<>();

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
        this.playerCredits = 0;
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
        bossDummy = null;
        weaponChests.clear();
        ammoPickups.clear();
        healthPickups.clear();
        activeCombatRoom = null;

        this.playerMaxHealth = 100;
        this.playerCurrentHealth = 100;
        this.invulnerableTimer = 0.0;
        this.screenShakeTimer = 0.0;
        this.isPlayerDead = false;

        this.isStandingOnExtract = false;
        this.isExtracting = false;
        this.extractTransitionTimer = 0.0;
        this.isBackpackOpen = false;
        this.stage1FreeWeaponGranted = false;
        this.weaponManager.reset();

        populateShopItems();

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
        bossDummy = null;
        weaponChests.clear();
        ammoPickups.clear();
        healthPickups.clear();
        activeCombatRoom = null;
        isStandingOnExtract = false;
        invulnerableTimer = 0.0;
        screenShakeTimer = 0.0;

        populateShopItems();

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
     * Khởi tạo các quầy hàng buôn bán trong Room Shop nếu có.
     * Cân bằng kinh tế theo đúng quy luật thiết kế:
     * - Bàn nâng cấp súng 1 cấp: 80 Credits (nâng từ I -> II hoặc II -> III).
     * - Quầy mua súng Level III ngẫu nhiên: 135 Credits (80C < 135C < 160C).
     * - Quầy nạp đầy toàn bộ đạn dược: 70 Credits.
     * - Quầy hồi phục HP: Ẩn hoàn toàn (chưa show).
     */
    private void populateShopItems() {
        shopItems.clear();
        Room shop = dungeonMap.getShopRoom();
        if (shop == null) return;

        double cx = shop.getCenterWorldX();
        double cy = shop.getCenterWorldY();

        // 1. Quầy bán súng chính ngẫu nhiên: Level = currentStage + 2, tỉ lệ nhỏ (20%) là currentStage + 3
        int randChoice = ThreadLocalRandom.current().nextInt(3);
        Weapon randomWeapon;
        switch (randChoice) {
            case 0:
                randomWeapon = new AssaultRifle();
                break;
            case 1:
                randomWeapon = new Shotgun();
                break;
            case 2:
            default:
                randomWeapon = new SMG();
                break;
        }
        // Quy tắc: Súng bán ở shop = stage + 2 (tỉ lệ nhỏ 20% là stage + 3)
        int shopWeaponLevel = currentStage + 2;
        if (ThreadLocalRandom.current().nextInt(100) < 20) {
            shopWeaponLevel = currentStage + 3;
        }
        randomWeapon.configureLevel(shopWeaponLevel);
        // Định giá súng shop: Chênh 2 cấp so với stage rơi có giá 135 Credits; chênh 3 cấp có giá 170 Credits
        int weaponPrice = 65 + (shopWeaponLevel - currentStage) * 35;
        shopItems.add(ShopItem.createWeaponItem(cx - 110, cy, randomWeapon, weaponPrice));

        // 2. Bàn nâng cấp vũ khí (ngay giữa trung tâm phòng shop): Phí 80 Credits/cấp
        shopItems.add(ShopItem.createUpgradeStation(cx, cy, 80));

        // 3. Quầy tiếp tế (bên phải tâm phòng 110px):
        // QUY TẮC: Xuất hiện ngẫu nhiên giữa Hộp Tiếp Đạn và Hộp Cứu Thương, có thể thay thế nhau cho mỗi stage
        boolean spawnMedkit = ThreadLocalRandom.current().nextBoolean();
        if (spawnMedkit) {
            shopItems.add(ShopItem.createHealthItem(cx + 110, cy, 75));
        } else {
            shopItems.add(ShopItem.createAmmoItem(cx + 110, cy, 70));
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
    private boolean isSafeEnemySpawnPoint(Room arena, double x, double y, double entitySize, List<TargetDummy> currentList) {
        if (arena == null) return false;

        double half = entitySize / 2.0;
        // 1. Kiểm tra nằm trong phạm vi lòng phòng (cách biên tường ít nhất nửa kích thước + đệm an toàn)
        double margin = half + 4;
        double minX = arena.getGridX() * GameConfig.TILE_SIZE + margin;
        double maxX = (arena.getGridX() + arena.getWidthInTiles()) * GameConfig.TILE_SIZE - margin;
        double minY = arena.getGridY() * GameConfig.TILE_SIZE + margin;
        double maxY = (arena.getGridY() + arena.getHeightInTiles()) * GameConfig.TILE_SIZE - margin;

        if (x < minX || x > maxX || y < minY || y > maxY) {
            return false;
        }

        // 2. Bounding box quanh tâm (x, y) phải hoàn toàn walkable (không tường, không cover cứng, không cover tạm thời)
        double boxSize = entitySize + 4;
        if (!dungeonMap.isAreaWalkable(x - boxSize / 2.0, y - boxSize / 2.0, boxSize, boxSize)) {
            return false;
        }

        // 3. Không sinh đè lên người chơi (khoảng cách an toàn ít nhất half + 55px)
        double distToPlayer = Math.hypot(x - playerX, y - playerY);
        if (distToPlayer < (half + 55.0)) {
            return false;
        }

        // 4. Không đè lên các enemy khác đã được thêm vào phòng (khoảng cách an toàn)
        for (TargetDummy other : currentList) {
            double minDist = half + (other.getSize() / 2.0) + 15.0;
            double dist = Math.hypot(x - other.getX(), y - other.getY());
            if (dist < minDist) {
                return false;
            }
        }

        // 5. Không nằm quá gần các ô cửa của phòng (khoảng cách an toàn)
        double minDoorDist = half + 40.0;
        for (Tile door : arena.getDoorTiles()) {
            double doorCenterX = door.getWorldX() + GameConfig.TILE_SIZE / 2.0;
            double doorCenterY = door.getWorldY() + GameConfig.TILE_SIZE / 2.0;
            if (Math.hypot(x - doorCenterX, y - doorCenterY) < minDoorDist) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tìm vị trí an toàn nhất trong phòng để sinh kẻ địch, ưu tiên vị trí lý tưởng (preferredX, preferredY).
     * Nếu vị trí lý tưởng bị vướng cover hoặc vật cản, tự động quét bán kính xung quanh để tìm ô sàn trống.
     */
    private Point2D.Double findSafeSpawnLocation(Room arena, double preferredX, double preferredY, double entitySize, List<TargetDummy> currentList) {
        // Nếu vị trí mong muốn đã hoàn toàn an toàn, sử dụng ngay
        if (isSafeEnemySpawnPoint(arena, preferredX, preferredY, entitySize, currentList)) {
            return new Point2D.Double(preferredX, preferredY);
        }

        // Quét bán kính xung quanh vị trí mong muốn theo các vòng tròn bán kính tăng dần (spiral search)
        double[] radiuses = {24, 48, 72, 96, 120, 150, 180, 220};
        for (double r : radiuses) {
            for (int deg = 0; deg < 360; deg += 30) {
                double rad = Math.toRadians(deg);
                double testX = preferredX + r * Math.cos(rad);
                double testY = preferredY + r * Math.sin(rad);
                if (isSafeEnemySpawnPoint(arena, testX, testY, entitySize, currentList)) {
                    return new Point2D.Double(testX, testY);
                }
            }
        }

        // Nếu vẫn chưa tìm được qua spiral search, quét tất cả các ô FLOOR trong phòng
        int rx = arena.getGridX();
        int ry = arena.getGridY();
        int rw = arena.getWidthInTiles();
        int rh = arena.getHeightInTiles();

        for (int c = rx + 2; c < rx + rw - 2; c++) {
            for (int r = ry + 2; r < ry + rh - 2; r++) {
                Tile tile = dungeonMap.getTile(c, r);
                if (tile != null && tile.getType() == TileType.FLOOR) {
                    double testX = tile.getWorldX() + GameConfig.TILE_SIZE / 2.0;
                    double testY = tile.getWorldY() + GameConfig.TILE_SIZE / 2.0;
                    if (isSafeEnemySpawnPoint(arena, testX, testY, entitySize, currentList)) {
                        return new Point2D.Double(testX, testY);
                    }
                }
            }
        }

        // Fallback an toàn: Tâm phòng
        return new Point2D.Double(arena.getCenterWorldX(), arena.getCenterWorldY());
    }

    /**
     * Sinh một kẻ địch tiêu chuẩn (kích thước 30px) một cách an toàn vào danh sách.
     */
    private void spawnEnemySafely(Room arena, double preferredX, double preferredY, int health) {
        Point2D.Double safePos = findSafeSpawnLocation(arena, preferredX, preferredY, 30.0, combatTargets);
        combatTargets.add(new TargetDummy(safePos.x, safePos.y, health));
    }

    /**
     * Sinh Boss Dummy kích thước lớn (size 64px, thanh máu dày) an toàn vào danh sách và lưu tham chiếu boss.
     */
    private void spawnBossSafely(Room arena, double preferredX, double preferredY, int health, double size, String name) {
        Point2D.Double safePos = findSafeSpawnLocation(arena, preferredX, preferredY, size, combatTargets);
        TargetDummy boss = new TargetDummy(safePos.x, safePos.y, health, size, true, name);
        this.bossDummy = boss;
        combatTargets.add(boss);
    }

    /**
     * Sinh các mục tiêu chiến đấu an toàn cho từng phòng combat khi bị phong tỏa.
     */
    private void spawnTargetsForRoom(Room arena) {
        combatTargets.clear();
        this.bossDummy = null;
        if (arena != null) {
            double cx = arena.getCenterWorldX();
            double cy = arena.getCenterWorldY();

            if (currentStage == 3) {
                // Stage 3 Grand Boss Arena: Titan Mecha Dummy khổng lồ (size 64px, 1000 HP) và 8 minion hộ tống
                spawnBossSafely(arena, cx, cy, 1000, 64.0, "TITAN MECHA DUMMY [BOSS]");
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
        // --- Handle Player Death (Game Over Sequence) ---
        if (isPlayerDead) {
            if (bannerTimer > 0) {
                bannerTimer -= deltaTime;
            }
            if (keyH.isKeyPressedOnce(KeyEvent.VK_ENTER) || keyH.isKeyPressedOnce(KeyEvent.VK_R)) {
                init(); // Tái xuất phát run mới từ đầu
            } else if (keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
                gsm.setState(GameStateManager.StateType.MENU);
            }
            return;
        }

        // Đếm ngược thời gian bất tử i-frame và rung camera
        if (invulnerableTimer > 0) {
            invulnerableTimer -= deltaTime;
        }
        if (screenShakeTimer > 0) {
            screenShakeTimer -= deltaTime;
        }

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
            return;
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
                    if (currentStage == 3) {
                        showBanner("☠ CẢNH BÁO: TITAN MECHA DUMMY [BOSS] XUẤT HIỆN! ☠",
                                new Color(255, 60, 60), 4.0);
                    } else {
                        showBanner(String.format("! PHÒNG CHIẾN ĐẤU %d/%d PHONG TỎA - TIÊU DIỆT KẺ ĐỊCH !", idx, total),
                                new Color(255, 60, 60), 3.5);
                    }
                    break;
                }
            }
        }

        // --- 5. Smooth Camera Tracking with Screen Shake ---
        double camCX = playerCenterX;
        double camCY = playerCenterY;
        if (screenShakeTimer > 0) {
            camCX += ThreadLocalRandom.current().nextDouble(-3.0, 3.0);
            camCY += ThreadLocalRandom.current().nextDouble(-3.0, 3.0);
        }
        camera.update(camCX, camCY);

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
            int clearReward = (currentStage == 3) ? 25 : 15; // Giảm tiền thưởng khi dọn sạch phòng chiến đấu
            addCredits(clearReward);

            long clearedCount = dungeonMap.getCombatRooms().stream().filter(Room::isCleared).count();
            int totalCount = dungeonMap.getCombatRooms().size();

            // Rơi ra hòm tiếp tế chiến lợi phẩm tại phòng chiến đấu vừa dọn sạch:
            // QUY TẮC PHẦN THƯỞNG:
            // 1. Ở Stage 1: Đảm bảo phòng combat đầu tiên dọn sạch luôn cho 1 cây súng chính Level 1 free.
            // 2. Sau khi đã nhận cây súng free (và ở các stage khác): Hòm trao thưởng theo tỉ lệ (Tiền ra nhiều hơn súng ~75%).
            boolean giveGuaranteedWeapon = (currentStage == 1 && !stage1FreeWeaponGranted);
            if (giveGuaranteedWeapon) {
                stage1FreeWeaponGranted = true;
            }

            double chestCenterX = activeCombatRoom.getCenterWorldX();
            double chestCenterY = (currentStage == 3)
                    ? activeCombatRoom.getCenterWorldY() - 161
                    : activeCombatRoom.getCenterWorldY();

            double chestX = chestCenterX - 19;
            double chestY = chestCenterY - 19;
            weaponChests.add(new WeaponChest(chestX, chestY, giveGuaranteedWeapon, currentStage));

            // Rơi 2 đơn vị stack của 2 loại đạn ngẫu nhiên đối xứng 2 bên (hòm thưởng ở chính giữa)
            spawnClearRoomAmmoStacks(chestCenterX, chestCenterY);

            // Tỉ lệ rất nhỏ (~15%) mở ra thêm vật phẩm hồi phục khi clear phòng
            if (ThreadLocalRandom.current().nextInt(100) < 15) {
                healthPickups.add(new HealthPickup(chestCenterX, chestCenterY + 58.0, HealthPickup.HealthSize.SMALL));
            }

            if (currentStage == 3) {
                // Stage 3 Boss Arena: Kích hoạt bệ sơ tán ngay tại tâm phòng combat!
                dungeonMap.spawnExtractZoneInRoom(activeCombatRoom);
                showBanner("★ ĐÃ DỌN SẠCH BOSS ARENA - BỆ SƠ TÁN ĐÃ MỞ TẠI TÂM PHÒNG (+" + clearReward + " CREDITS) ★",
                        new Color(60, 240, 140), 5.0);
            } else {
                if (clearedCount >= totalCount) {
                    showBanner("★ TOÀN BỘ PHÒNG ĐÃ DỌN SẠCH - CỬA SƠ TÁN ĐÃ MỞ (+" + clearReward + " CREDITS) ★",
                            new Color(60, 240, 140), 4.0);
                } else {
                    showBanner(String.format("★ PHÒNG %d/%d DỌN SẠCH: CỬA TIẾP TỤC ĐÃ MỞ (+%d CREDITS) ★", clearedCount, totalCount, clearReward),
                            new Color(60, 220, 120), 3.5);
                }
            }

            activeCombatRoom = null;
        }

        // --- 11. Handle Weapon Chests Auto-Open & Pickup Interaction [F] ---
        boolean chestInteracted = false;
        for (WeaponChest chest : weaponChests) {
            // Hòm tự động mở khi người chơi đi lại gần (bán kính ~58px) và lộ diện chiến lợi phẩm (Tiền hoặc Súng)
            chest.update(deltaTime, playerX, playerY, playerSize);

            // Khi hòm đã mở và chưa thu thập, bấm phím tương tác chính [F] để nhận thưởng
            if (chest.isPlayerInRange(playerX, playerY, playerSize) && chest.isOpened() && !chest.isCollected()) {
                if (keyH.isKeyPressedOnce(KeyEvent.VK_F)) {
                    boolean collected = chest.collectReward(this);
                    if (collected) {
                        chestInteracted = true;
                    }
                }
            }
        }

        // --- 11.2. Handle Ammo Pickups Proximity Auto-Collection ---
        for (AmmoPickup pickup : ammoPickups) {
            pickup.update(deltaTime, playerX, playerY, playerSize, weaponManager.getAmmoInventory(), this);
        }
        ammoPickups.removeIf(AmmoPickup::isCollected);

        // --- 11.3. Handle Health Pickups Manual Interaction [F] ---
        // QUY TẮC: Người chơi phải bấm phím [F] để sử dụng, tuyệt đối không tự động nhặt
        boolean healthInteracted = false;
        for (HealthPickup hp : healthPickups) {
            hp.update(deltaTime);
            if (!healthInteracted && hp.isPlayerInRange(playerX, playerY, playerSize)) {
                if (keyH.isKeyPressedOnce(KeyEvent.VK_F)) {
                    boolean used = hp.interact(this);
                    if (used) {
                        healthInteracted = true;
                        break;
                    }
                }
            }
        }
        healthPickups.removeIf(HealthPickup::isUsed);

        // --- 11.5. Handle Shop Items & Purchase Interaction [F] ---
        boolean shopInteracted = false;
        for (ShopItem item : shopItems) {
            item.update(deltaTime);
            if (item.isPlayerInRange(playerX, playerY, playerSize) && !item.isPurchased()) {
                if (keyH.isKeyPressedOnce(KeyEvent.VK_F)) {
                    boolean bought = item.purchase(this);
                    if (bought) {
                        shopInteracted = true;
                        break;
                    }
                }
            }
        }

        // --- 12. Check Extraction Zone Interaction [F] ---
        // WHY: Intersecting the 4x4 extract pad prompts the operative to press [F] to extract or advance stage
        isStandingOnExtract = dungeonMap.isIntersectsExtractZone(playerX, playerY, playerSize, playerSize);
        if (!chestInteracted && !shopInteracted && !healthInteracted && isStandingOnExtract && keyH.isKeyPressedOnce(KeyEvent.VK_F)) {
            if (currentStage < MAX_STAGES) {
                advanceToNextStage();
            } else {
                isExtracting = true;
                extractTransitionTimer = 0.0;
            }
        }
    }

    /**
     * Tỉ lệ nhỏ rơi tiền tệ Credits khi tiêu diệt một mục tiêu (quái).
     * Chỉ có ~20% cơ hội rơi tiền, mỗi lần rơi từ 3 đến 8 Credits.
     */
    private void tryDropEnemyCredits() {
        if (ThreadLocalRandom.current().nextInt(100) < 20) {
            int dropped = ThreadLocalRandom.current().nextInt(3, 9);
            addCredits(dropped);
        }
    }

    /**
     * Tỉ lệ nhỏ (~35%) rơi một lượng đạn nhỏ khi tiêu diệt một mục tiêu (quái).
     * Ngẫu nhiên 1 trong 3 loại đạn: AR (6-10 viên), SMG (8-14 viên), Shotgun (2-4 viên).
     */
    private void tryDropEnemyAmmo(double x, double y) {
        if (ThreadLocalRandom.current().nextInt(100) < 35) {
            AmmoType[] types = AmmoType.values();
            AmmoType chosen = types[ThreadLocalRandom.current().nextInt(types.length)];
            int amount = switch (chosen) {
                case AR -> ThreadLocalRandom.current().nextInt(6, 11);
                case SMG -> ThreadLocalRandom.current().nextInt(8, 15);
                case SHOTGUN -> ThreadLocalRandom.current().nextInt(2, 5);
            };
            ammoPickups.add(new AmmoPickup(x, y, chosen, amount));
        }
    }

    /**
     * Rơi 2 đơn vị stack của 2 loại đạn ngẫu nhiên (có thể trùng lặp) khi clear phòng chiến đấu.
     * Hòm thưởng nằm ở chính giữa, 2 stack đạn được bố trí đối xứng hoàn hảo 2 bên (Trái & Phải).
     * Định mức mỗi stack chuẩn: AR = 30 viên; SMG = 35 viên; Shotgun = 10 viên.
     */
    public void spawnClearRoomAmmoStacks(double centerX, double centerY) {
        AmmoType[] types = AmmoType.values();
        AmmoType type1 = types[ThreadLocalRandom.current().nextInt(types.length)];
        AmmoType type2 = types[ThreadLocalRandom.current().nextInt(types.length)];

        // Bố trí đối xứng: Hòm thưởng nằm ở chính giữa (centerX), 2 stack đạn nằm cân xứng 2 bên
        double spacing = 58.0; // Khoảng cách cân xứng từ tâm hòm sang 2 bên
        double box1X = centerX - spacing;
        double box1Y = centerY;
        double box2X = centerX + spacing;
        double box2Y = centerY;

        ammoPickups.add(new AmmoPickup(box1X, box1Y, type1, type1.getStackAmount()));
        ammoPickups.add(new AmmoPickup(box2X, box2Y, type2, type2.getStackAmount()));
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
                    if (!dummy.isAlive()) {
                        onEnemyEliminated(dummy);
                    }
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
                    if (!dummy.isAlive()) {
                        onEnemyEliminated(dummy);
                    }
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
     * Xử lý phần thưởng và phản hồi chiến thuật khi một mục tiêu bị tiêu diệt.
     * Khi Boss Dummy bị hạ gục: Thưởng lớn +50 Credits, rơi 3 stack đạn các loại, và hiển thị banner chiến tích.
     */
    private void onEnemyEliminated(TargetDummy dummy) {
        if (dummy.isBoss()) {
            addCredits(50);
            showBanner("☠ BOSS TIÊU DIỆT: TITAN MECHA DUMMY ĐÃ BỊ HẠ GỤC! (+50 CREDITS) ☠",
                    new Color(255, 215, 60), 4.0);
            ammoPickups.add(new AmmoPickup(dummy.getX() - 36, dummy.getY(), AmmoType.AR, 30));
            ammoPickups.add(new AmmoPickup(dummy.getX() + 36, dummy.getY(), AmmoType.SMG, 35));
            ammoPickups.add(new AmmoPickup(dummy.getX(), dummy.getY() + 36, AmmoType.SHOTGUN, 10));
            healthPickups.add(new HealthPickup(dummy.getX(), dummy.getY() - 36, HealthPickup.HealthSize.LARGE));
        } else {
            tryDropEnemyCredits(); // Tỉ lệ nhỏ rơi tiền khi tiêu diệt mục tiêu
            tryDropEnemyAmmo(dummy.getX(), dummy.getY()); // Tỉ lệ nhỏ rơi hộp đạn khi tiêu diệt mục tiêu
        }
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

        // 3.3 Render Danh sách Hộp Cứu Thương rơi trên sàn (Health Pickups)
        for (HealthPickup hp : healthPickups) {
            boolean inRange = hp.isPlayerInRange(playerX, playerY, playerSize);
            hp.render(g, camera, inRange);
        }

        // 3.4 Render Danh sách Hộp đạn chiến thuật rơi trên sàn (Ammo Pickups)
        for (AmmoPickup pickup : ammoPickups) {
            pickup.render(g, camera);
        }

        // 3.5 Render Danh sách Hòm tiếp tế vũ khí (Weapon Chests)
        for (WeaponChest chest : weaponChests) {
            boolean inRange = chest.isPlayerInRange(playerX, playerY, playerSize);
            chest.render(g, camera, inRange);
        }

        // 3.6 Render Danh sách Quầy hàng Shop (Tactical Field Shop)
        for (ShopItem item : shopItems) {
            boolean inRange = item.isPlayerInRange(playerX, playerY, playerSize);
            item.render(g, camera, inRange);
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
        // Khi bất tử (invulnerableTimer > 0): Nhấp nháy chớp đỏ / trong suốt phản hồi lực va chạm
        if (invulnerableTimer > 0 && ((int) (invulnerableTimer * 18)) % 2 == 0) {
            g.setColor(new Color(255, 90, 90, 180));
        } else {
            g.setColor(new Color(40, 180, 220));
        }
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

        // 6. Render Top Sector Information HUD, Player Health Bar HUD, Credits HUD, and Boss Health Bar
        renderSectorHUD(g, playerCenterX, playerCenterY);
        renderPlayerHealthHUD(g);
        renderCreditsHUD(g);
        renderBossHealthBar(g);

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

        // 10.4 Cảnh báo viền đỏ nguy hiểm khi máu thấp (<= 30%)
        if (playerCurrentHealth <= 30 && !isPlayerDead) {
            renderLowHealthVignette(g);
        }

        // 10.6 Màn hình Game Over khi tử trận
        if (isPlayerDead) {
            renderGameOverScreen(g);
        }

        // 11. Giao diện Balo chiến thuật (TAB Overlay)
        if (isBackpackOpen) {
            backpackUI.render(g, weaponManager, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        }
    }

    /**
     * Displays current operative location, active chamber title, remaining targets, and key tips.
     * Thu gọn kích thước chiều ngang khi bước vào trận chiến Boss để nhường không gian cho thanh máu Boss.
     */
    private void renderSectorHUD(Graphics2D g, double pcx, double pcy) {
        boolean isBossFight = (bossDummy != null && bossDummy.isAlive());
        int hudW = isBossFight ? 330 : 480;

        g.setColor(new Color(15, 20, 30, 200));
        g.fillRoundRect(20, 20, hudW, 88, 12, 12);
        g.setColor(new Color(60, 80, 110));
        g.drawRoundRect(20, 20, hudW, 88, 12, 12);

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
        g.setFont(new Font("SansSerif", Font.BOLD, isBossFight ? 13 : 15));
        if (activeCombatRoom != null && activeCombatRoom.isLocked()) {
            g.setColor(new Color(255, 60, 60));
            if (isBossFight) {
                g.drawString(String.format("BIOME 1 | %s [PHONG TỎA]", stageLabel), 35, 45);
            } else {
                g.drawString(String.format("BIOME 1 | %s | %s [PHONG TỎA: %d MỤC TIÊU]",
                        stageLabel, activeCombatRoom.getName().toUpperCase(), combatTargets.size()), 35, 45);
            }
        } else {
            g.setColor(new Color(255, 200, 50));
            g.drawString(String.format("BIOME 1 | %s | %s [%d/%d PHÒNG]",
                    stageLabel, currentRoomName.toUpperCase(), clearedRooms, totalRooms), 35, 45);
        }

        g.setFont(new Font("SansSerif", Font.PLAIN, isBossFight ? 12 : 13));
        g.setColor(new Color(200, 215, 235));
        if (isBossFight) {
            g.drawString(String.format("Operative: (%.0f, %.0f) | Hostiles: %d",
                    pcx, pcy, combatTargets.size()), 35, 68);
            g.setColor(new Color(140, 160, 185));
            g.drawString("WASD: Đi | TAB: Balo | F: Tương tác", 35, 88);
        } else {
            g.drawString(String.format("Operative: (%.0f, %.0f) | Bullets: %d | Hostiles: %d",
                    pcx, pcy, projectileManager.getCount(), combatTargets.size()), 35, 68);
            g.setColor(new Color(140, 160, 185));
            g.drawString("Di chuyển: [WASD] | [TAB] Balo | [F] Tương tác | Slots: [1] [2] [3] [4] [Q]", 35, 88);
        }
    }

    /**
     * Renders a large, imposing Boss Health Bar at the top-center of the screen during Boss encounters.
     * Includes golden boss header title, gradient crimson fill, segment ticks, and exact HP counter.
     */
    private void renderBossHealthBar(Graphics2D g) {
        if (bossDummy == null || !bossDummy.isAlive()) return;

        int barW = 520;
        int barH = 22;
        int barX = (GameConfig.SCREEN_WIDTH - barW) / 2;
        int barY = 44;

        // Container card frame behind the Boss Bar
        int cardPadX = 18;
        int cardPadY = 26;
        int cardX = barX - cardPadX;
        int cardY = barY - cardPadY;
        int cardW = barW + cardPadX * 2;
        int cardH = barH + cardPadY + 12;

        // Dark sci-fi backplate with gold trim
        g.setColor(new Color(12, 14, 22, 235));
        g.fillRoundRect(cardX, cardY, cardW, cardH, 12, 12);
        g.setColor(new Color(212, 160, 23, 200));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(cardX, cardY, cardW, cardH, 12, 12);
        g.setStroke(new BasicStroke(1.0f));

        // Decorative corner cyber notches
        g.setColor(new Color(255, 215, 60));
        g.fillRect(cardX + 2, cardY + 2, 8, 2);
        g.fillRect(cardX + 2, cardY + 2, 2, 8);
        g.fillRect(cardX + cardW - 10, cardY + 2, 8, 2);
        g.fillRect(cardX + cardW - 4, cardY + 2, 2, 8);

        // Header Title: Boss Name & Hazard Skull
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        String title = "☠ " + bossDummy.getName().toUpperCase() + " ☠";
        FontMetrics fm = g.getFontMetrics();
        int titleW = fm.stringWidth(title);
        g.setColor(new Color(255, 220, 80));
        g.drawString(title, (GameConfig.SCREEN_WIDTH - titleW) / 2, barY - 8);

        // Bar Track Background
        g.setColor(new Color(30, 25, 32));
        g.fillRoundRect(barX, barY, barW, barH, 6, 6);

        // Bar Fill (Gradient Crimson to Fiery Orange)
        float hpPercent = Math.max(0f, (float) bossDummy.getCurrentHealth() / bossDummy.getMaxHealth());
        int fillW = (int) (barW * hpPercent);

        if (fillW > 0) {
            GradientPaint hpGrad = new GradientPaint(
                    barX, barY, new Color(255, 55, 55),
                    barX + barW, barY, new Color(190, 15, 25)
            );
            Paint oldPaint = g.getPaint();
            g.setPaint(hpGrad);
            g.fillRoundRect(barX, barY, fillW, barH, 6, 6);
            g.setPaint(oldPaint);

            // Top highlight line for 3D metallic feel
            g.setColor(new Color(255, 140, 140, 180));
            g.fillRect(barX + 2, barY + 2, Math.max(0, fillW - 4), 3);
        }

        // Inner Segment Tick Marks (every 20%: 20%, 40%, 60%, 80%)
        g.setColor(new Color(20, 20, 28, 180));
        for (int i = 1; i <= 4; i++) {
            int tickX = barX + (barW * i) / 5;
            g.fillRect(tickX - 1, barY, 2, barH);
        }

        // Outer border
        g.setColor(new Color(255, 215, 60, 220));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(barX, barY, barW, barH, 6, 6);
        g.setStroke(new BasicStroke(1.0f));

        // Exact Health Text centered on the bar
        int percentInt = (int) (hpPercent * 100);
        String hpText = String.format("%d / %d HP (%d%%)", bossDummy.getCurrentHealth(), bossDummy.getMaxHealth(), percentInt);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics hpfm = g.getFontMetrics();
        int hpTextW = hpfm.stringWidth(hpText);
        int hpTextX = (GameConfig.SCREEN_WIDTH - hpTextW) / 2;
        int hpTextY = barY + 16;

        // Shadow text for high contrast readability
        g.setColor(Color.BLACK);
        g.drawString(hpText, hpTextX + 1, hpTextY + 1);
        g.setColor(Color.WHITE);
        g.drawString(hpText, hpTextX, hpTextY);
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
        int boxY = (bossDummy != null && bossDummy.isAlive()) ? 100 : 125;

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

    public List<AmmoPickup> getAmmoPickups() {
        return ammoPickups;
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

    /**
     * Hiển thị bảng HUD số dư tiền tệ Credits ở góc trên bên phải màn hình.
     */
    private void renderCreditsHUD(Graphics2D g) {
        int boxW = 160;
        int boxH = 36;
        int boxX = GameConfig.SCREEN_WIDTH - boxW - 20;
        int boxY = 20;

        g.setColor(new Color(15, 20, 30, 210));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setColor(new Color(255, 215, 60, 200));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        // Icon đồng tiền vàng
        g.setColor(new Color(255, 215, 60));
        g.fillOval(boxX + 10, boxY + 8, 20, 20);
        g.setColor(new Color(180, 140, 20));
        g.drawOval(boxX + 10, boxY + 8, 20, 20);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(new Color(30, 20, 5));
        g.drawString("C", boxX + 16, boxY + 23);

        // Chữ số Credits
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.setColor(new Color(255, 230, 90));
        g.drawString(playerCredits + " CREDITS", boxX + 38, boxY + 24);
    }

    public int getPlayerCredits() {
        return playerCredits;
    }

    public void setPlayerCredits(int credits) {
        this.playerCredits = Math.max(0, credits);
    }

    public void addCredits(int amount) {
        this.playerCredits += Math.max(0, amount);
    }

    public boolean deductCredits(int amount) {
        if (amount <= 0) return true;
        if (playerCredits >= amount) {
            playerCredits -= amount;
            return true;
        }
        return false;
    }

    public List<ShopItem> getShopItems() {
        return shopItems;
    }

    /**
     * Thả vũ khí xuống mặt đất dưới dạng hòm tiếp tế đã mở sẵn (phục vụ việc thay thế vũ khí phòng bấm nhầm).
     */
    public void dropWeaponOnGround(Weapon weapon, double x, double y) {
        if (weapon == null) return;
        WeaponChest chest = WeaponChest.createWeaponChest(x, y, weapon);
        chest.open();
        weaponChests.add(chest);
    }

    public double getPlayerX() {
        return playerX;
    }

    public double getPlayerY() {
        return playerY;
    }

    public void showTacticalBanner(String message, Color color, double duration) {
        showBanner(message, color, duration);
    }

    public TargetDummy getBossDummy() {
        return bossDummy;
    }

    /**
     * Helper hỗ trợ unit test khởi tạo Boss Dummy trong môi trường test mà không cần duyệt lockdown phòng.
     */
    public void spawnBossForTesting(double x, double y, int health, double size, String name) {
        TargetDummy boss = new TargetDummy(x, y, health, size, true, name);
        this.bossDummy = boss;
        this.combatTargets.add(boss);
    }

    /**
     * Hiển thị thanh máu của người chơi trên giao diện HUD màn hình (ngay dưới Sector HUD).
     * Tuyệt đối không hiển thị trên đầu người chơi theo đúng yêu cầu thiết kế.
     */
    private void renderPlayerHealthHUD(Graphics2D g) {
        int barX = 20;
        int barY = 114;
        int barW = 210;
        int barH = 22;

        // Khung nền xám đen viền kim loại công nghệ cao
        g.setColor(new Color(12, 16, 24, 230));
        g.fillRoundRect(barX, barY, barW, barH, 6, 6);
        g.setColor(new Color(55, 75, 105));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(barX, barY, barW, barH, 6, 6);
        g.setStroke(new BasicStroke(1.0f));

        // Thanh tiến trình máu
        float hpPercent = Math.max(0f, (float) playerCurrentHealth / playerMaxHealth);
        int fillW = (int) ((barW - 4) * hpPercent);

        Color hpColor;
        if (playerCurrentHealth > 50) {
            hpColor = new Color(50, 220, 100); // Xanh ngọc tươi
        } else if (playerCurrentHealth > 25) {
            hpColor = new Color(255, 180, 40); // Vàng hổ phách cảnh báo
        } else {
            hpColor = new Color(255, 55, 55);  // Đỏ tươi nguy cấp
        }

        if (fillW > 0) {
            g.setColor(hpColor);
            g.fillRoundRect(barX + 2, barY + 2, fillW, barH - 4, 4, 4);

            // Dải highlight 3D mép trên
            g.setColor(new Color(255, 255, 255, 80));
            g.fillRect(barX + 2, barY + 2, fillW, 3);
        }

        // Chữ số hiển thị máu rõ nét ở giữa thanh
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        String hpText = "✚ HP: " + playerCurrentHealth + " / " + playerMaxHealth;
        int textW = fm.stringWidth(hpText);
        int textX = barX + (barW - textW) / 2;
        int textY = barY + 16;

        // Shadow chữ tăng độ tương phản
        g.setColor(new Color(0, 0, 0, 220));
        g.drawString(hpText, textX + 1, textY + 1);
        g.setColor(Color.WHITE);
        g.drawString(hpText, textX, textY);
    }

    /**
     * Hiển thị hiệu ứng viền đỏ nhấp nháy quanh 4 cạnh màn hình khi máu xuống mức nguy cấp (<= 30%).
     */
    private void renderLowHealthVignette(Graphics2D g) {
        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.008) * 0.4 + 0.6);
        int alpha = (int) (65 * pulse);
        g.setColor(new Color(255, 0, 0, alpha));
        int thickness = 32;
        g.fillRect(0, 0, GameConfig.SCREEN_WIDTH, thickness);
        g.fillRect(0, GameConfig.SCREEN_HEIGHT - thickness, GameConfig.SCREEN_WIDTH, thickness);
        g.fillRect(0, 0, thickness, GameConfig.SCREEN_HEIGHT);
        g.fillRect(GameConfig.SCREEN_WIDTH - thickness, 0, thickness, GameConfig.SCREEN_HEIGHT);
    }

    /**
     * Hiển thị màn hình Game Over phong cách quân sự viễn tưởng khi chiến binh tử trận (HP = 0).
     */
    private void renderGameOverScreen(Graphics2D g) {
        // Phủ màn mờ đỏ đen kịch tính
        g.setColor(new Color(15, 5, 8, 235));
        g.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        int cx = GameConfig.SCREEN_WIDTH / 2;

        // Biểu tượng & Tiêu đề Tử trận
        g.setFont(new Font("SansSerif", Font.BOLD, 36));
        FontMetrics fm = g.getFontMetrics();
        String title = "☠ CHIẾN BINH TỬ TRẬN ☠";
        int titleW = fm.stringWidth(title);
        g.setColor(new Color(255, 60, 60));
        g.drawString(title, cx - titleW / 2, 220);

        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        fm = g.getFontMetrics();
        String sub = "OPERATIVE KIA - TÍN HIỆU SINH TỒN ĐÃ MẤT";
        g.setColor(new Color(255, 170, 170));
        g.drawString(sub, cx - fm.stringWidth(sub) / 2, 260);

        // Bảng thống kê chiến dịch
        int cardW = 440;
        int cardH = 150;
        int cardX = cx - cardW / 2;
        int cardY = 300;

        g.setColor(new Color(25, 18, 22, 220));
        g.fillRoundRect(cardX, cardY, cardW, cardH, 10, 10);
        g.setColor(new Color(255, 80, 80));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(cardX, cardY, cardW, cardH, 10, 10);
        g.setStroke(new BasicStroke(1.0f));

        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(new Color(220, 225, 235));
        g.drawString("• Giai đoạn chiến dịch: Stage " + currentStage + " / " + MAX_STAGES, cardX + 30, cardY + 40);
        g.drawString("• Ngân sách thu thập: " + playerCredits + " CREDITS", cardX + 30, cardY + 70);
        long cleared = dungeonMap.getCombatRooms().stream().filter(Room::isCleared).count();
        g.drawString("• Tiến độ dọn phòng: " + cleared + " phòng combat", cardX + 30, cardY + 100);
        g.drawString("• Trang bị hiện có: " + weaponManager.getWeaponSlots().size() + " vũ khí trong kho", cardX + 30, cardY + 130);

        // Hướng dẫn tương tác
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        fm = g.getFontMetrics();
        String prompt = "Bấm [ENTER] hoặc [R] để tái xuất phát | [ESC] Về Menu chính";
        g.setColor(new Color(255, 225, 75));
        g.drawString(prompt, cx - fm.stringWidth(prompt) / 2, 510);
    }

    /**
     * Gây sát thương cho chiến binh Operative.
     * Tự động kiểm tra thời gian bất tử (i-frames: 0.8s) để bảo vệ người chơi khỏi sát thương liên hoàn.
     */
    public void damagePlayer(int damage) {
        if (isPlayerDead || invulnerableTimer > 0 || damage <= 0) return;

        playerCurrentHealth = Math.max(0, playerCurrentHealth - damage);
        invulnerableTimer = 0.8; // 0.8 giây bất tử i-frame
        screenShakeTimer = 0.2;  // 0.2 giây rung màn hình

        if (playerCurrentHealth <= 0) {
            playerCurrentHealth = 0;
            isPlayerDead = true;
            showBanner("☠ CHIẾN BINH ĐÃ TỬ TRẬN (OPERATIVE KIA) ☠", new Color(255, 50, 50), 5.0);
        }
    }

    /**
     * Hồi phục máu cho chiến binh Operative, giới hạn tối đa bởi playerMaxHealth.
     */
    public void healPlayer(int amount) {
        if (isPlayerDead || amount <= 0) return;
        playerCurrentHealth = Math.min(playerMaxHealth, playerCurrentHealth + amount);
    }

    public int getPlayerMaxHealth() {
        return playerMaxHealth;
    }

    public void setPlayerMaxHealth(int maxHealth) {
        this.playerMaxHealth = Math.max(1, maxHealth);
    }

    public int getPlayerCurrentHealth() {
        return playerCurrentHealth;
    }

    public void setPlayerCurrentHealth(int health) {
        this.playerCurrentHealth = Math.max(0, Math.min(playerMaxHealth, health));
    }

    public boolean isPlayerDead() {
        return isPlayerDead;
    }

    public void setPlayerDead(boolean dead) {
        this.isPlayerDead = dead;
    }

    public double getInvulnerableTimer() {
        return invulnerableTimer;
    }

    public void setInvulnerableTimer(double timer) {
        this.invulnerableTimer = timer;
    }

    public List<HealthPickup> getHealthPickups() {
        return healthPickups;
    }
}
