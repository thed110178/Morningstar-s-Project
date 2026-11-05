package com.groupproject.game.entity.enemy;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import com.groupproject.game.core.GameSoundManager;
import com.groupproject.game.core.GameSoundManager.SoundEffect;
import com.groupproject.game.core.GameSpriteManager;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.dungeon.Room;
import com.groupproject.game.dungeon.Tile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.state.PlayingState;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Cybernetic combat field medic / support drone specializing in allied regeneration and tactical evasion.
 *
 * <p>WHY: Introduces non-offensive support dynamics into combat rooms starting from Stage 2.
 * The medic possesses no offensive attacks; instead, it projects a continuous emerald healing aura centered
 * on itself that periodically restores health to injured squadmates (raiders, scouts, grenadiers, and bosses).
 * Its autonomous AI actively seeks out and approaches wounded allies while prioritizing personal survival:
 * <ul>
 *   <li>Sensors obstacle geometry to prevent being corner-trapped, actively executing wall-slide steering maneuvers.</li>
 *   <li>When no allies require triage, it autonomously identifies and retreats behind solid cover (walls/crates)
 *       to break line-of-sight and avoid the player's firing cone.</li>
 *   <li>Maintains dynamic staging distance and never idles in open sightlines.</li>
 * </ul>
 */
public class MedicEnemy extends TargetDummy {

    private double speed;
    private double healRadius;
    private int healAmount;
    private double healInterval;
    private double healTimer = 0.0;
    private double pulseAnimTimer = 0.0;
    private double fleeDistance;
    private double facingAngle = 0.0;

    // Tactical state flags
    private boolean inCover = false;
    private boolean seekingCover = false;
    private double strafeTimer = 0.0;
    private int strafeDirection = 1;

    // Current ally being approached for medical triage
    private TargetDummy healingTarget = null;

    /**
     * Constructs a standard combat field medic with tactical parameters.
     *
     * @param x         Starting world X coordinate.
     * @param y         Starting world Y coordinate.
     * @param maxHealth Hit points.
     */
    public MedicEnemy(double x, double y, int maxHealth) {
        this(x, y, maxHealth, 115.0, 150.0, 8, 0.70, 240.0, "Combat Field Medic [Support]");
    }

    /**
     * Constructs a customizable combat field medic with configurable attributes.
     *
     * @param x             Starting world X coordinate.
     * @param y             Starting world Y coordinate.
     * @param maxHealth     Hit points.
     * @param speed         Locomotion speed in pixels per second.
     * @param healRadius    Radius of the restorative aura centered on this medic.
     * @param healAmount    Amount of HP restored per pulse to each ally within the aura.
     * @param healInterval  Frequency in seconds between successive restorative pulses.
     * @param fleeDistance  Proximity threshold triggering evasive maneuvers away from the player.
     * @param name          Tactical display name.
     */
    public MedicEnemy(double x, double y, int maxHealth, double speed, double healRadius,
                      int healAmount, double healInterval, double fleeDistance, String name) {
        super(x, y, maxHealth, 30.0, false, name);
        this.speed = speed;
        this.healRadius = healRadius;
        this.healAmount = healAmount;
        this.healInterval = healInterval;
        this.healTimer = healInterval;
        this.fleeDistance = fleeDistance;
    }

