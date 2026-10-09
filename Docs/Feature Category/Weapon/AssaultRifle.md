Cập nhật mới nhất vào ngày: 06/10/2026

# AssaultRifle.java - Súng Trường Tấn Công (Slot 1)

## 1. Mô tả
`AssaultRifle.java` kế thừa từ `Weapon`, là vũ khí hỏa lực liên thanh tầm trung gắn với phím tắt số **1**. Súng sở hữu băng đạn tiêu chuẩn **30 viên**, tốc độ bắn cao xấp xỉ **700 viên/phút** (khoảng cách giữa 2 viên đạn liên tiếp $\approx 0.0857$ giây). Người chơi chỉ cần **giữ chuột trái** để tự động xả đạn liên tục (Full-Auto).

---

## 2. Quy trình hoạt động
1. **Lựa chọn vũ khí**: Người chơi nhấn phím **1** (hoặc Numpad 1) để trang bị, hoặc dùng phím **Q** để Quick-Swap.
2. **Cơ chế giữ để xả đạn (Full-Auto)**:
   * Khi giữ chuột trái (`mouseH.isLeftPressed()`): Hệ thống liên tục kiểm tra trạng thái hồi súng (`cooldownTimer <= 0`).
   * Mỗi khi cooldown kết thúc, súng tự động phóng ra 1 viên đạn `Projectile` và trừ 1 viên đạn khỏi băng (`currentAmmo--`).
   * **Độ giật/tản xạ thực tế**: Mỗi viên đạn khi phóng có góc bắn dao động ngẫu nhiên một khoảng nhỏ $\pm 0.04$ radian ($\approx \pm 2.3^\circ$), tạo cảm giác đầm tay và phân tán đạn tự nhiên khi sấy cả băng.
3. **Chu trình nạp đạn**:
   * Khi viên đạn thứ 30 được bắn ra và băng đạn về 0, súng tự động kích hoạt nạp đạn trong 1.8 giây (`reload()`).
   * Người chơi có thể chủ động nhấn phím **R** bất kỳ lúc nào để nạp lại băng mới.

---

## 3. Cấu hình & Tham số
* **Tên hiển thị**: `Assault Rifle (AR)`
* **Ô trang bị (`slotIndex`)**: `1`
* **Sát thương mỗi viên (`damage`)**: 16 HP
* **Tốc độ bắn (`fireRateCooldown`)**: $60.0 / 700.0 \approx 0.0857$ giây/viên (tương đương 700 RPM)
* **Dung lượng băng đạn (`magazineSize`)**: 30 viên
* **Thời gian nạp đạn (`reloadDuration`)**: 1.8 giây
* **Vận tốc đạn (`BULLET_SPEED`)**: 820 px/giây (vận tốc đầu nòng lớn hơn súng lục)
* **Thời gian sống của đạn (`BULLET_LIFETIME`)**: 1.2 giây (tầm bay tối đa ~984 px)
* **Độ lệch ngẫu nhiên (`SPREAD_ANGLE`)**: $\pm 0.04$ radian

---

## 4. Đồ họa & Hiển thị
* **Mô hình súng**: Thân súng nòng dài màu xám đen quân sự (28px), thước ngắm điểm vàng, ốp hộp tiếp đạn AR đặc trưng.
* **Màu đạn**: Đạn vàng kim sáng bay nhanh theo hướng tâm ngắm 360 độ của con trỏ chuột.

---

## 5. Quan hệ với hệ thống khác
* **`WeaponManager`**: Quản lý chọn trang bị tại slot 1, chuyển đổi vũ khí linh hoạt, cập nhật thanh HUD hiển thị 30/30 và tiến trình nạp đạn.
* **`ProjectileManager`**: Tiếp nhận và quản lý các viên đạn AR đang bay, kiểm tra va chạm tường/cửa/kẻ địch.
* **`PlayingState`**: Nhận tín hiệu phím `1` / `Numpad 1` để chuyển đổi sang AR.

---

## 6. Sửa đổi / Cập nhật
* **06/10/2026**: Khởi tạo vũ khí súng trường tấn công AR slot 1 theo yêu cầu: 30 viên, 700 RPM, giữ bắn tự động.

---

## 7. Lỗi đã sửa
Chưa ghi nhận lỗi.
