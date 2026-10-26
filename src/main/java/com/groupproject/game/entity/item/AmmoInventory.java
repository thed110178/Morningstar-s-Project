package com.groupproject.game.entity.item;

import java.util.EnumMap;
import java.util.Map;

/**
 * Quản lý kho dự trữ đạn dược (Reserve Ammo Inventory) của nhân vật người chơi.
 *
 * <p>WHY: Độc lập hóa việc lưu trữ và phân phối đạn dự trữ khỏi từng thực thể vũ khí riêng lẻ.
 * Khi súng nạp đạn (Reload), đạn sẽ được rút trực tiếp từ kho này. Súng lục (Pistol) là ngoại lệ
 * duy nhất với cơ chế vô hạn đạn (không tiêu hao kho dự trữ).
 */
public class AmmoInventory {

    private final Map<AmmoType, Integer> reserves = new EnumMap<>(AmmoType.class);

    /**
     * Khởi tạo kho đạn dự trữ. Theo yêu cầu thiết kế, bắt đầu lượt chơi mới với 0 đạn dự trữ
     * cho tất cả các chủng loại súng chính (người chơi ban đầu chỉ có súng lục vô hạn đạn).
     */
    public AmmoInventory() {
        for (AmmoType type : AmmoType.values()) {
            reserves.put(type, 0);
        }
    }

    /**
     * Lấy số lượng đạn hiện có trong kho dự trữ cho loại đạn chỉ định.
     */
    public int getAmmo(AmmoType type) {
        if (type == null) return 0;
        return reserves.getOrDefault(type, 0);
    }

    /**
     * Lấy sức chứa tối đa của loại đạn chỉ định.
     */
    public int getMaxCapacity(AmmoType type) {
        if (type == null) return 0;
        return type.getMaxCapacity();
    }

    /**
     * Kiểm tra kho đạn của loại chỉ định đã đầy tối đa hay chưa.
     */
    public boolean isFull(AmmoType type) {
        if (type == null) return true;
        return getAmmo(type) >= getMaxCapacity(type);
    }

    /**
     * Bổ sung thêm đạn vào kho dự trữ, không vượt quá giới hạn cất trữ tối đa (Capacity Cap).
     *
     * @param type   Chủng loại đạn cần thêm.
     * @param amount Số lượng đạn muốn thêm vào.
     * @return Số lượng đạn thực tế đã được cộng thêm vào kho.
     */
    public int addAmmo(AmmoType type, int amount) {
        if (type == null || amount <= 0) return 0;

        int current = getAmmo(type);
        int max = getMaxCapacity(type);
        int canAdd = Math.max(0, max - current);
        int actualAdd = Math.min(amount, canAdd);

        reserves.put(type, current + actualAdd);
        return actualAdd;
    }

    /**
     * Trực tiếp gán số lượng đạn cho loại chỉ định (đảm bảo nằm trong khoảng [0, maxCapacity]).
     */
    public void setAmmo(AmmoType type, int amount) {
        if (type == null) return;
        int clamped = Math.max(0, Math.min(amount, getMaxCapacity(type)));
        reserves.put(type, clamped);
    }

    /**
     * Tiêu hao một lượng đạn từ kho dự trữ phục vụ việc nạp đạn (Reload).
     *
     * @param type   Chủng loại đạn cần rút.
     * @param needed Số lượng đạn cần nạp vào băng đạn.
     * @return Số lượng đạn thực tế rút được từ kho.
     */
    public int consumeAmmo(AmmoType type, int needed) {
        if (type == null || needed <= 0) return 0;

        int current = getAmmo(type);
        int actualDrawn = Math.min(needed, current);
        reserves.put(type, current - actualDrawn);
        return actualDrawn;
    }

    /**
     * Tiếp tế số lượng stack chỉ định cho toàn bộ các chủng loại đạn dự trữ.
     * Tự động giới hạn không vượt quá sức chứa tối đa của từng loại (AR: 150, SMG: 180, Shotgun: 50).
     *
     * @param stacksPerType Số lượng stack muốn cộng thêm cho mỗi loại đạn.
     */
    public void addStacksForAllTypes(int stacksPerType) {
        if (stacksPerType <= 0) return;
        for (AmmoType type : AmmoType.values()) {
            addAmmo(type, stacksPerType * type.getStackAmount());
        }
    }

    /**
     * Nạp đầy 100% dung lượng tối đa cho tất cả các loại đạn dự trữ.
     */
    public void resupplyAll() {
        for (AmmoType type : AmmoType.values()) {
            reserves.put(type, type.getMaxCapacity());
        }
    }

    /**
     * Đặt lại toàn bộ kho đạn về 0 (phục vụ khởi động ván chơi mới).
     */
    public void reset() {
        for (AmmoType type : AmmoType.values()) {
            reserves.put(type, 0);
        }
    }

    /**
     * Xuất toàn bộ kho đạn dự trữ sang Map chuỗi phục vụ lưu checkpoint.
     */
    public Map<String, Integer> exportAmmoReserves() {
        Map<String, Integer> exported = new java.util.HashMap<>();
        for (Map.Entry<AmmoType, Integer> entry : reserves.entrySet()) {
            exported.put(entry.getKey().name(), entry.getValue());
        }
        return exported;
    }

    /**
     * Khôi phục số lượng đạn dự trữ từ dữ liệu lưu checkpoint.
     */
    public void restoreAmmoReserves(Map<String, Integer> savedReserves) {
        reset();
        if (savedReserves == null) return;
        for (AmmoType type : AmmoType.values()) {
            Integer val = savedReserves.get(type.name());
            if (val != null) {
                setAmmo(type, val);
            }
        }
    }
}
