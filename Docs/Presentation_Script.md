Cập nhật mới nhất vào ngày: 27/10/2026

# KỊCH BẢN THUYẾT TRÌNH BẢO VỆ ĐỒ ÁN CUỐI KỲ: PROJECT DONTKNOW
### ĐỀ TÀI: THIẾT KẾ VÀ PHÁT TRIỂN HỆ THỐNG TRÒ CHƠI BẮN SÚNG ROGUELITE TỪ GÓC NHÌN TRÊN XUỐNG DỰA TRÊN KIẾN TRÚC ĐA LUỒNG VÀ THUẬT TOÁN SINH HẦM NGỤC THỦ TỤC TRÊN NỀN TẢNG JAVA THUẦN
**Nhóm thực hiện**: Group Unknown (Team Morningstar)  
**Môn học**: Lập trình Java Nâng Cao (Advanced Java Programming)  
**Thời lượng dự kiến**: 12 – 15 phút (10 phút thuyết trình + 3-5 phút Live Demo + Phản biện Q&A)

---

## 👥 BẢNG PHÂN VAI & PHÂN CHIA THỜI LƯỢNG (CHUẨN HÓA 4 THÀNH VIÊN)

| Thành viên | Vai trò thuyết trình | Nội dung kỹ thuật phụ trách | Thời lượng |
|---|---|---|---|
| **DƯƠNG** *(Trưởng nhóm)* | **Host & Lead Engine Architect** | Lời chào, Bối cảnh, Đặt vấn đề, Cảm hứng thiết kế, Kiến trúc phân tầng tách rời, Vòng lặp `GameLoop` đa luồng 60 FPS/UPS, 4 Trụ cột OOP, 5 Nguyên lý SOLID, Lưu trữ Checkpoint an toàn, Kiểm thử tự động (249 tests JUnit 5), Tổng kết & Điều phối Q&A. | **3.5 phút** |
| **BẢO** | **Weapons & Character Progression Specialist** | Hệ thống xạ kích $360^\circ$ ($\operatorname{atan2}$), Mô hình đường cong giật nở tâm, Hệ thống đạn đạo vật lý, 4 Kho đạn dự trữ dã chiến, Bàn Nâng cấp súng 3 nhánh (DMG/RPM/ACC), Trạm Tẩy luyện chỉ số súng, và Hệ thống Nâng cấp Sinh trắc học Bio-Tech (6 chỉ số + 7 Perk). | **2.5 phút** |
| **HOÀNG ANH** | **Procedural Map & Optimization Specialist** | Thuật toán Sinh hầm ngục thủ tục (Bounded Grid Tree ma trận $4 \times 3$), Nút giao đa lối đi (Transit Crossroads), 6 Archetypes kích thước phòng & bố cục cover kiên cố, Bộ đôi thuật toán tối ưu hóa đồ họa: Viewport Culling & Map Surface Baking, Texture Pre-scaling 1:1, Bù giáp mí 1px và Băm không gian ma trận (Spatial Grid $O(1)$). | **2.5 phút** |
| **PHAN ANH** | **Enemy AI & Tactical UI Specialist** | Trí tuệ nhân tạo (AI) Đa tầng (BFS Pathfinding + 10 cảm biến xúc tu né vật cản, Quái Quân Y Medic, Quái Công Binh Grenadier), Thiết kế Trùm Titan Tank 3 tầng xoay độc lập, Hệ thống Giao diện Tác chiến UI (PlayingHUD, Balo 2 Tab PDA, Radar Minimap, Boss Intro Cinematic) và Điều phối Live Demo 3 phút. | **3.0 phút** |
| **Cả nhóm** | **Q&A Defense** | Phối hợp trả lời câu hỏi phản biện của Giảng viên / Hội đồng theo đúng chuyên môn phụ trách. | **3 – 5 phút** |

---

## 🎬 KỊCH BẢN LỜI THOẠI CHI TIẾT (WORD-FOR-WORD SPOKEN SCRIPT)

---

### PHẦN 1: MỞ ĐẦU, BỐI CẢNH & KIẾN TRÚC LÕI (Thành viên 1 - Trưởng nhóm trình bày)

> ⏱️ **Thời gian ước tính**: 00:00 – 03:00 (3.0 phút)  
> 🎯 **Mục tiêu**: Thu hút sự chú ý của Hội đồng, khẳng định tính học thuật và sự phức tạp của đồ án khi phát triển hoàn toàn trên Java thuần.

#### [SLIDE 1: TRANG TIÊU ĐỀ BÁO CÁO]
*(Phong thái tự tin, đứng thẳng, giao tiếp ánh mắt với Hội đồng)*

> **Thành viên 1 (Trưởng nhóm) nói:**  
> "Kính thưa quý Thầy/Cô trong Hội đồng chấm đồ án và toàn thể các bạn sinh viên!  
> Lời đầu tiên, cho phép em xin đại diện cho **Nhóm Morningstar** gửi tới quý Thầy/Cô lời chúc sức khỏe và lời chào trân trọng nhất.  
> 
> Hôm nay, nhóm chúng em rất vinh hạnh được đứng tại đây để báo cáo đồ án môn học Lập trình Java Nâng cao với đề tài: **'Thiết kế và Phát triển Hệ thống Trò chơi Bắn súng Roguelite góc nhìn trên xuống DontKnow dựa trên Kiến trúc Đa luồng và Thuật toán Sinh hầm ngục Thủ tục trên nền tảng Java thuần'**."

---

#### [SLIDE 2: ĐẶT VẤN ĐỀ & ĐỘNG LỰC NGHIÊN CỨU]
*(Chỉ tay vào các biểu đồ thách thức kỹ thuật trên màn hình)*

