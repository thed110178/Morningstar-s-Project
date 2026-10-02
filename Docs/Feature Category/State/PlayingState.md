Cập nhật mới nhất vào ngày: 02/10/2026

# PlayingState.java - Màn Chơi Chiến Đấu Chính (Core Combat State)

## 1. Mô tả
`PlayingState.java` kế thừa từ `GameState`, là màn hình diễn ra toàn bộ gameplay chiến đấu sinh tồn của người chơi. Hiện tại, lớp này quản lý phiên bản thử nghiệm (Prototype) bao gồm: nhân vật di chuyển 8 hướng mượt mà trên bản đồ thế giới rộng lớn, Camera 2D Lerp tự động theo dõi người chơi, cơ chế ngắm xoay súng 360 độ theo con trỏ chuột, và vẽ lưới bản đồ tile động theo tọa độ camera.

---

## 2. Quy trình hoạt động
1. **Khởi tạo (`init`)**:
   * Đặt vị trí ban đầu của nhân vật tại chính giữa bản đồ thế giới:
     $$x = \frac{\text{WORLD\_WIDTH}}{2}, \quad y = \frac{\text{WORLD\_HEIGHT}}{2}$$
2. **Cập nhật (`update`)**:
   * Kiểm tra phím ESC: Nếu nhấn, gọi `gsm.setState(GameStateManager.StateType.MENU)` để tạm thời quay lại menu.
   * Tính toán quãng đường di chuyển dựa trên delta time:
     $$\text{step} = \text{PLAYER\_DEFAULT\_SPEED} \times \text{deltaTime}$$
   * Xử lý di chuyển theo tổ hợp phím W, A, S, D.
   * Cập nhật vị trí Camera bám theo nhân vật (`camera.update(testPlayerX, testPlayerY)`).
   * Chuyển đổi tọa độ chuột từ màn hình sang tọa độ thế giới thông qua Camera:
     $$\text{mouseWorldX} = \text{camera.screenToWorldX(mouseH.getScreenX())}$$
     $$\text{mouseWorldY} = \text{camera.screenToWorldY(mouseH.getScreenY())}$$
   * Tính toán góc xoay súng 360 độ bằng hàm lượng giác:
     $$\text{aimAngle} = \text{atan2}(\text{mouseWorldY} - \text{testPlayerY}, \text{mouseWorldX} - \text{testPlayerX})$$
3. **Hiển thị (`render`)**:
   * Vẽ màu nền map thế giới (`24, 28, 38`).
   * Duyệt và vẽ các đường lưới tile (`renderWorldGrid`) dựa trên vị trí bù trừ của Camera.
   * Chuyển đổi tọa độ nhân vật sang tọa độ màn hình (`camera.worldToScreenX/Y`) và vẽ thân nhân vật (hình tròn phát sáng).
   * Vẽ nòng súng vươn ra từ tâm nhân vật theo góc $\text{aimAngle}$.
   * Hiển thị thông tin HUD (tọa độ nhân vật, tọa độ camera, hướng dẫn phím).

---

## 3. API
* `public PlayingState(GameStateManager gsm)`: Khởi tạo màn chơi và Camera 2D.
* `public void init()`: Khởi tạo lại phiên chơi mới.
* `public void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH)`: Điều phối logic di chuyển, camera và góc bắn.
* `public void render(Graphics2D g)`: Vẽ thế giới game, nhân vật và giao diện HUD.

---

## 4. Quan hệ với hệ thống khác
* Sử dụng `Camera` để điều phối hiển thị không gian 2D.
* Đọc dữ liệu từ `KeyboardHandler` và `MouseHandler`.
* Sau này sẽ là nơi chứa và điều phối các đối tượng `Player`, danh sách `Enemy`, `Projectile` và `DungeonMap`.

---

## 5. Sửa đổi / Cập nhật
* **02/10/2026**: Khởi tạo ban đầu với cơ chế di chuyển WASD, nòng súng xoay 360 độ và Camera Lerp.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
