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
3. **Kết nối đa chiều tự do (Multi-directional Adjacent Connectivity)**:
   - **Tất cả các phòng Combat ở cạnh nhau đều được nối với nhau**: Sau khi các phòng được phân bổ trên lưới, hệ thống tự động quét toàn bộ lưới và nối mọi cặp phòng combat kề nhau (ngang hoặc dọc) bằng hành lang thẳng và cửa blast doors.
   - Người chơi hoàn toàn tự do chọn lối đi, có thể rẽ sang bất kỳ phòng kề nào theo các hướng 12h, 3h, 6h, 9h.
4. **1 hành lang thẳng tắp duy nhất giữa 2 phòng kề nhau**:
   - Không có hành lang chữ L uốn lượn lộn xộn.
   - Hành lang ngang (3h / 9h) dài 6 ô, rộng 4 ô gạch thông suốt.
   - Hành lang dọc (12h / 6h) dài 4 ô, rộng 4 ô gạch thông suốt.
   - Bọc tường đá `WALL` kín khít hai bên sườn hành lang.
   - Cửa tự động (Blast Doors) được lắp ở 2 đầu hành lang nối vào các phòng chiến đấu.
5. **Không chồng lấn (Zero Overlap)**: Các phòng hoàn toàn tách bạch, vuông vắn, không bị cắt xén, không có góc phòng nọ lấn vào tầm nhìn của phòng kia.
6. **Bộ lọc nhãn phòng thông minh theo khoảng cách Camera**: Chỉ phòng mà người chơi đang ở trong hoặc đứng sát bên cạnh ($\le 480\text{px}$) mới hiển thị nhãn, triệt tiêu hoàn toàn lỗi nhãn phòng trước lọt vào mép màn hình.

---

## 2. Quy trình hoạt động
1. **Khởi tạo & Tái sinh ngẫu nhiên (`generateProceduralDungeon`)**:
   * Khởi tạo mảng `Tile[80][45]` toàn bộ là `TileType.VOID`.
   * **Spawn Room**: Đặt tại ô lưới $(0, 1)$ (tọa độ $col = 3, row = 18$). Bệ `SPAWN_ZONE` $4 \times 4$ đặt tại tâm phòng.
   * **Combat Arena 1**: Luôn đặt tại ô lưới $(1, 1)$ (hướng 3h của Spawn). Người chơi chỉ cần nhấn phím D từ Spawn là đi thẳng qua hành lang ngang vào ngay Combat Arena 1.
   * **Các phòng Combat tiếp theo ($N \in [3, 5]$)**: Từ phòng combat mới nhất, hệ thống tìm các ô lân cận trống theo 4 hướng 12h, 3h, 6h, 9h và chọn ngẫu nhiên một hướng để đặt phòng mới. Nếu bị kẹt, hệ thống phân nhánh từ các phòng trước đó.
   * **Extract Room**: Đặt tại một ô lân cận theo 4 hướng kề với phòng combat cuối cùng. Bệ `EXTRACT_ZONE` $4 \times 4$ đặt tại tâm phòng.
   * **Quét và kết nối toàn bộ Combat Rooms kề nhau**: Quét ma trận lưới $4 \times 3$, với mọi cặp phòng combat kề nhau theo trục hoành (EAST) hoặc trục tung (SOUTH), tự động tạo hành lang thẳng và lắp blast doors ở cả 2 đầu.
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
* `public boolean isNearDoorway(Room room, int c, int r)`: Kiểm tra xem ô (c, r) có nằm trong vùng lối ra vào (hướng 12h, 3h, 6h, 9h) hoặc tâm phòng hay không để đảm bảo thông thoáng 100%.
* `public void spawnRandomDestructibleCovers(Room room, int count)`: Sinh ngẫu nhiên các ô cover tạm thời (`DESTRUCTIBLE_COVER`) trong phòng tại các vị trí an toàn xa cửa.
* `public boolean damageDestructibleCoverInArea(double worldX, double worldY, double width, double height, int damage)`: Gây sát thương lên các ô cover tạm thời trong vùng AABB.
* `public boolean damageDestructibleCoverAt(double worldX, double worldY, int damage)`: Gây sát thương lên ô cover tạm thời tại tọa độ xác định.
* `public void generateStage(int stage)`: Sinh bản đồ theo chỉ số stage trong Biome (Stage 1 & 2: Lưới 3-5 phòng combat; Stage 3: Grand Boss Arena).
* `public void spawnExtractZoneInRoom(Room room)`: Kích hoạt tạo bệ sơ tán `EXTRACT_ZONE` $4 \times 4$ ngay tại tâm của một phòng cụ thể (dành cho Stage 3 Boss Arena).
* `public int getCurrentStage()`: Truy xuất chỉ số stage hiện tại trong Biome.
* `public void reset()`: Reset trạng thái của tất cả phòng cũ về `UNVISITED` và mở cửa, sau đó tái sinh toàn bộ hầm ngục mới từ Stage 1 cho lượt run tiếp theo.
* `public Room getSpawnRoom()`: Truy xuất phòng xuất phát.
* `public Room getCombatRoom()`: Truy xuất phòng chiến đấu đầu tiên (hoặc Boss Arena ở Stage 3).
* `public List<Room> getCombatRooms()`: Truy xuất toàn bộ danh sách phòng chiến đấu của stage hiện tại.
* `public Room getExtractRoom()`: Truy xuất phòng sơ tán (trả về null ở Stage 3 khi chưa dọn sạch).
* `public List<Room> getRooms()`: Truy xuất tất cả các phòng trên bản đồ.
* `public Tile getTile(int col, int row)`: Truy xuất ô gạch tại tọa độ lưới.

