Cập nhật mới nhất vào ngày: 22/10/2026

# Kẻ Địch Ném Lựu Đạn - Lính Công Binh Hạng Nặng (Grenadier Enemy)

## 1. Mô tả
`GrenadierEnemy` là đơn vị hỏa lực khống chế diện rộng (Area Denial / Zoning Specialist) của lực lượng đối địch trong trò chơi DontKnow.
Đặc điểm then chốt:
* **Hỏa lực Lựu đạn Hẹn giờ (`EnemyGrenade`)**: Ném lựu đạn hẹn giờ bay bổng theo quỹ đạo parabol về hướng vị trí hiện tại của Operative. Lựu đạn đáp xuống đất, đếm ngược ngòi nổ **chính xác 2.0 giây** rồi phát nổ hủy diệt trên diện rộng bán kính $78\text{px}$.
* **Khống chế & Ép góc (Area Denial / Zoning)**:
  - Khi quả lựu đạn nằm trên mặt đất, một vòng tròn cảnh báo nguy hiểm màu đỏ dồn dập (AoE Danger Warning Circle) phát sáng nhấp nháy liên tục cùng đồng hồ đếm ngược.
  - Buộc người chơi phải di chuyển đổi vị trí, không thể ẩn nấp lâu sau vật cản hoặc camp góc phòng.
  - Sức công phá của vụ nổ gây $24\text{ HP}$ sát thương và phá hủy ngay lập tức các vật cản (Destructible Obstacles / Cover) nằm trong bán kính vụ nổ.
* **Hành vi AI Chiến Thuật (Tactical AI)**:
  - Giữ khoảng cách tầm trung ổn định ($175\text{px} - 400\text{px}$).
  - Nếu người chơi áp sát $< 175\text{px}$: Lùi lại (Backpedal).
  - Nếu người chơi ra xa $> 400\text{px}$: Tiến lên (Advance).
  - Nếu ở cự ly lý tưởng: Di chuyển ngang (Strafe) quanh người chơi để né đạn và ngắm ném lựu đạn mỗi $3.0\text{s}$.
* **Thời điểm xuất hiện**: Bắt đầu xuất hiện từ **Stage 3** (High-tech Stronghold), và xuất hiện trong đội cận vệ hộ tống của Boss tại **Stage 4 (Boss Arena)**.

---

## 2. Quy trình hoạt động

### 2.1. Chu kỳ AI Logic (`updateAI`)
1. **Kiểm tra khoảng cách & Định hướng di chuyển**:
   - Tính vector khoảng cách:
     $$\text{dist} = \sqrt{(\text{playerCenterX} - x)^2 + (\text{playerCenterY} - y)^2}$$
   - Nếu $\text{dist} < \text{preferredMinDist}$ ($175\text{px}$): Lùi xa khỏi người chơi.
   - Nếu $\text{dist} > \text{preferredMaxDist}$ ($400\text{px}$): Tiến tới áp sát người chơi.
   - Nếu nằm trong khoảng $[175\text{px}, 400\text{px}]$: Di chuyển trượt ngang vuông góc vector ngắm bắn để né hỏa lực của người chơi.
2. **Quy trình ném Lựu đạn**:
   - Bộ đếm hồi chiêu `grenadeCooldownTimer` giảm dần theo `deltaTime`.
   - Khi hồi chiêu kết thúc và cự ly với người chơi nằm trong tầm ném hiệu dụng ($90\text{px} \le \text{dist} \le 460\text{px}$):
     - Kiểm tra đường ngắm thẳng `dungeonMap.hasLineOfSight(...)`.
     - Nếu có đường ngắm quang học rõ ràng: Khởi tạo thực thể `EnemyGrenade` xuất phát từ tâm Grenadier và hướng bay về phía vị trí Operative.
     - Đặt lại thời gian hồi chiêu là $3.0\text{s}$ (kèm biến thiên ngẫu nhiên nhỏ $\pm 0.3\text{s}$ để tránh nhịp điệu máy móc).
     - Kích hoạt hoạt ảnh giật nòng ném lựu đạn (`throwArmTimer = 0.25s`).

### 2.2. Vòng đời Lựu đạn Kẻ địch (`EnemyGrenade`)
1. **Pha Bay (Arc Travel Phase - $0.5\text{s}$ đầu)**:
   - Bay theo sơ tốc $220.0\text{ px/s}$ về phía tọa độ mục tiêu.
   - Độ cao giả lập $z$ tạo hiệu ứng bóng đổ và độ phóng to thu nhỏ của quả lựu đạn.
   - Nếu va chạm góc tường trước khi hết pha bay, lựu đạn nảy lại và dừng lại tại vị trí tiếp đất.
2. **Pha Đếm ngược Ngòi nổ (Arming & Ticking Phase - Tổng thời gian ngòi nổ $2.0\text{s}$)**:
   - Lựu đạn dừng cố định trên mặt đất.
   - Bán kính nguy hiểm $78\text{px}$ được vẽ rõ ràng xuống nền sàn:
     - Vòng tròn viền đỏ nhấp nháy với tần số tăng dần khi sắp nổ ($t \to 0$).
     - Đèn LED trung tâm chớp đỏ báo động.
   - **Đặc tính chiến thuật**: Người chơi chạm vào lựu đạn khi đang đếm ngược sẽ không bị nổ tức thì (khác với đạn thường), mà buộc phải chạy ra khỏi bán kính cảnh báo trước khi hết 2.0 giây.
