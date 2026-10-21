Cập nhật mới nhất vào ngày: 21/10/2026

# PlayerHealthSystem.md - Hệ Thống Sinh Lực (HP), Kỹ Năng Né Đòn (Dodge Roll) & Vật Phẩm Hồi Phục

## 1. Mô tả
Tài liệu đặc tả kiến trúc quản lý sinh lực của nhân vật người chơi (Operative), cơ chế nhận sát thương an toàn với thời gian bất tử (i-frames), kỹ năng lướt né cơ động (Combat Dash / Dodge Roll) kích hoạt bằng phím `[SPACE]` mang lại khả năng miễn nhiễm sát thương tuyệt đối trong thời gian lướt, phản hồi thị giác khi bị thương, trạng thái tử trận (Game Over), thanh máu cố định trên HUD màn hình, và hệ thống vật phẩm hồi phục tương tác thủ công bằng phím `[F]`.

---

## 2. Quy trình hoạt động

### 2.1 Chỉ số Sinh Lực & Nhận Sát Thương
* **Máu tối đa (`maxHealth`)**: Mặc định **`100 HP`**.
* **Máu hiện tại (`playerCurrentHealth`)**: Khởi đầu **`100 HP`**.
* **Cơ chế nhận sát thương (`damagePlayer(int damage)`)**:
  * Khi dính đòn (đạn hoặc cận chiến từ kẻ địch), nếu đang trong trạng thái né đòn (`isDodging()`) hoặc đang trong thời gian bất tử (`invulnerableTimer > 0`), toàn bộ sát thương bị vô hiệu hóa (100% i-frames immunity).
  * Khi không bất tử: Trừ máu `currentHealth = Math.max(0, currentHealth - damage)`.
  * Kích hoạt thời gian bất tử (i-frames sau va chạm): **`0.8 giây`** (`invulnerableTimer = 0.8`).
  * Kích hoạt rung màn hình nhẹ (*Camera Screen Shake*): **`0.2 giây`** với biên độ ngẫu nhiên $\pm 3\text{px}$.
  * Hiệu ứng chớp nhấp nháy đỏ / trong suốt trên thân nhân vật trong suốt thời gian bất tử.
  * Cảnh báo viền đỏ nguy hiểm (*Danger Vignette*): Khi máu tụt xuống $\le 30\%$ và chưa chết, 4 cạnh màn hình liên tục phát xung nhấp nháy đỏ báo động.
* **Cơ chế hồi phục (`healPlayer(int amount)`)**:
  * Tăng máu: `playerCurrentHealth = Math.min(playerMaxHealth, playerCurrentHealth + amount)`.
  * Không bao giờ vượt quá trần `playerMaxHealth` (100 HP).

### 2.2 Kỹ Năng Né Đòn Chiến Thuật (Combat Dash / Dodge Roll) & i-frames
* **Kích hoạt**: Bấm phím **`[SPACE]`** (`KeyEvent.VK_SPACE`).
* **Thời lượng lướt né (`DODGE_DURATION`)**: **`0.22 giây`**.
* **Vận tốc lướt xung kích (`DODGE_SPEED`)**: **`560.0 px/s`** (~2.6 lần vận tốc chạy bộ thường `210.0 px/s`), lướt qua quãng đường ~123px (~3.8 ô gạch).
* **Thời gian hồi chiêu (`DODGE_COOLDOWN`)**: **`0.80 giây`** kể từ thời điểm bắt đầu né, chống lạm dụng né liên tục.
* **Hướng lướt né**:
  * Nếu người chơi đang giữ các phím di chuyển `WASD`: Lướt theo vector tổng hợp của hướng di chuyển (chuẩn hóa độ dài 1.0).
  * Nếu người chơi đang đứng yên: Lướt theo hướng tâm ngắm chuột (`aimAngle`).
