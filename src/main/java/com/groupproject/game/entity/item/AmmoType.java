package com.groupproject.game.entity.item;

import java.awt.Color;

/**
 * Phân loại các chủng loại đạn dược dự trữ và thông số dung lượng chuẩn trong trò chơi.
 *
 * <p>WHY: Thiết lập ranh giới rõ ràng giữa các loại đạn súng chính (AR, SMG, Shotgun),
 * đồng thời quy định giới hạn lưu trữ tối đa (Capacity) và định mức 1 stack chiến lợi phẩm
 * theo yêu cầu thiết kế hệ thống Roguelite Shooter.
 */
public enum AmmoType {
    AR("Đạn súng trường (Rifle ammo)", 150, 30),
    SMG("Đạn SMG", 180, 35),
    SHOTGUN("Đạn Shotgun", 50, 10),
    SR("Đạn bắn tỉa SR (Sniper ammo)", 30, 5);

    private final String displayName;
    private final int maxCapacity;
    private final int stackAmount;

    AmmoType(String displayName, int maxCapacity, int stackAmount) {
        this.displayName = displayName;
        this.maxCapacity = maxCapacity;
        this.stackAmount = stackAmount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getStackAmount() {
        return stackAmount;
    }

    /**
     * Màu sắc nhận diện chuẩn của chủng loại đạn:
     * - AR: Xanh lục quân sự (#50DC64)
     * - SMG: Xanh lơ cyan (#00D2FF)
     * - SHOTGUN: Đỏ cam rực lửa (#FF5A46)
     * - SR: Tím điện quang neon (#B950FF)
     */
    public Color getColor() {
        return switch (this) {
            case AR -> new Color(80, 220, 100);
            case SMG -> new Color(0, 210, 255);
            case SHOTGUN -> new Color(255, 90, 70);
            case SR -> new Color(185, 80, 255);
        };
    }
}
