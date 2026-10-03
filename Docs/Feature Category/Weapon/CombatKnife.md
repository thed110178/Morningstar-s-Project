Cập nhật mới nhất vào ngày: 03/10/2026

# CombatKnife.java - Dao Găm Tác Chiến Cận Chiến (Slot 4)

## 1. Mô tả
`CombatKnife.java` kế thừa từ `Weapon`, là vũ khí cận chiến khởi đầu gắn với phím tắt số **4**. Dao găm không tiêu hao đạn, không cần nạp đạn, gây lượng sát thương cao (45 HP) và tạo ra một vùng Hitbox hình vuông kích thước xấp xỉ 1 ô gạch (44 px) phía trước mặt nhân vật theo hướng con trỏ chuột.

---

## 2. Quy trình hoạt động
1. Người chơi bấm số 4 để trang bị dao găm.
2. Khi bấm chuột trái:
   * Nếu hết thời gian hồi chiêu chém (0.32s):
     * Tạo một đối tượng `MeleeSlash` đặt tâm cách người chơi 34 px về phía góc ngắm chuột.
     * Kích thước hộp bao sát thương là $44 \times 44$ px (xấp xỉ 1 ô tile $48 \times 48$).
     * Hiển thị vệt chém màu bạc - vàng ánh kim trong 0.12 giây.
     * Đặt lại cooldown 0.32 giây.

---

## 3. Cấu hình & Tham số
* Sát thương (`damage`): 45 HP.
* Băng đạn (`magazineSize`): 0 (vô hạn, không tốn đạn).
* Thời gian hồi vung dao (`fireRateCooldown`): 0.32 giây.
* Khoảng với (`reachOffset`): 34 px phía trước nhân vật.
* Kích thước Hitbox: 44 px.

---

## 4. Quan hệ với hệ thống khác
* Tạo đối tượng: `MeleeSlash`.
* Được quản lý bởi: `WeaponManager`.

---

## 5. Sửa đổi / Cập nhật
* **03/10/2026**: Khởi tạo ban đầu với hitbox cận chiến 1 ô và vệt chém thời gian thực.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
