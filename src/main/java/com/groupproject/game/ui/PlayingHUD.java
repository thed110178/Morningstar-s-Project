package com.groupproject.game.ui;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.dungeon.Room;
import com.groupproject.game.entity.enemy.TargetDummy;
import com.groupproject.game.entity.player.Player;
import com.groupproject.game.input.KeyBindings;
import com.groupproject.game.input.KeyBindings.GameAction;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;

/**
 * Hệ thống Giao diện Chiến thuật thời gian thực trong phòng chơi (In-Game Playing HUD System).
 *
 * <p>WHY: Bóc tách toàn bộ trách nhiệm hiển thị giao diện người dùng (HUD, Banners,
 * Boss Health Bar, Sector/Stage tag, Health bar, Credits badge, Low HP vignette, Extraction, Game Over)
 * ra khỏi God Class PlayingState, tuân thủ nguyên tắc Single Responsibility Principle (SRP).
 */
public class PlayingHUD {

    /**
     * Hiển thị thanh máu của chiến binh Operative trên giao diện HUD góc trên bên trái màn hình.
     * Có chỉ báo phần trăm, dải chuyển màu cảnh báo (Xanh ngọc -> Vàng hổ phách -> Đỏ nguy cấp).
     *
     * @param g      Đối tượng đồ họa Graphics2D.
     * @param player Thực thể người chơi.
     */
    public void renderPlayerHealthHUD(Graphics2D g, Player player) {
        if (player == null) return;

        int barX = 20;
        int barY = 60;
        int barW = 210;
        int barH = 22;

        // Khung nền xám đen viền kim loại công nghệ cao
        g.setColor(new Color(12, 16, 24, 230));
        g.fillRoundRect(barX, barY, barW, barH, 6, 6);
        g.setColor(new Color(55, 75, 105));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(barX, barY, barW, barH, 6, 6);
        g.setStroke(new BasicStroke(1.0f));

        // Thanh tiến trình máu
        int maxHp = Math.max(1, player.getMaxHealth());
        int curHp = Math.max(0, Math.min(maxHp, player.getCurrentHealth()));
        float hpPercent = (float) curHp / maxHp;
        int fillW = (int) ((barW - 4) * hpPercent);

        Color hpColor;
        if (curHp > 50) {
            hpColor = new Color(50, 220, 100); // Xanh ngọc tươi
        } else if (curHp > 25) {
            hpColor = new Color(255, 180, 40); // Vàng hổ phách cảnh báo
        } else {
            hpColor = new Color(255, 55, 55);  // Đỏ tươi nguy cấp
        }

        if (fillW > 0) {
            g.setColor(hpColor);
            g.fillRoundRect(barX + 2, barY + 2, fillW, barH - 4, 4, 4);

            // Dải highlight 3D mép trên
            g.setColor(new Color(255, 255, 255, 80));
            g.fillRect(barX + 2, barY + 2, fillW, 3);
        }

        // Chữ số hiển thị máu rõ nét ở giữa thanh
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        String hpText = "✚ HP: " + curHp + " / " + maxHp;
        int textW = fm.stringWidth(hpText);
        int textX = barX + (barW - textW) / 2;
        int textY = barY + 16;

        // Shadow chữ tăng độ tương phản
        g.setColor(new Color(0, 0, 0, 220));
        g.drawString(hpText, textX + 1, textY + 1);
        g.setColor(Color.WHITE);
        g.drawString(hpText, textX, textY);
    }

