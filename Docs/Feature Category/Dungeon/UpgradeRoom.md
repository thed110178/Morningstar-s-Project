Cập nhật mới nhất vào ngày: 20/10/2026

# Phòng Nâng Cấp Nhân Vật (Bio-Tech Augmentation Laboratory)

## 1. Mô tả
**Phòng Nâng Cấp Nhân Vật (RoomType.UPGRADE)** là một phòng chức năng an toàn đặc biệt trong tổ hợp cơ sở ngầm, tương tự như Phòng Shop (`RoomType.SHOP`) và Phòng Reset Stat (`RoomType.RESET_STAT`).
- Tên định danh: *"Bio-Tech Augmentation Laboratory"*.
- Cửa vòm ra vào: Thiết kế vòm kính phát quang vi mạch công nghệ cao, **luôn luôn mở (Always Open)**, không bao giờ bị khóa bởi đợt quái.
- Sàn trung tâm: Bố trí tấm bệ kim loại titan xanh sẫm `UPGRADE_ZONE` kích thước 4x4 ô.
- Trang thiết bị: Lắp đặt **2 Trạm Nâng Cấp Bệ Máy Đối Xứng**:
  - Trạm phía Tây (cx - 70px): Trạm Nâng Cấp Chỉ Số Cơ Bản (Basic Stats Upgrade Station).
  - Trạm phía Đông (cx + 70px): Trạm Cường Hóa Kỹ Năng Đặc Biệt (Special Perks Station).

---

## 2. Quy trình hoạt động

### 2.1. Sinh phòng theo thuật toán Procedural Generation
1. Tại mỗi Stage thường (Stage 1, 2, 3), `DungeonMap` khởi tạo lưới hầm ngục 4x3 (12 ô).
2. Phân bổ các phòng chức năng:
   - Phòng Xuất Phát (`SPAWN`).
   - Phòng Sơ Tán (`EXTRACT`).
   - Phòng Cửa Hàng Tiếp Tế (`SHOP`).
   - Phòng Xưởng Quân Khí Tẩy Chỉ Số (`RESET_STAT`).
   - **Phòng Nâng Cấp Nhân Vật (`UPGRADE`)**.
   - 3 đến 4 Phòng Giao Tranh Chiến Đấu (`COMBAT`).
3. Hành lang liên kết tự động kết nối đảm bảo người chơi có thể tự do ghé thăm phòng nâng cấp trước hoặc sau bất kỳ trận chiến nào.

### 2.2. Tương tác trong phòng
- Khi người chơi bước vào phạm vi bán kính ~60px của một trạm:
  - Vòng năng lượng phát quang dưới chân trạm sáng rực rỡ.
  - Hiển thị tooltip gợi ý thao tác nổi: `[F] NÂNG CẤP CHỈ SỐ` hoặc `[F] CƯỜNG HÓA KỸ NĂNG`.
- Nhấn phím `[F]` mở bảng giao diện Sci-Fi modal chọn 3 thẻ buff.

---

## 3. Quan hệ với hệ thống khác
- **`DungeonMap`**: Quản lý node phòng, gán loại `RoomType.UPGRADE`, gán bệ sàn `TileType.UPGRADE_ZONE`.
- **`CharacterUpgradeStation`**: Thực thể trạm 2D đặt trong phòng.
- **`PlayingState`**: Khởi tạo trạm trong `populateUpgradeStations()`, xử lý tương tác [F] và render.

---

## 4. Sửa đổi / Cập nhật
- **20/10/2026**:
  - Khởi tạo phòng `RoomType.UPGRADE` và tích hợp vào thuật toán sinh màn chơi.
  - Bổ sung gạch lát bệ máy `TileType.UPGRADE_ZONE` và biển hiệu vòm cửa dã chiến.