> **Thành viên 1 (Trưởng nhóm) nói:**  
> "Kính thưa Thầy/Cô, trong ngành công nghiệp game hiện nay, phần lớn các lập trình viên thường chọn sử dụng các Game Engine có sẵn như Unity, Unreal Engine hay Godot. Dù rất tiện lợi, các engine này vô tình che giấu đi toàn bộ bản chất kiến trúc cốt lõi bên dưới: từ việc cấp phát bộ nhớ, xử lý luồng, đến việc kiểm soát chu kỳ cập nhật vật lý.  
> 
> Nhóm chúng em quyết định đón nhận thử thách lớn hơn: **Tự xây dựng một Game Engine 2D hoàn chỉnh từ con số không (From Scratch) bằng ngôn ngữ Java 21 LTS thuần túy và thư viện đồ họa Swing/Graphics2D**, hoàn toàn không sử dụng bất kỳ thư viện game engine bên thứ ba nào.  
> 
> Khi làm việc với Java Swing truyền thống, chúng em phải đối mặt với 3 bài toán kỹ thuật hóc búa:
> 1. Mô hình đơn luồng của Swing dựa trên **Event Dispatch Thread (EDT)** rất dễ bị nghẽn, dẫn đến hiện tượng trôi phím, đơ chuột khi có hàng trăm viên đạn và quái vật cùng xuất hiện.
> 2. Nguy cơ thoái hóa mã nguồn thành các lớp khổng lồ (**God Objects**) khi game chứa quá nhiều trạng thái phức tạp.
> 3. Hiện tượng xé hình (**Screen Tearing**) và độ trễ kiểm tra va chạm không gian."

---

#### [SLIDE 3: CẢM HỨNG THIẾT KẾ & ĐỊNH VỊ SẢN PHẨM]
*(Chuyển slide, làm nổi bật hai tựa game truyền cảm hứng)*

> **Thành viên 1 (Trưởng nhóm) nói:**  
> "Để giải quyết bài toán trên và mang lại trải nghiệm tương tác hấp dẫn nhất, chúng em đã học tập và kết hợp tinh hoa từ hai tượng đài game 2D:
> * Từ **Soul Knight**: Chúng em kế thừa cơ chế khám phá hầm ngục ngẫu nhiên sinh theo thủ tục, hệ thống cửa phòng tự động phong tỏa giao tranh đa pha, và tính bất định cuốn hút của thể loại Roguelite.
> * Từ **Surviv.io**: Chúng em tiếp thu cơ chế xạ kích chiến thuật $360^\circ$ cực kỳ mượt mà theo con trỏ chuột, đường cong giật súng nở tâm, cơ chế nạp đạn và quản lý hộp đạn dự trữ phân loại dã chiến.
> 
> Kết quả là **Project DontKnow** ra đời — vừa là một trò chơi hành động kịch tính, vừa là một **bản mẫu kiến trúc phần mềm kiểu mẫu (Architectural Reference)**, vận hành mượt mà ở chuẩn **60 FPS / 60 UPS** với thời gian phản hồi khung hình lý tưởng $\approx 16.6\text{ ms}$."

---

#### [SLIDE 4: KIẾN TRÚC HỆ THỐNG PHÂN TẦNG TÁCH RỜI (DECOUPLED ARCHITECTURE)]
*(Trỏ trực tiếp vào sơ đồ luồng dữ liệu hai luồng song song)*

> **Thành viên 1 (Trưởng nhóm) nói:**  
> "Trọng tâm kỹ thuật đầu tiên của dự án là **Kiến trúc Vòng lặp Game Tách rời (Decoupled Game Loop Architecture)**.  
> Thay vì dùng `javax.swing.Timer` vốn bị ràng buộc trên luồng giao diện, chúng em khởi tạo một luồng tính toán độc lập (**Worker Simulation Thread**). Luồng này chạy chu kỳ cố định đúng **60.0 lần/giây (Fixed Timestep 60 UPS)** để tính toán động học vật lý, AI tìm đường và va chạm đạn đạo.  
> Trong khi đó, luồng **Swing EDT** chỉ tập trung duy nhất vào nhiệm vụ kết xuất đồ họa với tần số **60 FPS** thông qua cơ chế **Double Buffering** và thuật toán **Viewport Culling**.  
> Sự phân tách này đảm bảo trò chơi không bao giờ bị đơ phím hay khựng hình ngay cả khi xử lý các pha giao tranh diện rộng."

---

#### [SLIDE 5: VẬN DỤNG OOP, SOLID VÀ LƯU CHECKPOINT AN TOÀN]
*(Trình chiếu bảng ma trận SOLID và cấu trúc lưu checkpoint)*

> **Thành viên 1 (Trưởng nhóm) nói:**  
> "Về mặt thiết kế mã nguồn, toàn bộ 25 gói package trong dự án tuân thủ nghiêm ngặt 4 trụ cột OOP và 5 nguyên lý **SOLID**:
> * Chúng em áp dụng nguyên lý **Đơn trách nhiệm (SRP)**: Lớp `DungeonMap` chỉ quản lý địa hình; `ProjectileManager` chỉ quản lý vòng đời đạn; `Camera` chỉ xử lý ma trận dời hình tọa độ.
> * Nguyên lý **Đóng/Mở (OCP)**: Lớp trừu tượng `Weapon` và `Enemy` cho phép mở rộng thêm bất kỳ loại súng hay quái vật mới nào mà không cần sửa đổi mã nguồn của Engine.
> * Chúng em kết hợp nhuần nhuyễn các **Design Patterns** kinh điển: **State Pattern** quản lý vòng đời game mượt mà (`PlayingState`, `PauseState`, `MenuState`); **Strategy Pattern** thay đổi hành vi cơ động của AI; **Factory Method** sinh ngẫu nhiên hòm trang bị; và **Observer Pattern** kích hoạt các chuỗi sự kiện khi quái vật bị tiêu diệt.
> * Ngoài ra, hệ thống tích hợp **Lưu tiến trình Checkpoint tự động (`RunSaveManager`)**: Cho phép người chơi thoát game bất kỳ lúc nào và '★ TIẾP TỤC CHƠI' với đầy đủ súng đạn, máu và bùa chú; đồng thời kiểm tra mã `GAME_BUILD_VERSION` để tự hủy save an toàn khi game cập nhật phiên bản mới.
> 
> Và ngay sau đây, em xin kính mời bạn **[Tên Thành viên 2]** sẽ trình bày về Hệ thống Vũ khí, Cơ chế Đạn đạo và Hệ sinh thái Nâng cấp Nhân vật của trò chơi. Xin mời bạn!"

