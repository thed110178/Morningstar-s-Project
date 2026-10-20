Cập nhật mới nhất vào ngày: 20/10/2026

# KeyboardHandler.java - Bộ Lắng Nghe & Xử Lý Sự Kiện Bàn Phím

## 1. Mô tả
`KeyboardHandler.java` triển khai `java.awt.event.KeyListener`, chịu trách nhiệm theo dõi và lưu trữ trạng thái của tất cả các phím bấm trên bàn phím. Lớp này khắc phục triệt để vấn đề "độ trễ lặp phím" (Key Repeat Delay) của hệ điều hành, giúp nhân vật di chuyển 8 hướng mượt mà và phân biệt chính xác giữa việc "giữ phím liên tục" (như WASD để chạy) với "nhấn phím một lần duy nhất" (như ESC để mở menu, SPACE để lướt, R để thay đạn).

---

## 2. Quy trình hoạt động
1. Sử dụng 3 mảng boolean kích thước 512 phần tử:
   * `keys[]`: Trạng thái thực tế phím đang được đè xuống hay thả ra.
   * `keysJustPressed[]`: Cờ báo phím vừa mới được ấn xuống ở frame hiện tại (chỉ `true` đúng 1 nhịp tick).
   * `keysCantPress[]`: Cờ khóa chống kích hoạt lặp lại khi người dùng vẫn đang giữ phím.
2. Khi hệ điều hành phát sinh sự kiện `keyPressed`: Đánh dấu `keys[code] = true`.
3. Khi hệ điều hành phát sinh sự kiện `keyReleased`: Đánh dấu `keys[code] = false`.
4. Hàm `update()` được gọi ở đầu mỗi tick logic của game để cập nhật trạng thái `keysJustPressed` và giải phóng khóa `keysCantPress` khi phím đã được nhả ra.
5. Cung cấp phương thức `getAnyKeyPressedOnce()` quét toàn bộ mảng `keysJustPressed` để phát hiện mã phím mới phục vụ giao diện gán phím tùy chỉnh (Keybind Rebinding).

---

## 3. API
* `public void update()`: Cập nhật vòng đời các cờ bấm phím một lần.
* `public boolean isKeyDown(int keyCode)`: Kiểm tra phím có đang được giữ hay không (dùng cho WASD di chuyển).
* `public boolean isKeyPressedOnce(int keyCode)`: Kiểm tra phím có vừa mới được nhấn một lần hay không (dùng cho ESC, Space, Enter, R).
* `public int getAnyKeyPressedOnce()`: Trả về mã keyCode của phím đầu tiên được nhấn trong khung hình hiện tại, hoặc `-1` nếu không có phím nào được nhấn. Phục vụ tính năng bắt phím trong giao diện Cài đặt phím (Keybind Settings).

---

## 4. Quan hệ với hệ thống khác
* Được đăng ký làm listener trong `GamePanel`.
* Được truyền vào phương thức `update` của `GameState` và các lớp con (`MenuState`, `PlayingState`).
* Kết nối trực tiếp với `KeyBindings` và `PauseMenuUI` trong việc bắt phím và chuyển đổi cấu hình phím.

---

## 5. Sửa đổi / Cập nhật
* **02/10/2026**: Khởi tạo ban đầu với cơ chế 3 mảng trạng thái chống trễ phím.
* **20/10/2026**: Bổ sung API `getAnyKeyPressedOnce()` phục vụ tính năng bắt phím gán mới cho hệ thống Keybind Settings.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
