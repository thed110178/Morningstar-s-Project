Cập nhật mới nhất vào ngày: 03/10/2026

# KẾ HOẠCH TRIỂN KHAI: TƯƠNG TÁC Ô SƠ TÁN [F] & MÀN HÌNH HOÀN THÀNH MÀN 0-1

## 1. Mục tiêu
* Bổ sung tính năng tương tác với ô sơ tán (**Extract Zone**) tại phòng **Extract Room**:
  1. Khi nhân vật đứng trên ô sơ tán (`EXTRACT_ZONE`), hiển thị gợi ý tương tác `[F] SƠ TÁN (HOÀN THÀNH MÀN 0-1)`.
  2. Khi người chơi nhấn phím **F**:
     * Đóng băng di chuyển và bắn súng của nhân vật.
     * Chuyển sang màn hình đen toàn phần (**Black Screen Victory Overlay**).
     * Báo cáo hoàn thành: `★ MÀN 0-1 HOÀN THÀNH ★ - SƠ TÁN THÀNH CÔNG`.
     * Tự động hoặc nhấn phím để quay trở về màn hình Menu chính (`GameStateManager.StateType.MENU`).

---

## 2. Thiết kế chi tiết

### A. Tầng Bản đồ (`DungeonMap.java`)
* Bổ sung phương thức kiểm tra va chạm với ô sơ tán:
  ```java
  public boolean isIntersectsExtractZone(double worldX, double worldY, double width, double height);
  ```
* Duyệt các ô gạch trong phạm vi bounding box của nhân vật. Nếu có ô nào mang kiểu `TileType.EXTRACT_ZONE`, trả về `true`.

### B. Tầng Trạng thái Gameplay (`PlayingState.java`)
* **Biến trạng thái**:
  * `boolean isStandingOnExtract`: Cờ đánh dấu người chơi đang đứng trên bệ sơ tán.
  * `boolean isExtracting`: Cờ đánh dấu chu trình hoàn thành màn chơi đang kích hoạt.
  * `double extractTimer`: Đếm thời gian hiển thị màn hình đen (khoảng 3.5 giây).
* **Luồng xử lý (`update`)**:
  * Khi `isExtracting == true`: Bỏ qua các xử lý di chuyển và bắn đạn, tăng `extractTimer`. Khi hết giờ hoặc bấm `ENTER` / `SPACE` / `F`, gọi `gsm.setState(MENU)`.
  * Khi `isExtracting == false`: Kiểm tra `isIntersectsExtractZone(...)`. Nếu người chơi nhấn `F` (`keyH.isKeyPressedOnce(KeyEvent.VK_F)`), kích hoạt `isExtracting = true`.
* **Giao diện hiển thị (`render`)**:
  * Gợi ý phím bấm: Vẽ hộp nút phím `[F]` viền xanh dạ quang phía trên nhân vật kèm dòng chữ `[F] SƠ TÁN - HOÀN THÀNH MÀN 0-1`.
  * Màn hình đen chiến thắng:
    * Phủ nền đen `Color.BLACK` toàn màn hình `1280x720`.
    * Tiêu đề lớn màu vàng kim rực rỡ: `★ MÀN 0-1 HOÀN THÀNH ★`.
    * Dòng trạng thái phụ: `SƠ TÁN THÀNH CÔNG - NHIỆM VỤ HOÀN THÀNH`.
    * Bảng tóm tắt nhiệm vụ và dòng hướng dẫn `[Nhấn ENTER hoặc F để quay về Menu]`.

---

## 3. Kế hoạch kiểm thử (JUnit 5)
* Kiểm tra `isIntersectsExtractZone()` trả về đúng `true` khi ở trong phạm vi ô bệ phóng sơ tán và `false` khi ở ngoài.
* Kiểm tra chu trình chuyển đổi trạng thái khi nhấn phím `F` trên ô sơ tán.
