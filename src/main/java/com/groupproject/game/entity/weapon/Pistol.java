package com.groupproject.game.entity.weapon;

import com.groupproject.game.core.Camera;
import com.groupproject.game.entity.projectile.Projectile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Standard-issue Tactical Sidearm (Pistol) mapped to hotkey slot [3].
 *
 * <p>
 * WHY: Provides reliable, accurate semi-automatic firepower. Holds a strict
 * 7-round magazine
 * as specified in design requirements. Once all 7 rounds are expended or the
 * player presses [R],
 * a 1.2-second tactical reload cycle is enforced.
 */
public class Pistol extends Weapon {

    private static final int DEFAULT_DAMAGE = 25;
    private static final double FIRE_RATE = 0.09; // Seconds between semi-auto trigger pulls
    private static final int MAG_CAPACITY = 7; // 7-round magazine
    private static final double RELOAD_TIME = 1.2; // 1.2s reload duration
    private static final double BULLET_SPEED = 700.0; // Fast travel velocity

    public Pistol() {
        super("Tactical 9mm Pistol", 3, DEFAULT_DAMAGE, FIRE_RATE, MAG_CAPACITY, RELOAD_TIME);
    }

    @Override
    public boolean attack(double originX, double originY, double aimAngle,
            ProjectileManager pm, List<MeleeSlash> slashList) {
        if (!canAttack())
            return false;

        // If magazine is empty, trigger reload automatically
        if (currentAmmo <= 0) {
            reload();
            return false;
        }

        // Compute barrel muzzle tip coordinate
        int barrelLength = 22;
        double muzzleX = originX + Math.cos(aimAngle) * barrelLength;
        double muzzleY = originY + Math.sin(aimAngle) * barrelLength;

        // Roll critical hit and calculate dynamic damage
        boolean isCrit = rollCritical();
        int shotDamage = calculateDamage(isCrit);

        // Spawn bullet projectile travelling along the aim angle
        Projectile bullet = new Projectile(muzzleX, muzzleY, aimAngle, BULLET_SPEED,
                shotDamage, 1.6, true, isCrit);
        pm.spawn(bullet);

        // Deduct ammunition and reset trigger cooldown
        currentAmmo--;
        cooldownTimer = fireRateCooldown;

        // Automatically begin reload cycle when the last round is spent
        if (currentAmmo <= 0) {
            reload();
        }

        return true;
    }

    @Override
    public void render(Graphics2D g, Camera camera, double originX, double originY, double aimAngle) {
        int screenPX = camera.worldToScreenX(originX);
        int screenPY = camera.worldToScreenY(originY);

        int barrelLen = 22;
        int endX = (int) (screenPX + Math.cos(aimAngle) * barrelLen);
        int endY = (int) (screenPY + Math.sin(aimAngle) * barrelLen);

        // Draw gun body (sleek matte black slide with orange muzzle tip)
        g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        g.setColor(new Color(40, 45, 55));
        g.drawLine(screenPX, screenPY, endX, endY);

        // Muzzle highlight
        g.setColor(new Color(255, 140, 30));
        g.fillOval(endX - 2, endY - 2, 4, 4);

        g.setStroke(new BasicStroke(1.0f));
    }
}
