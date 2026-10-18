Cập nhật mới nhất vào ngày: 09/10/2026

# Balo Chiến Thuật (Tactical Backpack & Slot Swapping)

## 1. Mô tả
Hệ thống Balo chiến thuật (`BackpackUI`) cho phép người chơi bấm phím **[Tab]** bất kỳ lúc nào trong quá trình chiến đấu để mở giao diện quản lý trang bị.
Giao diện được thiết kế theo phong cách Cyberpunk Tactical HUD mờ trong suốt (Glassmorphism), hiển thị đầy đủ thông số kỹ thuật của từng vũ khí trong kho.
Tính năng cốt lõi đặc biệt: **Hoán đổi vị trí giữa 2 Vũ khí chính ở Slot [1] và Slot [2]** linh hoạt thông qua click chuột hoặc phím tắt **[SPACE] / [S]**.

---

## 2. Quy trình hoạt động
```
Người chơi đang trong trận đấu
       │
       ▼ [Bấm phím TAB]
Mở giao diện Balo (Tạm dừng di chuyển & bắn súng)
       │
       ├──> Hiển thị Slot [1]: Tên, Hình vẽ, Sát thương, Băng đạn, Tốc bắn, Chế độ
       ├──> Hiển thị Slot [2]: Tên, Hình vẽ, Sát thương, Băng đạn, Tốc bắn, Chế độ
       ├──> Hiển thị Slot [3]: Súng lục phụ (Pistol)
       └──> Hiển thị Slot [4]: Dao cận chiến (Combat Knife)
       │
       ├───> [Click nút ⇄ ĐỔI VỊ TRÍ hoặc bấm SPACE / S]:
       │       Hoán đổi tức thì vũ khí giữa Slot [1] ⇄ Slot [2]
       │       (Cập nhật lại phím nóng [1] và [2] trong game tương ứng)
       │
       ├───> [Click vào ô hoặc bấm phím 1, 2, 3, 4]:
       │       Kích hoạt trang bị vũ khí đó lên tay người chơi
       │
       ▼ [Bấm phím TAB / ESC hoặc Click nút X]
Đóng giao diện Balo, tiếp tục trận chiến!
```

---

## 3. API & Các phương thức chính

### 3.1 `WeaponManager.java`
* `public boolean swapPrimarySlots()`: Hoán đổi vị trí lưu trữ giữa Slot 1 và Slot 2 trong bảng `weaponSlots`. Đồng thời cập nhật `setSlotIndex(2)` cho vũ khí cũ ở ô 1 và `setSlotIndex(1)` cho vũ khí cũ ở ô 2.
* `public Map<Integer, Weapon> getWeaponSlots()`: Trả về bảng ánh xạ bất biến chứa các vũ khí người chơi đang sở hữu.

### 3.2 `BackpackUI.java`
* `public boolean update(KeyboardHandler keyH, MouseHandler mouseH, WeaponManager weaponManager, double deltaTime)`:
  - Bắt phím `SPACE` / `S` hoặc click chuột vào nút trung tâm để kích hoạt `swapPrimarySlots()`.
  - Bắt click chọn ô để trang bị nhanh súng lên tay (`selectSlot`).
  - Trả về `true` khi người chơi yêu cầu đóng Balo (`TAB`, `ESC`, hoặc click nút `[X]`).
* `public void render(Graphics2D g, WeaponManager weaponManager, int screenWidth, int screenHeight)`:
  - Phủ mờ nền đen vũ trụ (`Color(5, 10, 20, 215)`).
  - Vẽ khung viền đôi dạ quang phong cách Cyberpunk (740x490px).
  - Vẽ các thẻ vũ khí chi tiết (Thẻ Slot 1, Thẻ Slot 2, Nút Đổi vị trí trung tâm, Thẻ Súng lục và Thẻ Dao cận chiến).

---

## 4. Inspector / Tinh chỉnh phím tắt
| Thao tác | Phím tắt / Chuột | Chức năng |
|---|---|---|
| Mở / Đóng Balo | `[TAB]` | Bật hoặc tắt giao diện Balo bất kỳ lúc nào |
| Đóng Balo | `[ESC]` hoặc Click `[X]` | Thoát khỏi giao diện Balo |
| Đổi vị trí Slot 1 & 2 | `[SPACE]` hoặc `[S]` hoặc Click nút `[⇄]` | Hoán đổi vị trí vũ khí chính giữa ô [1] và [2] |
| Trang bị nhanh | `[1]`, `[2]`, `[3]`, `[4]` hoặc Click ô | Cầm vũ khí tương ứng lên tay |

---

## 5. Quan hệ với hệ thống khác
* **`PlayingState`**: Quản lý cờ `isBackpackOpen`. Khi `isBackpackOpen = true`, tạm dừng vòng lặp chiến đấu của người chơi và chuyển quyền xử lý nhập liệu cho `BackpackUI`.
* **`WeaponManager`**: Nơi lưu trữ thực tế dữ liệu của các slot và thực hiện hoán đổi cấu trúc bộ nhớ.
* **`Weapon`**: Cung cấp `setSlotIndex(int)` để đồng bộ chỉ số phím nóng khi bị di chuyển giữa slot 1 và slot 2.

---

## 6. Sửa đổi / Cập nhật
* **16/10/2026**:
  - **Sửa lỗi không mở được Balo bằng phím [Tab]**: Loại bỏ việc kiểm tra phím TAB trùng lặp bên trong `BackpackUI.update()`, trao quyền kiểm soát toggle mở/đóng duy nhất cho `PlayingState`. Ngăn ngừa triệt để hiện tượng phím TAB vừa kích hoạt mở vừa bị hiểu là lệnh đóng ngay trong cùng một frame logic.
  - Bổ sung hiển thị thông tin cấp độ súng (Level) và số lần nâng cấp `[NÂNG: X/3]` cùng 3 hàng sao chỉ số trực quan ngay trên từng thẻ súng trong Balo.
