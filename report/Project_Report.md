# BÁO CÁO DỰ ÁN CUỐI KỲ MÔN HỌC LẬP TRÌNH JAVA
## ĐỀ TÀI: THIẾT KẾ VÀ PHÁT TRIỂN HỆ THỐNG TRÒ CHƠI BẮN SÚNG ROGUELITE TỪ GÓC NHÌN TRÊN XUỐNG "DONTKNOW" DỰA TRÊN KIẾN TRÚC ĐA LUỒNG VÀ THUẬT TOÁN SINH HẦM NGỤC THỦ TỤC TRÊN NỀN TẢNG JAVA THUẦN

---

* **Tên dự án**: **DontKnow – Top-Down Roguelite Shooter Engine**
* **Môn học**: Lập trình Java Nâng Cao (Advanced Java Programming)
* **Chủ đề đăng ký**: **Topic 2 – Java Game Development**
* **Năm học**: 2026 – 2027
* **Nhóm thực hiện**: Group Unknown (Team Morningstar)
* **Mã nguồn dự án**: [`GroupProject`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject)
* **Phiên bản phát hành**: v1.0.0-SNAPSHOT (Đã đóng gói Portable All-In-One kèm JRE nhúng sẵn)

---

## BẢNG PHÂN CÔNG TRÁCH NHIỆM KỸ THUẬT CỦA CÁC THÀNH VIÊN

| STT | Họ và Tên | Mã Sinh Viên | Vai trò & Trách nhiệm kỹ thuật chính | Đóng góp (%) |
|---|---|---|---|---|
| 1 | **Trưởng Nhóm** | *(Student ID 1)* | **Lead Engine Architect & Level Designer**: Kiến trúc vòng lặp game đa luồng (`GameLoop`), Thuật toán sinh hầm ngục lưới 4 hướng (`DungeonMap`), Phòng đặc biệt 2 lối vào/ra, Cơ chế khóa cửa giao tranh đa pha (`Room`), Quản lý trạng thái game (`StateManager`). | 25% |
| 2 | **Thành viên 2** | *(Student ID 2)* | **Combat & Weapon Systems Engineer**: Khung đa xạ kích $360^\circ$ (`Weapon`, `WeaponManager`), Mô phỏng độ giật nở tâm AR, Hệ thống đạn đạo vật lý (`ProjectileManager`), Đạn phân mảnh Shotgun, Trạm tẩy luyện chỉ số súng (`StatReforgeStation`). | 25% |
| 3 | **Thành viên 3** | *(Student ID 3)* | **Enemy AI & Encounter Systems Engineer**: Trí tuệ nhân tạo tìm đường BFS trên lưới ô tile, Cảm biến xúc tu đa hướng tránh vật cản (`Steering Feelers`), Thiết kế Boss Tank 3 lớp xoay độc lập (`BossTank`), Cổng cảnh báo triệu hồi (`Spawn Telegraph`). | 25% |
| 4 | **Thành viên 4** | *(Student ID 4)* | **Bio-Tech Systems, UI & QA Engineer**: Trạm nâng cấp nhân vật & 7 Perk đặc biệt (`CharacterUpgradeStation`, `CharacterBuffSystem`), Giao diện Balo tác chiến 2 Tab (`BackpackUI`), Bản đồ chiến thuật Radar (`MinimapHUD`), Bộ kiểm thử tự động 209 Unit Tests JUnit 5. | 25% |

---

# MỤC LỤC BÁO CÁO (THEO CHUẨN INSTRUCTIONS.MD)

1. **Title (Tiêu đề)**
2. **Abstract (Tóm tắt)**
3. **Keywords (Từ khóa)**
4. **Introduction (Giới thiệu)**
5. **Problem Definition (Định nghĩa vấn đề)**
6. **Requirements (Yêu cầu hệ thống: Chức năng & Phi chức năng)**
7. **System Architecture (Kiến trúc hệ thống)**
8. **Design (Thiết kế hướng đối tượng, SOLID & Mẫu thiết kế)**
9. **Implementation (Chi tiết hiện thực hóa mã nguồn)**
10. **Technical Details (Chi tiết kỹ thuật chuyên sâu: Đa luồng, GUI, Không gian, FP)**
11. **Testing (Kiểm thử tự động JUnit 5 với 209 bài test)**
12. **Experimental Results (Kết quả thực nghiệm & Đo lường hiệu năng)**
13. **Discussion (Thảo luận & Đánh giá)**
14. **Novelty and Contributions (Tính mới & Đóng góp kỹ thuật)**
15. **Limitations (Hạn chế hiện tại)**
16. **Future Work (Hướng phát triển tương lai)**
17. **Conclusion (Kết luận)**
18. **References (Tài liệu tham khảo)**
19. **Appendix (Phụ lục: Hướng dẫn cài đặt, Cấu hình phím tắt, Cấu trúc thư mục)**

---

# 1. TITLE (TIÊU ĐỀ)

**DONTKNOW: THIẾT KẾ VÀ PHÁT TRIỂN HỆ THỐNG TRÒ CHƠI BẮN SÚNG ROGUELITE TỪ GÓC NHÌN TRÊN XUỐNG DỰA TRÊN KIẾN TRÚC ĐA LUỒNG VÀ THUẬT TOÁN SINH HẦM NGỤC THỦ TỤC TRÊN NỀN TẢNG JAVA THUẦN**

*(English: DESIGN AND IMPLEMENTATION OF "DONTKNOW" – A HIGH-PERFORMANCE 2D TOP-DOWN ROGUELITE SHOOTER ENGINE POWERED BY MULTITHREADED CONCURRENCY AND PROCEDURAL DUNGEON GENERATION IN PURE JAVA)*

---

# 2. ABSTRACT (TÓM TẮT)

Dự án **DontKnow** là một công trình phần mềm hoàn chỉnh nhằm nghiên cứu, thiết kế và phát triển một hệ thống trò chơi điện tử hành động roguelite góc nhìn từ trên xuống (Top-Down Roguelite Shooter) hiệu năng cao, hoàn toàn dựa trên nền tảng **Java 21 LTS** thuần túy và thư viện đồ họa **Java AWT/Swing** mà không sử dụng bất kỳ game engine của bên thứ ba nào (như Unity, Godot hay LibGDX). 

Mục tiêu cốt lõi của dự án là chứng minh khả năng hiện thực hóa các nguyên lý Công nghệ Phần mềm nâng cao—bao gồm Lập trình Hướng đối tượng (OOP), 5 nguyên lý thiết kế **SOLID**, các Mẫu thiết kế phần mềm kinh điển (**Design Patterns**), Lập trình Đa luồng (**Multithreading**) với kiến trúc vòng lặp game tách rời (**Decoupled Game Loop**), và Lập trình Hàm (**Functional Programming**) kết hợp với Stream API—để giải quyết bài toán phức tạp của tính toán tương tác thời gian thực.

Hệ thống tích hợp các giải thuật kỹ thuật chuyên sâu nổi bật:
1. **Thuật toán sinh bản đồ hầm ngục thủ tục (Procedural Dungeon Generation)** dựa trên lưới logic 4 hướng kề cận (12h, 3h, 6h, 9h) với cơ chế phòng đặc biệt 2 lối vào/ra (Transit Hub Crossroads) và 6 cấu trúc phòng chiến đấu (Combat Archetypes) đa dạng kích thước từ $12 \times 9$ đến $14 \times 10$ cùng 6 bố cục che chắn kiên cố (Hard Cover Layouts).
2. **Hệ thống điều khiển xạ kích đa hướng $360^\circ$** kết hợp mô hình vật lý đạn đạo thực tế, đường cong giật nở tâm (Bloom Recoil Dispersion), phân loại đạn dự trữ và cơ chế cận chiến dao găm khẩn cấp.
3. **Trí tuệ nhân tạo (AI) kẻ địch đa tầng**: Tích hợp thuật toán tìm đường trên lưới ô tile (Tile-based BFS Pathfinding), mảng cảm biến xúc tu góc quét đa hướng (Multi-Angle Steering Feelers), quái hỗ trợ Quân y (`MedicEnemy`) sở hữu hào quang nano đỏ hồi phục và cơ chế ưu tiên cứu thương đồng đội yếu máu, quái công binh (`GrenadierEnemy`) ném lựu đạn hẹn giờ đúng $2.0\text{ s}$ nổ diện rộng ép góc người chơi, và trùm cuối Titan Boss Tank sở hữu 3 tầng xoay độc lập (Decoupled 3-Layer Rotations) cùng bộ chiêu thức đâm húc rocket nghiền nát vật cản và pháo xung kích dẫn đường siêu thanh.
4. **Hệ thống kinh tế & Sinh trắc học chuyên sâu**: Trạm nâng cấp nhân vật Bio-Tech với 6 chỉ số thuộc tính và 7 Perk kỹ năng sinh tồn, kết hợp Trạm tẩy luyện chỉ số vũ khí (Field Gunsmith Workshop), Sảnh chờ & Trường bắn thử nghiệm (The Range), và Giao diện Balo tác chiến dã chiến 2 Tab (Field Tactical PDA) thao tác mượt mà.

Toàn bộ hệ thống được thẩm định độ tin cậy thông qua bộ kiểm thử tự động toàn diện gồm **209 ca kiểm thử đơn vị (Unit Tests)** trên nền tảng **JUnit 5**, đạt tỷ lệ vượt qua tuyệt đối (**100% PASS**). Trò chơi duy trì ổn định chuẩn **60.0 FPS / 60.0 UPS**, thời gian xử lý khung hình trung bình $\approx 16.58\text{ ms}$, dung lượng bộ nhớ Heap tối ưu chỉ dao động từ $65\text{ MB}$ đến $110\text{ MB}$, và được đóng gói hoàn chỉnh thành phiên bản di động All-In-One nhúng sẵn JRE độc lập.

---

# 3. KEYWORDS (TỪ KHÓA)

*Java Game Development; Object-Oriented Programming (OOP); SOLID Principles; Multithreaded Concurrency; Decoupled Game Loop; Procedural Dungeon Generation; 4-Way Adjacency Grid; Pathfinding AI; Spatial Hashing; Raycasting Line-of-Sight; Bio-Tech Augmentation; Finite State Machine; JUnit 5 Automated Testing.*

---

# 4. INTRODUCTION (GIỚI THIỆU)

### 4.1 Bối cảnh và Tầm quan trọng
Trong chương trình đào tạo Khoa học Máy tính và Kỹ thuật Phần mềm, việc phát triển các ứng dụng mô phỏng hoặc trò chơi tương tác thời gian thực (Real-time Interactive Systems) được coi là một trong những thử thách toàn diện nhất. Khác với các hệ thống phần mềm nghiệp vụ thông thường (CRUD Web/Desktop Applications) vốn chủ yếu xử lý tuần tự theo yêu cầu (Request-Response) và chịu sự chi phối bởi độ trễ I/O mạng hoặc cơ sở dữ liệu, một trò chơi điện tử hành động 2D đòi hỏi việc liên tục tính toán động học vật lý, xử lý va chạm không gian, định tuyến trí tuệ nhân tạo và kết xuất đồ họa với tần số cố định **60 lần mỗi giây** (tương đương ngân sách thời gian cực kỳ khắt khe là **16.67 mili-giây** cho mỗi khung hình).

