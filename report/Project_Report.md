Cập nhật mới nhất vào ngày: 29/10/2026

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
| 1 | **Trưởng Nhóm** | *(Student ID 1)* | **Lead Engine Architect & Level Designer**: Kiến trúc vòng lặp game đa luồng (`GameLoop`), Thuật toán sinh hầm ngục lưới 4 hướng (`DungeonMap`), Phòng đặc biệt 2 lối vào/ra, Cơ chế khóa cửa giao tranh đa pha (`Room`), Quản lý trạng thái game (`GameStateManager`) & Lưu/Khôi phục Checkpoint (`RunSaveManager`). | 25% |
| 2 | **Thành viên 2** | *(Student ID 2)* | **Combat, Weapon & Audio Systems Engineer**: Khung đa xạ kích $360^\circ$ (`Weapon`, `WeaponManager`), Độ giật nở tâm AR/SCR, Hệ thống đạn đạo vật lý (`ProjectileManager`), Trạm tẩy luyện chỉ số súng (`StatReforgeStation`), Bộ trộn âm thanh phần mềm đa kênh PCM (`GameSoundManager` - 39 SFX & 7 BGM). | 25% |
| 3 | **Thành viên 3** | *(Student ID 3)* | **Enemy AI & Encounter Systems Engineer**: Trí tuệ nhân tạo tìm đường BFS trên lưới ô tile, Cảm biến xúc tu đa hướng tránh vật cản (`Steering Feelers`), Quân y `MedicEnemy`, Công binh `GrenadierEnemy`, Thiết kế Boss Tank 3 lớp xoay độc lập (`BossTank`), Đại pháo suy giảm sát thương theo khoảng cách (`BossCannonShell`). | 25% |
| 4 | **Thành viên 4** | *(Student ID 4)* | **Bio-Tech Systems, Graphics, UI & QA Engineer**: Trạm nâng cấp nhân vật & 7 Perk đặc biệt (`CharacterUpgradeStation`, `PlayerBuffManager`), Bộ đệm đồ họa phần cứng (`GameSpriteManager`, `WeaponSpriteManager`), Giao diện Balo tác chiến 2 Tab (`BackpackUI`), Radar (`MinimapHUD`), Bộ kiểm thử tự động 256 Unit Tests JUnit 5. | 25% |

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
12. **Testing (Kiểm thử tự động JUnit 5 với 256 bài test)**
13. **Experimental Results (Kết quả thực nghiệm & Đo lường hiệu năng)**
14. **Discussion (Thảo luận & Đánh giá)**
15. **Novelty and Contributions (Tính mới & Đóng góp kỹ thuật)**
16. **Limitations & Future Work (Hạn chế hiện tại & Hướng phát triển tương lai)**
17. **Conclusion (Kết luận)**
18. **References (Tài liệu tham khảo)**
19. **Appendix (Phụ lục: Hướng dẫn cài đặt, Cấu hình phím tắt, Ký hiệu Radar)**

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
5. **Hệ thống lưu trữ Checkpoint tự động (`RunSaveManager`) & Kiểm thử tự động JUnit 5**: Lưu trữ bền vững tiến trình vượt ải ra đĩa cứng (`save/run_save.properties`) và được kiểm chứng bởi **17 bộ kiểm thử (Test Suites)** với **256 bài kiểm thử tự động (Unit Tests)** đạt tỷ lệ vượt qua tuyệt đối **100% PASS**.

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
* **NFR3 – Độ tin cậy & Kiểm thử tự động**: Bao phủ toàn bộ nghiệp vụ bằng **256 Unit Tests** trên JUnit 5, chạy tự động trong môi trường Headless.

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
* `src/test/java/com/groupproject/game/`: Chứa 17 bộ kiểm thử tự động JUnit 5 (256 bài test).

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

# 12. TESTING (KIỂM THỬ TỰ ĐỘNG JUNIT 5 VỚI 256 BÀI TEST)

Dự án thiết lập hệ thống kiểm thử tự động toàn diện chuẩn **JUnit 5** gồm **17 Test Suites độc lập** với tổng cộng **256 ca kiểm thử (Unit Tests)**, đạt tỷ lệ **256/256 PASS (100%)**.

### Bảng Tổng Hợp 17 Bộ Kiểm Thử (Test Suites Summary)