*(Thành viên 1 gật đầu chuyển lời, Thành viên 2 bước lên bục thuyết trình)*

---

### PHẦN 2: HỆ THỐNG VŨ KHÍ & NÂNG CẤP NHÂN VẬT (Thành viên 2 trình bày)

> ⏱️ **Thời gian ước tính**: 03:00 – 05:30 (2.5 phút)  
> 🎯 **Mục tiêu**: Làm nổi bật cơ chế vật lý súng đạn chân thực lấy cảm hứng từ Surviv.io, độ nở tâm và hệ thống nâng cấp nhân vật Bio-Tech 6 chỉ số & 7 Perk.

#### [SLIDE 6: XẠ KÍCH 360 ĐỘ & MÔ HÌNH VẬT LÝ ĐẠN ĐẠO THỜI GIAN THỰC]
*(Tự tin, chỉ vào hình ảnh hồng tâm chuột và các tia đạn vệt sáng)*

> **Thành viên 2 nói:**  
> "Em xin kính chào quý Thầy/Cô! Em là **[Tên Thành viên 2]**, phụ trách kỹ thuật hệ thống **Vũ khí và Nâng cấp nhân vật** trong dự án.  
> 
> Trải nghiệm cốt lõi của *DontKnow* được xây dựng xoay quanh cơ chế **Xạ kích Tự do 360 độ**.  
> Thông qua giải thuật lượng giác $\theta = \operatorname{atan2}(dy, dx)$, nòng súng của nhân vật liên tục bám sát tọa độ con trỏ chuột thời gian thực.  
> 
> Điểm đặc sắc ở đây là chúng em đã mô phỏng thành công **Đường cong Giật súng Nở tâm (Dynamic Bloom Recoil Dispersion)**:
> * Khi người chơi bắn liên thanh (như súng trường Viper AR hay tiểu liên Spectre SMG), góc tản mát của nòng súng sẽ nở rộng dần theo từng phát bắn, làm giảm độ chuẩn xác.
> * Nếu người chơi ngừng bắn, tâm ngắm sẽ lập tức hồi phục về trạng thái ổn định ban đầu.
> * Súng bắn tỉa **Sniper Rifle** sở hữu hệ thống **Pierce Power (Sức xuyên phá)**, cho phép một phát đạn xuyên thẳng qua từ 2 đến 5 kẻ địch trên cùng đường ngắm.
> * Súng hoa cải **Shotgun** tính toán phân mảnh đa đầu đạn hình nón (Cone Dispersion), mang lại cảm giác bắn dã chiến chân thực như trong các tựa game sinh tồn hàng đầu.
> * Hệ thống thiết lập **4 kho đạn dự trữ riêng biệt**: Đạn súng trường (RIF), Đạn tiểu liên (SMG), Đạn hoa cải (SG) và Đạn súng ngắm (SR), buộc người chơi phải liên tục di chuyển nhặt đạn rơi từ quái vật."

---

#### [SLIDE 7 & 8: TIẾN TRÌNH VŨ KHÍ & HỆ THỐNG NÂNG CẤP SINH TRẮC HỌC BIO-TECH]
*(Trỏ vào hình ảnh Bàn Nâng Cấp, Bàn Tẩy Luyện và Bảng Perk Bio-Tech)*

> **Thành viên 2 nói:**  
> "Song hành cùng kho khí tài là chiều sâu tiến trình nâng cấp vượt trội:
> * **Bàn Nâng Cấp Quân Khí Dã Chiến (Field Gunsmith)**: Cho phép cường hóa vũ khí lên tới **Cấp 10**. Ở mỗi mốc cấp độ, người chơi được tự do lựa chọn 1 trong 3 nhánh tiến hóa: Tăng Sát thương thuần (DMG), Tăng Tốc độ xả đạn (RPM), hoặc Tăng Độ chính xác & Sức xuyên (ACC/Pierce).
> * **Trạm Tẩy Luyện Chỉ Số (Stat Reforge)**: Cung cấp cơ chế reroll độc lập: Reroll sao chỉ số (1 đến 5 sao) hoặc Reroll tỉ lệ/hệ số chí mạng mà vẫn bảo lưu nguyên vẹn cấp độ súng.
> * **Trường Bắn Thử Nghiệm (The Range)**: Căn cứ tập huấn với bia hình nộm đo chỉ số DPS thực tế, giúp người chơi thử nghiệm độ giật của từng khẩu súng trước khi bước vào hầm ngục.
> * **Hệ Thống Nâng Cấp Sinh Trắc Học Bio-Tech**:
>   - Trạm Bio-Tech cung cấp **6 nhánh nâng cấp chỉ số cơ bản**: Máu tối đa, Sát thương công kích, Tốc độ di chuyển, Giáp giảm thương, Hồi né đòn và May mắn nhặt đồ.
>   - Đi kèm là **7 Perk kỹ năng sinh tồn độc bản**: Giáp chặn đòn, Diệt địch hồi máu, Né xuyên vật cản, Thu nhỏ hitbox, Băng đạn định kỳ, Băng đạn mở rộng và Bậc thầy chí mạng.
> 
> Tiếp theo sau đây, xin kính mời bạn **[Tên Thành viên 3]** sẽ trình bày về Thuật toán sinh hầm ngục ngẫu nhiên và các kỹ thuật tối ưu hóa đồ họa chuyên sâu. Xin mời bạn!"

*(Thành viên 2 chuyển micro cho Thành viên 3)*

---

### PHẦN 3: MAP & THUẬT TOÁN TỐI ƯU HIỆU NĂNG (Thành viên 3 trình bày)

