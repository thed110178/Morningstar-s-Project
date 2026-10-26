package com.groupproject.game.hub;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import com.groupproject.game.entity.enemy.TargetDummy;
import com.groupproject.game.entity.item.AmmoInventory;
import com.groupproject.game.entity.item.AmmoType;
import com.groupproject.game.entity.player.Player;
import com.groupproject.game.entity.projectile.Projectile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.entity.weapon.AssaultRifle;
import com.groupproject.game.entity.weapon.SMG;
import com.groupproject.game.entity.weapon.SemiCarbineRifle;
import com.groupproject.game.entity.weapon.Shotgun;
import com.groupproject.game.entity.weapon.SniperRifle;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.entity.weapon.WeaponManager;
import com.groupproject.game.entity.weapon.WeaponSpriteManager;
import com.groupproject.game.input.KeyBindings;
import com.groupproject.game.input.KeyBindings.GameAction;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import com.groupproject.game.state.GameState;
import com.groupproject.game.state.GameStateManager;
import com.groupproject.game.state.PlayingState;
import com.groupproject.game.ui.BackpackUI;
import com.groupproject.game.ui.PauseMenuUI;
import com.groupproject.game.ui.PlayingHUD;
import com.groupproject.game.ui.WeaponUpgradeModalUI;
import com.groupproject.game.ui.StatReforgeModalUI;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Hub The Range - Sảnh chờ căn cứ và trường bắn thử nghiệm trước khi bước vào chiến dịch Dungeon.
 *
 * <p>Bố cục & Tính năng:
 * <ul>
 *   <li><b>Ô vuông trung tâm giới hạn di chuyển:</b> Người chơi di chuyển tự do trong khu vực an toàn,
 *       rào chắn 2 bên ngăn người đi qua nhưng đạn và đòn chém bay xuyên qua tự do.</li>
 *   <li><b>Trường bắn bên Phải:</b> 3 bia Dummy đứng yên ở 3 cự ly (Gần, Trung bình, Xa). Khi bắn vào trường
 *       bắn này, đạn vẽ đường vệt dạ quang 3 giây rồi mờ dần để người chơi hình dung mẫu giật (recoil pattern).
 *       Hiển thị sát thương bay lên khi trúng đạn.</li>
 *   <li><b>Trường bắn bên Trái:</b> 4 bia Dummy di chuyển lạng lách (strafing) qua lại với tốc độ ngẫu nhiên.
 *       Bảng điều khiển cho phép chỉnh mốc máu (100, 250, 500, 1000 HP). Tự hồi sinh khi bị hạ.</li>
 *   <li><b>Khu vũ khí & Nâng cấp (100% Free):</b> Giá súng đầy đủ (AR, SMG, Shotgun, SCR, Sniper), bàn nâng cấp
 *       tối đa Lv 10 Free, bàn Reroll sao & chí mạng Free, thùng rác bỏ súng (khóa an toàn lục & dao), đạn vô hạn.</li>
 *   <li><b>Cổng xuất kích:</b> Bước vào cổng để bắt đầu trận đấu Dungeon mang theo chính xác các vũ khí đã chọn.</li>
 * </ul>
 */
public class HubRangeState extends GameState {

    // Kích thước thế giới The Range
    public static final double WORLD_WIDTH = 2300.0;
    public static final double WORLD_HEIGHT = 1200.0;

    // Phạm vi ô vuông trung tâm giới hạn di chuyển của Operative
    public static final double CENTER_BOUND_MIN_X = 660.0;
    public static final double CENTER_BOUND_MAX_X = 1440.0;
    public static final double CENTER_BOUND_MIN_Y = 320.0;
    public static final double CENTER_BOUND_MAX_Y = 920.0;

    // Vị trí rào chắn năng lượng 2 bên
    public static final double LEFT_BARRIER_X = 640.0;
    public static final double RIGHT_BARRIER_X = 1460.0;

    private Player player;
    private final WeaponManager weaponManager;
    private final Camera camera;
    private ProjectileManager projectileManager;
    private RecoilTracerManager tracerManager;
    private DamagePopupManager popupManager;

    private final List<TargetDummy> rightRangeDummies = new ArrayList<>();
    private final List<StrafingTargetDummy> leftRangeDummies = new ArrayList<>();
    private final List<HubStation> stations = new ArrayList<>();

    // Thông báo nổi trên màn hình
    private String notificationMessage = null;
    private double notificationTimer = 0.0;
    private Color notificationColor = new Color(80, 225, 255);

    // Hệ thống Giao diện Chiến thuật đồng bộ 100% với trong trận (In-Game HUD, Balo TAB, Menu Pause ESC)
    private final PlayingHUD playingHUD = new PlayingHUD();
    private final BackpackUI backpackUI = new BackpackUI();
    private final PauseMenuUI pauseMenuUI = new PauseMenuUI();
    private final WeaponUpgradeModalUI weaponUpgradeModalUI = new WeaponUpgradeModalUI();
    private final StatReforgeModalUI statReforgeModalUI = new StatReforgeModalUI();
    private boolean isBackpackOpen = false;
    private boolean isPaused = false;

    // Hiệu ứng nhấp nháy đèn rào chắn
    private double barrierAnimTimer = 0.0;

    // Góc ngắm bắn 360 độ từ tâm Operative đến con trỏ chuột
    private double aimAngle = 0.0;

