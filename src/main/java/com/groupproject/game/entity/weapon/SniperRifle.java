package com.groupproject.game.entity.weapon;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameSoundManager;
import com.groupproject.game.core.GameSoundManager.SoundEffect;
import com.groupproject.game.entity.item.AmmoType;
import com.groupproject.game.entity.projectile.Projectile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Vũ khí: Súng bắn tỉa kéo quy lát (Bolt-Action Sniper Rifle - SR).
 *
 * <p>WHY: Đại diện cho dòng vũ khí tầm xa siêu uy lực với triết lý "One Shot, Multi-Kill".
 * Băng đạn giới hạn 5 viên, tốc độ đạn cực cao (1450 px/s) và sát thương đơn cực khủng (95 base DMG).
 * Sở hữu cơ chế ĐỘ XUYÊN độc quyền: đạn có khả năng đâm xuyên qua nhiều kẻ địch xếp thẳng hàng
 * và các ô cover tạm thời (destructible cover), với lượng sát thương giảm dần sau mỗi mục tiêu bị xuyên.
 *
 * <p>3 CHỈ SỐ CHÍNH ĐƯỢC ĐẦU TƯ:
 * <ol>
 *   <li>Sát thương đơn (Single-shot Damage): 95 base DMG, tăng +20% mỗi sao thăng cấp.</li>
 *   <li>Tốc độ ra đạn (Bolt-action Chambering / Fire Rate): Cooldown ~0.95s/viên (~63 RPM), giảm thời gian kéo lát khi lên sao.</li>
 *   <li>Độ xuyên (Penetration / Pierce Power): Quyết định số lượng mục tiêu/cover có thể xuyên qua và tỉ lệ giảm sát thương do xuyên.</li>
 * </ol>
 */
public class SniperRifle extends Weapon {

    private static final int DEFAULT_DAMAGE = 95;          // Sát thương cơ sở cực cao
    private static final double FIRE_RATE = 0.95;           // Cooldown kéo chốt quy lát cơ học bolt-action (~63 RPM)
    private static final int MAG_CAPACITY = 5;              // Băng đạn chuẩn 5 viên theo yêu cầu
    private static final double RELOAD_TIME = 2.4;          // 2.4s nạp băng đạn mới
    private static final double BULLET_SPEED = 1450.0;      // Tốc độ đạn cực cao (1450 px/s)
    private static final double BULLET_LIFETIME = 1.0;      // Đạn bay xa tới 1450 px (bao quát trọn bản đồ)
    private static final double SPREAD_ANGLE = 0.005;       // Đường đạn thẳng tắp gần như không tản xạ (+- 0.3 độ)

    // Base penetration attributes (1 sao độ xuyên) - Đã tái cân bằng chuẩn chiến thuật:
    // Lv.1 (1★) chỉ xuyên qua 1 mục tiêu (chạm tối đa 2 kẻ địch/cover), giảm 40% sát thương cho mục tiêu sau
    public static final int BASE_PIERCES = 1;               // Mặc định xuyên qua 1 mục tiêu (chạm tối đa 2 mục tiêu)
    public static final double BASE_FALLOFF = 0.40;         // Giảm 40% sát thương sau lần xuyên phá đầu tiên

    public SniperRifle() {
        this(1);
    }

    public SniperRifle(int slotIndex) {
        super("Sniper Rifle (SR)", slotIndex, DEFAULT_DAMAGE, FIRE_RATE, MAG_CAPACITY, RELOAD_TIME, SPREAD_ANGLE);
        this.ammoType = AmmoType.SR;
        // Tỉ lệ chí mạng mặc định của súng ngắm (20% - 25%)
        this.critChance = 0.25;
    }

    @Override
    public boolean isAutomatic() {
        return false; // Súng bắn từng viên một (Semi-auto/Bolt-action), người chơi click từng phát bắn
    }

    /**
     * Số sao độ xuyên (Penetration Stars) - ánh xạ trực tiếp từ nhánh sao thứ 3 của hệ thống vũ khí.
     */
    public int getPierceStars() {
        return accuracyStars;
    }

    public void setPierceStars(int stars) {
        setStars(this.damageStars, this.fireRateStars, stars);
    }

    /**
     * Số lượng mục tiêu (kẻ địch hoặc cover tạm thời) đạn có thể xuyên qua tối đa:
     * - 1★: 1 lần xuyên (trúng tối đa 2 mục tiêu).
     * - 2★: 1 lần xuyên (trúng tối đa 2 mục tiêu, tối ưu hao hụt sát thương).
     * - 3★: 2 lần xuyên (trúng tối đa 3 mục tiêu).
     * - 4★: 2 lần xuyên (trúng tối đa 3 mục tiêu).
     * - 5★: 3 lần xuyên (trúng tối đa 4 mục tiêu - khống chế trần tránh one-shot toàn bản đồ).
     */
    public int getMaxPierces() {
        return BASE_PIERCES + (accuracyStars - 1) / 2;
    }

    /**
     * Số lần xuyên dự kiến khi nâng thêm 1 sao Sức Xuyên (Preview).
     */
    public int getPreviewMaxPierces() {
        return BASE_PIERCES + accuracyStars / 2;
    }

    /**
     * Tỉ lệ giảm sát thương sau mỗi lần xuyên qua mục tiêu:
     * - 1★: Giảm 40% sát thương (-40%).
     * - Mỗi sao thăng cấp: Giảm nhẹ độ sụt giảm sát thương (giảm 5% mỗi sao, tối thiểu vẫn giảm 20%).
     */
    public double getPierceDamageFalloff() {
        return Math.max(0.20, BASE_FALLOFF - (accuracyStars - 1) * 0.05);
    }