> ⏱️ **Thời gian ước tính**: 05:30 – 08:00 (2.5 phút)  
> 🎯 **Mục tiêu**: Thể hiện chiều sâu giải thuật đồ thị, phòng chuyển tiếp đa lối đi và bộ đôi kỹ thuật tối ưu hóa Viewport Culling & Map Surface Baking.

#### [SLIDE 9: THUẬT TOÁN SINH HẦM NGỤC THỦ TỤC TRÊN LƯỚI 4 HƯỚNG (PROCEDURAL 4-WAY DUNGEON)]
*(Chỉ vào sơ đồ ma trận lưới tô-pô hầm ngục và các phòng đặc biệt)*

> **Thành viên 3 nói:**  
> "Kính chào quý Thầy/Cô! Em là **[Tên Thành viên 3]**, phụ trách **Thế giới Hầm ngục và các Thuật toán Tối ưu hóa Hiệu năng** trong dự án.  
> 
> Thách thức lớn nhất của một trò chơi Roguelite thời gian thực là làm sao để mỗi màn chơi đều được sinh ngẫu nhiên, hấp dẫn nhưng **thời gian sinh map phải cực nhanh và việc kết xuất thế giới rộng lớn không được làm sụt giảm FPS**.  
> 
> Nhóm chúng em đã phát triển **Thuật toán Sinh Hầm Ngục Thủ Tục trên Lưới Logic 4 Hướng (Bounded Grid Tree)**:
> * Thay vì dùng thuật toán đào hầm ngẫu nhiên tự do (Random Walk) vốn dễ gây nghẽn đệ quy và bít đường, chúng em trừu tượng hóa thế giới thành một **Ma trận lưới $4 \times 3$ buồng phòng chuẩn mực**.
> * Các phòng kết nối trực giao 4 hướng chính: 12h, 3h, 6h, 9h qua hành lang rộng 4 ô tile. Thời gian sinh toàn bộ hầm ngục hoàn tất trong **chưa đầy $2\text{ ms}$**, triệt tiêu 100% rủi ro đệ quy vô hạn hay bít đường.
> * **Giải pháp đột phá — Nút Giao Đa Lối Đi (Transit Crossroads)**: Tại các phòng tiện ích (Cửa hàng, Xưởng Quân Khí, Phòng Nâng Cấp), thuật toán tự động mở tối đa **2 lối vào/ra thông suốt**, biến các phòng này thành trạm trung chuyển xuyên suốt hầm ngục, loại bỏ hoàn toàn tình trạng ngõ cụt gây khó chịu cho người chơi.
> * Ngoài ra, các phòng chiến đấu biến thiên qua **6 Archetypes kích thước** khác nhau ($12 \times 9$ đến $14 \times 10$) kết hợp cùng **6 bố cục cột che chắn kiên cố (Hard Covers)**, và cơ chế **Cửa Blast Doors tự động khóa chặt** khi giao tranh nổ ra."

---

#### [SLIDE 10: BỘ ĐÔI THUẬT TOÁN TỐI ƯU ĐỒ HỌA: VIEWPORT CULLING & MAP SURFACE BAKING]
*(Chỉ vào sơ đồ công thức Viewport Culling và hình ảnh minh họa cơ chế nướng nền Baking)*

> **Thành viên 3 nói:**  
> "Để đảm bảo bản đồ rộng $3840 \times 2160\text{ px}$ gồm 3.600 ô gạch luôn chạy mượt mà ở chuẩn 60 FPS, chúng em áp dụng **bộ đôi thuật toán tối ưu hóa đồ họa kinh điển**:
> 
> 🔹 **1. Thuật toán Cắt Tỉa Khung Nhìn (Viewport Culling & Void Culling)**:  
> * Màn hình người chơi chỉ là $1280 \times 720\text{ px}$. Thay vì duyệt vẽ toàn bộ 3.600 ô gạch mỗi frame gây lãng phí CPU, giải thuật **Viewport Culling** sử dụng tọa độ thời gian thực của Camera $(CamX, CamY)$ để tính toán dải ô khả kiến `[startCol..endCol]` và `[startRow..endRow]`.  
> * Số lượng ô gạch cần duyệt vẽ giảm ngoạn mục từ 3.600 ô xuống chỉ còn khoảng ~980 ô (**giảm hơn 72% khối lượng render**).  
> * Đồng thời, kỹ thuật **Void Culling** lập tức bỏ qua 100% các ô hư vô `TileType.VOID` bên ngoài buồng phòng, loại bỏ hoàn toàn các phép biến đổi ma trận `worldToScreen` thừa thãi.
> 
> 🔹 **2. Kỹ Thuật 'Nướng' Bề Mặt Màn Chơi (Map Surface Baking & Pre-Scaled VRAM Caching)**:  
> * **Kỹ thuật Surface Baking (Nướng bề mặt)**: Tại sảnh The Range, nếu mỗi frame đều phải lặp qua vẽ từng bờ tường gạch, rào chắn và hàng trăm cụm cỏ trang trí ngẫu nhiên thì CPU sẽ bị nghẽn.  
>   Do đó, nhóm sử dụng kỹ thuật **Map Baking**: Toàn bộ lưới gạch nền và chi tiết tĩnh được vẽ một lần duy nhất lúc khởi tạo lên một tấm ảnh đệm ngoài màn hình, sau đó chuyển đổi bằng phương thức `toCompatibleImage()` thành **ảnh đệm tăng tốc phần cứng tương thích trực tiếp với VRAM card màn hình**.  
>   Khi chơi, mỗi frame hệ thống chỉ cần gọi một lệnh **BitBlt (Bit Block Transfer)** sao chép nguyên khối pixel từ VRAM lên màn hình trong độ phức tạp $O(1)$ gần như $0\text{ ms}$!  
> * **Kỹ thuật Pre-Scaled Textures 1:1**: Toàn bộ các texture gạch đá, mặt đất, thảm cỏ, thùng gỗ đều được scale sẵn đúng $49 \times 49\text{ px}$ trong VRAM, vẽ 1-pass không overdraw, loại bỏ hoàn toàn chi phí co giãn ảnh khi đang chơi.  
> * **Kỹ thuật Bù Giáp Mí Subpixel (1px Seam Prevention)**: Kích thước vẽ được bù thêm 1px (`drawSize = size + 1`), triệt tiêu vĩnh viễn các khe nứt rãnh đen rạn giữa các ô gạch do sai số làm tròn số thực khi Camera lướt mượt.  
> * **Băm Không Gian Ma Trận (Spatial Grid Indexing $O(1)$)**: Truy xuất ô gạch bằng phép chia nguyên `(x / 48, y / 48)`, kiểm tra va chạm địa hình chỉ mất $< 0.005\text{ ms}$.
> 
> Và ngay sau đây, xin kính mời bạn **[Tên Thành viên 4]** sẽ trình bày về Trí tuệ nhân tạo của Kẻ địch, Hệ thống Giao diện Tác chiến và điều phối phần Live Demo. Xin mời bạn!"

