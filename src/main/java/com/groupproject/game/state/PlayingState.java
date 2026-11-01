package com.groupproject.game.state;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import com.groupproject.game.core.GameSoundManager;
import com.groupproject.game.core.GameSoundManager.SoundEffect;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.dungeon.Room;
import com.groupproject.game.entity.enemy.BossTank;
import com.groupproject.game.entity.enemy.EnemyCorpse;
import com.groupproject.game.entity.enemy.MedicEnemy;
import com.groupproject.game.entity.enemy.GrenadierEnemy;
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
import com.groupproject.game.ui.WeaponUpgradeModalUI;
import com.groupproject.game.ui.StatReforgeModalUI;
import com.groupproject.game.ui.MinimapHUD;
import com.groupproject.game.ui.PauseMenuUI;
import com.groupproject.game.ui.PlayingHUD;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import com.groupproject.game.dungeon.Tile;
import com.groupproject.game.dungeon.TileExplosionListener;
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
public class PlayingState extends GameState implements TileExplosionListener {

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
    private MouseHandler cachedMouseHandler = null;

    // Tactical announcement banner timer
    private String bannerMessage = "";
    private Color bannerColor = Color.WHITE;
    private double bannerTimer = 0.0;

    // Extraction zone interaction and victory transition
    private boolean isStandingOnExtract = false;
    private boolean isExtracting = false;
    private double extractTransitionTimer = 0.0;
    // Cờ đánh dấu chế độ chơi: true nếu là Boss Fight riêng biệt từ Menu; false nếu là Campaign vượt ải
    private boolean isStandaloneBossFight = false;

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
    // Cờ hiển thị thanh máu Boss: Boss được spawn sẵn từ đầu trong phòng, nhưng thanh máu
    // CHỈ ĐƯỢC PHÉP HIỂN THỊ khi người chơi thực sự bước vào phòng Boss hoặc trận chiến được kích hoạt.
    private boolean isBossHealthBarRevealed = false;
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

    // Hệ thống Modal UI Nâng Cấp Vũ Khí & Tái Phân Bổ (Reforge) Súng
    private final WeaponUpgradeModalUI weaponUpgradeModalUI = new WeaponUpgradeModalUI();
    private final StatReforgeModalUI statReforgeModalUI = new StatReforgeModalUI();

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
        Tile.setExplosionListener(this);
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

    // Dữ liệu checkpoint chờ nạp khi chuyển cảnh vào PlayingState
    private static com.groupproject.game.storage.RunProgressData pendingResumeData = null;

    public static void setPendingResumeData(com.groupproject.game.storage.RunProgressData data) {
        pendingResumeData = data;
        if (data != null) {
            spawnDirectlyInBossRoom = false;
        }
    }

    public static com.groupproject.game.storage.RunProgressData getPendingResumeData() {
        return pendingResumeData;
    }

    public static boolean hasPendingResumeData() {
        return pendingResumeData != null;
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
        Tile.setExplosionListener(this);
        if (pendingResumeData != null) {
            this.isStandaloneBossFight = false;
            spawnDirectlyInBossRoom = false;
            com.groupproject.game.storage.RunProgressData resumeData = pendingResumeData;
            pendingResumeData = null; // Đảm bảo chỉ nạp 1 lần khi chuyển state
            resumeFromSave(resumeData);
            return;
        }

        if (spawnDirectlyInBossRoom) {
            this.isStandaloneBossFight = true;
            spawnDirectlyInBossRoom = false; // Reset cờ tĩnh sau khi đã khởi tạo
            loadBossStageDirectly();
            return;
        }

        this.isStandaloneBossFight = false;

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

        clearStageTransientEntities();

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

        populateAllFieldStations();

        // Reset cắt cảnh: run mới không được giữ lại intro Boss/transition của run trước
        bossIntroTriggered = false;
        isBossHealthBarRevealed = false;
        bossIntroCinematic.skip();

        GameSoundManager.playStageBgm(this.currentStage);
        showBanner("STAGE 1", new Color(60, 220, 160), 2.0);

        // Center camera on operative immediately on mission start
        camera.update(player.getCenterX(), player.getCenterY());
    }

    /**
     * Dọn sạch toàn bộ thực thể động tạm thời (đạn, quái, hiệu ứng nổ, hòm đồ, vật phẩm rơi) khi chuyển/khởi tạo màn.
     */
    private void clearStageTransientEntities() {
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
        pendingBarrelExplosions.clear();
        isProcessingExplosion = false;
    }

    /**
     * Khởi tạo đồng bộ toàn bộ các trạm dịch vụ dã chiến (Shop, Trạm Tẩy Luyện, Trạm Nâng Cấp) cho Stage hiện tại.
     */
    private void populateAllFieldStations() {
        populateShopItems();
        populateResetStatStation();
        populateUpgradeStations();
    }

    /**
     * Tải và spawn thẳng người chơi vào Grand Boss Arena (Stage 4) đối đầu Chiến Xa Titan.
     * Cung cấp sẵn trang bị vũ khí hỏa lực cao (AR Cấp 3, Shotgun Cấp 3, đầy ắp đạn) và 500 Credits.
     */
    public void loadBossStageDirectly() {
        this.isStandaloneBossFight = true;
        this.currentStage = BOSS_STAGE;
        this.playerCredits = 500;
        dungeonMap.generateStage(BOSS_STAGE);

        clearStageTransientEntities();

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

        populateAllFieldStations();

        showBanner("STAGE 4: BOSS ARENA", new Color(255, 60, 60), 2.0);

        // Spawn thẳng vào phòng Boss: boss đã được sinh ở trên nên phát cắt cảnh giới thiệu ngay
        bossIntroTriggered = false;
        isBossHealthBarRevealed = true;
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
        isBossHealthBarRevealed = false; // Thanh máu Boss chỉ hiện khi người chơi bước vào phòng Boss
        GameSoundManager.playStageBgm(currentStage);
        dungeonMap.generateStage(currentStage);

        Room spawnRoom = dungeonMap.getSpawnRoom();
        if (spawnRoom != null) {
            player.setPosition(spawnRoom.getCenterWorldX() - (player.getSize() / 2.0),
                               spawnRoom.getCenterWorldY() - (player.getSize() / 2.0));
        } else {
            player.setPosition(GameConfig.WORLD_WIDTH / 2.0, GameConfig.WORLD_HEIGHT / 2.0);
        }

        clearStageTransientEntities();
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

        populateAllFieldStations();

        camera.update(player.getCenterX(), player.getCenterY());

        if (!sTierUpgrades.isEmpty()) {
            String msg = "[S] KHÍ TÀI TỰ LÊN CẤP: " + String.join(", ", sTierUpgrades);
            showBanner(msg, new Color(255, 215, 60), 3.5);
        } else if (currentStage == BOSS_STAGE) {
            showBanner("STAGE 4: BOSS ARENA", new Color(255, 70, 70), 2.0);
        } else {
            showBanner("STAGE " + currentStage, new Color(60, 220, 160), 2.0);
        }

        // Nếu chuyển sang Stage Boss: Tiền sản sinh sẵn chiếc xe tăng BossTank trong phòng Boss
        // để xe tăng hiện diện sẵn sàng ngay từ đầu, không dùng portal spawn
        if (currentStage == BOSS_STAGE && !dungeonMap.getCombatRooms().isEmpty()) {
            Room bossRoom = dungeonMap.getCombatRooms().get(0);
            double cx = bossRoom.getCenterWorldX();
            double cy = bossRoom.getCenterWorldY();
            spawnBossSafely(bossRoom, cx, cy, 1250, 96.0, "HEAVY COMBAT BATTLE TANK [BOSS]");
        }
    }

