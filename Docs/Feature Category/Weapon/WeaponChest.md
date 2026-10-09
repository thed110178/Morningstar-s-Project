Cập nhật mới nhất vào ngày: 09/10/2026

# Hòm Tiếp Tế Vũ Khí (Weapon Chest)

## 1. Mô tả
Hòm tiếp tế vũ khí (`WeaponChest`) là cơ chế trao thưởng cho người chơi sau khi dọn sạch (clear) toàn bộ kẻ địch trong phòng chiến đấu (`Combat Arena`).
Khởi đầu nhiệm vụ, người chơi chỉ được trang bị súng lục chiến thuật (`Pistol` - ô 3) và dao găm (`CombatKnife` - ô 4). Hai ô vũ khí chính (`[1] AR` và `[2] Shotgun`) chưa được kích hoạt.
Khi căn phòng được giải phóng khỏi trạng thái phong tỏa (Combat Lockdown), một hòm vũ khí mạ vàng sẽ rơi ra ở chính giữa phòng.
- **Tự động mở nắp (`Auto-Open on Approach`)**: Khi người chơi tiến lại gần hòm (bán kính $\le 58\text{px}$), nắp hòm sẽ **TỰ ĐỘNG BẬT MỞ** và làm lộ diện khẩu súng bên trong (vẽ súng nổi lơ lửng kèm hào quang lấp lánh và nhãn tên vũ khí rõ ràng).
- **Phím tương tác chính [F]**: Người chơi nhấn phím **[F]** để nhặt vũ khí vào trang bị.

---

## 2. Quy trình hoạt động
```
[Bắt đầu màn chơi]
       │
       ▼
Chỉ có Súng lục [3] & Dao [4] (Slot 1 & Slot 2 trống)
       │
       ▼
Tiêu diệt toàn bộ mục tiêu trong Combat Arena
       │
       ▼
Cửa mở (Unlock) ──> Hòm tiếp tế (WeaponChest) rơi tại tâm phòng (Đang đóng nắp)
       │
       ▼
Người chơi di chuyển lại gần hòm (Bán kính <= 58px)
       │
       ├──> Nắp hòm TỰ ĐỘNG BẬT MỞ
       ├──> Khẩu súng (AR hoặc Shotgun) nổi lơ lửng trên miệng hòm với hào quang rực rỡ
       ├──> Hiện nhãn tên súng và lời nhắc: "[F] NHẶT [TÊN VŨ KHÍ]"
       │
       ▼
Người chơi bấm phím tương tác chính [F]
       │
       ├──> Súng được nhặt vào trang bị (ưu tiên lấp vào Slot 1, nếu Slot 1 có súng thì vào Slot 2)
       ├──> Lập tức cầm súng vừa nhặt trên tay kèm thông báo biểu ngữ chiến thuật
       └──> Hòm chuyển sang trạng thái rỗng đã nhặt xong
```

---

## 3. API & Các thuộc tính chính

### 3.1 Constructor
* `public WeaponChest(double x, double y)`: Khởi tạo hòm tại tọa độ thế giới (world coordinates).

### 3.2 Phương thức
* `public Weapon open()`: Mở hòm và ngẫu nhiên trả về một thực thể `AssaultRifle` hoặc `Shotgun` (50/50).
* `public void update(double deltaTime, double playerX, double playerY, double playerSize)`: Cập nhật nhấp nhô lơ lửng và tự động kích hoạt `open()` khi người chơi bước vào bán kính tương tác.
* `public Weapon collect()`: Thu thập vũ khí vào túi đồ khi người chơi nhấn phím **[F]**, đánh dấu `collected = true` và trả về thực thể vũ khí.
* `public boolean isPlayerInRange(double px, double py, double pSize)`: Xác định người chơi có nằm trong bán kính tương tác ($\le 58\text{px}$) hay không.
* `public void render(Graphics2D g, Camera camera, boolean showPrompt)`: Vẽ hòm gỗ mạ vàng, ánh sáng phát quang, mô hình súng nổi lơ lửng và khung lời nhắc phím [F].
* `public boolean isOpened()`: Trả về trạng thái hòm đã mở nắp hay chưa.
* `public boolean isCollected()`: Trả về trạng thái súng trong hòm đã được nhặt hay chưa.
* `public Weapon getDroppedWeapon()`: Trả về vũ khí đã sinh ra từ hòm.

---

## 4. Inspector / Tinh chỉnh thông số
| Thuộc tính | Giá trị mặc định | Giải thích |
|---|---|---|
| `size` | 38x38 px | Kích thước hitbox hình hộp của hòm |
| `Bán kính tương tác` | 58 px | Khoảng cách tối đa để hòm tự mở và bấm [F] nhặt súng |
| `Vũ khí rơi ra` | AR hoặc Shotgun | Tỉ lệ 50/50 ngẫu nhiên giữa 2 loại vũ khí chính |
| `Hiệu ứng lơ lửng` | Sin-wave $3.5 \times \text{deltaTime}$ | Nhấp nhô $\pm 4\text{px}$ tạo cảm giác vật phẩm thưởng nổi bật |
| `Phím nhặt` | `[F]` | Phím tương tác chính thống nhất toàn game |

---

## 5. Quan hệ với hệ thống khác
* **`PlayingState`**: Khi dọn sạch phòng, sinh `WeaponChest`. Gọi `chest.update(...)` để tự mở khi người chơi đến gần. Bắt sự kiện phím **`[F]`** để gọi `chest.collect()` và trang bị vào `weaponManager`.
* **`WeaponManager`**: Nhận thực thể vũ khí từ `chest.collect()`, gọi `weaponManager.equipWeapon(dropped)` để xếp vào slot trống và cầm ngay trên tay.
* **`BackpackUI`**: Cho phép người chơi bấm **[Tab]** xem khẩu súng vừa nhặt và đổi chỗ giữa Slot 1 và Slot 2 nếu muốn.

---

## 6. Sửa đổi / Cập nhật
* **09/10/2026**:
  - Chuyển đổi phím tương tác chính từ [E] sang **[F]** (đồng bộ với phím sơ tán [F]).
  - Cập nhật cơ chế: Khi người chơi đi ra chỗ hòm, hòm **tự động mở nắp** và hiện ra mô hình/tên khẩu súng lơ lửng phía trên miệng hòm.
  - Người chơi bấm **[F]** để nhặt vũ khí vào Balo.
* **06/10/2026**:
  - Tạo mới hệ thống hòm tiếp tế vũ khí.