    /**
     * Hiển thị thanh trạng thái hồi chiêu kỹ năng né đòn [SPACE] (Dodge Roll / Combat Dash HUD)
     * ngay dưới thanh máu của người chơi Operative.
     *
     * @param g      Đối tượng đồ họa Graphics2D.
     * @param player Thực thể người chơi.
     */
    public void renderDodgeHUD(Graphics2D g, Player player) {
        if (player == null) return;

        int barX = 20;
        int barY = 88;
        int barW = 210;
        int barH = 15;

        // Khung nền xám tối công nghệ cao
        g.setColor(new Color(12, 16, 24, 220));
        g.fillRoundRect(barX, barY, barW, barH, 4, 4);
        g.setColor(new Color(45, 65, 95));
        g.setStroke(new BasicStroke(1.0f));
        g.drawRoundRect(barX, barY, barW, barH, 4, 4);

        boolean isDodging = player.isDodging();
        boolean canDodge = player.canDodge();
        double cooldown = player.getDodgeCooldownTimer();
        double progress = player.getDodgeProgress();

        int fillW = (int) ((barW - 4) * Math.max(0.0, Math.min(1.0, progress)));

        if (isDodging) {
            // Khi đang né: Xung điện sáng rực báo hiệu đang kích hoạt i-frames
            g.setColor(new Color(90, 240, 255));
            g.fillRoundRect(barX + 2, barY + 2, barW - 4, barH - 4, 3, 3);
        } else if (fillW > 0) {
            Color dodgeColor = canDodge ? new Color(0, 195, 235) : new Color(0, 120, 160);
            g.setColor(dodgeColor);
            g.fillRoundRect(barX + 2, barY + 2, fillW, barH - 4, 3, 3);

            // Highlight mép trên
            g.setColor(new Color(255, 255, 255, canDodge ? 90 : 40));
            g.fillRect(barX + 2, barY + 2, fillW, 2);
        }

        // Chữ trạng thái
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        String dodgeKey = KeyBindings.getKeyName(KeyBindings.getKeyCode(GameAction.DODGE));
        String dodgeText;
        Color textColor;

        if (isDodging) {
            dodgeText = "⚡ [" + dodgeKey + "] ĐANG NÉ (i-frames)";
            textColor = new Color(10, 25, 40);
        } else if (canDodge) {
            dodgeText = "[" + dodgeKey + "] NÉ ĐÒN : SẴN SÀNG";
            textColor = Color.WHITE;
        } else {
            dodgeText = String.format("[%s] HỒI CHIÊU: %.1fs", dodgeKey, cooldown);
            textColor = new Color(200, 225, 245);
        }

        int textW = fm.stringWidth(dodgeText);
        int textX = barX + (barW - textW) / 2;
        int textY = barY + 11;

        if (!isDodging) {
            g.setColor(new Color(0, 0, 0, 200));
            g.drawString(dodgeText, textX + 1, textY + 1);
        }
        g.setColor(textColor);
        g.drawString(dodgeText, textX, textY);
    }

    /**
     * Hiển thị bảng HUD số dư tiền tệ Credits ở góc trên bên phải màn hình.
     *
     * @param g             Đối tượng đồ họa Graphics2D.
     * @param playerCredits Số dư tiền tệ tác chiến hiện tại.
     */
    public void renderCreditsHUD(Graphics2D g, int playerCredits) {
        int boxW = 160;
        int boxH = 36;
        int boxX = GameConfig.SCREEN_WIDTH - boxW - 20;
        int boxY = 20;

        g.setColor(new Color(15, 20, 30, 210));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setColor(new Color(255, 215, 60, 200));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        // Icon đồng tiền vàng
        g.setColor(new Color(255, 215, 60));
        g.fillOval(boxX + 10, boxY + 8, 20, 20);
        g.setColor(new Color(180, 140, 20));
        g.drawOval(boxX + 10, boxY + 8, 20, 20);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(new Color(30, 20, 5));
        g.drawString("C", boxX + 16, boxY + 23);

        // Chữ số Credits
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.setColor(new Color(255, 230, 90));
        g.drawString(playerCredits + " CREDITS", boxX + 38, boxY + 24);
    }

    /**
     * Hiển thị nhãn Stage tinh gọn ở góc trên bên trái: "Stage 1|[số stage]".
     *
     * @param g                Đối tượng đồ họa Graphics2D.
     * @param currentStage     Số thứ tự stage hiện tại.
     * @param activeCombatRoom Phòng chiến đấu hiện tại đang kích hoạt (nếu có).
     */
    public void renderSectorHUD(Graphics2D g, int currentStage, Room activeCombatRoom) {
        int boxX = 20;
        int boxY = 20;
        int boxW = 125;
        int boxH = 34;

        boolean isLockedCombat = (activeCombatRoom != null && activeCombatRoom.isLocked());

        // Khung nền xám đen viền công nghệ cao
        g.setColor(new Color(15, 20, 30, 225));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setColor(isLockedCombat ? new Color(255, 75, 75, 180) : new Color(60, 80, 110));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setStroke(new BasicStroke(1.0f));

        // Text Stage: "Stage 1|1", "Stage 1|2", "Stage 1|3"...
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        String stageText = String.format("Stage 1|%d", currentStage);
        FontMetrics fm = g.getFontMetrics();
        int textX = boxX + (boxW - fm.stringWidth(stageText)) / 2;
        int textY = boxY + ((boxH - fm.getHeight()) / 2) + fm.getAscent();

        g.setColor(isLockedCombat ? new Color(255, 90, 90) : new Color(255, 205, 55));
        g.drawString(stageText, textX, textY);
    }

