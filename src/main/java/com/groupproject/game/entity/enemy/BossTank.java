package com.groupproject.game.entity.enemy;

import com.groupproject.game.core.Camera;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.entity.projectile.BossCannonShell;
import com.groupproject.game.entity.projectile.ExplosionVisual;
import com.groupproject.game.entity.projectile.Projectile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.state.PlayingState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Stage 4 Climax Titan Mecha Battle Tank Boss featuring a 3-layer decoupled
 * mechanical structure:
 * <ul>
 * <li><b>Layer 1 (Topmost)</b>: Autonomous Machine Gun Turret that continuously
 * tracks the player
 * and fires 5-round barrages (stationary or strafing).</li>
 * <li><b>Layer 2 (Middle)</b>: Heavy Artillery Cannon Turret activated only
 * when <b>HP &lt; 50%</b>.
 * Projects a targeting laser, locks on, and fires cover-penetrating AoE
 * explosive shells.</li>
 * <li><b>Layer 3 (Bottom)</b>: Heavy Armored Chassis &amp; Dual Caterpillar
 * Treads. The chassis angle
 * strictly aligns with the physical locomotion vector of the tank.</li>
 * </ul>
 */
public class BossTank extends TargetDummy {

    public enum TankState {
        PATROL_REPOSITION, // Chạy quanh buồng đấu tìm góc chiến thuật (0.20s - 0.55s)
        RAM_WINDUP, // Lấy đà bẻ lái cực nhanh (0.22s - 0.25s)
        RAM_RUSH, // Lao vọt tới tốc độ cực đại (950 px/s) gây 40 HP va chạm
        RAM_STUN, // Khựng lại 0.38s - 0.45s sau khi đâm sầm vào tường/cột cover cứng
        MG_BURST, // Xả loạt đạn súng máy 6-8 viên (bắn tại chỗ hoặc vừa chạy ngang vừa bắn)
        CANNON_AIMING, // Tháp pháo chính xoay nòng, chiếu laser bám theo người chơi (0.65s, chỉ khi HP
                       // < 50%)
        CANNON_LOCKED, // Khóa laser cố định hướng, nạp năng lượng (0.38s, chỉ khi HP < 50%)
        COOLDOWN // Nghỉ ngắn giữa các đợt ra chiêu (0.18s - 0.35s)
    }

    private TankState state = TankState.PATROL_REPOSITION;
    private double stateTimer = 0.50; // Tấn công dồn dập sau 0.50s mở màn
    private double actionCooldown = 0.35;

    // --- 3-Layer Decoupled Rotations ---
    private double chassisAngle = 0.0; // Layer 3: Góc xoay khung gầm & xích xe (đồng nhất hướng di chuyển)
    private double cannonAngle = 0.0; // Layer 2: Góc xoay tháp pháo chính cỡ đại
    private double machineGunAngle = 0.0; // Layer 1: Góc xoay ụ súng máy nóc xe

    // --- Locomotion Parameters ---
    private final double roamSpeed = 110.0; // Tăng tính cơ động khi lượn tìm góc bắn (110 px/s)
    private final double rushSpeed = 950.0; // Tốc độ đâm húc kinh hoàng (950 px/s - siêu nhanh như tên lửa)
    private double treadAnimTimer = 0.0;

    // --- Ram / Charge Attack Fields ---
    private double chargeTargetAngle = 0.0;
    private double chargeDistRemaining = 0.0;
    private double crashEffectTimer = 0.0; // Hiệu ứng va chạm chấn động khi đâm trúng vật cản

    // --- Machine Gun Burst Fields ---
    private int mgBulletsLeft = 0;
    private double mgFireTimer = 0.0;
    private boolean mgIsStrafing = false;
    private double mgStrafeAngle = 0.0;

    // --- Attack Sequence & Anti-Spam ---
    private boolean lastAttackWasRam = false;

    // --- Heavy Cannon Attack Fields ---
    private double cannonLaserAngle = 0.0;
    private double cannonRecoilTimer = 0.0;
    private double laserTargetWorldX = 0.0;
    private double laserTargetWorldY = 0.0;

    /**
     * Constructs the Titan Battle Tank Boss.
     *
     * @param x         Starting world X coordinate.
     * @param y         Starting world Y coordinate.
     * @param maxHealth Hit points (default 1000).
     * @param size      Hitbox diameter (default 84px).
     * @param name      Display classification name.
     */
    public BossTank(double x, double y, int maxHealth, double size, String name) {
        super(x, y, maxHealth, size, true, name);
    }

    public BossTank(double x, double y) {
        this(x, y, 1250, 96.0, "HEAVY COMBAT BATTLE TANK [BOSS]");
    }

    public TankState getState() {
        return state;
    }

    public void setState(TankState state) {
        this.state = state;
        if (state == TankState.RAM_RUSH && this.chargeDistRemaining <= 0) {
            this.chargeDistRemaining = 800.0;
        }
    }

    public double getChargeDistRemaining() {
        return chargeDistRemaining;
    }

    public void setChargeDistRemaining(double dist) {
        this.chargeDistRemaining = dist;
    }

    public double getStateTimer() {
        return stateTimer;
    }

    public void setStateTimer(double timer) {
        this.stateTimer = timer;
    }

    public double getChassisAngle() {
        return chassisAngle;
    }

    public void setChassisAngle(double angle) {
        this.chassisAngle = angle;
    }

    public double getCannonAngle() {
        return cannonAngle;
    }

