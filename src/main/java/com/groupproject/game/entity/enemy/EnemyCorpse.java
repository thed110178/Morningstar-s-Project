package com.groupproject.game.entity.enemy;

import com.groupproject.game.core.Camera;
import com.groupproject.game.dungeon.DungeonMap;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * Thực thể xác kẻ địch (Enemy Corpse) xuất hiện khi mục tiêu bị tiêu diệt.
 *
 * <p>WHY: Thể hiện phản hồi thị giác chân thực cho trận chiến:
 * - Kẻ địch thông thường để lại một cái xác hình đầu lâu tông màu xám nâu mờ nhạt (faded skull)
 *   nhận lực đẩy lùi từ đòn kết liễu, trượt văng trên sàn và từ từ biến mất.
 * - Boss Tank khi bị tiêu diệt để lại xác cỗ xe bọc thép bị thiêu cháy rụi (charred wreck)
 *   với tháp pháo vỡ nát lệch góc, bánh xích đứt đoạn, tàn lửa đỏ âm ỉ và khói bốc lên,
 *   tồn tại vĩnh viễn trên đấu trường làm chiến tích.
 */
public class EnemyCorpse {

    private double x;
    private double y;
    private final double size;
    private double vx;
    private double vy;
    private double rotation;
    private double angularVelocity;
    private double lifeTime;
    private final double maxLifeTime;
    private final boolean isBoss;
    private double smokeAnimTimer = 0.0;

    /**
     * Khởi tạo xác kẻ địch với vị trí tâm, kích thước và xung lực đẩy lùi ban đầu.
     *
     * @param centerX        Tọa độ tâm X của kẻ địch tử trận.
     * @param centerY        Tọa độ tâm Y của kẻ địch tử trận.
     * @param enemySize      Kích thước hitbox của kẻ địch vừa chết.
     * @param knockbackAngle Góc văng đẩy lùi (radian) hoặc hướng thân xe Boss Tank.
     * @param knockbackForce Xung lực đẩy lùi (px/s).
     * @param isBoss         Cờ đánh dấu nếu đây là Boss Tank cỡ đại.
     */
    public EnemyCorpse(double centerX, double centerY, double enemySize, double knockbackAngle, double knockbackForce, boolean isBoss) {
        this.isBoss = isBoss;
        // Kích thước: quái thường ~22-26px, Boss Tank giữ nguyên kích thước thân xe (~84-96px)
        this.size = isBoss ? enemySize : Math.max(22.0, enemySize * 0.85);
        this.x = centerX - this.size / 2.0;
        this.y = centerY - this.size / 2.0;
        this.maxLifeTime = isBoss ? Double.MAX_VALUE : 4.0;
        this.lifeTime = this.maxLifeTime;

        if (isBoss) {
            // Xác Boss Tank hạng nặng đứng bất động vĩnh viễn tại vị trí nổ tung
            this.vx = 0.0;
            this.vy = 0.0;
            this.rotation = knockbackAngle;
            this.angularVelocity = 0.0;
        } else {
            // Vận tốc trượt ban đầu từ lực knockback (tối thiểu 160 px/s để luôn có độ trượt văng)
            double initialSpeed = Math.max(160.0, knockbackForce);
            this.vx = Math.cos(knockbackAngle) * initialSpeed;
            this.vy = Math.sin(knockbackAngle) * initialSpeed;

            // Xoay nhẹ theo hướng văng và quán tính góc xoay
            this.rotation = knockbackAngle;
            this.angularVelocity = (Math.random() - 0.5) * 6.5;
        }
    }

    /**
     * Cập nhật chuyển động trượt ma sát trên sàn và va chạm tường 2 trục độc lập.
     *
     * @param deltaTime  Thời gian delta trôi qua giữa 2 khung hình (giây).
     * @param dungeonMap Bản đồ hầm ngục kiểm tra va chạm tường và cửa.
     */
    public void update(double deltaTime, DungeonMap dungeonMap) {
        if (isBoss) {
            smokeAnimTimer += deltaTime;
            return;
        }

        lifeTime -= deltaTime;

        double speed = Math.hypot(vx, vy);
        if (speed > 8.0) {
            // Hệ số ma sát mặt sàn gạch hầm ngục (trượt mượt mà trong ~0.6s - 1.2s rồi dừng hẳn)
            double friction = Math.max(0.0, 1.0 - 5.2 * deltaTime);
            vx *= friction;
            vy *= friction;

            rotation += angularVelocity * deltaTime;
            angularVelocity *= friction;

            double moveX = vx * deltaTime;
            double moveY = vy * deltaTime;

            // Trượt va chạm 2 trục độc lập (Decoupled axis sliding) để đầu lâu không xuyên tường
            if (moveX != 0) {
                double nextX = x + moveX;
                if (dungeonMap.isAreaWalkable(nextX, y, size, size)) {
                    x = nextX;
                } else {
                    vx = -vx * 0.25; // Nảy nhẹ khi đập vào vách tường kiên cố
                }
            }
            if (moveY != 0) {
                double nextY = y + moveY;
                if (dungeonMap.isAreaWalkable(x, nextY, size, size)) {
                    y = nextY;
                } else {
                    vy = -vy * 0.25; // Nảy nhẹ khi đập vào vách tường kiên cố
                }
            }
        } else {
            vx = 0.0;
            vy = 0.0;
            angularVelocity = 0.0;
        }
    }

