Cập nhật mới nhất vào ngày: 02/10/2026

# MenuState.java - Màn Hình Menu Chính (Main Menu Interface)

## 1. Mô tả
`MenuState.java` kế thừa từ `GameState`, chịu trách nhiệm hiển thị giao diện bắt đầu của trò chơi với phong cách Cybernetic / Arcade. Màn hình cung cấp các lựa chọn: **Bắt đầu nhiệm vụ (START MISSION)**, **Xem hướng dẫn thao tác (HOW TO PLAY)**, và **Thoát game (EXIT)**. Giao diện hỗ trợ điều khiển song song bằng cả bàn phím (W/S/Mũi tên + Enter/Space) và chuột (rê chuột hover + nhấp click trực tiếp).

---

## 2. Quy trình hoạt động
1. **Khởi tạo (`init`)**: Reset lựa chọn về mục đầu tiên (`selectedOption = 0`) và ẩn bảng hướng dẫn (`showHowToPlay = false`).
2. **Cập nhật (`update`)**:
   * Nếu bảng hướng dẫn đang mở: Lắng nghe phím ESC, Enter hoặc nhấp chuột để đóng bảng.
   * Nếu ở menu chính:
     * Lắng nghe phím W/S hoặc Mũi tên Lên/Xuống để thay đổi mục chọn `selectedOption` (có quay vòng tròn).
     * Duyệt qua các hình chữ nhật `optionBounds[i]` để kiểm tra xem con trỏ chuột có đang hover qua hay không. Nếu có hover, cập nhật `selectedOption = i`. Nếu nhấp chuột trái hoặc nhấn Enter/Space, gọi `selectCurrentOption()`.
3. **Thực thi lựa chọn (`selectCurrentOption`)**:
   * Mục 0: Gọi `gsm.setState(GameStateManager.StateType.PLAYING)` để vào game.
   * Mục 1: Bật cờ `showHowToPlay = true`.
   * Mục 2: Gọi `System.exit(0)` để đóng ứng dụng.
4. **Hiển thị (`render`)**:
   * Vẽ nền tối với lưới cyberpunk đa giác (`28, 35, 54`).
   * Vẽ tiêu đề rực rỡ có bóng đổ phát sáng (Glow Shadow).
   * Vẽ các nút bấm menu: mục được chọn có khung bo tròn dạ quang và dấu mũi tên chỉ thị `> ... <`.
   * Nếu `showHowToPlay = true`, vẽ hộp thoại bán trong suốt chứa danh sách hướng dẫn phím bấm chi tiết.

---

## 3. API
* `public MenuState(GameStateManager gsm)`: Constructor nhận bộ quản lý state.
* `public void init()`: Khởi tạo lại trạng thái menu.
* `public void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH)`: Xử lý tương tác phím & chuột.
* `public void render(Graphics2D g)`: Vẽ giao diện menu và popup hướng dẫn.

---

## 4. Quan hệ với hệ thống khác
* Sử dụng `GameConfig` để lấy độ phân giải màn hình.
* Tương tác với `GameStateManager` để chuyển sang `PlayingState`.
* Nhận input từ `KeyboardHandler` và `MouseHandler`.

---

## 5. Sửa đổi / Cập nhật
* **02/10/2026**: Cập nhật lại tiêu đề thành `"Project DontKnow"` và phụ đề `"An unplanned top-down roguelite shooter"`.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