    public void setCannonAngle(double angle) {
        this.cannonAngle = angle;
    }

    public double getMachineGunAngle() {
        return machineGunAngle;
    }

    public void setMachineGunAngle(double angle) {
        this.machineGunAngle = angle;
    }

    public double getCannonLaserAngle() {
        return cannonLaserAngle;
    }

    public double getRushSpeed() {
        return rushSpeed;
    }

    public int getMgBulletsLeft() {
        return mgBulletsLeft;
    }

    public boolean isMgStrafing() {
        return mgIsStrafing;
    }

    public boolean isCannonAwakened() {
        return currentHealth < (maxHealth * 0.5);
    }

    public boolean isLastAttackWasRam() {
        return lastAttackWasRam;
    }

    public void setLastAttackWasRam(boolean lastAttackWasRam) {
        this.lastAttackWasRam = lastAttackWasRam;
    }

    public double getLaserTargetWorldX() {
        return laserTargetWorldX;
    }

    public double getLaserTargetWorldY() {
        return laserTargetWorldY;
    }

    @Override
    public void updateAI(double deltaTime, double playerCenterX, double playerCenterY, double playerSize,
            DungeonMap dungeonMap, ProjectileManager projectileManager, PlayingState playingState) {
        if (!alive)
            return;

        update(deltaTime);
        updateKnockback(deltaTime, dungeonMap);

        if (cannonRecoilTimer > 0) {
            cannonRecoilTimer -= deltaTime;
        }
        if (crashEffectTimer > 0) {
            crashEffectTimer -= deltaTime;
        }

        // Trong thời gian xuất hiện (spawn portal telegraph), đứng yên và không hành
        // động
        if (isSpawning()) {
            return;
        }

        double dx = playerCenterX - x;
        double dy = playerCenterY - y;
        double distToPlayer = Math.hypot(dx, dy);
        double angleToPlayer = (distToPlayer > 0.001) ? Math.atan2(dy, dx) : 0.0;

        // Layer 1: Ụ súng máy trên cùng LUÔN LUÔN xoay ngắm bám theo người chơi
        machineGunAngle = angleToPlayer;

        // Layer 2: Tháp pháo chính: nếu HP >= 50% thì khóa nòng theo thân xe; nếu HP <
        // 50% thì xoay ngắm tự do
        if (!isCannonAwakened()) {
            cannonAngle = chassisAngle;
        }

        // Cập nhật State Machine
        switch (state) {
            case PATROL_REPOSITION ->
                handlePatrolState(deltaTime, playerCenterX, playerCenterY, angleToPlayer, distToPlayer, dungeonMap);
            case RAM_WINDUP -> handleRamWindupState(deltaTime, angleToPlayer);
            case RAM_RUSH ->
                handleRamRushState(deltaTime, playerCenterX, playerCenterY, playerSize, dungeonMap, playingState);
            case RAM_STUN -> handleRamStunState(deltaTime);
            case MG_BURST -> handleMgBurstState(deltaTime, playerCenterX, playerCenterY, dungeonMap, projectileManager);
            case CANNON_AIMING -> handleCannonAimingState(deltaTime, playerCenterX, playerCenterY, angleToPlayer);
            case CANNON_LOCKED -> handleCannonLockedState(deltaTime, projectileManager, playingState, dungeonMap);
            case COOLDOWN -> handleCooldownState(deltaTime, angleToPlayer);
        }

        // Layer 2: Nếu HP >= 50%, tháp pháo chính tuyệt đối khóa chặt theo góc xoay
        // thân xe
        if (!isCannonAwakened()) {
            cannonAngle = chassisAngle;
        }
    }

    /**
     * Trạng thái tuần tra điều động vị trí (Patrol / Reposition).
     * Xe tăng di chuyển nhẹ nhàng tìm góc thoáng, góc xích và thân xe đồng nhất với
     * hướng chạy.
     */
    private void handlePatrolState(double deltaTime, double playerCX, double playerCY,
            double angleToPlayer, double distToPlayer, DungeonMap map) {
        stateTimer -= deltaTime;
        treadAnimTimer += deltaTime * 8.0;

        // Di chuyển tiếp cận người chơi nếu ở quá xa, hoặc lượn vòng quanh
        double targetMoveAngle = angleToPlayer;
        if (distToPlayer < 200.0) {
            // Ở gần thì bẻ lái góc 75 độ để lượn vòng quanh người chơi
            targetMoveAngle = angleToPlayer + 1.3;
        }

        // Đồng nhất góc thân xe với hướng di chuyển
        chassisAngle = smoothRotate(chassisAngle, targetMoveAngle, 4.0 * deltaTime);

        double stepDist = roamSpeed * deltaTime;
        double stepX = Math.cos(chassisAngle) * stepDist;
        double stepY = Math.sin(chassisAngle) * stepDist;
        double half = size / 2.0;

        // Trượt dọc theo địa hình
        if (map == null || map.isAreaWalkable(x + stepX - half, y - half, size, size)) {
            x += stepX;
        }
        if (map == null || map.isAreaWalkable(x - half, y + stepY - half, size, size)) {
            y += stepY;
        }

        if (stateTimer <= 0) {
            chooseNextAction(distToPlayer);
        }
    }

