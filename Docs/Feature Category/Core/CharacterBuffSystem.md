Cập nhật mới nhất vào ngày: 20/10/2026

# Hệ Thống Nâng Cấp & Cường Hóa Nhân Vật (Character Upgrade & Buffs System)

## 1. Mô tả
Hệ thống **Nâng Cấp & Cường Hóa Nhân Vật (Character Upgrade System)** cung cấp cho người chơi chiến binh Operative khả năng gia tăng sức mạnh thông qua 13 loại Buff chuyên biệt. Các Buff được chia thành 2 nhóm rõ ràng và phục vụ mua bán qua 2 trạm bệ máy riêng biệt bằng tiền tệ chiến thuật Credits trong **Phòng Nâng Cấp Nhân Vật (Bio-Tech Augmentation Laboratory)**.

Hệ thống mang đậm phong cách Roguelite:
- **Nhóm 1 - Chỉ số cơ bản (6 Buffs)**: Có thể thăng nhiều cấp độ tới ngưỡng trần (caps), giá thành vừa phải (~40 - 50 Credits/cấp).
- **Nhóm 2 - Kỹ năng đặc biệt (7 Buffs)**: Mở khóa các cơ chế gameplay độc nhất vô nhị (1 cấp duy nhất), giá thành cao (~110 - 150 Credits) mang lại bước ngoặt chiến thuật.

---

## 2. Quy trình hoạt động

### 2.1. Phân loại 13 Buff Cường Hóa

#### Nhóm 1: Buff Chỉ Số Cơ Bản (Basic Stats - Trạm 1)
1. **TĂNG % MÁU TỐI ĐA (`MAX_HEALTH`)**:
   - Tác dụng: +20% Máu tối đa mỗi cấp. Tối đa 5 cấp = 200% máu ban đầu (200 HP).
   - Cơ chế tức thời: Khi nâng cấp, máu tối đa tăng đồng thời hồi ngay lượng máu tương ứng.
2. **TĂNG % TẤN CÔNG (`ATTACK_DAMAGE`)**:
   - Tác dụng: +10% sát thương cho toàn bộ các loại súng và dao găm. Tối đa 5 cấp = 150% (1.5x sát thương).
3. **TĂNG % TỐC ĐỘ (`MOVE_SPEED`)**:
   - Tác dụng: +5% tốc độ di chuyển nhân vật mỗi cấp. Tối đa 4 cấp = 120% tốc độ ban đầu (252 px/s).
4. **TĂNG % PHÒNG THỦ (`DEFENSE`)**:
   - Tác dụng: Giảm 10% sát thương phải chịu mỗi cấp. Tối đa 3 cấp = Giảm tối đa 30% mọi sát thương nhận vào.
5. **GIẢM HỒI CHIÊU NÉ (`DODGE_COOLDOWN`)**:
   - Tác dụng: Rút ngắn thời gian hồi chiêu lướt né [SPACE]. Tối đa 3 cấp = Nhanh hơn 1.5 lần (CD giảm từ 0.80s xuống còn 0.533s).
6. **MAY MẮN NHẶT ĐỒ (`LOOT_LUCK`)**:
   - Tác dụng: Tăng tỉ lệ mở hòm chiến lợi phẩm ra súng nhiều sao (4-5★) và súng Đỏ Mythic. Tối đa 3 cấp.

#### Nhóm 2: Kỹ Năng Đặc Biệt (Special Perks - Trạm 2)
1. **GIÁP SỐ LẦN CÓ HỒI PHỤC (`ONE_HIT_BARRIER`)**:
   - Tác dụng: Khiên năng lượng hấp thụ chặn đứng 100% sát thương của 1 đòn đánh bất kỳ (kể cả pháo Boss hay bộc phá).
   - Cơ chế hồi phục: Tự động hồi phục sau `10.0 giây` hồi chiêu kể từ khi vỡ khiên.
2. **DIỆT ĐỊCH HỒI MÁU (`KILL_HEAL`)**:
   - Tác dụng: Hồi phục `2.5 HP` cho mỗi mạng kẻ địch bị tiêu diệt.
   - Cơ chế tích lũy phần lẻ: Địch 1 hồi 2 HP (dư 0.5), địch 2 hồi 3 HP (đủ 5 HP sau 2 mạng diệt).
3. **NÉ QUA VẬT CẢN (`PHASE_DODGE`)**:
   - Tác dụng: Khi kích hoạt lướt né [SPACE], người chơi có thể lướt đi xuyên qua mọi vật cản địa hình (tường, cửa, thùng che chắn).
4. **THU NHỎ HITBOX (`SHRINK_HITBOX`)**:
   - Tác dụng: Giảm 30% kích thước hitbox nhận sát thương của người chơi (từ 28px xuống 19.6px), giúp luồn lách né đạn trong mưa đạn địa ngục (bullet hell).
5. **BĂNG ĐẠN BỔ SUNG (`PERIODIC_AMMO`)**:
   - Tác dụng: Tiếp tế tự động 2 băng đạn dự trữ cho vũ khí chính đang cầm mỗi chu kỳ `30 giây`.
6. **BĂNG ĐẠN MỞ RỘNG (`EXTENDED_MAGAZINE`)**:
   - Tác dụng: Tăng vĩnh viễn dung lượng băng đạn của các vũ khí cơ bản:
     - Assault Rifle: 30 viên -> 40 viên.
     - SMG: 25 viên -> 60 viên.
     - Tactical Pistol: 7 viên -> 12 viên.
