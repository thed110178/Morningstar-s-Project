package com.groupproject.game.state;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.dungeon.Room;
import com.groupproject.game.entity.enemy.BossTank;
import com.groupproject.game.entity.enemy.EnemyCorpse;
import com.groupproject.game.entity.enemy.MeleeEnemy;
import com.groupproject.game.entity.enemy.RangedPistolEnemy;
import com.groupproject.game.entity.enemy.TargetDummy;
import com.groupproject.game.entity.item.AmmoPickup;
import com.groupproject.game.entity.item.AmmoType;
import com.groupproject.game.entity.item.HealthPickup;
import com.groupproject.game.entity.item.WeaponChest;
import com.groupproject.game.entity.projectile.BossCannonShell;
import com.groupproject.game.entity.projectile.CriticalHitVisual;
import com.groupproject.game.entity.projectile.ExplosionVisual;
import com.groupproject.game.entity.projectile.Projectile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.entity.shop.ShopItem;
import com.groupproject.game.entity.station.CharacterUpgradeStation;
import com.groupproject.game.entity.station.StatReforgeStation;
import com.groupproject.game.entity.weapon.AssaultRifle;
import com.groupproject.game.entity.weapon.MeleeSlash;
import com.groupproject.game.entity.player.Player;
import com.groupproject.game.entity.weapon.Shotgun;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.entity.weapon.WeaponManager;
import com.groupproject.game.input.KeyBindings;
import com.groupproject.game.input.KeyBindings.GameAction;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import com.groupproject.game.ui.BackpackUI;
import com.groupproject.game.ui.CharacterUpgradeUI;
import com.groupproject.game.ui.MinimapHUD;
import com.groupproject.game.ui.PauseMenuUI;
import com.groupproject.game.ui.PlayingHUD;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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
    private final Player player;

    private final List<TargetDummy> combatTargets = new CopyOnWriteArrayList<>();
    private TargetDummy bossDummy = null;
    private final List<ExplosionVisual> activeExplosions = new CopyOnWriteArrayList<>();
    private final List<CriticalHitVisual> activeCritVisuals = new CopyOnWriteArrayList<>();
    private final List<EnemyCorpse> activeCorpses = new CopyOnWriteArrayList<>();

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

    // Tiến trình Stage trong Biome (3 Stage thường: 1, 2, 3 và 1 Stage Boss: 4)
    private int currentStage = 1;
    public static final int MAX_STAGES = 4;
    public static final int BOSS_STAGE = 4;

    // Hệ thống tiếp viện lính con bảo vệ Boss Tank (Sau 5s kể từ khi lính con bị clear)
    public static final double BOSS_MINION_RESPAWN_DELAY = 5.0;
    private double bossMinionRespawnTimer = BOSS_MINION_RESPAWN_DELAY;
    private int bossMinionWaveCount = 0;

    // Rung màn hình khi bị thương hoặc bộc phá (0.2s)
    private double screenShakeTimer = 0.0;
    private final List<HealthPickup> healthPickups = new CopyOnWriteArrayList<>();

    // Giao diện Balo chiến thuật (Tab), Bản đồ thu nhỏ Minimap và Hệ thống HUD
    private final BackpackUI backpackUI = new BackpackUI();
    private final PlayingHUD playingHUD = new PlayingHUD();
    private final MinimapHUD minimapHUD = new MinimapHUD();
    private boolean isBackpackOpen = false;

    // Hệ thống Cắt cảnh Giới thiệu Boss & Chuyển màn Điện ảnh
    private final com.groupproject.game.ui.BossIntroCinematic bossIntroCinematic = new com.groupproject.game.ui.BossIntroCinematic();
    private boolean bossIntroTriggered = false;
    private final com.groupproject.game.ui.ScreenTransition stageTransition = new com.groupproject.game.ui.ScreenTransition();

    // Menu Tạm dừng (Pause Menu UI) và Trạng thái Tạm dừng
    private final PauseMenuUI pauseMenuUI = new PauseMenuUI();
    private boolean isPaused = false;

    // Hệ thống tiền tệ chiến thuật (Credits) và quầy giao dịch Shop
    private int playerCredits = 0;
    private final List<ShopItem> shopItems = new CopyOnWriteArrayList<>();
    private StatReforgeStation statReforgeStation;

    // Hệ thống Phòng Nâng Cấp Nhân Vật (Character Upgrade Stations & Modal UI)
    private CharacterUpgradeStation basicUpgradeStation;
    private CharacterUpgradeStation specialUpgradeStation;
    private final CharacterUpgradeUI characterUpgradeUI = new CharacterUpgradeUI();

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
        this.player = new Player(0, 0);
        this.weaponManager.bindPlayerBuffManager(this.player.getBuffManager());
    }

    /**
     * Deploys the player operative in the center of the Spawn Room and initializes the procedural run.
     */
    // Cờ thiết lập spawn trực tiếp vào phòng Boss để kiểm thử nhanh
    private static boolean spawnDirectlyInBossRoom = false;

    public static void setSpawnDirectlyInBossRoom(boolean enable) {
        spawnDirectlyInBossRoom = enable;
    }

    public static boolean isSpawnDirectlyInBossRoom() {
        return spawnDirectlyInBossRoom;
    }

    // Danh sách vũ khí chuyển giao từ The Range vào Dungeon
    private static java.util.List<Weapon> transferredHubWeapons = null;

    public static void transferWeaponsFromHub(java.util.List<Weapon> weapons) {
        transferredHubWeapons = (weapons != null) ? new java.util.ArrayList<>(weapons) : null;
    }

    public static java.util.List<Weapon> getTransferredHubWeapons() {
        return transferredHubWeapons;
    }

    @Override
    public void init() {
        if (spawnDirectlyInBossRoom) {
            loadBossStageDirectly();
            return;
        }

        this.currentStage = 1;
        this.playerCredits = 0;
        // Reset dungeon rooms and doors so new mission starts with procedural layout
        dungeonMap.reset();

        Room spawnRoom = dungeonMap.getSpawnRoom();
        if (spawnRoom != null) {
            player.reset(spawnRoom.getCenterWorldX() - (player.getSize() / 2.0),
                         spawnRoom.getCenterWorldY() - (player.getSize() / 2.0));
        } else {
            player.reset(GameConfig.WORLD_WIDTH / 2.0, GameConfig.WORLD_HEIGHT / 2.0);
        }

        projectileManager.clear();
        combatTargets.clear();
        bossDummy = null;
        activeExplosions.clear();
        activeCritVisuals.clear();
        weaponChests.clear();
        ammoPickups.clear();
        healthPickups.clear();
        activeCombatRoom = null;

        this.screenShakeTimer = 0.0;
        this.isStandingOnExtract = false;
        this.isExtracting = false;
        this.extractTransitionTimer = 0.0;
        this.isBackpackOpen = false;
        this.isPaused = false;
        this.stage1FreeWeaponGranted = false;
        this.bossMinionRespawnTimer = BOSS_MINION_RESPAWN_DELAY;
        this.bossMinionWaveCount = 0;
        this.weaponManager.reset();
        this.player.getBuffManager().reset();
        this.weaponManager.bindPlayerBuffManager(this.player.getBuffManager());

        // Đảm bảo cấm mang vũ khí thử nghiệm và vũ khí Lv 10 từ The Range vào Stage 1.
        // Mọi chiến dịch khởi đầu đều bắt đầu bằng trang bị tiêu chuẩn (Pistol Slot 3 + Knife Slot 4).
        transferredHubWeapons = null;

        populateShopItems();
        populateResetStatStation();
        populateUpgradeStations();

        // Reset cắt cảnh: run mới không được giữ lại intro Boss/transition của run trước
        bossIntroTriggered = false;
        bossIntroCinematic.skip();

        showBanner("STAGE 1", new Color(60, 220, 160), 2.0);

        // Center camera on operative immediately on mission start
        camera.update(player.getCenterX(), player.getCenterY());
    }

    /**
     * Tải và spawn thẳng người chơi vào Grand Boss Arena (Stage 4) đối đầu Chiến Xa Titan.
     * Cung cấp sẵn trang bị vũ khí hỏa lực cao (AR Cấp 3, Shotgun Cấp 3, đầy ắp đạn) và 500 Credits.
     */
    public void loadBossStageDirectly() {
        this.currentStage = BOSS_STAGE;
        this.playerCredits = 500;
        dungeonMap.generateStage(BOSS_STAGE);

        projectileManager.clear();
        combatTargets.clear();
        bossDummy = null;
        activeExplosions.clear();
        activeCritVisuals.clear();
        activeCorpses.clear();
        weaponChests.clear();
        ammoPickups.clear();
        healthPickups.clear();
        activeCombatRoom = null;

        this.screenShakeTimer = 0.0;
        this.isStandingOnExtract = false;
        this.isExtracting = false;
        this.extractTransitionTimer = 0.0;
        this.isBackpackOpen = false;
        this.isPaused = false;
        this.stage1FreeWeaponGranted = false;
        this.bossMinionRespawnTimer = BOSS_MINION_RESPAWN_DELAY;
        this.bossMinionWaveCount = 0;
        this.weaponManager.reset();

        Room bossRoom = dungeonMap.getCombatRooms().isEmpty() ? null : dungeonMap.getCombatRooms().get(0);
        if (bossRoom != null) {
            // Đặt người chơi ngay trong lòng phòng Grand Boss Arena (phía Tây phòng, cách tâm phòng ~520px)
            player.reset((bossRoom.getGridX() + 3) * GameConfig.TILE_SIZE,
                         bossRoom.getCenterWorldY() - (player.getSize() / 2.0));
            this.activeCombatRoom = bossRoom;
            bossRoom.setCurrentPhase(1);
            bossRoom.lockdown();
            spawnTargetsForRoomPhase(bossRoom, 1);
        } else {
            Room spawnRoom = dungeonMap.getSpawnRoom();
            if (spawnRoom != null) {
                player.reset(spawnRoom.getCenterWorldX() - (player.getSize() / 2.0),
                             spawnRoom.getCenterWorldY() - (player.getSize() / 2.0));
            } else {
                player.reset(GameConfig.WORLD_WIDTH / 2.0, GameConfig.WORLD_HEIGHT / 2.0);
            }
        }

        // Trang bị vũ khí đầy đủ cho trận chiến Boss: AR Cấp 3 (Slot 1) và Shotgun Cấp 3 (Slot 2)
        AssaultRifle ar = new AssaultRifle();
        ar.configureLevel(3);
        ar.setSlotIndex(1);
        weaponManager.addWeapon(ar);
        weaponManager.selectSlot(1);

        Shotgun sg = new Shotgun();
        sg.configureLevel(3);
        sg.setSlotIndex(2);
        weaponManager.addWeapon(sg);

        weaponManager.resupplyAmmoPackage(5); // Tiếp tế tối đa đạn dự trữ cho toàn bộ vũ khí
        weaponManager.bindPlayerBuffManager(player.getBuffManager());

        populateShopItems();
        populateResetStatStation();
        populateUpgradeStations();

        showBanner("STAGE 4: BOSS ARENA", new Color(255, 60, 60), 2.0);

        // Spawn thẳng vào phòng Boss: boss đã được sinh ở trên nên phát cắt cảnh giới thiệu ngay
        bossIntroTriggered = false;
        tryStartBossIntro();

        // Center camera on operative immediately on mission start
        camera.update(player.getCenterX(), player.getCenterY());
    }

    /**
     * Chuyển tiếp người chơi sang stage tiếp theo trong Biome mà vẫn bảo lưu trang bị / balo.
     * Tự động thăng thêm 1 Cấp (+1 Level) cho tất cả các khí tài Hạng S (S-Tier Legendary) mà người chơi đang sở hữu.
     */
    public void advanceToNextStage() {
        currentStage++;
        bossIntroTriggered = false; // Stage mới -> cho phép phát lại intro khi gặp Boss
        dungeonMap.generateStage(currentStage);

        Room spawnRoom = dungeonMap.getSpawnRoom();
        if (spawnRoom != null) {
            player.setPosition(spawnRoom.getCenterWorldX() - (player.getSize() / 2.0),
                               spawnRoom.getCenterWorldY() - (player.getSize() / 2.0));
        } else {
            player.setPosition(GameConfig.WORLD_WIDTH / 2.0, GameConfig.WORLD_HEIGHT / 2.0);
        }

        projectileManager.clear();
        combatTargets.clear();
        bossDummy = null;
        activeExplosions.clear();
        activeCritVisuals.clear();
        activeCorpses.clear();
        weaponChests.clear();
        ammoPickups.clear();
        healthPickups.clear();
        activeCombatRoom = null;
        isStandingOnExtract = false;
        player.setInvulnerableTimer(0.0);
        screenShakeTimer = 0.0;
        weaponManager.bindPlayerBuffManager(player.getBuffManager());

        // Kiểm tra và tự động thăng cấp (+1 Level) cho các khí tài Hạng S (S-Tier Legendary)
        java.util.List<String> sTierUpgrades = new java.util.ArrayList<>();
        for (Weapon w : weaponManager.getAllWeapons()) {
            if (w != null && w.isSTier()) {
                String upgradedStat = w.autoAdvanceStageLevel();
                if (upgradedStat != null) {
                    sTierUpgrades.add(w.getShortDisplayName() + " (+" + upgradedStat + ")");
                }
            }
        }

        populateShopItems();
        populateResetStatStation();
        populateUpgradeStations();

        camera.update(player.getCenterX(), player.getCenterY());

        if (!sTierUpgrades.isEmpty()) {
            String msg = "★ KHÍ TÀI HẠNG S TỰ LÊN CẤP: " + String.join(", ", sTierUpgrades) + " ★";
            showBanner(msg, new Color(255, 215, 60), 3.5);
        } else if (currentStage == BOSS_STAGE) {
            showBanner("STAGE 4: BOSS ARENA", new Color(255, 70, 70), 2.0);
        } else {
            showBanner("STAGE " + currentStage, new Color(60, 220, 160), 2.0);
        }
    }

    /** Bỏ qua tức thì cắt cảnh giới thiệu Boss (dùng cho test và debug). */
    public void skipBossIntro() {
        bossIntroCinematic.skip();
    }

    /** @return true nếu cắt cảnh giới thiệu Boss đang chạy (gameplay đang bị đóng băng). */
    public boolean isBossIntroActive() {
        return bossIntroCinematic.isActive();
    }

    /** @return true nếu hiệu ứng Fade chuyển stage đang chạy. */
    public boolean isStageTransitionActive() {
        return stageTransition.isActive();
    }

    /**
     * Chuyển sang stage kế tiếp kèm hiệu ứng Fade + banner tên tầng.
     *
     * <p>WHY: {@link #advanceToNextStage()} dựng lại toàn bộ map ngay lập tức nên gây "giật cảnh".
     * Ở đây map chỉ được dựng khi màn hình đã tối hẳn (callback HOLD của {@link ScreenTransition}).
     * Giữ nguyên advanceToNextStage() đồng bộ để test và code cũ không bị ảnh hưởng.
     */
    private void requestAdvanceToNextStage() {
        if (stageTransition.isActive()) {
            return;
        }
        int nextStage = currentStage + 1;
        boolean toBoss = nextStage == BOSS_STAGE;
        String title = toBoss ? "STAGE " + nextStage + ": BOSS ARENA" : "STAGE " + nextStage;
        String subtitle = toBoss ? "CẢNH BÁO: PHÁT HIỆN MỤC TIÊU CƠ GIỚI HẠNG NẶNG" : "TIẾP TỤC TIẾN SÂU VÀO CƠ SỞ";
        Color accent = toBoss ? new Color(255, 70, 70) : new Color(60, 220, 160);
        stageTransition.start(0.45, 0.35, this::advanceToNextStage, title, subtitle, accent);
    }

    /**
     * Phát cắt cảnh giới thiệu Boss đúng 1 lần mỗi stage Boss, khi Boss đã tồn tại và còn sống.
     * Cắt cảnh đóng băng gameplay (xem nhánh đầu {@link #update}) để Boss không tấn công khi camera đang lia.
     */
    private void tryStartBossIntro() {
        if (bossIntroTriggered || currentStage != BOSS_STAGE || bossDummy == null || !bossDummy.isAlive()) {
            return;
        }
        bossIntroTriggered = true;
        bossIntroCinematic.start(bossDummy, player);
    }

    /**
     * Bố trí các quầy hàng và trạm nâng cấp vũ khí trong Phòng Cửa Hàng (Shop Room).
     *
     * <p>Cân bằng kinh tế tác chiến theo đúng quy luật thiết kế phân tầng theo Stage:
     * - Stage 1 (Khởi đầu):
     *   + Súng Shop: Level 3 (90 Credits) - Đảm bảo đủ hỏa lực càn quét 2 sector kế tiếp (Sector 2 & 3).
     *   + Bàn nâng cấp súng 1 cấp: 45 Credits.
     *   + Hộp cứu thương: 35 Credits; Hộp tiếp đạn: 30 Credits.
     * - Stage 2 (Tăng tiến):
     *   + Súng Shop: Level 4 (135 Credits); Bàn nâng cấp: 65 Credits; Hộp máu: 55 Credits; Hộp đạn: 50 Credits.
     * - Stage 3+ / Boss (Thử thách):
     *   + Súng Shop: Level 5+ (175 Credits); Bàn nâng cấp: 85 Credits; Hộp máu: 75 Credits; Hộp đạn: 70 Credits.
     */
    private void populateShopItems() {
        shopItems.clear();
        Room shop = dungeonMap.getShopRoom();
        if (shop == null) return;

        double cx = shop.getCenterWorldX();
        double cy = shop.getCenterWorldY();

        // 1. Quầy bán súng chính ngẫu nhiên: Súng mua ở shop là súng tiêu chuẩn Level (currentStage + 2)
        // Ở Stage 1: Bán súng Level 3 tiêu chuẩn, đủ hỏa lực tác chiến tới hết Stage 3.
        Weapon randomWeapon = Weapon.getRandomDropWeapon();
        int shopWeaponLevel = currentStage + 2; // Stage 1 bán Lv.3 bình thường, Stage 2 bán Lv.4...
        randomWeapon.configureLevel(shopWeaponLevel);

        // Định giá súng shop theo Stage: Nâng giá tương xứng với giá trị súng Lv.3 (Stage 1 = 90 Credits thay vì giá 75 Credits ban đầu quá rẻ)
        int weaponPrice = switch (currentStage) {
            case 1 -> 90;
            case 2 -> 135;
            default -> 175;
        };
        shopItems.add(ShopItem.createWeaponItem(cx - 110, cy, randomWeapon, weaponPrice));

        // 2. Bàn nâng cấp vũ khí (ngay giữa trung tâm phòng shop):
        // Stage 1 ưu đãi 45 Credits/cấp; Stage 2 là 65C; Stage 3+ là 85C
        int upgradeStationCost = switch (currentStage) {
            case 1 -> 45;
            case 2 -> 65;
            default -> 85;
        };
        shopItems.add(ShopItem.createUpgradeStation(cx, cy, upgradeStationCost));

        // 3. Quầy tiếp tế (bên phải tâm phòng 110px):
        // QUY TẮC: Xuất hiện ngẫu nhiên giữa Hộp Tiếp Đạn và Hộp Cứu Thương
        boolean spawnMedkit = ThreadLocalRandom.current().nextBoolean();
        if (spawnMedkit) {
            int healthPrice = switch (currentStage) {
                case 1 -> 35;
                case 2 -> 55;
                default -> 75;
            };
            shopItems.add(ShopItem.createHealthItem(cx + 110, cy, healthPrice));
        } else {
            int ammoPrice = switch (currentStage) {
                case 1 -> 30;
                case 2 -> 50;
                default -> 70;
            };
            shopItems.add(ShopItem.createAmmoItem(cx + 110, cy, ammoPrice));
        }
    }

    /**
     * Khởi tạo Trạm Tẩy Luyện Chỉ Số (StatReforgeStation) tại phòng Reset Stat nếu có.
     */
    private void populateResetStatStation() {
        this.statReforgeStation = null;
        Room reforgeRoom = dungeonMap.getResetStatRoom();
        if (reforgeRoom != null) {
            double cx = reforgeRoom.getCenterWorldX();
            double cy = reforgeRoom.getCenterWorldY();
            this.statReforgeStation = new StatReforgeStation(cx, cy);
        }
    }

    /**
     * Khởi tạo 2 Trạm Nâng Cấp Nhân Vật (Cơ bản & Đặc biệt) trong Phòng Nâng Cấp nếu có.
     * Bố trí 2 bệ máy đối xứng qua tâm phòng (khoảng cách 70px mỗi bên).
     */
    private void populateUpgradeStations() {
        this.basicUpgradeStation = null;
        this.specialUpgradeStation = null;
        Room upgradeRoom = dungeonMap.getUpgradeRoom();
        if (upgradeRoom != null) {
            double cx = upgradeRoom.getCenterWorldX();
            double cy = upgradeRoom.getCenterWorldY();
            this.basicUpgradeStation = new CharacterUpgradeStation(cx - 70, cy, CharacterUpgradeStation.StationType.BASIC_STATS);
            this.specialUpgradeStation = new CharacterUpgradeStation(cx + 70, cy, CharacterUpgradeStation.StationType.SPECIAL_PERKS);
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

        // 3. Không sinh quá sát người chơi (bảo đảm khoảng cách an toàn ít nhất 130px để người chơi không bị quái spawn đè sát người)
        double minPlayerDist = Math.max(half + 115.0, 130.0);
        double distToPlayer = Math.hypot(x - player.getCenterX(), y - player.getCenterY());
        if (distToPlayer < minPlayerDist) {
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
        double[] radiuses = {32, 64, 96, 128, 160, 200, 240, 280};
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

        // Fallback an toàn: Quét tìm ô sàn xa người chơi nhất trong phòng thay vì chọn tâm phòng một cách mù quáng
        Point2D.Double bestFallback = null;
        double maxDist = -1;
        for (int c = rx + 1; c < rx + rw - 1; c++) {
            for (int r = ry + 1; r < ry + rh - 1; r++) {
                Tile tile = dungeonMap.getTile(c, r);
                if (tile != null && tile.getType() == TileType.FLOOR) {
                    double testX = tile.getWorldX() + GameConfig.TILE_SIZE / 2.0;
                    double testY = tile.getWorldY() + GameConfig.TILE_SIZE / 2.0;
                    double dist = Math.hypot(testX - player.getCenterX(), testY - player.getCenterY());
                    if (dist > maxDist) {
                        maxDist = dist;
                        bestFallback = new Point2D.Double(testX, testY);
                    }
                }
            }
        }
        if (bestFallback != null) {
            return bestFallback;
        }

        return new Point2D.Double(arena.getCenterWorldX(), arena.getCenterWorldY());
    }

    /**
     * Sinh một kẻ địch cận chiến (Melee Swarmer) an toàn với chỉ số được cường hóa nhẹ theo từng Stage:
     * - Máu (HP): +15 HP mỗi Stage sau Stage 1 (ví dụ Stage 1: 50 HP, Stage 2: 65 HP, Stage 3: 80 HP).
     * - Tốc độ di chuyển: +8 px/s mỗi Stage (125 -> 133 -> 141 px/s).
     * - Sát thương va chạm cận chiến: +2 DMG mỗi Stage (12 -> 14 -> 16 DMG).
     * - Thời gian hồi đòn đánh: Giảm 0.05s mỗi Stage (0.90s -> 0.85s -> 0.80s).
     * - Tên hiển thị: Tự động gắn nhãn phân hạng MK-2, MK-3...
     * - Cảnh báo hiển thị điểm spawn (Spawn Telegraph): 1.25s vòng ma trận cảnh báo trước khi xuất hiện.
     */
    private void spawnMeleeEnemySafely(Room arena, double preferredX, double preferredY, int baseHealth) {
        Point2D.Double safePos = findSafeSpawnLocation(arena, preferredX, preferredY, 30.0, combatTargets);
        int stageBonus = Math.max(0, currentStage - 1);
        int scaledHealth = baseHealth + stageBonus * 15;
        double scaledSpeed = 125.0 + stageBonus * 8.0;
        int scaledDamage = 12 + stageBonus * 2;
        double scaledCooldown = Math.max(0.60, 0.90 - stageBonus * 0.05);
        String name = stageBonus == 0 ? "Mercenary Raider [Melee]" : String.format("Mercenary Raider MK-%d [Melee]", currentStage);

        MeleeEnemy enemy = new MeleeEnemy(safePos.x, safePos.y, scaledHealth, scaledSpeed, scaledDamage, scaledCooldown, name);
        enemy.setSpawnTelegraph(0.40); // 0.40s vòng tròn cảnh báo trước khi kích hoạt
        enemy.setAttackTimer(0.30);    // 0.30s đệm hồi chiêu ban đầu tránh đòn chém sát thương frame 0
        combatTargets.add(enemy);
    }

    /**
     * Sinh một kẻ địch tầm xa dùng súng lục (Ranged Pistol Scout) an toàn với chỉ số được cường hóa nhẹ theo từng Stage:
     * - Máu (HP): +12 HP mỗi Stage sau Stage 1 (ví dụ Stage 1: 45 HP, Stage 2: 57 HP, Stage 3: 69 HP).
     * - Tốc độ di chuyển (giữ khoảng cách/thả diều): +6 px/s mỗi Stage (105 -> 111 -> 117 px/s).
     * - Sát thương đạn súng lục: +1 DMG mỗi Stage (10 -> 11 -> 12 DMG).
     * - Tên hiển thị: Tự động gắn nhãn phân hạng MK-2, MK-3...
     * - Cảnh báo hiển thị điểm spawn (Spawn Telegraph): 0.40s vòng ma trận cảnh báo trước khi xuất hiện.
     */
    private void spawnRangedPistolEnemySafely(Room arena, double preferredX, double preferredY, int baseHealth) {
        Point2D.Double safePos = findSafeSpawnLocation(arena, preferredX, preferredY, 30.0, combatTargets);
        int stageBonus = Math.max(0, currentStage - 1);
        int scaledHealth = baseHealth + stageBonus * 12;
        double scaledSpeed = 105.0 + stageBonus * 6.0;
        int scaledDamage = 10 + stageBonus * 1;
        String name = stageBonus == 0 ? "Recon Scout [Pistol]" : String.format("Recon Scout MK-%d [Pistol]", currentStage);

        RangedPistolEnemy enemy = new RangedPistolEnemy(safePos.x, safePos.y, scaledHealth, scaledSpeed, scaledDamage, 190.0, 360.0, name);
        enemy.setSpawnTelegraph(0.40); // 0.40s vòng tròn cảnh báo trước khi kích hoạt
        combatTargets.add(enemy);
    }

    /**
     * Sinh một kẻ địch tiêu chuẩn một cách an toàn vào danh sách (xen kẽ Cận chiến và Tầm xa).
     */
    @SuppressWarnings("unused")
    private void spawnEnemySafely(Room arena, double preferredX, double preferredY, int health) {
        if (combatTargets.size() % 2 == 0) {
            spawnMeleeEnemySafely(arena, preferredX, preferredY, health);
        } else {
            spawnRangedPistolEnemySafely(arena, preferredX, preferredY, health);
        }
    }

    /**
     * Sinh Boss Dummy kích thước lớn (size 84px, thanh máu dày) an toàn vào danh sách và lưu tham chiếu boss.
     */
    private void spawnBossSafely(Room arena, double preferredX, double preferredY, int health, double size, String name) {
        Point2D.Double safePos = findSafeSpawnLocation(arena, preferredX, preferredY, size, combatTargets);
        BossTank boss = new BossTank(safePos.x, safePos.y, health, size, name);
        boss.setSpawnTelegraph(0.50); // 0.50s xuất hiện nhanh của Boss
        this.bossDummy = boss;
        combatTargets.add(boss);
    }

    /**
     * Sinh các mục tiêu chiến đấu an toàn cho từng phòng combat theo Phase chỉ định.
     * @param arena Phòng chiến đấu.
     * @param phase Số thứ tự phase (1 hoặc 2).
     */
    private void spawnTargetsForRoomPhase(Room arena, int phase) {
        combatTargets.clear();
        this.bossDummy = null;
        if (arena == null) return;

        double cx = arena.getCenterWorldX();
        double cy = arena.getCenterWorldY();

        if (currentStage == BOSS_STAGE) {
            // Stage Boss (Stage 4) Grand Boss Arena: Xe tăng chiến đấu hạng nặng (size 96px, 1250 HP) và 8 AI hộ tống
            spawnBossSafely(arena, cx, cy, 1250, 96.0, "HEAVY COMBAT BATTLE TANK [BOSS]");
            spawnMeleeEnemySafely(arena, cx + 220, cy, 80);
            spawnMeleeEnemySafely(arena, cx - 220, cy, 80);
            spawnRangedPistolEnemySafely(arena, cx, cy + 180, 60);
            spawnRangedPistolEnemySafely(arena, cx, cy - 180, 60);
            spawnMeleeEnemySafely(arena, cx + 160, cy + 130, 70);
            spawnRangedPistolEnemySafely(arena, cx - 160, cy + 130, 60);
            spawnMeleeEnemySafely(arena, cx + 160, cy - 130, 70);
            spawnRangedPistolEnemySafely(arena, cx - 160, cy - 130, 60);
            this.bossMinionRespawnTimer = BOSS_MINION_RESPAWN_DELAY;
            this.bossMinionWaveCount = 0;
        } else {
            if (arena.getTotalPhases() <= 1) {
                // Phòng 1 Phase: Số lượng quái tiêu chuẩn kết hợp cận chiến và súng lục
                spawnMeleeEnemySafely(arena, cx - 120, cy - 60, 50);
                spawnRangedPistolEnemySafely(arena, cx + 120, cy + 60, 45);
                spawnMeleeEnemySafely(arena, cx + 120, cy - 80, 50);
                if (arena.getWidthInTiles() >= 13) {
                    spawnRangedPistolEnemySafely(arena, cx - 140, cy + 70, 45);
                }
            } else {
                // Phòng 2 Phase:
                if (phase == 1) {
                    // Phase 1: Đợt quái tiền trạm mở màn
                    spawnMeleeEnemySafely(arena, cx - 120, cy - 60, 50);
                    spawnRangedPistolEnemySafely(arena, cx + 120, cy + 60, 45);
                    if (arena.getWidthInTiles() >= 13) {
                        spawnMeleeEnemySafely(arena, cx + 120, cy - 80, 50);
                    }
                } else {
                    // Phase 2: Đợt quái tiếp viện bất ngờ từ các góc phòng
                    spawnMeleeEnemySafely(arena, cx - 140, cy + 70, 55);
                    spawnRangedPistolEnemySafely(arena, cx + 140, cy - 70, 45);
                    spawnMeleeEnemySafely(arena, cx, cy - 90, 55);
                    if (arena.getWidthInTiles() >= 13) {
                        spawnRangedPistolEnemySafely(arena, cx, cy + 90, 45);
                    }
                }
            }
        }
    }

    /**
     * Sinh các mục tiêu chiến đấu an toàn cho từng phòng combat (mặc định lấy phase hiện tại của phòng).
     */
    @SuppressWarnings("unused")
    private void spawnTargetsForRoom(Room arena) {
        int phase = (arena != null) ? arena.getCurrentPhase() : 1;
        spawnTargetsForRoomPhase(arena, phase);
    }

    /**
     * Cập nhật bộ đếm thời gian hồi lính con: khi toàn bộ lính con bị tiêu diệt trong lúc Boss còn sống,
     * sau 15 giây đếm ngược sẽ tự động gọi thêm 5-6 kẻ địch để bảo vệ Boss.
     */
    public void updateBossMinionReinforcements(double deltaTime) {
        if (currentStage != BOSS_STAGE || bossDummy == null || !bossDummy.isAlive()) {
            bossMinionRespawnTimer = BOSS_MINION_RESPAWN_DELAY;
            return;
        }

        // Đếm số lượng lính con hộ tống (không tính Boss) còn sống
        long activeMinions = combatTargets.stream()
                .filter(d -> d.isAlive() && !d.isBoss())
                .count();

        if (activeMinions == 0) {
            bossMinionRespawnTimer -= deltaTime;
            if (bossMinionRespawnTimer <= 0) {
                bossMinionRespawnTimer = BOSS_MINION_RESPAWN_DELAY;
                bossMinionWaveCount++;
                spawnBossMinionReinforcements();
            }
        } else {
            bossMinionRespawnTimer = BOSS_MINION_RESPAWN_DELAY;
        }
    }

    /**
     * Sinh thêm 5-6 lính tiếp viện bảo vệ Boss (kết hợp cận chiến Cyber Swarmer và tầm xa Rogue Pistol Scout)
     * sau 15 giây kể từ khi các lính con trước đó bị dọn sạch.
     */
    public void spawnBossMinionReinforcements() {
        Room arena = activeCombatRoom;
        if (arena == null && !dungeonMap.getCombatRooms().isEmpty()) {
            arena = dungeonMap.getCombatRooms().get(0);
        }
        if (arena == null) return;

        double cx = arena.getCenterWorldX();
        double cy = arena.getCenterWorldY();

        // Tầm 5-6 kẻ địch ngẫu nhiên
        int count = ThreadLocalRandom.current().nextInt(5, 7);

        // Các vị trí góc chiến thuật phân bố đều quanh phòng Grand Boss Arena
        double[][] spawnOffsets = {
                {220, -140},
                {-220, 140},
                {180, 160},
                {-180, -160},
                {0, 200},
                {0, -200}
        };

        for (int i = 0; i < count; i++) {
            double ox = spawnOffsets[i][0];
            double oy = spawnOffsets[i][1];
            if (i % 2 == 0) {
                // Lính cận chiến Mercenary Raider
                spawnMeleeEnemySafely(arena, cx + ox, cy + oy, 75);
            } else {
                // Lính bắn tầm xa Recon Scout
                spawnRangedPistolEnemySafely(arena, cx + ox, cy + oy, 60);
            }
        }

        screenShakeTimer = 0.20;
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
        // --- -1. Chuyển màn & Giới thiệu Boss (Cinematic) ---
        // WHY: Khi đang fade chuyển stage hoặc đang lia camera giới thiệu Boss, toàn bộ gameplay
        // bị đóng băng để người chơi không bị đánh/di chuyển khi chưa nhìn thấy gì.
        stageTransition.update(deltaTime);
        if (stageTransition.isBlockingInput()) {
            return;
        }
        if (bossIntroCinematic.isActive()) {
            bossIntroCinematic.handleInput(keyH, mouseH); // Intro Boss được phép skip
            bossIntroCinematic.update(deltaTime);
            camera.update(bossIntroCinematic.getCameraFocusX(), bossIntroCinematic.getCameraFocusY());
            return;
        }

        // --- 0. Pause Menu Handling & Updates ---
        // WHY: Khi tạm dừng, toàn bộ tiến trình chiến đấu bị đóng băng. Người chơi có 3 tùy chọn:
        // TIẾP TỤC (Resume), CÀI ĐẶT (Settings - Keybinds), THOÁT RA MENU (Exit to Menu).
        if (isPaused) {
            if (KeyBindings.isActionPressedOnce(keyH, GameAction.PAUSE_MENU) || keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
                PauseMenuUI.PauseAction escAction = pauseMenuUI.handleEsc();
                if (escAction == PauseMenuUI.PauseAction.RESUME) {
                    isPaused = false;
                }
                return;
            }

            PauseMenuUI.PauseAction action = pauseMenuUI.update(keyH, mouseH, deltaTime);
            if (action == PauseMenuUI.PauseAction.RESUME) {
                isPaused = false;
            } else if (action == PauseMenuUI.PauseAction.EXIT_TO_MENU) {
                isPaused = false;
                gsm.setState(GameStateManager.StateType.MENU);
            }
            return; // Đóng băng toàn bộ hoạt động chiến trường khi đang tạm dừng
        }

        // --- 0.5. Handle Character Upgrade Modal UI ---
        // WHY: Khi đang mở Bảng Nâng Cấp Nhân Vật, đóng băng vòng lặp chiến đấu để người chơi tương tác an toàn
        if (characterUpgradeUI.isVisible()) {
            characterUpgradeUI.update(keyH, mouseH, this, deltaTime);
            return;
        }

        // --- Handle Player Death (Game Over Sequence) ---
        if (player.isDead()) {
            // Cập nhật hoạt cảnh đầu lâu trượt ma sát trên sàn và va chạm tường
            player.update(deltaTime, dungeonMap);

            // Camera vẫn mượt mà bám theo đầu lâu đang trượt
            camera.update(player.getCenterX(), player.getCenterY());

            if (bannerTimer > 0) {
                bannerTimer -= deltaTime;
            }

            // Khi hoạt cảnh trượt đã kết thúc (sau 2.0s):
            if (player.isDeathSequenceComplete()) {
                if (keyH.isKeyPressedOnce(KeyEvent.VK_ENTER) || keyH.isKeyPressedOnce(KeyEvent.VK_R)) {
                    init(); // Tái xuất phát run mới từ đầu
                } else if (keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
                    gsm.setState(GameStateManager.StateType.MENU);
                }
            }
            // Trong 2.0s đầu lâu đang trượt: KHÔNG nhận phím nào (ENTER/SPACE/R/ESC) để người chơi
            // xem trọn hoạt cảnh tử trận; chỉ sau khi hoàn tất mới cho Chơi lại / Thoát ra menu.
            return;
        }

        // Cập nhật bộ đếm bất tử của người chơi, rung camera và bộ đếm kỹ năng Buff
        player.update(deltaTime, dungeonMap);
        player.getBuffManager().update(deltaTime, player, weaponManager);
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
                    || KeyBindings.isActionPressedOnce(keyH, GameAction.INTERACT)) {
                gsm.setState(GameStateManager.StateType.MENU);
            }
            return;
        }

        // --- Handle Tactical Backpack (TAB) ---
        // WHY: Cho phép xem kho trang bị và hoán đổi vị trí vũ khí 1 và 2 trong không gian an toàn
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.BACKPACK)) {
            isBackpackOpen = !isBackpackOpen;
            return;
        }

        if (isBackpackOpen) {
            if (KeyBindings.isActionPressedOnce(keyH, GameAction.PAUSE_MENU) || keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
                isBackpackOpen = false;
                return;
            }
            boolean closeRequested = backpackUI.update(keyH, mouseH, weaponManager, player, deltaTime);
            if (closeRequested) {
                isBackpackOpen = false;
            }
            return; // Đóng băng vòng lặp chiến đấu khi đang xem Balo
        }

        // --- Handle Pause Request (PAUSE_MENU / ESC) ---
        // WHY: Bấm phím Tạm dừng (mặc định ESC hoặc phím đã tùy chỉnh trong KeyBindings) để mở Menu Tạm Dừng
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.PAUSE_MENU) || keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
            isPaused = true;
            pauseMenuUI.open();
            return;
        }

        // Phím tắt F2: Khởi động lại / Respawn ngay tại phòng Boss để test nhanh
        if (keyH.isKeyPressedOnce(KeyEvent.VK_F2)) {
            loadBossStageDirectly();
            return;
        }

        if (bannerTimer > 0) {
            bannerTimer -= deltaTime;
        }

        // --- 1. Weapon Hotkey Switching & Reloading ---
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.SLOT_1)) {
            weaponManager.selectSlot(1); // Trang bị vũ khí Slot 1
        }
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.SLOT_2)) {
            weaponManager.selectSlot(2); // Trang bị vũ khí Slot 2
        }
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.SLOT_3)) {
            weaponManager.selectSlot(3); // Trang bị Tactical Pistol (Súng lục)
        }
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.SLOT_4)) {
            weaponManager.selectSlot(4); // Trang bị Combat Knife (Dao găm chiến thuật)
        }
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.QUICK_SWAP)) {
            weaponManager.quickSwap(); // Quick swap between last 2 weapons
        }
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.RELOAD)) {
            weaponManager.reload(); // Manual magazine reload
        }

        // --- 1.5. Tactical Dodge Roll / Combat Dash [SPACE / DODGE] ---
        // Cho phép lướt nhanh né đòn và kích hoạt i-frames miễn nhiễm 100% sát thương
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.DODGE)) {
            player.triggerDodge(
                    KeyBindings.isActionDown(keyH, GameAction.MOVE_UP),
                    KeyBindings.isActionDown(keyH, GameAction.MOVE_DOWN),
                    KeyBindings.isActionDown(keyH, GameAction.MOVE_LEFT),
                    KeyBindings.isActionDown(keyH, GameAction.MOVE_RIGHT)
            );
        }

        // --- 2. Locomotion & Decoupled Axis Wall Collision ---
        player.handleMovement(
                KeyBindings.isActionDown(keyH, GameAction.MOVE_UP),
                KeyBindings.isActionDown(keyH, GameAction.MOVE_DOWN),
                KeyBindings.isActionDown(keyH, GameAction.MOVE_LEFT),
                KeyBindings.isActionDown(keyH, GameAction.MOVE_RIGHT),
                deltaTime,
                dungeonMap
        );

        // --- 3. Cập nhật Sương Mù Khám Phá (Fog of War) và Minimap Radar ---
        dungeonMap.updateDiscovery(player.getCenterX(), player.getCenterY());
        minimapHUD.update(deltaTime);

        // --- 4. Multi-Chamber Combat Lockdown & Detection ---
        if (activeCombatRoom == null) {
            for (Room cr : dungeonMap.getCombatRooms()) {
                if (cr.getStatus() == Room.RoomStatus.UNVISITED
                        && cr.hasEntityEntered(player.getX(), player.getY(), player.getSize(), player.getSize())) {

                    activeCombatRoom = cr;

                    double minInteriorX = (cr.getGridX() + 1.0) * GameConfig.TILE_SIZE;
                    double maxInteriorX = (cr.getGridX() + cr.getWidthInTiles() - 1.0) * GameConfig.TILE_SIZE;
                    double minInteriorY = (cr.getGridY() + 1.0) * GameConfig.TILE_SIZE;
                    double maxInteriorY = (cr.getGridY() + cr.getHeightInTiles() - 1.0) * GameConfig.TILE_SIZE;

                    if (player.getX() < minInteriorX) player.setX(minInteriorX);
                    if (player.getX() + player.getSize() > maxInteriorX) player.setX(maxInteriorX - player.getSize());
                    if (player.getY() < minInteriorY) player.setY(minInteriorY);
                    if (player.getY() + player.getSize() > maxInteriorY) player.setY(maxInteriorY - player.getSize());

                    cr.setCurrentPhase(1);
                    cr.lockdown();
                    spawnTargetsForRoomPhase(cr, 1);
                    tryStartBossIntro(); // Chỉ có tác dụng ở Stage Boss

                    int idx = dungeonMap.getCombatRooms().indexOf(cr) + 1;
                    int total = dungeonMap.getCombatRooms().size();
                    if (currentStage != BOSS_STAGE) {
                        showBanner(String.format("PHÒNG CHIẾN ĐẤU %d/%d", idx, total),
                                new Color(255, 75, 75), 1.5);
                    }
                    break;
                }
            }
        }

        // --- 5. Smooth Camera Tracking with Screen Shake ---
        double camCX = player.getCenterX();
        double camCY = player.getCenterY();
        if (screenShakeTimer > 0) {
            camCX += ThreadLocalRandom.current().nextDouble(-3.0, 3.0);
            camCY += ThreadLocalRandom.current().nextDouble(-3.0, 3.0);
        }
        camera.update(camCX, camCY);

        // --- 6. 360-Degree Aim Angle Calculation ---
        double mouseWorldX = camera.screenToWorldX(mouseH.getScreenX());
        double mouseWorldY = camera.screenToWorldY(mouseH.getScreenY());
        aimAngle = Math.atan2(mouseWorldY - player.getCenterY(), mouseWorldX - player.getCenterX());
        player.setAimAngle(aimAngle);

        // --- 7. Weapon Attack Execution ---
        // Nút bắn (FIRE): Mặc định Chuột Trái (hoặc phím tùy chỉnh trong KeyBindings).
        // Súng tự động (AR/SMG) cho phép giữ nút liên thanh (isActionDown).
        // Súng bán tự động (Pistol/Shotgun/Dao) yêu cầu bấm từng phát một (isActionPressedOnce).
        // ĐẶC BIỆT: Khi đang kích hoạt Quick-Melee (cận chiến nhanh), người chơi tập trung vung dao
        // nên bắt buộc ngừng xả đạn súng (kể cả súng tự động đang giữ nút bắn).
        boolean isQuickMeleeActive = KeyBindings.isActionDown(keyH, mouseH, GameAction.QUICK_MELEE);
        Weapon currentWeapon = weaponManager.getActiveWeapon();
        boolean shouldTriggerAttack = false;
        if (currentWeapon != null && !isQuickMeleeActive) {
            if (currentWeapon.isAutomatic()) {
                shouldTriggerAttack = KeyBindings.isActionDown(keyH, mouseH, GameAction.FIRE);
            } else {
                shouldTriggerAttack = KeyBindings.isActionPressedOnce(keyH, mouseH, GameAction.FIRE);
            }
        }
        if (shouldTriggerAttack) {
            boolean fired = weaponManager.attack(player.getCenterX(), player.getCenterY(), aimAngle, projectileManager);
            if (fired && currentWeapon instanceof com.groupproject.game.entity.weapon.SemiCarbineRifle scr && scr.getContinuousShots() >= 2) {
                triggerScreenShake(0.08);
            }
        }

        // --- 7.1. Quick-Melee Strike ---
        // Cho phép người chơi chém dao cận chiến tức thì khi bấm nút cận chiến nhanh (mặc định Chuột Phải)
        // bất kể đang trang bị vũ khí nào (Slot 1, Slot 2, Pistol...).
        // Tăng độ cơ động phản xạ nhanh khi bị quái cận chiến áp sát
        if (isQuickMeleeActive) {
            weaponManager.quickMelee(player.getCenterX(), player.getCenterY(), aimAngle, projectileManager);
        }

        // --- 8. Advance Weapons & Projectiles ---
        weaponManager.update(deltaTime);
        projectileManager.update(deltaTime, dungeonMap, this);

        // --- 8.1. Advance Active Explosions ---
        for (ExplosionVisual ev : activeExplosions) {
            ev.update(deltaTime);
        }
        activeExplosions.removeIf(ExplosionVisual::isFinished);

        // --- 8.2. Advance Active Critical Hit Visuals ---
        for (CriticalHitVisual cv : activeCritVisuals) {
            cv.update(deltaTime);
        }
        activeCritVisuals.removeIf(CriticalHitVisual::isFinished);

        // --- 8.3. Advance Active Enemy Corpses (Knockback sliding & fade out) ---
        for (EnemyCorpse corpse : activeCorpses) {
            corpse.update(deltaTime, dungeonMap);
        }
        activeCorpses.removeIf(EnemyCorpse::isFinished);

        // --- 9. Check Projectile and Melee Hits on Hostile Targets ---
        updateCombatDamageResolution(deltaTime);

        // --- 9.1. Boss Arena Minion Reinforcements (Spawn 5-6 enemies 15s after minions are cleared) ---
        updateBossMinionReinforcements(deltaTime);

        // --- 10. Check Room Clear Condition and Unlock Doors ---
        if (activeCombatRoom != null && activeCombatRoom.isLocked() && combatTargets.isEmpty()) {
            if (activeCombatRoom.hasNextPhase()) {
                // Đợt quái tiếp viện (Phase 2): Cửa vẫn phong tỏa, spawn thêm quái và rung màn hình cảnh báo
                activeCombatRoom.advancePhase();
                int nextPhase = activeCombatRoom.getCurrentPhase();
                spawnTargetsForRoomPhase(activeCombatRoom, nextPhase);

                screenShakeTimer = 0.20;
            } else {
                activeCombatRoom.unlock();
                int clearReward = switch (currentStage) {
                    case 1 -> 10; // Giảm nhẹ ở màn 1 (10 Credits thay vì 15)
                    case 2 -> 16; // Tăng dần ở màn 2 (16 Credits)
                    case 3 -> 22; // Tăng dần ở màn 3 (22 Credits)
                    default -> 30; // Boss Stage / Thử thách cao cấp (30 Credits)
                };
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
                double chestCenterY = (currentStage == BOSS_STAGE)
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

                if (currentStage == BOSS_STAGE) {
                    // Stage Boss (Stage 4) Grand Boss Arena: Kích hoạt bệ sơ tán ngay tại tâm phòng combat!
                    dungeonMap.spawnExtractZoneInRoom(activeCombatRoom);
                    showBanner("CHIẾN THẮNG! BỆ SƠ TÁN ĐÃ MỞ (+" + clearReward + " C)",
                            new Color(60, 240, 140), 2.5);
                } else {
                    if (clearedCount >= totalCount) {
                        showBanner("CỬA SƠ TÁN ĐÃ MỞ (+" + clearReward + " C)",
                                new Color(60, 240, 140), 2.5);
                    } else {
                        showBanner(String.format("PHÒNG %d/%d HOÀN THÀNH (+%d C)", clearedCount, totalCount, clearReward),
                                new Color(60, 220, 120), 2.0);
                    }
                }

                activeCombatRoom = null;
            }
        }

        // --- 11. Handle Weapon Chests Auto-Open & Pickup Interaction [F] ---
        boolean chestInteracted = false;
        for (WeaponChest chest : weaponChests) {
            // Hòm tự động mở khi người chơi đi lại gần (bán kính ~58px) và lộ diện chiến lợi phẩm (Tiền hoặc Súng)
            chest.update(deltaTime, player.getX(), player.getY(), player.getSize());

            // Khi hòm đã mở và chưa thu thập, bấm phím tương tác chính [F] để nhận thưởng
            if (chest.isPlayerInRange(player.getX(), player.getY(), player.getSize()) && chest.isOpened() && !chest.isCollected()) {
                if (KeyBindings.isActionPressedOnce(keyH, GameAction.INTERACT)) {
                    boolean collected = chest.collectReward(this);
                    if (collected) {
                        chestInteracted = true;
                    }
                }
            }
        }

        // --- 11.2. Handle Ammo Pickups Proximity Auto-Collection ---
        for (AmmoPickup pickup : ammoPickups) {
            pickup.update(deltaTime, player.getX(), player.getY(), player.getSize(), weaponManager.getAmmoInventory(), this);
        }
        ammoPickups.removeIf(AmmoPickup::isCollected);

        // --- 11.3. Handle Health Pickups Manual Interaction [F] ---
        // QUY TẮC: Người chơi phải bấm phím [F] để sử dụng, tuyệt đối không tự động nhặt
        boolean healthInteracted = false;
        for (HealthPickup hp : healthPickups) {
            hp.update(deltaTime);
            if (!healthInteracted && hp.isPlayerInRange(player.getX(), player.getY(), player.getSize())) {
                if (KeyBindings.isActionPressedOnce(keyH, GameAction.INTERACT)) {
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
            if (item.isPlayerInRange(player.getX(), player.getY(), player.getSize()) && !item.isPurchased()) {
                if (KeyBindings.isActionPressedOnce(keyH, GameAction.INTERACT)) {
                    boolean bought = item.purchase(this);
                    if (bought) {
                        shopInteracted = true;
                        break;
                    }
                }
            }
        }

        // --- 11.6. Handle Stat Reforge Station Interaction [F] ---
        boolean reforgeInteracted = false;
        if (statReforgeStation != null) {
            statReforgeStation.update(deltaTime);
            if (statReforgeStation.isPlayerInRange(player.getX(), player.getY(), player.getSize())) {
                if (KeyBindings.isActionPressedOnce(keyH, GameAction.INTERACT)) {
                    boolean reforged = statReforgeStation.interact(this);
                    if (reforged) {
                        reforgeInteracted = true;
                    }
                }
            }
        }

        // --- 11.7. Handle Character Upgrade Stations Interaction [F] ---
        boolean upgradeInteracted = false;
        if (basicUpgradeStation != null) {
            basicUpgradeStation.update(deltaTime);
            if (basicUpgradeStation.isPlayerInRange(player.getX(), player.getY(), player.getSize())) {
                if (KeyBindings.isActionPressedOnce(keyH, GameAction.INTERACT)) {
                    characterUpgradeUI.open(basicUpgradeStation, player.getBuffManager());
                    upgradeInteracted = true;
                }
            }
        }
        if (!upgradeInteracted && specialUpgradeStation != null) {
            specialUpgradeStation.update(deltaTime);
            if (specialUpgradeStation.isPlayerInRange(player.getX(), player.getY(), player.getSize())) {
                if (KeyBindings.isActionPressedOnce(keyH, GameAction.INTERACT)) {
                    characterUpgradeUI.open(specialUpgradeStation, player.getBuffManager());
                    upgradeInteracted = true;
                }
            }
        }

        // --- 12. Check Extraction Zone Interaction [F] ---
        // WHY: Intersecting the 4x4 extract pad prompts the operative to press [F] to extract or advance stage
        isStandingOnExtract = dungeonMap.isIntersectsExtractZone(player.getX(), player.getY(), player.getSize(), player.getSize());
        if (!chestInteracted && !shopInteracted && !healthInteracted && !reforgeInteracted && !upgradeInteracted && isStandingOnExtract && KeyBindings.isActionPressedOnce(keyH, GameAction.INTERACT)) {
            if (currentStage < MAX_STAGES) {
                requestAdvanceToNextStage();
            } else {
                isExtracting = true;
                extractTransitionTimer = 0.0;
            }
        }
    }

    /**
     * Tỉ lệ nhỏ (~20%) rơi tiền tệ Credits khi tiêu diệt một mục tiêu (quái).
     * Phân tầng theo Stage: Màn 1 giảm nhẹ và tăng dần qua các màn sau.
     */
    private void tryDropEnemyCredits() {
        if (ThreadLocalRandom.current().nextInt(100) < 20) {
            int dropped = switch (currentStage) {
                case 1 -> ThreadLocalRandom.current().nextInt(2, 6); // 2 đến 5 Credits ở Stage 1
                case 2 -> ThreadLocalRandom.current().nextInt(4, 9); // 4 đến 8 Credits ở Stage 2
                default -> ThreadLocalRandom.current().nextInt(6, 13); // 6 đến 12 Credits ở Stage 3+
            };
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
                case SR -> ThreadLocalRandom.current().nextInt(2, 5);
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
     * Resolves collision detection between active bullets / knife slashes, hostile targets, and operative.
     */
    private void updateCombatDamageResolution(double deltaTime) {
        // 1. Projectile hits
        for (Projectile p : projectileManager.getActiveProjectiles()) {
            if (!p.isActive()) continue;

            if (p.isPlayerBullet()) {
                // Đạn của Operative trúng mục tiêu thù địch (bỏ qua kẻ địch đang chuẩn bị spawn trong portal)
                for (TargetDummy dummy : combatTargets) {
                    if (dummy.isAlive() && !dummy.isSpawning() && p.canHit(dummy) && p.getBounds().intersects(dummy.getBounds())) {
                        double impulse = p.isCritical() ? 190.0 : 130.0;
                        int damageDealt = p.getDamage();
                        dummy.takeDamage(damageDealt, p.getAngle(), impulse, p.isCritical());
                        if (p.isCritical()) {
                            activeCritVisuals.add(new CriticalHitVisual(dummy.getX(), dummy.getY(), damageDealt));
                            screenShakeTimer = Math.max(screenShakeTimer, 0.08);
                        }
                        // Ghi nhận va chạm mục tiêu: tiêu hao 1 lượt xuyên và giảm sát thương cho kẻ địch kế tiếp
                        p.onHitTarget(dummy);
                        if (!dummy.isAlive()) {
                            double fatalAngle = p.getAngle();
                            double fatalForce = p.isCritical() ? 480.0 : 360.0;
                            onEnemyEliminated(dummy, fatalAngle, fatalForce);
                        }
                        // Nếu đạn đã hết lượt xuyên và bị tiêu biến thì dừng xét các mục tiêu phía sau
                        if (!p.isActive()) {
                            break;
                        }
                    }
                }
            } else {
                // Đạn của Kẻ Địch trúng Operative
                Rectangle playerBox = new Rectangle(
                        (int) player.getX(), (int) player.getY(), (int) player.getSize(), (int) player.getSize());
                if (p.getBounds().intersects(playerBox)) {
                    if (p instanceof BossCannonShell) {
                        ((BossCannonShell) p).explode(this, dungeonMap);
                    } else {
                        damagePlayer(p.getDamage());
                        p.deactivate();
                    }
                }
            }
        }

        // 2. Melee knife slash hits (Chỉ tác động sát thương 1 lần duy nhất cho mỗi mục tiêu trong 1 nhát chém)
        for (MeleeSlash slash : weaponManager.getActiveSlashes()) {
            if (!slash.isActive()) continue;

            for (TargetDummy dummy : combatTargets) {
                if (dummy.isAlive() && !dummy.isSpawning() && slash.canHit(dummy) && slash.getBounds().intersects(dummy.getBounds())) {
                    slash.markHit(dummy);
                    // Nhát chém dao cận chiến gây sát thương và đẩy lùi cực mạnh theo hướng vung dao
                    dummy.takeDamage(slash.getDamage(), slash.getAngle(), slash.getKnockbackForce(), slash.isCritical());
                    if (slash.isCritical()) {
                        activeCritVisuals.add(new CriticalHitVisual(dummy.getX(), dummy.getY(), slash.getDamage()));
                        screenShakeTimer = Math.max(screenShakeTimer, 0.08);
                    }
                    if (dummy instanceof MeleeEnemy meleeEnemy) {
                        meleeEnemy.stagger(0.40); // Khựng đòn cắn của quái cận chiến, giải tỏa áp lực bị vây hãm
                    }
                    if (!dummy.isAlive()) {
                        double fatalAngle = slash.getAngle();
                        double fatalForce = slash.isCritical() ? 650.0 : 560.0;
                        onEnemyEliminated(dummy, fatalAngle, fatalForce);
                    }
                }
            }

            // Gây sát thương và phá hủy ô cover tạm thời trong tầm chém dao găm (chỉ 1 lần mỗi nhát chém)
            if (!slash.hasDamagedCover()) {
                slash.markCoverDamaged();
                java.awt.geom.Rectangle2D slashBounds = slash.getBounds();
                dungeonMap.damageDestructibleCoverInArea(
                        slashBounds.getX(), slashBounds.getY(),
                        slashBounds.getWidth(), slashBounds.getHeight(),
                        slash.getDamage());
            }
        }

        // 3. Update targets AI (locomotion, kiting, melee attacks, pistol bursts)
        double playerCenterX = player.getCenterX();
        double playerCenterY = player.getCenterY();
        for (TargetDummy dummy : combatTargets) {
            dummy.updateAI(deltaTime, playerCenterX, playerCenterY, player.getSize(), dungeonMap, projectileManager, this);
        }
        combatTargets.removeIf(d -> !d.isAlive());
    }

    /**
     * Xử lý sinh xác đầu lâu nâu mờ và phần thưởng chiến thuật khi một mục tiêu bị tiêu diệt.
     * Khi Boss Dummy bị hạ gục: Thưởng lớn +50 Credits, rơi 3 stack đạn các loại, và hiển thị banner chiến tích.
     *
     * @param dummy      Mục tiêu bị tiêu diệt.
     * @param fatalAngle Hướng văng đẩy lùi (radian) theo đòn kết liễu.
     * @param fatalForce Xung lực đẩy lùi kết liễu (px/s).
     */
    public void onEnemyEliminated(TargetDummy dummy, double fatalAngle, double fatalForce) {
        // Sinh xác đầu lâu tông màu xám nâu mờ nhạt trượt văng do knockback
        EnemyCorpse corpse = new EnemyCorpse(dummy.getX(), dummy.getY(), dummy.getSize(), fatalAngle, fatalForce, dummy.isBoss());
        activeCorpses.add(corpse);

        if (dummy.isBoss()) {
            addCredits(50);
            showBanner("HẠ GỤC BOSS (+50 C)", new Color(255, 215, 60), 2.5);
            ammoPickups.add(new AmmoPickup(dummy.getX() - 36, dummy.getY(), AmmoType.AR, 30));
            ammoPickups.add(new AmmoPickup(dummy.getX() + 36, dummy.getY(), AmmoType.SMG, 35));
            ammoPickups.add(new AmmoPickup(dummy.getX(), dummy.getY() + 36, AmmoType.SHOTGUN, 10));
            ammoPickups.add(new AmmoPickup(dummy.getX() - 25, dummy.getY() + 25, AmmoType.SR, 5));
            healthPickups.add(new HealthPickup(dummy.getX(), dummy.getY() - 36, HealthPickup.HealthSize.LARGE));
        } else {
            tryDropEnemyCredits(); // Tỉ lệ nhỏ rơi tiền khi tiêu diệt mục tiêu
            tryDropEnemyAmmo(dummy.getX(), dummy.getY()); // Tỉ lệ nhỏ rơi hộp đạn khi tiêu diệt mục tiêu
        }

        // Diệt địch hồi máu (+2.5 HP tích lũy từ Kỹ năng Buff)
        int killHeal = player.getBuffManager().onEnemyKilled();
        if (killHeal > 0) {
            healPlayer(killHeal);
        }
    }

    public void onEnemyEliminated(TargetDummy dummy) {
        double angle = Math.atan2(dummy.getY() - player.getY(), dummy.getX() - player.getX());
        onEnemyEliminated(dummy, angle, 240.0);
    }

    /**
     * Renders dungeon terrain, doors, flying bullets, melee slashes, operative, targets, and tactical HUDs.
     */
    @Override
    public void render(Graphics2D g) {
        // 1. Render Visible Dungeon Map (with Viewport Culling & Door tiles)
        dungeonMap.render(g, camera);

        // 1.5. Render Enemy Corpses on dungeon floor (Xác đầu lâu nâu mờ trượt trên sàn)
        for (EnemyCorpse corpse : activeCorpses) {
            corpse.render(g, camera);
        }

        // 2. Render Hostile Combat Targets
        for (TargetDummy dummy : combatTargets) {
            dummy.render(g, camera);
        }

        // 3. Render Active Flying Projectiles
        projectileManager.render(g, camera);

        // 3.1 Render Heavy AoE Explosions (Shockwaves & fireballs from Boss Cannon)
        for (ExplosionVisual ev : activeExplosions) {
            ev.render(g, camera);
        }

        // 3.3 Render Danh sách Hộp Cứu Thương rơi trên sàn (Health Pickups)
        for (HealthPickup hp : healthPickups) {
            boolean inRange = hp.isPlayerInRange(player.getX(), player.getY(), player.getSize());
            hp.render(g, camera, inRange);
        }

        // 3.4 Render Danh sách Hộp đạn chiến thuật rơi trên sàn (Ammo Pickups)
        for (AmmoPickup pickup : ammoPickups) {
            pickup.render(g, camera);
        }

        // 3.5 Render Danh sách Hòm tiếp tế vũ khí (Weapon Chests)
        for (WeaponChest chest : weaponChests) {
            boolean inRange = chest.isPlayerInRange(player.getX(), player.getY(), player.getSize());
            chest.render(g, camera, inRange);
        }

        // 3.6 Render Danh sách Quầy hàng Shop (Tactical Field Shop)
        for (ShopItem item : shopItems) {
            boolean inRange = item.isPlayerInRange(player.getX(), player.getY(), player.getSize());
            item.render(g, camera, inRange);
        }

        // 3.7 Render Trạm Tẩy Luyện Chỉ Số (Stat Reforge Station)
        if (statReforgeStation != null) {
            statReforgeStation.render(g, camera, player.getX(), player.getY(), player.getSize(),
                    weaponManager.getActiveWeapon(), playerCredits);
        }

        // 3.8 Render 2 Trạm Nâng Cấp Nhân Vật (Basic Stats & Special Perks)
        if (basicUpgradeStation != null) {
            basicUpgradeStation.render(g, camera, player.getX(), player.getY(), player.getSize());
        }
        if (specialUpgradeStation != null) {
            specialUpgradeStation.render(g, camera, player.getX(), player.getY(), player.getSize());
        }

        // 4. Render Player Operative (Screen Space)
        player.render(g, camera);
        player.renderReloadIndicator(g, camera, weaponManager.getActiveWeapon());
        player.renderOutOfAmmoWarning(g, camera, weaponManager.isCurrentWeaponOutOfAmmo());

        // 5. Render Equipped Weapon & Melee Slash VFX
        weaponManager.render(g, camera, player.getCenterX(), player.getCenterY(), aimAngle);

        // 5.1. Render Critical Hit Visual Effects (Expanding golden rings, starbursts, sparks, and floating CRIT! numbers)
        for (CriticalHitVisual cv : activeCritVisuals) {
            cv.render(g, camera);
        }

        // 6. Render Top Sector Information HUD, Player Health Bar HUD, Credits HUD, and Boss Health Bar
        playingHUD.renderSectorHUD(g, currentStage, activeCombatRoom);
        playingHUD.renderPlayerHealthHUD(g, player);
        playingHUD.renderDodgeHUD(g, player);
        playingHUD.renderCreditsHUD(g, playerCredits);
        playingHUD.renderBuffsHUD(g, player);
        long activeMinions = combatTargets.stream().filter(d -> d.isAlive() && !d.isBoss()).count();
        playingHUD.renderBossHealthBar(g, bossDummy, activeMinions, bossMinionRespawnTimer);

        // 6.5 Render Minimap Radar HUD (Fog of War & Real-Time Exploration)
        minimapHUD.render(g, dungeonMap, player, currentStage);

        // 7. Render Bottom Weapon & Ammunition HUD
        weaponManager.renderHUD(g, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        // 8. Render Tactical Announcement Banner if active
        if (bannerTimer > 0 && !bannerMessage.isEmpty()) {
            boolean hasBoss = (bossDummy != null && bossDummy.isAlive());
            playingHUD.renderBanner(g, bannerMessage, bannerColor, hasBoss);
        }

        // 9. Floating Extract Interaction Prompt if standing on extract pad
        if (isStandingOnExtract && !isExtracting) {
            int screenPX = camera.worldToScreenX(player.getCenterX());
            int screenPY = camera.worldToScreenY(player.getCenterY());
            playingHUD.renderExtractPrompt(g, screenPX, screenPY, currentStage);
        }

        // 10. Extraction Victory Black Screen Overlay (Report Stage 0-1 Completed)
        if (isExtracting) {
            playingHUD.renderExtractionVictoryScreen(g);
        }

        // 10.4 Cảnh báo viền đỏ nguy hiểm khi máu thấp (<= 30%)
        if (player.getCurrentHealth() <= 30 && !player.isDead()) {
            playingHUD.renderLowHealthVignette(g);
        }

        // 10.6 Màn hình Game Over khi tử trận
        if (player.isDead()) {
            if (player.isDeathSequenceComplete()) {
                long cleared = dungeonMap.getCombatRooms().stream().filter(Room::isCleared).count();
                playingHUD.renderGameOverScreen(g, currentStage, MAX_STAGES, playerCredits, cleared, weaponManager.getWeaponSlots().size());
            } else {
                // Trong thời gian 2.0s đầu lâu đang trượt:
                // Phủ hiệu ứng chuyển cảnh điện ảnh mờ tối và viền đỏ bi tráng
                playingHUD.renderDeathVignetteTransition(g, player.getDeathSequenceProgress());
            }
        }

        // 11. Giao diện Balo chiến thuật (TAB Overlay)
        if (isBackpackOpen) {
            backpackUI.render(g, weaponManager, player, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        }

        // 12. Giao diện Menu Tạm dừng (Pause Menu UI Overlay)
        if (isPaused) {
            pauseMenuUI.render(g, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        }

        // 13. Giao diện Bảng Nâng Cấp Nhân Vật (Character Upgrade Modal UI Overlay)
        if (characterUpgradeUI.isVisible()) {
            characterUpgradeUI.render(g, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT, this);
        }

        // 14. Cắt cảnh giới thiệu Boss (Letterbox + banner) và hiệu ứng Fade chuyển stage — luôn vẽ trên cùng
        bossIntroCinematic.render(g);
        stageTransition.render(g);
    }

    public boolean isStandingOnExtract() {
        return isStandingOnExtract;
    }

    public boolean isExtracting() {
        return isExtracting;
    }

    public void setPlayerPosition(double x, double y) {
        player.setPosition(x, y);
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

    public PlayingHUD getPlayingHUD() {
        return playingHUD;
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

    public StatReforgeStation getStatReforgeStation() {
        return statReforgeStation;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
        if (paused) {
            pauseMenuUI.open();
        }
    }

    public PauseMenuUI getPauseMenuUI() {
        return pauseMenuUI;
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
        return player.getX();
    }

    public double getPlayerY() {
        return player.getY();
    }

    public void showTacticalBanner(String message, Color color, double duration) {
        showBanner(message, color, duration);
    }

    public void showTacticalBanner(String message, double duration, Color color) {
        showBanner(message, color, duration);
    }

    public TargetDummy getBossDummy() {
        return bossDummy;
    }

    /**
     * Helper hỗ trợ unit test khởi tạo Boss Dummy trong môi trường test mà không cần duyệt lockdown phòng.
     */
    public void spawnBossForTesting(double x, double y, int health, double size, String name) {
        BossTank boss = new BossTank(x, y, health, size, name);
        this.bossDummy = boss;
        this.combatTargets.add(boss);
    }



    /**
     * Gây sát thương cho chiến binh Operative.
     * Tự động kiểm tra thời gian bất tử (i-frames: 0.8s) để bảo vệ người chơi khỏi sát thương liên hoàn.
     */
    public void damagePlayer(int damage) {
        if (player.takeDamage(damage)) {
            screenShakeTimer = 0.2; // 0.2 giây rung màn hình phản hồi va chạm
            if (player.isDead()) {
                showBanner("☠ CHIẾN BINH ĐÃ TỬ TRẬN (OPERATIVE KIA) ☠", new Color(255, 50, 50), 5.0);
            }
        }
    }

    /**
     * Hồi phục máu cho chiến binh Operative, giới hạn tối đa bởi playerMaxHealth.
     */
    public void healPlayer(int amount) {
        player.heal(amount);
    }

    public Player getPlayer() {
        return player;
    }

    public double getPlayerSize() {
        return player.getSize();
    }

    public int getPlayerMaxHealth() {
        return player.getMaxHealth();
    }

    public void setPlayerMaxHealth(int maxHealth) {
        player.setMaxHealth(maxHealth);
    }

    public int getPlayerCurrentHealth() {
        return player.getCurrentHealth();
    }

    public void setPlayerCurrentHealth(int health) {
        player.setCurrentHealth(health);
    }

    public boolean isPlayerDead() {
        return player.isDead();
    }

    public void setPlayerDead(boolean dead) {
        player.setDead(dead);
    }

    public double getInvulnerableTimer() {
        return player.getInvulnerableTimer();
    }

    public void setInvulnerableTimer(double timer) {
        player.setInvulnerableTimer(timer);
    }

    public boolean triggerPlayerDodge(boolean up, boolean down, boolean left, boolean right) {
        return player.triggerDodge(up, down, left, right);
    }

    public boolean isPlayerDodging() {
        return player.isDodging();
    }

    public double getPlayerDodgeCooldownTimer() {
        return player.getDodgeCooldownTimer();
    }

    public double getAimAngle() {
        return aimAngle;
    }

    public List<HealthPickup> getHealthPickups() {
        return healthPickups;
    }

    public void triggerScreenShake(double duration) {
        this.screenShakeTimer = Math.max(this.screenShakeTimer, duration);
    }

    public List<ExplosionVisual> getActiveExplosions() {
        return activeExplosions;
    }

    public List<CriticalHitVisual> getActiveCritVisuals() {
        return activeCritVisuals;
    }

    public List<EnemyCorpse> getActiveCorpses() {
        return activeCorpses;
    }

    /**
     * Kích hoạt một vụ nổ bộc phá diện rộng (AoE Explosion) tại tọa độ thế giới (x, y).
     * Gây sát thương xuyên vật cản lên Operative nếu đứng trong bán kính nổ,
     * đồng thời gây rung chấn màn hình (screen shake) và sinh hiệu ứng visual.
     *
     * @param x       Tọa độ tâm vụ nổ X.
     * @param y       Tọa độ tâm vụ nổ Y.
     * @param radius  Bán kính sát thương và hiệu ứng (px).
     * @param damage  Lượng sát thương bộc phá.
     */
    public void triggerAreaExplosion(double x, double y, double radius, int damage) {
        double duration = radius >= 120.0 ? 0.58 : 0.45;
        activeExplosions.add(new ExplosionVisual(x, y, radius, duration));
        triggerScreenShake(radius >= 120.0 ? 0.48 : 0.35);

        // 1. Kiểm tra sát thương lên người chơi (xuyên cover/địa hình)
        double playerCenterX = player.getCenterX();
        double playerCenterY = player.getCenterY();
        double distSq = (playerCenterX - x) * (playerCenterX - x) + (playerCenterY - y) * (playerCenterY - y);
        if (distSq <= (radius + player.getSize() / 2.0) * (radius + player.getSize() / 2.0)) {
            damagePlayer(damage);
        }

        // 2. Tác động sát thương lan lên cả quái phụ xung quanh (nếu dính pháo)
        for (TargetDummy dummy : combatTargets) {
            if (dummy.isAlive() && !dummy.isBoss()) {
                double dummyCenterX = dummy.getX() + (dummy.getSize() / 2.0);
                double dummyCenterY = dummy.getY() + (dummy.getSize() / 2.0);
                double dDistSq = (dummyCenterX - x) * (dummyCenterX - x) + (dummyCenterY - y) * (dummyCenterY - y);
                if (dDistSq <= radius * radius) {
                    dummy.takeDamage(damage / 2, Math.atan2(dummyCenterY - y, dummyCenterX - x), 150.0);
                    if (!dummy.isAlive()) {
                        double fatalAngle = Math.atan2(dummyCenterY - y, dummyCenterX - x);
                        onEnemyEliminated(dummy, fatalAngle, 440.0);
                    }
                }
            }
        }
    }

    public double getBossMinionRespawnTimer() {
        return bossMinionRespawnTimer;
    }

    public void setBossMinionRespawnTimer(double timer) {
        this.bossMinionRespawnTimer = timer;
    }

    public int getBossMinionWaveCount() {
        return bossMinionWaveCount;
    }

    public CharacterUpgradeStation getBasicUpgradeStation() {
        return basicUpgradeStation;
    }

    public CharacterUpgradeStation getSpecialUpgradeStation() {
        return specialUpgradeStation;
    }

    public CharacterUpgradeUI getCharacterUpgradeUI() {
        return characterUpgradeUI;
    }

    public MinimapHUD getMinimapHUD() {
        return minimapHUD;
    }
}
