---
trigger: always_on
---

Cập nhật mới nhất vào ngày: 02/10/2026

# Project DontKnow - Workspace Rules

## Chào mừng đến với Project DontKnow
Dự án game: **Project DontKnow - An unplanned top-down roguelite shooter** (phong cách Soul Knight / POBG).
Môi trường công nghệ: **Java 21 LTS, Java Swing / Graphics2D, Maven, JUnit 5**.

Trước khi bắt đầu hỗ trợ dự án, AI Agent phải dành thời gian tìm hiểu về cấu trúc, tài liệu và mã nguồn của Project DontKnow.

---

# 1. Quy trình tìm hiểu dự án

## Bước 1 - Đọc tài liệu (nếu có)
Đầu tiên, hãy đọc toàn bộ tài liệu trong:
```
Docs/
```
để nắm được:
- Tổng quan dự án và yêu cầu môn học (`f2026-final-project`).
- Thiết kế gameplay roguelite (di chuyển 8 hướng, ngắm bắn 360 độ, kho vũ khí, thẻ nâng cấp perks, phòng dungeon).
- Kiến trúc hệ thống phân tầng (Core, Entity, Weapon, Physics, State, Dungeon, UI, Storage).
- Quy tắc phát triển và tiêu chí chấm điểm (Rubric).

Nếu chưa biết cấu trúc thư mục tài liệu, hãy đọc trước:
```
Docs/Project_Summary.md
```

---

## Bước 2 - Đọc mã nguồn
Sau khi hiểu tài liệu, tiếp tục đọc mã nguồn trong:
```
src/main/java/com/groupproject/game/
```
để hiểu:
- Kiến trúc Game Loop 60 FPS đa luồng (`core/`).
- Quản lý trạng thái bằng State Pattern (`state/`).
- Hệ thống nhân vật, quái vật, vũ khí và đạn (`entity/`).
- Hệ thống kiểm tra va chạm tối ưu không gian Spatial Grid (`physics/`).
- API hiện có và mối quan hệ giữa các thành phần.

Mã nguồn của dự án đã được chú thích (comment JavaDoc) nhằm hỗ trợ cả lập trình viên và AI Agent hiểu nhanh chức năng và lý do thiết kế của từng đoạn code.

---

## Bước 3 - Thực hiện yêu cầu
Sau khi hoàn thành hai bước trên, AI Agent mới bắt đầu xử lý yêu cầu của người dùng.
Trong toàn bộ quá trình làm việc, luôn tuân thủ các quy tắc được định nghĩa trong **Global Rules** và file quy tắc này.

---

# 2. Quy định về tài liệu

## 2.1 Cấu trúc thư mục tài liệu
Toàn bộ tài liệu được lưu tại:
```
Docs/
│
├── Object/
├── Checklist/
├── Feature Category/
│   ├── Combat/
│   ├── Dungeon/
│   └── Progression/
├── PlanForNextFeature/
└── Update/
```

Nếu chưa hiểu cách phân loại, hãy đọc:
```
Docs/Project_Summary.md
```

---

## 2.2 Cập nhật tài liệu
Khi người dùng yêu cầu cập nhật tài liệu Markdown, AI Agent phải thực hiện đầy đủ các bước sau:

### Cập nhật thời gian
Ở đầu mỗi file Markdown, thêm dòng:
```markdown
Cập nhật mới nhất vào ngày: dd/mm/yyyy
```

### Tạo tài liệu mới
Nếu tính năng là hoàn toàn mới:
- Được phép tạo file Markdown mới.
- Đặt file vào đúng thư mục phân loại.
- Không tự ý tạo thư mục mới nếu chưa hỏi ý kiến người dùng.

### Cấu trúc file mô tả tính năng
Khuyến nghị sử dụng cấu trúc sau:
```markdown
1. Mô tả
2. Quy trình hoạt động
3. API / Class & Interface
4. Cấu hình & Tham số cân bằng (Game Balance)
5. Quan hệ với hệ thống khác
6. Sửa đổi / Cập nhật
7. Lỗi đã sửa
```

---

