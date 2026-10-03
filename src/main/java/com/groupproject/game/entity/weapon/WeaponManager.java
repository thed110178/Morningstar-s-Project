package com.groupproject.game.entity.weapon;

import com.groupproject.game.core.Camera;
import com.groupproject.game.entity.projectile.ProjectileManager;
import java.awt.BasicStroke;
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
        // Equip default loadout requested by user: Slot 3 = Pistol, Slot 4 = Knife
        Pistol pistol = new Pistol();
        CombatKnife knife = new CombatKnife();

        addWeapon(pistol);
        addWeapon(knife);

        this.activeWeapon = pistol;
        this.previousWeapon = knife;
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

        // Quick tip
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(140, 160, 185));
        g.drawString("[3] Pistol | [4] Knife | [Q] Swap | [R] Reload", hudX + 15, hudY + 75);
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
}
