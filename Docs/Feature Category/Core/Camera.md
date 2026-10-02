Cập nhật mới nhất vào ngày: 02/10/2026

# Camera.java - Hệ Thống Camera 2D & Nội Suy Tuyến Tính (Lerp)

## 1. Mô tả
`Camera.java` là hệ thống Camera 2D trực giao (Orthographic 2D Camera), chịu trách nhiệm theo dõi nhân vật người chơi trên bản đồ lớn, tính toán độ dịch chuyển (offset) và chuyển đổi hai chiều giữa tọa độ màn hình (Screen Coordinates) và tọa độ thế giới (World Coordinates). Camera tích hợp thuật toán nội suy tuyến tính (**Linear Interpolation - Lerp**) giúp góc nhìn lia mượt mà, không bị giật khung hình khi người chơi đổi hướng đột ngột.

---

## 2. Quy trình hoạt động
1. **Theo dõi nhân vật (`update(focusX, focusY)`)**:
   * Tính toán điểm mục tiêu để tâm nhân vật luôn nằm chính giữa màn hình:
     $$\text{targetX} = \text{focusX} - \frac{\text{viewportWidth}}{2}, \quad \text{targetY} = \text{focusY} - \frac{\text{viewportHeight}}{2}$$
   * Áp dụng công thức Lerp với hệ số trễ $\alpha = 0.1$:
     $$x = x + (\text{targetX} - x) \times \alpha, \quad y = y + (\text{targetY} - y) \times \alpha$$
   * Kiểm tra và ép biên (`clamp()`) không cho Camera trôi ra ngoài rìa bản đồ thế giới (`GameConfig.WORLD_WIDTH`, `WORLD_HEIGHT`).
2. **Chuyển đổi tọa độ**:
   * Tọa độ màn hình sang tọa độ thế giới: $\text{worldX} = \text{screenX} + x$
   * Tọa độ thế giới sang tọa độ màn hình: $\text{screenX} = \text{round}(\text{worldX} - x)$

---

## 3. API
* `public void update(double focusX, double focusY)`: Cập nhật vị trí camera bám theo điểm tiêu điểm.
* `public double screenToWorldX(int screenX)` / `screenToWorldY(int screenY)`: Chuyển đổi tọa độ chuột/màn hình sang tọa độ thực trong map.
* `public int worldToScreenX(double worldX)` / `worldToScreenY(double worldY)`: Chuyển đổi tọa độ thực thể trong map sang vị trí vẽ trên màn hình.
* `public double getX()`, `public double getY()`: Lấy tọa độ góc trên bên trái của Camera.

---

## 4. Quan hệ với hệ thống khác
* Được sử dụng bởi: `PlayingState` (để vẽ map, thực thể, và chuyển đổi tọa độ chuột ngắm 360 độ).

---

## 5. Sửa đổi / Cập nhật
* **02/10/2026**: Khởi tạo ban đầu với kỹ thuật Lerp 0.1 và tính năng khóa biên bản đồ (`clamp`).

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
