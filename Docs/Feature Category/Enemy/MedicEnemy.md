Cập nhật mới nhất vào ngày: 22/10/2026

# Kẻ Địch Hỗ Trợ - Lính Quân Y (Medic Enemy)

## 1. Mô tả
`MedicEnemy` là đơn vị hỗ trợ chiến thuật (Tactical Support Unit) của lực lượng phản diện trong trò chơi DontKnow. 
Đặc điểm then chốt:
* **Hoàn toàn không có khả năng chiến đấu trực tiếp**: Không mang vũ khí tấn công, không gây sát thương lên người chơi qua đòn đánh hay đạn đạo.
* **Hào quang Hồi Máu (Healing Aura)**: Phát ra một vòng tròn năng lượng nano-repair màu xanh ngọc bích (emerald green) với bán kính 135px lấy chính nó làm tâm. Mọi đồng minh (MeleeEnemy, RangedPistolEnemy, GrenadierEnemy, BossTank, v.v.) đứng trong vùng hào quang này sẽ được hồi máu liên tục định kỳ.
* **Hành vi AI Chiến Thuật (Support AI)**:
  1. *Tránh né Người chơi (Player Evasion / Flee)*: Nếu người chơi áp sát trong phạm vi cảnh giác ($< 210\text{px}$), Medic lập tức quay lưng bỏ chạy theo hướng ngược lại để tự bảo toàn mạng sống.
  2. *Tiếp cận đồng đội bị thương (Prioritized Ally Triage)*: Quét toàn bộ kẻ địch còn sống trong phòng, tìm ra đồng minh có tỉ lệ máu thấp nhất ($\text{currentHealth} / \text{maxHealth}$) và chưa đầy máu để di chuyển tới hỗ trợ.
  3. *Trú ẩn / Neo giữ (Hold & Orbit)*: Khi ở gần đồng đội bị thương ($< 80\text{px}$), Medic giữ khoảng cách ổn định và kích hoạt hào quang hồi phục.
* **Thời điểm xuất hiện**: Bắt đầu xuất hiện từ **Stage 2** (Laboratory / Complex), và xuất hiện trong đội hình cận vệ của Boss tại **Stage 4 (Boss Arena)**.

---

## 2. Quy trình hoạt động

### 2.1. Chu kỳ AI Logic (`updateAI`)
1. **Kiểm tra nguy hiểm từ Operative**:
   - Tính vector tới người chơi:
     $$\Delta x = \text{playerCenterX} - x, \quad \Delta y = \text{playerCenterY} - y, \quad \text{dist} = \sqrt{\Delta x^2 + \Delta y^2}$$
   - Nếu $\text{dist} < \text{fleeRadius}$ (mặc định $210\text{px}$): Medic chuyển sang trạng thái hoảng loạn/chạy trốn, di chuyển ngược vector người chơi:
     $$v_x = -\frac{\Delta x}{\text{dist}} \times \text{speed}, \quad v_y = -\frac{\Delta y}{\text{dist}} \times \text{speed}$$
     Áp dụng trượt va chạm địa hình `dungeonMap.isAreaWalkable`.
2. **Quét tìm đồng minh cần sơ cứu (Ally Scanning)**:
   - Nếu không bị người chơi đe dọa trực tiếp, Medic lặp qua danh sách `playingState.getEnemies()`.
   - Bỏ qua bản thân (`other != this`) và chỉ xét các thực thể còn sống (`other.isAlive()`).
   - Ưu tiên đồng đội có tỉ lệ phần trăm máu còn lại thấp nhất:
     $$\text{healthRatio} = \frac{\text{other.getCurrentHealth()}}{\text{other.getMaxHealth()}} < 1.0$$
   - Nếu tìm thấy, Medic xác định mục tiêu và di chuyển áp sát đồng minh đó. Khi vào cự ly $\le 80\text{px}$, Medic dừng lại để duy trì vòng tròn hồi phục bao phủ đồng đội.
3. **Phát xung Hồi Máu (Heal Pulse Pulse Cycle)**:
   - Bộ đếm thời gian `healPulseTimer` tích lũy `deltaTime`.
   - Khi `healPulseTimer >= healInterval` (mặc định $0.8\text{s}$):
     - Quét lại danh sách kẻ địch nằm trong bán kính `healAuraRadius` (mặc định $135\text{px}$).
     - Thực hiện gọi `ally.heal(healAmount)` (mặc định hồi $6\text{ HP}$ mỗi nhịp xung).
     - Khởi tạo hiệu ứng sóng nano mở rộng (`auraPulseEffect`) để biểu diễn trực quan.

---

## 3. Thông số kỹ thuật (Specifications)

