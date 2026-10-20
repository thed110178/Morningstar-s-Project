Cập nhật mới nhất vào ngày: 19/10/2026

# CombatKnife.java - Dao Găm Tác Chiến Cận Chiến (Slot 4 & Chuột Phải)

## 1. Mô tả
`CombatKnife.java` kế thừa từ `Weapon`, là vũ khí cận chiến khởi đầu gắn với phím tắt số **4** và hỗ trợ **Chém tức thì bằng Chuột Phải (Quick-Melee on Right Mouse Button)**. Dao găm không tiêu hao đạn, không cần nạp đạn, gây lượng sát thương cao (45 HP), xung lực đẩy lùi mạnh mẽ (650.0 px/s) và tạo ra một vùng Hitbox hình vuông kích thước xấp xỉ 1 ô gạch ($48 \times 48\text{ px}$) phía trước mặt nhân vật theo hướng con trỏ chuột.

---

## 2. Quy trình hoạt động
1. **Trang bị truyền thống (Slot 4)**:
   - Người chơi bấm phím `4` để cầm dao găm trên tay. Bấm chuột trái để chém.
2. **Cơ chế Chém Nhanh Cơ Động Cao (Quick-Melee on Right-Click)**:
   - Khi đang cầm bất kỳ vũ khí nào khác (Súng trường tấn công AR, Shotgun, Súng lục Pistol...):
   - Người chơi **bấm chuột phải (Right Mouse Button)** $\rightarrow$ Dao găm lập tức vung chém theo góc ngắm chuột mà không cần đổi vũ khí cầm trên tay.
   - Giúp người chơi phản xạ tức thì, bảo toàn hỏa lực súng và đẩy lùi lũ quái vật cận chiến đang điên cuồng áp sát.
3. Khi thực hiện nhát chém:
   - Nếu hết thời gian hồi chiêu chém ($0.32\text{s}$):
     * Tạo một đối tượng `MeleeSlash` đặt tâm cách người chơi $36\text{ px}$ về phía góc ngắm chuột.
     * Kích thước hộp bao sát thương là $48 \times 48\text{ px}$ (xấp xỉ 1 ô gạch $32\text{px} - 48\text{px}$).
     * Gây $45\text{ HP}$ sát thương và truyền xung lực đẩy lùi cực mạnh **$600.0\text{ px/s}$** đẩy bật kẻ địch ra xa $\sim 30 - 36\text{px}$, làm choáng bộ đếm cắn cào của quái cận chiến.
     * Hiển thị vệt chém màu bạc - vàng ánh kim (hoặc đỏ cam rực rỡ khi nổ đòn chí mạng $90\text{ HP}$) trong $0.12\text{s}$.
     * Đặt lại cooldown $0.32\text{s}$.
     * **Quy tắc ưu tiên thao tác**: Khi người chơi đang giữ chuột trái bắn súng liên thanh (Auto-Fire), việc nhấn chuột phải sẽ **lập tức ngắt xả đạn súng** để thực hiện trọn vẹn nhát chém dao.

---

## 3. Cấu hình & Tham số
* Sát thương cơ bản (`damage`): $45\text{ HP}$ (chí mạng x2.0 đạt $90\text{ HP}$).
* Băng đạn (`magazineSize`): $0$ (vô hạn, không tốn đạn).
* Thời gian hồi vung dao (`fireRateCooldown`): $0.32\text{s}$.
* Khoảng với (`reachOffset`): $36.0\text{ px}$ phía trước nhân vật.
* Kích thước Hitbox: $48.0\text{ px}$.
* Xung lực đẩy lùi (`KNOCKBACK_FORCE`): **$600.0\text{ px/s}$** (tăng cường đẩy bật kẻ địch áp sát).

---

## 4. Quan hệ với hệ thống khác
* Tạo đối tượng: `MeleeSlash`.
* Được quản lý bởi: `WeaponManager` (thông qua `quickMelee` và `attack`).
* Tương tác với: `PlayingState` (bắt sự kiện `mouseH.isRightPressed()`).

---

## 5. Sửa đổi / Cập nhật
* **03/10/2026**: Khởi tạo ban đầu với hitbox cận chiến 1 ô và vệt chém thời gian thực.
* **19/10/2026**:
  - Buff sát thương từ $22\text{ HP}$ lên $45\text{ HP}$.
  - Tinh chỉnh xung lực đẩy lùi cân bằng ở mức **$600.0\text{ px/s}$** (giúp đẩy văng quái áp sát ra xa hơn 1 ô gạch).
  - Tích hợp cơ chế **Quick-Melee bằng Chuột Phải**: Chém dao lập tức khi bấm chuột phải ngay cả khi đang cầm súng chính, giải phóng độ cơ động tối đa trong các pha giao tranh cự ly gần.
  - Tích hợp cơ chế ngắt xả đạn tự động: Khi đang giữ chuột trái sấy súng tự động (AR/SMG) mà bấm chuột phải thì súng lập tức ngừng xả đạn để tập trung chém dao.

---

## 6. Lỗi đã sửa
* Khắc phục triệt để lỗi Multi-Hit gây sát thương lặp nhiều frame trong thời gian vệt chém tồn tại ($0.12\text{s}$) thông qua cơ chế `Set<Object> hitTargets`.