* **Cơ chế Bất tử (i-frames)**:
  * Trong suốt `0.22 giây` lướt né, `isInvulnerable()` trả về `true`.
  * Nhân vật hoàn toàn miễn nhiễm với tất cả các loại sát thương: Đạn súng máy, đạn shotgun, pháo nổ AoE của Boss, đòn chém cận chiến.
* **Hiệu ứng đồ họa (FX)**:
  * Vệt bóng ma tàn ảnh (Ghost Trail): Cứ mỗi `0.04s` sinh một tàn ảnh bóng ma mờ dần màu Cyan phía sau nhân vật (tồn tại `0.20s`).
  * Vòng hào quang xung chấn khí động học bao quanh nhân vật trong suốt thời gian né.
* **Thanh hồi chiêu trên Giao diện HUD**:
  * Đặt ngay dưới thanh máu người chơi (`x = 20, y = 88, w = 210, h = 15`).
  * Khi sẵn sàng: Hiển thị đầy thanh màu Cyan `[SPACE] NÉ ĐÒN : SẴN SÀNG`.
  * Khi đang né: Xung điện phát sáng `⚡ [SPACE] ĐANG NÉ (i-frames)`.
  * Khi hồi chiêu: Thanh tiến trình nạp dần kèm số giây đếm ngược `[SPACE] HỒI CHIÊU: X.Xs`.

### 2.3 Giao diện Thanh Máu Người Chơi (Player Health Bar HUD)
* **Vị trí**: Đặt cố định ở góc trên bên trái màn hình (`x = 20, y = 60, w = 210, h = 22`), ngay dưới bảng Sector HUD.
* **Quy tắc thiết kế**: **Tuyệt đối KHÔNG hiển thị thanh máu trên đầu người chơi** để giữ tầm nhìn chiến thuật thông thoáng.
* **Hiển thị thông minh**:
  * Dải màu biến đổi:
    * $>50\%$ HP: Xanh ngọc lục bảo tươi (Emerald Green).
    * $25\% - 50\%$ HP: Vàng hổ phách cảnh báo (Amber Yellow).
    * $\le 25\%$ HP: Đỏ rực nguy cấp (Crimson Red).
  * Chữ số rõ ràng: `✚ HP: X / 100`.

### 2.4 Vật Phẩm Hồi Phục Rơi Trên Sàn (`HealthPickup.java`)
* **Quy tắc tương tác**:
  * **Tuyệt đối KHÔNG tự động nhặt** khi đi ngang qua.
  * Người chơi phải bước lại gần (bán kính $\le 45\text{px}$) và **bấm phím `[F]`** để sử dụng.
  * Khi lại gần: Hiển thị bảng gợi ý `[F] DÙNG HỘP CỨU THƯƠNG (+X HP)`.
  * Nếu máu của người chơi đã đầy ($100/100$ HP): Hệ thống từ chối tiêu thụ, hiển thị cảnh báo `MÁU ĐÃ ĐẠT TỐI ĐA! KHÔNG CẦN CỨU THƯƠNG` để bảo tồn vật phẩm trên sàn.
* **Hai kích cỡ**:
  * **Hộp Sơ Cứu Nhỏ (Small Medkit)**: Hồi **`+25 HP`**, hộp trắng viền xanh lá, biểu tượng chữ thập xanh.
  * **Hộp Cứu Thương Lớn (Large Medkit)**: Hồi **`+50 HP`**, hộp bọc giáp viền vàng, biểu tượng chữ thập phát sáng.
* **Cơ chế rơi trong màn chơi**:
  * **Khi Clear phòng chiến đấu**: Có tỉ lệ rất nhỏ (**~15%**) rơi thêm một Hộp Sơ Cứu Nhỏ trên sàn cạnh hòm tiếp tế.
  * **Khi hạ gục Titan Mecha Boss**: Đảm bảo rơi **1 Hộp Cứu Thương Lớn (+50 HP)** tại tọa độ boss bị tiêu diệt.

