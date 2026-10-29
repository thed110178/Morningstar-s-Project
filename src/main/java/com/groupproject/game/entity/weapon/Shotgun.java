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
 * Vũ khí: Súng săn (Shotgun) gắn vào ô phím [2].
 *
 * <p>
 * WHY: Vũ khí hủy diệt cự ly gần (Close-Quarters Breaching). Mỗi phát bắn phóng
 * ra một chùm 6 viên đạn chì (pellets) tỏa rộng theo hình nón.
 * 
 * <p>
 * Cơ chế nạp đạn đặc biệt (Incremental Shell-by-Shell Reload):
 * <ul>
 * <li>Nạp từng viên một cho đến khi đầy băng (8 viên). Mỗi viên mất khoảng 0.45s để nạp vào buồng.</li>
 * <li>Ngắt nạp đạn (Reload Cancel): Khi người chơi bấm bắn (chuột trái), nếu trong súng đã có ít nhất
 *     1 viên đạn, tiến trình nạp đạn sẽ bị hủy ngay lập tức và khẩu súng bắn ngay mà không cần đợi nạp đầy.</li>
 * <li>Nếu trong súng đang có 0 viên đạn, bấm bắn trong lúc nạp sẽ không ngắt nạp để súng nạp xong ít nhất 1 viên.</li>
 * <li>Cơ chế tiêu biến theo tầm xa: Đạn có thời gian sống ngắn (0.32s), tốc độ cao (1000 px/s),
 *     tối đa bay ~320 px rồi tự tiêu biến trong không khí để phản ánh đúng đặc tính cận chiến.</li>
 * </ul>
 */
public class Shotgun extends Weapon {

    /** Số lượng viên đạn chì bắn ra trong mỗi phát. */
    public static final int PELLETS_PER_SHOT = 6;

    /** Tốc độ đạn chì bay (pixel/giây). */
    public static final double PELLET_SPEED = 1000.0;

    /**
     * Thời gian tồn tại của đạn chì (giây).
     * Quãng đường tối đa: 1000 * 0.32 = 320 px.
     * Quá tầm này đạn tự động biến mất, ngăn việc bắn tỉa từ xa.
     */
    public static final double PELLET_LIFETIME = 0.32;

    /** Góc mở rộng tối đa của hình nón đạn (+- radian, tương đương +- 12.6 độ). */
    public static final double SPREAD_CONE_ANGLE = 0.22;

    /** Thời gian nạp mỗi viên đạn chì vào buồng đạn (giây). */
    public static final double TIME_PER_SHELL = 0.45;

    public Shotgun() {
        // reloadDuration ở lớp cha đại diện cho thời gian nạp 1 viên shell (0.45s)
        super("Shotgun", 2, 14, 0.75, 8, TIME_PER_SHELL, SPREAD_CONE_ANGLE);
        this.ammoType = com.groupproject.game.entity.item.AmmoType.SHOTGUN;
    }

    /**
     * Cập nhật tiến trình nạp đạn từng viên một.
     * <p>
     * Mỗi khi hết chu kỳ TIME_PER_SHELL (0.45s), nếu kho còn đạn thì nạp 1 viên (+1 ammo, trừ 1 từ kho).
     * Nếu đạn chưa đầy và kho còn đạn, tiếp tục chu kỳ nạp viên tiếp theo cho đến khi đạt tối đa 8 viên hoặc hết đạn.
     */
    @Override
    public void update(double deltaTime) {
        if (cooldownTimer > 0) {
            cooldownTimer -= deltaTime;
        }

        if (reloading) {
            reloadTimer -= deltaTime;
            if (reloadTimer <= 0) {
                if (ammoType != null && boundAmmoInventory != null) {
                    if (boundAmmoInventory.getAmmo(ammoType) > 0) {
                        currentAmmo++;
                        boundAmmoInventory.consumeAmmo(ammoType, 1);

                        // Nếu chưa đầy đạn và kho còn đạn: mỗi lần nạp là 1 lần phát tiếng!
                        if (currentAmmo < magazineSize && boundAmmoInventory.getAmmo(ammoType) > 0) {
                            reloadTimer = reloadDuration;
                            GameSoundManager.playShotgunShellReload();
                        } else {
                            // Đã nạp đầy hoặc hết đạn dự trữ: hoàn tất chu kỳ nạp tự nhiên,
                            // để âm thanh viên cuối cùng vang trọn vẹn, không cưỡng chế stopReload()
                            reloading = false;
                            reloadPaused = false;
                            reloadTimer = 0.0;
                        }
                    } else {
                        reloading = false;
                        reloadTimer = 0.0;
                    }
                } else {
                    // Nạp thành công 1 viên đạn (chế độ độc lập / unit test không gắn kho)
                    currentAmmo++;

                    if (currentAmmo < magazineSize) {
                        reloadTimer = reloadDuration;
                        GameSoundManager.playShotgunShellReload();
                    } else {
                        reloading = false;
                        reloadTimer = 0.0;
                    }
                }
            }
        }
    }

