Cập nhật mới nhất vào ngày: 03/10/2026

# Room.java - Lớp Biểu Diễn Căn Phòng Hầm Ngục (Dungeon Chamber)

## 1. Mô tả
`Room.java` đại diện cho một căn phòng hình chữ nhật trong hầm ngục, được xác định bởi tọa độ ô góc trên bên trái (`gridX`, `gridY`), kích thước số ô (`widthInTiles`, `heightInTiles`), vai trò chức năng (`RoomType`), và tên định danh phòng.

---

## 2. Quy trình hoạt động
1. `DungeonMap` khởi tạo các phòng với vai trò khác nhau:
   * `RoomType.SPAWN`: Phòng xuất phát an toàn ban đầu.
   * `RoomType.COMBAT`: Đấu trường chiến đấu có cột trụ che chắn.
   * `RoomType.EXTRACT`: Phòng sơ tán / cổng kết thúc màn chơi.
2. Cung cấp các phương thức tính toán tọa độ trung tâm phòng (`getCenterWorldX()`, `getCenterWorldY()`) để đặt điểm xuất hiện của người chơi, vị trí sinh quái hoặc đặt cổng dịch chuyển.
3. Cung cấp phương thức `containsWorldPoint(worldX, worldY)` để xác định người chơi hiện đang đứng ở phòng nào phục vụ hiển thị trên HUD.

---

## 3. API
* `public double getCenterWorldX()`, `public double getCenterWorldY()`: Lấy tọa độ trung tâm phòng (pixel).
* `public boolean containsWorldPoint(double worldX, double worldY)`: Kiểm tra một tọa độ pixel có nằm trong lòng phòng hay không.
* `public Rectangle getWorldBounds()`: Lấy hình chữ nhật bao quanh phòng trong không gian thế giới.
* `public RoomType getRoomType()`: Lấy vai trò chức năng của phòng.
* `public String getName()`: Lấy tên hiển thị của phòng.

---

## 4. Quan hệ với hệ thống khác
* Được quản lý trong danh sách `List<Room>` của `DungeonMap`.
* Được truy vấn bởi `PlayingState` để hiển thị tên khu vực hiện tại trên thanh HUD.

---

## 5. Sửa đổi / Cập nhật
* **03/10/2026**: Khởi tạo ban đầu với các phép tính tọa độ trung tâm và nhận diện điểm nằm trong phòng.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
