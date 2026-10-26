package com.groupproject.game.entity.buff;

import com.groupproject.game.entity.player.Player;
import com.groupproject.game.entity.weapon.AssaultRifle;
import com.groupproject.game.entity.weapon.CombatKnife;
import com.groupproject.game.entity.weapon.Pistol;
import com.groupproject.game.entity.weapon.SMG;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.entity.weapon.WeaponManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Quản lý tập trung toàn bộ trạng thái Buff, Chỉ số cường hóa và Kỹ năng đặc biệt của người chơi.
 *
 * <p>WHY: Đóng gói toàn bộ logic tính toán chỉ số (Máu, Công, Thủ, Tốc, CD Né, May Mắn),
 * cơ chế chặn đòn 10s một lần, diệt địch hồi 2.5 HP, cấp đạn chu kỳ 30s, mở rộng băng đạn và chí mạng.
 * Giúp mã nguồn tách bạch, dễ mở rộng và kiểm thử độc lập mà không làm phình to Player.java.
 */
public class PlayerBuffManager {

    private final Map<CharacterBuff, Integer> buffLevels = new EnumMap<>(CharacterBuff.class);

    // --- Biến trạng thái cho Buff Giáp Số Lần Có Hồi Phục (One-Hit Barrier) ---
    public static final double BARRIER_COOLDOWN = 10.0; // 10 giây hồi chiêu sau khi vỡ khiên
    private boolean barrierReady = false;
    private double barrierCooldownTimer = 0.0;
    private boolean barrierTriggeredVisual = false;

    // --- Biến trạng thái cho Buff Diệt Địch Hồi 2.5 HP ---
    private double fractionalHpAccumulator = 0.0;

    // --- Biến trạng thái cho Buff Băng Đạn Tiếp Tế (30s hồi 2 băng đạn) ---
    public static final double PERIODIC_AMMO_INTERVAL = 30.0;
    private double periodicAmmoTimer = PERIODIC_AMMO_INTERVAL;
    private boolean ammoSuppliedVisual = false;

    public PlayerBuffManager() {
        for (CharacterBuff buff : CharacterBuff.values()) {
            buffLevels.put(buff, 0);
        }
    }

    /**
     * Khôi phục toàn bộ trạng thái buff về ban đầu (phục vụ reset khi tử trận hoặc khởi động game mới).
     */
    public void reset() {
        for (CharacterBuff buff : CharacterBuff.values()) {
            buffLevels.put(buff, 0);
        }
        barrierReady = false;
        barrierCooldownTimer = 0.0;
        barrierTriggeredVisual = false;
        fractionalHpAccumulator = 0.0;
        periodicAmmoTimer = PERIODIC_AMMO_INTERVAL;
        ammoSuppliedVisual = false;
    }

    // ==========================================
    // TRUY VẤN VÀ ÁP DỤNG BUFF
    // ==========================================

    public int getLevel(CharacterBuff buff) {
        return buffLevels.getOrDefault(buff, 0);
    }

    public boolean isMaxLevel(CharacterBuff buff) {
        return getLevel(buff) >= buff.getMaxLevel();
    }

    /**
     * Mua hoặc nâng cấp một buff.
     *
     * @param buff   Buff được chọn.
     * @param player Chiến binh người chơi để cập nhật tức thời các chỉ số (Max HP, Hitbox, Băng đạn).
     * @return {@code true} nếu nâng cấp thành công; {@code false} nếu đã đạt cấp tối đa.
     */
    public boolean applyBuff(CharacterBuff buff, Player player, WeaponManager weaponManager) {
        if (isMaxLevel(buff)) return false;

        int newLevel = getLevel(buff) + 1;
        buffLevels.put(buff, newLevel);

        // Kích hoạt tức thời các hiệu ứng đặc biệt khi vừa nhận buff
        switch (buff) {
            case MAX_HEALTH -> {
                if (player != null) {
                    int oldMax = player.getMaxHealth();
                    int newMax = (int) Math.round(100.0 * getMaxHealthMultiplier());
                    player.setMaxHealth(newMax);
                    // Hồi thêm lượng máu tương ứng với phần máu vừa tăng
                    player.heal(newMax - oldMax);
                }
            }
            case ONE_HIT_BARRIER -> {
                barrierReady = true;
                barrierCooldownTimer = 0.0;
            }
            case EXTENDED_MAGAZINE -> {
                if (weaponManager != null) {
                    applyExtendedMagazines(weaponManager);
                }
            }
            default -> {}
        }

        return true;
    }

