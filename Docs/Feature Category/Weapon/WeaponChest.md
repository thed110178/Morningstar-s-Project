Cập nhật mới nhất vào ngày: 16/10/2026

# Hòm Tiếp Tế Chiến Lợi Phẩm (Reward Chest / Weapon Chest)

---

## 1. Mô tả
Hòm tiếp tế chiến lợi phẩm (`WeaponChest`) là cơ chế trao thưởng cho người chơi sau khi dọn sạch (clear) toàn bộ kẻ địch trong một phòng chiến đấu (`Combat Arena` hoặc `Grand Boss Arena`).

Khởi đầu nhiệm vụ, người chơi được trang bị súng lục chiến thuật (`Pistol` - ô 3) và dao găm (`CombatKnife` - ô 4), cùng hai ô vũ khí chính (`[1]` và `[2]`).
Khi căn phòng được giải phóng khỏi trạng thái phong tỏa (Combat Lockdown), một hòm tiếp tế mạ vàng sẽ rơi ra bên trong phòng:
- **Tự động mở nắp (`Auto-Open on Approach`)**: Khi người chơi tiến lại gần hòm (bán kính $\le 58\text{px}$), nắp hòm sẽ **TỰ ĐỘNG BẬT MỞ** và làm lộ diện chiến lợi phẩm bên trong (nổi lơ lửng kèm hào quang lấp lánh và nhãn tên vật phẩm rõ ràng).
- **Phân loại phần thưởng ngẫu nhiên (Tiền hoặc Súng)**: Hòm trao thưởng 1 trong 2 loại tài nguyên chiến thuật:
  1. **Tiền tệ (Credits)**: **Tỉ lệ ra cao hơn (~70%)**, nhận từ 35 đến 65 Credits. Khi mở hòm, xuất hiện mô hình đống đồng xu vàng kim lấp lánh với chữ "C" nổi bật.
  2. **Súng chính (Primary Weapon)**: **Tỉ lệ ra thấp hơn (~30%)**, ngẫu nhiên 1 trong 3 loại vũ khí chính: `AssaultRifle`, `Shotgun`, hoặc `SMG`. Khi mở hòm, xuất hiện mô hình súng 2D nổi lơ lửng.
- **Phím tương tác chính [F]**: Người chơi nhấn phím **[F]** để thu thập phần thưởng (cộng Credits vào ví hoặc trang bị súng vào kho đồ).

---

## 2. Quy trình hoạt động
```
[Tiêu diệt toàn bộ mục tiêu trong Combat Arena]
                       │
                       ▼
[Cửa mở phong tỏa] ──> Hòm tiếp tế (WeaponChest) rơi tại phòng
                       │
                       ▼
[Người chơi di chuyển lại gần hòm (Bán kính <= 58px)]
                       │
                       ├──> Nắp hòm TỰ ĐỘNG BẬT MỞ
                       │
         ┌─────────────┴─────────────┐
         ▼                           ▼
[Ra Tiền Credits (~70%)]    [Ra Súng Chính (~30%)]
- Thưởng: 35-65 Credits     - AR / Shotgun / SMG
- Mô hình: Đồng xu vàng C   - Mô hình: Khẩu súng 2D
- Nhãn: "[F] NHẶT +XC"      - Nhãn: "[F] NHẶT [SÚNG]"
         │                           │
         └─────────────┬─────────────┘
                       ▼
[Người chơi bấm phím tương tác [F]]
  ├── Nhận tiền: Thưởng tiền vào ví Credits, hiện Banner vàng
  └── Nhận súng: Trang bị vào Slot 1/2, hiện Banner chiến thuật
  └── Hòm chuyển sang trạng thái rỗng đã nhặt xong
```

---

## 3. API & Các thuộc tính chính

### 3.1 `ChestRewardType` Enum
* `CREDITS`: Phần thưởng tiền tệ.
* `WEAPON`: Phần thưởng vũ khí chính.

### 3.2 Constructor & Factory Methods
* `public WeaponChest(double x, double y)`: Khởi tạo hòm tại tọa độ thế giới với tỉ lệ mặc định 70% Tiền, 30% Súng.
* `public static WeaponChest createWeaponChest(double x, double y, Weapon weapon)`: Khởi tạo hòm chỉ định vũ khí.
* `public static WeaponChest createCreditsChest(double x, double y, int credits)`: Khởi tạo hòm chỉ định số tiền.

