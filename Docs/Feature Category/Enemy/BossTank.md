Cập nhật mới nhất vào ngày: 27/10/2026

# Chiến Xa Bọc Thép Titan (BossTank - Titan Battle Tank)

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

Cỗ xe tăng vận hành thông qua máy trạng thái hữu hạn (FSM) gồm 8 trạng thái: `PATROL_REPOSITION`, `RAM_WINDUP`, `RAM_RUSH`, `RAM_STUN`, `MG_BURST`, `CANNON_AIMING`, `CANNON_LOCKED`, `COOLDOWN`.

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
  - Toàn bộ test tự động trong test suite đạt 100% Passed (243 bài test).
