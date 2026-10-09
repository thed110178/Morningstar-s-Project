Cập nhật mới nhất vào ngày: 06/10/2026

# Shotgun.java - Súng Săn Chùm Đạn Tỏa (Slot 2)

## 1. Mô tả
`Shotgun.java` kế thừa từ `Weapon`, là vũ khí hủy diệt tầm gần (Close-Quarters Breaching) gắn với phím tắt số **2**. Súng có băng đạn **8 viên**, mỗi lần bóp cò sẽ phóng ra **1 chùm 6 viên đạn chì (pellets)** tỏa rộng theo hình nón. Súng có sát thương bộc phá cực lớn ở cự ly gần nhưng các viên đạn sẽ **tự động tiêu biến sau một quãng ngắn** (~204 px) để phản ánh chân thực đặc tính shotgun và tránh việc bắn tỉa từ xa.

---

## 2. Quy trình hoạt động
1. **Lựa chọn vũ khí**: Người chơi nhấn phím **2** (hoặc Numpad 2) để trang bị, hoặc phím **Q** để Quick-Swap.
2. **Cơ chế bắn chùm tỏa (Pellet Spread)**:
   * Khi nhấn hoặc giữ chuột trái: Kiểm tra trạng thái nạp đạn và thời gian hồi (0.75s).
   * Tại thời điểm phát bắn kích hoạt, 1 viên đạn trong băng bị trừ (`currentAmmo--`).
   * Hệ thống sinh ra đồng thời **6 viên đạn chì** phân bố đều trên góc hình nón $\pm 0.22$ radian ($\approx \pm 12.6^\circ$) quanh trục ngắm chuột, kèm độ rung nhẹ ngẫu nhiên.
3. **Cơ chế tiêu biến ở tầm xa (Limited Range Dissipation)**:
   * Mỗi viên đạn chì có thời gian sống giới hạn là `PELLET_LIFETIME = 0.32` giây với vận tốc `640.0` px/s.
   * Quãng đường bay tối đa: $640.0 \times 0.32 = 204.8$ pixel (tương đương khoảng 4.2 ô gạch dungeon).
   * Vượt quá thời gian này, các viên đạn tự tiêu biến trong không khí, không gây sát thương lên mục tiêu ở quá xa.
4. **Sát thương bộc phá cự ly gần**:
   * Mỗi viên đạn chì trúng đích gây 14 HP.
   * Nếu người chơi áp sát mục tiêu và bắn trúng trọn 6 viên: Tổng sát thương dồn đạt $14 \times 6 = 84$ HP (đủ hạ gục ngay lập tức bù nhìn thử nghiệm 75 HP).
5. **Nạp đạn**:
   * Khi bắn hết 8 viên, súng tự động nạp đạn trong 2.2 giây. Có thể bấm phím **R** để nạp thủ công.

---

## 3. Cấu hình & Tham số
* **Tên hiển thị**: `Shotgun`
* **Ô trang bị (`slotIndex`)**: `2`
* **Sát thương mỗi pellet (`damage`)**: 14 HP (tối đa 84 HP nếu trúng toàn bộ chùm)
* **Số đạn chì mỗi phát (`PELLETS_PER_SHOT`)**: 6 viên
* **Góc mở chùm đạn (`SPREAD_CONE_ANGLE`)**: $\pm 0.22$ radian ($\approx \pm 12.6^\circ$)
* **Tốc độ bắn (`fireRateCooldown`)**: 0.75 giây giữa các phát bắn
* **Dung lượng băng đạn (`magazineSize`)**: 8 viên (shells)
* **Thời gian nạp đạn (`reloadDuration`)**: 2.2 giây
* **Vận tốc đạn (`PELLET_SPEED`)**: 640 px/giây
* **Thời gian sống của đạn (`PELLET_LIFETIME`)**: 0.32 giây (tầm bay tối đa ~204.8 px)

---

## 4. Đồ họa & Hiển thị
* **Mô hình súng**: Nòng súng đôi cỡ lớn bằng thép xám (24px, nét đậm 5.0), ốp lót tay bơm bằng gỗ mộc màu nâu cổ điển, điểm nhấn đầu nòng màu cam bộc phá.
* **Màu đạn**: Đạn chì màu cam đỏ rực cháy mô phỏng luồng hỏa lực nổ áp đảo.

---

## 5. Quan hệ với hệ thống khác
* **`WeaponManager`**: Quản lý slot 2, hiển thị số đạn 8/8 và thời gian hồi/nạp trên thanh HUD.
* **`ProjectileManager`**: Tiếp nhận đồng thời 6 thực thể `Projectile`, tự động loại bỏ chúng sau 0.32s hoặc khi va chạm tường/kẻ địch.
* **`PlayingState`**: Nhận phím `2` / `Numpad 2` để chuyển đổi sang Shotgun.

---

## 6. Sửa đổi / Cập nhật
* **06/10/2026**: Khởi tạo vũ khí Shotgun slot 2 theo yêu cầu: 8 viên, chùm đạn tỏa 6 viên, sát thương cao tầm gần, tự biến mất ở tầm xa.

---

## 7. Lỗi đã sửa
Chưa ghi nhận lỗi.
