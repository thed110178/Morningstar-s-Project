Cập nhật mới nhất vào ngày: 26/10/2026

# TỔNG QUAN DỰ ÁN: PROJECT DONTKNOW
*An unplanned top-down roguelite shooter*

---

## 1. Giới thiệu Dự án
* **Tên dự án**: Project DontKnow
* **Thể loại**: 2D Top-Down Roguelite Shooter (Lấy cảm hứng và học tập từ Soul Knight về cơ chế hầm ngục thủ tục và Surviv.io về cơ chế xạ kích 360 độ, kho đạn và sinh tồn dã chiến).
* **Nền tảng công nghệ**:
  * Ngôn ngữ: **Java 21 LTS**
  * Đồ họa & GUI: **Java Swing + Graphics2D + Double Buffering (Anti-Tearing Engine)**
  * Quản lý gói: **Apache Maven**
  * Kiểm thử: **JUnit 5 (Jupiter - 226 tests passing 100%)**
  * Kiểm soát phiên bản: **Git**

---

## 2. Thiết kế Gameplay Cốt lõi (Core Loop)
1. **Di chuyển & Điều khiển**:
   * Di chuyển 8 hướng mượt mà bằng phím **W, A, S, D**.
   * Lướt né đòn (**Dash/Roll**) với phím **Spacebar** (kèm thời gian bất tử i-frame).
2. **Cơ chế Bắn súng 360 độ & Định hướng Xạ kích**:
   * Con trỏ chuột đóng vai trò hồng tâm (Crosshair).
   * Nòng súng xoay tự do 360 độ theo con trỏ chuột ($\text{atan2}(dy, dx)$).
   * **Đường ngắm chuột nét đứt mờ (Aim Guideline)**: Hỗ trợ căn đường đạn chuẩn xác từ nòng súng đến con trỏ (Bật / Tắt trong Cài đặt).
   * Chuột trái để bắn, phím **R** để nạp đạn (Reload), phím **1, 2, 3, 4, Q** để đổi vũ khí và cận chiến.
3. **Tiến trình Vũ khí & Trang bị**:
   * **Nâng cấp vũ khí chủ động**: Bàn Nâng Cấp Quân Khí Dã Chiến cho phép chọn 1 trong 3 nhánh (DMG, RPM, ACC/Pierce) lên tới Cấp 10.
   * **Súng Bắn Tỉa (Sniper Rifle)**: Nhánh chuyên biệt hóa Sức Xuyên (Pierce Power) xuyên qua 2 đến 5 mục tiêu liên tiếp.
   * **Reroll súng 2 mục độc lập**: Reroll Sao Chỉ Số (bảo lưu chí mạng, khóa súng Lv.1) và Reroll Chí Mạng (bảo lưu cấu hình sao, áp dụng mọi cấp độ).
   * **Trường Bắn The Range**: Căn cứ tập huấn tự do với bia bắn hình nộm đo DPS thực tế và các trạm dịch vụ quân khí miễn phí.
4. **Yếu tố Roguelite & Nâng cấp Nhân vật**:
   * Thu thập Credits từ quái vật và rương thử thách.
   * **Phòng Nâng Cấp Nhân Vật**: Cường hóa chỉ số sinh tồn vĩnh viễn (HP, Speed, Defense, Reload Speed, Regen).
   * Bản đồ phòng ngẫu nhiên 4 Stages (3 standard stages + 1 Titan Boss Arena).
   * Boss Xe Tăng Titan với vùng đỏ cảnh báo xung kích đâm húc, pháo phản lực siêu thanh và đợt lính tiếp viện vô tận.
   * Permadeath: Chết là kết thúc ván đấu, lưu điểm vào Bảng xếp hạng (**Leaderboard**).

---

## 3. Kiến trúc Hệ thống
Dự án tuân thủ nghiêm ngặt nguyên lý **SOLID** và các mẫu thiết kế (**Design Patterns**):
* **`core/`**: Game Window, Double Buffered Game Panel, Multithreaded Game Loop (60 FPS), Camera 2D Lerp.
* **`state/`**: State Pattern (`MenuState`, `PlayingState`, `PauseState`, `UpgradeState`, `GameOverState`).
* **`entity/`**: Hệ thống thực thể kế thừa (`Player`, `Enemy`, `Projectile`).
* **`physics/`**: Hitbox AABB / Circle và thuật toán phân vùng không gian **Spatial Grid** tối ưu va chạm $O(N)$.
* **`input/`**: Bộ xử lý bàn phím và chuột không độ trễ.
* **`storage/`**: Lưu trữ điểm số và tiến trình chơi ra file (I/O).

---

## 4. Cấu trúc Tài liệu `Docs/`
* `Docs/Project_Summary.md`: Tài liệu tổng quan dự án.
* `Docs/Checklist/`: Danh mục việc cần làm và tiến độ các giai đoạn.
* `Docs/Feature Category/`: Tài liệu chi tiết từng tính năng (Combat, Dungeon, Progression).
* `Docs/PlanForNextFeature/`: Kế hoạch và thiết kế cho các tính năng tiếp theo.
* `Docs/Update/`: Nhật ký cập nhật và sửa lỗi từng ngày.