    /**
     * Quyết định chiêu thức tiếp theo dựa trên cự ly và lượng máu còn lại.
     * Cân bằng Phase 1 (nửa thanh máu đầu): Giảm spam đâm bằng cách kết hợp với bắn
     * súng máy.
     * Nếu đòn trước vừa đâm thì đòn tiếp theo bắt buộc dùng súng máy.
     */
    private void chooseNextAction(double distToPlayer) {
        int roll = ThreadLocalRandom.current().nextInt(100);

        // Nếu vừa mới thực hiện đòn đâm trước đó: BẮT BUỘC dùng súng máy để tránh spam
        // đâm liên tục
        if (lastAttackWasRam) {
            triggerMachineGunBurst();
            lastAttackWasRam = false;
            return;
        }

        // Khi HP < 50%, tháp pháo chính thức tỉnh: có 45% cơ hội dùng Chiêu 3 (Đại pháo
        // nổ)
        if (isCannonAwakened() && roll < 45) {
            state = TankState.CANNON_AIMING;
            stateTimer = 0.65; // 0.65s (rút ngắn từ 0.90s) chiếu laser dính chặt theo người chơi
            cannonLaserAngle = cannonAngle;
            laserTargetWorldX = x;
            laserTargetWorldY = y;
            lastAttackWasRam = false;
            return;
        }

        // Khi HP >= 50% (nửa thanh máu đầu): Cân bằng kết hợp súng máy (50%) và đâm húc
        // (50%)
        if (!isCannonAwakened()) {
            if (roll < 50) {
                triggerMachineGunBurst();
                lastAttackWasRam = false;
            } else {
                state = TankState.RAM_WINDUP;
                stateTimer = 0.25; // 0.25s (rút ngắn từ 0.30s) bẻ lái cực nhanh rồi phóng vọt đi tức thì
                lastAttackWasRam = true;
            }
            return;
        }

        // Khi HP < 50%: nếu không dùng đại pháo thì đâm hoặc súng máy
        if (distToPlayer >= 140.0 && roll < 70) {
            state = TankState.RAM_WINDUP;
            stateTimer = 0.22;
            lastAttackWasRam = true;
            return;
        }

        triggerMachineGunBurst();
        lastAttackWasRam = false;
    }

    /**
     * Kích hoạt Chiêu 2: Bắn loạt đạn súng máy (6-8 viên), 65% cơ hội vừa chạy
     * ngang (strafe) vừa xả đạn để khó né hơn.
     */
    private void triggerMachineGunBurst() {
        state = TankState.MG_BURST;
        stateTimer = 1.0; // Thời gian timeout bảo vệ
        mgBulletsLeft = isCannonAwakened() ? 8 : 6;
        mgFireTimer = 0.04; // Nổ súng phát đầu tiên gần như lập tức
        mgIsStrafing = ThreadLocalRandom.current().nextInt(100) < 65;
        mgStrafeAngle = chassisAngle + (ThreadLocalRandom.current().nextBoolean() ? 1.57 : -1.57);
    }

    /**
     * Chiêu 1: Giai đoạn lấy đà đâm húc (Wind-up).
     * Bẻ lái cực nhanh thẳng hướng người chơi và lao vút tới mà không hiện vệt cảnh
     * báo hướng đâm.
     */
    private void handleRamWindupState(double deltaTime, double angleToPlayer) {
        stateTimer -= deltaTime;
        chargeTargetAngle = angleToPlayer;
        // Bánh xích và thân xe xoay bám rất nhanh về hướng người chơi
        chassisAngle = smoothRotate(chassisAngle, chargeTargetAngle, 18.0 * deltaTime);

        if (stateTimer <= 0) {
            chassisAngle = chargeTargetAngle; // Khóa chính xác góc đâm theo người chơi
            state = TankState.RAM_RUSH;
            stateTimer = 0.90; // Tối đa lao trong 0.90s
            chargeDistRemaining = 800.0;
        }
    }

