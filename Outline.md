Cập nhật mới nhất vào ngày: 27/10/2026

# DÀN Ý THUYẾT TRÌNH BẢO VỆ DỰ ÁN CUỐI KỲ: PROJECT DONTKNOW
### ĐỀ TÀI: GAME BẮN SÚNG 2D TOP-DOWN ROGUELITE ENGINE TRÊN NỀN TẢNG JAVA THUẦN
**Nhóm thực hiện**: Group Unknown (Team Morningstar) | **Môn học**: Lập trình Java Nâng Cao (Advanced Java)

---

## ⏱️ THÔNG TIN TỔNG QUAN PHIÊN THUYẾT TRÌNH
* **Tổng thời lượng khuyến nghị**: 12 – 15 phút (Bao gồm 10 phút thuyết trình lý thuyết/tính năng + 3-5 phút Live Demo + Q&A phản biện).
* **Cơ cấu phân chia vai trò 4 Thành viên (Đã chuẩn hóa)**:
  * **Thành viên 1 (Trưởng nhóm)**: Mở đầu, Bối cảnh, Đặt vấn đề, Cảm hứng thiết kế, Kiến trúc phân tầng tách rời, Vòng lặp `GameLoop` đa luồng, 4 Trụ cột OOP, 5 Nguyên lý SOLID, Lưu trữ Checkpoint an toàn, Kiểm thử tự động (249 tests JUnit 5) & Tổng kết.
  * **Thành viên 2 (Vũ khí & Nâng cấp nhân vật)**: Hệ thống xạ kích $360^\circ$ ($\operatorname{atan2}$), Đạn đạo vật lý, Cơ chế giật súng nở tâm, Phân loại kho đạn dã chiến, Bàn Nâng cấp súng 3 nhánh, Trạm Tẩy luyện chỉ số súng, và Hệ thống Nâng cấp Sinh trắc học Bio-Tech (6 chỉ số + 7 Perk kỹ năng).
  * **Thành viên 3 (Map & Thuật toán tối ưu)**: Thuật toán Sinh hầm ngục thủ tục (Bounded Grid Tree ma trận $4 \times 3$), Nút giao đa lối đi (Transit Crossroads), 6 Archetypes kích thước phòng & bố cục cover kiên cố, Bộ đôi thuật toán tối ưu hóa đồ họa: Viewport Culling & Map Surface Baking, Texture Pre-scaling 1:1, Bù giáp mí 1px và Băm không gian ma trận (Spatial Grid $O(1)$).
  * **Thành viên 4 (AI Kẻ địch & Hệ thống UI)**: Trí tuệ nhân tạo (AI) Đa tầng (BFS Pathfinding + Cảm biến xúc tu né vật cản, Quái Quân Y Medic, Quái Công Binh Grenadier), Thiết kế Trùm Titan Tank 3 tầng xoay độc lập, Hệ thống Giao diện Tác chiến UI (PlayingHUD, Balo 2 Tab PDA, Radar Minimap, Boss Intro Cinematic) và Điều phối Live Demo 3 phút.

---

## 📑 BỐ CỤC SLIDE & PHÂN CÔNG THUYẾT TRÌNH CHI TIẾT

### PHẦN 1: TỔNG QUAN, BỐI CẢNH & KIẾN TRÚC HỆ THỐNG (Thành viên 1 - Trưởng nhóm phụ trách | ~3.0 phút)
* **Slide 1: Trang bìa giới thiệu (Title Slide)**
  * Tên đề tài: *Thiết kế và Phát triển Hệ thống Trò chơi Bắn súng Roguelite góc nhìn trên xuống "DontKnow" trên nền tảng Java thuần*.
  * Giảng viên hướng dẫn, thành viên nhóm và phân công vai trò kỹ thuật.
* **Slide 2: Đặt vấn đề & Động lực nghiên cứu (Problem Definition & Motivation)**
  * Thực trạng phụ thuộc vào Game Engine thương mại (Unity, Unreal, Godot).
  * Thách thức khi tự làm Game Engine trên Java Swing: Mô hình đơn luồng EDT dễ nghẽn, hiện tượng xé hình (Tearing) và trôi phím khi xử lý tác vụ nặng.
* **Slide 3: Cảm hứng thiết kế & Định vị sản phẩm (Design Inspiration)**
  * **Soul Knight**: Hầm ngục thủ tục ngẫu nhiên, khóa phòng giao tranh đa pha, yếu tố Roguelite bất định.
  * **Surviv.io**: Xạ kích $360^\circ$ theo chuột, giật nở tâm, kho đạn dự trữ phân loại, nhịp độ combat khốc liệt.
  * **Mục tiêu sản phẩm**: Bản mẫu kiến trúc phần mềm chuẩn mực (Architectural Reference) chạy mượt mà 60 FPS/UPS trên Java 21 LTS thuần túy.
