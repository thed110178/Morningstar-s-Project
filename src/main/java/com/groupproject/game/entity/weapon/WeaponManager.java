package com.groupproject.game.entity.weapon;

import com.groupproject.game.core.Camera;
import com.groupproject.game.entity.projectile.ProjectileManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the operative's firearm arsenal, hotkey cycling, and ammunition HUD rendering.
 *
 * <p>WHY: Decouples weapon switching and inventory storage from the player's core locomotion
 * logic. Supports easy expansion for additional weapon slots (e.g. primary assault rifles on slot 1,
 * shotguns on slot 2) while keeping existing weapon mechanics untouched.
 */
public class WeaponManager {

    private final Map<Integer, Weapon> weaponSlots = new HashMap<>();
    private final List<MeleeSlash> activeSlashes = new ArrayList<>();
    private final com.groupproject.game.entity.item.AmmoInventory ammoInventory = new com.groupproject.game.entity.item.AmmoInventory();

    private Weapon activeWeapon;
    private Weapon previousWeapon;

    public WeaponManager() {
        // Khởi đầu người chơi chỉ có súng lục phụ (Slot 3) và dao găm (Slot 4)
        // Chưa có vũ khí chính (Slot 1 và Slot 2 sẽ được mở khóa ngẫu nhiên khi nhặt hòm)
        Pistol pistol = new Pistol();
        CombatKnife knife = new CombatKnife();

        addWeapon(pistol);
        addWeapon(knife);

        // Mặc định ban đầu kích hoạt ô số 3 (Pistol) và phụ là ô số 4 (Knife)
        this.activeWeapon = pistol;
        this.previousWeapon = knife;
    }

    /**
     * Resets loadout to starting setup (Pistol + Knife only) and resets ammo reserves.
     */
    public void reset() {
        ammoInventory.reset();
        weaponSlots.clear();
        activeSlashes.clear();

        Pistol pistol = new Pistol();
        CombatKnife knife = new CombatKnife();

        addWeapon(pistol);
        addWeapon(knife);

        this.activeWeapon = pistol;
        this.previousWeapon = knife;
    }

    /**
     * Registers and immediately equips a newly collected primary weapon.
     * Tự động ưu tiên xếp vào Slot 1 nếu Slot 1 đang trống, hoặc Slot 2 nếu Slot 1 đã có súng.
     * Nếu cả 2 slot chính đều đã đầy, thay thế súng đang cầm (hoặc slot 1) và trả về súng cũ để rơi ra đất.
     *
     * @param weapon Vũ khí mới cần trang bị.
     * @return Khẩu súng cũ bị thay thế nếu cả 2 slot đều đã có súng; {@code null} nếu lấp vào slot trống.
     */
    public Weapon equipWeapon(Weapon weapon) {
        if (weapon == null) {
            return null;
        }

        Weapon replacedWeapon = null;
        // Nếu là vũ khí chính (không phải súng lục slot 3 hay dao slot 4)
        if (!weapon.isMelee() && weapon.getSlotIndex() != 3 && weapon.getSlotIndex() != 4) {
            if (!weaponSlots.containsKey(1)) {
                weapon.setSlotIndex(1);
            } else if (!weaponSlots.containsKey(2)) {
                weapon.setSlotIndex(2);
            } else {
                // Cả Slot 1 và Slot 2 đều đã có vũ khí chính:
                // Thay thế vào slot đang cầm (nếu đang cầm slot 1 hoặc slot 2), ngược lại ưu tiên slot 1
                int targetSlot = (activeWeapon != null && activeWeapon.getSlotIndex() == 2) ? 2 : 1;
                replacedWeapon = weaponSlots.get(targetSlot);
                weapon.setSlotIndex(targetSlot);
            }
        }
        addWeapon(weapon);
        previousWeapon = activeWeapon;
        activeWeapon = weapon;
        return replacedWeapon;
    }

    /**
     * Hoán đổi vị trí của 2 vũ khí chính giữa Slot [1] và Slot [2].
     *
     * <p>WHY: Cho phép người chơi tự do sắp xếp vũ khí yêu thích vào ô số 1 hoặc ô số 2
     * thông qua giao diện Balo (Tab) hoặc phím tắt hoán đổi.
     *
     * @return {@code true} nếu hoán đổi thành công; {@code false} nếu cả 2 slot đều trống.
     */
    public boolean swapPrimarySlots() {
        Weapon w1 = weaponSlots.get(1);
        Weapon w2 = weaponSlots.get(2);

        if (w1 == null && w2 == null) {
            return false;
        }

        weaponSlots.remove(1);
        weaponSlots.remove(2);

        if (w1 != null) {
            w1.setSlotIndex(2);
            weaponSlots.put(2, w1);
        }
        if (w2 != null) {
            w2.setSlotIndex(1);
            weaponSlots.put(1, w2);
        }

        return true;
    }

