Cập nhật mới nhất vào ngày: 22/10/2026

# Hệ Thống AI Kẻ Địch (Enemy AI System)

## 1. Mô tả
Hệ thống AI cho kẻ địch quản lý hành vi chiến thuật, di chuyển và tấn công của các đơn vị thù địch trong phòng chiến đấu (Combat Rooms) và phòng Boss. Hệ thống chuyển đổi các bia tập bắn tĩnh (`TargetDummy`) thành các thực thể chiến thuật tự động có phản xạ theo thời gian thực:
* **Kẻ địch Cận Chiến (`MeleeEnemy`)**: Di chuyển tốc độ cao, bám đuổi áp sát Operative, trượt dọc theo tường địa hình và thực hiện đòn đánh cận chiến khi vào cự ly.
* **Kẻ địch Tầm Xa dùng Súng Lục (`RangedPistolEnemy`)**: Giữ khoảng cách an toàn (kiting), chủ động lùi lại khi người chơi áp sát và tiến lên khi người chơi ra xa; bắn loạt $1 - 3$ viên đạn súng lục có sơ tốc chậm để người chơi có thể phản xạ né tránh.
* **Kẻ địch Hỗ Trợ Quân Y (`MedicEnemy`)**: Không mang vũ khí chiến đấu, sở hữu hào quang hồi máu (Healing Aura) bán kính $135\text{px}$ để sơ cứu đồng đội mất máu trong vùng, đồng thời tự động né tránh Operative khi bị áp sát. (Xuất hiện từ Stage 2).
* **Kẻ địch Ném Lựu Đạn Ép Góc (`GrenadierEnemy`)**: Lính công binh hạng nặng ném lựu đạn hẹn giờ (`EnemyGrenade`) nổ sau đúng $2.0\text{s}$ gây sát thương $24\text{ HP}$ và phá hủy cover trong bán kính $78\text{px}$, ép góc người chơi. (Xuất hiện từ Stage 3).

---

## 2. Quy trình hoạt động

### 2.1. Chu kỳ cập nhật AI (AI Loop)
Mỗi khung hình trong `PlayingState`, phương thức `updateCombatDamageResolution(deltaTime)` gọi `dummy.updateAI(...)` cho từng kẻ địch còn sống:
1. **Xác định mục tiêu**: Tính toán vector khoảng cách từ tâm kẻ địch đến tâm người chơi:
   $$\Delta x = \text{playerCenterX} - x, \quad \Delta y = \text{playerCenterY} - y, \quad \text{dist} = \sqrt{\Delta x^2 + \Delta y^2}$$
   $$\theta = \text{atan2}(\Delta y, \Delta x)$$
2. **Ra quyết định di chuyển (Locomotion Decision)**:
   - *Melee*: Tiến thẳng về phía người chơi cho đến khi chạm vào cự ly đánh.
   - *Ranged Pistol*:
     - Nếu $\text{dist} < 190\text{px}$: Lùi lại (Backpedal) để giữ an toàn.
     - Nếu $\text{dist} > 360\text{px}$: Tiến tới (Advance) để vào tầm bắn.
     - Nếu $190\text{px} \le \text{dist} \le 360\text{px}$: Giữ vững cự ly hoặc strafe nhẹ, ngắm bắn.
3. **Va chạm trượt địa hình (Sliding Collision)**:
   - Tọa độ dự kiến di chuyển được kiểm tra độc lập theo trục $X$ và $Y$ qua `dungeonMap.isAreaWalkable(...)`.
   - Ngăn chặn triệt để hiện tượng kẻ địch đi xuyên tường hoặc kẹt vào góc vật cản.
4. **Kiểm tra tầm nhìn & Bóp cò (Line of Sight & Weapon Discharge)**:
   - Kẻ địch tầm xa kiểm tra `dungeonMap.hasLineOfSight(x, y, playerCenterX, playerCenterY)`.
   - Khi có đường ngắm quang học rõ ràng và hết thời gian hồi chiêu, bắn loạt $1 - 3$ viên đạn súng lục.

---

## 3. Phân loại kẻ địch (Enemy Archetypes)

### 3.1. Kẻ địch Cận Chiến (`MeleeEnemy` - Cyber Swarmer)
* **Kích thước**: $30 \times 30\text{px}$.
* **Máu (HP)**: $50 - 80\text{ HP}$.
* **Tốc độ di chuyển**: $125.0\text{ px/s}$.
* **Sát thương đòn đánh**: $12\text{ HP}$ (hoặc tùy biến theo cấu hình).
* **Thời gian hồi chiêu đòn đánh**: $0.9\text{s}$.
* **Hiệu ứng đồ họa**: Khung giáp đỏ thẫm sắc nhọn, 2 càng cơ khí hướng về người chơi, cảm biến quang học màu vàng cam, hiệu ứng vệt chém móng vuốt đỏ hình vòng cung khi tung đòn.

