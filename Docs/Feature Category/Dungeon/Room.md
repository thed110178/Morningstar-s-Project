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
* `public void reset()`: Đưa trạng thái phòng về `UNVISITED` và mở lại toàn bộ các ô cửa thành `DOOR_OPEN`.
* `public boolean isLocked()` / `public boolean isCleared()`: Kiểm tra trạng thái khóa/hoàn thành.
* `public boolean hasEntityEntered(double x, double y, double width, double height)`: Nhận diện chính xác khi tâm thực thể bước qua ngưỡng cửa vào phạm vi sàn trong của phòng.
* `public boolean containsWorldPointInterior(double worldX, double worldY)`: Kiểm tra tọa độ điểm nằm trong phạm vi sàn nội thất (cách tường biên 1 ô gạch).
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
* **03/10/2026 (Lần 3)**: Bổ sung `hasEntityEntered()`, `reset()`, và chuẩn hóa ngưỡng phát hiện sàn phòng cách tường 1 ô gạch.

---

## 6. Lỗi đã sửa
* **Ngày sửa**: 03/10/2026
* **Nguyên nhân lỗi**: Cửa Combat Room không đóng khi người chơi bước vào do:
  1. Ngưỡng phát hiện cũ `containsWorldPointInterior` đặt thụt vào quá sâu (`gridX + 1.2` gạch = 57.6px), người chơi bước qua cửa nhưng chưa đi sâu vào giữa phòng nên chưa kích hoạt đóng cửa.
  2. Chưa có phương thức `reset()` cho phòng và bản đồ, nên khi chơi lại màn từ menu, phòng vẫn giữ cờ `CLEARED` hoặc `ACTIVE_LOCKDOWN`, dẫn đến điều kiện kích hoạt `status == UNVISITED` bị bỏ qua.
* **Cách khắc phục**:
  1. Bổ sung `hasEntityEntered()` phát hiện tức thì khi tâm nhân vật bước qua ô cửa vào sàn phòng (`col >= 29`).
  2. Thêm phương thức `reset()` khôi phục trạng thái `UNVISITED` và mở lại toàn bộ cửa.
  3. Trong `PlayingState`, bổ sung cơ chế đẩy nhẹ an toàn (`nudge`) đảm bảo hitbox người chơi không bị đè kẹt trong ô cửa khi cửa chuyển sang vật cản rắn (`DOOR_CLOSED`).
* **Ảnh hưởng tới hệ thống**: Đảm bảo cửa phòng chiến đấu đóng dứt khoát, tin cậy 100% khi người chơi bước vào phòng và không bao giờ bị kẹt người chơi bên trong tường va chạm.
