Cập nhật mới nhất vào ngày: 04/11/2026

# Kẻ Địch Hỗ Trợ - Lính Quân Y (Medic Enemy)

## 1. Mô tả
`MedicEnemy` là đơn vị hỗ trợ chiến thuật (Tactical Support Unit) của lực lượng phản diện trong trò chơi DontKnow. 
Đặc điểm then chốt:
* **Hoàn toàn không có khả năng chiến đấu trực tiếp**: Không mang vũ khí tấn công, không gây sát thương lên người chơi qua đòn đánh hay đạn đạo.
* **Hào quang Hồi Máu (Healing Aura)**: Phát ra một vòng tròn năng lượng nano-repair màu đỏ thẫm cảnh báo với bán kính **$150\text{px}$** lấy chính nó làm tâm. Mọi đồng minh (MeleeEnemy, RangedPistolEnemy, GrenadierEnemy, BossTank, v.v.) đứng trong vùng hào quang này sẽ được hồi máu liên tục định kỳ (**$8\text{ HP}$ mỗi $0.70\text{s}$**).
* **Tự hồi phục nano (Nano Self-Repair)**: Mỗi khi phát xung hào quang, bản thân Medic cũng tự động hồi phục **$4\text{ HP}$** nếu đang bị thương, giúp gia tăng đáng kể độ sống sót.
* **Hành vi AI Chiến Thuật Đỉnh Cao (Smart Support & Tactical Movement)**:
  1. *Bộ Lọc Ngõ Cụt & Góc Chết (Dead-End & Corner Pocket Detection)*: Tuyệt đối không bao giờ chui vào góc tường hoặc ngách cụt (như ngách sau thùng gỗ) để làm nơi nấp. Medic sử dụng thuật toán quét đa tia 16 hướng đo độ mở không gian (`calculateOpennessScore`), nhận diện chính xác các vị trí bị bao vây 2 phía vuông góc hoặc 3 phía (`isDeadEndOrPocket`) để loại bỏ hoàn toàn khỏi danh sách Cover.
  2. *Hệ Thống Né Tránh Lượn Sườn & Chống Dồn Vào Góc (Tactical Flank Steering & Wall Repulsion)*: Khi người chơi áp sát ($< 240\text{px}$), Medic không lùi thụ động thẳng vào tường sau lưng. Hệ thống tự động hòa trộn lực lùi, lực tiếp tuyến lượn ngang sườn (Tangential Flank) và lực kéo về trung tâm phòng (Room Center Attraction). Càng gần tường biên, lực hút về tâm phòng càng mạnh, giúp Medic tự bẻ lái lướt cong qua sườn người chơi để chạy ra không gian rộng mở giữa phòng.
  3. *Cơ Chế Bứt Phá Thoát Hiểm Khẩn Cấp (Emergency Breakout / Slip-Through)*: Trong tình huống bị người chơi dồn sát góc tường (phía sau bị chặn), Medic không đứng im chịu trận mà kích hoạt thuật toán Breakout quét 16 hướng $360^\circ$. Medic quyết đoán lướt qua khoảng trống bên cạnh người chơi (lệch $\ge 22^\circ$) để phóng ra ngoài ngách cụt, quay trở lại trung tâm căn phòng.
  4. *Tốc độ di chuyển tự nhiên*: Duy trì tốc độ chuẩn **$115.0\text{ px/s}$** như cũ, tạo nên sự cân bằng hoàn hảo: Medic thông minh và khó bị bắt bài nhờ chiến thuật cơ động khéo léo thay vì buff tốc độ quá đà.
  5. *Lùi về sau vật cản ẩn nấp (Cover-Seeking Behavior)*: Khi **không có đồng minh bị thương** và bị lộ mặt trước làn đạn người chơi (`!inCover`), Medic quét bán kính 8 ô gạch lân cận (~$384\text{px}$) tìm ô Cover an toàn nhất (ở không gian mở, che khuất tầm đạn, không nằm ở góc cụt) để lùi về sau vật cản.
  6. *Cơ động liên tục, không bao giờ đứng im (Dynamic Loitering & Kiting)*: Khi ở xa hoặc trong phòng trống không có cover, Medic chủ động giữ cự ly $350 - 450\text{px}$, liên tục lượn ngang đổi hướng (Tactical Strafe) thay vì đứng im làm bia tập bắn.
  7. *Tiếp cận đồng đội bị thương (Prioritized Ally Triage)*: Quét toàn bộ kẻ địch còn sống trong phòng, tìm ra đồng minh có tỉ lệ máu thấp nhất ($\text{currentHealth} / \text{maxHealth}$) để di chuyển tới bảo vệ và hồi máu.
