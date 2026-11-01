Cập nhật mới nhất vào ngày: 01/11/2026

# The Range - Căn Cứ & Trường Bắn Thử Nghiệm (HubRangeState)

## 1. Mô tả
`HubRangeState` là phân cảnh Hub tiền chiến dịch, đóng vai trò căn cứ dã chiến kiêm trường bắn huấn luyện trước khi người chơi chính thức bước vào các hầm ngục Dungeon. Tại đây, đặc nhiệm có thể tự do thử nghiệm cơ chế bắn súng, căn chỉnh góc tản đạn, làm quen với độ giật của các loại vũ khí, nâng cấp trang bị và kiểm tra DPS thực tế mà không phải đối mặt với nguy cơ tử trận.

### Các khu vực chức năng chính:
- **Khu vực trung tâm giới hạn di chuyển:** Vùng an toàn hình chữ nhật (X: 660 -> 1440, Y: 320 -> 920). Rào chắn năng lượng Cyan ngăn người chơi đi lạc vào trường bắn nhưng cho phép đạn bay xuyên qua 100%.
- **Trường bắn bên Phải (Recoil Pattern Tracer & Static Dummies):**
  - Gồm 3 bia Dummy đứng yên ở 3 cự ly chuẩn: Gần (180px), Vừa (380px), Xa (630px).
  - Khi bắn vào khu vực này, đường đạn vẽ các vệt dạ quang phát sáng (Recoil Tracer Lines) tồn tại trong đúng 3.0 giây rồi mờ dần, giúp người chơi quan sát trực quan độ nảy tâm và mẫu giật khi sấy đạn liên tục.
  - Hiển thị số sát thương nhảy lên (Damage Popups) kèm màu phân biệt giữa đòn chí mạng (Đỏ cam) và đòn thường (Vàng rực).
- **Trường bắn bên Trái (Tracking & Strafing Dummies):**
  - Gồm 4 bia Dummy di chuyển lạng lách (strafing) qua lại với tốc độ ngẫu nhiên.
  - Bảng điều khiển cảm ứng cho phép tùy chỉnh mốc máu: 100 HP, 250 HP, 500 HP, 1000 HP.
  - Khi bị hạ gục, Dummy tự động hồi sinh thay thế sau 0.65 giây.
- **Khu vực trang bị & Nâng cấp (100% Free):**
  - Kệ vũ khí đầy đủ 5 dòng súng chính: AR, SMG, Shotgun, SCR (Semi-Carbine Rifle), Sniper Rifle.
  - Bàn Nâng Cấp Tối Đa (Max Level 10) hoàn toàn miễn phí.
  - Bàn Tái Rèn (Reroll Station) quay lại thuộc tính sao (Damage, Fire Rate, Accuracy) và tỷ lệ chí mạng miễn phí.
  - Thùng rác (Trash Bin) dọn dẹp súng Slot 1 và 2 không dùng đến; bảo vệ an toàn tuyệt đối cho Súng lục (Slot 3) và Dao găm (Slot 4).
  - Kho đạn vô hạn: Luôn được làm đầy tự động.
- **Cổng Xuất Kích (Departure Gate):**
  - Bước vào cổng để bắt đầu chiến dịch Dungeon mang theo chính xác các vũ khí và chỉ số nâng cấp đã tinh chỉnh.

---

## 2. Quy trình hoạt động

```
[Main Menu] 
     │ (Chọn The Range)
     ▼
[Hub The Range] ◄───► [Thử súng tại 2 Trường Bắn: Bia đứng yên & Bia lạng lách]
     │                      │
     │                      ├── [Trang bị súng Free từ Kệ vũ khí]
     │                      ├── [Nâng cấp Max Cấp 10 Free]
     │                      └── [Tái rèn sao & Crit Free]
     │
     ▼ (Bước vào Cổng Xuất Kích)
[PlayingState - Dungeon Stage 1] (Bảo toàn 100% vũ khí đã trang bị)
```

1. **Khởi tạo:** Nạp Operative vào tọa độ trung tâm, nạp đạn vô hạn, dựng 3 Dummy đứng yên bên phải và 4 Dummy di động bên trái.
2. **Thao tác:** Người chơi di chuyển bằng WASD, ngắm bắn bằng chuột, đổi súng bằng phím 1-4 / Q, tương tác phím [E] khi đứng gần các trạm.
3. **Thử nghiệm:** Bắn thử vào trường bắn phải để xem vệt giật đạn dạ quang 3s; bắn vào trường bắn trái để rèn kỹ năng bám mục tiêu di động (tracking).
4. **Xuất kích:** Khi chuẩn bị xong, tương tác với Cổng Xuất Kích ở tường Bắc để chuyển giao toàn bộ vũ khí sang hầm ngục Dungeon.

---

## 3. API & Các Trạm Tương Tác

