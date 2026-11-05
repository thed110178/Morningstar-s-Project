Cập nhật mới nhất vào ngày: 04/11/2026

# Chiến Xa Titan MK-IV (BossTank - TITAN TANK MK-IV)

## 1. Mô tả
Chiến Xa Bọc Thép Titan (`BossTank`) là thủ lĩnh tối cao (Grand Boss) xuất hiện tại Stage 4 (**Grand Boss Arena**) của Biome Alpha Sector. Đây là một cỗ xe tăng chiến đấu bọc thép hạng nặng sở hữu thiết kế cơ khí 3 tầng xoay độc lập (3-Layer Decoupled Structure), lượng sinh lực khổng lồ ($1250\text{ HP}$), kích thước buồng chiến đấu đồ sộ và uy dũng ($96 \times 96\text{px}$ - thân xe $94 \times 76\text{px}$), cùng bộ chiêu thức kết hợp giữa cú húc cơ học thần tốc cực mạnh không báo trước, hỏa lực súng máy áp đảo và pháo bộc phá siêu thanh nổ tức thời xuyên vật cản.

---

## 2. Cấu trúc Thiết kế 3 Tầng Độc Lập (3-Layer Decoupled Rotations)

Nhằm tái hiện độ chân thực và phong cách cơ khí quân sự hiện đại, mọi góc xoay của xe tăng được tách biệt hoàn toàn thành 3 tầng từ trên xuống dưới:

```
[Layer 1] Ụ Súng Máy Nóc Xe (Machine Gun Turret)
          └── Luôn xoay ngắm 360° tự do bám sát Operative

[Layer 2] Tháp Pháo Xe Tăng Đại Bác (Main Cannon Turret)
          ├── HP ≥ 50%: Khóa nòng cố định theo hướng thân xe (Layer 3)
          └── HP < 50%: Thức tỉnh hoàn toàn, chiếu laser đỏ bám đuổi & bắn pháo siêu thanh bộc phá

[Layer 3] Khung Gầm Bọc Thép & Bánh Xích Kép (Chassis & Caterpillar Treads)
          └── Kích thước đồ sộ (96px hitbox, thân 94x76px), góc xoay LUÔN LUÔN đồng nhất tuyệt đối với hướng di chuyển thực tế
```

1. **Tầng 1 (Trên cùng - Layer 1)**: *Ụ súng máy nóc xe (`machineGunAngle`)*
   - Độc lập hoàn toàn với hướng thân xe và tháp pháo chính.
   - Luôn luôn tính toán góc $\theta = \text{atan2}(y_{\text{player}} - y, x_{\text{player}} - x)$ để xoay nòng theo dõi sát sao từng bước chân của người chơi.
2. **Tầng 2 (Ở giữa - Layer 2)**: *Tháp pháo xe tăng đại bác (`cannonAngle`)*
   - Khi $\text{HP} \ge 50\%$ ($625 - 1250\text{ HP}$): Tháp pháo chính ở trạng thái khóa cơ học, góc quay luôn đồng nhất với thân xe (`cannonAngle = chassisAngle`).
   - Khi $\text{HP} < 50\%$ ($< 625\text{ HP}$): Hệ thống pháo chính chính thức "thức tỉnh" (Awakened). Tháp pháo xoay độc lập, kích hoạt hệ thống ngắm quang học Laser và đại pháo tầm xa siêu thanh.
3. **Tầng 3 (Dưới cùng - Layer 3)**: *Khung gầm bọc thép & bánh xích kép (`chassisAngle`)*
   - **Quy tắc cốt lõi**: Hướng di chuyển của xe tăng phải đồng nhất tuyệt đối với góc xoay của thân và bánh xích.
   - Khi tuần tra bẻ lái, góc xích quay mượt mà qua hàm nội suy `smoothRotate`, và bước tiến $\Delta x, \Delta y$ được tính toán trực tiếp từ $\cos(\text{chassisAngle}), \sin(\text{chassisAngle})$.
   - Hiệu ứng chuyển động bánh xích (Tread Animation) cuộn sinh động theo tốc độ di chuyển.

---

## 3. Bộ Chiêu Thức (Moveset)

Cỗ xe tăng vận hành thông qua máy trạng thái hữu hạn (FSM) gồm 10 trạng thái: `PATROL_REPOSITION`, `RAM_WINDUP`, `RAM_RUSH`, `RAM_STUN`, `MG_BURST`, `CANNON_AIMING`, `CANNON_LOCKED`, `CANISTER_WINDUP`, `CANISTER_FIRE`, `COOLDOWN`.

