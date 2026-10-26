package com.groupproject.game.storage;

/**
 * Đối tượng dữ liệu phẳng đại diện cho một vũ khí trong bản lưu tiến trình (Weapon Save DTO).
 *
 * <p>WHY: Đóng gói toàn bộ thông tin nâng cấp của vũ khí (Slot, Cấp độ, Sao nhánh DMG/RPM/ACC/Pierce,
 * Tỉ lệ chí mạng, Đạn trong băng, Mác súng S-Tier) độc lập khỏi logic hiển thị và cập nhật thời gian thực.
 */
public class WeaponSaveData {

    private int slotIndex;
    private String weaponName;
    private int level = 1;
    private int damageStars = 1;
    private int fireRateStars = 1;
    private int accuracyStars = 1;
    private double critChance = 0.15;
    private double critMultiplier = 2.0;
    private int currentAmmo = 0;
    private boolean sTier = false;

    public WeaponSaveData() {
    }

    public WeaponSaveData(int slotIndex, String weaponName, int level, int damageStars,
                          int fireRateStars, int accuracyStars, double critChance,
                          double critMultiplier, int currentAmmo, boolean sTier) {
        this.slotIndex = slotIndex;
        this.weaponName = weaponName;
        this.level = level;
        this.damageStars = damageStars;
        this.fireRateStars = fireRateStars;
        this.accuracyStars = accuracyStars;
        this.critChance = critChance;
        this.critMultiplier = critMultiplier;
        this.currentAmmo = currentAmmo;
        this.sTier = sTier;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public void setSlotIndex(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public String getWeaponName() {
        return weaponName;
    }

    public void setWeaponName(String weaponName) {
        this.weaponName = weaponName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getDamageStars() {
        return damageStars;
    }

    public void setDamageStars(int damageStars) {
        this.damageStars = damageStars;
    }

    public int getFireRateStars() {
        return fireRateStars;
    }

    public void setFireRateStars(int fireRateStars) {
        this.fireRateStars = fireRateStars;
    }

    public int getAccuracyStars() {
        return accuracyStars;
    }

    public void setAccuracyStars(int accuracyStars) {
        this.accuracyStars = accuracyStars;
    }

    public double getCritChance() {
        return critChance;
    }

    public void setCritChance(double critChance) {
        this.critChance = critChance;
    }

    public double getCritMultiplier() {
        return critMultiplier;
    }

    public void setCritMultiplier(double critMultiplier) {
        this.critMultiplier = critMultiplier;
    }

    public int getCurrentAmmo() {
        return currentAmmo;
    }

    public void setCurrentAmmo(int currentAmmo) {
        this.currentAmmo = currentAmmo;
    }

    public boolean isSTier() {
        return sTier;
    }

    public void setSTier(boolean sTier) {
        this.sTier = sTier;
    }
}
