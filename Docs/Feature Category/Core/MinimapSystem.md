Cập nhật mới nhất vào ngày: 20/10/2026

# Hệ thống Bản Đồ Chiến Thuật Thu Nhỏ (Minimap / Tactical Radar System)

## 1. Mô tả
Hệ thống **Minimap Chiến Thuật (Tactical Radar)** cung cấp một góc nhìn tổng thể thu nhỏ theo thời gian thực về bản đồ hầm ngục (Dungeon) ở góc trên bên phải màn hình chơi (`PlayingState`), ngay dưới bảng số dư Credits tác chiến.

Hệ thống áp dụng cơ chế **Khám phá sương mù chiến thuật tiến trình (Progressive Room Discovery / Fog of War)**:
- Người chơi khi xuất trận ban đầu chỉ thấy căn phòng xuất phát (`Deployment Staging Room - SPAWN`) và các hành lang kết nối trực tiếp với nó.
- Khi người chơi di chuyển bước chân vào bất kỳ căn phòng mới nào (ví dụ `Combat Room 1`), phòng đó lập tức được khám phá, hiển thị rõ màu sắc, loại phòng và biểu tượng đặc trưng.
- **Toàn bộ các hành lang thông tới căn phòng đó sẽ tự động hiện lên trên Minimap**.
- Các căn phòng kề cận (được nối bởi hành lang với phòng đã khám phá) nhưng người chơi chưa bước vào sẽ hiện viền mờ kèm biểu tượng bí ẩn `?`.
- Các căn phòng ở xa chưa có hành lang nối tới sẽ ẩn hoàn toàn trong bóng tối chiến trường (Fog of War).
- Vị trí thực tế của chiến binh Operative được định vị chính xác bằng chấm vàng chanh kèm hiệu ứng vòng xung điện phát quang nhấp nháy (Player Blip & Radar Pulse Ring).

---

## 2. Quy trình hoạt động

### 2.1 Khởi tạo Stage & Trạng thái Ban Đầu
1. Khi hầm ngục được sinh qua `DungeonMap.generateProceduralDungeon()` hoặc `generateBossStage()`:
   - Tất cả các phòng có `discovered = false`, riêng `spawnRoom.setDiscovered(true)`.
   - Danh sách tất cả các hành lang giữa các phòng được lưu trong `activeConnections`.
2. Khi vẽ Minimap:
   - Kiểm tra từng hành lang trong `activeConnections`: nếu `conn.from.isDiscovered() || conn.to.isDiscovered()` $\rightarrow$ Hành lang này được kết xuất lên Minimap.
   - Kiểm tra từng phòng:
     - Nếu `r.isDiscovered() == true` $\rightarrow$ Vẽ phòng sáng với icon và màu nền theo loại phòng.
     - Nếu `r.isDiscovered() == false` và `dungeonMap.isRoomAdjacentToDiscovered(r) == true` $\rightarrow$ Vẽ khung mờ và icon `?`.
     - Ngược lại $\rightarrow$ Không vẽ (ẩn trong sương mù).

### 2.2 Quy trình Cập nhật Khám phá (Real-Time Discovery)
Trong mỗi frame logic tại `PlayingState.update(...)`:
1. Sau khi `player.handleMovement(...)`, hệ thống gọi `dungeonMap.updateDiscovery(player.getCenterX(), player.getCenterY())`.
2. `DungeonMap` kiểm tra xem tọa độ của Operative có nằm trong lòng căn phòng nào (`getRoomAtWorldPoint(...)`) hay không.
3. Nếu người chơi bước vào một căn phòng chưa khám phá:
   - Căn phòng đó được đặt `r.setDiscovered(true)`.
   - Toàn bộ các hành lang nối tới căn phòng đó lập tức thỏa mãn điều kiện `conn.from.isDiscovered() || conn.to.isDiscovered()`, bộc lộ các lối đi trên Minimap.
   - Các phòng kề cạnh ở đầu kia của những hành lang này lập tức thỏa mãn `isRoomAdjacentToDiscovered(...)`, hiển thị viền mờ bí ẩn và dấu `?`.

### 2.3 Bố cục & Tỷ lệ Vẽ (Layout & Scaling)
- **Tọa độ & Kích thước HUD**:
  - `boxX = GameConfig.SCREEN_WIDTH - 176 - 20 = 1084`
  - `boxY = 64` (ngay dưới Credits HUD tại y=20, h=36)
  - `HUD_WIDTH = 176`, `HUD_HEIGHT = 122`
- **Vùng vẽ bản đồ (Viewport)**:
  - `innerX = boxX + 8 = 1092`
  - `innerY = boxY + 24 = 88`
  - `innerW = 160`, `innerH = 90`
