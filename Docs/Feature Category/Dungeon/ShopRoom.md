Cập nhật mới nhất vào ngày: 16/10/2026

# Phòng Cửa Hàng Chiến Thuật (Tactical Field Shop Room)

---

## 1. Mô tả

**Phòng Cửa Hàng Chiến Thuật (Shop Room)** là căn phòng an toàn chuyên biệt được kiến tạo ngẫu nhiên tại **bất kỳ vị trí nào trên bản đồ hầm ngục** (có thể rẽ nhánh từ phòng Combat, phòng Extract hoặc phòng Spawn, không bị cố định phải dính liền với phòng Spawn) ở các Stage thường (Stage 1, 2) và bố trí tại vị trí chiến lược trong Stage 3 (Grand Boss Arena).

Shop Room cung cấp trạm dừng chân an toàn để người chơi chi tiêu **Credits** (tiền tệ kiếm được khi tiêu diệt mục tiêu và dọn sạch phòng chiến đấu) nhằm nâng cấp hỏa lực và tiếp tế tài nguyên:
1. **Quầy Súng Vượt Cấp (Over-leveled Weapon Stall)**: Trưng bày ngẫu nhiên một trong các loại vũ khí chính với cấp độ bằng `Stage + 2` (80%) giá 135 Credits, hoặc đột biến `Stage + 3` (20%) giá 170 Credits. Khi mua mà 2 slot chính đã đầy, vũ khí cũ sẽ **rơi xuống đất ngay tại chân người chơi** để phòng ngừa mua nhầm.
2. **Bàn Nâng Cấp Vũ Khí (Upgrade Station)**: Nâng cấp súng đang cầm +1 cấp và +1★ ngẫu nhiên (tối đa 3 lần/súng) với giá 80 Credits/lần.
3. **Quầy Tiếp Tế Đạn (Ammo Resupply Depot)**: Giá 70 Credits. Khi tương tác `[F]`, toàn bộ vũ khí có trong kho đồ của người chơi sẽ được nạp đầy 100% băng đạn ngay lập tức và hủy mọi trạng thái nạp dở.
4. **Quầy Hồi Phục HP (Health Pack Stall)**: Giá dự kiến 90 Credits. Đã được cấu hình trong hệ thống nhưng theo yêu cầu thiết kế hiện tại, do hệ thống máu người chơi chưa phát triển nên quầy này được **ẩn hoàn toàn (`available = false`)**, không vẽ ra màn hình và không cho phép tương tác.

---

## 2. Quy trình hoạt động

```
[Tiêu diệt Dummy (20% rơi 3-8 Credits) / Clear Phòng (+15 Credits)]
                               │
                               ▼
              [Tích lũy số dư trong Ví Credits]
                               │
                               ▼
              [Người chơi di chuyển vào Shop Room]
                               │
                 ┌─────────────┴─────────────┐
                 ▼                           ▼
       [Quầy Súng Chính]            [Quầy Tiếp Tế Đạn]
       - AR: 180 Credits            - 70 Credits
       - Shotgun: 150 Credits       - Hồi đầy đạn tất cả vũ khí
       - SMG: 165 Credits
                 │                           │
                 └─────────────┬─────────────┘
                               ▼
               [Đứng trong phạm vi tương tác (<= 55px)]
                               │
                               ▼
               [Bấm phím [F] để thực hiện giao dịch]
                 ├── Đủ tiền: Trừ Credits, nhận đồ, treo biển [SOLD OUT]
                 └── Thiếu tiền: Báo Banner đỏ "KHÔNG ĐỦ CREDITS!"
```

### Đặc điểm thiết kế an toàn:
* **Không phong tỏa (No Lockdown)**: Cửa của Shop Room không bao giờ bị khóa kín trong chiến đấu. Người chơi có thể tự do di chuyển ra vào.
* **Không sinh kẻ địch**: Tuyệt đối không sinh Dummy trong Shop Room.
* **Hiệu ứng trực quan công nghệ cao**: Bệ đứng với vòng hào quang vàng kim lơ lửng, mô hình vũ khí/hộp đạn 2D nhấp nhô tuần hoàn theo thời gian (`Math.sin`), nhãn giá gắn huy hiệu chiến thuật và bảng phím tương tác `[F]`.

