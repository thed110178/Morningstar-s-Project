Cập nhật mới nhất vào ngày: 01/11/2026

# Hệ thống Đạn Dược Dự Trữ (Reserve Ammo System)

---

## 1. Mô tả
Hệ thống Đạn Dược Dự Trữ quản lý việc phân loại, tích trữ, tiêu hao và nhặt đạn cho toàn bộ các chủng loại vũ khí trong Project DontKnow:
* **Súng lục phụ (Pistol)**: Vô hạn đạn dự trữ (`∞`). Vẫn giữ cơ chế thay băng đạn 7 viên (1.2s nạp) nhưng không bao giờ cạn đạn trong túi.
* **Vũ khí cận chiến (Combat Knife)**: Không dùng đạn (`∞ MELEE`).
* **Vũ khí chính (Súng trường AR, SMG, Shotgun, Súng bắn tỉa SR)**:
  - Bị ràng buộc nghiêm ngặt bởi kho đạn dự trữ riêng biệt cho từng chủng loại.
  - Khi bắn: Tiêu hao đạn trong băng (`currentAmmo`).
  - Khi nạp đạn (`[R]` hoặc tự động khi hết băng): Rút đạn từ kho dự trữ để nạp vào băng. Nếu kho dự trữ hết đạn (0 viên), súng không thể nạp tiếp.

---

## 2. Thông số Sức chứa & Định mức Stack Đạn