- **Tỷ lệ ánh xạ chuẩn xác**:
  - Bản đồ hầm ngục có `COLS = 80`, `ROWS = 45`.
  - Tỷ lệ `scale = 2.0 px / tile` $\rightarrow$ `80 * 2.0 = 160 px`, `45 * 2.0 = 90 px` (khớp 100% kích thước pixel).
  - Phòng tiêu chuẩn 14x10 tiles $\rightarrow$ `28 x 20 px`.
  - Tọa độ người chơi: `px = innerX + (playerCenterX / 32.0) * 2.0`, `py = innerY + (playerCenterY / 32.0) * 2.0`.

---

## 3. API

### 3.1 `Room.java`
- `public boolean isDiscovered()`: Trả về trạng thái đã được người chơi bước chân vào hay chưa.
- `public void setDiscovered(boolean discovered)`: Cập nhật cờ khám phá của phòng.
- `public void reset()`: Đặt lại `discovered = false` khi khởi động lại màn chơi.

### 3.2 `DungeonMap.java`
- `public List<RoomConnection> getActiveConnections()`: Trả về danh sách hành lang kết nối hoạt động trong stage.
- `public Room.RoomConnection.getCorridorTileBounds()`: Trả về `Rectangle` tọa độ và kích thước ô tile của hành lang.
- `public Room getRoomAtWorldPoint(double worldX, double worldY)`: Trả về phòng chứa tọa độ thế giới (hoặc null nếu ở hành lang).
- `public void updateDiscovery(double worldX, double worldY)`: Cập nhật `setDiscovered(true)` cho phòng người chơi đang đứng.
- `public boolean isRoomAdjacentToDiscovered(Room room)`: Kiểm tra phòng có thông hành lang với phòng đã khám phá hay không.

### 3.3 `MinimapHUD.java`
- `public void update(double deltaTime)`: Cập nhật hiệu ứng phát quang nhấp nháy và vệt quét radar.
- `public void render(Graphics2D g, DungeonMap dungeonMap, Player player, int currentStage)`: Kết xuất giao diện Minimap Radar thời gian thực.

---

## 4. Inspector Settings (Quy chuẩn Màu sắc & Biểu tượng)

| Loại phòng | Trạng thái | Màu nền (RGBA) | Màu viền (RGBA) | Biểu tượng (Icon) |
|---|---|---|---|---|
| **SPAWN** | Mặc định | `(22, 68, 50, 235)` | `(45, 215, 135, 240)` | **`S`** |
| **COMBAT** | Bị khóa (Lockdown) | `(110, 22, 28, 245)` | `(255, 60, 70, 255)` | **`!`** |
| **COMBAT** | Đã dọn sạch (Cleared) | `(22, 50, 85, 235)` | `(60, 155, 240, 230)` | **`✔`** |
| **COMBAT** | Chưa giao chiến | `(78, 48, 22, 235)` | `(250, 140, 40, 235)` | **`⚔`** |
| **SHOP** | Luôn mở | `(85, 72, 18, 235)` | `(255, 215, 55, 240)` | **`Ⓒ`** |
| **RESET_STAT** | Luôn mở | `(18, 68, 88, 235)` | `(38, 205, 230, 240)` | **`⚙`** |
| **UPGRADE** | Luôn mở | `(72, 26, 92, 235)` | `(195, 72, 255, 240)` | **`★`** |
| **EXTRACT** | Sơ tán | `(18, 82, 68, 235)` | `(42, 230, 180, 240)` | **`H`** |
| **Phòng kề bên** | Chưa bước vào | `(20, 26, 36, 170)` | `(85, 105, 135, 180)` | **`?`** |

---

## 5. Quan hệ với hệ thống khác
- **`DungeonMap` & `Room`**: Quản lý dữ liệu không gian, danh sách các kết nối hành lang `activeConnections` và cờ trạng thái `discovered`.
- **`Player`**: Cung cấp tọa độ tâm `getCenterX()`, `getCenterY()` để cập nhật sương mù và vẽ chấm định vị Operative trên Minimap.
- **`PlayingState`**: Điều phối việc gọi `updateDiscovery()` trong vòng lặp cập nhật và `minimapHUD.render()` trong tầng hiển thị HUD.
- **`PlayingHUD`**: Hoạt động song song với MinimapHUD; Minimap được bố trí tại vị trí chiến lược `(1084, 64)` không che khuất bảng Credits `(1100, 20)` hay các thanh máu/thông tin ở bên trái.

---

## 6. Sửa đổi / Cập nhật
- **20/10/2026**:
  - Khởi tạo hệ thống Minimap Radar hoàn toàn mới với cơ chế Fog of War.
  - Hỗ trợ mở rộng tức thời phòng và toàn bộ các hành lang thông tới phòng khi người chơi di chuyển đến.
  - Hỗ trợ hiển thị dấu hỏi `?` cho phòng liền kề chưa khám phá.
  - Tích hợp 6 bài kiểm thử tự động trong `MinimapSystemTest.java`.

---

## 7. Lỗi đã sửa
- Không có lỗi hồi quy. Tỷ lệ ánh xạ 2.0 px/tile đảm bảo tính toàn vẹn hình học không bị lệch pixel.
