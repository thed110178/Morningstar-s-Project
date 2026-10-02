Cập nhật mới nhất vào ngày: 02/10/2026

# Main.java - Điểm Khởi Chạy Ứng Dụng (Application Entry Point)

## 1. Mô tả
`Main.java` là lớp chứa phương thức `public static void main(String[] args)`, đóng vai trò là điểm vào (entry point) duy nhất của toàn bộ chương trình khi khởi chạy từ terminal hoặc file `.jar`.

---

## 2. Quy trình hoạt động
1. Khi máy ảo Java (JVM) nạp chương trình, phương thức `main` được thực thi trên luồng chính (`main thread`).
2. `Main` điều phối việc khởi tạo giao diện người dùng Swing an toàn thông qua `SwingUtilities.invokeLater()`.
3. Trong `invokeLater`, một đối tượng `GameWindow` được khởi tạo và gọi phương thức `launch()` để hiển thị cửa sổ đồng thời khởi động luồng `GameLoop`.

---

## 3. API / Cấu trúc Lớp
```java
public class Main {
    public static void main(String[] args)
}
```

---

## 4. Cấu hình & Tham số
Không có tham số cấu hình tĩnh. Quản lý cấu hình tập trung tại `GameConfig.java`.

---

## 5. Quan hệ với hệ thống khác
* **Phụ thuộc vào**: [`GameWindow.java`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/src/main/java/com/groupproject/game/core/GameWindow.java) để hiển thị cửa sổ.
* **Tương tác**: Đưa việc khởi tạo GUI vào hàng đợi Event Dispatch Thread (EDT) của Swing để tránh race condition đồ họa.

---

## 6. Sửa đổi / Cập nhật
* **02/10/2026**: Khởi tạo ban đầu với `SwingUtilities.invokeLater()`.

---

## 7. Lỗi đã sửa
Chưa ghi nhận lỗi.