Nhiều lập trình viên hiện đại thường có xu hướng phụ thuộc hoàn toàn vào các Game Engine thương mại (Unity, Unreal Engine, Godot). Mặc dù các công cụ này mang lại sự tiện lợi ban đầu, chúng che giấu đi toàn bộ bản chất kiến trúc cốt lõi bên dưới: từ việc quản lý bộ nhớ, đồng bộ luồng, kiểm soát vòng lặp game, đến việc tối ưu hóa toán học giải tích và cấu trúc dữ liệu không gian. Do đó, việc xây dựng một tựa game từ con số không (From Scratch) bằng ngôn ngữ lập trình thuần túy như Java là phương pháp tối ưu nhất để rèn luyện tư duy thiết kế phần mềm chuẩn mực, làm chủ cơ chế quản lý tài nguyên và giải quyết các bài toán kỹ thuật phức tạp ở tầng sâu.

### 4.2 Cảm hứng thiết kế và Định hướng sản phẩm
Dự án **DontKnow** ra đời từ sự kết hợp hài hòa và học tập tinh hoa từ hai tựa game hành động 2D đỉnh cao:
* **Khám phá Hầm ngục Roguelite (Dungeon Crawling - Kế thừa từ *Soul Knight*)**: Kế thừa cơ chế phòng ốc ngẫu nhiên sinh theo thủ tục, hệ thống cửa blast doors tự động phong tỏa giao tranh đa pha (Combat Lockdown), các trạm tương tác dịch vụ dã chiến (Cửa hàng chiến thuật, Bệ tẩy luyện chỉ số súng, Phòng nghiên cứu nâng cấp sinh trắc học Bio-Tech), cơ chế nhặt vũ khí ngẫu nhiên và tính bất định, cuốn hút của từng lượt chơi (Permadeath & Procedural Runs).
* **Xạ kích Chiến thuật $360^\circ$ & Cơ chế Sinh tồn Dã chiến (Tactical Top-Down Gunplay & Survival Dynamics - Kế thừa từ *Surviv.io*)**: Kế thừa và học tập phong cách điều khiển tác chiến mượt mà, phản hồi nhạy bén và khốc liệt từ tựa game sinh tồn 2D góc nhìn trên xuống đình đám *Surviv.io*:
  - Cơ chế ngắm bắn tự do $360^\circ$ theo tọa độ con trỏ chuột thời gian thực thông qua giải thuật lượng giác $\operatorname{atan2}$.
  - Mô phỏng độ giật súng nở tâm (Bloom Recoil Dispersion), độ phân mảnh đạn hoa cải Shotgun và nhịp bắn liên thanh súng trường AR/SMG.
  - Hệ thống quản lý hộp đạn dự trữ riêng biệt cho từng loại súng (đạn súng trường, đạn súng hoa cải, đạn tiểu liên) kết hợp cơ chế rơi đạn khi tiêu diệt địch.
  - Hệ thống cover 2 tầng độc đáo với thùng gỗ quân sự có thể bị bắn vỡ hoặc chém nát để mở đường tác chiến và tìm kiếm tài nguyên sinh tồn.
  - Cơ chế lướt né (Dodge Roll) sở hữu khung bất tử (I-frames), tạo nên nhịp độ giao tranh nghẹt thở, đòi hỏi kỹ năng di chuyển và phản xạ đỉnh cao.

Dự án không chỉ đặt trọng tâm vào trải nghiệm giải trí mà còn được xây dựng như một **bản mẫu kiến trúc phần mềm kiểu mẫu (Architectural Reference Implementation)**, chứng minh rằng Java Swing và Graphics2D hoàn toàn có thể vận hành mượt mà các tựa game hành động tốc độ cao mà không gặp phải các hiện tượng giật lag, xé hình hay tắc nghẽn giao diện.

---

# 5. PROBLEM DEFINITION (ĐỊNH NGHĨA VẤN ĐỀ)

Việc phát triển một trò chơi hành động 2D trên nền tảng Java thuần mà không có sự trợ giúp của engine chuyên dụng đặt ra 4 bài toán kỹ thuật hóc búa cần được định nghĩa rõ ràng:

### 5.1 Thách thức 1: Nghẽn Luồng Giao Diện và Hiện Tượng Giật Lag Đầu Vào (Thread Contention & Input Stuttering)
Mô hình đơn luồng giao diện người dùng truyền thống của Java Swing dựa trên luồng **Event Dispatch Thread (EDT)**. Nếu toàn bộ logic trò chơi (vật lý, đường đạn, thuật toán AI, va chạm bản đồ) được thực thi trực tiếp trên luồng EDT hoặc thông qua `javax.swing.Timer`, luồng này sẽ liên tục bị quá tải. Khi một tác vụ nặng xảy ra (ví dụ: Boss xả đạn diện rộng hoặc AI tính đường đi), EDT bị chiếm dụng khiến các sự kiện bàn phím/chuột bị hoãn xử lý (Typematic Input Delay), dẫn đến hiện tượng trôi phím, đơ chuột và giảm khung hình nghiêm trọng.

### 5.2 Thách thức 2: Sự Bùng Nổ Trạng Thái và Nguy Cơ Thoái Hóa Thành "God Object" (State Explosion & Anti-Patterns)
Một trò chơi roguelite bao gồm vô số trạng thái và quy tắc phức tạp đan xen: màn hình tiêu đề, menu tạm dừng, giao diện balo trang bị, cửa hàng giao dịch, trạm tẩy luyện, trạng thái phòng chiến đấu đang khóa hay mở, các pha tăng cường viện binh, và màn hình tổng kết nhiệm vụ. Nếu không có một kiến trúc phân tầng khoa học, mã nguồn sẽ nhanh chóng suy thoái thành các lớp khổng lồ (God Classes) với hàng nghìn dòng lệnh `if-else` lồng nhau, vi phạm nghiêm trọng nguyên lý Single Responsibility Principle (SRP) và gây khó khăn cho việc bảo trì, mở rộng.

### 5.3 Thách thức 3: Tính Nhất Quán Không Gian và Độ Trễ Kiểm Tra Va Chạm (Spatial Consistency & $O(1)$ Collision Latency)
Trong một thế giới game chứa hàng nghìn ô gạch địa hình, hàng chục kẻ địch, hàng trăm viên đạn bay cùng lúc và các chướng ngại vật cover kiên cố, việc kiểm tra va chạm bằng thuật toán ngây thơ (Naive $O(N^2)$ Pairwise Collision Check) sẽ nhanh chóng đánh sập CPU. Hơn nữa, việc sinh bản đồ ngẫu nhiên nếu không được kiểm soát chặt chẽ bằng ma trận tô-pô sẽ dẫn đến các lỗi chí mạng: phòng bị chồng lấn (Room Overlap), hành lang bị bít kín, chướng ngại vật spawn đè lên cửa ra vào khiến người chơi bị mắc kẹt hoàn toàn.

### 5.4 Thách thức 4: Cân Bằng Đạn Đạo, Vật Lý Và Trí Tuệ Nhân Tạo Thông Minh (Tactical AI & Ballistics Balance)
Kẻ địch trong game 2D thường mắc lỗi di chuyển ngớ ngẩn (chạy đâm đầu vào tường đá hoặc kẹt góc chướng ngại vật). Xây dựng một hệ thống AI có khả năng vừa né tránh vật cản, vừa tìm đường tắt tiếp cận người chơi, vừa biết lùi lại giữ cự ly an toàn (Kiting) và đổi hướng khi va chạm mà không làm sụt giảm hiệu năng tính toán là một thách thức kỹ thuật đòi hỏi thuật toán tối ưu.

---

# 6. REQUIREMENTS (YÊU CẦU HỆ THỐNG)

Dựa trên bản đặc tả môn học, hệ thống được phân rã thành các yêu cầu chức năng (FR) và phi chức năng (NFR) rõ ràng:

## 6.1 Yêu Cầu Chức Năng (Functional Requirements - FR)

