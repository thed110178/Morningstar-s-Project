package com.groupproject.game.ui;

import com.groupproject.game.core.GameFontManager;
import com.groupproject.game.core.GameSoundManager;
import com.groupproject.game.core.GameSoundManager.SoundEffect;
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
 * Giao diện Modal tương tác chọn 1 trong 3 chỉ số khi nâng cấp vũ khí (Weapon Upgrade Modal UI).
 *
 * <p>WHY: Thay thế cơ chế cộng sao ngẫu nhiên cũ bằng quyết định chiến thuật chủ động của người chơi:
 * hiển thị trực quan thông số trước/sau khi nâng của 3 nhánh (Sát thương, Tốc bắn, Độ chính xác),
 * hỗ trợ click chuột hoặc phím tắt [1], [2], [3] và phím [ESC] để đóng.
 */
public class WeaponUpgradeModalUI {

    @FunctionalInterface
    public interface UpgradeSelectionCallback {
        void onStatSelected(Weapon.UpgradeStatType statType);
    }

    private volatile boolean visible = false;
    private volatile Weapon targetWeapon = null;
    private volatile int cost = 0;
    private volatile boolean hubMode = false;
    private volatile UpgradeSelectionCallback callback = null;

    private volatile List<Rectangle> cardBounds = new ArrayList<>();
    private volatile List<Rectangle> buttonBounds = new ArrayList<>();
    private volatile int hoveredIndex = -1;

    public boolean isVisible() {
        return visible;
    }

    /**
     * Mở Modal nâng cấp vũ khí với vũ khí và chi phí chỉ định.
     */
    public synchronized void open(Weapon weapon, int cost, boolean hubMode, UpgradeSelectionCallback callback) {
        this.targetWeapon = weapon;
        this.cost = cost;
        this.hubMode = hubMode;
        this.callback = callback;
        this.visible = true;
        this.hoveredIndex = -1;
    }

    /**
     * Đóng Modal nâng cấp.
     */
    public synchronized void close() {
        this.visible = false;
        this.targetWeapon = null;
        this.callback = null;
        this.hoveredIndex = -1;
    }

    public Weapon getTargetWeapon() {
        return targetWeapon;
    }

