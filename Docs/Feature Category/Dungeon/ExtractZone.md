Cập nhật mới nhất vào ngày: 03/10/2026

# ExtractZone (Ô Sơ Tán & Chuyển Màn)

## 1. Mô tả
`ExtractZone` đại diện cho khu vực bệ phóng sơ tán (cổng kết thúc màn chơi) nằm ở trung tâm phòng Sơ tán (**Extract Room**). Khu vực này được trải bằng loại ô gạch đặc thù `TileType.EXTRACT_ZONE` (màu tím dạ quang, có vòng tròn năng lượng portal).

Khi người chơi tiêu diệt hết kẻ địch trong Combat Arena và di chuyển sang Extract Room, bước lên bệ sơ tán sẽ xuất hiện tương tác nhấn phím **F** để sơ tán và hoàn thành màn chơi.

---

## 2. Quy trình hoạt động
1. **Phát hiện đứng trên bệ**:
   * Phương thức `dungeonMap.isIntersectsExtractZone(playerX, playerY, width, height)` kiểm tra hitbox nhân vật có chạm vào bất kỳ ô gạch `EXTRACT_ZONE` nào hay không.
2. **Hiển thị gợi ý tương tác**:
   * Nếu người chơi đứng trên bệ và chưa kích hoạt sơ tán: Hiển thị hộp tương tác nổi `[F] SƠ TÁN (HOÀN THÀNH MÀN 0-1)` phía trên đầu nhân vật.
3. **Kích hoạt sơ tán [F]**:
   * Khi người chơi nhấn phím **F** (`KeyEvent.VK_F`):
     * Kích hoạt cờ `isExtracting = true`.
     * Đóng băng chuyển động của nhân vật, không cho bắn súng hay dùng vũ khí.
4. **Màn hình đen báo cáo hoàn thành (Black Screen Victory Debriefing)**:
   * Phủ đen toàn bộ màn hình `1280x720` (`Color.BLACK`).
   * Hiển thị bảng vinh danh chiến thuật:
     * Tiêu đề: `★ MÀN 0-1 HOÀN THÀNH ★`
     * Phụ đề: `SƠ TÁN THÀNH CÔNG (EXTRACTION SUCCESSFUL)`
     * Thông tin chi tiết:
       * Khu vực: Hầm Ngục Thử Nghiệm Alpha (Chamber 0-1).
       * Kết quả: Toàn bộ mục tiêu chiến đấu đã bị tiêu diệt.
       * Trạng thái: Dữ liệu màn tiếp theo đang được cập nhật...
       * Tạm thời: Chuyển tiếp về Màn hình Chính (Main Menu).
5. **Chuyển tiếp về Menu**:
   * Sau 4.0 giây hoặc ngay khi người chơi nhấn `ENTER`, `SPACE`, `ESC`, hoặc `F`, hệ thống chuyển đổi trạng thái về `GameStateManager.StateType.MENU`.

---

## 3. API
* `dungeonMap.isIntersectsExtractZone(double worldX, double worldY, double width, double height)`: Kiểm tra hitbox của thực thể có giao nhau với ô sơ tán.
* `dungeonMap.isOnExtractZone(double worldX, double worldY)`: Kiểm tra một tọa độ điểm có nằm trên ô sơ tán.
* `playingState.isStandingOnExtract()`: Trả về cờ kiểm tra người chơi có đang đứng trên bệ sơ tán.
* `playingState.isExtracting()`: Trả về trạng thái đang trong quá trình chuyển màn chiến thắng.

---

## 4. Quan hệ với hệ thống khác
* Phụ thuộc: `DungeonMap` (vị trí các ô `TileType.EXTRACT_ZONE`), `KeyboardHandler` (bắt phím `F`).
* Chuyển giao: `GameStateManager` (gọi `setState(MENU)` để hoàn tất chuyển màn).

---

## 5. Sửa đổi / Cập nhật
* **03/10/2026**: Khởi tạo tính năng tương tác nút F tại ô sơ tán và màn hình đen báo cáo hoàn thành Màn 0-1.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
