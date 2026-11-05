Cập nhật mới nhất vào ngày: 04/11/2026

# BÁO CÁO DỰ ÁN CUỐI KỲ MÔN HỌC LẬP TRÌNH JAVA
## ĐỀ TÀI: THIẾT KẾ VÀ PHÁT TRIỂN HỆ THỐNG TRÒ CHƠI BẮN SÚNG ROGUELITE TỪ GÓC NHÌN TRÊN XUỐNG "DONTKNOW" DỰA TRÊN KIẾN TRÚC ĐA LUỒNG, BỘ TRỘN ÂM THANH SỐ PCM ĐA KÊNH VÀ THUẬT TOÁN SINH HẦM NGỤC THỦ TỤC TRÊN NỀN TẢNG JAVA THUẦN

---

* **Tên dự án**: **DontKnow – Top-Down Roguelite Shooter Engine**
* **Môn học**: Lập trình Java Nâng Cao (Advanced Java Programming)
* **Chủ đề đăng ký**: **Topic 2 – Java Game Development**
* **Năm học**: 2026 – 2027
* **Nhóm thực hiện**: Group Unknown (Team Morningstar)
* **Mã nguồn dự án**: [`GroupProject`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject)
* **Phiên bản phát hành**: v1.0.0-SNAPSHOT (Đã đóng gói JAR thực thi `DontKnow.jar` & Bản Portable All-In-One kèm JRE nhúng sẵn)

---

## BẢNG PHÂN CÔNG TRÁCH NHIỆM KỸ THUẬT CỦA CÁC THÀNH VIÊN

| STT | Họ và Tên | Mã Sinh Viên | Vai trò & Trách nhiệm kỹ thuật chính | Đóng góp (%) |
|---|---|---|---|---|
| 1 | **Trưởng Nhóm** | *(Student ID 1)* | **Lead Engine Architect & Enemy AI Engineer**: Kiến trúc vòng lặp game đa luồng (`GameLoop`), Quản lý trạng thái game (`GameStateManager`), Trí tuệ nhân tạo kẻ địch (Enemy AI) tìm đường BFS trên lưới ô tile, Cảm biến xúc tu đa hướng né vật cản (`Steering Feelers`), Thiết kế Boss Chiến Xa Titan MK-IV 3 lớp xoay độc lập (`BossTank`), Quân y (`MedicEnemy`), Công binh (`GrenadierEnemy`), Lưu/Khôi phục Checkpoint (`RunSaveManager`) & Bộ kiểm thử tự động 315 Unit Tests JUnit 5. | 34% |
| 2 | **Thành viên 1** | *(Student ID 2)* | **Combat, Weapon & Upgrade Systems Engineer**: Khung đa xạ kích $360^\circ$ (`Weapon`, `WeaponManager`), Hệ thống đạn đạo vật lý (`ProjectileManager`), Độ giật nở tâm súng AR/SCR, Hệ thống nâng cấp nhân vật & 7 Perk sinh học dã chiến (`CharacterUpgradeStation`, `PlayerBuffManager`, `CharacterBuff`), Hệ thống nâng cấp và tẩy luyện chỉ số vũ khí (`StatReforgeStation`, `WeaponLevels`), Giao diện Balo tác chiến 2 Tab (`BackpackUI`). | 33% |
| 3 | **Thành viên 2** | *(Student ID 3)* | **Map Systems & Level Designer**: Thuật toán sinh hầm ngục ngẫu nhiên theo thủ tục trên lưới ô tile 4 hướng (`DungeonMap`), Thiết kế các buồng phòng đặc chủng (`Room`, `ShopRoom`, `UpgradeRoom`, `ExtractZone`), Cơ chế blast doors khóa phòng giao tranh đa pha (`CombatRoom`), Radar bản đồ thu nhỏ (`MinimapHUD`), Hệ thống Camera và chuyển cảnh màn chơi (`Camera`, `ScreenTransition`). | 33% |

---

# MỤC LỤC BÁO CÁO (THEO CHUẨN INSTRUCTIONS.MD)

1. **Title (Tiêu đề)**
2. **Abstract (Tóm tắt)**
3. **Keywords (Từ khóa)**
4. **Introduction (Giới thiệu)**
5. **Problem Definition (Định nghĩa vấn đề)**
6. **Requirements (Yêu cầu hệ thống: Chức năng & Phi chức năng)**
7. **System Architecture & Game Operation (Kiến trúc hệ thống & Quy trình vận hành chi tiết)**
8. **Design (Thiết kế hướng đối tượng, SOLID & Mẫu thiết kế)**
9. **Implementation (Chi tiết hiện thực hóa mã nguồn & Các phân hệ)**
10. **Technical Details & Algorithms (Chi tiết kỹ thuật chuyên sâu & Các thuật toán cốt lõi)**
11. **Engineering Challenges & Solutions (Các vấn đề kỹ thuật đã gặp phải & Cách xử lý triệt để)**
12. **Testing (Kiểm thử tự động JUnit 5 với 315 bài test - 100% Pass)**
13. **Experimental Results (Kết quả thực nghiệm & Đo lường hiệu năng)**
14. **Discussion (Thảo luận & Đánh giá)**
15. **Novelty and Contributions (Tính mới & Đóng góp kỹ thuật)**
16. **Limitations & Future Work (Hạn chế hiện tại & Hướng phát triển tương lai)**
17. **Conclusion (Kết luận)**
18. **References (Tài liệu tham khảo)**
19. **Appendix (Phụ lục: Hướng dẫn cài đặt, Cấu hình phím tắt, Ký hiệu Radar)**
20. **Curriculum Mapping (Đối chiếu kiến thức môn học với dự án)**

---

# 1. TITLE (TIÊU ĐỀ)

**DONTKNOW: THIẾT KẾ VÀ PHÁT TRIỂN HỆ THỐNG TRÒ CHƠI BẮN SÚNG ROGUELITE TỪ GÓC NHÌN TRÊN XUỐNG DỰA TRÊN KIẾN TRÚC ĐA LUỒNG, BỘ TRỘN ÂM THANH SỐ ĐA KÊNH VÀ THUẬT TOÁN SINH HẦM NGỤC THỦ TỤC TRÊN NỀN TẢNG JAVA THUẦN**

*(English: DESIGN AND IMPLEMENTATION OF "DONTKNOW" – A HIGH-PERFORMANCE 2D TOP-DOWN ROGUELITE SHOOTER ENGINE POWERED BY MULTITHREADED CONCURRENCY, REAL-TIME POLYPHONIC PCM AUDIO MIXING, AND PROCEDURAL DUNGEON GENERATION IN PURE JAVA)*

---

# 2. ABSTRACT (TÓM TẮT)

Dự án **DontKnow** là một công trình phần mềm hoàn chỉnh nhằm nghiên cứu, thiết kế và phát triển một hệ thống trò chơi điện tử hành động roguelite góc nhìn từ trên xuống (Top-Down Roguelite Shooter) hiệu năng cao, được xây dựng dựa trên nền tảng **Java 21 LTS**, thư viện đồ họa **Java AWT/Swing (`Graphics2D`)**, giao diện âm thanh mức thấp **Java Sound API (`SourceDataLine`)** kết hợp bộ giải mã luồng **JLayer MP3**, hoàn toàn không phụ thuộc vào bất kỳ game engine bên thứ ba nào (như Unity, Godot hay LibGDX).

Hệ thống thể hiện việc áp dụng toàn diện các nguyên lý Công nghệ Phần mềm hiện đại: Lập trình Hướng đối tượng (**OOP**), 5 nguyên lý thiết kế **SOLID**, các Mẫu thiết kế (**Design Patterns**), Lập trình Đa luồng (**Multithreading**) với vòng lặp game tách rời (**Decoupled Fixed-Timestep Game Loop**) kết hợp luồng trộn âm thanh số thời gian thực (**Real-Time Polyphonic PCM Audio Mixer Thread**), và Lập trình Hàm (**Functional Programming / Stream API**).

Các trụ cột kỹ thuật và thuật toán nổi bật của hệ thống bao gồm:
1. **Thuật toán sinh bản đồ hầm ngục thủ tục (Procedural Dungeon Generation)** dựa trên lưới logic 4 hướng trực giao (12h, 3h, 6h, 9h), cơ chế phòng đặc biệt 2 lối vào/ra (Transit Hub Crossroads), và 6 cấu trúc phòng chiến đấu (Combat Archetypes) biến thiên kích thước từ $12 \times 9$ đến $14 \times 10$ kèm 6 bố cục vật chắn kiên cố (`COVER`) và thùng gỗ phá hủy được (`DESTRUCTIBLE_COVER`).
2. **Hệ thống điều khiển xạ kích $360^\circ$ & 7 lớp vũ khí chiến thuật**: Bao gồm Súng trường tấn công (`AssaultRifle`), Súng trường bán tự động (`SemiCarbineRifle`), Súng bắn tỉa kéo quy lát (`SniperRifle`), Súng hoa cải bơm tay (`Shotgun`), Súng tiểu liên (`SMG`), Súng lục (`Pistol`) và Dao găm (`CombatKnife`), tích hợp mô phỏng nở tâm giật súng (Bloom Recoil), cơ chế lên đạn từng viên/kéo quy lát sau mỗi phát bắn, nâng cấp chọn nhánh chỉ số (DMG, RPM, ACC) và phân hạng phẩm chất tới Hạng S (`S-Tier`).
3. **Trí tuệ nhân tạo (AI) kẻ địch đa tầng & Trùm cuối Chiến Xa Titan (`BossTank`)**: Kết hợp thuật toán tìm đường chiều rộng trên lưới ô tile (Tile-based BFS Pathfinding), mảng 10 cảm biến xúc tu lượng giác tránh vật cản (`Steering Feelers`), tia kiểm tra tầm nhìn thẳng (`Raycasting Line-of-Sight`), quái Quân y (`MedicEnemy`), quái Công binh ném lựu đạn hẹn giờ 2.0s (`GrenadierEnemy`), và trùm cuối `BossTank` 3 tầng xoay độc lập với đại pháo siêu thanh suy giảm sát thương tuyến tính theo khoảng cách từ tâm vụ nổ (40 HP tại tâm nổ giảm dần ra rìa bán kính 165px).
4. **Bộ trộn âm thanh phần mềm đa kênh thời gian thực (`GameSoundManager`)**: Vận hành trên một luồng Daemon độc lập đẩy mẫu PCM 44.1kHz 16-bit Stereo qua một `SourceDataLine` duy nhất, tích hợp bộ nén **Soft-Knee Limiter** chống vỡ tiếng, quản lý **39 hiệu ứng âm thanh (SFX)** và **7 bản nhạc nền (BGM)** hỗ trợ chuyển cảnh liền mạch (0ms gap) giữa cắt cảnh giới thiệu Boss 2.8s (`BossIntroCinematic`) và vòng lặp chiến đấu.
5. **Hệ thống lưu trữ Checkpoint tự động (`RunSaveManager`) & Kiểm thử tự động JUnit 5**: Lưu trữ bền vững tiến trình vượt ải ra đĩa cứng (`save/run_save.properties`) và được kiểm chứng bởi **17 bộ kiểm thử (Test Suites)** với **315 bài kiểm thử tự động (Unit Tests) trên 24 bộ kiểm thử (Test Suites)** đạt tỷ lệ vượt qua tuyệt đối **100% PASS**.

---

# 3. KEYWORDS (TỪ KHÓA)

*Java Game Development; Object-Oriented Programming (OOP); SOLID Principles; Multithreaded Concurrency; Decoupled Game Loop; Real-Time Polyphonic PCM Audio Mixer; Soft-Knee Limiter; Procedural Dungeon Generation; 4-Way Adjacency Grid; BFS Pathfinding AI; Spatial Hashing; Raycasting Line-of-Sight; Distance-Attenuated AoE Ballistics; JUnit 5 Automated Testing.*

---

# 4. INTRODUCTION (GIỚI THIỆU)

### 4.1 Bối cảnh và Tầm quan trọng
Phát triển hệ thống trò chơi điện tử tương tác thời gian thực (Real-Time Interactive Systems) là một trong những bài toán tổng hợp khắt khe nhất của Kỹ thuật Phần mềm. Khác với các ứng dụng nghiệp vụ thông thường vốn hoạt động theo chu kỳ yêu cầu – phản hồi (Request – Response), một trò chơi hành động góc nhìn từ trên xuống đòi hỏi hệ thống phải đồng thời giải quyết bài toán động học vật lý, kiểm tra va chạm không gian, điều phối trí tuệ nhân tạo, trộn âm thanh đa kênh và kết xuất đồ họa 2D với chu kỳ nghiêm ngặt **60 khung hình/giây** (ngân sách thời gian chỉ **16.67 mili-giây** mỗi khung hình).

Việc tự thiết kế và hiện thực hóa toàn bộ engine từ con số không (From Scratch) trên nền tảng Java thuần túy giúp nhóm phát triển làm chủ hoàn toàn kiến trúc đa luồng, quản lý bộ nhớ đệm phần cứng (VRAM Compatible Images), xử lý tín hiệu âm thanh số (Digital Signal Processing - DSP) và tối ưu hóa độ phức tạp thuật toán.

### 4.2 Cảm hứng thiết kế và Định hướng sản phẩm
Dự án **DontKnow** kết hợp tinh hoa của hai dòng game hành động 2D kinh điển:
* **Khám phá Hầm ngục Roguelite (*Soul Knight*)**: Bản đồ sinh ngẫu nhiên theo thủ tục qua 4 Stage, cửa blast doors tự động phong tỏa giao tranh đa pha, các phòng chức năng chuyên biệt (Cửa hàng quân nhu `SHOP`, Xưởng tẩy luyện súng `RESET_STAT`, Phòng nâng cấp sinh học `UPGRADE`), và hòm tiếp tế vũ khí phân cấp theo độ hiếm.
* **Xạ kích Chiến thuật $360^\circ$ & Sinh tồn Dã chiến (*Surviv.io*)**: Ngắm bắn tự do $360^\circ$ theo góc lượng giác con trỏ chuột, quản lý kho đạn dự trữ riêng biệt theo từng chủng loại súng, hệ thống vật chắn 2 tầng (cột đá vĩnh cửu và thùng gỗ 40 HP có thể bắn/chém vỡ), cơ chế nhào lộn né đòn (Dodge Roll) có khung bất tử (I-frames), và nhịp độ đấu súng tốc độ cao.

---

# 5. PROBLEM DEFINITION (ĐỊNH NGHĨA VẤN ĐỀ)

