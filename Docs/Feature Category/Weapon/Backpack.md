Cập nhật mới nhất vào ngày: 20/10/2026

# Balo Chiến Thuật & Hồ Sơ Nhân Vật (Tactical Backpack & Character Profile)

## 1. Mô tả
Hệ thống Balo chiến thuật (`BackpackUI`) cho phép người chơi bấm phím **[Tab]** bất kỳ lúc nào trong trận đấu để mở thiết bị liên lạc tác chiến dã chiến (Field Tactical PDA).
Giao diện được thiết kế theo phong cách Cyberpunk Sci-Fi Glassmorphism mờ sang trọng với hệ thống **Chuyển Tab (Tabbed View)** trực quan:
* **Tab 1: `[Q] ⚔ KHO KHÍ TÀI & ĐẠN`**: Quản lý 4 slot vũ khí (Slot 1 & 2 vũ khí chính, Slot 3 súng lục, Slot 4 dao găm), hoán đổi nhanh vị trí Slot 1 ⇄ Slot 2 bằng phím **[SPACE] / [S]** hoặc click chuột, hiển thị đạn dự trữ và thông số súng.
* **Tab 2: `[E] 🧬 HỒ SƠ & PERKS`**: Hiển thị toàn diện dữ liệu sinh trắc học và tiến trình cường hóa của chiến binh, bao gồm:
  - **Thanh tóm tắt sinh tồn**: Máu thực tế (HP bar), Công lực %, Tốc độ %, Tỉ lệ giảm thương %, Gia tốc hồi né %, Chỉ số may mắn %.
  - **6 Chỉ số nâng cấp cơ bản (Basic Stats)**: Máu tối đa, Sát thương, Tốc độ chạy, Phòng thủ, Hồi chiêu né lướt, May mắn nhặt đồ (kèm badge cấp độ `Lv.X/Y`, vạch ô vuông pips tiến trình và mô tả chỉ số).
  - **7 Kỹ năng đặc biệt (Special Perks)**: Giáp chặn đòn hồi phục, Diệt địch hồi máu, Né xuyên vật cản, Thu nhỏ thân thể, Băng đạn tiếp tế chu kỳ, Băng đạn mở rộng, Bậc thầy chí mạng (với thẻ phân loại trực quan: dạ quang lục viền neon khi `[ĐÃ KÍCH HOẠT]`, xám mờ tối khi `[CHƯA CÓ]`).

---

## 2. Quy trình hoạt động
```
Người chơi đang trong trận đấu
       │
       ▼ [Bấm phím TAB]
Mở giao diện Balo (Tạm dừng di chuyển & bắn súng)
       │
       ├───> [Bấm phím Q hoặc Click Tab 1]: Chuyển sang TAB KHO KHÍ TÀI & ĐẠN
       │       ├──> Hiển thị Slot [1]: Súng chính 1, Cấp độ, Sao chỉ số, Đạn, Chế độ
       │       ├──> Hiển thị Slot [2]: Súng chính 2, Cấp độ, Sao chỉ số, Đạn, Chế độ
       │       ├──> [Nút ⇄ ĐỔI VỊ TRÍ / SPACE / S]: Hoán đổi tức thì Slot 1 ⇄ Slot 2
       │       ├──> Hiển thị Slot [3]: Súng lục phụ & Đạn
       │       └──> Hiển thị Slot [4]: Dao găm cận chiến & Đạn dự trữ toàn kho
       │
       ├───> [Bấm phím E hoặc Click Tab 2]: Chuyển sang TAB HỒ SƠ & PERKS
       │       ├──> Thanh sinh trắc học tổng quan: HP Bar, Tấn công %, Tốc độ %, Giảm thương %, Hồi né %, May mắn %
       │       ├──> Khung 6 Buff cơ bản (Basic Stats): Badge Cấp độ (Lv.X/Y), Vạch Pips tiến trình, Giá trị tăng
       │       └──> Khung 7 Kỹ năng đặc biệt (Special Perks): Card trạng thái kích hoạt [ĐÃ KÍCH HOẠT] hoặc [CHƯA CÓ]
       │
       ▼ [Bấm phím TAB / ESC hoặc Click nút X]
Đóng giao diện Balo, tiếp tục trận chiến!
```

---

## 3. API & Các phương thức chính

### 3.1 `BackpackUI.java`
* `public enum BackpackTab { WEAPONS, CHARACTER_PERKS }`: Enum định nghĩa các tab chức năng.
* `public BackpackTab getActiveTab()` / `public void setActiveTab(BackpackTab tab)`: Truy vấn và chuyển đổi Tab đang kích hoạt.
* `public boolean update(KeyboardHandler keyH, MouseHandler mouseH, WeaponManager weaponManager, Player player, double deltaTime)`:
  - Bắt phím tắt `Q` / `E` hoặc click chuột vào 2 Tab Header để chuyển tab.
  - Xử lý click nút đổi vị trí vũ khí, chọn slot vũ khí nhanh (khi ở Tab `WEAPONS`).
  - Trả về `true` khi người chơi yêu cầu đóng Balo (`ESC` hoặc click nút `[X]`).