3. **Pha Phát nổ (Detonation Phase)**:
   - Khi `fuseTimer <= 0`:
     - Kích hoạt `explode(playingState)`.
     - Kiểm tra khoảng cách tới người chơi: Nếu khoảng cách từ tâm vụ nổ tới tâm Operative $\le 78\text{px}$, người chơi chịu $24\text{ HP}$ sát thương.
     - Phá hủy mọi chướng ngại vật có thể phá hủy (`DestructibleObstacle`) trong phạm vi bán kính nổ.
     - Tạo hiệu ứng sóng xung kích cam đỏ rực lửa, tia lửa văng đa hướng và rung lắc màn hình nhẹ.
     - Đánh dấu thực thể hết hiệu lực (`deactivate()`).

---

## 3. Thông số kỹ thuật (Specifications)

| Thuộc tính | Giá trị mặc định | Giải thích vai trò |
|---|---|---|
| Kích thước (Hitbox) | $32 \times 32\text{px}$ | Kích thước đầm chắc của lính công binh hạng nặng |
| Máu tối đa (`maxHealth`) | $55 - 75\text{ HP}$ | Giáp trung bình khá |
| Tốc độ di chuyển (`speed`) | $95.0\text{ px/s}$ | Di chuyển đầm chắc hơn Ranged Scout để tập trung hỏa lực |
| Sát thương vụ nổ (`grenadeDamage`) | $24\text{ HP}$ | Sát thương nổ diện rộng lớn, phạt nặng người chơi đứng yên |
| Bán kính vụ nổ (`blastRadius`) | $78.0\text{ px}$ | Diện tích vùng ép góc đủ rộng để phủ trọn 1 góc phòng |
| Thời gian ngòi nổ (`fuseDuration`) | $2.0\text{s}$ | Chuẩn xác 2 giây theo yêu cầu thiết kế |
| Hồi chiêu ném lựu đạn (`grenadeInterval`) | $3.0\text{s}$ | Tần suất ném duy trì nhịp độ ép góc liên tục |
| Khoảng cách chiến thuật | $175\text{px} - 400\text{px}$ | Cự ly tầm trung an toàn để phóng lựu |

---

## 4. Đồ họa & Hiệu ứng (Visuals 100% Java Graphics2D)
* **Thực thể Grenadier**:
  - Khung giáp hạng nặng màu xanh rêu quân sự đậm (`Color(44, 62, 80)` phối `Color(39, 174, 96)`).
  - Ống phóng lựu khí nén / găng tay cơ khí phóng lựu vươn dài về phía mục tiêu.
  - Ba-lô đạn dược / khay lựu đạn gắn sau lưng với đèn tín hiệu vàng cam.
* **Quả Lựu Đạn (`EnemyGrenade`)**:
  - Thân vỏ hình quả trám/hình trứng màu xám chì đậm viền vàng cảnh báo.
  - Đèn LED ngòi nổ chớp đỏ theo nhịp tim dồn dập ($f_{\text{blink}} \propto \frac{1}{\text{fuseTimer}}$).
  - Vùng cảnh báo nổ (Hazard Circle): Vòng cung tròn $78\text{px}$ màu đỏ mờ bán trong suốt trải trên sàn gạch, kèm viền đứt nét xoay cảnh báo.

* **Thanh Máu & Giao Diện Trên Đầu**:
  - Thanh máu trên đầu luôn luôn hiển thị màu đỏ `new Color(230, 45, 45)` của kẻ địch.
  - **Loại bỏ hoàn toàn tên / nhãn text trên đầu** để giao diện tối giản, tập trung vào chiến đấu.

---

## 5. Quan hệ với hệ thống khác
* **`TargetDummy`**: Kế thừa để quản lý máu, trạng thái sống/chết và logic nhận sát thương.
* **`EnemyGrenade`**: Projectile chuyên biệt được sinh ra bởi Grenadier, quản lý bởi `ProjectileManager`.
* **`PlayingState`**: Điều phối triệu hồi lính vào phòng chiến đấu Stage 3 & Stage 4 Boss Arena, tích hợp xử lý né tránh và kiểm tra phá hủy chướng ngại vật.
* **`DestructibleObstacle`**: Bị lựu đạn phá hủy dọn dẹp mặt bằng phòng.

---

## 6. Sửa đổi / Cập nhật
* **22/10/2026**:
  - Khởi tạo mới lớp `GrenadierEnemy` và `EnemyGrenade`.
  - Triển khai ngòi nổ hẹn giờ chuẩn xác 2.0s và vòng tròn cảnh báo nguy hiểm AoE bán kính 78px.
  - Tích hợp phá hủy vật cản và ép góc người chơi.
  - Chuẩn hóa thanh máu trên đầu **luôn luôn là màu đỏ** và **loại bỏ tên trên đầu kẻ địch**.
  - Phân tầng xuất hiện tại Stage 3 và Stage 4 Boss Arena.
