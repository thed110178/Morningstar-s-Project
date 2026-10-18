Cập nhật mới nhất vào ngày: 18/10/2026

# AssaultRifle.java - Súng Trường Tấn Công (Slot 1)

## 1. Mô tả
`AssaultRifle.java` kế thừa từ `Weapon`, là vũ khí hỏa lực liên thanh tầm trung gắn với phím tắt số **1**. Súng sở hữu băng đạn tiêu chuẩn **30 viên**, tốc độ bắn cao xấp xỉ **700 viên/phút** (khoảng cách giữa 2 viên đạn liên tiếp $\approx 0.0857$ giây). Người chơi chỉ cần **giữ chuột trái** để tự động xả đạn liên tục (Full-Auto).

---

## 2. Quy trình hoạt động
1. **Lựa chọn vũ khí**: Người chơi nhấn phím **1** (hoặc Numpad 1) để trang bị, hoặc dùng phím **Q** để Quick-Swap.
2. **Cơ chế giữ để xả đạn (Full-Auto)**:
   * Khi giữ chuột trái (`mouseH.isLeftPressed()`): Hệ thống liên tục kiểm tra trạng thái hồi súng (`cooldownTimer <= 0`).
   * Mỗi khi cooldown kết thúc, súng tự động phóng ra 1 viên đạn `Projectile` và trừ 1 viên đạn khỏi băng (`currentAmmo--`).
3. **Cơ chế Độ Chính Xác Động (First-Shot Accuracy & Full-Auto Spray Bloom)**:
   * **Phát bắn chuẩn xác đầu tiên**: $1 - 2$ viên đạn đầu tiên trong mỗi loạt bắn sở hữu độ chụm cực cao (`initialSpreadAngle = currentSpreadAngle * 0.25`), giúp bắn tỉa chính xác từng phát (Tap-firing) ở cự ly xa.
   * **Độ lệch tăng dần khi sấy liên thanh**: Từ viên thứ 3 đến viên thứ 10, độ lệch mở rộng dần đều lên ngưỡng tối đa (`maxContinuousSpreadAngle = currentSpreadAngle * 3.5`).
   * **Hồi phục độ chính xác (Recoil Recovery)**: Khi người chơi ngừng bóp cò quá $0.13$ giây, số phát liên thanh hồi phục nhanh chóng về $0$ trong vòng $\sim 0.25$ giây, đưa loạt bắn tiếp theo trở lại độ chuẩn xác viên đầu.
4. **Chu trình nạp đạn**:
   * Khi viên đạn thứ 30 được bắn ra và băng đạn về 0, súng tự động kích hoạt nạp đạn trong 1.8 giây (`reload()`), đồng thời reset bộ đếm liên thanh về 0.
   * Người chơi có thể chủ động nhấn phím **R** bất kỳ lúc nào để nạp lại băng mới.

---

## 3. Cấu hình & Tham số
* **Tên hiển thị**: `Assault Rifle (AR)`
* **Ô trang bị (`slotIndex`)**: `1`
* **Sát thương mỗi viên (`damage`)**: 16 HP (tăng theo sao Damage)
* **Tốc độ bắn (`fireRateCooldown`)**: $60.0 / 700.0 \approx 0.0857$ giây/viên (tương đương 700 RPM)
* **Dung lượng băng đạn (`magazineSize`)**: 30 viên
* **Thời gian nạp đạn (`reloadDuration`)**: 1.8 giây
* **Vận tốc đạn (`BULLET_SPEED`)**: 820 px/giây (vận tốc đầu nòng lớn hơn súng lục)
* **Thời gian sống của đạn (`BULLET_LIFETIME`)**: 1.2 giây (tầm bay tối đa ~984 px)
* **Góc lệch cơ sở (`baseSpreadAngle`)**: $\pm 0.04$ radian

### Tương quan độ lệch đạn theo Sao Chính Xác (`accuracyStars`):
| Cấp Sao Accuracy | Độ Lệch Viên Đầu (`initialSpread`) | Độ Lệch Tối Đa Khi Sấy (`maxSpread`) | Đặc Điểm Chiến Thuật |
|---|---|---|---|
| **1★** | $\pm 0.0100\text{ rad}$ ($\approx \pm 0.57^\circ$) | $\pm 0.1400\text{ rad}$ ($\approx \pm 8.02^\circ$) | Viên đầu chuẩn xác, sấy cả băng tản rộng rõ rệt |
| **2★** | $\pm 0.0065\text{ rad}$ ($\approx \pm 0.37^\circ$) | $\pm 0.0910\text{ rad}$ ($\approx \pm 5.21^\circ$) | Viên đầu gom hơn, sấy liên thanh chụm hơn |
| **3★** | $\pm 0.0042\text{ rad}$ ($\approx \pm 0.24^\circ$) | $\pm 0.0591\text{ rad}$ ($\approx \pm 3.39^\circ$) | Viên đầu cực kỳ chuẩn xác, sấy tầm xa đầm chắc |
| **4★** | $\pm 0.0027\text{ rad}$ ($\approx \pm 0.16^\circ$) | $\pm 0.0384\text{ rad}$ ($\approx \pm 2.20^\circ$) | Độ chính xác cấp độ thiện xạ |

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
* **18/10/2026**: Triển khai cơ chế độ chính xác động cho AR:
  - Viên đầu tiên và viên thứ hai cực kỳ chuẩn xác (First-shot accuracy).
  - Độ tản đạn tăng dần đều khi duy trì sấy liên thanh cả băng (Spray bloom).
  - Tự động hồi phục độ chính xác khi nhả cò/ngừng bắn trong $\sim 0.25$ giây.
  - Tỉ lệ nghịch với số sao Accuracy: Sao càng cao thì viên đầu càng chụm và độ lệch tối đa khi sấy càng ít, và ngược lại.
  - Bổ sung 2 unit test trong `WeaponSystemTest` đỗ $100\%$.

---

## 7. Lỗi đã sửa
Chưa ghi nhận lỗi.
