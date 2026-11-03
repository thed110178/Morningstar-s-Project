Cập nhật mới nhất vào ngày: 03/11/2026

# Semi-auto Shotgun (SAS) - Súng Săn Bán Tự Động

## 1. Mô Tả Tổng Quan
**Semi-auto Shotgun (SAS)** là vũ khí chính (Primary Weapon - Slot 1 hoặc 2) thuộc dòng súng săn cận chiến hỏa lực cao. Súng sử dụng cơ chế nạp đạn bằng hộp tiếp đạn (Box Magazine) và bắn ở chế độ bán tự động (Semi-Automatic), cho phép xả đạn với tốc độ cực nhanh khi người chơi nhấp cò liên tục.

So với khẩu **Shotgun truyền thống (Pump-Action)**:
- **Ưu điểm vượt trội**: Tốc độ ra đạn cực nhanh (cooldown 0.28s ~ 214 RPM so với 0.75s / 80 RPM), tạo ra lượng sát thương theo giây (DPS) cận chiến hủy diệt (~257 DPS so với ~112 DPS của Shotgun thường), áp đảo hoàn toàn khi đối đầu với bầy quái vây hãm hoặc Boss xe tăng bọc thép.
- **Nhược điểm cân bằng**: Chùm đạn có độ tỏa rộng hơn (±18.3° / 0.32 rad so với ±12.6° / 0.22 rad) và thời gian sống của đạn ngắn hơn (0.24s ~ 228 px so với 0.32s ~ 320 px), đòi hỏi người chơi phải cơ động áp sát mục tiêu ở cự ly gần để đạt hiệu quả tối đa.
- **Băng đạn hộp**: Băng đạn tiêu chuẩn 5 viên, thay nguyên hộp tiếp đạn trong 2.2s. Có khả năng mở rộng lên 8 viên khi kích hoạt kỹ năng buff Băng Đạn Mở Rộng (`EXTENDED_MAGAZINE`).

---

## 2. Thông Số Kỹ Thuật Chi Tiết

| Thuộc tính | Giá trị tiêu chuẩn (1 sao) | Cơ chế tính toán & Quy tắc cân bằng |
|---|---|---|
| **Sát thương (DMG)** | 12 DMG / pellet | Bắn ra 6 viên đạn chì (pellets), tối đa 72 DMG/phát ở cự ly gần. Nâng cấp: +20% mỗi sao sát thương. |
| **Thời gian hồi (Cooldown)** | 0.28s (~214 RPM) | Nhấp cò bán tự động liên hoàn. Nâng cấp: +15% RPM mỗi sao tốc bắn (cooldown = 0.28 / (1 + (stars - 1) * 0.15)). |
| **Góc tỏa đạn (Spread)** | ±18.3° (0.32 rad) | Tỏa rộng hơn shotgun thường. Nâng cấp: Thu hẹp theo `0.32 * pow(0.65, stars - 1)`. |
| **Băng đạn (Mag Capacity)** | 5 viên | Băng đạn tiêu chuẩn 5 viên. Tăng lên 8 viên khi có buff Băng Đạn Mở Rộng (`EXTENDED_MAGAZINE`). |
| **Thời gian nạp (Reload)** | 2.2 giây | Thay nguyên hộp tiếp đạn dạng Box Magazine, phát âm thanh `gun_SAS_reload.mp3`. |
| **Số viên chì / phát** | 6 pellets | Tốc độ đạn chì 950 px/s, thời gian tồn tại 0.24s (tầm bay tối đa ~228 px). |
| **Loại đạn tiêu thụ** | `AmmoType.SHOTGUN` | Tiêu hao 1 viên đạn súng săn từ kho đạn dự trữ cho mỗi phát bắn. |
| **Chế độ bắn** | Bán tự động (Semi-Auto) | `isAutomatic() == false`. Người chơi nhấp chuột cho từng phát bắn chiến thuật. |
| **Tỉ lệ chí mạng** | Ngẫu nhiên [10% - 35%] | Nhân hệ số sát thương chí mạng cấu hình theo súng (mặc định 1.50x - 2.00x). |