    /**
     * Chiêu 1: Giai đoạn lao vọt tốc độ cao (Rush - 950 px/s).
     * Thân xe và bánh xích giữ nguyên hướng lao. Đâm trúng người chơi gây 40 HP sát
     * thương cực mạnh.
     * Khi chạm vào vật cản (tường cứng, cột đá, hoặc thùng gỗ) thì DỪNG ĐÂM LẠI và
     * tạo hiệu ứng đâm trúng vật cản!
     */
    private void handleRamRushState(double deltaTime, double playerCX, double playerCY, double playerSize,
            DungeonMap map, PlayingState playingState) {
        stateTimer -= deltaTime;
        treadAnimTimer += deltaTime * 40.0;

        double stepDist = rushSpeed * deltaTime;
        double stepX = Math.cos(chassisAngle) * stepDist;
        double stepY = Math.sin(chassisAngle) * stepDist;
        double half = size / 2.0;

        // 1. Kiểm tra va chạm với người chơi
        double distToPlayer = Math.hypot(playerCX - x, playerCY - y);
        if (distToPlayer <= (half + playerSize / 2.0 + 4.0)) {
            if (playingState != null) {
                playingState.damagePlayer(40); // Sát thương đâm húc nâng cấp lên 40 HP cực mạnh
                playingState.triggerScreenShake(0.35);
                double impactX = x + Math.cos(chassisAngle) * half;
                double impactY = y + Math.sin(chassisAngle) * half;
                playingState.getActiveExplosions().add(new ExplosionVisual(impactX, impactY, 50.0, 0.30));
            }
            // Sau khi húc trúng người chơi, dừng lao và chuyển sang cooldown ngắn
            state = TankState.COOLDOWN;
            stateTimer = isCannonAwakened() ? 0.25 : 0.35;
            return;
        }

        // 2. Cày nát và đâm xuyên qua các ô cover tạm thời (thùng gỗ) nằm trên đường
        // đâm
        // Cỗ xe tăng bọc thép hạng nặng nghiền nát thùng gỗ thành mảnh vụn mà KHÔNG bị
        // dừng lại
        if (map != null) {
            boolean crushedAny = map.damageDestructibleCoverInArea(x + stepX - half - 4, y + stepY - half - 4, size + 8,
                    size + 8, 120);
            if (crushedAny && playingState != null) {
                playingState.triggerScreenShake(0.20);
                double impactX = x + Math.cos(chassisAngle) * (half + 4.0);
                double impactY = y + Math.sin(chassisAngle) * (half + 4.0);
                playingState.getActiveExplosions().add(new ExplosionVisual(impactX, impactY, 40.0, 0.25));
            }
        }

        // 3. Kiểm tra va chạm với các vật cản kiên cố vĩnh viễn (Tường cứng, Cột đá) -
        // BỎ QUA cover tạm thời
        boolean solidBlocked = false;
        if (map != null) {
            solidBlocked = !map.isAreaWalkableIgnoringDestructible(x + stepX - half, y + stepY - half, size, size);
        }

        // 4. CHỈ KHI chạm vào vật cản kiên cố không thể phá hủy (Tường, Cột): DỪNG ĐÂM
        // LẠI và bị Stun
        if (solidBlocked) {
            state = TankState.RAM_STUN;
            stateTimer = isCannonAwakened() ? 0.38 : 0.45; // 0.38s - 0.45s Stun khựng lại
            crashEffectTimer = 0.40; // Kích hoạt hiệu ứng tóe lửa chấn động đâm trúng vật cản

            if (playingState != null) {
                playingState.triggerScreenShake(0.45);
                // Hiệu ứng sóng chấn động bộc phá và quầng lửa tại điểm mũi xe đâm vào vật cản
                double impactX = x + Math.cos(chassisAngle) * (half + 6.0);
                double impactY = y + Math.sin(chassisAngle) * (half + 6.0);
                playingState.getActiveExplosions().add(new ExplosionVisual(impactX, impactY, 65.0, 0.40));
            }
            return;
        }

        x += stepX;
        y += stepY;
        chargeDistRemaining -= stepDist;

        if (stateTimer <= 0 || chargeDistRemaining <= 0) {
            state = TankState.COOLDOWN;
            stateTimer = isCannonAwakened() ? 0.20 : 0.30;
        }
    }

    /**
     * Chiêu 1: Giai đoạn khựng lại (Stun) sau khi húc vào tường/cột đá kiên cố.
     */
    private void handleRamStunState(double deltaTime) {
        stateTimer -= deltaTime;
        if (stateTimer <= 0) {
            state = TankState.COOLDOWN;
            stateTimer = isCannonAwakened() ? 0.18 : 0.25;
        }
    }

    /**
     * Chiêu 2: Bắn loạt 6-8 viên đạn súng máy (kèm biến thể vừa chạy ngang vừa
     * bắn).
     */
    private void handleMgBurstState(double deltaTime, double playerCX, double playerCY,
            DungeonMap map, ProjectileManager projectileManager) {
        stateTimer -= deltaTime;

        // Nếu chọn biến thể chạy ngang (Strafe Run)
        if (mgIsStrafing) {
            treadAnimTimer += deltaTime * 10.0;
            chassisAngle = smoothRotate(chassisAngle, mgStrafeAngle, 6.0 * deltaTime);
            double stepDist = (roamSpeed * 1.1) * deltaTime;
            double stepX = Math.cos(chassisAngle) * stepDist;
            double stepY = Math.sin(chassisAngle) * stepDist;
            double half = size / 2.0;

            if (map == null || map.isAreaWalkable(x + stepX - half, y - half, size, size)) {
                x += stepX;
            } else {
                mgStrafeAngle += Math.PI; // Đảo chiều nếu chạm tường
            }
            if (map == null || map.isAreaWalkable(x - half, y + stepY - half, size, size)) {
                y += stepY;
            } else {
                mgStrafeAngle += Math.PI;
            }
        }

        mgFireTimer -= deltaTime;
        if (mgFireTimer <= 0 && mgBulletsLeft > 0) {
            mgFireTimer = isCannonAwakened() ? 0.08 : 0.09; // Cách nhau 80ms - 90ms mỗi viên
            mgBulletsLeft--;

            if (projectileManager != null) {
                // Đạn súng máy bắn ra từ nòng súng máy nóc xe (Layer 1)
                double muzzleDist = 18.0;
                double mx = x + Math.cos(machineGunAngle) * muzzleDist;
                double my = y + Math.sin(machineGunAngle) * muzzleDist;
                // Thêm độ lệch góc rất nhỏ để tạo chùm đạn liên thanh tự nhiên
                double spread = ThreadLocalRandom.current().nextDouble(-0.06, 0.06);
                Projectile bullet = new Projectile(mx, my, machineGunAngle + spread, 360.0, 9, 2.0, false);
                projectileManager.spawn(bullet);
            }
        }

        // Ngay khi xả hết đạn hoặc hết thời gian timeout, lập tức chuyển sang Cooldown
        // ngắn
        if (mgBulletsLeft <= 0 || stateTimer <= 0) {
            state = TankState.COOLDOWN;
            stateTimer = isCannonAwakened() ? 0.22 : 0.32;
        }
    }

