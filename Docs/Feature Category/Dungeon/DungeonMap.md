Cập nhật mới nhất vào ngày: 09/10/2026

# DungeonMap.java - Hệ Thống Hầm Ngục Lưới 4 Hướng (12h, 3h, 6h, 9h) Liền Kề & Kiểm Tra Va Chạm Lưới

## 1. Mô tả
`DungeonMap.java` quản lý toàn bộ ma trận địa hình 2 chiều $80 \times 45$ ô gạch của thế giới game ($3840 \times 2160$ px, mỗi ô tile $48 \times 48$ px).
Hệ thống hầm ngục được xây dựng theo **Kiến trúc Lưới Phòng Liền Kề 4 Hướng (4-Directional Grid-Adjacent Architecture)** chuẩn phong cách Roguelite (Soul Knight / The Binding of Isaac):
1. **Lưới phòng chuẩn $4 \times 3$**: Thế giới được chia thành lưới logic $4 \times 3$ ô phòng. Mỗi phòng có kích thước chuẩn $14 \times 10$ ô gạch ($672 \times 480\text{px}$).
2. **Liền kề chặt chẽ theo 4 hướng chính**: Các phòng chỉ được sinh và nối với nhau theo đúng 4 hướng:
   - **12h (Bắc / NORTH)**: Đi lên $(gx, gy - 1)$
   - **3h (Đông / EAST)**: Đi sang phải $(gx + 1, gy)$
   - **6h (Nam / SOUTH)**: Đi xuống $(gx, gy + 1)$
   - **9h (Tây / WEST)**: Đi sang trái $(gx - 1, gy)$
3. **1 hành lang thẳng tắp duy nhất giữa 2 phòng kề nhau**:
   - Không có hành lang chữ L uốn lượn lộn xộn.
   - Hành lang ngang (3h / 9h) dài 6 ô, rộng 4 ô gạch thông suốt.
   - Hành lang dọc (12h / 6h) dài 4 ô, rộng 4 ô gạch thông suốt.
   - Bọc tường đá `WALL` kín khít hai bên sườn hành lang.
   - Cửa tự động (Blast Doors) được lắp ở 2 đầu hành lang nối vào các phòng chiến đấu.
4. **Không chồng lấn (Zero Overlap)**: Các phòng hoàn toàn tách bạch, vuông vắn, không bị cắt xén, không có góc phòng nọ lấn vào tầm nhìn của phòng kia.
5. **Bộ lọc nhãn phòng thông minh theo khoảng cách Camera**: Chỉ phòng mà người chơi đang ở trong hoặc đứng sát bên cạnh ($\le 480\text{px}$) mới hiển thị nhãn, triệt tiêu hoàn toàn lỗi nhãn phòng trước lọt vào mép màn hình.

---

## 2. Quy trình hoạt động
1. **Khởi tạo & Tái sinh ngẫu nhiên (`generateProceduralDungeon`)**:
   * Khởi tạo mảng `Tile[80][45]` toàn bộ là `TileType.VOID`.
   * **Spawn Room**: Đặt tại ô lưới $(0, 1)$ (tọa độ $col = 3, row = 18$). Bệ `SPAWN_ZONE` $4 \times 4$ đặt tại tâm phòng.
   * **Combat Arena 1**: Luôn đặt tại ô lưới $(1, 1)$ (hướng 3h của Spawn). Người chơi chỉ cần nhấn phím D từ Spawn là đi thẳng qua hành lang ngang vào ngay Combat Arena 1.
   * **Các phòng Combat tiếp theo ($N \in [3, 5]$)**: Từ phòng combat mới nhất, hệ thống tìm các ô lân cận trống theo 4 hướng 12h, 3h, 6h, 9h và chọn ngẫu nhiên một hướng để đặt phòng mới và nối hành lang thẳng. Nếu bị kẹt, hệ thống phân nhánh từ các phòng trước đó.
   * **Extract Room**: Đặt tại một ô lân cận theo 4 hướng kề với phòng combat cuối cùng. Bệ `EXTRACT_ZONE` $4 \times 4$ đặt tại tâm phòng.
   * **Trụ che chắn (`COVER`)**: Mỗi phòng combat được chạm trổ 2 cột trụ $2 \times 2$ ở các góc phòng ($col + 2, row + 2$ và $col + 10, row + 6$). Tâm phòng được giữ trống hoàn toàn làm đấu trường và nơi xuất hiện hòm tiếp tế.