| STT | Tên Test Suite | File Kiểm Thử | Số bài test | Kết quả | Nội dung kiểm chứng chính |
|---|---|---|---|---|---|
| 1 | **Weapon System** | `WeaponSystemTest.java` | **47** | **100% PASS** | Khí tài 4 slot, độ giật AR, sát thương Shotgun, nâng cấp chọn nhánh (DMG/RPM/ACC), súng Hạng S tự lên cấp, tiếng lên đạn Sniper/Shotgun. |
| 2 | **Enemy AI & Boss** | `EnemyAITest.java` | **37** | **100% PASS** | AI tìm đường BFS, cảm biến né tường, Raycasting LoS, Boss Tank 3 tầng xoay, đâm húc nghiền cover, đại pháo 40 HP tại tâm & giảm dần theo khoảng cách. |
| 3 | **Game Sound & BGM** | `GameSoundManagerTest.java` | **26** | **100% PASS** | Bộ trộn PCM 39 SFX & 7 BGM, nạp từng viên Shotgun, chuỗi nhạc Boss `Intro (2.8s fade-in) -> Loop -> End`, nhạc `Game Over -> The Range`. |
| 4 | **Dungeon Map** | `DungeonMapTest.java` | **18** | **100% PASS** | Lưới 4 hướng, Phòng đặc biệt 2 lối vào/ra, 6 Archetypes kích thước & bố cục cover, bảo vệ thông thoáng cửa ra vào. |
| 5 | **Combat Door Lock** | `CombatDoorTest.java` | **15** | **100% PASS** | Cửa blast doors tự động khóa khi giao tranh, triệu hồi viện binh Phase 2, mở cửa khi dọn sạch quái. |
| 6 | **Medic & Grenadier** | `MedicAndGrenadierEnemyTest.java` | **14** | **100% PASS** | Medic hồi máu $6\text{ HP}/0.8\text{ s}$, né người chơi, cứu đồng đội yếu máu; Grenadier ném lựu đạn hẹn giờ $2.0\text{ s}$, nổ AoE $78\text{ px}$. |
| 7 | **Ammo System** | `AmmoSystemTest.java` | **13** | **100% PASS** | Kho đạn dự trữ riêng biệt từng loại súng, tỷ lệ rơi đạn từ quái, nạp đạn không hao hụt. |
| 8 | **Player Health** | `PlayerHealthTest.java` | **12** | **100% PASS** | Máu người chơi, khung bất tử I-frames, trừ máu chính xác, rung màn hình, tử trận. |
| 9 | **Character Buff** | `CharacterBuffSystemTest.java` | **11** | **100% PASS** | 6 chỉ số nâng cấp Bio-Tech, 7 Perk đặc biệt (`PHASE_DODGE` chống kẹt 3 tầng, `KILL_HEAL`, `BLOCK_SHIELD`...). |
| 10 | **Hub The Range** | `HubRangeTest.java` | **11** | **100% PASS** | Sảnh chờ huấn luyện, trường bắn thử đạn, nâng cấp miễn phí tới Lv.10, thu hồi súng thử nghiệm khi xuất kích. |
| 11 | **Run Save Manager** | `RunSaveManagerTest.java` | **10** | **100% PASS** | Lưu/Đọc Checkpoint vượt ải, khôi phục đúng Stage/Vũ khí/Buff, chống lỗi Cold-Start nhảy nhầm vào phòng Boss Tank. |
| 12 | **Input & Pause Menu** | `KeyBindingsAndPauseMenuTest.java` | **9** | **100% PASS** | Phím tắt điều khiển tùy chỉnh, menu tạm dừng `[ESC]`, thanh trượt âm lượng SFX/BGM. |
| 13 | **Stat Reforge Room** | `StatReforgeRoomTest.java` | **9** | **100% PASS** | Xưởng Quân Khí, tách biệt Reroll Sao Chỉ Số vs Reroll Chí Mạng độc lập, lũy tiến chi phí Credits. |
| 14 | **Semi-Carbine Rifle** | `SemiCarbineRifleTest.java` | **8** | **100% PASS** | Súng trường bán tự động SCR băng 20 viên, sát thương $34\text{ HP}$, sơ tốc $1150\text{ px/s}$, độ giật nhịp bắn. |
| 15 | **Game Sprite Manager** | `GameSpriteManagerTest.java` | **6** | **100% PASS** | Nạp & Cache toàn bộ Sprite/Texture từ `src/main/resources/`, ảnh tương thích phần cứng VRAM & Pre-scaled Textures. |
| 16 | **Minimap System** | `MinimapSystemTest.java` | **6** | **100% PASS** | Radar bản đồ thu nhỏ, sương mù chiến thuật (Fog of War), biểu tượng nhận diện phòng. |
| 17 | **Weapon Sprite & Font** | `WeaponSpriteManagerTest.java` & `GameFontManagerTest.java` | **5** | **100% PASS** | Nạp 7 sprite vũ khí pixel art, nạp font `DeArPix` và bộ lọc khử ký tự `.notdef`. |
| **TỔNG** | **17 Test Suites** | **256 Unit Tests** | **256** | **100% PASS** | **Kiểm chứng toàn diện 100% các phân hệ Engine, Gameplay, AI, Audio & Storage** |

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
5. **Hệ Thống Lưu/Khôi Phục Checkpoint Bền Vững (`RunSaveManager`) & Bộ Kiểm Thử 256 Unit Tests**: Bảo toàn trọn vẹn trạng thái chiến dịch giữa các lần tắt mở game và đạt độ phủ kiểm thử tự động chuẩn công nghiệp.

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

Dự án **DontKnow – Top-Down Roguelite Shooter** đã hoàn thành toàn diện mọi mục tiêu thiết kế, kiến trúc và hiệu năng đề ra. Bằng việc kết hợp chặt chẽ các nguyên lý thiết kế hướng đối tượng **SOLID**, kiến trúc đa luồng 3 tầng tách rời, bộ trộn âm thanh số PCM đa kênh thời gian thực, giải thuật sinh hầm ngục thủ tục 4 hướng, trí tuệ nhân tạo tìm đường kết hợp xúc tu lượng giác và bộ kiểm thử tự động **256 Unit Tests (100% PASS)**, dự án là minh chứng rõ nét cho năng lực làm chủ công nghệ phần mềm và phát triển hệ thống tương tác thời gian thực chất lượng cao trên nền tảng Java thuần túy.

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
# Chạy toàn bộ 256 bài unit tests tự động
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