### 2.5 Quầy Cứu Thương Tại Shop (`ShopItem.java`)
* **Vị trí**: Nằm tại quầy tiếp tế bên phải tâm phòng Shop (`cx + 110, cy`).
* **Quy tắc xuất hiện**: **Xuất hiện ngẫu nhiên với vị trí của hộp tiếp đạn, và có thể thay thế nhau cho mỗi stage** (50% cơ hội là Hộp Tiếp Đạn +2 Stack giá 70C, 50% cơ hội là Trạm Cứu Thương Nano giá 75C).
* **Hiệu quả**: Hồi phục **`+50 HP`** khi bấm `[F]`.
* **Cơ chế bảo vệ**: Nếu máu đang đầy (100 HP), từ chối giao dịch và không trừ Credits.

### 2.6 Chuỗi Hoạt Cảnh Tử Trận & Màn Hình Kết Quả (Death Slide Sequence & Game Over)
* Khi `playerCurrentHealth == 0`:
  * Kích hoạt `isPlayerDead = true` và khởi động chuỗi hoạt cảnh tử trận kéo dài **`2.0 giây`**.
  * **Tuyệt đối KHÔNG hiện màn hình Game Over đột ngột**: Người chơi có thời gian chứng kiến nhân vật của mình ngã xuống chiến trường.
  * **Đầu Lâu Trượt Trên Mặt Đất (Sliding Skull)**:
    * Nhân vật lập tức biến thành hình ảnh **Đầu lâu chiến binh (Fallen Operative Skull)** màu trắng ngà xương với 2 hốc mắt to màu đen sâu thẳm le lói đốm lửa ma trơi, hốc mũi tam giác, hàng răng xương trắng và vết nứt sọ chiến trường bi tráng.
    * Đầu lâu trượt và xoay lăn trên mặt đất theo quán tính di chuyển hoặc lực hất văng từ đòn đánh cuối cùng ($v_0 \approx 280\text{ px/s}$).
    * Tốc độ trượt giảm dần theo lực ma sát sàn hầm ngục, để lại các vệt khói bụi tro ma sát (`SkidParticle`) cọ xát sau lưng.
    * Va chạm địa hình: Nếu trượt chạm vào tường đá, đầu lâu dội nhẹ lại rồi tiếp tục giảm tốc tự nhiên, không bị kẹt hay xuyên tường.
    * Camera tiếp tục mượt mà bám theo đầu lâu đang trượt.
  * **Hiệu ứng chuyển cảnh điện ảnh (Cinematic Death Fade & Vignette)**:
    * Màn hình từ từ phủ bóng tối mờ ảo và viền đỏ bi tráng quanh 4 cạnh (`renderDeathVignetteTransition`).
    * Từ 21/10/2026: hoạt cảnh trượt 2.0s KHÔNG thể bỏ qua bằng bất kỳ phím nào; chỉ sau khi kết thúc mới nhấn [ENTER]/[R] chơi lại hoặc [ESC] ra menu.
  * **Màn hình Game Over chính thức**:
    * Khi đầu lâu đã dừng trượt hẳn (sau 2.0s) hoặc khi người chơi nhấn bỏ qua, bảng thống kê Game Over mới xuất hiện đầy đủ: Stage đạt được, Credits thu thập, số phòng combat dọn sạch, số vũ khí trong kho.
    * Phím điều hướng: Bấm `[ENTER]` hoặc `[R]` để tái xuất phát run mới; bấm `[ESC]` để trở về Menu chính.

---

## 3. API Liên quan

