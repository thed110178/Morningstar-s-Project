package com.groupproject.game.hub;

import com.groupproject.game.entity.weapon.Weapon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Bàn nâng cấp vũ khí miễn phí tối đa Cấp 10 (Max Level 10 Upgrade Station) trong The Range.
 *
 * <p>WHY: Sử dụng ngôn ngữ hình ảnh của Trạm Nâng Cấp Công Nghệ Cao (CharacterUpgradeStation)
 * từ gameplay hầm ngục chính, với vòng phát quang năng lượng Bio-Tech, bệ máy bát giác bo góc
 * và lõi năng lượng phát sáng rực rỡ, giúp người chơi dễ dàng nhận ra hình ảnh quen thuộc.
 */
public class HubUpgradeStation extends HubStation {

    public static final int MAX_HUB_LEVEL = 10;
    private double animTimer = 0.0;

    public HubUpgradeStation(double x, double y) {
        super(x, y, 64.0, 48.0, "Bàn Nâng Cấp (Max Lv10)", new Color(50, 220, 140));
    }

    @Override
    public void update(double deltaTime) {
        animTimer += deltaTime * 2.5;
    }

    @Override
    public String getActionPrompt() {
        return "Nâng cấp vũ khí (+1 Lv / Free)";
    }

    @Override
    public void onInteract(HubRangeState hub) {
        Weapon current = hub.getWeaponManager().getActiveWeapon();
        if (current == null || current.isMelee() || current.getSlotIndex() == 3 || current.getSlotIndex() == 4) {
            hub.showNotification("CHỈ CÓ THỂ NÂNG CẤP VŨ KHÍ CHÍNH (SLOT 1 & 2)!");
            return;
        }

        if (current.getLevel() >= MAX_HUB_LEVEL) {
            hub.showNotification(current.getName() + " ĐÃ ĐẠT CẤP TỐI ĐA (LV." + MAX_HUB_LEVEL + ")!");
            return;
        }

        // Mở Modal Nâng Cấp Súng để người chơi chủ động chọn 1 trong 3 chỉ số
        hub.openWeaponUpgradeModal(current, selectedStat -> {
            executeUpgrade(hub, current, selectedStat);
        });
    }

    /**
     * Thực thi nâng cấp trực tiếp (dùng cho callback modal hoặc gọi trực tiếp).
     */
    public void executeUpgrade(HubRangeState hub, Weapon weapon, Weapon.UpgradeStatType selectedStat) {
        if (hub == null || weapon == null) return;
        if (weapon.getLevel() >= MAX_HUB_LEVEL) {
            hub.showNotification(weapon.getName() + " ĐÃ ĐẠT CẤP TỐI ĐA (LV." + MAX_HUB_LEVEL + ")!");
            return;
        }
        if (selectedStat != null) {
            weapon.upgradeChosenStat(selectedStat, true);
        } else {
            weapon.setLevel(weapon.getLevel() + 1);
            weapon.addRandomStar();
            weapon.recalculateStatsFromStars();
        }
        String statName = selectedStat != null ? weapon.getStatDisplayName(selectedStat) : "Ngẫu nhiên";
        hub.showNotification("ĐÃ NÂNG CẤP " + weapon.getShortDisplayName() + "! (+1 SAO " + statName + ")");
    }

    @Override
    protected void renderStationContent(Graphics2D g, int sx, int sy, int halfW, int halfH, boolean playerNear) {
        // --- Mô hình Trạm Nâng Cấp Bio-Tech / Công nghệ cao từ gameplay chính ---
        int cardW = 54;
        int cardH = 34;
        int cx = sx - cardW / 2;
        int cy = sy - cardH / 2;

        // 1. Vòng phát quang năng lượng dưới chân bệ máy
        float pulse = (float) (0.55 + 0.45 * Math.sin(animTimer));
        int glowAlpha = (int) (80 * pulse);
        g.setColor(new Color(50, 220, 140, Math.clamp(glowAlpha, 20, 100)));
        g.fillOval(sx - 26, sy - 18, 52, 36);

        // 2. Khung bệ máy bát giác bo góc dã chiến màu xám xanh đậm (Bio-Tech Base)
        g.setColor(new Color(24, 38, 34));
        g.fillRoundRect(cx, cy, cardW, cardH, 8, 8);

        // Nẹp viền Neon Emerald công nghệ cao
        g.setColor(playerNear ? new Color(90, 255, 175) : new Color(50, 210, 135));
        g.setStroke(new BasicStroke(1.8f));
        g.drawRoundRect(cx, cy, cardW, cardH, 8, 8);

        // 3. Hai rãnh mạch dẫn năng lượng công nghệ hai bên (Circuit Traces)
        g.setColor(new Color(60, 230, 150, (int) (120 + 90 * pulse)));
        g.drawLine(cx + 4, sy - 6, cx + 12, sy - 6);
        g.drawLine(cx + 4, sy + 6, cx + 12, sy + 6);
        g.drawLine(cx + cardW - 12, sy - 6, cx + cardW - 4, sy - 6);
        g.drawLine(cx + cardW - 12, sy + 6, cx + cardW - 4, sy + 6);

        // 4. Lõi năng lượng trung tâm phát sáng
        int coreR = 8;
        g.setColor(new Color(220, 255, 240));
        g.fillOval(sx - coreR, sy - coreR, coreR * 2, coreR * 2);
        g.setColor(new Color(40, 200, 130));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(sx - coreR, sy - coreR, coreR * 2, coreR * 2);

        // Biểu tượng chevron tăng cấp ở tâm lõi
        g.setColor(new Color(20, 100, 60));
        g.drawLine(sx - 3, sy + 1, sx, sy - 3);
        g.drawLine(sx, sy - 3, sx + 3, sy + 1);

        // 5. Chữ nhãn "LV 10" nhỏ gọn ở phía dưới
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(180, 255, 210));
        String label = "LV 10";
        g.drawString(label, sx - fm.stringWidth(label) / 2, cy + cardH - 2);
    }
}