Quá trình xây dựng trò chơi trên nền tảng Java thuần đặt ra 5 bài toán cốt lõi:
1. **Nghẽn luồng giao diện (EDT Thread Contention) & Độ trễ điều khiển**: Nếu cập nhật vật lý và AI ngay trên luồng sự kiện `Event Dispatch Thread (EDT)` của Swing, giao diện sẽ bị giật cục và sự kiện bàn phím/chuột bị trễ.
2. **Độ phức tạp kiểm tra va chạm không gian & Sinh bản đồ ngẫu nhiên**: Kiểm tra va chạm giữa hàng trăm viên đạn với hàng nghìn ô gạch địa hình nếu dùng duyệt vét cạn $O(N^2)$ sẽ gây sụt giảm FPS nghiêm trọng; đồng thời sinh bản đồ ngẫu nhiên dễ tạo ra phòng cụt hoặc vật cản bít lối đi.
3. **Trí tuệ nhân tạo (AI) di chuyển chiến thuật & Phối hợp binh chủng**: Kẻ địch cần biết tìm đường vòng qua tường/cột đá mà không bị kẹt góc, biết giữ khoảng cách (Kiting), cứu thương đồng đội (`MedicEnemy`), ném lựu đạn ép góc (`GrenadierEnemy`) và điều khiển trùm `BossTank` đa tầng.
4. **Giới hạn phần cứng của Java Sound `Clip` khi phát âm thanh súng liên thanh có tiếng vọng (Reverb Tail)**: Cơ chế `DirectClip` mặc định của Windows/Java khi đóng mở liên tục hoặc phát chồng hàng chục viên đạn AR/SMG có đuôi vọng dài sẽ gây nghẽn đường truyền phần cứng, mất tiếng (Audio Cutout) hoặc vỡ tiếng (Clipping).
5. **Quản lý trạng thái phức tạp, lưu trữ tiến trình bền vững và tối ưu bộ nhớ tài nguyên**: Đảm bảo chuyển đổi mượt mà giữa Menu, Trường bắn `The Range`, Chiến dịch vượt ải, Chế độ đấu Boss độc lập, Lưu/Tiếp tục ván chơi (`RunSaveManager`) và đóng gói toàn bộ tài nguyên gọn nhẹ trong một thư mục chuẩn duy nhất (`src/main/resources/`).

---

# 6. REQUIREMENTS (YÊU CẦU HỆ THỐNG)

## 6.1 Yêu Cầu Chức Năng (Functional Requirements - FR)
* **FR1 – Vòng lặp Game Đa luồng Tách rời**: Luồng cập nhật logic/vật lý cố định ở mức $60\text{ UPS}$ tách biệt hoàn toàn với luồng kết xuất đồ họa Swing EDT ($60\text{ FPS}$) và luồng trộn âm thanh số PCM.
* **FR2 – Sinh Hầm ngục Thủ tục Lưới 4 Hướng & Phòng Đặc biệt 2 Cửa**: Tự động sinh bản đồ gồm phòng `SPAWN`, 3–5 phòng `COMBAT` (6 Archetypes kích thước và bố cục cover), phòng `EXTRACT`, cùng các phòng tiện ích (`SHOP`, `RESET_STAT`, `UPGRADE`) ưu tiên đặt tại nút giao 2 lối vào/ra.
* **FR3 – Hệ thống 7 Vũ khí, Đạn đạo Vật lý & Tẩy luyện/Nâng cấp**: Hỗ trợ 7 loại vũ khí (`AssaultRifle`, `SemiCarbineRifle`, `SniperRifle`, `Shotgun`, `SMG`, `Pistol`, `CombatKnife`) với 4 loại đạn dự trữ, nâng cấp chọn 1 trong 3 nhánh chỉ số (`DMG`, `RPM`, `ACC`), tự động thăng cấp mỗi khi qua màn đối với súng Hạng S (`S-Tier`), và tẩy luyện lại sao/chí mạng tại `StatReforgeStation`.
* **FR4 – Hệ sinh thái Kẻ địch Đa binh chủng & Trùm cuối Chiến Xa Titan**: Bao gồm Quái cận chiến (`MeleeEnemy`), Quái xạ thủ (`RangedPistolEnemy`), Quân y (`MedicEnemy`), Công binh lựu đạn (`GrenadierEnemy`) và Trùm cuối `BossTank` (Stage 4) sở hữu 3 tầng xoay độc lập, đòn đâm húc nghiền nát thùng gỗ, súng máy liên thanh và đại pháo siêu thanh có sát thương giảm dần theo khoảng cách từ tâm vụ nổ (tâm nổ 40 HP).
* **FR5 – Hệ thống Sinh trắc học Bio-Tech (6 Chỉ số & 7 Perks)**: Nâng cấp 6 chỉ số cơ bản và mở khóa 7 Perk đặc biệt (`BLOCK_SHIELD`, `KILL_HEAL`, `PHASE_DODGE`, `TINY_HITBOX`, `PERIODIC_MAG`, `EXTENDED_MAG`, `CRIT_MASTER`).
* **FR6 – Trường bắn Huấn luyện (`The Range`) & Lưu trữ Checkpoint (`RunSaveManager`)**: Cho phép thử nghiệm toàn bộ vũ khí, nâng cấp miễn phí lên cấp 10 và phân tích vệt đạn tại `The Range`; tự động lưu tiến trình vượt ải vào tệp `save/run_save.properties` để tiếp tục chơi bất cứ lúc nào.
* **FR7 – Hệ thống Âm thanh Đa kênh (39 SFX & 7 BGM)**: Phát đầy đủ hiệu ứng âm thanh cho mọi thao tác chiến đấu, nạp đạn, lên đạn, tương tác UI/môi trường và 7 bản nhạc nền tự động chuyển tiếp theo ngữ cảnh màn chơi.

## 6.2 Yêu Cầu Phi Chức Năng (Non-Functional Requirements - NFR)
* **NFR1 – Hiệu năng thời gian thực**: Duy trì ổn định $60.0\text{ UPS} / 60.0\text{ FPS}$, thời gian xử lý mỗi khung hình $\le 16.67\text{ ms}$.
* **NFR2 – Tối ưu dung lượng & Bộ nhớ**: Hợp nhất 100% tài nguyên (Sprites, Textures, Fonts, SFX, BGM) vào `src/main/resources/` (loại bỏ hoàn toàn các thư mục trùng lặp bên ngoài), mức chiếm dụng RAM Heap $\le 120\text{ MB}$.
* **NFR3 – Độ tin cậy & Kiểm thử tự động**: Bao phủ toàn bộ nghiệp vụ bằng **315 Unit Tests** trên JUnit 5, chạy tự động trong môi trường Headless.

---

# 7. SYSTEM ARCHITECTURE & GAME OPERATION (KIẾN TRÚC HỆ THỐNG & QUY TRÌNH VẬN HÀNH CHI TIẾT)

## 7.1 Sơ Đồ Kiến Trúc Đa Luồng 3 Tầng (3-Thread Decoupled Architecture)

Hệ thống vận hành đồng thời **3 luồng chuyên biệt** phối hợp nhịp nhàng:
1. **Physics & Simulation Worker Thread (`GamePanel.run()`)**: Chạy vòng lặp Fixed-Timestep $60\text{ UPS}$ ($\Delta t = 1/60\text{ s}$), cập nhật trạng thái game (`GameStateManager.update()`), giải thuật AI, quỹ đạo đạn, kiểm tra va chạm không gian và xử lý sự kiện bàn phím/chuột.
2. **AWT/Swing Event Dispatch Thread (`EDT`)**: Đảm nhận kết xuất đồ họa 2D (`paintComponent(Graphics)` $\rightarrow$ `GameStateManager.render(Graphics2D)`) trên bộ đệm kép (`Double Buffering`) kèm co giãn khung hình tỷ lệ chuẩn $16:9$ ($1280 \times 720$).
3. **Software Audio Mixer Daemon Thread (`GameSoundManager-SoftwareMixer`)**: Chạy liên tục với chu kỳ $\approx 5.8\text{ ms}$ ($256\text{ frames}$ mỗi chu kỳ ở tần số lấy mẫu $44,100\text{ Hz}$), trộn (mix) đồng thời tới **64 luồng hiệu ứng âm thanh (SFX)** cùng luồng nhạc nền **BGM (MP3/WAV)** và đẩy thẳng ra thiết bị phần cứng qua một đường truyền `SourceDataLine` duy nhất.

```
+---------------------------------------------------------------------------------------------------+
|                                         MAIN APPLICATION                                          |
|                                   com.groupproject.game.Main                                      |
+-------------------------------------------------+-------------------------------------------------+
                                                  |
        +-----------------------------------------+-----------------------------------------+
        |                                         |                                         |
        v                                         v                                         v
+-------------------------------+   +-----------------------------------+   +-------------------------------+
|   SWING EDT RENDER THREAD     |   |   SIMULATION WORKER THREAD        |   |   AUDIO MIXER DAEMON THREAD   |
|  - GameWindow / GamePanel     |   |  - Fixed-Timestep 60.0 UPS Loop   |   |  - GameSoundManager           |
|  - Double-Buffered Graphics2D |   |  - KeyboardHandler & MouseHandler |   |  - 44.1kHz 16-bit Stereo PCM  |
|  - Hardware Compatible VRAM   |   |  - GameStateManager.update(dt)    |   |  - 64 Polyphonic SFX Voices   |
|  - Viewport Culling           |   |  - Physics, Collision, AI, Buffs  |   |  - JLayer MP3 & WAV Streaming |
|                               |   |  - RunSaveManager Checkpoints     |   |  - Soft-Knee Peak Limiter     |
+-------------------------------+   +-----------------------------------+   +-------------------------------+
```

## 7.2 Quy Trình Hoạt Động Chi Tiết Của Trò Chơi (End-to-End Game Flow)

1. **Khởi động & Nạp bộ đệm tài nguyên (`Main` $\rightarrow$ `GameWindow` $\rightarrow$ `GamePanel`)**:
   - Ngay khi khởi chạy, hệ thống nạp trước (Preload) toàn bộ 12 Sprite/Texture địa hình vào bộ nhớ tương thích VRAM (`GameSpriteManager`), 7 Sprite vũ khí (`WeaponSpriteManager`), font chữ pixel `DeArPix` (`GameFontManager`), và giải mã toàn bộ 39 tệp âm thanh SFX sang mảng mẫu `short[]` PCM 44.1kHz Stereo trong RAM (`GameSoundManager.init()`).
2. **Màn hình Menu Chính (`MenuState`)**:
   - Phát nhạc nền `In The Sky - Evening Telecast.mp3` (tự động lặp lại).
   - Tự động kiểm tra tệp lưu `save/run_save.properties` thông qua phương thức đọc nhẹ `RunSaveManager.peekSaveInfo()` (không làm thay đổi trạng thái tĩnh của game). Nếu có ván chơi đang dở dang, nút **"TIẾP TỤC CHƠI (STAGE X)"** sẽ xuất hiện trên cùng.
   - Cung cấp các lựa chọn: **Tiếp tục chơi**, **Chiến dịch mới (Stage 1)**, **Đấu Trùm (Boss Tank Arena)**, **Trường bắn (The Range)**, **Cài đặt âm lượng & Phím tắt**, và **Thoát**.
3. **Sảnh chờ & Trường bắn Huấn luyện (`HubRangeState` - The Range)**:
   - Phát nhạc nền `Play Again_ - Evening Telecast.mp3`.
   - Người chơi tự do lấy thử cả 7 loại vũ khí từ giá súng, nâng cấp miễn phí lên tối đa Cấp 10, tẩy luyện thử chỉ số tại bệ rèn, bắn vào bia tĩnh/bia di động để xem thống kê DPS thời gian thực và quan sát vệt đạn đạo dạ quang (`RecoilTracerManager`).
   - Khi bước vào cổng dịch chuyển **DEPLOY PORTAL**, hệ thống thu hồi toàn bộ vũ khí thử nghiệm tại trường bắn để đảm bảo tính công bằng và đưa người chơi bước vào **Stage 1** với trang bị khởi đầu tiêu chuẩn (`Pistol` + `CombatKnife`).
4. **Chiến dịch Vượt Ải Hầm Ngục (`PlayingState` - Stage 1 đến Stage 3)**:
   - Phát nhạc nền `Survival Mode - Blue Deer Studio.mp3` xuyên suốt từ Stage 1 đến Stage 3.
   - Ở mỗi Stage, `DungeonMap.generateStage(stage)` sinh ngẫu nhiên sơ đồ hầm ngục lưới 4 hướng.
   - Khi người chơi bước vào một phòng `COMBAT` chưa khám phá, toàn bộ cửa tự động đóng sập (`DOOR_LOCKED`). Quái vật xuất hiện qua hiệu ứng vòng tròn triệu hồi (`Spawn Telegraph` 0.4s):
     - **Stage 1**: Quái cận chiến (`MeleeEnemy`) + Quái súng lục (`RangedPistolEnemy`).
     - **Stage 2**: Bổ sung thêm Quân y hỗ trợ (`MedicEnemy`) có hào quang hồi máu.
     - **Stage 3**: Bổ sung thêm Công binh hạng nặng (`GrenadierEnemy`) ném lựu đạn hẹn giờ 2.0s.
   - Sau khi tiêu diệt hết toàn bộ các đợt quái (1 hoặc 2 Phase), cửa phòng mở lại, thưởng tiền `Credits`, rơi đạn/máu và mở khóa hòm vũ khí (`WeaponChest`). Ở Stage 1, hòm vũ khí đầu tiên chắc chắn rơi ra một khẩu súng chính miễn phí.
   - Người chơi sử dụng `Credits` tại phòng `SHOP` (mua súng, máu, đạn), phòng `RESET_STAT` (tẩy luyện lại phân bổ sao hoặc chí mạng của súng), và phòng `UPGRADE` (nâng 6 chỉ số cơ bản hoặc mở khóa 7 Perk đặc biệt).
   - Khi bước lên bục sơ tán `EXTRACT` và nhấn `[F]`, hệ thống tự động thăng thêm +1 cấp cho mọi vũ khí Hạng S (`S-Tier`), lưu Checkpoint tự động qua `RunSaveManager.saveProgress(this)` và chuyển sang Stage tiếp theo.
