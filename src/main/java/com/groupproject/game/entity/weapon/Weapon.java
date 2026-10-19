package com.groupproject.game.entity.weapon;

import com.groupproject.game.core.Camera;
import com.groupproject.game.entity.projectile.ProjectileManager;
import java.awt.Graphics2D;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Base abstract class defining common attributes and behavioral contracts for all weapons.
 *
 * <p>WHY: Adheres to the Strategy Pattern and Open/Closed Principle (SOLID). Whether an armament
 * is a semi-automatic 7-round pistol, a close-quarters combat knife, or a future burst-fire rifle,
 * the player controller interacts uniformly with the {@link #attack} and {@link #reload} interface
 * without knowing concrete firearm mechanics. Adding new weaponry requires zero modifications
 * to existing gameplay code.
 */
public abstract class Weapon {

    protected final String name;
    protected int slotIndex; // Slot số 1 hoặc 2 cho vũ khí chính, 3 cho Pistol, 4 cho Knife
    protected int damage;
    protected double fireRateCooldown; // Seconds between successive strikes/shots
    protected final int magazineSize;        // Maximum magazine capacity (0 for melee)
    protected final double reloadDuration;   // Time in seconds to reload a fresh magazine

    // --- Hệ thống cấp độ và chỉ số sao (Star Modifiers) ---
    public static final int MAX_UPGRADE_COUNT = 3; // Giới hạn số lần nâng cấp tối đa cho mỗi khẩu súng
    protected int level = 1;                 // Cấp độ vũ khí (Level = Stage khi rơi, nâng cấp tối đa +3)
    protected int upgradeCount = 0;          // Số lần đã thực hiện nâng cấp tại Shop (tối đa 3 lần)
    protected int damageStars = 1;           // Sao sát thương đơn
    protected int fireRateStars = 1;         // Sao tốc độ bắn
    protected int accuracyStars = 1;         // Sao độ chính xác / giảm tản đạn
    protected int baseDamage;                // Sát thương tiêu chuẩn (1 sao)
    protected double baseFireRateCooldown;   // Cooldown tiêu chuẩn giữa các phát (1 sao)
    protected double baseSpreadAngle = 0.0;  // Góc tản đạn cơ sở (+- radian, 1 sao)
    protected double currentSpreadAngle = 0.0; // Góc tản đạn thực tế sau khi tính sao

    // --- Hệ thống phân loại đạn dược dự trữ ---
    protected com.groupproject.game.entity.item.AmmoType ammoType = null;
    protected com.groupproject.game.entity.item.AmmoInventory boundAmmoInventory = null;

    // --- Hệ thống Tỉ Lệ Chí Mạng (Critical Hit) ---
    // WHY: Mỗi khẩu súng có tỉ lệ chí mạng riêng cố định, KHÔNG dựa theo cấp độ/số sao.
    // Tỉ lệ chí mạng càng cao thì tỉ lệ xuất hiện trong hòm/shop càng ít.
    protected double critChance = 0.15; // Mặc định 15%
    protected double critMultiplier = 2.0; // Sát thương x2.0 khi nổ chí mạng

    protected int currentAmmo;
    protected double cooldownTimer = 0.0;
    protected double reloadTimer = 0.0;
    protected boolean reloading = false;

    /**
     * Constructs a base weapon configuration with spread angle.
     *
     * @param name             Display title of the firearm or melee tool.
     * @param slotIndex        Keybinding index (e.g. 1, 2, 3 or 4).
     * @param damage           Base damage payload per hit (1 star).
     * @param fireRateCooldown Base delay in seconds between attacks (1 star).
     * @param magazineSize     Magazine bullet capacity (0 indicates infinite/melee).
     * @param reloadDuration   Seconds required to complete a reload cycle.
     * @param baseSpreadAngle  Base spray jitter angle in radians (1 star).
     */
    public Weapon(String name, int slotIndex, int damage, double fireRateCooldown,
                  int magazineSize, double reloadDuration, double baseSpreadAngle) {
        this.name = name;
        this.slotIndex = slotIndex;
        this.baseDamage = damage;
        this.damage = damage;
        this.baseFireRateCooldown = fireRateCooldown;
        this.fireRateCooldown = fireRateCooldown;
        this.magazineSize = magazineSize;
        this.currentAmmo = magazineSize;
        this.reloadDuration = reloadDuration;
        this.baseSpreadAngle = baseSpreadAngle;
        this.currentSpreadAngle = baseSpreadAngle;
        this.level = 1;
        this.damageStars = 1;
        this.fireRateStars = 1;
        this.accuracyStars = 1;
        this.critChance = rollRandomCritChance();
    }

    /**
     * Constructs a base weapon configuration (backward compatibility without spread).
     */
    public Weapon(String name, int slotIndex, int damage, double fireRateCooldown,
                  int magazineSize, double reloadDuration) {
        this(name, slotIndex, damage, fireRateCooldown, magazineSize, reloadDuration, 0.0);
    }

    /**
     * Executes an attack cycle (firing bullets or striking with melee hitbox).
     *
     * @param originX   X coordinate of operative center.
     * @param originY   Y coordinate of operative center.
     * @param aimAngle  Aiming angle in radians towards mouse cursor.
     * @param pm        Projectile manager for registering spawned bullets.
     * @param slashList List for registering active melee slash hitboxes.
     * @return {@code true} if attack was successfully executed; {@code false} if on cooldown or reloading.
     */
    public abstract boolean attack(double originX, double originY, double aimAngle,
                                   ProjectileManager pm, List<MeleeSlash> slashList);

    /**
     * Advances weapon cooldown and reload progression timers.
     */
    public void update(double deltaTime) {
        if (cooldownTimer > 0) {
            cooldownTimer -= deltaTime;
        }

        if (reloading) {
            reloadTimer -= deltaTime;
            if (reloadTimer <= 0) {
                // Reload finished: nếu có liên kết kho đạn và dùng đạn dự trữ, trừ đúng lượng đạn cần
                if (ammoType != null && boundAmmoInventory != null) {
                    int needed = magazineSize - currentAmmo;
                    int loaded = boundAmmoInventory.consumeAmmo(ammoType, needed);
                    currentAmmo += loaded;
                } else {
                    currentAmmo = magazineSize;
                }
                reloadTimer = 0.0;
                reloading = false;
            }
        }
    }

    /**
     * Initiates weapon reloading if not already full or reloading.
     */
    public void reload() {
        if (magazineSize > 0 && currentAmmo < magazineSize && !reloading) {
            // Nếu là súng dùng đạn dự trữ (AR, SMG, Shotgun) và đã liên kết kho đạn,
            // bắt buộc kho đạn phải còn ít nhất 1 viên mới cho phép kích hoạt nạp đạn
            if (ammoType != null && boundAmmoInventory != null && boundAmmoInventory.getAmmo(ammoType) <= 0) {
                return;
            }
            reloading = true;
            reloadTimer = reloadDuration;
        }
    }

    /**
     * Cancels an ongoing reload cycle immediately (e.g. when interrupted by shooting).
     */
    public void cancelReload() {
        if (reloading) {
            reloading = false;
            reloadTimer = 0.0;
        }
    }

    /**
     * Nạp đầy toàn bộ băng đạn cho vũ khí và hủy tiến trình nạp dở (dành cho quầy tiếp tế đạn trong Shop).
     */
    public void resupplyAmmo() {
        if (magazineSize > 0) {
            this.currentAmmo = magazineSize;
            this.reloading = false;
            this.reloadTimer = 0.0;
        }
    }

    public com.groupproject.game.entity.item.AmmoType getAmmoType() {
        return ammoType;
    }

    public void setAmmoType(com.groupproject.game.entity.item.AmmoType ammoType) {
        this.ammoType = ammoType;
    }

    public void bindAmmoInventory(com.groupproject.game.entity.item.AmmoInventory inventory) {
        this.boundAmmoInventory = inventory;
    }

    public com.groupproject.game.entity.item.AmmoInventory getBoundAmmoInventory() {
        return boundAmmoInventory;
    }

    /**
     * Lấy số lượng đạn dự trữ của loại đạn mà vũ khí này sử dụng.
     * @return Số lượng đạn dự trữ; hoặc -1 nếu súng có đạn vô hạn (Pistol) hoặc cận chiến.
     */
    public int getReserveAmmo() {
        if (ammoType == null) {
            return -1; // Vô hạn hoặc cận chiến
        }
        if (boundAmmoInventory != null) {
            return boundAmmoInventory.getAmmo(ammoType);
        }
        return -1;
    }

    public boolean hasInfiniteAmmo() {
        return ammoType == null && !isMelee();
    }

    /**
     * Checks if the weapon is ready to strike or fire.
     */
    public boolean canAttack() {
        return cooldownTimer <= 0 && !reloading;
    }

    /**
     * Renders visual representation of the equipped weapon on the player operative.
     */
    public abstract void render(Graphics2D g, Camera camera, double originX, double originY, double aimAngle);

    public String getName() {
        return name;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public void setSlotIndex(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public int getDamage() {
        return damage;
    }

    public int getCurrentAmmo() {
        return currentAmmo;
    }

    public int getMagazineSize() {
        return magazineSize;
    }

    public boolean isReloading() {
        return reloading;
    }

    public double getReloadProgress() {
        if (!reloading || reloadDuration <= 0) return 0.0;
        return 1.0 - (reloadTimer / reloadDuration);
    }

    public double getReloadTimer() {
        return Math.max(0.0, reloadTimer);
    }

    public boolean isMelee() {
        return magazineSize == 0;
    }

    /**
     * Indicates whether this firearm supports continuous full-auto fire by holding the mouse trigger.
     * Semi-automatic weapons (such as pistols) return false and require distinct mouse clicks.
     */
    public boolean isAutomatic() {
        return false;
    }

    /**
     * Tính toán lại các chỉ số thực tế (Damage, Cooldown, Spread) dựa trên số lượng sao hiện tại.
     * <ul>
     * <li>Sát thương đơn: +20% mỗi sao thêm (+0% ở 1★, +20% ở 2★, +40% ở 3★).</li>
     * <li>Tốc độ ra đạn: +15% RPM mỗi sao thêm (giảm cooldown chia cho 1.0 + (stars - 1) * 0.15).</li>
     * <li>Độ chính xác: Giảm tản đạn theo cấp lũy thừa 0.65 mỗi sao thêm (chùm đạn gom chặt hơn).</li>
     * </ul>
     */
    public void recalculateStatsFromStars() {
        this.damage = Math.max(1, (int) Math.round(baseDamage * (1.0 + (damageStars - 1) * 0.20)));
        if (baseFireRateCooldown > 0) {
            this.fireRateCooldown = Math.max(0.02, baseFireRateCooldown / (1.0 + (fireRateStars - 1) * 0.15));
        }
        if (baseSpreadAngle > 0.0001) {
            this.currentSpreadAngle = baseSpreadAngle * Math.pow(0.65, accuracyStars - 1);
        } else {
            this.currentSpreadAngle = 0.0;
        }
    }

    /**
     * Cấu hình vũ khí về cấp độ chỉ định theo Stage hầm ngục (Stage 1 -> Lv 1, Stage 2 -> Lv 2, Stage 100 -> Lv 100).
     * <ul>
     * <li>Level 1 (I): Tiêu chuẩn cơ sở, mỗi chỉ số đúng 1 sao (1★ Damage, 1★ Fire Rate, 1★ Accuracy).</li>
     * <li>Level L: Thêm ngẫu nhiên (L - 1) sao vào 3 chỉ số chính.</li>
     * <li>Reset số lần đã nâng cấp tại shop về 0/3.</li>
     * </ul>
     *
     * @param targetLevel Cấp độ mục tiêu tương ứng với số Stage.
     */
    public void configureLevel(int targetLevel) {
        this.level = Math.max(1, targetLevel);
        this.upgradeCount = 0;
        this.damageStars = 1;
        this.fireRateStars = 1;
        this.accuracyStars = 1;

        int extraStars = this.level - 1;
        for (int i = 0; i < extraStars; i++) {
            addRandomStar();
        }
        recalculateStatsFromStars();
    }

    /**
     * Thêm 1 sao ngẫu nhiên vào 1 trong 3 chỉ số chính.
     * Trả về tên chỉ số vừa được tăng (để hiển thị thông báo phản hồi).
     */
    public String addRandomStar() {
        int pick = ThreadLocalRandom.current().nextInt(3);
        if (pick == 0) {
            damageStars++;
            return "SÁT THƯƠNG (DMG)";
        } else if (pick == 1) {
            fireRateStars++;
            return "TỐC BẮN (RPM)";
        } else {
            accuracyStars++;
            return "CHÍNH XÁC (ACC)";
        }
    }

    /**
     * Kiểm tra vũ khí còn đủ điều kiện để nâng cấp tại Shop hay không.
     * Quy tắc: Mỗi khẩu súng chỉ có thể nâng cấp tối đa 3 lần và không áp dụng cho vũ khí cận chiến.
     */
    public boolean canUpgrade() {
        return !isMelee() && upgradeCount < MAX_UPGRADE_COUNT;
    }

    /**
     * Nâng cấp khẩu súng lên 1 cấp tại bàn nâng cấp Shop.
     * Tối đa 3 lần nâng cấp cho mỗi khẩu súng.
     *
     * @return Tên chỉ số vừa được cộng thêm sao nếu nâng cấp thành công; {@code null} nếu đã đạt giới hạn 3 lần nâng hoặc là vũ khí cận chiến.
     */
    public String upgradeLevel() {
        if (!canUpgrade()) {
            return null;
        }
        level++;
        upgradeCount++;
        String upgradedStat = addRandomStar();
        recalculateStatsFromStars();
        return upgradedStat;
    }

    /**
     * Trả về chuỗi hiển thị cấp độ: Số La Mã nếu cấp <= 5 (I, II, III, IV, V), và "Lv.X" nếu cấp cao hơn.
     */
    public String getLevelDisplay() {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> "Lv." + level;
        };
    }

    /**
     * Giữ tương thích ngược với API cũ.
     */
    public String getLevelRoman() {
        return getLevelDisplay();
    }

    /**
     * Trả về tên ngắn gọn kèm cấp độ hiển thị trực quan (VD: "AR I", "Shotgun II", "SMG III", "AR Lv.100").
     */
    public String getShortDisplayName() {
        String lvl = getLevelDisplay();
        if (this instanceof AssaultRifle) {
            return "AR " + lvl;
        } else if (this instanceof Shotgun) {
            return "Shotgun " + lvl;
        } else if (this instanceof SMG) {
            return "SMG " + lvl;
        } else if (this instanceof Pistol) {
            return "Pistol " + lvl;
        } else {
            return name + " " + lvl;
        }
    }

    /**
     * Trả về chuỗi sao hiển thị (1 sao -> "★ ☆ ☆", 2 sao -> "★ ★ ☆", 3 sao -> "★ ★ ★", khi số sao > 3 -> "[stars]x ★").
     */
    public static String formatStars(int stars) {
        if (stars > 3) {
            return stars + "x ★";
        }
        StringBuilder sb = new StringBuilder();
        int totalSlots = 3;
        for (int i = 1; i <= totalSlots; i++) {
            if (i <= stars) {
                sb.append("★ ");
            } else {
                sb.append("☆ ");
            }
        }
        return sb.toString().trim();
    }

    // --- Getters & Setters ---

    public int getLevel() {
        return level;
    }

    public int getUpgradeCount() {
        return upgradeCount;
    }

    public void setUpgradeCount(int count) {
        this.upgradeCount = Math.max(0, count);
    }

    public int getDamageStars() {
        return damageStars;
    }

    public int getFireRateStars() {
        return fireRateStars;
    }

    public int getAccuracyStars() {
        return accuracyStars;
    }

    public double getFireRateCooldown() {
        return fireRateCooldown;
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public double getBaseFireRateCooldown() {
        return baseFireRateCooldown;
    }

    public double getBaseSpreadAngle() {
        return baseSpreadAngle;
    }

    public double getCurrentSpreadAngle() {
        return currentSpreadAngle;
    }

    /**
     * Thiết lập trực tiếp số sao (phục vụ viết unit test kiểm thử các trường hợp biên).
     */
    public void setStars(int dmg, int fr, int acc) {
        this.damageStars = Math.max(1, dmg);
        this.fireRateStars = Math.max(1, fr);
        this.accuracyStars = Math.max(1, acc);
        this.level = Math.max(1, (damageStars + fireRateStars + accuracyStars) - 2);
        recalculateStatsFromStars();
    }

    public double getCritChance() {
        return critChance;
    }

    public void setCritChance(double critChance) {
        this.critChance = Math.clamp(critChance, 0.0, 0.50);
    }

    public int getCritChancePercent() {
        return (int) Math.round(critChance * 100.0);
    }

    public double getCritMultiplier() {
        return critMultiplier;
    }

    public void setCritMultiplier(double critMultiplier) {
        this.critMultiplier = Math.max(1.0, critMultiplier);
    }

    /**
     * Quay xúc xắc ngẫu nhiên xác định phát bắn/chém có phải đòn chí mạng hay không.
     */
    public boolean rollCritical() {
        return ThreadLocalRandom.current().nextDouble() < critChance;
    }

    /**
     * Quay ngẫu nhiên tỉ lệ chí mạng riêng cho mỗi khẩu súng (tối đa 50%).
     * WHY: Mỗi khẩu súng có tỉ lệ chí mạng riêng biệt kể cả cùng lớp.
     * Tỉ lệ chí mạng càng cao thì tỉ lệ ra càng thấp:
     * - ~50% súng rơi vào khoảng phổ thông: 5% - 15%
     * - ~30% súng rơi vào khoảng khá: 16% - 30%
     * - ~15% súng rơi vào khoảng hiếm: 31% - 44%
     * - ~5% súng siêu hiếm đạt cực đại: 45% - 50%
     */
    public static double rollRandomCritChance() {
        double r = ThreadLocalRandom.current().nextDouble(); // [0.0, 1.0)
        // Đường cong lũy thừa r^2.2 làm giảm mạnh xác suất xuất hiện của các giá trị cao
        int percent = 5 + (int) Math.round(45.0 * Math.pow(r, 2.2));
        percent = Math.clamp(percent, 5, 50);
        return percent / 100.0;
    }

    /**
     * Tính toán lượng sát thương thực tế dựa trên cờ chí mạng (x2.0 sát thương).
     */
    public int calculateDamage(boolean isCrit) {
        if (isCrit) {
            return Math.max(1, (int) Math.round(damage * critMultiplier));
        }
        return damage;
    }

    /**
     * Tốc độ bắn tính theo đơn vị RPM (Rounds Per Minute / Số phát mỗi phút).
     */
    public int getRPM() {
        if (fireRateCooldown <= 0.0001) return 0;
        return (int) Math.round(60.0 / fireRateCooldown);
    }

    public String getFormattedRPM() {
        return getRPM() + " RPM";
    }

    /**
     * Trả về góc lệch tối đa khi bắn (radian).
     * Mặc định là currentSpreadAngle, các lớp con (như AssaultRifle) override để trả về góc nở tối đa khi sấy.
     */
    public double getMaxSpreadAngle() {
        return currentSpreadAngle;
    }

    public String getFormattedSpread() {
        double deg = Math.toDegrees(getMaxSpreadAngle());
        return String.format("±%.1f°", deg);
    }

    /**
     * Sinh ngẫu nhiên một khẩu súng chính theo trọng số xác suất nghịch đảo với tỉ lệ chí mạng.
     * Quy tắc: Tỉ lệ chí mạng càng cao thì tỉ lệ ra càng ít.
     * - SMG (10% crit): Tỉ lệ ra cao nhất ~50%
     * - Assault Rifle (20% crit): Tỉ lệ ra trung bình ~30%
     * - Shotgun (30% crit): Tỉ lệ ra hiếm nhất ~20%
     */
    public static Weapon getRandomDropWeapon() {
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < 50) {
            return new SMG();
        } else if (roll < 80) {
            return new AssaultRifle();
        } else {
            return new Shotgun();
        }
    }
}
