Cập nhật mới nhất vào ngày: 16/10/2026

# Hệ thống Cấp độ và Chỉ số Sao Vũ khí (Weapon Levels & Star Modifiers)

## 1. Mô tả

Nhằm tăng tính đa dạng, chiều sâu chiến thuật và trải nghiệm roguelite cho kho vũ khí, toàn bộ vũ khí chiến đấu được nâng cấp với **Hệ thống Cấp độ (Levels)** và **Hệ thống 3 Chỉ số đo bằng Sao (Star Modifiers)**.

Hệ thống cho phép các khẩu súng cùng loại (AR, Shotgun, SMG) sở hữu những thuộc tính biến thiên độc đáo, khuyến khích người chơi đưa ra quyết định kinh tế tại Phòng Cửa Hàng (Shop Room): nên giữ súng đang cầm để nâng cấp hay mua súng Level III ngẫu nhiên.

---

## 2. Ba Chỉ số Chính đo bằng Sao

Mỗi khẩu súng sở hữu 3 chỉ số chiến đấu cốt lõi, mỗi chỉ số được xếp hạng từ **1 sao ($1★$)** đến tối đa **3 sao ($3★$)**:

| Chỉ số | Biểu tượng | Tác dụng | Công thức bổ sung |
| :--- | :---: | :--- | :--- |
| **Sát thương đơn** (Damage) | DMG | Gia tăng lượng sát thương mỗi viên đạn trúng đích | $+20\%$ sát thương cơ sở mỗi sao thêm ($1★: +0\%, 2★: +20\%, 3★: +40\%$) |
| **Tốc độ ra đạn** (Fire Rate) | RPM | Rút ngắn thời gian hồi giữa các phát bắn | Tăng $+15\%$ RPM mỗi sao thêm (cooldown $= \text{baseCooldown} / (1.0 + (\text{stars} - 1) \times 0.15)$) |
| **Độ chính xác** (Accuracy) | ACC | Thu hẹp độ tản đạn / độ giật rung ngẫu nhiên khi sấy | Giảm góc tản đạn còn $65\%$ mỗi sao thêm ($\text{spread} = \text{baseSpread} \times 0.65^{\text{stars} - 1}$) |

---

## 3. Ba Quy Tắc Thống Nhất về Cấp Độ và Nâng Cấp Súng

### 3.1 Quy tắc 1: Cấp độ Súng Rơi Phụ Thuộc Vào Số Stage (Stage = Level)
- Khi dọn sạch phòng chiến đấu, hòm tiếp tế trao súng có cấp độ **tương ứng đúng với Stage hiện tại**:
  $$\text{Weapon Level} = \text{Stage}$$
  - **Stage 1**: Rơi súng **Level 1** (VD: `AR I`, `Shotgun I`, `SMG I`). Ở phòng combat đầu tiên chắc chắn 100% rơi súng free.
  - **Stage 2**: Rơi súng **Level 2** (VD: `AR II`, `Shotgun II`, `SMG II`).
  - **Stage 3**: Rơi súng **Level 3** (VD: `AR III`, `Shotgun III`, `SMG III`).
  - **Stage 100**: Rơi súng **Level 100** (VD: `AR Lv.100`, `Shotgun Lv.100`).
- **Phân bổ sao**: Cấp độ $L$ sở hữu tổng cộng $3 + (L - 1)$ sao (Level 1 có 3 sao tiêu chuẩn $1★/1★/1★$, mỗi level cao hơn được cộng thêm $+1★$ ngẫu nhiên vào DMG / RPM / ACC).

### 3.2 Quy tắc 2: Súng Bán ở Shop Luôn Vượt Trội Hơn Stage Hiện Tại (+2 hoặc +3 Level)
- Vũ khí chính bán tại quầy Shop được tạo với cấp độ cao hơn hẳn Stage người chơi đang đi:
  $$\text{Shop Weapon Level} = \text{Stage} + 2 \quad (\text{Tỉ lệ nhỏ } \sim 20\% \text{ sẽ đạt } \text{Stage} + 3)$$
  - **Ở Stage 1**: Súng bán trong shop có **Level 3 (80%)** hoặc đột biến lên **Level 4 (20%)**.
  - **Ở Stage 2**: Súng bán trong shop có **Level 4 (80%)** hoặc đột biến lên **Level 5 (20%)**.
  - **Ở Stage 100**: Súng bán trong shop có **Level 102 (80%)** hoặc **Level 103 (20%)**.