    /**
     * Hiển thị thanh máu Boss Titan hùng hậu ở đỉnh màn hình:
     * - Tiêu đề vàng, khung nền đen bo góc viền vàng.
     * - Thanh tiến trình gradient đỏ thẫm đến cam lửa kèm dải highlight 3D.
     * - Các vạch chia 20% (Ticks).
     * - Chỉ báo số lượng lính hộ vệ hoặc bộ đếm ngược tiếp viện.
     *
     * @param g                      Đối tượng đồ họa Graphics2D.
     * @param bossDummy              Thực thể Boss Dummy.
     * @param activeMinions          Số lượng quái hộ tống còn sống.
     * @param bossMinionRespawnTimer Thời gian đếm ngược tiếp viện (giây).
     */
    public void renderBossHealthBar(Graphics2D g, TargetDummy bossDummy, long activeMinions, double bossMinionRespawnTimer) {
        if (bossDummy == null || !bossDummy.isAlive()) return;

        int barW = 520;
        int barH = 22;
        int barX = (GameConfig.SCREEN_WIDTH - barW) / 2;
        int barY = 44;

        // Container card frame behind the Boss Bar
        int cardPadX = 18;
        int cardPadY = 26;
        int cardX = barX - cardPadX;
        int cardY = barY - cardPadY;
        int cardW = barW + cardPadX * 2;
        int cardH = barH + cardPadY + 24;

        // Dark sci-fi backplate with gold trim
        g.setColor(new Color(12, 14, 22, 235));
        g.fillRoundRect(cardX, cardY, cardW, cardH, 12, 12);
        g.setColor(new Color(212, 160, 23, 200));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(cardX, cardY, cardW, cardH, 12, 12);
        g.setStroke(new BasicStroke(1.0f));

        // Decorative corner cyber notches
        g.setColor(new Color(255, 215, 60));
        g.fillRect(cardX + 2, cardY + 2, 8, 2);
        g.fillRect(cardX + 2, cardY + 2, 2, 8);
        g.fillRect(cardX + cardW - 10, cardY + 2, 8, 2);
        g.fillRect(cardX + cardW - 4, cardY + 2, 2, 8);

        // Header Title: Boss Name & Hazard Skull
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        String title = "☠ " + bossDummy.getName().toUpperCase() + " ☠";
        FontMetrics fm = g.getFontMetrics();
        int titleW = fm.stringWidth(title);
        g.setColor(new Color(255, 220, 80));
        g.drawString(title, (GameConfig.SCREEN_WIDTH - titleW) / 2, barY - 8);

        // Bar Track Background
        g.setColor(new Color(30, 25, 32));
        g.fillRoundRect(barX, barY, barW, barH, 6, 6);

        // Bar Fill (Gradient Crimson to Fiery Orange)
        float hpPercent = Math.max(0f, (float) bossDummy.getCurrentHealth() / bossDummy.getMaxHealth());
        int fillW = (int) (barW * hpPercent);

        if (fillW > 0) {
            GradientPaint hpGrad = new GradientPaint(
                    barX, barY, new Color(255, 55, 55),
                    barX + barW, barY, new Color(190, 15, 25)
            );
            Paint oldPaint = g.getPaint();
            g.setPaint(hpGrad);
            g.fillRoundRect(barX, barY, fillW, barH, 6, 6);
            g.setPaint(oldPaint);

            // Top highlight line for 3D metallic feel
            g.setColor(new Color(255, 140, 140, 180));
            g.fillRect(barX + 2, barY + 2, Math.max(0, fillW - 4), 3);
        }

        // Inner Segment Tick Marks (every 20%: 20%, 40%, 60%, 80%)
        g.setColor(new Color(20, 20, 28, 180));
        for (int i = 1; i <= 4; i++) {
            int tickX = barX + (barW * i) / 5;
            g.fillRect(tickX - 1, barY, 2, barH);
        }

        // Outer border
        g.setColor(new Color(255, 215, 60, 220));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(barX, barY, barW, barH, 6, 6);
        g.setStroke(new BasicStroke(1.0f));

        // Exact Health Text centered on the bar
        int percentInt = (int) (hpPercent * 100);
        String hpText = String.format("%d / %d HP (%d%%)", bossDummy.getCurrentHealth(), bossDummy.getMaxHealth(), percentInt);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics hpfm = g.getFontMetrics();
        int hpTextW = hpfm.stringWidth(hpText);
        int hpTextX = (GameConfig.SCREEN_WIDTH - hpTextW) / 2;
        int hpTextY = barY + 16;

        // Shadow text for high contrast readability
        g.setColor(Color.BLACK);
        g.drawString(hpText, hpTextX + 1, hpTextY + 1);
        g.setColor(Color.WHITE);
        g.drawString(hpText, hpTextX, hpTextY);

        // Minion Guardian Status / Reinforcement Countdown indicator
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics statusFm = g.getFontMetrics();
        String statusText;
        Color statusColor;

        if (activeMinions > 0) {
            statusText = "HỘ VỆ: " + activeMinions;
            statusColor = new Color(175, 195, 220, 210);
        } else {
            statusText = String.format("TIẾP VIỆN: %.0fs", Math.max(0.0, bossMinionRespawnTimer));
            if (bossMinionRespawnTimer <= 3.0) {
                statusColor = new Color(255, 80, 80, 240);
            } else if (bossMinionRespawnTimer <= 7.0) {
                statusColor = new Color(255, 180, 50, 230);
            } else {
                statusColor = new Color(255, 220, 90, 210);
            }
        }

        int statusW = statusFm.stringWidth(statusText);
        int statusX = (GameConfig.SCREEN_WIDTH - statusW) / 2;
        int statusY = barY + barH + 16;

        g.setColor(Color.BLACK);
        g.drawString(statusText, statusX + 1, statusY + 1);
        g.setColor(statusColor);
        g.drawString(statusText, statusX, statusY);
    }

