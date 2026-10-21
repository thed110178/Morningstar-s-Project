Cập nhật mới nhất vào ngày: 21/10/2026

# SniperRifle.java - Súng Bắn Tỉa Kéo Quy Lát (Bolt-Action Sniper Rifle - SR)

## 1. Mô tả
[SniperRifle.java](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/SniperRifle.java) kế thừa từ [Weapon](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/Weapon.java), là dòng vũ khí hỏa lực tầm xa hạng nặng đại diện cho phong cách "One Shot, Multi-Kill". Súng sở hữu băng đạn giới hạn **5 viên**, chu kỳ kéo chốt quy lát cơ học (Bolt Action) **0.95 giây/phát** (~63 RPM), vận tốc đạn cực cao lên tới **1450 px/giây** và sát thương đơn cơ sở cực khủng (**95 HP**).

Đặc biệt, súng sở hữu cơ chế **ĐỘ XUYÊN (Penetration Power)** độc quyền: viên đạn có thể đâm xuyên qua nhiều kẻ địch xếp thẳng hàng và xuyên qua các ô cover tạm thời ([DESTRUCTIBLE_COVER](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/dungeon/TileType.java)), với lượng sát thương giảm dần sau mỗi mục tiêu bị xuyên.

---

## 2. Quy trình hoạt động
1. **Trang bị vũ khí**: Người chơi có thể trang bị vào Slot 1 hoặc Slot 2 từ hòm tiếp tế ([WeaponChest](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/item/WeaponChest.java)) hoặc mua tại Shop dã chiến.
2. **Khai hỏa từng phát (Semi-Auto / Bolt-Action)**:
   * Người chơi nhấp chuột trái cho mỗi phát bắn (không phải súng tự động `isAutomatic() == false`).
   * Mỗi phát bắn tiêu hao 1 viên đạn trong băng (`currentAmmo--`) và kích hoạt thời gian kéo chốt quy lát cơ học 0.95 giây.
3. **Cơ chế Độ Xuyên và Giảm Sát Thương (Penetration & Falloff)**:
   * **Số lần xuyên tối đa**: Mặc định 1★ đạn có thể xuyên qua tối đa 2 mục tiêu / cover (chạm mục tiêu thứ 3 thì tiêu biến). Mỗi sao Độ Xuyên tăng thêm +1 lần xuyên.
   * **Giảm sát thương do xuyên**: Ban đầu giảm 25% sát thương mỗi lần xuyên phá. Khi nâng sao Độ Xuyên, tỉ lệ sụt giảm sát thương được tối ưu (giảm 5% mỗi sao, tối thiểu chỉ giảm 8%).
   * **Xuyên qua Cover Tạm Thời**: Khi đạn bay vào ô cover tạm thời ([DESTRUCTIBLE_COVER](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/dungeon/TileType.java)), cover nhận toàn bộ sát thương của đạn; nếu đạn còn lượt xuyên, đạn tiếp tục bay xuyên qua thùng gỗ và gây sát thương cho mục tiêu phía sau.
   * **Vật cản cứng tuyệt đối**: Đạn bị chặn lại ngay lập tức khi chạm vào tường đá, cửa sắt đóng hoặc mép bản đồ (không xuyên qua tường cứng).
4. **Nạp đạn**:
   * Khi băng đạn về 0, súng kích hoạt nạp đạn trong 2.4 giây, rút 5 viên từ kho đạn dự trữ [AmmoType.SR](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/item/AmmoType.java) (sức chứa tối đa trong balô là 30 viên).

---

## 3. Ba Chỉ Số Chính (Key Indicators)

| Chỉ số | Thông số cơ sở (1★) | Cơ chế thăng cấp Sao / Nâng cấp | Ý nghĩa chiến thuật |
|---|---|---|---|
| **1. Sát thương đơn** | 95 HP / viên | +20% mỗi sao (1★: 95, 2★: 114, 3★: 133, 4★: 152) | 1 phát hạ gục hầu hết lính thường, gây sát thương khủng lên Boss |
| **2. Tốc độ ra đạn** | 0.95 giây (~63 RPM) | Giảm thời gian kéo chốt quy lát (-15% cooldown/sao) | Tăng tốc độ thao tác nhồi đạn giữa các phát bắn |
| **3. Độ xuyên** | 2 lần xuyên, giảm 25% DMG | +1 lần xuyên/sao, giảm độ sụt sát thương (-5%/sao) | Bắn xuyên đội hình kẻ địch và bắn xuyên chướng ngại vật tạm thời |

---

## 4. Cấu hình & Tham số chi tiết
* **Tên hiển thị**: `Sniper Rifle (SR)` / Tên ngắn: `SR`
* **Loại đạn gắn kết**: [AmmoType.SR](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/item/AmmoType.java) (Đạn bắn tỉa SR, tối đa 30 viên trong kho balô, stack rơi 5 viên)
* **Băng đạn (`magazineSize`)**: 5 viên
* **Thời gian nạp đạn (`reloadDuration`)**: 2.4 giây
* **Vận tốc đạn (`BULLET_SPEED`)**: 1450 px/giây (vận tốc xé gió tức thời)
* **Thời gian tồn tại của đạn (`BULLET_LIFETIME`)**: 1.0 giây (tầm bay tới 1450 px bao quát màn hình)
* **Góc lệch cơ sở (`baseSpreadAngle`)**: $\pm 0.005\text{ rad}$ ($\approx \pm 0.3^\circ$, cực kỳ chuẩn xác)
* **Tỉ lệ chí mạng cơ sở (`critChance`)**: 25% (Hệ số x1.50 - x2.00; lên tới x2.50 khi có Perk Bậc Thầy Chí Mạng)

---

## 5. Đồ họa & Hiển thị
* **Mô hình súng**: Nòng súng dài thanh mảnh màu xám kim loại vũ khí (34px), ống ngắm quang học bắn tỉa (Sniper Scope) gắn dọc sống súng với thấu kính dạ quang tím neon đặc trưng, đầu nòng hãm giật (Muzzle Brake) và hiệu ứng ánh chớp đầu nòng khi khai hỏa.
* **Vệt đạn (Tracer Trail)**: Đường đạn phát sáng với vệt đuôi tím điện quang neon kéo dài 18px xé gió, lõi đạn thạch anh ánh trắng tím uy lực.

---

## 6. Quan hệ với hệ thống khác
* [WeaponManager.java](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/WeaponManager.java): Quản lý chuyển đổi slot, hiển thị nhãn `SR`, hiển thị pill đạn `SR` (màu tím neon) trên thanh HUD.
* [BackpackUI.java](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/ui/BackpackUI.java): Hiển thị icon mô phỏng súng ngắm có ống ngắm, thẻ thông số chi tiết với dòng "• Độ xuyên: ★ ☆ ☆ (2 mục tiêu | -25%)" và thanh đạn dự trữ `• BẮN TỈA (SR): X / 30`.
* [ProjectileManager.java](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/projectile/ProjectileManager.java): Xử lý va chạm đạn xuyên qua các ô cover tạm thời ([DESTRUCTIBLE_COVER](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/dungeon/TileType.java)).
* [PlayingState.java](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/state/PlayingState.java): Xử lý sát thương xuyên qua chuỗi kẻ địch và rơi hộp đạn SR khi clear phòng / hạ Boss.