    /**
     * Kết xuất đồ họa xác kẻ địch trên mặt sàn:
     * - Boss Tank: Xác xe tăng bọc thép cháy rụi tồn tại vĩnh viễn.
     * - Quái thường: Đầu lâu tông màu xám nâu dã chiến mờ dần.
     *
     * @param g      Đối tượng đồ họa Graphics2D.
     * @param camera Camera thế giới.
     */
    public void render(Graphics2D g, Camera camera) {
        if (isFinished()) return;

        int screenX = camera.worldToScreenX(x + size / 2.0);
        int screenY = camera.worldToScreenY(y + size / 2.0);

        // Viewport Culling: Bỏ qua nếu xác nằm ngoài tầm nhìn camera
        if (screenX < -100 || screenX > com.groupproject.game.core.GameConfig.SCREEN_WIDTH + 100 ||
            screenY < -100 || screenY > com.groupproject.game.core.GameConfig.SCREEN_HEIGHT + 100) {
            return;
        }

        if (isBoss) {
            renderCharredBossTank(g, screenX, screenY);
            return;
        }

        // Tính toán độ mờ (Alpha) động cho quái thường
        float alpha = getAlpha();
        if (alpha <= 0.02f) return;

        int alphaInt = (int) (alpha * 255);
        int borderAlpha = Math.min(255, (int) (alpha * 230));
        int shadowAlpha = (int) (alpha * 85);

        AffineTransform oldTx = g.getTransform();
        g.rotate(rotation, screenX, screenY);

        int r = (int) (size / 2.0);

        // 1. Bóng đổ dã chiến trên sàn hầm ngục
        g.setColor(new Color(0, 0, 0, shadowAlpha));
        g.fillOval(screenX - (int) (r * 1.1), screenY - (int) (r * 0.4), (int) (r * 2.2), (int) (r * 0.9));

        // 2. Vòm hộp sọ (Cranium Dome)
        Color skullColor = new Color(165, 138, 108, alphaInt);
        Color skullBorder = new Color(90, 70, 50, borderAlpha);

        g.setColor(skullColor);
        int domeW = (int) (r * 1.85);
        int domeH = (int) (r * 1.60);
        g.fillOval(screenX - domeW / 2, screenY - domeH / 2 - 2, domeW, domeH);
        g.setColor(skullBorder);
        g.setStroke(new BasicStroke(1.2f));
        g.drawOval(screenX - domeW / 2, screenY - domeH / 2 - 2, domeW, domeH);

        // 3. Hàm răng xương (Jaw / Maxilla) mở rộng phía dưới
        int jawW = (int) (r * 1.15);
        int jawH = (int) (r * 0.85);
        int jawX = screenX - jawW / 2;
        int jawY = screenY + (int) (r * 0.15);

        g.setColor(skullColor);
        g.fillRoundRect(jawX, jawY, jawW, jawH, 3, 3);
        g.setColor(skullBorder);
        g.drawRoundRect(jawX, jawY, jawW, jawH, 3, 3);

        // Các rãnh răng xương (Teeth notches)
        Color teethNotchColor = new Color(55, 40, 30, borderAlpha);
        g.setColor(teethNotchColor);
        int toothSpacing = Math.max(2, jawW / 4);
        g.fillRect(screenX - toothSpacing, jawY + 2, 1, jawH - 3);
        g.fillRect(screenX, jawY + 2, 1, jawH - 3);
        g.fillRect(screenX + toothSpacing, jawY + 2, 1, jawH - 3);

        // 4. Hai hốc mắt rỗng tối sâu (Hollow Eye Sockets)
        Color socketColor = new Color(30, 20, 15, alphaInt);
        int eyeW = Math.max(3, (int) (r * 0.42));
        int eyeH = Math.max(3, (int) (r * 0.50));
        int eyeOffsetX = (int) (r * 0.38);
        int eyeY = screenY - (int) (r * 0.20);

        g.setColor(socketColor);
        g.fillOval(screenX - eyeOffsetX - eyeW / 2, eyeY, eyeW, eyeH);
        g.fillOval(screenX + eyeOffsetX - eyeW / 2, eyeY, eyeW, eyeH);

        // 5. Hốc mũi rỗng hình tam giác ngược (Nasal Cavity)
        int noseSize = Math.max(2, (int) (r * 0.22));
        int[] nx = {screenX, screenX - noseSize, screenX + noseSize};
        int[] ny = {screenY + (int) (r * 0.25), screenY + (int) (r * 0.05), screenY + (int) (r * 0.05)};
        g.fillPolygon(nx, ny, 3);

        g.setStroke(new BasicStroke(1.0f));
        g.setTransform(oldTx);
    }