    /**
     * Chiêu 3: Giai đoạn tháp pháo chính chiếu Laser bám theo người chơi (0.65s).
     * Laser HOÀN TOÀN LOCK ON vào vị trí người chơi theo thời gian thực (không trễ,
     * không chậm chạp bám đuôi).
     */
    private void handleCannonAimingState(double deltaTime, double playerCX, double playerCY, double angleToPlayer) {
        stateTimer -= deltaTime;
        // Khóa mục tiêu tuyệt đối: nòng pháo, góc laser và tọa độ ngắm dính chặt 100%
        // vào người chơi
        cannonAngle = angleToPlayer;
        cannonLaserAngle = angleToPlayer;
        laserTargetWorldX = playerCX;
        laserTargetWorldY = playerCY;

        if (stateTimer <= 0) {
            state = TankState.CANNON_LOCKED;
            stateTimer = 0.38; // Laser dừng lại khóa góc cố định trong 0.38s - thời gian duy nhất để người
                               // chơi né đòn!
        }
    }

    /**
     * Chiêu 3: Giai đoạn tia laser đã bị khóa cố định (0.38s).
     * Laser dừng lại, đây là khoảng thời gian DUY NHẤT để người chơi né đòn trước
     * khi phát đại pháo siêu tốc nổ bộc phá!
     */
    private void handleCannonLockedState(double deltaTime, ProjectileManager projectileManager,
            PlayingState playingState, DungeonMap dungeonMap) {
        stateTimer -= deltaTime;
        // cannonLaserAngle giữ nguyên không đổi để tạo cửa sổ né đòn duy nhất

        if (stateTimer <= 0) {
            // Khai hỏa đại pháo siêu tốc tức thì!
            cannonRecoilTimer = 0.25;
            double barrelLen = 54.0;
            double bx = x + Math.cos(cannonLaserAngle) * barrelLen;
            double by = y + Math.sin(cannonLaserAngle) * barrelLen;

            if (projectileManager != null) {
                // Đạn pháo bộc phá hạng nặng siêu thanh: 1350 px/s, sát thương 60 HP, nổ bán
                // kính cực đại 165px (quét rộng 330px)
                BossCannonShell shell = new BossCannonShell(bx, by, cannonLaserAngle, 1350.0, 60, 2.0, 165.0);
                projectileManager.spawn(shell);
            }

            if (playingState != null) {
                playingState.triggerScreenShake(0.45);
            }

            state = TankState.COOLDOWN;
            stateTimer = isCannonAwakened() ? 0.28 : 0.35; // Nghỉ rất ngắn sau phát đại pháo uy lực
        }
    }

    /**
     * Trạng thái hồi phục / nghỉ ngơi giữa các đợt chiêu thức (0.20s - 0.55s).
     */
    private void handleCooldownState(double deltaTime, double angleToPlayer) {
        stateTimer -= deltaTime;
        if (stateTimer <= 0) {
            state = TankState.PATROL_REPOSITION;
            if (isCannonAwakened()) {
                stateTimer = ThreadLocalRandom.current().nextDouble(0.20, 0.38);
            } else {
                stateTimer = ThreadLocalRandom.current().nextDouble(0.32, 0.55);
            }
        }
    }

    /**
     * Hàm nội suy góc quay mượt mà tránh giật góc quanh trục PI / -PI.
     */
    private double smoothRotate(double current, double target, double rate) {
        double diff = target - current;
        while (diff < -Math.PI)
            diff += 2 * Math.PI;
        while (diff > Math.PI)
            diff -= 2 * Math.PI;
        return current + diff * Math.min(1.0, rate);
    }

    // =========================================================================
    // RENDERING: 3-LAYER DECOUPLED TANK GRAPHICS
    // =========================================================================

    @Override
    public void render(Graphics2D g, Camera camera) {
        if (!alive)
            return;
        if (isSpawning()) {
            renderSpawnTelegraph(g, camera);
            return;
        }

        int screenCX = camera.worldToScreenX(x);
        int screenCY = camera.worldToScreenY(y);

        // 1. RENDER WARNING TELEGRAPHS (Laser đại pháo lock-on hoặc khóa góc cố định)
        renderWarningTelegraphs(g, camera, screenCX, screenCY);

        // 2. LAYER 3: CHASSIS & CATERPILLAR TREADS (Góc quay = chassisAngle)
        renderLayer3ChassisAndTreads(g, screenCX, screenCY);

        // 3. LAYER 2: MAIN HEAVY CANNON TURRET (Góc quay = cannonAngle)
        renderLayer2MainCannon(g, screenCX, screenCY);

        // 4. LAYER 1: TOP MACHINE GUN TURRET (Góc quay = machineGunAngle)
        renderLayer1MachineGun(g, screenCX, screenCY);

        // 5. OVERHEAD TITAN BOSS HEALTH BAR & STATUS BADGE
        renderOverheadBossUI(g, screenCX, screenCY);

        // 6. DẤU "!" CẢNH BÁO NGUY HIỂM KHI ĐÂM (RAM_WINDUP)
        renderWarningExclamation(g, screenCX, screenCY);
    }