    /**
     * Bắt đầu tiến trình nạp đạn từng viên nếu chưa đầy và kho dự trữ còn đạn.
     * Mỗi lần nạp là 1 lần phát tiếng độc lập.
     */
    @Override
    protected void onStartReload() {
        GameSoundManager.playShotgunShellReload();
    }

    @Override
    public void reload() {
        if (magazineSize > 0 && currentAmmo < magazineSize && !reloading) {
            if (ammoType != null && boundAmmoInventory != null && boundAmmoInventory.getAmmo(ammoType) <= 0) {
                return;
            }
            reloading = true;
            if (reloadPaused && reloadTimer > 0) {
                reloadPaused = false;
            } else {
                reloadTimer = reloadDuration; // 0.45s cho viên đầu tiên
            }
            onStartReload();
        }
    }

    /**
     * Bắn súng săn.
     * <p>
     * Nếu đang nạp dở mà trong súng có ít nhất 1 viên đạn:
     * HỦY TIẾN TRÌNH NẠP ĐẠN NGAY LẬP TỨC và thực hiện phát bắn.
     */
    @Override
    public boolean attack(double originX, double originY, double aimAngle,
            ProjectileManager pm, List<MeleeSlash> slashList) {

        // 1. Nếu đang nạp đạn (kể cả đang tạm dừng):
        if (reloading || reloadPaused) {
            if (currentAmmo > 0) {
                // Người chơi bấm bắn khi đã có ít nhất 1 viên -> Hủy nạp đạn ngay lập tức!
                cancelReload();
            } else {
                // Đang nạp viên đầu tiên (currentAmmo == 0) -> không thể bắn
                return false;
            }
        }

        // Kiểm tra thời gian hồi giữa các phát bắn (0.75s)
        if (cooldownTimer > 0) {
            return false;
        }

        // Nếu hết sạch đạn -> tự động kích hoạt nạp đạn từng viên
        if (currentAmmo <= 0) {
            reload();
            return false;
        }

        // Điểm đầu nòng súng săn đôi
        int barrelLength = 24;
        double muzzleX = originX + Math.cos(aimAngle) * barrelLength;
        double muzzleY = originY + Math.sin(aimAngle) * barrelLength;

        // Bắn chùm 6 viên đạn chì tỏa đều theo hình nón (gom chặt hơn khi accuracyStars tăng)
        double currentCone = currentSpreadAngle > 0.0001 ? currentSpreadAngle : SPREAD_CONE_ANGLE;
        double angleStep = (currentCone * 2.0) / (PELLETS_PER_SHOT - 1);

        // Roll critical hit and calculate dynamic pellet damage
        boolean isCrit = rollCritical();
        int pelletDmg = calculateDamage(isCrit);

        for (int i = 0; i < PELLETS_PER_SHOT; i++) {
            double angleOffset = -currentCone + (i * angleStep);
            // Thêm rung lắc ngẫu nhiên nhẹ giữa các viên
            double jitter = ThreadLocalRandom.current().nextDouble(-0.02, 0.02);
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
        // Phát tiếng nổ Shotgun và tiếng kéo ốp lót tay bơm lên đạn (Pump-Action) sau mỗi phát bắn
        GameSoundManager.play(SoundEffect.GUN_SHOTGUN_SHOOT);
        GameSoundManager.playChamber(SoundEffect.GUN_SHOTGUN_LOADING_AMMO);

        // Tiêu hao 1 viên đạn lớn và đặt lại cooldown
        currentAmmo--;
        cooldownTimer = fireRateCooldown;

        // Nếu vừa bắn viên cuối cùng -> tự động bắt đầu nạp viên mới
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

        int barrelLen = 24;
        int endX = (int) (screenPX + Math.cos(aimAngle) * barrelLen);
        int endY = (int) (screenPY + Math.sin(aimAngle) * barrelLen);

        // 1. Thân súng săn nòng đôi to dày (Heavy Double-Barrel Shotgun)
        g.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.setColor(new Color(60, 64, 72));
        g.drawLine(screenPX, screenPY, endX, endY);

        // 2. Ốp gỗ lót tay bơm (Pump Action Wooden Forend)
        int pumpOffset = 12;
        int pumpX = (int) (screenPX + Math.cos(aimAngle) * pumpOffset);
        int pumpY = (int) (screenPY + Math.sin(aimAngle) * pumpOffset);
        g.setColor(new Color(139, 69, 19)); // Nâu gỗ cổ điển
        g.fillOval(pumpX - 3, pumpY - 3, 6, 6);

        // 3. Đầu nòng súng đôi kim loại
        g.setColor(new Color(255, 110, 0)); // Đốm cam biểu thị uy lực bộc phá
        g.fillOval(endX - 2, endY - 2, 4, 4);

        g.setStroke(new BasicStroke(1.0f));
    }
}