    /**
     * Hiển thị biểu ngữ thông báo nổi (Tactical Announcement Banner).
     *
     * @param g             Đối tượng đồ họa Graphics2D.
     * @param bannerMessage Nội dung thông báo.
     * @param bannerColor   Màu sắc thông báo.
     * @param hasBoss       Cờ xác định có Boss đang sống trên màn hình (để điều chỉnh tọa độ Y tránh đè thanh máu Boss).
     */
    public void renderBanner(Graphics2D g, String bannerMessage, Color bannerColor, boolean hasBoss) {
        if (bannerMessage == null || bannerMessage.isEmpty()) return;

        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(bannerMessage);
        int pad = 18;

        int boxW = textW + pad * 2;
        int boxH = 36;
        int boxX = (GameConfig.SCREEN_WIDTH - boxW) / 2;
        int boxY = hasBoss ? 96 : 115;

        g.setColor(new Color(15, 18, 28, 215));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setColor(bannerColor);
        g.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        g.setColor(bannerColor);
        g.drawString(bannerMessage, boxX + pad, boxY + 23);
    }

    /**
     * Hiển thị gợi ý tương tác sơ tán / chuyển Stage [F] khi đứng trên bệ đáp trực thăng Helipad.
     *
     * @param g            Đối tượng đồ họa Graphics2D.
     * @param screenPX     Tọa độ màn hình X của người chơi.
     * @param screenPY     Tọa độ màn hình Y của người chơi.
     * @param currentStage Stage hiện tại.
     */
    public void renderExtractPrompt(Graphics2D g, int screenPX, int screenPY, int currentStage) {
        String interactKey = KeyBindings.getKeyName(KeyBindings.getKeyCode(GameAction.INTERACT));
        String actionPrompt;
        if (currentStage == 1) {
            actionPrompt = "LÊN TRỰC THĂNG TIẾN VÀO SECTOR 2";
        } else if (currentStage == 2) {
            actionPrompt = "LÊN TRỰC THĂNG TIẾN VÀO SECTOR 3";
        } else if (currentStage == 3) {
            actionPrompt = "LÊN TRỰC THĂNG TIẾN VÀO VÙNG CHIẾN SỰ TRÙM (SECTOR 4)";
        } else {
            actionPrompt = "LÊN TRỰC THĂNG SƠ TÁN (HOÀN THÀNH CHIẾN DỊCH)";
        }
        String fullPrompt = "[" + interactKey + "] " + actionPrompt;

        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(fullPrompt);
        int pad = 12;

        int boxW = textW + pad * 2;
        int boxH = 32;
        int boxX = screenPX - boxW / 2;
        int boxY = screenPY - 50;

        // Background box with glowing border
        g.setColor(new Color(22, 26, 24, 235));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setColor(new Color(235, 190, 35)); // Helipad amber gold
        g.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        // Keycap indicator
        String keycap = "[" + interactKey + "]";
        g.setColor(new Color(255, 220, 60));
        g.drawString(keycap, boxX + pad, boxY + 21);

        g.setColor(Color.WHITE);
        g.drawString(actionPrompt, boxX + pad + fm.stringWidth(keycap + " "), boxY + 21);
    }

