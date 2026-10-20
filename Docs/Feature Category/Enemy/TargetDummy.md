Cập nhật mới nhất vào ngày: 18/10/2026

# TargetDummy.java - Mục Tiêu Thử Nghiệm & Boss Dummy Trong Phòng Chiến Đấu

## 1. Mô tả
`TargetDummy.java` đại diện cho mục tiêu kẻ địch giả định đặt bên trong các đấu trường **Combat Arena** và **Grand Boss Arena**.
Hệ thống hỗ trợ 2 biến thể thực thể:
1. **Regular Target Dummy**: Kích thước tiêu chuẩn (30px), máu 75 - 100 HP, giáp đỏ cơ bản với mắt cảm biến quang học, thanh máu nhỏ 34px trên đầu.
2. **Titan Mecha Dummy [BOSS]**: Kích thước đồ sộ (64px), thanh máu cực dày (1000 HP), bộ khung giáp titan đỏ sẫm góc cạnh với tấm ốp hắc diện thạch, viền cyber vàng rực, giáp vai pauldrons tích hợp đèn tín hiệu, lõi lò phản ứng năng lượng trung tâm đập theo nhịp (Pulsing Reactor Core), vòng hào quang cảnh báo nguy hiểm dưới chân (Hazard Danger Aura Ring), và thanh máu đỉnh đầu 68px. Khi bước vào phòng Boss, toàn màn hình hiển thị thanh máu Boss cỡ lớn (Large Boss Health Bar 520px).

---

## 2. Quy trình hoạt động
1. **Khởi tạo và Phong tỏa phòng**:
   * Ở Stage 1 và Stage 2: Khi phòng chiến đấu phong tỏa, `PlayingState` sinh ra 3 đến 4 mục tiêu thường (75 - 100 HP, 30px) tại các vị trí sàn an toàn.
   * Ở Stage 3 (Grand Boss Arena): Khi người chơi bước vào phòng và kích hoạt phong tỏa, sinh ra **Titan Mecha Dummy [BOSS]** (1000 HP, 64px) tại tâm phòng và 8 minion hộ tống xung quanh.
2. **Hiển thị Thanh Máu Boss Lớn (Top-Center Large Boss Health Bar)**:
   * Ngay khi bước vào phòng Boss và phong tỏa diễn ra, thanh máu Boss lớn (rộng 520px, cao 22px) xuất hiện ở chính giữa phía trên màn hình.
   * Hiển thị bảng tên kim loại vàng `☠ TITAN MECHA DUMMY [BOSS] ☠`.
   * Thanh máu có hiệu ứng dải màu Gradient đỏ tươi pha cam, vạch chia 5 phân đoạn (mỗi 20%), hiển thị chỉ số chính xác dạng số: `X / 1000 HP (Y%)`.
   * Bảng Sector HUD bên trái tự động thu gọn chiều ngang (width 330px) để không che khuất hay đè lên thanh máu Boss.
3. **Va chạm và Nhận Sát Thương**:
   * Khi viên đạn (`Projectile`) hoặc nhát chém dao (`MeleeSlash`) chạm vào hộp bao của Boss:
     * Trừ máu tương ứng (`takeDamage`).
     * Toàn bộ khung giáp và lõi lò phản ứng chớp trắng phản hồi lực tác động (`hitFlashTimer = 0.1s`).
     * Thanh máu lớn cập nhật tỷ lệ máu tức thì.
4. **Hạ gục Boss & Phần thưởng**:
   * Khi `currentHealth <= 0`:
     * Đánh dấu `alive = false`, thanh máu Boss tự động biến mất khỏi màn hình.
     * Người chơi nhận thưởng đặc biệt: **+50 Credits**.
     * Văng ra 3 stack đạn các loại (AR: 30 viên, SMG: 35 viên, Shotgun: 10 viên) xung quanh xác Boss.
     * Kích hoạt banner chiến tích: `☠ BOSS TIÊU DIỆT: TITAN MECHA DUMMY ĐÃ BỊ HẠ GỤC! (+50 CREDITS) ☠`.
     * Khi toàn bộ minion còn lại bị tiêu diệt, cửa phòng mở và bệ sơ tán (Extract Pad) mở ra tại tâm phòng để hoàn thành màn chơi.

