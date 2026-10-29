package com.groupproject.game.ui;

import com.groupproject.game.core.GameFontManager;
import com.groupproject.game.core.GameSoundManager;
import com.groupproject.game.core.GameSoundManager.SoundEffect;
import com.groupproject.game.entity.station.StatReforgeStation;
import com.groupproject.game.entity.weapon.SniperRifle;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Giao diện Modal tương tác Xưởng Quân Khí Dã Chiến (Stat & Crit Reforge Modal UI).
 *
 * <p>WHY: Tách bạch rõ ràng 2 nhu cầu tối ưu hóa vũ khí của người chơi:
 * <ul>
 *   <li>[1] Reroll Sao Chỉ Số: Tái phân bổ sao thặng dư vào 3 nhánh (DMG, RPM, ACC), giữ nguyên chí mạng.</li>
 *   <li>[2] Reroll Chí Mạng: Gieo lại % chí mạng và hệ số xCrit, giữ nguyên phân bổ sao chỉ số.</li>
 * </ul>
 * Hỗ trợ tương tác chuột và phím tắt [1], [2], thoát bằng [ESC].
 */
public class StatReforgeModalUI {

    @FunctionalInterface
    public interface ReforgeActionCallback {
        void onReforgeRequested(ReforgeType type);
    }

    public enum ReforgeType {
        STARS_ONLY,
        CRIT_ONLY
    }

    private volatile boolean visible = false;
    private volatile Weapon targetWeapon = null;
    private volatile StatReforgeStation station = null;
    private volatile boolean hubMode = false;
    private volatile ReforgeActionCallback callback = null;

    private volatile List<Rectangle> cardBounds = new ArrayList<>();
    private volatile List<Rectangle> buttonBounds = new ArrayList<>();
    private volatile int hoveredIndex = -1;

    private volatile String feedbackMessage = "";
    private volatile double feedbackTimer = 0.0;
    private volatile Color feedbackColor = Color.GREEN;

    public boolean isVisible() {
        return visible;
    }

    public synchronized void open(Weapon weapon, StatReforgeStation station, boolean hubMode, ReforgeActionCallback callback) {
        this.targetWeapon = weapon;
        this.station = station;
        this.hubMode = hubMode;
        this.callback = callback;
        this.visible = true;
        this.hoveredIndex = -1;
        this.feedbackMessage = "";
        this.feedbackTimer = 0.0;
    }

    public synchronized void close() {
        this.visible = false;
        this.targetWeapon = null;
        this.station = null;
        this.callback = null;
        this.hoveredIndex = -1;
        this.feedbackMessage = "";
    }

    public void showFeedback(String msg, Color color) {
        this.feedbackMessage = msg;
        this.feedbackColor = color;
        this.feedbackTimer = 3.0;
    }

    public Weapon getTargetWeapon() {
        return targetWeapon;
    }

    public int getStarsCost() {
        if (hubMode) return 0;
        return station != null ? station.getStarsReforgeCost(targetWeapon) : 25;
    }

    public int getCritCost() {
        if (hubMode) return 0;
        return station != null ? station.getCritReforgeCost(targetWeapon) : 15;
    }

