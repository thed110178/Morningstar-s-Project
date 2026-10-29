package com.groupproject.game.ui;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.core.GameFontManager;
import com.groupproject.game.dungeon.Room;
import com.groupproject.game.entity.enemy.TargetDummy;
import com.groupproject.game.entity.player.Player;
import com.groupproject.game.input.KeyBindings;
import com.groupproject.game.input.KeyBindings.GameAction;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;

/**
 * Hệ thống Giao diện Chiến thuật thời gian thực trong phòng chơi (In-Game
 * Playing HUD System).
 *
 * <p>
 * WHY: Bóc tách toàn bộ trách nhiệm hiển thị giao diện người dùng (HUD,
 * Banners,
 * Boss Health Bar, Sector/Stage tag, Health bar, Credits badge, Low HP
 * vignette, Extraction, Game Over)
 * ra khỏi God Class PlayingState, tuân thủ nguyên tắc Single Responsibility
 * Principle (SRP).
 */
public class PlayingHUD {

    /**
     * Hiển thị thanh máu của chiến binh Operative trên giao diện HUD góc trên bên
     * trái màn hình.
     * Có chỉ báo phần trăm, dải chuyển màu cảnh báo (Xanh ngọc -> Vàng hổ phách ->
     * Đỏ nguy cấp).
     *
     * @param g      Đối tượng đồ họa Graphics2D.
     * @param player Thực thể người chơi.
     */
    public void renderPlayerHealthHUD(Graphics2D g, Player player) {
        if (player == null)
            return;

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
            hpColor = new Color(255, 55, 55); // Đỏ tươi nguy cấp
        }

        if (fillW > 0) {
            g.setColor(hpColor);
            g.fillRoundRect(barX + 2, barY + 2, fillW, barH - 4, 4, 4);

            // Dải highlight 3D mép trên
            g.setColor(new Color(255, 255, 255, 80));
            g.fillRect(barX + 2, barY + 2, fillW, 3);
        }