| Chủng loại đạn | Loại súng sử dụng | Sức chứa tối đa (Max Capacity) | Định mức 1 Stack chiến lợi phẩm | Màu sắc nhận diện |
| :--- | :--- | :--- | :--- | :--- |
| **Đạn súng trường (Rifle ammo)** | Assault Rifle (AR) | **150 viên** | **30 viên** | Xanh lục (#50DC64) |
| **Đạn SMG** | Submachine Gun (SMG) | **180 viên** | **35 viên** | Xanh cyan (#00D2FF) |
| **Đạn Shotgun** | Shotgun | **50 viên** | **10 viên** | Đỏ cam (#FF5A46) |
| **Đạn bắn tỉa SR (Sniper ammo)** | Sniper Rifle (SR) | **30 viên** | **5 viên** | Tím điện quang (#B950FF) |

* **Trạng thái ban đầu**: Bắt đầu lượt chơi mới (Stage 1), kho đạn dự trữ khởi đầu với **0 viên** cho cả 4 loại. Người chơi phải dùng súng lục ban đầu và tiêu diệt quái / dọn phòng để gom nhặt đạn súng chính.

---

## 3. Quy trình Rơi & Thu thập Đạn (Ammo Pickups)

### 3.1 Rơi đạn khi tiêu diệt mục tiêu (quái)
* Khi một mục tiêu hoặc kẻ địch bị hạ gục:
  - Có tỉ lệ **~35%** rơi ra một hộp đạn nhỏ (`AmmoPickup`) ngay tại tọa độ mục tiêu tử trận.
  - Chủng loại rơi: Ngẫu nhiên 1 trong 4 loại (Súng trường, SMG, Shotgun, Bắn tỉa SR).
  - Lượng đạn nhỏ:
    * Súng trường (AR): $6 - 10$ viên.
    * SMG: $8 - 14$ viên.
    * Shotgun: $2 - 4$ viên.
    * Bắn tỉa SR: $2 - 4$ viên.

### 3.2 Thưởng đạn khi dọn sạch phòng chiến đấu (Room Clearance)
* Khi operative dọn sạch toàn bộ quái trong phòng chiến đấu:
  - Hệ thống tự động rơi ra **2 đơn vị stack của 2 loại đạn ngẫu nhiên** (chọn độc lập, có thể trùng loại).
  - **Bố cục đối xứng hoàn hảo**: Hòm tiếp tế `WeaponChest` nằm ở chính giữa trung tâm phòng (`centerX, centerY`). Hai stack đạn rơi đối xứng 2 bên tại `centerX - 58px` (Trái) và `centerX + 58px` (Phải) trên cùng đường ngang, tạo bố cục cân đối và dễ quan sát.
  - Số lượng chuẩn theo stack: Súng trường: 30 viên; SMG: 35 viên; Shotgun: 10 viên; Bắn tỉa SR: 5 viên.

### 3.3 Cơ chế Hút/Nhặt đạn tự động & Giữ lại đạn thừa dưới đất (Partial Ammo Collection & Ground Retention)
* Hộp đạn trên mặt đất hiển thị dưới dạng crate quân sự 2D với vạch sơn đặc trưng, hào quang neon nhấp nhô lơ lửng (bobbing effect) và nhãn số lượng bên trên (VD: `+30 AR`, `+5 SR`).
* Khi người chơi di chuyển lại gần (bán kính tiếp cận $\le 38$ pixel):
  - Nếu kho đạn của loại tương ứng chưa đầy:
    * Hệ thống kiểm tra khoảng trống còn lại trong balô: `added = inventory.addAmmo(type, amount)`.
    * **Nếu ba lô đủ chỗ chứa toàn bộ (`added >= amount`)**: Nhận trọn vẹn số đạn, đánh dấu `collected = true` và hộp đạn biến mất.
    * **Nếu ba lô bị đầy một phần (`added < amount`)**: Ba lô chỉ nạp số đạn vừa đủ để đạt trần tối đa, hộp đạn **KHÔNG bị biến mất** (`collected = false`), số lượng trên hộp đạn được trừ bớt (`amount -= added`) và giữ lại số đạn dư thừa nguyên vẹn trên mặt đất. Nhãn số lượng trên hộp đạn tự động cập nhật động theo số dư còn lại (VD: từ `+30 AR` còn `+20 AR`).
    * Khi người chơi xả bớt đạn và quay lại nhặt, phần đạn thừa sẽ tiếp tục được thu gom.
  - Hiển thị thông báo chiến thuật: `★ ĐÃ NHẶT +X ĐẠN [Y/MAX] ★`.

### 3.4 Thu hồi đạn khi hoán đổi vũ khí & Rơi đạn thừa ra sàn (Weapon Swap Ammo Reclaim)
* Khi người chơi hoán đổi hoặc mua súng chính mới làm thay thế khẩu súng cũ trong túi đồ:
  - **Thu hồi đạn trong băng về balô**: Toàn bộ số đạn còn lại trong băng của súng cũ (`currentAmmo`) được chuyển thẳng về kho đạn dự trữ trong balô (`AmmoInventory`) của người chơi (`reclaimAmmoFromWeapon`).
  - **Rơi đạn thừa ra sàn đấu**: Nếu kho dự trữ của loại đạn đó đã đạt trần sức chứa (Max Capacity), số đạn dôi dư (`surplus`) sẽ tự động rơi xuống mặt đất ngay tại vị trí hoán đổi dưới dạng một hộp tiếp đạn `AmmoPickup`.
  - **Chống gian lận nhân bản đạn (Duplication Exploit Prevention)**: Khẩu súng cũ rơi ra sàn/đặt vào hòm sẽ được rút đạn trong băng về 0 (`currentAmmo = 0`), ngăn chặn việc hoán đổi qua lại liên tục để farm đạn vô tận.
  - Hiển thị thông báo chi tiết trên Banner chiến thuật (VD: `★ ĐÃ TRANG BỊ: VIPER AR | VŨ KHÍ CŨ (BULLPUP SHOTGUN) ĐÃ ĐẶT VÀO HÒM [+6 ĐẠN BĂNG VỀ BALÔ] [RƠI 2 ĐẠN THỪA] ★`).

---

## 4. Gói Tiếp tế Đạn tại Shop (`ShopItemType.AMMO`)
* Quầy tiếp tế đạn tại Tactical Shop Room:
  - Cung cấp **+2 stack cho tất cả các loại đạn** trong kho dự trữ: **+60 Súng trường, +70 SMG, +20 Shotgun, +10 Bắn tỉa SR** (giới hạn tối đa tới sức chứa của từng loại).
  - Nạp đầy băng đạn cho tất cả súng hiện có trong túi đồ của người chơi.

---

## 5. Tích hợp Giao diện HUD & Balo

1. **HUD Góc dưới bên trái (`WeaponManager.renderHUD`)**:
   - Súng chính (AR, SMG, Shotgun): Hiển thị định dạng `[Đạn trong băng] / [Đạn dự trữ]  [LOẠI ĐẠN]` (Ví dụ: `30 / 120  [AR]`).
   - Súng hết đạn toàn diện (cả băng lẫn kho đều bằng 0): Hiển thị cảnh báo đỏ rực rỡ `"HẾT ĐẠN! [0 / 0]"`.
   - Súng lục: Hiển thị `"7 / ∞"`.
   - Cận chiến: Hiển thị `"∞ MELEE"`.
2. **Balo chiến thuật (`BackpackUI` - phím `[Tab]`)**:
   - Thêm thanh trạng thái thống kê 3 huy hiệu kho đạn dự trữ:
     * `• ĐẠN AR: X / 150` (Màu xanh lục quân sự)
     * `• ĐẠN SMG: Y / 180` (Màu xanh lơ cyan)
     * `• ĐẠN SHOTGUN: Z / 50` (Màu đỏ cam)
   - Trên mỗi thẻ vũ khí chính: Hiển thị chi tiết đạn băng kèm đạn dự trữ `[Kho: X/Max]`.

---

## 6. Sửa đổi / Cập nhật
* **Ngày 01/11/2026**:
  - **Khắc phục lỗi đạn thừa biến mất khi nhặt đạn lúc balô đầy**: Khi nhặt đạn mà balô đạt tối đa sức chứa, đạn thừa không còn bị biến mất mà được giữ lại nguyên vẹn trên mặt đất (`AmmoPickup.amount -= added`, `collected = false`).
  - **Cơ chế thu hồi đạn băng súng cũ khi thay súng**: Rút toàn bộ số đạn còn lại trong băng súng cũ về kho dự trữ balô. Nếu kho đầy, số đạn thừa tự động rơi xuống sàn dưới dạng `AmmoPickup`. Rút đạn băng súng cũ về 0 để triệt tiêu nhân bản đạn.
* **Ngày 18/10/2026**:
  - Khởi tạo `AmmoType.java`, `AmmoInventory.java`, `AmmoPickup.java`.
  - Cập nhật cơ chế nạp đạn trong `Weapon.java`, `AssaultRifle.java`, `SMG.java`, `Shotgun.java` tiêu hao từ kho dự trữ.
  - Shotgun nạp từng viên, trừ 1 shell/viên và tự động dừng khi cạn kho đạn.
  - Tích hợp rơi đạn khi quái chết và 2 stack ngẫu nhiên khi clear phòng trong `PlayingState.java`.
  - Bổ sung thanh thống kê đạn trong `BackpackUI.java`.
  - Xây dựng bộ kiểm thử `AmmoSystemTest.java` đạt 10/10 test cases passed.
