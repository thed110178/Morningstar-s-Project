package com.groupproject.game.hub;

import com.groupproject.game.entity.weapon.Weapon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Trạm tái rèn / Reroll sao và chí mạng miễn phí (Free Reforge Station) trong The Range.
 *
 * <p>WHY: Cho phép người chơi gieo lại cấu hình sao và tỉ lệ chí mạng theo ý muốn để thử nghiệm
 * các hướng build khác nhau (tối đa DMG, tối đa RPM, hoặc tối đa ACC).
 */
public class HubRerollStation extends HubStation {

    public HubRerollStation(double x, double y) {
        super(x, y, 54.0, 54.0, "Trạm Tái Rèn (Reroll)", new Color(180, 80, 255));
    }

    @Override
    public String getActionPrompt() {
        return "Tái rèn sao & chí mạng (Free)";
    }

    @Override
    public void onInteract(HubRangeState hub) {
        Weapon current = hub.getWeaponManager().getActiveWeapon();
        if (current == null || current.isMelee() || current.getSlotIndex() == 3 || current.getSlotIndex() == 4) {
            hub.showNotification("CHỈ CÓ THỂ TÁI RÈN VŨ KHÍ CHÍNH (SLOT 1 & 2)!");
            return;
        }

        // Mở Modal Reroll Súng với 2 lựa chọn Reroll Sao & Reroll Chí Mạng riêng biệt
        hub.openStatReforgeModal(current);
    }

    public Weapon.ReforgeResult executeReforgeStars(HubRangeState hub, Weapon weapon) {
        if (weapon == null) return null;
        if (weapon.getLevel() <= 1) {
            if (hub != null) {
                hub.showNotification("VŨ KHÍ LV.1 CHƯA CÓ SAO NÂNG CẤP ĐỂ REROLL!");
            }
            return null;
        }
        Weapon.ReforgeResult res = weapon.reforgeStarsOnly();
        if (res == null) return null;
        if (hub != null) {
            hub.showNotification("★ REROLL SAO: DMG " + res.newDmgStars() + "★ | RPM " + res.newFrStars() + "★ | ACC " + res.newAccStars() + "★ ★");
        }
        return res;
    }

    public Weapon.ReforgeResult executeReforgeCrit(HubRangeState hub, Weapon weapon) {
        if (weapon == null) return null;
        Weapon.ReforgeResult res = weapon.reforgeCritOnly();
        if (hub != null) {
            hub.showNotification("★ REROLL CHÍ MẠNG: CRIT " + res.newCritPercent() + "% (x" + String.format(java.util.Locale.US, "%.2f", weapon.getCritMultiplier()) + ") ★");
        }
        return res;
    }

    public Weapon.ReforgeResult executeReforge(HubRangeState hub, Weapon weapon) {
        if (weapon == null) return null;
        Weapon.ReforgeResult res = weapon.reforge();
        if (hub != null) {
            hub.showNotification("TÁI RÈN THÀNH CÔNG! DMG: " + res.newDmgStars() + "★ | RPM: " + res.newFrStars()
                    + "★ | ACC: " + res.newAccStars() + "★ | CRIT: " + res.newCritPercent() + "%");
        }
        return res;
    }

    @Override
    protected void renderStationContent(Graphics2D g, int sx, int sy, int halfW, int halfH, boolean playerNear) {
        // Biểu tượng vòng xoay xúc xắc / 2 mũi tên xoay tròn (Reroll loop)
        g.setColor(new Color(200, 120, 255));
        g.setStroke(new BasicStroke(2.2f));

        int r = 11;
        g.drawArc(sx - r, sy - r - 2, r * 2, r * 2, 45, 270);
        // Đầu mũi tên xoay
        g.drawLine(sx + r - 2, sy - 2, sx + r + 3, sy - 5);
        g.drawLine(sx + r - 2, sy - 2, sx + r - 3, sy - 8);

        // Chữ REROLL
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(230, 190, 255));
        g.drawString("REROLL", sx - fm.stringWidth("REROLL") / 2, sy + 18);
    }
}
