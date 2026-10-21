package com.groupproject.game.ui;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.entity.enemy.TargetDummy;
import com.groupproject.game.entity.player.Player;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Điều phối đoạn cắt cảnh điện ảnh (Cinematic Intro) giới thiệu Boss khi người chơi bước vào Room Boss.
 *
 * <p>Tính năng:
 * <ul>
 *   <li>Tự động lia camera (Pan Camera) mượt mà từ vị trí Operative đến vị trí Boss và quay trở lại.</li>
 *   <li>Thanh viền điện ảnh Letterbox (Cinematic Bars) trượt từ mép trên và dưới màn hình.</li>
 *   <li>Banner cảnh báo công nghệ Cyberpunk: Tên Boss Neon rực lửa, danh hiệu, thanh chỉ số chiến thuật.</li>
 *   <li>Hiệu ứng rung chấn động cơ xe tăng hạng nặng và radar quét mục tiêu nguy hiểm cấp độ cao.</li>
 *   <li>Hỗ trợ bỏ qua tức thì khi nhấn [SPACE], [ENTER], [ESC] hoặc Click chuột.</li>
 * </ul>
 */
public class BossIntroCinematic {

    private static final double TOTAL_DURATION = 2.8; // Tổng thời lượng 2.8 giây
    private static final double PAN_TO_BOSS_TIME = 0.7; // 0.7s lia tới Boss
    private static final double HOLD_BOSS_TIME = 2.1;   // Đến 2.1s ngắm nhìn Boss
    private static final double RETURN_TIME = 2.8;      // 0.7s lia về Operative

    private boolean active = false;
    private double timer = 0.0;

    private double startCamX = 0;
    private double startCamY = 0;
    private double bossCamX = 0;
    private double bossCamY = 0;

    private String bossTitle = "HEAVY COMBAT BATTLE TANK [MK-IV]";
    private String bossSubtitle = "PHÁO ĐÀI CƠ GIỚI BỌC THÉP HẠNG NẶNG";
    private String bossThreatInfo = "PHÁO HẠNG NẶNG & TRIỆU HỒI TIẾP VIỆN";

    public BossIntroCinematic() {
    }

    /**
     * Khởi động chuỗi giới thiệu điện ảnh Boss.
     */
    public void start(TargetDummy boss, Player player) {
        if (boss == null || player == null) {
            return;
        }

        this.startCamX = player.getCenterX();
        this.startCamY = player.getCenterY();
        this.bossCamX = boss.getCenterX();
        this.bossCamY = boss.getCenterY();

        String rawName = boss.getName();
        if (rawName != null && !rawName.isEmpty()) {
            this.bossTitle = rawName.toUpperCase();
        }

        this.timer = 0.0;
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Bỏ qua ngay lập tức đoạn giới thiệu.
     */
    public void skip() {
        this.active = false;
        this.timer = TOTAL_DURATION;
    }

    /**
     * Xử lý phím tắt bỏ qua intro Boss ([SPACE], [ENTER], [ESC] hoặc click chuột trái).
     */
    public void handleInput(KeyboardHandler keyH, MouseHandler mouseH) {
        if (!active) return;

        if (keyH.isKeyPressedOnce(KeyEvent.VK_SPACE)
                || keyH.isKeyPressedOnce(KeyEvent.VK_ENTER)
                || keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)
                || (mouseH != null && mouseH.isLeftClickedOnce())) {
            skip();
        }
    }

    /**
     * Cập nhật thời gian cắt cảnh.
     */
    public void update(double deltaTime) {
        if (!active) return;

        timer += deltaTime;
        if (timer >= TOTAL_DURATION) {
            skip();
        }
    }

    /**
     * Lấy tọa độ X trọng tâm Camera hiện tại theo tiến trình lia máy Lerp mượt mà.
     */
    public double getCameraFocusX() {
        if (!active) return startCamX;

        double t = 0.0;
        if (timer < PAN_TO_BOSS_TIME) {
            // Lerp từ Player -> Boss
            t = smoothStep(timer / PAN_TO_BOSS_TIME);
            return startCamX + (bossCamX - startCamX) * t;
        } else if (timer < HOLD_BOSS_TIME) {
            // Giữ ngắm Boss + rung nhẹ
            double shakeX = ThreadLocalRandom.current().nextDouble(-1.5, 1.5);
            return bossCamX + shakeX;
        } else {
            // Lerp từ Boss -> Player
            t = smoothStep((timer - HOLD_BOSS_TIME) / (RETURN_TIME - HOLD_BOSS_TIME));
            return bossCamX + (startCamX - bossCamX) * t;
        }
    }

    /**
     * Lấy tọa độ Y trọng tâm Camera hiện tại theo tiến trình lia máy Lerp mượt mà.
     */
    public double getCameraFocusY() {
        if (!active) return startCamY;

        double t = 0.0;
        if (timer < PAN_TO_BOSS_TIME) {
            t = smoothStep(timer / PAN_TO_BOSS_TIME);
            return startCamY + (bossCamY - startCamY) * t;
        } else if (timer < HOLD_BOSS_TIME) {
            double shakeY = ThreadLocalRandom.current().nextDouble(-1.5, 1.5);
            return bossCamY + shakeY;
        } else {
            t = smoothStep((timer - HOLD_BOSS_TIME) / (RETURN_TIME - HOLD_BOSS_TIME));
            return bossCamY + (startCamY - bossCamY) * t;
        }
    }

