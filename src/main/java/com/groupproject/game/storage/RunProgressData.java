package com.groupproject.game.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Đối tượng truyền dữ liệu (DTO) lưu trữ toàn bộ trạng thái tiến trình của một ván đấu dở dang.
 *
 * <p>WHY: Đóng gói sạch sẽ toàn bộ trạng thái nhân vật (Máu, Buff, Kho đạn, Vũ khí ở các Slot,
 * Tiền Credits và Stage hiện tại). Khi chuyển hóa thành định dạng lưu trữ hoặc khôi phục,
 * lớp này đóng vai trò Single Source of Truth độc lập với các hệ thống State thời gian thực.
 */
public class RunProgressData {

    private String gameVersion = "";
    private long timestamp = 0L;
    private int currentStage = 1;
    private int playerHealth = 100;
    private int playerMaxHealth = 100;
    private int playerCredits = 0;
    private int activeWeaponSlot = 1;

    private Map<String, Integer> buffLevels = new HashMap<>();
    private Map<String, Integer> ammoReserves = new HashMap<>();
    private List<WeaponSaveData> weapons = new ArrayList<>();

    // Thống kê chiến trường
    private int enemiesKilled = 0;
    private int bossesDefeated = 0;
    private int damageDealt = 0;
    private int shotsFired = 0;
    private int shotsHit = 0;
    private int creditsCollected = 0;

    public RunProgressData() {
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(int currentStage) {
        this.currentStage = currentStage;
    }

    public int getPlayerHealth() {
        return playerHealth;
    }

    public void setPlayerHealth(int playerHealth) {
        this.playerHealth = playerHealth;
    }

    public int getPlayerMaxHealth() {
        return playerMaxHealth;
    }

    public void setPlayerMaxHealth(int playerMaxHealth) {
        this.playerMaxHealth = playerMaxHealth;
    }

    public int getPlayerCredits() {
        return playerCredits;
    }

    public void setPlayerCredits(int playerCredits) {
        this.playerCredits = playerCredits;
    }

    public int getActiveWeaponSlot() {
        return activeWeaponSlot;
    }

    public void setActiveWeaponSlot(int activeWeaponSlot) {
        this.activeWeaponSlot = activeWeaponSlot;
    }

    public Map<String, Integer> getBuffLevels() {
        return buffLevels;
    }

    public void setBuffLevels(Map<String, Integer> buffLevels) {
        this.buffLevels = (buffLevels != null) ? buffLevels : new HashMap<>();
    }

    public Map<String, Integer> getAmmoReserves() {
        return ammoReserves;
    }

    public void setAmmoReserves(Map<String, Integer> ammoReserves) {
        this.ammoReserves = (ammoReserves != null) ? ammoReserves : new HashMap<>();
    }

    public List<WeaponSaveData> getWeapons() {
        return weapons;
    }

    public void setWeapons(List<WeaponSaveData> weapons) {
        this.weapons = (weapons != null) ? weapons : new ArrayList<>();
    }

    public int getEnemiesKilled() {
        return enemiesKilled;
    }

    public void setEnemiesKilled(int enemiesKilled) {
        this.enemiesKilled = enemiesKilled;
    }

    public int getBossesDefeated() {
        return bossesDefeated;
    }

    public void setBossesDefeated(int bossesDefeated) {
        this.bossesDefeated = bossesDefeated;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

    public void setDamageDealt(int damageDealt) {
        this.damageDealt = damageDealt;
    }

    public int getShotsFired() {
        return shotsFired;
    }

    public void setShotsFired(int shotsFired) {
        this.shotsFired = shotsFired;
    }

    public int getShotsHit() {
        return shotsHit;
    }

    public void setShotsHit(int shotsHit) {
        this.shotsHit = shotsHit;
    }

    public int getCreditsCollected() {
        return creditsCollected;
    }

    public void setCreditsCollected(int creditsCollected) {
        this.creditsCollected = creditsCollected;
    }
}
