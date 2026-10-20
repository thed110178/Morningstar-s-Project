Cập nhật mới nhất vào ngày: 20/10/2026

# KeyBindings.java - Hệ Thống Tùy Chỉnh Phím Điều Khiển (Keybinding System)

## 1. Mô tả
`KeyBindings.java` là trung tâm quản lý cấu hình toàn bộ phím và nút chuột điều khiển trong trò chơi. Lớp này loại bỏ hoàn toàn các mã phím gán cứng (hardcoded), cho phép người chơi tự do thay đổi gán phím (Rebinding) cho 16 hành động tác chiến:
- **Tấn công / Bắn súng (`FIRE`)**: Mặc định Chuột Trái (`MOUSE_LEFT`).
- **Cận chiến nhanh (`QUICK_MELEE`)**: Mặc định Chuột Phải (`MOUSE_RIGHT`), chém dao tức thì bất kể đang cầm vũ khí nào.
- **Di chuyển 4 hướng**: Tiến lên (`W`), Lùi xuống (`S`), Sang trái (`A`), Sang phải (`D`).
- **Né đòn / Lướt (`DODGE`)**: Kích hoạt i-frames né sát thương (Mặc định: `SPACE`).
- **Tương tác / Nhặt đồ (`INTERACT`)**: Mở hòm chiến lợi phẩm, dùng hộp cứu thương, mua sắm quầy Shop, hiệu chỉnh bàn súng, bước lên trực thăng sơ tán (Mặc định: `F`).
- **Nạp đạn thủ công (`RELOAD`)**: Mặc định `R`.
- **Đổi vũ khí nhanh (`QUICK_SWAP`)**: Hoán đổi giữa 2 vũ khí gần nhất (Mặc định: `Q`).
- **Tạm dừng / Mở Menu (`PAUSE_MENU`)**: Mở menu tạm dừng trận chiến (Mặc định: `ESC`, có thể tùy chỉnh sang phím bất kỳ).
- **Mở Balo chiến thuật (`BACKPACK`)**: Mặc định `TAB` / `B`.
- **Chọn Slot Trang bị (Slot 1 - 4)**:
  * `SLOT_1` ("Vũ khí Slot 1" — Phím `1` / Numpad 1): Vị trí trang bị tự do do người chơi quyết định (không cố định loại súng).
  * `SLOT_2` ("Vũ khí Slot 2" — Phím `2` / Numpad 2): Vị trí trang bị tự do do người chơi quyết định.
  * `SLOT_3` ("Vũ khí Slot 3 (Súng lục)" — Phím `3` / Numpad 3).
  * `SLOT_4` ("Vũ khí Slot 4 (Dao găm)" — Phím `4` / Numpad 4).

---

## 2. Quy trình hoạt động
1. Sử dụng Enum `GameAction` định nghĩa toàn bộ 16 hành động tác chiến kèm tên hiển thị tiếng Việt, tiếng Anh và mã phím/nút chuột mặc định (`defaultKeyCode`).
2. Định nghĩa hằng số nút chuột ảo:
   * `MOUSE_LEFT = -101`: Chuột Trái.
   * `MOUSE_RIGHT = -102`: Chuột Phải.
   * `MOUSE_MIDDLE = -103`: Chuột Giữa.
3. Lưu trữ cấu hình gán phím hiện hành trong `EnumMap<GameAction, Integer>`.
4. Khi người chơi muốn đổi phím trong Menu Cài đặt:
   - Hệ thống kiểm tra trùng lặp phím qua phương thức `findConflict(action, newKeyCode)`.
   - Cập nhật mã phím mới qua `setKeyCode(action, keyCode)`.
   - Cho phép gán cả phím bàn phím (`keyH.getAnyKeyPressedOnce()`) và nút chuột (`mouseH.isRightClickedOnce()`, click chuột trái).
5. Cung cấp các phương thức truy vấn tiện ích:
   - `isActionDown(keyH, mouseH, action)`: Kiểm tra giữ phím/chuột liên tục.
   - `isActionPressedOnce(keyH, mouseH, action)`: Kiểm tra nhấn phím/chuột 1 lần duy nhất.
   - `getKeyName(keyCode)`: Chuyển đổi mã phím thành chuỗi hiển thị rõ nghĩa ("CHUỘT TRÁI", "CHUỘT PHẢI", "SPACE", "ESC", "TAB", "ENTER", v.v.).
   - `resetToDefaults()`: Khôi phục toàn bộ bảng phím về mặc định chuẩn chỉ với một lệnh.

---

## 3. API
* `public static void resetToDefaults()`: Đặt lại toàn bộ 16 hành động về phím mặc định ban đầu.
* `public static int getKeyCode(GameAction action)`: Lấy mã phím đang được gán cho hành động.
* `public static void setKeyCode(GameAction action, int keyCode)`: Gán mã phím/nút chuột mới cho hành động.
* `public static GameAction findConflict(GameAction targetAction, int newKeyCode)`: Kiểm tra xem phím mới có bị trùng với hành động nào khác không. Trả về `GameAction` bị trùng hoặc `null`.
* `public static boolean isActionDown(KeyboardHandler keyH, MouseHandler mouseH, GameAction action)`: Kiểm tra trạng thái đang giữ phím hoặc chuột.
* `public static boolean isActionPressedOnce(KeyboardHandler keyH, MouseHandler mouseH, GameAction action)`: Kiểm tra trạng thái nhấn phím hoặc chuột 1 lần.
* `public static String getKeyName(int keyCode)`: Lấy tên hiển thị tiếng Việt/Anh chuẩn hóa của phím hoặc nút chuột.

---

## 4. Quan hệ với hệ thống khác
* **`PlayingState`**: Điều phối bắn súng (`FIRE`), cận chiến nhanh (`QUICK_MELEE`), mở menu tạm dừng (`PAUSE_MENU`), di chuyển và đổi vũ khí thông qua `KeyBindings`.
* **`PauseMenuUI`**: Giao diện hiển thị danh sách 16 phím trong bố cục 2 cột, hỗ trợ bắt phím bàn phím và nút chuột, hiển thị thông báo xung đột phím và nút reset về mặc định.
* **`PlayingHUD`**: Tự động lấy tên phím gán hiện tại (`KeyBindings.getKeyName(...)`) để hiển thị gợi ý trên thanh né đòn và gợi ý trực thăng.

---

## 5. Sửa đổi / Cập nhật
* **20/10/2026**:
  - Khởi tạo ban đầu với 13 hành động tác chiến và Menu tạm dừng.
  - Mở rộng lên 16 hành động: bổ sung Tấn công / Bắn (`FIRE`), Cận chiến nhanh (`QUICK_MELEE`), và Tạm dừng / Menu (`PAUSE_MENU`).
  - Hỗ trợ gán phím nút chuột (`CHUỘT TRÁI`, `CHUỘT PHẢI`).
  - Bỏ gán cứng loại súng cho Slot 1 và Slot 2, trả toàn quyền quyết định cấu hình vũ khí cho người chơi.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
