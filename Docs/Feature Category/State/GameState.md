Cập nhật mới nhất vào ngày: 02/10/2026

# GameState.java - Lớp Cơ Sở Trạng Thái Game (State Pattern)

## 1. Mô tả
`GameState.java` là lớp trừu tượng (Abstract Base Class) định nghĩa bộ khung hành vi chung cho mọi màn hình trong trò chơi theo mẫu thiết kế **State Pattern**. Mỗi trạng thái trò chơi (như Menu chính, Màn chơi chiến đấu, Màn hình tạm dừng, Màn hình nâng cấp thẻ bài, Màn hình Game Over) đều phải kế thừa từ lớp này.

---

## 2. Quy trình hoạt động
1. Khi khởi tạo một `GameState`, nó luôn nhận và giữ tham chiếu tới `GameStateManager` (`gsm`) để có thể yêu cầu chuyển đổi trạng thái khi cần (ví dụ: từ Menu chuyển sang Playing khi bấm Start).
2. Lớp định nghĩa 3 phương thức trừu tượng cốt lõi:
   * `init()`: Được gọi mỗi khi trạng thái này được kích hoạt hoặc khởi động lại.
   * `update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH)`: Được gọi mỗi tick 60 UPS để xử lý logic, input và vật lý của trạng thái đó.
   * `render(Graphics2D g)`: Được gọi mỗi frame để vẽ giao diện và đối tượng của trạng thái đó lên canvas.

---

## 3. API
```java
public abstract class GameState {
    protected final GameStateManager gsm;
    public GameState(GameStateManager gsm)
    public abstract void init();
    public abstract void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH);
    public abstract void render(Graphics2D g);
}
```

---

## 4. Quan hệ với hệ thống khác
* Là lớp cha của: `MenuState`, `PlayingState`, và sau này là `PauseState`, `UpgradeState`, `GameOverState`.
* Được quản lý tập trung bởi: `GameStateManager`.

---

## 5. Sửa đổi / Cập nhật
* **02/10/2026**: Khởi tạo ban đầu với cấu trúc State Pattern chuẩn.

---

## 6. Lỗi đã sửa
Chưa ghi nhận lỗi.