### 3.2. Kẻ địch Tầm Xa Bắn Súng Lục (`RangedPistolEnemy` - Rogue Scout)
* **Kích thước**: $30 \times 30\text{px}$.
* **Máu (HP)**: $45 - 60\text{ HP}$.
* **Tốc độ di chuyển**: $105.0\text{ px/s}$.
* **Khoảng cách chiến thuật**:
  - Cự ly lùi an toàn: $< 190\text{px}$.
  - Cự ly áp sát: $> 360\text{px}$.
  - Cự ly lý tưởng: $190\text{px} - 360\text{px}$.
* **Cơ chế Bắn Súng Lục**:
  - Số lượng đạn mỗi đợt bắn: $1 - 3$ viên ngẫu nhiên (`ThreadLocalRandom.current().nextInt(3) + 1`).
  - Khoảng cách giữa các viên trong loạt: $0.35\text{s}$.
  - Thời gian nghỉ hồi chiêu giữa các đợt: $2.0\text{s} - 2.6\text{s}$ (tốc độ ra đạn chậm).
  - Tốc độ đạn: $380.0\text{ px/s}$ (vừa phải, có thể né tránh).
  - Sát thương đạn: $10\text{ HP}$.
* **Hiệu ứng đồ họa**: Thân giáp xanh coban quân sự, nòng súng lục nhô ra hướng về mục tiêu, chớp lửa đầu nòng màu vàng cam rực rỡ khi bắn, đạn đỏ phát quang nguy hiểm.

### 3.3. Kẻ địch Hỗ Trợ Quân Y (`MedicEnemy` - Combat Field Medic)
* **Kích thước**: $30 \times 30\text{px}$.
* **Máu (HP)**: $50 - 65\text{ HP}$.
* **Tốc độ di chuyển**: $110.0\text{ px/s}$.
* **Hào quang Hồi Máu (Healing Aura)**:
  - Bán kính: $135.0\text{px}$ lấy tâm Medic làm tâm.
  - Lượng máu hồi: $6\text{ HP}$ mỗi nhịp xung $0.8\text{s}$.
  - Hồi cho toàn bộ kẻ địch/đồng minh nằm trong vùng khi máu chưa đầy.
* **Hành vi AI**:
  - Không tấn công hay gây sát thương.
  - Né tránh người chơi khi bị áp sát ($< 210\text{px}$).
  - Quét tìm và tiếp cận đồng đội bị mất máu nhiều nhất để hỗ trợ.
* **Xuất hiện**: Từ **Stage 2** trở lên và tham gia phòng Boss Stage 4.

### 3.4. Kẻ địch Ném Lựu Đạn Ép Góc (`GrenadierEnemy` - Heavy Grenadier)
* **Kích thước**: $32 \times 32\text{px}$.
* **Máu (HP)**: $55 - 75\text{ HP}$.
* **Tốc độ di chuyển**: $95.0\text{ px/s}$.
* **Khoảng cách chiến thuật**:
  - Cự ly lý tưởng: $175\text{px} - 400\text{px}$ (lùi lại khi $< 175\text{px}$, tiến lên khi $> 400\text{px}$).
  - Tầm ném: $90\text{px} - 460\text{px}$.
* **Hỏa lực Lựu Đạn Hẹn Giờ (`EnemyGrenade`)**:
  - Tần suất ném: Mỗi $3.0\text{s}$ ném 1 quả lựu đạn về phía người chơi.
  - Ngòi nổ hẹn giờ: Chuẩn xác **2.0 giây**.
  - Vòng tròn cảnh báo nguy hiểm: Bán kính $78\text{px}$ nhấp nháy đỏ báo động dồn dập.
  - Sát thương nổ: $24\text{ HP}$ và phá hủy mọi vật cản trong bán kính nổ.
* **Xuất hiện**: Từ **Stage 3** trở lên và tham gia phòng Boss Stage 4.

---

## 4. API & Phương thức cốt lõi

### `TargetDummy.java` (Lớp Cơ Sở)
```java
public void updateAI(double deltaTime, double playerCenterX, double playerCenterY, double playerSize,
                     DungeonMap dungeonMap, ProjectileManager projectileManager, PlayingState playingState);
```

### `MeleeEnemy.java`
```java
public MeleeEnemy(double x, double y, int maxHealth);
public MeleeEnemy(double x, double y, int maxHealth, double speed, int meleeDamage, double attackCooldown);
public MeleeEnemy(double x, double y, int maxHealth, double speed, int meleeDamage, double attackCooldown, String name);
public double getSpeed();
public int getMeleeDamage();
public double getAttackTimer();
public double getAttackCooldown();
```

