Cập nhật mới nhất vào ngày: 02/10/2026

# GamePanel.java - Khung Vẽ Đồ Họa & Điều Phối Sự Kiện

## 1. Mô tả
`GamePanel.java` kế thừa từ `javax.swing.JPanel`, là bề mặt canvas chính để hiển thị mọi hình ảnh trong trò chơi. Lớp này kích hoạt tính năng **Double Buffering** phần cứng nhằm loại bỏ triệt để hiện tượng nhấp nháy (flickering) và xé hình (tearing). Đồng thời, `GamePanel` là nơi đăng ký các bộ lắng nghe sự kiện chuột (`MouseHandler`) và bàn phím (`KeyboardHandler`).

---

## 2. Quy trình hoạt động
1. **Khởi tạo**: Thiết lập kích thước cửa sổ (`GameConfig.SCREEN_WIDTH`, `SCREEN_HEIGHT`), bật `setDoubleBuffered(true)`, `setFocusable(true)`.
2. Đăng ký các đối tượng `KeyboardHandler`, `MouseHandler` và `GameStateManager`.
3. Đăng ký các trạng thái mặc định (`MenuState`, `PlayingState`) vào `GameStateManager`.
4. **Cập nhật (`update(double deltaTime)`)**:
   * Gọi `keyHandler.update()` để cập nhật cờ phím bấm một lần.
   * Chuyển quyền cập nhật cho State hiện tại thông qua `stateManager.update(deltaTime, keyHandler, mouseHandler)`.
   * Reset trạng thái nhấp chuột thông qua `mouseHandler.update()`.
5. **Vẽ (`paintComponent(Graphics g)`)**:
   * Ép kiểu sang `Graphics2D`.
   * Kích hoạt các cờ tối ưu đồ họa (`RenderingHints.KEY_ANTIALIASING`, `KEY_TEXT_ANTIALIASING`, `KEY_RENDERING`).
   * Chuyển quyền vẽ cho State hiện tại thông qua `stateManager.render(g2d)`.
   * Hủy đối tượng cọ vẽ `g2d.dispose()` để giải phóng bộ nhớ hệ thống.

---

## 3. API
* `public void update(double deltaTime)`: Cập nhật dữ liệu toàn game theo khoảng thời gian thực tế.
* `protected void paintComponent(Graphics g)`: Phương thức vẽ lại canvas đồ họa của Swing.
* `public GameStateManager getStateManager()`: Truy xuất bộ quản lý trạng thái.

---

## 4. Quan hệ với hệ thống khác
* Chứa và điều phối: `KeyboardHandler`, `MouseHandler`, `GameStateManager`.
* Nhận lệnh từ: `GameLoop` (gọi `update` và `repaint`).
* Được chứa trong: `GameWindow`.

---

## 5. Sửa đổi / Cập nhật
* **02/10/2026**: Khởi tạo ban đầu với Double Buffering và Rendering Hints.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