### 3.1. Chiêu 1: Đâm Húc Thần Tốc Kinh Hoàng (Ram / Charge - 950 px/s, Biển Cảnh Báo "!" & Dừng Va Chạm)
* **Quy trình hoạt động**:
  1. *Lấy đà (RAM_WINDUP - 0.58s ở Phase 1, 0.48s ở Phase 2)*: Khung gầm bẻ lái hướng về phía người chơi. **Hiển thị vệt đỏ cảnh báo đường đâm (Ram Telegraph Corridor)** dài $800\text{px}$, rộng $96\text{px}$ trên mặt đất kèm thanh nạp lực dâng dần và các mũi tên xung kích `>>>`. Ở $0.16\text{s}$ cuối (Phase 1) hoặc $0.14\text{s}$ cuối (Phase 2), góc đâm được KHÓA CỐ ĐỊNH và viền chuyển sang màu vàng cam phát quang chói lòa, đồng thời biển cảnh báo nhấp nháy dấu "!" màu vàng viền đỏ rực rỡ hiển thị trên đầu xe tăng, tạo cơ hội vàng cho người chơi lướt né ra khỏi vùng nguy hiểm.
  2. *Lao vọt thần tốc (RAM_RUSH - 950 px/s)*: Cả cỗ xe tăng thiết giáp $96\text{px}$ phóng vụt đi như một quả tên lửa mặt đất với vận tốc kinh hoàng $950.0\text{ px/s}$.
* **Cơ chế chống spam đâm ở nửa thanh máu đầu (Phase 1 Anti-Ram Spam)**:
  - Ở nửa thanh máu đầu ($\text{HP} \ge 50\%$), tỉ lệ ra chiêu được tái cân bằng: $55\%$ Bắn súng máy và $45\%$ Đâm húc.
  - Sau khi vừa tung ra một đòn đâm húc, boss **tuyệt đối bị cấm đâm liên tiếp đòn tiếp theo** (`lastAttackWasRam = true`), bắt buộc phải chuyển sang bắn súng máy (`MG_BURST`), giúp nhịp độ trận đấu cân bằng, kịch tính và không gây ức chế.
* **Tương tác va chạm & Quy tắc cày nát / đâm xuyên qua vật cản**:
  - **Trúng người chơi**: Gây $40\text{ HP}$ sát thương va chạm cực mạnh, kích hoạt sóng chấn động $50\text{px}$, rung màn hình $0.35\text{s}$, đẩy lùi người chơi và kết thúc đòn lao chuyển sang `COOLDOWN`.
  - **Va chạm với Thùng gỗ tạm thời (`TileType.DESTRUCTIBLE_COVER`)**:
    - **CÀY NÁT & ĐÂM XUYÊN QUA (PLOW THROUGH)**: Cỗ xe tăng bọc thép hạng nặng nghiền nát ngay lập tức mọi khối thùng gỗ trên quỹ đạo đâm ($120\text{ DMG}$ lập tức phá hủy thành sàn `FLOOR`).
    - **KHÔNG BỊ DỪNG LẠI**: Xe tăng tiếp tục duy trì vận tốc $950\text{ px/s}$ phóng xuyên qua các hàng thùng gỗ vỡ vụn, nghiền nát người chơi nếu họ tưởng rằng đứng sau thùng gỗ là an toàn!
  - **Va chạm với Vật cản kiên cố (Tường cứng `WALL`, Cột đá `COVER`)**:
    - **DỪNG ĐÂM LẠI NGAY LẬP TỨC**: Xe tăng bị chặn đứng lại hoàn toàn bởi vật cản không thể phá hủy.
    - **Hiệu ứng va đập chấn động**:
      * Spawns sóng chấn động bộc phá quầng lửa khổng lồ (`ExplosionVisual` bán kính $65\text{px}$) ngay tại mũi cản trước xe.
      * Rung chấn màn hình dữ dội ($0.45\text{s}$ screen shake).
      * Mũi cản trước xe tóe ra chùm tia lửa vàng cam rực rỡ và các đám khói bụi đen trắng cuồn cuộn bốc lên.
      * Rơi vào trạng thái choáng váng (`RAM_STUN`) bất động trong $0.38\text{s} - 0.45\text{s}$, mở ra cơ hội phản công quý giá.

### 3.2. Chiêu 2: Xả Loạt Đạn Súng Máy (Machine Gun Burst)
* **Quy trình hoạt động**:
  - Ụ súng máy trên nóc xả liên thanh $6$ viên đạn súng máy liên tiếp về phía người chơi.
  - Sát thương: $9\text{ HP/viên}$, tốc độ đạn: $340.0\text{ px/s}$, độ tản xạ nhỏ ($\pm 0.06\text{ rad}$) tạo chùm đạn liên thanh tự nhiên.