- **Định giá súng shop**:
  - Chênh 2 cấp: **135 Credits** (đắt hơn phí nâng 1 cấp 80C nhưng rẻ hơn phí nâng 2 cấp 160C).
  - Chênh 3 cấp: **170 Credits** (rẻ hơn phí nâng 3 cấp 240C).

### 3.3 Quy tắc 3: Giới Hạn Nâng Cấp Tối Đa 3 Lần Mỗi Khẩu Súng
- Mỗi khẩu súng độc lập chỉ được phép nâng cấp **tối đa 3 lần** tại Bàn Nâng Cấp Shop (`MAX_UPGRADE_COUNT = 3`):
  $$\text{Upgrade Count} \le 3$$
- Bất kể súng nhặt được ở cấp độ nào (Stage 1 nhặt Lv 1 hay Stage 10 nhặt Lv 10), biến đếm nâng cấp đều khởi tạo từ $0/3$.
  - Súng nhặt ở Stage 1 (Lv 1): Nâng 3 lần tối đa lên **Level 4** ($[3/3]$).
  - Súng nhặt ở Stage 5 (Lv 5): Nâng 3 lần tối đa lên **Level 8** ($[3/3]$).
  - Súng mua ở Shop Stage 1 (Lv 3): Nâng 3 lần tối đa lên **Level 6** ($[3/3]$).
- Khi đã đạt giới hạn $3/3$ lần, Bàn Nâng Cấp tại Shop sẽ từ chối giao dịch và hiển thị thông báo:
  `"VŨ KHÍ ĐÃ ĐẠT GIỚI HẠN NÂNG CẤP (3/3 LẦN)! KHÔNG THỂ NÂNG THÊM"`

---

## 4. Cân Bằng Kinh Tế và Định Giá Shop

Mô hình định giá tuân thủ chặt chẽ nguyên lý so sánh chi phí:
- **Phí nâng cấp tại Bàn Nâng Cấp**: **80 Credits / lần** (+1 cấp và +1★ ngẫu nhiên).
- **Mua súng tại Shop**:
  $$\text{Nâng 1 cấp (80C)} < \text{Mua súng Shop chênh +2 cấp (135C)} < \text{Nâng 2 cấp (160C)}$$
- Giúp người chơi cân nhắc giữa:
  1. Giữ súng quen tay nâng tối đa 3 lần.
  2. Dành tiền mua súng Shop có level khởi điểm cao hơn nhiều cấp rồi tiếp tục nâng cấp thêm 3 lần nữa!

---

## 5. Giao Diện Hiển Thị Trực Quan (UI / UX)

### 5.1 Khung Tooltip Khi Tiếp Cận Súng
Khi người chơi đi lại gần khẩu súng lộ diện trên miệng hòm `WeaponChest` hoặc trên bệ `ShopItem`, một ô UI Tooltip tự động hiện lên:
- **Tiêu đề**: Tên súng và cấp độ (VD: `★ AR I ★`, `★ SHOTGUN III ★`).
- **3 hàng sao vàng kim**:
  - `• Sát thương:  ★ ★ ☆  (19 DMG)`
  - `• Tốc ra đạn:  ★ ☆ ☆`
  - `• Chính xác:   ★ ☆ ☆`
- **Thanh tương tác**: `[F] NHẶT TRANG BỊ` hoặc `[F] MUA SÚNG (135 CREDITS)`.

### 5.2 Giao diện Balo Chiến thuật ([Tab])
Trong `BackpackUI`, các thẻ vũ khí chính hiển thị đầy đủ tên cấp độ (VD: `AR I`, `SMG III`), 3 hàng sao hiển thị trực quan thay vì chỉ số tĩnh.

---

## 6. Sửa đổi / Cập nhật

- **16/10/2026**:
  - Xây dựng hệ thống cấp độ (Level I, II, III) và chỉ số sao vào `Weapon.java`.
  - Cập nhật công thức toán học tính toán sát thương, tốc độ bắn và độ tản xạ theo sao.
  - Đồng bộ `AssaultRifle`, `Shotgun`, `SMG`, `Pistol` với cơ chế sao.
  - Cập nhật hòm tiếp tế `WeaponChest` và quầy hàng `ShopItem` với UI Tooltip 3 hàng sao.
  - Bổ sung bàn nâng cấp vũ khí `createUpgradeStation(80C)` trong phòng Shop.