    /**
     * Dựng hình xác xe tăng Boss Tank bọc thép bị thiêu cháy rụi (Charred Tank Wreckage).
     * Tồn tại vĩnh viễn trên sàn đấu với các chi tiết cơ khí bị phá hủy, tàn lửa và khói bốc lên.
     */
    private void renderCharredBossTank(Graphics2D g, int screenCX, int screenCY) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(screenCX, screenCY);
        g2.rotate(rotation);

        int tankL = 94; // Chiều dài thân xe tăng Boss
        int tankW = 68; // Chiều rộng thân xe tăng Boss
        int treadW = 16; // Bề rộng mỗi dải xích

        // 1. Vệt cháy sém loang lổ trên mặt đất (Scorched blast crater / burn marks on floor)
        g2.setColor(new Color(15, 14, 12, 175));
        g2.fillOval(-tankL / 2 - 12, -tankW / 2 - 10, tankL + 24, tankW + 20);
        g2.setColor(new Color(25, 20, 16, 120));
        g2.fillOval(-tankL / 2 - 6, -tankW / 2 - 6, tankL + 12, tankW + 12);

        // 2. Hai dải bánh xích kim loại bị nổ đứt gãy, ám muội than (Charred Caterpillar Treads)
        Color charredTreadBase = new Color(20, 22, 24);
        Color charredTreadSegment = new Color(42, 44, 46);

        // Xích trái (nứt vỡ, đứt đoạn)
        g2.setColor(charredTreadBase);
        g2.fillRoundRect(-tankL / 2, -tankW / 2, tankL - 6, treadW, 4, 4);
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(-tankL / 2, -tankW / 2, tankL - 6, treadW, 4, 4);

        // Xích phải (bị bung xích xòe ra sau)
        g2.setColor(charredTreadBase);
        g2.fillRoundRect(-tankL / 2 + 4, tankW / 2 - treadW, tankL - 4, treadW, 4, 4);
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(-tankL / 2 + 4, tankW / 2 - treadW, tankL - 4, treadW, 4, 4);

        // Các mắt xích kim loại cháy xém tróc sơn
        g2.setColor(charredTreadSegment);
        for (int tx = -tankL / 2 + 6; tx < tankL / 2 - 8; tx += 12) {
            g2.drawLine(tx, -tankW / 2 + 2, tx, -tankW / 2 + treadW - 3);
            g2.drawLine(tx, tankW / 2 - treadW + 2, tx, tankW / 2 - 3);
        }

        // 3. Khung gầm bọc thép bị thiêu cháy (Charred Chassis Body)
        int bodyW = tankW - treadW * 2 + 4;
        Color charredBody = new Color(30, 32, 35); // Đen xám chì của thép bị thiêu rụi
        g2.setColor(charredBody);
        g2.fillRoundRect(-tankL / 2 + 6, -bodyW / 2, tankL - 12, bodyW, 8, 8);

        // Viền vỏ thép cháy biến dạng
        g2.setColor(new Color(18, 18, 20));
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawRoundRect(-tankL / 2 + 6, -bodyW / 2, tankL - 12, bodyW, 8, 8);

        // Các mảng ố muội than & kim loại rỗ (Soot & scorch patches)
        g2.setColor(new Color(12, 12, 14, 210));
        g2.fillOval(-tankL / 2 + 12, -bodyW / 2 + 4, 28, 16);
        g2.fillOval(4, -8, 24, 18);
        g2.fillOval(-14, 2, 20, 14);