    /**
     * Vẽ các đường dẫn cảnh báo chiến thuật (Tia laser ngắm bắn đại pháo lock on
     * hoặc khóa góc cố định).
     * Tuyệt đối không vẽ vệt cảnh báo hướng đâm theo đúng yêu cầu thiết kế.
     */
    private void renderWarningTelegraphs(Graphics2D g, Camera camera, int screenCX, int screenCY) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        // Tia Laser ngắm bắn đại pháo (CANNON_AIMING / CANNON_LOCKED)
        if (state == TankState.CANNON_AIMING || state == TankState.CANNON_LOCKED) {
            double barrelLen = isCannonAwakened() ? 60.0 : 46.0;
            int startX = (int) (screenCX + Math.cos(cannonAngle) * barrelLen);
            int startY = (int) (screenCY + Math.sin(cannonAngle) * barrelLen);

            double laserLen = 1200.0;
            int beamEndX = (int) (screenCX + Math.cos(cannonLaserAngle) * laserLen);
            int beamEndY = (int) (screenCY + Math.sin(cannonLaserAngle) * laserLen);

            int targetScreenX = camera.worldToScreenX(laserTargetWorldX);
            int targetScreenY = camera.worldToScreenY(laserTargetWorldY);

            if (state == TankState.CANNON_AIMING) {
                // Laser hoàn toàn lock-on theo người chơi: Vạch đỏ rực cảnh báo quang học
                g2.setColor(new Color(255, 40, 30, 200));
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(startX, startY, beamEndX, beamEndY);

                g2.setColor(new Color(255, 200, 200, 230));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawLine(startX, startY, beamEndX, beamEndY);

                // Tâm ngắm quang học Lock-On đỏ rực dính chặt 100% vào người chơi
                g2.setColor(new Color(255, 45, 35, 220));
                g2.setStroke(new BasicStroke(1.8f));
                int reticleR = 16;
                g2.drawOval(targetScreenX - reticleR, targetScreenY - reticleR, reticleR * 2, reticleR * 2);
                g2.drawLine(targetScreenX - reticleR - 5, targetScreenY, targetScreenX - reticleR + 3, targetScreenY);
                g2.drawLine(targetScreenX + reticleR - 3, targetScreenY, targetScreenX + reticleR + 5, targetScreenY);
                g2.drawLine(targetScreenX, targetScreenY - reticleR - 5, targetScreenX, targetScreenY - reticleR + 3);
                g2.drawLine(targetScreenX, targetScreenY + reticleR - 3, targetScreenX, targetScreenY + reticleR + 5);

                g2.setColor(Color.RED);
                g2.fillOval(targetScreenX - 2, targetScreenY - 2, 5, 5);
            } else {
                // Laser ĐÃ KHÓA CỐ ĐỊNH (cửa sổ né duy nhất): Chùm tia năng lượng đỏ thẫm viền
                // vàng phát quang cực mạnh
                g2.setColor(new Color(255, 30, 20, 245));
                g2.setStroke(new BasicStroke(4.5f));
                g2.drawLine(startX, startY, beamEndX, beamEndY);

                g2.setColor(new Color(255, 235, 110));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawLine(startX, startY, beamEndX, beamEndY);

                // Tâm ngắm cố định phát xung cảnh báo bộc phá đạn pháo
                g2.setColor(new Color(255, 220, 50, 230));
                g2.setStroke(new BasicStroke(2.2f));
                int lockedR = 20;
                g2.drawOval(targetScreenX - lockedR, targetScreenY - lockedR, lockedR * 2, lockedR * 2);
                g2.setColor(new Color(255, 40, 30, 210));
                g2.fillOval(targetScreenX - 4, targetScreenY - 4, 9, 9);
            }
        }

