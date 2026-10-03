package com.groupproject.game.entity.weapon;

import com.groupproject.game.core.Camera;
import com.groupproject.game.entity.projectile.ProjectileManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Tactical Combat Knife mapped to hotkey slot [4].
 *
 * <p>WHY: High-risk, high-reward silent melee weapon. Requires zero ammunition and never reloads.
 * Upon swinging, it projects a square damage hitbox (~1 tile size: 44px) directly forward in the
 * direction of the mouse cursor, accompanied by an instantaneous kinetic slash effect.
 */
public class CombatKnife extends Weapon {

    private static final int DEFAULT_DAMAGE = 45;
    private static final double SWING_COOLDOWN = 0.32; // Seconds between swings
    private static final double REACH_OFFSET = 34.0;   // Distance in front of player
    private static final double HITBOX_SIZE = 44.0;    // ~1 tile size
    private static final double SLASH_DURATION = 0.12; // Duration of visual arc

    public CombatKnife() {
        super("Combat Knife", 4, DEFAULT_DAMAGE, SWING_COOLDOWN, 0, 0.0);
    }

    @Override
    public boolean attack(double originX, double originY, double aimAngle,
                          ProjectileManager pm, List<MeleeSlash> slashList) {
        if (!canAttack()) return false;

        // Project a melee strike hitbox 1 tile ahead in the aim direction
        MeleeSlash slash = new MeleeSlash(originX, originY, aimAngle, REACH_OFFSET,
                HITBOX_SIZE, damage, SLASH_DURATION);
        if (slashList != null) {
            slashList.add(slash);
        }

        cooldownTimer = fireRateCooldown;
        return true;
    }

    @Override
    public void render(Graphics2D g, Camera camera, double originX, double originY, double aimAngle) {
        int screenPX = camera.worldToScreenX(originX);
        int screenPY = camera.worldToScreenY(originY);

        int handleLen = 14;
        int bladeLen = 22;

        int hx = (int) (screenPX + Math.cos(aimAngle) * handleLen);
        int hy = (int) (screenPY + Math.sin(aimAngle) * handleLen);
        int bx = (int) (screenPX + Math.cos(aimAngle) * bladeLen);
        int by = (int) (screenPY + Math.sin(aimAngle) * bladeLen);

        // Draw handle (grip)
        g.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.setColor(new Color(60, 50, 40));
        g.drawLine(screenPX, screenPY, hx, hy);

        // Draw tactical steel blade
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        g.setColor(new Color(220, 230, 245));
        g.drawLine(hx, hy, bx, by);

        g.setStroke(new BasicStroke(1.0f));
    }
}
