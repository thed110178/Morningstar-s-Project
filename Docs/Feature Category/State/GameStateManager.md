Cập nhật mới nhất vào ngày: 02/10/2026

# GameStateManager.java - Bộ Quản Lý Trạng Thái Game

## 1. Mô tả
`GameStateManager.java` (GSM) đóng vai trò là Context trong mẫu thiết kế **State Pattern**. Lớp này lưu trữ danh mục các trạng thái trò chơi (thông qua Enum `StateType` và `EnumMap`), quản lý trạng thái đang hoạt động và chuyển giao các lệnh cập nhật (`update`) cùng lệnh hiển thị (`render`) từ `GamePanel` tới trạng thái hiện hành.

---

## 2. Quy trình hoạt động
1. Khởi tạo một `EnumMap<StateType, GameState>` để lưu trữ ánh xạ giữa kiểu trạng thái và đối tượng trạng thái cụ thể.
2. Các trạng thái được đăng ký bằng phương thức `registerState(StateType, GameState)`.
3. Khi cần chuyển trạng thái (`setState(StateType)`):
   * Biến `currentState` được cập nhật.
   * Phương thức `init()` của trạng thái mới được gọi ngay lập tức để thiết lập lại dữ liệu màn hình (như vị trí con trỏ menu, khởi tạo lại nhân vật).
4. Trong mỗi frame:
   * Phương thức `update()` của GSM gọi `currentState.update(...)`.
   * Phương thức `render()` của GSM gọi `currentState.render(...)`.

---

## 3. API
* `public enum StateType { MENU, PLAYING, PAUSE, UPGRADE, GAME_OVER }`: Danh mục các trạng thái được hỗ trợ.
* `public void registerState(StateType type, GameState state)`: Đăng ký một trạng thái mới vào bộ quản lý.
* `public void setState(StateType type)`: Kích hoạt trạng thái được chỉ định và gọi `init()`.
* `public GameState getState(StateType type)`: Truy xuất đối tượng trạng thái theo kiểu.
* `public StateType getCurrentStateType()`: Lấy kiểu trạng thái hiện tại.
* `public void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH)`: Điều phối cập nhật logic.
* `public void render(Graphics2D g)`: Điều phối vẽ đồ họa.

---

## 4. Quan hệ với hệ thống khác
* Được chứa trong: `GamePanel`.
* Quản lý: Các lớp con của `GameState` (`MenuState`, `PlayingState`...).

---

## 5. Sửa đổi / Cập nhật
* **02/10/2026**: Khởi tạo ban đầu với EnumMap tối ưu hiệu năng.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