### 3.3 Phương thức
* `public void open()`: Mở hòm và sinh ngẫu nhiên phần thưởng theo tỉ lệ (70% Credits 35-65, 30% Weapon AR/Shotgun/SMG).
* `public void update(double deltaTime, double playerX, double playerY, double playerSize)`: Cập nhật nhấp nhô lơ lửng và tự động kích hoạt `open()` khi người chơi bước vào bán kính tương tác.
* `public boolean collectReward(PlayingState state)`: Thu thập phần thưởng khi bấm **[F]**, cộng tiền hoặc trang bị súng, đánh dấu `collected = true` và hiển thị thông báo chiến thuật.
* `public Weapon collect()`: Thu thập vũ khí (hỗ trợ tương thích ngược).
* `public boolean isPlayerInRange(double px, double py, double pSize)`: Xác định người chơi có nằm trong bán kính tương tác ($\le 58\text{px}$) hay không.
* `public void render(Graphics2D g, Camera camera, boolean showPrompt)`: Vẽ hòm mạ vàng, hào quang lơ lửng, mô hình đồng xu hoặc súng, và khung lời nhắc phím [F].
* `public boolean isOpened()`: Trạng thái hòm đã mở nắp hay chưa.
* `public boolean isCollected()`: Trạng thái đã thu thập phần thưởng hay chưa.
* `public ChestRewardType getRewardType()`: Loại phần thưởng trong hòm.
* `public int getCreditsReward()`: Số tiền thưởng (nếu là Credits).
* `public Weapon getDroppedWeapon()`: Vũ khí thưởng (nếu là Weapon).

---

## 4. Inspector / Tinh chỉnh thông số

| Thuộc tính | Giá trị mặc định | Giải thích |
|---|---|---|
| `size` | 38x38 px | Kích thước hitbox hình hộp của hòm |
| `Bán kính tương tác` | 58 px | Khoảng cách tối đa để hòm tự mở và bấm [F] thu thập |
| `Tỉ lệ Tiền (Credits)` | **70%** | Xác suất hòm mở ra tiền tệ chiến thuật |
| `Khoảng Tiền thưởng` | 35 đến 65 Credits | Số tiền nhận được khi mở hòm trúng Tiền |
| `Tỉ lệ Súng (Weapon)` | **30%** | Xác suất hòm mở ra vũ khí chính (AR, Shotgun, SMG) |
| `Hiệu ứng lơ lửng` | Sin-wave $3.5 \times \text{deltaTime}$ | Nhấp nhô $\pm 4\text{px}$ tạo cảm giác vật phẩm nổi bật |
| `Phím tương tác` | `[F]` | Phím tương tác chính thống nhất toàn game |

---

## 5. Quan hệ với hệ thống khác
* **`PlayingState`**: Khi dọn sạch phòng, sinh `WeaponChest`. Gọi `chest.update(...)` để tự mở khi người chơi đến gần. Bắt sự kiện phím **`[F]`** để gọi `chest.collectReward(this)`.
* **`WeaponManager`**: Nhận thực thể vũ khí khi hòm chứa súng, xếp vào slot trang bị phù hợp.
* **`BackpackUI`**: Hiển thị vũ khí nhặt được từ hòm trong Balo chiến thuật.
* **Ví tiền Credits (`PlayingState`)**: Nhận số tiền thưởng trực tiếp khi hòm chứa Credits.

---

## 6. Sửa đổi / Cập nhật
* **16/10/2026 (Thống nhất Cấp độ Súng Rơi Phụ Thuộc Vào Số Stage)**:
  - **Quy tắc Stage = Level**: Stage nào thì cho súng free khi clear room ở Level như thế:
    - Stage 1: Rơi súng Level 1 (phòng combat đầu tiên chắc chắn 100% rơi súng free).
    - Stage 2: Rơi súng Level 2.
    - Stage 100: Rơi súng Level 100.
    - Công thức sao: $3 + (\text{stage} - 1)$ sao ngẫu nhiên.
  - Súng khi rơi khởi tạo số lần nâng cấp `upgradeCount = 0` (cho phép nâng tối đa 3 lần tại Shop).
  - Tích hợp **Ô UI Tooltip hiển thị 3 hàng sao kèm số lần nâng cấp** (`renderWeaponStatCard`) ngay phía trên khẩu súng.
* **16/10/2026 (Cân đối Phần thưởng Hòm qua Room)**:
  - **Đảm bảo ở Stage 1 luôn cho 1 cây súng free**: Phòng combat đầu tiên được dọn sạch trong Stage 1 chắc chắn 100% rơi 1 cây súng chính Level 1 free (`guaranteedWeapon = true`) để người chơi có vũ khí chính tác chiến.
  - **Các phòng tiếp theo và các stage khác**: Do tỉ lệ quyết định, trong đó **tỉ lệ ra Tiền thưởng cao hơn hẳn Súng** (~75% Tiền nhận từ 20-35 Credits, ~25% Súng).
* **09/10/2026**:
  - Chuyển đổi phím tương tác chính từ [E] sang **[F]**.
  - Hòm tự động mở nắp khi người chơi lại gần.
* **06/10/2026**:
  - Tạo mới hệ thống hòm tiếp tế vũ khí ban đầu.
