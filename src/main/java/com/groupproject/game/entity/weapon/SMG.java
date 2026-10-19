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
 * Vũ khí chính: Súng tiểu liên (Submachine Gun - SMG).
 *
 * <p>WHY: Thiết kế đặc trưng cho phong cách chiến đấu cơ động, áp sát nhanh cự ly gần - trung bình.
 * Súng sở hữu sát thương đơn thấp (11 HP mỗi viên) nhưng được bù đắp bởi tốc độ xả đạn cực cao (~920 RPM,
 * tức ~0.0652s giữa mỗi phát bắn, ~15.3 viên/giây) mang lại lượng sát thương theo giây (DPS) dồi dào (~168 DPS).
 *
 * <p>Độ lệch đạn khi sấy cao (High Bullet Spread / Spray Inaccuracy):
 * Khi người chơi giữ chặt phím bắn để sấy liên thanh (Full-Auto), mỗi viên đạn sẽ chịu độ tản xạ ngẫu nhiên
 * lên tới +-0.18 radian (+-10.3 độ, gấp hơn 4.5 lần súng trường AR). Điều này đòi hỏi người chơi phải khéo léo
 * áp sát mục tiêu để tối đa hóa lượng đạn trúng đích hoặc sấy quét diện rộng khi đối đầu với đám đông quái.
 */
public class SMG extends Weapon {

    /** Sát thương đơn của mỗi viên đạn tiểu liên. */
    public static final int DEFAULT_DAMAGE = 11;

    /** Tốc độ bắn cực cao ~920 viên/phút (~0.0652s giữa mỗi phát bắn). */
    public static final double FIRE_RATE = 60.0 / 920.0;

    /** Băng đạn lớn 35 viên giúp xả đạn liên tục trong ~2.28 giây. */
    public static final int MAG_CAPACITY = 25;

    /** Thời gian nạp đạn nhanh (1.4 giây) tạo tính cơ động vượt trội. */
    public static final double RELOAD_TIME = 1.4;

    /** Vận tốc bay của đạn tiểu liên (pixel/giây). */
    public static final double BULLET_SPEED = 760.0;

    /** Thời gian tồn tại của đạn (giây), bay tối đa ~646 px trước khi tiêu biến. */
    public static final double BULLET_LIFETIME = 0.85;

    /** Độ lệch đạn tối đa khi sấy liên thanh (+- radian, tương đương +- 10.3 độ). */
    public static final double SPREAD_ANGLE = 0.18;

    /**
     * Khởi tạo súng tiểu liên SMG với cấu hình mặc định (Slot 1 hoặc Slot 2).
     */
    public SMG() {
        this(1);
    }

    /**
     * Khởi tạo súng tiểu liên SMG với chỉ số slot chỉ định.
     *
     * @param slotIndex Vị trí phím nóng (1 hoặc 2 cho vũ khí chính).
     */
    public SMG(int slotIndex) {
        super("Submachine Gun (SMG)", slotIndex, DEFAULT_DAMAGE, FIRE_RATE, MAG_CAPACITY, RELOAD_TIME, SPREAD_ANGLE);
        this.ammoType = com.groupproject.game.entity.item.AmmoType.SMG;
    }

    @Override
    public boolean isAutomatic() {
        // Cho phép người chơi giữ phím chuột trái để xả đạn liên tục (Full-Auto)
        return true;
    }

    @Override
    public boolean attack(double originX, double originY, double aimAngle,
                          ProjectileManager pm, List<MeleeSlash> slashList) {
        if (!canAttack()) return false;

        // Nếu hết đạn, tự động kích hoạt nạp đạn nhanh
        if (currentAmmo <= 0) {
            reload();
            return false;
        }

        // Tạo độ lệch đạn ngẫu nhiên khi sấy liên thanh (giảm dần khi accuracyStars tăng)
        double jitter = currentSpreadAngle > 0.0001
                ? ThreadLocalRandom.current().nextDouble(-currentSpreadAngle, currentSpreadAngle)
                : 0.0;
        double firedAngle = aimAngle + jitter;

        // Tọa độ đầu nòng súng tiểu liên (cách tâm operative 22px)
        int barrelLength = 22;
        double muzzleX = originX + Math.cos(firedAngle) * barrelLength;
        double muzzleY = originY + Math.sin(firedAngle) * barrelLength;

        // Roll critical hit and calculate dynamic damage
        boolean isCrit = rollCritical();
        int shotDamage = calculateDamage(isCrit);

        // Bắn viên đạn SMG
        Projectile bullet = new Projectile(muzzleX, muzzleY, firedAngle, BULLET_SPEED,
                shotDamage, BULLET_LIFETIME, true, isCrit);
        pm.spawn(bullet);

        // Tiêu hao 1 viên đạn và bắt đầu đếm thời gian hồi chiêu
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

        int barrelLen = 22;
        int endX = (int) (screenPX + Math.cos(aimAngle) * barrelLen);
        int endY = (int) (screenPY + Math.sin(aimAngle) * barrelLen);

        // 1. Thân súng tiểu liên nhỏ gọn, màu xám xanh titan hiện đại
        g.setStroke(new BasicStroke(3.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        g.setColor(new Color(40, 50, 62));
        g.drawLine(screenPX, screenPY, endX, endY);

        // 2. Đầu nòng súng flash hider màu cyan neon đặc trưng của SMG
        g.setColor(new Color(0, 230, 255));
        g.fillOval(endX - 2, endY - 2, 4, 4);

        // 3. Hộp tiếp đạn thẳng dài đặc trưng của súng tiểu liên 9mm
        int magOffset = 11;
        int magX = (int) (screenPX + Math.cos(aimAngle) * magOffset);
        int magY = (int) (screenPY + Math.sin(aimAngle) * magOffset);
        g.setColor(new Color(20, 26, 32));
        g.fillOval(magX - 2, magY - 2, 5, 5);

        g.setStroke(new BasicStroke(1.0f));
    }
}
