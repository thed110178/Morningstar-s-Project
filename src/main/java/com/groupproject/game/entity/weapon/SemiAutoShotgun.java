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
 * Vũ khí: Súng săn bán tự động (Semi-auto Shotgun - SAS).
 *
 * <p>
 * WHY: Khẩu súng săn hỏa lực cự ly siêu gần với cơ chế tiếp đạn bằng hộp (Box Magazine)
 * và bắn bán tự động (Semi-Automatic). So với Shotgun truyền thống (Pump-Action):
 * <ul>
 *   <li><b>Hỏa lực DPS áp đảo:</b> Tốc độ ra đạn cực nhanh (0.28s hồi chiêu ~ 214 RPM so với 0.75s / 80 RPM của Shotgun),
 *       cho phép nhấp cò dồn hỏa lực liên hoàn, thổi bay kẻ địch trong tích tắc.</li>
 *   <li><b>Độ tỏa & Tầm bắn ngắn:</b> Góc tỏa đạn rộng hơn (±18.3° / 0.32 rad so với ±12.6° / 0.22 rad)
 *       và tầm sống của đạn ngắn hơn (0.24s ~ 228 px so với 320 px), đòi hỏi người chơi áp sát mục tiêu.</li>
 *   <li><b>Băng đạn hộp:</b> Băng đạn tiêu chuẩn 5 viên (nâng cấp tối đa 8 viên qua kỹ năng Băng Đạn Mở Rộng),
 *       thay nguyên hộp tiếp đạn trong 2.2s.</li>
 * </ul>
 */
public class SemiAutoShotgun extends Weapon {

    /** Sát thương tiêu chuẩn của mỗi viên đạn chì (1 sao). */
    public static final int DEFAULT_DAMAGE = 12;

    /** Thời gian hồi giữa các phát nhấp cò (0.28s ~ 214 RPM). */
    public static final double FIRE_RATE = 0.28;

    /** Dung lượng băng đạn hộp tiêu chuẩn (5 viên). */
    public static final int MAG_CAPACITY = 5;

    /** Thời gian thay toàn bộ hộp tiếp đạn (giây). */
    public static final double RELOAD_TIME = 2.2;

    /** Góc mở rộng tối đa của hình nón chùm đạn cơ sở (+- radian, tương đương +- 18.3 độ). */
    public static final double SPREAD_CONE_ANGLE = 0.32;

    /** Số lượng viên đạn chì bắn ra trong mỗi phát. */
    public static final int PELLETS_PER_SHOT = 6;

    /** Tốc độ đạn chì bay (pixel/giây). */
    public static final double PELLET_SPEED = 950.0;

    /**
     * Thời gian tồn tại của đạn chì (giây).
     * Quãng đường tối đa: 950 * 0.24 = 228 px.
     */
    public static final double PELLET_LIFETIME = 0.24;

    public SemiAutoShotgun() {
        this(1);
    }

    public SemiAutoShotgun(int slotIndex) {
        super("Semi-auto Shotgun", slotIndex, DEFAULT_DAMAGE, FIRE_RATE, MAG_CAPACITY, RELOAD_TIME, SPREAD_CONE_ANGLE);
        this.ammoType = AmmoType.SHOTGUN;
    }

    @Override
    public boolean isAutomatic() {
        return false; // Súng bán tự động: Người chơi nhấp chuột cho từng phát bắn chiến thuật tốc độ cao
    }

    @Override
    protected void onStartReload() {
        GameSoundManager.playReload(SoundEffect.GUN_SAS_RELOAD);
    }