| Thuộc tính | Giá trị mặc định | Giải thích vai trò |
|---|---|---|
| Kích thước (Hitbox) | $30 \times 30\text{px}$ | Thực thể hình hộp tròn tương đồng các lính thông thường |
| Máu tối đa (`maxHealth`) | $50 - 65\text{ HP}$ | Lượng máu mỏng hơn lính chiến đấu, cần được bảo vệ |
| Tốc độ di chuyển (`speed`) | $110.0\text{ px/s}$ | Tốc độ nhanh nhẹn giúp né tránh người chơi hiệu quả |
| Bán kính hào quang (`healAuraRadius`) | $135.0\text{ px}$ | Phạm vi vòng tròn hồi máu lấy Medic làm tâm |
| Lượng máu hồi mỗi nhịp (`healAmount`) | $6\text{ HP}$ | Hồi máu lũy tiến, tạo áp lực buộc người chơi phải ưu tiên diệt Medic trước |
| Tần suất hồi (`healInterval`) | $0.8\text{s}$ | Chu kỳ phát xung nano hồi phục |
| Bán kính cảnh giác né tránh (`fleeRadius`) | $210.0\text{ px}$ | Khoảng cách người chơi kích hoạt phản xạ chạy trốn của Medic |

---

## 4. Đồ họa & Hiệu ứng (Visuals 100% Java Graphics2D)
* **Vòng tròn Hào quang Hồi Máu (Healing Aura Ring - Red Hostile Zone)**:
  - **Tách Layer Độc Lập**: Phương thức `renderAura(g, camera)` được vẽ tại layer mặt đất sàn dungeon, **nằm ở dưới layer của toàn bộ các entity** (người chơi, kẻ địch, vật cản, trang bị) để không che khuất thực thể.
  - Vòng tròn nền phát quang màu đỏ thẫm mờ `new Color(220, 35, 45, 24)`.
  - Viền phát quang xoay đều với đường nét đứt đỏ cảnh báo `new Color(255, 60, 60, 135)`.
  - 4 phù hiệu chữ thập đỏ ở 4 hướng trên vành đai hào quang.
  - Sóng xung (Pulse Ripple) màu đỏ rực mở rộng ra ngoài mỗi khi nhịp hồi phục kích hoạt.
* **Ngoại hình Thực thể (Military Gray Hostile Chassis)**:
  - Thân vỏ hợp kim titan màu xám quân sự đậm `new Color(68, 74, 84)` (ăn khớp màu xám mặc định của lực lượng kẻ địch).
  - Tấm giáp bảo vệ bên trong xám đen carbon `new Color(48, 53, 62)`.
  - Đường viền giáp màu đỏ thẫm nguy hiểm `new Color(195, 45, 50)`.
  - Phù hiệu chữ thập y tế màu đỏ rực `#E62828` (`new Color(230, 40, 40)`) với lõi phát sáng hồng đỏ.
  - Cảm biến mắt quang học LED đỏ sắc nhọn `new Color(255, 60, 60)`.
  - Khe xả nhiệt phản lực phía sau phát ánh đỏ.
* **Thanh Máu & Giao Diện Trên Đầu**:
  - Thanh máu trên đầu luôn luôn hiển thị màu đỏ `new Color(230, 45, 45)` đặc trưng của phe địch.
  - **Loại bỏ hoàn toàn tên / nhãn text trên đầu** để giữ màn hình thông thoáng, chuyên nghiệp.

---

## 5. Quan hệ với hệ thống khác
* **`TargetDummy`**: Lớp cơ sở kế thừa. Medic ghi đè `updateAI` và `render` chuyên biệt.
* **`PlayingState`**: Quản lý danh sách enemies; triệu hồi Medic tại Stage 2, 3 và 4 Boss Arena; gọi `renderAura` tại layer mặt đất trước khi vẽ các entity.
* **`DungeonMap`**: Kiểm tra va chạm di chuyển trượt trên địa hình.
* **Các Archetype khác**: Hưởng lợi trực tiếp từ `heal(amount)` khi đứng gần Medic.

---

## 6. Sửa đổi / Cập nhật
* **22/10/2026**:
  - Khởi tạo mới lớp `MedicEnemy`.
  - Triển khai thuật toán né tránh người chơi và ưu tiên cứu thương đồng đội yếu máu nhất.
  - Tích hợp vòng tròn hào quang $135\text{px}$ phát xung $0.8\text{s}$ hồi $6\text{ HP}$.
  - Chuyển đổi vùng hào quang sang **màu đỏ** và hạ xuống **layer mặt sàn ở dưới các entity**.
  - Tinh chỉnh màu thân thành **màu xám quân sự** kèm chi tiết đỏ nguy hiểm của phe phản diện.
  - Chuẩn hóa thanh máu trên đầu **luôn luôn là màu đỏ** và **loại bỏ tên trên đầu kẻ địch**.
  - Phân tầng xuất hiện tại Stage 2 trở lên.