### `RangedPistolEnemy.java`
```java
public RangedPistolEnemy(double x, double y, int maxHealth);
public RangedPistolEnemy(double x, double y, int maxHealth, double speed, int bulletDamage, double minRange, double maxRange);
public RangedPistolEnemy(double x, double y, int maxHealth, double speed, int bulletDamage, double minRange, double maxRange, String name);
public int getBurstRemaining();
public int getBurstTotal();
public double getBurstCooldownTimer();
public double getMinRange();
public double getMaxRange();
public int getBulletDamage();
```

### `MedicEnemy.java`
```java
public MedicEnemy(double x, double y, int maxHealth);
public MedicEnemy(double x, double y, int maxHealth, double speed, double healAuraRadius, int healAmount, double healInterval, double fleeRadius, String name);
public double getHealAuraRadius();
public int getHealAmount();
public double getHealInterval();
public double getFleeRadius();
```

### `GrenadierEnemy.java`
```java
public GrenadierEnemy(double x, double y, int maxHealth);
public GrenadierEnemy(double x, double y, int maxHealth, double speed, int grenadeDamage, double blastRadius, double grenadeInterval, double preferredMinDist, double preferredMaxDist, String name);
public double getGrenadeInterval();
public double getBlastRadius();
public int getGrenadeDamage();
```

### `DungeonMap.java`
```java
public boolean hasLineOfSight(double x1, double y1, double x2, double y2);
```

---

## 5. Quan hệ với hệ thống khác
* **`PlayingState`**: Khởi tạo phân bổ kẻ địch cận chiến và súng lục trong phòng chiến đấu; giải quyết va chạm đạn kẻ địch trúng người chơi (`damagePlayer(int)`) và gọi cập nhật AI.
* **`DungeonMap`**: Cung cấp lưới va chạm `isAreaWalkable` cho AI trượt vật lý và `hasLineOfSight` cho kiểm tra đường bắn.
* **`ProjectileManager`**: Tiếp nhận các viên đạn kẻ địch (`isPlayerBullet = false`) và lựu đạn hẹn giờ (`EnemyGrenade`).
* **`PlayerHealth`**: Nhận sát thương khi bị đạn kẻ địch bắn trúng, bị quái cận chiến cào trúng hoặc trong bán kính nổ của lựu đạn.

---

## 6. Cơ Chế Cường Hóa Theo Màn (Stage-based Enemy Buffing)
Nhằm tạo chiều sâu tiến trình thử thách (difficulty progression) và độ căng thẳng qua từng tầng hầm ngục, qua mỗi màn kẻ địch được buff nhẹ các chỉ số:

| Thuộc tính | Kẻ địch Cận Chiến (`MeleeEnemy`) | Kẻ địch Súng Lục (`RangedPistolEnemy`) |
|---|---|---|
| **Máu (HP)** | $+15\text{ HP}$ / Stage ($50 \rightarrow 65 \rightarrow 80$) | $+12\text{ HP}$ / Stage ($45 \rightarrow 57 \rightarrow 69$) |
| **Tốc độ di chuyển** | $+8\text{ px/s}$ / Stage ($125 \rightarrow 133 \rightarrow 141$) | $+6\text{ px/s}$ / Stage ($105 \rightarrow 111 \rightarrow 117$) |
| **Sát thương đòn đánh** | $+2\text{ DMG}$ / Stage ($12 \rightarrow 14 \rightarrow 16$) | $+1\text{ DMG}$ / Stage ($10 \rightarrow 11 \rightarrow 12$) |
| **Hồi chiêu đòn đánh** | $-0.05\text{s}$ / Stage ($0.90\text{s} \rightarrow 0.85\text{s} \rightarrow 0.80\text{s}$) | Giữ nguyên ($2.0\text{s} - 2.6\text{s}$ giữa các loạt bắn) |
| **Nhãn phân cấp (Tier Name)**| Stage 1: Base; Stage 2: `MK-2`; Stage 3: `MK-3` | Stage 1: Base; Stage 2: `MK-2`; Stage 3: `MK-3` |

*Ghi chú*:
* Boss Dummy tại Stage 3 (`TITAN MECHA DUMMY`) duy trì cố định $1000\text{ HP}$ và kích thước $64\text{px}$ để đảm bảo tính cân bằng và tương thích tuyệt đối với bộ test suite, trong khi toàn bộ 8 đơn vị hộ tống (minions) trong phòng Boss đều nhận đầy đủ bùa tăng cường `MK-3`.
* Banner chuyển màn tại `advanceToNextStage` thông báo trực quan cấp độ cường hóa của đối thủ: `"★ TIẾN VÀO STAGE 2/3: TIÊU DIỆT 3 PHÒNG CHIẾN ĐẤU (KẺ ĐỊCH CƯỜNG HÓA MK-2) ★"`.

---

