Cập nhật mới nhất vào ngày: 03/10/2026

# Weapon.java - Lớp Cơ Sở Vũ Khí (Strategy Pattern)

## 1. Mô tả
`Weapon.java` là lớp trừu tượng (Abstract Base Class) định nghĩa toàn bộ các thuộc tính và hành vi chung của kho vũ khí trong trò chơi, áp dụng mẫu thiết kế **Strategy Pattern** và nguyên lý **Open/Closed Principle (SOLID)**. Mọi loại vũ khí (súng lục 7 viên, dao cận chiến, và các loại súng trường/shotgun sau này) đều kế thừa từ lớp này mà không làm ảnh hưởng tới logic điều khiển người chơi.

---

## 2. Quy trình hoạt động
1. Khi khởi tạo một vũ khí, các thông số cơ bản được nạp: `name`, `slotIndex` (phím 3 hoặc 4), `damage`, `fireRateCooldown`, `magazineSize`, và `reloadDuration`.
2. Phương thức trừu tượng `attack(...)` được gọi khi người chơi bấm chuột trái. Tùy loại vũ khí mà đạn bay (`Projectile`) hoặc nhát chém cận chiến (`MeleeSlash`) được sinh ra.
3. Phương thức `reload()` kích hoạt nạp lại băng đạn khi người chơi bấm phím R hoặc khi bắn hết đạn trong băng.
4. Phương thức `update(double deltaTime)` tự động đếm ngược thời gian hồi (cooldown) và tiến trình nạp đạn (reload timer).

---

## 3. API
* `public abstract boolean attack(double originX, double originY, double aimAngle, ProjectileManager pm, List<MeleeSlash> slashList)`: Thực hiện đòn tấn công.
* `public void reload()`: Nạp lại băng đạn.
* `public void update(double deltaTime)`: Cập nhật cooldown và tiến trình reload.
* `public boolean canAttack()`: Kiểm tra vũ khí đã sẵn sàng tấn công hay chưa.
* `public boolean isMelee()`: Kiểm tra vũ khí có phải là cận chiến hay không (`magazineSize == 0`).
* `public double getReloadProgress()`: Trả về tỉ lệ hoàn thành nạp đạn (0.0 đến 1.0) để vẽ thanh reload HUD.

---

## 4. Quan hệ với hệ thống khác
* Lớp cha của: `Pistol.java`, `CombatKnife.java`.
* Được quản lý tập trung bởi: `WeaponManager.java`.

---

## 5. Sửa đổi / Cập nhật
* **03/10/2026**: Khởi tạo ban đầu với Strategy Pattern cho vũ khí tầm xa và cận chiến.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
