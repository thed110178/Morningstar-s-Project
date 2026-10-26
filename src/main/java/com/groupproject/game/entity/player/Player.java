package com.groupproject.game.entity.player;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import com.groupproject.game.core.GameSpriteManager;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.entity.weapon.Weapon;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Thực thể Chiến binh Đặc nhiệm (Operative Player Entity).
 *
 * <p>WHY: Đóng gói toàn bộ trạng thái không gian (tọa độ, kích thước hitbox, góc ngắm),
 * cơ chế di chuyển kết hợp trượt va chạm tường/cửa khóa 2 trục độc lập,
 * hệ thống sinh lực (HP, i-frames khi dính đòn) và các hàm hiển thị đồ họa dã chiến
 * của chiến binh người chơi. Tách biệt hoàn toàn khỏi God Class PlayingState.
 */
public class Player {

    // Trạng thái không gian (Spatial properties)
    private double x;
    private double y;
    private double size = 28.0; // Hitbox width and height in pixels
    private double speed = GameConfig.PLAYER_DEFAULT_SPEED; // 210.0 px/s
    private double aimAngle = 0.0; // Góc ngắm radian: atan2(dy, dx)

    // Hệ thống sinh lực & phòng thủ (Health & Defense)
    private int maxHealth = 100;
    private int currentHealth = 100;
    private double invulnerableTimer = 0.0; // Thời gian bất tử (i-frames: 0.8s) khi dính sát thương
    private boolean isDead = false;

    // Hệ thống né đòn & i-frames (Combat Dash / Dodge Roll)
    public static final double DODGE_DURATION = 0.22; // Thời lượng lướt né (giây)
    public static final double DODGE_SPEED = 560.0; // Vận tốc lướt né (px/s) (~2.6x tốc độ chạy 210 px/s)
    public static final double DODGE_COOLDOWN = 0.80; // Thời gian hồi chiêu né (giây)
    private static final double GHOST_INTERVAL = 0.04; // Chu kỳ sinh tàn ảnh bóng ma (giây)
    private static final double MAX_PHASE_EXTENSION = 0.20; // Thời gian tối đa kéo dài cú lướt né để thoát khỏi vật cản tạm thời (giây)

    private boolean dodging = false;
    private double dodgeTimer = 0.0;
    private double dodgeCooldownTimer = 0.0;
    private double dodgeDirX = 0.0;
    private double dodgeDirY = 0.0;
    private double ghostSpawnTimer = 0.0;
    private double phaseExtensionTimer = 0.0;
    private final List<DodgeGhost> dodgeGhosts = new CopyOnWriteArrayList<>();
    private final com.groupproject.game.entity.buff.PlayerBuffManager buffManager = new com.groupproject.game.entity.buff.PlayerBuffManager();

    // --- Hoạt cảnh tử trận: Đầu lâu trượt ma sát trên sàn (Death Slide Sequence) ---
    public static final double DEATH_SLIDE_DURATION = 2.0; // Thời lượng quan sát đầu lâu trượt (2.0 giây)
    private double deathTimer = 0.0;
    private double deathSlideVx = 0.0;
    private double deathSlideVy = 0.0;
    private double deathRotation = 0.0;
    private double skidSpawnTimer = 0.0;
    private double lastMoveDirX = 0.0;
    private double lastMoveDirY = 0.0;
    private final List<SkidParticle> deathSkidParticles = new CopyOnWriteArrayList<>();

    /**
     * Hạt khói bụi / vệt ma sát cọ xát trên mặt đất khi đầu lâu trượt.
     */
    public static class SkidParticle {
        public double x, y;
        public double radius;
        public double life;
        public double maxLife;
        public Color color;

        public SkidParticle(double x, double y, double radius, double maxLife, Color color) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.life = maxLife;
            this.maxLife = maxLife;
            this.color = color;
        }

        public boolean update(double deltaTime) {
            life -= deltaTime;
            return life <= 0;
        }

