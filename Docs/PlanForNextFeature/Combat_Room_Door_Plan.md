Cập nhật mới nhất vào ngày: 03/10/2026

# KẾ HOẠCH TRIỂN KHAI: HỆ THỐNG CỬA PHÒNG CHIẾN ĐẤU (COMBAT ROOM LOCKDOWN DOORS)

## 1. Mục tiêu tính năng
Tái hiện cơ chế cốt lõi của dòng game Roguelite Dungeon Crawler (**Soul Knight / Enter the Gungeon**):
* Khi người chơi bước chân vào **Combat Room**:
  * Các lối ra vào (Cửa Tây & Cửa Đông) lập tức đóng sập lại thành **`DOOR_CLOSED`** (`isSolid = true`), khóa chặt người chơi bên trong đấu trường.
  * Xuất hiện thông báo cảnh báo: `[ COMBAT ZONE LOCKDOWN - ELIMINATE ALL TARGETS ]`.
* Trong phòng xuất hiện các mục tiêu thử nghiệm (Combat Targets / Enemies) có lượng máu HP. Người chơi sử dụng Súng lục (Slot 3) và Dao găm (Slot 4) để tiêu diệt chúng.
* Khi toàn bộ mục tiêu trong phòng bị quét sạch (Room Cleared):
  * Cửa phòng tự động chuyển thành **`DOOR_OPEN`** (`isSolid = false`), dỡ bỏ hoàn toàn vật cản.
  * Xuất hiện thông báo: `[ ROOM CLEARED - DOORS UNLOCKED ]`.
  * Người chơi có thể tự do tiến qua hành lang phía Đông để tới **Extract Room**.

---

## 2. Kiến trúc & Các lớp liên quan

```
src/main/java/com/groupproject/game/
├── dungeon/
│   ├── TileType.java          // Thêm DOOR_CLOSED (solid=true) và DOOR_OPEN (solid=false)
│   ├── Tile.java              // Cập nhật phương thức render vẽ rào chắn cửa laser/sắt đỏ và xanh
│   ├── Room.java              // Thêm trạng thái phòng (UNVISITED, ACTIVE_LOCKDOWN, CLEARED) và quản lý doorTiles
│   └── DungeonMap.java        // Tích hợp gán cửa tại các điểm nghẽn hành lang Combat Room
└── entity/
    └── enemy/
        └── TargetDummy.java   // Mục tiêu thử nghiệm có máu (HP) để bắn/chém kiểm tra cơ chế mở cửa
```

---

## 3. Quy trình tính toán & Thuật toán

### 3.1 Bố trí vị trí Cửa phòng Combat (Door Coordinates)
* **Cửa Tây (West Door)**: Tiếp giáp giữa Corridor 1 và Combat Arena.
  * Tọa độ: Cột $c = 28$, Hàng $r = 21, 22$.
* **Cửa Đông (East Door)**: Tiếp giáp giữa Combat Arena và Corridor 2.
  * Tọa độ: Cột $c = 51$, Hàng $r = 21, 22$.
* Mặc định ban đầu khi chưa bước vào: Cửa ở trạng thái mở `DOOR_OPEN` để người chơi có thể bước vào phòng.

### 3.2 Chu trình Trạng thái Phòng (Room State Lifecycle)
```text
[ UNVISITED ]
     │ (Người chơi bước vào phòng)
     ▼
[ ACTIVE_LOCKDOWN ] ──> Cửa đóng: DOOR_CLOSED (isSolid = true)
     │ (Tiêu diệt hết mục tiêu trong phòng)
     ▼
[ CLEARED ] ──────────> Cửa mở: DOOR_OPEN (isSolid = false)
```

---

## 4. Kế hoạch Kiểm thử (Unit Testing)
Tạo `src/test/java/com/groupproject/game/dungeon/CombatDoorTest.java`:
* Kiểm tra ban đầu cửa ở trạng thái mở `DOOR_OPEN` (`isSolid == false`).
* Gọi `combatRoom.lockdown(dungeonMap)`: Tất cả các ô cửa chuyển thành `DOOR_CLOSED` (`isSolid == true`), kiểm tra `isAreaWalkable()` tại cửa trả về `false`.
* Gọi `combatRoom.unlock(dungeonMap)`: Các ô cửa chuyển thành `DOOR_OPEN` (`isSolid == false`), kiểm tra `isAreaWalkable()` tại cửa trả về `true`.
