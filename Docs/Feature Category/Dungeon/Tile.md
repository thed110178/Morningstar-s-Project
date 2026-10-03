Cập nhật mới nhất vào ngày: 03/10/2026

# Tile.java - Lớp Biểu Diễn Ô Gạch Địa Hình (Terrain Tile Unit)

## 1. Mô tả
`Tile.java` đại diện cho một ô gạch đơn lẻ trên lưới ma trận bản đồ ($48 \times 48$ px). Lớp này lưu giữ tọa độ ô lưới (`gridX`, `gridY`), tọa độ thế giới thực tế (`worldX`, `worldY`), phân loại địa hình (`TileType`) và phương thức vẽ đồ họa chi tiết (`render`).

---

## 2. Quy trình hoạt động
1. Khi `DungeonMap` khởi tạo, các đối tượng `Tile` được phân bổ vào mảng 2 chiều `tiles[COLS][ROWS]`.
2. Khi thực thể cần kiểm tra va chạm, `Tile.isSolid()` được gọi để biết ô có cản đường hay không.
3. Khi vẽ màn hình:
   * Phương thức `render(Graphics2D g, int screenX, int screenY)` nhận tọa độ màn hình đã trừ độ lệch camera.
   * Vẽ màu nền cơ sở của `TileType`.
   * Thêm các chi tiết đồ họa viền nổi: vát cạnh 3D cho `WALL`, gạch chéo chiến thuật cho `COVER`, hào quang xanh cho `SPAWN_ZONE`, và vòng xoáy tím cho `EXTRACT_ZONE`.

---

## 3. API
* `public boolean isSolid()`: Kiểm tra ô có cản trở di chuyển hay không.
* `public int getGridX()`, `public int getGridY()`: Lấy tọa độ cột và hàng trên lưới.
* `public int getWorldX()`, `public int getWorldY()`: Lấy tọa độ pixel góc trên bên trái trong thế giới.
* `public TileType getType()` / `public void setType(TileType type)`: Đọc/ghi kiểu ô gạch.
* `public void render(Graphics2D g, int screenX, int screenY)`: Vẽ ô gạch tại tọa độ màn hình.

---

## 4. Quan hệ với hệ thống khác
* Chứa thuộc tính: `TileType`.
* Được quản lý bởi: `DungeonMap.java`.

---

## 5. Sửa đổi / Cập nhật
* **03/10/2026**: Khởi tạo ban đầu với khả năng vẽ chi tiết từng loại địa hình.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