* **Biến thể Chạy Ngang (Lateral Strafe Run)**:
  - Có $60\%$ tỉ lệ xe tăng vừa di chuyển vuông góc (chạy ngang góc $90^\circ$ so với hướng ngắm) vừa xả đạn.
  - Chiến thuật này giúp mở góc bắn mới, tạt sườn và truy quét người chơi đang nấp sau góc vật cản.

### 3.3. Chiêu 3: Đại Pháo Siêu Tốc Nổ Bộc Phá (Laser Hoàn Toàn Lock-On & Cửa Sổ Né Đòn Duy Nhất - HP < 50%)
* **Điều kiện kích hoạt**: Chỉ khả dụng khi máu của Boss giảm xuống dưới $50\%$ ($< 500\text{ HP}$).
* **Quy trình hoạt động**:
  1. *Ngắm bắn Lock-On tuyệt đối (CANNON_AIMING - 0.90s)*: Tháp pháo chính xoay nòng theo người chơi. Tia laser và tâm ngắm quang học $[+]$ **hoàn toàn lock-on trực tiếp vào tọa độ người chơi theo thời gian thực (Zero Lag)**. Không có độ trễ xoay chậm chạp đuổi theo phía sau khi người chơi di chuyển.
  2. *Laser dừng lại & Khóa cố định (CANNON_LOCKED - 0.40s)*: Tia laser và tâm ngắm **dừng lại hoàn toàn tại vị trí vừa khóa**, chuyển sang chùm năng lượng đỏ thẫm viền vàng phát quang cực mạnh. **Khoảng thời gian 0.40s dừng lại này là thời gian DUY NHẤT để người chơi né đòn** (di chuyển hoặc lướt né ra khỏi đường ngắm).
  3. *Khai hỏa Siêu Tốc tức thời*: Ngay khi hết $0.40\text{s}$, pháo chính bắn ra đầu đạn cỡ đại `BossCannonShell` với sơ tốc $1350.0\text{ px/s}$ ("bắn 1 phát tới nơi luôn").
* **Hiệu ứng sát thương diện rộng (AoE Explosion)**:
  - Khi đầu đạn chạm đích, phát nổ thành một quầng lửa bộc phá khổng lồ và sóng xung kích kép (`ExplosionVisual`) bán kính cực đại phục hồi về **$165.0\text{px}$** (đường kính quét rộng $330\text{px}$, tương đương hơn $5$ ô gạch).
  - Gây **$40\text{ HP}$ sát thương bộc phá** (trừ $40\%$ lượng máu tối đa) **xuyên qua toàn bộ địa hình và cover** cho bất kỳ ai đứng trong bán kính nổ.
  - Phá hủy toàn bộ các ô cover tạm thời lân cận và gây chấn động rung lắc màn hình dữ dội ($0.48\text{s}$).

---


### 3.4. Chiêu 4: Đạn Diệt Bộ Binh (Canister Shot / Shotgun Shell for Tank Cannon - HP ≤ 75%)
* **Bối cảnh & Ý tưởng**: Khi cỗ xe tăng bị tiêu hao từ $25\%$ lượng máu trở lên ($	ext{HP} \le 75\% 	ext{ maxHealth}$, tương đương $\le 937	ext{ HP}$), Boss mở khóa khoang đạn chống bộ binh chuyên dụng: nạp một liều đạn chùm shotgun cỡ lớn vào nòng pháo chính để trừng phạt người chơi dám áp sát hoặc chạy lòng vòng quanh xe tăng.
* **Phân Hóa Hỏa Lực Theo Cự Ly (Distance-based Priority)**:
  - **CỰ LY GẦN ($	ext{dist} \le 320	ext{ px}$)**: **ƯU TIÊN PHÁO BỘ BINH (Canister Shot)**! Tỷ lệ kích hoạt áp đảo lên tới $80\%$ khi cực gần ($< 220	ext{px}$) và $65\%$ khi ở cự ly $220 - 320	ext{px}$. Pháo nổ đại bác ở gần chỉ là đòn phụ trợ nhỏ ($20\%$).
  - **CỰ LY XA ($	ext{dist} > 320	ext{ px}$)**: **ƯU TIÊN PHÁO NỔ ĐẠI BÁC (Heavy Cannon Blast)**! Tỷ lệ kích hoạt $75\%$ khi $	ext{HP} < 50\%$. Pháo bộ binh ở cự ly xa hoàn toàn bị vô hiệu hóa ($0\%$).
