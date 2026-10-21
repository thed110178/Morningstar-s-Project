Cập nhật mới nhất vào ngày: 21/10/2026

# SemiCarbineRifle.java - Súng Trường Bán Tự Động (Semi-Carbine Rifle - SCR)

## 1. Mô tả
[SemiCarbineRifle.java](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/SemiCarbineRifle.java) kế thừa từ [Weapon](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/Weapon.java), là dòng súng trường bán tự động chiến thuật (Semi-Automatic Carbine) kết hợp giữa uy lực hỏa lực cao của súng trường chiến đấu và khả năng bắn tỉa tầm trung - xa chính xác.

Súng sở hữu băng đạn tiêu chuẩn **20 viên**, sử dụng chung kho đạn súng trường ([AmmoType.AR](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/item/AmmoType.java)), tốc độ nhấp cò chiến thuật **0.18 giây/phát** (~333 RPM), sát thương đơn cơ sở cao vượt trội (**34 HP / phát**, gấp hơn 2 lần so với 16 HP của AR), và sơ tốc đầu nòng đạt tới **1150 px/giây** (vượt trội so với 900 px/giây của AR).

Đặc biệt, súng sở hữu cơ chế động lực học độ giật **First-Shot Pinpoint & Heavy Continuous Bloom**:
* **Độ chính xác viên đầu tiên tuyệt đối**: Chỉ duy nhất viên đạn đầu tiên có độ lệch cực nhỏ ($\pm 0.29^\circ$), đạn bay thẳng tắp như súng bắn tỉa vào tâm ngắm.
* **Độ giật và tản đạn nảy vọt ngay từ viên thứ 2 (Heavy Spray Bloom)**: Nếu người chơi spam nhấp chuột dồn dập không ngừng nghỉ, góc lệch tản đạn tăng vọt rất dốc ngay từ phát thứ 2 và đạt cực đại gấp 8 lần cơ sở (lên tới $\pm 22.9^\circ$) ở phát thứ 4.
* **Ép đạn văng dạt sang hai bên khi spam (Anti-Lucky Recoil Jitter)**: Khi nhấp dồn dập, lực giật nảy hất đạn dạt lệch tối thiểu 25% bán kính bloom ra xa tâm ngắm, triệt tiêu hoàn toàn hiện tượng đạn may mắn rơi vào hồng tâm.
* **Cửa sổ hồi phục tâm dài hơn (Recoil Recovery Window)**: Chỉ khi ngừng bắn quá 0.45 giây, tâm súng mới bắt đầu hồi phục (nhấp cò nhanh hơn 2.2 click/s sẽ không thể hồi tâm). Sau 0.70s tâm súng trở về trạng thái viên đầu tiên.
* **Rung chấn màn hình (Screen Shake)**: Khi bắn SCR ở nhịp dồn dập (từ phát 2 trở đi), màn hình nảy rung phản hồi tạo cảm giác nảy giật uy lực.

---

## 2. Quy trình hoạt động
1. **Trang bị vũ khí**:
   * Người chơi có thể nhặt súng tại hòm tiếp tế dã chiến ([WeaponChest](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/item/WeaponChest.java)) hoặc mua tại Cửa hàng Quân Nhu ([ShopItem](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/shop/ShopItem.java)).
   * Súng được trang bị vào Ô [1] hoặc Ô [2] (vũ khí chính Primary Weapon).
2. **Khai hỏa bán tự động (Semi-Automatic Firing)**:
   * Súng không phải súng tự động (`isAutomatic() == false`). Mỗi lần người chơi click chuột trái sẽ bắn ra một viên đạn.
   * Thời gian chờ giữa 2 lần nhấp cò tối thiểu là 0.18 giây.