    /**
     * Cập nhật xử lý phím tắt và chuột khi Modal đang mở.
     */
    public void update(KeyboardHandler keyH, MouseHandler mouseH, int playerCredits, double deltaTime) {
        if (!visible || targetWeapon == null) return;

        if (feedbackTimer > 0) {
            feedbackTimer -= deltaTime;
            if (feedbackTimer <= 0) {
                feedbackMessage = "";
            }
        }

        // 1. Phím thoát [ESC] hoặc [F]
        if (keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
            GameSoundManager.play(SoundEffect.UI_INTERACT);
            close();
            return;
        }

        int starsCost = getStarsCost();
        int critCost = getCritCost();
        boolean isStarsAllowed = targetWeapon != null && targetWeapon.getLevel() > 1;
        boolean canAffordStars = hubMode || (playerCredits >= starsCost);
        boolean canAffordCrit = hubMode || (playerCredits >= critCost);

        // 2. Phím tắt số [1] và [2]
        if (keyH.isKeyPressedOnce(KeyEvent.VK_1) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD1)) {
            if (!isStarsAllowed) {
                GameSoundManager.play(SoundEffect.BUY_FAILED);
                showFeedback("VŨ KHÍ LV.1 CHƯA CÓ SAO NÂNG CẤP ĐỂ REROLL! HÃY NÂNG CẤP LÊN LV.2+ TRƯỚC.", new Color(255, 140, 60));
            } else if (canAffordStars) {
                triggerAction(ReforgeType.STARS_ONLY);
            } else {
                GameSoundManager.play(SoundEffect.BUY_FAILED);
                showFeedback("KHÔNG ĐỦ CREDITS ĐỂ REROLL SAO CHỈ SỐ!", new Color(255, 90, 90));
            }
            return;
        }