### Trạm Tương Tác (`HubStation`):
- `WeaponRackStation`: Trang bị súng miễn phí vào Slot 1 hoặc Slot 2.
- `HubUpgradeStation`: Nâng cấp vũ khí đang cầm lên 1 cấp độ miễn phí (tối đa Lv 10).
- `HubRerollStation`: Tái phân bổ ngẫu nhiên các chỉ số sao và thuộc tính chí mạng.
- `TrashBinStation`: Tháo súng Slot 1 hoặc Slot 2 đang cầm vào thùng rác; bảo vệ Slot 3 và 4.
- `DummyHPControllerStation`: Chuyển đổi các mốc máu Dummy di động (100 -> 250 -> 500 -> 1000 -> 100).
- `DepartureGateStation`: Kích hoạt chuyển giao súng và bắt đầu chiến dịch Dungeon.

### Quản lý vệt đạn & Sát thương:
- `RecoilTracerManager`: Quản lý các đoạn thẳng vệt đạn với thời gian tồn tại `LIFESPAN = 3.0s`.
- `DamagePopupManager`: Hiển thị số sát thương bay lên trên đầu Dummy khi trúng đạn.

---

## 4. Inspector Settings / Thông số cấu hình

| Thông số | Giá trị | Ý nghĩa |
|---|---|---|
| `WORLD_WIDTH` | 2300 px | Chiều rộng thế giới The Range |
| `WORLD_HEIGHT` | 1200 px | Chiều cao thế giới The Range |
| `CENTER_BOUND_MIN_X` | 660 px | Biên trái khu vực di chuyển Operative |
| `CENTER_BOUND_MAX_X` | 1440 px | Biên phải khu vực di chuyển Operative |
| `CENTER_BOUND_MIN_Y` | 320 px | Biên trên khu vực di chuyển Operative |
| `CENTER_BOUND_MAX_Y` | 920 px | Biên dưới khu vực di chuyển Operative |
| `LEFT_BARRIER_X` | 640 px | Tọa độ X rào chắn năng lượng bên trái |
| `RIGHT_BARRIER_X` | 1460 px | Tọa độ X rào chắn năng lượng bên phải |
| `RECOIL_TRACER_LIFESPAN` | 3.0 s | Thời gian vệt đạn dạ quang tồn tại |
| `RESPAWN_DELAY` | 0.65 s | Thời gian hồi sinh Dummy di động |

---

## 5. Quan hệ với hệ thống khác
- **`GameStateManager`:** Quản lý chuyển đổi trạng thái giữa `MENU`, `THE_RANGE` và `PLAYING`.
- **`PlayingState`:** Nhận tín hiệu bắt đầu chiến dịch từ The Range. Đảm bảo xóa sạch toàn bộ vũ khí thử nghiệm / Lv 10 tại The Range để Operative bước vào Stage 1 với trang bị khởi đầu tiêu chuẩn (Pistol + Knife).
- **`WeaponManager`:** Quản lý kho vũ khí, gắn kết kho đạn và các bộ đếm hồi chiêu.
- **`TargetDummy` & `StrafingTargetDummy`:** Các thực thể bia đỡ đạn nhận sát thương và kích hoạt vệt đạn recoil / popup sát thương.

---

## 6. Sửa đổi / Cập nhật
* **31/10/2026 (Cập nhật thảm cỏ & Tích hợp Autotiling 30 biến thể ranh giới)**:
  - Tái cấu trúc toàn diện hệ thống hiển thị cỏ của The Range theo mô hình 3 lớp đồng bộ với DungeonMap.
  - Phân bổ cỏ lõi dày liên tục ở 4 góc sân Hub ($2 \times 2$), ven chân tường bờ Bắc/Nam, dọc theo 2 mép rào chắn năng lượng và các cụm dã chiến giữa sân.
  - Tự động tính toán hoa văn ranh giới chuyển tiếp `Grass_1` đến `Grass_30` cho các ô sàn tiếp giáp cỏ (`updateHubGrassPatterns`).
  - Nướng hoàn toàn nền cỏ và hoa văn vào bộ đệm VRAM Surface Baking `bakedHubSurface`, duy trì 60.0 FPS mượt mà tuyệt đối.