    /**
     * Cập nhật xử lý phím tắt và chuột khi Modal đang mở.
     */
    public void update(KeyboardHandler keyH, MouseHandler mouseH, int playerCredits, double deltaTime) {
        if (!visible || targetWeapon == null) return;

        // 1. Phím thoát [ESC]
        if (keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
            GameSoundManager.play(SoundEffect.UI_INTERACT);
            close();
            return;
        }

        // Kiểm tra điều kiện đủ tiền (nếu trong Dungeon)
        boolean canAfford = hubMode || (playerCredits >= cost);

        // 2. Phím tắt số [1], [2], [3]
        if (keyH.isKeyPressedOnce(KeyEvent.VK_1) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD1)) {
            if (canAfford) {
                triggerSelection(Weapon.UpgradeStatType.DAMAGE);
            } else {
                GameSoundManager.play(SoundEffect.BUY_FAILED);
            }
            return;
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_2) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD2)) {
            if (canAfford) {
                triggerSelection(Weapon.UpgradeStatType.FIRE_RATE);
            } else {
                GameSoundManager.play(SoundEffect.BUY_FAILED);
            }
            return;
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_3) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD3)) {
            if (canAfford) {
                triggerSelection(Weapon.UpgradeStatType.ACCURACY);
            } else {
                GameSoundManager.play(SoundEffect.BUY_FAILED);
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
                    if (canAfford) {
                        Weapon.UpgradeStatType selected = switch (i) {
                            case 0 -> Weapon.UpgradeStatType.DAMAGE;
                            case 1 -> Weapon.UpgradeStatType.FIRE_RATE;
                            default -> Weapon.UpgradeStatType.ACCURACY;
                        };
                        triggerSelection(selected);
                    } else {
                        GameSoundManager.play(SoundEffect.BUY_FAILED);
                    }
                    return;
                }
            }
        }
    }

    public void triggerSelection(Weapon.UpgradeStatType statType) {
        UpgradeSelectionCallback cb = this.callback;
        close();
        if (cb != null) {
            cb.onStatSelected(statType);
        }
    }

    /**
     * Vẽ giao diện Modal nâng cấp súng phong cách hiện đại.
     */
    public void render(Graphics2D g, int screenW, int screenH, int playerCredits) {
        if (!visible || targetWeapon == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Phủ mờ nền đen điện ảnh
        g2.setColor(new Color(10, 14, 20, 215));
        g2.fillRect(0, 0, screenW, screenH);

        // 2. Khung cửa sổ chính
        int modalW = 760;
        int modalH = 460;
        int modalX = (screenW - modalW) / 2;
        int modalY = (screenH - modalH) / 2;

        // Thân khung modal
        g2.setColor(new Color(24, 29, 38, 245));
        g2.fillRoundRect(modalX, modalY, modalW, modalH, 12, 12);

        // Viền kim loại dã chiến
        boolean canAfford = hubMode || (playerCredits >= cost);
        Color borderColor = canAfford ? new Color(220, 185, 60) : new Color(220, 75, 75);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawRoundRect(modalX, modalY, modalW, modalH, 12, 12);

        // Header Modal
        g2.setColor(new Color(35, 42, 54));
        g2.fillRoundRect(modalX + 2, modalY + 2, modalW - 4, 62, 10, 10);
        g2.fillRect(modalX + 2, modalY + 40, modalW - 4, 24);

        g2.setFont(GameFontManager.getBoldFont(15.0f));
        g2.setColor(new Color(255, 220, 80));
        g2.drawString("BẢNG NÂNG CẤP VŨ KHÍ CHIẾN THUẬT (CHỌN 1 TRONG 3 CHỈ SỐ)", modalX + 24, modalY + 28);

        // Dòng phụ đề thông tin vũ khí & chi phí
        g2.setFont(GameFontManager.getSmallBoldFont(11.0f));
        String upgradeProg = hubMode
                ? "The Range (Max Lv.10)"
                : (targetWeapon.getUpgradeCount() + "/" + Weapon.MAX_UPGRADE_COUNT + " lần");
        String weaponInfo = String.format("Khí tài: %s (Lv.%d -> Lv.%d) | Đã nâng: %s",
                targetWeapon.getShortDisplayName(), targetWeapon.getLevel(), targetWeapon.getLevel() + 1, upgradeProg);
        g2.setColor(new Color(210, 225, 235));
        g2.drawString(weaponInfo, modalX + 24, modalY + 52);

        // Chi phí bên phải
        FontMetrics fm = g2.getFontMetrics();
        String costStr = hubMode ? "MIỄN PHÍ TẠI TRƯỜNG BẮN (FREE)" : String.format("CHI PHÍ: %d CREDITS (CÓ: %d C)", cost, playerCredits);
        g2.setColor(canAfford ? new Color(100, 255, 140) : new Color(255, 90, 90));
        g2.drawString(costStr, modalX + modalW - fm.stringWidth(costStr) - 24, modalY + 52);

        // 3. Render 3 Thẻ Chỉ Số
        int cardW = 224;
        int cardH = 320;
        int spacing = 18;
        int startCardX = modalX + (modalW - (cardW * 3 + spacing * 2)) / 2;
        int cardY = modalY + 80;

        List<Rectangle> newCards = new ArrayList<>();
        List<Rectangle> newBtns = new ArrayList<>();

        Weapon.UpgradeStatType[] statTypes = {
                Weapon.UpgradeStatType.DAMAGE,
                Weapon.UpgradeStatType.FIRE_RATE,
                Weapon.UpgradeStatType.ACCURACY
        };

        for (int i = 0; i < 3; i++) {
            int cx = startCardX + i * (cardW + spacing);
            Rectangle cr = new Rectangle(cx, cardY, cardW, cardH);
            newCards.add(cr);

            boolean isHovered = (hoveredIndex == i);
            renderStatCard(g2, cx, cardY, cardW, cardH, i, statTypes[i], isHovered, canAfford, newBtns);
        }

        this.cardBounds = newCards;
        this.buttonBounds = newBtns;

        // 4. Footer hướng dẫn
        g2.setFont(GameFontManager.getSmallFont(10.0f));
        g2.setColor(new Color(160, 175, 190));
        String footerText = "[1, 2, 3] Chọn nhanh nhánh nâng cấp | Click chuột vào thẻ | [ESC] Thoát bỏ qua";
        g2.drawString(footerText, modalX + (modalW - fm.stringWidth(footerText)) / 2, modalY + modalH - 18);

        g2.dispose();
    }

    private void renderStatCard(Graphics2D g, int cx, int cy, int cw, int ch, int index,
                                Weapon.UpgradeStatType statType, boolean hovered, boolean canAfford,
                                List<Rectangle> outBtns) {
        // Nền thẻ
        Color bgCard = hovered ? new Color(38, 46, 60) : new Color(30, 36, 48);
        g.setColor(bgCard);
        g.fillRoundRect(cx, cy, cw, ch, 8, 8);

        // Viền thẻ
        boolean isSniper = targetWeapon instanceof SniperRifle;
        Color themeColor = switch (statType) {
            case DAMAGE -> new Color(255, 110, 80);
            case FIRE_RATE -> new Color(255, 205, 50);
            case ACCURACY -> isSniper ? new Color(185, 100, 255) : new Color(50, 230, 160);
        };
        g.setColor(hovered ? themeColor : new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 120));
        g.setStroke(new BasicStroke(hovered ? 2.2f : 1.2f));
        g.drawRoundRect(cx, cy, cw, ch, 8, 8);

        // Header thẻ (Badge số phím tắt [1], [2], [3])
        g.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 45));
        g.fillRoundRect(cx + 4, cy + 4, cw - 8, 38, 6, 6);

        g.setFont(GameFontManager.getBoldFont(11.5f));
        g.setColor(themeColor);
        String headerTitle = String.format("[%d] %s", index + 1, targetWeapon.getStatDisplayName(statType));
        g.drawString(headerTitle, cx + 12, cy + 28);

        // Mô tả ngắn
        g.setFont(GameFontManager.getSmallFont(9.5f));
        g.setColor(new Color(190, 205, 215));
        g.drawString(targetWeapon.getStatDescription(statType), cx + 12, cy + 62);

        // Đường kẻ phân cách
        g.setColor(new Color(55, 65, 80));
        g.drawLine(cx + 12, cy + 74, cx + cw - 12, cy + 74);

        // Thông số chi tiết: Hiện tại -> Mới
        int lineY = cy + 96;

        // 1. Cấp sao nhánh (Vẽ icon sao đồ họa vector trực quan)
        int currentStars = switch (statType) {
            case DAMAGE -> targetWeapon.getDamageStars();
            case FIRE_RATE -> targetWeapon.getFireRateStars();
            case ACCURACY -> targetWeapon.getAccuracyStars();
        };
        g.setFont(GameFontManager.getSmallFont(10.5f));
        g.setColor(new Color(210, 220, 230));
        g.drawString("Cấp sao nhánh:", cx + 14, lineY);

        // Vẽ vector sao hiện tại -> sao sau nâng cấp
        Weapon.renderStarIcons(g, cx + 14, lineY + 16, currentStars, 3, 5, 12);
        g.setFont(GameFontManager.getSmallBoldFont(11.0f));
        g.setColor(new Color(255, 215, 60));
        g.drawString("->", cx + 58, lineY + 20);
        Weapon.renderStarIcons(g, cx + 78, lineY + 16, currentStars + 1, 3, 5, 12);
        g.setColor(new Color(100, 255, 140));
        g.drawString("(+1)", cx + 122, lineY + 20);

        // 2. Chỉ số cụ thể
        lineY += 46;
        g.setFont(GameFontManager.getSmallFont(10.5f));
        g.setColor(new Color(210, 220, 230));
        if (statType == Weapon.UpgradeStatType.DAMAGE) {
            g.drawString("Sát thương đơn:", cx + 14, lineY);
            int newDmg = targetWeapon.getPreviewDamage(statType);
            g.setFont(GameFontManager.getSmallBoldFont(11.0f));
            g.setColor(themeColor);
            g.drawString(targetWeapon.getDamage() + "  ->  " + newDmg + " (+20%)", cx + 14, lineY + 18);

            lineY += 42;
            g.setFont(GameFontManager.getSmallFont(10.5f));
            g.setColor(new Color(210, 220, 230));
            g.drawString("Sát thương DPS:", cx + 14, lineY);
            int newDPS = targetWeapon.getPreviewNormalDPS(statType);
            g.setFont(GameFontManager.getSmallBoldFont(11.0f));
            g.setColor(themeColor);
            g.drawString(targetWeapon.getNormalDPS() + "  ->  " + newDPS + " DPS", cx + 14, lineY + 18);

        } else if (statType == Weapon.UpgradeStatType.FIRE_RATE) {
            g.drawString("Độ trễ bắn:", cx + 14, lineY);
            double newCd = targetWeapon.getPreviewFireRateCooldown(statType);
            g.setFont(GameFontManager.getSmallBoldFont(11.0f));
            g.setColor(themeColor);
            g.drawString(String.format("%.3fs -> %.3fs", targetWeapon.getFireRateCooldown(), newCd), cx + 14, lineY + 18);

            lineY += 42;
            g.setFont(GameFontManager.getSmallFont(10.5f));
            g.setColor(new Color(210, 220, 230));
            g.drawString("Sát thương DPS:", cx + 14, lineY);
            int newDPS = targetWeapon.getPreviewNormalDPS(statType);
            g.setFont(GameFontManager.getSmallBoldFont(11.0f));
            g.setColor(themeColor);
            g.drawString(targetWeapon.getNormalDPS() + "  ->  " + newDPS + " DPS", cx + 14, lineY + 18);

        } else { // ACCURACY hoặc SỨC XUYÊN (PIERCE đối với Sniper Rifle)
            if (isSniper) {
                SniperRifle sr = (SniperRifle) targetWeapon;
                g.drawString("Mục tiêu xuyên phá:", cx + 14, lineY);
                int curPierces = sr.getMaxPierces();
                int newPierces = sr.getPreviewMaxPierces();
                int diffPierces = newPierces - curPierces;
                g.setFont(GameFontManager.getSmallBoldFont(11.0f));
                g.setColor(themeColor);
                String pierceText = (diffPierces > 0)
                        ? String.format("%d  ->  %d (+%d mục tiêu)", curPierces, newPierces, diffPierces)
                        : String.format("%d mục tiêu (Tối ưu hao hụt)", curPierces);
                g.drawString(pierceText, cx + 14, lineY + 18);

                lineY += 42;
                g.setFont(GameFontManager.getSmallFont(10.5f));
                g.setColor(new Color(210, 220, 230));
                g.drawString("Hao hụt sát thương:", cx + 14, lineY);
                int curFalloff = sr.getPierceFalloffPercent();
                int newFalloff = sr.getPreviewPierceFalloffPercent();
                g.setFont(GameFontManager.getSmallBoldFont(11.0f));
                g.setColor(themeColor);
                g.drawString(String.format("-%d%% -> -%d%% (Tối ưu)", curFalloff, newFalloff), cx + 14, lineY + 18);
            } else {
                g.drawString("Góc tản đạn:", cx + 14, lineY);
                double curDeg = Math.toDegrees(targetWeapon.getMaxSpreadAngle());
                double newDeg = Math.toDegrees(targetWeapon.getPreviewSpreadAngle(statType));
                g.setFont(GameFontManager.getSmallBoldFont(11.0f));
                g.setColor(themeColor);
                g.drawString(String.format("±%.1f° -> ±%.1f°", curDeg, newDeg), cx + 14, lineY + 18);

                lineY += 42;
                g.setFont(GameFontManager.getSmallFont(10.5f));
                g.setColor(new Color(210, 220, 230));
                g.drawString("Chùm đạn:", cx + 14, lineY);
                g.setFont(GameFontManager.getSmallBoldFont(11.0f));
                g.setColor(themeColor);
                g.drawString("Gom chặt hơn 35%", cx + 14, lineY + 18);
            }
        }

        // 3. Nút bấm nâng cấp
        int btnW = cw - 28;
        int btnH = 34;
        int btnX = cx + 14;
        int btnY = cy + ch - btnH - 14;
        Rectangle btnRect = new Rectangle(btnX, btnY, btnW, btnH);
        outBtns.add(btnRect);

        if (canAfford) {
            g.setColor(hovered ? themeColor : new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 180));
            g.fillRoundRect(btnX, btnY, btnW, btnH, 6, 6);
            g.setColor(new Color(20, 25, 30));
            g.setFont(GameFontManager.getSmallBoldFont(11.0f));
            String btnText = "CHỌN NÂNG CẤP [" + (index + 1) + "]";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(btnText, btnX + (btnW - fm.stringWidth(btnText)) / 2, btnY + 22);
        } else {
            g.setColor(new Color(50, 55, 65));
            g.fillRoundRect(btnX, btnY, btnW, btnH, 6, 6);
            g.setColor(new Color(160, 100, 100));
            g.setFont(GameFontManager.getSmallBoldFont(10.5f));
            String btnText = "KHÔNG ĐỦ TIỀN";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(btnText, btnX + (btnW - fm.stringWidth(btnText)) / 2, btnY + 22);
        }
    }
}