    public HubRangeState(GameStateManager gsm) {
        super(gsm);
        this.camera = new Camera(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        this.camera.setCustomWorldBounds(WORLD_WIDTH, WORLD_HEIGHT);
        this.weaponManager = new WeaponManager();
    }

    @Override
    public void init() {
        this.isBackpackOpen = false;
        this.isPaused = false;

        // 1. Khởi tạo Operative ở trung tâm The Range
        this.player = new Player(1050, 620);
        this.camera.setCustomWorldBounds(WORLD_WIDTH, WORLD_HEIGHT);
        this.camera.snapTo(player.getCenterX(), player.getCenterY());
        this.projectileManager = new ProjectileManager();
        this.tracerManager = new RecoilTracerManager();
        this.popupManager = new DamagePopupManager();

        // Trang bị vũ khí khởi đầu tiêu chuẩn
        this.weaponManager.reset();
        this.weaponManager.bindPlayerBuffManager(player.getBuffManager());

        // Bổ sung đầy đủ đạn dược vô hạn
        replenishInfiniteAmmo();

        // 2. Khởi tạo 3 Dummy đứng yên ở trường bắn Phải (cự ly 180px, 380px, 630px tính từ rào chắn phải 1460px)
        rightRangeDummies.clear();
        rightRangeDummies.add(new TargetDummy(1640, 620, 10000, 34.0, false, "Dummy Gần (180px)"));
        rightRangeDummies.add(new TargetDummy(1840, 620, 10000, 34.0, false, "Dummy Vừa (380px)"));
        rightRangeDummies.add(new TargetDummy(2090, 620, 10000, 34.0, false, "Dummy Xa (630px)"));

        // 3. Khởi tạo 4 Dummy di chuyển lạng lách ở trường bắn Trái (vùng X: 120 -> 580, Y: 240 -> 980)
        leftRangeDummies.clear();
        for (int i = 0; i < 4; i++) {
            double startX = 200 + i * 90;
            double startY = 350 + (i % 2) * 220;
            leftRangeDummies.add(new StrafingTargetDummy(startX, startY, 100, 120, 580, 240, 980));
        }

        // 4. Khởi tạo các trạm tương tác (Stations)
        stations.clear();

        // Bờ tường Bắc: Bàn Nâng Cấp Lv10 (trái), Cầu Thang Xuất Kích (trung tâm), Bàn Tái Rèn (phải)
        stations.add(new HubUpgradeStation(880, 370));
        stations.add(new DepartureGateStation(1050, 360));
        stations.add(new HubRerollStation(1220, 370));

        // Cạnh trường bắn Trái: Bảng chỉnh HP Dummy
        stations.add(new DummyHPControllerStation(710, 620));

        // Bờ tường Nam: Kệ vũ khí đầy đủ 5 dòng súng chính (100% Free)
        stations.add(new WeaponRackStation(760, 860, "AR", "Assault Rifle",
                WeaponSpriteManager.SPRITE_AR, AssaultRifle::new));
        stations.add(new WeaponRackStation(890, 860, "SMG", "Submachine Gun",
                WeaponSpriteManager.SPRITE_SMG, SMG::new));
        stations.add(new WeaponRackStation(1020, 860, "Shotgun", "Shotgun",
                WeaponSpriteManager.SPRITE_SHOTGUN, Shotgun::new));
        stations.add(new WeaponRackStation(1150, 860, "SCR", "Semi-Carbine",
                WeaponSpriteManager.SPRITE_SEMI_CARBINE, SemiCarbineRifle::new));
        stations.add(new WeaponRackStation(1280, 860, "Sniper", "Sniper Rifle",
                WeaponSpriteManager.SPRITE_SNIPER, SniperRifle::new));

        // Center camera vào người chơi
        camera.update(player.getCenterX(), player.getCenterY());
        showNotification("CHÀO MỪNG ĐẾN VỚI THE RANGE! HÃY CHỌN & THỬ NGHIỆM VŨ KHÍ.");
    }

    /**
     * Bổ sung đạn dược vô hạn để người chơi bắn thử súng thoải mái.
     */
    public void replenishInfiniteAmmo() {
        if (weaponManager == null) return;
        AmmoInventory inv = weaponManager.getAmmoInventory();
        if (inv == null) return;
        inv.addAmmo(AmmoType.AR, 150);
        inv.addAmmo(AmmoType.SMG, 150);
        inv.addAmmo(AmmoType.SHOTGUN, 50);
        inv.addAmmo(AmmoType.SR, 30);
    }

    /**
     * Hiển thị thông báo nổi phía trên màn hình.
     */
    public void showNotification(String message) {
        showNotification(message, new Color(80, 225, 255));
    }

    public void showNotification(String message, Color color) {
        this.notificationMessage = message;
        this.notificationColor = color;
        this.notificationTimer = 2.8;
    }

    /**
     * Trang bị súng từ giá vũ khí vào Slot 1 hoặc Slot 2 của Operative.
     */
    public void equipWeaponFromRack(Weapon newWeapon) {
        Weapon w1 = weaponManager.getWeapon(1);
        Weapon w2 = weaponManager.getWeapon(2);
        Weapon active = weaponManager.getActiveWeapon();
        int activeSlot = (active != null) ? active.getSlotIndex() : 1;

        if (w1 == null) {
            newWeapon.setSlotIndex(1);
            weaponManager.addWeapon(newWeapon);
            weaponManager.selectSlot(1);
        } else if (w2 == null) {
            newWeapon.setSlotIndex(2);
            weaponManager.addWeapon(newWeapon);
            weaponManager.selectSlot(2);
        } else {
            // Cả 2 slot đều có súng -> thay thế súng ở slot đang cầm (1 hoặc 2)
            int targetSlot = (activeSlot == 2) ? 2 : 1;
            newWeapon.setSlotIndex(targetSlot);
            weaponManager.addWeapon(newWeapon);
            weaponManager.selectSlot(targetSlot);
        }

        replenishInfiniteAmmo();
        showNotification("ĐÃ TRANG BỊ: " + newWeapon.getName() + " VÀO SLOT [" + newWeapon.getSlotIndex() + "]!");
    }

    /**
     * Bỏ súng đang cầm vào thùng rác (chỉ cho phép Slot 1 hoặc Slot 2).
     */
    public void discardCurrentWeapon() {
        Weapon active = weaponManager.getActiveWeapon();
        if (active == null) return;

        int slot = active.getSlotIndex();
        if (slot == 1 || slot == 2) {
            String discardedName = active.getName();
            // Gỡ bỏ súng khỏi slot
            weaponManager.removeWeapon(slot);

            // Chuyển sang slot súng chính còn lại nếu có, hoặc về Pistol Slot 3
            int otherSlot = (slot == 1) ? 2 : 1;
            if (weaponManager.getWeapon(otherSlot) != null) {
                weaponManager.selectSlot(otherSlot);
            } else {
                weaponManager.selectSlot(3);
            }

            showNotification("ĐÃ BỎ " + discardedName + " VÀO THÙNG RÁC!", new Color(255, 100, 100));
        }
    }

    /**
     * Cập nhật lượng máu cho toàn bộ Dummy di động bên trái.
     */
    public void updateStrafingDummiesHealth(int newHp) {
        for (StrafingTargetDummy dummy : leftRangeDummies) {
            dummy.configureHealth(newHp);
        }
    }

    /**
     * Bắt đầu chiến dịch Dungeon từ The Range.
     * Toàn bộ trang bị thử nghiệm ở The Range bị xóa bỏ; Operative bước vào Stage 1 với trang bị chuẩn (Pistol + Knife).
     */
    public void startCampaignFromHub() {
        PlayingState.setSpawnDirectlyInBossRoom(false);
        PlayingState.transferWeaponsFromHub(null);
        gsm.transitionTo(GameStateManager.StateType.PLAYING, "XUẤT KÍCH CHIẾN TRƯỜNG", "TIẾN VÀO TỔNG HÀNH DINH SECTOR 01");
    }

    public void update(double deltaTime) {
        update(deltaTime, null, null);
    }

    @Override
    public void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH) {
        barrierAnimTimer += deltaTime * 5.0;

        // Cập nhật thông báo nổi
        if (notificationTimer > 0) {
            notificationTimer -= deltaTime;
            if (notificationTimer <= 0) {
                notificationMessage = null;
            }
        }

        // Đảm bảo kho đạn luôn đầy đặn
        replenishInfiniteAmmo();

        // --- 0. Xử lý Modal UI Nâng Cấp & Reroll Súng (Đóng băng khi đang mở) ---
        if (weaponUpgradeModalUI.isVisible()) {
            if (keyH != null && mouseH != null) {
                weaponUpgradeModalUI.update(keyH, mouseH, 9999, deltaTime);
            }
            return;
        }
        if (statReforgeModalUI.isVisible()) {
            if (keyH != null && mouseH != null) {
                statReforgeModalUI.update(keyH, mouseH, 9999, deltaTime);
            }
            return;
        }

        // --- 0.1. Xử lý Menu Tạm Dừng (ESC / PAUSE_MENU) ---
        if (isPaused) {
            if (keyH != null && (KeyBindings.isActionPressedOnce(keyH, GameAction.PAUSE_MENU) || keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE))) {
                PauseMenuUI.PauseAction escAction = pauseMenuUI.handleEsc();
                if (escAction == PauseMenuUI.PauseAction.RESUME) {
                    isPaused = false;
                }
                return;
            }
            if (keyH != null && mouseH != null) {
                PauseMenuUI.PauseAction action = pauseMenuUI.update(keyH, mouseH, deltaTime);
                if (action == PauseMenuUI.PauseAction.RESUME) {
                    isPaused = false;
                } else if (action == PauseMenuUI.PauseAction.EXIT_TO_MENU) {
                    isPaused = false;
                    gsm.transitionTo(GameStateManager.StateType.MENU);
                }
            }
            return;
        }