*(Thành viên 3 chuyển micro cho Thành viên 4)*

---

### PHẦN 4: TRÍ TUỆ NHÂN TẠO KẺ ĐỊCH & HỆ THỐNG GIAO DIỆN TÁC CHIẾN (Thành viên 4 trình bày)

> ⏱️ **Thời gian ước tính**: 08:00 – 11:00 (3.0 phút)  
> 🎯 **Mục tiêu**: Thể hiện trí tuệ AI đa tầng, thiết kế Trùm Titan Tank 3 tầng xoay độc lập, giao diện Balo 2 Tab PDA và điều phối Live Demo thực tế.

#### [SLIDE 11 & 12: TRÍ TUỆ NHÂN TẠO ĐA TẦNG, TRÙM TITAN TANK & HỆ THỐNG GIAO DIỆN UI]
*(Chỉ vào hình ảnh quái Medic hào quang đỏ, sơ đồ 3 lớp xoay của Boss Tank và giao diện Balo 2 Tab)*

> **Thành viên 4 nói:**  
> "Kính chào quý Thầy/Cô! Em là **[Tên Thành viên 4]**, phụ trách **Trí tuệ nhân tạo (AI) Kẻ địch và Hệ thống Giao diện Tác chiến (UI)**.  
> 
> Trong *DontKnow*, kẻ địch không hề di chuyển ngây thơ mà phối hợp tác chiến với nhau rất chặt chẽ:
> * **AI Tìm đường**: Quái cận chiến sử dụng giải thuật **BFS Pathfinding trên lưới ô tile** kết hợp cụm **10 cảm biến xúc tu đa hướng (Multi-Angle Steering Feelers)** lệch góc từ $\pm 25^\circ$ đến $\pm 115^\circ$. Nhờ đó, quái vật luôn trượt mượt mà men theo mép tường, không bao giờ bị kẹt góc đá.
> * **Quái xạ thủ**: Áp dụng cơ chế thả diều (Kiting) lùi lại khi bị áp sát, lạng sườn (Strafing) và tự động đảo chiều khi va chạm tường.
> * **Quái Quân Y (Medic)**: Sở hữu hào quang nano đỏ hồi máu $6\text{ HP}/0.8\text{ s}$, tự động bỏ chạy khi người chơi áp sát và quét tìm cứu thương cho đồng đội yếu máu nhất.
> * **Quái Công Binh (Grenadier)**: Ném lựu đạn hẹn giờ đúng $2.0\text{ s}$ nổ diện rộng AoE $78\text{ px}$, phá hủy mọi chướng ngại vật và ép góc người chơi.
> * **Đỉnh cao thiết kế — Trùm cuối Titan Tank**:
>   - Được cấu thành từ **3 Tầng Xoay Độc Lập**: Lớp gầm xe xoay theo hướng di chuyển; Tháp pháo súng máy xoay $360^\circ$ bám đuổi người chơi; và Tháp pháo hạng nặng kích hoạt tia laser ngắm chuẩn xác trước khi phóng đạn xung kích siêu thanh.
>   - Đòn húc **Rocket Charge $950\text{ px/s}$** nghiền nát toàn bộ thùng gỗ che chắn trên đường lao, tạo nên các pha né đòn đầy kịch tính.
> * **Hệ Thống Giao Diện Tác Chiến (UI/HUD)**:
>   - HUD trực quan hiển thị máu, đạn và thanh hồi né đòn (i-frames).
>   - **Giao diện Balo tác chiến 2 Tab (Field Tactical PDA)**: Tab 1 quản lý 4 slot súng và đạn dự trữ; Tab 2 theo dõi hồ sơ 7 Perk và chỉ số sinh tồn.
>   - Radar Minimap tích hợp sương mù khám phá (Fog of War) và hoạt cảnh cắt cảnh giới thiệu Boss (Letterbox Boss Intro)."

---

#### [SLIDE 13: TRÌNH DIỄN TRỰC TIẾP TRÒ CHƠI (LIVE GAMEPLAY DEMO - 3 PHÚT)]
*(Thành viên 4 ra hiệu cho người cầm máy bắt đầu chơi trực tiếp trên màn chiếu, thuyết minh ăn khớp 100% theo thao tác)*