    /**
     * Chuẩn hóa góc về khoảng [-PI, PI].
     */
    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2.0 * Math.PI;
        while (angle < -Math.PI) angle += 2.0 * Math.PI;
        return angle;
    }

    /**
     * Đo độ thông thoáng (Openness Score) tại một tọa độ thế giới bằng cách bắn tia đa hướng:
     * Quét 16 hướng phân bố đều 360 độ trong bán kính tối đa 96px (~2 ô gạch).
     * Điểm số càng cao nghĩa là không gian xung quanh càng rộng rãi, dễ dàng xoay trở và thoát hiểm.
     */
    public double calculateOpennessScore(double worldX, double worldY, DungeonMap map) {
        if (map == null) return 1000.0;
        double totalClearance = 0.0;
        int numRays = 16;
        double maxDist = 96.0; // 2 ô gạch
        double step = 16.0;
        double half = size / 2.0;

        for (int i = 0; i < numRays; i++) {
            double angle = (2.0 * Math.PI * i) / numRays;
            double cosA = Math.cos(angle);
            double sinA = Math.sin(angle);
            double dist = 0.0;

            for (double d = 16.0; d <= maxDist; d += step) {
                double qx = worldX + cosA * d;
                double qy = worldY + sinA * d;
                if (!map.isAreaWalkable(qx - half, qy - half, size, size)) {
                    break;
                }
                dist = d;
            }
            totalClearance += dist;
        }
        return totalClearance;
    }

    /**
     * Nhận diện xem một vị trí có phải là Ngõ Cụt (Dead-End) hoặc Góc Tường (Corner Pocket) nguy hiểm hay không.
     * Trả về true nếu vị trí này có nguy cơ cao biến thành bẫy tử thần khi bị người chơi áp sát dồn góc.
     */
    public boolean isDeadEndOrPocket(double worldX, double worldY, DungeonMap map) {
        if (map == null) return false;
        double half = size / 2.0;

        // 1. Kiểm tra 4 hướng chính ở cự ly 1 tile (48px)
        double checkDist = 48.0;
        boolean blockedNorth = !map.isAreaWalkable(worldX - half, worldY - checkDist - half, size, size);
        boolean blockedSouth = !map.isAreaWalkable(worldX - half, worldY + checkDist - half, size, size);
        boolean blockedWest = !map.isAreaWalkable(worldX - checkDist - half, worldY - half, size, size);
        boolean blockedEast = !map.isAreaWalkable(worldX + checkDist - half, worldY - half, size, size);

        int blockedCount = (blockedNorth ? 1 : 0) + (blockedSouth ? 1 : 0) +
                           (blockedWest ? 1 : 0) + (blockedEast ? 1 : 0);

        // Nếu 3 hoặc 4 phía chính bị chặn: Ngõ cụt 100%
        if (blockedCount >= 3) {
            return true;
        }

        // Nếu 2 phía vuông góc bị chặn (Góc tường 90 độ):
        boolean isCorner = (blockedNorth && blockedEast) || (blockedNorth && blockedWest) ||
                          (blockedSouth && blockedEast) || (blockedSouth && blockedWest);
        if (isCorner) {
            // Nếu là góc tường và độ thông thoáng tổng thể thấp (< 650 trên max 1536) -> Góc cụt nguy hiểm!
            double openness = calculateOpennessScore(worldX, worldY, map);
            if (openness < 650.0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Kiểm tra xem đường ngắm (Line of Sight) giữa 2 tọa độ thế giới có bị che chắn bởi vật cản kiên cố
     * (Tường WALL, Cột đá COVER, Thùng gỗ DESTRUCTIBLE_COVER, hoặc ra ngoài biên map) hay không.
     */
    public boolean isLineOfSightBlocked(double fromX, double fromY, double toX, double toY, DungeonMap map) {
        if (map == null) return false;
        double dx = toX - fromX;
        double dy = toY - fromY;
        double dist = Math.hypot(dx, dy);
        if (dist < 12.0) return false;

        double cosA = dx / dist;
        double sinA = dy / dist;
        double step = 16.0; // Quét mịn 16px mỗi bước
        for (double d = 16.0; d < dist - 8.0; d += step) {
            double qx = fromX + cosA * d;
            double qy = fromY + sinA * d;
            int c = (int) Math.floor(qx / GameConfig.TILE_SIZE);
            int r = (int) Math.floor(qy / GameConfig.TILE_SIZE);
            if (map.isValidGrid(c, r)) {
                Tile tile = map.getTile(c, r);
                if (tile != null && tile.isSolid()) {
                    return true; // Có vật cản che chắn làn đạn!
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Quét ma trận ô gạch lân cận trong bán kính ~384px để tìm vị trí ẩn nấp (Cover Spot) lý tưởng:
     * - Nằm sau vật cản (khuất đường ngắm Line-of-Sight với Operative).
     * - Địa hình đi lại an toàn (Walkable).
     * - Khoảng cách tới Operative an toàn (> 190px).
     * - TUYỆT ĐỐI LOẠI BỎ CÁC GÓC TƯỜNG / NGÕ CỤT (Dead-End / Corner Pocket Filter).
     * - Ưu tiên vị trí thông thoáng (Openness cao), có nhiều lối thoát hiểm dẫn ra trung tâm phòng.
     */
    public Point2D.Double findBestCoverLocation(double playerCenterX, double playerCenterY, DungeonMap map) {
        if (map == null) return null;

        int centerCol = (int) Math.floor(x / GameConfig.TILE_SIZE);
        int centerRow = (int) Math.floor(y / GameConfig.TILE_SIZE);
        double half = size / 2.0;

        Room currentRoom = map.getRoomAtWorldPoint(x, y);

        Point2D.Double bestSpot = null;
        double bestScore = -Double.MAX_VALUE;

        int searchRadius = 8; // Quét bán kính 8 ô gạch (~384px)
        for (int dc = -searchRadius; dc <= searchRadius; dc++) {
            for (int dr = -searchRadius; dr <= searchRadius; dr++) {
                int col = centerCol + dc;
                int row = centerRow + dr;
                if (!map.isValidGrid(col, row)) continue;

                Tile tile = map.getTile(col, row);
                if (tile == null || tile.isSolid()) continue;

                double spotX = (col + 0.5) * GameConfig.TILE_SIZE;
                double spotY = (row + 0.5) * GameConfig.TILE_SIZE;

                // 1. Phải đi lại được an toàn
                if (!map.isAreaWalkable(spotX - half, spotY - half, size, size)) continue;

                // 2. Phải cách người chơi ít nhất 190px
                double distToPlayer = Math.hypot(spotX - playerCenterX, spotY - playerCenterY);
                if (distToPlayer < 190.0) continue;

                // 3. Phải bị che chắn đường đạn từ người chơi
                if (!isLineOfSightBlocked(spotX, spotY, playerCenterX, playerCenterY, map)) continue;

                // 4. BỘ LỌC CHỐNG BẪY GÓC TƯỜNG & NGÕ CỤT: Tuyệt đối không chọn góc chết / sau thùng gỗ sát tường làm nơi nấp
                if (isDeadEndOrPocket(spotX, spotY, map)) continue;

                // 5. Nếu ở trong phòng, tránh các ô nằm quá sát bờ tường biên của phòng
                if (currentRoom != null) {
                    double wallLeft = (currentRoom.getGridX() + 1) * GameConfig.TILE_SIZE;
                    double wallRight = (currentRoom.getGridX() + currentRoom.getWidthInTiles() - 1) * GameConfig.TILE_SIZE;
                    double wallTop = (currentRoom.getGridY() + 1) * GameConfig.TILE_SIZE;
                    double wallBottom = (currentRoom.getGridY() + currentRoom.getHeightInTiles() - 1) * GameConfig.TILE_SIZE;

                    double distToWall = Math.min(Math.min(spotX - wallLeft, wallRight - spotX),
                                                 Math.min(spotY - wallTop, wallBottom - spotY));
                    if (distToWall < 36.0) {
                        continue; // Quá sát tường biên phòng, dễ bị kẹt góc
                    }
                }

                // 6. Chấm điểm Cover Spot: Ưu tiên độ thông thoáng (openness), xa người chơi, gần Medic
                double openness = calculateOpennessScore(spotX, spotY, map);
                double distToMedic = Math.hypot(spotX - x, spotY - y);

                double score = openness * 1.2 + distToPlayer * 1.5 - distToMedic * 2.0;
                if (score > bestScore) {
                    bestScore = score;
                    bestSpot = new Point2D.Double(spotX, spotY);
                }
            }
        }
        return bestSpot;
    }

    /**
     * Thuật toán bứt phá khẩn cấp (Emergency Breakout / Slip-Through):
     * Khi bị người chơi dồn vào góc tường hoặc ngõ cụt (phía sau bị chặn, không thể lùi xa thêm),
     * Medic quét toàn bộ 360 độ (16 hướng) để tìm lối thoát hiểm tốt nhất dẫn ra trung tâm phòng
     * hoặc không gian mở, sẵn sàng lướt tạt qua sườn của người chơi để thoát kẹt.
     */
    private Point2D.Double findEmergencyBreakoutStep(double dt, DungeonMap map, double playerCenterX, double playerCenterY) {
        if (map == null) return null;
        double half = size / 2.0;

        Room room = map.getRoomAtWorldPoint(x, y);
        double roomCX = (room != null) ? room.getCenterWorldX() : playerCenterX;
        double roomCY = (room != null) ? room.getCenterWorldY() : playerCenterY;

        double dirToPlayer = Math.atan2(playerCenterY - y, playerCenterX - x);

        int numRays = 16;
        double bestScore = -Double.MAX_VALUE;
        Point2D.Double bestStep = null;

        for (int i = 0; i < numRays; i++) {
            double angle = (2.0 * Math.PI * i) / numRays;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;

            double stepX = vx * dt;
            double stepY = vy * dt;

            // 1. Kiểm tra bước đi ngay lập tức
            boolean okX = map.isAreaWalkable(x + stepX - half, y - half, size, size);
            boolean okY = map.isAreaWalkable(x - half, y + stepY - half, size, size);
            if (!okX && !okY) continue;

            double probeX = x + (okX ? stepX : 0);
            double probeY = y + (okY ? stepY : 0);

            // Kiểm tra thêm 2 bước tiếp theo theo hướng này xem có bị cụt ngay không
            double probe2X = x + (okX ? stepX * 2.2 : 0);
            double probe2Y = y + (okY ? stepY * 2.2 : 0);
            boolean deepOk = map.isAreaWalkable(probe2X - half, probe2Y - half, size, size);

            // 2. Không được đâm thẳng vào người chơi (phải lệch ít nhất 22 độ so với tâm người chơi)
            double angleDiff = Math.abs(normalizeAngle(angle - dirToPlayer));
            if (angleDiff < Math.toRadians(22.0)) {
                continue; // Quá gần miệng súng người chơi
            }

            // 3. Chấm điểm lối thoát:
            // a. Dẫn về trung tâm phòng (hướng ra không gian mở)
            double distToCenter = Math.hypot(probeX - roomCX, probeY - roomCY);
            double centerScore = -distToCenter * 2.2;

            // b. Độ mở không gian (openness)
            double openness = calculateOpennessScore(probeX, probeY, map);

            // c. Đủ xa người chơi hoặc tạt sườn góc rộng
            double distToPlayer = Math.hypot(probeX - playerCenterX, probeY - playerCenterY);

            double score = centerScore + (openness * 0.8) + (distToPlayer * 1.1) + (deepOk ? 90.0 : 0.0);
            if (score > bestScore) {
                bestScore = score;
                bestStep = new Point2D.Double(okX ? stepX : 0, okY ? stepY : 0);
            }
        }
        return bestStep;
    }

    /**
     * Di chuyển thông minh với cơ chế cảm biến vật cản (Obstacle Awareness) và né góc chết (Corner Escape):
     * Khi hướng di chuyển mong muốn bị tường/góc chặn, Medic tự động quét đa hướng để bẻ lái men theo bờ tường (Wall Slide),
     * hoặc kích hoạt thuật toán bứt phá khẩn cấp (Breakout) để lách qua sườn người chơi thoát ra không gian mở.
     */
    private void moveWithSmartCollision(double moveVx, double moveVy, double dt, DungeonMap map, double playerCenterX, double playerCenterY) {
        if (Math.abs(moveVx) < 0.001 && Math.abs(moveVy) < 0.001) {
            return;
        }

        double half = size / 2.0;
        double stepX = moveVx * dt;
        double stepY = moveVy * dt;

        // 1. Thử nghiệm di chuyển trực tiếp trên cả 2 trục
        boolean canMoveX = (map == null) || map.isAreaWalkable(x + stepX - half, y - half, size, size);
        boolean canMoveY = (map == null) || map.isAreaWalkable(x - half, y + stepY - half, size, size);

        if (canMoveX && canMoveY) {
            x += stepX;
            y += stepY;
            return;
        }

        // 2. Nếu một trong 2 trục bị tường chặn (hoặc đang bị ép sát góc tường):
        // Nếu bị chặn cả 2 trục hoặc đang ở trong ngõ cụt/góc tường -> Kích hoạt Emergency Breakout trước!
        if (!canMoveX && !canMoveY || isDeadEndOrPocket(x, y, map)) {
            Point2D.Double breakout = findEmergencyBreakoutStep(dt, map, playerCenterX, playerCenterY);
            if (breakout != null && (Math.abs(breakout.x) > 0.0001 || Math.abs(breakout.y) > 0.0001)) {
                x += breakout.x;
                y += breakout.y;
                facingAngle = Math.atan2(breakout.y, breakout.x);
                return;
            }
        }

        if (canMoveX) {
            x += stepX;
        }
        if (canMoveY) {
            y += stepY;
        }

        // 3. Nếu một trong 2 trục bị cản, kích hoạt thuật toán dò góc bẻ lái thông minh (Multi-ray Steering Flank)
        if (!canMoveX || !canMoveY) {
            double currentSpeed = Math.hypot(moveVx, moveVy);
            if (currentSpeed < 1.0) return;

            Room room = (map != null) ? map.getRoomAtWorldPoint(x, y) : null;
            double roomCX = (room != null) ? room.getCenterWorldX() : playerCenterX;
            double roomCY = (room != null) ? room.getCenterWorldY() : playerCenterY;

            double baseAngle = Math.atan2(moveVy, moveVx);
            // Thử các góc bẻ lái lệch dần sang 2 bên
            double[] testOffsets = {
                    Math.toRadians(35), Math.toRadians(-35),
                    Math.toRadians(65), Math.toRadians(-65),
                    Math.toRadians(90), Math.toRadians(-90),
                    Math.toRadians(115), Math.toRadians(-115),
                    Math.toRadians(140), Math.toRadians(-140),
                    Math.toRadians(180)
            };

            double bestScore = -Double.MAX_VALUE;
            double bestSteerStepX = 0.0;
            double bestSteerStepY = 0.0;

            for (double offset : testOffsets) {
                double testAngle = baseAngle + offset;
                double testVx = Math.cos(testAngle) * currentSpeed;
                double testVy = Math.sin(testAngle) * currentSpeed;

                double testStepX = testVx * dt;
                double testStepY = testVy * dt;

                boolean okX = (map == null) || map.isAreaWalkable(x + testStepX - half, y - half, size, size);
                boolean okY = (map == null) || map.isAreaWalkable(x - half, y + testStepY - half, size, size);

                if (okX || okY) {
                    double probeX = x + (okX ? testStepX : 0);
                    double probeY = y + (okY ? testStepY : 0);
                    double newDistToPlayer = Math.hypot(probeX - playerCenterX, probeY - playerCenterY);
                    double newDistToCenter = Math.hypot(probeX - roomCX, probeY - roomCY);
                    double openness = calculateOpennessScore(probeX, probeY, map);

                    // Điểm số: ưu tiên hướng về tâm phòng và không gian mở, tránh góc chết
                    double score = (openness * 0.6) - (newDistToCenter * 1.5) + (newDistToPlayer * 1.0);
                    if (okX && okY) {
                        score += 50.0; // Thoáng cả 2 trục được ưu tiên cao
                    }

                    if (score > bestScore) {
                        bestScore = score;
                        bestSteerStepX = okX ? testStepX : 0;
                        bestSteerStepY = okY ? testStepY : 0;
                    }
                }
            }

            if (bestScore > -Double.MAX_VALUE && (bestSteerStepX != 0 || bestSteerStepY != 0)) {
                x += bestSteerStepX;
                y += bestSteerStepY;
                facingAngle = Math.atan2(bestSteerStepY, bestSteerStepX);
            }
        }
    }

    @Override
    public void updateAI(double deltaTime, double playerCenterX, double playerCenterY, double playerSize,
                         DungeonMap dungeonMap, ProjectileManager projectileManager, PlayingState playingState) {
        if (!alive) return;

        update(deltaTime);
        updateKnockback(deltaTime, dungeonMap);

        if (pulseAnimTimer > 0) {
            pulseAnimTimer -= deltaTime;
            if (pulseAnimTimer < 0) pulseAnimTimer = 0.0;
        }

        strafeTimer += deltaTime;
        if (strafeTimer >= 1.8) {
            strafeTimer = 0.0;
            strafeDirection = -strafeDirection; // Đổi hướng lượn ngang định kỳ
        }

        if (isSpawning()) {
            return;
        }

        // --- 1. SCAN FOR WOUNDED ALLIED SQUADMATES ---
        // WHY: The medic's primary objective is squad sustainment. It continually identifies
        // the most severely wounded ally (lowest HP percentage) to close distance and provide triage.
        TargetDummy bestWoundedAlly = null;
        double lowestHpRatio = 1.0;

        List<TargetDummy> targets = (playingState != null) ? playingState.getCombatTargets() : null;
        if (targets != null) {
            for (TargetDummy ally : targets) {
                if (ally != this && ally.isAlive() && !ally.isSpawning()) {
                    if (ally.getCurrentHealth() < ally.getMaxHealth()) {
                        double ratio = (double) ally.getCurrentHealth() / ally.getMaxHealth();
                        if (ratio < lowestHpRatio) {
                            lowestHpRatio = ratio;
                            bestWoundedAlly = ally;
                        }
                    }
                }
            }
        }
        this.healingTarget = bestWoundedAlly;

        // --- 2. PERIODIC HEALING AURA PULSE ---
        // Restores health to all allied units within healRadius centered on this medic.
        // BUFF: Bản thân Medic cũng tự hồi 4 HP (Nano Self-Repair) để tăng khả năng sinh tồn.
        healTimer -= deltaTime;
        if (healTimer <= 0.0) {
            healTimer = healInterval;
            pulseAnimTimer = 0.35; // Trigger visual emerald pulse ring

            boolean healedAnyAlly = false;
            if (targets != null) {
                for (TargetDummy ally : targets) {
                    if (ally != this && ally.isAlive() && !ally.isSpawning()) {
                        double distToAlly = Math.hypot(ally.getCenterX() - x, ally.getCenterY() - y);
                        if (distToAlly <= healRadius && ally.getCurrentHealth() < ally.getMaxHealth()) {
                            ally.heal(healAmount);
                            healedAnyAlly = true;
                        }
                    }
                }
            }

            // Bản thân tự phục hồi thương tích nhẹ
            if (currentHealth < maxHealth) {
                heal(4);
                healedAnyAlly = true;
            }

            if (healedAnyAlly) {
                GameSoundManager.play(SoundEffect.HEALING, 0.55f);
            }
        }

        // --- 3. AUTONOMOUS TRIAGE LOCOMOTION, COVER SEEKING & SMART EVASION ---
        double dxPlayer = playerCenterX - x;
        double dyPlayer = playerCenterY - y;
        double distToPlayer = Math.hypot(dxPlayer, dyPlayer);

        double moveVx = 0.0;
        double moveVy = 0.0;

        // Kiểm tra xem vị trí hiện tại của Medic có được che chắn bởi vật cản khỏi người chơi hay không
        this.inCover = isLineOfSightBlocked(x, y, playerCenterX, playerCenterY, dungeonMap);
        this.seekingCover = false;

        if (distToPlayer < fleeDistance) {
            // PRIORITY 1: PLAYER EVASION (Né tránh chiến thuật thông minh, lướt sườn & chống dồn vào góc tường)
            Room room = (dungeonMap != null) ? dungeonMap.getRoomAtWorldPoint(x, y) : null;

            // 1. Vector lùi khỏi người chơi
            double fleeX = -(dxPlayer / (distToPlayer + 0.0001));
            double fleeY = -(dyPlayer / (distToPlayer + 0.0001));

            // 2. Vector hướng về trung tâm phòng (chống lùi dồn vào tường/góc chết)
            double centerDirX = 0.0;
            double centerDirY = 0.0;
            double centerWeight = 0.0;

            if (room != null) {
                double roomCX = room.getCenterWorldX();
                double roomCY = room.getCenterWorldY();
                double dxC = roomCX - x;
                double dyC = roomCY - y;
                double distC = Math.hypot(dxC, dyC);
                if (distC > 10.0) {
                    centerDirX = dxC / distC;
                    centerDirY = dyC / distC;
                }

                double wallLeft = (room.getGridX() + 1) * GameConfig.TILE_SIZE;
                double wallRight = (room.getGridX() + room.getWidthInTiles() - 1) * GameConfig.TILE_SIZE;
                double wallTop = (room.getGridY() + 1) * GameConfig.TILE_SIZE;
                double wallBottom = (room.getGridY() + room.getHeightInTiles() - 1) * GameConfig.TILE_SIZE;
                double minWallDist = Math.min(Math.min(x - wallLeft, wallRight - x),
                                              Math.min(y - wallTop, wallBottom - y));

                // Càng gần tường biên (< 96px ~ 2 tile), lực kéo về trung tâm phòng càng mạnh
                if (minWallDist < 96.0) {
                    centerWeight = Math.min(1.0, (96.0 - Math.max(0, minWallDist)) / 72.0);
                }
            }

            // 3. Vector lướt tiếp tuyến (Tangential Flank) vuông góc với người chơi
            double tan1X = -dyPlayer / (distToPlayer + 0.0001);
            double tan1Y = dxPlayer / (distToPlayer + 0.0001);
            double tan2X = dyPlayer / (distToPlayer + 0.0001);
            double tan2Y = -dxPlayer / (distToPlayer + 0.0001);

            // Chọn hướng tiếp tuyến dẫn về phía trung tâm phòng tốt hơn
            double dot1 = tan1X * centerDirX + tan1Y * centerDirY;
            double dot2 = tan2X * centerDirX + tan2Y * centerDirY;
            double bestTanX = (dot1 >= dot2) ? tan1X : tan2X;
            double bestTanY = (dot1 >= dot2) ? tan1Y : tan2Y;

            // 4. Hòa trộn các vector:
            // Khi ở xa tường: lùi kết hợp lướt ngang sườn
            // Khi ở gần tường: giảm lùi thẳng, tăng mạnh lướt tiếp tuyến và kéo về tâm phòng
            double blendedVx = fleeX * (1.0 - centerWeight * 0.75) + bestTanX * (0.65 + centerWeight * 0.35) + centerDirX * (centerWeight * 0.85);
            double blendedVy = fleeY * (1.0 - centerWeight * 0.75) + bestTanY * (0.65 + centerWeight * 0.35) + centerDirY * (centerWeight * 0.85);

            double blendedMag = Math.hypot(blendedVx, blendedVy);
            if (blendedMag > 0.001) {
                moveVx = (blendedVx / blendedMag) * speed;
                moveVy = (blendedVy / blendedMag) * speed;
                facingAngle = Math.atan2(moveVy, moveVx);
            }
        } else if (healingTarget != null) {
            // PRIORITY 2: APPROACH WOUNDED SQUADMATE
            double dxAlly = healingTarget.getCenterX() - x;
            double dyAlly = healingTarget.getCenterY() - y;
            double distToAlly = Math.hypot(dxAlly, dyAlly);

            if (distToAlly > healRadius * 0.60) {
                // Tiếp cận đồng đội bị thương để đưa họ vào vòng hào quang hồi máu
                moveVx = (dxAlly / distToAlly) * speed;
                moveVy = (dyAlly / distToAlly) * speed;
                facingAngle = Math.atan2(moveVy, moveVx);
            } else {
                // Đã ở cự ly thoải mái: di chuyển nhẹ quanh đồng đội tránh đứng yên một chỗ
                double perpX = -dyAlly / (distToAlly + 0.001);
                double perpY = dxAlly / (distToAlly + 0.001);
                moveVx = perpX * (speed * 0.35) * strafeDirection;
                moveVy = perpY * (speed * 0.35) * strafeDirection;
                facingAngle = Math.atan2(dyAlly, dxAlly);
            }
        } else {
            // PRIORITY 3: KHÔNG CÓ ĐỒNG MINH BỊ THƯƠNG -> TÌM VẬT CẢN ĐỂ NẤP TRÁNH ĐẠN NGƯỜI CHƠI
            if (!inCover) {
                // Đang bị lộ mặt trước người chơi: Quét tìm Cover Spot gần nhất để lùi về sau vật cản
                Point2D.Double coverSpot = findBestCoverLocation(playerCenterX, playerCenterY, dungeonMap);
                if (coverSpot != null) {
                    this.seekingCover = true;
                    double dxCover = coverSpot.x - x;
                    double dyCover = coverSpot.y - y;
                    double distCover = Math.hypot(dxCover, dyCover);

                    if (distCover > 14.0) {
                        moveVx = (dxCover / distCover) * speed;
                        moveVy = (dyCover / distCover) * speed;
                        facingAngle = Math.atan2(moveVy, moveVx);
                    } else {
                        // Đã tới vị trí nấp: Dừng lại và quay mặt cảnh giới người chơi
                        facingAngle = Math.atan2(dyPlayer, dxPlayer);
                    }
                } else {
                    // Không tìm thấy cover nào (phòng trống): Cơ động lùi xa kết hợp lượn ngang (Kiting), không bao giờ đứng im
                    double retreatSpeed = speed * 0.85;
                    double normX = -(dxPlayer / (distToPlayer + 0.001));
                    double normY = -(dyPlayer / (distToPlayer + 0.001));
                    double strafeX = -normY * strafeDirection * 0.65;
                    double strafeY = normX * strafeDirection * 0.65;

                    moveVx = (normX + strafeX) * retreatSpeed;
                    moveVy = (normY + strafeY) * retreatSpeed;
                    facingAngle = Math.atan2(dyPlayer, dxPlayer);
                }
            } else {
                // Đã đứng an toàn sau vật cản: Giữ vị trí ẩn nấp (Hold Cover)
                if (distToPlayer < 240.0 && distToPlayer > 0.001) {
                    // Người chơi đang áp sát mép cover: nhích lùi thêm một chút giữ khoảng cách an toàn
                    moveVx = -(dxPlayer / distToPlayer) * (speed * 0.50);
                    moveVy = -(dyPlayer / distToPlayer) * (speed * 0.50);
                } else {
                    // Di chuyển nhẹ giữ chân, không bao giờ đứng đờ đẫn
                    double perpX = -dyPlayer / (distToPlayer + 0.001);
                    double perpY = dxPlayer / (distToPlayer + 0.001);
                    moveVx = perpX * (speed * 0.25) * strafeDirection;
                    moveVy = perpY * (speed * 0.25) * strafeDirection;
                }
                facingAngle = Math.atan2(dyPlayer, dxPlayer);
            }
        }

        // Thực thi di chuyển thông minh với bộ cảm biến vật cản & né góc tường
        moveWithSmartCollision(moveVx, moveVy, deltaTime, dungeonMap, playerCenterX, playerCenterY);
    }

    public double getHealRadius() {
        return healRadius;
    }

    public int getHealAmount() {
        return healAmount;
    }

    public double getHealInterval() {
        return healInterval;
    }

    public double getSpeed() {
        return speed;
    }

    public double getFleeDistance() {
        return fleeDistance;
    }

    public TargetDummy getHealingTarget() {
        return healingTarget;
    }

    public boolean isInCover() {
        return inCover;
    }

    public boolean isSeekingCover() {
        return seekingCover;
    }

    /**
     * Renders the restorative healing aura perimeter onto the dungeon floor.
     * <p>WHY: Rendered on the ground layer UNDER all entities so the healing field
     * does not obscure player, enemies, or obstacles while clearly demarcating hostile recovery zone.
     * Uses red / crimson styling to clearly distinguish as a hostile enemy threat.
     */
    public void renderAura(Graphics2D g, Camera camera) {
        if (!alive || isSpawning()) return;

        int screenCX = camera.worldToScreenX(x);
        int screenCY = camera.worldToScreenY(y);
        int hr = (int) healRadius;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        // Translucent hostile crimson ground fill
        g2.setColor(new Color(220, 35, 45, 24));
        g2.fillOval(screenCX - hr, screenCY - hr, hr * 2, hr * 2);

        // Dashed rotating perimeter line in warning red
        float[] dashPattern = {6.0f, 6.0f};
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, (float) (animTimer * 12.0)));
        g2.setColor(new Color(255, 60, 60, 135));
        g2.drawOval(screenCX - hr, screenCY - hr, hr * 2, hr * 2);

        // 4 Cardinal medical cross badges '+' on the perimeter ring in crimson
        g2.setStroke(new BasicStroke(1.6f));
        g2.setColor(new Color(255, 75, 75, 185));
        int crossArm = 4;
        // North
        g2.drawLine(screenCX, screenCY - hr - crossArm, screenCX, screenCY - hr + crossArm);
        g2.drawLine(screenCX - crossArm, screenCY - hr, screenCX + crossArm, screenCY - hr);
        // South
        g2.drawLine(screenCX, screenCY + hr - crossArm, screenCX, screenCY + hr + crossArm);
        g2.drawLine(screenCX - crossArm, screenCY + hr, screenCX + crossArm, screenCY + hr);
        // West
        g2.drawLine(screenCX - hr - crossArm, screenCY, screenCX - hr + crossArm, screenCY);
        g2.drawLine(screenCX - hr, screenCY - crossArm, screenCX - hr, screenCY + crossArm);
        // East
        g2.drawLine(screenCX + hr - crossArm, screenCY, screenCX + hr + crossArm, screenCY);
        g2.drawLine(screenCX + hr, screenCY - crossArm, screenCX + hr, screenCY + crossArm);

        // Expanding pulse wave when a healing cycle executes (crimson ripple)
        if (pulseAnimTimer > 0) {
            double pulseProgress = 1.0 - (pulseAnimTimer / 0.35); // 0.0 -> 1.0
            int pulseRadius = (int) (hr * pulseProgress);
            int pulseAlpha = (int) (180 * (1.0 - pulseProgress));
            g2.setStroke(new BasicStroke((float) (3.2 * (1.0 - pulseProgress * 0.5))));
            g2.setColor(new Color(255, 50, 50, pulseAlpha));
            g2.drawOval(screenCX - pulseRadius, screenCY - pulseRadius, pulseRadius * 2, pulseRadius * 2);
        }

        g2.dispose();
    }

    @Override
    public void render(Graphics2D g, Camera camera) {
        if (!alive) return;
        if (isSpawning()) {
            renderSpawnTelegraph(g, camera);
            return;
        }

        int screenCX = camera.worldToScreenX(x);
        int screenCY = camera.worldToScreenY(y);
        int half = (int) (size / 2.0);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        // Floor shadow
        g2.setColor(new Color(0, 0, 0, 95));
        g2.fillOval(screenCX - half, screenCY + 2, (int) size, 12);

        // 2. Thử nạp và hiển thị Sprite Quân Y (Combat Field Medic) xoay theo hướng di chuyển
        BufferedImage sprite = GameSpriteManager.getEnemyMedicSprite();
        if (sprite != null) {
            Graphics2D gSprite = (Graphics2D) g2.create();
            if (critFlashTimer > 0) {
                gSprite.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            } else if (hitFlashTimer > 0) {
                gSprite.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
            }
            int spriteDrawSize = (int) Math.round(size * 1.35);
            GameSpriteManager.renderRotatedSprite(gSprite, sprite, screenCX, screenCY, facingAngle, spriteDrawSize, spriteDrawSize);
            gSprite.dispose();
        } else {
            // --- Dự phòng Vector Fallback ---
            // Armor body - Dark Military Gray with Hostile Red Trims
            int bx = screenCX - half;
            int by = screenCY - half;
            int bSize = (int) size;

            if (critFlashTimer > 0) {
                g2.setColor(new Color(255, 45, 45));
                g2.fillRoundRect(bx, by, bSize, bSize, 8, 8);
            } else if (hitFlashTimer > 0) {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(bx, by, bSize, bSize, 8, 8);
            } else {
                // Dark military gray chassis (phù hợp màu mặc định của kẻ địch)
                g2.setColor(new Color(68, 74, 84));
                g2.fillRoundRect(bx, by, bSize, bSize, 8, 8);

                // Sub-armor carbon plating
                g2.setColor(new Color(48, 53, 62));
                g2.fillRoundRect(bx + 4, by + 4, bSize - 8, bSize - 8, 5, 5);

                // Rear twin thruster nubs
                g2.setColor(new Color(55, 60, 70));
                g2.fillRoundRect(bx + 4, by - 4, bSize - 8, 5, 3, 3);
                g2.setColor(new Color(220, 40, 45));
                g2.fillRect(bx + bSize / 2 - 2, by - 4, 4, 5);

                // Crimson hostile hazard trims
                g2.setColor(new Color(195, 45, 50));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(bx, by, bSize, bSize, 8, 8);
            }

            // Hostile red medical cross '+' emblem at center chest
            g2.setColor(new Color(230, 40, 40));
            g2.fillRect(screenCX - 2, screenCY - 6, 4, 12);
            g2.fillRect(screenCX - 6, screenCY - 2, 12, 4);

            // Glowing center core of the cross
            g2.setColor(new Color(255, 140, 140));
            g2.fillRect(screenCX - 2, screenCY - 2, 4, 4);

            // Glowing red optic sensor eye
            g2.setColor(new Color(255, 60, 60));
            g2.fillOval(screenCX - 3, screenCY - 3, 6, 6);
        }

        // Overhead health bar - luôn luôn màu đỏ
        int barW = 34;
        int barH = 5;
        int barX = screenCX - barW / 2;
        int barY = screenCY - half - 10;

        g2.setColor(new Color(30, 30, 40));
        g2.fillRect(barX, barY, barW, barH);
        float hpPercent = (float) currentHealth / maxHealth;
        g2.setColor(new Color(230, 45, 45)); // Luôn luôn là màu đỏ
        g2.fillRect(barX, barY, (int) (barW * hpPercent), barH);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(barX, barY, barW, barH);

        g2.dispose();
    }
}
