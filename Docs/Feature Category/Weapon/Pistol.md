Cập nhật mới nhất vào ngày: 03/10/2026

# Pistol.java - Súng Lục Chiến Thuật 7 Viên (Slot 3)

## 1. Mô tả
`Pistol.java` kế thừa từ `Weapon`, là vũ khí tầm xa khởi đầu gắn với phím tắt số **3**. Súng có cơ số băng đạn đúng **7 viên**, độ chính xác cao và tốc độ bay của đạn nhanh (700 px/s).

---

## 2. Quy trình hoạt động
1. Người chơi bấm số 3 để trang bị hoặc số 4/phím Q để đổi vũ khí.
2. Khi bấm chuột trái:
   * Nếu đang trong quá trình nạp đạn (`isReloading`): Không cho phép bắn.
   * Nếu băng đạn còn $\ge 1$ viên: Tính toán tọa độ đầu nòng súng, sinh ra 1 viên `Projectile` bay theo góc chuột `aimAngle`, giảm 1 viên đạn (`currentAmmo--`).
   * Nếu viên đạn thứ 7 được bắn ra (hết đạn trong băng): Tự động kích hoạt chu trình nạp đạn kéo dài 1.2 giây (`reload()`).
3. Người chơi có thể chủ động bấm phím **R** để nạp đạn bất cứ lúc nào nếu đạn chưa đầy.

---

## 3. Cấu hình & Tham số
* Sát thương (`damage`): 25 HP.
* Cỡ băng đạn (`magazineSize`): 7 viên.
* Tốc độ bắn (`fireRateCooldown`): 0.22 giây giữa các phát bắn bán tự động.
* Thời gian nạp đạn (`reloadDuration`): 1.2 giây.
* Vận tốc đạn: 700 px/giây.

---

## 4. Quan hệ với hệ thống khác
* Sinh đạn vào: `ProjectileManager`.
* Được quản lý bởi: `WeaponManager`.

---

## 5. Sửa đổi / Cập nhật
* **03/10/2026**: Khởi tạo ban đầu với băng đạn 7 viên và tự động nạp đạn.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