5. **Trận Chiến Cuối Cùng Với Chiến Xa Titan (`PlayingState` - Stage 4 Boss Fight)**:
   - Khi bước vào phòng Grand Boss Arena ở Stage 4, trò chơi kích hoạt cắt cảnh giới thiệu trùm **`BossIntroCinematic`** kéo dài **2.8 giây**: máy quay lướt từ vị trí người chơi sang khóa mục tiêu vào chiếc xe tăng `BossTank`, hai dải băng đen điện ảnh (Letterbox) khép lại kèm bảng cảnh báo chiến thuật.
   - Cùng lúc đó, `GameSoundManager.playBossIntroBgm()` phát chính xác 2.8 giây cao trào cuối của bản nhạc `Intro.wav` (với hiệu ứng **Fade-in 0.75s** mượt mà). Ngay khoảnh khắc cắt cảnh kết thúc và hiện dòng chữ **"FIGHT!"**, hệ thống tự động nối tiếp không độ trễ (0ms gap) sang bản nhạc chiến đấu `Middle_loop.wav`.
   - Trong trận đánh, nếu toàn bộ 8 lính hộ tống bị tiêu diệt mà Boss vẫn còn sống, sau mỗi 5 giây hệ thống sẽ triệu hồi đội quân tiếp viện 5–6 lính để duy trì áp lực và cung cấp đạn dược rơi ra cho người chơi.
   - Khi máu Boss xuống dưới 50% ($\le 625\text{ HP}$), tháp đại pháo chính thức tỉnh (`isCannonAwakened() == true`), chiếu tia laser đỏ khóa mục tiêu rồi khai hỏa đạn pháo siêu thanh nổ diện rộng.
   - Khi tiêu diệt thành công `BossTank`, hệ thống phát bản nhạc chiến thắng `end.wav` (`BgmTrack.BOSS_END`), xóa file save tạm thời (vì chiến dịch đã hoàn tất vẻ vang) và mở cổng sơ tán chiến thắng.
6. **Xử lý Tử trận (`Game Over`) & Quay về Trường bắn**:
   - Nếu máu người chơi về $0$, hệ thống xóa file save hiện tại (cơ chế Permadeath của dòng game Roguelite), phát hiệu ứng âm thanh `gameover.wav` và bắt đầu phát bản nhạc nền `Game Over - Evening Telecast.mp3`.
   - Ngay cả khi người chơi nhấn phím `[R]` để quay về sảnh chờ `The Range`, bản nhạc `Game Over` vẫn tiếp tục chạy trọn vẹn hết bài để giữ mạch cảm xúc; ngay khi bài `Game Over` kết thúc, hệ thống tự động chuyển sang phát nhạc nền `Play Again_ - Evening Telecast.mp3` của `The Range`.

---

# 8. DESIGN (THIẾT KẾ HƯỚNG ĐỐI TƯỢNG, SOLID & DESIGN PATTERNS)

## 8.1 Vận Dụng 4 Trụ Cột Lập Trình Hướng Đối Tượng (OOP)
* **Tính Đóng Gói (Encapsulation)**: Toàn bộ trạng thái nội tại của thực thể (`Player`, `Weapon`, `TargetDummy`, `Tile`, `RunProgressData`) được bảo vệ bằng phạm vi `private`/`protected` và chỉ biến đổi qua các phương thức kiểm soát chặt chẽ (như `damagePlayer`, `upgradeSelectedBranch`, `rerollStarsOnly`, `rerollCritOnly`).
* **Tính Trừu Tượng Hóa (Abstraction)**: Trừu tượng hóa hành vi qua lớp cơ sở `GameState`, `Weapon`, `TargetDummy`, `Projectile`.
* **Tính Kế Thừa (Inheritance)**:
  - Nhánh Vũ khí: `Weapon` $\rightarrow$ `AssaultRifle`, `SemiCarbineRifle`, `SniperRifle`, `Shotgun`, `SMG`, `Pistol`, `CombatKnife`.
  - Nhánh Thực thể địch: `TargetDummy` $\rightarrow$ `MeleeEnemy`, `RangedPistolEnemy`, `MedicEnemy`, `GrenadierEnemy`, `BossTank`.
  - Nhánh Đạn đạo: `Projectile` $\rightarrow$ `BossCannonShell`, `EnemyGrenade`.
* **Tính Đa Hình (Polymorphism)**: Vòng lặp `PlayingState` và `ProjectileManager` xử lý đồng nhất danh sách `List<TargetDummy>` và `List<Projectile>`. Khi đạn va chạm hoặc hết thời gian sống, lời gọi đa hình tự động kích hoạt `BossCannonShell.explode()` (vụ nổ suy giảm sát thương theo khoảng cách) hoặc `EnemyGrenade.explode()` (vụ nổ lựu đạn hẹn giờ 2.0s).

## 8.2 Áp Dụng 5 Nguyên Lý SOLID & Các Mẫu Thiết Kế (Design Patterns)
* **Single Responsibility Principle (SRP)**: Phân tách rõ ràng từng lớp chuyên trách: `DungeonMap` (địa hình), `ProjectileManager` (đạn đạo), `GameSoundManager` (trộn âm thanh PCM & BGM), `GameSpriteManager` (bộ đệm ảnh tương thích phần cứng VRAM), `RunSaveManager` (lưu/đọc tiến trình).
* **Open/Closed Principle (OCP)**: Dễ dàng mở rộng thêm vũ khí mới (như `SemiCarbineRifle`), kẻ địch mới (`MedicEnemy`, `GrenadierEnemy`) hoặc âm thanh mới (`SoundEffect`, `BgmTrack`) mà không làm xáo trộn cấu trúc cốt lõi.
* **Các Design Patterns chủ đạo**:
  1. **State Pattern**: Điều phối `MenuState`, `HubRangeState`, `PlayingState` qua `GameStateManager`.
  2. **Flyweight & Hardware Cache Pattern**: `GameSpriteManager`, `WeaponSpriteManager`, `GameFontManager` và `GameSoundManager` nạp tài nguyên một lần duy nhất vào RAM/VRAM và chia sẻ chung cho hàng trăm thực thể.
  3. **Producer-Consumer / Staging Buffer Pattern**: `ProjectileManager` sử dụng danh sách đệm `pendingAdd` để thu nhận đạn mới sinh ra trong frame và hợp nhất an toàn vào đầu chu kỳ `update()`, ngăn chặn tuyệt đối lỗi `ConcurrentModificationException`.

---

# 9. IMPLEMENTATION (CHI TIẾT HIỆN THỰC HÓA MÃ NGUỒN & CÁC PHÂN HỆ)

Mã nguồn dự án được tổ chức chuẩn hóa theo cấu trúc Apache Maven:
* `src/main/java/com/groupproject/game/`: Chứa toàn bộ mã nguồn Java của Engine và Gameplay.
* `src/main/resources/`: Thư mục tài nguyên chuẩn duy nhất chứa `BGM/`, `sfx/`, `weapons/`, `player/`, `enemy/`, `cover/`, `textures/`, `effects/`, `fonts/`.
* `src/test/java/com/groupproject/game/`: Chứa 17 bộ kiểm thử tự động JUnit 5 (315 bài test).

### 9.1 Phân Hệ Vũ Khí & Cơ Chế Nâng Cấp / Tẩy Luyện Độc Lập
* **7 Vũ khí chuyên biệt**:
  1. **Viper AR (`AssaultRifle`)**: Súng trường liên thanh cân bằng, băng đạn 30 viên, có đường cong nở tâm khi sấy liên tục.
  2. **Semi-Carbine Rifle (`SemiCarbineRifle` - SCR)**: Súng trường bán tự động 20 viên, sát thương cơ sở cao ($34\text{ HP}$), sơ tốc $1150\text{ px/s}$, viên đầu chính xác tuyệt đối, từ viên thứ 2 giật mạnh buộc người chơi bắn nhịp (Tap-firing).
  3. **SR Bolt-Action (`SniperRifle`)**: Súng bắn tỉa sát thương chí tử, xuyên thấu mục tiêu, sau mỗi phát bắn có hiệu ứng âm thanh kéo quy lát lên đạn (`GUN_SNIPER_LOADING_AMMO`).
  4. **Bulldog SG (`Shotgun`)**: Súng hoa cải bắn chùm 6–8 viên đạn phân mảnh; nạp đạn từng viên một (mỗi viên phát 1 tiếng `GUN_SHOTGUN_RELOAD`) và sau mỗi phát bắn phát tiếng bơm tay lên đạn (`GUN_SHOTGUN_LOADING_AMMO`).
  5. **Spectre SMG (`SMG`)**: Tiểu liên tốc độ xả đạn cực nhanh ở cự ly gần.
  6. **P-9 (`Pistol`)**: Súng lục tiêu chuẩn dùng đạn dự trữ vô hạn.
  7. **Tactical Knife (`CombatKnife`)**: Dao găm cận chiến vung nhát chém hình quạt đẩy lùi kẻ địch ($600\text{ px/s}$) và chém vỡ thùng gỗ tức thì.
* **Nâng cấp chọn nhánh & Tẩy luyện tách biệt (`StatReforgeModalUI`)**:
  - Khi nâng cấp súng, người chơi được chọn cộng $+1$ sao vào 1 trong 3 nhánh: **Sát thương (DMG)**, **Tốc độ bắn (RPM)**, hoặc **Độ chính xác (ACC)** kèm thông số xem trước (Preview).
  - Tại Xưởng Quân Khí (`StatReforgeStation`), người chơi có thể chọn **Reroll Sao Chỉ Số** (giữ nguyên Chí mạng) hoặc **Reroll Chí Mạng** (giữ nguyên số Sao 3 nhánh).

### 9.2 Phân Hệ Kẻ Địch & Trùm Chiến Xa Titan (`BossTank`)
* **Đội hình 4 chủng loại quái thường**: `MeleeEnemy` (cận chiến tìm đường BFS), `RangedPistolEnemy` (xạ thủ thả diều & lạng ngang), `MedicEnemy` (quân y phát xung hào quang đỏ bán kính $135\text{ px}$ hồi $6\text{ HP}$ mỗi $0.8\text{ s}$ cho đồng minh yếu máu nhất và chủ động chạy trốn khi người chơi áp sát $< 210\text{ px}$), `GrenadierEnemy` (công binh ném lựu đạn hẹn giờ đúng $2.0\text{ s}$ nổ bán kính $78\text{ px}$ gây $24\text{ HP}$ và phá hủy cover).
* **Trùm cuối `BossTank` (1250 HP, Hitbox $96\text{ px}$, 3 tầng xoay độc lập)**:
  - **Chiêu 1 – Đâm húc Rocket (`RAM_WINDUP` $\rightarrow$ `RAM_RUSH`)**: Chiếu vệt đỏ cảnh báo đường đâm (`Ram Telegraph Corridor`), khóa cứng góc ở $0.14\text{ s} - 0.16\text{ s}$ cuối để người chơi phản xạ lướt né, sau đó lao vọt với vận tốc $950\text{ px/s}$ gây $40\text{ HP}$ sát thương. Cỗ xe tăng nghiền nát mọi thùng gỗ trên đường đi mà không dừng lại, và chỉ bị choáng (`RAM_STUN`) khi đâm sầm vào tường đá hoặc cột trụ kiên cố (`COVER`).
  - **Chiêu 2 – Súng máy liên thanh (`MG_BURST`)**: Bắn loạt 6–8 viên đạn súng máy kết hợp chạy tạt ngang (`Strafe Run`).
  - **Chiêu 3 – Đại pháo Siêu thanh Suy giảm Sát thương theo Khoảng cách (`CANNON_AIMING` $\rightarrow$ `CANNON_LOCKED` $\rightarrow$ `BossCannonShell`)**: Thức tỉnh khi máu $< 50\%$. Chiếu tia laser đỏ bám chặt tọa độ người chơi trong $0.65\text{ s}$, khóa cứng góc ngắm trong $0.38\text{ s}$ (cửa sổ duy nhất để né), rồi phóng đạn đại pháo vận tốc $1350\text{ px/s}$ phát nổ bán kính $165\text{ px}$. Tại tâm vụ nổ, người chơi nhận đủ **40 HP** sát thương; càng ra xa tâm vụ nổ về phía rìa bán kính, sát thương giảm dần tuyến tính xuống mức tối thiểu $10\text{ HP}$ ($25\%$).

---

# 10. TECHNICAL DETAILS & ALGORITHMS (CHI TIẾT KỸ THUẬT CHUYÊN SÂU & CÁC THUẬT TOÁN CỐT LÕI)

## 10.1 Thuật Toán Vòng Lặp Game Bước Thời Gian Cố Định (Fixed-Timestep Accumulator Loop)
Hệ thống sử dụng biến tích lũy thời gian thực bằng `System.nanoTime()` với bước nhảy cố định $\Delta t = 1.0 / 60.0\text{ s}$ ($16.666\text{ ms}$). Cơ chế này đảm bảo mọi phép tính vận tốc, gia tốc, thời gian hồi chiêu và ngòi nổ lựu đạn hoạt động chính xác tuyệt đối và đồng nhất trên mọi cấu hình phần cứng.

## 10.2 Thuật Toán Băm Không Gian Lưới $O(1)$ (Spatial Grid Indexing & AABB Collision)
Thay vì duyệt qua danh sách hàng nghìn vật thể địa hình, tọa độ hộp bao AABB $(x, y, w, h)$ của thực thể hoặc viên đạn được ánh xạ trực tiếp sang chỉ số hàng và cột của mảng 2 chiều `Tile[45][80]` thông qua phép chia nguyên cho kích thước ô gạch (`TILE_SIZE = 48`):
$$\text{minCol} = \left\lfloor \frac{x}{48} \right\rfloor, \quad \text{maxCol} = \left\lfloor \frac{x + w - 0.001}{48} \right\rfloor, \quad \text{minRow} = \left\lfloor \frac{y}{48} \right\rfloor, \quad \text{maxRow} = \left\lfloor \frac{y + h - 0.001}{48} \right\rfloor$$
Nhờ đó, mỗi phép kiểm tra va chạm chỉ truy xuất từ 1 đến 9 ô gạch lân cận, đạt độ phức tạp hằng số **$O(1)$** (thời gian thực thi $< 0.004\text{ ms}$).

## 10.3 Thuật Toán Tìm Đường BFS Kết Hợp 10 Xúc Tu Lượng Giác (BFS Pathfinding & Multi-Angle Steering Feelers)
* **Raycasting Line-of-Sight**: Phóng tia kiểm tra tầm nhìn từ tâm quái đến tâm người chơi với bước nhảy $16\text{ px}$.
* **Tile-based BFS Pathfinding**: Khi tầm nhìn bị khuất bởi tường hoặc cột đá, quái cận chiến thực thi thuật toán Tìm kiếm theo chiều rộng (Breadth-First Search) trên đồ thị lưới ô gạch trong phạm vi phòng để tìm ô bước đi kế tiếp ngắn nhất dẫn tới người chơi.
* **10-Ray Steering Feelers**: Để tránh hiện tượng quái bị khựng lại ở mép góc cột đá, thuật toán phóng 10 vector cảm biến lệch góc từ $\pm 25^\circ$ đến $\pm 115^\circ$ quanh hướng di chuyển mong muốn, tự động chọn góc trượt thông thoáng nhất để lách mượt qua vật cản.

