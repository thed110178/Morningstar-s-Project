Cập nhật mới nhất vào ngày: 01/11/2026

# Tile.java - Lớp Biểu Diễn Ô Gạch Địa Hình (Terrain Tile Unit)

## 1. Mô tả
`Tile.java` đại diện cho một ô gạch đơn lẻ trên lưới ma trận bản đồ ($48 \times 48$ px). Lớp này lưu giữ tọa độ ô lưới (`gridX`, `gridY`), tọa độ thế giới thực tế (`worldX`, `worldY`), phân loại địa hình (`TileType`), cờ trạng thái thảm cỏ (`hasGrass`), chỉ số hoa văn chuyển tiếp autotiling (`grassPattern`), máu của ô che chắn (`health`, `maxHealth`), khả năng phát nổ (`isExplosiveBarrel`) và phương thức vẽ đồ họa chi tiết (`render`).

---

## 2. Quy trình hoạt động
1. Khi `DungeonMap` khởi tạo, các đối tượng `Tile` được phân bổ vào mảng 2 chiều `tiles[COLS][ROWS]`.
2. Khi thực thể cần kiểm tra va chạm, `Tile.isSolid()` được gọi để biết ô có cản đường hay không.
3. Khi nhận sát thương từ đạn hoặc dao cận chiến, `Tile.takeDamage(damage)` khấu trừ máu của ô cover.
   - Thùng gỗ (`DESTRUCTIBLE_COVER`): Máu 40 HP, phát ra âm thanh vỡ vụn khi phá hủy.
   - Thùng phi nổ (`EXPLOSIVE_BARREL`): Máu 20 HP, phát nổ diện rộng (AoE) gây sát thương lên cả người chơi lẫn kẻ địch (kể cả Boss Tank) và kích nổ dây chuyền các thùng lân cận.
4. Khi vẽ màn hình:
   - Phương thức `render(Graphics2D g, int screenX, int screenY)` nhận tọa độ màn hình đã trừ độ lệch camera.
   - Vẽ màu nền hoặc texture cơ sở của `TileType` (sàn cỏ/đất, tường gạch, cột đá, thùng gỗ, thùng phi đỏ).
   - Vẽ thanh máu nhỏ (màu vàng cam cho thùng gỗ, đỏ cam cho thùng phi nổ) khi ô che chắn đang bị tổn thương.

---

## 3. API
* `public boolean isSolid()`: Kiểm tra ô có cản trở di chuyển vật lý của nhân vật hay không.
* `public boolean blocksProjectiles()`: Kiểm tra ô có cản trở hoặc hấp thụ đường bay của đạn hay không (bao gồm tường, vật cản, thùng gỗ, thùng phi nổ và cửa mở `DOOR_OPEN`).
* `public boolean isDestructible()`: Kiểm tra ô có thể nhận sát thương và phá hủy được hay không (`DESTRUCTIBLE_COVER` hoặc `EXPLOSIVE_BARREL`).
* `public boolean isExplosiveBarrel()`: Kiểm tra ô có phải là thùng phi xăng dầu phát nổ hay không.
* `public boolean takeDamage(int damage)`: Gây sát thương lên ô che chắn, trả về `true` nếu ô bị phá hủy hoàn toàn.
* `public static void setExplosionListener(TileExplosionListener listener)`: Đăng ký lắng nghe sự kiện bộc phá khi thùng phi nổ bị phá hủy.
* `public double getCenterX()`, `public double getCenterY()`: Lấy tọa độ pixel tâm thế giới của ô gạch.
* `public int getGridX()`, `public int getGridY()`: Lấy tọa độ cột và hàng trên lưới.
* `public int getWorldX()`, `public int getWorldY()`: Lấy tọa độ pixel góc trên bên trái trong thế giới.
* `public TileType getType()` / `public void setType(TileType type)`: Đọc/ghi kiểu ô gạch.
* `public boolean hasGrass()` / `public void setHasGrass(boolean hasGrass)`: Đọc/ghi cờ thảm cỏ sinh thái trên ô sàn.
* `public int getGrassPattern()` / `public void setGrassPattern(int pattern)`: Đọc/ghi chỉ số biến thể autotiling (1-30 từ `Grass_pattern`, hoặc 0 nếu dùng texture nguyên bản).
* `public void render(Graphics2D g, int screenX, int screenY)`: Vẽ ô gạch tại tọa độ màn hình với độ trễ $O(1)$, tự động dùng ảnh pre-scaled hardware-compatible theo phân loại tile và chỉ số `grassPattern`.

---

## 4. Quan hệ với hệ thống khác
* Chứa thuộc tính: `TileType`.
* Được quản lý bởi: `DungeonMap.java`.
* Tương tác với:
  - `GameSpriteManager.java`: Cung cấp bộ đệm texture VRAM (tường, đất, đá, thùng gỗ, thùng phi đỏ `Red Barrel.png` và 30 biến thể autotiling).
  - `TileExplosionListener.java`: Interface callback thông báo cho `PlayingState` kích hoạt hiệu ứng nổ và sát thương diện rộng.
  - `ProjectileManager.java`: Kiểm tra va chạm triệt tiêu đạn bay và gây sát thương phá hủy cover.

---

## 5. Sửa đổi / Cập nhật
* **03/10/2026 (Lần 1)**: Khởi tạo ban đầu với khả năng vẽ chi tiết từng loại địa hình.
* **03/10/2026 (Lần 2)**: Bổ sung phương thức `blocksProjectiles()` cho phép cửa mở (`DOOR_OPEN`) hấp thụ đạn bay qua để ngăn người chơi bắn tỉa quái từ hành lang trước khi bước vào phòng đấu trường.
* **31/10/2026 (Lần 3 - Tích hợp Autotiling 30 biến thể cỏ - đất)**:
  - Bổ sung thuộc tính `grassPattern` (0 - 30) kèm getter/setter.
  - Cập nhật hàm `render()` ưu tiên vẽ texture chuyển tiếp mềm mại từ `GameSpriteManager.getPreScaledGrassPattern(grassPattern)`.
* **01/11/2026 (Lần 4 - Thùng phi nổ `EXPLOSIVE_BARREL`)**:
  - Bổ sung `TileType.EXPLOSIVE_BARREL`: Máu 20 HP, cản đường và cản đạn.
  - Khi HP giảm về 0, kích nổ bộc phá diện rộng (bán kính 110px, sát thương tâm 65) qua `TileExplosionListener`.
  - Gây sát thương suy giảm theo khoảng cách lên CẢ người chơi lẫn KẺ ĐỊCH (kể cả Boss Tank và quái phụ).
  - Phản ứng nổ dây chuyền (Chain Reaction): Phá hủy thùng gỗ và kích nổ các thùng phi lân cận.
  - Sinh ngẫu nhiên số lượng nhỏ (1-2 thùng trong phòng combat, 3 thùng trong Boss Arena).

---

## 6. Lỗi đã sửa
* Đã khắc phục triệt để khả năng tràn ngăn xếp (Stack Overflow) khi nổ dây chuyền nhiều thùng liên tiếp nhờ kiến trúc hàng đợi sự kiện tuần tự (`pendingBarrelExplosions`) trong `PlayingState`.