        // Mở Menu Tạm dừng
        if (keyH != null && (KeyBindings.isActionPressedOnce(keyH, GameAction.PAUSE_MENU) || keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE))) {
            if (isBackpackOpen) {
                isBackpackOpen = false;
                return;
            }
            isPaused = true;
            pauseMenuUI.open();
            return;
        }

        // --- 0.5. Xử lý Balo Chiến Thuật (TAB / BACKPACK) ---
        if (keyH != null && KeyBindings.isActionPressedOnce(keyH, GameAction.BACKPACK)) {
            isBackpackOpen = !isBackpackOpen;
            return;
        }

        if (isBackpackOpen) {
            if (keyH != null && (KeyBindings.isActionPressedOnce(keyH, GameAction.PAUSE_MENU) || keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE))) {
                isBackpackOpen = false;
                return;
            }
            if (keyH != null && mouseH != null) {
                boolean closeRequested = backpackUI.update(keyH, mouseH, weaponManager, player, deltaTime);
                if (closeRequested) {
                    isBackpackOpen = false;
                }
            }
            return; // Đóng băng thao tác di chuyển / bắn súng khi đang xem Balo
        }

        // --- 0.8. Tactical Dodge Roll / Combat Dash [SPACE / DODGE] ---
        if (keyH != null && KeyBindings.isActionPressedOnce(keyH, GameAction.DODGE)) {
            player.triggerDodge(
                    KeyBindings.isActionDown(keyH, GameAction.MOVE_UP),
                    KeyBindings.isActionDown(keyH, GameAction.MOVE_DOWN),
                    KeyBindings.isActionDown(keyH, GameAction.MOVE_LEFT),
                    KeyBindings.isActionDown(keyH, GameAction.MOVE_RIGHT)
            );
        }

        // --- 1. Xử lý di chuyển Operative & Rào chắn trung tâm ---
        if (keyH != null && mouseH != null) {
            player.handleMovement(
                    KeyBindings.isActionDown(keyH, GameAction.MOVE_UP),
                    KeyBindings.isActionDown(keyH, GameAction.MOVE_DOWN),
                    KeyBindings.isActionDown(keyH, GameAction.MOVE_LEFT),
                    KeyBindings.isActionDown(keyH, GameAction.MOVE_RIGHT),
                    deltaTime,
                    null
            );
        }

        // Ép vị trí Operative trong phạm vi ô vuông trung tâm
        double halfP = player.getSize() / 2.0;
        double clampedX = Math.clamp(player.getCenterX(), CENTER_BOUND_MIN_X + halfP, CENTER_BOUND_MAX_X - halfP);
        double clampedY = Math.clamp(player.getCenterY(), CENTER_BOUND_MIN_Y + halfP, CENTER_BOUND_MAX_Y - halfP);
        player.setX(clampedX - halfP);
        player.setY(clampedY - halfP);

        player.update(deltaTime);
        player.getBuffManager().update(deltaTime, player, weaponManager);

        // Cập nhật góc ngắm bắn theo chuột
        if (mouseH != null) {
            double mouseWorldX = camera.screenToWorldX(mouseH.getScreenX());
            double mouseWorldY = camera.screenToWorldY(mouseH.getScreenY());
            aimAngle = Math.atan2(mouseWorldY - player.getCenterY(), mouseWorldX - player.getCenterX());
            player.setAimAngle(aimAngle);
        }

        // --- 2. Xử lý chuyển đổi vũ khí (Phím 1-4, Quick Swap Q, Reload R) ---
        if (keyH != null && mouseH != null) {
            if (KeyBindings.isActionPressedOnce(keyH, GameAction.SLOT_1)) weaponManager.selectSlot(1);
            if (KeyBindings.isActionPressedOnce(keyH, GameAction.SLOT_2)) weaponManager.selectSlot(2);
            if (KeyBindings.isActionPressedOnce(keyH, GameAction.SLOT_3)) weaponManager.selectSlot(3);
            if (KeyBindings.isActionPressedOnce(keyH, GameAction.SLOT_4)) weaponManager.selectSlot(4);
            if (KeyBindings.isActionPressedOnce(keyH, GameAction.QUICK_SWAP)) weaponManager.quickSwap();

            if (KeyBindings.isActionPressedOnce(keyH, GameAction.RELOAD)) {
                weaponManager.reload();
            }

            // --- 3. Xử lý Tương tác phím tương tác [F] với các trạm ---
            if (KeyBindings.isActionPressedOnce(keyH, GameAction.INTERACT) || (keyH != null && keyH.isKeyPressedOnce(KeyEvent.VK_F))) {
                for (HubStation station : stations) {
                    if (station.isPlayerInRange(player.getCenterX(), player.getCenterY())) {
                        station.onInteract(this);
                        break;
                    }
                }
            }

            // --- 4. Xử lý Bắn súng & Chém dao ---
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
                double muzzleX = player.getCenterX();
                double muzzleY = player.getCenterY();
                weaponManager.attack(muzzleX, muzzleY, aimAngle, projectileManager);
            }

            if (isQuickMeleeActive) {
                weaponManager.quickMelee(player.getCenterX(), player.getCenterY(), aimAngle, projectileManager);
            }
        }

        // Cập nhật trạm tương tác
        for (HubStation station : stations) {
            station.update(deltaTime);
        }

        // Cập nhật vũ khí và đường đạn
        weaponManager.update(deltaTime);

        // Cập nhật đạn
        projectileManager.update(deltaTime, null, null);

        // --- 5. Kiểm tra va chạm đạn với Dummy (Phải & Trái) ---
        for (Projectile bullet : projectileManager.getActiveProjectiles()) {
            if (!bullet.isActive() || !bullet.isPlayerBullet()) continue;

            Rectangle bulletBounds = bullet.getBounds();

            // Kiểm tra va chạm với Dummy bên phải
            for (TargetDummy dummy : rightRangeDummies) {
                if (bullet.canHit(dummy) && dummy.getBounds().intersects(bulletBounds)) {
                    int dmg = bullet.getDamage();
                    boolean isCrit = bullet.isCritical();
                    dummy.takeDamage(dmg, isCrit);
                    popupManager.addPopup(dummy.getX(), dummy.getY(), dmg, isCrit);

                    // Tiêu hao lượt xuyên phá của đạn (nếu là Sniper có thể xuyên qua dummy tiếp theo)
                    bullet.onHitTarget(dummy);
                    if (!bullet.isActive()) {
                        // Đạn kết thúc tại đây (hết lượt xuyên phá) -> Ghi nhận 1 vệt tracer duy nhất từ nòng súng đến mục tiêu cuối
                        Color tracerColor = isCrit ? new Color(255, 100, 80) : new Color(255, 215, 60);
                        tracerManager.addTracer(bullet.getOriginX(), bullet.getOriginY(), dummy.getX(), dummy.getY(), tracerColor);
                        break;
                    }
                }
            }

            // Kiểm tra va chạm với Dummy bên trái
            for (StrafingTargetDummy dummy : leftRangeDummies) {
                if (dummy.isAlive() && bullet.canHit(dummy) && dummy.getBounds().intersects(bulletBounds)) {
                    int dmg = bullet.getDamage();
                    boolean isCrit = bullet.isCritical();
                    dummy.takeDamage(dmg, isCrit);
                    popupManager.addPopup(dummy.getX(), dummy.getY(), dmg, isCrit);

                    // Tiêu hao lượt xuyên phá của đạn (nếu là Sniper có thể xuyên qua dummy tiếp theo)
                    bullet.onHitTarget(dummy);
                    if (!bullet.isActive()) {
                        Color tracerColor = isCrit ? new Color(255, 100, 80) : new Color(255, 215, 60);
                        tracerManager.addTracer(bullet.getOriginX(), bullet.getOriginY(), dummy.getX(), dummy.getY(), tracerColor);
                        break;
                    }
                }
            }

            // Nếu đạn bay vào trường bắn phải và vượt quá giới hạn tường phải (X >= 2200)
            if (bullet.isActive() && bullet.getX() >= 2200.0) {
                Color tracerColor = bullet.isCritical() ? new Color(255, 100, 80) : new Color(255, 215, 60);
                tracerManager.addTracer(bullet.getOriginX(), bullet.getOriginY(), bullet.getX(), bullet.getY(), tracerColor);
                bullet.deactivate();
            }

            // Nếu đạn bay vào trường bắn trái và vượt quá giới hạn tường trái (X <= 100)
            if (bullet.isActive() && bullet.getX() <= 100.0) {
                Color tracerColor = bullet.isCritical() ? new Color(255, 100, 80) : new Color(255, 215, 60);
                tracerManager.addTracer(bullet.getOriginX(), bullet.getOriginY(), bullet.getX(), bullet.getY(), tracerColor);
                bullet.deactivate();
            }
        }

        // Cập nhật các vệt đường đạn tracer và popup sát thương
        tracerManager.update(deltaTime);
        popupManager.update(deltaTime);

        // Cập nhật Dummy
        for (TargetDummy dummy : rightRangeDummies) {
            dummy.update(deltaTime);
            // Dummy đứng yên tự hồi máu nếu bị trừ máu
            if (dummy.getCurrentHealth() < dummy.getMaxHealth()) {
                dummy.setAlive(true);
            }
        }
        for (StrafingTargetDummy dummy : leftRangeDummies) {
            dummy.update(deltaTime);
        }

        // Smooth camera bám theo player
        camera.update(player.getCenterX(), player.getCenterY());
    }

    @Override
    public void render(Graphics2D g) {
        // --- 1. Vẽ nền căn cứ The Range ---
        renderBackgroundGrid(g);

        // --- 2. Vẽ rào chắn năng lượng 2 bên ---
        renderEnergyBarriers(g);

        // --- 3. Vẽ các trạm tương tác ---
        for (HubStation station : stations) {
            boolean near = station.isPlayerInRange(player.getCenterX(), player.getCenterY());
            station.render(g, camera, near);
        }

        // --- 4. Vẽ bia Dummy 2 bên ---
        for (TargetDummy dummy : rightRangeDummies) {
            dummy.render(g, camera);
        }
        for (StrafingTargetDummy dummy : leftRangeDummies) {
            dummy.render(g, camera);
        }

        // --- 5. Vẽ vệt đạn Recoil Tracer Lines 3 giây (Trường bắn phải) ---
        tracerManager.render(g, camera);

        // --- 6. Vẽ đạn & vũ khí (layer dưới) & operative & thanh đếm ngược nạp đạn & vệt chém ---
        projectileManager.render(g, camera);
        weaponManager.renderWeapon(g, camera, player.getCenterX(), player.getCenterY(), aimAngle);
        player.render(g, camera, weaponManager.getActiveWeapon());
        player.renderReloadIndicator(g, camera, weaponManager.getActiveWeapon());
        player.renderOutOfAmmoWarning(g, camera, weaponManager.isCurrentWeaponOutOfAmmo());
        weaponManager.renderSlashes(g, camera);

        // --- 7. Vẽ số sát thương bay lên (Damage Popups) ---
        popupManager.render(g, camera);

        // --- 8. Vẽ HUD The Range ---
        renderHubHUD(g);
    }

    /** Hàng tường biên Bắc (trên) và biên Nam (dưới) của The Range để thay thế tường vô hình bằng tường gạch thật */
    public static final int WALL_ROW_TOP = 4;
    public static final int WALL_ROW_BOT = 21;

    /** Lưới gạch chỉ để hiển thị (không va chạm) - dùng chung Tile với dungeon để khớp phong cách. */
    private com.groupproject.game.dungeon.Tile[][] hubTiles;

    /** Tấm ảnh nền The Range đã được nướng sẵn (Surface Baking) tương thích VRAM - giảm từ 500 lệnh vẽ xuống đúng 1 lệnh */
    private java.awt.image.BufferedImage bakedHubSurface;

    /**
     * Dựng lưới gạch nền và nướng (bake) thành 1 tấm ảnh duy nhất tương thích phần cứng.
     * Xây tường gạch kiên cố ở hàng WALL_ROW_TOP và WALL_ROW_BOT, xóa bỏ hoàn toàn 100% tile thừa ở ngoài biên (Void Culling).
     */
    private void buildHubTiles() {
        int ts = GameConfig.TILE_SIZE;
        int cols = (int) Math.ceil(WORLD_WIDTH / ts);
        int rows = (int) Math.ceil(WORLD_HEIGHT / ts);
        int leftBarCol = (int) (LEFT_BARRIER_X / ts);
        int rightBarCol = (int) (RIGHT_BARRIER_X / ts);

        hubTiles = new com.groupproject.game.dungeon.Tile[cols][rows];

        // 1. Chỉ sinh các ô tile trong phạm vi hoạt động từ WALL_ROW_TOP đến WALL_ROW_BOT
        // Các hàng y < WALL_ROW_TOP và y > WALL_ROW_BOT để null (xóa hoàn toàn tile thừa tiết kiệm bộ nhớ & CPU)
        for (int x = 0; x < cols; x++) {
            for (int y = WALL_ROW_TOP; y <= WALL_ROW_BOT; y++) {
                com.groupproject.game.dungeon.TileType type = com.groupproject.game.dungeon.TileType.FLOOR;

                // Xây bờ tường gạch kiên cố bao quanh 4 phía: Bờ Bắc, Bờ Nam, Bờ Tây, Bờ Đông
                if (x == 0 || x == cols - 1 || y == WALL_ROW_TOP || y == WALL_ROW_BOT) {
                    type = com.groupproject.game.dungeon.TileType.WALL;
                } else {
                    if (x == leftBarCol || x == rightBarCol) {
                        type = com.groupproject.game.dungeon.TileType.DOOR_RESET_STAT; // sọc hazard rào chắn
                    } else if (x < leftBarCol || x > rightBarCol) {
                        type = com.groupproject.game.dungeon.TileType.REFORGE_ZONE; // thảm cao su trường bắn
                    }
                }
                hubTiles[x][y] = new com.groupproject.game.dungeon.Tile(x, y, type);
            }
        }

        // 2. Trang trí vạt cỏ ở rìa chân tường bao và bãi đất trống The Range
        for (int x = 1; x < cols - 1; x++) {
            for (int y = WALL_ROW_TOP + 1; y < WALL_ROW_BOT; y++) {
                com.groupproject.game.dungeon.Tile t = hubTiles[x][y];
                if (t != null && t.getType() == com.groupproject.game.dungeon.TileType.FLOOR) {
                    boolean nearWall = (x == 1 || x == cols - 2 || y == WALL_ROW_TOP + 1 || y == WALL_ROW_BOT - 1);
                    if (nearWall) {
                        int hash = (x * 31 + y * 17 + (x ^ y) * 7) & 0x7FFFFFFF;
                        if (hash % 100 < 75) {
                            t.setHasGrass(true);
                        }
                    } else {
                        // Bãi đất trống ở giữa sân: Phân bổ các cụm cỏ xanh dã chiến đan xen tự nhiên (~38%)
                        int hash = (x * 73856093 ^ y * 19349663 ^ (x + y) * 83492791) & 0x7FFFFFFF;
                        if (hash % 100 < 38) {
                            t.setHasGrass(true);
                        }
                    }
                }
            }
        }

        // 3. Surface Baking: Nướng toàn bộ nền The Range vào một tấm ảnh đệm VRAM duy nhất
        int surfaceW = cols * ts;
        int surfaceH = (WALL_ROW_BOT - WALL_ROW_TOP + 1) * ts;
        java.awt.image.BufferedImage rawSurface = new java.awt.image.BufferedImage(surfaceW, surfaceH, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = rawSurface.createGraphics();

        for (int x = 0; x < cols; x++) {
            for (int y = WALL_ROW_TOP; y <= WALL_ROW_BOT; y++) {
                com.groupproject.game.dungeon.Tile t = hubTiles[x][y];
                if (t != null) {
                    int px = x * ts;
                    int py = (y - WALL_ROW_TOP) * ts;
                    t.render(sg, px, py);
                }
            }
        }
        sg.dispose();

        // Chuyển đổi sang CompatibleImage khớp với VRAM card màn hình để BitBlt cực nhanh
        this.bakedHubSurface = com.groupproject.game.core.GameSpriteManager.toCompatibleImage(rawSurface);
    }

    private static final BasicStroke BARRIER_GLOW_STROKE = new BasicStroke(6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke BARRIER_CORE_STROKE = new BasicStroke(2.0f);
    private static final Font BARRIER_FONT = new Font("SansSerif", Font.BOLD, 17);

    /**
     * Vẽ nền The Range: Áp dụng Viewport Culling kết hợp các texture 49x49 pre-scaled 1:1 trong VRAM.
     * Chỉ duyệt và vẽ các ô tile nằm trong khung nhìn camera, hoàn toàn loại bỏ tải nặng khi GPU scale ảnh lớn 2304x864.
     */
    private void renderBackgroundGrid(Graphics2D g) {
        g.setColor(new Color(15, 18, 25));
        g.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        if (hubTiles == null) {
            buildHubTiles();
        }

        int ts = GameConfig.TILE_SIZE;
        int cols = hubTiles.length;
        int startX = Math.max(0, (int) Math.floor(camera.getX() / ts));
        int endX = Math.min(cols - 1, (int) Math.ceil((camera.getX() + camera.getViewportWidth()) / ts));
        int startY = Math.max(WALL_ROW_TOP, (int) Math.floor(camera.getY() / ts));
        int endY = Math.min(WALL_ROW_BOT, (int) Math.ceil((camera.getY() + camera.getViewportHeight()) / ts));

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                com.groupproject.game.dungeon.Tile t = hubTiles[x][y];
                if (t != null) {
                    t.render(g, camera.worldToScreenX(t.getWorldX()), camera.worldToScreenY(t.getWorldY()));
                }
            }
        }
    }

    /**
     * Vẽ rào chắn năng lượng 2 bên (cho phép đạn xuyên qua nhưng chặn người).
     */
    private void renderEnergyBarriers(Graphics2D g) {
        Stroke oldStroke = g.getStroke();
        Color oldColor = g.getColor();
        Font oldFont = g.getFont();

        double pulse = Math.sin(barrierAnimTimer) * 0.5 + 0.5;
        int alphaGlow = (int) (60 + pulse * 70);

        renderSingleBarrier(g, LEFT_BARRIER_X, "[ TRƯỜNG BẮN TRÁI ]", alphaGlow);
        renderSingleBarrier(g, RIGHT_BARRIER_X, "[ TRƯỜNG BẮN PHẢI ]", alphaGlow);

        g.setStroke(oldStroke);
        g.setColor(oldColor);
        g.setFont(oldFont);
    }

    private void renderSingleBarrier(Graphics2D g, double worldX, String bannerText, int alphaGlow) {
        int sx = camera.worldToScreenX(worldX);
        // Viewport Culling cho từng rào chắn
        if (sx < -100 || sx > GameConfig.SCREEN_WIDTH + 100) {
            return;
        }

        int topY = camera.worldToScreenY(CENTER_BOUND_MIN_Y - 40);
        int botY = camera.worldToScreenY(CENTER_BOUND_MAX_Y + 40);

        // Hào quang tông cam hazard (thay cyan để khớp sọc vàng-đen của gạch)
        g.setColor(new Color(255, 140, 60, alphaGlow));
        g.setStroke(BARRIER_GLOW_STROKE);
        g.drawLine(sx, topY, sx, botY);

        // Cột tia laser trung tâm
        g.setColor(new Color(255, 225, 160, 220));
        g.setStroke(BARRIER_CORE_STROKE);
        g.drawLine(sx, topY, sx, botY);

        // Nhãn khu vực cùng style nhãn phòng gameplay chính
        g.setFont(BARRIER_FONT);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(255, 140, 60, 200));
        g.drawString(bannerText, sx - fm.stringWidth(bannerText) / 2, topY - 10);
    }

    /**
     * Vẽ hệ thống Giao diện Chiến thuật The Range đồng bộ 100% với trong trận:
     * - Thẻ The Range, Thanh máu HP, Né đòn Dash HUD, Buffs HUD (Góc trên bên trái).
     * - Thanh nhắc nhở phím tắt tinh tế ở đỉnh giữa màn hình.
     * - HUD Vũ khí & Kho đạn 4 slot chuẩn ở góc dưới bên phải (WeaponManager HUD).
     * - Balo chiến thuật toàn màn hình (TAB BackpackUI Overlay).
     * - Menu Tạm dừng (ESC PauseMenuUI Overlay).
     */
    private void renderHubHUD(Graphics2D g) {
        // 1. HUD góc trên bên trái: Thẻ căn cứ The Range + Máu + Né đòn + Buff
        renderHubHeader(g);
        playingHUD.renderPlayerHealthHUD(g, player);
        playingHUD.renderDodgeHUD(g, player);
        playingHUD.renderBuffsHUD(g, player);

        // 2. Thanh gợi ý phím điều khiển ở đỉnh giữa màn hình
        renderControlsHintBar(g);

        // 3. Thông báo nổi (Notification Banner)
        if (notificationMessage != null && notificationTimer > 0) {
            renderNotificationBanner(g);
        }

        // 4. HUD Vũ khí & Kho đạn tiêu chuẩn của trận đấu ở góc dưới bên phải
        weaponManager.renderHUD(g, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        // 5. Giao diện Balo chiến thuật (TAB Overlay)
        if (isBackpackOpen) {
            backpackUI.render(g, weaponManager, player, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        }

        // 6. Giao diện Menu Tạm dừng (ESC Overlay)
        if (isPaused) {
            pauseMenuUI.render(g, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        }

        // 7. Giao diện Modal Nâng Cấp & Reroll Súng
        if (weaponUpgradeModalUI.isVisible()) {
            weaponUpgradeModalUI.render(g, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT, 9999);
        }
        if (statReforgeModalUI.isVisible()) {
            statReforgeModalUI.render(g, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT, 9999);
        }
    }

    /**
     * Thẻ nhận diện phân cảnh The Range ở góc trên bên trái màn hình.
     */
    private void renderHubHeader(Graphics2D g) {
        int boxX = 20;
        int boxY = 20;
        int boxW = 210;
        int boxH = 34;

        g.setColor(new Color(15, 20, 30, 225));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setColor(new Color(60, 180, 240, 180));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setStroke(new BasicStroke(1.0f));

        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(new Color(80, 225, 255));
        FontMetrics fm = g.getFontMetrics();
        String title = "THE RANGE";
        int textX = boxX + (boxW - fm.stringWidth(title)) / 2;
        g.drawString(title, textX, boxY + 22);
    }

    /**
     * Thanh gợi ý phím tắt tinh tế ở đỉnh giữa màn hình.
     */
    private void renderControlsHintBar(Graphics2D g) {
        String interactKey = KeyBindings.getKeyName(KeyBindings.getKeyCode(GameAction.INTERACT));
        String backpackKey = KeyBindings.getKeyName(KeyBindings.getKeyCode(GameAction.BACKPACK));
        String pauseKey = KeyBindings.getKeyName(KeyBindings.getKeyCode(GameAction.PAUSE_MENU));
        String dodgeKey = KeyBindings.getKeyName(KeyBindings.getKeyCode(GameAction.DODGE));

        String hint = "[" + backpackKey + "] Balo | [" + pauseKey + "] Menu | [" + interactKey + "] Tương tác | [" + dodgeKey + "] Né đòn | [1-4] Đổi súng";

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(hint);
        int pad = 14;
        int boxW = textW + pad * 2;
        int boxH = 26;
        int boxX = (GameConfig.SCREEN_WIDTH - boxW) / 2;
        int boxY = 16;

        g.setColor(new Color(15, 20, 30, 200));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 6, 6);
        g.setColor(new Color(45, 65, 95));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 6, 6);

        g.setColor(new Color(185, 215, 245));
        g.drawString(hint, boxX + pad, boxY + 18);
    }

    /**
     * Banner thông báo nổi phía trên màn hình.
     */
    private void renderNotificationBanner(Graphics2D g) {
        if (notificationMessage == null || notificationMessage.trim().isEmpty()) {
            return;
        }
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int msgW = fm.stringWidth(notificationMessage);
        int boxW = msgW + 36;
        int boxX = (GameConfig.SCREEN_WIDTH - boxW) / 2;
        int boxY = 48;

        g.setColor(new Color(12, 16, 24, 235));
        g.fillRoundRect(boxX, boxY, boxW, 36, 6, 6);
        g.setColor(notificationColor);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(boxX, boxY, boxW, 36, 6, 6);
        g.setStroke(new BasicStroke(1.0f));

        g.setColor(Color.WHITE);
        g.drawString(notificationMessage, boxX + 18, boxY + 23);
    }

    public boolean isBackpackOpen() {
        return isBackpackOpen;
    }

    public void setBackpackOpen(boolean backpackOpen) {
        this.isBackpackOpen = backpackOpen;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    public BackpackUI getBackpackUI() {
        return backpackUI;
    }

    public PauseMenuUI getPauseMenuUI() {
        return pauseMenuUI;
    }

    public PlayingHUD getPlayingHUD() {
        return playingHUD;
    }

    public Player getPlayer() {
        return player;
    }

    public WeaponManager getWeaponManager() {
        return weaponManager;
    }

    public ProjectileManager getProjectileManager() {
        return projectileManager;
    }

    public RecoilTracerManager getTracerManager() {
        return tracerManager;
    }

    public DamagePopupManager getPopupManager() {
        return popupManager;
    }

    public List<TargetDummy> getRightRangeDummies() {
        return rightRangeDummies;
    }

    public List<StrafingTargetDummy> getLeftRangeDummies() {
        return leftRangeDummies;
    }

    public List<HubStation> getStations() {
        return stations;
    }

    public WeaponUpgradeModalUI getWeaponUpgradeModalUI() {
        return weaponUpgradeModalUI;
    }

    public StatReforgeModalUI getStatReforgeModalUI() {
        return statReforgeModalUI;
    }

    /**
     * Mở Modal Nâng Cấp Súng trong The Range (miễn phí, tối đa Cấp 10).
     */
    public void openWeaponUpgradeModal(Weapon weapon, WeaponUpgradeModalUI.UpgradeSelectionCallback callback) {
        if (weapon == null) return;
        this.weaponUpgradeModalUI.open(weapon, 0, true, callback);
    }

    /**
     * Mở Modal Reroll Súng trong The Range (miễn phí 2 mục Reroll Sao & Reroll Chí Mạng).
     */
    public void openStatReforgeModal(Weapon weapon) {
        if (weapon == null) return;
        this.statReforgeModalUI.open(weapon, null, true, type -> {
            if (type == StatReforgeModalUI.ReforgeType.STARS_ONLY) {
                Weapon.ReforgeResult res = weapon.reforgeStarsOnly();
                showNotification("★ REROLL SAO: DMG " + res.newDmgStars() + "★ | RPM " + res.newFrStars() + "★ | ACC " + res.newAccStars() + "★ ★");
            } else if (type == StatReforgeModalUI.ReforgeType.CRIT_ONLY) {
                Weapon.ReforgeResult res = weapon.reforgeCritOnly();
                showNotification("★ REROLL CHÍ MẠNG: CRIT " + res.newCritPercent() + "% (x" + String.format(java.util.Locale.US, "%.2f", weapon.getCritMultiplier()) + ") ★");
            }
        });
    }

    public com.groupproject.game.dungeon.Tile[][] getHubTiles() {
        if (hubTiles == null) {
            buildHubTiles();
        }
        return hubTiles;
    }

    public java.awt.image.BufferedImage getBakedHubSurface() {
        if (bakedHubSurface == null) {
            buildHubTiles();
        }
        return bakedHubSurface;
    }
}