3. **Cơ chế giật động lực học (Dynamic Recoil & Bloom)**:
   * Phát 1: Độ lệch đạt cực tiểu $\pm 0.29^\circ$ (10% góc lệch cơ sở).
   * Phát 2: Góc lệch tăng vọt lên $\approx \pm 7.8^\circ$ kèm độ văng tối thiểu sang hai bên.
   * Phát 3: Góc lệch tiếp tục tăng lên $\approx \pm 15.4^\circ$.
   * Từ phát 4 trở đi: Góc lệch đạt trần tối đa $\pm 22.9^\circ$ (gấp 8 lần cơ sở), đạn tản rộng sang hai bên.
   * Khi ngừng click chuột $>0.45$s: Bộ đếm đạn giật tự động giảm dần; sau $0.70$s độ giật hồi phục hoàn toàn về viên đầu tiên.
4. **Nạp đạn**:
   * Khi băng đạn 20 viên cạn kiệt, súng tự động kích hoạt nạp đạn trong 1.9 giây, nạp lại 20 viên từ kho đạn súng trường [AmmoType.AR](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/item/AmmoType.java).
   * Khi có Perk `EXTENDED_MAG`, băng đạn mở rộng lên tới **28 viên**.

---

## 3. Ba Chỉ Số Chính (Key Indicators)

| Chỉ số | Thông số cơ sở (1★) | Cơ chế thăng cấp Sao / Nâng cấp | Ý nghĩa chiến thuật |
|---|---|---|---|
| **1. Sát thương (Damage)** | 34 HP / phát | +20% mỗi sao (1★: 34, 2★: 41, 3★: 48, 4★: 55) | Uy lực cao, 2-3 phát tiêu diệt kẻ địch tầm trung, hỗ trợ DPS ổn định |
| **2. Tốc bắn (Fire Rate)** | 0.18s (~333 RPM) | Giảm thời gian trễ (+15% RPM/sao) | Tăng tốc độ nhấp nhả cò khi cận chiến hoặc bắn cấp tập |
| **3. Độ chính xác (Accuracy)** | Đầu $\pm 0.3^\circ$ \| Max $\pm 22.9^\circ$ | Giảm góc lệch cơ sở $(\times 0.65/\text{sao})$, gom tâm viên đầu và giảm tản tối đa | Tăng cường khả năng bắn tỉa tầm xa và kiểm soát hỏa lực dồn dập |

---

## 4. Cấu hình & Tham số chi tiết
* **Tên hiển thị**: `Semi-Carbine Rifle (SCR)` / Ký hiệu ngắn: `SCR`
* **Loại đạn**: [AmmoType.AR](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/item/AmmoType.java) (Đạn súng trường, sức chứa kho ba lô tối đa 150 viên)
* **Băng đạn (`magazineSize`)**: 20 viên (mở rộng lên 28 viên với Perk `EXTENDED_MAG`)
* **Thời gian nạp đạn (`reloadDuration`)**: 1.9 giây
* **Vận tốc đạn (`BULLET_SPEED`)**: 1150 px/giây (vận tốc cao hơn AR 900 px/giây)
* **Thời gian tồn tại của đạn (`BULLET_LIFETIME`)**: 1.1 giây (tầm bay hiệu dụng ~1265 px)
* **Góc lệch cơ sở (`baseSpreadAngle`)**: $\pm 0.05\text{ rad}$ ($\approx \pm 2.86^\circ$)
* **Hệ số viên đầu (`INITIAL_SPREAD_RATIO`)**: $0.10$ ($\approx \pm 0.29^\circ$)
* **Hệ số giật tối đa (`MAX_SPREAD_RATIO`)**: $8.0$ ($\approx \pm 22.9^\circ$)
* **Tỉ lệ chí mạng cơ sở (`critChance`)**: 15% - 25% (Hạng S lên tới 35% - 50%)

---

