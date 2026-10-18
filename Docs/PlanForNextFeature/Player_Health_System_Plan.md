# Kế hoạch Triển khai: Hệ thống HP & Vật Phẩm Hồi Phục Cho Người Chơi

Cập nhật mới nhất vào ngày: 18/10/2026

## 1. Yêu cầu Người Dùng & Điều Chỉnh Thiết Kế
1. **Tương tác phím [F] để sử dụng vật phẩm hồi phục**:
   - Tuyệt đối không tự động nhặt khi đi ngang qua.
   - Khi lại gần (bán kính $\le 45\text{px}$), hiển thị gợi ý phím `[F] DÙNG HỘP CỨU THƯƠNG (+X HP)`.
   - Bấm `[F]` sẽ tiêu thụ hộp cứu thương, hồi máu và biến mất khỏi sàn.
   - Nếu máu đang đầy (100/100 HP), bấm `[F]` sẽ hiển thị cảnh báo `MÁU ĐÃ ĐẠT TỐI ĐA (100/100 HP)! KHÔNG CẦN CỨU THƯƠNG` và giữ nguyên hộp cứu thương trên sàn.
2. **Không hiện thanh máu trên đầu người chơi**:
   - Tuyệt đối không vẽ overhead health bar trên đầu nhân vật Operative.
   - Thanh máu chỉ hiển thị cố định trên giao diện HUD màn hình (dưới Sector HUD) với số đo `100 / 100 HP` và dải màu trạng thái.
3. **Vật phẩm hồi phục xuất hiện ngẫu nhiên tại vị trí hộp tiếp đạn trong Shop và thay thế nhau qua mỗi Stage**:
   - Tại vị trí quầy tiếp tế trung tâm phòng Shop (`cx, cy`): Thay vì luôn cố định là Hộp Tiếp Đạn, hệ thống sẽ ngẫu nhiên chọn Hộp Tiếp Đạn (+2 Stack đạn, giá 70C) HOẶC Hộp Cứu Thương Nano (+50 HP, giá 75C), hai vật phẩm này có thể hoán đổi/thay thế nhau cho mỗi stage.
4. **Phòng khi clear có tỉ lệ rất nhỏ mở ra vật phẩm hồi phục**:
   - Khi phòng chiến đấu được dọn sạch, ngoài hòm thưởng vũ khí và 2 stack đạn, hệ thống có tỉ lệ rất nhỏ (~15%) mở ra thêm 1 Hộp Cứu Thương trên sàn cạnh hòm thưởng.

---

## 2. Kiến trúc & Phân rã kỹ thuật

### 2.1 Mã nguồn mới: `HealthPickup.java`
- Gói: `com.groupproject.game.entity.item.HealthPickup`.
- Kích thước: `SMALL` (+25 HP), `LARGE` (+50 HP).
- Kiểm tra tương tác cự ly `isPlayerInRange(playerX, playerY, playerSize)`.
- Phương thức `interact(PlayingState state)`: Hồi máu khi bấm `[F]`.

### 2.2 Cập nhật `ShopItem.java`
- Bật `available = true` cho `ShopItemType.HEALTH`.
- Trong `purchase()`: Nếu `playerCurrentHealth >= playerMaxHealth` thì từ chối và cảnh báo `MÁU ĐÃ ĐẦY!`. Ngược lại trừ 75 Credits và hồi 50 HP.

### 2.3 Cập nhật `PlayingState.java`
- Quản lý máu: `playerMaxHealth`, `playerCurrentHealth`, `invulnerableTimer`, `screenShakeTimer`, `isPlayerDead`.
- Danh sách `healthPickups`.
- Phương thức `damagePlayer(int damage)` và `healPlayer(int amount)`.
- Shop hoán đổi ngẫu nhiên giữa Hộp Đạn và Hộp Cứu Thương theo từng stage.
- Tỉ lệ rất nhỏ (~15%) rơi hộp cứu thương khi clear phòng.
- Xử lý phím `[F]` tương tác dùng hộp cứu thương.
- Vẽ HUD máu người chơi trên màn hình (không vẽ trên đầu người chơi).
- Vẽ màn hình Game Over khi `isPlayerDead == true`.

### 2.4 Kiểm thử tự động: `PlayerHealthTest.java`
- Kiểm tra đầy đủ: Khởi đầu, nhận sát thương, i-frames, phím F dùng cứu thương, shop hoán đổi ngẫu nhiên, tỉ lệ rơi khi clear room, tử trận.
