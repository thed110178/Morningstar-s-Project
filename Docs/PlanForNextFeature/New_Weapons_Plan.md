Cập nhật mới nhất vào ngày: 06/10/2026

# KẾ HOẠCH TRIỂN KHAI: HỆ THỐNG VŨ KHÍ MỚI (ASSAULT RIFLE & SHOTGUN)

## 1. Mục tiêu
Mở rộng kho vũ khí của nhân vật đặc vụ với 2 loại vũ khí mới linh hoạt gắn trên phím số [1] và phím số [2]:
1. **Súng trường tấn công (Assault Rifle - AR) [Phím 1]**:
   * Băng đạn: **30 viên**.
   * Tốc độ bắn: **700 viên / phút** (tương đương cooldown $\approx 0.0857$ giây/viên).
   * Cơ chế: Giữ chuột trái để tự động xả đạn liên thanh (Full-Auto).
   * Độ lệch đạn (Spread/Recoil): Dao động nhẹ $\pm 2.5^\circ$ tạo cảm giác súng quân dụng chân thực.
2. **Súng săn (Shotgun) [Phím 2]**:
   * Cơ chế bắn: Bắn chùm gồm **6 viên đạn chì (pellets)** xòe hình nón góc $\pm 13^\circ$.
   * Sát thương tầm gần: Mỗi viên gây $15$ sát thương (tổng $90$ sát thương nếu trúng trọn ở tầm gần, hạ gục ngay mục tiêu 75 HP).
   * Giới hạn tầm bắn: Đạn chì có thời gian tồn tại ngắn ($0.32$ giây với vận tốc $640$ px/s $\approx 205$ px), tự động tiêu biến khi ra khỏi cự ly hiệu quả.
   * Băng đạn: **8 viên**, thời gian nạp $2.2$ giây, thời gian giãn cách mỗi phát bắn $0.75$ giây.

---

## 2. Thiết kế Kiến trúc Mã Nguồn

```
src/main/java/com/groupproject/game/entity/weapon/
├── Weapon.java            (Lớp cơ sở trừu tượng - Strategy Pattern)
├── Pistol.java            (Súng lục 7 viên - Phím 3)
├── CombatKnife.java       (Dao găm cận chiến - Phím 4)
├── AssaultRifle.java      (MỚI - Súng trường 30 viên, 700 RPM - Phím 1)
├── Shotgun.java           (MỚI - Súng săn 6 đạn chùm tầm gần - Phím 2)
└── WeaponManager.java     (Quản lý 4 slot, phím tắt 1/2/3/4, đổi nhanh [Q], nạp đạn [R])
```

---

## 3. Các bước triển khai

### Bước 1: Tạo lớp `AssaultRifle.java`
* Kế thừa `Weapon`.
* Cấu hình: `slotIndex = 1`, `damage = 16`, `fireRateCooldown = 60.0 / 700.0`, `magazineSize = 30`, `reloadDuration = 1.8`.
* Triển khai hàm `attack(...)` với độ tản mát góc nhỏ ngẫu nhiên, sinh đạn đạn đạo tầm xa.
* Triển khai hàm `render(...)` vẽ nòng súng dài, ốp tay cầm và loa che lửa.

### Bước 2: Tạo lớp `Shotgun.java`
* Kế thừa `Weapon`.
* Cấu hình: `slotIndex = 2`, `damage = 15` mỗi viên chì, `fireRateCooldown = 0.75`, `magazineSize = 8`, `reloadDuration = 2.2`.
* Triển khai hàm `attack(...)` sinh đồng thời 6 tia đạn phân bố đều trên góc nón với thời gian sống `lifetime = 0.32`s.
* Triển khai hàm `render(...)` vẽ nòng súng đôi cỡ nòng lớn màu xám thép nòng súng săn.

### Bước 3: Cập nhật `WeaponManager.java` & `PlayingState.java`
* Thêm `AssaultRifle` và `Shotgun` vào `weaponSlots`.
* `PlayingState`: Bổ sung lắng nghe phím `VK_1` / `VK_NUMPAD1` và `VK_2` / `VK_NUMPAD2`.
* Cập nhật giao diện thanh HUD góc dưới bên phải hiển thị đầy đủ hướng dẫn phím:
  `[1] AR | [2] Shotgun | [3] Pistol | [4] Knife | [Q] Swap | [R] Reload`.

### Bước 4: Viết Unit Tests & Tài liệu
* Mở rộng `WeaponSystemTest.java` kiểm tra xả đạn liên thanh AR và chùm đạn shotgun tầm gần.
* Tạo tài liệu `AssaultRifle.md`, `Shotgun.md` trong `Docs/Feature Category/Weapon/`.
* Cập nhật nhật ký ngày mới `Docs/Update/26-10-06_01.md`.
