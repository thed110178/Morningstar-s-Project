package com.groupproject.game.hub;

import com.groupproject.game.entity.weapon.Weapon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Trạm tái rèn / Reroll sao và chí mạng miễn phí (Free Reforge Station) trong The Range.
 *
 * <p>WHY: Lấy nguyên bản hình ảnh Bàn Kỹ Thuật Quân Khí Dã Chiến (Field Gunsmith Workbench)
 * từ gameplay hầm ngục chính (StatReforgeStation) để người chơi dễ dàng nhận biết và thử nghiệm
 * tính năng reroll sao chỉ số và tỷ lệ chí mạng quen thuộc.
 */
public class HubRerollStation extends HubStation {

    private double animTimer = 0.0;

    public HubRerollStation(double x, double y) {
        super(x, y, 68.0, 48.0, "Bàn Tái Rèn (Reroll)", new Color(245, 175, 45));
    }

    @Override
    public void update(double deltaTime) {
        animTimer += deltaTime * 2.2;
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
        // --- Mô hình Bàn Kỹ Thuật Quân Khí Dã Chiến (Field Gunsmith Workbench) từ gameplay chính ---
        int benchW = 56;
        int benchH = 32;
        int bx = sx - benchW / 2;
        int by = sy - benchH / 2;

        // 1. Khung thép màu xanh rêu quân đội (Olive Drab Steel Frame)
        g.setColor(new Color(48, 56, 46));
        g.fillRoundRect(bx, by, benchW, benchH, 6, 6);

        // 2. Mặt bàn gỗ ép chịu lực dày màu nâu sẫm dã chiến
        g.setColor(new Color(78, 64, 50));
        g.fillRect(bx + 3, by + 3, benchW - 6, benchH - 6);

        // 3. Nẹp viền thép kim loại chống mẻ mép bàn
        g.setColor(new Color(95, 105, 95));
        g.setStroke(new BasicStroke(1.4f));
        g.drawRoundRect(bx, by, benchW, benchH, 6, 6);

        // 4. Tấm lót vải/cao su tháo lắp súng màu vàng cát (Khaki Gunsmith Mat)
        g.setColor(new Color(135, 120, 85));
        g.fillRect(sx - 15, sy - 8, 30, 16);
        g.setColor(new Color(105, 92, 65));
        g.drawRect(sx - 15, sy - 8, 30, 16);

        // Thân súng trường đang được tháo bảo dưỡng trên tấm lót
        g.setColor(new Color(32, 35, 38));
        g.fillRect(sx - 11, sy - 2, 22, 4);
        g.setColor(new Color(85, 90, 95));
        g.fillRect(sx - 5, sy - 4, 8, 2); // Thoi nạp đạn

        // 5. Khối ê-tô cơ khí kẹp nòng súng bằng thép bên góc trái bàn
        g.setColor(new Color(55, 60, 65));
        g.fillRect(bx + 4, by + 4, 10, 10);
        g.setColor(new Color(150, 155, 160));
        g.fillRect(bx + 2, by + 7, 14, 3); // Mỏ kẹp
        g.setColor(new Color(200, 205, 210));
        g.fillRect(bx + 1, by + 5, 2, 7); // Tay vặn chữ T

        // 6. Hộp đựng dụng cụ cơ khí quân sự màu đỏ dã chiến bên góc phải
        g.setColor(new Color(165, 45, 38));
        g.fillRoundRect(bx + benchW - 15, by + 4, 11, 9, 2, 2);
        g.setColor(new Color(220, 215, 180));
        g.fillRect(bx + benchW - 10, by + 7, 3, 2); // Khóa kim loại

        // 7. Đèn bàn kỹ thuật dã chiến (Tactical Gooseneck Lamp)
        g.setColor(new Color(30, 32, 35));
        g.setStroke(new BasicStroke(2.0f));
        g.drawArc(bx + benchW - 9, by + benchH - 14, 12, 12, 90, 180);
        // Chao đèn xanh quân sự
        g.setColor(new Color(50, 70, 55));
        g.fillOval(sx + 9, sy + 2, 8, 6);

        // Quầng sáng vàng ấm dã chiến tỏa xuống mặt bàn nhấp nháy nhẹ
        int pulseAlpha = (int) (40 + 10 * Math.sin(animTimer));
        g.setColor(new Color(255, 235, 150, Math.clamp(pulseAlpha, 20, 60)));
        g.fillOval(sx - 20, sy - 12, 40, 24);

        // Biểu tượng REROLL nhỏ gọn ở góc dưới
        g.setFont(new Font("SansSerif", Font.BOLD, 8));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(255, 225, 120));
        g.drawString("REROLL", sx - fm.stringWidth("REROLL") / 2, by + benchH - 2);
    }
}
