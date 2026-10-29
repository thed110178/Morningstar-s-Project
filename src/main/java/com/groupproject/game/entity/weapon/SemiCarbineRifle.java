package com.groupproject.game.entity.weapon;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameSoundManager;
import com.groupproject.game.core.GameSoundManager.SoundEffect;
import com.groupproject.game.entity.projectile.Projectile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Vũ khí: Súng trường bán tự động (Semi-Carbine Rifle - SCR).
 *
 * <p>
 * WHY: Vũ khí hỏa lực cao dành cho giao tranh tầm trung - xa.
 * Sử dụng chung loại đạn súng trường (Rifle ammo -
 * {@link com.groupproject.game.entity.item.AmmoType#AR}),
 * sở hữu băng đạn 20 viên, sát thương cao vượt trội (34 DMG so với 16 DMG của
 * AR), và sơ tốc đầu nòng
 * rất cao (1150 px/giây so với 900 px/giây của AR).
 *
 * <p>
 * Đặc tính động lực học (Dynamic Recoil Mechanics):
 * <ul>
 * <li><b>Độ chính xác viên đầu tiên cao:</b> 2 phát bắn đầu tiên có độ tản đạn
 * cực nhỏ (khoảng 15% độ lệch cơ sở),
 * cho phép người chơi ngắm bắn tỉa nhịp nhàng với độ chuẩn xác tuyệt đối.</li>
 * <li><b>Độ giật cao khi bắn dồn dập (Spray Bloom):</b> Nếu người chơi nhấp cò
 * liên tục với tốc độ cao không nghỉ,
 * độ tản đạn tăng mạnh gấp 4 lần cơ sở (lên tới ±11.5°), đòi hỏi kỹ năng ghìm
 * tâm và nhả nhịp (Tap-Fire).</li>
 * <li><b>Hồi phục tâm nhanh (Recoil Recovery):</b> Khi ngừng bắn quá 0.27 giây,
 * tâm súng nhanh chóng hồi phục về
 * trạng thái viên đầu tiên.</li>
 * </ul>
 */
public class SemiCarbineRifle extends Weapon {

    public static final int DEFAULT_DAMAGE = 34;
    public static final double FIRE_RATE = 0.18; // ~0.18s mỗi phát nhấp cò (~333 RPM)
    public static final int MAG_CAPACITY = 20; // Băng đạn tiêu chuẩn 20 viên
    public static final double RELOAD_TIME = 1.9; // 1.9s thay băng đạn
    public static final double BULLET_SPEED = 1150.0; // Sơ tốc cao vượt trội (1150 px/s so với AR 900 px/s)
    public static final double BULLET_LIFETIME = 1.1; // Tầm bắn xa hiệu dụng ~1265 px
    public static final double SPREAD_ANGLE = 0.05; // Góc tản đạn cơ sở (~2.86 độ)

    public static final double INITIAL_SPREAD_RATIO = 0.10; // Viên đầu tiên cực chụm (~0.29 độ - Pinpoint Accuracy)
    public static final double MAX_SPREAD_RATIO = 3.0; // Khi bắn dồn dập tản tối đa gấp 8 lần (~22.9 độ)

    // Bộ đếm đạn liên tiếp và thời gian hồi phục độ giật
    private int continuousShots = 0;
    private double timeSinceLastShot = 999.0;

    public SemiCarbineRifle() {
        this(1);
    }

    public SemiCarbineRifle(int slotIndex) {
        super("Semi-Carbine Rifle (SCR)", slotIndex, DEFAULT_DAMAGE, FIRE_RATE, MAG_CAPACITY, RELOAD_TIME,
                SPREAD_ANGLE);
        this.ammoType = com.groupproject.game.entity.item.AmmoType.AR;
    }

    @Override
    public boolean isAutomatic() {
        return false; // Súng bán tự động: Người chơi nhấp chuột cho từng phát bắn chiến thuật
    }

    /**
     * Độ lệch góc của phát bắn đầu tiên (First-shot pinpoint accuracy).
     */
    public double getInitialSpreadAngle() {
        return currentSpreadAngle * INITIAL_SPREAD_RATIO;
    }

    /**
     * Độ lệch góc tối đa khi duy trì bắn dồn dập (Max continuous bloom spread).
     */
    public double getMaxContinuousSpreadAngle() {
        return currentSpreadAngle * MAX_SPREAD_RATIO;
    }

    @Override
    public double getMaxSpreadAngle() {
        return getMaxContinuousSpreadAngle();
    }

    /**
     * Trả về chuỗi góc lệch viên đầu hiển thị trực quan (Ví dụ: "±0.3°").
     */
    public String getFormattedInitialSpread() {
        double deg = Math.toDegrees(getInitialSpreadAngle());
        return String.format("±%.1f°", deg);
    }

    /**
     * Số phát đạn đã bắn liên tiếp trong chuỗi nhấp hỏa lực hiện tại.
     */
    public int getContinuousShots() {
        return continuousShots;
    }

    /**
     * Reset số đạn dồn dập và thời gian hồi tâm về trạng thái chuẩn ban đầu.
     */
    public void resetRecoil() {
        this.continuousShots = 0;
        this.timeSinceLastShot = 999.0;
    }

    /**
     * Tính toán độ tản đạn thực tế của phát bắn tiếp theo:
     * - Chỉ phát bắn đầu tiên duy nhất: Độ chính xác tuyệt đối (First-shot pinpoint
     * accuracy).
     * - Ngay từ phát thứ 2 đến phát thứ 4: Độ tản đạn tăng vọt rất dốc theo nhịp
     * click dồn dập.
     * - Từ phát thứ 4 trở đi: Đạt ngưỡng tản đạn cực đại gấp 8 lần (Max bloom
     * spread ±22.9°).
     */
    public double getCurrentDynamicSpreadAngle() {
        double minSpread = getInitialSpreadAngle();
        double maxSpread = getMaxContinuousSpreadAngle();
        if (continuousShots <= 1) {
            return minSpread;
        }
        double bloomProgress = Math.min(1.0, (continuousShots - 1) / 3.0);
        return minSpread + (maxSpread - minSpread) * bloomProgress;
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);

        timeSinceLastShot += deltaTime;
        // Ngưỡng phục hồi tâm ngắm: Nếu người chơi ngừng bắn quá 0.45s mới bắt đầu giảm
        // độ giật
        // Nếu nhấp chuột liên tục (nhịp < 0.45s), độ giật được giữ nguyên hoặc tăng
        // tiếp
        if (timeSinceLastShot > 0.45) {
            if (continuousShots > 0) {
                continuousShots = Math.max(0, continuousShots - (int) Math.ceil(deltaTime * 16.0));
            }
            if (timeSinceLastShot > 0.70) {
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
        if (!canAttack())
            return false;

        // Nếu hết đạn, kích hoạt nạp đạn tự động
        if (currentAmmo <= 0) {
            reload();
            return false;
        }

        // Tăng số đạn liên tiếp và cập nhật thời gian phát bắn
        continuousShots++;
        timeSinceLastShot = 0.0;

        // Tạo độ tản xạ động:
        // - Viên đầu tiên: chuẩn xác tuyệt đối vào hồng tâm.
        // - Từ viên thứ 2 trở đi khi spam click: lực giật nảy hất đạn dạt mạnh sang 2
        // bên
        // với độ lệch tối thiểu 25% bán kính bloom, ngăn việc đạn vô tình trúng tâm khi
        // spam.
        double dynamicSpread = getCurrentDynamicSpreadAngle();
        double jitter;
        if (continuousShots <= 1) {
            jitter = dynamicSpread > 0.0001
                    ? ThreadLocalRandom.current().nextDouble(-dynamicSpread, dynamicSpread)
                    : 0.0;
        } else {
            double minOffset = dynamicSpread * 0.30;
            double spreadRange = Math.max(0.0001, dynamicSpread - minOffset);
            double mag = minOffset + ThreadLocalRandom.current().nextDouble(0.0, spreadRange);
            boolean sign = ThreadLocalRandom.current().nextBoolean();
            jitter = sign ? mag : -mag;
        }
        double firedAngle = aimAngle + jitter;

        // Tính tọa độ đầu nòng súng (cách tâm operative 28px)
        int barrelLength = 28;
        double muzzleX = originX + Math.cos(firedAngle) * barrelLength;
        double muzzleY = originY + Math.sin(firedAngle) * barrelLength;

        // Roll critical hit và tính toán sát thương thực tế
        boolean isCrit = rollCritical();
        int shotDamage = calculateDamage(isCrit);

        // Bắn viên đạn Carbine uy lực cao
        Projectile bullet = new Projectile(muzzleX, muzzleY, firedAngle, BULLET_SPEED,
                shotDamage, BULLET_LIFETIME, true, isCrit);
        pm.spawn(bullet);
        GameSoundManager.play(SoundEffect.GUN_SCR_SHOOT);

        // Tiêu hao đạn và hồi cooldown
        currentAmmo--;
        cooldownTimer = fireRateCooldown;

        if (currentAmmo <= 0) {
            reload();
        }

        return true;
    }

    @Override
    protected void onStartReload() {
        GameSoundManager.playReload(SoundEffect.GUN_SCR_RELOAD);
    }

    @Override
    public void render(Graphics2D g, Camera camera, double originX, double originY, double aimAngle) {
        int screenPX = camera.worldToScreenX(originX);
        int screenPY = camera.worldToScreenY(originY);

        int barrelLen = 30;
        int endX = (int) (screenPX + Math.cos(aimAngle) * barrelLen);
        int endY = (int) (screenPY + Math.sin(aimAngle) * barrelLen);

        // 1. Thân súng Semi-Carbine thon gọn kim loại màu xanh ô liu quân sự / xám thép
        // dã chiến
        g.setStroke(new BasicStroke(3.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        g.setColor(new Color(55, 65, 58)); // Xanh ô liu dã chiến
        g.drawLine(screenPX, screenPY, endX, endY);

        // 2. Đầu nòng bù giật (Muzzle Brake) màu bạc sáng
        g.setColor(new Color(210, 225, 235));
        g.fillOval(endX - 2, endY - 2, 4, 4);

        // 3. Hộp tiếp đạn 20 viên vát xéo đặc trưng
        int magOffset = 13;
        int magX = (int) (screenPX + Math.cos(aimAngle) * magOffset);
        int magY = (int) (screenPY + Math.sin(aimAngle) * magOffset);
        g.setColor(new Color(28, 34, 30));
        g.fillOval(magX - 3, magY - 3, 6, 6);

        g.setStroke(new BasicStroke(1.0f));
    }
}
