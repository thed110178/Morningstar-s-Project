Cập nhật mới nhất vào ngày: 06/10/2026

# WeaponManager.java - Quản Lý Kho Vũ Khí & Giao Diện Đạn (Weapon HUD)

## 1. Mô tả
`WeaponManager.java` quản lý toàn bộ các khe trang bị vũ khí của người chơi (Slot 1, Slot 2, Slot 3, Slot 4), điều phối chuyển đổi vũ khí thông qua phím bấm số hoặc phím Q (Quick-swap), đồng thời vẽ bảng hiển thị số lượng đạn và thanh tiến trình nạp đạn (Ammunition HUD) tại góc dưới bên phải màn hình.

---

## 2. Quy trình hoạt động
1. Khởi tạo và đăng ký kho vũ khí chiến thuật:
   * Slot 1: `AssaultRifle` (Súng trường tấn công AR 30 viên, 700 RPM, giữ bắn liên thanh).
   * Slot 2: `Shotgun` (Súng săn 8 viên, chùm đạn tỏa 6 viên, sát thương cao tầm gần).
   * Slot 3: `Pistol` (Súng lục chiến thuật 7 viên).
   * Slot 4: `CombatKnife` (Dao găm cận chiến sát thương 45 HP).
   * Mặc định vũ khí cầm trên tay là Slot 1 (AR) và vũ khí phụ là Slot 2 (Shotgun).
2. Xử lý chuyển đổi:
   * `selectSlot(int slotIndex)`: Chuyển sang vũ khí tương ứng nếu có trong kho (hỗ trợ phím 1, 2, 3, 4).
   * `quickSwap()`: Đổi nhanh qua lại giữa vũ khí đang cầm và vũ khí trước đó khi bấm phím **Q**.
3. Xử lý tấn công & Nạp đạn:
   * Chuyển tiếp lệnh `attack` và `reload` tới vũ khí hiện hành.
4. Cập nhật & Vẽ giao diện:
   * Cập nhật thời gian hồi và nạp đạn cho tất cả vũ khí trong kho.
   * Phương thức `renderHUD(...)` vẽ khung giao diện hiển thị tên súng, slot, số đạn (ví dụ: `30 / 30`, `8 / 8`, `7 / 7` màu trắng, khi còn $\le 2$ viên chuyển màu đỏ cảnh báo, hoặc thanh màu cam chạy ngang khi đang `RELOADING...`). Đối với dao găm, hiển thị ký hiệu `∞ MELEE`.
   * Chú thích phím tắt: `[1] AR | [2] Shotgun | [3] Pistol | [4] Knife | [Q] Swap | [R] Reload`.

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
* Chứa: `Weapon`, `AssaultRifle`, `Shotgun`, `Pistol`, `CombatKnife`, `MeleeSlash`.
* Tương tác với: `ProjectileManager` và `PlayingState`.

---

## 5. Sửa đổi / Cập nhật
* **03/10/2026**: Khởi tạo ban đầu với hỗ trợ phím 3, 4, Q, R và giao diện HUD trực quan.
* **06/10/2026**: Mở rộng hỗ trợ toàn diện 4 slot: Bổ sung Slot 1 (Assault Rifle 30 viên, 700 RPM) và Slot 2 (Shotgun 8 viên cự ly gần). Mặc định kích hoạt Slot 1.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