    /** Bỏ qua tức thì cắt cảnh giới thiệu Boss (dùng cho test và debug). */
    public void skipBossIntro() {
        boolean wasActive = bossIntroCinematic.isActive();
        bossIntroCinematic.skip();
        if (wasActive || bossIntroTriggered) {
            GameSoundManager.transitionBossIntroToLoop();
        }
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
        isBossHealthBarRevealed = true;
        bossIntroCinematic.start(bossDummy, player);
        GameSoundManager.startBossIntroBgm(2.8);
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
     * Sinh một kẻ địch hỗ trợ hồi máu (Combat Field Medic) an toàn vào danh sách.
     * Xuất hiện từ Stage 2 trở đi:
     * - Máu: 40 HP (tăng +10 HP mỗi Stage sau Stage 2).
     * - Tốc độ: 115 px/s (tăng +5 px/s mỗi Stage sau Stage 2).
     * - Hào quang hồi máu (Healing Aura): Bán kính 135px, hồi 6 HP (+1 HP/Stage) mỗi 0.8s cho đồng đội.
     */
    private void spawnMedicEnemySafely(Room arena, double preferredX, double preferredY, int baseHealth) {
        Point2D.Double safePos = findSafeSpawnLocation(arena, preferredX, preferredY, 30.0, combatTargets);
        int stageBonus = Math.max(0, currentStage - 2);
        int scaledHealth = baseHealth + stageBonus * 10;
        double scaledSpeed = 115.0 + stageBonus * 5.0;
        int scaledHeal = 6 + stageBonus * 1;
        String name = stageBonus == 0 ? "Combat Field Medic [Support]" : String.format("Combat Field Medic MK-%d [Support]", currentStage);

        MedicEnemy enemy = new MedicEnemy(safePos.x, safePos.y, scaledHealth, scaledSpeed, 135.0, scaledHeal, 0.80, 210.0, name);
        enemy.setSpawnTelegraph(0.40);
        combatTargets.add(enemy);
    }

    /**
     * Sinh một kẻ địch ném lựu đạn hẹn giờ (Heavy Grenadier) an toàn vào danh sách.
     * Xuất hiện từ Stage 3 trở đi:
     * - Máu: 55 HP (tăng +12 HP mỗi Stage sau Stage 3).
     * - Tốc độ: 95 px/s.
     * - Lựu đạn bộc phá hẹn giờ: Đúng 2.0 giây phát nổ, sát thương 24 HP (+2 DMG/Stage), bán kính 78px.
     */
    private void spawnGrenadierEnemySafely(Room arena, double preferredX, double preferredY, int baseHealth) {
        Point2D.Double safePos = findSafeSpawnLocation(arena, preferredX, preferredY, 30.0, combatTargets);
        int stageBonus = Math.max(0, currentStage - 3);
        int scaledHealth = baseHealth + stageBonus * 12;
        double scaledSpeed = 95.0 + stageBonus * 4.0;
        int scaledDamage = 24 + stageBonus * 2;
        String name = stageBonus == 0 ? "Heavy Grenadier [Demolitions]" : String.format("Heavy Grenadier MK-%d [Demolitions]", currentStage);

        GrenadierEnemy enemy = new GrenadierEnemy(safePos.x, safePos.y, scaledHealth, scaledSpeed, scaledDamage, 78.0, 3.0, 175.0, 400.0, name);
        enemy.setSpawnTelegraph(0.40);
        combatTargets.add(enemy);
    }

    /**
     * Sinh Boss Dummy kích thước lớn (size 96px, thanh máu dày) an toàn vào danh sách và lưu tham chiếu boss.
     */
    private void spawnBossSafely(Room arena, double preferredX, double preferredY, int health, double size, String name) {
        Point2D.Double safePos = findSafeSpawnLocation(arena, preferredX, preferredY, size, combatTargets);
        BossTank boss = new BossTank(safePos.x, safePos.y, health, size, name);
        boss.setSpawnTelegraph(0.0); // Không hiển thị portal telegraph, hiện diện trực tiếp cỗ xe tăng bọc thép
        this.bossDummy = boss;
        combatTargets.add(boss);
    }

    /**
     * Sinh các mục tiêu chiến đấu an toàn cho từng phòng combat theo Phase chỉ định.
     * @param arena Phòng chiến đấu.
     * @param phase Số thứ tự phase (1 hoặc 2).
     */
    private void spawnTargetsForRoomPhase(Room arena, int phase) {
        if (arena == null) return;

        double cx = arena.getCenterWorldX();
        double cy = arena.getCenterWorldY();

        if (currentStage == BOSS_STAGE) {
            spawnBossArenaSquad(arena, cx, cy);
        } else {
            combatTargets.clear();
            this.bossDummy = null;
            if (arena.getTotalPhases() <= 1) {
                spawnSinglePhaseSquad(arena, cx, cy);
            } else {
                spawnTwoPhaseSquad(arena, phase, cx, cy);
            }
        }
    }

    private void spawnBossArenaSquad(Room arena, double cx, double cy) {
        // Stage Boss (Stage 4) Grand Boss Arena: Xe tăng chiến đấu hạng nặng (size 96px, 1250 HP) và 8 AI hộ tống
        // Đội hình tinh nhuệ đa dạng: 3 Cận chiến + 3 Súng lục + 1 Combat Medic hồi máu + 1 Heavy Grenadier ép góc
        BossTank existingBoss = (this.bossDummy instanceof BossTank bt) ? bt : null;
        combatTargets.clear();
        if (existingBoss != null && existingBoss.isAlive()) {
            this.bossDummy = existingBoss;
            combatTargets.add(existingBoss);
        } else {
            spawnBossSafely(arena, cx, cy, 1250, 96.0, "HEAVY COMBAT BATTLE TANK [BOSS]");
        }
        spawnMeleeEnemySafely(arena, cx + 220, cy, 80);
        spawnMeleeEnemySafely(arena, cx - 220, cy, 80);
        spawnRangedPistolEnemySafely(arena, cx, cy + 180, 60);
        spawnRangedPistolEnemySafely(arena, cx, cy - 180, 60);
        spawnMeleeEnemySafely(arena, cx + 160, cy + 130, 70);
        spawnRangedPistolEnemySafely(arena, cx - 160, cy + 130, 60);
        spawnMedicEnemySafely(arena, cx + 160, cy - 130, 60);
        spawnGrenadierEnemySafely(arena, cx - 160, cy - 130, 70);
        this.bossMinionRespawnTimer = BOSS_MINION_RESPAWN_DELAY;
        this.bossMinionWaveCount = 0;
    }

    private void spawnSinglePhaseSquad(Room arena, double cx, double cy) {
        spawnMeleeEnemySafely(arena, cx - 120, cy - 60, 50);
        spawnRangedPistolEnemySafely(arena, cx + 120, cy + 60, 45);

        if (currentStage == 1) {
            spawnMeleeEnemySafely(arena, cx + 120, cy - 80, 50);
            if (arena.getWidthInTiles() >= 13) {
                spawnRangedPistolEnemySafely(arena, cx - 140, cy + 70, 45);
            }
        } else if (currentStage == 2) {
            spawnMeleeEnemySafely(arena, cx + 120, cy - 80, 50);
            spawnMedicEnemySafely(arena, cx - 140, cy + 70, 40);
        } else {
            spawnMedicEnemySafely(arena, cx + 120, cy - 80, 40);
            spawnGrenadierEnemySafely(arena, cx - 140, cy + 70, 55);
        }
    }

    private void spawnTwoPhaseSquad(Room arena, int phase, double cx, double cy) {
        if (phase == 1) {
            spawnMeleeEnemySafely(arena, cx - 120, cy - 60, 50);
            spawnRangedPistolEnemySafely(arena, cx + 120, cy + 60, 45);

            if (currentStage == 1) {
                if (arena.getWidthInTiles() >= 13) {
                    spawnMeleeEnemySafely(arena, cx + 120, cy - 80, 50);
                }
            } else if (currentStage == 2) {
                spawnMedicEnemySafely(arena, cx + 120, cy - 80, 40);
            } else {
                spawnMedicEnemySafely(arena, cx + 120, cy - 80, 40);
                spawnGrenadierEnemySafely(arena, cx - 140, cy + 70, 55);
            }
        } else {
            spawnMeleeEnemySafely(arena, cx - 140, cy + 70, 55);
            spawnRangedPistolEnemySafely(arena, cx + 140, cy - 70, 45);

            if (currentStage == 1) {
                spawnMeleeEnemySafely(arena, cx, cy - 90, 55);
                if (arena.getWidthInTiles() >= 13) {
                    spawnRangedPistolEnemySafely(arena, cx, cy + 90, 45);
                }
            } else if (currentStage == 2) {
                spawnMeleeEnemySafely(arena, cx, cy - 90, 55);
                spawnMedicEnemySafely(arena, cx, cy + 90, 40);
            } else {
                spawnMedicEnemySafely(arena, cx, cy - 90, 40);
                spawnGrenadierEnemySafely(arena, cx, cy + 90, 55);
            }
        }
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
     * Sinh thêm 5-6 lính tiếp viện bảo vệ Boss (kết hợp cận chiến, súng lục, medic và grenadier)
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
            int type = i % 4;
            if (type == 0) {
                spawnMeleeEnemySafely(arena, cx + ox, cy + oy, 75);
            } else if (type == 1) {
                spawnRangedPistolEnemySafely(arena, cx + ox, cy + oy, 60);
            } else if (type == 2) {
                spawnMedicEnemySafely(arena, cx + ox, cy + oy, 55);
            } else {
                spawnGrenadierEnemySafely(arena, cx + ox, cy + oy, 65);
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
     * Vòng lặp cập nhật chính của màn chơi (Main Game Loop Orchestrator).
     * <p>
     * THIẾT KẾ GIẢM ĐỘ PHỨC TẠP:
     * Thay vì một hàm khổng lồ hàng trăm dòng lồng nhau, update() đóng vai trò điều phối (Orchestrator)
     * tuần tự theo mức độ ưu tiên rõ ràng:
     * <ol>
     *   <li>Ưu tiên cao nhất: Cinematic, Pause Menu, Modal nâng cấp, Chuỗi tử trận, Chuỗi sơ tán, Balo.</li>
     *   <li>Phím tắt hệ thống và bộ đếm thời gian.</li>
     *   <li>Điều khiển người chơi (Vũ khí, Lướt né, Di chuyển).</li>
     *   <li>Bản đồ, Radar, và Cơ chế phong tỏa phòng chiến đấu.</li>
     *   <li>Camera bám theo, góc ngắm 360° và hành động tấn công.</li>
     *   <li>Cập nhật các thực thể động (Đạn, Vụ nổ, Tia crit, Xác quái).</li>
     *   <li>Giải quyết va chạm và tính toán sát thương.</li>
     *   <li>Kiểm tra hoàn thành phòng và trao thưởng chiến thuật.</li>
     *   <li>Tương tác chiến trường (Hòm tiếp tế, Hộp đạn, Hộp máu, Shop, Nâng cấp, Sơ tán).</li>
     * </ol>
     */
    @Override
    public void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH) {
        // --- 1. CÁC TRẠNG THÁI ƯU TIÊN CAO (ĐÓNG BĂNG CHIẾN TRƯỜNG KHI KÍCH HOẠT) ---
        if (handleCinematics(deltaTime, keyH, mouseH)) return;
        if (handlePauseMenu(deltaTime, keyH, mouseH)) return;
        if (handleModalMenus(deltaTime, keyH, mouseH)) return;
        if (handlePlayerDeathSequence(deltaTime)) return;
        if (handleExtractionSequence(deltaTime, keyH, mouseH)) return;
        if (handleTacticalBackpack(deltaTime, keyH, mouseH)) return;

        // --- 2. PHÍM TẮT HỆ THỐNG & CẬP NHẬT ĐỊNH THỜI ---
        if (handleSystemShortcuts(keyH)) return;
        updateBannerAndTimers(deltaTime);

        // --- 3. ĐIỀU KHIỂN NHÂN VẬT (VŨ KHÍ, LƯỚT NÉ, DI CHUYỂN) ---
        handlePlayerLocomotionAndSkills(deltaTime, keyH);

        // --- 4. BẢN ĐỒ, RADAR & PHÁT HIỆN BƯỚC VÀO PHÒNG CHIẾN ĐẤU ---
        updateFogOfWarAndRadar(deltaTime);
        updateCombatRoomLockdownDetection();

        // --- 5. CAMERA BÁM THEO, TÍNH GÓC NGẮM & THAO TÁC TẤN CÔNG ---
        updateCameraAndAim(mouseH);
        handleWeaponCombatInput(keyH, mouseH);

        // --- 6. CẬP NHẬT THỰC THỂ CHIẾN TRƯỜNG (ĐẠN, VỤ NỔ, CRIT, XÁC QUÁI) ---
        updateCombatVisualsAndProjectiles(deltaTime);

        // --- 7. GIẢI QUYẾT VA CHẠM SÁT THƯƠNG & LÍNH TIẾP VIỆN BOSS ---
        updateCombatDamageResolution(deltaTime);
        updateBossMinionReinforcements(deltaTime);

        // --- 8. KIỂM TRA QUÉT SẠCH PHÒNG CHIẾN ĐẤU & PHÂN PHỐI CHIẾN LỢI PHẨM ---
        updateRoomClearAndRewards();

        // --- 9. TƯƠNG TÁC CHIẾN TRƯỜNG (HÒM, HỘP ĐẠN, MÁU, SHOP, TRẠM DỊCH VỤ, SƠ TÁN) ---
        boolean fieldInteracted = handleFieldInteractions(keyH, deltaTime);
        handleExtractionZoneInteraction(keyH, fieldInteracted);
    }

    /**
     * Xử lý các chuỗi hoạt cảnh điện ảnh (Cinematic) và chuyển màn.
     * WHY: Khi đang mờ dần chuyển cảnh hoặc lia camera giới thiệu Boss (Cinematic Camera Pan),
     * cần đóng băng gameplay để người chơi không bị tấn công bất ngờ.
     *
     * @return true nếu đang trong chuỗi Cinematic và cần tạm dừng vòng lặp game chính.
     */
    private boolean handleCinematics(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH) {
        stageTransition.update(deltaTime);
        if (stageTransition.isBlockingInput()) {
            return true;
        }
        if (bossIntroCinematic.isActive()) {
            bossIntroCinematic.handleInput(keyH, mouseH); // Cho phép bấm để bỏ qua (Skip)
            bossIntroCinematic.update(deltaTime);
            camera.update(bossIntroCinematic.getCameraFocusX(), bossIntroCinematic.getCameraFocusY());
            if (!bossIntroCinematic.isActive()) {
                GameSoundManager.transitionBossIntroToLoop();
            }
            return true;
        }
        return false;
    }

    /**
     * Xử lý Menu Tạm Dừng (Pause Menu) khi người chơi bấm ESC hoặc phím gán PAUSE_MENU.
     * WHY: Đóng băng toàn bộ hoạt động chiến trường để người chơi có thể điều chỉnh cài đặt,
     * tiếp tục ván đấu, hoặc lưu tiến trình và thoát về Menu chính.
     *
     * @return true nếu Menu Tạm Dừng đang mở và cần đóng băng logic gameplay.
     */
    private boolean handlePauseMenu(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH) {
        if (!isPaused) {
            return false;
        }

        if (KeyBindings.isActionPressedOnce(keyH, GameAction.PAUSE_MENU) || keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
            PauseMenuUI.PauseAction escAction = pauseMenuUI.handleEsc();
            if (escAction == PauseMenuUI.PauseAction.RESUME) {
                isPaused = false;
            }
            return true;
        }

        PauseMenuUI.PauseAction action = pauseMenuUI.update(keyH, mouseH, deltaTime);
        if (action == PauseMenuUI.PauseAction.RESUME) {
            isPaused = false;
        } else if (action == PauseMenuUI.PauseAction.EXIT_TO_MENU) {
            isPaused = false;
            if (!isStandaloneBossFight && !player.isDead() && !isExtracting) {
                com.groupproject.game.storage.RunSaveManager.saveRun(this);
            }
            gsm.setState(GameStateManager.StateType.MENU);
        }
        return true;
    }

    /**
     * Xử lý các cửa sổ giao diện Modal tương tác (Nâng cấp nhân vật, Nâng cấp vũ khí, Tẩy luyện chỉ số).
     * WHY: Đóng băng vòng lặp chiến đấu để người chơi có thể an tâm đọc thông số và đưa ra quyết định
     * nâng cấp mà không bị kẻ địch tấn công từ sau lưng.
     *
     * @return true nếu có ít nhất một cửa sổ Modal đang hiển thị.
     */
    private boolean handleModalMenus(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH) {
        if (characterUpgradeUI.isVisible()) {
            characterUpgradeUI.update(keyH, mouseH, this, deltaTime);
            return true;
        }
        if (weaponUpgradeModalUI.isVisible()) {
            weaponUpgradeModalUI.update(keyH, mouseH, playerCredits, deltaTime);
            return true;
        }
        if (statReforgeModalUI.isVisible()) {
            statReforgeModalUI.update(keyH, mouseH, playerCredits, deltaTime);
            return true;
        }
        return false;
    }

    /**
     * Xử lý chuỗi hoạt cảnh khi người chơi tử trận (Game Over / Death Sequence).
     * WHY: Cho phép đầu lâu của nhân vật trượt ma sát trên sàn và va chạm tường theo quán tính thực tế,
     * camera mượt mà bám theo, trước khi tự động chuyển cảnh sang màn hình Căn Cứ Tiền Phương.
     *
     * @return true nếu người chơi đã hy sinh.
     */
    private boolean handlePlayerDeathSequence(double deltaTime) {
        if (!player.isDead()) {
            return false;
        }

        // Cập nhật hoạt cảnh đầu lâu trượt ma sát trên sàn và va chạm tường
        player.update(deltaTime, dungeonMap);

        // Camera vẫn mượt mà bám theo đầu lâu đang trượt
        camera.update(player.getCenterX(), player.getCenterY());

        if (bannerTimer > 0) {
            bannerTimer -= deltaTime;
        }

        // Khi hoạt cảnh trượt tử trận hoàn tất (màn hình đã đen hoàn toàn):
        // Xóa file lưu và kích hoạt transition mở sáng dần từ bóng tối vào The Range (hoặc Menu nếu là Boss Fight riêng)
        if (player.isDeathSequenceComplete()) {
            com.groupproject.game.storage.RunSaveManager.deleteSave();
            if (isStandaloneBossFight) {
                gsm.transitionFromBlack(GameStateManager.StateType.MENU);
            } else {
                gsm.transitionFromBlack(GameStateManager.StateType.THE_RANGE);
            }
        }
        return true;
    }

    /**
     * Xử lý chuỗi sự kiện khi hoàn tất màn chơi cuối cùng và bắt đầu sơ tán thành công (Victory Extraction).
     * WHY: Đóng băng chiến trường và hiển thị màn hình chúc mừng kết thúc Demo, chờ người chơi bấm phím
     * để quay về căn cứ The Range hoặc Menu chính.
     *
     * @return true nếu đang trong trạng thái sơ tán.
     */
    private boolean handleExtractionSequence(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH) {
        if (!isExtracting) {
            return false;
        }

        extractTransitionTimer += deltaTime;

        // Bấm nút bất kỳ (ENTER, SPACE, ESC, F, ...) hoặc Click chuột để tiếp tục
        boolean userConfirmed = keyH.isKeyPressedOnce(KeyEvent.VK_ENTER)
                || keyH.isKeyPressedOnce(KeyEvent.VK_SPACE)
                || keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)
                || KeyBindings.isActionPressedOnce(keyH, GameAction.INTERACT)
                || (mouseH != null && mouseH.isLeftClickedOnce())
                || extractTransitionTimer >= 180.0;

        if (userConfirmed) {
            com.groupproject.game.storage.RunSaveManager.deleteSave();
            if (isStandaloneBossFight) {
                gsm.transitionTo(GameStateManager.StateType.MENU);
            } else {
                gsm.transitionTo(GameStateManager.StateType.THE_RANGE, "CĂN CỨ TIỀN PHƯƠNG", "THE RANGE");
            }
        }
        return true;
    }

    /**
     * Xử lý giao diện Balo Chiến Thuật (Tactical Backpack Overlay) khi bấm phím TAB.
     * WHY: Cho phép xem kho trang bị, chỉ số vũ khí và hoán đổi vị trí vũ khí Slot 1 - Slot 2 trong môi trường an toàn.
     *
     * @return true nếu Balo đang mở và cần đóng băng logic chiến đấu.
     */
    private boolean handleTacticalBackpack(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH) {
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.BACKPACK)) {
            GameSoundManager.play(SoundEffect.UI_INTERACT);
            isBackpackOpen = !isBackpackOpen;
            return isBackpackOpen;
        }