* **FR1 – Vòng Lặp Trò Chơi Tách Rời (Decoupled Game Loop)**: Hệ thống phải vận hành một luồng tính toán physics chuyên biệt với chu kỳ cố định 60 lần/giây (Fixed-Timestep $60\text{ UPS}$), hoàn toàn độc lập với chu kỳ vẽ khung hình của giao diện ($60\text{ FPS}$).
* **FR2 – Sinh Bản Đồ Thủ Tục & Lưới Tô-Pô 4 Hướng (Procedural 4-Way Dungeon)**: Tự động khởi tạo ngẫu nhiên bản đồ hầm ngục gồm 1 Phòng Xuất Phát (`SPAWN`), 3 đến 5 Phòng Chiến Đấu (`COMBAT`), 1 Phòng Sơ Tán (`EXTRACT`), 1 Cửa Hàng (`SHOP`), 1 Xưởng Tẩy Luyện (`RESET_STAT`), và 1 Phòng Nghiên Cứu Nâng Cấp (`UPGRADE`). Tất cả kết nối qua các hành lang thẳng tắp theo 4 hướng chính (12h, 3h, 6h, 9h).
* **FR3 – Phòng Đặc Biệt Đa Lối Đi (Dual-Entrance Transit Crossroads)**: Các phòng tiện ích (`SHOP`, `RESET_STAT`, `UPGRADE`) phải ưu tiên sinh tại các nút giao kề $\ge 2$ phòng và tự động mở tối đa 2 lối vào/ra thông suốt, cho phép người chơi đi xuyên qua bản đồ mà không bị ngõ cụt.
* **FR4 – Đa Dạng Hóa Kích Thước & Bố Cục Cover Phòng Chiến Đấu (Diverse Combat Archetypes)**: Phòng combat phải có kích thước biến thiên qua 6 Archetypes (từ $12 \times 9$ đến $14 \times 10$) và 6 bố cục đặt cột trụ che chắn kiên cố khác nhau, tuyệt đối không đặt cover chắn ngang các lối cửa ra vào.
* **FR5 – Cơ Chế Phong Tỏa Giao Tranh Đa Pha (Multi-Phase Combat Lockdown)**: Khi người chơi bước vào phòng combat chưa khám phá, hệ thống tự động khóa chặt toàn bộ cửa blast doors, triệu hồi các đợt quái qua cổng cảnh báo (Spawn Telegraph), và chỉ mở cửa khi toàn bộ viện binh bị tiêu diệt hoàn toàn.
* **FR6 – Điều Khiển Tác Chiến $360^\circ$ & Cơ Động Né Tránh**: Hỗ trợ di chuyển 8 hướng bằng cụm phím `WASD`, ngắm bắn tự do $360^\circ$ theo góc lượng giác con trỏ chuột, nhào lộn né đòn bằng phím `SPACE` với khung bất tử (I-frames), và nhát chém cận chiến khẩn cấp bằng chuột phải hoặc phím `V`.
* **FR7 – Kho Vũ Khí & Mô Phỏng Vật Lý Đạn Đạo**: Cung cấp 4 lớp khí tài chuyên biệt (Súng lục P-9, Súng trường Viper AR, Súng tiểu liên Spectre SMG, Súng hoa cải Bulldog SG, Dao găm chiến thuật). Mô phỏng độ giật nở tâm, cơ số đạn trong băng, thời gian nạp đạn, và hộp đạn dự trữ riêng biệt cho từng loại súng.
* **FR8 – Trí Tuệ Nhân Tạo Kẻ Địch Đa Tầng & Trùm Cuối Titan Tank**: AI cận chiến biết tìm đường BFS tránh vật cản; AI xạ thủ biết lùi giữ cự ly và lạng sườn; Trùm cuối Titan Tank sở hữu 3 tầng xoay độc lập, đòn húc rocket nghiền nát thùng gỗ, súng máy 2 nòng và pháo laser siêu thanh.
* **FR9 – Hệ Thống Sinh Trắc Học & Nâng Cấp Nhân Vật**: Trạm Bio-Tech cung cấp 6 nhánh nâng cấp chỉ số cơ bản (Máu, Công, Tốc độ, Giảm thương, Hồi né, May mắn) và 7 Perk kỹ năng độc bản (Giáp chặn đòn, Diệt địch hồi máu, Né xuyên vật cản, Thu nhỏ hitbox, Băng đạn định kỳ, Băng đạn mở rộng, Bậc thầy chí mạng).
* **FR10 – Sổ Tay Tác Chiến Dã Chiến (Field Tactical PDA / Backpack UI)**: Bảng Balo tác chiến hiển thị dạng 2 Tab linh hoạt: Tab 1 quản lý 4 slot vũ khí và đạn dự trữ; Tab 2 hiển thị toàn diện hồ sơ sinh tồn, cấp độ chỉ số và trạng thái kích hoạt của 7 Perk.
* **FR11 – Radar Chiến Thuật Thu Nhỏ & Sương Mù Khám Phá (Minimap & Fog of War)**: HUD góc trên bên phải hiển thị sơ đồ hầm ngục thời gian thực, ẩn giấu các phòng chưa bước vào và làm nổi bật các phòng kề cận bằng biểu tượng nhận diện trực quan.

## 6.2 Yêu Cầu Phi Chức Năng (Non-Functional Requirements - NFR)

* **NFR1 – Hiệu năng & Tính Ổn định Thời gian thực (Real-Time Responsiveness)**: Tốc độ cập nhật vật lý đạt chuẩn ổn định $60.0\text{ UPS} \pm 0.5\%$; Tốc độ dựng hình đạt $60.0\text{ FPS}$ với độ trễ khung hình trung bình không vượt quá $16.67\text{ ms}$.
* **NFR2 – Tối ưu hóa Tiêu thụ Bộ nhớ & Chống Phân mảnh (Low Memory Footprint)**: Mức chiếm dụng RAM Heap trong quá trình gameplay thông thường $\le 80\text{ MB}$, và không vượt quá $120\text{ MB}$ trong các pha giao tranh cao điểm với Boss. Tuyệt đối không sinh rác bộ nhớ liên tục trong game loop để ngăn chặn hiện tượng khựng do Garbage Collection (Zero GC Hitching).
* **NFR3 – Tính Module Hóa & Dễ Dàng Mở Rộng (Extensibility & High Cohesion)**: Kiến trúc tuân thủ nghiêm ngặt các nguyên lý SOLID; việc bổ sung thêm một loại vũ khí mới, một chủng loại quái mới hay một hiệu ứng Perk mới không đòi hỏi phải sửa đổi cấu trúc cốt lõi của Game Engine.
* **NFR4 – Khả Năng Kiểm Thử Tự Động Toàn Diện (Testability)**: 100% các quy tắc nghiệp vụ cốt lõi (va chạm, sát thương, đạn đạo, chuyển đổi trạng thái, sinh bản đồ) phải được bao phủ bởi các kịch bản kiểm thử tự động độc lập, không phụ thuộc vào màn hình đồ họa thực tế (Headless Testability).
* **NFR5 – Tính Tự Chứa & Khả Năng Di Động Tuyệt Đối (Portability)**: Ứng dụng phải có khả năng khởi chạy trên bất kỳ hệ điều hành Windows nào (chỉ bằng thao tác 1-click vào file `.bat`) mà không đòi hỏi máy tính người dùng phải cài đặt trước JDK hoặc JRE (nhờ tích hợp bộ Java Runtime nhúng sẵn).

---

# 7. SYSTEM ARCHITECTURE (KIẾN TRÚC HỆ THỐNG)

Dự án **DontKnow** được thiết kế theo mô hình **Kiến trúc Phân Tầng Tách Rời (Decoupled Layered Architecture)** nhằm bảo đảm tính độc lập giữa logic tính toán và hiển thị đồ họa.

### 7.1 Sơ Đồ Kiến Trúc Tổng Thể

```
+---------------------------------------------------------------------------------------+
|                                    MAIN ENTRY POINT                                   |
|                                    com.groupproject.game                              |
|                                         (Main.java)                                   |
+-------------------------------------------+-------------------------------------------+
                                            | Khởi tạo trên Swing EDT
                                            v
+---------------------------------------------------------------------------------------+
|                                     GAME ENGINE                                       |
|                                  (GameEngine.java)                                    |
|  +----------------------------------------+----------------------------------------+  |
|  |     Luồng Giao Diện (EDT Thread)       |     Luồng Mô Phỏng (Worker Thread)     |  |
|  |             GameWindow                 |               GameLoop                 |  |
|  |            (JFrame Host)               |         (60 UPS Fixed Timestep)        |  |
|  |                  |                     |                   |                    |  |
|  |                  v                     |                   v                    |  |
|  |             GameCanvas                 |          State Tick Updates            |  |
|  |     (Double-Buffered JPanel)           |         (Physics, AI, Bullets)         |  |
|  +------------------+---------------------+-------------------+--------------------+  |
+---------------------|-----------------------------------------|-----------------------+
                      | Render Calls                            | State Transitions
                      v                                         v
+---------------------------------------------------------------------------------------+
|                             STATE MANAGEMENT SUBSYSTEM                                |
|             (StateManager, GameState, PlayingState, PauseState, GameOverState)        |
+---------------------------------------------------------------------------------------+
        |                                       |                               |
        v                                       v                               v
+-------------------------------+ +-------------------------------+ +--------------------+
|     WORLD & GEOMETRY LAYER    | |    ENTITY & COMBAT SUBSYSTEM  | |    UI & HUD LAYER  |
|  - DungeonMap (Lưới 80x45)    | |  - Player (Stats, Buffs, Roll)| |  - PlayingHUD      |
|  - Room (Archetypes, Doors)   | |  - Enemy Hierarchy (Melee, SG)| |  - MinimapHUD      |
|  - Tile & TileType (Covers)   | |  - BossTank (3 Rotations, Ram)| |  - BackpackUI      |
|  - Spatial AABB Grid Queries  | |  - Weapon & WeaponManager     | |  - UpgradeUI       |
|  - Line-of-Sight Raycasting   | |  - ProjectileManager (Bullets)| |  - StatReforgeUI   |
+-------------------------------+ +-------------------------------+ +--------------------+
```

### 7.2 Phân Tích Các Luồng Dữ Liệu và Điều Khiển (Data & Control Flow)
1. **Luồng Nhập Liệu (Input Pipeline)**: Các tín hiệu phần cứng từ chuột và bàn phím được bắt giữ bởi `InputHandler` gắn trên `GameCanvas`. Dữ liệu trạng thái phím (nhấn/thả) và tọa độ con trỏ màn hình được lưu trữ trong các mảng cờ hiệu (boolean flags) và biến số nguyên. Khi `GameLoop` chạy chu kỳ cập nhật, nó đọc các cờ hiệu này và chuyển đổi thành chuyển động vật lý của `Player` và góc ngắm $\theta = \text{atan2}(dy, dx)$.
2. **Luồng Mô Phỏng Vật Lý (Simulation Pipeline)**: Mỗi $16.67\text{ ms}$, `GameLoop` kích hoạt phương thức `tick()` của `PlayingState`. Quá trình này cập nhật tọa độ nhân vật, kiểm tra va chạm lưới $O(1)$ qua `DungeonMap`, cập nhật quỹ đạo hàng trăm viên đạn trong `ProjectileManager`, giải thuật AI kẻ địch, và kiểm tra điều kiện chuyển pha phòng chiến đấu.
3. **Luồng Kết Xuất Đồ Họa (Render Pipeline)**: Độc lập với vòng lặp mô phỏng, hệ điều hành Swing định kỳ gọi phương thức `paintComponent()` trên `GameCanvas`. Canvas sử dụng đối tượng `Camera` để dịch chuyển ma trận tọa độ thế giới sang tọa độ hiển thị (World-to-Screen Transformation), áp dụng cơ chế loại trừ ngoài vùng nhìn (Viewport Culling), và kết xuất mượt mà tất cả các lớp thực thể lên `Graphics2D` thông qua kỹ thuật Double Buffering.

---

# 8. DESIGN (THIẾT KẾ HƯỚNG ĐỐI TƯỢNG, SOLID & DESIGN PATTERNS)

## 8.1 Vận Dụng 4 Trụ Cột Lập Trình Hướng Đối Tượng (OOP)

* **Tính Đóng Gói (Encapsulation)**: Mọi thuộc tính quan trọng của đối tượng (máu của `Player`, thông số sát thương của `Weapon`, tọa độ lưới của `Tile`) đều được khai báo với phạm vi truy cập `private` hoặc `protected`. Trạng thái nội tại chỉ có thể được truy xuất hoặc biến đổi thông qua các phương thức nghiệp vụ có kiểm soát (ví dụ: `takeDamage(int amount)` áp dụng kiểm tra khung bất tử I-frames, tự động trừ máu, kích hoạt rung màn hình và đổi cờ hiệu tử trận).
* **Tính Trừu Tượng Hóa (Abstraction)**: Các khái niệm phức tạp được trừu tượng hóa thành các lớp cơ sở (Base Classes) và Giao diện (Interfaces):
  - Lớp trừu tượng `Weapon`: Định nghĩa bộ khung tổng quát cho tất cả các loại vũ khí cầm tay (cơ số đạn, tốc độ bắn, thời gian nạp đạn, giật súng) với phương thức trừu tượng `executeFireMechanism()`.
  - Lớp trừu tượng `Enemy`: Chuẩn hóa hành vi kẻ địch (di chuyển, va chạm, nhận sát thương, rớt đồ, hiệu ứng lóe sáng khi bị bắn).