    /**
     * Registers a weapon into its assigned slot and binds player's ammo inventory.
     */
    public void addWeapon(Weapon weapon) {
        if (weapon != null) {
            weapon.bindAmmoInventory(ammoInventory);
            weaponSlots.put(weapon.getSlotIndex(), weapon);
        }
    }

    /**
     * Switches the active weapon using its direct slot number (e.g. 3 or 4).
     */
    public void selectSlot(int slotIndex) {
        Weapon selected = weaponSlots.get(slotIndex);
        if (selected != null && selected != activeWeapon) {
            previousWeapon = activeWeapon;
            activeWeapon = selected;
        }
    }

    /**
     * Quick-swaps between the current weapon and the previously equipped weapon (Hotkey [Q]).
     */
    public void quickSwap() {
        if (previousWeapon != null && previousWeapon != activeWeapon) {
            Weapon temp = activeWeapon;
            activeWeapon = previousWeapon;
            previousWeapon = temp;
        }
    }

    /**
     * Triggers attack on the currently active weapon.
     */
    public boolean attack(double originX, double originY, double aimAngle, ProjectileManager pm) {
        if (activeWeapon != null) {
            return activeWeapon.attack(originX, originY, aimAngle, pm, activeSlashes);
        }
        return false;
    }

    /**
     * Triggers magazine reload on the currently active weapon.
     */
    public void reload() {
        if (activeWeapon != null) {
            activeWeapon.reload();
        }
    }

    /**
     * Updates weapon cooldowns, reload timers, and active melee strike animations.
     */
    public void update(double deltaTime) {
        // Update all weapons in inventory (so secondary weapons can finish reloading in pocket)
        for (Weapon w : weaponSlots.values()) {
            w.update(deltaTime);
        }

        // Update active melee slashes
        for (MeleeSlash slash : activeSlashes) {
            slash.update(deltaTime);
        }
        activeSlashes.removeIf(s -> !s.isActive());
    }

    /**
     * Renders equipped weapon on operative and all active melee slash animations.
     */
    public void render(Graphics2D g, Camera camera, double originX, double originY, double aimAngle) {
        // Draw weapon barrel or blade
        if (activeWeapon != null) {
            activeWeapon.render(g, camera, originX, originY, aimAngle);
        }

        // Draw active melee slashes
        for (MeleeSlash slash : activeSlashes) {
            slash.render(g, camera);
        }
    }