        if (isBackpackOpen) {
            if (KeyBindings.isActionPressedOnce(keyH, GameAction.PAUSE_MENU) || keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
                GameSoundManager.play(SoundEffect.UI_INTERACT);
                isBackpackOpen = false;
                return true;
            }
            boolean closeRequested = backpackUI.update(keyH, mouseH, weaponManager, player, deltaTime);
            if (closeRequested) {
                isBackpackOpen = false;
            }
            return true;
        }
        return false;
    }

    /**
     * Xử lý các phím tắt hệ thống (Mở Menu Tạm dừng, Phím tắt debug F2).
     *
     * @return true nếu đã xử lý phím tắt và không cần chạy tiếp các logic điều khiển bên dưới.
     */
    private boolean handleSystemShortcuts(KeyboardHandler keyH) {
        // Mở Menu Tạm Dừng khi bấm PAUSE_MENU hoặc ESC
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.PAUSE_MENU) || keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
            isPaused = true;
            pauseMenuUI.open();
            return true;
        }

        // Phím tắt F2: Khởi động lại / Respawn ngay tại phòng Boss để kiểm thử nhanh
        if (keyH.isKeyPressedOnce(KeyEvent.VK_F2)) {
            loadBossStageDirectly();
            return true;
        }

        return false;
    }

    /**
     * Cập nhật bộ đếm thời gian cho banner thông báo chiến thuật, rung màn hình và các hiệu ứng định thời.
     */
    private void updateBannerAndTimers(double deltaTime) {
        if (bannerTimer > 0) {
            bannerTimer -= deltaTime;
        }
        if (screenShakeTimer > 0) {
            screenShakeTimer -= deltaTime;
        }
    }

    /**
     * Xử lý điều khiển người chơi: chọn phím tắt vũ khí, lướt né đòn (Tactical Dodge), và di chuyển WASD.
     * WHY: Tách riêng phần nhận diện input điều khiển nhân vật khỏi logic chiến đấu và va chạm đạn.
     */
    private void handlePlayerLocomotionAndSkills(double deltaTime, KeyboardHandler keyH) {
        // Cập nhật hồi chiêu, trạng thái lướt né và bộ đếm Buff
        player.update(deltaTime, dungeonMap);
        player.getBuffManager().update(deltaTime, player, weaponManager);

        // 1. Phím tắt trang bị và nạp đạn vũ khí
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.SLOT_1)) {
            weaponManager.selectSlot(1);
        }
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.SLOT_2)) {
            weaponManager.selectSlot(2);
        }
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.SLOT_3)) {
            weaponManager.selectSlot(3);
        }
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.SLOT_4)) {
            weaponManager.selectSlot(4);
        }
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.QUICK_SWAP)) {
            weaponManager.quickSwap();
        }
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.RELOAD)) {
            weaponManager.reload();
        }

        // 2. Kỹ năng lướt né đòn chiến thuật [DODGE / SPACE]
        if (KeyBindings.isActionPressedOnce(keyH, GameAction.DODGE)) {
            player.triggerDodge(
                    KeyBindings.isActionDown(keyH, GameAction.MOVE_UP),
                    KeyBindings.isActionDown(keyH, GameAction.MOVE_DOWN),
                    KeyBindings.isActionDown(keyH, GameAction.MOVE_LEFT),
                    KeyBindings.isActionDown(keyH, GameAction.MOVE_RIGHT)
            );
        }

        // 3. Di chuyển có trượt va chạm tách trục (Decoupled Axis Collision Sliding)
        player.handleMovement(
                KeyBindings.isActionDown(keyH, GameAction.MOVE_UP),
                KeyBindings.isActionDown(keyH, GameAction.MOVE_DOWN),
                KeyBindings.isActionDown(keyH, GameAction.MOVE_LEFT),
                KeyBindings.isActionDown(keyH, GameAction.MOVE_RIGHT),
                deltaTime,
                dungeonMap
        );
    }

    /**
     * Cập nhật cơ chế sương mù khám phá (Fog of War) và radar bản đồ nhỏ (Minimap HUD).
     */
    private void updateFogOfWarAndRadar(double deltaTime) {
        dungeonMap.updateDiscovery(player.getCenterX(), player.getCenterY());
        minimapHUD.update(deltaTime);
    }

    /**
     * Phát hiện khi người chơi bước chân vào phòng chiến đấu mới chưa viếng thăm.
     * WHY: Khi bước vào phòng, tự động đẩy nhẹ người chơi vào trong lòng phòng để tránh kẹt cửa,
     * đóng kín các cửa phòng (Lockdown), kích hoạt Phase 1 và sinh các mục tiêu thù địch.
     */
    private void updateCombatRoomLockdownDetection() {
        if (activeCombatRoom != null) {
            return;
        }

        for (Room cr : dungeonMap.getCombatRooms()) {
            if (cr.getStatus() == Room.RoomStatus.UNVISITED
                    && cr.hasEntityEntered(player.getX(), player.getY(), player.getSize(), player.getSize())) {

                activeCombatRoom = cr;

                // Giới hạn tọa độ bên trong phòng để người chơi không bị kẹt khi cửa sập xuống
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
                if (currentStage == BOSS_STAGE) {
                    isBossHealthBarRevealed = true;
                }
                tryStartBossIntro(); // Kích hoạt Intro cinematic nếu là màn Boss

                if (currentStage != BOSS_STAGE) {
                    showBanner("FIGHT!", new Color(255, 65, 65), 1.5);
                }
                break;
            }
        }
    }

    /**
     * Cập nhật vị trí Camera bám theo người chơi (kèm hiệu ứng rung lắc) và tính góc ngắm 360 độ từ con trỏ chuột.
     */
    private void updateCameraAndAim(MouseHandler mouseH) {
        double camCX = player.getCenterX();
        double camCY = player.getCenterY();
        if (screenShakeTimer > 0) {
            camCX += ThreadLocalRandom.current().nextDouble(-3.0, 3.0);
            camCY += ThreadLocalRandom.current().nextDouble(-3.0, 3.0);
        }
        camera.update(camCX, camCY);

        this.cachedMouseHandler = mouseH;
        double mouseWorldX = camera.screenToWorldX(mouseH.getScreenX());
        double mouseWorldY = camera.screenToWorldY(mouseH.getScreenY());
        aimAngle = Math.atan2(mouseWorldY - player.getCenterY(), mouseWorldX - player.getCenterX());
        player.setAimAngle(aimAngle);
    }

    /**
     * Xử lý thao tác bắn vũ khí chính và đòn cận chiến nhanh (Quick-Melee).
     * WHY: Súng tự động (AR/SMG) cho phép đè giữ nút bắn; súng bán tự động (Pistol/Shotgun) cần nhấp nhả từng phát.
     * Khi vung dao cận chiến nhanh, tạm dừng bắn súng để tập trung đòn cận chiến.
     */
    private void handleWeaponCombatInput(KeyboardHandler keyH, MouseHandler mouseH) {
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

        // Đòn chém dao cận chiến tức thì khi bấm phím Quick-Melee (mặc định Chuột Phải)
        if (isQuickMeleeActive) {
            weaponManager.quickMelee(player.getCenterX(), player.getCenterY(), aimAngle, projectileManager);
        }
    }

    /**
     * Cập nhật đạn bay, hoạt cảnh vụ nổ, tia bộc phá bạo kích và xác quái trượt trên sàn.
     */
    private void updateCombatVisualsAndProjectiles(double deltaTime) {
        weaponManager.update(deltaTime);
        projectileManager.update(deltaTime, dungeonMap, this);

        // Cập nhật và dọn dẹp các vụ nổ đã hoàn tất
        for (ExplosionVisual ev : activeExplosions) {
            ev.update(deltaTime);
        }
        activeExplosions.removeIf(ExplosionVisual::isFinished);

        // Cập nhật và dọn dẹp các hiệu ứng bạo kích (Critical Hits)
        for (CriticalHitVisual cv : activeCritVisuals) {
            cv.update(deltaTime);
        }
        activeCritVisuals.removeIf(CriticalHitVisual::isFinished);

        // Cập nhật xác quái trượt quán tính trên sàn và mờ dần
        for (EnemyCorpse corpse : activeCorpses) {
            corpse.update(deltaTime, dungeonMap);
        }
        activeCorpses.removeIf(EnemyCorpse::isFinished);
    }

    /**
     * Kiểm tra điều kiện quét sạch quái trong phòng chiến đấu để mở khóa cửa phong tỏa
     * hoặc kích hoạt đợt quái tiếp viện (Phase 2), đồng thời phân phối phần thưởng chiến thuật.
     */
    private void updateRoomClearAndRewards() {
        if (activeCombatRoom == null || !activeCombatRoom.isLocked() || !combatTargets.isEmpty()) {
            return;
        }

        if (activeCombatRoom.hasNextPhase()) {
            // Đợt quái tiếp viện (Phase 2): Cửa vẫn phong tỏa, sinh thêm quái và rung màn hình cảnh báo
            activeCombatRoom.advancePhase();
            int nextPhase = activeCombatRoom.getCurrentPhase();
            spawnTargetsForRoomPhase(activeCombatRoom, nextPhase);
            screenShakeTimer = 0.20;
        } else {
            // Quét sạch toàn bộ các đợt: Mở khóa cửa và trao thưởng
            activeCombatRoom.unlock();
            int clearReward = switch (currentStage) {
                case 1 -> 10; // Giảm nhẹ ở màn 1 (10 Credits)
                case 2 -> 16; // Tăng dần ở màn 2 (16 Credits)
                case 3 -> 22; // Tăng dần ở màn 3 (22 Credits)
                default -> 30; // Boss Stage / Thử thách cao cấp (30 Credits)
            };
            addCredits(clearReward);

            // Rơi ra hòm tiếp tế vũ khí tại phòng chiến đấu vừa dọn sạch
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

            // Rơi 2 stack đạn ngẫu nhiên đối xứng 2 bên hòm thưởng
            spawnClearRoomAmmoStacks(chestCenterX, chestCenterY);

            // Tỉ lệ nhỏ (~15%) rơi thêm hộp máu cứu thương
            if (ThreadLocalRandom.current().nextInt(100) < 15) {
                healthPickups.add(new HealthPickup(chestCenterX, chestCenterY + 58.0, HealthPickup.HealthSize.SMALL));
            }

            if (currentStage == BOSS_STAGE) {
                // Tại Boss Arena: Kích hoạt bệ sơ tán ngay tại tâm phòng khi hạ gục Boss
                dungeonMap.spawnExtractZoneInRoom(activeCombatRoom);
                GameSoundManager.playBossClearBgm();
            }
            showBanner("CLEAR", new Color(60, 235, 130), 2.0);

            activeCombatRoom = null;
        }
    }

    /**
     * Xử lý tương tác chiến trường với các vật phẩm và trạm dịch vụ (Hòm vũ khí, Hộp đạn, Hộp máu, Shop, Nâng cấp).
     *
     * @return true nếu người chơi vừa tương tác thành công với một trạm hoặc vật phẩm.
     */
    private boolean handleFieldInteractions(KeyboardHandler keyH, double deltaTime) {
        boolean interacted = false;

        // 1. Hòm tiếp tế vũ khí (Weapon Chests)
        for (WeaponChest chest : weaponChests) {
            chest.update(deltaTime, player.getX(), player.getY(), player.getSize());
            if (chest.isPlayerInRange(player.getX(), player.getY(), player.getSize()) && chest.isOpened() && !chest.isCollected()) {
                if (KeyBindings.isActionPressedOnce(keyH, GameAction.INTERACT)) {
                    if (chest.collectReward(this)) {
                        interacted = true;
                    }
                }
            }
        }

        // 2. Hộp đạn tự động hút khi lại gần (Ammo Pickups Proximity Auto-Collection)
        for (AmmoPickup pickup : ammoPickups) {
            pickup.update(deltaTime, player.getX(), player.getY(), player.getSize(), weaponManager.getAmmoInventory(), this);
        }
        ammoPickups.removeIf(AmmoPickup::isCollected);

        // 3. Hộp cứu thương nhặt bằng phím tương tác [F] (Health Pickups Manual Interaction)
        for (HealthPickup hp : healthPickups) {
            hp.update(deltaTime);
            if (!interacted && hp.isPlayerInRange(player.getX(), player.getY(), player.getSize())) {
                if (KeyBindings.isActionPressedOnce(keyH, GameAction.INTERACT)) {
                    if (hp.interact(this)) {
                        interacted = true;
                        break;
                    }
                }
            }
        }
        healthPickups.removeIf(HealthPickup::isUsed);

        // 4. Quầy hàng dã chiến (Field Shop Items)
        for (ShopItem item : shopItems) {
            item.update(deltaTime);
            if (item.isPlayerInRange(player.getX(), player.getY(), player.getSize()) && !item.isPurchased()) {
                if (KeyBindings.isActionPressedOnce(keyH, GameAction.INTERACT)) {
                    if (item.purchase(this)) {
                        interacted = true;
                        break;
                    }
                }
            }
        }

        // 5. Trạm Tẩy Luyện Chỉ Số (Stat Reforge Station)
        if (statReforgeStation != null) {
            statReforgeStation.update(deltaTime);
            if (statReforgeStation.isPlayerInRange(player.getX(), player.getY(), player.getSize())) {
                if (KeyBindings.isActionPressedOnce(keyH, GameAction.INTERACT)) {
                    if (statReforgeStation.interact(this)) {
                        interacted = true;
                    }
                }
            }
        }

        // 6. Trạm Nâng Cấp Nhân Vật (Character Upgrade Stations)
        if (tryInteractUpgradeStation(basicUpgradeStation, keyH, deltaTime, false)) {
            interacted = true;
        }
        if (tryInteractUpgradeStation(specialUpgradeStation, keyH, deltaTime, interacted)) {
            interacted = true;
        }

        return interacted;
    }

    private boolean tryInteractUpgradeStation(CharacterUpgradeStation station, KeyboardHandler keyH,
                                              double deltaTime, boolean alreadyInteracted) {
        if (station == null) return false;
        station.update(deltaTime);
        if (!alreadyInteracted
                && station.isPlayerInRange(player.getX(), player.getY(), player.getSize())
                && KeyBindings.isActionPressedOnce(keyH, GameAction.INTERACT)) {
            GameSoundManager.play(SoundEffect.INTERACT);
            characterUpgradeUI.open(station, player.getBuffManager());
            return true;
        }
        return false;
    }

    /**
     * Xử lý tương tác đứng trên bục sơ tán (Extraction Pad) để chuyển sang màn tiếp theo hoặc hoàn tất chiến dịch.
     * WHY: Kiểm tra va chạm với bục sơ tán 4x4 ô. Nếu người chơi bấm [F] (và không trùng với thao tác nhặt đồ khác),
     * sẽ kích hoạt chuyển màn hoặc màn hình kết thúc ván chơi.
     */
    private void handleExtractionZoneInteraction(KeyboardHandler keyH, boolean hasOtherInteraction) {
        isStandingOnExtract = dungeonMap.isIntersectsExtractZone(player.getX(), player.getY(), player.getSize(), player.getSize());
        if (!hasOtherInteraction && isStandingOnExtract && KeyBindings.isActionPressedOnce(keyH, GameAction.INTERACT)) {
            GameSoundManager.play(SoundEffect.INTERACT);
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
     * Ngẫu nhiên 1 trong 4 loại đạn: AR (6-10), SMG (8-14), Shotgun (2-4), Sniper (2-4).
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
     * Định mức mỗi stack chuẩn: AR = 30 viên; SMG = 35 viên; Shotgun = 10 viên; SR = 5 viên.
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
     * Giải quyết va chạm và tính toán sát thương giữa đạn bay, nhát chém dao, người chơi và kẻ địch.
     * WHY: Tách thành 3 khối chuyên biệt để giảm độ sâu lồng lặp (Cyclomatic Complexity):
     * 1. Va chạm đạn (Đạn ta trúng địch & Đạn địch trúng ta).
     * 2. Va chạm đòn chém cận chiến của dao găm.
     * 3. Cập nhật AI hành vi của kẻ địch.
     */
    private void updateCombatDamageResolution(double deltaTime) {
        resolveProjectileCollisions();
        resolveMeleeSlashCollisions();
        updateTargetsAI(deltaTime);
    }

    /**
     * Xử lý va chạm đạn bay với mục tiêu (kẻ địch hoặc người chơi).
     */
    private void resolveProjectileCollisions() {
        for (Projectile p : projectileManager.getActiveProjectiles()) {
            if (!p.isActive()) continue;

            if (p.isPlayerBullet()) {
                handlePlayerBulletHit(p);
            } else {
                handleEnemyBulletHit(p);
            }
        }
    }

    /**
     * Xử lý đạn của người chơi va chạm với kẻ địch.
     */
    private void handlePlayerBulletHit(Projectile p) {
        for (TargetDummy dummy : combatTargets) {
            if (dummy.isAlive() && !dummy.isSpawning() && p.canHit(dummy) && p.getBounds().intersects(dummy.getBounds())) {
                double impulse = p.isCritical() ? 190.0 : 130.0;
                int baseDamage = p.getDamage();
                int damageDealt = dummy.takeBulletDamage(baseDamage, p.getAngle(), impulse, p.isCritical());

                // Nếu bắn trúng Boss, ngay lập tức hiển thị thanh máu Boss
                if (dummy == bossDummy) {
                    isBossHealthBarRevealed = true;
                    GameSoundManager.play(SoundEffect.HIT_BOSS);
                } else {
                    GameSoundManager.play(SoundEffect.HIT_TARGET);
                }

                if (p.isCritical() && damageDealt > 0) {
                    activeCritVisuals.add(new CriticalHitVisual(dummy.getX(), dummy.getY(), damageDealt));
                    GameSoundManager.play(SoundEffect.CRITICAL_HIT);
                    screenShakeTimer = Math.max(screenShakeTimer, 0.08);
                }

                // Ghi nhận va chạm: tiêu hao 1 lượt xuyên và giảm sát thương cho kẻ địch kế tiếp
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
    }

    /**
     * Xử lý đạn của kẻ địch va chạm với người chơi.
     */
    private void handleEnemyBulletHit(Projectile p) {
        Rectangle playerBox = new Rectangle(
                (int) player.getX(), (int) player.getY(), (int) player.getSize(), (int) player.getSize());
        if (p.getBounds().intersects(playerBox)) {
            if (p instanceof com.groupproject.game.entity.projectile.EnemyGrenade) {
                // Lựu đạn hẹn giờ không phát nổ ngay khi chạm người chơi; nó đếm ngược 2s để nổ ép góc!
            } else if (p instanceof BossCannonShell bcs) {
                bcs.explode(this, dungeonMap);
            } else {
                damagePlayer(p.getDamage());
                p.deactivate();
            }
        }
    }

    /**
     * Xử lý va chạm nhát chém dao cận chiến (Melee Slash) với kẻ địch và vật cản cover.
     */
    private void resolveMeleeSlashCollisions() {
        for (MeleeSlash slash : weaponManager.getActiveSlashes()) {
            if (!slash.isActive()) continue;

            for (TargetDummy dummy : combatTargets) {
                if (dummy.isAlive() && !dummy.isSpawning() && slash.canHit(dummy) && slash.getBounds().intersects(dummy.getBounds())) {
                    slash.markHit(dummy);
                    // Nhát chém dao cận chiến gây sát thương và đẩy lùi cực mạnh theo hướng vung dao
                    int damageDealt = dummy.takeMeleeDamage(slash.getDamage(), slash.getAngle(), slash.getKnockbackForce(), slash.isCritical());
                    if (damageDealt > 0) {
                        if (dummy == bossDummy) {
                            isBossHealthBarRevealed = true;
                            GameSoundManager.play(SoundEffect.HIT_BOSS);
                        } else {
                            GameSoundManager.play(SoundEffect.HIT_TARGET);
                        }
                        if (slash.isCritical()) {
                            activeCritVisuals.add(new CriticalHitVisual(dummy.getX(), dummy.getY(), damageDealt));
                    GameSoundManager.play(SoundEffect.CRITICAL_HIT);
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
                    } else if (dummy == bossDummy) {
                        // Boss Tank miễn nhiễm sát thương dao -> Keng! Bật âm thanh dội giáp kim loại và mở lộ thanh máu Boss
                        isBossHealthBarRevealed = true;
                        GameSoundManager.play(SoundEffect.HIT_UNBREAKABLE_WALL);
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
    }

    /**
     * Cập nhật trí tuệ nhân tạo (AI) di chuyển và tấn công của kẻ địch trong phòng đang kích hoạt.
     */
    private void updateTargetsAI(double deltaTime) {
        if (activeCombatRoom != null) {
            double playerCenterX = player.getCenterX();
            double playerCenterY = player.getCenterY();
            for (TargetDummy dummy : combatTargets) {
                dummy.updateAI(deltaTime, playerCenterX, playerCenterY, player.getSize(), dungeonMap, projectileManager, this);
            }
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
        double corpseAngle = fatalAngle;
        double corpseForce = fatalForce;
        if (dummy instanceof com.groupproject.game.entity.enemy.BossTank bossTank) {
            corpseAngle = bossTank.getChassisAngle();
            corpseForce = 0.0; // Xe tăng bọc thép hạng nặng cố định vị trí khi bị nổ hủy
        }

        // Sinh xác kẻ địch (Boss Tank sinh xác xe bọc thép cháy rụi tồn tại vĩnh viễn, quái thường sinh đầu lâu trượt văng)
        EnemyCorpse corpse = new EnemyCorpse(dummy.getX(), dummy.getY(), dummy.getSize(), corpseAngle, corpseForce, dummy.isBoss());
        activeCorpses.add(corpse);

        if (dummy.isBoss()) {
            // Khi Boss Tank bị tiêu diệt: Tạo 1 vụ nổ nhỏ (thuần visual + sfx, không gây sát thương)
            double bossCX = dummy.getX();
            double bossCY = dummy.getY();
            activeExplosions.add(new ExplosionVisual(bossCX, bossCY, 85.0, 0.55));
            com.groupproject.game.core.GameSoundManager.play(com.groupproject.game.core.GameSoundManager.SoundEffect.ENEMY_BOSS_EXPLOSION);
            triggerScreenShake(0.38);

            addCredits(50);
            showBanner("HẠ GỤC BOSS (+50 C)", new Color(255, 215, 60), 2.5);
            ammoPickups.add(new AmmoPickup(dummy.getX() - 36, dummy.getY(), AmmoType.AR, 30));
            ammoPickups.add(new AmmoPickup(dummy.getX() + 36, dummy.getY(), AmmoType.SMG, 35));
            ammoPickups.add(new AmmoPickup(dummy.getX(), dummy.getY() + 36, AmmoType.SHOTGUN, 10));
            ammoPickups.add(new AmmoPickup(dummy.getX() - 25, dummy.getY() + 25, AmmoType.SR, 5));
            healthPickups.add(new HealthPickup(dummy.getX(), dummy.getY() - 36, HealthPickup.HealthSize.LARGE));
        } else {
            com.groupproject.game.core.GameSoundManager.playRandomEnemyDead();
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
     * Vẽ toàn bộ màn chơi theo kiến trúc phân tầng Rendering Layers chuẩn mực:
     * <ol>
     *   <li>Layer 1: Bản đồ địa hình thế giới và các thực thể chiến trường.</li>
     *   <li>Layer 2: Các trạm tương tác, hòm tiếp tế và vật phẩm rơi trên sàn.</li>
     *   <li>Layer 3: Nhân vật Operative, vũ khí và hiệu ứng chiến đấu đặc biệt.</li>
     *   <li>Layer 4: Giao diện hiển thị trạng thái chiến thuật (HUD Layer).</li>
     *   <li>Layer 5: Các màn hình giao diện toàn màn hình, cửa sổ Modal và hiệu ứng chuyển cảnh.</li>
     * </ol>
     */
    @Override
    public void render(Graphics2D g) {
        renderWorldAndEntities(g);
        renderInteractiveStationsAndPickups(g);
        renderOperativeAndCombatEffects(g);
        renderHUDLayer(g);
        renderFullscreenOverlaysAndModals(g);
    }

    /**
     * Layer 1: Vẽ địa hình hầm ngục, cửa ngăn, xác quái, aura hồi phục, kẻ địch và đạn bay.
     */
    private void renderWorldAndEntities(Graphics2D g) {
        // 1. Bản đồ hầm ngục (kèm culling tối ưu hiệu năng)
        dungeonMap.render(g, camera);

        // 2. Xác quái vật trượt quán tính trên mặt sàn
        for (EnemyCorpse corpse : activeCorpses) {
            corpse.render(g, camera);
        }

        // 3. Vùng hiệu ứng nano hồi máu của Medic trên mặt sàn
        for (TargetDummy dummy : combatTargets) {
            if (dummy instanceof com.groupproject.game.entity.enemy.MedicEnemy medic) {
                medic.renderAura(g, camera);
            }
        }

        // 4. Kẻ địch và mục tiêu thù địch
        for (TargetDummy dummy : combatTargets) {
            dummy.render(g, camera);
        }

        // 5. Đạn bay trên chiến trường
        projectileManager.render(g, camera);

        // 6. Hiệu ứng bộc phá diện rộng (Explosions)
        for (ExplosionVisual ev : activeExplosions) {
            ev.render(g, camera);
        }
    }

    /**
     * Layer 2: Vẽ các vật phẩm rơi (Hộp máu, Hộp đạn, Hòm tiếp tế) và các trạm dịch vụ dã chiến.
     */
    private void renderInteractiveStationsAndPickups(Graphics2D g) {
        // Hộp máu
        for (HealthPickup hp : healthPickups) {
            boolean inRange = hp.isPlayerInRange(player.getX(), player.getY(), player.getSize());
            hp.render(g, camera, inRange);
        }

        // Hộp đạn
        for (AmmoPickup pickup : ammoPickups) {
            pickup.render(g, camera);
        }

        // Hòm tiếp tế vũ khí
        for (WeaponChest chest : weaponChests) {
            boolean inRange = chest.isPlayerInRange(player.getX(), player.getY(), player.getSize());
            chest.render(g, camera, inRange);
        }

        // Quầy hàng dã chiến
        for (ShopItem item : shopItems) {
            boolean inRange = item.isPlayerInRange(player.getX(), player.getY(), player.getSize());
            item.render(g, camera, inRange);
        }

        // Trạm Tẩy Luyện Chỉ Số
        if (statReforgeStation != null) {
            statReforgeStation.render(g, camera, player.getX(), player.getY(), player.getSize(),
                    weaponManager.getActiveWeapon(), playerCredits);
        }

        // Trạm Nâng Cấp Nhân Vật
        if (basicUpgradeStation != null) {
            basicUpgradeStation.render(g, camera, player.getX(), player.getY(), player.getSize());
        }
        if (specialUpgradeStation != null) {
            specialUpgradeStation.render(g, camera, player.getX(), player.getY(), player.getSize());
        }
    }

    /**
     * Layer 3: Vẽ súng, nhân vật Operative, vệt chém dao, hiệu ứng bạo kích và đường ngắm chuột.
     */
    private void renderOperativeAndCombatEffects(Graphics2D g) {
        // Súng nằm ở layer dưới nhân vật theo thiết kế
        weaponManager.renderWeapon(g, camera, player.getCenterX(), player.getCenterY(), aimAngle);

        // Nhân vật chính và các chỉ báo nạp đạn / hết đạn
        player.render(g, camera, weaponManager.getActiveWeapon());
        player.renderReloadIndicator(g, camera, weaponManager.getActiveWeapon());
        player.renderOutOfAmmoWarning(g, camera, weaponManager.isCurrentWeaponOutOfAmmo());

        // Vệt chém dao cận chiến (layer trên nhân vật)
        weaponManager.renderSlashes(g, camera);

        // Hiệu ứng tia sáng bạo kích (Critical Hit Visuals)
        for (CriticalHitVisual cv : activeCritVisuals) {
            cv.render(g, camera);
        }

        // Đường ngắm nét đứt mờ nối từ tâm nhân vật đến con trỏ chuột
        renderAimGuideline(g);
    }

    /**
     * Layer 4: Vẽ các phần tử hiển thị thông số chiến thuật (Heads-Up Display).
     */
    private void renderHUDLayer(Graphics2D g) {
        playingHUD.renderSectorHUD(g, currentStage, activeCombatRoom);
        playingHUD.renderPlayerHealthHUD(g, player);
        playingHUD.renderDodgeHUD(g, player);
        playingHUD.renderCreditsHUD(g, playerCredits);
        playingHUD.renderBuffsHUD(g, player);

        // Thanh máu Boss trên đỉnh màn hình (chỉ hiển thị khi đã bước vào phòng đấu Boss)
        if (isBossHealthBarRevealed && bossDummy != null && bossDummy.isAlive()) {
            long activeMinions = combatTargets.stream().filter(d -> d.isAlive() && !d.isBoss()).count();
            playingHUD.renderBossHealthBar(g, bossDummy, activeMinions, bossMinionRespawnTimer);
        }

        // Radar bản đồ thu nhỏ (Minimap Radar HUD)
        minimapHUD.render(g, dungeonMap, player, currentStage);

        // Bảng điều khiển vũ khí và băng đạn ở đáy màn hình
        weaponManager.renderHUD(g, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
    }

    /**
     * Layer 5: Vẽ các giao diện phủ toàn màn hình, cửa sổ Modal, màn hình tạm dừng và chuyển cảnh.
     */
    private void renderFullscreenOverlaysAndModals(Graphics2D g) {
        // Banner thông báo chiến thuật
        if (bannerTimer > 0 && !bannerMessage.isEmpty()) {
            boolean hasBoss = (isBossHealthBarRevealed && bossDummy != null && bossDummy.isAlive());
            playingHUD.renderBanner(g, bannerMessage, bannerColor, hasBoss);
        }

        // Hướng dẫn phím tương tác sơ tán khi đứng trên bục
        if (isStandingOnExtract && !isExtracting) {
            int screenPX = camera.worldToScreenX(player.getCenterX());
            int screenPY = camera.worldToScreenY(player.getCenterY());
            playingHUD.renderExtractPrompt(g, screenPX, screenPY, currentStage);
        }

        // Màn hình kết thúc chiến dịch (Victory Extraction Debriefing)
        if (isExtracting) {
            playingHUD.renderExtractionVictoryScreen(g, isStandaloneBossFight);
        }

        // Cảnh báo viền đỏ nguy hiểm khi lượng máu thấp (<= 30%)
        if (player.getCurrentHealth() <= 30 && !player.isDead()) {
            playingHUD.renderLowHealthVignette(g);
        }

        // Hiệu ứng mờ dần về bóng tối khi tử trận
        if (player.isDead()) {
            playingHUD.renderDeathVignetteTransition(g, player.getDeathSequenceProgress());
        }

        // Giao diện Balo chiến thuật (TAB Overlay)
        if (isBackpackOpen) {
            backpackUI.render(g, weaponManager, player, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        }

        // Giao diện Menu Tạm dừng (Pause Menu UI Overlay)
        if (isPaused) {
            pauseMenuUI.render(g, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        }

        // Giao diện Nâng Cấp Nhân Vật (Character Upgrade Modal UI Overlay)
        if (characterUpgradeUI.isVisible()) {
            characterUpgradeUI.render(g, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT, this);
        }

        // Giao diện Nâng Cấp Vũ Khí & Tẩy Luyện Chỉ Số (Modals)
        if (weaponUpgradeModalUI.isVisible()) {
            weaponUpgradeModalUI.render(g, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT, playerCredits);
        }
        if (statReforgeModalUI.isVisible()) {
            statReforgeModalUI.render(g, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT, playerCredits);
        }

        // Hoạt cảnh cắt cảnh giới thiệu Boss (Letterbox cinematic bars + banner)
        bossIntroCinematic.render(g);

        // Hiệu ứng Fade chuyển cảnh stage — luôn vẽ trên cùng
        stageTransition.render(g);
    }

    /**
     * Vẽ đường ngắm nét đứt mờ nối từ vị trí người chơi đến con trỏ chuột.
     * Có thể bật/tắt trong Cài đặt (GameConfig.showAimGuideline).
     */
    private static final float[] AIM_DASH_PATTERN = { 6.0f, 6.0f };
    private static final java.awt.BasicStroke AIM_GUIDELINE_STROKE = new java.awt.BasicStroke(1.2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND, 1.0f, AIM_DASH_PATTERN, 0.0f);
    private static final java.awt.BasicStroke AIM_CIRCLE_STROKE = new java.awt.BasicStroke(1.0f);
    private static final Color AIM_LINE_COLOR = new Color(175, 230, 255, 65);
    private static final Color AIM_CIRCLE_COLOR = new Color(175, 230, 255, 80);

    /**
     * Đường ngắm nét đứt mờ nối từ người chơi đến con trỏ chuột.
     */
    private void renderAimGuideline(Graphics2D g) {
        if (!GameConfig.showAimGuideline || cachedMouseHandler == null || player == null || player.isDead()) {
            return;
        }
        // Không vẽ khi đang mở menu tạm dừng hoặc các modal che khuất màn hình
        if (isPaused || isBackpackOpen || weaponUpgradeModalUI.isVisible() || statReforgeModalUI.isVisible() || characterUpgradeUI.isVisible()) {
            return;
        }

        int screenPX = camera.worldToScreenX(player.getCenterX());
        int screenPY = camera.worldToScreenY(player.getCenterY());
        int mouseSX = cachedMouseHandler.getScreenX();
        int mouseSY = cachedMouseHandler.getScreenY();

        double dx = mouseSX - screenPX;
        double dy = mouseSY - screenPY;
        double dist = Math.hypot(dx, dy);

        // Bán kính an toàn tránh vẽ đè sát thân người chơi (< 24px)
        if (dist < 24.0) {
            return;
        }

        java.awt.Stroke oldStroke = g.getStroke();
        Color oldColor = g.getColor();

        // Điểm bắt đầu ngay đầu nòng súng (~24px)
        double startOffset = 24.0;
        int startX = (int) (screenPX + (dx / dist) * startOffset);
        int startY = (int) (screenPY + (dy / dist) * startOffset);

        // Đường kẻ nét đứt mờ (Faint dashed guideline)
        g.setStroke(AIM_GUIDELINE_STROKE);
        g.setColor(AIM_LINE_COLOR);
        g.drawLine(startX, startY, mouseSX, mouseSY);

        // Vòng tròn chấm tâm ngắm nhỏ mờ nhạt quanh con trỏ chuột
        g.setStroke(AIM_CIRCLE_STROKE);
        g.setColor(AIM_CIRCLE_COLOR);
        g.drawOval(mouseSX - 3, mouseSY - 3, 6, 6);

        g.setStroke(oldStroke);
        g.setColor(oldColor);
    }

    public boolean isStandingOnExtract() {
        return isStandingOnExtract;
    }

    public boolean isExtracting() {
        return isExtracting;
    }

    public void setIsExtracting(boolean extracting) {
        this.isExtracting = extracting;
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
        if (amount > 0) {
            this.playerCredits += amount;
            GameSoundManager.play(SoundEffect.COIN_PICK_UP);
        }
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

    public boolean isBossHealthBarRevealed() {
        return isBossHealthBarRevealed;
    }

    public void setBossHealthBarRevealed(boolean revealed) {
        this.isBossHealthBarRevealed = revealed;
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
            GameSoundManager.play(SoundEffect.PLAYER_GOT_HIT);
            screenShakeTimer = 0.2; // 0.2 giây rung màn hình phản hồi va chạm
            if (player.isDead()) {
                showBanner("[!] CHIẾN BINH ĐÃ TỬ TRẬN (OPERATIVE KIA) [!]", new Color(255, 50, 50), 5.0);
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

    public double getScreenShakeTimer() {
        return screenShakeTimer;
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
        triggerAreaExplosion(x, y, radius, damage, false);
    }

    /**
     * Kích hoạt vụ nổ diện rộng (AoE) tại tọa độ chỉ định, hỗ trợ suy giảm sát thương theo khoảng cách.
     *
     * <p>WHY: Khi {@code distanceFalloff == true} (dành cho đạn đại pháo của Boss Tank),
     * tâm vụ nổ gây đủ sát thương cực đại (40 HP), và giảm dần tuyến tính khi người chơi
     * đứng xa tâm nổ về phía rìa bán kính.
     *
     * @param x               Tọa độ tâm vụ nổ X.
     * @param y               Tọa độ tâm vụ nổ Y.
     * @param radius          Bán kính sát thương và hiệu ứng (px).
     * @param damage          Lượng sát thương bộc phá tối đa tại tâm nổ.
     * @param distanceFalloff {@code true} nếu sát thương giảm dần theo khoảng cách đến tâm nổ.
     */
    public void triggerAreaExplosion(double x, double y, double radius, int damage, boolean distanceFalloff) {
        double duration = radius >= 120.0 ? 0.58 : 0.45;
        activeExplosions.add(new ExplosionVisual(x, y, radius, duration));
        triggerScreenShake(radius >= 120.0 ? 0.48 : 0.35);

        // 1. Kiểm tra sát thương lên người chơi (xuyên cover/địa hình)
        double playerCenterX = player.getCenterX();
        double playerCenterY = player.getCenterY();
        double dist = Math.hypot(playerCenterX - x, playerCenterY - y);
        double maxHitDist = radius + player.getSize() / 2.0;
        if (dist <= maxHitDist) {
            int finalDamage = distanceFalloff
                    ? BossCannonShell.calculateFalloffDamage(dist, maxHitDist, damage)
                    : damage;
            damagePlayer(finalDamage);
        }

        // 2. Tác động sát thương lan lên cả quái phụ xung quanh (nếu dính pháo)
        for (TargetDummy dummy : combatTargets) {
            if (dummy.isAlive() && !dummy.isBoss()) {
                double dummyCenterX = dummy.getX() + (dummy.getSize() / 2.0);
                double dummyCenterY = dummy.getY() + (dummy.getSize() / 2.0);
                double dDist = Math.hypot(dummyCenterX - x, dummyCenterY - y);
                if (dDist <= radius) {
                    int baseDummyDmg = distanceFalloff
                            ? BossCannonShell.calculateFalloffDamage(dDist, radius, damage)
                            : damage;
                    dummy.takeDamage(Math.max(1, baseDummyDmg / 2), Math.atan2(dummyCenterY - y, dummyCenterX - x), 150.0);
                    if (!dummy.isAlive()) {
                        double fatalAngle = Math.atan2(dummyCenterY - y, dummyCenterX - x);
                        onEnemyEliminated(dummy, fatalAngle, 440.0);
                    }
                }
            }
        }
    }

    private boolean isProcessingExplosion = false;
    private final java.util.ArrayDeque<ExplosionRequest> pendingBarrelExplosions = new java.util.ArrayDeque<>();

    private record ExplosionRequest(double x, double y, double radius, int damage) {}

    @Override
    public void onTileExploded(int col, int row, double worldCenterX, double worldCenterY, double radius, int damage) {
        pendingBarrelExplosions.add(new ExplosionRequest(worldCenterX, worldCenterY, radius, damage));
        if (!isProcessingExplosion) {
            isProcessingExplosion = true;
            try {
                while (!pendingBarrelExplosions.isEmpty()) {
                    ExplosionRequest req = pendingBarrelExplosions.poll();
                    executeBarrelExplosion(req.x, req.y, req.radius, req.damage);
                }
            } finally {
                isProcessingExplosion = false;
            }
        }
    }

    /**
     * Thuc thi boc pha thung phi no tai toa do the gioi (worldCenterX, worldCenterY).
     *
     * <p>WHY: Thung phi no gay sat thuong cuc lon len moi doi tuong trong ban kinh:
     * - Nguoi choi nhan sat thuong co suy giam theo khoang cach (distance falloff).
     * - Toan bo ke dich (ke ca Boss Tank va quai phu) deu nhan sat thuong va hieu ung day lui.
     * - Kich hoat phan ung no day chuyen (chain reaction) pha huy thung go va kich no cac thung phi lan can.
     */
    private void executeBarrelExplosion(double x, double y, double radius, int damage) {
        GameSoundManager.play(SoundEffect.GRENADIER_EXPLOSION);
        activeExplosions.add(new ExplosionVisual(x, y, radius, 0.48));
        triggerScreenShake(0.40);

        // 1. Kiem tra sat thuong len nguoi choi (xuyen cover/dia hinh)
        double playerCX = player.getCenterX();
        double playerCY = player.getCenterY();
        double pDist = Math.hypot(playerCX - x, playerCY - y);
        double maxHitDist = radius + (player.getSize() / 2.0);
        if (pDist <= maxHitDist) {
            int pDmg = BossCannonShell.calculateFalloffDamage(pDist, maxHitDist, damage);
            damagePlayer(pDmg);
        }

        // 2. Tac dong sat thuong len toan bo muc tieu (ke dich thuong va Boss)
        for (TargetDummy dummy : combatTargets) {
            if (dummy.isAlive()) {
                double dummyCX = dummy.getX() + (dummy.getSize() / 2.0);
                double dummyCY = dummy.getY() + (dummy.getSize() / 2.0);
                double dDist = Math.hypot(dummyCX - x, dummyCY - y);
                double dummyMaxDist = radius + (dummy.getSize() / 2.0);
                if (dDist <= dummyMaxDist) {
                    int enemyDmg = BossCannonShell.calculateFalloffDamage(dDist, dummyMaxDist, damage);
                    double angle = Math.atan2(dummyCY - y, dummyCX - x);
                    double force = 220.0;
                    if (dummy == bossDummy) {
                        isBossHealthBarRevealed = true;
                        GameSoundManager.play(SoundEffect.HIT_BOSS);
                        dummy.takeDamage(enemyDmg, angle, 0.0);
                    } else {
                        dummy.takeDamage(enemyDmg, angle, force);
                    }
                    if (!dummy.isAlive()) {
                        onEnemyEliminated(dummy, angle, 420.0);
                    }
                }
            }
        }

        // 3. Phan ung no day chuyen len cac o cover va thung phi lan can
        double aoeReach = radius * 1.6;
        dungeonMap.damageDestructibleCoverInArea(x - aoeReach / 2.0, y - aoeReach / 2.0, aoeReach, aoeReach, damage);
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

    public WeaponUpgradeModalUI getWeaponUpgradeModalUI() {
        return weaponUpgradeModalUI;
    }

    public StatReforgeModalUI getStatReforgeModalUI() {
        return statReforgeModalUI;
    }

    /**
     * Mở Modal Nâng Cấp Vũ Khí cho phép người chơi chủ động chọn 1 trong 3 chỉ số (DMG, RPM, ACC).
     */
    public void openWeaponUpgradeModal(Weapon weapon, int cost, boolean hubMode, WeaponUpgradeModalUI.UpgradeSelectionCallback callback) {
        if (weapon == null) return;
        this.weaponUpgradeModalUI.open(weapon, cost, hubMode, callback);
    }

    /**
     * Mở Modal Tái Chế Quân Khí Dã Chiến (Reroll Súng) phân chia độc lập giữa Reroll Sao và Reroll Chí Mạng.
     */
    public void openStatReforgeModal(Weapon weapon, StatReforgeStation station) {
        if (weapon == null || station == null) return;
        this.statReforgeModalUI.open(weapon, station, false, type -> {
            if (type == StatReforgeModalUI.ReforgeType.STARS_ONLY) {
                station.executeReforgeStars(this, weapon);
            } else if (type == StatReforgeModalUI.ReforgeType.CRIT_ONLY) {
                station.executeReforgeCrit(this, weapon);
            }
        });
    }

    public MinimapHUD getMinimapHUD() {
        return minimapHUD;
    }

    public boolean isStandaloneBossFight() {
        return isStandaloneBossFight;
    }

    public void setStandaloneBossFight(boolean standaloneBossFight) {
        this.isStandaloneBossFight = standaloneBossFight;
    }

    /**
     * Xuất toàn bộ dữ liệu trạng thái tiến trình của ván đấu hiện tại phục vụ lưu checkpoint.
     */
    public com.groupproject.game.storage.RunProgressData exportSaveData() {
        if (isStandaloneBossFight || player.isDead() || isExtracting) {
            return null;
        }

        com.groupproject.game.storage.RunProgressData data = new com.groupproject.game.storage.RunProgressData();
        data.setGameVersion(GameConfig.GAME_BUILD_VERSION);
        data.setTimestamp(System.currentTimeMillis());
        data.setCurrentStage(this.currentStage);
        data.setPlayerHealth(player.getCurrentHealth());
        data.setPlayerMaxHealth(player.getMaxHealth());
        data.setPlayerCredits(this.playerCredits);
        data.setActiveWeaponSlot(weaponManager.getActiveWeapon() != null ? weaponManager.getActiveWeapon().getSlotIndex() : 1);

        // Khôi phục Buff
        data.setBuffLevels(player.getBuffManager().exportBuffLevels());

        // Khôi phục Đạn dự trữ
        data.setAmmoReserves(weaponManager.getAmmoInventory().exportAmmoReserves());

        // Danh sách vũ khí
        java.util.List<com.groupproject.game.storage.WeaponSaveData> wList = new java.util.ArrayList<>();
        for (Weapon w : weaponManager.getAllWeapons()) {
            if (w != null) {
                wList.add(new com.groupproject.game.storage.WeaponSaveData(
                        w.getSlotIndex(),
                        w.getRawName(),
                        w.getLevel(),
                        w.getDamageStars(),
                        w.getFireRateStars(),
                        w.getAccuracyStars(),
                        w.getCritChance(),
                        w.getCritMultiplier(),
                        w.getCurrentAmmo(),
                        w.isSTier()
                ));
            }
        }
        data.setWeapons(wList);

        return data;
    }

    /**
     * Khôi phục ván đấu dở dang từ đối tượng dữ liệu lưu checkpoint.
     */
    public void resumeFromSave(com.groupproject.game.storage.RunProgressData data) {
        if (data == null) return;

        this.isStandaloneBossFight = false;
        this.currentStage = Math.max(1, data.getCurrentStage());
        this.playerCredits = data.getPlayerCredits();

        // 1. Khôi phục sinh lực
        player.setMaxHealth(data.getPlayerMaxHealth());
        player.setCurrentHealth(data.getPlayerHealth());
        player.setDead(false);

        // 2. Khôi phục Buff
        player.getBuffManager().restoreBuffLevels(data.getBuffLevels(), player, weaponManager);

        // 3. Khôi phục Đạn dự trữ & Vũ khí
        weaponManager.getAmmoInventory().restoreAmmoReserves(data.getAmmoReserves());
        weaponManager.restoreFromSave(data.getWeapons(), data.getActiveWeaponSlot());
        weaponManager.bindPlayerBuffManager(player.getBuffManager());

        // 4. Khởi tạo bản đồ Stage tương ứng
        bossIntroTriggered = false;
        isBossHealthBarRevealed = false;
        dungeonMap.generateStage(currentStage);

        Room spawnRoom = dungeonMap.getSpawnRoom();
        if (spawnRoom != null) {
            player.setPosition(spawnRoom.getCenterWorldX() - (player.getSize() / 2.0),
                               spawnRoom.getCenterWorldY() - (player.getSize() / 2.0));
        } else {
            player.setPosition(GameConfig.WORLD_WIDTH / 2.0, GameConfig.WORLD_HEIGHT / 2.0);
        }

        clearStageTransientEntities();
        isStandingOnExtract = false;
        isExtracting = false;
        extractTransitionTimer = 0.0;
        player.setInvulnerableTimer(0.0);
        screenShakeTimer = 0.0;

        populateAllFieldStations();

        // Nếu là Boss Stage (Stage 4), sinh sẵn chiếc xe tăng BossTank trong phòng
        if (currentStage == BOSS_STAGE && !dungeonMap.getCombatRooms().isEmpty()) {
            Room bossRoom = dungeonMap.getCombatRooms().get(0);
            double cx = bossRoom.getCenterWorldX();
            double cy = bossRoom.getCenterWorldY();
            spawnBossSafely(bossRoom, cx, cy, 1250, 96.0, "HEAVY COMBAT BATTLE TANK [BOSS]");
        }

        camera.update(player.getCenterX(), player.getCenterY());
        GameSoundManager.playStageBgm(this.currentStage);
        showBanner("TIẾP TỤC CHIẾN DỊCH — STAGE " + currentStage, new Color(255, 215, 60), 2.5);
    }
}