---

## 3. API

### 3.1 `Room.RoomType`
* Thêm loại phòng mới: `Room.RoomType.SHOP`.
* Phương thức: `room.isShopRoom() -> boolean`.

### 3.2 `ShopItem` (`com.groupproject.game.entity.shop.ShopItem`)
* `createWeaponItem(double x, double y, Weapon weapon, int price) -> ShopItem`: Khởi tạo quầy bán súng chính.
* `createAmmoItem(double x, double y, int price) -> ShopItem`: Khởi tạo quầy tiếp tế đạn.
* `createHealthItem(double x, double y, int price) -> ShopItem`: Khởi tạo quầy hồi máu HP (mặc định `available = false`).
* `isPlayerInRange(double px, double py, double pSize) -> boolean`: Kiểm tra cự ly tương tác.
* `purchase(PlayingState state) -> boolean`: Thực hiện giao dịch mua bán, trừ tiền và cấp trang bị.
* `render(Graphics2D g, Camera camera, boolean showPrompt)`: Hiển thị bệ trưng bày, mô hình nổi và bảng tương tác.

### 3.3 `Weapon` & `WeaponManager`
* `weapon.resupplyAmmo()`: Nạp đầy băng đạn và hủy trạng thái nạp dở của vũ khí.
* `weaponManager.resupplyAllWeapons()`: Nạp đầy đạn cho tất cả súng hiện có trong kho đồ.

### 3.4 `PlayingState`
* `getPlayerCredits() -> int`: Lấy số dư Credits hiện tại.
* `addCredits(int amount)`: Cộng thêm tiền tệ.
* `deductCredits(int amount) -> boolean`: Trừ tiền tệ nếu đủ số dư.
* `getShopItems() -> List<ShopItem>`: Lấy danh sách quầy hàng hiện diện trong stage.

---

## 4. Inspector Settings / Hằng số cấu hình (Cân bằng mới)

| Thông số | Giá trị | Giải thích |
|---|---|---|
| `SHOP_ROOM_WIDTH` | 14 tiles (Stage 1-2) / 12 tiles (Stage 3) | Kích thước ngang của phòng Shop |
| `SHOP_ROOM_HEIGHT` | 10 tiles | Kích thước dọc của phòng Shop |
| Giá Súng Shop (Chênh +2 cấp) | **135 Credits** | Level = Stage + 2 (Tỉ lệ 80%), ngẫu nhiên AR/Shotgun/SMG |
| Giá Súng Shop (Chênh +3 cấp) | **170 Credits** | Level = Stage + 3 (Tỉ lệ 20%), đột biến chỉ số cực mạnh |
| Phí Bàn Nâng Cấp | **80 Credits / lần** | Nâng cấp súng đang cầm +1 cấp và +1★ ngẫu nhiên (Tối đa 3 lần/súng) |
| Giá Tiếp tế Đạn | **70 Credits** | Hồi phục 100% đạn cho toàn bộ vũ khí (Cân bằng lại giá) |
| Khởi đầu Run | **0 Credits** | Bắt đầu không có tiền, người chơi tự cày cuốc trong ải |
| Thưởng diệt mục tiêu | **20% tỉ lệ rơi 3 - 8 Credits** | Chỉ tỉ lệ nhỏ quái chết mới rơi tiền |
| Thưởng dọn phòng | **+15 Credits** (Boss **+25 Credits**) | Giảm đáng kể tiền thưởng clear phòng để tránh lạm phát |

---

## 5. Quan hệ với hệ thống khác