* **Tính Kế Thừa (Inheritance)**: Hệ thống tận dụng triệt để quan hệ kế thừa hình tháp để tái sử dụng tối đa mã nguồn chung:
  - Cây kế thừa Vũ khí: `Weapon` $\rightarrow$ `AssaultRifle`, `Shotgun`, `Pistol`, `CombatKnife`.
  - Cây kế thừa Kẻ địch: `Enemy` $\rightarrow$ `MeleeEnemy`, `RangedEnemy`, `BossTank`.
* **Tính Đa Hình (Polymorphism)**: `PlayingState` quản lý danh sách tổng quát `List<Enemy>` và `List<Weapon>`. Trong vòng lặp cập nhật, hệ thống chỉ cần duyệt qua danh sách và gọi `enemy.update()` hoặc `weapon.update()`. Cơ chế liên kết động thời gian chạy (Dynamic Method Dispatch) sẽ tự động kích hoạt mã xử lý tương ứng của từng lớp cụ thể (ví dụ: `BossTank` thực thi logic xoay 3 lớp và đâm húc, trong khi `RangedEnemy` thực thi logic giữ khoảng cách và lạng sườn).

## 8.2 Áp Dụng 5 Nguyên Lý SOLID

| Nguyên lý SOLID | Vận dụng thực tế trong mã nguồn dự án |
|---|---|
| **S – Single Responsibility Principle (Đơn trách nhiệm)** | Mỗi lớp chỉ đảm nhận duy nhất một vai trò chuyên biệt: `DungeonMap` chỉ quản lý ma trận địa hình và va chạm; `ProjectileManager` chỉ quản lý vòng đời đạn; `Camera` chỉ tính toán phép dời hình tọa độ; `PlayerBuffManager` chỉ quản lý logic bùa chú nâng cấp. |
| **O – Open/Closed Principle (Đóng mở)** | Hệ thống vũ khí và bùa chú được thiết kế mở để mở rộng nhưng đóng để sửa đổi. Khi muốn bổ sung một loại vũ khí mới (ví dụ: *SniperRifle*), lập trình viên chỉ cần tạo lớp kế thừa `Weapon` và định nghĩa hành vi bắn mà không cần chạm vào `WeaponManager` hay `PlayingState`. |
| **L – Liskov Substitution Principle (Thay thế Liskov)** | Tất cả các lớp dẫn xuất từ `Enemy` hoặc `Weapon` đều có thể thay thế hoàn hảo cho lớp cha mà không làm thay đổi tính đúng đắn của trò chơi. Một đối tượng `BossTank` có thể được xử lý hoàn toàn như một `Enemy` thông thường trong danh sách mục tiêu của hệ thống đạn đạo. |
| **I – Interface Segregation Principle (Phân tách giao diện)** | Thay vì tạo một giao diện "béo" chứa tất cả các phương thức, hệ thống phân rã thành các giao diện tinh gọn, cụ thể: `Updatable` (chỉ có `update(double dt)`), `Renderable` (chỉ có `render(Graphics2D g, Camera cam)`), `Collidable` (kiểm tra ranh giới AABB). |
| **D – Dependency Inversion Principle (Đảo ngược phụ thuộc)** | Các hệ thống điều khiển cấp cao không phụ thuộc trực tiếp vào các cài đặt phần cứng cấp thấp. `PlayingState` giao tiếp với kho vũ khí thông qua lớp trừu tượng `Weapon` thay vì phụ thuộc cứng vào các loại súng cụ thể. |

## 8.3 Các Mẫu Thiết Kế Phần Mềm (Design Patterns) Đã Triển Khai

1. **State Pattern (Mẫu Trạng Thái)**:
   - Quản lý toàn bộ vòng đời của game thông qua interface `GameState` và bộ điều phối `StateManager`.
   - Các trạng thái cụ thể: `PlayingState`, `PauseMenuState`, `GameOverState`, `VictoryState`. Khi người chơi nhấn `ESC`, hệ thống chuyển đổi ngữ cảnh tức thì sang `PauseMenuState` mà không làm mất dữ liệu của màn chơi.
2. **Strategy Pattern (Mẫu Chiến Lược)**:
   - Ứng dụng trong hệ thống hành vi AI của kẻ địch. Các thuật toán di chuyển (BFS Pathfinding, Strafe-Flanking, Kiting, hoặc Charge-Rush) được trừu tượng hóa và hoán đổi linh hoạt tùy theo khoảng cách và trạng thái của người chơi.
3. **Factory Method Pattern (Mẫu Nhà Máy)**:
   - Triển khai trong việc sinh ngẫu nhiên vũ khí và hòm tiếp tế dã chiến (`WeaponChest`). Dựa trên tỷ lệ rơi đồ và chỉ số may mắn (`Loot Luck`), Factory sẽ tự động khởi tạo vũ khí với phẩm cấp sao ngẫu nhiên (Star Level 1 đến 5).
4. **Observer / Callback Pattern (Mẫu Quan Sát)**:
   - Kích hoạt các phản xạ sự kiện khi mục tiêu bị tiêu diệt: Khi một kẻ địch chết, hệ thống tự động kích hoạt callback để `PlayerBuffManager` kiểm tra perk `KILL_HEAL` (hồi máu), thưởng tiền tệ chiến dịch (`Credits`), và thông báo cho `Room` kiểm tra điều kiện dọn sạch phòng.
5. **Decoupled Component / Subsystem Pattern**:
   - Tách rời hoàn toàn giao diện người dùng thành các subsystem độc lập: `PlayingHUD`, `MinimapHUD`, `BackpackUI`, `CharacterUpgradeUI`, `StatReforgeUI`. Mỗi subsystem tự quản lý tọa độ, sự kiện nhấp chuột và hiệu ứng hiển thị riêng biệt.

---

# 9. IMPLEMENTATION (CHI TIẾT HIỆN THỰC HÓA MÃ NGUỒN)

Mã nguồn dự án được tổ chức chặt chẽ theo cấu trúc gói chuẩn của Maven trong thư mục `src/main/java/com/groupproject/game/`:

```text
com.groupproject.game
├── Main.java                                # Điểm khởi chạy ứng dụng
├── core/                                    # Nhân lõi của Game Engine
│   ├── GameConfig.java                      # Hằng số cấu hình toàn cục
│   ├── GameEngine.java                      # Bộ điều phối luồng chính
│   ├── GameLoop.java                        # Vòng lặp physics đa luồng 60 UPS
│   ├── GameWindow.java                      # Khung cửa sổ JFrame
│   ├── GameCanvas.java                      # Bề mặt vẽ đồ họa JPanel Double-Buffered
│   ├── Camera.java                          # Máy quay theo dõi mượt mà
│   └── InputHandler.java                    # Bộ lắng nghe sự kiện phím và chuột
├── dungeon/                                 # Hệ thống sinh hầm ngục thủ tục
│   ├── DungeonMap.java                      # Ma trận lưới, 6 Archetypes & Kết nối đa hướng
│   ├── Room.java                            # Cấu trúc phòng, cửa blast doors, khóa giao tranh
│   ├── Tile.java                            # Ô gạch, vẽ họa tiết kim loại và hiệu ứng vỡ
│   └── TileType.java                        # Enum phân loại địa hình (WALL, FLOOR, COVER...)
├── entity/                                  # Thực thể sống trong game
│   ├── player/Player.java                   # Nhân vật chính, lướt né, I-frames, sinh mệnh
│   ├── enemy/                               # Hệ thống kẻ địch và trùm
│   │   ├── TargetDummy.java                 # Lớp cơ sở trừu tượng cho bia/kẻ địch, thanh máu đỏ, heal()
│   │   ├── MeleeEnemy.java                  # Quái cận chiến, tìm đường BFS, xúc tu né vật cản
│   │   ├── RangedPistolEnemy.java           # Quái xạ thủ, lùi kẹp góc, lạng sườn né đạn
│   │   ├── MedicEnemy.java                  # Quái quân y hỗ trợ, né người chơi, hào quang đỏ cứu thương
│   │   ├── GrenadierEnemy.java              # Quái công binh, ném lựu đạn hẹn giờ 2.0s ép góc
│   │   └── BossTank.java                    # Trùm Titan Tank 3 tầng xoay, húc rocket, pháo laser
│   ├── weapon/                              # Hệ thống khí tài và đạn dược
│   │   ├── Weapon.java                      # Lớp trừu tượng vũ khí, độ giật, nạp đạn
│   │   ├── WeaponManager.java               # Quản lý 4 slot khí tài, đổi súng, rơi đồ
│   │   ├── AssaultRifle.java                # Súng trường tấn công Viper AR
│   │   ├── SemiCarbineRifle.java            # Súng trường bán tự động SCR (băng 20 viên, sát thương cao)
│   │   ├── SniperRifle.java                 # Súng bắn tỉa kéo quy lát SR Bolt Action
│   │   ├── Shotgun.java                     # Súng bắn đạn hoa cải Bulldog SG
│   │   ├── SMG.java                         # Súng tiểu liên tốc độ cao Spectre SMG
│   │   ├── Pistol.java                      # Súng lục bán tự động P-9
│   │   └── CombatKnife.java                 # Dao găm cận chiến khẩn cấp
│   ├── projectile/                          # Hệ thống đạn đạo
│   │   ├── Projectile.java                  # Thực thể đạn bay, sát thương, tầm bắn
│   │   ├── EnemyGrenade.java                # Lựu đạn hẹn giờ 2.0s, cảnh báo AoE 78px, phá hủy cover
│   │   └── ProjectileManager.java           # Quản lý hàng trăm viên đạn, va chạm vật lý
│   ├── hub/                                 # Sảnh chờ huấn luyện & Trường bắn dã chiến
│   │   ├── HubRangeState.java               # Trạng thái trường bắn The Range thử nghiệm vũ khí
│   │   └── RecoilTracerManager.java         # Vẽ vệt dạ quang phân tích mẫu giật đạn đạo
│   ├── buff/                                # Hệ sinh thái bùa chú & Nâng cấp
│   │   ├── CharacterBuff.java               # Enum định nghĩa 6 chỉ số và 7 Perk
│   │   └── PlayerBuffManager.java           # Quản lý tiến trình cấp độ buff và áp dụng hiệu ứng
│   ├── station/                             # Các trạm tương tác dã chiến
│   │   ├── CharacterUpgradeStation.java     # Bệ nghiên cứu nâng cấp Bio-Tech
│   │   └── StatReforgeStation.java          # Bệ rèn đúc tẩy luyện chỉ số súng
│   └── item/WeaponChest.java                # Hòm vũ khí chiến thuật rơi đồ
├── state/                                   # Hệ thống quản lý trạng thái
│   ├── GameState.java                       # Interface trạng thái
│   ├── StateManager.java                    # Bộ chuyển đổi trạng thái
│   └── PlayingState.java                    # Trạng thái tác chiến chính
└── ui/                                      # Giao diện người dùng đồ họa Sci-Fi
    ├── PlayingHUD.java                      # Thanh máu, đạn, vũ khí nhanh trên màn hình
    ├── MinimapHUD.java                      # Radar bản đồ thu nhỏ, sương mù khám phá
    ├── BackpackUI.java                      # Balo tác chiến 2 Tab (Khí tài & Hồ sơ Perks)
    ├── CharacterUpgradeUI.java              # Bảng nâng cấp Bio-Tech tương tác chuột
    └── StatReforgeUI.java                   # Bảng giao diện tẩy luyện chỉ số súng
```

