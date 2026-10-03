Cập nhật mới nhất vào ngày: 03/10/2026

# DungeonMap.java - Hệ Thống Bản Đồ 3 Phòng & Kiểm Tra Va Chạm Lưới

## 1. Mô tả
`DungeonMap.java` quản lý toàn bộ ma trận địa hình 2 chiều $80 \times 45$ ô gạch của thế giới game ($3840 \times 2160$ px). Lớp này chịu trách nhiệm:
1. Xây dựng bố cục 3 phòng liên hoàn: **Spawn Room (Phòng xuất phát)** $\rightarrow$ **Combat Arena (Đấu trường chiến đấu)** $\rightarrow$ **Extract Room (Phòng sơ tán)** kết nối qua các hành lang chiến thuật.
2. Kiểm tra va chạm hộp bao (AABB) siêu nhanh $O(1)$ thông qua phương thức `isAreaWalkable`.
3. Tối ưu hóa hiển thị đồ họa bằng thuật toán **Viewport Culling**, chỉ vẽ các ô gạch lọt vào khung nhìn của `Camera`.

---

## 2. Quy trình hoạt động
1. **Khởi tạo (`DungeonMap`)**:
   * Khởi tạo mảng `Tile[80][45]` toàn bộ là `TileType.VOID`.
   * Khắc (Carve) **Spawn Room** tại cột 6, hàng 16 ($14 \times 12$ ô), đặt ô `SPAWN_ZONE` ở giữa.
   * Khắc **Combat Arena** tại cột 28, hàng 11 ($24 \times 22$ ô), tạo thêm 4 cột trụ `COVER` kiên cố bên trong.
   * Khắc **Extract Room** tại cột 60, hàng 16 ($14 \times 12$ ô), đặt ô `EXTRACT_ZONE` ở giữa.
   * Đào 2 hành lang ngang rộng 4 ô kết nối lần lượt Spawn $\rightarrow$ Combat và Combat $\rightarrow$ Extract.
2. **Kiểm tra va chạm (`isAreaWalkable`)**:
   * Nhận hộp bao nhân vật $(x, y, w, h)$.
   * Quy đổi ra khoảng chỉ số ô lưới: `minCol`, `maxCol`, `minRow`, `maxRow`.
   * Duyệt qua 4 đến 9 ô liên quan. Nếu có bất kỳ ô nào vượt biên hoặc có thuộc tính `isSolid() == true`, lập tức trả về `false`.
3. **Hiển thị đồ họa (`render`)**:
   * Xác định tọa độ cột/hàng bắt đầu và kết thúc dựa trên tọa độ góc của `Camera`.
   * Chỉ vẽ các ô nằm trong phạm vi này (khoảng 350 - 400 ô thay vì toàn bộ 3.600 ô).
   * Vẽ nhãn chỉ thị khu vực nổi bên trên các phòng khi camera di chuyển tới.

---

## 3. API
* `public boolean isAreaWalkable(double worldX, double worldY, double width, double height)`: Kiểm tra một vùng hình chữ nhật có thể di chuyển vào được không (chỉ chặn các ô `isSolid = true`).
* `public boolean isAreaBlockingProjectiles(double worldX, double worldY, double width, double height)`: Kiểm tra vùng có cản trở đạn bay hay không (chặn tường, vật cản, cửa đóng và hấp thụ đạn khi gặp cửa mở `DOOR_OPEN`).
* `public boolean isIntersectsExtractZone(double worldX, double worldY, double width, double height)`: Kiểm tra hitbox thực thể có chạm vào bệ sơ tán hay không.
* `public boolean isOnExtractZone(double worldX, double worldY)`: Kiểm tra điểm tọa độ có nằm trên ô sơ tán hay không.
* `public void reset()`: Hoàn nguyên trạng thái của toàn bộ phòng và mở lại toàn bộ cửa trong hầm ngục.
* `public void render(Graphics2D g, Camera camera)`: Vẽ bản đồ áp dụng Viewport Culling.
* `public Room getSpawnRoom()`, `getCombatRoom()`, `getExtractRoom()`: Lấy tham chiếu các phòng cụ thể.
* `public List<Room> getRooms()`: Lấy danh sách tất cả các phòng.
* `public Tile getTile(int col, int row)`: Truy xuất ô gạch tại tọa độ lưới.

---

## 4. Quan hệ với hệ thống khác
* Sử dụng: `Tile`, `TileType`, `Room`, `GameConfig`, `Camera`.
* Được gọi trực tiếp bởi: `PlayingState` (di chuyển, va chạm, sơ tán và hiển thị), `ProjectileManager` (triệt tiêu đạn bay).

---

## 5. Sửa đổi / Cập nhật
* **03/10/2026 (Lần 1)**: Khởi tạo ban đầu với layout 3 phòng, kiểm tra va chạm tường $O(1)$ và Viewport Culling.
* **03/10/2026 (Lần 2)**: Bổ sung `reset()`, `isIntersectsExtractZone()`, `isOnExtractZone()`, và `isAreaBlockingProjectiles()` ngăn bắn tỉa quái qua cửa mở.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