        // Vết nứt giáp xuyên thủng do đạn bộc phá
        g2.setColor(new Color(10, 10, 12));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(-16, -10, -4, -4);
        g2.drawLine(-4, -4, 8, -9);
        g2.drawLine(14, 6, 26, 2);

        // Tấm chắn giáp bùn trước & sau méo mó
        g2.setColor(new Color(22, 24, 26));
        g2.fillRect(-tankL / 2 + 2, -tankW / 2 - 1, 10, treadW + 2);
        g2.fillRect(-tankL / 2 + 2, tankW / 2 - treadW - 1, 10, treadW + 2);
        g2.fillRect(tankL / 2 - 12, -tankW / 2 - 1, 10, treadW + 2);
        g2.fillRect(tankL / 2 - 12, tankW / 2 - treadW - 1, 10, treadW + 2);

        // 4. Tháp pháo chính bị nổ sụp, nghiêng lệch góc (Ruined Broken Turret)
        Graphics2D gt = (Graphics2D) g2.create();
        gt.translate(2, 1);
        gt.rotate(-0.22); // Tháp pháo lệch góc do sức ép vụ nổ

        // Nòng pháo chính bị vỡ nứt, gãy gập đầu nòng
        int barrelLen = 38; // Nòng pháo bị nổ vỡ ngắn đi
        int barrelW = 13;
        Color charredBarrel = new Color(24, 26, 28);
        gt.setColor(charredBarrel);
        gt.fillRoundRect(10, -barrelW / 2, barrelLen, barrelW, 3, 3);
        gt.setColor(Color.BLACK);
        gt.drawRoundRect(10, -barrelW / 2, barrelLen, barrelW, 3, 3);

        // Loa hãm giật vỡ toác
        gt.setColor(new Color(18, 18, 20));
        gt.fillRect(10 + barrelLen - 6, -barrelW / 2 - 3, 8, barrelW + 6);
        // Vết nứt rách nòng
        gt.setColor(new Color(8, 8, 10));
        gt.drawLine(10 + barrelLen - 12, 0, 10 + barrelLen, -3);

        // Tháp pháo lục giác cháy đen
        int r = 28;
        int[] hexX = new int[] { r, r / 2, -r / 2, -r, -r / 2, r / 2 };
        int[] hexY = new int[] { 0, (int) (r * 0.86), (int) (r * 0.86), 0, (int) (-r * 0.86), (int) (-r * 0.86) };
        gt.setColor(new Color(25, 27, 30));
        gt.fillPolygon(hexX, hexY, 6);
        gt.setColor(new Color(15, 15, 18));
        gt.setStroke(new BasicStroke(2.0f));
        gt.drawPolygon(hexX, hexY, 6);

        // Nắp cửa hầm chỉ huy bị bật tung, để lộ khoang trong đen ngòm
        gt.setColor(new Color(10, 10, 12));
        gt.fillOval(-9, -9, 18, 18);
        gt.setColor(new Color(35, 38, 42));
        gt.drawOval(-9, -9, 18, 18);
        // Nắp kim loại bật nghiêng sang bên
        gt.fillRoundRect(6, -11, 8, 14, 2, 2);

        // 5. Tàn lửa đỏ rực âm ỉ trong buồng máy / khoang tháp pháo (Glowing Embers)
        double emberWave = Math.sin(smokeAnimTimer * 3.2);
        int emberAlpha = (int) (160 + 80 * emberWave);
        emberAlpha = Math.max(0, Math.min(255, emberAlpha));
        gt.setColor(new Color(230, 50, 15, emberAlpha));
        gt.fillOval(-6, -6, 12, 12);
        gt.setColor(new Color(255, 170, 30, (int) (emberAlpha * 0.85)));
        gt.fillOval(-3, -3, 6, 6);
        gt.setColor(new Color(255, 240, 120, (int) (emberAlpha * 0.50)));
        gt.fillOval(-1, -1, 3, 3);

        gt.dispose();
        g2.dispose();

        // 6. Cột khói đen & xám tro cuồn cuộn bốc lên cao từ xác xe (Billowing Dense Smoke Plume)
        // WHY: Vẽ theo hệ tọa độ màn hình không xoay (unrotated world/screen space) để làn khói
        // luôn bay thẳng đứng lên trên (-Y) kèm độ loang sóng gió, sử dụng các gam màu xám tro
        // tương phản rõ ràng với nền gạch và thân xe đen, giúp người chơi quan sát thấy rõ rệt.
        Graphics2D gs = (Graphics2D) g.create();
        gs.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        int baseOriginX = screenCX;
        int baseOriginY = screenCY - 4;

