Cập nhật mới nhất vào ngày: 03/10/2026

# Projectile.java & ProjectileManager.java - Hệ Thống Đạn Bay & An Toàn Đa Luồng

## 1. Mô tả
Bộ đôi lớp này quản lý toàn bộ vòng đời của các viên đạn bay trong thế giới 2D:
* `Projectile.java`: Biểu diễn một viên đạn duy nhất với vận tốc góc, cự ly sống (lifetime), và sát thương.
* `ProjectileManager.java`: Bộ điều phối danh sách đạn bay, kiểm tra va chạm tường với `DungeonMap` và đảm bảo an toàn đa luồng (**Concurrency Safety**) tránh lỗi `ConcurrentModificationException`.

---

## 2. Quy trình hoạt động
1. Khi vũ khí (như Súng lục) bắn: Gọi `pm.spawn(new Projectile(...))`. Viên đạn được đưa vào danh sách đệm `pendingAdd`.
2. Ở đầu mỗi tick update: Các viên đạn từ `pendingAdd` được tích hợp vào danh sách chính `projectiles`.
3. Tọa độ đạn được cập nhật theo vận tốc $v_x, v_y$:
   $$x = x + v_x \times \Delta t, \quad y = y + v_y \times \Delta t$$
4. Đạn được kiểm tra va chạm với tường hầm ngục:
   * Nếu `dungeonMap.isAreaWalkable(px, py, 4, 4) == false`, đạn bị vô hiệu hóa lập tức (`active = false`).
5. Khi hết thời gian sống hoặc va chạm: Đạn được dọn dẹp sạch sẽ khỏi bộ nhớ (`removeIf(!isActive)`).
6. Khi vẽ: Đạn có vầng hào quang phát sáng (glow) xung quanh lõi đạn vàng rực.

---

## 3. API
### Projectile:
* `public void update(double deltaTime)`: Cập nhật tọa độ và giảm thời gian sống.
* `public void render(Graphics2D g, Camera camera)`: Vẽ viên đạn phát sáng.
* `public Rectangle getBounds()`: Lấy hộp bao va chạm của đạn.
* `public void deactivate()`: Vô hiệu hóa viên đạn khi va chạm.

### ProjectileManager:
* `public synchronized void spawn(Projectile p)`: Thêm đạn vào hàng đợi an toàn.
* `public synchronized void update(double dt, DungeonMap map)`: Cập nhật toàn bộ đạn và va chạm tường.
* `public synchronized void render(Graphics2D g, Camera camera)`: Vẽ toàn bộ đạn.
* `public synchronized void clear()`: Xóa sạch danh sách đạn khi đổi màn.

---

## 4. Quan hệ với hệ thống khác
* Nhận đạn từ: `Pistol` (hoặc các loại súng khác sau này).
* Tương tác với: `DungeonMap` (va chạm địa hình) và `PlayingState`.

---

## 5. Sửa đổi / Cập nhật
* **03/10/2026**: Khởi tạo ban đầu với cơ chế buffer chống lỗi Concurrency.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