* **Quy trình hoạt động**:
  1. *Lấy ngắm & Quạt nón cảnh báo nguy hiểm (CANISTER_WINDUP - 0.75s)*:
     - Tháp pháo chính xoay nòng trực diện theo dõi Operative.
     - Kích hoạt **Telegraph hình quạt nón cảnh báo kéo dài 0.75s** trên sàn đấu (góc mở $70^\circ$, bán kính quét $300	ext{px}$) với viền đỏ cam chớp tắt nhịp điệu và vệt nạp lực tỏa dần từ tâm xe.
     - **Cửa sổ né đòn $0.20	ext{s}$ cuối**: Ở $0.20	ext{s}$ cuối cùng, góc ngắm của nón được KHÓA CỐ ĐỊNH, viền phát quang vàng chói báo hiệu sắp khai hỏa, tạo cơ hội cho người chơi lướt né ra ngoài hoặc nấp sau cột đá kiên cố.
  2. *Khai hỏa Siêu Tốc tức thì (CANISTER_FIRE - 0.12s)*: Pháo chính nổ tung phát bắn đạn chùm diệt bộ binh:
     - **Sát thương hình nón tức thì (Instant Hitscan Cone AoE)**: Gây ngay $34	ext{ HP}$ sát thương nếu Operative nằm trong góc quét $70^\circ$ và bán kính $300	ext{px}$.
     - **Cơ chế ẩn nấp tránh đạn (Line-of-Sight Cover Check)**: Nếu người chơi kịp thời nấp sau bờ tường cứng (`WALL`) hoặc cột trụ kiên cố (`COVER`), toàn bộ chùm đạn bị chắn lại hoàn toàn ($0	ext{ DMG}$), tạo cơ hội chiến thuật ẩn nấp hoàn hảo.
     - **Hiệu ứng hỏa lực cực đại**: Đầu nòng pháo bùng lên chớp lửa hình nón cực đại rực sáng, tóe chùm tia lửa cam vàng, sóng chấn động $55	ext{px}$, rung màn hình $0.25	ext{s}$ và âm thanh khai hỏa uy lực.

## 4. API & Phương Thức Chính

| Phương thức | Kiểu trả về | Mô tả |
|---|---|---|
| `getState()` | `TankState` | Lấy trạng thái hiện tại của máy trạng thái xe tăng. |
| `getChassisAngle()` | `double` | Lấy góc xoay của thân xe và bánh xích kép (Layer 3). |
| `getCannonAngle()` | `double` | Lấy góc xoay của tháp pháo chính đại bác (Layer 2). |
| `getMachineGunAngle()` | `double` | Lấy góc xoay của ụ súng máy nóc xe (Layer 1). |
| `getCannonLaserAngle()` | `double` | Lấy góc ngắm tia laser đại pháo. |
| `getLaserTargetWorldX()` | `double` | Lấy tọa độ X thế giới của điểm lock-on laser. |
| `getLaserTargetWorldY()` | `double` | Lấy tọa độ Y thế giới của điểm lock-on laser. |
| `isLastAttackWasRam()` | `boolean` | Kiểm tra cờ ghi nhận đòn tấn công trước có phải là đâm hay không. |
| `getCanisterWindupDuration()` | `double` | Lấy thời gian chuẩn bị & quạt nón cảnh báo đạn diệt bộ binh (0.75s). |
| `isCannonAwakened()` | `boolean` | Kiểm tra xem tháp pháo chính đã thức tỉnh chưa ($\text{HP} < 50\%$). |
| `updateAI(...)` | `void` | Cập nhật logic di chuyển, xoay 3 tầng độc lập, và ra chiêu. |
| `render(Graphics2D, Camera)` | `void` | Vẽ đồ họa Vector 3 tầng, dấu "!" cảnh báo, laser lock-on và thanh máu Boss. |

---

## 5. Quan Hệ Với Các Hệ Thống Khác

* **`PlayingState`**: Khởi tạo `BossTank` tại Stage 4 trong `spawnTargetsForRoomPhase`. Nhận diện va chạm đạn pháo `BossCannonShell` để kích hoạt `triggerAreaExplosion` và rung chấn màn hình `triggerScreenShake`.
* **`DungeonMap`**: Cung cấp dữ liệu `isAreaWalkable` để xác định va chạm đâm tường gây Stun và phá hủy ô cover tạm thời qua `damageDestructibleCoverInArea`.
* **`ProjectileManager`**: Tiếp nhận các viên đạn súng máy và đầu đạn pháo `BossCannonShell`, quản lý quỹ đạo bay và kích hoạt nổ bộc phá khi va chạm tường.
* **`ExplosionVisual`**: Tạo hiệu ứng vòng sóng chấn động màu cam đỏ lan tỏa từ tâm vụ nổ và tan biến dần sau $0.45\text{s}$.

---

## 6. Sửa Đổi / Cập Nhật