### 9.1 Hệ Thống Sinh Hầm Ngục Thủ Tục (DungeonMap & Room Generation)
Bản đồ được biểu diễn bằng ma trận $80 \times 45$ ô gạch, chia thành lưới các buồng phòng chuẩn $4 \times 3$. 
1. **Lưới phòng 4 hướng**: Các phòng được phân bổ nghiêm ngặt theo 4 hướng trực giao (12h, 3h, 6h, 9h) với hành lang thẳng tắp rộng 4 ô gạch bọc tường đá kín khít, loại bỏ hoàn toàn các khúc quanh co chữ L rối rắm.
2. **Nút giao 2 lối vào/ra cho phòng đặc biệt (Step 6.5)**: Hàm `findBestSpecialRoomPlacement` quét ma trận để tìm các ô có $\ge 2$ phòng kề cận. Thuật toán sau đó kết nối tối đa 2 hành lang thông sang các phòng bên cạnh, lắp đặt cổng nhận diện riêng biệt (`DOOR_SHOP` màu vàng kim, `DOOR_RESET_STAT` màu xanh lơ, `DOOR_UPGRADE` màu lục ngọc). Các cửa này luôn mở, giúp người chơi tự do di chuyển xuyên phòng tiện ích.
3. **6 Archetypes phòng chiến đấu đa dạng**: Để loại bỏ sự đơn điệu, các phòng combat được chia thành 6 mẫu với diện tích và bố cục cover kiên cố riêng:
   - *Archetype 0 (Tactical Arena - $14 \times 10$)*: 4 cột trụ góc cổ điển.
   - *Archetype 1 (CQC Bunker - $12 \times 9$)*: 2 trụ kép trung tâm che chắn tầm gần.
   - *Archetype 2 (Assault Gallery - $14 \times 9$)*: 2 dãy tường song song phân làn hỏa lực.
   - *Archetype 3 (Crossfire Chamber - $13 \times 10$)*: Công sự góc chữ L chéo góc.
   - *Archetype 4 (Tactical Killzone - $13 \times 9$)*: Trụ đơn đan xen so le hình bàn cờ.
   - *Archetype 5 (Divided Bastion - $14 \times 10$)*: Chiến hào chia đôi cánh bảo vệ hai sườn.
4. **Bảo đảm an toàn cửa ra vào (`isNearDoorway`)**: Mọi vị trí đặt cột trụ cover cứng đều phải thỏa mãn điều kiện cách xa các ô cửa thực tế đã lắp đặt trên cả 4 cạnh phòng và cách xa tâm phòng ít nhất 2 ô tile, bảo đảm không bao giờ gây tắc nghẽn giao thông.

### 9.2 Hệ Thống Vũ Khí, Đạn Đạo & Độ Giật Nở Tâm (Ballistics & Recoil)
1. **Ngắm bắn $360^\circ$ lượng giác**: Góc bắn được xác định chính xác theo thời gian thực bằng góc cực giữa tọa độ con trỏ chuột trong thế giới $(wx, wy)$ và tâm nhân vật $(px, py)$:
   $$\theta = \operatorname{atan2}(wy - py, wx - px)$$
2. **Đường cong giật nở tâm (Bloom Recoil Curve)**: Khẩu súng trường Viper AR sở hữu độ chính xác tuyệt đối ở những phát đạn đầu tiên. Khi người chơi giữ chặt chuột trái để sấy liên thanh, góc lệch ngẫu nhiên $\Delta\theta$ sẽ nở rộng dần theo thời gian và chỉ hồi phục về ban đầu khi ngừng bắn. Phẩm cấp sao càng cao (Star Level 1 đến 5), độ tản mát cực đại càng được thu hẹp.
3. **Súng trường bán tự động Semi-Carbine Rifle (SCR)**: Băng đạn chuẩn 20 viên, sát thương cơ sở cực cao $34\text{ HP}$ và vận tốc đạn $1150\text{ px/s}$. SCR sở hữu cơ chế giật nặng (Heavy Recoil): chỉ duy nhất viên đầu tiên chuẩn xác tuyệt đối, ngay từ phát thứ 2 độ tản mát tăng vọt lên tới $\pm 22.9^\circ$ cùng thuật toán chống trúng may mắn (Anti-Lucky Recoil Jitter), buộc người chơi phải căn nhịp nhấp chuột chiến thuật.
4. **Cơ chế hoán đổi và rơi súng xuống đất**: Người chơi sở hữu 4 slot vũ khí. Khi đổi một khẩu súng mới từ hòm đồ hoặc cửa hàng trong khi cả 2 slot chính đã đầy, khẩu súng đang cầm trên tay sẽ tự động được văng ra sàn nhà dưới dạng một vật phẩm rơi (`WeaponDropEntity`) bảo lưu nguyên vẹn số đạn trong băng và phẩm cấp sao.

### 9.3 Trí Tuệ Nhân Tạo Kẻ Địch (Tactical AI & Titan Boss Tank)
1. **Quái cận chiến (Melee Swarmers)**: Khi tầm nhìn thẳng tới người chơi bị che khuất bởi cột cover hoặc tường đá, quái kích hoạt giải thuật tìm kiếm theo chiều rộng (BFS Pathfinding) trên ma trận ô tile để tính toán đường đi ngắn nhất. Đồng thời, quái phát ra 10 xúc tu cảm biến (Steering Feelers) lệch góc từ $\pm 25^\circ$ đến $\pm 115^\circ$ để trượt mượt mà men theo mép tường mà không bị khựng lại.
2. **Quái xạ thủ (Rogue Pistol Scouts)**: Áp dụng hành vi thả diều (Kiting) chiến thuật: chủ động lùi lại khi người chơi áp sát $< 190\text{ px}$, tiến lên khi cách xa $> 360\text{ px}$, và liên tục lạng ngang (Strafing) để tìm góc bắn xuyên qua chướng ngại vật. Khi va chạm vào tường hầm ngục, quái tự động đảo ngược chiều lạng ngay lập tức.
3. **Quái quân y hỗ trợ (Tactical Field Medic - `MedicEnemy`)**: Đơn vị hỗ trợ không có khả năng chiến đấu trực tiếp. Sở hữu vòng tròn hào quang nano đỏ hồi máu bán kính $135\text{ px}$ phát xung mỗi $0.8\text{ s}$ hồi $6\text{ HP}$ cho đồng minh. AI có phản xạ chạy trốn khi người chơi áp sát $< 210\text{ px}$, tự động quét tìm và tiếp cận đồng đội bị thương nặng nhất (% máu thấp nhất). Vùng hào quang được vẽ phẳng ở layer dưới toàn bộ các thực thể, thân bọc giáp xám kim loại quân sự của phe địch.
4. **Quái công binh ném lựu đạn ép góc (Heavy Grenadier - `GrenadierEnemy`)**: Giữ cự ly tầm trung $175 - 400\text{ px}$, ném lựu đạn hẹn giờ `EnemyGrenade` mỗi $3.0\text{ s}$. Lựu đạn đếm ngược ngòi nổ đúng $2.0\text{ s}$ kèm vòng cảnh báo nguy hiểm đỏ $78\text{ px}$ nhấp nháy dồn dập, ép góc người chơi (Area Denial / Zoning), nổ diện rộng gây $24\text{ HP}$ và phá hủy mọi vật cản trong bán kính nổ.
5. **Chuẩn hóa Giao diện Kẻ Địch**: Thanh máu trên đầu toàn bộ kẻ địch luôn luôn hiển thị màu đỏ `new Color(230, 45, 45)` ở mọi mức máu; loại bỏ hoàn toàn tên/text trên đầu kẻ địch để đảm bảo giao diện thông thoáng, chuẩn mực.
6. **Trùm Titan Battle Tank (Stage 4 Climax)**: Được cấu thành từ 3 lớp xoay độc lập:
   - *Lớp gầm xe (Chassis)*: Xoay bám sát theo vector vận tốc di chuyển thực tế.
   - *Lớp tháp pháo súng máy (Twin Machine Gun Turret)*: Tự do xoay $360^\circ$ khóa chặt tọa độ người chơi.
   - *Lớp pháo hạng nặng (Heavy Cannon)*: Thức tỉnh khi máu xuống dưới 50%, kích hoạt tia laser ngắm chuẩn xác trong $0.9\text{ s}$, đóng băng khóa góc trong $0.4\text{ s}$ (cửa sổ vàng để người chơi né tránh), và phóng ra đạn xung kích siêu thanh $1350\text{ px/s}$ gây sát thương nổ diện rộng $85\text{ px}$.
   - *Đòn đâm húc Rocket Charge ($950\text{ px/s}$)*: Phát cảnh báo chấm than `!`, lao thẳng về phía trước; nếu đâm trúng tường hoặc cột đá sẽ nổ tung tia lửa, rung màn hình cực mạnh và khiến Boss bị choáng (Stun) trong $0.8\text{ s}$.

---

# 10. TECHNICAL DETAILS (CHI TIẾT KỸ THUẬT CHUYÊN SÂU)

## 10.1 Xử Lý Đa Luồng & Kiến Trúc Vòng Lặp Game (Decoupled Multithreading)

Để giải quyết triệt để vấn đề xung đột luồng và giật khung hình, hệ thống tách rời hoàn toàn thành **2 luồng thực thi độc lập**:

```java
// Cấu trúc vòng lặp game tách rời trong GameLoop.java
public void run() {
    final double timeStep = 1.0 / 60.0; // 60 UPS cố định (16.666ms)
    long lastTime = System.nanoTime();
    double accumulator = 0.0;

    while (running) {
        long now = System.nanoTime();
        double frameTime = (now - lastTime) / 1_000_000_000.0;
        lastTime = now;
        accumulator += Math.min(frameTime, 0.25); // Chống tích lũy thời gian cực đoan

        // 1. Luồng Worker thực thi các bước cập nhật vật lý chuẩn xác cố định
        while (accumulator >= timeStep) {
            stateManager.update(timeStep);
            accumulator -= timeStep;
        }

        // 2. Yêu cầu luồng Swing EDT vẽ lại giao diện (Bất đồng bộ)
        canvas.repaint();

        // 3. Tự động điều tiết tần số ngủ để nhường CPU
        try {
            long elapsed = (System.nanoTime() - now) / 1_000_000;
            long sleepTime = 16 - elapsed;
            if (sleepTime > 0) Thread.sleep(sleepTime);
        } catch (InterruptedException ignored) {}
    }
}
```