* **Slide 4: Kiến trúc hệ thống phân tầng tách rời & Game Loop đa luồng**
  * Sơ đồ phân tầng: `core` $\rightarrow$ `state` $\rightarrow$ `world/geometry` $\rightarrow$ `entity/combat` $\rightarrow$ `ui`.
  * Tách biệt hoàn toàn: **Worker Thread tính toán vật lý (Fixed Timestep 60 UPS)** và **Swing EDT kết xuất đồ họa (60 FPS)**.
  * Double Buffering khử xé hình và kiểm soát phân bổ bộ nhớ Zero GC Hitching.
* **Slide 5: Vận dụng 4 Trụ cột OOP, SOLID & Design Patterns**
  * Đóng gói, Trừu tượng hóa (`Weapon`, `Enemy`), Kế thừa và Đa hình.
  * 5 Nguyên lý SOLID: SRP (đơn trách nhiệm), OCP (dễ mở rộng vũ khí/quái mới mà không sửa engine).
  * Design Patterns kinh điển: State Pattern, Strategy Pattern, Factory Method, Observer Pattern.
  * *(Thành viên 1 chuyển lời cho Thành viên 2)*

---

### PHẦN 2: HỆ THỐNG VŨ KHÍ & NÂNG CẤP NHÂN VẬT (Thành viên 2 phụ trách | ~2.5 phút)
* **Slide 6: Hệ thống Xạ kích 360 độ & Mô phỏng Đạn đạo Vật lý**
  * Định hướng nòng súng tự do theo chuột bằng giải thuật lượng giác $\theta = \operatorname{atan2}(dy, dx)$.
  * Mô hình đường cong giật súng nở tâm động (Dynamic Bloom Recoil Dispersion) theo từng phát bắn.
  * Đạn hoa cải phân mảnh Shotgun, đạn xuyên mục tiêu của Sniper Rifle (Pierce System xuyên 2 - 5 mục tiêu).
  * Quản lý 4 kho đạn dự trữ dã chiến: Súng trường (RIF), Tiểu liên (SMG), Súng hoa cải (SG), Súng ngắm (SR).
* **Slide 7: Tiến trình Vũ khí, Tẩy luyện Chỉ số & Căn cứ The Range**
  * Bàn Nâng cấp Quân khí dã chiến (Field Gunsmith): Cường hóa súng lên tới Cấp 10 với 3 nhánh rẽ (DMG, RPM, ACC/Pierce).
  * Trạm Tẩy luyện chỉ số (Stat Reforge): Reroll sao chỉ số độc lập và Reroll tỉ lệ/hệ số chí mạng.
  * Căn cứ The Range: Khu vực thử súng tự do với bia hình nộm đo chỉ số DPS thực tế thời gian thực.
* **Slide 8: Hệ thống Nâng cấp Sinh trắc học Bio-Tech (Character Augmentation)**
  * Trạm Bio-Tech: 6 chỉ số thuộc tính cơ bản (Máu, Sát thương, Tốc độ, Giáp giảm thương, Hồi né, May mắn nhặt đồ).
  * 7 Perk kỹ năng sinh tồn độc bản: Giáp chặn đòn, Diệt địch hồi máu, Né xuyên vật cản, Thu nhỏ hitbox, Băng đạn định kỳ, Băng đạn mở rộng, Bậc thầy chí mạng.
  * *(Thành viên 2 chuyển lời cho Thành viên 3)*

---

### PHẦN 3: MAP & THUẬT TOÁN TỐI ƯU HIỆU NĂNG (Thành viên 3 phụ trách | ~2.5 phút)
* **Slide 9: Thuật toán Sinh hầm ngục thủ tục trên Lưới 4 Hướng (Procedural Generation)**
  * Thuật toán sinh lưới tô-pô có kiểm soát (Bounded Grid Tree): Ma trận $4 \times 3$ phòng trực giao 4 hướng (12h, 3h, 6h, 9h), thời gian sinh map $< 2\text{ ms}$, triệt tiêu 100% rủi ro đệ quy vô hạn hay bít đường.
  * Cơ chế phòng tiện ích đặc biệt (Transit Crossroads): Tự động mở tối đa 2 lối vào/ra thông suốt, loại bỏ tình trạng ngõ cụt.
  * 6 Archetypes kích thước phòng ($12 \times 9$ đến $14 \times 10$) kết hợp 6 bố cục đặt cột che chắn (Cover Pillars) bảo đảm cách cửa $\ge 2$ ô tile.
  * Cơ chế khóa phòng giao tranh đa pha (Combat Lockdown) với cửa blast doors tự động đóng và triệu hồi quái theo đợt.
