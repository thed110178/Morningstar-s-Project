Cập nhật mới nhất vào ngày: 18/10/2026

# PlayerHealthSystem.md - Hệ Thống Sinh Lực (HP) & Vật Phẩm Hồi Phục Cho Người Chơi

## 1. Mô tả
Tài liệu đặc tả kiến trúc quản lý sinh lực của nhân vật người chơi (Operative), cơ chế nhận sát thương an toàn với thời gian bất tử (i-frames), phản hồi thị giác khi bị thương, trạng thái tử trận (Game Over), thanh máu cố định trên HUD màn hình, và hệ thống vật phẩm hồi phục tương tác thủ công bằng phím `[F]`.

---

## 2. Quy trình hoạt động

### 2.1 Chỉ số Sinh Lực & Nhận Sát Thương
* **Máu tối đa (`maxHealth`)**: Mặc định **`100 HP`**.
* **Máu hiện tại (`playerCurrentHealth`)**: Khởi đầu **`100 HP`**.
* **Cơ chế nhận sát thương (`damagePlayer(int damage)`)**:
  * Khi dính đòn (đạn hoặc cận chiến từ kẻ địch), nếu đang trong thời gian bất tử (`invulnerableTimer > 0`), toàn bộ sát thương bị vô hiệu hóa.
  * Khi không bất tử: Trừ máu `playerCurrentHealth = Math.max(0, playerCurrentHealth - damage)`.
  * Kích hoạt thời gian bất tử (i-frames): **`0.8 giây`** (`invulnerableTimer = 0.8`).
  * Kích hoạt rung màn hình nhẹ (*Camera Screen Shake*): **`0.2 giây`** với biên độ ngẫu nhiên $\pm 3\text{px}$.
  * Hiệu ứng chớp nhấp nháy đỏ / trong suốt trên thân nhân vật trong suốt thời gian bất tử.
  * Cảnh báo viền đỏ nguy hiểm (*Danger Vignette*): Khi máu tụt xuống $\le 30\%$ và chưa chết, 4 cạnh màn hình liên tục phát xung nhấp nháy đỏ báo động.
* **Cơ chế hồi phục (`healPlayer(int amount)`)**:
  * Tăng máu: `playerCurrentHealth = Math.min(playerMaxHealth, playerCurrentHealth + amount)`.
  * Không bao giờ vượt quá trần `playerMaxHealth` (100 HP).

### 2.2 Giao diện Thanh Máu Người Chơi (Player Health Bar HUD)
* **Vị trí**: Đặt cố định ở góc trên bên trái màn hình (`x = 20, y = 114, w = 210, h = 22`), ngay dưới bảng Sector HUD.
* **Quy tắc thiết kế**: **Tuyệt đối KHÔNG hiển thị thanh máu trên đầu người chơi** để giữ tầm nhìn chiến thuật thông thoáng.
* **Hiển thị thông minh**:
  * Dải màu biến đổi:
    * $>50\%$ HP: Xanh ngọc lục bảo tươi (Emerald Green).
    * $25\% - 50\%$ HP: Vàng hổ phách cảnh báo (Amber Yellow).
    * $\le 25\%$ HP: Đỏ rực nguy cấp (Crimson Red).
  * Chữ số rõ ràng: `✚ HP: X / 100`.

### 2.3 Vật Phẩm Hồi Phục Rơi Trên Sàn (`HealthPickup.java`)
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

### 2.4 Quầy Cứu Thương Tại Shop (`ShopItem.java`)
* **Vị trí**: Nằm tại quầy tiếp tế bên phải tâm phòng Shop (`cx + 110, cy`).
* **Quy tắc xuất hiện**: **Xuất hiện ngẫu nhiên với vị trí của hộp tiếp đạn, và có thể thay thế nhau cho mỗi stage** (50% cơ hội là Hộp Tiếp Đạn +2 Stack giá 70C, 50% cơ hội là Trạm Cứu Thương Nano giá 75C).
* **Hiệu quả**: Hồi phục **`+50 HP`** khi bấm `[F]`.
* **Cơ chế bảo vệ**: Nếu máu đang đầy (100 HP), từ chối giao dịch và không trừ Credits.

### 2.5 Màn Hình Tử Trận (Game Over Screen)
* Khi `playerCurrentHealth == 0`:
  * Kích hoạt `isPlayerDead = true`.
  * Đóng băng di chuyển và bắn súng.
  * Phủ màn mờ đỏ đen kịch tính, hiển thị tiêu đề `☠ CHIẾN BINH TỬ TRẬN ☠ (OPERATIVE KIA)`.
  * Bảng thống kê chi tiết ván đấu: Stage đạt được, Credits thu thập, số phòng combat dọn sạch, số vũ khí trong kho.
  * Phím điều hướng: Bấm `[ENTER]` hoặc `[R]` để tái xuất phát run mới; bấm `[ESC]` để trở về Menu chính.

---

## 3. API Liên quan

### `PlayingState.java`
* `public void damagePlayer(int damage)`: Gây sát thương kèm thời gian bất tử i-frame.
* `public void healPlayer(int amount)`: Hồi phục máu bị chặn bởi maxHealth.
* `public int getPlayerMaxHealth()`: Lấy máu tối đa.
* `public int getPlayerCurrentHealth()`: Lấy máu hiện tại.
* `public void setPlayerCurrentHealth(int health)`: Đặt máu hiện tại.
* `public boolean isPlayerDead()`: Kiểm tra đã tử trận hay chưa.
* `public double getInvulnerableTimer()`: Lấy thời gian bất tử còn lại.
* `public List<HealthPickup> getHealthPickups()`: Lấy danh sách hộp cứu thương đang có trên sàn.

### `HealthPickup.java`
* `public boolean isPlayerInRange(double px, double py, double pSize)`: Kiểm tra người chơi trong tầm bấm [F] ($\le 45\text{px}$).
* `public boolean interact(PlayingState state)`: Xử lý tiêu thụ hộp cứu thương khi bấm [F].
* `public int getHealAmount()`: Lấy định lượng máu hồi phục (+25 hoặc +50).
* `public boolean isUsed()`: Kiểm tra hộp cứu thương đã được dùng hay chưa.

---

## 4. Sửa đổi / Cập nhật
* **18/10/2026**: 
  - Khởi tạo toàn diện Hệ thống Sinh lực (HP) và Vật phẩm hồi phục cho người chơi.
  - Tích hợp thời gian bất tử i-frames (0.8s), rung màn hình khi trúng đòn, cảnh báo viền đỏ nguy hiểm khi máu thấp.
  - Tích hợp màn hình Game Over khi tử trận.
  - Triển khai `HealthPickup` với cơ chế bấm phím `[F]` thủ công, không tự nhặt, bảo toàn khi đầy máu.
  - Cập nhật quầy Shop hoán đổi ngẫu nhiên giữa Hộp Đạn và Hộp Cứu Thương theo từng stage.
  - Tích hợp tỉ lệ rất nhỏ (~15%) rơi hộp cứu thương khi dọn sạch phòng chiến đấu.