* **04/11/2026 (Thống Nhất Định Danh TITAN TANK MK-IV, Tinh Gọn Thanh Máu Boss & Random 2 Súng Cấp 3 Trong Boss Fight)**:
  - **Trang bị ngẫu nhiên 2 súng chính Cấp 3 không trùng lặp cho Chế độ Boss Fight (PlayingState.loadBossStageDirectly)**:
    * Loại bỏ cấu hình vũ khí cố định đơn điệu (trước đây luôn là AR Lv 3 + Shotgun Lv 3).
    * Hệ thống tự động tuyển chọn ngẫu nhiên 2 khẩu súng chính **hoàn toàn khác loại nhau** từ kho vũ khí gồm 6 chủng loại: AR, Shotgun, SMG, Sniper Rifle, Semi-Auto Shotgun (SAS), và Semi-Carbine Rifle (SCR).
    * Cả 2 khẩu súng đều được nâng cấp lên **Level 3** (configureLevel(3)), gán vào Slot 1 và Slot 2, nạp đầy băng đạn và tiếp tế tối đa đạn dự trữ.
    * Bổ sung bộ kiểm thử BossFightLoadoutTest (3 test cases), tổng số bài kiểm thử tăng lên **307/307 Passed 100%**.
  - **Thống nhất tên gọi toàn diện trên toàn bộ trò chơi**:
    * Chuẩn hóa định danh duy nhất của Boss: **TITAN TANK MK-IV** (tiếng Việt: **Chiến Xa Titan MK-IV**).
    * Loại bỏ hoàn toàn các tên gọi phân mảnh trước đây (HEAVY COMBAT BATTLE TANK, Xe tăng MK-IV, Titan Battle Tank).
  - **Tinh gọn giao diện thanh máu Boss (PlayingHUD)**:
    * Lược bỏ nhãn [BOSS] và các cặp ký tự cảm thán rườm rà ! ... !, thanh máu ở đỉnh màn hình giờ đây chỉ hiển thị duy nhất định danh chiến đấu trang trọng: **TITAN TANK MK-IV**.
    * Bảng tổng kết kết quả chiến đấu sau phòng đấu Boss (Victory Debriefing) được đồng bộ: ĐÃ TIÊU DIỆT TITAN TANK MK-IV.
  - **Đồng bộ hóa các thành phần giao diện & điện ảnh**:
    * Màn hình cắt cảnh xuất hiện trùm (BossIntroCinematic): Thống nhất hiển thị tiêu đề TITAN TANK MK-IV.
    * Menu chọn màn đấu thử thách trùm (MenuState): Cập nhật thành BOSS FIGHT: TITAN TANK MK-IV.
  - **Cập nhật toàn bộ Test Suite**: Sửa đổi các bộ kiểm thử đơn vị (CombatDoorTest, EnemyAITest), đảm bảo **304/304 test cases Passed 100%**.

* **03/11/2026 (Sửa Lỗi Đòn Đâm Phase 2, Cân Bằng Hỏa Lực Đa Cự Ly & Toàn Màn Hình Tự Động)**:
  - **Khắc phục lỗi triệt để: Boss dưới 50% máu ngừng đâm húc**:
    * Sửa thuật toán chọn chiêu `chooseNextAction()`: Loại bỏ lỗi chia sẻ biến ngẫu nhiên `roll` trong các chuỗi `if-else` lồng nhau.
    * Khi Boss dưới 50% máu (Phase 2 thức tỉnh), xe tăng duy trì tỷ lệ đâm húc cực kỳ uy lực (**32% - 35% khi đường đâm thông thoáng**) ở cả cự ly gần lẫn cự ly xa, kết hợp với thời gian lấy đà nhanh hơn ($0.48	ext{s}$) và tốc độ đâm khủng khiếp ($720	ext{px/s}$).
    * Sau khi bắn Canister Shot cận chiến, nếu người chơi vẫn ở cự ly gần và không nấp sau cover, tỷ lệ Boss lập tức bồi đòn đâm húc lên tới **60%**!
  - **Tái thiết kế Đấu trường Trùm (Grand Boss Arena 28x18)**: Dịch 4 cột trụ cover cứng ra xa nhau 12 ô gạch (~576px), cách tường Bắc/Nam 5 ô (~240px). Mở toang đại lộ chữ thập trung tâm rộng rãi, vừa đảm bảo người chơi có vật che chắn nấp bắn, vừa tạo không gian lý tưởng cho cỗ xe tăng thi triển hết bộ kỹ năng.
  - **Nâng cấp AI Quét Cover Cứng Chống Đâm Mù Quáng**: Tích hợp thuật toán raycast 3 luồng kiểm tra đường đâm. Khi người chơi nấp sau cover cứng, Boss không bao giờ thực hiện đòn đâm (tránh bị đâm vào cột gây Stun vô ích) mà lập tức chuyển sang cơ động xả súng máy chạy ngang hoặc bắn đại pháo bộc phá.
  - **Bổ sung chiêu thức mới Đạn Diệt Bộ Binh (Canister Shot)**: Dùng khi Boss mất 25% máu (HP <= 75%), kích hoạt khi người chơi áp sát trong phạm vi 320px. Quạt nón cảnh báo 0.75s, sát thương tức thì 34 HP, có thể né tránh hoàn toàn bằng cách nấp sau cover cứng.
  - **Toàn bộ 304 bài kiểm thử tự động của dự án đạt 100% Passed**.

