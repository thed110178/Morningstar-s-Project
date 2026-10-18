Cập nhật mới nhất vào ngày: 16/10/2026

# SMG.java - Súng Tiểu Liên Tốc Độ Bắn Cao & Độ Lệch Đạn Khi Sấy Lớn

## 1. Mô tả
`SMG.java` kế thừa từ `Weapon`, là vũ khí chính (Primary Weapon) thuộc nhóm súng tiểu liên chiến thuật (Submachine Gun).
Súng được thiết kế đặc thù cho lối chơi áp sát linh hoạt, cơ động cao với các đặc tính nổi bật:
* **Sát thương đơn thấp**: Mỗi viên đạn chỉ gây **11 HP** (thấp nhất trong các loại súng, so với AR 16 HP, Pistol 25 HP).
* **Tốc độ bắn cực cao để bù DPS**: Đạt **~920 viên/phút (RPM)** (khoảng 0.0652s giữa mỗi phát bắn, ~15.3 viên/giây). Tổng lượng sát thương mỗi giây (DPS) lý thuyết đạt tới **~168.6 DPS**, xả hết cả băng 35 viên chỉ trong 2.28 giây để tạo áp lực hỏa lực dày đặc.
* **Độ lệch đạn khi sấy cao (High Spray Spread)**: Khi người chơi giữ chặt chuột trái để sấy liên thanh (Full-Auto), đạn sẽ chịu độ tản mác ngẫu nhiên lên tới **$\pm 0.18$ radian** ($\approx \pm 10.3^\circ$), gấp hơn 4.5 lần súng trường AR ($\pm 0.04$ rad). Độ lệch này đòi hỏi người chơi phải chủ động áp sát kẻ địch để tối ưu hóa lượng đạn trúng đích hoặc sấy quét diện rộng để áp chế đám đông quái.
* **Băng đạn lớn & Nạp nhanh**: Băng đạn chứa **35 viên** đạn tiểu liên và thời gian nạp đạn nhanh vượt trội (**1.4 giây**) giúp người chơi duy trì nhịp độ chiến đấu liên tục.

---

## 2. Quy trình hoạt động
1. **Thu thập vũ khí**:
   * Khi dọn sạch phòng chiến đấu, người chơi tiếp cận hòm tiếp tế (`WeaponChest`). Hòm tự động mở và có xác suất 1/3 rơi ra SMG (bên cạnh AR và Shotgun).
   * Bấm phím **[F]** để nhặt SMG vào Slot 1 (hoặc Slot 2 nếu Slot 1 đã có vũ khí).
2. **Cơ chế sấy đạn liên thanh (Full-Auto Spray)**:
   * Người chơi chỉ cần giữ phím chuột trái (`isAutomatic() == true`), khẩu súng sẽ liên tục nhả đạn với chu kỳ hồi chiêu 0.0652s.
   * Mỗi viên đạn được bắn ra từ đầu nòng súng với góc lệch ngẫu nhiên:
     $$\text{firedAngle} = \text{aimAngle} + \text{random}(-\text{SPREAD\_ANGLE}, +\text{SPREAD\_ANGLE})$$
   * Đạn bay với tốc độ 760 px/s và tồn tại trong 0.85 giây (tầm bắn hữu hiệu khoảng ~646 px), phù hợp hoàn hảo với các phòng chiến đấu hầm ngục.
3. **Cơ chế nạp đạn nhanh**:
   * Khi xả hết 35 viên, súng tự động kích hoạt nạp đạn trong 1.4 giây. Người chơi cũng có thể chủ động bấm **[R]** để nạp đạn bất cứ lúc nào.
4. **Quản lý trong Balo chiến thuật (Tab)**:
   * Trong giao diện Balo (`BackpackUI`), SMG hiển thị thẻ thông số chi tiết: Sát thương 11 HP, Băng đạn X/35, Tốc bắn ~920 RPM, Chế độ Tự động độ lệch cao.
   * Người chơi có thể dùng nút `[⇄ ĐỔI VỊ TRÍ]` hoặc phím **[SPACE]** để hoán đổi SMG giữa Slot 1 và Slot 2.

---

## 3. Cấu hình & Tham số
* **Tên hiển thị**: `Submachine Gun (SMG)`
* **Vị trí phím nóng (`slotIndex`)**: `1` hoặc `2` (Vũ khí chính)
* **Sát thương mỗi viên (`DEFAULT_DAMAGE`)**: `11` HP
* **Tốc độ bắn (`FIRE_RATE`)**: `60.0 / 920.0` ($\approx 0.0652$ giây/viên, ~920 RPM)
* **Dung lượng băng đạn (`MAG_CAPACITY`)**: `35` viên
* **Thời gian nạp đạn (`RELOAD_TIME`)**: `1.4` giây
* **Vận tốc đạn bay (`BULLET_SPEED`)**: `760.0` pixel/giây
* **Thời gian tồn tại của đạn (`BULLET_LIFETIME`)**: `0.85` giây (~646 px tầm bay tối đa)
* **Độ lệch đạn khi sấy (`SPREAD_ANGLE`)**: `0.18` radian ($\approx \pm 10.3^\circ$)
* **Chế độ bắn (`isAutomatic`)**: `true` (Full-Auto)

---

## 4. Đồ họa & Hiển thị
* **Mô hình trên Operative**:
  - Thân súng tiểu liên nhỏ gọn, cơ động màu xám xanh titan (`new Color(40, 50, 62)`).
  - Hộp tiếp đạn thẳng dài chúc xuống dưới màu đen nhám (`new Color(20, 26, 32)`).
  - Đầu nòng flash hider dạ quang màu xanh cyan neon (`new Color(0, 230, 255)`).
* **Mô hình trong Hòm tiếp tế (`WeaponChest`)**:
  - Mô hình SMG thu nhỏ nổi lơ lửng trên miệng hòm với nòng cyan neon và nhãn vàng `★ SUBMACHINE GUN (SMG) ★`.
* **Icon trong Balo (`BackpackUI`)**:
  - Thẻ trang bị với biểu tượng súng tiểu liên và bảng thông số so sánh trực quan.

---

## 5. Quan hệ với hệ thống khác
* **`Weapon`**: Lớp cơ sở trừu tượng mà SMG kế thừa.
* **`WeaponChest`**: Sinh ngẫu nhiên SMG với tỉ lệ 1/3 khi mở hòm tiếp tế.
* **`WeaponManager`**: Lưu trữ và quản lý trang bị SMG trên Slot 1 hoặc Slot 2, xử lý hoán đổi vị trí và nạp đạn.
* **`ProjectileManager`**: Tiếp nhận và mô phỏng vật lý các viên đạn bay có độ lệch góc cao từ SMG.
* **`BackpackUI`**: Hiển thị thông số, trạng thái cầm trên tay và hỗ trợ hoán đổi vị trí.

---

## 6. Sửa đổi / Cập nhật
* **16/10/2026**:
  - Khởi tạo súng tiểu liên SMG theo yêu cầu: Sát thương đơn thấp (11 HP), tốc độ bắn cao (920 RPM) bù DPS, độ lệch khi sấy lớn ($\pm 0.18$ rad), băng đạn 35 viên nạp nhanh 1.4s.
  - Tích hợp vào hòm tiếp tế và giao diện Balo chiến thuật.
  - Viết 2 bài unit test chuyên sâu xác thực chỉ số và cơ chế tản đạn.

---

## 7. Lỗi đã sửa
Chưa ghi nhận lỗi.