## 10.4 Thuật Toán Suy Giảm Sát Thương Vụ Nổ Tuyến Tính Theo Khoảng Cách (Linear Distance-Falloff AoE)
Trong lớp [`BossCannonShell`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/projectile/BossCannonShell.java), khi viên đạn đại pháo của Boss phát nổ tại tọa độ $(x_0, y_0)$ với bán kính $R = 165\text{ px}$ và sát thương cực đại tại tâm $D_{\max} = 40\text{ HP}$:
- Gọi $d = \sqrt{(x_p - x_0)^2 + (y_p - y_0)^2}$ là khoảng cách Euclid từ tâm vụ nổ đến tâm người chơi, $R_{\text{core}} = 16.0\text{ px}$ là bán kính lõi tâm nổ, và $R_{\text{max}} = R + r_{\text{player}}$ là bán kính va chạm tối đa.
- Nếu $d \le R_{\text{core}}$: Người chơi trúng trực diện tâm nổ và nhận nguyên $D = D_{\max} = 40\text{ HP}$.
- Nếu $R_{\text{core}} < d \le R_{\text{max}}$: Hệ số khoảng cách chuẩn hóa được tính bằng:
  $$t = \operatorname{clamp}\left(\frac{d - R_{\text{core}}}{R_{\text{max}} - R_{\text{core}}}, 0.0, 1.0\right)$$
  Lượng sát thương thực tế giảm dần tuyến tính từ $100\%$ ($40\text{ HP}$) tại tâm nổ xuống $25\%$ ($10\text{ HP}$) tại rìa ngoài cùng:
  $$D(d) = \max\left(1, \operatorname{round}\left(D_{\max} \times (1.0 - 0.75 \times t)\right)\right)$$

## 10.5 Thuật Toán Trộn Âm Thanh Số Đa Kênh Thời Gian Thực & Nén Biên Độ Mềm (Polyphonic PCM Mixing & Soft-Knee Limiter)
Trong [`GameSoundManager`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameSoundManager.java):
- Tất cả các luồng âm thanh đang phát (`ActiveVoice` cho SFX và `BgmStreamState` cho BGM) được cộng dồn tuyến tính từng mẫu âm thanh (Sample-by-sample superposition) trên bộ đệm số thực `float[] accumBuffer` ở tần số $44,100\text{ Hz}$ Stereo:
  $$S_{\text{mix}}[i] = S_{\text{BGM}}[i] \cdot G_{\text{BGM}} + \sum_{k=1}^{K} S_{k}[i] \cdot G_{\text{SFX}, k}$$
- Để ngăn chặn hiện tượng tràn số nguyên 16-bit ($\pm 32767$) gây rè/vỡ tiếng khi có 20–30 viên đạn nổ cùng lúc với nhạc nền, hệ thống áp dụng hàm **Nén biên độ mềm phi tuyến (Hyperbolic Soft-Knee Limiter)** ngay khi biên độ vượt ngưỡng $T = 24000$:
  $$y(x) = \operatorname{sgn}(x) \cdot \left( 24000 + 8767 \cdot \left(1 - \frac{1}{1 + \frac{|x| - 24000}{12000}}\right) \right) \quad \text{với } |x| > 24000$$
  Hàm toán học này tiệm cận trơn tru về ngưỡng trần $\pm 32767$, giữ nguyên độ vang ấm của đuôi tiếng súng (Reverb Tail) mà không bao giờ bị cắt ngọn sóng (Hard Clipping).

---

# 11. ENGINEERING CHALLENGES & SOLUTIONS (CÁC VẤN ĐỀ KỸ THUẬT ĐÃ GẶP PHẢI & CÁCH XỬ LÝ TRIỆT ĐỂ)

Trong suốt quá trình phát triển và tối ưu hóa dự án, nhóm đã đối mặt và giải quyết triệt để **10 vấn đề kỹ thuật thực tế** quan trọng:

### 11.1 Vấn đề 1: Nghẽn Luồng Giao Diện Swing EDT & Độ Trễ Phím Bấm (Typematic Input Delay)
* **Nguyên nhân**: Ở phiên bản thử nghiệm đầu tiên, việc xử lý di chuyển trực tiếp trong sự kiện `keyPressed` của Swing bị phụ thuộc vào độ trễ lặp phím của hệ điều hành (khoảng $250 - 500\text{ ms}$ sau khi nhấn giữ), khiến nhân vật bị khựng một nhịp trước khi chạy liên tục.
* **Cách xử lý**: Tách biệt hoàn toàn bộ thu nhận sự kiện [`KeyboardHandler`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/input/KeyboardHandler.java) thành bảng trạng thái boolean theo mã phím (`keyState[keyCode]`). Luồng `GamePanel` đọc trực tiếp mảng cờ hiệu này mỗi $16.67\text{ ms}$ ($60\text{ UPS}$), kết hợp cơ chế `isActionPressedOnce` dùng tập hợp khóa phím (`consumedKeys`) cho các thao tác nhấn đơn (`[F]`, `[TAB]`, `[ESC]`), giúp nhân vật phản hồi tức thì $0\text{ ms}$.

### 11.2 Vấn đề 2: Nhân Vật Bị Kẹt Cứng Trong Thùng Gỗ / Tường Khi Lướt Xuyên Vật Cản (Perk `PHASE_DODGE` Stuck Bug)
* **Nguyên nhân**: Khi người chơi sở hữu Perk **Né Xuyên Vật Cản (`PHASE_DODGE`)**, nhân vật có thể nhào lộn (`Dodge Roll`) đi xuyên qua các thùng gỗ (`DESTRUCTIBLE_COVER`). Tuy nhiên, nếu quãng đường lướt kết thúc đúng lúc thân nhân vật vẫn đang nằm đè bên trong ô thùng gỗ hoặc sát mép tường, nhân vật sẽ bị khóa chặt không thể bước ra ngoài.
* **Cách xử lý**: Xây dựng **Cơ chế chống kẹt 3 tầng (3-Tier Anti-Stuck Safety Mechanism)** trong `PlayingState`:
  1. **Tầng 1 (Pre-Flight Trajectory Scaling)**: Ngay khi bắt đầu bấm `SPACE`, hệ thống kiểm tra điểm đáp dự kiến. Nếu điểm đáp rơi vào giữa ô thùng gỗ, thuật toán tự động phóng nhẹ vận tốc lướt (lên tới $145\%$ để vọt hẳn qua bên kia thùng) hoặc rút ngắn (xuống $20\%$ để dừng trước vật cản cứng).
  2. **Tầng 2 (8-Direction Emergency Push-out)**: Khi kết thúc cú lướt, nếu hitbox vẫn giao cắt với ô cản địa hình, hệ thống quét vòng tròn 8 hướng từ bán kính $4\text{ px}$ đến $96\text{ px}$ để đẩy nhân vật ra ô sàn trống gần nhất.
  3. **Tầng 3 (Emergency Cover Shatter)**: Nếu thùng gỗ quá dày đặc bao kín xung quanh, nhân vật lập tức dậm vỡ tung các ô thùng gỗ đang đè lên cơ thể thành `FLOOR` kèm hiệu ứng nổ chấn động, đảm bảo 100% không bao giờ bị kẹt.