## 5. Đồ họa & Hiển thị
* **Hình ảnh Sprite Pixel Art**: Tài nguyên `SemiCarbineRifle.png` (32x32 pixel) tại `Material/SemiCarbineRifle.png` và `resources/weapons/SemiCarbineRifle.png`.
* **Hiển thị giao diện**:
  * **Ba lô tác chiến ([BackpackUI](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/ui/BackpackUI.java))**: Hiển thị ảnh Sprite 56x56 trên nền tương phản sáng 64x64, thông số hiển thị `• Chính xác: ★ ☆ ☆ (Đầu ±0.3° | Max ±22.9°)`.
  * **Hòm tiếp tế ([WeaponChest](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/item/WeaponChest.java))**: Vũ khí nổi lơ lửng kèm ảnh Sprite 32x32 trên bệ ánh sáng vàng.
  * **Cửa hàng Quân Nhu ([ShopItem](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/shop/ShopItem.java))**: Hiển thị ảnh súng trên bệ trưng bày.
  * **HUD thanh trang bị ([WeaponManager](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/WeaponManager.java))**: Hiển thị nhãn chip `SCR I`, `SCR II`, `[S] SCR III`.
* **Mô hình ngắm bắn trong game**: Nòng carbine thon dài màu xanh ô liu quân sự (30px), đầu bù giật Muzzle Brake ánh bạc và hộp tiếp đạn 20 viên vát xéo đặc trưng.
* **Vector Fallback dự phòng**: Hệ thống tự động vẽ vector dự phòng sắc nét nếu môi trường chạy không tìm thấy ảnh.

---

## 6. Quan hệ với hệ thống khác
* [WeaponSpriteManager.java](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/WeaponSpriteManager.java): Quản lý nạp bộ đệm cache, ánh xạ và render Sprite cho `SemiCarbineRifle`.
* [WeaponManager.java](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/WeaponManager.java): Quản lý chuyển đổi slot, kiểm tra đạn và vẽ slot HUD.
* [PlayingState.java](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/state/PlayingState.java): Kích hoạt rung màn hình (`triggerScreenShake(0.08)`) khi bắn dồn dập.
* [PlayerBuffManager.java](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/buff/PlayerBuffManager.java): Mở rộng băng đạn từ 20 lên 28 viên khi kích hoạt Perk `EXTENDED_MAG`.
* [WeaponChest.java](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/item/WeaponChest.java) & [ShopItem.java](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/shop/ShopItem.java): Tự động xuất hiện trong pool rơi súng ngẫu nhiên (20% tỉ lệ rơi súng thường, và trong pool Hạng S).

---

## 7. Sửa đổi / Cập nhật
* **21/10/2026**:
  * Khởi tạo lớp súng mới `SemiCarbineRifle` với băng đạn 20 viên, đạn Rifle, sát thương 34 HP, sơ tốc 1150 px/s.
  * Tích hợp cơ chế độ giật động lực học (First-Shot Pinpoint và Spray Bloom).
  * Tích hợp hình ảnh minh họa Pixel Art `SemiCarbineRifle.png` vào `WeaponSpriteManager`.
  * Cập nhật tỉ lệ rơi ngẫu nhiên súng trong `Weapon.getRandomDropWeapon()`.
  * Bổ sung hiển thị thông số chi tiết độ lệch viên đầu và độ lệch tối đa trong `BackpackUI`.
  * Bổ sung hỗ trợ mở rộng băng đạn lên 28 viên trong `PlayerBuffManager`.
  * **Cải tiến độ giật nảy hỏa lực dồn dập (Heavy Recoil Overhaul)**:
    - Nâng góc lệch tối đa khi spam click từ 4.0x ($\pm 11.5^\circ$) lên 8.0x ($\pm 22.9^\circ$).
    - Chỉ phát bắn đầu tiên duy nhất được hưởng độ chính xác tuyệt đối ($\pm 0.29^\circ$), ngay từ phát thứ 2 độ lệch tăng vọt và đạt trần ở phát thứ 4.
    - Ép đạn văng dạt tối thiểu 25% bán kính bloom ra xa tâm khi bắn dồn dập, ngăn chặn việc vô tình trúng đích hồng tâm khi spam click.
    - Kéo dài ngưỡng ngừng bắn để hồi tâm từ 0.27s lên 0.45s; người chơi nhấp cò nhanh hơn 2.2 nhịp/giây sẽ không thể tự hồi phục độ giật.
    - Thêm phản hồi rung màn hình (`triggerScreenShake(0.08)`) trong `PlayingState` khi bắn SCR dồn dập.