> **Thành viên 4 nói (thuyết minh đồng bộ theo từng thao tác của người chơi):**  
> "Và ngay sau đây, kính mời quý Thầy/Cô và các bạn cùng theo dõi màn **Trình diễn Trực tiếp Trò chơi (Live Gameplay Demo)** của nhóm chúng em!
> 
> 🎮 **Thao tác 1 (Menu & The Range)**:  
> **(Trên màn chiếu: Người chơi chọn 'TRƯỜNG BẮN DÃ CHIẾN')**  
> 'Trước tiên, người chơi xuất hiện tại sảnh chờ The Range. Các Thầy/Cô có thể thấy nòng súng xoay $360^\circ$ theo chuột rất mượt mà. Khi bạn em xả liên thanh khẩu Viper AR vào bia hình nộm, tâm ngắm nở rộng ra do độ giật, và màn hình hiển thị trực tiếp chỉ số DPS thực tế.'
> 
> 🎮 **Thao tác 2 (Bước vào Hầm ngục & Combat Lockdown)**:  
> **(Trên màn chiếu: Người chơi bước qua cổng dịch chuyển vào Stage 1, đi vào phòng quái)**  
> 'Bây giờ, chúng ta bước vào hầm ngục. Khi bước vào phòng giao tranh, toàn bộ cửa blast doors lập tức sập xuống phong tỏa căn phòng! Các cổng cảnh báo Spawn Telegraph lóe sáng và triệu hồi quái vật. Người chơi sử dụng phím Space để lướt né đòn với khung bất tử I-frames, đồng thời tiêu diệt quái Quân Y trước để ngăn nó hồi máu cho đồng đội.'
> 
> 🎮 **Thao tác 3 (Tương tác Trạm Dã Chiến & Balo 2 Tab)**:  
> **(Trên màn chiếu: Dọn sạch phòng, cửa mở ra, đi sang phòng Shop và trạm Bio-Tech, nhấn TAB mở Balo)**  
> 'Sau khi dọn sạch phòng, cửa tự động mở. Người chơi mở Balo với phím TAB: Tab 1 quản lý vũ khí, Tab 2 theo dõi các Perk sinh trắc học. Tại trạm Bio-Tech, chúng em nâng cấp chỉ số Máu và tốc độ di chuyển.'
> 
> 🎮 **Thao tác 4 (Chạm trán Trùm Titan Tank)**:  
> **(Trên màn chiếu: Người chơi tiến vào phòng Trùm, cắt cảnh Boss Intro hiện lên uy lực)**  
> 'Và đây là đỉnh điểm của chiến dịch: Trùm cuối Titan Tank xuất hiện! Các Thầy/Cô có thể thấy 3 tầng xoay độc lập của xe tăng hoạt động hoàn hảo: Tháp pháo chính liên tục bám theo người chơi trong khi thân xe ủi nát các thùng gỗ che chắn bằng cú húc rocket. Một trải nghiệm chiến đấu vô cùng nghẹt thở!  
> 
> Sau đây, em xin nhường lại phần tổng kết cho bạn Trưởng nhóm ạ!'"

*(Thành viên 4 chuyển lại micro cho Thành viên 1 - Trưởng nhóm)*

---

### PHẦN 5: KIỂM THỬ TỰ ĐỘNG, ĐÓNG GÓI & KẾT LUẬN (Thành viên 1 - Trưởng nhóm trình bày)

> ⏱️ **Thời gian ước tính**: 11:00 – 12:30 (1.5 phút)  
> 🎯 **Mục tiêu**: Khẳng định độ tin cậy tuyệt đối qua 249 Unit Tests JUnit 5, hiệu năng thực nghiệm chuẩn 60 FPS và mở đầu phiên hỏi đáp phản biện.

#### [SLIDE 14: KIỂM THỬ TỰ ĐỘNG JUNIT 5 & HIỆU NĂNG THỰC NGHIỆM]
*(Trưởng nhóm chỉ vào ảnh chụp kết quả kiểm thử xanh mướt của Maven)*

> **Thành viên 1 (Trưởng nhóm) nói:**  
> "Kính thưa Thầy/Cô, để đảm bảo chất lượng phần mềm đạt tiêu chuẩn cao nhất, chúng em không chỉ kiểm thử thủ công mà đã xây dựng bộ kiểm thử tự động toàn diện trên nền tảng **JUnit 5**:
> * Tổng cộng **249 Unit Tests độc lập**, bao phủ từ logic va chạm, đạn đạo, thuật toán AI, cơ chế lưu save, đến hệ thống quản lý phông chữ.
> * Toàn bộ **249/249 bài test đều đạt tỷ lệ vượt qua tuyệt đối: 100% PASS**!
> * Về mặt hiệu năng thực nghiệm: Trò chơi duy trì ổn định chuẩn **60.0 FPS / 60.0 UPS**; mức tiêu thụ bộ nhớ Heap RAM cực kỳ tối ưu, chỉ dao động từ **$65\text{ MB}$ đến $110\text{ MB}$**; và đạt chỉ số **Zero GC Hitching** (không bị khựng khung hình do dọn rác Java).
> * Ứng dụng đã được đóng gói thành tệp phát hành **Portable All-In-One nhúng sẵn JRE**, người dùng chỉ cần nhấp đúp vào tệp `.bat` là có thể chơi ngay trên mọi máy tính Windows mà không cần cài đặt môi trường phát triển."

---

#### [SLIDE 15: KẾT LUẬN & PHIÊN PHẢN BIỆN HỎI - ĐÁP (Q&A)]
*(Cả 4 thành viên cùng bước lên đứng trước Hội đồng)*

> **Thành viên 1 (Trưởng nhóm) nói:**  
> "Kính thưa quý Thầy/Cô, dự án *DontKnow* là minh chứng rõ nét cho thấy: Ngay cả trên nền tảng Java Swing thuần túy, nếu áp dụng đúng đắn các nguyên lý Kiến trúc Phần mềm, Lập trình Đa luồng và Thiết kế Hướng đối tượng chuẩn mực, chúng ta hoàn toàn có thể xây dựng nên một hệ thống tương tác thời gian thực đạt độ mượt mà và tính hoàn thiện cao.  
> 
> Thay mặt nhóm Morningstar, em xin chân thành cảm ơn quý Thầy/Cô đã chú ý lắng nghe bài thuyết trình của nhóm chúng em.  
> Sau đây, nhóm chúng em xin sẵn sàng lắng nghe những nhận xét quý báu và giải đáp toàn bộ câu hỏi phản biện của quý Thầy/Cô ạ!"

*(Cả nhóm cúi đầu chào lịch sự, sẵn sàng cho phần Q&A)*

---