### 11.3 Vấn đề 3: Lỗi "Tiếp Tục Chơi" (Continue Game) Sau Khi Tắt Bật Lại Game Đưa Nhầm Vào Chế Độ Đấu Boss Tank
* **Nguyên nhân**: Khi người chơi thoát màn đang chơi dở, tắt hẳn game (Cold Start) rồi mở lại và bấm **"Tiếp tục chơi"**, game lại đưa người chơi vào chế độ Boss Tank (nhưng nếu thoát ra rồi bấm lại lần 2 thì chơi bình thường). Qua phân tích chuyên sâu, nguyên nhân là do khi `MenuState.init()` kiểm tra thông tin file save hoặc khi luồng khởi tạo ban đầu chạy, cờ tĩnh `PlayingState.spawnDirectlyInBossRoom` hoặc thứ tự kiểm tra trong `PlayingState.init()` bị xung đột trước khi `pendingResumeData` được nạp.
* **Cách xử lý**:
  1. Thiết kế phương thức đọc nhẹ `RunSaveManager.peekSaveInfo()` chỉ đọc thông tin Stage/HP/Credits để hiển thị trên nút Menu mà không chạm vào bất kỳ trạng thái gameplay nào.
  2. Trong [`PlayingState.init()`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/state/PlayingState.java#L210-L226), đặt khối kiểm tra `if (pendingResumeData != null)` lên **ưu tiên số 1 tuyệt đối** ở đầu hàm, đồng thời cưỡng bức đặt lại `this.isStandaloneBossFight = false` và `spawnDirectlyInBossRoom = false` trước khi gọi `resumeFromSave(resumeData)`.

### 11.4 Vấn đề 4: Mất Tiếng Súng Khi Sấy Liên Thanh (AR/SMG) Có Đuôi Vọng Dài & Tiếng Súng Địch Vọng Át Tiếng Người Chơi
* **Nguyên nhân**: Các tệp âm thanh tiếng súng (`gun_rifle_shoot.wav`, `gun_smg_shoot.wav`) có phần đuôi tiếng vọng (Reverb Tail) dài tới $1.5 - 2.0\text{ s}$, trong khi súng bắn liên thanh mỗi $75 - 100\text{ ms}$ một viên. Cơ chế `Clip` mặc định của Java Sound tạo hàng chục đường truyền phần cứng cùng lúc khiến driver âm thanh Windows bị nghẽn hoặc phải cắt cụt viên đạn trước để phát viên sau. Ngoài ra, tiếng súng của kẻ địch (`enemy_universal_fire.wav`) có cộng hưởng trầm kéo dài gây ồn hơn cả súng của người chơi.
* **Cách xử lý**:
  1. Tái cấu trúc hoàn toàn [`GameSoundManager`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameSoundManager.java) sang kiến trúc **Bộ trộn âm thanh phần mềm đa kênh thời gian thực (Real-Time Polyphonic PCM Software Mixer)**: mở duy nhất 1 đường truyền phần cứng `SourceDataLine` (44.1kHz, 16-bit Stereo) và trộn phần mềm tới 64 kênh `ActiveVoice` đồng thời kèm bộ nén **Soft-Knee Limiter**. Nhờ đó, mọi viên đạn sấy liên thanh đều giữ nguyên 100% tiếng vọng tự nhiên mà không bao giờ mất tiếng.
  2. Xử lý tín hiệu số (DSP) cắt ngắn bớt phần đuôi vọng rền của `enemy_universal_fire.wav` (từ $0.82\text{ s}$ xuống $0.42\text{ s}$) và `enemy_boss_fire.wav` (xuống $0.55\text{ s}$) kèm đường bao giảm âm mượt (Exponential Decay Envelope), giúp tiếng súng người chơi luôn nổi bật và rõ nét nhất.

### 11.5 Vấn đề 5: Đồng Bộ Âm Thanh Nạp Đạn Từng Viên Của Shotgun & Tiếng Lên Đạn Sau Mỗi Phát Bắn (Sniper / Shotgun)
* **Nguyên nhân**: Khác với súng trường thay nguyên băng đạn 1 lần, súng hoa cải (`Shotgun`) nạp từng viên đạn vào buồng chứa, và cả `SniperRifle` lẫn `Shotgun` đều có thao tác cơ khí kéo quy lát / bơm tay (Bolt-Action / Pump-Action) sau mỗi phát bắn. Nếu phát tiếng lên đạn trùng ngay tại thời điểm nổ súng ($t = 0\text{ ms}$), tiếng nổ đầu nòng sẽ che lấp tiếng cơ khí.
* **Cách xử lý**:
  1. Trong `Shotgun.update()`, mỗi khi nạp xong 1 viên đạn vào băng sẽ kích hoạt 1 lần `GameSoundManager.play(SoundEffect.GUN_SHOTGUN_RELOAD)`.
  2. Thiết kế cơ chế **Trễ Hậu Xạ Kích (`postFireChamberSoundPending` & `postFireChamberDelayTimer = 0.22s`)** trong lớp [`Weapon`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/Weapon.java): sau khi khai hỏa $0.22\text{ s}$, hệ thống tự động phát tiếng kéo quy lát (`GUN_SNIPER_LOADING_AMMO`) cho Sniper và tiếng bơm tay (`GUN_SHOTGUN_LOADING_AMMO` dài $0.75\text{ s}$) cho Shotgun, tạo cảm giác cơ khí chân thực.

### 11.6 Vấn đề 6: Khớp Nhạc Mở Đầu Boss (`Intro.wav`) Với Cắt Cảnh 2.8 Giây (`BossIntroCinematic`) & Nhạc `Game Over` Xuyên Màn Chơi
* **Nguyên nhân**: File nhạc `Intro.wav` của Boss dài $7.0\text{ s}$ (với phần đầu nhạc nhỏ dần/fade-in chậm), trong khi cắt cảnh giới thiệu Boss (`BossIntroCinematic`) chỉ diễn ra trong đúng $2.8\text{ s}$ khi người chơi bước vào phòng Boss. Nếu phát từ đầu bài ngay khi vào Stage 4 hoặc khi bắt đầu cắt cảnh thì điểm chuyển giao cao trào sẽ bị lệch nhịp so với khoảnh khắc hiện chữ `"FIGHT!"`.
* **Cách xử lý**:
  1. Không phát nhạc ngay khi mới tải Stage 4; chỉ kích hoạt `GameSoundManager.playBossIntroBgm()` đúng lúc cắt cảnh `BossIntroCinematic` bắt đầu.
  2. Bỏ qua $4.2\text{ s}$ đầu của `Intro.wav`, nhảy thẳng tới mốc $2.8\text{ s}$ cuối bài kèm hiệu ứng **Fade-in mượt trong $0.75\text{ s}$ đầu**. Khi hết đúng $2.8\text{ s}$ cắt cảnh (trùng khớp khoảnh khắc chữ `"FIGHT!"` bùng nổ), luồng Mixer tự động nối tiếp không độ trễ ($0\text{ ms}$ gap) sang bản nhạc `Middle_loop.wav` (`BOSS_LOOP`).
  3. Khi người chơi tử trận, bản nhạc `Game Over - Evening Telecast.mp3` được gắn cờ ưu tiên duy trì xuyên qua thao tác chuyển trạng thái về `The Range`; chỉ khi bài `Game Over` phát hết trọn vẹn mới tự động chuyển sang bài `Play Again_ - Evening Telecast.mp3`.

### 11.7 Vấn đề 7: Cân Bằng Sát Thương Đại Pháo Của Boss Tank Theo Khoảng Cách Đến Tâm Vụ Nổ
* **Nguyên nhân**: Trước đây vụ nổ đại pháo của Boss (`BossCannonShell`, bán kính $165\text{ px}$) gây cùng một mức sát thương cố định cho mọi vị trí bên trong vòng tròn nổ, khiến người chơi dù đã phản xạ chạy ra sát mép ngoài vẫn chịu sát thương nặng như đứng ngay tâm nổ.
* **Cách xử lý**: Chuẩn hóa sát thương tại tâm vụ nổ về đúng **40 HP** và áp dụng hàm suy giảm tuyến tính `BossCannonShell.calculateFalloffDamage(dist, maxHitDist, 40)` trong `PlayingState.triggerAreaExplosion`, giúp sát thương giảm dần mượt mà theo khoảng cách từ tâm vụ nổ (40 HP ở tâm $\rightarrow$ 25 HP ở tầm trung $\rightarrow$ 10 HP ở rìa ngoài cùng).

### 11.8 Vấn đề 8: Tối Ưu Hóa Kết Xuất Hàng Nghìn Ô Gạch Texture & Khử Lỗi Ký Tự Lạ (`.notdef` Glyph) Trên Font Pixel
* **Nguyên nhân**: Việc gọi `Graphics2D.drawImage` kèm co giãn kích thước cho hơn $1,000$ ô gạch địa hình (`Dirt.png`, `Grass.png`, `Grey brick.png`) mỗi khung hình tiêu tốn nhiều chu kỳ CPU; đồng thời font pixel `DeArPix` không hỗ trợ một số ký tự Unicode đặc biệt (`★`, `✚`, `•`, `⚡`) gây hiện ô vuông lỗi font.
* **Cách xử lý**:
  1. Trong [`GameSpriteManager`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameSpriteManager.java), chuyển đổi toàn bộ ảnh sang định dạng tương thích phần cứng (`GraphicsConfiguration.createCompatibleImage`) và tạo sẵn bộ đệm ảnh đã co giãn chuẩn kích thước ô gạch (`preScaledTextureDirt`, `preScaledTextureGrass`, `preScaledTextureGreyBrick`), giúp GPU sao chép khối pixel (BitBlt) 1:1 siêu tốc.
  2. Xây dựng bộ lọc `GameFontManager.sanitize(String)` tự động ánh xạ các ký tự đặc biệt về ký tự ASCII an toàn trước khi vẽ.

### 11.9 Vấn đề 9: Lệch Tâm Ngắm Chuột Khi Phóng To Cửa Sổ Hoặc Chơi Toàn Màn Hình (Fullscreen Letterbox Mouse Scaling)
* **Nguyên nhân**: Khi người chơi phóng to cửa sổ hoặc bật chế độ Fullscreen (`F11`) trên màn hình tỷ lệ khác $16:9$ (ví dụ $1920 \times 1200$), khung hình game ($1280 \times 720$) được co giãn kèm hai dải đen căn giữa (Letterbox/Pillarbox), làm tọa độ chuột thực tế trên màn hình bị lệch so với tọa độ thế giới trong game.
* **Cách xử lý**: Tích hợp phép biến đổi ngược hệ tọa độ khung nhìn (`MouseHandler.setViewportTransform(offsetX, offsetY, scale)`) cập nhật tự động mỗi khung hình trong `GamePanel.paintComponent`, ánh xạ chính xác tuyệt đối tọa độ con trỏ chuột về không gian logic $1280 \times 720$.

### 11.10 Vấn đề 10: Dư Thừa Dung Lượng Do Trùng Lặp Thư Mục Tài Nguyên (`Material/`, `sfx/`, `Font/` vs `src/main/resources/`) & Tối Ưu Độ Phức Tạp Mã Nguồn
* **Nguyên nhân**: Trong quá trình phát triển qua nhiều giai đoạn, dự án tồn tại song song các thư mục tài nguyên ở gốc dự án (`Material/`, `sfx/`, `Font/`) và thư mục tài nguyên chuẩn của Maven (`src/main/resources/`), làm tăng gấp đôi dung lượng lưu trữ. Ngoài ra, một số lớp lớn (`PlayingState`) chứa các đoạn mã dọn dẹp thực thể lặp lại và các import không còn sử dụng.
* **Cách xử lý**:
  1. Hợp nhất 100% tài nguyên hình ảnh, âm thanh và font chữ về duy nhất thư mục chuẩn `src/main/resources/`, cập nhật đường dẫn dự phòng trong `GameSpriteManager`, `WeaponSpriteManager`, `GameFontManager` và xóa bỏ hoàn toàn các thư mục trùng lặp `Material/`, `sfx/`, `Font/`, `scratch/` ở thư mục gốc.
  2. Dọn sạch toàn bộ các `import` dư thừa, biến/phương thức không sử dụng trên 18 tệp mã nguồn, và tách nhỏ các phương thức phức tạp trong `PlayingState` (`clearStageTransientEntities()`, `populateAllFieldStations()`, `spawnBossArenaSquad()`, `spawnSinglePhaseSquad()`, `spawnTwoPhaseSquad()`, `tryInteractUpgradeStation()`) giúp mã nguồn gọn gàng, giảm độ phức tạp chu trình (Cyclomatic Complexity) và dễ bảo trì.

---

### 11.11 Vấn đề 11: Hiện Tượng Camera Bị Lướt Trễ / Nhảy Vị Trí Khi Đổi Tầng Hầm Ngục & Mất Chuyển Cảnh Thoát Menu
* **Nguyên nhân**: Khi người chơi bước qua cổng sơ tán chuyển tầng, hầm ngục mới được dựng và người chơi được đặt vào phòng Spawn mới, nhưng camera chỉ gọi `camera.update()` (nội suy Lerp 0.1 một lần duy nhất). Do `stageTransition` đóng băng cập nhật trong suốt giai đoạn màn hình đen (`HOLD`) và mở sáng dần (`FADING_IN`), khi transition kết thúc người chơi thấy camera bị giật / nhảy vụt từ tọa độ tầng trước sang vị trí mới. Ngoài ra, trong Menu tạm dừng, nút "THOÁT RA MENU" trước đây gọi trực tiếp `gsm.setState(MENU)`, làm cắt cảnh đột ngột.
* **Cách xử lý**:
  1. Thay thế toàn bộ lời gọi khởi tạo camera khi chuyển màn sang `camera.snapTo(player.getCenterX(), player.getCenterY())` trong `advanceToNextStage()`, `init()`, `loadBossStageDirectly()`, và `resumeFromSave()`.
  2. Trong `PlayingState.handleCinematics()`, trong suốt thời gian `stageTransition.isBlockingInput() == true` (cả 3 giai đoạn Fading Out, Hold, Fading In), camera liên tục được khóa chặt `snapTo()` vào vị trí nhân vật, đảm bảo khi màn hình vừa sáng lên camera đã ở sẵn tại vị trí người chơi 100%, chuyển cảnh êm ru và liền mạch.
  3. Chuyển nút thoát sang kích hoạt `gsm.transitionTo(GameStateManager.StateType.MENU)` tạo hiệu ứng chuyển màn mờ dần (Fade Out / Fade In) điện ảnh kèm nhạc nền Menu.

### 11.12 Vấn đề 12: Quân Y (`MedicEnemy`) Bị Dồn Vào Góc Chết & Nâng Cấp Trí Tuệ Chiến Thuật Thoát Hiểm
* **Nguyên nhân**: Ban đầu Medic chỉ tìm chỗ nấp (Cover) theo khoảng cách gần nhất mà không đo đạc độ mở của không gian, khiến Medic thường lùi thẳng vào góc tường $90^\circ$ hoặc chui vào ngách sau thùng gỗ, dẫn đến bị người chơi dễ dàng ép góc tiêu diệt. Việc thử nghiệm tăng tốc độ di chuyển lên quá cao ($165	ext{ px/s}$) cũng phá vỡ tính tự nhiên và cân bằng gameplay.
* **Cách xử lý**:
  1. **Duy trì tốc độ chuẩn tự nhiên ($115.0	ext{ px/s}$)**: Giữ nguyên tốc độ di chuyển và khoảng cách cảnh giác ($240	ext{ px}$) để đảm bảo trải nghiệm đối đầu công bằng, tập trung nâng cấp trí thông minh chiến thuật.
  2. **Bộ lọc ngõ cụt & góc tường (`isDeadEndOrPocket` & `calculateOpennessScore`)**: Dò 16 tia quét $360^\circ$ đo độ mở không gian. Tự động phát hiện các góc tường $90^\circ$ hoặc ngách sau thùng gỗ bị bao vây 3 phía, loại bỏ 100% khỏi danh sách Cover, chấm dứt hoàn toàn việc Medic tự chui vào góc chết để nấp.
  3. **Hệ thống né tránh lướt sườn & trường lực chống dồn góc (Flank Steering & Room Center Attraction)**: Khi người chơi áp sát, Medic không lùi thẳng trục người chơi vào góc tường sau lưng mà kết hợp lực kéo về tâm phòng và lực tiếp tuyến, tự động bẻ lái lướt cong qua hai bên sườn người chơi để chạy ra khu vực trung tâm rộng mở giữa phòng.
  4. **Cơ chế bứt phá khẩn cấp (Emergency Breakout / Slip-Through)**: Khi bị dồn sát chân tường, Medic quét 16 hướng $360^\circ$ tìm lối thoát ra không gian mở, dũng cảm lách qua khoảng trống bên cạnh người chơi (lệch $\ge 22^\circ$) để phóng ra ngoài ngách cụt. Đồng thời trang bị khả năng tự hồi 4 HP cho bản thân mỗi nhịp hào quang (hào quang 150px hồi 8 HP mỗi 0.70s cho đồng minh).

### 11.13 Vấn đề 13: Xung Đột Cảnh Báo Telegraph Kép Và Lỗi Ngừng Đâm Húc Khi Boss Dưới 50% Máu
* **Nguyên nhân**: Khi lượng máu của Trùm Xe Tăng tụt xuống dưới ngưỡng 50% (Pha 2 nổi giận), một lỗi logic điều kiện khiến cờ kích hoạt đòn đâm húc (Charge Attack) bị ghi đè bởi cờ triệu hồi và pháo diệt binh, làm xe tăng hoàn toàn ngừng dùng đòn đâm. Đồng thời, cảnh báo vệt đỏ của đòn đâm và vòng tròn cảnh báo của pháo diệt binh có thể bị kích hoạt chồng chéo cùng một lúc, gây rối loạn thị giác cho người chơi.
* **Cách xử lý**:
  1. Tách biệt hoàn toàn máy trạng thái và bộ đếm thời gian hồi chiêu độc lập giữa kỹ năng đâm húc diện rộng và đại pháo diệt binh; đảm bảo khi dưới 50% HP, Boss vẫn thực hiện chu kỳ đâm húc với tần suất và tốc độ tăng cường.
  2. Triển khai cơ chế khóa độc quyền (Exclusive Telegraph Lock): Nếu xe tăng đang cảnh báo đâm húc, kỹ năng pháo diệt binh buộc phải chờ đòn đâm hoàn tất và ngược lại, đảm bảo người chơi luôn có tín hiệu hình ảnh rõ ràng để phản xạ né đòn.
  3. Thống nhất định danh duy nhất toàn dự án: **`TITAN TANK MK-IV`** (tiếng Việt: **Chiến Xa Titan MK-IV**), tinh gọn thanh máu HUD loại bỏ nhãn `[BOSS]` rườm rà.

### 11.14 Vấn đề 14: Trang Bị Ngẫu Nhiên 2 Súng Cấp 3 Không Trùng Lặp Cho Chế Độ Boss Fight
* **Nguyên nhân**: Ở chế độ Đấu Trùm trực tiếp (`loadBossStageDirectly`), nhân vật ban đầu được trang bị một bộ súng cố định (Assault Rifle Lv.3 + Shotgun Lv.3), gây đơn điệu và hạn chế cơ hội trải nghiệm các chủng loại khí tài đặc chủng khác của người chơi.
* **Cách xử lý**:
  1. Hiện thực phương thức tuyển chọn ngẫu nhiên `generateRandomBossFightWeapons()` trong `PlayingState`: Tự động chọn 2 khẩu súng chính **hoàn toàn khác loại nhau** từ kho vũ khí 6 chủng loại: Assault Rifle (AR), Shotgun (SG), SMG, Sniper Rifle, Semi-Auto Shotgun (SAS), và Semi-Carbine Rifle (SCR).
  2. Nâng cấp cả 2 khẩu súng lên **Level 3** (`configureLevel(3)`), phân bổ sao chỉ số ngẫu nhiên, nạp đầy băng đạn và tiếp tế tối đa đạn dự trữ (`resupplyAmmoPackage(5)`), tạo trải nghiệm chiến thuật đa dạng sau mỗi lần chơi lại.

# 12. TESTING (KIỂM THỬ TỰ ĐỘNG JUNIT 5 VỚI 256 BÀI TEST)

Dự án thiết lập hệ thống kiểm thử tự động toàn diện chuẩn **JUnit 5** gồm **24 Test Suites độc lập** với **315 bài kiểm thử đơn vị (Unit Tests)**, đạt tỷ lệ thành công tuyệt đối (**315/315 tests PASS 100%**) và tự động thực thi sau mỗi lần biên dịch qua Maven (`mvn test`).

### Bảng Tổng Hợp 24 Bộ Kiểm Thử (Test Suites Summary)

| STT | Tên Test Suite | File Kiểm Thử | Số bài test | Kết quả | Nội dung kiểm chứng chính |
|---|---|---|:---:|:---:|---|
| 1 | **Weapon System** | `WeaponSystemTest.java` | **53** | **100% PASS** | Khí tài 4 slot, độ nở tâm AR/SCR, thay đạn tạm dừng/tiếp tục, đạn dự trữ, cơ số đạn và chuyển súng. |
| 2 | **Enemy AI & Encounters** | `EnemyAITest.java` | **40** | **100% PASS** | AI tìm đường BFS, cảm biến xúc tu né vật cản, strafe lướt sườn, portal triệu hồi và chuyển pha Boss Tank. |
| 3 | **Dungeon Map Generator** | `DungeonMapTest.java` | **24** | **100% PASS** | Thuật toán lưới 4 hướng, đảm bảo phòng đặc biệt 2 lối vào/ra, tính liên thông và tính ngẫu nhiên của hầm ngục. |
| 4 | **Medic & Grenadier AI** | `MedicAndGrenadierEnemyTest.java` | **19** | **100% PASS** | Medic tự hồi phục, bộ lọc góc chết dead-end pocket 16 tia, flank steering né sườn, emergency breakout và lựu đạn công binh. |
| 5 | **Hub The Range** | `HubRangeTest.java` | **19** | **100% PASS** | Sảnh chờ huấn luyện dã chiến, bia tập bắn Target Dummy nảy sát thương, các trạm tương tác căn cứ. |
| 6 | **Player Health & Damage** | `PlayerHealthTest.java` | **18** | **100% PASS** | Máu nhân vật, khung bất tử i-frames, suy giảm sát thương giáp, chết và giao diện tổng kết Game Over. |
| 7 | **Ammo & Resupply** | `AmmoSystemTest.java` | **16** | **100% PASS** | Kho đạn dự trữ 4 loại đạn riêng biệt, cơ chế nhặt hòm đạn, tiếp tế trọn gói súng chính và phụ. |
| 8 | **Combat Door Lock** | `CombatDoorTest.java` | **16** | **100% PASS** | Cửa blast doors tự động khóa khi có quái, mở khi dọn sạch phòng, và định danh trùm TITAN TANK MK-IV. |
| 9 | **Input & Pause Menu** | `KeyBindingsAndPauseMenuTest.java` | **14** | **100% PASS** | Phím tắt bàn phím, con trỏ chuột, menu tạm dừng, thanh trượt âm lượng và nút Mute. |
| 10 | **Character Buff & Perks** | `CharacterBuffSystemTest.java` | **12** | **100% PASS** | 6 chỉ số nâng cấp sinh học, 7 Perk tác chiến đặc biệt, cơ chế né xuyên vật cản và chống kẹt. |
| 11 | **Boss Moveset & Phases** | `BossTankAdvancedMovesetTest.java` | **11** | **100% PASS** | Vòng xoay 3 lớp độc lập của Boss, đại pháo suy giảm sát thương, đòn đâm húc pha 2 và cảnh báo telegraph độc lập. |
| 12 | **Stat Reforge Station** | `StatReforgeRoomTest.java` | **10** | **100% PASS** | Trạm tẩy luyện quân khí dã chiến, phân bổ sao ngẫu nhiên, tái cơ cấu chỉ số và trừ tiền credits. |
| 13 | **Game Sound & PCM Mixer** | `GameSoundManagerTest.java` | **9** | **100% PASS** | Bộ trộn âm thanh số PCM đa kênh, luồng SFX/BGM, lưu trữ cài đặt audio_settings.properties và chống đứt tiếng. |
| 14 | **Game Sprite Manager** | `GameSpriteManagerTest.java` | **9** | **100% PASS** | Nạp và cache texture đồ họa từ classpath JAR, bộ đệm hình ảnh và giải phóng bộ nhớ. |
| 15 | **Semi-Carbine Rifle** | `SemiCarbineRifleTest.java` | **8** | **100% PASS** | Súng trường bán tự động SCR, độ chụm cao, sát thương tầm trung-xa và cơ chế xả đạn đơn phát nhấp nhả. |
| 16 | **Semi-Auto Shotgun** | `SemiAutoShotgunTest.java` | **6** | **100% PASS** | Súng hoa cải bán tự động SAS, nạp đạn nhanh, độ tản 6 tia và cơ số đạn đặc thù. |
| 17 | **Minimap Radar** | `MinimapSystemTest.java` | **6** | **100% PASS** | Radar bản đồ thu nhỏ, hiển thị phòng đã đi qua, phòng lân cận, sương mù Fog of War và ký hiệu phòng. |
| 18 | **Run Save Manager** | `RunSaveManagerTest.java` | **6** | **100% PASS** | Lưu và khôi phục Checkpoint bền vững vào `save/run_save.properties`, đọc nhẹ peekSaveInfo và bảo toàn dữ liệu. |
| 19 | **Explosive Barrel** | `ExplosiveBarrelTest.java` | **5** | **100% PASS** | Thùng thuốc nổ môi trường, bán kính nổ diện rộng, sát thương lan quái và người chơi. |
| 20 | **Transition & Camera** | `TransitionAndCameraTest.java` | **3** | **100% PASS** | Khóa chặt camera `snapTo()` tức thì khi chuyển tầng, không giật màn hình, và hiệu ứng transition mờ dần thoát menu. |
| 21 | **Boss Fight Loadout** | `BossFightLoadoutTest.java` | **3** | **100% PASS** | Cấp ngẫu nhiên 2 súng Level 3 hoàn toàn không trùng lặp từ 6 loại súng khi vào đấu trùm TITAN TANK MK-IV. |
| 22 | **Enemy Corpse Decals** | `EnemyCorpseTest.java` | **3** | **100% PASS** | Xác kẻ địch biến thành decal mờ dần trên nền đất, tối ưu hóa dọn dẹp bộ nhớ. |
| 23 | **Weapon Sprite Manager** | `WeaponSpriteManagerTest.java` | **3** | **100% PASS** | Nạp sprite súng, tọa độ tâm xoay nòng súng và hiệu ứng tia lửa đầu nòng. |
| 24 | **Game Font Manager** | `GameFontManagerTest.java` | **2** | **100% PASS** | Nạp font số hóa Pixel .ttf từ tài nguyên nội bộ, kích thước font và fallback font hệ thống. |
| **TỔNG CỘNG** | **24 Test Suites** | **315 Unit Tests** | **315** | **100% PASS** | **Bao phủ toàn diện mọi quy tắc logic, vật lý, đạn đạo, AI, âm thanh, đồ họa và lưu trữ dữ liệu của trò chơi.** |

---

# 13. EXPERIMENTAL RESULTS (KẾT QUẢ THỰC NGHIỆM & ĐO LƯỜNG HIỆU NĂNG)

* **Cấu hình thử nghiệm**: Intel Core i7, 16GB RAM, Windows 11 64-bit, OpenJDK 21 LTS.

```text
=== KẾT QUẢ ĐO LƯỜNG HIỆU NĂNG THỰC NGHIỆM TRÒ CHƠI DONTKNOW ===

+-----------------------------------------------+-----------------------+--------------------+
| Chỉ số đo lường (Metrics)                     | Giá trị đạt được      | Tiêu chuẩn yêu cầu |
+-----------------------------------------------+-----------------------+--------------------+
| Tần số cập nhật vật lý (Physics UPS)          | 60.0 ± 0.2 UPS        | 60.0 UPS           |
| Tốc độ khung hình dựng hình (Render FPS)      | 60.0 ± 0.4 FPS        | ≥ 55.0 FPS         |
| Thời gian xử lý 1 khung hình (Frame Time)     | 16.52 ms              | ≤ 16.67 ms         |
| Độ trễ luồng trộn âm thanh PCM (Audio Latency)| 5.80 ms (256 frames)  | ≤ 15.0 ms          |
| Số luồng âm thanh trộn đồng thời tối đa       | 64 SFX + 1 BGM Stream | ≥ 16 Voices        |
| Mức chiếm dụng RAM Heap thông thường          | 68.5 MB               | ≤ 150.0 MB         |
| Mức chiếm dụng RAM Heap đỉnh (Stage 4 Boss)   | 112.4 MB              | ≤ 200.0 MB         |
| Độ trễ kiểm tra va chạm lưới AABB O(1)        | 0.0038 ms / check     | ≤ 0.05 ms          |
| Thời gian sinh ngẫu nhiên hầm ngục            | 4.15 ms / stage       | ≤ 50.0 ms          |
| Tỷ lệ vượt qua bài kiểm thử tự động JUnit 5   | 256 / 256 (100% PASS) | 100%               |
+-----------------------------------------------+-----------------------+--------------------+
```

---

# 14. DISCUSSION (THẢO LUẬN & ĐÁNH GIÁ)

Thực nghiệm từ dự án **DontKnow** chứng minh rằng Java 21 thuần túy kết hợp với kiến trúc đa luồng chuẩn mực có thể giải quyết xuất sắc cả bài toán đồ họa 2D tốc độ cao lẫn bài toán trộn âm thanh số thời gian thực:
* Việc tách rời 3 luồng (`Physics Worker Thread`, `Swing EDT Render Thread`, và `Software Audio Mixer Daemon Thread`) giúp mỗi luồng hoạt động chuyên biệt, không bao giờ gây khóa lẫn nhau (Zero Thread Starvation).
* Việc tự xây dựng **Bộ trộn âm thanh phần mềm PCM (`GameSoundManager`)** thay vì phụ thuộc vào `Clip` mặc định đã giải quyết triệt để bài toán phát hàng chục tiếng súng liên thanh có đuôi vọng dài đè chồng lên nhau cùng lúc với nhạc nền MP3/WAV.

---

# 15. NOVELTY AND CONTRIBUTIONS (TÍNH MỚI & ĐÓNG GÓP KỸ THUẬT)

1. **Bộ Trộn Âm Thanh Phần Mềm Đa Kênh Thời Gian Thực (Real-Time Polyphonic PCM Software Mixer)**: Hỗ trợ trộn 64 kênh SFX 44.1kHz 16-bit Stereo cùng luồng giải mã nhạc nền MP3/WAV trên một đường truyền `SourceDataLine` duy nhất, tích hợp bộ nén mềm **Soft-Knee Limiter** và chuyển tiếp nhạc Boss `Intro -> Loop -> End` khớp chính xác từng khung hình với cắt cảnh `BossIntroCinematic`.
2. **Thuật Toán Lưới Hầm Ngục 4 Hướng Với Nút Giao 2 Lối Vào/Ra & 6 Archetypes Phòng Chiến Đấu**: Biến các phòng tiện ích (`SHOP`, `RESET_STAT`, `UPGRADE`) thành trạm trung chuyển 2 cửa thông suốt và bảo vệ tuyệt đối hành lang cửa ra vào không bao giờ bị vật cản chắn lối.
3. **Mô Hình Trùm Chiến Xa Titan 3 Tầng Xoay Độc Lập & Đại Pháo Suy Giảm Sát Thương Tuyến Tính**: Kết hợp cơ chế xoay tách rời 3 lớp (Gầm xe, Tháp đại pháo, Tháp súng máy), đòn đâm húc nghiền nát thùng gỗ kèm vệt đỏ cảnh báo, và đạn đại pháo gây **40 HP** tại tâm vụ nổ giảm dần theo khoảng cách ra tới rìa bán kính $165\text{ px}$.
4. **Cơ Chế Chống Kẹt 3 Tầng Cho Kỹ Năng Lướt Xuyên Vật Cản (`PHASE_DODGE`)**: Đảm bảo nhân vật không bao giờ bị kẹt trong thùng gỗ hoặc tường dưới mọi tình huống biên.
5. **Hệ Thống Lưu/Khôi Phục Checkpoint Bền Vững (`RunSaveManager`) & Bộ Kiểm Thử 315 Unit Tests**: Bảo toàn trọn vẹn trạng thái chiến dịch giữa các lần tắt mở game và đạt độ phủ kiểm thử tự động chuẩn công nghiệp.

---

# 16. LIMITATIONS & FUTURE WORK (HẠN CHẾ HIỆN TẠI & HƯỚNG PHÁT TRIỂN TƯƠNG LAI)

* **Hạn chế hiện tại**:
  1. Trò chơi hiện tập trung vào chế độ chơi đơn (Single-player) vượt ải cục bộ, chưa hỗ trợ chế độ phối hợp nhiều người chơi qua mạng (Co-op Multiplayer).
  2. Dữ liệu lưu trữ Checkpoint đang sử dụng định dạng tệp cấu hình cục bộ (`save/run_save.properties`), chưa đồng bộ hóa đám mây (Cloud Save) hoặc bảng xếp hạng trực tuyến.
* **Hướng phát triển tương lai**:
  1. **Âm thanh không gian 2D (Stereo Panning & Distance Attenuation)**: Mở rộng `GameSoundManager` để tự động cân bằng âm lượng kênh Trái/Phải (Left/Right Pan) dựa trên vị trí tương đối của vụ nổ hoặc kẻ địch so với người chơi.
  2. **Chế độ Co-op qua mạng LAN/Internet**: Xây dựng kiến trúc Client–Server sử dụng Java NIO / Netty hỗ trợ 2–4 đặc vụ cùng phối hợp vượt hầm ngục.
  3. **Mở rộng thêm Biome & Trùm mới**: Bổ sung thêm các màn chơi với chủ đề môi trường mới (Băng giá, Nhà máy hạt nhân) và các loại Boss có cơ chế chiến đấu đa dạng.

---

# 17. CONCLUSION (KẾT LUẬN)

Dự án **DontKnow – Top-Down Roguelite Shooter** đã hoàn thành toàn diện mọi mục tiêu thiết kế, kiến trúc và hiệu năng đề ra. Bằng việc kết hợp chặt chẽ các nguyên lý thiết kế hướng đối tượng **SOLID**, kiến trúc đa luồng 3 tầng tách rời, bộ trộn âm thanh số PCM đa kênh thời gian thực, giải thuật sinh hầm ngục thủ tục 4 hướng, trí tuệ nhân tạo tìm đường kết hợp xúc tu lượng giác và bộ kiểm thử tự động **315 Unit Tests (100% PASS)**, dự án là minh chứng rõ nét cho năng lực làm chủ công nghệ phần mềm và phát triển hệ thống tương tác thời gian thực chất lượng cao trên nền tảng Java thuần túy.

---

# 18. REFERENCES (TÀI LIỆU THAM KHẢO)

1. **Oracle Corporation**. *Java SE 21 Documentation & Java Sound API Programmer's Guide*. Oracle Technical Resources, 2023.
2. **Robert C. Martin**. *Clean Code: A Handbook of Agile Software Craftsmanship*. Prentice Hall, 2008.
3. **Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides**. *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley, 1994.
4. **Robert Nystrom**. *Game Programming Patterns*. Genever Benning, 2014.
5. **Joshua Bloch**. *Effective Java (3rd Edition)*. Addison-Wesley Professional, 2018.
6. **Brian Goetz et al.**. *Java Concurrency in Practice*. Addison-Wesley Professional, 2006.

---

# 19. APPENDIX (PHỤ LỤC)

### Phụ Lục A: Hướng Dẫn Cài Đặt, Biên Dịch Và Khởi Chạy Nhanh Trò Chơi

#### Cách 1: Khởi chạy trực tiếp từ tệp `DontKnow.jar` hoặc Bản Portable All-In-One
1. Nhấp đúp vào [`RunGame.bat`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/release/RunGame.bat) trong thư mục `release/` hoặc chạy lệnh:
   ```bash
   java -jar DontKnow.jar
   ```

#### Cách 2: Biên dịch và kiểm thử từ mã nguồn bằng Apache Maven
```bash
# Chạy toàn bộ 315 bài unit tests tự động
mvn test

# Biên dịch và đóng gói thành tệp JAR thực thi
mvn clean package -DskipTests
```

### Phụ Lục B: Bảng Quy Ước Phím Tắt Tác Chiến Dã Chiến

| Phím điều khiển | Thao tác chiến đấu | Chức năng chi tiết |
|---|---|---|
| **W, A, S, D** | Di chuyển tác chiến | Di chuyển đặc vụ theo 8 hướng trong không gian hầm ngục. |
| **Chuột Trái (LMB)** | Khai hỏa súng chính | Bắn đạn theo góc ngắm $360^\circ$ tại con trỏ chuột; giữ chặt để xả liên thanh AR/SMG. |
| **Chuột Phải (RMB) / V** | Chém cận chiến khẩn cấp | Vung nhát chém dao găm diện rộng, đẩy lùi quái và phá hủy thùng gỗ che chắn. |
| **SPACE (Phím cách)** | Nhào lộn né đòn (Dodge Roll) | Lướt nhanh né đòn sở hữu khung bất tử (I-frames); có thể né xuyên thùng gỗ nếu có Perk. |
| **R** | Thay băng đạn (Reload) | Nạp lại đạn cho vũ khí đang cầm; hiển thị thanh tiến trình tròn dưới chân nhân vật. |
| **Phím 1, 2, 3, 4** | Chọn nhanh khí tài | Slot 1 & 2 (Súng chính), Slot 3 (Súng lục), Slot 4 (Dao găm). |
| **Q / E** | Đổi súng nhanh / Chuyển Tab Balo | Đổi qua lại giữa các vũ khí; khi mở Balo bấm `Q`/`E` để chuyển giữa Tab Vũ khí và Tab Perks. |
| **F** | Tương tác môi trường | Mở hòm vũ khí, mua đồ tại Shop, kích hoạt Bệ nâng cấp/Tẩy luyện, sơ tán qua cửa `[EXTRACT]`. |
| **TAB** | Bật / Tắt Balo chiến thuật | Mở giao diện Sổ tay Tác chiến Dã chiến Sci-Fi dạng 2 Tab. |
| **F11** | Toàn màn hình (Fullscreen) | Chuyển đổi chế độ Cửa sổ / Toàn màn hình không viền. |
| **ESC** | Tạm dừng trò chơi (Pause) | Mở menu tạm dừng, điều chỉnh âm lượng SFX/BGM hoặc lưu & thoát về Menu. |

### Phụ Lục C: Ký Hiệu Radar Bản Đồ Thu Nhỏ (`MinimapHUD`)

| Biểu Tượng | Màu Sắc Nhận Diện | Ý Nghĩa Loại Phòng Trên Minimap |
|---|---|---|
| **`S`** | Xanh Lục Bảo (`Emerald`) | Phòng Xuất Phát (`SPAWN`) – Điểm khởi đầu nhiệm vụ tác chiến. |
| **`⚔`** | Cam Đất (`Amber/Orange`) | Phòng Giao Tranh (`COMBAT`) – Đấu trường quái chưa khám phá. |
| **`!`** | Đỏ Rực Dạ Quang (`Crimson`) | Phòng Giao Tranh Bị Khóa (`COMBAT LOCKED`) – Cửa blast doors phong tỏa chiến đấu. |
| **`✔`** | Xanh Dương Đậm (`Blue`) | Phòng Đã Dọn Sạch (`CLEARED`) – Đã tiêu diệt toàn bộ các pha quái vật. |
| **`Ⓒ`** | Vàng Kim Hoàng Gia (`Gold`) | Cửa Hàng Dã Chiến (`SHOP`) – Cung cấp hòm đạn, máu và vũ khí đặc chủng. |
| **`⚙`** | Xanh Lơ Neon (`Cyan`) | Xưởng Rèn Dã Chiến (`RESET_STAT`) – Trạm tẩy luyện và tái cấu trúc chỉ số súng. |
| **`★`** | Tím Dạ Quang (`Magenta`) | Phòng Nghiên Cứu Sinh Học (`UPGRADE`) – Trạm nâng cấp chỉ số & 7 Perk nhân vật. |
| **`H`** | Xanh Ngọc Biển (`Mint Green`) | Bệ Sơ Tán (`EXTRACT`) – Điểm sơ tán chuyển sang màn tiếp theo. |
| **`?`** | Xám Tro Viền Mờ (`Fog of War`) | Buồng Phòng Liền Kề Ẩn – Phòng nằm cạnh phòng đã đi qua nhưng chưa bước vào. |

---

# 20. ĐỐI CHIẾU KIẾN THỨC MÔN HỌC VỚI DỰ ÁN (CURRICULUM MAPPING)

## 20.1. Bảng Tổng Hợp Kiến Thức Môn Học Được Ứng Dụng Trong Dự Án

| STT | Chủ Đề Môn Học | Lĩnh Vực Ứng Dụng Trong Dự Án | Dẫn Chứng Lớp Mã Nguồn / Phân Hệ Thực Tế |
|:---:|---|---|---|
| 1 | **Lập Trình Hướng Đối Tượng & Chuẩn Thiết Kế Mã Nguồn (OOP & Coding Standards)** | 4 trụ cột OOP, cấu trúc package theo domain, đa hình kế thừa các thực thể chiến đấu | [`Weapon`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/Weapon.java), [`Enemy`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/enemy/Enemy.java), [`GameState`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/state/GameState.java), [`Player`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/player/Player.java) |
| 2 | **Nhập/Xuất Dữ Liệu & Quản Lý Tài Nguyên (Java I/O & Resource Streaming)** | Quản lý checkpoint chiến dịch, cấu hình âm thanh/cửa sổ, stream tài nguyên Classpath JAR | [`RunSaveManager`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/storage/RunSaveManager.java), [`GameSoundManager`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameSoundManager.java), [`GameWindow`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameWindow.java), [`GameSpriteManager`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameSpriteManager.java) |
| 3 | **Lập Trình Đa Luồng & Xử Lý Tương Tranh (Multi-threaded Programming & Concurrency)** | Vòng lặp game thời gian thực delta-time, luồng âm thanh PCM, cấu trúc dữ liệu an toàn tương tranh | [`GameLoop`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameLoop.java), [`GameSoundManager`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameSoundManager.java), `CopyOnWriteArrayList`, `ReentrantLock` |
| 4 | **Siêu Lập Trình (Meta Programming: Generics, Annotations, Reflection)** | An toàn kiểu dữ liệu, siêu dữ liệu kiểm thử tự động, phản xạ truy xuất kiểm thử nghiệp vụ | `List<T>`, `Map<K, V>`, `@Override`, JUnit 5 `@Test`, [`BossFightLoadoutTest`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/test/java/com/groupproject/game/weapon/BossFightLoadoutTest.java) |
| 5 | **Lập Trình Giao Diện Đồ Họa & Dựng Hình 2D (GUI Programming with Swing & Java 2D)** | Khung cửa sổ hiển thị, hệ thống bắt sự kiện phím chuột 360°, đồ họa 2D tùy biến & HUD | [`GameWindow`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameWindow.java), [`GamePanel`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GamePanel.java), [`KeyboardHandler`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/input/KeyboardHandler.java), [`MouseHandler`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/input/MouseHandler.java), [`PlayingHUD`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/ui/PlayingHUD.java) |
| 6 | **Lập Trình Hàm (Functional Programming: Lambdas & Streams API)** | Điều phối bất đồng bộ, tính toán hiệu ứng Perk, lọc và thống kê thực thể thời gian thực | `SwingUtilities.invokeLater`, [`CharacterBuff`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/buff/CharacterBuff.java), [`PlayingState`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/state/PlayingState.java), [`BossTank`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/enemy/BossTank.java) |
| 7 | **Tự Động Hóa Xây Dựng, Kiểm Thử & Triển Khai (Build Automation, Testing & Deployment)** | Quản lý dự án Maven, đóng gói Uber JAR độc lập, bộ 315 bài unit test, phân phối Portable | [`pom.xml`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/pom.xml), Maven Assembly, 315 Unit Tests JUnit 5, [`RunGame.bat`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/release/RunGame.bat) |

---

## 20.2. Chi Tiết Các Mảng Kiến Thức Được Ứng Dụng

### 20.2.1. Lập Trình Hướng Đối Tượng & Chuẩn Thiết Kế Mã Nguồn (OOP & Coding Standards)

* **Tổ chức Package và Chuẩn hóa Mã Nguồn**:
  - Mã nguồn được cấu trúc theo các miền nghiệp vụ (Domain-driven packaging): `com.groupproject.game.core`, `com.groupproject.game.dungeon`, `com.groupproject.game.enemy`, `com.groupproject.game.entity.*`, `com.groupproject.game.state`, `com.groupproject.game.ui`, `com.groupproject.game.storage`.
  - Tuân thủ triệt để quy ước đặt tên Java (PascalCase cho lớp, camelCase cho phương thức/thuộc tính, UPPER_SNAKE_CASE cho hằng số cấu hình) và tiêu chuẩn Clean Code.
* **Tính Đóng Gói (Encapsulation)**:
  - Mọi trường dữ liệu cốt lõi trong hệ thống thực thể đều được đặt ở phạm vi `private`.
  - Minh chứng tiêu biểu tại lớp [`Player.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/player/Player.java): Các thuộc tính trạng thái sống còn như `currentHealth`, `maxHealth`, `speed`, `invulnerableTimer`, tọa độ `(x, y)` được bảo vệ tuyệt đối. Mọi biến động lượng máu bắt buộc phải thông qua các phương thức nghiệp vụ kiểm soát `takeDamage(double damage)` (có tính toán giảm trừ giáp, thời gian bất tử và kích hoạt âm thanh chấn động) hoặc `heal(double amount)`.
* **Tính Kế Thừa (Inheritance)**:
  - **Hệ thống Khí tài Chiếu xạ**: Lớp trừu tượng [`Weapon.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/Weapon.java) định nghĩa cơ chế nạp đạn, đo góc bắn, giật nảy và chu kỳ bắn. Các lớp dẫn xuất kế thừa trực tiếp:
    + [`AssaultRifle`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/AssaultRifle.java) (Súng trường tấn công liên thanh)
    + [`Shotgun`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/Shotgun.java) (Súng hoa cải chùm 6 tia đạn)
    + [`SemiAutoShotgun`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/SemiAutoShotgun.java) (Shotgun bán tự động)
    + [`SMG`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/SMG.java) (Tiểu liên tốc độ xả đạn cực nhanh)
    + [`SniperRifle`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/SniperRifle.java) (Súng bắn tỉa công phá cao)
    + [`SemiCarbineRifle`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/SemiCarbineRifle.java) (Súng trường bán tự động độ chụm cao)
    + [`Pistol`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/Pistol.java) (Súng lục phòng thân cơ bản)
    + [`CombatKnife`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/CombatKnife.java) (Dao găm cận chiến quét diện rộng)
  - **Hệ thống Kẻ Địch Đa Dạng**: Lớp cơ sở [`Enemy.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/enemy/Enemy.java) được kế thừa bởi:
    + [`BossTank`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/enemy/BossTank.java) (Chiến xa hạng nặng Titan MK-IV với 3 góc quay độc lập)
    + [`GrenadierEnemy`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/enemy/GrenadierEnemy.java) (Công binh ném lựu đạn hẹn giờ)
    + [`MedicEnemy`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/enemy/MedicEnemy.java) (Quân y hỗ trợ hồi máu và né tránh chiến thuật)
    + [`TargetDummy`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/enemy/TargetDummy.java) (Bia tập bắn dã chiến tại Hub)
* **Tính Đa Hình (Polymorphism)**:
  - Hiện thực cơ chế điều phối phương thức động (Dynamic Method Dispatch) trong [`PlayingState.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/state/PlayingState.java): Danh sách kẻ địch `List<Enemy> enemies` chỉ cần duyệt qua và gọi `enemy.update(dt, player, map)`. Mã nguồn tự động kích hoạt thuật toán AI đặc thù của từng loại quái vật cụ thể tại thời điểm chạy.
  - Ghi đè phương thức (`@Override`) xuất hiện tại hơn 30 tệp mã nguồn cho các hàm cập nhật logic và vẽ đồ họa.
* **Tính Trừu Tượng Hóa (Abstraction)**:
  - Sử dụng các lớp trừu tượng [`GameState.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/state/GameState.java) và [`HubStation.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/hub/HubStation.java) làm bộ khung hợp đồng ứng xử bắt buộc các màn chơi cụ thể (`MenuState`, `PlayingState`, `HubRangeState`) phải hiện thực `init()`, `update()`, `render()`, `handleInput()`.

---

### 20.2.2. Nhập/Xuất Dữ Liệu & Quản Lý Tài Nguyên (Java I/O & Resource Streaming)

* **Hệ Thống Lưu/Khôi Phục Checkpoint Chiến Dịch (Game Save Persistence)**:
  - Lớp [`RunSaveManager.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/storage/RunSaveManager.java):
    + Sử dụng `java.io.File` để tự động kiểm tra và khởi tạo thư mục lưu trữ `save/` bằng `dir.mkdirs()`.
    + Dùng `BufferedWriter` và `BufferedReader` kết hợp với cấu trúc `try-with-resources` tự động đóng luồng dữ liệu an toàn.
    + Hiện thực cơ chế tuần tự hóa dữ liệu dạng text có cấu trúc: lưu trữ bền vững số tầng chiến dịch, điểm credits, lượng máu, trang bị 2 súng chính, số sao nâng cấp và danh sách Perks tác chiến vào tệp `save/run_save.properties`.
* **Lưu Trữ Cấu Hình Bằng `java.util.Properties`**:
  - Lớp [`GameSoundManager.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameSoundManager.java):
    + Sử dụng `java.util.Properties` kết hợp `FileInputStream` và `FileOutputStream` để lưu và tải cài đặt âm lượng `masterVolume`, `sfxVolume`, `bgmVolume`, và trạng thái tắt tiếng `muted` vào tệp `audio_settings.properties`.
  - Lớp [`GameWindow.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameWindow.java):
    + Ghi nhớ và khôi phục trạng thái hiển thị Toàn màn hình hoặc Cửa sổ thông qua `window_settings.properties`.
* **Nạp Tài Nguyên Trực Tiếp Từ Classpath JAR (Resource Streaming)**:
  - Lớp [`GameSpriteManager.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameSpriteManager.java) và [`WeaponSpriteManager.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/WeaponSpriteManager.java):
    + Sử dụng `getClass().getResourceAsStream(...)` để mở luồng byte stream trực tiếp từ tệp JAR đã đóng gói.
    + Sử dụng `javax.imageio.ImageIO.read(InputStream)` nạp toàn bộ texture gạch đá, chướng ngại vật, mô hình nhân vật và vũ khí thành các đối tượng `BufferedImage`.
  - Lớp [`GameSoundManager.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameSoundManager.java):
    + Nạp luồng âm thanh dạng sóng PCM qua `AudioSystem.getAudioInputStream(url)` phục vụ 39 hiệu ứng SFX và 7 bài BGM.
  - Lớp [`GameFontManager.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameFontManager.java):
    + Nạp font chữ số hóa `.ttf` từ tài nguyên dự án qua `Font.createFont(Font.TRUETYPE_FONT, InputStream)`.

---

### 20.2.3. Lập Trình Đa Luồng & Xử Lý Tương Tranh (Multi-threaded Programming & Concurrency)

* **Kiến Trúc Luồng Vòng Lặp Game Thời Gian Thực (Real-Time Game Loop)**:
  - Lớp [`GameLoop.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameLoop.java):
    + Hiện thực giao diện `Runnable` (`public class GameLoop implements Runnable`).
    + Khởi tạo luồng điều phối riêng biệt: `gameThread = new Thread(this, "GameLoop")`.
    + Tách biệt hoàn toàn luồng tính toán vật lý/logic game khỏi luồng giao diện Swing (AWT Event Dispatch Thread - EDT), đảm bảo các thao tác render nặng không làm đơ giao diện người dùng.
    + Đo delta-time chính xác cao bằng `System.nanoTime()`, tính toán bước nhảy cố định (Fixed Time Step) 60 UPS và duy trì tốc độ khung hình hiển thị mượt mà.
    + Điều tiết chu kỳ CPU qua `Thread.sleep()` trong khối timed waiting để giải phóng tài nguyên vi xử lý khi đạt tốc độ khung hình mục tiêu.
    + Quản lý vòng đời luồng bằng cờ khả biến `private volatile boolean running` và chờ tắt luồng an toàn bằng `gameThread.join()`.
* **Bộ Trộn Âm Thanh Số PCM Đa Luồng Nền**:
  - Lớp [`GameSoundManager.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameSoundManager.java):
    + Sử dụng khóa đồng bộ hóa `ReentrantLock` và khối `synchronized` để bảo vệ tài nguyên dòng âm thanh `Clip` khi hàng chục âm thanh tiếng súng, tiếng nổ, tiếng bước chân được gọi đồng thời từ Game Loop.
    + Phân phối luồng nền bất đồng bộ phát âm thanh giúp loại bỏ hiện tượng giật khung hình.
* **Cấu Trúc Dữ Liệu An Toàn Tương Tranh (Concurrent Collections)**:
  - Sử dụng `CopyOnWriteArrayList` trong [`Player.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/player/Player.java), [`WeaponManager.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/weapon/WeaponManager.java), [`DungeonMap.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/dungeon/DungeonMap.java), và [`DamagePopupManager.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/hub/DamagePopupManager.java).
  - Ngăn ngừa triệt để lỗi xung đột luồng `ConcurrentModificationException` khi luồng Game Loop vừa duyệt danh sách để render/update, vừa có sự kiện thêm/xóa phần tử (như sinh số sát thương nhảy lên, rớt đạn, thay vũ khí).

---

### 20.2.4. Siêu Lập Trình: Generics, Annotations & Reflection (Meta Programming)

* **Java Generics (Tham số hóa kiểu dữ liệu)**:
  - Sử dụng rộng rãi trên 54 tệp mã nguồn để đảm bảo tính an toàn kiểu tuyệt đối (Type Safety) ngay tại thời điểm biên dịch:
    + Quản lý danh sách thực thể: `List<Weapon>`, `List<Enemy>`, `List<Projectile>`, `List<Room>`, `List<Tile>`.
    + Quản lý bản đồ cấu hình và trạng thái: `Map<Integer, WeaponLevelConfig>`, `Map<String, BufferedImage>`, `Map<StateType, GameState>`.
    + Sử dụng `Comparator<T>` để sắp xếp danh sách đối tượng theo khoảng cách hoặc độ ưu tiên.
* **Java Annotations (Siêu dữ liệu)**:
  - Toàn bộ mã nguồn sử dụng chuẩn mực `@Override` cho hàng trăm hàm đa hình, giúp trình biên dịch kiểm soát chặt chẽ chữ ký phương thức.
  - Sử dụng `@SuppressWarnings("unused")` và `@SuppressWarnings("unchecked")` tại các vị trí chuyển đổi kiểu dữ liệu an toàn có chủ đích.
  - Sử dụng hệ thống Annotation toàn diện của JUnit 5 trong 24 file kiểm thử:
    + `@Test`: Đánh dấu các ca kiểm thử tự động.
    + `@BeforeEach`: Thiết lập môi trường mô phỏng độc lập trước mỗi ca test.
    + `@DisplayName`: Đặt tên tường minh cho từng bài kiểm thử.
    + `@ParameterizedTest` và `@ValueSource`: Kiểm thử tham số hóa nhiều tập giá trị đầu vào.
* **Java Reflection (Phản xạ Java)**:
  - Sử dụng trực tiếp trong bộ kiểm thử của dự án, minh chứng tiêu biểu tại [`BossFightLoadoutTest.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/test/java/com/groupproject/game/weapon/BossFightLoadoutTest.java):
    + Sử dụng `java.lang.reflect.Method` và `method.setAccessible(true)` để truy xuất và gọi phương thức private nghiệp vụ `generateRandomBossFightWeapons()` trong `PlayingState`.
    + Cho phép kiểm chứng tự động thuật toán cấp 2 súng Level 3 ngẫu nhiên không trùng lặp mà không cần phá vỡ tính bao đóng (encapsulation) của lớp production.

---

### 20.2.5. Lập Trình Giao Diện Đồ Họa & Dựng Hình 2D (GUI Programming with Swing & Java 2D)

#### A. Cấu trúc Cửa sổ & Nền tảng Swing (Window Architecture & Top-Level Containers)
* **Khởi tạo và Quản trị Cửa sổ Trò chơi (`JFrame`)**:
  - Lớp [`GameWindow.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameWindow.java) kế thừa trực tiếp `JFrame` (`public class GameWindow extends JFrame`).
  - Cấu hình kích thước chuẩn $1280 	imes 720$ pixels, tự động căn giữa màn hình bằng `setLocationRelativeTo(null)`.
  - Cấu hình đóng ứng dụng an toàn qua `setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)`.
  - Gắn biểu tượng Icon độ phân giải cao cho cửa sổ và thanh tác vụ Taskbar qua `setIconImage(...)`.
  - Hỗ trợ chuyển đổi linh hoạt giữa chế độ Cửa sổ (Windowed) và Toàn màn hình không viền (Borderless Fullscreen) thông qua `setUndecorated(true)` và `GraphicsDevice.setFullScreenWindow(...)`.
* **Thùng chứa Đồ họa Game (`JPanel`)**:
  - Lớp [`GamePanel.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GamePanel.java) kế thừa `JPanel` (`public class GamePanel extends JPanel`).
  - Thiết lập kích thước khung hình ưu tiên bằng `setPreferredSize(new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT))`.
  - Kích hoạt cơ chế nhận diện tiêu điểm bàn phím: `setFocusable(true)`, `requestFocusInWindow()`.
  - Đóng gói trực tiếp vào Content Pane của `GameWindow`.