    /**
     * Renders a dedicated tactical Weapon HUD at the bottom right corner of the screen.
     */
    /**
     * Renders a dedicated tactical Weapon & Equipment HUD at the bottom right corner of the screen.
     * Cung cấp thông tin trực quan, gọn gàng và dễ hiểu:
     * 1. Tên súng đang cầm kèm Cấp độ (Level).
     * 2. Trạng thái đạn của vũ khí (băng / nạp đạn).
     * 3. Hiển thị rõ số đạn của tất cả các loại trong người (AR, SMG, Shotgun).
     * 4. Các vị trí nút trang bị [1] [2] [3] [4] làm nổi bật vũ khí đang cầm trên tay.
     */
    public void renderHUD(Graphics2D g, int screenWidth, int screenHeight) {
        if (activeWeapon == null) return;

        int hudW = 340;
        int hudH = 132;
        int hudX = screenWidth - hudW - 20;
        int hudY = screenHeight - hudH - 20;

        // 1. Container panel nền tối sci-fi bo góc
        g.setColor(new Color(12, 16, 24, 230));
        g.fillRoundRect(hudX, hudY, hudW, hudH, 10, 10);
        g.setColor(new Color(50, 70, 95));
        g.drawRoundRect(hudX, hudY, hudW, hudH, 10, 10);

        // 2. Dòng 1: Tên vũ khí đang cầm kèm CẤP ĐỘ (Level) và TỈ LỆ CHÍ MẠNG
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(new Color(255, 205, 50)); // Màu vàng kim
        String weaponTitle = String.format("[%d] %s   Lv.%d   •   CRIT: %d%%",
                activeWeapon.getSlotIndex(),
                activeWeapon.getName().toUpperCase(),
                activeWeapon.getLevel(),
                activeWeapon.getCritChancePercent());
        g.drawString(weaponTitle, hudX + 14, hudY + 22);

        // 3. Dòng 2: Hiển thị đạn băng / nạp đạn của vũ khí đang cầm
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        if (activeWeapon.isMelee()) {
            g.setColor(new Color(180, 225, 255));
            g.drawString("∞ CẬN CHIẾN", hudX + 14, hudY + 48);
        } else if (activeWeapon.isReloading()) {
            g.setColor(new Color(255, 130, 40));
            g.drawString("RELOADING...", hudX + 14, hudY + 48);

            // Thanh tiến trình nạp đạn
            int barW = 120;
            int barH = 7;
            int barX = hudX + 175;
            int barY = hudY + 39;
            g.setColor(new Color(35, 45, 60));
            g.fillRect(barX, barY, barW, barH);
            g.setColor(new Color(255, 140, 30));
            g.fillRect(barX, barY, (int) (barW * activeWeapon.getReloadProgress()), barH);
            g.setColor(new Color(200, 220, 240));
            g.drawRect(barX, barY, barW, barH);
        } else {
            if (activeWeapon.hasInfiniteAmmo()) {
                g.setColor(activeWeapon.getCurrentAmmo() <= 2 ? new Color(255, 80, 80) : Color.WHITE);
                g.drawString(String.format("%d / ∞", activeWeapon.getCurrentAmmo()), hudX + 14, hudY + 48);
            } else if (activeWeapon.getAmmoType() != null) {
                int reserve = ammoInventory != null ? ammoInventory.getAmmo(activeWeapon.getAmmoType()) : 0;
                boolean isOutOfAmmo = (activeWeapon.getCurrentAmmo() <= 0 && reserve <= 0);
                boolean isLowAmmo = (activeWeapon.getCurrentAmmo() <= 2 || reserve <= 5);

                g.setColor(isOutOfAmmo ? new Color(255, 60, 60) : (isLowAmmo ? new Color(255, 150, 50) : Color.WHITE));
                String ammoDisplay = isOutOfAmmo
                        ? "HẾT ĐẠN! [0 / 0]"
                        : String.format("%d / %d", activeWeapon.getCurrentAmmo(), reserve);
                g.drawString(ammoDisplay, hudX + 14, hudY + 48);

                // Loại đạn badge đồng bộ màu chuẩn với hộp đạn rơi bên ngoài
                FontMetrics fm = g.getFontMetrics();
                int ammoTextW = fm.stringWidth(ammoDisplay);
                g.setFont(new Font("SansSerif", Font.BOLD, 12));
                g.setColor(activeWeapon.getAmmoType().getColor());
                g.drawString("[" + activeWeapon.getAmmoType().name() + "]", hudX + 14 + ammoTextW + 8, hudY + 47);
            } else {
                g.setColor(activeWeapon.getCurrentAmmo() <= 2 ? new Color(255, 80, 80) : Color.WHITE);
                g.drawString(String.format("%d / %d", activeWeapon.getCurrentAmmo(), activeWeapon.getMagazineSize()),
                        hudX + 14, hudY + 48);
            }
        }

        // 4. Dòng 3: Hiển thị kho đạn của tất cả các loại trong người đồng bộ màu với hộp đạn bên ngoài
        int arReserve = ammoInventory != null ? ammoInventory.getAmmo(com.groupproject.game.entity.item.AmmoType.AR) : 0;
        int smgReserve = ammoInventory != null ? ammoInventory.getAmmo(com.groupproject.game.entity.item.AmmoType.SMG) : 0;
        int sgReserve = ammoInventory != null ? ammoInventory.getAmmo(com.groupproject.game.entity.item.AmmoType.SHOTGUN) : 0;

        int arMax = com.groupproject.game.entity.item.AmmoType.AR.getMaxCapacity();
        int smgMax = com.groupproject.game.entity.item.AmmoType.SMG.getMaxCapacity();
        int sgMax = com.groupproject.game.entity.item.AmmoType.SHOTGUN.getMaxCapacity();

        int ammoBarY = hudY + 62;
        int ammoBadgeW = 100;
        int ammoBadgeH = 18;
        int ammoGap = 7;
        int ammoStartX = hudX + 14;

        renderAmmoPill(g, ammoStartX, ammoBarY, ammoBadgeW, ammoBadgeH, "AR", arReserve, arMax, com.groupproject.game.entity.item.AmmoType.AR.getColor());
        renderAmmoPill(g, ammoStartX + ammoBadgeW + ammoGap, ammoBarY, ammoBadgeW, ammoBadgeH, "SMG", smgReserve, smgMax, com.groupproject.game.entity.item.AmmoType.SMG.getColor());
        renderAmmoPill(g, ammoStartX + (ammoBadgeW + ammoGap) * 2, ammoBarY, ammoBadgeW, ammoBadgeH, "SG", sgReserve, sgMax, com.groupproject.game.entity.item.AmmoType.SHOTGUN.getColor());

        // 5. Dòng 4: Các vị trí nút trang bị (Equipment Slots) [1] [2] [3] [4] (Yêu cầu 2)
        int slotY = hudY + 92;
        int chipW = 73;
        int chipH = 28;
        int chipGap = 7;
        int chipStartX = hudX + 14;

        for (int i = 1; i <= 4; i++) {
            int cx = chipStartX + (i - 1) * (chipW + chipGap);
            Weapon w = weaponSlots.get(i);
            boolean isCur = (activeWeapon.getSlotIndex() == i);

            // Nền chip
            if (isCur) {
                g.setColor(new Color(35, 75, 120, 240));
                g.fillRoundRect(cx, slotY, chipW, chipH, 6, 6);
                g.setColor(new Color(255, 215, 60)); // Viền vàng sáng cho slot đang chọn
                g.drawRoundRect(cx, slotY, chipW, chipH, 6, 6);
            } else {
                g.setColor(new Color(20, 26, 36, 180));
                g.fillRoundRect(cx, slotY, chipW, chipH, 6, 6);
                g.setColor(new Color(45, 60, 80));
                g.drawRoundRect(cx, slotY, chipW, chipH, 6, 6);
            }

            // Tên ngắn gọn trang bị
            String keycap = "[" + i + "]";
            String label;
            if (w == null) {
                label = "Trống";
            } else if (w instanceof AssaultRifle) {
                label = "AR " + w.getLevelDisplay();
            } else if (w instanceof SMG) {
                label = "SMG " + w.getLevelDisplay();
            } else if (w instanceof Shotgun) {
                label = "SG " + w.getLevelDisplay();
            } else if (w instanceof Pistol) {
                label = "Pistol";
            } else if (w instanceof CombatKnife) {
                label = "Dao";
            } else {
                label = w.getShortDisplayName();
            }

            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.setColor(isCur ? new Color(255, 220, 80) : new Color(130, 150, 175));
            g.drawString(keycap, cx + 5, slotY + 12);

            g.setFont(new Font("SansSerif", isCur ? Font.BOLD : Font.PLAIN, 10));
            g.setColor(isCur ? Color.WHITE : (w == null ? new Color(90, 105, 125) : new Color(185, 205, 230)));
            g.drawString(label, cx + 5, slotY + 24);
        }
    }

