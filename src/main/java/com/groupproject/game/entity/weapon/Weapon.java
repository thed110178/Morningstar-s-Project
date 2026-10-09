package com.groupproject.game.entity.weapon;

import com.groupproject.game.core.Camera;
import com.groupproject.game.entity.projectile.ProjectileManager;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Base abstract class defining common attributes and behavioral contracts for all weapons.
 *
 * <p>WHY: Adheres to the Strategy Pattern and Open/Closed Principle (SOLID). Whether an armament
 * is a semi-automatic 7-round pistol, a close-quarters combat knife, or a future burst-fire rifle,
 * the player controller interacts uniformly with the {@link #attack} and {@link #reload} interface
 * without knowing concrete firearm mechanics. Adding new weaponry requires zero modifications
 * to existing gameplay code.
 */
public abstract class Weapon {

    protected final String name;
    protected int slotIndex; // Slot số 1 hoặc 2 cho vũ khí chính, 3 cho Pistol, 4 cho Knife
    protected final int damage;
    protected final double fireRateCooldown; // Seconds between successive strikes/shots
    protected final int magazineSize;        // Maximum magazine capacity (0 for melee)
    protected final double reloadDuration;   // Time in seconds to reload a fresh magazine

    protected int currentAmmo;
    protected double cooldownTimer = 0.0;
    protected double reloadTimer = 0.0;
    protected boolean reloading = false;

    /**
     * Constructs a base weapon configuration.
     *
     * @param name             Display title of the firearm or melee tool.
     * @param slotIndex        Keybinding index (e.g. 3 or 4).
     * @param damage           Damage payload per hit.
     * @param fireRateCooldown Minimum delay in seconds between attacks.
     * @param magazineSize     Magazine bullet capacity (0 indicates infinite/melee).
     * @param reloadDuration   Seconds required to complete a reload cycle.
     */
    public Weapon(String name, int slotIndex, int damage, double fireRateCooldown,
                  int magazineSize, double reloadDuration) {
        this.name = name;
        this.slotIndex = slotIndex;
        this.damage = damage;
        this.fireRateCooldown = fireRateCooldown;
        this.magazineSize = magazineSize;
        this.currentAmmo = magazineSize;
        this.reloadDuration = reloadDuration;
    }

    /**
     * Executes an attack cycle (firing bullets or striking with melee hitbox).
     *
     * @param originX   X coordinate of operative center.
     * @param originY   Y coordinate of operative center.
     * @param aimAngle  Aiming angle in radians towards mouse cursor.
     * @param pm        Projectile manager for registering spawned bullets.
     * @param slashList List for registering active melee slash hitboxes.
     * @return {@code true} if attack was successfully executed; {@code false} if on cooldown or reloading.
     */
    public abstract boolean attack(double originX, double originY, double aimAngle,
                                   ProjectileManager pm, List<MeleeSlash> slashList);

    /**
     * Advances weapon cooldown and reload progression timers.
     */
    public void update(double deltaTime) {
        if (cooldownTimer > 0) {
            cooldownTimer -= deltaTime;
        }

        if (reloading) {
            reloadTimer -= deltaTime;
            if (reloadTimer <= 0) {
                // Reload finished: restore full magazine
                currentAmmo = magazineSize;
                reloading = false;
            }
        }
    }

    /**
     * Initiates weapon reloading if not already full or reloading.
     */
    public void reload() {
        if (magazineSize > 0 && currentAmmo < magazineSize && !reloading) {
            reloading = true;
            reloadTimer = reloadDuration;
        }
    }

    /**
     * Cancels an ongoing reload cycle immediately (e.g. when interrupted by shooting).
     */
    public void cancelReload() {
        if (reloading) {
            reloading = false;
            reloadTimer = 0.0;
        }
    }

    /**
     * Checks if the weapon is ready to strike or fire.
     */
    public boolean canAttack() {
        return cooldownTimer <= 0 && !reloading;
    }

    /**
     * Renders visual representation of the equipped weapon on the player operative.
     */
    public abstract void render(Graphics2D g, Camera camera, double originX, double originY, double aimAngle);

    public String getName() {
        return name;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public void setSlotIndex(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public int getDamage() {
        return damage;
    }

    public int getCurrentAmmo() {
        return currentAmmo;
    }

    public int getMagazineSize() {
        return magazineSize;
    }

    public boolean isReloading() {
        return reloading;
    }

    public double getReloadProgress() {
        if (!reloading || reloadDuration <= 0) return 0.0;
        return 1.0 - (reloadTimer / reloadDuration);
    }

    public boolean isMelee() {
        return magazineSize == 0;
    }

    /**
     * Indicates whether this firearm supports continuous full-auto fire by holding the mouse trigger.
     * Semi-automatic weapons (such as pistols) return false and require distinct mouse clicks.
     */
    public boolean isAutomatic() {
        return false;
    }
}