* **`DungeonMap`**: Đảm nhiệm sinh phòng Shop trên lưới kết nối 4 hướng, chạm trổ hành lang và lắp cửa mở tự do.
* **`WeaponManager`**: Nhận vũ khí mua mới qua phương thức `equipWeapon(...)` và nạp đạn qua `resupplyAllWeapons()`.
* **`BackpackUI`**: Hiển thị vũ khí mua được từ Shop ngay trong kho đồ Balo chiến thuật.
* **`Camera`**: Chuyển đổi tọa độ thế giới sang tọa độ màn hình để vẽ quầy hàng và nhãn tương tác.

---

## 6. Sửa đổi / Cập nhật
* **16/10/2026 (Đợt 4 - Thống nhất Quy tắc Cấp độ Súng Shop & Giới hạn 3 lần nâng)**:
  - Súng bán tại Shop có cấp độ bằng `Stage + 2`, với tỉ lệ nhỏ (~20%) đột biến lên `Stage + 3`.
  - Giới hạn Bàn Nâng Cấp: Mỗi khẩu súng độc lập chỉ được phép nâng cấp tối đa **3 lần** (`upgradeCount < 3`), từ chối khi đạt 3/3 lần.
  - Cập nhật hiển thị số lần nâng cấp trên thẻ Tooltip và giao diện Balo chiến thuật.
* **16/10/2026 (Đợt 3 - Bàn Nâng Cấp & Súng Level III)**:
  - Bổ sung **Bàn Nâng Cấp Vũ Khí (Weapon Upgrade Terminal)** tại tâm phòng Shop: Phí **80 Credits** để nâng cấp vũ khí đang cầm trên tay lên 1 cấp (từ I lên II, hoặc từ II lên III), tự động roll ngẫu nhiên $+1★$ vào Sát thương, Tốc ra đạn hoặc Độ chính xác.
  - Quầy bán súng chính bán trực tiếp **Vũ khí Level III ngẫu nhiên** với giá **135 Credits** (sở hữu tổng 5 sao).
  - Áp dụng nguyên lý định giá kinh tế nghiêm ngặt:
    $$\text{Nâng 1 cấp (80C)} < \text{Mua súng Level III (135C)} < \text{Nâng 2 cấp (160C)}$$
  - Hiển thị Tooltip HUD 3 hàng sao cho súng Level III và bàn nâng cấp khi người chơi tiếp cận.
* **16/10/2026 (Đợt 2 - Cân bằng kinh tế)**:
  - Cân bằng lại toàn diện kinh tế: Tăng giá bán súng và đạn (AR 180, Shotgun 150, SMG 165, Ammo 70).
  - Khởi đầu lượt chơi với 0 Credits thay vì 100 Credits.
  - Quái chết chỉ có tỉ lệ nhỏ (~20%) rơi từ 3 đến 8 Credits.
  - Bớt tiền thưởng khi clear phòng: từ 40 Credits xuống còn 15 Credits (phòng Boss là 25 Credits).
* **16/10/2026 (Đợt 1)**:
  - Khởi tạo phòng `SHOP` trong hầm ngục thủ tục (Stage 1, 2) và hầm ngục Boss (Stage 3).
  - Triển khai lớp quầy hàng `ShopItem` với hai mặt hàng mở bán: Súng chính ngẫu nhiên và Tiếp tế đạn.
  - Quầy HP được thiết kế sẵn sàng trong code nhưng đặt trạng thái ẩn hoàn toàn (`available = false`) chờ phát triển hệ thống máu người chơi.

---

## 7. Lỗi đã sửa
* **Tránh xung đột phím [F]**: Khi đứng gần quầy hàng Shop và bấm [F], cờ `shopInteracted` được kích hoạt để ngăn chặn việc người chơi vô tình kích hoạt chuyển stage nếu quầy hàng đặt gần lối đi hoặc bệ sơ tán.
* **Bảo vệ tình trạng nạp đạn khi mua tiếp tế**: Gọi `resupplyAmmo()` sẽ tự động đặt lại `isReloading = false` và `reloadTimer = 0`, ngăn lỗi súng bị kẹt trong trạng thái nạp đạn vô tận nếu người chơi mua tiếp tế đúng lúc đang nạp đạn dở.