## 2.3 Ghi nhận thay đổi (Update Logs)
Mỗi lần được yêu cầu cập nhật tài liệu hoặc hoàn thành một tính năng lớn (trừ khi tạo kế hoạch trong `Docs/PlanForNextFeature`), AI Agent phải tự động tạo hoặc ghi đè một báo cáo trong:
```
Docs/Update/
```
Tên file:
```
yy-mm-dd_[số thứ tự trong ngày].md
```
Ví dụ:
```
26-10-02_01.md
26-10-02_02.md
```

Báo cáo cần ghi rõ chi tiết:
- Những gì đã thay đổi.
- File nào đã chỉnh sửa, file nào mới được tạo.
- Lý do chỉnh sửa và nguyên lý giải quyết.
- Ghi chú quan trọng và ảnh hưởng tới hệ thống.
- Nếu là sửa lỗi: nêu rõ nguyên nhân (Root Cause) và cách khắc phục.

*Quy tắc gộp file:* Nếu người dùng yêu cầu cập nhật liên tục trong vòng 4 tiếng, ghi tiếp vào file update hiện tại trong ngày để tránh sinh quá nhiều file vụn vặt.

---

## 2.4 Khi cập nhật hệ thống đã tồn tại
Nếu chỉnh sửa một hệ thống đã có tài liệu:
* **Bổ sung tính năng:** Thêm vào mục `Sửa đổi / Cập nhật` (Ngày, Nội dung thay đổi, Cách thức hoạt động).
* **Sửa lỗi:** Thêm vào mục `Lỗi đã sửa` (Ngày sửa, Nguyên nhân lỗi, Cách khắc phục, Ảnh hưởng).
* **Tuyệt đối không** xóa lịch sử cập nhật cũ.

---

## 2.5 Quy định về Comment mã nguồn (JavaDoc & Inline)
Khi sửa đổi hoặc viết mới mã nguồn Java, AI Agent phải bổ sung hoặc cập nhật comment cho các đoạn code liên quan.

### Bắt buộc
- Mọi Class, Interface và Method mới phải có JavaDoc (`/** ... */`) tóm tắt vai trò và tham số/kết quả trả về.
- Các biến hoặc hằng số cân bằng quan trọng cần có comment giải thích vai trò nếu tên gọi chưa đủ rõ nghĩa.
- Khi thay đổi logic, **bắt buộc đồng bộ comment** để phản ánh đúng hành vi mới. Không để code một đằng comment một nẻo.

### Nội dung comment: Ưu tiên giải thích WHY hơn là WHAT
Comment không chỉ mô tả *code đang làm gì*, mà phải giải thích:
- **Tại sao** cần đoạn code đó?
- Đoạn code giải quyết bài toán gì?
- Hệ thống nào phụ thuộc vào đoạn code này?
- Các trường hợp biên (Edge Cases) cần lưu ý?

*Ví dụ tốt (Java):*
```java
/**
 * Smoothly interpolates camera position toward target using Linear Interpolation (Lerp).
 * Prevents camera jitter when player changes movement direction rapidly on 60 FPS tick.
 */
public void update(double targetX, double targetY) {
    // Lerp factor balances responsiveness with visual smoothness
    this.x += (targetX - this.x) * LERP_FACTOR;
}
```

*Ví dụ chưa tốt:*
```java
// Cập nhật camera
public void update(double targetX, double targetY) { ... }
```

---

# 3. Quy định về Vibe Coding & Kiến trúc Mã nguồn

Mục tiêu của **Project DontKnow** là xây dựng mã nguồn có tính nhất quán cao, cấu trúc sạch đẹp, dễ mở rộng và đạt điểm tối đa tiêu chí OOP & SOLID của môn học.

---

## 3.1 Tư duy khi lập trình
- **Đơn giản hơn là thông minh**: Ưu tiên giải pháp sáng sủa, dễ đọc hơn là code mẹo phức tạp.
- **Dễ đọc hơn là ngắn**: Viết code tự diễn giải (Self-documenting code), tên biến và tên hàm rõ ràng.
- **Dễ mở rộng hơn là tối ưu sớm**: Tuân thủ SOLID, tránh over-engineering những tính năng chưa cần đến.
- **Refactor khi cần**: Giữ nguyên hành vi gameplay nhưng dọn dẹp cấu trúc khi phát hiện code bốc mùi (code smell).

---

