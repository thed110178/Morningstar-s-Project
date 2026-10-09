package com.groupproject.game.entity.weapon;

import com.groupproject.game.core.Camera;
import com.groupproject.game.entity.projectile.Projectile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Vũ khí: Súng trường tấn công (Assault Rifle - AR) gắn vào ô phím [1].
 *
 * <p>WHY: Vũ khí tự động hỏa lực cao dành cho giao tranh tầm trung. Sở hữu băng đạn 30 viên,
 * tốc độ xả đạn ~700 viên/phút (cooldown ~ 60.0 / 700.0 = 0.0857s mỗi phát).
 * Người chơi chỉ cần giữ phím bắn để sấy đạn liên tục (Full-Auto) với độ tản xạ ngẫu nhiên nhẹ
 * mô phỏng độ rung giật súng thực tế.
 */
public class AssaultRifle extends Weapon {

    private static final int DEFAULT_DAMAGE = 16;
    private static final double FIRE_RATE = 60.0 / 700.0; // ~0.0857s/viên tương đương 700 RPM
    private static final int MAG_CAPACITY = 30;           // Băng đạn tiêu chuẩn 30 viên
    private static final double RELOAD_TIME = 1.8;        // 1.8s nạp đạn
    private static final double BULLET_SPEED = 820.0;     // Đạn AR bay nhanh (820 px/s)
    private static final double BULLET_LIFETIME = 1.2;    // Đạn bay xa tới 984 px
    private static final double SPREAD_ANGLE = 0.04;      // Độ giật rung ngẫu nhiên (+- 2.3 độ)

    public AssaultRifle() {
        super("Assault Rifle (AR)", 1, DEFAULT_DAMAGE, FIRE_RATE, MAG_CAPACITY, RELOAD_TIME);
    }

    @Override
    public boolean isAutomatic() {
        return true; // Giữ chuột trái để xả đạn liên tục
    }

    @Override
    public boolean attack(double originX, double originY, double aimAngle,
                          ProjectileManager pm, List<MeleeSlash> slashList) {
        if (!canAttack()) return false;

        // Nếu hết đạn, kích hoạt nạp đạn tự động
        if (currentAmmo <= 0) {
            reload();
            return false;
        }

        // Tạo độ tản xạ góc nhỏ ngẫu nhiên cho cơ chế sấy đạn chân thực
        double jitter = ThreadLocalRandom.current().nextDouble(-SPREAD_ANGLE, SPREAD_ANGLE);
        double firedAngle = aimAngle + jitter;

        // Tính tọa độ đầu nòng súng (cách tâm operative 26px)
        int barrelLength = 26;
        double muzzleX = originX + Math.cos(firedAngle) * barrelLength;
        double muzzleY = originY + Math.sin(firedAngle) * barrelLength;

        // Bắn viên đạn AR
        Projectile bullet = new Projectile(muzzleX, muzzleY, firedAngle, BULLET_SPEED,
                damage, BULLET_LIFETIME, true);
        pm.spawn(bullet);

        // Tiêu hao đạn và hồi cooldown
        currentAmmo--;
        cooldownTimer = fireRateCooldown;

        if (currentAmmo <= 0) {
            reload();
        }

        return true;
    }

    @Override
    public void render(Graphics2D g, Camera camera, double originX, double originY, double aimAngle) {
        int screenPX = camera.worldToScreenX(originX);
        int screenPY = camera.worldToScreenY(originY);

        int barrelLen = 28;
        int endX = (int) (screenPX + Math.cos(aimAngle) * barrelLen);
        int endY = (int) (screenPY + Math.sin(aimAngle) * barrelLen);

        // 1. Thân súng nòng dài kim loại xám đen quân sự
        g.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        g.setColor(new Color(45, 52, 60));
        g.drawLine(screenPX, screenPY, endX, endY);

        // 2. Thước ngắm và đầu nòng súng (Muzzle Flash Suppressor)
        g.setColor(new Color(255, 215, 0)); // Chấm vàng kim đầu nòng
        g.fillOval(endX - 2, endY - 2, 4, 4);

        // 3. Ốp hộp tiếp đạn AR đặc trưng
        int magOffset = 14;
        int magX = (int) (screenPX + Math.cos(aimAngle) * magOffset);
        int magY = (int) (screenPY + Math.sin(aimAngle) * magOffset);
        g.setColor(new Color(20, 24, 30));
        g.fillOval(magX - 3, magY - 3, 6, 6);

        g.setStroke(new BasicStroke(1.0f));
    }
}
