Cập nhật mới nhất vào ngày: 02/10/2026

# GameLoop.java - Vòng Lặp Trò Chơi Đa Luồng (Multithreaded Game Loop)

## 1. Mô tả
`GameLoop.java` triển khai giao diện `Runnable`, đóng vai trò là "trái tim" điều phối thời gian của toàn bộ game. Lớp này chạy trên một luồng độc lập (`Game-Loop-Worker`), áp dụng kỹ thuật **Fixed Timestep with Variable Rendering** nhằm giữ cho vật lý và logic game cập nhật đúng 60 nhịp/giây (60 UPS) trên mọi cấu hình phần cứng, không bị ảnh hưởng bởi tốc độ xử lý nhanh hay chậm của máy tính.

---

## 2. Quy trình hoạt động
1. `GameLoop` được khởi tạo và nhận tham chiếu tới `GamePanel`.
2. Khi `start()` được gọi, một luồng mới được tạo và chạy phương thức `run()`.
3. Vòng lặp tính toán độ chênh lệch thời gian (`delta`) dựa trên `System.nanoTime()`.
4. Khi `delta >= 1`:
   * Gọi `gamePanel.update(dt)` với fixed timestep $dt = 1/60$ giây.
   * Logic di chuyển, va chạm, bắn đạn được tính toán nhất quán.
5. Sau khi cập nhật logic xong, gọi `gamePanel.repaint()` để vẽ lại màn hình.
6. Mỗi 1 giây, bộ đếm đo và lưu lại chỉ số `FPS` (Frames Per Second) và `UPS` (Updates Per Second) phục vụ việc đánh giá hiệu năng.
7. Gọi `Thread.sleep(1)` ngắn để tránh tình trạng ngốn 100% CPU (Thread starvation).

---

## 3. API
* `public synchronized void start()`: Khởi động luồng vòng lặp game.
* `public synchronized void stop()`: Dừng vòng lặp an toàn và chờ luồng kết thúc (`join`).
* `public int getCurrentFPS()`: Lấy số khung hình vẽ được trong giây vừa qua.
* `public int getCurrentUPS()`: Lấy số lần cập nhật logic trong giây vừa qua.

---

## 4. Tham số Đa luồng & Concurrency
* `volatile boolean running`: Đảm bảo tính thấy được (visibility) giữa luồng điều khiển và luồng công nhân.
* Luồng riêng biệt: `"Game-Loop-Worker"`.

---

## 5. Quan hệ với hệ thống khác
* Gọi trực tiếp `gamePanel.update(deltaTime)` và `gamePanel.repaint()`.
* Được khởi chạy và dừng bởi `GameWindow.java`.

---

## 6. Sửa đổi / Cập nhật
* **02/10/2026**: Khởi tạo ban đầu với kỹ thuật Fixed Timestep và bộ đo FPS/UPS.

---

## 7. Lỗi đã sửa
Chưa ghi nhận lỗi.
