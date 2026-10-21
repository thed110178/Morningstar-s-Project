Cập nhật mới nhất vào ngày: 21/10/2026

# ScreenTransition & BossIntroCinematic - Chuyển Màn Và Giới Thiệu Boss

## 1. Mô tả
* [`ScreenTransition`](../../../src/main/java/com/groupproject/game/ui/ScreenTransition.java): hiệu ứng Fade đen (FADING_OUT → HOLD → FADING_IN) kèm banner tiêu đề/phụ đề. Callback `onHold` chạy khi màn hình tối hoàn toàn nên việc đổi state/dựng lại map không bị "giật cảnh".
* [`BossIntroCinematic`](../../../src/main/java/com/groupproject/game/ui/BossIntroCinematic.java): cắt cảnh 2.8s giới thiệu Boss: Letterbox trượt vào, camera lia Player → Boss (0.7s) → giữ + rung (1.4s) → quay về Player (0.7s), banner cảnh báo + tên Boss + chỉ số.

## 2. Quy trình hoạt động
1. **Chuyển state** ([`GameStateManager`](../../../src/main/java/com/groupproject/game/state/GameStateManager.java)): `transitionTo(type[, title, subtitle])` dùng 1 `ScreenTransition` toàn cục, được update/render trong `update()`/`render()` của manager. Đã dùng ở [`MenuState`](../../../src/main/java/com/groupproject/game/state/MenuState.java) (vào The Range / Boss Arena).
2. **Chuyển stage** ([`PlayingState`](../../../src/main/java/com/groupproject/game/state/PlayingState.java)): bấm [F] tại bệ extract → `requestAdvanceToNextStage()` → Fade (0.45s) → hold 0.35s (gọi `advanceToNextStage()`) → Fade in. Stage Boss dùng màu đỏ và phụ đề cảnh báo.
3. **Giới thiệu Boss**: `tryStartBossIntro()` chỉ chạy 1 lần/stage Boss, khi Boss đã spawn và còn sống.
4. Khi một trong hai hiệu ứng đang chạy, `PlayingState.update()` return sớm → gameplay đóng băng.

## 3. API
* `ScreenTransition.start(fade, hold, onHold, title, subtitle, accent)`, `isActive()`, `isBlockingInput()`.
* `BossIntroCinematic.start(TargetDummy, Player)`, `skip()` (chỉ code nội bộ/test; đã bỏ `handleInput`, cutscene không thể skip bằng phím/chuột), `getCameraFocusX/Y()`.
* `PlayingState.skipBossIntro()`, `isBossIntroActive()`, `isStageTransitionActive()` (dùng cho test/debug).

## 4. Quan hệ với hệ thống khác
Phụ thuộc `Camera` (focus tạm thời), `TargetDummy`/`BossTank`, `KeyboardHandler`, `MouseHandler`, `GameConfig`.

## 5. Sửa đổi / Cập nhật
* **21/10/2026**: Hoàn thiện nối `ScreenTransition` + `BossIntroCinematic` vào `PlayingState`; sửa `BossIntroCinematic` dùng đúng package `core.GameConfig` và kiểu `TargetDummy`.

## 6. Lỗi đã sửa
* **21/10/2026**: `BossIntroCinematic` import `config.GameConfig` và `CombatTarget` không tồn tại nên không biên dịch được → đổi sang `core.GameConfig` và `TargetDummy`.