* **19/10/2026**:
  - Tạo mới class `BossTank`, `BossCannonShell`, và `ExplosionVisual`.
  - Triển khai kiến trúc 3 tầng xoay độc lập (Ụ súng máy, Tháp pháo đại bác, Khung gầm bánh xích).
  - Tích hợp trọn vẹn bộ 3 chiêu thức: Đâm húc Stun vào tường / nghiền nát cover, Xả đạn súng máy có biến thể chạy ngang, Đại pháo laser khóa mục tiêu nổ AoE xuyên địa hình khi HP < 50%.
  - **Nâng cấp chiến đấu đỉnh cao**:
    1. Mở rộng kích thước Boss lên $84\text{px}$ (thân xe $82 \times 66\text{px}$, tháp pháo $r=24\text{px}$, nòng đại bác dài $52\text{px}$, thanh máu đầu xe $140\text{px}$).
    2. Tăng cường chiêu Đâm: rút ngắn thời gian lấy đà xuống $0.30\text{s}$, tăng tốc độ lao vọt lên $950.0\text{ px/s}$, tăng sát thương lên $40\text{ HP}$, và bỏ vệt hướng đâm trên sàn.
    3. Cơ chế dừng đâm khi gặp vật cản: Khi đâm trúng tường hoặc cover, xe dừng ngay lập tức, nghiền nát thùng gỗ, gây nổ va chạm $65\text{px}$, rung màn hình $0.45\text{s}$, tóe tia lửa khói đen và bị Stun $0.8\text{s}$.
    4. Nâng cấp đạn pháo siêu thanh: tốc độ bắn tăng vọt lên $1350.0\text{ px/s}$ ("bắn 1 phát tới nơi luôn"), sát thương nổ AoE $35\text{ HP}$, bán kính bộc phá mở rộng $85\text{px}$.
    5. **Chống spam đâm ở nửa thanh máu đầu**: Xen kẽ súng máy (55%) và đâm (45%), cấm đâm 2 lần liên tiếp.
    6. **Biển cảnh báo "!" nhấp nháy**: Hiển thị dấu "!" màu vàng viền đỏ rực rỡ trên đầu Boss khi chuẩn bị đâm.
    8. **Nâng cấp độ trâu & kích thước uy dũng (Bigger & Tankier Boss)**:
       - Hitbox mở rộng lên $96.0\text{px}$ (khung gầm thân xe $94 \times 76\text{px}$, xích xe $16\text{px}$, tháp pháo chính bán kính $r=28\text{px}$, nòng đại bác dài $60\text{px}$, thanh máu đầu xe mở rộng $160\text{px}$).
       - Máu cơ bản tăng từ $1000\text{ HP}$ lên **$1250\text{ HP}$** (tăng $25\%$ độ trâu), ngưỡng thức tỉnh đại pháo dịch chuyển sang $< 625\text{ HP}$.
       - Kết hợp cùng việc khắc phục lỗi dao cận chiến multi-hit khiến trận chiến Boss thực sự trở thành một màn đấu nghẹt thở, đòi hỏi kỹ năng lướt né, ẩn nấp cover và hỏa lực chiến thuật.
    9. **Cơ chế Triệu hồi Viện Binh bảo vệ Boss sau 5 giây (Minion Reinforcements Wave System)**:
       - Khi toàn bộ quái phụ hộ vệ bị quét sạch, hệ thống kích hoạt đếm ngược $5.0\text{s}$ (hiển thị trực quan thanh `TIẾP VIỆN: Xs` bên dưới Boss Health Bar).
       - Khi hết $5\text{s}$, tự động triệu hồi $5 - 6$ kẻ địch ngẫu nhiên (kết hợp Quái Cận Chiến và Lính Bắn Súng Lục) đổ bộ bảo vệ Boss, kèm hiệu ứng cổng teleport an toàn ($0.40\text{s}$).
       - Tự động ngắt tiếp viện vĩnh viễn ngay khi BossTank bị tiêu diệt.
    10. **Rút ngắn khoảng nghỉ giữa các kỹ năng & Đẩy cao nhịp độ chiến đấu (Condensed Skill Downtime & Aggressive Rhythm)**:
       - Rút ngắn triệt để thời gian hồi chiêu `COOLDOWN` từ $0.8\text{s} - 1.0\text{s}$ xuống **$0.20\text{s} - 0.35\text{s}$**.
       - Rút ngắn thời gian lượn tìm góc `PATROL_REPOSITION` từ $1.4\text{s} - 2.2\text{s}$ xuống **$0.20\text{s} - 0.55\text{s}$**.
       - Loại bỏ hoàn toàn độ trễ thừa sau loạt súng máy: Ngay khi $6 - 8$ viên đạn bắn xong, boss lập tức chuyển sang Cooldown ngắn mà không phải đợi hết timer chạy ngang.
       - Rút ngắn thời gian ngắm Laser đại pháo `CANNON_AIMING` từ $0.90\text{s}$ xuống **$0.65\text{s}$**.
       - Rút ngắn thời gian Stun đâm tường `RAM_STUN` từ $0.80\text{s}$ xuống **$0.38\text{s} - 0.45\text{s}$**.
       - Tăng tốc độ di chuyển cơ động `roamSpeed` từ $90.0\text{ px/s}$ lên **$110.0\text{ px/s}$**.
       - Tổng thời gian nghỉ giữa các chiêu thức giảm mạnh từ $\sim 3.0\text{s} - 3.5\text{s}$ xuống chỉ còn **$0.5\text{s} - 0.9\text{s}$**, biến Boss thành một cỗ máy chiến tranh dồn dập, liên tục gây áp lực nghẹt thở lên Operative.
    11. **Cân Bằng & Nerf Chỉ Số Boss (Balanced Boss Nerf)**:
        - Tăng thời gian lấy đà đòn đâm húc `RAM_WINDUP`: Phase 1 ($\text{HP} \ge 50\%$) tăng từ $0.25\text{s}$ lên **$0.55\text{s}$**; Phase 2 ($\text{HP} < 50\%$) tăng từ $0.22\text{s}$ lên **$0.45\text{s}$**, giúp người chơi có cửa sổ quan sát và lướt né đòn đâm hiệu quả.
        - Khôi phục bán kính nổ đại pháo: Bán kính nổ bộc phá `BossCannonShell` được phục hồi về **$165.0\text{px}$** (đường kính quét $330\text{px}$, quét rộng hơn 5 ô gạch) theo phản hồi trải nghiệm thực chiến của người dùng, đảm bảo uy lực chấn động của cỗ chiến xa hạng nặng.
        - Giảm sát thương vụ nổ pháo: Từ $60\text{ HP}$ xuống **$40\text{ HP}$**, giảm nguy cơ người chơi bị sốc sát thương tức thì trong buồng giao tranh.
    12. **Tiền Sản Sinh Xe Tăng (Pre-spawn) & Ẩn Thanh Máu Boss Khi Chưa Vào Phòng (27/10/2026)**:
        - **Hiện diện thực tế**: Cỗ xe tăng `BossTank` được tiền sản sinh sẵn tại tâm phòng đấu Stage 4, loại bỏ cổng xoáy portal để cỗ xe tăng hiện diện sẵn sàng ngay từ đầu.
        - **Kiểm soát thanh máu**: Mặc dù xe tăng đã sinh ra trước, thanh máu Boss ở đỉnh màn hình (`renderBossHealthBar`) **hoàn toàn được giấu đi** khi người chơi còn ở Spawn Room hoặc ngoài hành lang (`isBossHealthBarRevealed == false`).
        - **Thời điểm hiển thị**: Thanh máu Boss chỉ xuất hiện khi người chơi thực sự bước qua cửa phòng Boss làm kích hoạt lockdown phòng, phát cắt cảnh giới thiệu Boss (`BossIntroCinematic`), hoặc khi Boss bị trúng đạn.
    13. **Buff Nhẹ Sức Chống Chịu Của Boss Tank (01/11/2026)**:
        - **Giảm 30% sát thương từ đạn (`BULLET_DAMAGE_REDUCTION = 0.30`)**: Mọi loại đạn của người chơi (Pistol, AR, SMG, Shotgun, Sniper) khi bắn trúng Boss Tank đều bị lớp giáp thép phức hợp (Composite Titan Armor) hấp thụ 30% chấn động, Boss chỉ chịu $70\%$ lượng sát thương cơ bản (tối thiểu 1 HP). Điều này giúp kéo dài thời lượng trận đấu trùm cuối, buộc người chơi phải giữ vị trí và căn thời điểm xả đạn thông minh.
        - **Miễn nhiễm hoàn toàn sát thương từ dao cận chiến (`IMMUNE_TO_KNIFE = true`)**: Lưỡi dao găm chiến thuật (Combat Knife) không thể xuyên thủng lớp giáp xe tăng hạng nặng 50 tấn. Khi chém dao trúng Boss, Boss nhận $0	ext{ DMG}$, phát ra âm thanh leng keng dội giáp kim loại (`HIT_UNBREAKABLE_WALL`) và kích hoạt hiển thị thanh máu Boss, ngăn chặn hoàn toàn chiến thuật áp sát chém liên hoàn (knife spam) gây lỗi nhịp độ trận đấu trùm.
    14. **Nâng Cấp Cơ Chế Tử Trận & Xác Xe Tăng Thiêu Cháy Vĩnh Viễn (01/11/2026)**:
        - **Loại bỏ tiếng chết quái thường**: Boss Tank là cỗ chiến xa bọc thép khổng lồ, khi bị tiêu diệt không phát ra tiếng la hét tử trận của quái người (`ENEMY_DEAD_1..3`).
        - **Vụ nổ nhỏ khi tử trận (Non-damaging Explosion)**: Khi Boss Tank bị tiêu diệt, lập tức tạo một vụ nổ nhỏ chấn động (`ExplosionVisual` bán kính $85.0	ext{px}$, âm thanh `ENEMY_BOSS_EXPLOSION`, rung chấn màn hình $0.38	ext{s}$) thuần hiệu ứng thị giác và âm thanh, hoàn toàn **không gây sát thương** lên Operative hay địa hình xung quanh.
        - **Xác xe tăng thiêu cháy tồn tại vĩnh viễn (`EnemyCorpse.isBoss == true`)**:
          * Thay vì biến thành đầu lâu nhỏ trôi dạt và mờ dần biến mất như quái thường, Boss Tank để lại xác cỗ xe bọc thép bị thiêu cháy rụi (Charred Tank Wreckage) giữ nguyên kích thước thân xe đồ sộ ($96	ext{px}$).
          * Xác xe đứng bất động tuyệt đối trên sàn đấu, không trôi văng, alpha đạt $1.0	ext{f}$ và **tồn tại vĩnh viễn** (`isFinished() == false`, không bao giờ biến mất), trở thành một tượng đài chiến tích uy dũng trong đấu trường Boss Arena.
          * Đồ họa chi tiết: vệt cháy sém loang lổ trên mặt sàn, hai dải xích đứt gãy ám muội than, khung gầm bọc thép xám đen chì có vết nứt xé toạc, tháp pháo lục giác nứt vỡ lệch góc, nắp hầm bật tung để lộ tàn lửa đỏ cam âm ỉ nhấp nháy theo thời gian thực.
          * **Cột khói đen & xám tro cuồn cuộn bốc lên cao (Billowing Dense Smoke Plume)**:
            - Chuyển sang vẽ theo hệ tọa độ thế giới/màn hình thẳng đứng không bị xoay theo thân xe (`unrotated space`), giúp các luồng khói luôn bay thẳng lên trời (`-Y`) kèm độ lượn sóng tự nhiên theo gió.
            - Phối hợp 3 tầng màu khói tương phản rõ rệt: Khói xám tro đậm dày đặc ở biên ngoài (`Color(50, 52, 58)`), lõi khói sáng tương phản ở giữa (`Color(125, 130, 140)`), và tán khói trắng xám nhạt bốc lên cao nhất (`Color(185, 190, 200)`).
            - Độ mờ dâng cao tối đa lên đến $175$ (thay vì $65 - 75$ mờ nhạt như trước), kích thước nở rộng từ $18	ext{px}$ lên $48	ext{px}$ và bốc cao $68	ext{px}$ lên không trung.
            - Kèm theo các đốm tàn lửa đỏ cam phát sáng (`Color(255, 140, 30)`) cuốn theo luồng khói bay lên từ buồng máy, giúp người chơi quan sát thấy rõ rệt xác xe tăng đang bốc cháy từ xa.
        - **Khắc phục triệt để autotiling ranh giới cỏ (`grassPattern` 1-30)**: Khôi phục logic vẽ pattern cỏ - đất chuyển tiếp trong `Tile.java` và mở rộng nhận diện cover/thùng phi trong `DungeonMap.java`, mang lại đường viền cỏ mượt mà, chân thực trên toàn bộ đấu trường Boss Stage.
  - Toàn bộ test tự động trong test suite đạt 100% Passed (277 bài test).
