package com.groupproject.game.entity.weapon;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameSoundManager;
import com.groupproject.game.core.GameSoundManager.SoundEffect;
import com.groupproject.game.entity.projectile.ProjectileManager;
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

    private static final int DEFAULT_DAMAGE = 45; // Buff sát thương từ 22 lên 45 HP (thưởng xứng đáng cho rủi ro cận chiến)
    private static final double SWING_COOLDOWN = 0.32; // Seconds between swings
    private static final double REACH_OFFSET = 36.0;   // Distance in front of player
    private static final double HITBOX_SIZE = 48.0;    // ~1 tile size
    private static final double SLASH_DURATION = 0.12; // Duration of visual arc
    private static final double KNOCKBACK_FORCE = 600.0; // Tăng xung lực đẩy lùi từ 520 lên 650 px/s để đẩy lùi quái cận chiến áp sát dũng mãnh hơn

    public CombatKnife() {
        super("Combat Knife", 4, DEFAULT_DAMAGE, SWING_COOLDOWN, 0, 0.0);
    }

    @Override
    public boolean attack(double originX, double originY, double aimAngle,
                          ProjectileManager pm, List<MeleeSlash> slashList) {
        if (!canAttack()) return false;

        // Roll critical hit and calculate dynamic damage and knockback
        boolean isCrit = rollCritical();
        int slashDmg = calculateDamage(isCrit);
        double knockback = KNOCKBACK_FORCE;

        // Project a melee strike hitbox 1 tile ahead in the aim direction with high knockback
        MeleeSlash slash = new MeleeSlash(originX, originY, aimAngle, REACH_OFFSET,
                HITBOX_SIZE, slashDmg, SLASH_DURATION, knockback, isCrit);
        if (slashList != null) {
            slashList.add(slash);
        }

        GameSoundManager.play(SoundEffect.GUN_MELEE);

        cooldownTimer = fireRateCooldown;
        return true;
    }

    @Override
    public void render(Graphics2D g, Camera camera, double originX, double originY, double aimAngle) {
        // WHY: Khi nhân vật cầm dao, sprite PlayerMelee.png đã tích hợp sẵn tư thế cầm dao cận chiến hoàn chỉnh.
        // Loại bỏ hoàn toàn thanh que vector vẽ đè ở giữa hai tay để hình ảnh nhân vật tự nhiên, đẹp mắt và chuẩn pixel-art.
    }
}