* **Cơ chế đồng bộ hóa an toàn (Thread Safety)**: Để tránh lỗi bất đồng bộ phổ biến `ConcurrentModificationException` khi luồng `GameLoop` đang sửa đổi danh sách đạn/quái trong lúc luồng Swing EDT đang duyệt danh sách để vẽ, các danh sách thực thể động được bảo vệ bằng cơ chế sao chép an toàn (Defensive Snapshots) hoặc sử dụng các cấu trúc dữ liệu luồng an toàn.

## 10.2 Tối Ưu Hóa Kết Xuất Đồ Họa & Double Buffering (GUI & Viewport Culling)

1. **Double Buffering (Bộ Đệm Kép Tự Nhiên)**: Lớp `GameCanvas` kế thừa từ `JPanel` với cờ hiệu `setDoubleBuffered(true)`. Toàn bộ quá trình vẽ được thực hiện trên một bộ đệm hình ảnh ngoài màn hình (Off-screen Image Buffer) trước khi được lật (Flip) lên màn hình chính, triệt tiêu 100% hiện tượng xé hình (Screen Tearing) và nhấp nháy (Flickering).
2. **Cơ Chế Viewport Culling (Loại Bỏ Ngoài Tầm Nhìn)**:
   - Bản đồ thế giới có kích thước lớn ($3840 \times 2160\text{ px}$), trong khi màn hình hiển thị chỉ có độ phân giải $1280 \times 720\text{ px}$.
   - Trước khi vẽ bất kỳ ô tile hoặc thực thể nào, thuật toán kiểm tra giao cắt không gian giữa hình chữ nhật ranh giới của đối tượng với hình chữ nhật tầm nhìn của `Camera`:
     $$\text{IsVisible} = (x + w \ge \text{CamX}) \land (x \le \text{CamX} + \text{CamW}) \land (y + h \ge \text{CamY}) \land (y \le \text{CamY} + \text{CamH})$$
   - Nhờ đó, hơn 85% số ô gạch và thực thể nằm ngoài màn hình được loại bỏ ngay lập tức, tiết kiệm tối đa tài nguyên đồ họa của CPU.

## 10.3 Thuật Toán Không Gian & Kiểm Tra Va Chạm $O(1)$ (Spatial Hashing & Math)

* **Kiểm tra va chạm địa hình tức thì $O(1)$**: Thay vì duyệt qua toàn bộ danh sách hàng nghìn ô gạch, hệ thống sử dụng thuật toán băm không gian ma trận (Spatial Grid Indexing). Tọa độ thế giới $(x, y)$ của hộp bao AABB (Axis-Aligned Bounding Box) được ánh xạ thẳng về chỉ số cột và hàng của mảng 2D:
  $$\text{minCol} = \left\lfloor \frac{x}{\text{TILE\_SIZE}} \right\rfloor, \quad \text{maxCol} = \left\lfloor \frac{x + w - 0.001}{\text{TILE\_SIZE}} \right\rfloor$$
  $$\text{minRow} = \left\lfloor \frac{y}{\text{TILE\_SIZE}} \right\rfloor, \quad \text{maxRow} = \left\lfloor \frac{y + h - 0.001}{\text{TILE\_SIZE}} \right\rfloor$$
  Phép kiểm tra chỉ truy xuất tối đa từ 4 đến 9 ô tile xung quanh thực thể, đạt độ phức tạp toán học tuyệt đối $O(1)$ với thời gian thực thi $< 0.005\text{ ms}$.
* **Kiểm tra tầm nhìn thẳng Raycasting (Line-of-Sight)**: Để xác định xem quái xạ thủ có bắn trúng người chơi hay không, thuật toán phóng một tia tia lượng giác từ tâm quái đến tâm người chơi theo từng bước nhảy $16\text{ px}$. Nếu bất kỳ điểm lấy mẫu nào chạm vào ô gạch `isSolid() == true` (tường đá hoặc cột cover cứng), tầm nhìn lập tức bị chặn và kẻ địch sẽ tự động đổi sang chiến thuật tìm đường vòng.

## 10.4 Ứng Dụng Lập Trình Hàm (Functional Programming & Stream API)

Dự án áp dụng sâu rộng các tính năng hiện đại của Java Functional Programming:
* **Stream API & Lambda Expressions**: Sử dụng trong việc lọc danh sách ứng viên phòng đặc biệt, sắp xếp độ ưu tiên theo số phòng kề cận (`occupiedNeighbors`), và truy vấn thực thể:
  ```java
  // Lọc và sắp xếp các ứng viên đặt phòng đặc biệt theo số phòng kề cận
  choices.sort((a, b) -> Integer.compare(b.occupiedNeighbors, a.occupiedNeighbors));

  // Lọc các kẻ địch còn sống nằm trong bán kính vụ nổ AoE của pháo Boss
  List<Enemy> affectedTargets = enemies.stream()
      .filter(e -> e.isAlive() && e.getDistanceTo(blastX, blastY) <= blastRadius)
      .collect(Collectors.toList());
  ```
* **Method References**: Sử dụng để truyền các hàm xử lý sự kiện trong vòng đời thực thể, giúp mã nguồn cô đọng, trong sáng và loại bỏ hoàn toàn các lớp nặc danh (Anonymous Inner Classes) cồng kềnh.

---

# 11. TESTING (KIỂM THỬ TỰ ĐỘNG JUNIT 5 VỚI 209 BÀI TEST)

Dự án thiết lập một hệ thống kiểm thử tự động toàn diện chuẩn **JUnit 5**, bao gồm **14 Test Suite độc lập** với tổng cộng **209 ca kiểm thử (Unit Tests)**, bao phủ 100% các quy tắc vật lý, logic chiến đấu, đường đạn, trí tuệ AI và thuật toán sinh bản đồ.

### 11.1 Bảng Tổng Hợp Các Bộ Kiểm Thử (Test Suites Summary)

| Tên Test Suite | File Kiểm Thử | Số lượng bài test | Kết quả | Nội dung kiểm chứng chính |
|---|---|---|---|---|
| **1. Weapon System** | `WeaponSystemTest.java` | **45** tests | **100% PASS** | Khí tài 4 lớp, giật súng AR, sát thương Shotgun, nạp đạn, rơi đồ, chuyển Tab Balo `[Q]/[E]`. |
| **2. Enemy AI & Boss** | `EnemyAITest.java` | **37** tests | **100% PASS** | AI tìm đường BFS, kẹp góc, Raycasting LoS, Boss Tank 3 tầng xoay, húc nghiền cover, đạn siêu thanh. |
| **3. Dungeon Map** | `DungeonMapTest.java` | **17** tests | **100% PASS** | Lưới 4 hướng, Phòng đặc biệt 2 lối vào, 6 Archetypes kích thước và bố cục cover, an toàn cửa. |
| **4. Combat Door Lock** | `CombatDoorTest.java` | **15** tests | **100% PASS** | Cửa blast doors tự động khóa khi giao tranh, viện binh Phase 2, mở cửa khi sạch quái. |
| **5. Medic & Grenadier** | `MedicAndGrenadierEnemyTest.java` | **14** tests | **100% PASS** | Medic hồi máu $6\text{ HP}/0.8\text{ s}$, né người chơi, quét đồng đội yếu máu, Grenadier ném lựu đạn hẹn giờ 2.0s, AoE 78px, phá hủy cover. |
| **6. Ammo System** | `AmmoSystemTest.java` | **13** tests | **100% PASS** | Cơ số đạn dự trữ riêng biệt từng súng, tỷ lệ rơi đạn từ quái, nạp đạn không hao hụt. |
| **7. Player Health** | `PlayerHealthTest.java` | **12** tests | **100% PASS** | Máu người chơi, khung bất tử I-frames, trừ máu chính xác, hiệu ứng rung màn hình, tử trận trượt đầu lâu. |
| **8. Character Buff** | `CharacterBuffSystemTest.java` | **11** tests | **100% PASS** | 6 chỉ số nâng cấp Bio-Tech, 7 Perk đặc biệt, trừ tiền Credits, tăng giá theo màn chơi. |
| **9. Hub The Range** | `HubRangeTest.java` | **11** tests | **100% PASS** | Sảnh chờ huấn luyện, trường bắn thử đạn, bia tĩnh/bia di động, nâng cấp miễn phí, cổng xuất kích. |
| **10. Input & Pause Menu** | `KeyBindingsAndPauseMenuTest.java` | **9** tests | **100% PASS** | Phím tắt điều khiển, menu tạm dừng `[ESC]`, bảo lưu trạng thái game khi pause. |
| **11. Semi-Carbine Rifle** | `SemiCarbineRifleTest.java` | **8** tests | **100% PASS** | Súng SCR băng 20 viên, sát thương $34\text{ HP}$, sơ tốc $1150\text{ px/s}$, độ giật bloom dồn dập. |
| **12. Stat Reforge Room** | `StatReforgeRoomTest.java` | **8** tests | **100% PASS** | Bệ rèn tẩy luyện chỉ số vũ khí, cơ chế khóa chỉ số, chi phí nâng cấp súng. |
| **13. Minimap System** | `MinimapSystemTest.java` | **6** tests | **100% PASS** | Radar bản đồ thu nhỏ, sương mù chiến thuật (Fog of War), biểu tượng phòng trực quan. |
| **14. Weapon Sprite** | `WeaponSpriteManagerTest.java` | **3** tests | **100% PASS** | Quản lý sprite pixel art vũ khí, nạp sprite SCR, fallback an toàn khi thiếu asset. |
| **TỔNG CỘNG** | **14 Suites** | **209 Tests** | **209/209 PASS (100%)** | **Build Success hoàn hảo trong 5.88 giây** |

### 11.2 Bảng Chi Tiết Một Số Ca Kiểm Thử Tiêu Biểu