    /**
     * Rút ngẫu nhiên danh sách Buff từ kho theo nhóm (Cơ bản hoặc Đặc biệt).
     * Ưu tiên các buff chưa đạt cấp tối đa.
     */
    public List<CharacterBuff> getRandomOfferedBuffs(CharacterBuff.BuffCategory category, int count) {
        List<CharacterBuff> pool = new ArrayList<>();
        for (CharacterBuff b : CharacterBuff.values()) {
            if (b.getCategory() == category && !isMaxLevel(b)) {
                pool.add(b);
            }
        }

        Collections.shuffle(pool, ThreadLocalRandom.current());
        if (pool.size() <= count) {
            return pool;
        }
        return pool.subList(0, count);
    }

    // ==========================================
    // TÍNH TOÁN CÁC CHỈ SỐ CƠ BẢN
    // ==========================================

    /**
     * Hệ số nhân Máu tối đa: +20% mỗi cấp (Cấp 5 đạt tối đa 200% = 200 HP).
     */
    public double getMaxHealthMultiplier() {
        return 1.0 + getLevel(CharacterBuff.MAX_HEALTH) * 0.20;
    }

    /**
     * Hệ số nhân Tấn công: +10% mỗi cấp (Cấp 5 đạt tối đa 150% = 1.5x sát thương).
     */
    public double getAttackMultiplier() {
        return 1.0 + getLevel(CharacterBuff.ATTACK_DAMAGE) * 0.10;
    }

    /**
     * Hệ số nhân Tốc độ chạy: +5% mỗi cấp (Cấp 4 đạt tối đa 120% = 1.2x tốc độ).
     */
    public double getSpeedMultiplier() {
        return 1.0 + getLevel(CharacterBuff.MOVE_SPEED) * 0.05;
    }

    /**
     * Tỉ lệ giảm sát thương phải chịu: +10% mỗi cấp (Cấp 3 đạt tối đa 30% giảm sát thương).
     */
    public double getDamageReduction() {
        return Math.min(0.30, getLevel(CharacterBuff.DEFENSE) * 0.10);
    }

    /**
     * Hệ số rút ngắn Cooldown né: Tối đa nhanh hơn 1.5 lần ở Cấp 3 (tức CD giảm từ 0.8s xuống 0.533s).
     */
    public double getDodgeCooldownFactor() {
        // Cấp 0: 1.0x, Cấp 1: 1.166x, Cấp 2: 1.333x, Cấp 3: 1.50x
        return 1.0 + getLevel(CharacterBuff.DODGE_COOLDOWN) * (0.50 / 3.0);
    }

    /**
     * Phần trăm may mắn nhặt đồ: Tăng cấp sao và tỉ lệ súng Đỏ Mythic.
     */
    public double getLootLuckRatio() {
        return getLevel(CharacterBuff.LOOT_LUCK) * 0.25;
    }

    // ==========================================
    // CÁC KỸ NĂNG ĐẶC BIỆT
    // ==========================================

    public boolean hasOneHitBarrier() {
        return getLevel(CharacterBuff.ONE_HIT_BARRIER) > 0;
    }

    public boolean isBarrierReady() {
        return hasOneHitBarrier() && barrierReady;
    }

    public double getBarrierCooldownTimer() {
        return Math.max(0.0, barrierCooldownTimer);
    }

    /**
     * Xử lý khiên chặn đòn 1 lần.
     *
     * @return {@code true} nếu đòn đánh này bị khiên chặn đứng 100%; {@code false} nếu khiên chưa sẵn sàng.
     */
    public boolean checkAndConsumeBarrier() {
        if (isBarrierReady()) {
            barrierReady = false;
            barrierCooldownTimer = BARRIER_COOLDOWN;
            barrierTriggeredVisual = true;
            return true;
        }
        return false;
    }