    /**
     * Helper vẽ nhãn hiển thị số lượng đạn dự trữ cho từng loại đạn.
     */
    private void renderAmmoPill(Graphics2D g, int x, int y, int w, int h, String name, int current, int max, Color accentColor) {
        g.setColor(new Color(18, 24, 34, 200));
        g.fillRoundRect(x, y, w, h, 4, 4);
        g.setColor(new Color(40, 55, 75));
        g.drawRoundRect(x, y, w, h, 4, 4);

        g.setColor(accentColor);
        g.fillOval(x + 5, y + 5, 6, 6);

        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.setColor(new Color(190, 205, 225));
        g.drawString(name + ":", x + 15, y + 13);

        boolean isZero = (current <= 0);
        g.setColor(isZero ? new Color(255, 75, 75) : Color.WHITE);
        String amountText = current + "/" + max;
        g.drawString(amountText, x + 42, y + 13);
    }

    public Weapon getActiveWeapon() {
        return activeWeapon;
    }

    public List<MeleeSlash> getActiveSlashes() {
        return activeSlashes;
    }

    public Weapon getWeaponInSlot(int slotIndex) {
        return weaponSlots.get(slotIndex);
    }

    public Map<Integer, Weapon> getWeaponSlots() {
        return java.util.Collections.unmodifiableMap(weaponSlots);
    }

    public com.groupproject.game.entity.item.AmmoInventory getAmmoInventory() {
        return ammoInventory;
    }

    /**
     * Tiếp tế 2 stack đạn cho toàn bộ các chủng loại đạn dự trữ (AR: +60, SMG: +70, Shotgun: +20)
     * và nạp đầy băng đạn cho tất cả súng hiện có trong túi đồ.
     */
    public void resupplyAllWeapons() {
        resupplyAmmoPackage(2);
    }

    /**
     * Tiếp tế gói đạn theo số lượng stack cho mỗi chủng loại đạn và làm đầy băng đạn cho súng.
     *
     * @param stacksPerType Số lượng stack tiếp tế cho mỗi loại (mặc định là 2 stack khi mua tại Shop).
     */
    public void resupplyAmmoPackage(int stacksPerType) {
        ammoInventory.addStacksForAllTypes(stacksPerType);
        for (Weapon w : weaponSlots.values()) {
            if (w != null && !w.isMelee()) {
                w.resupplyAmmo();
            }
        }
    }
}