---

## 4. Quan hệ với hệ thống khác
* Sử dụng: `Tile`, `TileType`, `Room`, `GameConfig`, `Camera`.
* Phối hợp chặt chẽ với `PlayingState` và `ProjectileManager`:
  - `ProjectileManager`: Đạn bắn chạm vào ô `DESTRUCTIBLE_COVER` sẽ bị hấp thụ và truyền sát thương phá hủy thùng gỗ.
  - `PlayingState`: Nhát chém cận chiến `MeleeSlash` (dao găm) gây sát thương và phá hủy tức thì thùng gỗ trong tầm chém.
  - Loại bỏ hoàn toàn cover cứng bao quanh lối vào của các phòng combat.

---

## 5. Sửa đổi / Cập nhật
* **09/10/2026 (Lần 1)**: Mở rộng dungeon 3-5 phòng combat.
* **09/10/2026 (Lần 2 - Tái cấu trúc chuẩn Lưới 4 Hướng)**:
  - Thay thế hoàn toàn thuật toán sinh tự do bằng thuật toán Lưới Phòng Liền Kề 4 Hướng (12h, 3h, 6h, 9h).
  - Chuẩn hóa kích thước phòng $14 \times 10$ tiles và khoảng cách hành lang cố định.
  - Mỗi cặp phòng kết nối bằng 1 hành lang thẳng tắp duy nhất, bọc tường kín khít và lắp blast doors.
* **09/10/2026 (Lần 3 - Kết nối đa chiều tự do giữa tất cả Combat Rooms cạnh nhau)**:
  - Tự động phát hiện và kết nối tất cả các cặp phòng Combat kề nhau theo 4 hướng (12h, 3h, 6h, 9h).
  - Cho phép người chơi tự do di chuyển qua lại giữa các phòng chiến đấu kề nhau theo mạng lưới đa chiều.
* **09/10/2026 (Lần 4 - Hệ thống Biome 3 Stage & Grand Boss Arena)**:
  - Bổ sung `generateStage(int stage)`: Stage 1 và Stage 2 sinh hầm ngục mạng lưới 3-5 phòng; Stage 3 sinh Grand Boss Arena to ($28 \times 18$ tiles) nối thẳng từ Spawn Room.
  - Bổ sung `spawnExtractZoneInRoom(Room room)`: Kích hoạt mở bệ sơ tán $4 \times 4$ ngay tại tâm phòng chiến đấu to sau khi clear toàn bộ quái.
  - Khắc phục triệt để lỗi luồng `ConcurrentModificationException` giữa Swing EDT và Game Loop.
* **09/10/2026 (Lần 5 - Ô Cover Tạm Thời (Destructible Cover) & Loại Bỏ Cover Cứng Chắn Cửa)**:
  - Thêm `TileType.DESTRUCTIBLE_COVER`: Thùng gỗ che chắn chiến thuật có 40 HP, bị phá hủy thành `FLOOR` khi bị đạn bắn hoặc cận chiến chém trúng.
  - Sinh ngẫu nhiên từ 3-5 ô cover tạm thời trong các phòng combat và 8 ô trong Boss Arena tại các vị trí an toàn.
  - Loại bỏ hoàn toàn các khối cover cứng cũ nằm sát mép cửa; đảm bảo 100% lối ra vào và hành lang của các phòng thông thoáng tuyệt đối.
* **09/10/2026 (Lần 6 - Cover Cứng Song Hành Cover Tạm Thời & Sửa Lỗi Enemy Spawn Vào Cover)**:
  - Bổ sung `spawnSolidCovers(Room room)`: Bố trí các cột cover cứng kiên cố (`COVER`) tại các góc chiến thuật an toàn của phòng thường, kết hợp với các thùng gỗ cover tạm thời (`DESTRUCTIBLE_COVER`) sinh ngẫu nhiên.
  - Cả hai loại cover xuất hiện đồng thời trong mọi phòng chiến đấu, nhưng được kiểm tra nghiêm ngặt qua `isNearDoorway` để đảm bảo tuyệt đối không sinh ở gần cửa ra vào của phòng.
  - Khắc phục triệt để lỗi Enemy bị spawn đè lên các ô cover: Tích hợp thuật toán `isSafeEnemySpawnPoint` và `findSafeSpawnLocation` (spiral search) trong `PlayingState`, đảm bảo 100% mục tiêu chiến đấu được sinh trên ô sàn trống (`FLOOR`), hoàn toàn cách ly khỏi các ô cover cứng, cover tạm thời và tường.