    public boolean pollBarrierVisual() {
        if (barrierTriggeredVisual) {
            barrierTriggeredVisual = false;
            return true;
        }
        return false;
    }

    /**
     * Xử lý diệt địch hồi 2.5 HP sinh lực.
     */
    public int onEnemyKilled() {
        if (getLevel(CharacterBuff.KILL_HEAL) <= 0) return 0;

        fractionalHpAccumulator += 2.5;
        if (fractionalHpAccumulator >= 1.0) {
            int healWhole = (int) Math.floor(fractionalHpAccumulator);
            fractionalHpAccumulator -= healWhole;
            return healWhole;
        }
        return 0;
    }

    public boolean hasKillHeal() {
        return getLevel(CharacterBuff.KILL_HEAL) > 0;
    }

    public boolean hasPhaseDodge() {
        return getLevel(CharacterBuff.PHASE_DODGE) > 0;
    }

    public boolean hasShrinkHitbox() {
        return getLevel(CharacterBuff.SHRINK_HITBOX) > 0;
    }

    public double getHitboxScale() {
        return hasShrinkHitbox() ? 0.70 : 1.0; // Giảm 30% kích thước
    }

    public boolean hasPeriodicAmmo() {
        return getLevel(CharacterBuff.PERIODIC_AMMO) > 0;
    }

    public double getPeriodicAmmoTimer() {
        return Math.max(0.0, periodicAmmoTimer);
    }

    public boolean pollAmmoSuppliedVisual() {
        if (ammoSuppliedVisual) {
            ammoSuppliedVisual = false;
            return true;
        }
        return false;
    }

    public boolean hasExtendedMagazine() {
        return getLevel(CharacterBuff.EXTENDED_MAGAZINE) > 0;
    }

    /**
     * Cập nhật dung lượng băng đạn mở rộng cho các vũ khí cơ bản:
     * - Rifle: 40 viên
     * - SMG: 60 viên
     * - Pistol: 12 viên
     */
    public void applyExtendedMagazines(WeaponManager weaponManager) {
        if (!hasExtendedMagazine() || weaponManager == null) return;

        for (Weapon w : weaponManager.getAllWeapons()) {
            applyExtendedMagazineToWeapon(w);
        }
    }

    public void applyExtendedMagazineToWeapon(Weapon w) {
        if (!hasExtendedMagazine() || w == null) return;

        if (w instanceof AssaultRifle) {
            if (w.getMagazineSize() < 40) {
                w.setMagazineSize(40);
            }
        } else if (w instanceof SMG) {
            if (w.getMagazineSize() < 60) {
                w.setMagazineSize(60);
            }
        } else if (w instanceof Pistol) {
            if (w.getMagazineSize() < 12) {
                w.setMagazineSize(12);
            }
        } else if (w instanceof com.groupproject.game.entity.weapon.SniperRifle) {
            if (w.getMagazineSize() < 8) {
                w.setMagazineSize(8); // Băng đạn súng ngắm mở rộng từ 5 lên 8 viên
            }
        } else if (w instanceof com.groupproject.game.entity.weapon.SemiCarbineRifle) {
            if (w.getMagazineSize() < 28) {
                w.setMagazineSize(28); // Băng đạn SCR mở rộng từ 20 lên 28 viên
            }
        }
    }

    public boolean hasCriticalMastery() {
        return getLevel(CharacterBuff.CRITICAL_MASTERY) > 0;
    }

    /**
     * Tặng +15% tỉ lệ chí mạng.
     */
    public double getCritChanceBonus() {
        return hasCriticalMastery() ? 0.15 : 0.0;
    }