    /**
     * Tỉ lệ % giảm sát thương hiển thị trên HUD / Balo (VD: 40%, 35%, 30%).
     */
    public int getPierceFalloffPercent() {
        return (int) Math.round(getPierceDamageFalloff() * 100.0);
    }

    /**
     * Tỉ lệ % giảm sát thương dự kiến khi nâng thêm 1 sao Sức Xuyên (Preview).
     */
    public int getPreviewPierceFalloffPercent() {
        return (int) Math.round(Math.max(0.20, BASE_FALLOFF - accuracyStars * 0.05) * 100.0);
    }

    @Override
    public boolean attack(double originX, double originY, double aimAngle,
                          ProjectileManager pm, List<MeleeSlash> slashList) {
        if (!canAttack()) return false;

        // Nếu hết đạn trong băng, tự động kích hoạt nạp đạn
        if (currentAmmo <= 0) {
            reload();
            return false;
        }

        // Tiêu hao 1 viên đạn trong băng và thiết lập nhịp kéo chốt quy lát
        currentAmmo--;
        cooldownTimer = fireRateCooldown;

        // Độ rung tản xạ cực nhỏ mô phỏng độ giật của súng ngắm
        double jitter = currentSpreadAngle > 0.0001
                ? ThreadLocalRandom.current().nextDouble(-currentSpreadAngle, currentSpreadAngle)
                : 0.0;
        double firedAngle = aimAngle + jitter;

        // Tọa độ đầu nòng súng ngắm vươn dài (cách tâm nhân vật 34px)
        int barrelLength = 34;
        double muzzleX = originX + Math.cos(firedAngle) * barrelLength;
        double muzzleY = originY + Math.sin(firedAngle) * barrelLength;

        // Tính toán sát thương đơn và kiểm tra đòn chí mạng
        boolean isCrit = rollCritical();
        int shotDamage = calculateDamage(isCrit);

        // Khởi tạo viên đạn bắn tỉa xuyên phá siêu vận tốc
        Projectile bullet = new Projectile(
                muzzleX, muzzleY, firedAngle, BULLET_SPEED,
                shotDamage, BULLET_LIFETIME, true, isCrit,
                getMaxPierces(), getPierceDamageFalloff()
        );
        pm.spawn(bullet);
        // Phát tiếng nổ súng bắn tỉa và tiếng kéo chốt quy lát lên đạn (Bolt-Action) sau mỗi phát bắn
        GameSoundManager.play(SoundEffect.GUN_SNIPER_SHOOT);
        GameSoundManager.playChamber(SoundEffect.GUN_SNIPER_LOADING_AMMO);

        // Nếu bắn hết viên cuối cùng, tự động chuyển trạng thái nạp đạn
        if (currentAmmo <= 0) {
            reload();
        }

        return true;
    }

    @Override
    protected void onStartReload() {
        GameSoundManager.playReload(SoundEffect.GUN_SNIPER_RELOAD);
    }

    @Override
    public void render(Graphics2D g, Camera camera, double originX, double originY, double aimAngle) {
        int screenPX = camera.worldToScreenX(originX);
        int screenPY = camera.worldToScreenY(originY);

        int barrelLen = 34;
        int endX = (int) (screenPX + Math.cos(aimAngle) * barrelLen);
        int endY = (int) (screenPY + Math.sin(aimAngle) * barrelLen);

        // 1. Nòng súng ngắm dài thanh mảnh màu xám thép nòng súng
        g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        g.setColor(new Color(40, 46, 54));
        g.drawLine(screenPX, screenPY, endX, endY);

        // 2. Ống ngắm quang học bắn tỉa (Sniper Scope) gắn trên sống súng
        int scopeOffset = 16;
        int scopeX = (int) (screenPX + Math.cos(aimAngle) * scopeOffset);
        int scopeY = (int) (screenPY + Math.sin(aimAngle) * scopeOffset);

        // Thân ống ngắm
        g.setColor(new Color(25, 30, 38));
        g.setStroke(new BasicStroke(4.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int scopeBackX = (int) (scopeX - Math.cos(aimAngle) * 6);
        int scopeBackY = (int) (scopeY - Math.sin(aimAngle) * 6);
        int scopeFrontX = (int) (scopeX + Math.cos(aimAngle) * 8);
        int scopeFrontY = (int) (scopeY + Math.sin(aimAngle) * 8);
        g.drawLine(scopeBackX, scopeBackY, scopeFrontX, scopeFrontY);

        // Thấu kính phản quang tím neon đặc trưng của đạn SR
        g.setColor(new Color(185, 80, 255));
        g.fillOval(scopeFrontX - 2, scopeFrontY - 2, 4, 4);

        // 3. Đầu nòng giảm thanh và chống giật (Muzzle Brake)
        g.setColor(new Color(220, 200, 255));
        g.fillOval(endX - 2, endY - 2, 4, 4);

        // 4. Ánh chớp đầu nòng (Muzzle Flash) khi vừa khai hỏa (kéo dài ~0.06s)
        if (cooldownTimer > fireRateCooldown - 0.06) {
            g.setColor(new Color(255, 120, 255, 200));
            g.fillOval(endX - 5, endY - 5, 10, 10);
            g.setColor(new Color(255, 255, 255, 240));
            g.fillOval(endX - 2, endY - 2, 4, 4);
        }

        g.setStroke(new BasicStroke(1.0f));
    }
}