---

## 3. API
* `public TargetDummy(double x, double y, int maxHealth)`: Constructor tạo mục tiêu thường (size 30px, isBoss = false).
* `public TargetDummy(double x, double y, int maxHealth, double size, boolean isBoss, String name)`: Constructor mở rộng tạo Boss Dummy hoặc mục tiêu tùy biến.
* `public void takeDamage(int damage)`: Trừ máu và kích hoạt hiệu ứng chớp trắng.
* `public void update(double deltaTime)`: Cập nhật bộ đếm thời gian chớp trắng và nhịp đập hoạt họa lò phản ứng.
* `public void render(Graphics2D g, Camera camera)`: Vẽ mục tiêu theo phong cách thường hoặc Titan Mecha Boss với đầy đủ giáp trụ, đèn hiệu, vòng hào quang và thanh máu overhead.
* `public Rectangle getBounds()`: Lấy hộp bao AABB để kiểm tra va chạm đạn và cận chiến.
* `public boolean isAlive()`: Kiểm tra mục tiêu còn sống hay không.
* `public boolean isBoss()`: Kiểm tra thực thể có phải Boss hay không.
* `public String getName()`: Lấy tên hiển thị của thực thể.
* `public int getMaxHealth()`: Lấy lượng máu tối đa.
* `public int getCurrentHealth()`: Lấy lượng máu hiện tại.
* `public double getSize()`: Lấy kích thước cạnh hộp bao (30.0 hoặc 64.0).

---

## 4. Quan hệ với hệ thống khác
* Được kiểm tra va chạm bởi `ProjectileManager` và `WeaponManager` trong `PlayingState`.
* `PlayingState` quản lý tham chiếu `bossDummy` để render thanh máu Boss cỡ lớn (`renderBossHealthBar`).
* Điều kiện hoàn thành phòng và kích hoạt bệ sơ tán `ExtractZone` của `DungeonMap`.
* Khi tử trận: Sinh thực thể `EnemyCorpse` (đầu lâu tông màu xám nâu mờ nhạt) trượt văng trên sàn theo góc knockback của đòn kết liễu.

---

## 5. Sửa đổi / Cập nhật
* **20/10/2026**:
  - Tích hợp sinh xác kẻ địch (`EnemyCorpse`) hình đầu lâu nâu mờ nhạt (faded sepia-brown skull) khi mục tiêu bị tiêu diệt.
  - Xác nhận lực knockback từ đòn kết liễu (đạn, dao chém, bộc phá), trượt văng trên sàn gạch theo ma sát địa hình, không xuyên tường, và mờ dần trong 4.0s.
* **18/10/2026**: 
  - Nâng cấp Boss Dummy thành **Titan Mecha Dummy**: Tăng kích thước lên 64px, tăng lượng máu lên 1000 HP.
  - Thiết kế đồ họa chi tiết cho Boss: Giáp titan đỏ sẫm, tấm đệm hắc diện thạch, viền cyber vàng, giáp vai pauldrons với đèn cảnh báo, lò phản ứng hạt nhân phát xung nhịp tim, và vòng hazard aura.
  - Tích hợp thanh máu lớn cố định ở cạnh trên màn hình (Top-Center Boss Health Bar) rộng 520px, có phân vạch 20%, hiển thị tỷ lệ % và chỉ số máu cụ thể.
  - Tích hợp cơ chế thưởng hạ gục Boss: +50 Credits, văng 3 stack đạn các loại, thông báo biểu ngữ vinh quang.
* **03/10/2026**: Khởi tạo ban đầu phục vụ kiểm tra cơ chế chiến đấu và mở cửa phòng.

---

## 6. Lỗi đã sửa
* Khắc phục việc dummy boss ban đầu quá nhỏ và cùng lượng máu với quái thường, gây thiếu cảm giác đối đầu với trùm cuối.

