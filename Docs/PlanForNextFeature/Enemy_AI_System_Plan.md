Cập nhật mới nhất vào ngày: 18/10/2026

# Kế Hoạch Triển Khai: Hệ Thống AI Cho Kẻ Địch (Enemy AI System Plan)

## 1. Mục tiêu
Nâng cấp từ các bia tập bắn tĩnh (Target Dummy) thành các thực thể kẻ địch có trí tuệ nhân tạo (AI):
* **Kẻ địch cận chiến (Melee Swarmer/Brawler)**:
  - Có khả năng truy đuổi và tiếp cận người chơi theo thời gian thực.
  - Sử dụng trượt vật lý trên địa hình (axis-independent sliding collision) để không đi xuyên tường.
  - Đánh cận chiến khi tiếp cận đủ gần ($\le 34\text{px}$) với thời gian hồi chiêu để không giật lag sát thương.
* **Kẻ địch tầm xa trang bị súng lục (Ranged Pistol Scout)**:
  - Biết giữ khoảng cách chiến thuật an toàn ($180 - 280\text{px}$).
  - Khi người chơi áp sát quá gần ($< 180\text{px}$): chủ động lùi lại (kiting / backpedaling).
  - Khi người chơi ở quá xa ($> 280\text{px}$): tiến lại gần để vào tầm bắn.
  - Bắn loạt $1 - 3$ viên đạn súng lục mỗi lần bắn với tốc độ ra đạn chậm (giữa các viên $0.35\text{s}$, thời gian hồi giữa các đợt bắn $2.0 - 2.8\text{s}$).
  - Đạn bay với sơ tốc vừa phải ($380\text{px/s}$) cho phép người chơi có phản xạ né tránh.

## 2. Thiết kế Lớp (Class Hierarchy)
* `TargetDummy`: Lớp cơ sở (Base Class) hỗ trợ các thuộc tính sinh mệnh cơ bản và phương thức ảo `updateAI(...)`.
* `MeleeEnemy extends TargetDummy`: Triển khai AI tiếp cận và tấn công va chạm/chém cận chiến.
* `RangedPistolEnemy extends TargetDummy`: Triển khai AI giữ cự ly và bắn loạt súng lục.

## 3. Tương tác với Hệ thống Hiện tại
* `PlayingState`:
  - Trong vòng lặp đạn: Đạn người chơi (`isPlayerBullet() == true`) sát thương kẻ địch; đạn kẻ địch (`isPlayerBullet() == false`) sát thương người chơi qua `damagePlayer(...)`.
  - Trong vòng lặp quái: Gọi `dummy.updateAI(...)` truyền tọa độ người chơi và bản đồ.
* `DungeonMap`:
  - Bổ sung `hasLineOfSight(x1, y1, x2, y2)` kiểm tra vật cản giữa 2 điểm.
