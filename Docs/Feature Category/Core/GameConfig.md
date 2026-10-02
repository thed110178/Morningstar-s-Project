Cập nhật mới nhất vào ngày: 02/10/2026

# GameConfig.java - Cấu Hình Toàn Cục & Cân Bằng Game

## 1. Mô tả
`GameConfig.java` là lớp tiện ích tĩnh (Utility Class) lưu trữ toàn bộ các hằng số cấu hình hệ thống, độ phân giải màn hình, thông số nhịp độ khung hình (FPS/UPS), kích thước thế giới game và các chỉ số cân bằng mặc định của nhân vật. Lớp này được đánh dấu `final` và có constructor `private` nhằm ngăn chặn việc khởi tạo đối tượng.

---

## 2. Quy trình hoạt động
1. Khi nạp các lớp khác trong game (`GamePanel`, `GameLoop`, `Camera`, `PlayingState`), JVM tự động nạp các hằng số tĩnh từ `GameConfig`.
2. Mọi thay đổi về độ phân giải, tốc độ di chuyển, giới hạn FPS chỉ cần chỉnh sửa tại một nơi duy nhất (Single Source of Truth), đảm bảo tính nhất quán trên toàn bộ hệ thống.

---

## 3. API / Danh sách Hằng số
* `GAME_TITLE`: Tiêu đề hiển thị trên thanh tiêu đề cửa sổ JFrame.
* `SCREEN_WIDTH`, `SCREEN_HEIGHT`: Kích thước khung nhìn cửa sổ (1280x720).
* `TARGET_FPS`, `TARGET_UPS`: Tốc độ làm tươi đồ họa (60 FPS) và tần số cập nhật logic vật lý (60 updates/sec).
* `OPTIMAL_TIME_NS`: Khoảng thời gian tối ưu cho mỗi tick logic (tính bằng nano giây).
* `TILE_SIZE`: Kích thước cơ sở của 1 ô gạch bản đồ (48x48 pixel).
* `WORLD_WIDTH`, `WORLD_HEIGHT`: Kích thước toàn bộ thế giới game (3840x2160 pixel).
* `PLAYER_DEFAULT_SPEED`: Vận tốc di chuyển cơ bản của người chơi (pixel/giây).
* `PLAYER_MAX_HEALTH`, `PLAYER_MAX_SHIELD`, `PLAYER_MAX_ENERGY`: Giới hạn sinh mệnh của nhân vật.

---

## 4. Quan hệ với hệ thống khác
* Được sử dụng bởi: `GameWindow`, `GamePanel`, `GameLoop`, `Camera`, `PlayingState`, `Player`.

---

## 5. Sửa đổi / Cập nhật
* **02/10/2026**: Cập nhật lại tiêu đề `GAME_TITLE` thành `"Project DontKnown- An unplanned top-down roguelite shooter"`.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