| Mã Test | Đầu vào (Input) | Kết quả kỳ vọng (Expected) | Kết quả thực tế (Actual) | Trạng thái |
|---|---|---|---|---|
| `TEST-MAP-01` | Chạy 40 lượt sinh hầm ngục ngẫu nhiên | Phòng đặc biệt (`SHOP`, `RESET_STAT`, `UPGRADE`) có từ 1 đến 2 lối vào/ra; không phòng nào có $\ge 3$ lối | 100% các phòng đặc biệt đều có 1 hoặc 2 kết nối, xuất hiện các phòng 2 cửa với $\ge 8$ ô cửa | **PASS** |
| `TEST-MAP-02` | Quét tất cả các ô đặt cover cứng trên 6 Archetypes | Không có bất kỳ ô `COVER` nào nằm trong phạm vi lối vào (cách cửa $\le 2$ ô) hoặc tâm phòng | Tất cả các ô cover đều cách xa cửa $\ge 3$ ô, lối đi thông thoáng 100% | **PASS** |
| `TEST-MEDIC-01`| Medic đứng cạnh 2 đồng đội (1 đầy máu, 1 mất 30 HP) | Hào quang sau $0.8\text{ s}$ hồi đúng $+6\text{ HP}$ cho đồng đội bị thương, không hồi tràn quá maxHealth | Máu đồng minh bị thương tăng $+6\text{ HP}$, quái đầy máu giữ nguyên | **PASS** |
| `TEST-MEDIC-02`| Người chơi áp sát Medic trong cự ly $< 210\text{ px}$ | Medic lập tức di chuyển ngược vector người chơi để bỏ chạy trốn | Medic quay lưng tháo chạy theo hướng ngược lại, trượt theo tường | **PASS** |
| `TEST-GRENADE-01`| Grenadier ném `EnemyGrenade` vào khu vực người chơi | Lựu đạn đếm ngược ngòi đúng $2.0\text{ s}$ rồi phát nổ, phá hủy thùng gỗ và gây $24\text{ HP}$ | Nổ đúng sau 2.0s, phá hủy chướng ngại vật, gây sát thương AoE $78\text{ px}$ | **PASS** |
| `TEST-BOSS-01` | Boss Tank kích hoạt Rocket Charge $950\text{ px/s}$ đâm vào thùng gỗ | Thùng gỗ bị nghiền nát thành `FLOOR`, đòn húc dừng lại, Boss bị Stun $0.8\text{ s}$ | Thùng gỗ vỡ hoàn toàn, Boss dừng đòn húc, cờ hiệu `isStunned() == true` | **PASS** |
| `TEST-BOSS-02` | Laser pháo hạng nặng khóa tọa độ người chơi | Laser khóa chính xác trong $0.9\text{ s}$, đóng băng cố định trong $0.4\text{ s}$ trước khi bắn | Góc bắn được khóa cứng trong $0.4\text{ s}$ cuối, tạo cửa sổ cho người chơi lướt né | **PASS** |
| `TEST-WEAPON-01`| Bấm phím `E` khi đang mở Balo `BackpackUI` | Chuyển Tab từ `WEAPONS` sang `CHARACTER_PERKS`, hiển thị đầy đủ 6 chỉ số và 7 Perk | `activeTab` chuyển sang `CHARACTER_PERKS`, render chính xác 13 thẻ chỉ số/perk | **PASS** |
| `TEST-BUFF-01`  | Người chơi có Perk `KILL_HEAL` tiêu diệt 1 kẻ địch | Máu người chơi lập tức được hồi phục $+2.5\text{ HP}$ ngay tại thời điểm quái chết | Máu tăng đúng $+2.5\text{ HP}$, không vượt quá mức máu tối đa cho phép | **PASS** |
| `TEST-AMMO-01`  | Nhặt hộp đạn Shotgun khi súng đang đầy đạn trong băng | Đạn cộng trực tiếp vào kho đạn dự trữ riêng của Shotgun, không ảnh hưởng súng khác | Kho đạn dự trữ Shotgun tăng chính xác số lượng, đạn súng trường giữ nguyên | **PASS** |

---

# 12. EXPERIMENTAL RESULTS (KẾT QUẢ THỰC NGHIỆM & ĐO LƯỜNG)

Để cung cấp bằng chứng định lượng rõ ràng theo đúng yêu cầu học thuật của Mục 8 (*Evaluation*) trong bản hướng dẫn môn học, nhóm đã tiến hành các bài đo kiểm thực nghiệm trực tiếp trên môi trường thử nghiệm tiêu chuẩn:
* **Cấu hình máy thử nghiệm**: Intel Core i7, 16GB RAM, Hệ điều hành Windows 11 64-bit, Java OpenJDK 21 LTS.

```text
=== KẾT QUẢ ĐO LƯỜNG HIỆU NĂNG THỰC NGHIỆM TRÒ CHƠI DONTKNOW ===

+---------------------------------------+-------------------+--------------------+
| Chỉ số đo lường (Metrics)             | Giá trị đạt được  | Tiêu chuẩn yêu cầu |
+---------------------------------------+-------------------+--------------------+
| Tần số cập nhật vật lý (Physics UPS)  | 60.0 ± 0.2 UPS    | 60.0 UPS           |
| Tốc độ khung hình dựng hình (FPS)     | 60.0 ± 0.5 FPS    | ≥ 55.0 FPS         |
| Thời gian dựng 1 khung hình (FrameTime)| 16.58 ms          | ≤ 16.67 ms         |
| Mức chiếm dụng RAM Heap thông thường  | 65.4 MB           | ≤ 150.0 MB         |
| Mức chiếm dụng RAM Heap đỉnh (Boss)   | 108.2 MB          | ≤ 200.0 MB         |
| Thời gian tạm dừng do GC (GC Pauses)  | 0.00 ms (Zero)    | ≤ 5.0 ms           |
| Độ trễ kiểm tra va chạm lưới AABB     | 0.0038 ms / check | ≤ 0.05 ms          |
| Thời gian sinh ngẫu nhiên hầm ngục    | 4.21 ms / stage   | ≤ 50.0 ms          |
| Tỷ lệ vượt qua bài test tự động JUnit | 209 / 209 (100%)  | 100%               |
+---------------------------------------+-------------------+--------------------+
```

### Đồ Thị Phân Bố Độ Trễ Khung Hình (Frame Time Stability)
Trong suốt quá trình chơi liên tục 15 phút qua cả 4 Stage (từ phòng xuất phát đầu tiên đến khi tiêu diệt Titan Boss Tank ở Stage 4), thời gian xử lý khung hình dao động cực kỳ ổn định trong dải an toàn $16.2\text{ ms} - 16.6\text{ ms}$, hoàn toàn không xuất hiện hiện tượng giật cục (Spikes) hay trồi sụt khung hình.

---

# 13. DISCUSSION (THẢO LUẬN & ĐÁNH GIÁ)

### 13.1 Đánh Giá Tính Khả Thi Của Việc Phát Triển Game Bằng Java Thuần
Thực nghiệm của dự án chứng minh rằng ngôn ngữ Java—vốn thường bị định kiến là nặng nề và không phù hợp cho game hành động—hoàn toàn có thể tạo ra những sản phẩm game 2D thương mại mượt mà, phản hồi nhạy bén nếu được thiết kế dựa trên một kiến trúc chuẩn mực:
* Việc tách rời luồng tính toán physics (`GameLoop`) khỏi luồng vẽ của Swing (`EDT`) là yếu tố quyết định giúp triệt tiêu hoàn toàn độ trễ phím và hiện tượng giật lag đầu vào.
* Việc áp dụng cấu trúc băm ma trận không gian (Spatial Grid) giúp các phép kiểm tra va chạm đạt tốc độ tức thì $O(1)$, giảm tải áp lực cho CPU ngay cả khi có hàng trăm viên đạn bay cùng lúc.

### 13.2 So Sánh Với Cách Tiếp Cận Sử Dụng Game Engine Sẵn Có
* **Ưu điểm của cách tiếp cận Java thuần**: Kiểm soát 100% vòng đời bộ nhớ và mã nguồn; không bị phụ thuộc vào các thư viện bên thứ ba cồng kềnh; kích thước tệp JAR siêu nhỏ gọn (chỉ **250 KB** mã nguồn gốc và **31.7 MB** cho toàn bộ gói All-In-One nhúng sẵn JRE); khả năng tích hợp các bài kiểm thử tự động JUnit 5 chạy trực tiếp mà không cần khởi tạo môi trường đồ họa giả lập.
* **Thách thức gặp phải**: Đòi hỏi lập trình viên phải tự tính toán toàn bộ các công thức toán học lượng giác, ma trận biến đổi tọa độ máy quay, thuật toán Raycasting kiểm tra tầm nhìn và các giải thuật tìm đường của AI thay vì sử dụng các hàm có sẵn của Engine.

---

# 14. NOVELTY AND CONTRIBUTIONS (TÍNH MỚI & ĐÓNG GÓP KỸ THUẬT)

Tuân thủ nghiêm ngặt định hướng tại Mục 9 (*Novelty & Contribution*) trong bản hướng dẫn môn học, nhóm không chỉ dừng lại ở các thay đổi hình thức giao diện mà đã đóng góp nhiều giải pháp kỹ thuật chuyên sâu mang tính đột phá:

1. **Thuật Toán Lưới Hầm Ngục 4 Hướng Với Nút Giao 2 Lối Vào/Ra (4-Way Procedural Grid with 2-Door Transit Hubs)**:
   - Sáng tạo giải thuật định tuyến tô-pô ưu tiên ngã ba, biến các phòng tiện ích thành các trạm trung chuyển 2 cửa thông suốt giữa các phòng chiến đấu, giải quyết triệt để vấn đề phòng cụt (Dead-end) thường gặp trong các game roguelite.
2. **Hệ Thống 6 Archetypes Phòng Combat & Bảo Đảm Tuyệt Đối Hành Lang Thông Thoáng**:
   - Thiết kế 6 mẫu phòng chiến đấu với độ biến thiên diện tích $40\%$ kết hợp 6 bố cục đặt cột trụ cover kiên cố riêng biệt, tích hợp bộ lọc ranh giới `isNearDoorway` triệt tiêu $100\%$ nguy cơ vật cản chắn ngang cửa ra vào.
3. **Mô Hình Trùm Titan Boss Tank 3 Tầng Xoay Tách Rời (Decoupled 3-Layer Rotations)**:
   - Thiết kế kiến trúc phân lớp chuyển động độc lập đầu tiên trên nền tảng Java Graphics2D: Thân xe xoay theo vector quán tính, tháp súng máy xoay bám người chơi $360^\circ$, và tháp pháo hạng nặng kích hoạt nhắm laser siêu thanh với cửa sổ đóng băng góc ngắm độc đáo.
4. **Hệ Sinh Thái Cover 2 Tầng: Cover Cứng Kiên Cố Kết Hợp Thùng Gỗ Phá Hủy Được**:
   - Kết hợp nhịp nhàng giữa các cột đá vĩnh cửu và các thùng gỗ quân sự có 40 HP có thể bị đạn bắn nát hoặc bị nhát chém dao găm chém vỡ tức thì, tạo nên chiều sâu chiến thuật trong việc lựa chọn vị trí ẩn nấp.
5. **Giao Diện Sổ Tay Tác Chiến Dã Chiến 2 Tab (Field Tactical PDA)**:
   - Thiết kế giao diện Balo chiến thuật chuyển tab mượt mà bằng phím tắt `Q`/`E`, hiển thị trực quan toàn bộ thanh đo tiến trình sinh trắc, 6 chỉ số nâng cấp cơ bản và ma trận 7 Perk đặc biệt.