        public void render(Graphics2D g, Camera camera) {
            if (life <= 0) return;
            int sx = camera.worldToScreenX(x);
            int sy = camera.worldToScreenY(y);
            float ratio = (float) Math.max(0.0, life / maxLife);
            int alpha = (int) (color.getAlpha() * ratio);
            if (alpha <= 0) return;
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            int r = (int) Math.round(radius);
            g.fillOval(sx - r, sy - r, r * 2, r * 2);
        }
    }

    /**
     * Khởi tạo chiến binh với tọa độ ban đầu.
     *
     * @param x Tọa độ góc trên bên trái X.
     * @param y Tọa độ góc trên bên trái Y.
     */
    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.maxHealth = 100;
        this.currentHealth = 100;
    }

    /**
     * Kiểm tra xem hitbox của chiến binh có đang giao cắt/chồng lấn với ít nhất một ô vật cản tạm thời
     * có thể phá hủy (DESTRUCTIBLE_COVER) hay không.
     *
     * <p>WHY: Dùng cho cơ chế phòng ngừa khi hết thời gian lướt né (i-frames) mà người chơi chưa kịp
     * thoát ra khỏi hàng thùng gỗ dã chiến, ngăn chặn hoàn toàn việc bị kẹt (stuck) hoặc mất i-frame
     * giữa chừng trong vật cản.
     */
    public boolean isOverlappingDestructibleCover(DungeonMap dungeonMap) {
        if (dungeonMap == null) return false;
        double curSize = getSize();
        return !dungeonMap.isAreaWalkable(x, y, curSize, curSize)
                && dungeonMap.isAreaWalkableIgnoringDestructible(x, y, curSize, curSize);
    }

    /**
     * Cập nhật đếm ngược bộ đếm thời gian bất tử (i-frames), hồi chiêu né và tàn ảnh bóng ma.
     *
     * @param deltaTime Khoảng thời gian trôi qua giữa 2 khung hình (giây).
     */
    public void update(double deltaTime) {
        update(deltaTime, null);
    }

    /**
     * Cập nhật đếm ngược bộ đếm thời gian bất tử (i-frames), hồi chiêu né, tàn ảnh bóng ma
     * và cơ chế phòng ngừa an toàn khi lướt né xuyên qua vật cản tạm thời.
     *
     * @param deltaTime  Khoảng thời gian trôi qua giữa 2 khung hình (giây).
     * @param dungeonMap Bản đồ hầm ngục kiểm tra va chạm chướng ngại vật tạm thời.
     */
    public void update(double deltaTime, DungeonMap dungeonMap) {
        if (isDead) {
            updateDeathSlide(deltaTime, dungeonMap);
            return;
        }

        if (invulnerableTimer > 0) {
            invulnerableTimer -= deltaTime;
        }

        // Cập nhật hồi chiêu né đòn
        if (dodgeCooldownTimer > 0) {
            dodgeCooldownTimer = Math.max(0.0, dodgeCooldownTimer - deltaTime);
        }

        // Cập nhật trạng thái né và chu kỳ sinh tàn ảnh bóng ma
        if (dodging) {
            ghostSpawnTimer += deltaTime;
            if (ghostSpawnTimer >= GHOST_INTERVAL) {
                ghostSpawnTimer = 0.0;
                spawnGhost();
            }

            if (dodgeTimer > 0) {
                dodgeTimer -= deltaTime;
            }

            if (dodgeTimer <= 0) {
                // Kiểm tra xem có đang ở giữa vật cản tạm thời với buff Phase Dodge hay không
                boolean insideDestructible = (dungeonMap != null && buffManager.hasPhaseDodge())
                        && isOverlappingDestructibleCover(dungeonMap);

                if (insideDestructible) {
                    // PHÒNG NGỪA 1: Tiếp tục duy trì cú lướt và bất tử 100% (I-frame) để thoát ra ngoài
                    phaseExtensionTimer += deltaTime;
                    if (phaseExtensionTimer >= MAX_PHASE_EXTENSION) {
                        // PHÒNG NGỪA 2 (Tối hậu): Sau thời gian gia hạn tối đa vẫn chưa thoát ra (ví dụ kẹt góc tường),
                        // lập tức kích hoạt sóng xung kích nghiền nát vật cản tạm thời tại vị trí này thành ô FLOOR trống.
                        dungeonMap.damageDestructibleCoverInArea(x, y, getSize(), getSize(), 999);
                        dodging = false;
                        dodgeTimer = 0.0;
                        phaseExtensionTimer = 0.0;
                        invulnerableTimer = Math.max(invulnerableTimer, 0.30); // Cấp đệm an toàn I-frame 0.3s
                    }
                } else {
                    // Đã ra khỏi vật cản an toàn hoặc không có buff Phase Dodge
                    if (phaseExtensionTimer > 0) {
                        // Vừa thoát ra khỏi vật cản tạm thời nhờ pha gia hạn: cấp đệm I-frame an toàn 0.25s
                        invulnerableTimer = Math.max(invulnerableTimer, 0.25);
                    }
                    dodging = false;
                    dodgeTimer = 0.0;
                    phaseExtensionTimer = 0.0;
                }
            }
        } else {
            phaseExtensionTimer = 0.0;
        }

        // Cập nhật và dọn dẹp các tàn ảnh bóng ma đã hết hạn
        dodgeGhosts.removeIf(ghost -> ghost.update(deltaTime));
    }

    /**
     * Xử lý di chuyển theo phím bấm WASD với cơ chế trượt va chạm địa hình 2 trục độc lập.
     * Khi đang trong trạng thái né (dodging), nhân vật lướt theo hướng né với vận tốc xung kích DODGE_SPEED.
     * Khi di chuyển bình thường, chuẩn hóa vector chéo với hệ số 1/sqrt(2) để đảm bảo tốc độ đồng đều.
     *
     * @param up         Cờ bấm W (tiến lên).
     * @param down       Cờ bấm S (lùi xuống).
     * @param left       Cờ bấm A (sang trái).
     * @param right      Cờ bấm D (sang phải).
     * @param deltaTime  Thời gian delta.
     * @param dungeonMap Bản đồ hầm ngục kiểm tra va chạm tường và cửa khóa.
     */
    public void handleMovement(boolean up, boolean down, boolean left, boolean right, double deltaTime, DungeonMap dungeonMap) {
        if (isDead) return;

        double moveStep;
        double moveX = 0;
        double moveY = 0;

        if (dodging) {
            // Khi đang né: Lướt theo hướng né định sẵn với vận tốc xung kích DODGE_SPEED
            moveStep = DODGE_SPEED * deltaTime;
            moveX = dodgeDirX * moveStep;
            moveY = dodgeDirY * moveStep;
            lastMoveDirX = dodgeDirX;
            lastMoveDirY = dodgeDirY;
        } else {
            double effectiveSpeed = speed * buffManager.getSpeedMultiplier();
            moveStep = effectiveSpeed * deltaTime;
            if (up) moveY -= moveStep;
            if (down) moveY += moveStep;
            if (left) moveX -= moveStep;
            if (right) moveX += moveStep;

            if (moveX != 0 || moveY != 0) {
                double len = Math.hypot(moveX, moveY);
                lastMoveDirX = moveX / len;
                lastMoveDirY = moveY / len;
            }

            // Chuẩn hóa di chuyển chéo (Diagonal movement normalization)
            if (moveX != 0 && moveY != 0) {
                double invSqrt = 1.0 / Math.sqrt(2.0);
                moveX *= invSqrt;
                moveY *= invSqrt;
            }
        }

        // Trượt va chạm 2 trục độc lập (Decoupled axis wall sliding)
        // Nếu có buff Né xuyên vật cản (PHASE_DODGE) và đang lướt né:
        // CHỈ cho phép đi xuyên qua vật cản tạm thời (DESTRUCTIBLE_COVER),
        // vẫn bị cản và trượt bởi tường kiên cố (WALL, Cột COVER vĩnh viễn, Cửa khóa DOOR_CLOSED, Vực VOID).
        double curSize = getSize();
        boolean phaseDodgeActive = dodging && buffManager.hasPhaseDodge();

        double prevX = x;
        double prevY = y;

        if (moveX != 0) {
            double nextX = x + moveX;
            boolean canMoveX;
            if (dungeonMap == null) {
                canMoveX = true;
            } else if (phaseDodgeActive) {
                canMoveX = dungeonMap.isAreaWalkableIgnoringDestructible(nextX, y, curSize, curSize);
            } else {
                canMoveX = dungeonMap.isAreaWalkable(nextX, y, curSize, curSize);
            }
            if (canMoveX) {
                x = nextX;
            }
        }
        if (moveY != 0) {
            double nextY = y + moveY;
            boolean canMoveY;
            if (dungeonMap == null) {
                canMoveY = true;
            } else if (phaseDodgeActive) {
                canMoveY = dungeonMap.isAreaWalkableIgnoringDestructible(x, nextY, curSize, curSize);
            } else {
                canMoveY = dungeonMap.isAreaWalkable(x, nextY, curSize, curSize);
            }
            if (canMoveY) {
                y = nextY;
            }
        }

        // PHÒNG NGỪA BỔ SUNG: Nếu đang trong pha gia hạn an toàn (dodgeTimer <= 0) bên trong vật cản tạm thời,
        // nhưng bị tường cứng/cửa khóa chặn lại khiến nhân vật không thể di chuyển tiếp (prevX == x && prevY == y):
        // Lập tức phá hủy vật cản tạm thời tại chỗ để giải phóng không gian ngay, không để người chơi bị đứng hình.
        if (dungeonMap != null && phaseDodgeActive && dodgeTimer <= 0 && isOverlappingDestructibleCover(dungeonMap)) {
            boolean blockedBySolid = (moveX != 0 && prevX == x) || (moveY != 0 && prevY == y);
            if (blockedBySolid) {
                dungeonMap.damageDestructibleCoverInArea(x, y, curSize, curSize, 999);
                dodging = false;
                dodgeTimer = 0.0;
                phaseExtensionTimer = 0.0;
                invulnerableTimer = Math.max(invulnerableTimer, 0.30);
            }
        }
    }

    /**
     * Kích hoạt kỹ năng né đòn (Combat Dash / Dodge Roll).
     *
     * <p>WHY: Cho phép đặc nhiệm tức thời lướt né tránh hỏa lực dữ dội hoặc quái cận chiến áp sát,
     * đồng thời kích hoạt trạng thái bất tử (i-frames) trong suốt thời gian lướt né.
     *
     * <p>Hướng né:
     * - Nếu đang nhấn các phím di chuyển WASD: Lướt theo hướng tổ hợp di chuyển.
     * - Nếu đang đứng yên: Lướt theo hướng tâm ngắm chuột (aimAngle).
     *
     * @param up    Phím W
     * @param down  Phím S
     * @param left  Phím A
     * @param right Phím D
     * @return true nếu kích hoạt né thành công, false nếu đang hồi chiêu hoặc đã chết / đang né.
     */
    public boolean triggerDodge(boolean up, boolean down, boolean left, boolean right) {
        if (isDead || dodging || dodgeCooldownTimer > 0) {
            return false;
        }

        double dirX = 0;
        double dirY = 0;
        if (up) dirY -= 1.0;
        if (down) dirY += 1.0;
        if (left) dirX -= 1.0;
        if (right) dirX += 1.0;

        if (dirX != 0 || dirY != 0) {
            double length = Math.sqrt(dirX * dirX + dirY * dirY);
            this.dodgeDirX = dirX / length;
            this.dodgeDirY = dirY / length;
        } else {
            // Đứng yên -> lướt theo hướng ngắm chuột (aimAngle)
            this.dodgeDirX = Math.cos(aimAngle);
            this.dodgeDirY = Math.sin(aimAngle);
        }

        this.dodging = true;
        this.dodgeTimer = DODGE_DURATION;
        this.dodgeCooldownTimer = DODGE_COOLDOWN / buffManager.getDodgeCooldownFactor();
        this.ghostSpawnTimer = 0.0;
        spawnGhost();
        return true;
    }

    private void spawnGhost() {
        dodgeGhosts.add(new DodgeGhost(x, y, aimAngle, 0.20));
    }

    /**
     * Gây sát thương lên chiến binh nếu không trong trạng thái bất tử (i-frames).
     * Miễn nhiễm hoàn toàn 100% sát thương khi đang lướt né đòn (dodging).
     *
     * @param damage Lượng sát thương nhận vào.
     * @return true nếu chiến binh thực sự nhận sát thương (phục vụ rung màn hình/cảnh báo), false nếu miễn nhiễm.
     */
    public boolean takeDamage(int damage) {
        if (isDead || isInvulnerable() || damage <= 0) {
            return false;
        }

        // 1. Kiểm tra Khiên Chặn Đòn 1 Lần (ONE_HIT_BARRIER)
        if (buffManager.checkAndConsumeBarrier()) {
            return false; // Chặn đứng 100% sát thương đòn này!
        }

        // 2. Áp dụng giảm % sát thương phòng thủ (DEFENSE - tối đa 30%)
        double defenseReduction = buffManager.getDamageReduction();
        int finalDamage = Math.max(1, (int) Math.round(damage * (1.0 - defenseReduction)));

        currentHealth = Math.max(0, currentHealth - finalDamage);
        invulnerableTimer = 0.8; // 0.8s thời gian bất tử i-frames sau khi trúng đòn

        if (currentHealth <= 0) {
            currentHealth = 0;
            initiateDeathSequence();
        }
        return true;
    }

    /**
     * Kiểm tra trạng thái bất tử (i-frames).
     * Bao gồm bất tử khi đang lướt né (dodging) hoặc đệm bất tử sau khi dính đòn (invulnerableTimer > 0).
     */
    public boolean isInvulnerable() {
        return dodging || invulnerableTimer > 0;
    }

    /**
     * Hồi máu cho chiến binh, giới hạn tối đa bởi maxHealth.
     *
     * @param amount Lượng máu hồi phục.
     */
    public void heal(int amount) {
        if (isDead || amount <= 0) return;
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }

    /**
     * Khởi tạo lại trạng thái chiến binh tại tọa độ mới.
     *
     * @param newX Tọa độ X mới.
     * @param newY Tọa độ Y mới.
     */
    public void reset(double newX, double newY) {
        this.x = newX;
        this.y = newY;
        this.maxHealth = 100;
        this.currentHealth = 100;
        this.invulnerableTimer = 0.0;
        this.isDead = false;
        this.dodging = false;
        this.dodgeTimer = 0.0;
        this.phaseExtensionTimer = 0.0;
        this.dodgeCooldownTimer = 0.0;
        this.dodgeGhosts.clear();
        this.buffManager.reset();
        this.deathTimer = 0.0;
        this.deathSlideVx = 0.0;
        this.deathSlideVy = 0.0;
        this.deathRotation = 0.0;
        this.skidSpawnTimer = 0.0;
        this.lastMoveDirX = 0.0;
        this.lastMoveDirY = 0.0;
        this.deathSkidParticles.clear();
    }

    /**
     * Vẽ mô hình đặc nhiệm chiến thuật trong không gian màn hình:
     * - Bóng đổ chân thực.
     * - Áo giáp Plate Carrier màu Sa Mạc / Coyote Tan dã chiến.
     * - Mũ chống đạn Kevlar Desert FAST Combat Helmet bo tròn.
     * - Dải phản quang nhận diện đồng minh chiến thuật (IFF Squad Beacon) Cyan trên gáy mũ.
     * - Kính ngắm quang học tác chiến Electric Cyan Visor xoay theo góc ngắm chuột.
     * - Hiệu ứng chớp đỏ khi đang trong thời gian bất tử (i-frames).
     *
     * @param g      Đối tượng đồ họa Graphics2D.
     * @param camera Camera chuyển đổi thế giới sang màn hình.
     */
    public void render(Graphics2D g, Camera camera) {
        render(g, camera, null);
    }

    /**
     * Vẽ mô hình đặc nhiệm chiến thuật trong không gian màn hình:
     * - Ưu tiên nạp và vẽ Sprite pixel art (PlayerGun / PlayerMelee) xoay theo góc ngắm chuột.
     * - Khi không có sprite: tự động chuyển sang vẽ mô hình Vector dã chiến dự phòng.
     *
     * @param g            Đối tượng đồ họa Graphics2D.
     * @param camera       Camera chuyển đổi thế giới sang màn hình.
     * @param activeWeapon Vũ khí hiện đang kích hoạt (để chọn form cầm súng hoặc cầm dao).
     */
    public void render(Graphics2D g, Camera camera, Weapon activeWeapon) {
        if (isDead) {
            renderDeathSkull(g, camera);
            return;
        }

        // 0. Render các tàn ảnh bóng ma (Dodge Ghost Trail) khi đang né đòn
        for (DodgeGhost ghost : dodgeGhosts) {
            renderGhost(g, camera, ghost);
        }

        double centerX = getCenterX();
        double centerY = getCenterY();
        int screenPX = camera.worldToScreenX(centerX);
        int screenPY = camera.worldToScreenY(centerY);

        // 1. Bóng đổ dã chiến (Shadow)
        g.setColor(new Color(0, 0, 0, 100));
        g.fillOval(screenPX - 14, screenPY - 8, 28, 16);

        // 2. Thử nạp và hiển thị Sprite nhân vật (PlayerGun hoặc PlayerMelee)
        BufferedImage sprite = (activeWeapon != null && activeWeapon.isMelee())
                ? GameSpriteManager.getPlayerMeleeSprite()
                : GameSpriteManager.getPlayerGunSprite();

        if (sprite != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            // Khi bất tử (invulnerableTimer > 0): Nhấp nháy chớp phản hồi lực va chạm
            if (invulnerableTimer > 0 && ((int) (invulnerableTimer * 18)) % 2 == 0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            }
            int spriteDrawSize = (int) Math.round(getSize() * 1.4); // ~40-42px, cân xứng với hitbox 30px
            GameSpriteManager.renderRotatedSprite(g2, sprite, screenPX, screenPY, aimAngle, spriteDrawSize, spriteDrawSize);
            g2.dispose();

            // Hiệu ứng vòng hào quang khí động học và vệt sóng xung chấn khi đang lướt né (i-frames)
            if (dodging) {
                g.setColor(new Color(0, 235, 255, 140));
                g.setStroke(new BasicStroke(2.0f));
                g.drawOval(screenPX - 18, screenPY - 18, 36, 36);
                g.setColor(new Color(0, 210, 255, 60));
                g.fillOval(screenPX - 17, screenPY - 17, 34, 34);
                g.setStroke(new BasicStroke(1.0f));
            }
            return;
        }

        // --- Dự phòng an toàn (Vector Fallback) nếu chưa nạp được sprite ---

        // 2. Thân chiến binh (Áo giáp Plate Carrier màu Sa Mạc / Coyote Tan)
        // Khi bất tử (invulnerableTimer > 0): Nhấp nháy chớp đỏ phản hồi lực va chạm
        Color operatorVest;
        Color operatorBorder;
        if (invulnerableTimer > 0 && ((int) (invulnerableTimer * 18)) % 2 == 0) {
            operatorVest = new Color(255, 90, 90, 190);
            operatorBorder = new Color(180, 40, 40);
        } else {
            operatorVest = new Color(195, 155, 95); // Coyote Tan dã chiến hiện đại
            operatorBorder = new Color(130, 98, 55); // Viền đai chịu lực tác chiến
        }
        g.setColor(operatorVest);
        g.fillOval(screenPX - 15, screenPY - 15, 30, 30);
        g.setColor(operatorBorder);
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(screenPX - 15, screenPY - 15, 30, 30);
        g.setStroke(new BasicStroke(1.0f));

        // 3. Tấm đệm giáp ngực dã chiến (MOLLE Chest Rig Plate)
        g.setColor(invulnerableTimer > 0 ? new Color(255, 130, 130) : new Color(165, 128, 75));
        g.fillRoundRect(screenPX - 8, screenPY - 8, 16, 16, 4, 4);

        // 4. Mũ chống đạn Kevlar tác chiến Sa Mạc (Desert FAST Combat Helmet) bo tròn trên đầu
        int helmetR = 10;
        Color helmetColor = invulnerableTimer > 0 ? new Color(255, 120, 120) : new Color(225, 188, 125);
        g.setColor(helmetColor);
        g.fillOval(screenPX - helmetR, screenPY - helmetR, helmetR * 2, helmetR * 2);
        g.setColor(new Color(145, 115, 75));
        g.drawOval(screenPX - helmetR, screenPY - helmetR, helmetR * 2, helmetR * 2);

        // 5. Dải phản quang nhận diện đồng minh chiến thuật (IFF Squad Beacon) trên gáy mũ
        g.setColor(new Color(0, 210, 255)); // Cyan nhận diện đặc nhiệm phe ta
        g.fillRect(screenPX - 4, screenPY - helmetR - 1, 8, 3);

        // 6. Kính ngắm quang học tác chiến Xanh Lam Điện Tử (Electric Cyan HUD Visor) theo góc ngắm
        int visorX = (int) (screenPX + Math.cos(aimAngle) * 7);
        int visorY = (int) (screenPY + Math.sin(aimAngle) * 7);
        g.setColor(new Color(0, 235, 255)); // Xanh lam neon / cyan rực rỡ
        g.fillOval(visorX - 3, visorY - 3, 7, 7);
        g.setColor(new Color(230, 255, 255)); // Tâm kính sáng hội tụ
        g.fillOval(visorX - 1, visorY - 1, 3, 3);

        // 7. Hiệu ứng vòng hào quang khí động học và vệt sóng xung chấn khi đang lướt né (i-frames)
        if (dodging) {
            g.setColor(new Color(0, 235, 255, 140));
            g.setStroke(new BasicStroke(2.0f));
            g.drawOval(screenPX - 18, screenPY - 18, 36, 36);
            g.setColor(new Color(0, 210, 255, 60));
            g.fillOval(screenPX - 17, screenPY - 17, 34, 34);
            g.setStroke(new BasicStroke(1.0f));
        }
    }

    /**
     * Hiển thị thanh đếm ngược tiến trình nạp đạn (Reload Progress Bar & Countdown) ngay dưới chân người chơi.
     * Tự động xuất hiện khi vũ khí đang nạp đạn và biến mất khi hoàn tất.
     *
     * @param g            Đối tượng đồ họa Graphics2D.
     * @param camera       Camera quan sát.
     * @param activeWeapon Vũ khí đang kích hoạt của người chơi.
     */
    public void renderReloadIndicator(Graphics2D g, Camera camera, Weapon activeWeapon) {
        if (activeWeapon == null || !activeWeapon.isReloading()) return;

        double centerX = getCenterX();
        double centerY = getCenterY();
        int screenPX = camera.worldToScreenX(centerX);
        int screenPY = camera.worldToScreenY(centerY);

        int barW = 38;
        int barH = 5;
        int barX = screenPX - (barW / 2);
        int barY = screenPY + 20;

        // Khung nền tối sci-fi bo góc
        g.setColor(new Color(15, 20, 30, 220));
        g.fillRoundRect(barX - 1, barY - 1, barW + 2, barH + 2, 4, 4);
        g.setColor(new Color(65, 85, 115));
        g.setStroke(new BasicStroke(1.0f));
        g.drawRoundRect(barX - 1, barY - 1, barW + 2, barH + 2, 4, 4);

        // Dải tiến trình nạp đạn
        double progress = Math.max(0.0, Math.min(1.0, activeWeapon.getReloadProgress()));
        int fillW = (int) Math.round(barW * progress);
        if (fillW > 0) {
            g.setColor(new Color(255, 150, 40)); // Vàng cam ấm áp rực rỡ
            g.fillRoundRect(barX, barY, fillW, barH, 2, 2);
            // Highlight mép trên
            g.setColor(new Color(255, 255, 255, 120));
            g.fillRect(barX, barY, fillW, 1);
        }

        // Chữ số đếm ngược thời gian nạp đạn còn lại (ví dụ "1.2s", "0.6s")
        double remainingSec = Math.max(0.0, activeWeapon.getReloadTimer());
        String timeText = String.format("%.1fs", remainingSec);
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(timeText);
        int textX = screenPX - (textW / 2);
        int textY = barY + barH + 9;

        g.setColor(new Color(10, 15, 22, 190));
        g.fillRoundRect(textX - 3, textY - 8, textW + 6, 10, 3, 3);
        g.setColor(new Color(255, 210, 75));
        g.drawString(timeText, textX, textY);
    }

    /**
     * Hiển thị thông báo nhỏ cảnh báo "HẾT SẠCH ĐẠN" ở ngay dưới chân nhân vật
     * khi vũ khí hiện tại hết cả đạn trong băng lẫn đạn dự trữ.
     * Cảnh báo có viền đỏ phát sáng nhấp nháy (pulsing) để người chơi dễ dàng nhận biết.
     *
     * @param g           Đối tượng đồ họa Graphics2D.
     * @param camera      Camera quan sát.
     * @param isOutOfAmmo Trạng thái cạn kiệt toàn bộ đạn của vũ khí đang cầm.
     */
    public void renderOutOfAmmoWarning(Graphics2D g, Camera camera, boolean isOutOfAmmo) {
        if (!isOutOfAmmo || isDead) return;

        double centerX = getCenterX();
        double centerY = getCenterY();
        int screenPX = camera.worldToScreenX(centerX);
        int screenPY = camera.worldToScreenY(centerY);

        // Hiệu ứng nhấp nháy phát sáng nhẹ (pulsing cảnh báo)
        double pulse = 0.5 + 0.5 * Math.sin(System.currentTimeMillis() * 0.009);
        int alphaBox = (int) (185 + 40 * pulse);
        int alphaBorder = (int) (160 + 95 * pulse);

        String warningText = "⚠ HẾT SẠCH ĐẠN!";
        Font font = new Font("SansSerif", Font.BOLD, 10);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(warningText);
        int boxW = textW + 14;
        int boxH = 17;
        int boxX = screenPX - (boxW / 2);
        int boxY = screenPY + 22; // Nằm ngay dưới chân người chơi

        // Khung nền tối sci-fi bo góc
        g.setColor(new Color(25, 10, 15, alphaBox));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 4, 4);

        // Viền đỏ phát sáng nhấp nháy
        g.setColor(new Color(255, 55, 55, alphaBorder));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 4, 4);

        // Chữ cảnh báo màu đỏ tươi phát sáng rực rỡ
        int redG = (int) (80 + 50 * pulse);
        int redB = (int) (80 + 50 * pulse);
        g.setColor(new Color(255, redG, redB));
        g.drawString(warningText, boxX + 7, boxY + 12);
    }

    // --- Getters & Setters ---

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getCenterX() {
        return x + (getSize() / 2.0);
    }

    public double getCenterY() {
        return y + (getSize() / 2.0);
    }

    public double getSize() {
        return size * buffManager.getHitboxScale();
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getAimAngle() {
        return aimAngle;
    }

    public void setAimAngle(double aimAngle) {
        this.aimAngle = aimAngle;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = Math.max(1, maxHealth);
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = Math.max(0, Math.min(this.maxHealth, currentHealth));
    }

    public double getInvulnerableTimer() {
        return invulnerableTimer;
    }

    public void setInvulnerableTimer(double invulnerableTimer) {
        this.invulnerableTimer = invulnerableTimer;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        if (dead) {
            initiateDeathSequence();
        } else {
            this.isDead = false;
            this.deathTimer = 0.0;
            this.deathSlideVx = 0.0;
            this.deathSlideVy = 0.0;
            this.deathSkidParticles.clear();
        }
    }

    // --- Kỹ năng Né đòn & i-frames (Combat Dash / Dodge Roll) Getters & Setters ---

    public boolean isDodging() {
        return dodging;
    }

    public boolean canDodge() {
        return !isDead && !dodging && dodgeCooldownTimer <= 0;
    }

    public double getDodgeTimer() {
        return dodgeTimer;
    }

    public void setDodgeTimer(double timer) {
        this.dodgeTimer = Math.max(0.0, timer);
    }

    public void setDodging(boolean dodging) {
        this.dodging = dodging;
    }

    public void setDodgeDir(double dirX, double dirY) {
        this.dodgeDirX = dirX;
        this.dodgeDirY = dirY;
    }

    public double getDodgeCooldownTimer() {
        return dodgeCooldownTimer;
    }

    public void setDodgeCooldownTimer(double timer) {
        this.dodgeCooldownTimer = Math.max(0.0, timer);
    }

    public double getDodgeCooldownMax() {
        return DODGE_COOLDOWN / buffManager.getDodgeCooldownFactor();
    }

    public double getDodgeProgress() {
        double maxCd = getDodgeCooldownMax();
        if (dodgeCooldownTimer <= 0) return 1.0;
        return Math.max(0.0, Math.min(1.0, 1.0 - (dodgeCooldownTimer / maxCd)));
    }

    public com.groupproject.game.entity.buff.PlayerBuffManager getBuffManager() {
        return buffManager;
    }

    public double getDodgeDirX() {
        return dodgeDirX;
    }

    public double getDodgeDirY() {
        return dodgeDirY;
    }

    public List<DodgeGhost> getDodgeGhosts() {
        return dodgeGhosts;
    }

    /**
     * Vẽ tàn ảnh bóng ma (Dodge Ghost Trail) mờ dần theo thời gian sống.
     */
    private void renderGhost(Graphics2D g, Camera camera, DodgeGhost ghost) {
        int screenX = camera.worldToScreenX(ghost.getX() + size / 2.0);
        int screenY = camera.worldToScreenY(ghost.getY() + size / 2.0);
        float alpha = ghost.getAlpha() * 0.45f;
        if (alpha <= 0.01f) return;

        Color ghostBody = new Color(0.0f, 0.85f, 1.0f, alpha);
        Color ghostBorder = new Color(0.0f, 0.5f, 0.8f, alpha);

        g.setColor(ghostBody);
        g.fillOval(screenX - 14, screenY - 14, 28, 28);
        g.setColor(ghostBorder);
        g.drawOval(screenX - 14, screenY - 14, 28, 28);

        // Kính ngắm quang học ghost
        int visorX = (int) (screenX + Math.cos(ghost.getAimAngle()) * 6);
        int visorY = (int) (screenY + Math.sin(ghost.getAimAngle()) * 6);
        g.setColor(new Color(1.0f, 1.0f, 1.0f, alpha));
        g.fillOval(visorX - 2, visorY - 2, 5, 5);
    }

    /**
     * Thực thể tàn ảnh bóng ma (Ghost Trail) phát sinh sau lưng người chơi trong lúc né đòn.
     */
    public static class DodgeGhost {
        private final double x;
        private final double y;
        private final double aimAngle;
        private double lifeTime;
        private final double maxLifeTime;

        public DodgeGhost(double x, double y, double aimAngle, double maxLifeTime) {
            this.x = x;
            this.y = y;
            this.aimAngle = aimAngle;
            this.lifeTime = maxLifeTime;
            this.maxLifeTime = maxLifeTime;
        }

        public boolean update(double deltaTime) {
            lifeTime -= deltaTime;
            return lifeTime <= 0;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getAimAngle() {
            return aimAngle;
        }

        public float getAlpha() {
            return (float) Math.max(0.0, Math.min(1.0, lifeTime / maxLifeTime));
        }
    }

    // --- Các phương thức hỗ trợ hoạt cảnh tử trận (Death Slide Mechanics) ---

    /**
     * Kích hoạt chuỗi hoạt cảnh tử trận: Biến thành đầu lâu trượt ma sát trên sàn hầm ngục.
     */
    public void initiateDeathSequence() {
        this.isDead = true;
        this.deathTimer = 0.0;
        this.dodging = false;
        this.invulnerableTimer = 999.0; // Miễn nhiễm sát thương tiếp theo khi đã tử trận
        this.deathRotation = 0.0;
        this.skidSpawnTimer = 0.0;
        this.deathSkidParticles.clear();

        // Xác định hướng và vận tốc trượt ban đầu theo quán tính di chuyển hoặc bị hất văng ra sau
        double slideSpeed = 280.0; // 280 px/s ban đầu
        if (Math.hypot(lastMoveDirX, lastMoveDirY) > 0.1) {
            this.deathSlideVx = lastMoveDirX * slideSpeed;
            this.deathSlideVy = lastMoveDirY * slideSpeed;
        } else {
            // Đang đứng yên: Trượt văng ngược lại hướng ngắm đòn đánh
            this.deathSlideVx = -Math.cos(aimAngle) * 220.0;
            this.deathSlideVy = -Math.sin(aimAngle) * 220.0;
        }
    }

    /**
     * Cập nhật chuyển động trượt, va chạm tường và giảm tốc ma sát của đầu lâu trên sàn.
     */
    public void updateDeathSlide(double deltaTime, DungeonMap dungeonMap) {
        deathTimer += deltaTime;

        // Cập nhật các hạt bụi ma sát
        deathSkidParticles.removeIf(p -> p.update(deltaTime));

        double currentSlideSpeed = Math.hypot(deathSlideVx, deathSlideVy);
        if (currentSlideSpeed > 3.0) {
            double moveX = deathSlideVx * deltaTime;
            double moveY = deathSlideVy * deltaTime;
            double curSize = getSize();

            if (dungeonMap != null) {
                if (dungeonMap.isAreaWalkable(x + moveX, y, curSize, curSize)) {
                    x += moveX;
                } else {
                    deathSlideVx = -deathSlideVx * 0.25; // Dội nhẹ khi va tường
                }
                if (dungeonMap.isAreaWalkable(x, y + moveY, curSize, curSize)) {
                    y += moveY;
                } else {
                    deathSlideVy = -deathSlideVy * 0.25;
                }
            } else {
                x += moveX;
                y += moveY;
            }

            // Giảm tốc do lực ma sát trượt mặt sàn hầm ngục
            double friction = 2.3;
            deathSlideVx *= Math.max(0.0, 1.0 - friction * deltaTime);
            deathSlideVy *= Math.max(0.0, 1.0 - friction * deltaTime);

            // Góc xoay lăn đầu lâu theo lực quán tính trượt
            deathRotation += (currentSlideSpeed * 0.025) * deltaTime;

            // Sinh các hạt khói bụi cọ xát sau lưng đầu lâu
            skidSpawnTimer += deltaTime;
            if (skidSpawnTimer >= 0.035 && currentSlideSpeed > 20.0) {
                skidSpawnTimer = 0.0;
                spawnSkidParticle();
            }
        } else {
            deathSlideVx = 0.0;
            deathSlideVy = 0.0;
        }
    }

    private void spawnSkidParticle() {
        double px = getCenterX() + ThreadLocalRandom.current().nextDouble(-4, 4);
        double py = getCenterY() + ThreadLocalRandom.current().nextDouble(-3, 3);
        double r = ThreadLocalRandom.current().nextDouble(2.0, 5.0);
        double life = ThreadLocalRandom.current().nextDouble(0.4, 0.8);
        int gray = ThreadLocalRandom.current().nextInt(55, 90);
        Color color = new Color(gray, gray, gray, 140);
        deathSkidParticles.add(new SkidParticle(px, py, r, life, color));
    }

    /**
     * Vẽ chiếc Đầu Lâu Chiến Binh (Fallen Operative Skull) trượt ma sát trên sàn hầm ngục.
     */
    private void renderDeathSkull(Graphics2D g, Camera camera) {
        // 1. Render các vệt ma sát / hạt bụi cọ xát trên sàn
        for (SkidParticle p : deathSkidParticles) {
            p.render(g, camera);
        }

        double centerX = getCenterX();
        double centerY = getCenterY();
        int screenPX = camera.worldToScreenX(centerX);
        int screenPY = camera.worldToScreenY(centerY);

        // 2. Bóng đổ dã chiến phẳng sát mặt đất
        g.setColor(new Color(0, 0, 0, 110));
        g.fillOval(screenPX - 12, screenPY + 4, 24, 10);

        // 3. Vệt cọ xát ma sát cháy sém sau hướng trượt
        double speed = Math.hypot(deathSlideVx, deathSlideVy);
        if (speed > 10.0) {
            g.setColor(new Color(40, 35, 30, 95));
            double slideAngle = Math.atan2(deathSlideVy, deathSlideVx);
            int skidTailX = (int) (screenPX - Math.cos(slideAngle) * 14);
            int skidTailY = (int) (screenPY - Math.sin(slideAngle) * 14);
            g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(skidTailX, skidTailY, screenPX, screenPY);
            g.setStroke(new BasicStroke(1.0f));
        }

        // 4. Vẽ Đầu Lâu Tử Trận (Operative Fallen Skull)
        Graphics2D g2 = (Graphics2D) g.create();
        // Áp dụng góc xoay nhẹ theo lực quán tính trượt
        g2.rotate(deathRotation * 0.4, screenPX, screenPY);

        // Hộp sọ chính (Cranium) bo tròn màu trắng ngà xương
        g2.setColor(new Color(238, 232, 222)); // Bone Ivory
        g2.fillOval(screenPX - 12, screenPY - 12, 24, 22);
        g2.setColor(new Color(145, 135, 125)); // Viền xương xám
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(screenPX - 12, screenPY - 12, 24, 22);

        // Vùng hàm dưới và hàng răng xương trắng
        g2.setColor(new Color(238, 232, 222));
        g2.fillRoundRect(screenPX - 7, screenPY + 6, 14, 8, 3, 3);
        g2.setColor(new Color(145, 135, 125));
        g2.drawRoundRect(screenPX - 7, screenPY + 6, 14, 8, 3, 3);

        // Kẽ các răng xương
        g2.setColor(new Color(60, 55, 50));
        g2.drawLine(screenPX - 3, screenPY + 6, screenPX - 3, screenPY + 13);
        g2.drawLine(screenPX, screenPY + 6, screenPX, screenPY + 13);
        g2.drawLine(screenPX + 3, screenPY + 6, screenPX + 3, screenPY + 13);

        // Hai hốc mắt to màu đen sâu thẳm
        g2.setColor(new Color(22, 18, 20));
        g2.fillOval(screenPX - 9, screenPY - 5, 7, 9);
        g2.fillOval(screenPX + 2, screenPY - 5, 7, 9);

        // Đốm lửa ma trơi le lói trong hốc mắt (nhạt dần theo tiến trình trượt)
        double soulAlpha = Math.max(0.0, 1.0 - deathTimer / DEATH_SLIDE_DURATION);
        if (soulAlpha > 0.05) {
            int eyeAlpha = (int) (220 * soulAlpha);
            g2.setColor(new Color(0, 230, 255, eyeAlpha)); // Cyan linh hồn chiến binh đặc nhiệm
            g2.fillOval(screenPX - 6, screenPY - 2, 3, 3);
            g2.fillOval(screenPX + 5, screenPY - 2, 3, 3);
        }

        // Hốc mũi tam giác ngược
        g2.setColor(new Color(22, 18, 20));
        g2.fillPolygon(new int[]{screenPX - 2, screenPX + 2, screenPX}, new int[]{screenPY + 2, screenPY + 2, screenPY + 5}, 3);

        // Vết nứt sọ chiến trường (Battle damage crack)
        g2.setColor(new Color(125, 115, 105));
        g2.drawLine(screenPX + 2, screenPY - 12, screenPX + 4, screenPY - 8);
        g2.drawLine(screenPX + 4, screenPY - 8, screenPX + 1, screenPY - 5);

        g2.dispose();
    }

    /**
     * Kiểm tra xem chuỗi hoạt cảnh đầu lâu trượt tử trận đã hoàn tất chưa (sau 2.0 giây).
     */
    public boolean isDeathSequenceComplete() {
        return isDead && deathTimer >= DEATH_SLIDE_DURATION;
    }

    public double getDeathTimer() {
        return deathTimer;
    }

    public double getDeathSequenceDuration() {
        return DEATH_SLIDE_DURATION;
    }

    public double getDeathSequenceProgress() {
        return Math.clamp(deathTimer / DEATH_SLIDE_DURATION, 0.0, 1.0);
    }

    /**
     * Hoàn tất ngay chuỗi tử trận (khi người chơi nhấn phím bỏ qua để xem bảng kết quả ngay).
     */
    public void completeDeathSequence() {
        if (isDead) {
            this.deathTimer = DEATH_SLIDE_DURATION;
            this.deathSlideVx = 0.0;
            this.deathSlideVy = 0.0;
        }
    }
}

