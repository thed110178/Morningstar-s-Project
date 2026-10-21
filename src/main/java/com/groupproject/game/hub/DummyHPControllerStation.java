package com.groupproject.game.hub;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Bảng điều khiển lượng máu Dummy di động (Dummy HP Controller Station) trong The Range.
 *
 * <p>Cho phép người chơi điều chỉnh mức HP của các Dummy lạng lách bên trái:
 * 100 HP (quái thường) -> 250 HP (quái tinh anh) -> 500 HP (Mini-Boss) -> 1000 HP (Titan Boss).
 */
public class DummyHPControllerStation extends HubStation {

    private static final int[] HP_PRESETS = {100, 250, 500, 1000};
    private int currentPresetIndex = 0;

    public DummyHPControllerStation(double x, double y) {
        super(x, y, 54.0, 54.0, "Điều Chỉnh HP Dummy", new Color(255, 160, 40));
    }

    public int getCurrentHealthPreset() {
        return HP_PRESETS[currentPresetIndex];
    }

    @Override
    public String getActionPrompt() {
        int nextPreset = HP_PRESETS[(currentPresetIndex + 1) % HP_PRESETS.length];
        return "Đổi HP: " + getCurrentHealthPreset() + " -> " + nextPreset + " HP";
    }

    @Override
    public void onInteract(HubRangeState hub) {
        currentPresetIndex = (currentPresetIndex + 1) % HP_PRESETS.length;
        int newHp = getCurrentHealthPreset();
        hub.updateStrafingDummiesHealth(newHp);
        hub.showNotification("ĐÃ THIẾT LẬP MÁU DUMMY DI ĐỘNG: " + newHp + " HP!");
    }

    @Override
    protected void renderStationContent(Graphics2D g, int sx, int sy, int halfW, int halfH, boolean playerNear) {
        // Biểu tượng trái tim / thanh máu
        g.setColor(new Color(255, 175, 50));
        g.setStroke(new BasicStroke(2.0f));

        // Khung thanh máu mô phỏng
        int bw = 24;
        int bh = 8;
        g.drawRoundRect(sx - bw / 2, sy - 8, bw, bh, 3, 3);
        g.setColor(new Color(255, 80, 80));
        g.fillRoundRect(sx - bw / 2 + 2, sy - 6, bw - 4, bh - 4, 2, 2);

        // Hiển thị con số HP hiện tại
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        String hpStr = getCurrentHealthPreset() + " HP";
        g.setColor(Color.WHITE);
        g.drawString(hpStr, sx - fm.stringWidth(hpStr) / 2, sy + 15);
    }
}
