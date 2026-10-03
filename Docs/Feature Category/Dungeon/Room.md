Cập nhật mới nhất vào ngày: 03/10/2026

# Room.java - Lớp Biểu Diễn Căn Phòng Hầm Ngục (Dungeon Chamber)

## 1. Mô tả
`Room.java` đại diện cho một căn phòng hình chữ nhật trong hầm ngục, được xác định bởi tọa độ ô góc trên bên trái (`gridX`, `gridY`), kích thước số ô (`widthInTiles`, `heightInTiles`), vai trò chức năng (`RoomType`), trạng thái chiến đấu (`RoomStatus`), và danh sách các ô cửa rào chắn (`doorTiles`).

---

## 2. Quy trình hoạt động
1. `DungeonMap` khởi tạo các phòng với vai trò khác nhau:
   * `RoomType.SPAWN`: Phòng xuất phát an toàn ban đầu.
   * `RoomType.COMBAT`: Đấu trường chiến đấu có cửa khóa và cột trụ che chắn.
   * `RoomType.EXTRACT`: Phòng sơ tán / cổng kết thúc màn chơi.
2. Quản lý trạng thái chiến đấu của phòng:
   * `UNVISITED`: Người chơi chưa bước vào.
   * `ACTIVE_LOCKDOWN`: Người chơi đã bước vào, cửa phòng đóng sập lại thành `DOOR_CLOSED` (`isSolid = true`).
   * `CLEARED`: Toàn bộ mục tiêu đã bị tiêu diệt, cửa mở toang thành `DOOR_OPEN` (`isSolid = false`).
3. Cung cấp các phương thức điều khiển:
   * `lockdown()`: Kích hoạt đóng toàn bộ cửa phòng.
   * `unlock()`: Kích hoạt mở toàn bộ cửa phòng và đánh dấu phòng đã hoàn thành.

---

## 3. API
* `public void addDoorTile(Tile doorTile)`: Đăng ký một ô cửa thuộc về lối vào/ra của phòng.
* `public void lockdown()`: Đóng và biến các ô cửa thành vật cản rắn (`DOOR_CLOSED`).
* `public void unlock()`: Mở và biến các ô cửa thành lối đi thông thoáng (`DOOR_OPEN`).
* `public boolean isLocked()` / `public boolean isCleared()`: Kiểm tra trạng thái khóa/hoàn thành.
* `public boolean containsWorldPointInterior(double worldX, double worldY)`: Kiểm tra người chơi đã bước hẳn vào lòng phòng (không tính mép tường).
* `public double getCenterWorldX()`, `public double getCenterWorldY()`: Lấy tọa độ trung tâm phòng (pixel).
* `public RoomType getRoomType()`: Lấy vai trò chức năng của phòng.
* `public String getName()`: Lấy tên hiển thị của phòng.

---

## 4. Quan hệ với hệ thống khác
* Được quản lý trong danh sách `List<Room>` của `DungeonMap`.
* Nhận lệnh từ: `PlayingState` (kiểm tra điều kiện người chơi vào phòng và diệt hết địch).

---

## 5. Sửa đổi / Cập nhật
* **03/10/2026 (Lần 1)**: Khởi tạo ban đầu với các phép tính tọa độ trung tâm và nhận diện điểm nằm trong phòng.
* **03/10/2026 (Lần 2)**: Tích hợp `RoomStatus`, danh sách `doorTiles`, phương thức `lockdown()` và `unlock()`.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