    private double smoothStep(double x) {
        double clamped = Math.max(0.0, Math.min(1.0, x));
        return clamped * clamped * (3.0 - 2.0 * clamped);
    }

    /**
     * Kết xuất lớp phủ giao diện điện ảnh (Letterbox & Boss Presentation Banner).
     */
    public void render(Graphics2D g) {
        if (!active) return;

        int screenW = GameConfig.SCREEN_WIDTH;
        int screenH = GameConfig.SCREEN_HEIGHT;

        // 1. Dải viền đen điện ảnh Letterbox (trượt vào/ra mượt mà)
        int maxBarH = 68;
        double barProgress = 1.0;
        if (timer < 0.35) {
            barProgress = timer / 0.35;
        } else if (timer > TOTAL_DURATION - 0.35) {
            barProgress = Math.max(0.0, (TOTAL_DURATION - timer) / 0.35);
        }
        int curBarH = (int) (maxBarH * barProgress);

        g.setColor(new Color(5, 7, 10, 245));
        g.fillRect(0, 0, screenW, curBarH);
        g.fillRect(0, screenH - curBarH, screenW, curBarH);

        // Viền chỉ vàng/đỏ công nghệ ngăn cách
        g.setColor(new Color(255, 60, 60, 180));
        g.fillRect(0, curBarH - 2, screenW, 2);
        g.fillRect(0, screenH - curBarH, screenW, 2);

        // 2. Banner Cảnh Báo Nguy Hiểm & Nhãn Tên Boss (Hiển thị khi máy quay đã tới Boss)
        if (timer >= 0.45 && timer <= TOTAL_DURATION - 0.25) {
            double bannerProgress = Math.min(1.0, (timer - 0.45) / 0.3);
            int bannerAlpha = (int) (bannerProgress * 255);

            // Dải cảnh báo chớp nháy phía trên
            boolean flash = ((int) (timer * 6)) % 2 == 0;
            Color warnColor = flash ? new Color(255, 50, 50, bannerAlpha) : new Color(255, 180, 0, bannerAlpha);
            g.setColor(warnColor);
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            FontMetrics fmWarn = g.getFontMetrics();
            String warnMsg = "⚠ // CẢNH BÁO: TRÙM XUẤT HIỆN // ⚠";
            g.drawString(warnMsg, (screenW - fmWarn.stringWidth(warnMsg)) / 2, curBarH + 28);

            // Khung thẻ tên Boss hoành tráng ở phần dưới
            int cardW = Math.min(840, screenW - 80);
            int cardH = 110;
            int cardX = (screenW - cardW) / 2;
            int cardY = screenH - curBarH - cardH - 16;

            // Nền thẻ đen mờ trong suốt
            g.setColor(new Color(10, 14, 22, (int) (bannerAlpha * 0.88)));
            g.fillRoundRect(cardX, cardY, cardW, cardH, 12, 12);

            // Viền đỏ Neon
            g.setColor(new Color(255, 65, 65, (int) (bannerAlpha * 0.95)));
            g.setStroke(new BasicStroke(2.0f));
            g.drawRoundRect(cardX, cardY, cardW, cardH, 12, 12);
            g.setStroke(new BasicStroke(1.0f));

            // Tiêu đề Boss
            g.setFont(new Font("SansSerif", Font.BOLD, 25));
            FontMetrics fmTitle = g.getFontMetrics();
            int titleW = fmTitle.stringWidth(bossTitle);
            int titleX = cardX + (cardW - titleW) / 2;
            int titleY = cardY + 34;

            // Bóng chữ đen đậm
            g.setColor(new Color(0, 0, 0, bannerAlpha));
            g.drawString(bossTitle, titleX + 2, titleY + 2);

            // Chữ Neon đỏ cam rực lửa
            g.setColor(new Color(255, 75, 75, bannerAlpha));
            g.drawString(bossTitle, titleX, titleY);

            // Danh hiệu
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fmSub = g.getFontMetrics();
            int subW = fmSub.stringWidth(bossSubtitle);
            int subX = cardX + (cardW - subW) / 2;
            int subY = cardY + 60;
            g.setColor(new Color(255, 205, 80, (int) (bannerAlpha * 0.95)));
            g.drawString(bossSubtitle, subX, subY);

            // Chỉ số chiến thuật
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            FontMetrics fmThreat = g.getFontMetrics();
            int threatW = fmThreat.stringWidth(bossThreatInfo);
            int threatX = cardX + (cardW - threatW) / 2;
            int threatY = cardY + 88;
            g.setColor(new Color(185, 215, 245, (int) (bannerAlpha * 0.90)));
            g.drawString(bossThreatInfo, threatX, threatY);

            // Gợi ý bấm phím bỏ qua ở đáy
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.setColor(new Color(160, 180, 200, (int) (bannerAlpha * 0.75)));
            String skipTip = "[SPACE] HOẶC [CLICK CHUỘT] ĐỂ BỎ QUA GIỚI THIỆU";
            FontMetrics fmSkip = g.getFontMetrics();
            g.drawString(skipTip, (screenW - fmSkip.stringWidth(skipTip)) / 2, screenH - 12);
        }
    }
}