- **21/10/2026:**
  - Khởi tạo mới hệ thống The Range theo yêu cầu thiết kế Hub tiền chiến dịch.
  - Tích hợp 2 trường bắn độc lập (Trường bắn phải vệt đạn 3 giây và Trường bắn trái bia di động đổi HP).
  - Thêm 5 giá súng đầy đủ 5 dòng súng chính bao gồm Semi-Carbine Rifle (SCR).
  - Tích hợp Bàn nâng cấp Lv 10 Free, Bàn Reroll Free, Thùng rác bảo vệ súng lục & dao, Cổng xuất kích vào Dungeon.
  - Đồng bộ phím tương tác [F] (`GameAction.INTERACT`) theo đúng chuẩn thiết lập Settings của trò chơi.
  - Loại bỏ hoàn toàn việc mang vũ khí thử nghiệm/Lv 10 vào Stage 1: Khi bước qua Cổng Xuất Kích, hệ thống xóa sạch toàn bộ súng ở The Range, Operative khởi đầu Stage 1 với trang bị mặc định (Pistol + Knife).
  - **Tích hợp toàn diện Hệ thống UI trong trận (In-Game Playing UI Integration):**
    * Loại bỏ thanh hotbar đơn sơ cũ ở đáy màn hình.
    * Tích hợp **HUD Vũ khí & Đạn dược chuẩn (`WeaponManager.renderHUD`)** ở góc dưới bên phải: Hiển thị đầy đủ tên súng, cấp độ súng, ảnh minh họa sprite, sao DMG/RPM/ACC, số đạn băng/dự trữ (RIF, SMG, SG, SR) và 4 ô súng `[1] [2] [3] [4]`.
    * Tích hợp **PlayingHUD** ở góc trên bên trái: Thanh máu `HP 100/100`, thanh hồi chiêu né đòn `Dodge [SPACE]` và huy hiệu Buff.
    * Tích hợp **Balo chiến thuật (`BackpackUI`)**: Nhấn `[TAB]` để bật/tắt Balo, xem chỉ số chi tiết và hoán đổi Slot 1 & 2.
    * Tích hợp **Menu Tạm dừng (`PauseMenuUI`)**: Nhấn `[ESC]` để tạm dừng, điều chỉnh Cài đặt phím/âm lượng hoặc Thoát về Menu.
    * Tích hợp kỹ năng né đòn **Combat Dash [SPACE]** ngay tại trường bắn.

---

## 7. Lỗi đã sửa
- **21/10/2026:**
  - **Khắc phục lỗi NullPointerException trong Player.handleMovement**: Khi ở Hub The Range (`dungeonMap == null`), `Player.handleMovement` trước đó gọi trực tiếp `dungeonMap.isAreaWalkable()` gây văng game. Đã bổ sung kiểm tra an toàn `dungeonMap == null` cho phép di chuyển và để HubRangeState kiểm soát biên giới hạn.
  - **Khắc phục sai lệch phím tương tác**: Sửa toàn bộ tooltip prompt và bắt phím tương tác từ phím [E] sang phím [F] theo đúng cấu hình `KeyBindings.GameAction.INTERACT`.
  - Khắc phục lỗi `UnsupportedOperationException` khi tháo súng vào thùng rác bằng cách bổ sung hàm `removeWeapon(int slotIndex)` an toàn vào `WeaponManager`.
  - Khắc phục lỗi tương thích tên gọi giữa `switchWeapon` và `selectSlot` trong `WeaponManager`.
  - Khắc phục lỗi căn biên va chạm rào chắn Operative không bị rung lắc.
  - **Khắc phục lỗi không hiển thị súng trên tay nhân vật**: Do `HubRangeState.render()` trước đó thiếu lời gọi `weaponManager.render(...)`. Đã bổ sung đầy đủ và liên kết biến `aimAngle` để vẽ súng xoay 360 độ theo hướng chuột.
  - **Khắc phục lỗi mất thanh tiến độ nạp đạn ở chân người chơi**: Bổ sung lời gọi `player.renderReloadIndicator(...)` và `player.renderOutOfAmmoWarning(...)` để hiển thị thanh đếm ngược nạp đạn (1.2s, 0.6s) và cảnh báo hết đạn ngay dưới chân Operative.
  - **Khắc phục lỗi đạn Sniper Rifle mất tính năng xuyên phá Dummy**: Do vòng lặp va chạm đạn trước đó gọi cưỡng chế `bullet.deactivate(); break;` khi chạm trúng bia đầu tiên. Đã khắc phục bằng cách sử dụng `bullet.canHit(dummy)` và `bullet.onHitTarget(dummy); if (!bullet.isActive()) break;`, khôi phục trọn vẹn khả năng xuyên qua nhiều Dummy liên tiếp của Sniper Rifle.
- **01/11/2026:**
  - **Khắc phục lỗi vệt đạn tracer bị bẻ góc hướng về tâm Dummy**: Khi đạn súng lục, AR, SMG, Shotgun va chạm vào bia TargetDummy hoặc StrafingTargetDummy, hệ thống trước đó truyền `dummy.getX(), dummy.getY()` vào `tracerManager.addTracer(...)`. Vì đây là tọa độ tâm của bia nên mọi đường đạn đều bị tụ lại vào chấm vàng ở giữa Dummy. Đã sửa thành truyền `bullet.getX(), bullet.getY()`, giúp tia tracer laser kết thúc tại đúng điểm va chạm thực tế của viên đạn, phản ánh trung thực mẫu giật súng (Recoil pattern) và góc tản đạn.