* **Thời điểm xuất hiện**: Bắt đầu xuất hiện từ **Stage 2** (Laboratory / Complex), và xuất hiện trong đội hình cận vệ của Boss tại **Stage 4 (Grand Boss Arena)**.

---

## 2. Quy trình hoạt động

### 2.1. Chu kỳ AI Logic (`updateAI`)
1. **Kiểm tra nguy hiểm từ Operative**:
   - Nếu $\text{distToPlayer} < \text{fleeDistance}$ ($260\text{px}$): Kích hoạt Evasive Sprint ($165\text{ px/s}$). Dùng `moveWithSmartCollision` dò góc bẻ lái men theo tường né góc cụt.
2. **Quét tìm đồng minh cần sơ cứu (Ally Scanning)**:
   - Ưu tiên đồng đội có tỉ lệ phần trăm máu còn lại thấp nhất:
     $$\text{healthRatio} = \frac{\text{other.getCurrentHealth()}}{\text{other.getMaxHealth()}} < 1.0$$
   - Nếu tìm thấy, Medic xác định mục tiêu và di chuyển áp sát đồng minh đó. Khi vào cự ly $\le 0.60 \times \text{healRadius}$, Medic cơ động nhẹ quanh đồng đội.
3. **Ẩn nấp sau vật cản khi không có đồng minh bị thương (Cover-Seeking)**:
   - Nếu không có đồng minh bị thương (`healingTarget == null`):
     * Kiểm tra Line-of-Sight tới người chơi qua `isLineOfSightBlocked`.
     * Nếu bị lộ mặt (`!inCover`): Gọi `findBestCoverLocation` tìm vị trí ô sàn nằm sau vật cản gần nhất để di chuyển đến ẩn nấp.
     * Nếu không có cover: Lùi xa kết hợp lượn ngang (Kiting Strafe), không bao giờ đứng im.
     * Nếu đã ở sau cover (`inCover == true`): Duy trì vị trí ẩn nấp (Hold Cover) và nhích nhẹ giữ cự ly an toàn.
4. **Phát xung Hồi Máu & Tự Hồi Phục**:
   - Mỗi $0.70\text{s}$: Hồi $8\text{ HP}$ (+1 HP/Stage) cho toàn bộ đồng minh trong bán kính $150\text{px}$.
   - Tự hồi $4\text{ HP}$ cho chính bản thân Medic nếu `currentHealth < maxHealth`.

---

## 3. Thông số kỹ thuật (Specifications)

| Thuộc tính | Giá trị cập nhật | Giá trị cũ | Giải thích vai trò |
|---|---|---|---|
| Kích thước (Hitbox) | $30 \times 30\text{px}$ | $30 \times 30\text{px}$ | Thực thể hình hộp tròn chuẩn |
| Máu tối đa (`maxHealth`) | **$65 - 85\text{ HP}$** | $40 - 60\text{ HP}$ | Tăng cường độ chống chịu, không dễ bị sốc sát thương chết tức thì |
| Tốc độ di chuyển (`speed`) | **$115.0\text{ px/s}$** | $110.0\text{ px/s}$ | Tốc độ cơ bản như cũ, giữ nhịp gameplay tự nhiên |
| Tốc độ né chạy (`evasiveSpeed`) | **$115.0\text{ px/s}$** | $110.0\text{ px/s}$ | Di chuyển thông minh bằng chiến thuật bẻ lái lướt sườn thay vì buff tốc độ quá đà |
| Bán kính hào quang (`healRadius`) | **$150.0\text{ px}$** | $135.0\text{ px}$ | Phạm vi hào quang mở rộng, bao phủ đồng đội tốt hơn |
| Lượng máu hồi mỗi nhịp (`healAmount`) | **$8\text{ HP}$** | $6\text{ HP}$ | Tăng năng lực hồi phục cho đồng đội |
| Tần suất hồi (`healInterval`) | **$0.70\text{s}$** | $0.80\text{s}$ | Tần số phát xung nhanh hơn |
| Tự hồi máu bản thân (Self-Repair) | **$4\text{ HP/nhịp}$** | $0\text{ HP}$ | Tự dưỡng thương, chống bị rỉa máu từ xa |
| Bán kính cảnh giác né tránh (`fleeDistance`) | **$240.0\text{ px}$** | $210.0\text{ px}$ | Nhận diện nguy hiểm sớm hơn để chủ động lùi |
| Cơ chế né góc chết & Bứt phá (Breakout) | **Có (Đa lực & 16 tia quét $360^\circ$)** | Không có (kẹt góc) | Tự động lướt sườn ra giữa phòng; lách qua cạnh người chơi khi bị ép sát góc |
| Tìm chỗ nấp an toàn (Safe Cover) | **Có (Lọc bỏ 100% ngõ cụt/góc tường)** | Không có (đứng tơ hơ) | Quét ô sàn thông thoáng sau vật cản, tuyệt đối không chui vào ngõ cụt |