---

## 3. Quy Trình Hoạt Động (Mechanics & Workflow)
1. **Phát bắn chiến thuật (Attack Cycle)**:
   - Khi người chơi nhấp chuột trái, súng kiểm tra đạn và cooldown (`0.28s`).
   - Nếu súng đang nạp hộp tiếp đạn, phát bắn bị khóa để đảm bảo an toàn thao tác.
   - Khi bắn, 6 viên đạn chì được phóng ra từ đầu nòng theo góc nón `±18.3°` kèm độ rung lắc ngẫu nhiên `±0.025 rad`.
   - Hệ thống phát âm thanh tiếng súng nổ đanh gọn `GUN_SAS_SHOOT` (`gun_SAS_shoot.mp3`).
   - Đạn tiêu hao 1 viên. Nếu hết 5 viên, súng tự động bắt đầu nạp đạn.
2. **Nạp đạn hộp tiếp đạn (Box Magazine Reload)**:
   - Gọi `reload()`, súng kích hoạt trạng thái `reloading = true` với thời lượng `2.2s` và phát âm thanh `GUN_SAS_RELOAD`.
   - Nếu người chơi chuyển sang vũ khí khác trong khi đang nạp, tiến trình nạp được tạm dừng bảo lưu qua `pauseReload()` và âm thanh được ngắt ngay lập tức (`stopReload()`). Khi chuyển lại súng SAS, tiến trình nạp tiếp tục qua `resumeReload()`.
   - Khi hoàn tất chu kỳ, súng nạp đầy băng đạn (5 hoặc 8 viên) từ kho đạn dự trữ `AmmoType.SHOTGUN`.
3. **Mở rộng băng đạn (Extended Magazine)**:
   - Kỹ năng buff `EXTENDED_MAGAZINE` thông qua `PlayerBuffManager` tự động nâng trần băng đạn từ 5 lên 8 viên.
4. **Trải nghiệm miễn phí tại The Range**:
   - Khẩu SAS được trưng bày tại kệ số 4 (`WeaponRackStation`) cạnh bờ tường Nam của khu tập bắn The Range. Người chơi có thể đến bệ, bấm [F] để trang bị và bắn thử thoải mái với đạn vô hạn.

---

## 4. API & Phương Thức Chính
- `public SemiAutoShotgun()`: Khởi tạo súng SAS với slot mặc định [1].
- `public SemiAutoShotgun(int slotIndex)`: Khởi tạo với slot chỉ định.
- `public boolean attack(double originX, double originY, double aimAngle, ProjectileManager pm, List<MeleeSlash> slashList)`: Thực hiện phát bắn chùm 6 viên đạn chì.
- `public int getProjectilesPerShot()`: Trả về `6`.
- `public boolean isAutomatic()`: Trả về `false`.
- `public void render(...)`: Vẽ hình ảnh súng xám thép với hộp tiếp đạn đỏ đô trên tay nhân vật Operative.

---

## 5. Quan Hệ Với Hệ Thống Khác
- **WeaponManager**: Quản lý gán slot phím tắt, hiển thị nhãn `"SAS"` trên HUD, và phát âm thanh rút súng `GUN_SAS_DRAW` (`gun_SAS_draw.mp3`).
- **PlayerBuffManager**: Nâng cấp dung lượng băng đạn từ 5 lên 8 viên khi có buff `EXTENDED_MAGAZINE`.
- **GameSoundManager**: Nạp và giải mã 3 file âm thanh MP3 (`gun_SAS_draw.mp3`, `gun_SAS_reload.mp3`, `gun_SAS_shoot.mp3`) sang mảng PCM short[] bằng JLayer.
- **WeaponSpriteManager**: Quản lý nạp sprite `Semi-auto Shotgun.png` và vẽ Vector Fallback dự phòng.
- **WeaponChest & Shop**: Súng có tỷ lệ rơi 11% từ hòm chiến thuật và hỗ trợ bản nâng cấp Hạng S (`sTier`).
- **HubRangeState**: Kệ vũ khí miễn phí tại tọa độ `(1150, 860)` tại The Range.