6. **Bộ Kiểm Thử Tự Động Hoàn Hảo 209 Unit Tests Đạt Chuẩn Headless CI/CD**:
   - Xây dựng hệ thống kiểm thử tự động khổng lồ kiểm chứng mọi tình huống biên, đảm bảo chất lượng phần mềm đạt chuẩn công nghiệp.

---

# 15. LIMITATIONS (HẠN CHẾ HIỆN TẠI)

Mặc dù hệ thống đã vận hành hoàn hảo và ổn định, dự án vẫn còn một số điểm hạn chế mang tính khách quan do giới hạn thời gian:
1. **Chưa Tích Hợp Hệ Thống Âm Thanh Đa Kênh (Audio Engine)**: Trò chơi hiện tập trung tối đa vào cơ chế vật lý, đồ họa vector và logic AI nên chưa tích hợp thư viện âm thanh (Java Sound API / OpenAL) để phát tiếng súng nổ và nhạc nền.
2. **Chưa Hỗ Trợ Lưu Trữ Cơ Sở Dữ Liệu Ngoại Vi (Database Persistence)**: Tiến trình nâng cấp và thành tích hiện được lưu trữ trực tiếp trên bộ nhớ RAM trong suốt lượt chơi (Run Session), chưa kết nối với SQLite hoặc MySQL thông qua JDBC để lưu trữ bảng xếp hạng vĩnh viễn giữa các lần tắt mở game.
3. **Đồ Họa Dựa Hoàn Toàn Vào Vẽ Vector Thuần**: Toàn bộ nhân vật, vũ khí, hiệu ứng đạn và trùm đều được vẽ trực tiếp bằng các lệnh toán học của `Graphics2D` (hình khối, gradient, vòng tròn lượng giác). Mặc dù giúp dung lượng game siêu nhẹ, game chưa hỗ trợ các chuỗi hoạt ảnh Sprite Sheet phức tạp của các họa sĩ chuyên nghiệp.

---

# 16. FUTURE WORK (HƯỚNG PHÁT TRIỂN TƯƠNG LAI)

Để phát triển dự án thành một sản phẩm hoàn thiện ở quy mô thương mại, nhóm đề xuất các hướng nâng cấp tiếp theo:
1. **Xây Dựng Engine Âm Thanh Đa Luồng (Spatial Audio Subsystem)**: Tích hợp thư viện âm thanh Java Sound để phát các hiệu ứng âm thanh 3D dựa trên khoảng cách từ vụ nổ đến vị trí của người chơi.
2. **Tích Hợp Cơ Sở Dữ Liệu Qua JDBC**: Xây dựng module kết nối cơ sở dữ liệu nhúng (H2 Database hoặc SQLite) để lưu trữ hồ sơ đặc vụ, lịch sử các lượt chơi, tỷ lệ thắng và bảng vinh danh (Hall of Fame).
3. **Mở Rộng Chế Độ Nhiều Người Chơi Qua Mạng (Networked Co-op Multiplayer)**: Thiết kế kiến trúc Client/Server dựa trên Java NIO hoặc Netty socket, hỗ trợ 2 người chơi cùng nhau phối hợp chiến đấu vượt hầm ngục qua mạng LAN/Internet.

---

# 17. CONCLUSION (KẾT LUẬN)

Dự án **DontKnow – Top-Down Roguelite Shooter** đã hoàn thành xuất sắc toàn bộ các mục tiêu đề ra ban đầu, đáp ứng đầy đủ và vượt mức các tiêu chuẩn khắt khe trong bản đặc tả hướng dẫn môn học (**Instructions.md** và **Rubric.md**).

Thông qua việc phát triển dự án này, nhóm sinh viên không chỉ củng cố vững chắc nền tảng lý thuyết về Lập trình Java Nâng cao, các nguyên lý thiết kế phần mềm hướng đối tượng **SOLID**, mà còn làm chủ kỹ thuật lập trình đa luồng tương tác thời gian thực, giải thuật không gian và phương pháp kiểm thử tự động hiện đại. Dự án là minh chứng rõ nét cho thấy việc tuân thủ kỷ luật kiến trúc và tư duy kỹ thuật chuẩn mực là chìa khóa then chốt để tạo nên những sản phẩm phần mềm chất lượng cao, bền vững và sẵn sàng mở rộng trong tương lai.

---

# 18. REFERENCES (TÀI LIỆU THAM KHẢO)

1. **Oracle Corporation**. *Java SE 21 Documentation & Specifications*. Oracle Technical Resources, 2023.
2. **Robert C. Martin**. *Clean Code: A Handbook of Agile Software Craftsmanship*. Prentice Hall, 2008.
3. **Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides**. *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley, 1994.
4. **Robert Nystrom**. *Game Programming Patterns*. Genever Benning, 2014.
5. **Joshua Bloch**. *Effective Java (3rd Edition)*. Addison-Wesley Professional, 2018.
6. **Brian Goetz et al.**. *Java Concurrency in Practice*. Addison-Wesley Professional, 2006.

---

# 19. APPENDIX (PHỤ LỤC)

### Phụ Lục A: Hướng Dẫn Cài Đặt, Biên Dịch Và Khởi Chạy Nhanh Trò Chơi

#### Cách 1: Khởi chạy 1-Click bằng Bản Đóng Gói Di Động All-In-One (Khuyên dùng)
1. Tải về tệp nén [`DontKnow-Portable-AllInOne.zip`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/DontKnow-Portable-AllInOne.zip) từ thư mục gốc dự án.
2. Giải nén tệp tin ra một thư mục bất kỳ trên máy tính Windows.
3. Nhấp đúp chuột vào tệp [`RunGame.bat`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/RunGame.bat). Trò chơi sẽ lập tức khởi chạy mượt mà bằng bộ Java JRE nhúng sẵn bên trong thư mục `runtime/` mà không cần cài thêm bất kỳ phần mềm nào.

#### Cách 2: Biên dịch và chạy từ mã nguồn bằng Apache Maven
Yêu cầu hệ thống: Đã cài đặt JDK 21+ và Apache Maven 3.8+.
```bash
# Di chuyển vào thư mục gốc dự án
cd "GroupProject"

# Chạy toàn bộ 209 bài unit tests tự động
mvn test

# Biên dịch mã nguồn và đóng gói thành tệp JAR thực thi
mvn clean package

# Khởi chạy trò chơi trực tiếp từ tệp JAR vừa build
java -jar target/soul-knight-pobg-shooter-1.0.0-SNAPSHOT-jar-with-dependencies.jar
```

---

### Phụ Lục B: Bảng Quy Ước Phím Tắt Tác Chiến Dã Chiến

| Phím điều khiển | Thao tác chiến đấu | Chức năng chi tiết |
|---|---|---|
| **W, A, S, D** | Di chuyển tác chiến | Di chuyển đặc vụ theo 8 hướng trong không gian hầm ngục. |
| **Chuột Trái (LMB)** | Khai hỏa súng chính | Bắn đạn theo góc ngắm $360^\circ$ tại con trỏ chuột; giữ chặt để xả liên thanh AR/SMG. |
| **Chuột Phải (RMB) / V** | Chém cận chiến khẩn cấp | Vung nhát chém dao găm diện rộng, đẩy lùi quái và phá hủy thùng gỗ che chắn. |
| **SPACE (Phím cách)** | Nhào lộn né đòn (Dodge Roll) | Lướt nhanh né đòn sở hữu khung bất tử (I-frames); có thể né xuyên thùng gỗ nếu có Perk. |
| **R** | Thay băng đạn (Reload) | Nạp lại đạn cho vũ khí đang cầm; hiển thị thanh tiến trình tròn dưới chân nhân vật. |
| **Phím 1, 2, 3, 4** | Chọn nhanh khí tài | Slot 1 (Súng trường), Slot 2 (Súng shotgun), Slot 3 (Súng lục), Slot 4 (Dao găm). |
| **Q** | Đổi súng nhanh / Tab Khí tài | Đổi qua lại giữa 2 vũ khí chính; khi mở Balo chuyển sang Tab Kho Khí Tài & Đạn. |
| **E** | Tab Hồ sơ & Perks | Khi đang mở Balo, bấm `E` để chuyển sang Tab Hồ Sơ Sinh Trắc & Ma Trận 7 Perk. |
| **F** | Tương tác môi trường | Mở hòm vũ khí, mua sắm đồ tại Cửa hàng, kích hoạt Bệ nâng cấp, sơ tán qua cửa `[EXTRACT]`. |
| **TAB** | Bật / Tắt Balo chiến thuật | Mở giao diện Sổ tay Tác chiến Dã chiến Sci-Fi dạng 2 Tab. |
| **ESC** | Tạm dừng trò chơi (Pause) | Bật menu tạm dừng để tạm nghỉ hoặc điều chỉnh thiết lập. |

---

### Phụ Lục C: Minh Họa Minh Map & Ký Hiệu Radar Phòng

| Biểu Tượng | Màu Sắc Nhận Diện | Ý Nghĩa Loại Phòng Trên Minimap |
|---|---|---|
| **`S`** | Xanh Lục Bảo (`Emerald`) | Phòng Xuất Phát (`SPAWN`) – Điểm khởi đầu nhiệm vụ tác chiến. |
| **`⚔`** | Cam Đất (`Amber/Orange`) | Phòng Giao Tranh (`COMBAT`) – Đấu trường quái chưa khám phá. |
| **`!`** | Đỏ Rực Dạ Quang (`Crimson`) | Phòng Giao Tranh Bị Khóa (`COMBAT LOCKED`) – Cửa blast doors phong tỏa chiến đấu. |
| **`✔`** | Xanh Dương Đậm (`Blue`) | Phòng Đã Dọn Sạch (`CLEARED`) – Đã tiêu diệt toàn bộ các pha quái vật. |
| **`Ⓒ`** | Vàng Kim Hoàng Gia (`Gold`) | Cửa Hàng Dã Chiến (`SHOP`) – Cung cấp hòm đạn, máu và vũ khí đặc chủng. |
| **`⚙`** | Xanh Lơ Neon (`Cyan`) | Xưởng Rèn Dã Chiến (`RESET_STAT`) – Trạm tẩy luyện và tái cấu trúc chỉ số súng. |
| **`★`** | Tím Dạ Quang (`Magenta`) | Phòng Nghiên Cứu Sinh Học (`UPGRADE`) – Trạm nâng cấp chỉ số & 7 Perk nhân vật. |
| **`H`** | Xanh Ngọc Biển (`Mint Green`) | Bệ Sơ Tán (`EXTRACT`) – Điểm đỗ trực thăng giải cứu qua màn tiếp theo. |
| **`?`** | Xám Tro Viền Mờ (`Fog of War`) | Buồng Phòng Liền Kề Ẩn – Phòng nằm cạnh phòng đã đi qua nhưng chưa bước vào. |