7. **BẬC THẦY CHÍ MẠNG (`CRITICAL_MASTERY`)**:
   - Tác dụng: Tăng thêm `+15%` tỉ lệ chí mạng cho mọi đòn đánh.
   - Hệ số sát thương chí mạng chuyên biệt:
     - Rifle: 150% (1.5x).
     - SMG & Pistol: 120% (1.2x).
     - Dao găm (Combat Knife): 200% (2.0x).
     - Shotgun / Khác: 150% (1.5x).

---

### 2.2. Quy trình rút thẻ ngẫu nhiên và mua bán
1. Người chơi tiếp cận một trong hai bệ máy trạm trong phòng nâng cấp (`basicUpgradeStation` hoặc `specialUpgradeStation`) và nhấn phím **`[F]`**.
2. Trạm tự động rút ngẫu nhiên **3 Buff** từ kho tương ứng (chưa đạt cấp tối đa) và mở bảng giao diện Sci-Fi Modal UI.
3. Người chơi quan sát tên, cấp độ hiện tại / tối đa, mô tả chi tiết và giá tiền Credits.
4. Mua buff bằng phím tắt **`[1]`**, **`[2]`**, **`[3]`** hoặc click chuột trực tiếp:
   - Nếu đủ Credits: Khấu trừ tiền, thăng cấp buff, kích hoạt tức thời hiệu ứng và phát thông báo xác nhận màu xanh.
   - Nếu thiếu Credits hoặc đã max level: Hiển thị cảnh báo màu đỏ/cam.
5. Thoát giao diện bằng phím **`[ESC]`** hoặc **`[F]`**.

---

## 3. API

### Lớp `PlayerBuffManager`
- `applyBuff(CharacterBuff buff, Player player, WeaponManager weaponManager)`: Thăng cấp buff và kích hoạt hiệu ứng tức thời.
- `getMaxHealthMultiplier()`: Hệ số nhân máu tối đa (1.0 -> 2.0).
- `getAttackMultiplier()`: Hệ số nhân sát thương tấn công (1.0 -> 1.5).
- `getSpeedMultiplier()`: Hệ số nhân tốc độ chạy (1.0 -> 1.2).
- `getDamageReduction()`: Tỉ lệ giảm sát thương (0.0 -> 0.30).
- `getDodgeCooldownFactor()`: Hệ số rút ngắn hồi chiêu né (1.0 -> 1.5).
- `checkAndConsumeBarrier()`: Kiểm tra và tiêu hao khiên chặn đòn 10s CD.
- `onEnemyKilled()`: Tích lũy 2.5 HP khi diệt địch và trả về lượng máu chẵn cần hồi.
- `hasPhaseDodge()`: Kiểm tra né xuyên vật cản.
- `getHitboxScale()`: Trả về tỉ lệ thu nhỏ hitbox (1.0 hoặc 0.70).
- `getCritChanceBonus()`: Trả về tỉ lệ crit cộng thêm (+15%).
- `getCustomCritMultiplier(Weapon weapon)`: Trả về hệ số crit theo loại súng.

### Lớp `CharacterUpgradeStation`
- `rollBuffs(PlayerBuffManager buffManager)`: Rút ngẫu nhiên 3 buff hợp lệ theo phân loại trạm.
- `purchaseBuff(int index, PlayingState state)`: Khấu trừ Credits và thăng cấp buff.
- `render(Graphics2D g, Camera camera, double playerX, double playerY, double playerSize)`: Kết xuất bệ máy 2D dã chiến.

### Lớp `CharacterUpgradeUI`
- `open(CharacterUpgradeStation station, PlayerBuffManager buffManager)`: Mở modal UI.
- `close()`: Đóng modal UI.
- `update(KeyboardHandler keyH, MouseHandler mouseH, PlayingState state, double deltaTime)`: Xử lý phím tắt và click chuột.
- `render(Graphics2D g, int screenW, int screenH, PlayingState state)`: Vẽ bảng modal Sci-Fi.

---

## 4. Quan hệ với hệ thống khác
- **`Player`**: Áp dụng tốc độ chạy, máu tối đa, hitbox scale, i-frames né xuyên vật cản và khiên chặn đòn.
- **`Weapon` & `WeaponManager`**: Áp dụng hệ số tấn công `attackMultiplier`, cộng thêm `critChanceBonus`, tính sát thương crit theo `customCritMultiplier`, và mở rộng băng đạn.
- **`DungeonMap` & `Room`**: Tự động sinh `upgradeRoom` trong quá trình kiến tạo hầm ngục và lắp đặt 2 trạm đối xứng.
- **`PlayingHUD`**: Kết xuất thanh đếm Khiên Chặn Đòn và các huy hiệu biểu tượng buff đang kích hoạt.

---

## 5. Sửa đổi / Cập nhật
- **20/10/2026**:
  - Khởi tạo toàn diện hệ thống Nâng Cấp Nhân Vật và 13 Buffs.
  - Phân tách thành 2 trạm bệ máy riêng biệt: Trạm Chỉ Số Cơ Bản và Trạm Kỹ Năng Đặc Biệt.
  - Cân bằng theo phản hồi người dùng: Loại bỏ buff Hóa máu thành giáp, điều chỉnh diệt địch hồi 2.5 HP, điều chỉnh crit +15%, loại bỏ các loại súng chưa có (MG/Sniper).
  - Hoàn thiện 9 bài Unit Test tự động trong `CharacterBuffSystemTest.java`.