## 🎯 BỘ CÂU HỎI & TRẢ LỜI PHẢN BIỆN HỘI ĐỒNG (Q&A DEFENSE CHEAT SHEET)

Dưới đây là 6 câu hỏi trọng tâm mà Hội đồng thường chất vấn đối với đồ án Game Java, kèm gợi ý phân công người trả lời theo đúng 4 vai trò:

### ❓ Câu hỏi 1: "Tại sao nhóm không dùng `javax.swing.Timer` cho đơn giản mà lại phải tự viết `GameLoop` đa luồng trên một Worker Thread riêng?"
* **Người trả lời ưu tiên**: **Thành viên 1 (Trưởng nhóm / Lead Engine Architect)**.
* **Câu trả lời chuẩn**:
  > *"Dạ thưa Thầy/Cô, `javax.swing.Timer` hoạt động bằng cách bắn sự kiện trực tiếp vào hàng đợi của luồng giao diện Event Dispatch Thread (EDT). Nếu một chu kỳ tính toán vật lý, kiểm tra va chạm của hàng trăm viên đạn hoặc thuật toán AI tìm đường BFS bị quá tải, nó sẽ chiếm dụng luồng EDT khiến toàn bộ cửa sổ bị treo và các sự kiện bàn phím/chuột bị hoãn xử lý (Input Stuttering).*  
  > *Bằng cách tách thành một **Worker Thread riêng biệt chạy chu kỳ cố định 60 UPS**, toàn bộ logic mô phỏng được tính toán song song, độc lập hoàn toàn với tốc độ dựng hình của luồng EDT. Nhờ đó, ngay cả khi đồ họa có biến động, logic vật lý và khả năng phản hồi thao tác của người chơi vẫn luôn đạt chuẩn 60.0 lần/giây ổn định tuyệt đối ạ."*

---

### ❓ Câu hỏi 2: "Mô hình giật súng nở tâm và hệ thống nâng cấp vũ khí 3 nhánh được tính toán như thế nào?"
* **Người trả lời ưu tiên**: **Thành viên 2 (Weapons & Character Progression Specialist)**.
* **Câu trả lời chuẩn**:
  > *"Dạ thưa Thầy/Cô, góc lệch đạn $\Delta\theta$ được tích lũy theo từng phát bắn liên tục dựa trên hệ số nở tâm của từng loại súng (ví dụ khẩu Viper AR tăng $+1.2^\circ$ mỗi viên cho tới ngưỡng tối đa). Khi ngừng bắn, góc lệch hồi phục theo hàm số thời gian thực $\Delta t$.*  
  > *Khi nâng cấp vũ khí tại Bàn Nâng Cấp Dã Chiến, người chơi chọn 1 trong 3 nhánh: Sát thương (DMG), Tốc độ bắn (RPM), hoặc Độ chính xác & Xuyên phá (ACC/Pierce). Các thuộc tính này được đóng gói trong đối tượng vũ khí và lưu trữ theo cấp độ từ 1 đến 10, đồng thời hiển thị bản xem trước chỉ số (Preview Stats) rõ ràng trước khi người chơi xác nhận nâng cấp ạ."*

---

### ❓ Câu hỏi 3: "Em hãy giải thích rõ cơ chế Viewport Culling và kỹ thuật Baking bản đồ trong game được hiện thực hóa như thế nào?"
* **Người trả lời ưu tiên**: **Thành viên 3 (Procedural Map & Optimization Specialist)**.
* **Câu trả lời chuẩn**:
  > *"Dạ thưa Thầy/Cô, để bản đồ hầm ngục rộng $3840 \times 2160\text{ px}$ (3.600 ô gạch) và trường bắn The Range vận hành mượt mà ở chuẩn 60 FPS mà không nghẽn CPU, nhóm đã hiện thực hóa 2 kỹ thuật tối ưu hóa đồ họa chuyên sâu:
  > 1. **Thuật toán Cắt Tỉa Khung Nhìn (Viewport Culling)**:
  >    - Trong phương thức `DungeonMap.render()`, thay vì duyệt qua toàn bộ 3.600 ô gạch, thuật toán lấy tọa độ Camera $(CamX, CamY)$ và tính toán chính xác dải chỉ số ô nằm trong vùng nhìn `[startCol..endCol]` và `[startRow..endRow]`.
  >    - Số lượng ô cần xử lý giảm từ 3.600 ô xuống chỉ còn khoảng ~980 ô (**giảm hơn 72% khối lượng render**). Thêm vào đó, kỹ thuật **Void Culling** lập tức bỏ qua các ô hư vô bên ngoài phòng, triệt tiêu toàn bộ các phép tính dời hình `worldToScreen` thừa thãi.
  > 2. **Kỹ thuật 'Nướng' Bề Mặt Màn Chơi (Map Surface Baking)**:
  >    - Trong `HubRangeState.buildHubTiles()`, thay vì mỗi frame 60 FPS đều phải lặp qua vẽ từng bờ tường gạch, rào chắn và hàng trăm cụm cỏ trang trí ngẫu nhiên, hệ thống vẽ toàn bộ nền một lần duy nhất lúc khởi tạo lên một `BufferedImage` đệm ngoài màn hình.
  >    - Tấm ảnh này được chuyển đổi bằng `GameSpriteManager.toCompatibleImage()` thành định dạng ảnh tăng tốc phần cứng tương thích trực tiếp với VRAM card đồ họa.
  >    - Khi chơi, mỗi khung hình card màn hình chỉ cần gọi lệnh **BitBlt (Bit Block Transfer)** sao chép nguyên khối pixel từ VRAM lên màn hình trong thời gian $O(1)$ gần như $0\text{ ms}$, đạt hiệu năng tuyệt đối ạ."*

---