    @Override
    public boolean attack(double originX, double originY, double aimAngle,
                          ProjectileManager pm, List<MeleeSlash> slashList) {

        // 1. Khi đang nạp hộp tiếp đạn: không thể bắn (phải đợi nạp xong hoặc chuyển đổi vũ khí)
        if (reloading || reloadPaused) {
            return false;
        }

        // 2. Kiểm tra cooldown hồi chiêu giữa các phát nhấp cò
        if (cooldownTimer > 0) {
            return false;
        }

        // 3. Nếu hết đạn -> tự động kích hoạt nạp đạn
        if (currentAmmo <= 0) {
            reload();
            return false;
        }

        // Điểm đầu nòng súng
        int barrelLength = 22;
        double muzzleX = originX + Math.cos(aimAngle) * barrelLength;
        double muzzleY = originY + Math.sin(aimAngle) * barrelLength;

        // Bắn chùm 6 viên đạn chì tỏa đều theo hình nón (gom chặt hơn khi accuracyStars tăng)
        double currentCone = currentSpreadAngle > 0.0001 ? currentSpreadAngle : SPREAD_CONE_ANGLE;
        double angleStep = (currentCone * 2.0) / (PELLETS_PER_SHOT - 1);

        // Roll critical hit và tính toán sát thương từng viên đạn chì
        boolean isCrit = rollCritical();
        int pelletDmg = calculateDamage(isCrit);

        for (int i = 0; i < PELLETS_PER_SHOT; i++) {
            double angleOffset = -currentCone + (i * angleStep);
            double jitter = ThreadLocalRandom.current().nextDouble(-0.025, 0.025);
            double pelletAngle = aimAngle + angleOffset + jitter;

            Projectile pellet = new Projectile(
                    muzzleX,
                    muzzleY,
                    pelletAngle,
                    PELLET_SPEED,
                    pelletDmg,
                    PELLET_LIFETIME,
                    true,
                    isCrit);
            pm.spawn(pellet);
        }

        // Phát âm thanh tiếng súng SAS nổ đanh gọn
        GameSoundManager.play(SoundEffect.GUN_SAS_SHOOT);

        // Tiêu hao 1 viên đạn trong hộp và đặt lại cooldown
        currentAmmo--;
        cooldownTimer = fireRateCooldown;

        // Nếu vừa bắn viên cuối cùng -> tự động nạp hộp tiếp đạn mới
        if (currentAmmo <= 0) {
            reload();
        }

        return true;
    }

    @Override
    public int getProjectilesPerShot() {
        return PELLETS_PER_SHOT; // 6 viên đạn chì mỗi phát bắn
    }

    @Override
    public void render(Graphics2D g, Camera camera, double originX, double originY, double aimAngle) {
        int screenPX = camera.worldToScreenX(originX);
        int screenPY = camera.worldToScreenY(originY);

        int barrelLen = 22;
        int endX = (int) (screenPX + Math.cos(aimAngle) * barrelLen);
        int endY = (int) (screenPY + Math.sin(aimAngle) * barrelLen);

        // 1. Thân súng bán tự động xám nòng thép hiện đại
        g.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.setColor(new Color(50, 52, 60));
        g.drawLine(screenPX, screenPY, endX, endY);

        // 2. Hộp tiếp đạn đỏ đô (Red Box Magazine) đặc trưng nhô ra phía trước
        int magOffset = 9;
        double perpAngle = aimAngle + Math.PI / 2.0;
        int magBaseX = (int) (screenPX + Math.cos(aimAngle) * magOffset);
        int magBaseY = (int) (screenPY + Math.sin(aimAngle) * magOffset);
        int magTipX = (int) (magBaseX + Math.cos(perpAngle) * 7);
        int magTipY = (int) (magBaseY + Math.sin(perpAngle) * 7);
        g.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(180, 30, 30)); // Đỏ đô hộp tiếp đạn
        g.drawLine(magBaseX, magBaseY, magTipX, magTipY);

        // 3. Đầu nòng súng ngắn bọc loa che lửa kim loại
        g.setColor(new Color(240, 100, 30));
        g.fillOval(endX - 2, endY - 2, 4, 4);

        g.setStroke(new BasicStroke(1.0f));
    }
}