## 3.2 Không phá vỡ kiến trúc (Kiểm soát God Object)
- **CẤM God Object**: Tuyệt đối không nhét toàn bộ logic game vào `GamePanel` hoặc `Player`. Mỗi tính năng (Bắn súng, Va chạm, AI, Quản lý trạng thái) phải thuộc về Class/System riêng biệt.
- **Phân tách trách nhiệm (Separation of Concerns)**:
  - `GameLoop`: Chỉ điều phối thời gian và nhịp cập nhật.
  - `State`: Chỉ xử lý logic của màn hình tương ứng.
  - `Entity`: Chứa dữ liệu và hành vi của đối tượng trong game.
  - `Physics`: Xử lý tính toán va chạm.
- Nếu phát hiện kiến trúc chưa hợp lý: Phải giải thích ưu/nhược điểm và đề xuất với người dùng trước khi sửa.

---

## 3.3 Chuẩn hóa phong cách Java (Coding Conventions)
- **Tên Class / Interface**: `PascalCase` (ví dụ: `Player`, `GameStateManager`, `BulletFactory`).
- **Tên Method / Variable**: `camelCase` (ví dụ: `calculateDamage()`, `aimAngle`, `currentHealth`).
- **Tên Hằng số**: `UPPER_SNAKE_CASE` (ví dụ: `TARGET_FPS`, `PLAYER_DEFAULT_SPEED`).
- **Hàm ngắn gọn**: Mỗi hàm chỉ nên làm đúng một nhiệm vụ duy nhất (Single Responsibility Principle).

---

## 3.4 Quy định về Quản lý Sprite, Hoạt ảnh & Đồ họa (Java 2D)
Vì dự án xây dựng trên nền **Java Swing / Graphics2D**, việc quản lý tài nguyên và hiển thị đồ họa phải tuân thủ nghiêm ngặt:

### Quản lý Asset tập trung (Tránh Memory Leak)
- **CẤM** đọc ảnh (`ImageIO.read()`) hoặc file âm thanh từ ổ cứng bên trong vòng lặp game (`update()` hoặc `render()`).
- Mọi hình ảnh (Spritesheet, Icon, Background) phải được nạp trước một lần thông qua `AssetManager` tập trung tại thời điểm khởi động game hoặc chuyển State.

### Quản lý Hoạt ảnh (Animation State Machine)
- Quản lý trạng thái chuyển động của thực thể bằng Enum rõ ràng:
  ```java
  public enum AnimationState {
      IDLE,
      WALK,
      SHOOT,
      DASH,
      HURT,
      DEATH
  }
  ```
- Tính toán chuyển frame dựa trên delta time (`elapsedTime += deltaTime`), không phụ thuộc vào tốc độ khung hình của máy tính.

---

## 3.5 Quy định về Đa luồng & An toàn Luồng (Concurrency Safety)
Dự án có sử dụng Đa luồng (luồng Game Loop 60 FPS tách biệt với EDT và Audio Thread):
- Danh sách các thực thể (Quái vật, Đạn bay, Hiệu ứng) được cập nhật trên Game Loop thread và vẽ trên Paint thread: Phải dùng cấu trúc an toàn luồng hoặc kỹ thuật snapshot (như `CopyOnWriteArrayList` hoặc danh sách buffer thêm/xóa `pendingAdd` / `pendingRemove`) để **tránh hoàn toàn ngoại lệ `ConcurrentModificationException`**.
- Không gọi các thao tác I/O nặng (ghi file điểm, load màn chơi lớn) làm tắc nghẽn luồng `GameLoop`.

---

## 3.6 Quy định về Kiểm thử (Unit Testing JUnit 5)
Để đáp ứng tiêu chí 15% điểm Testing của Rubric môn học:
- Mọi module logic cốt lõi (Công thức tính sát thương, Hệ thống va chạm Hitbox AABB, Thẻ nâng cấp perks, Lưu/Đọc bảng điểm) khi hoàn thành phải có ít nhất 1 tệp Unit Test tương ứng trong `src/test/java/`.
- Test case phải kiểm tra cả trường hợp bình thường lẫn trường hợp biên (Boundary/Edge Cases).

---

## 3.7 Hỏi ý kiến khi không chắc chắn
- Nếu một thay đổi có thể ảnh hưởng lớn đến Gameplay, Cân bằng vũ khí, hoặc Kiến trúc hệ thống: AI Agent phải hỏi ý kiến người dùng trước khi triển khai, không được tự ý sửa đổi theo sở thích cá nhân.