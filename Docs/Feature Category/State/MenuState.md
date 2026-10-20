Cập nhật mới nhất vào ngày: 20/10/2026

# MenuState.java - Màn Hình Menu Chính (Main Menu Interface)

## 1. Mô tả
`MenuState.java` kế thừa từ `GameState`, chịu trách nhiệm hiển thị giao diện bắt đầu của trò chơi với phong cách Cybernetic / Arcade. Màn hình cung cấp 5 lựa chọn:
1. **BOSS FIGHT: TITAN TANK**: Thử thách trực tiếp phòng Boss Titan Tank.
2. **START CAMPAIGN (STAGE 1)**: Bắt đầu chiến dịch Roguelite tiêu chuẩn từ tầng 1.
3. **CÀI ĐẶT PHÍM (SETTINGS)**: Tùy biến toàn bộ 16 phím điều khiển và nút chuột trước khi vào trận.
4. **HOW TO PLAY**: Xem hướng dẫn tác chiến và điều khiển.
5. **EXIT**: Thoát trò chơi.

Giao diện hỗ trợ điều khiển song song bằng cả bàn phím (W/S/Mũi tên + Enter/Space) và chuột (rê chuột hover + nhấp click trực tiếp).

---

## 2. Quy trình hoạt động
1. **Khởi tạo (`init`)**: Reset lựa chọn về mục đầu tiên (`selectedOption = 0`), ẩn bảng hướng dẫn (`showHowToPlay = false`) và ẩn cài đặt (`showSettings = false`).
2. **Cập nhật (`update`)**:
   * Nếu màn hình Cài đặt phím đang mở (`showSettings = true`):
     - Chuyển quyền điều khiển cho `settingsUI.update(keyH, mouseH, deltaTime)`.
     - Lắng nghe phím ESC hoặc click nút "HOÀN TẤT (QUAY LẠI)" để đóng màn hình cài đặt và trở về menu chính.
   * Nếu bảng hướng dẫn đang mở: Lắng nghe phím ESC, Enter hoặc nhấp chuột để đóng bảng.
   * Nếu ở menu chính:
     - Lắng nghe phím W/S hoặc Mũi tên Lên/Xuống để thay đổi mục chọn `selectedOption` (có quay vòng tròn).
     - Duyệt qua 5 hình chữ nhật `optionBounds[i]` để kiểm tra xem con trỏ chuột có đang hover qua hay không. Nếu có hover, cập nhật `selectedOption = i`. Nếu nhấp chuột trái hoặc nhấn Enter/Space, gọi `selectCurrentOption()`.
3. **Thực thi lựa chọn (`selectCurrentOption`)**:
   * Mục 0: Đặt spawn boss và gọi `gsm.setState(GameStateManager.StateType.PLAYING)`.
   * Mục 1: Đặt chiến dịch thường và gọi `gsm.setState(GameStateManager.StateType.PLAYING)`.
   * Mục 2: Bật cờ `showSettings = true`, cấu hình `settingsUI.setCurrentSubMenu(PauseMenuUI.SubMenu.SETTINGS)`.
   * Mục 3: Bật cờ `showHowToPlay = true`.
   * Mục 4: Gọi `System.exit(0)` để đóng ứng dụng.
4. **Hiển thị (`render`)**:
   * Nếu `showSettings = true`, kết xuất giao diện Cài đặt phím 2 cột của `settingsUI`.
   * Nếu `showHowToPlay = true`, vẽ hộp thoại bán trong suốt chứa danh sách hướng dẫn phím bấm chi tiết.
   * Nếu ở menu chính:
     - Vẽ nền tối với lưới cyberpunk đa giác (`28, 35, 54`).
     - Vẽ tiêu đề rực rỡ có bóng đổ phát sáng (Glow Shadow).
     - Vẽ 5 nút bấm menu: mục được chọn có khung bo tròn dạ quang và dấu mũi tên chỉ thị `> ... <`.
     - Hiển thị footer trợ giúp phím bấm.

---

## 3. API
* `public MenuState(GameStateManager gsm)`: Constructor nhận bộ quản lý state.
* `public void init()`: Khởi tạo lại trạng thái menu.
* `public void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH)`: Xử lý tương tác phím & chuột.
* `public void render(Graphics2D g)`: Vẽ giao diện menu, popup hướng dẫn hoặc màn hình cài đặt.
* `public boolean isShowSettings()`: Kiểm tra trạng thái hiển thị màn hình Cài đặt phím.
* `public void setShowSettings(boolean showSettings)`: Bật/tắt màn hình Cài đặt phím.
* `public PauseMenuUI getSettingsUI()`: Lấy đối tượng giao diện cài đặt phím.
* `public int getSelectedOption()`: Lấy chỉ mục mục đang được chọn.

---

## 4. Quan hệ với hệ thống khác
* Sử dụng `GameConfig` để lấy độ phân giải màn hình.
* Tương tác với `GameStateManager` để chuyển sang `PlayingState`.
* Nhận input từ `KeyboardHandler` và `MouseHandler`.
* Tích hợp trực tiếp với `PauseMenuUI` và `KeyBindings` để chỉnh sửa phím ngay từ màn hình chờ.

---

## 5. Sửa đổi / Cập nhật
* **02/10/2026**: Cập nhật lại tiêu đề thành `"Project DontKnow"` và phụ đề `"An unplanned top-down roguelite shooter"`.
* **20/10/2026**: Bổ sung lựa chọn thứ 3 `CÀI ĐẶT PHÍM (SETTINGS)` cho phép người chơi tùy biến 16 phím tác chiến ngay tại Menu chính trước khi bước vào trận.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