* **Slide 10: Bộ đôi Thuật toán Tối ưu hóa Đồ họa: Viewport Culling & Map Surface Baking**
  * **Thuật toán Cắt tỉa Khung nhìn (Viewport Culling & Void Culling)**: Tính toán dải ô khả kiến `[startCol..endCol]`, `[startRow..endRow]`, giảm từ 3.600 ô xuống ~980 ô (**tiết kiệm hơn 72% chi phí vẽ**); bỏ qua 100% ô hư vô `TileType.VOID`.
  * **Kỹ thuật "Nướng" Bề Mặt (Map Surface Baking)**: Dựng sẵn toàn bộ nền gạch và chi tiết tĩnh lên ảnh đệm ngoài màn hình, nướng sang định dạng tương thích VRAM bằng `toCompatibleImage()`, sao chép khối pixel BitBlt siêu tốc $O(1)$.
  * **Pre-Scaled Texture Caching 1:1 & Bù Giáp Mí 1px**: Nạp sẵn texture $49 \times 49\text{ px}$ trong VRAM, vẽ 1-pass không overdraw, triệt tiêu khe hở rãnh đen khi Camera Lerp mượt.
  * **Băm Không Gian Ma Trận (Spatial Grid Indexing)**: Kiểm tra va chạm địa hình tức thì đạt độ phức tạp toán học $O(1) < 0.005\text{ ms}$.
  * **Dò tia Tầm nhìn Tối ưu (Optimized Raymarching LoS)**: Lấy mẫu bước nhảy $16\text{ px}$ giúp AI kiểm tra tầm nhìn LoS nhanh gấp 16 lần.
  * *(Thành viên 3 chuyển lời cho Thành viên 4)*

---

### PHẦN 4: AI KẺ ĐỊCH & HỆ THỐNG GIAO DIỆN TÁC CHIẾN (Thành viên 4 phụ trách | ~2.5 phút)
* **Slide 11: Trí tuệ Nhân tạo (AI) Đa Tầng & Các Chủng Loại Quái Chiến Thuật**
  * **AI Tìm đường**: Quái cận chiến dùng giải thuật BFS trên lưới ô tile kết hợp cụm 10 cảm biến xúc tu (`Steering Feelers`) né vật cản mượt mà.
  * **Quái xạ thủ**: Cơ động lùi giữ khoảng cách (Kiting), lạng sườn (Strafing) và tự động đảo chiều khi chạm tường.
  * **Quái Quân Y (Medic)**: Hào quang nano đỏ hồi máu $6\text{ HP}/0.8\text{ s}$, cơ chế chạy trốn người chơi và ưu tiên cứu thương đồng đội yếu máu.
  * **Quái Công Binh (Grenadier)**: Ném lựu đạn hẹn giờ 2.0s, cảnh báo nguy hiểm đỏ 78px nhấp nháy, nổ diện rộng ép góc và phá hủy vật cản.
* **Slide 12: Trùm Cuối Titan Tank & Hệ Thống Giao Diện Tác Chiến (UI/HUD)**
  * **Trùm cuối Titan Tank**: 3 tầng xoay độc lập (Thân xe, Tháp pháo chính, Tháp súng máy); Cú húc rocket $950\text{ px/s}$ ủi nát thùng gỗ, súng máy kép và pháo laser siêu thanh với vùng cảnh báo đỏ Telegraph.
  * **Hệ thống Giao diện Tác chiến UI**: HUD hiển thị máu/đạn/né đòn, Balo tác chiến 2 Tab (Field Tactical PDA), Radar Minimap sương mù khám phá (Fog of War) và Cắt cảnh giới thiệu Boss uy lực (Letterbox Boss Intro).
  * *(Thành viên 4 điều phối màn Live Gameplay Demo 3 phút, sau đó chuyển lại cho Thành viên 1)*

---

### PHẦN 5: KIỂM THỬ TỰ ĐỘNG, LIVE DEMO & KẾT LUẬN (Thành viên 1 & Cả nhóm | ~3.0 phút)
* **Slide 13: Trình diễn Trực tiếp Trò chơi (Live Gameplay Demo - 3 phút)**
  * Kịch bản Demo phối hợp: Menu $\rightarrow$ Trường bắn The Range $\rightarrow$ Hầm ngục giao tranh khóa phòng $\rightarrow$ Nâng cấp vũ khí & Bio-Tech $\rightarrow$ Chạm trán Boss Titan Tank.
* **Slide 14: Kiểm thử tự động JUnit 5 & Đo lường Hiệu năng Thực nghiệm**
  * **249 Unit Tests JUnit 5 đạt 100% PASS**: Bao phủ va chạm, đạn đạo, AI, lưu tiến trình và chuyển trạng thái.
  * **Chỉ số hiệu năng thực nghiệm**: Ổn định chuẩn 60.0 FPS / 60.0 UPS; Độ trễ khung hình $\approx 16.5\text{ ms}$; Heap RAM chỉ $65\text{ MB} - 110\text{ MB}$; Zero GC Hitching; Đóng gói Portable All-In-One nhúng sẵn JRE.
* **Slide 15: Lời cảm ơn & Phiên Hỏi - Đáp (Q&A Session)**
  * Mở đầu phiên phản biện và trả lời câu hỏi của Hội đồng/Giảng viên.
