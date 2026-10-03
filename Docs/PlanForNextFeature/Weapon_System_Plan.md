Cập nhật mới nhất vào ngày: 03/10/2026

# KẾ HOẠCH TRIỂN KHAI: HỆ THỐNG VŨ KHÍ MỞ RỘNG (WEAPON SYSTEM)
*Trang bị ban đầu: Súng lục (Slot 3) & Dao găm Cận chiến (Slot 4)*

---

## 1. Mục tiêu tính năng
Thiết kế kiến trúc hệ thống vũ khí linh hoạt, tuân thủ nguyên lý **Open/Closed Principle (SOLID)** và **Strategy Pattern**, cho phép dễ dàng bổ sung các loại súng mới (Shotgun, Rifle, Sniper, Rocket) hoặc vũ khí cận chiến (Kiếm, Búa) mà không phải chỉnh sửa code cũ.

Cụ thể triển khai 2 vũ khí khởi đầu:
1. **Súng lục Chiến thuật (Tactical Pistol - Phím 3)**:
   * Cơ số đạn: Băng đạn **7 viên**.
   * Bắn từng phát chính xác theo góc trỏ chuột 360 độ.
   * Tự động nạp đạn khi hết 7 viên hoặc nạp chủ động khi bấm phím **R**.
   * Đạn bay theo vector lượng giác, va chạm tường sẽ bị hủy.
2. **Dao găm Tác chiến (Combat Knife - Phím 4)**:
   * Vũ khí cận chiến (Melee), không tốn đạn, không cần nạp.
   * Tạo 1 ô Hitbox sát thương hình chữ nhật/cung chém ở phía trước nhân vật theo hướng chuột.
   * Hiển thị vệt chém ánh kim (Slash Arc Effect) thời gian thực.

---

## 2. Kiến trúc Module (Package `com.groupproject.game.entity.weapon` & `projectile`)

```
src/main/java/com/groupproject/game/
├── entity/
│   ├── projectile/
│   │   ├── Projectile.java         // Viên đạn bay (tọa độ, vận tốc, sát thương, tầm bay)
│   │   └── ProjectileManager.java  // Quản lý danh sách đạn bay an toàn luồng (CopyOnWrite/Buffer)
│   └── weapon/
│       ├── Weapon.java             // Lớp cơ sở trừu tượng (Base Abstract Weapon)
│       ├── Pistol.java             // Súng lục 7 viên
│       ├── CombatKnife.java        // Dao cận chiến tạo hitbox theo góc chuột
│       ├── MeleeSlash.java         // Hiệu ứng vệt chém cận chiến
│       └── WeaponManager.java      // Quản lý kho đồ, chuyển đổi slot phím (3, 4, Q)
```

---

## 3. Quy trình tính toán & Thuật toán

### 3.1 Bắn đạn Súng lục (Pistol)
* Tọa độ xuất phát: Từ đầu nòng súng ($x + \cos(\theta) \times 24$, $y + \sin(\theta) \times 24$).
* Vận tốc đạn: $v_x = \cos(\theta) \times 650$, $v_y = \sin(\theta) \times 650$ (pixel/giây).
* Trừ 1 viên đạn trong băng. Nếu đạn còn 0, kích hoạt trạng thái `isReloading = true` với thời gian 1.2s.

### 3.2 Nhát chém Dao găm (Combat Knife)
* Tính tâm ô Hitbox phía trước theo hướng chuột:
  $$x_{\text{box}} = x_{\text{player}} + \cos(\theta) \times 36 - \frac{\text{size}}{2}$$
  $$y_{\text{box}} = y_{\text{player}} + \sin(\theta) \times 36 - \frac{\text{size}}{2}$$
  với kích thước ô $\text{size} = 44$ px (xấp xỉ 1 ô tile $48 \times 48$).
* Sinh đối tượng `MeleeSlash` tồn tại trong 0.12 giây để hiển thị cung chém ánh kim trực quan.

### 3.3 Đổi Vũ khí mượt mà
* Lắng nghe phím số **3** $\rightarrow$ Trang bị Pistol.
* Lắng nghe phím số **4** $\rightarrow$ Trang bị Knife.
* Phím **Q** $\rightarrow$ Đổi nhanh qua lại giữa vũ khí vừa dùng.

---

## 4. Kế hoạch Kiểm thử (Unit Testing)
Tạo `src/test/java/com/groupproject/game/weapon/WeaponSystemTest.java`:
* Kiểm tra Pistol khởi đầu có 7 viên đạn.
* Bắn 7 phát liên tiếp $\rightarrow$ súng chuyển sang trạng thái Reload.
* Kiểm tra dao Knife không bị giới hạn bởi đạn.
* Kiểm tra chuyển đổi vũ khí giữa Slot 3 và Slot 4 hoạt động chính xác.