---

## 4. Đồ họa & Hiệu ứng (Visuals 100% Java Graphics2D)
* **Vòng tròn Hào quang Hồi Máu (Healing Aura Ring - Red Hostile Zone)**:
  - Tách Layer Độc Lập vẽ ở sàn dưới tất cả entity, bán kính $150\text{px}$.
  - Nền đỏ thẫm phát quang mờ, viền nét đứt đỏ xoay đều, 4 chữ thập đỏ ở 4 hướng.
  - Sóng xung (Pulse Ripple) đỏ rực mở rộng ra ngoài mỗi khi nhịp hồi phục kích hoạt.
* **Ngoại hình Thực thể (Military Gray Hostile Chassis)**:
  - Thân vỏ hợp kim titan màu xám quân sự đậm `Color(68, 74, 84)`.
  - Tấm giáp carbon bên trong, đường viền đỏ thẫm hazard, phù hiệu chữ thập y tế đỏ rực `#E62828`.
* **Thanh Máu Trên Đầu**:
  - Thanh máu trên đầu luôn luôn hiển thị màu đỏ `Color(230, 45, 45)`. Không hiện text rườm rà.

---

## 5. Sửa đổi / Cập nhật
* **04/11/2026 (Nâng Cấp Trí Thông Minh Di Chuyển Chống Ép Góc & Giữ Tốc Độ Chuẩn)**:
  - **Giữ tốc độ di chuyển như cũ ($115.0\text{ px/s}$)**: Không buff tốc độ phi lý làm mất cân bằng trải nghiệm. Thay vào đó, tập trung nâng cấp trí tuệ nhân tạo (AI Locomotion) linh hoạt và thông minh hơn.
  - **Khắc phục triệt để lỗi bị ép vào góc chết (Corner-Trap Fix)**:
    1. *Bộ lọc ngõ cụt (Dead-End / Corner Pocket Filter)*: `isDeadEndOrPocket` và `calculateOpennessScore` loại bỏ 100% các ô nấp nằm ở góc phòng hoặc sau thùng gỗ bị bao vây 3 phía, ngăn chặn triệt để việc Medic tự dẫn xác vào góc chết.
    2. *Hệ thống trường lực né tránh lượn sườn (Tactical Flank Steering & Wall Repulsion)*: Khi người chơi áp sát, Medic kết hợp lực kéo về tâm phòng và lực tiếp tuyến, tự động lướt vòng cung qua sườn người chơi thay vì lùi thẳng vào góc tường.
    3. *Cơ chế bứt phá khẩn cấp (Emergency Breakout / Slip-Through)*: Khi bị người chơi dồn sát góc tường (phía sau bị chặn), Medic quét 16 hướng $360^\circ$ tìm lối thoát dẫn ra không gian mở của phòng, dũng cảm lách qua sườn người chơi để phóng ra ngoài căn phòng.
  - **Buff chỉ số sinh tồn & hỗ trợ**: Máu 65 - 85 HP; hào quang mở rộng 150px hồi 8 HP mỗi 0.70s; tự phục hồi 4 HP cho bản thân mỗi nhịp.
  - **Kiểm thử tự động**: Bổ sung 2 bài unit test chuyên sâu (`testMedicDeadEndAndPocketDetection` và `testMedicEmergencyBreakoutFromCorner`). Toàn bộ **315 / 315 test cases PASSED 100%**.
* **22/10/2026**:
  - Khởi tạo mới lớp `MedicEnemy`.
