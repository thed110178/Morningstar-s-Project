Cập nhật mới nhất vào ngày: 18/10/2026

# TỔNG QUAN DỰ ÁN: PROJECT DONTKNOW
*An unplanned top-down roguelite shooter*

---

## 1. Giới thiệu Dự án
* **Tên dự án**: Project DontKnow
* **Thể loại**: 2D Top-Down Roguelite Shooter (Lấy cảm hứng và tham chiếu cơ chế phòng ngẫu nhiên từ Soul Knight và góc bắn 360 độ từ POBG).
* **Nền tảng công nghệ**:
  * Ngôn ngữ: **Java 21 LTS**
  * Đồ họa & GUI: **Java Swing + Graphics2D + Double Buffering**
  * Quản lý gói: **Apache Maven**
  * Kiểm thử: **JUnit 5 (Jupiter)**
  * Kiểm soát phiên bản: **Git**

---

## 2. Thiết kế Gameplay Cốt lõi (Core Loop)
1. **Di chuyển & Điều khiển**:
   * Di chuyển 8 hướng mượt mà bằng phím **W, A, S, D**.
   * Lướt né đòn (**Dash/Roll**) với phím **Spacebar** (kèm thời gian bất tử i-frame).
2. **Cơ chế Bắn súng 360 độ**:
   * Con trỏ chuột đóng vai trò hồng tâm (Crosshair).
   * Nòng súng xoay tự do 360 độ theo con trỏ chuột ($\text{atan2}(dy, dx)$).
   * Chuột trái để bắn, phím **R** để nạp đạn (Reload), phím **1, 2, Q** để đổi vũ khí.
3. **Yếu tố Roguelite**:
   * Tiêu diệt quái vật thu thập kinh nghiệm (EXP) và vàng.
   * Lên cấp kích hoạt màn hình chọn 1 trong 3 thẻ nâng cấp ngẫu nhiên (**Upgrade Perks**).
   * Bản đồ phòng ngẫu nhiên (Procedural Dungeon) hoặc đấu trường sinh tồn theo đợt (Wave Survival).
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