        if (keyH.isKeyPressedOnce(KeyEvent.VK_2) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD2)) {
            if (canAffordCrit) {
                triggerAction(ReforgeType.CRIT_ONLY);
            } else {
                GameSoundManager.play(SoundEffect.BUY_FAILED);
                showFeedback("KHÔNG ĐỦ CREDITS ĐỂ REROLL CHÍ MẠNG!", new Color(255, 90, 90));
            }
            return;
        }

        // 3. Chuột hover và click
        int mx = mouseH.getScreenX();
        int my = mouseH.getScreenY();
        hoveredIndex = -1;

        List<Rectangle> cards = this.cardBounds;
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).contains(mx, my)) {
                hoveredIndex = i;
                break;
            }
        }

        if (mouseH.isLeftClickedOnce()) {
            List<Rectangle> btns = this.buttonBounds;
            for (int i = 0; i < btns.size(); i++) {
                if (btns.get(i).contains(mx, my) || (i < cards.size() && cards.get(i).contains(mx, my))) {
                    if (i == 0) {
                        if (!isStarsAllowed) {
                            GameSoundManager.play(SoundEffect.BUY_FAILED);
                            showFeedback("VŨ KHÍ LV.1 CHƯA CÓ SAO NÂNG CẤP ĐỂ REROLL! HÃY NÂNG CẤP LÊN LV.2+ TRƯỚC.", new Color(255, 140, 60));
                        } else if (canAffordStars) {
                            triggerAction(ReforgeType.STARS_ONLY);
                        } else {
                            GameSoundManager.play(SoundEffect.BUY_FAILED);
                            showFeedback("KHÔNG ĐỦ CREDITS ĐỂ REROLL SAO CHỈ SỐ!", new Color(255, 90, 90));
                        }
                    } else {
                        if (canAffordCrit) {
                            triggerAction(ReforgeType.CRIT_ONLY);
                        } else {
                            GameSoundManager.play(SoundEffect.BUY_FAILED);
                            showFeedback("KHÔNG ĐỦ CREDITS ĐỂ REROLL CHÍ MẠNG!", new Color(255, 90, 90));
                        }
                    }
                    return;
                }
            }
        }
    }

    public void triggerAction(ReforgeType type) {
        ReforgeActionCallback cb = this.callback;
        if (cb != null) {
            cb.onReforgeRequested(type);
        }
    }

    /**
     * Vẽ giao diện Modal Xưởng Quân Khí dã chiến với thông số hiển thị trực quan.
     */
    public void render(Graphics2D g, int screenW, int screenH, int playerCredits) {
        if (!visible || targetWeapon == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Phủ mờ nền đen điện ảnh
        g2.setColor(new Color(10, 14, 20, 225));
        g2.fillRect(0, 0, screenW, screenH);

        // 2. Khung cửa sổ chính
        int modalW = 840;
        int modalH = 506;
        int modalX = (screenW - modalW) / 2;
        int modalY = (screenH - modalH) / 2;

        // Thân khung modal
        g2.setColor(new Color(22, 26, 32, 250));
        g2.fillRoundRect(modalX, modalY, modalW, modalH, 12, 12);

        // Viền kim loại dã chiến vàng hổ phách
        g2.setColor(new Color(230, 190, 60));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawRoundRect(modalX, modalY, modalW, modalH, 12, 12);

        // Header Modal
        g2.setColor(new Color(32, 38, 48));
        g2.fillRoundRect(modalX + 2, modalY + 2, modalW - 4, 68, 10, 10);
        g2.fillRect(modalX + 2, modalY + 44, modalW - 4, 26);

        // Tiêu đề chính
        g2.setFont(GameFontManager.getBoldFont(15.0f));
        g2.setColor(new Color(255, 220, 80));
        g2.drawString("BÀN KỸ THUẬT QUÂN KHÍ DÃ CHIẾN (REFORGE WORKBENCH)", modalX + 22, modalY + 26);

        // Dòng tóm tắt nhanh khí tài
        g2.setFont(GameFontManager.getSmallBoldFont(11.0f));
        String sTierTag = targetWeapon.isSTier() ? "[S-TIER] " : "";
        String weaponHeader = String.format("Khí tài: %s%s (Lv.%d) | Hỏa lực: %d DPS (Chí mạng: %d DPS) | Băng đạn: %d viên",
                sTierTag,
                targetWeapon.getShortDisplayName(),
                targetWeapon.getLevel(),
                targetWeapon.getNormalDPS(),
                targetWeapon.getCritDPS(),
                targetWeapon.getMagazineSize());
        g2.setColor(new Color(215, 230, 240));
        g2.drawString(weaponHeader, modalX + 22, modalY + 52);

        // Số dư Credits người chơi
        FontMetrics fm = g2.getFontMetrics();
        String credStr = hubMode ? "THE RANGE (FREE)" : String.format("VÍ CREDITS: %d C", playerCredits);
        g2.setColor(new Color(255, 230, 110));
        g2.drawString(credStr, modalX + modalW - fm.stringWidth(credStr) - 22, modalY + 26);

        // Thông báo phản hồi nếu có
        if (feedbackTimer > 0 && !feedbackMessage.isEmpty()) {
            g2.setFont(GameFontManager.getSmallBoldFont(11.0f));
            g2.setColor(feedbackColor);
            g2.drawString(feedbackMessage, modalX + 22, modalY + 84);
        }

        // 3. Render 2 Thẻ Chức Năng Lớn
        int cardW = 390;
        int cardH = 372;
        int spacing = 20;
        int startCardX = modalX + (modalW - (cardW * 2 + spacing)) / 2;
        int cardY = modalY + 92;

        List<Rectangle> newCards = new ArrayList<>();
        List<Rectangle> newBtns = new ArrayList<>();

        int starsCost = getStarsCost();
        int critCost = getCritCost();
        boolean canAffordStars = hubMode || (playerCredits >= starsCost);
        boolean canAffordCrit = hubMode || (playerCredits >= critCost);

        // Thẻ 1: REROLL SAO CHỈ SỐ
        Rectangle card1 = new Rectangle(startCardX, cardY, cardW, cardH);
        newCards.add(card1);
        renderStarsCard(g2, startCardX, cardY, cardW, cardH,
                starsCost,
                station != null ? station.getStarsReforgeCount() : 0,
                hoveredIndex == 0,
                canAffordStars,
                newBtns);

        // Thẻ 2: REROLL CHÍ MẠNG
        int card2X = startCardX + cardW + spacing;
        Rectangle card2 = new Rectangle(card2X, cardY, cardW, cardH);
        newCards.add(card2);
        renderCritCard(g2, card2X, cardY, cardW, cardH,
                critCost,
                station != null ? station.getCritReforgeCount() : 0,
                hoveredIndex == 1,
                canAffordCrit,
                newBtns);

        this.cardBounds = newCards;
        this.buttonBounds = newBtns;

        // 4. Footer
        g2.setFont(GameFontManager.getSmallFont(10.0f));
        g2.setColor(new Color(160, 175, 190));
        String footerText = "[1] Reroll Sao Chỉ Số | [2] Reroll Chí Mạng | Click chuột vào thẻ | [ESC] Thoát bàn kỹ thuật";
        g2.drawString(footerText, modalX + (modalW - fm.stringWidth(footerText)) / 2, modalY + modalH - 14);

        g2.dispose();
    }

    /**
     * Vẽ Thẻ 1: Reroll Sao Chỉ Số kèm bảng chi tiết số sao hiện tại và giá trị sát thương/tốc bắn/góc tản.
     */
    private void renderStarsCard(Graphics2D g, int cx, int cy, int cw, int ch,
                                 int cost, int reforgeCount, boolean hovered,
                                 boolean canAfford, List<Rectangle> outBtns) {
        boolean isStarsAllowed = targetWeapon != null && targetWeapon.getLevel() > 1;
        Color themeColor = isStarsAllowed ? new Color(75, 175, 255) : new Color(130, 140, 155);

        // Nền thẻ
        Color bgCard = (hovered && isStarsAllowed) ? new Color(34, 42, 54) : new Color(26, 32, 42);
        g.setColor(bgCard);
        g.fillRoundRect(cx, cy, cw, ch, 8, 8);

        // Viền thẻ
        g.setColor((hovered && isStarsAllowed) ? themeColor : new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 120));
        g.setStroke(new BasicStroke((hovered && isStarsAllowed) ? 2.2f : 1.2f));
        g.drawRoundRect(cx, cy, cw, ch, 8, 8);

        // Header thẻ
        g.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), isStarsAllowed ? 45 : 30));
        g.fillRoundRect(cx + 4, cy + 4, cw - 8, 38, 6, 6);

        g.setFont(GameFontManager.getBoldFont(12.5f));
        g.setColor(themeColor);
        String headerTitle = isStarsAllowed ? "[1] REROLL SAO CHỈ SỐ" : "[1] REROLL SAO [KHÓA]";
        g.drawString(headerTitle, cx + 14, cy + 26);

        g.setFont(GameFontManager.getSmallFont(9.5f));
        g.setColor(new Color(190, 210, 225));
        String subHeader = isStarsAllowed ? "Tái phân bổ ngẫu nhiên sao thặng dư vào 3 nhánh" : "Yêu cầu vũ khí đạt cấp 2 trở lên (chưa có sao thặng dư)";
        g.drawString(subHeader, cx + 14, cy + 54);

        // --- KHUNG 1: THÔNG SỐ SAO HIỆN TẠI CỦA SÚNG ---
        int boxY = cy + 64;
        int boxH = 185;
        int boxW = cw - 24;
        int boxX = cx + 12;

        g.setColor(new Color(18, 24, 32));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 6, 6);
        g.setColor(new Color(60, 110, 160, 90));
        g.setStroke(new BasicStroke(1.0f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 6, 6);

        g.setFont(GameFontManager.getSmallBoldFont(10.5f));
        g.setColor(new Color(140, 205, 255));
        g.drawString("> CẤU HÌNH SAO HIỆN TẠI (CURRENT STATS):", boxX + 12, boxY + 22);

        // 3 nhánh sao chi tiết
        int rowY = boxY + 48;
        drawStatRow(g, boxX + 12, rowY, "Sát thương (DMG):",
                targetWeapon.getDamageStars(),
                String.format("%d DMG", targetWeapon.getDamage()),
                new Color(255, 120, 120));

        rowY += 28;
        int approxRpm = targetWeapon.getFireRateCooldown() > 0 ? (int) Math.round(60.0 / targetWeapon.getFireRateCooldown()) : 0;
        drawStatRow(g, boxX + 12, rowY, "Tốc độ bắn (RPM):",
                targetWeapon.getFireRateStars(),
                String.format("%.2fs (~%d RPM)", targetWeapon.getFireRateCooldown(), approxRpm),
                new Color(120, 220, 140));

        rowY += 28;
        if (targetWeapon instanceof SniperRifle sr) {
            drawStatRow(g, boxX + 12, rowY, "Sức xuyên (PIERCE):",
                sr.getPierceStars(),
                String.format("Xuyên %d mục tiêu (-%d%%)", sr.getMaxPierces(), sr.getPierceFalloffPercent()),
                new Color(185, 100, 255));
        } else {
            drawStatRow(g, boxX + 12, rowY, "Độ chính xác (ACC):",
                targetWeapon.getAccuracyStars(),
                String.format("±%.2f rad", targetWeapon.getCurrentSpreadAngle()),
                new Color(255, 210, 100));
        }

        // Dòng tổng sao thặng dư
        rowY += 30;
        g.setFont(GameFontManager.getSmallFont(9.5f));
        if (isStarsAllowed) {
            g.setColor(new Color(165, 185, 205));
            int totalExtra = (targetWeapon.getDamageStars() - 1) + (targetWeapon.getFireRateStars() - 1) + (targetWeapon.getAccuracyStars() - 1);
            g.drawString(String.format("Tổng: %d Sao (Lv.%d) -> Sẽ chia lại %d sao thặng dư",
                    targetWeapon.getDamageStars() + targetWeapon.getFireRateStars() + targetWeapon.getAccuracyStars(),
                    targetWeapon.getLevel(),
                    totalExtra), boxX + 12, rowY);
        } else {
            g.setColor(new Color(255, 150, 70));
            g.drawString("[!]: Súng Lv.1 chưa có sao nâng cấp thặng dư để tái phân bổ!", boxX + 12, rowY);
        }

        // Thông tin số lần rèn & chi phí
        int infoY = cy + ch - 58;
        g.setFont(GameFontManager.getSmallBoldFont(10.5f));
        g.setColor(new Color(180, 195, 210));
        String countStr = hubMode ? "Miễn phí (Không giới hạn tại The Range)" : String.format("Đã rèn: %d lần | Chi phí: %d Credits", reforgeCount, cost);
        g.drawString(countStr, cx + 14, infoY);

        // Nút bấm
        int btnW = cw - 28;
        int btnH = 36;
        int btnX = cx + 14;
        int btnY = cy + ch - btnH - 12;
        Rectangle btnRect = new Rectangle(btnX, btnY, btnW, btnH);
        outBtns.add(btnRect);

        if (!isStarsAllowed) {
            g.setColor(new Color(45, 48, 56));
            g.fillRoundRect(btnX, btnY, btnW, btnH, 6, 6);
            g.setColor(new Color(80, 85, 100));
            g.drawRoundRect(btnX, btnY, btnW, btnH, 6, 6);
            g.setColor(new Color(255, 150, 70));
            g.setFont(GameFontManager.getSmallBoldFont(10.5f));
            String btnText = "[KHÓA] (YÊU CẦU SÚNG LV.2+)";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(btnText, btnX + (btnW - fm.stringWidth(btnText)) / 2, btnY + 23);
        } else if (canAfford) {
            g.setColor(hovered ? themeColor : new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 180));
            g.fillRoundRect(btnX, btnY, btnW, btnH, 6, 6);
            g.setColor(new Color(20, 25, 30));
            g.setFont(GameFontManager.getSmallBoldFont(11.0f));
            String btnText = hubMode ? "REROLL SAO CHỈ SỐ [1]" : String.format("REROLL SAO CHỈ SỐ (%d C) [1]", cost);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(btnText, btnX + (btnW - fm.stringWidth(btnText)) / 2, btnY + 23);
        } else {
            g.setColor(new Color(50, 55, 65));
            g.fillRoundRect(btnX, btnY, btnW, btnH, 6, 6);
            g.setColor(new Color(200, 110, 110));
            g.setFont(GameFontManager.getSmallBoldFont(10.5f));
            String btnText = String.format("THIẾU TIỀN (CẦN %d CREDITS)", cost);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(btnText, btnX + (btnW - fm.stringWidth(btnText)) / 2, btnY + 23);
        }
    }

    /**
     * Vẽ Thẻ 2: Reroll Chí Mạng kèm thanh đo tiến trình, đánh giá cao/thấp và bảo lưu sao 3 nhánh.
     */
    private void renderCritCard(Graphics2D g, int cx, int cy, int cw, int ch,
                                int cost, int reforgeCount, boolean hovered,
                                boolean canAfford, List<Rectangle> outBtns) {
        Color themeColor = new Color(255, 130, 50);

        // Nền thẻ
        Color bgCard = hovered ? new Color(42, 36, 30) : new Color(32, 28, 24);
        g.setColor(bgCard);
        g.fillRoundRect(cx, cy, cw, ch, 8, 8);

        // Viền thẻ
        g.setColor(hovered ? themeColor : new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 120));
        g.setStroke(new BasicStroke(hovered ? 2.2f : 1.2f));
        g.drawRoundRect(cx, cy, cw, ch, 8, 8);

        // Header thẻ
        g.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 45));
        g.fillRoundRect(cx + 4, cy + 4, cw - 8, 38, 6, 6);

        g.setFont(GameFontManager.getBoldFont(12.5f));
        g.setColor(themeColor);
        g.drawString("[2] REROLL CHÍ MẠNG", cx + 14, cy + 26);

        g.setFont(GameFontManager.getSmallFont(9.5f));
        g.setColor(new Color(225, 205, 190));
        g.drawString("Gieo lại ngẫu nhiên tỉ lệ và hệ số sát thương chí mạng", cx + 14, cy + 54);

        // --- KHUNG 1: CHỈ SỐ CHÍ MẠNG HIỆN TẠI CỦA SÚNG ---
        int boxY = cy + 64;
        int boxH = 185;
        int boxW = cw - 24;
        int boxX = cx + 12;

        g.setColor(new Color(28, 22, 18));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 6, 6);
        g.setColor(new Color(180, 90, 40, 90));
        g.setStroke(new BasicStroke(1.0f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 6, 6);

        g.setFont(GameFontManager.getSmallBoldFont(10.5f));
        g.setColor(new Color(255, 175, 120));
        g.drawString("> CHỈ SỐ CHÍ MẠNG HIỆN TẠI (CURRENT CRIT):", boxX + 12, boxY + 22);

        // Hàng thông số số liệu lớn
        int critPct = targetWeapon.getCritChancePercent();
        double critMult = targetWeapon.getCritMultiplier();
        int critDmg = (int) Math.round(targetWeapon.getDamage() * critMult);

        g.setFont(GameFontManager.getBoldFont(12.0f));
        g.setColor(new Color(255, 200, 80));
        g.drawString(String.format("Tỉ Lệ: %d%%", critPct), boxX + 12, boxY + 48);

        g.setColor(new Color(255, 145, 70));
        g.drawString(String.format("Hệ Số: x%.2f", critMult), boxX + 125, boxY + 48);

        g.setFont(GameFontManager.getSmallBoldFont(11.0f));
        g.setColor(new Color(220, 200, 190));
        g.drawString(String.format("Nổ Dame: %d DMG", critDmg), boxX + 235, boxY + 48);

        // Thanh đo trực quan (Progress Bar từ 5% đến 50%)
        int barX = boxX + 12;
        int barY = boxY + 68;
        int barW = boxW - 24;
        int barH = 12;

        g.setColor(new Color(40, 30, 25));
        g.fillRoundRect(barX, barY, barW, barH, 4, 4);

        // Tỉ lệ % trong dải [5%, 50%]
        double fraction = Math.max(0.0, Math.min(1.0, (targetWeapon.getCritChance() - 0.05) / 0.45));
        int fillW = Math.max(6, (int) Math.round(barW * fraction));
        g.setColor(new Color(255, 130, 40));
        g.fillRoundRect(barX, barY, fillW, barH, 4, 4);

        // Vạch mốc trên thanh đo
        g.setFont(GameFontManager.getSmallFont(9.5f));
        g.setColor(new Color(160, 145, 135));
        g.drawString("5% (Min)", barX, barY + 24);
        g.drawString("25%", barX + barW / 2 - 10, barY + 24);
        g.drawString("50% (Max)", barX + barW - 48, barY + 24);

        // Khuyến nghị / Đánh giá hiện tại
        int evalY = boxY + 126;
        g.setFont(GameFontManager.getSmallBoldFont(10.0f));
        if (critPct < 18) {
            g.setColor(new Color(255, 100, 90));
            g.drawString("* ĐÁNH GIÁ: THẤP -> KHUYẾN NGHỊ NÊN REROLL (Cơ hội tăng cao)", boxX + 12, evalY);
        } else if (critPct <= 32) {
            g.setColor(new Color(255, 205, 80));
            g.drawString("* ĐÁNH GIÁ: TRUNG BÌNH -> CÓ THỂ REROLL ĐỂ TÌM MỐC CAO", boxX + 12, evalY);
        } else {
            g.setColor(new Color(90, 240, 140));
            g.drawString("* ĐÁNH GIÁ: RẤT CAO -> NÊN GIỮ LẠI (Reroll có nguy cơ giảm!)", boxX + 12, evalY);
        }

        g.setFont(GameFontManager.getSmallFont(9.5f));
        g.setColor(new Color(175, 160, 150));
        g.drawString("Dải gieo lại ngẫu nhiên: Tỉ lệ [5% - 50%] | Hệ số [1.50x - 2.00x]", boxX + 12, evalY + 20);

        // Thông tin số lần rèn & chi phí
        int infoY = cy + ch - 58;
        g.setFont(GameFontManager.getSmallBoldFont(10.5f));
        g.setColor(new Color(180, 195, 210));
        String countStr = hubMode ? "Miễn phí (Không giới hạn tại The Range)" : String.format("Đã rèn: %d lần | Chi phí: %d Credits", reforgeCount, cost);
        g.drawString(countStr, cx + 14, infoY);

        // Nút bấm
        int btnW = cw - 28;
        int btnH = 36;
        int btnX = cx + 14;
        int btnY = cy + ch - btnH - 12;
        Rectangle btnRect = new Rectangle(btnX, btnY, btnW, btnH);
        outBtns.add(btnRect);

        if (canAfford) {
            g.setColor(hovered ? themeColor : new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 180));
            g.fillRoundRect(btnX, btnY, btnW, btnH, 6, 6);
            g.setColor(new Color(20, 25, 30));
            g.setFont(GameFontManager.getSmallBoldFont(11.0f));
            String btnText = hubMode ? "REROLL CHÍ MẠNG [2]" : String.format("REROLL CHÍ MẠNG (%d C) [2]", cost);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(btnText, btnX + (btnW - fm.stringWidth(btnText)) / 2, btnY + 23);
        } else {
            g.setColor(new Color(50, 55, 65));
            g.fillRoundRect(btnX, btnY, btnW, btnH, 6, 6);
            g.setColor(new Color(200, 110, 110));
            g.setFont(GameFontManager.getSmallBoldFont(10.5f));
            String btnText = String.format("THIẾU TIỀN (CẦN %d CREDITS)", cost);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(btnText, btnX + (btnW - fm.stringWidth(btnText)) / 2, btnY + 23);
        }
    }

    /**
     * Hàm phụ trợ vẽ từng hàng sao với icon vàng đẹp mắt.
     */
    private void drawStatRow(Graphics2D g, int x, int y, String label, int stars, String valText, Color valColor) {
        g.setFont(GameFontManager.getSmallFont(10.5f));
        g.setColor(new Color(205, 220, 235));
        g.drawString(label, x, y);

        // Vẽ icon sao vector đồ họa đẹp mắt không phụ thuộc font
        Weapon.renderStarIcons(g, x + 124, y - 4, stars, 3, 5, 12);

        // Giá trị cụ thể
        g.setFont(GameFontManager.getSmallBoldFont(10.5f));
        g.setColor(valColor);
        g.drawString(valText, x + 172, y);
    }
}
