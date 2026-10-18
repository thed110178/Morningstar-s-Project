package com.groupproject.game.entity.item;

/**
 * Phân loại các chủng loại đạn dược dự trữ và thông số dung lượng chuẩn trong trò chơi.
 *
 * <p>WHY: Thiết lập ranh giới rõ ràng giữa các loại đạn súng chính (AR, SMG, Shotgun),
 * đồng thời quy định giới hạn lưu trữ tối đa (Capacity) và định mức 1 stack chiến lợi phẩm
 * theo yêu cầu thiết kế hệ thống Roguelite Shooter.
 */
public enum AmmoType {
    AR("Đạn AR", 150, 30),
    SMG("Đạn SMG", 180, 35),
    SHOTGUN("Đạn Shotgun", 50, 10);

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
}
