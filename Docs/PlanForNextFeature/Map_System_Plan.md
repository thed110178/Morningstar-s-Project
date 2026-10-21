Cập nhật mới nhất vào ngày: 03/10/2026

# KẾ HOẠCH TRIỂN KHAI: HỆ THỐNG BẢN ĐỒ 3 PHÒNG (SPAWN - COMBAT - EXTRACT)

## 1. Mục tiêu tính năng
Xây dựng hệ thống bản đồ dạng lưới gạch (**Tile-based Dungeon Map**) cho game theo kiến trúc Roguelite Soul Knight / Surviv.io. Bản đồ demo sẽ bao gồm 3 khu vực phòng chuyên biệt kết nối qua hành lang:
1. **Phòng Xuất phát (Spawn Room)**: Khu vực an toàn ban đầu để người chơi xuất hiện và chuẩn bị.
2. **Phòng Chiến đấu (Combat Room)**: Đấu trường trung tâm với diện tích lớn, có các cột trụ/chướng ngại vật làm vật che chắn (cover) phục vụ giao tranh né đạn.
3. **Phòng Sơ tán (Extract Room)**: Khu vực kết thúc màn chơi với Cổng dịch chuyển (Extraction Portal) để người chơi hoàn thành nhiệm vụ và chuyển tầng.

---

## 2. Kiến trúc & Các lớp cần tạo

```
src/main/java/com/groupproject/game/dungeon/
├── TileType.java          // Enum loại ô gạch (FLOOR, WALL, COVER, SPAWN_ZONE, EXTRACT_ZONE)
├── Tile.java              // Lớp ô gạch cơ sở lưu thuộc tính, hitbox và trạng thái rắn (solid)
├── Room.java              // Lớp biểu diễn một căn phòng hình chữ nhật (tọa độ, kích thước, RoomType)
└── DungeonMap.java        // Ma trận bản đồ 2D, kiểm tra va chạm tường và vẽ Viewport Culling
```

---

## 3. Quy trình tính toán & Thuật toán

### 3.1 Bố cục 3 phòng (Room Layout)
* **Thế giới game**: Lưới ma trận kích thước $80 \times 45$ ô gạch ($3840 \times 2160$ px).
* **Phòng 1 - Spawn Room**: Kích thước $14 \times 12$ ô (đặt ở phía Tây bản đồ, tâm tại khoảng tile (15, 22)).
* **Hành lang Tây - Trung tâm (Corridor 1)**: Nối từ Spawn Room sang Combat Room.
* **Phòng 2 - Combat Room**: Kích thước $26 \times 20$ ô (đặt ở trung tâm bản đồ, tile (32, 13)), chứa 4 cột trụ che chắn bên trong.
* **Hành lang Trung tâm - Đông (Corridor 2)**: Nối từ Combat Room sang Extract Room.
* **Phòng 3 - Extract Room**: Kích thước $14 \times 12$ ô (đặt ở phía Đông bản đồ, tile (60, 22)), chứa Cổng trích xuất ở giữa.

### 3.2 Cơ chế Va chạm Tường (Wall Collision)
* Phương thức `isAreaWalkable(double worldX, double worldY, double width, double height)`:
  * Chuyển đổi bounding box của nhân vật sang chỉ số hàng/cột của lưới ma trận tile.
  * Nếu bất kỳ ô nào nhân vật chạm phải có thuộc tính `isSolid = true`, chặn di chuyển trên trục đó (trượt theo tường - wall sliding).

### 3.3 Tối ưu Hiệu năng (Viewport Culling)
* Chỉ duyệt và vẽ các ô gạch giao với khung nhìn camera:
  $$\text{startCol} = \max(0, \lfloor \text{cameraX} / \text{TILE\_SIZE} \rfloor)$$
  $$\text{endCol} = \min(\text{COLS} - 1, \lceil (\text{cameraX} + \text{VIEWPORT\_W}) / \text{TILE\_SIZE} \rceil)$$
  (Tương tự với hàng Y). Đảm bảo giữ vững 60 FPS cố định.

---

## 4. Kế hoạch Kiểm thử (Unit Testing)
Tạo `src/test/java/com/groupproject/game/dungeon/DungeonMapTest.java` kiểm tra:
* Khởi tạo bản đồ đúng kích thước $80 \times 45$.
* Tọa độ tâm của Spawn Room là ô có thể đi lại được (`isSolid == false`).
* Các bức tường xung quanh có `isSolid == true`.
* Bounding Box ngoài map hoặc chạm tường trả về `isAreaWalkable() == false`.