        // Chữ số hiển thị máu rõ nét ở giữa thanh
        g.setFont(GameFontManager.getBoldFont(12.0f));
        FontMetrics fm = g.getFontMetrics();
        String hpText = "+ HP: " + curHp + " / " + maxHp;
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
     * Hiển thị thanh trạng thái hồi chiêu kỹ năng né đòn [SPACE] (Dodge Roll /
     * Combat Dash HUD)
     * ngay dưới thanh máu của người chơi Operative.
     *
     * @param g      Đối tượng đồ họa Graphics2D.
     * @param player Thực thể người chơi.
     */
    public void renderDodgeHUD(Graphics2D g, Player player) {
        if (player == null)
            return;

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
        g.setFont(GameFontManager.getSmallBoldFont(10.0f));
        FontMetrics fm = g.getFontMetrics();
        String dodgeKey = KeyBindings.getKeyName(KeyBindings.getKeyCode(GameAction.DODGE));
        String dodgeText;
        Color textColor;

        if (isDodging) {
            dodgeText = "! [" + dodgeKey + "] ĐANG NÉ (i-frames)";
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
     * Hiển thị trạng thái Khiên Chặn Đòn (Aegis Barrier) và các Huy hiệu Buff đang
     * kích hoạt trên HUD.
     */
    public void renderBuffsHUD(Graphics2D g, Player player) {
        if (player == null || player.getBuffManager() == null)
            return;
        com.groupproject.game.entity.buff.PlayerBuffManager bm = player.getBuffManager();

        int startX = 20;
        int startY = 108;
        int barW = 210;

        // 1. Nếu có Khiên Chặn Đòn (ONE_HIT_BARRIER)
        if (bm.hasOneHitBarrier()) {
            int barH = 15;
            g.setColor(new Color(12, 16, 24, 220));
            g.fillRoundRect(startX, startY, barW, barH, 4, 4);
            g.setColor(new Color(65, 55, 30));
            g.setStroke(new BasicStroke(1.0f));
            g.drawRoundRect(startX, startY, barW, barH, 4, 4);

            boolean ready = bm.isBarrierReady();
            double cd = bm.getBarrierCooldownTimer();
            if (ready) {
                g.setColor(new Color(255, 185, 40));
                g.fillRoundRect(startX + 2, startY + 2, barW - 4, barH - 4, 3, 3);
                g.setColor(new Color(255, 255, 255, 90));
                g.fillRect(startX + 2, startY + 2, barW - 4, 2);
            } else {
                double progress = 1.0 - (cd / com.groupproject.game.entity.buff.PlayerBuffManager.BARRIER_COOLDOWN);
                int fillW = (int) ((barW - 4) * Math.max(0.0, Math.min(1.0, progress)));
                if (fillW > 0) {
                    g.setColor(new Color(180, 110, 20));
                    g.fillRoundRect(startX + 2, startY + 2, fillW, barH - 4, 3, 3);
                }
            }

            g.setFont(GameFontManager.getSmallBoldFont(10.0f));
            FontMetrics fm = g.getFontMetrics();
            String barrierText = ready ? "[KHIÊN] SẴN SÀNG CHẶN ĐÒN" : String.format("[KHIÊN] HỒI PHỤC: %.1fs", cd);
            int textW = fm.stringWidth(barrierText);
            int textX = startX + (barW - textW) / 2;
            int textY = startY + 11;

            if (ready) {
                g.setColor(new Color(10, 10, 10));
            } else {
                g.setColor(new Color(245, 220, 180));
            }
            g.drawString(barrierText, textX, textY);
            startY += 20;
        }

        // 2. Mini badge các buff chỉ số kích hoạt
        java.util.List<String> activeBadges = new java.util.ArrayList<>();
        if (bm.getAttackMultiplier() > 1.0) {
            activeBadges.add(String.format("ATK +%d%%", (int) Math.round((bm.getAttackMultiplier() - 1.0) * 100)));
        }
        if (bm.getDamageReduction() > 0) {
            activeBadges.add(String.format("DEF +%d%%", (int) Math.round(bm.getDamageReduction() * 100)));
        }
        if (bm.getSpeedMultiplier() > 1.0) {
            activeBadges.add(String.format("SPD +%d%%", (int) Math.round((bm.getSpeedMultiplier() - 1.0) * 100)));
        }
        if (bm.hasCriticalMastery()) {
            activeBadges.add("CRIT 15%");
        }
        if (bm.hasShrinkHitbox()) {
            activeBadges.add("-30% BOX");
        }
        if (bm.hasPhaseDodge()) {
            activeBadges.add("NÉ XUYÊN");
        }

        if (!activeBadges.isEmpty()) {
            g.setFont(GameFontManager.getSmallBoldFont(10.0f));
            FontMetrics fm = g.getFontMetrics();
            int curX = startX;
            for (String badge : activeBadges) {
                int bW = fm.stringWidth(badge) + 8;
                if (curX + bW > startX + barW + 80)
                    break;
                g.setColor(new Color(15, 25, 38, 200));
                g.fillRoundRect(curX, startY, bW, 16, 4, 4);
                g.setColor(new Color(50, 160, 220, 180));
                g.drawRoundRect(curX, startY, bW, 16, 4, 4);
                g.setColor(new Color(180, 225, 255));
                g.drawString(badge, curX + 4, startY + 12);
                curX += bW + 5;
            }
        }
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
        g.setFont(GameFontManager.getBoldFont(12.0f));
        g.setColor(new Color(30, 20, 5));
        g.drawString("C", boxX + 16, boxY + 23);

        // Chữ số Credits
        g.setFont(GameFontManager.getBoldFont(15.0f));
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
        g.setFont(GameFontManager.getBoldFont(15.0f));
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
    public void renderBossHealthBar(Graphics2D g, TargetDummy bossDummy, long activeMinions,
            double bossMinionRespawnTimer) {
        if (bossDummy == null || !bossDummy.isAlive())
            return;

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

        // Header Title: Boss Name & Hazard Icon
        g.setFont(GameFontManager.getBoldFont(14.0f));
        String title = "! " + bossDummy.getName().toUpperCase() + " !";
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
                    barX + barW, barY, new Color(190, 15, 25));
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
        String hpText = String.format("%d / %d HP (%d%%)", bossDummy.getCurrentHealth(), bossDummy.getMaxHealth(),
                percentInt);
        g.setFont(GameFontManager.getBoldFont(12.0f));
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
        g.setFont(GameFontManager.getBoldFont(11.0f));
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
     * @param hasBoss       Cờ xác định có Boss đang sống trên màn hình (để điều
     *                      chỉnh tọa độ Y tránh đè thanh máu Boss).
     */
    public void renderBanner(Graphics2D g, String bannerMessage, Color bannerColor, boolean hasBoss) {
        if (bannerMessage == null || bannerMessage.isEmpty())
            return;

        String cleanMessage = GameFontManager.sanitize(bannerMessage);
        boolean isShortAction = "FIGHT!".equals(cleanMessage) || "CLEAR!".equals(cleanMessage);
        g.setFont(GameFontManager.getBoldFont(isShortAction ? 22.0f : 15.0f));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(cleanMessage);
        int pad = isShortAction ? 32 : 18;

        int boxW = textW + pad * 2;
        int boxH = isShortAction ? 44 : 36;
        int boxX = (GameConfig.SCREEN_WIDTH - boxW) / 2;
        int boxY = hasBoss ? 96 : 115;

        g.setColor(new Color(15, 18, 28, 230));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setColor(bannerColor);
        g.setStroke(new BasicStroke(isShortAction ? 2.0f : 1.0f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setStroke(new BasicStroke(1.0f));

        g.setColor(bannerColor);
        int textY = boxY + ((boxH - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(cleanMessage, boxX + (boxW - textW) / 2, textY);
    }

    /**
     * Hiển thị gợi ý tương tác sơ tán / chuyển Stage [F] khi đứng trên bệ đáp trực
     * thăng Helipad.
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

        g.setFont(GameFontManager.getBoldFont(13.0f));
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
     * Hiển thị màn hình đen tổng kết chiến dịch hoàn thành (Extraction Victory
     * Screen).
     *
     * @param g Đối tượng đồ họa Graphics2D.
     */
    /**
     * Hiển thị màn hình đen tổng kết chiến dịch hoàn thành (Extraction Victory Screen).
     *
     * @param g                     Đối tượng đồ họa Graphics2D.
     * @param isStandaloneBossFight true nếu đang chơi ở chế độ Boss Fight độc lập từ Menu; false nếu ở Campaign vượt ải.
     */
    public void renderExtractionVictoryScreen(Graphics2D g, boolean isStandaloneBossFight) {
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

        // 3. Victory Title
        g.setFont(GameFontManager.getBoldFont(32.0f));
        FontMetrics fmTitle = g.getFontMetrics();
        String title = isStandaloneBossFight
                ? "* CHIẾN DỊCH HẠ BOSS HOÀN THÀNH *"
                : "* CẢM ƠN VÌ ĐÃ CHƠI DEMO! *";
        int titleX = (width - fmTitle.stringWidth(title)) / 2;
        int titleY = 210;

        g.setColor(new Color(20, 60, 40));
        g.drawString(title, titleX + 2, titleY + 2);
        g.setColor(new Color(60, 240, 140));
        g.drawString(title, titleX, titleY);

        // Subtitle
        g.setFont(GameFontManager.getBoldFont(18.0f));
        FontMetrics fmSub = g.getFontMetrics();
        String subtitle = isStandaloneBossFight
                ? "ĐÃ TIÊU DIỆT CHIẾN XA TITAN MK-IV"
                : "CHIẾN DỊCH TOÀN THẮNG — TIÊU DIỆT BOSS & SƠ TÁN THÀNH CÔNG";
        int subX = (width - fmSub.stringWidth(subtitle)) / 2;
        g.setColor(new Color(255, 215, 60));
        g.drawString(subtitle, subX, 260);

        // 4. Mission Debriefing Tactical Card
        int cardW = 680;
        int cardH = 180;
        int cardX = (width - cardW) / 2;
        int cardY = 310;
        g.setColor(new Color(15, 20, 30, 220));
        g.fillRoundRect(cardX, cardY, cardW, cardH, 12, 12);
        g.setColor(new Color(60, 80, 110));
        g.drawRoundRect(cardX, cardY, cardW, cardH, 12, 12);

        g.setFont(GameFontManager.getSmallFont(12.0f));
        g.setColor(new Color(200, 220, 245));
        if (isStandaloneBossFight) {
            g.drawString("- Chế độ tác chiến: Thử thách độc lập Boss Fight (Titan Battle Tank)", cardX + 30, cardY + 45);
            g.drawString("- Mục tiêu: Khắc chế hỏa lực đại pháo & súng máy phòng không", cardX + 30, cardY + 80);
            g.drawString("- Trạng thái: Mục tiêu cơ giới Titan đã bị vô hiệu hóa hoàn toàn", cardX + 30, cardY + 115);
            g.drawString("- Dữ liệu tác chiến đã được ghi nhận vào hệ thống", cardX + 30, cardY + 150);
        } else {
            g.drawString("- Khu vực: Biome 1 (Alpha Sector) - Hoàn thành xuất sắc toàn bộ Chiến dịch", cardX + 30, cardY + 45);
            g.drawString("- Chiến tích: Tiêu diệt thành công Boss Chiến Xa Titan MK-IV và mở đường sơ tán", cardX + 30, cardY + 80);
            g.drawString("- Bản Demo hoàn tất: Bạn có thể tiếp tục rèn luyện vũ khí & thử nghiệm tại The Range", cardX + 30, cardY + 115);
            g.drawString("- Hãy tiếp tục cải thiện kỹ năng và đón chờ các bản cập nhật tiếp theo!", cardX + 30, cardY + 150);
        }

        // 5. User prompt at bottom
        g.setFont(GameFontManager.getSmallBoldFont(12.0f));
        FontMetrics fmPrompt = g.getFontMetrics();
        String prompt = isStandaloneBossFight
                ? "[ Bấm nút bất kỳ hoặc Click chuột để quay về Menu chính ]"
                : "[ Bấm nút bất kỳ hoặc Click chuột để tiếp tục quay về The Range ]";
        int promptX = (width - fmPrompt.stringWidth(prompt)) / 2;
        g.setColor(new Color(255, 230, 120));
        g.drawString(prompt, promptX, 550);
    }

    public void renderExtractionVictoryScreen(Graphics2D g) {
        renderExtractionVictoryScreen(g, false);
    }

    /**
     * Hiển thị hiệu ứng viền đỏ nhấp nháy quanh 4 cạnh màn hình khi máu xuống mức
     * nguy cấp (<= 30%).
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
     * Hiển thị hiệu ứng chuyển cảnh điện ảnh tử trận (Cinematic Death Vignette &
     * Fade).
     * Khi chiến binh ngã xuống biến thành đầu lâu trượt trên sàn, màn hình từ từ
     * phủ bóng tối
     * và viền đỏ bi tráng, không che mất hoạt cảnh đầu lâu trượt ma sát.
     *
     * @param g        Đối tượng đồ họa Graphics2D.
     * @param progress Tiến độ tử trận từ 0.0 (vừa chết) đến 1.0 (hoàn tất trượt).
     */
    public void renderDeathVignetteTransition(Graphics2D g, double progress) {
        int w = GameConfig.SCREEN_WIDTH;
        int h = GameConfig.SCREEN_HEIGHT;

        // Chuỗi tử trận điện ảnh gồm 3 giai đoạn:
        // 1. Chết & Màn đen dần (0.0 -> 0.28): Màn hình mờ tối dần từ 0 đến đen kịt 100% (alpha 255)
        // 2. "BẠN ĐÃ BỊ HẠ" (0.28 -> 0.70): Nền đen 100%, chữ hiện lên rõ nét (fade in 0.28->0.42, giữ sáng 0.42->0.70)
        // 3. Lại đen dần (0.70 -> 1.00): Chữ mờ dần vào bóng tối hoàn toàn (fade out 0.70->1.00) trước khi transition

        // 1. Phủ màn đen (Fade to black)
        int blackAlpha = (progress < 0.28)
                ? (int) Math.min(255, (progress / 0.28) * 255)
                : 255;

        if (blackAlpha > 0) {
            g.setColor(new Color(0, 0, 0, blackAlpha));
            g.fillRect(0, 0, w, h);
        }

        // 2. Viền đỏ rực (Vignette) bi tráng trong lúc màn hình đang đen dần (0.0 -> 0.30)
        if (progress < 0.30) {
            float vigFactor = (float) (1.0 - progress / 0.30);
            int edgeAlpha = (int) (190 * vigFactor);
            if (edgeAlpha > 0) {
                g.setColor(new Color(180, 25, 25, edgeAlpha));
                int edgeThickness = (int) (24 + 18 * (1.0 - vigFactor));
                g.fillRect(0, 0, w, edgeThickness);
                g.fillRect(0, h - edgeThickness, w, edgeThickness);
                g.fillRect(0, 0, edgeThickness, h);
                g.fillRect(w - edgeThickness, 0, edgeThickness, h);
            }
        }

        // 3. Hiển thị tiêu đề "☠ BẠN ĐÃ BỊ HẠ ☠" trên nền đen và mờ dần vào bóng tối (Lại đen dần)
        if (progress >= 0.28) {
            int textAlpha;
            if (progress < 0.42) {
                // Chữ hiện dần lên trên nền đen (Fade-in)
                textAlpha = (int) Math.clamp(((progress - 0.28) / 0.14) * 255, 0, 255);
            } else if (progress <= 0.70) {
                // Giữ sáng chữ rõ nét, rực lửa
                textAlpha = 255;
            } else {
                // Lại đen dần: Chữ mờ dần biến mất vào bóng tối hoàn toàn (Fade-out)
                textAlpha = (int) Math.clamp((1.0 - (progress - 0.70) / 0.30) * 255, 0, 255);
            }

            if (textAlpha > 0) {
                g.setFont(GameFontManager.getBoldFont(40.0f));
                FontMetrics fmTitle = g.getFontMetrics();
                String title = "[X] BẠN ĐÃ BỊ HẠ [X]";
                int titleW = fmTitle.stringWidth(title);

                // Đổ bóng đỏ sẫm chiều sâu 3D
                g.setColor(new Color(60, 0, 0, textAlpha));
                g.drawString(title, (w - titleW) / 2 + 2, h / 2 - 18);

                // Chữ đỏ tươi rực sáng
                g.setColor(new Color(255, 60, 60, textAlpha));
                g.drawString(title, (w - titleW) / 2, h / 2 - 20);
            }
        }
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
    public void renderGameOverScreen(Graphics2D g, int currentStage, int maxStages, int playerCredits,
            long clearedRoomCount, int weaponSlotsCount) {
        // Phủ màn mờ đỏ đen kịch tính
        g.setColor(new Color(15, 5, 8, 235));
        g.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        int cx = GameConfig.SCREEN_WIDTH / 2;

        // Biểu tượng & Tiêu đề Tử trận
        g.setFont(GameFontManager.getBoldFont(36.0f));
        FontMetrics fm = g.getFontMetrics();
        String title = "[X] CHIẾN BINH TỬ TRẬN [X]";
        int titleW = fm.stringWidth(title);
        g.setColor(new Color(255, 60, 60));
        g.drawString(title, cx - titleW / 2, 220);

        g.setFont(GameFontManager.getBoldFont(16.0f));
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

        g.setFont(GameFontManager.getSmallFont(12.0f));
        g.setColor(new Color(220, 225, 235));
        g.drawString("- Giai đoạn chiến dịch: Stage " + currentStage + " / " + maxStages, cardX + 30, cardY + 40);
        g.drawString("- Ngân sách thu thập: " + playerCredits + " CREDITS", cardX + 30, cardY + 70);
        g.drawString("- Tiến độ dọn phòng: " + clearedRoomCount + " phòng combat", cardX + 30, cardY + 100);
        g.drawString("- Trang bị hiện có: " + weaponSlotsCount + " vũ khí trong kho", cardX + 30, cardY + 130);

        // Hướng dẫn tương tác
        g.setFont(GameFontManager.getSmallBoldFont(12.0f));
        fm = g.getFontMetrics();
        String prompt = "Bấm [ENTER] hoặc [R] để tái xuất phát | [ESC] Về Menu chính";
        g.setColor(new Color(255, 225, 75));
        g.drawString(prompt, cx - fm.stringWidth(prompt) / 2, 510);
    }
}
