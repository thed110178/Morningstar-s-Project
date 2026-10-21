Cập nhật mới nhất vào ngày: 02/10/2026

# MouseHandler.java - Bộ Lắng Nghe Chuột & Hỗ Trợ Ngắm Bắn 360 Độ

## 1. Mô tả
`MouseHandler.java` kế thừa từ `java.awt.event.MouseAdapter`, quản lý tọa độ con trỏ chuột trên màn hình (`screenX`, `screenY`), các trạng thái nhấn giữ và nhấp một lần của chuột trái (Chuột 1 - Bắn súng) và chuột phải (Chuột 3 - Kỹ năng/Ngắm). Đây là nền tảng cốt lõi cho tính năng xoay súng tự do 360 độ phong cách Soul Knight và Surviv.io.

---

## 2. Quy trình hoạt động
1. Lắng nghe các sự kiện chuột từ `GamePanel`: `mouseMoved`, `mouseDragged`, `mousePressed`, `mouseReleased`.
2. Khi chuột di chuyển hoặc kéo rê: Liên tục cập nhật `screenX = e.getX()` và `screenY = e.getY()`.
3. Khi chuột được ấn xuống (`mousePressed`):
   * Phím chuột trái: Gán `leftPressed = true` và `leftClickedOnce = true`.
   * Phím chuột phải: Gán `rightPressed = true` và `rightClickedOnce = true`.
4. Khi chuột nhả ra (`mouseReleased`): Gán `leftPressed = false` hoặc `rightPressed = false`.
5. Phương thức `update()` được gọi ở cuối mỗi frame trong `GamePanel` để reset lại các cờ `leftClickedOnce` và `rightClickedOnce` về `false`, đảm bảo mỗi cú nhấp chuột chỉ được xử lý đúng 1 lần cho các hành động tương tác Menu.

---

## 3. API
* `public void update()`: Reset các cờ click một lần sau mỗi frame.
* `public int getScreenX()`, `public int getScreenY()`: Lấy tọa độ pixel của chuột trên cửa sổ game.
* `public boolean isLeftPressed()`: Kiểm tra chuột trái có đang được giữ hay không (dùng cho sấy súng liên thanh).
* `public boolean isLeftClickedOnce()`: Kiểm tra chuột trái có vừa được nhấp hay không (dùng cho bắn từng viên hoặc bấm nút Menu).
* `public boolean isRightPressed()`, `public boolean isRightClickedOnce()`: Kiểm tra thao tác chuột phải.

---

## 4. Quan hệ với hệ thống khác
* Được đăng ký làm MouseListener, MouseMotionListener và MouseWheelListener trong `GamePanel`.
* Cung cấp tọa độ cho `MenuState` (bấm nút) và `PlayingState` (tính góc xoay súng $\theta = \text{atan2}(dy, dx)$).

---

## 5. Sửa đổi / Cập nhật
* **02/10/2026**: Khởi tạo ban đầu hỗ trợ đầy đủ tọa độ, nhấp giữ và nhấp một lần.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