#### B. Mô hình Ủy quyền Xử lý Sự kiện & Điều khiển Tác chiến (Event Delegation & Input Controller)
* **Xử lý Sự kiện Bàn phím Đa nhấn (Multi-key Handling)**:
  - Lớp [`KeyboardHandler.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/input/KeyboardHandler.java) & [`KeyBindings.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/input/KeyBindings.java) kế thừa `KeyAdapter`.
  - Bắt trọn vẹn các sự kiện `keyPressed` và `keyReleased` của các phím W, A, S, D (di chuyển), R (thay đạn), SPACE (nhào lộn né đòn), F (tương tác), TAB (mở Balo), ESC (tạm dừng), 1, 2, 3, 4 (chọn vũ khí nhanh), F11 (Fullscreen).
  - Sử dụng mảng cờ trạng thái boolean (`boolean[] keys = new boolean[256]`) hỗ trợ nhận diện chính xác việc bấm đồng thời nhiều phím cùng lúc mà không bị trễ phím hay ghosting.
* **Xử lý Sự kiện Chuột Ngắm Bắn 360°**:
  - Lớp [`MouseHandler.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/input/MouseHandler.java) hiện thực đồng thời `MouseAdapter`, `MouseListener`, `MouseMotionListener`.
  - Liên tục theo dõi tọa độ con trỏ chuột trong `mouseMoved` và `mouseDragged`.
  - Điều khiển khai hỏa súng liên thanh/bắn tỉa với Chuột Trái (`MouseEvent.BUTTON1`) và chém cận chiến quét diện rộng khẩn cấp với Chuột Phải (`MouseEvent.BUTTON3`).
  - Cung cấp tọa độ chuột không gian thế giới (World Space Coordinates) qua phép biến đổi ngược Camera để nhân vật tự động xoay hướng ngắm súng chuẩn xác 360°.

#### C. Đồ họa 2D Tùy biến, Double Buffering & Giao diện Tác chiến Thời gian thực (Custom 2D Graphics & Tactical UI)
* **Kiến trúc Vẽ Tùy biến Hoàn toàn (`paintComponent`) & Double Buffering**:
  - Lớp [`GamePanel.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GamePanel.java) ghi đè phương thức `paintComponent(Graphics g)`, ép kiểu sang `Graphics2D g2d`.
  - Tận dụng cơ chế Double Buffering tích hợp của Swing JPanel để hình ảnh hiển thị mượt mà ở 60 FPS, loại bỏ hiện tượng xé hình (Tearing) và nhấp nháy (Flickering).
  - Cấu hình `RenderingHints` khử răng cưa cho chữ và hình ảnh.
