package com.groupproject.game.ui;

import com.groupproject.game.core.GameFontManager;
import com.groupproject.game.entity.buff.CharacterBuff;
import com.groupproject.game.entity.buff.PlayerBuffManager;
import com.groupproject.game.entity.station.CharacterUpgradeStation;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import com.groupproject.game.state.PlayingState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Giao diện tương tác Bảng Nâng Cấp & Cường Hóa Nhân Vật (Character Upgrade Modal UI).
 *
 * <p>WHY: Hiển thị trực quan 3 thẻ bài buff ngẫu nhiên được rút từ trạm (Cơ bản hoặc Đặc biệt),
 * cho phép người chơi xem rõ mô tả, cấp độ, giá Credits và thực hiện mua bằng phím tắt [1], [2], [3]
 * hoặc click chuột trực tiếp.
 */
public class CharacterUpgradeUI {

    private volatile boolean visible = false;
    private volatile CharacterUpgradeStation currentStation = null;
    private volatile List<Rectangle> cardBounds = java.util.Collections.emptyList();
    private volatile List<Rectangle> buyButtonBounds = java.util.Collections.emptyList();

    private volatile int hoveredIndex = -1;
    private volatile String feedbackMessage = "";
    private volatile double feedbackTimer = 0.0;
    private volatile Color feedbackColor = Color.GREEN;

    public boolean isVisible() {
        return visible;
    }

    public synchronized void open(CharacterUpgradeStation station, PlayerBuffManager buffManager) {
        this.currentStation = station;
        this.visible = true;
        this.feedbackMessage = "";
        this.feedbackTimer = 0.0;
        if (station != null) {
            station.ensureBuffsRolled(buffManager);
        }
    }

    public synchronized void close() {
        this.visible = false;
        this.currentStation = null;
        this.feedbackMessage = "";
    }

    public CharacterUpgradeStation getCurrentStation() {
        return currentStation;
    }

    /**
     * Cập nhật điều khiển phím và chuột khi đang mở giao diện trạm nâng cấp.
     */
    public void update(KeyboardHandler keyH, MouseHandler mouseH, PlayingState state, double deltaTime) {
        if (!visible || currentStation == null || state == null) return;

        if (feedbackTimer > 0) {
            feedbackTimer -= deltaTime;
            if (feedbackTimer <= 0) {
                feedbackMessage = "";
            }
        }

        // 1. Phím tắt thoát [ESC] hoặc [F]
        if (keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE) || keyH.isKeyPressedOnce(KeyEvent.VK_F)) {
            close();
            return;
        }