## 7. Cơ Chế Bị Đẩy Lùi Khi Nhận Sát Thương (Knockback On Damage)
Khi trúng đạn hoặc nhát chém dao găm của người chơi, kẻ địch bị giật lùi nhẹ (slight knockback) nhằm mang lại cảm giác tác động vật lý chân thực và tạo cơ hội chiến thuật cho người chơi ngắt nhịp áp sát:
1. **Xung lực đẩy lùi (Knockback Impulse)**:
   - **Đạn súng (Pistol, AR, SMG, Shotgun)**: Tạo xung lực $130.0\text{ px/s}$ dọc theo vector hướng bay của viên đạn (`p.getAngle()`).
   - **Nhát chém dao găm (Combat Knife Slash)**: Tạo xung lực $160.0\text{ px/s}$ theo hướng vung dao (`slash.getAngle()`), tạo lực đẩy lùi mạnh mẽ hơn ở cự ly cận chiến.
   - Khi súng Shotgun bắn trúng nhiều mảnh đạn cùng lúc, xung lực knockback được cộng dồn tạo cảm giác hất văng rất uy lực.
2. **Độ dời và ma sát giảm chấn (Displacement & Damping)**:
   - Xung lực giảm dần nhanh chóng qua hệ số ma sát cao ($\text{friction} = 1.0 - 14.0 \times \Delta t$), tiêu tán hoàn toàn chỉ sau khoảng $0.10 - 0.15$ giây.
   - Độ dời vật lý thực tế đạt khoảng **$7 - 12\text{px}$** ("knockback nhẹ"), đủ để hãm đà lao tới của quái cận chiến mà không phá vỡ tìm đường hay văng quá xa.
3. **Bảo vệ va chạm địa hình (Collision Safety)**:
   - Mọi bước di chuyển đẩy lùi đều kiểm tra độc lập qua `dungeonMap.isAreaWalkable(...)`.
   - Nếu phía sau kẻ địch là tường gạch, cửa đóng hoặc vật cản solid, xung lực tự động triệt tiêu về $0$, ngăn chặn triệt để hiện tượng xuyên tường hoặc kẹt vào địa hình.
4. **Kháng đẩy lùi của Boss (Boss Poise Armor)**:
   - Boss Dummy khổng lồ (`TITAN MECHA DUMMY`) sở hữu giáp hạng nặng, tự động triệt tiêu **$85\%$** xung lực đẩy lùi (chỉ chịu $15\%$), mang lại cảm giác một cỗ máy chiến tranh kiên cố, sừng sững.

---

## 8. Sửa đổi / Cập nhật
* **22/10/2026**:
  - Triển khai 2 Archetype kẻ địch mới:
    + `MedicEnemy`: Kẻ địch hỗ trợ quân y, không tấn công, có hào quang hồi máu $135\text{px}$ hồi $6\text{ HP}$ mỗi $0.8\text{s}$, chủ động né tránh người chơi và ưu tiên tiếp cận đồng đội bị thương nặng nhất. Xuất hiện từ Stage 2.
    + `GrenadierEnemy`: Kẻ địch công binh hạng nặng ném lựu đạn hẹn giờ `EnemyGrenade` nổ sau đúng $2.0\text{s}$ gây sát thương diện rộng $24\text{ HP}$ bán kính $78\text{px}$, ép góc người chơi và phá hủy cover. Xuất hiện từ Stage 3.
  - Tích hợp hiệu ứng hào quang phát quang xanh lục và vòng cảnh báo nổ nguy hiểm chớp đỏ chuẩn 100% Java `Graphics2D`.
  - Phân tầng xuất hiện tại Stage 2, Stage 3 và Stage 4 Boss Arena trong `PlayingState`.
  - Bổ sung 14 bài kiểm thử chuyên sâu trong `MedicAndGrenadierEnemyTest`, đạt $100\%$ độ bao phủ nghiệp vụ và tất cả test đều đỗ.
* **18/10/2026**:
  - Triển khai hoàn thiện `MeleeEnemy` và `RangedPistolEnemy`.
  - Bổ sung kiểm tra tầm nhìn `hasLineOfSight` trên `DungeonMap`.
  - Phân luồng sát thương đạn kẻ địch đối với người chơi trong `PlayingState`.
  - Bổ sung cơ chế tự động tăng chỉ số theo Stage (HP, Tốc độ, Sát thương, Hồi chiêu) và gắn tag phân cấp `MK-2`, `MK-3`.
  - Bổ sung cơ chế bị đẩy lùi nhẹ khi nhận sát thương (`takeDamage` kèm knockback impulse, ma sát giảm tốc, chống xuyên tường và kháng đẩy lùi cho Boss).
  - Mở rộng bộ kiểm thử tự động `EnemyAITest` lên 14 bài test đỗ $100\%$.
