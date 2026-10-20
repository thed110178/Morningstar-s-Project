package com.groupproject.game.entity.player;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.entity.weapon.Weapon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    private boolean dodging = false;
    private double dodgeTimer = 0.0;
    private double dodgeCooldownTimer = 0.0;
    private double dodgeDirX = 0.0;
    private double dodgeDirY = 0.0;
    private double ghostSpawnTimer = 0.0;
    private final List<DodgeGhost> dodgeGhosts = new CopyOnWriteArrayList<>();

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
     * Cập nhật đếm ngược bộ đếm thời gian bất tử (i-frames), hồi chiêu né và tàn ảnh bóng ma.
     *
     * @param deltaTime Khoảng thời gian trôi qua giữa 2 khung hình (giây).
     */
    public void update(double deltaTime) {
        if (invulnerableTimer > 0) {
            invulnerableTimer -= deltaTime;
        }

        // Cập nhật hồi chiêu né đòn
        if (dodgeCooldownTimer > 0) {
            dodgeCooldownTimer = Math.max(0.0, dodgeCooldownTimer - deltaTime);
        }

        // Cập nhật trạng thái né và chu kỳ sinh tàn ảnh bóng ma
        if (dodging) {
            dodgeTimer -= deltaTime;
            ghostSpawnTimer += deltaTime;
            if (ghostSpawnTimer >= GHOST_INTERVAL) {
                ghostSpawnTimer = 0.0;
                spawnGhost();
            }
            if (dodgeTimer <= 0) {
                dodging = false;
                dodgeTimer = 0.0;
            }
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
        } else {
            moveStep = speed * deltaTime;
            if (up) moveY -= moveStep;
            if (down) moveY += moveStep;
            if (left) moveX -= moveStep;
            if (right) moveX += moveStep;

            // Chuẩn hóa di chuyển chéo (Diagonal movement normalization)
            if (moveX != 0 && moveY != 0) {
                double invSqrt = 1.0 / Math.sqrt(2.0);
                moveX *= invSqrt;
                moveY *= invSqrt;
            }
        }

        // Trượt va chạm 2 trục độc lập (Decoupled axis wall sliding)
        if (moveX != 0) {
            double nextX = x + moveX;
            if (dungeonMap.isAreaWalkable(nextX, y, size, size)) {
                x = nextX;
            }
        }
        if (moveY != 0) {
            double nextY = y + moveY;
            if (dungeonMap.isAreaWalkable(x, nextY, size, size)) {
                y = nextY;
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
        this.dodgeCooldownTimer = DODGE_COOLDOWN;
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

        currentHealth = Math.max(0, currentHealth - damage);
        invulnerableTimer = 0.8; // 0.8s thời gian bất tử i-frames sau khi trúng đòn

        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
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
        this.dodgeCooldownTimer = 0.0;
        this.dodgeGhosts.clear();
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
        return x + (size / 2.0);
    }

    public double getCenterY() {
        return y + (size / 2.0);
    }

    public double getSize() {
        return size;
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
        this.isDead = dead;
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

    public double getDodgeCooldownTimer() {
        return dodgeCooldownTimer;
    }

    public void setDodgeCooldownTimer(double timer) {
        this.dodgeCooldownTimer = Math.max(0.0, timer);
    }

    public double getDodgeCooldownMax() {
        return DODGE_COOLDOWN;
    }

    public double getDodgeProgress() {
        if (dodgeCooldownTimer <= 0) return 1.0;
        return Math.max(0.0, Math.min(1.0, 1.0 - (dodgeCooldownTimer / DODGE_COOLDOWN)));
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
}
