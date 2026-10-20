Cập nhật mới nhất vào ngày: 20/10/2026

# PlayingState.java - Màn Chơi Chiến Đấu Chính (Core Combat State)

## 1. Mô tả
`PlayingState.java` kế thừa từ `GameState`, là màn hình diễn ra toàn bộ gameplay chiến đấu sinh tồn của người chơi. Lớp này quản lý: nhân vật di chuyển đa hướng mượt mà, kỹ năng né đòn i-frames [SPACE], hệ thống vũ khí (AR, Shotgun, SMG, Pistol, Dao găm), Camera 2D Lerp tự động theo dõi người chơi, cơ chế ngắm xoay súng 360 độ theo con trỏ chuột, dungeon map nhiều phòng chiến đấu và phòng Boss, hòm tiếp tế vũ khí, quầy giao dịch Shop, trạm hiệu chỉnh khí tài, Menu Balo [TAB], Menu Tạm Dừng [ESC] và hệ thống cài đặt phím tùy chỉnh.

---

## 2. Quy trình hoạt động
1. **Khởi tạo (`init`)**:
   * Đặt vị trí ban đầu của nhân vật tại tâm phòng xuất phát (Spawn Room) hoặc phòng Boss khi kiểm thử.
   * Khởi tạo bản đồ dungeon theo tầng (Stage 1 đến Stage 4 - Boss Tank).
   * Đặt lại trạng thái balo, trạng thái tạm dừng `isPaused = false`, túi đạn, vũ khí và kẻ địch.
2. **Cập nhật (`update`)**:
   * **Menu Tạm Dừng [ESC]**: Nếu `isPaused == true`, chuyển quyền điều khiển sang `PauseMenuUI.update(...)` và đóng băng toàn bộ tiến trình chiến trường (quái vật, đạn, người chơi, v.v.). Người chơi có 3 tùy chọn:
     1. TIẾP TỤC (`RESUME`): Đóng menu và tiếp tục game.
     2. CÀI ĐẶT (`SETTINGS`): Mở giao diện tùy chỉnh phím điều khiển (Keybind Settings).
     3. THOÁT RA MENU (`EXIT_TO_MENU`): Rời trận và quay về menu chính.
   * **Menu Balo [TAB]**: Mở kho trang bị an toàn, cho phép hoán đổi vị trí súng 1 và súng 2. Bấm [ESC] khi đang mở balo sẽ đóng balo.
   * **Bắt phím ESC trong trận**: Bấm [ESC] để mở Menu Tạm Dừng.
   * **Xử lý phím tác chiến qua `KeyBindings`**:
     - Di chuyển: `KeyBindings.isActionDown(..., MOVE_UP/DOWN/LEFT/RIGHT)`
     - Né đòn: `KeyBindings.isActionPressedOnce(..., DODGE)`
     - Đổi slot vũ khí 1-4: `SLOT_1`, `SLOT_2`, `SLOT_3`, `SLOT_4`
     - Đổi nhanh vũ khí: `QUICK_SWAP`
     - Nạp đạn: `RELOAD`
     - Tương tác hòm/cứu thương/shop/trạm thợ súng/trực thăng: `INTERACT`
   * **Tính toán góc ngắm chuột & Camera Lerp rung chấn**.
   * **Cập nhật quái vật, đường đạn, va chạm tường 2 trục độc lập và phòng chiến đấu lockdown**.
3. **Hiển thị (`render`)**:
   * Vẽ mặt sàn dungeon, xác quái đầu lâu nâu mờ (`EnemyCorpse`), hòm thưởng, trạm cứu thương, quầy shop.
   * Vẽ nhân vật, nòng súng, đường đạn, quái vật, vệt chém dao găm, hiệu ứng nổ.
   * Vẽ HUD chiến thuật (`PlayingHUD`): máu, thanh né đòn i-frames, số dư credits, thanh máu boss, biểu ngữ thông báo.
   * Vẽ giao diện Balo chiến thuật khi mở (`BackpackUI`).
   * Vẽ lớp phủ Menu Tạm Dừng khi `isPaused == true` (`PauseMenuUI`).

---

## 3. API
* `public PlayingState(GameStateManager gsm)`: Khởi tạo màn chơi và Camera 2D.
* `public void init()`: Khởi tạo lại phiên chơi mới từ đầu.
* `public void loadBossStageDirectly()`: Spawn thẳng người chơi vào Grand Boss Arena đối đầu Boss Tank.
* `public void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH)`: Điều phối logic toàn bộ chiến trường.
* `public void render(Graphics2D g)`: Vẽ thế giới game, nhân vật, thực thể và giao diện HUD / Pause Menu.
* `public boolean isPaused()`: Kiểm tra trạng thái tạm dừng của màn chơi.
* `public void setPaused(boolean paused)`: Đặt trạng thái tạm dừng của màn chơi.
* `public PauseMenuUI getPauseMenuUI()`: Lấy tham chiếu đến đối tượng Menu Tạm Dừng.

---

## 4. Quan hệ với hệ thống khác
* Sử dụng `Camera` để điều phối hiển thị không gian 2D.
* Đọc dữ liệu từ `KeyboardHandler` và `MouseHandler` thông qua `KeyBindings`.
* Phối hợp chặt chẽ với `PauseMenuUI` khi người chơi bấm ESC để tạm dừng hoặc cài đặt phím.
* Điều phối `Player`, `WeaponManager`, `ProjectileManager`, `DungeonMap` và `PlayingHUD`.

---

## 5. Sửa đổi / Cập nhật
* **02/10/2026**: Khởi tạo ban đầu với cơ chế di chuyển WASD, nòng súng xoay 360 độ và Camera Lerp.
* **20/10/2026**: Bổ sung kỹ năng né đòn [SPACE] i-frames, xác kẻ địch đầu lâu nâu mờ, tích hợp Menu Tạm Dừng [ESC] với 3 tùy chọn (Tiếp tục, Cài đặt, Thoát ra menu) và kết nối toàn diện hệ thống gán phím linh hoạt `KeyBindings`.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
