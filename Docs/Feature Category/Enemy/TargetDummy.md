Cập nhật mới nhất vào ngày: 03/10/2026

# TargetDummy.java - Mục Tiêu Thử Nghiệm Trong Phòng Chiến Đấu

## 1. Mô tả
`TargetDummy.java` đại diện cho mục tiêu kẻ địch giả định đặt bên trong đấu trường **Combat Arena**. Mỗi mục tiêu có một lượng máu (HP) tối đa, thanh máu hiển thị trên đầu (Overhead Health Bar), hiệu ứng nhấp nháy trắng khi trúng đạn (Hit-flash), và sẽ bị tiêu diệt khi máu về 0.

---

## 2. Quy trình hoạt động
1. Khi bước vào màn chơi, `PlayingState` sinh ra 3 mục tiêu `TargetDummy` tại các vị trí chiến thuật xung quanh 4 cột trụ che chắn trong Combat Arena.
2. Khi viên đạn (`Projectile`) hoặc nhát chém dao (`MeleeSlash`) chạm vào hộp bao của mục tiêu:
   * Gọi `dummy.takeDamage(damage)`.
   * Mục tiêu bị trừ máu và bật cờ `hitFlashTimer = 0.1` để chớp trắng phản hồi lực tác động.
   * Đạn va chạm sẽ bị tiêu hao (`deactivate`).
3. Khi `currentHealth <= 0`:
   * Mục tiêu đánh dấu `alive = false` và tự động bị loại bỏ khỏi danh sách thực thể sống.
4. Khi toàn bộ các mục tiêu trong phòng bị tiêu diệt, `PlayingState` kích hoạt mở toàn bộ các cửa phòng Combat (`DOOR_OPEN`).

---

## 3. API
* `public void takeDamage(int damage)`: Trừ máu và kích hoạt hiệu ứng chớp trắng.
* `public void update(double deltaTime)`: Đếm ngược hiệu ứng hit-flash.
* `public void render(Graphics2D g, Camera camera)`: Vẽ thân mục tiêu và thanh máu mini.
* `public Rectangle getBounds()`: Lấy hộp bao AABB để kiểm tra va chạm đạn và cận chiến.
* `public boolean isAlive()`: Kiểm tra mục tiêu còn sống hay không.

---

## 4. Quan hệ với hệ thống khác
* Được kiểm tra va chạm bởi: `ProjectileManager` và `WeaponManager` trong `PlayingState`.
* Quyết định điều kiện kết thúc lockdown và mở cửa của `Room`.

---

## 5. Sửa đổi / Cập nhật
* **03/10/2026**: Khởi tạo ban đầu phục vụ kiểm tra cơ chế chiến đấu và mở cửa phòng.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