### ❓ Câu hỏi 4: "Thuật toán sinh hầm ngục 4 hướng giải quyết bài toán chu trình và phòng bị cô lập như thế nào?"
* **Người trả lời ưu tiên**: **Thành viên 3 (Procedural Map & Optimization Specialist)**.
* **Câu trả lời chuẩn**:
  > *"Dạ thưa Thầy/Cô, thuật toán của chúng em dựa trên mô hình **Cây khung mở rộng có kiểm soát trên lưới tô-pô 4 hướng (Bounded Grid Tree)**.*  
  > *Mỗi khi khởi tạo một phòng mới từ một phòng hiện có, thuật toán chỉ được phép chọn 1 trong 4 hướng kề cận (12h, 3h, 6h, 9h) với điều kiện ô lưới đó chưa bị chiếm dụng trong ma trận $4 \times 3$. Do đó, các phòng không bao giờ bị đè lên nhau.*  
  > *Về vấn đề phòng bị cô lập: Mọi phòng đều bắt nguồn từ một đường đi liên thông bắt đầu từ Phòng Xuất phát (`SPAWN`). Đặc biệt, tại các phòng dịch vụ như Shop hay Trạm Tẩy luyện, chúng em sử dụng thuật toán kiểm tra số lượng phòng láng giềng: nếu phòng có $\ge 2$ hướng giáp với các nhánh khác, hệ thống sẽ mở thông cả 2 cửa, tạo thành nút giao thông chữ thập xuyên suốt, bảo đảm người chơi luôn có đường đi thông thoáng ạ."*

---

### ❓ Câu hỏi 5: "Làm thế nào để AI quái vật không bị kẹt góc tường và cơ chế 3 tầng xoay của Boss Tank hoạt động ra sao?"
* **Người trả lời ưu tiên**: **Thành viên 4 (Enemy AI & Tactical UI Specialist)**.
* **Câu trả lời chuẩn**:
  > *"Dạ thưa Thầy/Cô:
  > 1. **Về AI quái vật**: Quái cận chiến sử dụng thuật toán tìm đường **BFS trên ma trận ô tile** để tìm đường đi ngắn nhất tới người chơi. Đồng thời, quái phát ra cụm **10 cảm biến xúc tu (Steering Feelers)** lệch góc từ $\pm 25^\circ$ đến $\pm 115^\circ$. Khi một xúc tu chạm vào tường đá hoặc cột cover cứng, quái tự động tạo một lực đẩy vuông góc để trượt mượt mà men theo mép tường mà không bị khựng lại.
  > 2. **Về Trùm Titan Tank**: Boss được tách thành 3 ma trận xoay độc lập:
  >    - Lớp thân xe xoay theo góc của vector vận tốc di chuyển thực tế.
  >    - Lớp tháp pháo súng máy xoay bám theo tọa độ con trỏ của người chơi bằng góc $\operatorname{atan2}$.
  >    - Lớp pháo hạng nặng khóa góc mục tiêu trong $0.4\text{ s}$ trước khi bắn để người chơi có cửa sổ lướt né kịp thời. Ba lớp này được tính toán góc riêng biệt trong phương thức `update()` và kết xuất đè lớp lên nhau trong `render()` ạ."*

---

### ❓ Câu hỏi 6: "Ứng dụng Java chạy thời gian thực thường bị khựng khung hình do Garbage Collection (GC Hitching). Nhóm đã tối ưu hóa điều này như thế nào?"
* **Người trả lời ưu tiên**: **Thành viên 1 (Trưởng nhóm / Lead Engine Architect)**.
* **Câu trả lời chuẩn**:
  > *"Dạ thưa Thầy/Cô, hiện tượng GC Hitching xảy ra khi mã nguồn liên tục cấp phát các đối tượng tạm thời (`new Object()`) bên trong vòng lặp game loop 60 FPS, khiến bộ nhớ Heap nhanh chóng đầy và bộ dọn rác GC của máy ảo Java phải tạm dừng ứng dụng để thu hồi.*  
  > *Nhóm chúng em đã triệt tiêu vấn đề này bằng các kỹ thuật tối ưu hóa bộ nhớ chuyên sâu:
  > 1. **Tái sử dụng bộ đệm (Font & Texture Cache)**: Toàn bộ phông chữ và hình ảnh được nạp trước một lần và lưu trữ trong `HashMap`, không bao giờ khởi tạo lại trong hàm `paintComponent()`.*
  > 2. **Tối ưu hóa đối tượng đạn**: Các viên đạn bay ra khỏi tầm nhìn hoặc đã va chạm sẽ được đánh dấu để xóa khỏi mảng một cách có kiểm soát.*
  > 3. Kết quả đo lường thực tế trên công cụ giám sát JVM cho thấy: Dung lượng RAM Heap của game duy trì cực kỳ phẳng ở mức **$65\text{ MB} - 110\text{ MB}$**, hoàn toàn không có đột biến thu gom rác gây khựng khung hình (Zero GC Hitching) ạ."*

---

## 📌 LỜI KHUYÊN & KINH NGHIỆM ĐỂ ĐẠT ĐIỂM TỐI ĐA

1. **Phân chia thời gian chuẩn xác**: Tuyệt đối không để một thành viên nói quá dài. Dùng đồng hồ bấm giờ khi tập dượt để đảm bảo bài nói kết thúc trong khoảng 10-12 phút, để dành 3-5 phút cho demo và hỏi đáp.
2. **Tương tác giữa các thành viên**: Khi chuyển lời, hãy gọi tên thành viên tiếp theo bằng cử chỉ tay lịch sự. Khi trả lời câu hỏi phản biện, thành viên nắm chắc phần đó chủ động bước lên trả lời, các thành viên khác có thể bổ sung ngắn gọn nếu cần.
3. **Thao tác Demo mượt mà**: Người chơi demo cần tập trước để đi đúng kịch bản (không đi lung tung, không để nhân vật chết giữa chừng, biểu diễn đầy đủ các tính năng nổi bật: né đòn, đổi súng, nâng cấp và chạm trán Boss).
4. **Phong thái tự tin & Trung thực**: Nhấn mạnh vào những gì nhóm **tự tay xây dựng từ con số không trên Java thuần**, thể hiện sự am hiểu tường tận từng dòng code và thuật toán.