2. **Kiểm tra va chạm (`isAreaWalkable`)**:
   * Kiểm tra va chạm AABB $O(1)$ tức thì với các ô gạch `isSolid() == true`.
3. **Cản phá đạn bay (`isAreaBlockingProjectiles`)**:
   * Chặn đạn khi gặp tường `WALL`, cột `COVER`, cửa đóng `DOOR_CLOSED` và hấp thụ đạn khi gặp cửa mở `DOOR_OPEN`.
4. **Hiển thị đồ họa (`render`)**:
   * Viewport Culling siêu mượt.
   * `renderRoomLabels`: Lọc nhãn theo khoảng cách Euclid từ tâm Camera $\le 480\text{px}$.

---

## 3. API
* `public boolean isAreaWalkable(double worldX, double worldY, double width, double height)`: Kiểm tra vùng di chuyển hợp lệ.
* `public boolean isAreaBlockingProjectiles(double worldX, double worldY, double width, double height)`: Kiểm tra vùng cản hoặc hấp thụ đạn.
* `public boolean isIntersectsExtractZone(double worldX, double worldY, double width, double height)`: Kiểm tra tiếp xúc với bệ sơ tán.
* `public boolean isOnExtractZone(double worldX, double worldY)`: Kiểm tra điểm tọa độ trên bệ sơ tán.
* `public void reset()`: Reset trạng thái của tất cả phòng cũ về `UNVISITED` và mở cửa, sau đó tái sinh toàn bộ hầm ngục mới theo lưới 4 hướng cho lượt run tiếp theo.
* `public Room getSpawnRoom()`: Truy xuất phòng xuất phát.
* `public Room getCombatRoom()`: Truy xuất phòng chiến đấu đầu tiên.
* `public List<Room> getCombatRooms()`: Truy xuất toàn bộ danh sách 3 đến 5 phòng chiến đấu.
* `public Room getExtractRoom()`: Truy xuất phòng sơ tán.
* `public List<Room> getRooms()`: Truy xuất tất cả các phòng trên bản đồ.
* `public Tile getTile(int col, int row)`: Truy xuất ô gạch tại tọa độ lưới.

---

## 4. Quan hệ với hệ thống khác
* Sử dụng: `Tile`, `TileType`, `Room`, `GameConfig`, `Camera`.
* Phối hợp chặt chẽ với `PlayingState`:
  - `PlayingState` kích hoạt phong tỏa (`lockdown()`) và sinh quái khi người chơi bước vào từng phòng combat.
  - Hỗ trợ clamp vị trí người chơi an toàn trên cả 4 hướng (Bắc, Đông, Nam, Tây) khi cửa đóng lại.
  - Khi dọn sạch phòng: Mở cửa phòng, thả hòm tiếp tế chứa ngẫu nhiên vũ khí chính tại tâm phòng.
  - Khi dọn sạch toàn bộ phòng: Mở đường dẫn sang phòng Extract để người chơi sơ tán.

---

## 5. Sửa đổi / Cập nhật
* **09/10/2026 (Lần 1)**: Mở rộng dungeon 3-5 phòng combat.
* **09/10/2026 (Lần 2 - Tái cấu trúc chuẩn Lưới 4 Hướng)**:
  - Thay thế hoàn toàn thuật toán sinh tự do bằng thuật toán Lưới Phòng Liền Kề 4 Hướng (12h, 3h, 6h, 9h).
  - Chuẩn hóa kích thước phòng $14 \times 10$ tiles và khoảng cách hành lang cố định.
  - Mỗi cặp phòng kết nối bằng 1 hành lang thẳng tắp duy nhất, bọc tường kín khít và lắp blast doors.
  - Khắc phục triệt để lỗi phòng chồng lấn, góc phòng cắt xén và nhãn phòng lọt vào khung hình.
