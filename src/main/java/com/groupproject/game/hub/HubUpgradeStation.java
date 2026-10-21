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
 * <p>WHY: Cho phép người chơi thử nghiệm sức mạnh của súng ở các mốc cấp độ cao (lên tới Lv.10)
 * với các cấu hình sao nâng cấp khác nhau trước khi bước vào hầm ngục.
 */
public class HubUpgradeStation extends HubStation {

    public static final int MAX_HUB_LEVEL = 10;

    public HubUpgradeStation(double x, double y) {
        super(x, y, 54.0, 54.0, "Bàn Nâng Cấp (Max Lv10)", new Color(60, 220, 120));
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

        // Tăng cấp độ và cộng sao ngẫu nhiên
        int oldLvl = current.getLevel();
        current.setLevel(oldLvl + 1);
        String statName = current.addRandomStar();
        current.recalculateStatsFromStars();

        hub.showNotification("ĐÃ NÂNG CẤP " + current.getShortDisplayName() + "! (+1 SAO " + statName + ")");
    }

    @Override
    protected void renderStationContent(Graphics2D g, int sx, int sy, int halfW, int halfH, boolean playerNear) {
        // Vẽ biểu tượng búa nâng cấp / mũi tên sao cấp độ
        g.setColor(new Color(60, 220, 120));
        g.setStroke(new BasicStroke(2.2f));

        // Mũi tên tăng cấp hướng lên (Chevron)
        int arrowW = 12;
        g.drawLine(sx - arrowW, sy + 2, sx, sy - 10);
        g.drawLine(sx, sy - 10, sx + arrowW, sy + 2);
        g.drawLine(sx - arrowW, sy + 10, sx, sy - 2);
        g.drawLine(sx, sy - 2, sx + arrowW, sy + 10);

        // Chữ "UPGRADE"
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(180, 255, 200));
        g.drawString("LV 10", sx - fm.stringWidth("LV 10") / 2, sy + 20);
    }
}
