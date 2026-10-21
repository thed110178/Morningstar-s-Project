package com.groupproject.game.hub;

import com.groupproject.game.entity.weapon.Weapon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Thùng rác dọn vũ khí (Trash Bin Station) trong The Range.
 *
 * <p>WHY: Giúp người chơi loại bỏ súng ở Slot 1 hoặc Slot 2 khi muốn thử nghiệm một vũ khí khác
 * hoặc dọn gọn trang bị mà không bị vướng.
 *
 * <p>Quy tắc bảo vệ: Tuyệt đối không cho phép bỏ súng lục phòng thân (Slot 3) và dao găm (Slot 4).
 */
public class TrashBinStation extends HubStation {

    public TrashBinStation(double x, double y) {
        super(x, y, 50.0, 50.0, "Thùng Rác Bỏ Súng", new Color(255, 80, 80));
    }

    @Override
    public String getActionPrompt() {
        return "Bỏ súng đang cầm vào thùng rác";
    }

    @Override
    public void onInteract(HubRangeState hub) {
        Weapon current = hub.getWeaponManager().getActiveWeapon();
        if (current == null) {
            hub.showNotification("BẠN ĐANG KHÔNG CẦM VŨ KHÍ NÀO!");
            return;
        }

        int slot = current.getSlotIndex();
        if (slot == 3 || slot == 4 || current.isMelee()) {
            hub.showNotification("KHÔNG THỂ VỨT BỎ SÚNG LỤC HOẶC DAO CẬN CHIẾN!");
            return;
        }

        hub.discardCurrentWeapon();
    }

    @Override
    protected void renderStationContent(Graphics2D g, int sx, int sy, int halfW, int halfH, boolean playerNear) {
        // Vẽ icon thùng rác
        g.setColor(new Color(255, 90, 90));
        g.setStroke(new BasicStroke(1.8f));

        // Nắp thùng rác
        g.drawLine(sx - 12, sy - 8, sx + 12, sy - 8);
        g.drawLine(sx - 4, sy - 11, sx + 4, sy - 11);

        // Thân thùng rác
        g.drawRoundRect(sx - 9, sy - 6, 18, 20, 3, 3);
        // Các vạch sọc dọc trên thân thùng rác
        g.drawLine(sx - 4, sy - 2, sx - 4, sy + 10);
        g.drawLine(sx, sy - 2, sx, sy + 10);
        g.drawLine(sx + 4, sy - 2, sx + 4, sy + 10);

        // Chữ TRASH
        g.setFont(new Font("SansSerif", Font.BOLD, 8));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(255, 170, 170));
        g.drawString("TRASH", sx - fm.stringWidth("TRASH") / 2, sy + 21);
    }
}