* `public void render(Graphics2D g, WeaponManager weaponManager, Player player, int screenWidth, int screenHeight)`:
  - Vẽ nền bán trong suốt Glassmorphism và viền Cyberpunk HUD.
  - Vẽ 2 nút Tab Navigation Header ở phía trên cùng với phản hồi hover / active.
  - Phân luồng hiển thị: `renderWeaponsTab` (khi `activeTab == WEAPONS`) hoặc `renderCharacterPerksTab` (khi `activeTab == CHARACTER_PERKS`).
* `private void renderCharacterPerksTab(Graphics2D g, Player player, int panelX, int panelY)`:
  - Vẽ thanh chỉ số sinh trắc học tổng quan (Quick Vitals Summary).
  - Vẽ bảng 6 chỉ số nâng cấp cơ bản với hàm `renderBasicStatRow`.
  - Vẽ bảng 7 thẻ Perk kỹ năng đặc biệt với hàm `renderSpecialPerkCard`.

### 3.2 `PlayerBuffManager.java`
* Cung cấp các hàm kiểm tra trạng thái kích hoạt:
  - `getLevel(CharacterBuff buff)`: Lấy cấp độ nâng cấp hiện tại (0..max).
  - `hasOneHitBarrier()`: Có Giáp chặn đòn hồi phục hay không.
  - `hasKillHeal()`: Có Diệt địch hồi máu hay không.
  - `hasPhaseDodge()`: Có Né xuyên vật cản hay không.
  - `hasShrinkHitbox()`: Có Thu nhỏ thân thể hay không.
  - `hasPeriodicAmmo()`: Có Băng đạn tiếp tế tự động hay không.
  - `hasExtendedMagazine()`: Có Băng đạn mở rộng hay không.
  - `hasCriticalMastery()`: Có Bậc thầy chí mạng hay không.

---

## 4. Bảng phím tắt & Thao tác điều khiển
| Thao tác | Phím tắt / Chuột | Chức năng |
|---|---|---|
| Mở / Đóng Balo | `[TAB]` | Bật hoặc tắt giao diện Balo tác chiến bất kỳ lúc nào |
| Đóng Balo | `[ESC]` hoặc Click `[X]` | Thoát khỏi giao diện Balo |
| Chuyển Tab Khí Tài | `[Q]` hoặc Click Tab 1 | Xem 4 slot vũ khí, đạn và hoán đổi vị trí |
| Chuyển Tab Hồ Sơ | `[E]` hoặc Click Tab 2 | Xem chỉ số sinh tồn, 6 nâng cấp cơ bản và 7 perks |
| Đổi vị trí Slot 1 & 2 | `[SPACE]` hoặc `[S]` hoặc Click nút `[⇄]` | Hoán đổi vị trí vũ khí chính (ở Tab Khí Tài) |
| Trang bị nhanh | `[1]`, `[2]`, `[3]`, `[4]` hoặc Click ô | Cầm vũ khí tương ứng lên tay (ở Tab Khí Tài) |

---

## 5. Quan hệ với hệ thống khác
* **`PlayingState`**: Lưu giữ trạng thái mở balo, điều phối đối tượng `Player` và `WeaponManager` vào hàm `update` và `render` của `BackpackUI`.
* **`PlayerBuffManager`**: Nguồn dữ liệu thời gian thực cung cấp cấp độ buff và cờ kích hoạt của các perk đặc biệt.
* **`Player`**: Cung cấp thông tin lượng máu hiện tại và máu tối đa.
* **`WeaponManager`**: Quản lý các slot vũ khí, kho đạn dự trữ và hoán đổi slot.

---

## 6. Sửa đổi / Cập nhật
* **20/10/2026**:
  - **Tích hợp Hiển thị Perk và Nâng cấp Nhân vật vào Balo**: Nâng cấp giao diện Balo thành Sổ tay tác chiến dã chiến 2 Tab chuyển đổi linh hoạt:
    + Tab 1: `[Q] ⚔ KHO KHÍ TÀI & ĐẠN` giữ nguyên đầy đủ chức năng quản lý vũ khí, hoán đổi slot 1 & 2 và xem đạn.
    + Tab 2: `[E] 🧬 HỒ SƠ & PERKS` hiển thị toàn diện tình trạng sinh tồn, tiến trình nâng cấp của 6 chỉ số cơ bản (có vạch pips tiến trình và badge cấp độ) cùng 7 kỹ năng đặc biệt (với thẻ card trực quan phân biệt rõ trạng thái `[ĐÃ KÍCH HOẠT]` phát quang lục neon hoặc `[CHƯA CÓ]` xám mờ).
  - Bổ sung phím tắt chuyển Tab `[Q]` và `[E]`, cùng cơ chế click chuột mượt mà trên 2 Tab Header.
  - Bổ sung phương thức tiện ích `hasKillHeal()` trong `PlayerBuffManager` nhằm chuẩn hóa API truy vấn perk.
* **16/10/2026**:
  - **Sửa lỗi không mở được Balo bằng phím [Tab]**: Loại bỏ việc kiểm tra phím TAB trùng lặp bên trong `BackpackUI.update()`, trao quyền kiểm soát toggle mở/đóng duy nhất cho `PlayingState`.
  - Bổ sung hiển thị thông tin cấp độ súng (Level) và số lần nâng cấp `[NÂNG: X/3]` cùng 3 hàng sao chỉ số trực quan ngay trên từng thẻ súng trong Balo.
