package com.groupproject.game.entity.weapon;

import com.groupproject.game.core.Camera;
import com.groupproject.game.entity.projectile.ProjectileManager;
import java.awt.Color;
import java.awt.Font;
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
     * Resets loadout to starting setup (Pistol + Knife only).
     */
    public void reset() {
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
     * Registers a weapon into its assigned slot.
     */
    public void addWeapon(Weapon weapon) {
        if (weapon != null) {
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
    public void renderHUD(Graphics2D g, int screenWidth, int screenHeight) {
        if (activeWeapon == null) return;

        int hudW = 260;
        int hudH = 80;
        int hudX = screenWidth - hudW - 25;
        int hudY = screenHeight - hudH - 25;

        // Backdrop panel
        g.setColor(new Color(15, 20, 30, 220));
        g.fillRoundRect(hudX, hudY, hudW, hudH, 12, 12);
        g.setColor(new Color(60, 80, 110));
        g.drawRoundRect(hudX, hudY, hudW, hudH, 12, 12);

        // Weapon name & slot indicator
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(new Color(255, 200, 50));
        g.drawString(String.format("[%d] %s", activeWeapon.getSlotIndex(), activeWeapon.getName().toUpperCase()),
                hudX + 15, hudY + 28);

        // Ammunition display
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        if (activeWeapon.isMelee()) {
            g.setColor(new Color(180, 220, 255));
            g.drawString("∞ MELEE", hudX + 15, hudY + 60);
        } else if (activeWeapon.isReloading()) {
            g.setColor(new Color(255, 120, 50));
            g.drawString("RELOADING...", hudX + 15, hudY + 60);

            // Reload progress bar
            int barW = 100;
            int barH = 8;
            int barX = hudX + 145;
            int barY = hudY + 48;
            g.setColor(new Color(40, 50, 65));
            g.fillRect(barX, barY, barW, barH);
            g.setColor(new Color(255, 140, 30));
            g.fillRect(barX, barY, (int) (barW * activeWeapon.getReloadProgress()), barH);
            g.setColor(Color.WHITE);
            g.drawRect(barX, barY, barW, barH);
        } else {
            // Display: CurrentAmmo / MagSize (e.g. 7 / 7)
            g.setColor(activeWeapon.getCurrentAmmo() <= 2 ? new Color(255, 80, 80) : Color.WHITE);
            g.drawString(String.format("%d / %d", activeWeapon.getCurrentAmmo(), activeWeapon.getMagazineSize()),
                    hudX + 15, hudY + 60);
        }

        // Quick tip dynamically showing owned weapons
        StringBuilder tip = new StringBuilder();
        if (weaponSlots.containsKey(1)) tip.append(String.format("[1] %s | ", weaponSlots.get(1).getName()));
        if (weaponSlots.containsKey(2)) tip.append(String.format("[2] %s | ", weaponSlots.get(2).getName()));
        tip.append("[3] Pistol | [4] Knife | [Q] Swap | [R] Reload");

        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(140, 160, 185));
        g.drawString(tip.toString(), hudX + 15, hudY + 75);
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

    /**
     * Nạp đầy toàn bộ đạn cho tất cả vũ khí súng hiện có trong kho đồ.
     */
    public void resupplyAllWeapons() {
        for (Weapon w : weaponSlots.values()) {
            if (w != null && !w.isMelee()) {
                w.resupplyAmmo();
            }
        }
    }
}