    /**
     * Trả về hệ số sát thương chí mạng cấu hình riêng theo loại vũ khí:
     * - Sniper Rifle: 250% (2.5x)
     * - Rifle: 150% (1.5x)
     * - SMG & Pistol: 120% (1.2x)
     * - Dao găm: 200% (2.0x)
     * - Khác (Shotgun): 1.5x
     */
    public double getCustomCritMultiplier(Weapon weapon) {
        if (!hasCriticalMastery() || weapon == null) {
            return weapon != null ? weapon.getCritMultiplier() : 2.0;
        }
        if (weapon instanceof com.groupproject.game.entity.weapon.SniperRifle) {
            return 2.50;
        } else if (weapon instanceof AssaultRifle) {
            return 1.50;
        } else if (weapon instanceof SMG || weapon instanceof Pistol) {
            return 1.20;
        } else if (weapon instanceof CombatKnife) {
            return 2.00;
        }
        return 1.50;
    }

    // ==========================================
    // VÒNG LẶP CẬP NHẬT (UPDATE LOOP)
    // ==========================================

    /**
     * Cập nhật bộ đếm thời gian hồi khiên chặn 10s và cấp 2 băng đạn chu kỳ 30s.
     */
    public void update(double deltaTime, Player player, WeaponManager weaponManager) {
        // 1. Cập nhật hồi phục Khiên Chặn Đòn (10s CD)
        if (hasOneHitBarrier() && !barrierReady) {
            barrierCooldownTimer -= deltaTime;
            if (barrierCooldownTimer <= 0) {
                barrierReady = true;
                barrierCooldownTimer = 0.0;
            }
        }

        // 2. Cập nhật Băng Đạn Tiếp Tế (30s hồi 2 băng đạn vũ khí chính)
        if (hasPeriodicAmmo() && weaponManager != null) {
            periodicAmmoTimer -= deltaTime;
            if (periodicAmmoTimer <= 0) {
                periodicAmmoTimer = PERIODIC_AMMO_INTERVAL;
                supplyTwoMagazines(weaponManager);
            }
        }

        // 3. Đảm bảo súng mới nhặt cũng được mở rộng băng đạn nếu có buff
        if (hasExtendedMagazine() && weaponManager != null) {
            applyExtendedMagazines(weaponManager);
        }
    }

    /**
     * Cấp 2 băng đạn dự trữ cho vũ khí chính đang cầm.
     */
    private void supplyTwoMagazines(WeaponManager weaponManager) {
        Weapon active = weaponManager.getActiveWeapon();
        if (active != null && active.getAmmoType() != null && active.getBoundAmmoInventory() != null) {
            int mag = active.getMagazineSize();
            if (mag > 0) {
                active.getBoundAmmoInventory().addAmmo(active.getAmmoType(), mag * 2);
                ammoSuppliedVisual = true;
            }
        }
    }

    /**
     * Xuất toàn bộ cấp độ buff hiện tại sang dạng Map chuỗi phục vụ lưu checkpoint.
     */
    public Map<String, Integer> exportBuffLevels() {
        Map<String, Integer> exported = new HashMap<>();
        for (Map.Entry<CharacterBuff, Integer> entry : buffLevels.entrySet()) {
            if (entry.getValue() > 0) {
                exported.put(entry.getKey().name(), entry.getValue());
            }
        }
        return exported;
    }

    /**
     * Khôi phục toàn bộ trạng thái Buff từ bản lưu checkpoint.
     */
    public void restoreBuffLevels(Map<String, Integer> savedLevels, Player player, WeaponManager weaponManager) {
        reset();
        if (savedLevels == null || savedLevels.isEmpty()) {
            return;
        }

        for (CharacterBuff buff : CharacterBuff.values()) {
            Integer lvl = savedLevels.get(buff.name());
            if (lvl != null && lvl > 0) {
                int targetLvl = Math.min(lvl, buff.getMaxLevel());
                buffLevels.put(buff, targetLvl);

                // Áp dụng các hiệu ứng tức thời nếu có
                if (buff == CharacterBuff.MAX_HEALTH && player != null) {
                    int newMax = (int) Math.round(100.0 * getMaxHealthMultiplier());
                    player.setMaxHealth(newMax);
                } else if (buff == CharacterBuff.ONE_HIT_BARRIER) {
                    barrierReady = true;
                    barrierCooldownTimer = 0.0;
                } else if (buff == CharacterBuff.EXTENDED_MAGAZINE && weaponManager != null) {
                    applyExtendedMagazines(weaponManager);
                }
            }
        }
    }
}