        // Vẽ 6 luồng khói cuồn cuộn so le nhau liên tục bốc lên cao
        int numPuffs = 6;
        for (int i = 0; i < numPuffs; i++) {
            // Chu kỳ bốc lên từ 0.0 (gốc buồng máy) -> 1.0 (trên đỉnh không trung)
            double cycle = (smokeAnimTimer * 0.45 + i * (1.0 / numPuffs)) % 1.0;

            // Quỹ đạo bốc lên theo trục thẳng đứng (-Y) và lượn sóng nhẹ theo gió
            double riseHeight = cycle * 68.0; // Bốc cao 68px
            double drift = Math.sin(smokeAnimTimer * 2.0 + i * 1.5) * (6.0 + cycle * 12.0);

            int puffX = (int) (baseOriginX + drift);
            int puffY = (int) (baseOriginY - riseHeight);

            // Kích thước đám khói nở to dần khi bốc lên cao (từ 18px lên 48px)
            int puffSize = (int) (18.0 + cycle * 30.0);
            int halfPuff = puffSize / 2;

            // Hàm độ mờ hình sin: xuất hiện mờ, đậm nhất ở giữa luồng (0.3 - 0.6), và tan biến dần ở đỉnh
            double alphaFactor = Math.sin(cycle * Math.PI);
            int puffAlpha = (int) (alphaFactor * 175.0);
            puffAlpha = Math.max(0, Math.min(255, puffAlpha));

            if (puffAlpha <= 5) continue;

            // Lớp khói xám tro dày đặc bên ngoài (Dark Gray Outer Smoke)
            gs.setColor(new Color(50, 52, 58, puffAlpha));
            gs.fillOval(puffX - halfPuff, puffY - halfPuff, puffSize, puffSize);

            // Lõi khói tro sáng tương phản nổi bật ở giữa (Contrasting Ash Gray Core)
            int coreSize = (int) (puffSize * 0.65);
            int coreAlpha = (int) (puffAlpha * 0.85);
            gs.setColor(new Color(125, 130, 140, coreAlpha));
            gs.fillOval(puffX - coreSize / 2, puffY - coreSize / 2, coreSize, coreSize);

            // Tán khói trắng xám nhạt bay lên cao nhất (Lighter Ash Highlights)
            if (cycle > 0.40) {
                int lightSize = (int) (puffSize * 0.42);
                int lightAlpha = (int) (puffAlpha * 0.65);
                gs.setColor(new Color(185, 190, 200, lightAlpha));
                gs.fillOval(puffX - lightSize / 2 - 2, puffY - lightSize / 2 - 3, lightSize, lightSize);
            }
        }

        // Tàn lửa đỏ bay theo khói (Rising Sparks & Embers)
        for (int i = 0; i < 3; i++) {
            double sparkCycle = (smokeAnimTimer * 0.9 + i * 0.33) % 1.0;
            if (sparkCycle < 0.65) {
                double sRise = sparkCycle * 45.0;
                double sDrift = Math.cos(smokeAnimTimer * 3.5 + i * 2.0) * (5.0 + sparkCycle * 8.0);
                int sX = (int) (baseOriginX + sDrift);
                int sY = (int) (baseOriginY - sRise);
                int sAlpha = (int) ((1.0 - sparkCycle / 0.65) * 230);
                gs.setColor(new Color(255, 140, 30, Math.max(0, Math.min(255, sAlpha))));
                gs.fillOval(sX - 1, sY - 1, 3, 3);
            }
        }

        gs.dispose();
    }

    /**
     * Tính toán hệ số độ mờ Alpha (0.0f đến 0.80f cho quái thường, 1.0f vĩnh viễn cho Boss Tank).
     */
    public float getAlpha() {
        if (isBoss) {
            return 1.0f;
        }
        if (lifeTime <= 0) return 0.0f;
        double fadeDuration = 1.6;
        if (lifeTime > fadeDuration) {
            return 0.80f;
        }
        return (float) Math.max(0.0, Math.min(0.80, (lifeTime / fadeDuration) * 0.80));
    }

    public boolean isFinished() {
        return !isBoss && lifeTime <= 0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSize() {
        return size;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getLifeTime() {
        return lifeTime;
    }

    public double getMaxLifeTime() {
        return maxLifeTime;
    }

    public boolean isBoss() {
        return isBoss;
    }
}
