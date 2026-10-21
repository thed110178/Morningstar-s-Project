Cập nhật mới nhất vào ngày: 20/10/2026

# Weapon.java - Lớp Cơ Sở Vũ Khí (Strategy Pattern)

## 1. Mô tả
`Weapon.java` là lớp trừu tượng (Abstract Base Class) định nghĩa toàn bộ các thuộc tính và hành vi chung của kho vũ khí trong trò chơi, áp dụng mẫu thiết kế **Strategy Pattern** và nguyên lý **Open/Closed Principle (SOLID)**. Mọi loại vũ khí (Assault Rifle, SMG, Shotgun, Pistol, Combat Knife) đều kế thừa từ lớp này mà không làm ảnh hưởng tới logic điều khiển người chơi.

Lớp tích hợp hệ thống sao nâng cấp (Damage Stars, Fire Rate Stars, Accuracy Stars), hệ thống chí mạng độc lập (Critical Hit Chance & Multiplier), chỉ số sát thương theo giây (DPS), tính năng hiệu chỉnh/tùy biến khí tài dã chiến (Gunsmith Reforge), và hệ thống **Khí Tài Hạng S (S-Tier Legendary)** với cơ chế tự động thăng cấp khi qua màn.

---

## 2. Quy trình hoạt động
1. **Khởi tạo và cấu hình cấp độ**:
   - Khi khởi tạo, các thông số cơ sở 1 sao được thiết lập: `baseDamage`, `baseFireRateCooldown`, `baseSpreadAngle`.
   - Cấp độ và sao thặng dư được phân bổ ngẫu nhiên hoặc nâng cấp qua `configureLevel(int)` hoặc `upgradeLevel()`.
2. **Cơ chế Khí Tài Hạng S (S-Tier Legendary)**:
   - Súng có cờ `sTier == true` sở hữu tỉ lệ chí mạng thường xuyên cao (`35%` - `50%`) và hệ số chí mạng ngẫu nhiên (`1.50x` - `2.00x`).
   - Mỗi khi qua một Stage tác chiến mới (`advanceToNextStage`), súng S tự động thăng thêm 1 Cấp (`autoAdvanceStageLevel`) mà không làm tiêu tốn số lần nâng cấp tối đa tại Shop dã chiến.
   - Khi ở dưới mặt đất, súng S tỏa **Hào quang vàng kim hoàng gia** (`Pulsing Golden Aura`), các tia sáng nhấp nháy (`Sparkles`) và mang huy hiệu ký hiệu `[S]`.
3. **Tính toán sát thương & Chỉ số DPS**:
   - `calculateDamage(boolean isCrit)`: Kết hợp buff tấn công và hệ số chí mạng.
   - `getNormalDPS()` & `getCritDPS()`: Đo lường tổng lượng hỏa lực sát thương xả ra trong 1 giây.
4. **Hiệu chỉnh / Tái phân bổ chỉ số (Reforge)**:
   - Tái gieo ngẫu nhiên toàn bộ sao thặng dư vào 3 nhánh và gieo lại tỉ lệ chí mạng/hệ số chí mạng mà vẫn bảo toàn cấp độ súng.

---

## 3. API chính
* `public abstract boolean attack(double originX, double originY, double aimAngle, ProjectileManager pm, List<MeleeSlash> slashList)`: Thực hiện đòn tấn công.
* `public void reload()`: Nạp lại băng đạn.
* `public void update(double deltaTime)`: Cập nhật cooldown và tiến trình reload.
* `public boolean canAttack()`: Kiểm tra vũ khí đã sẵn sàng tấn công hay chưa.
* `public boolean isMelee()`: Kiểm tra vũ khí có phải là cận chiến hay không.
* `public boolean isSTier()`: Kiểm tra vũ khí có phải là Khí Tài Hạng S hay không.
* `public void applySTierUpgrade()`: Kích hoạt mác Hạng S với crit cao 35%-50%.
* `public String autoAdvanceStageLevel()`: Tự động thăng thêm 1 Cấp (+1 Level) cho súng Hạng S.
* `public int getNormalDPS()` / `public int getCritDPS()`: Lấy chỉ số DPS thường và chí mạng.
* `public ReforgeResult reforgeStats()`: Tái phân bổ sao và roll lại chí mạng tại trạm dã chiến.

---

## 4. Quan hệ với hệ thống khác
* **Lớp cha của**: `AssaultRifle.java`, `SMG.java`, `Shotgun.java`, `Pistol.java`, `CombatKnife.java`.
* **Được quản lý bởi**: `WeaponManager.java`.
* **Xuất hiện trong**: `WeaponChest.java` (hòm nhặt đồ rơi dưới đất), `ShopItem.java` (quầy giao dịch dã chiến), `StatReforgeStation.java` (bàn hiệu chỉnh).
* **Liên kết nâng cấp Stage**: `PlayingState.advanceToNextStage()`.

---

## 5. Sửa đổi / Cập nhật
* **03/10/2026**: Khởi tạo ban đầu với Strategy Pattern cho vũ khí tầm xa và cận chiến.
* **18/10/2026 - 19/10/2026**: Bổ sung hệ thống cấp độ sao, chỉ số DPS, hệ số chí mạng ngẫu nhiên và trạm tẩy luyện Reforge.
* **20/10/2026**: Thêm hệ thống **Khí Tài Hạng S (S-Tier Legendary)**:
  - Tỉ lệ rơi 5% khi mở hòm hoặc tại quầy shop.
  - Tỉ lệ chí mạng thường xuyên cao trong khoảng [35%, 50%].
  - Cơ chế tự thăng cấp +1 Level mỗi khi vượt qua Stage mới (`autoAdvanceStageLevel`).
  - Hào quang vàng kim hoàng gia tỏa nhịp thở, tia sáng sparkles và huy hiệu `[S]` khi ở dưới đất.

---

## 6. Lỗi đã sửa
* Sửa lỗi tràn số và tính toán DPS chính xác cho các dòng súng nhiều mảnh đạn (Shotgun 6 pellets).
* Bảo đảm việc tự thăng cấp súng S không làm tăng `upgradeCount`, giữ nguyên quyền nâng cấp tại Shop.