        // 2. Phím tắt mua nhanh [1], [2], [3] hoặc Numpad
        if (keyH.isKeyPressedOnce(KeyEvent.VK_1) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD1)) {
            triggerPurchase(0, state);
            return;
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_2) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD2)) {
            triggerPurchase(1, state);
            return;
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_3) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD3)) {
            triggerPurchase(2, state);
            return;
        }

        // 3. Chuột hover và click
        int mx = mouseH.getScreenX();
        int my = mouseH.getScreenY();
        hoveredIndex = -1;
        List<Rectangle> cards = this.cardBounds;
        List<Rectangle> buttons = this.buyButtonBounds;
        int count = Math.min(cards.size(), buttons.size());
        for (int i = 0; i < count; i++) {
            Rectangle card = cards.get(i);
            Rectangle btn = buttons.get(i);
            if ((card != null && card.contains(mx, my)) || (btn != null && btn.contains(mx, my))) {
                hoveredIndex = i;
                break;
            }
        }

        if (mouseH.isLeftClickedOnce() && hoveredIndex >= 0) {
            triggerPurchase(hoveredIndex, state);
        }
    }

    private void triggerPurchase(int index, PlayingState state) {
        if (currentStation == null) return;
        List<CharacterBuff> buffs = currentStation.getOfferedBuffs();
        if (index < 0 || index >= buffs.size()) return;

        CharacterBuff buff = buffs.get(index);
        PlayerBuffManager bm = state.getPlayer().getBuffManager();
        int curLvl = bm.getLevel(buff);

        if (bm.isMaxLevel(buff)) {
            feedbackMessage = "BUFF NÀY ĐÃ ĐẠT CẤP ĐỘ TỐI ĐA!";
            feedbackColor = new Color(255, 180, 50);
            feedbackTimer = 2.0;
            return;
        }

        int cost = buff.getCostForLevel(curLvl, state.getCurrentStage());
        if (state.getPlayerCredits() < cost) {
            feedbackMessage = "KHÔNG ĐỦ CREDITS! CẦN " + cost + " CREDITS";
            feedbackColor = new Color(255, 80, 80);
            feedbackTimer = 2.0;
            return;
        }

        boolean success = currentStation.purchaseBuff(index, state);
        if (success) {
            feedbackMessage = "NÂNG CẤP THÀNH CÔNG: " + buff.getDisplayNameVi() + "!";
            feedbackColor = new Color(50, 255, 150);
            feedbackTimer = 2.2;
        }
    }

    /**
     * Kết xuất toàn bộ bảng nâng cấp Sci-Fi 3 thẻ bài.
     */
    public void render(Graphics2D g, int screenW, int screenH, PlayingState state) {
        CharacterUpgradeStation station = this.currentStation;
        if (!visible || station == null || state == null) return;
        CharacterUpgradeStation.StationType sType = station.getType();
        if (sType == null) return;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Phủ mờ màn hình
        g.setColor(new Color(6, 12, 20, 225));
        g.fillRect(0, 0, screenW, screenH);

        int modalW = 920;
        int modalH = 540;
        int modalX = (screenW - modalW) / 2;
        int modalY = (screenH - modalH) / 2;

        // 2. Khung modal chính
        g.setColor(new Color(15, 22, 34, 250));
        g.fillRoundRect(modalX, modalY, modalW, modalH, 16, 16);
        g.setColor(sType.accentColor);
        g.setStroke(new BasicStroke(2.0f));
        g.drawRoundRect(modalX, modalY, modalW, modalH, 16, 16);
        g.setStroke(new BasicStroke(1.0f));

        // 3. Tiêu đề trạm
        g.setFont(GameFontManager.getBoldFont(18.0f));
        FontMetrics fmTitle = g.getFontMetrics();
        String title = "TRẠM " + sType.titleVi;
        int titleX = modalX + (modalW - fmTitle.stringWidth(title)) / 2;
        g.setColor(sType.accentColor);
        g.drawString(title, titleX, modalY + 38);

        // 4. Số dư Credits người chơi
        g.setFont(GameFontManager.getSmallBoldFont(12.5f));
        String credText = "CREDITS HIỆN CÓ: " + state.getPlayerCredits() + " CREDITS";
        FontMetrics fmCred = g.getFontMetrics();
        g.setColor(new Color(255, 215, 60));
        g.drawString(credText, modalX + (modalW - fmCred.stringWidth(credText)) / 2, modalY + 66);

        // Thông báo phản hồi nếu có
        if (!feedbackMessage.isEmpty()) {
            g.setFont(GameFontManager.getSmallBoldFont(11.5f));
            FontMetrics fmFb = g.getFontMetrics();
            g.setColor(feedbackColor);
            g.drawString(feedbackMessage, modalX + (modalW - fmFb.stringWidth(feedbackMessage)) / 2, modalY + 90);
        }

        // 5. Kết xuất 3 thẻ bài Buff
        List<CharacterBuff> buffs = station.getOfferedBuffs();
        List<Rectangle> newCardBounds = new java.util.ArrayList<>(3);
        List<Rectangle> newBuyButtonBounds = new java.util.ArrayList<>(3);

        int cardW = 265;
        int cardH = 340;
        int spacing = 22;
        int totalCardsW = 3 * cardW + 2 * spacing;
        int startX = modalX + (modalW - totalCardsW) / 2;
        int cardY = modalY + 110;

        PlayerBuffManager bm = state.getPlayer().getBuffManager();

        for (int i = 0; i < 3; i++) {
            int cx = startX + i * (cardW + spacing);
            Rectangle cRect = new Rectangle(cx, cardY, cardW, cardH);
            newCardBounds.add(cRect);

            if (i < buffs.size()) {
                CharacterBuff buff = buffs.get(i);
                renderCard(g, cRect, buff, i, bm, state.getPlayerCredits(), sType, state.getCurrentStage(), newBuyButtonBounds);
            } else {
                renderEmptyCard(g, cRect);
                newBuyButtonBounds.add(new Rectangle(0, 0, 0, 0));
            }
        }
        this.cardBounds = newCardBounds;
        this.buyButtonBounds = newBuyButtonBounds;

        // 6. Lời nhắc phím ở đáy
        g.setFont(GameFontManager.getSmallFont(10.0f));
        g.setColor(new Color(130, 155, 185));
        String tip = "Click chuột hoặc bấm phím số [1], [2], [3] để mua - Nhấn [ESC] hoặc [F] để Đóng";
        FontMetrics fmTip = g.getFontMetrics();
        g.drawString(tip, modalX + (modalW - fmTip.stringWidth(tip)) / 2, modalY + modalH - 18);
    }

    private void renderCard(Graphics2D g, Rectangle rect, CharacterBuff buff, int index,
                            PlayerBuffManager bm, int playerCredits, CharacterUpgradeStation.StationType sType,
                            int stage, List<Rectangle> targetBuyButtons) {
        boolean isHovered = (hoveredIndex == index);
        boolean isMax = bm.isMaxLevel(buff);
        int curLvl = bm.getLevel(buff);
        int cost = isMax ? -1 : buff.getCostForLevel(curLvl, stage);
        boolean canAfford = (!isMax && playerCredits >= cost);

        // Nền thẻ bài
        Color cardBg = isHovered ? new Color(26, 36, 52) : new Color(18, 25, 38);
        g.setColor(cardBg);
        g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 12, 12);

        // Viền thẻ bài
        Color borderColor = isHovered
                ? sType.accentColor
                : new Color(55, 75, 105);
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(isHovered ? 2.0f : 1.2f));
        g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 12, 12);
        g.setStroke(new BasicStroke(1.0f));

        // Phím tắt góc trên bên trái [1], [2], [3]
        int keyBadgeSize = 26;
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRoundRect(rect.x + 12, rect.y + 12, keyBadgeSize, keyBadgeSize, 6, 6);
        g.setColor(sType.accentColor);
        g.drawRoundRect(rect.x + 12, rect.y + 12, keyBadgeSize, keyBadgeSize, 6, 6);
        g.setFont(GameFontManager.getBoldFont(12.0f));
        g.drawString(String.valueOf(index + 1), rect.x + 21, rect.y + 30);

        // Phân loại (Nhóm)
        g.setFont(GameFontManager.getSmallBoldFont(9.5f));
        String catStr = (buff.getCategory() == CharacterBuff.BuffCategory.BASIC_STAT)
                ? "[CHỈ SỐ CƠ BẢN]" : "[KỸ NĂNG ĐẶC BIỆT]";
        g.setColor(new Color(140, 175, 210));
        g.drawString(catStr, rect.x + 46, rect.y + 29);

        // Tên Buff
        g.setFont(GameFontManager.getBoldFont(12.5f));
        g.setColor(Color.WHITE);
        drawWrappedText(g, buff.getDisplayNameVi(), rect.x + 14, rect.y + 68, rect.width - 28, 18);

        // Cấp độ hiện tại / Cấp tối đa
        g.setFont(GameFontManager.getSmallFont(10.5f));
        String lvlStr;
        if (buff.getMaxLevel() == 1) {
            lvlStr = isMax ? "Trạng thái: ĐÃ KÍCH HOẠT" : "Trạng thái: CHƯA SỞ HỮU";
        } else {
            lvlStr = "Cấp độ: " + curLvl + " / " + buff.getMaxLevel();
        }
        g.setColor(isMax ? new Color(80, 240, 140) : new Color(200, 215, 235));
        g.drawString(lvlStr, rect.x + 14, rect.y + 124);

        // Đường kẻ phân cách
        g.setColor(new Color(40, 55, 80));
        g.drawLine(rect.x + 14, rect.y + 136, rect.x + rect.width - 14, rect.y + 136);

        // Mô tả tác dụng chi tiết
        g.setFont(GameFontManager.getSmallFont(10.0f));
        g.setColor(new Color(175, 195, 220));
        drawWrappedText(g, buff.getDescriptionVi(), rect.x + 14, rect.y + 160, rect.width - 28, 17);

        // Giá Credits & Nút Mua ở đáy thẻ
        int btnW = rect.width - 28;
        int btnH = 42;
        int btnX = rect.x + 14;
        int btnY = rect.y + rect.height - btnH - 14;
        Rectangle btnRect = new Rectangle(btnX, btnY, btnW, btnH);
        targetBuyButtons.add(btnRect);

        if (isMax) {
            g.setColor(new Color(30, 45, 35));
            g.fillRoundRect(btnX, btnY, btnW, btnH, 8, 8);
            g.setColor(new Color(50, 180, 100));
            g.drawRoundRect(btnX, btnY, btnW, btnH, 8, 8);
            g.setFont(GameFontManager.getSmallBoldFont(11.0f));
            String maxTxt = "[ ĐÃ TỐI ĐA ]";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(maxTxt, btnX + (btnW - fm.stringWidth(maxTxt)) / 2, btnY + 26);
        } else {
            Color btnBg = canAfford ? new Color(25, 75, 55) : new Color(60, 30, 30);
            Color btnBorder = canAfford ? new Color(50, 220, 140) : new Color(180, 60, 60);
            g.setColor(btnBg);
            g.fillRoundRect(btnX, btnY, btnW, btnH, 8, 8);
            g.setColor(btnBorder);
            g.drawRoundRect(btnX, btnY, btnW, btnH, 8, 8);

            g.setFont(GameFontManager.getSmallBoldFont(11.0f));
            String btnTxt = canAfford
                    ? "MUA [" + (index + 1) + "] : " + cost + " C"
                    : "THIẾU C (CẦN " + cost + " C)";
            g.setColor(canAfford ? Color.WHITE : new Color(240, 140, 140));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(btnTxt, btnX + (btnW - fm.stringWidth(btnTxt)) / 2, btnY + 26);
        }
    }

    private void renderEmptyCard(Graphics2D g, Rectangle rect) {
        g.setColor(new Color(15, 20, 30, 160));
        g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 12, 12);
        g.setColor(new Color(40, 50, 70));
        g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 12, 12);

        g.setFont(GameFontManager.getSmallFont(10.5f));
        g.setColor(new Color(90, 110, 135));
        String text = "ĐÃ MUA HẾT BUFF NHÓM NÀY";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, rect.x + (rect.width - fm.stringWidth(text)) / 2, rect.y + rect.height / 2);
    }

    private void drawWrappedText(Graphics2D g, String text, int x, int y, int maxWidth, int lineHeight) {
        if (text == null || text.isEmpty()) return;
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int curY = y;
        FontMetrics fm = g.getFontMetrics();

        for (String word : words) {
            String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (fm.stringWidth(candidate) > maxWidth) {
                g.drawString(currentLine.toString(), x, curY);
                curY += lineHeight;
                currentLine = new StringBuilder(word);
            } else {
                currentLine = new StringBuilder(candidate);
            }
        }
        if (!currentLine.isEmpty()) {
            g.drawString(currentLine.toString(), x, curY);
        }
    }
}
