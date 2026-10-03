Cập nhật mới nhất vào ngày: 03/10/2026

# WeaponManager.java - Quản Lý Kho Vũ Khí & Giao Diện Đạn (Weapon HUD)

## 1. Mô tả
`WeaponManager.java` quản lý toàn bộ các khe trang bị vũ khí của người chơi (Slot 3, Slot 4), điều phối chuyển đổi vũ khí thông qua phím bấm số hoặc phím Q (Quick-swap), đồng thời vẽ bảng hiển thị số lượng đạn và thanh tiến trình nạp đạn (Ammunition HUD) tại góc dưới bên phải màn hình.

---

## 2. Quy trình hoạt động
1. Khởi tạo và đăng ký sẵn:
   * Slot 3: `Pistol` (Súng lục 7 viên).
   * Slot 4: `CombatKnife` (Dao găm).
   * Mặc định vũ khí cầm trên tay là Súng lục.
2. Xử lý chuyển đổi:
   * `selectSlot(int slotIndex)`: Chuyển sang vũ khí tương ứng nếu có trong kho.
   * `quickSwap()`: Đổi nhanh qua lại giữa vũ khí đang cầm và vũ khí trước đó khi bấm phím **Q**.
3. Xử lý tấn công & Nạp đạn:
   * Chuyển tiếp lệnh `attack` và `reload` tới vũ khí hiện hành.
4. Cập nhật & Vẽ giao diện:
   * Cập nhật thời gian hồi và nạp đạn cho tất cả vũ khí trong kho.
   * Phương thức `renderHUD(...)` vẽ khung giao diện hiển thị tên súng, slot, số đạn (ví dụ: `7 / 7` màu trắng, khi còn $\le 2$ viên chuyển màu đỏ cảnh báo, hoặc thanh màu cam chạy ngang khi đang `RELOADING...`). Đối với dao găm, hiển thị ký hiệu `∞ MELEE`.

---

## 3. API
* `public void addWeapon(Weapon weapon)`: Bổ sung một vũ khí mới vào kho đồ.
* `public void selectSlot(int slotIndex)`: Chọn trang bị theo phím slot.
* `public void quickSwap()`: Đổi nhanh 2 vũ khí gần nhất (phím Q).
* `public boolean attack(double ox, double oy, double angle, ProjectileManager pm)`: Bắn hoặc chém.
* `public void reload()`: Nạp lại băng đạn.
* `public void renderHUD(Graphics2D g, int screenW, int screenH)`: Vẽ thanh đạn và trạng thái vũ khí.

---

## 4. Quan hệ với hệ thống khác
* Chứa: `Weapon`, `Pistol`, `CombatKnife`, `MeleeSlash`.
* Tương tác với: `ProjectileManager` và `PlayingState`.

---

## 5. Sửa đổi / Cập nhật
* **03/10/2026**: Khởi tạo ban đầu với hỗ trợ phím 3, 4, Q, R và giao diện HUD trực quan.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