        g2.dispose();
    }

    /**
     * Hiển thị dấu "!" cảnh báo nguy hiểm trên đầu Boss khi chuẩn bị đâm
     * (RAM_WINDUP).
     */
    private void renderWarningExclamation(Graphics2D g, int screenCX, int screenCY) {
        if (state != TankState.RAM_WINDUP)
            return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        int half = (int) (size / 2.0);
        int badgeCX = screenCX;
        int badgeCY = screenCY - half - 52; // Nằm trên cả thanh máu và tên Boss

        // Hiệu ứng nhịp đập cảnh báo nhanh
        double pulse = 1.0 + 0.16 * Math.sin(System.currentTimeMillis() * 0.035);
        int w = (int) (26 * pulse);
        int h = (int) (26 * pulse);

        // Vòng hào quang cảnh báo đỏ cam lan tỏa
        g2.setColor(new Color(255, 50, 30, 95));
        g2.fillOval(badgeCX - w / 2 - 6, badgeCY - h / 2 - 6, w + 12, h + 12);

        // Biển cảnh báo vàng viền đỏ
        g2.setColor(new Color(255, 40, 30));
        g2.fillRoundRect(badgeCX - w / 2 - 2, badgeCY - h / 2 - 2, w + 4, h + 4, 8, 8);

        g2.setColor(new Color(255, 215, 0)); // Màu vàng cảnh báo rực rỡ
        g2.fillRoundRect(badgeCX - w / 2, badgeCY - h / 2, w, h, 6, 6);

        // Dấu "!" cảnh báo nổi bật
        g2.setColor(new Color(20, 20, 20));
        g2.setFont(new Font("SansSerif", Font.BOLD, (int) (18 * pulse)));
        FontMetrics fm = g2.getFontMetrics();
        String mark = "!";
        int textW = fm.stringWidth(mark);
        int textH = fm.getAscent() - fm.getDescent();
        g2.drawString(mark, badgeCX - textW / 2, badgeCY + textH / 2);

        g2.dispose();
    }

    /**
     * LAYER 3: KHUNG GẦM & BÁNH XÍCH (CHASSIS & TREADS).
     * Góc xoay = chassisAngle (đồng nhất hướng di chuyển thực tế).
     */
    private void renderLayer3ChassisAndTreads(Graphics2D g, int screenCX, int screenCY) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(screenCX, screenCY);
        g2.rotate(chassisAngle);

        int tankL = 94; // Chiều dài thân xe tăng (tương ứng kích thước hitbox size 96px)
        int tankW = 76; // Chiều rộng thân xe tăng
        int treadW = 16; // Bề rộng mỗi dải xích

        // 1. Bóng đổ gầm xe
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRoundRect(-tankL / 2 - 2, -tankW / 2 - 2, tankL + 4, tankW + 4, 8, 8);

        // 2. Hai dải bánh xích kim loại (Trái & Phải)
        Color treadBase = new Color(28, 32, 36);
        Color treadSegment = new Color(65, 70, 75);

        // Xích trái
        g2.setColor(treadBase);
        g2.fillRoundRect(-tankL / 2, -tankW / 2, tankL, treadW, 4, 4);
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(-tankL / 2, -tankW / 2, tankL, treadW, 4, 4);

        // Xích phải
        g2.setColor(treadBase);
        g2.fillRoundRect(-tankL / 2, tankW / 2 - treadW, tankL, treadW, 4, 4);
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(-tankL / 2, tankW / 2 - treadW, tankL, treadW, 4, 4);

        // Các rãnh xích cuộn chuyển động (Animated track treads)
        g2.setColor(treadSegment);
        int treadStep = 10;
        int treadOffset = (int) (treadAnimTimer * 10) % treadStep;
        for (int tx = -tankL / 2 + treadOffset; tx < tankL / 2; tx += treadStep) {
            g2.drawLine(tx, -tankW / 2 + 1, tx, -tankW / 2 + treadW - 2);
            g2.drawLine(tx, tankW / 2 - treadW + 1, tx, tankW / 2 - 2);
        }

        // 3. Khung gầm bọc thép Titan (Chassis Body)
        int bodyW = tankW - treadW * 2 + 4;
        Color bodyColor = (critFlashTimer > 0)
                ? new Color(225, 45, 45) // Chớp đỏ khi chịu đòn chí mạng
                : ((hitFlashTimer > 0) ? Color.WHITE : new Color(50, 62, 52)); // Rêu quân sự Titan
        g2.setColor(bodyColor);
        g2.fillRoundRect(-tankL / 2 + 6, -bodyW / 2, tankL - 12, bodyW, 8, 8);

        // Viền giáp
        g2.setColor(new Color(85, 105, 90));
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawRoundRect(-tankL / 2 + 6, -bodyW / 2, tankL - 12, bodyW, 8, 8);

        // Tấm chắn giáp bùn trước & sau
        g2.setColor(new Color(35, 42, 36));
        g2.fillRect(-tankL / 2 + 2, -tankW / 2 - 1, 12, treadW + 2);
        g2.fillRect(-tankL / 2 + 2, tankW / 2 - treadW - 1, 12, treadW + 2);
        g2.fillRect(tankL / 2 - 14, -tankW / 2 - 1, 12, treadW + 2);
        g2.fillRect(tankL / 2 - 14, tankW / 2 - treadW - 1, 12, treadW + 2);

        // Hiệu ứng đâm trúng vật cản: khói cuộn và tia lửa tóe ra từ mũi trước xe tăng
        if (state == TankState.RAM_STUN || crashEffectTimer > 0) {
            // Khói xám trắng và đen bốc lên cuồn cuộn từ mũi trước
            g2.setColor(new Color(230, 230, 230, 190));
            g2.fillOval(tankL / 2 - 6, -14, 22, 22);
            g2.setColor(new Color(160, 160, 160, 160));
            g2.fillOval(tankL / 2 - 2, -6, 26, 26);
            g2.setColor(new Color(90, 90, 90, 140));
            g2.fillOval(tankL / 2 - 4, 4, 20, 20);

            // Chùm tia lửa tóe ra từ cản trước va chạm
            g2.setColor(new Color(255, 240, 100));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawLine(tankL / 2, -8, tankL / 2 + 18, -16);
            g2.drawLine(tankL / 2, 0, tankL / 2 + 24, 0);
            g2.drawLine(tankL / 2, 8, tankL / 2 + 18, 16);
            g2.setColor(new Color(255, 120, 30));
            g2.drawLine(tankL / 2, -4, tankL / 2 + 14, -8);
            g2.drawLine(tankL / 2, 4, tankL / 2 + 14, 8);

            // Điểm chớp sáng kim loại va chạm
            g2.setColor(Color.WHITE);
            g2.fillOval(tankL / 2 - 3, -6, 12, 12);
        }

        g2.dispose();
    }

    /**
     * LAYER 2: THÁP PHÁO CHÍNH XE TĂNG (MAIN HEAVY CANNON TURRET).
     * Góc xoay = cannonAngle. Kích hoạt khi HP < 50%.
     */
    private void renderLayer2MainCannon(Graphics2D g, int screenCX, int screenCY) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(screenCX, screenCY);
        g2.rotate(cannonAngle);

        boolean awakened = isCannonAwakened();
        int barrelLen = awakened ? 60 : 46;
        int barrelW = 14;

        // Giật lùi nòng pháo khi khai hỏa
        if (cannonRecoilTimer > 0) {
            barrelLen -= (int) (cannonRecoilTimer * 22.0);
        }

        // 1. Nòng pháo chính cỡ đại (Heavy Cannon Barrel)
        Color barrelColor = awakened ? new Color(175, 45, 30) : new Color(55, 60, 65);
        g2.setColor(barrelColor);
        g2.fillRoundRect(12, -barrelW / 2, barrelLen, barrelW, 4, 4);
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(12, -barrelW / 2, barrelLen, barrelW, 4, 4);

        // Loa hãm giật đầu nòng (Muzzle Brake)
        g2.setColor(awakened ? new Color(230, 80, 40) : new Color(40, 45, 50));
        g2.fillRect(12 + barrelLen - 7, -barrelW / 2 - 2, 10, barrelW + 4);

        // 2. Tháp pháo chính hình lục giác (Hexagonal Turret)
        int r = 28;
        int[] hexX = new int[] { r, r / 2, -r / 2, -r, -r / 2, r / 2 };
        int[] hexY = new int[] { 0, (int) (r * 0.86), (int) (r * 0.86), 0, (int) (-r * 0.86), (int) (-r * 0.86) };

        Color turretBase = (critFlashTimer > 0)
                ? new Color(235, 45, 45) // Chớp đỏ khi chịu đòn chí mạng
                : (awakened
                        ? ((hitFlashTimer > 0) ? Color.WHITE : new Color(130, 35, 30))
                        : ((hitFlashTimer > 0) ? Color.WHITE : new Color(60, 72, 65)));

        g2.setColor(turretBase);
        g2.fillPolygon(hexX, hexY, 6);

        // Viền tháp pháo: phát sáng cam rực rỡ khi mở khóa
        Color turretBorder = awakened ? new Color(255, 120, 40) : new Color(90, 110, 95);
        g2.setColor(turretBorder);
        g2.setStroke(new BasicStroke(2.2f));
        g2.drawPolygon(hexX, hexY, 6);

        // 3. Lõi lò phản ứng tháp pháo
        if (awakened) {
            g2.setColor(new Color(255, 190, 30));
            g2.fillOval(-9, -9, 18, 18);
            g2.setColor(Color.WHITE);
            g2.fillOval(-5, -5, 10, 10);
        } else {
            g2.setColor(new Color(35, 40, 45));
            g2.fillOval(-8, -8, 16, 16);
        }

        g2.dispose();
    }

    /**
     * LAYER 1: Ụ SÚNG MÁY TRÊN CÙNG (TOP MACHINE GUN TURRET).
     * Góc xoay = machineGunAngle (liên tục bám theo người chơi).
     */
    private void renderLayer1MachineGun(Graphics2D g, int screenCX, int screenCY) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(screenCX, screenCY);
        g2.rotate(machineGunAngle);

        // 1. Hai nòng súng máy nòng kép song song
        g2.setColor(new Color(35, 40, 45));
        g2.fillRect(9, -6, 21, 4);
        g2.fillRect(9, 2, 21, 4);

        // Lóe sáng đầu nòng khi đang xả đạn súng máy
        if (state == TankState.MG_BURST && mgFireTimer > 0.04) {
            g2.setColor(new Color(255, 230, 70));
            g2.fillOval(27, -9, 10, 8);
            g2.fillOval(27, 2, 10, 8);
        }

        // 2. Bệ tròn ụ súng máy nóc xe
        g2.setColor(new Color(45, 52, 58));
        g2.fillOval(-12, -12, 24, 24);
        g2.setColor(new Color(110, 130, 140));
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawOval(-12, -12, 24, 24);

        // Cảm biến quang học đỏ
        g2.setColor(new Color(255, 50, 50));
        g2.fillOval(-4, -4, 8, 8);

        g2.dispose();
    }

    /**
     * Hiển thị thanh máu Boss Titan hùng vĩ trên đầu kèm trạng thái kích hoạt đại
     * pháo.
     */
    private void renderOverheadBossUI(Graphics2D g, int screenCX, int screenCY) {
        int half = (int) (size / 2.0);
        int barW = 160;
        int barH = 13;
        int barX = screenCX - barW / 2;
        int barY = screenCY - half - 30;

        // Nền thanh máu
        g.setColor(new Color(15, 20, 25, 220));
        g.fillRoundRect(barX - 2, barY - 2, barW + 4, barH + 4, 4, 4);

        // Thanh máu
        double hpRatio = Math.max(0.0, (double) currentHealth / maxHealth);
        int fillW = (int) (barW * hpRatio);

        // Màu thanh máu chuyển từ Vàng cam -> Đỏ thẫm khi thức tỉnh tháp pháo
        Color hpColor = isCannonAwakened() ? new Color(255, 55, 45) : new Color(255, 180, 30);
        g.setColor(hpColor);
        g.fillRoundRect(barX, barY, fillW, barH, 3, 3);

        g.setColor(new Color(220, 220, 230));
        g.setStroke(new BasicStroke(1.0f));
        g.drawRoundRect(barX - 2, barY - 2, barW + 4, barH + 4, 4, 4);

        // Nhãn tên Boss & Trạng thái tháp pháo
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        String title = isCannonAwakened()
                ? "☠ HEAVY COMBAT BATTLE TANK [ĐẠI PHÁO KÍCH HOẠT] ☠"
                : "★ HEAVY COMBAT BATTLE TANK [PHASE 1] ★";
        int textW = fm.stringWidth(title);
        g.setColor(isCannonAwakened() ? new Color(255, 80, 60) : new Color(255, 215, 80));
        g.drawString(title, screenCX - textW / 2, barY - 5);
    }
}
