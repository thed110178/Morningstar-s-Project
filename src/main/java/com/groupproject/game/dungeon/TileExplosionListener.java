package com.groupproject.game.dungeon;

/**
 * Bộ lắng nghe sự kiện bộc phá phát nổ của địa hình (như thùng phi nổ).
 *
 * <p>WHY: Phân tách trách nhiệm (Decoupling) giữa hệ thống ô bản đồ (Tile / DungeonMap)
 * và logic xử lý tác chiến (PlayingState). Khi một ô thùng phi nổ bị phá hủy, nó thông báo
 * qua interface này để PlayingState áp dụng hiệu ứng thị giác nổ, âm thanh, rung lắc màn hình,
 * và gây sát thương diện rộng (AoE) lên cả người chơi lẫn kẻ địch.
 */
@FunctionalInterface
public interface TileExplosionListener {

    /**
     * Được gọi khi một ô thùng phi nổ bị kích hoạt bộc phá.
     *
     * @param col          Cột lưới tọa độ của ô phát nổ.
     * @param row          Hàng lưới tọa độ của ô phát nổ.
     * @param worldCenterX Tọa độ X tâm thế giới của ô phát nổ.
     * @param worldCenterY Tọa độ Y tâm thế giới của ô phát nổ.
     * @param radius       Bán kính vụ nổ (px).
     * @param damage       Sát thương nổ cực đại tại tâm.
     */
    void onTileExploded(int col, int row, double worldCenterX, double worldCenterY, double radius, int damage);
}