    /**
     * Hiển thị màn hình đen tổng kết chiến dịch hoàn thành (Extraction Victory Screen).
     *
     * @param g Đối tượng đồ họa Graphics2D.
     */
    public void renderExtractionVictoryScreen(Graphics2D g) {
        int width = GameConfig.SCREEN_WIDTH;
        int height = GameConfig.SCREEN_HEIGHT;

        // 1. Solid Black Screen
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        // 2. Cyber grid decorative border
        g.setColor(new Color(30, 45, 60));
        g.drawRect(40, 40, width - 80, height - 80);
        g.drawRect(44, 44, width - 88, height - 88);

        // Corner accents
        g.setColor(new Color(60, 220, 160));
        int cSize = 24;
        g.drawLine(40, 40, 40 + cSize, 40);
        g.drawLine(40, 40, 40, 40 + cSize);
        g.drawLine(width - 40, 40, width - 40 - cSize, 40);
        g.drawLine(width - 40, 40, width - 40, 40 + cSize);
        g.drawLine(40, height - 40, 40 + cSize, height - 40);
        g.drawLine(40, height - 40, 40, height - 40 - cSize);
        g.drawLine(width - 40, height - 40, width - 40 - cSize, height - 40);
        g.drawLine(width - 40, height - 40, width - 40, height - 40 - cSize);

        // 3. Victory Title: ★ BIOME 1 HOÀN THÀNH ★
        g.setFont(new Font("SansSerif", Font.BOLD, 42));
        FontMetrics fmTitle = g.getFontMetrics();
        String title = "★ BIOME 1 HOÀN THÀNH ★";
        int titleX = (width - fmTitle.stringWidth(title)) / 2;
        int titleY = 210;

        g.setColor(new Color(20, 60, 40));
        g.drawString(title, titleX + 2, titleY + 2);
        g.setColor(new Color(60, 240, 140));
        g.drawString(title, titleX, titleY);

        // Subtitle
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics fmSub = g.getFontMetrics();
        String subtitle = "CHIẾN DỊCH TOÀN THẮNG - SƠ TÁN THÀNH CÔNG";
        int subX = (width - fmSub.stringWidth(subtitle)) / 2;
        g.setColor(new Color(255, 215, 60));
        g.drawString(subtitle, subX, 260);

        // 4. Mission Debriefing Tactical Card
        int cardW = 660;
        int cardH = 180;
        int cardX = (width - cardW) / 2;
        int cardY = 310;
        g.setColor(new Color(15, 20, 30, 220));
        g.fillRoundRect(cardX, cardY, cardW, cardH, 12, 12);
        g.setColor(new Color(60, 80, 110));
        g.drawRoundRect(cardX, cardY, cardW, cardH, 12, 12);

        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.setColor(new Color(200, 220, 245));
        g.drawString("• Khu vực: Biome 1 (Alpha Sector) - Hoàn thành xuất sắc 3/3 Stages", cardX + 30, cardY + 45);
        g.drawString("• Stage 1 & 2: Dọn sạch toàn bộ các phòng chiến đấu liên kết", cardX + 30, cardY + 80);
        g.drawString("• Stage 3: Chinh phục Grand Boss Arena & kích hoạt sơ tán", cardX + 30, cardY + 115);
        g.drawString("• Trang bị & Balo: Giữ nguyên toàn bộ vũ khí và chiến lợi phẩm", cardX + 30, cardY + 150);

        // 5. User prompt at bottom
        g.setFont(new Font("SansSerif", Font.ITALIC, 16));
        FontMetrics fmPrompt = g.getFontMetrics();
        String prompt = "[ Nhấn ENTER hoặc F để quay về Menu ngay ]";
        int promptX = (width - fmPrompt.stringWidth(prompt)) / 2;
        g.setColor(new Color(160, 180, 210));
        g.drawString(prompt, promptX, 550);
    }

