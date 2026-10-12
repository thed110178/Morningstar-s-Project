Cập nhật mới nhất vào ngày: 09/10/2026

# TileType.java - Định Danh Các Loại Ô Bản Đồ (Tile Enumeration)

## 1. Mô tả
`TileType.java` là một enum định nghĩa toàn bộ các phân loại ô địa hình trong lưới bản đồ của trò chơi. Enum này đóng gói các thuộc tính vật lý cốt lõi (tính rắn/cản đường `solid`), màu sắc trực quan mặc định (`defaultColor`), và tên hiển thị hỗ trợ gỡ lỗi.

---

## 2. Quy trình hoạt động
1. Khi khởi tạo bản đồ, mỗi ô `Tile` được gán một `TileType`.
2. Hệ thống kiểm tra va chạm chỉ cần gọi `tile.isSolid()` để biết ngay lập tức ô đó có chặn đường hay không mà không cần qua các câu lệnh rẽ nhánh `switch-case` phức tạp.
3. Các loại ô hỗ trợ:
   * `WALL`: Bức tường kiên cố, cản trở di chuyển và đạn bay.
   * `FLOOR`: Mặt đất thông thoáng cho phép đi lại và bắn xuyên qua.
   * `COVER`: Cột trụ che chắn chiến thuật vĩnh cửu không thể phá hủy.
   * `DESTRUCTIBLE_COVER`: Ô che chắn tạm thời (thùng gỗ chiến thuật) có 40 HP, cản di chuyển và chắn đạn; bị phá hủy và biến thành `FLOOR` khi nhận đủ sát thương từ đạn bắn hoặc đòn chém cận chiến.
   * `DOOR_CLOSED`: Cửa phòng chiến đấu trong trạng thái khóa chặn (`solid = true`, rào chắn laser đỏ).
   * `DOOR_OPEN`: Cửa phòng chiến đấu trong trạng thái mở thông (`solid = false`, viền xanh lục dạ quang).
   * `SPAWN_ZONE`: Vùng xuất hiện an toàn cho người chơi.
   * `EXTRACT_ZONE`: Cổng sơ tán / chuyển màn chơi kế tiếp.
   * `VOID`: Khoảng không ngoài rìa bản đồ, hoàn toàn không thể đi vào.

---

## 3. API
* `public boolean isSolid()`: Trả về `true` nếu ô gạch là chướng ngại vật cản đường.
* `public Color getDefaultColor()`: Trả về màu sắc vẽ cơ bản.
* `public String getDisplayName()`: Tên định danh hỗ trợ debug.

---

## 4. Quan hệ với hệ thống khác
* Được sử dụng bởi: `Tile.java`, `DungeonMap.java`, `Room.java`, `PlayingState.java`, `ProjectileManager.java`.

---

## 5. Sửa đổi / Cập nhật
* **03/10/2026 (Lần 1)**: Khởi tạo ban đầu phục vụ bản đồ 3 phòng (Spawn, Combat, Extract).
* **03/10/2026 (Lần 2)**: Bổ sung `DOOR_CLOSED` và `DOOR_OPEN` phục vụ cơ chế khóa/mở cửa tự động theo tình trạng kẻ địch trong Combat Room.
* **09/10/2026 (Lần 3)**: Bổ sung `DESTRUCTIBLE_COVER` - ô che chắn tạm thời có thể phá hủy khi trúng đạn hoặc chém cận chiến.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
