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
    private static final double BULLET_SPEED = 900.0;     // Đạn AR bay nhanh (900 px/s)
    private static final double BULLET_LIFETIME = 1.2;    // Đạn bay xa tới 984 px
    private static final double SPREAD_ANGLE = 0.04;      // Độ giật rung ngẫu nhiên (+- 2.3 độ)

    public static final double INITIAL_SPREAD_RATIO = 0.25; // Viên đầu lệch rất nhỏ (1/4 độ lệch cơ sở)
    public static final double MAX_SPREAD_RATIO = 3.5;       // Khi sấy liên thanh tản rộng tới 3.5x

    // Cơ chế tản đạn liên thanh và hồi phục độ chính xác
    private int continuousShots = 0;
    private double timeSinceLastShot = 999.0;

    public AssaultRifle() {
        this(1);
    }

    public AssaultRifle(int slotIndex) {
        super("Assault Rifle (AR)", slotIndex, DEFAULT_DAMAGE, FIRE_RATE, MAG_CAPACITY, RELOAD_TIME, SPREAD_ANGLE);
        this.ammoType = com.groupproject.game.entity.item.AmmoType.AR;
    }

    @Override
    public boolean isAutomatic() {
        return true; // Giữ chuột trái để xả đạn liên tục
    }

    /**
     * Độ lệch góc của phát bắn đầu tiên (First-shot accuracy).
     * Số sao accuracyStars càng cao thì độ lệch viên đầu càng nhỏ, và ngược lại.
     */
    public double getInitialSpreadAngle() {
        return currentSpreadAngle * INITIAL_SPREAD_RATIO;
    }

    /**
     * Độ lệch góc tối đa khi duy trì sấy liên thanh (Full-auto spray bloom ceiling).
     * Số sao accuracyStars càng cao thì độ lệch tối đa càng gom chặt, và ngược lại.
     */
    public double getMaxContinuousSpreadAngle() {
        return currentSpreadAngle * MAX_SPREAD_RATIO;
    }

    @Override
    public double getMaxSpreadAngle() {
        return getMaxContinuousSpreadAngle();
    }

    /**
     * Số phát đạn đã xả liên tiếp trong loạt sấy hiện tại.
     */
    public int getContinuousShots() {
        return continuousShots;
    }

    /**
     * Reset số đạn liên thanh về 0 (hồi phục hoàn toàn độ chính xác).
     */
    public void resetRecoil() {
        this.continuousShots = 0;
        this.timeSinceLastShot = 999.0;
    }

    /**
     * Tính toán độ tản đạn thực tế của phát bắn tiếp theo:
     * - 2 phát bắn đầu tiên: Giữ nguyên độ chính xác cao nhất (First-shot accuracy).
     * - Từ phát thứ 3 đến phát thứ 10: Độ tản đạn tăng dần đều theo thời gian duy trì liên thanh.
     * - Từ phát thứ 10 trở đi: Đạt ngưỡng tản đạn tối đa của khẩu súng.
     */
    public double getCurrentDynamicSpreadAngle() {
        double minSpread = getInitialSpreadAngle();
        double maxSpread = getMaxContinuousSpreadAngle();
        if (continuousShots <= 2) {
            return minSpread;
        }
        double bloomProgress = Math.min(1.0, (continuousShots - 2) / 8.0);
        return minSpread + (maxSpread - minSpread) * bloomProgress;
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);

        timeSinceLastShot += deltaTime;
        // Khi người chơi ngừng bắn quá 1.5 nhịp đạn (~0.13s), độ lệch liên thanh hồi phục nhanh về trạng thái viên đầu
        if (timeSinceLastShot > fireRateCooldown * 1.5) {
            if (continuousShots > 0) {
                continuousShots = Math.max(0, continuousShots - (int) Math.ceil(deltaTime * 35.0));
            }
            if (timeSinceLastShot > 0.28) {
                continuousShots = 0;
            }
        }
    }

    @Override
    public void reload() {
        super.reload();
        continuousShots = 0;
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

        // Tăng số đạn liên tiếp và cập nhật thời gian phát bắn
        continuousShots++;
        timeSinceLastShot = 0.0;

        // Tạo độ tản xạ động: chuẩn xác ở các viên đầu, tăng dần khi sấy liên thanh
        double dynamicSpread = getCurrentDynamicSpreadAngle();
        double jitter = dynamicSpread > 0.0001
                ? ThreadLocalRandom.current().nextDouble(-dynamicSpread, dynamicSpread)
                : 0.0;
        double firedAngle = aimAngle + jitter;

        // Tính tọa độ đầu nòng súng (cách tâm operative 26px)
        int barrelLength = 26;
        double muzzleX = originX + Math.cos(firedAngle) * barrelLength;
        double muzzleY = originY + Math.sin(firedAngle) * barrelLength;

        // Roll critical hit and calculate dynamic damage
        boolean isCrit = rollCritical();
        int shotDamage = calculateDamage(isCrit);

        // Bắn viên đạn AR
        Projectile bullet = new Projectile(muzzleX, muzzleY, firedAngle, BULLET_SPEED,
                shotDamage, BULLET_LIFETIME, true, isCrit);
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
