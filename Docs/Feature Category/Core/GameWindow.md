Cập nhật mới nhất vào ngày: 02/10/2026

# GameWindow.java - Cửa Sổ Ứng Dụng (JFrame Container)

## 1. Mô tả
`GameWindow.java` kế thừa từ `javax.swing.JFrame`, chịu trách nhiệm cấu hình cửa sổ hệ điều hành (tiêu đề, kích thước, nút tắt, canh giữa màn hình), đóng gói `GamePanel` và điều khiển vòng đời khởi động/dừng của `GameLoop`.

---

## 2. Quy trình hoạt động
1. Khởi tạo đối tượng `JFrame` với tiêu đề lấy từ `GameConfig.GAME_TITLE`.
2. Thiết lập `setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)` và `setResizable(false)` để đảm bảo tỷ lệ khung nhìn cố định (16:9).
3. Khởi tạo `GamePanel` và gắn vào `GameLoop`.
4. Gọi `pack()` để kích thước cửa sổ khớp chính xác với `setPreferredSize` của `GamePanel`.
5. Đặt cửa sổ ở giữa màn hình bằng `setLocationRelativeTo(null)`.
6. Lắng nghe sự kiện đóng cửa sổ (`WindowAdapter.windowClosing`) để dừng an toàn luồng `GameLoop` trước khi thoát hẳn tiến trình.
7. Khi gọi `launch()`: Hiển thị cửa sổ lên màn hình (`setVisible(true)`), xin quyền nhận tiêu điểm bàn phím (`requestFocusInWindow()`) và bắt đầu chạy `GameLoop`.

---

## 3. API
* `public GameWindow()`: Constructor cấu hình toàn bộ cửa sổ và khởi tạo các thành phần cốt lõi.
* `public void launch()`: Hiển thị cửa sổ và bắt đầu vòng lặp game.

---

## 4. Quan hệ với hệ thống khác
* Chứa: `GamePanel`, `GameLoop`.
* Được gọi trực tiếp bởi: `Main.java`.

---

## 5. Sửa đổi / Cập nhật
* **02/10/2026**: Khởi tạo ban đầu với xử lý đóng luồng game an toàn trong `WindowAdapter`.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