    /**
     * Hiển thị hiệu ứng viền đỏ nhấp nháy quanh 4 cạnh màn hình khi máu xuống mức nguy cấp (<= 30%).
     *
     * @param g Đối tượng đồ họa Graphics2D.
     */
    public void renderLowHealthVignette(Graphics2D g) {
        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.008) * 0.4 + 0.6);
        int alpha = (int) (65 * pulse);
        g.setColor(new Color(255, 0, 0, alpha));
        int thickness = 32;
        g.fillRect(0, 0, GameConfig.SCREEN_WIDTH, thickness);
        g.fillRect(0, GameConfig.SCREEN_HEIGHT - thickness, GameConfig.SCREEN_WIDTH, thickness);
        g.fillRect(0, 0, thickness, GameConfig.SCREEN_HEIGHT);
        g.fillRect(GameConfig.SCREEN_WIDTH - thickness, 0, thickness, GameConfig.SCREEN_HEIGHT);
    }

    /**
     * Hiển thị màn hình Game Over khi chiến binh tử trận (HP = 0).
     *
     * @param g                Đối tượng đồ họa Graphics2D.
     * @param currentStage     Stage hiện tại.
     * @param maxStages        Tổng số stage của Biome.
     * @param playerCredits    Số dư credits đã thu thập.
     * @param clearedRoomCount Số phòng chiến đấu đã dọn sạch.
     * @param weaponSlotsCount Số vũ khí trong kho đồ.
     */
    public void renderGameOverScreen(Graphics2D g, int currentStage, int maxStages, int playerCredits, long clearedRoomCount, int weaponSlotsCount) {
        // Phủ màn mờ đỏ đen kịch tính
        g.setColor(new Color(15, 5, 8, 235));
        g.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        int cx = GameConfig.SCREEN_WIDTH / 2;

        // Biểu tượng & Tiêu đề Tử trận
        g.setFont(new Font("SansSerif", Font.BOLD, 36));
        FontMetrics fm = g.getFontMetrics();
        String title = "☠ CHIẾN BINH TỬ TRẬN ☠";
        int titleW = fm.stringWidth(title);
        g.setColor(new Color(255, 60, 60));
        g.drawString(title, cx - titleW / 2, 220);

        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        fm = g.getFontMetrics();
        String sub = "OPERATIVE KIA - TÍN HIỆU SINH TỒN ĐÃ MẤT";
        g.setColor(new Color(255, 170, 170));
        g.drawString(sub, cx - fm.stringWidth(sub) / 2, 260);

        // Bảng thống kê chiến dịch
        int cardW = 440;
        int cardH = 150;
        int cardX = cx - cardW / 2;
        int cardY = 300;

        g.setColor(new Color(255, 80, 80));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(cardX, cardY, cardW, cardH, 10, 10);
        g.setStroke(new BasicStroke(1.0f));

        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(new Color(220, 225, 235));
        g.drawString("• Giai đoạn chiến dịch: Stage " + currentStage + " / " + maxStages, cardX + 30, cardY + 40);
        g.drawString("• Ngân sách thu thập: " + playerCredits + " CREDITS", cardX + 30, cardY + 70);
        g.drawString("• Tiến độ dọn phòng: " + clearedRoomCount + " phòng combat", cardX + 30, cardY + 100);
        g.drawString("• Trang bị hiện có: " + weaponSlotsCount + " vũ khí trong kho", cardX + 30, cardY + 130);

        // Hướng dẫn tương tác
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        fm = g.getFontMetrics();
        String prompt = "Bấm [ENTER] hoặc [R] để tái xuất phát | [ESC] Về Menu chính";
        g.setColor(new Color(255, 225, 75));
        g.drawString(prompt, cx - fm.stringWidth(prompt) / 2, 510);
    }
}