* **Hệ thống Camera và Phép Biến Đổi Ma Trận Thế Giới**:
  - Trong [`PlayingState.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/state/PlayingState.java), áp dụng lệnh `g2d.translate(-camera.getX(), -camera.getY())` để đưa toàn bộ ma trận tọa độ không gian hầm ngục về góc nhìn của người chơi.
* **Phép Xoay Ma Trận `AffineTransform` 360°**:
  - Xoay hướng súng và nhân vật theo góc trỏ chuột bằng `AffineTransform.getRotateInstance(...)`.
  - Xoay 3 lớp độc lập của Trùm Chiến Xa Titan MK-IV ([`BossTank.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/enemy/BossTank.java)): Thân xe xoay theo vector di chuyển; Tháp pháo xoay ngắm bắn người chơi; Nòng đại pháo giật thụt lùi khi khai hỏa.
* **Hiệu Ứng Điện Ảnh với `AlphaComposite`**:
  - Lớp [`ScreenTransition.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/state/ScreenTransition.java) sử dụng `AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)` tạo hiệu ứng chuyển màn mờ dần (Fade In / Fade Out) khi chuyển tầng hầm ngục và khi thoát ra Menu chính.
* **Hệ Thống Giao Diện Tác Chiến Tùy Biến (Custom UI Components)**:
  - Máy trạng thái Menu: [`MenuState.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/state/MenuState.java) và [`PauseMenuUI.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/ui/PauseMenuUI.java) (tích hợp thanh trượt Volume Slider, nút Mute và hiệu ứng rê chuột phát sáng).
  - Sổ tay Balo Tác chiến 2 Tab: [`BackpackUI.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/ui/BackpackUI.java) (Tab Khí tài chi tiết chỉ số vũ khí & Tab Mô-đun Sinh học hiển thị 7 Perk dã chiến).
  - Cửa hàng và trạm tương tác: [`ShopUI.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/ui/ShopUI.java), [`CharacterUpgradeUI.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/ui/CharacterUpgradeUI.java).
  - HUD thời gian thực: [`PlayingHUD.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/ui/PlayingHUD.java) (vẽ thanh máu đa sắc, vòng tròn nạp đạn, thanh hồi chiêu né đòn, radar bản đồ thu nhỏ `MinimapHUD` và thanh máu Boss Titan MK-IV).

---

### 20.2.6. Lập Trình Hàm Với Java 8+ (Functional Programming: Lambdas & Streams API)

* **Biểu Thức Lambda (Lambda Expressions)**:
  - Xuất hiện tại 57 tệp mã nguồn trong dự án.
  - Sử dụng trong điều phối luồng giao diện tại [`Main.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/Main.java):
    ```java
    SwingUtilities.invokeLater(() -> {
        new GameWindow().setVisible(true);
    });
    ```
  - Định nghĩa hành vi hiệu ứng của các Perk sinh học trong [`CharacterBuff.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/buff/CharacterBuff.java).
  - Sắp xếp Collections theo tiêu chí khoảng cách với `Comparator`: `list.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()))`.
* **Java Streams API**:
  - Lọc và thống kê số lượng kẻ địch đang sống trong phòng tại [`PlayingState.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/state/PlayingState.java) và [`BossTank.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/entity/enemy/BossTank.java):
    ```java
    long aliveCount = enemies.stream().filter(Enemy::isAlive).count();
    ```
  - Sử dụng rộng rãi trong bộ kiểm thử tự động ([`EnemyAITest.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/test/java/com/groupproject/game/enemy/EnemyAITest.java), [`MedicAndGrenadierEnemyTest.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/test/java/com/groupproject/game/enemy/MedicAndGrenadierEnemyTest.java)) với `stream().filter(...).collect(Collectors.toList())` và `anyMatch(...)` để kiểm tra các điều kiện logic của quái vật và đạn đạo.

---

### 20.2.7. Tự Động Hóa Xây Dựng, Đóng Gói & Kiểm Thử Phần Mềm (Build Automation, Testing & Deployment)

* **Tự Động Hóa Xây Dựng với Apache Maven**:
  - Tệp cấu hình [`pom.xml`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/pom.xml) chuẩn JDK 21, quản lý phụ thuộc JUnit 5 (`junit-jupiter-api`, `junit-jupiter-engine`, `junit-jupiter-params`).
  - Cấu hình `maven-assembly-plugin` tự động đóng gói toàn bộ mã nguồn, tài nguyên hình ảnh, âm thanh số và font chữ vào duy nhất một tệp **Uber JAR thực thi độc lập** `DontKnow.jar` với manifest trỏ tới `com.groupproject.game.Main`.
* **Bộ Kiểm Thử Tự Động Quy Mô Lớn (JUnit 5 Test Suite)**:
  - Dự án sở hữu **24 tệp kiểm thử với 315 bài kiểm thử đơn vị (Unit Tests)** trong thư mục `src/test/java`.
  - Toàn bộ các bài test đều vượt qua thành công (**315/315 PASSED, 0 FAILURES, 0 ERRORS**), bao phủ toàn diện từ giải thuật sinh map hầm ngục, thuật toán tìm đường BFS, logic 6 cấp độ vũ khí, cơ chế lưu trữ Checkpoint, AI né góc cho đến hiệu ứng chuyển màn hình.
* **Quản Lý & Giải Phóng Tài Nguyên Sạch Sẽ**:
  - Gọi `g2d.dispose()` sau mỗi khung vẽ để giải phóng bộ nhớ đồ họa hệ điều hành, chống rò rỉ bộ nhớ (Memory Leak).
  - Quản lý tắt luồng `GameLoop` sạch sẽ bằng cờ `volatile` và `join()`, toàn bộ tệp I/O được đóng bằng `try-with-resources`.
* **Đóng Gói Triển Khai Thực Tế (Portable All-In-One)**:
  - Cung cấp thư mục `release/` chứa tệp JAR thực thi, tệp kịch bản [`RunGame.bat`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/release/RunGame.bat) và JRE 21 nhúng kèm, cho phép người dùng khởi chạy ngay lập tức mà không cần cài đặt thêm môi trường Java trên máy.