### `Player.java`
* `public boolean triggerDodge(boolean up, boolean down, boolean left, boolean right)`: Kích hoạt né đòn lướt xung kích, tính hướng theo WASD hoặc tâm ngắm aimAngle.
* `public boolean isDodging()`: Kiểm tra người chơi có đang trong trạng thái lướt né hay không.
* `public boolean isInvulnerable()`: Kiểm tra trạng thái bất tử (i-frames: đang né hoặc bộ đếm invulnerableTimer > 0).
* `public double getDodgeCooldownTimer()`: Lấy thời gian hồi chiêu né còn lại.
* `public double getDodgeProgress()`: Tỉ lệ nạp hồi chiêu từ 0.0 đến 1.0 phục vụ hiển thị thanh HUD.
* `public boolean takeDamage(int damage)`: Gây sát thương kèm kiểm tra miễn nhiễm i-frames khi đang né.
* `public void heal(int amount)`: Hồi phục máu bị chặn bởi maxHealth.

### `PlayingState.java`
* `public boolean triggerPlayerDodge(boolean up, boolean down, boolean left, boolean right)`: Delegation gọi né đòn.
* `public boolean isPlayerDodging()`: Delegation kiểm tra trạng thái né.
* `public double getPlayerDodgeCooldownTimer()`: Delegation lấy thời gian hồi chiêu né.
* `public void damagePlayer(int damage)`: Gây sát thương kèm thời gian bất tử i-frame.
* `public void healPlayer(int amount)`: Hồi phục máu bị chặn bởi maxHealth.

### `HealthPickup.java`
* `public boolean isPlayerInRange(double px, double py, double pSize)`: Kiểm tra người chơi trong tầm bấm [F] ($\le 45\text{px}$).
* `public boolean interact(PlayingState state)`: Xử lý tiêu thụ hộp cứu thương khi bấm [F].
* `public int getHealAmount()`: Lấy định lượng máu hồi phục (+25 hoặc +50).
* `public boolean isUsed()`: Kiểm tra hộp cứu thương đã được dùng hay chưa.

---

## 4. Sửa đổi / Cập nhật
* **21/10/2026**:
  - Triển khai hoạt cảnh tử trận (Death Slide Sequence): Khi người chơi chết, không hiện bảng Game Over đột ngột mà kích hoạt 2.0 giây chuyển cảnh bi tráng.
  - Nhân vật biến thành Đầu lâu chiến binh (Fallen Operative Skull) trượt ma sát trên sàn hầm ngục, để lại vệt cọ xát bụi tro `SkidParticle` và xoay lăn theo quán tính.
  - Tích hợp hiệu ứng điện ảnh chuyển mờ tối và viền đỏ bi tráng `renderDeathVignetteTransition`.
  - (Đã bỏ 21/10/2026) Hoạt cảnh trượt không còn cho phép skip.
* **20/10/2026**:
  - Tích hợp kỹ năng né đòn chiến thuật (Combat Dash / Dodge Roll) kích hoạt bằng phím `[SPACE]`.
  - Bổ sung cơ chế bất tử (i-frames) trong suốt 0.22s lướt né, vô hiệu hóa 100% sát thương từ mọi nguồn đạn và va chạm.
  - Tích hợp hiệu ứng tàn ảnh bóng ma (Ghost Trail) màu Cyan và vòng hào quang khí động học xung chấn.
  - Tích hợp thanh trạng thái hồi chiêu né đòn tinh xảo trên HUD ngay dưới thanh máu người chơi.
* **18/10/2026**: 
  - Khởi tạo toàn diện Hệ thống Sinh lực (HP) và Vật phẩm hồi phục cho người chơi.
  - Tích hợp thời gian bất tử i-frames (0.8s), rung màn hình khi trúng đòn, cảnh báo viền đỏ nguy hiểm khi máu thấp.
  - Tích hợp màn hình Game Over khi tử trận.
  - Triển khai `HealthPickup` với cơ chế bấm phím `[F]` thủ công, không tự nhặt, bảo toàn khi đầy máu.
  - Cập nhật quầy Shop hoán đổi ngẫu nhiên giữa Hộp Đạn và Hộp Cứu Thương theo từng stage.
  - Tích hợp tỉ lệ rất nhỏ (~15%) rơi hộp cứu thương khi dọn sạch phòng chiến đấu.
