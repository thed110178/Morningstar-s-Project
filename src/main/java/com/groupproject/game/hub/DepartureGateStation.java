package com.groupproject.game.hub;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Cổng xuất kích vào trận (Departure Gate / Portal) trong The Range.
 *
 * <p>WHY: Là điểm chuyển tiếp mượt mà từ khu căn cứ The Range vào màn chơi Dungeon thực tế,
 * mang theo toàn bộ trang bị, cấp độ vũ khí mà người chơi đã lựa chọn và thử nghiệm.
 */
public class DepartureGateStation extends HubStation {

    private double animPulse = 0.0;

    public DepartureGateStation(double x, double y) {
        super(x, y, 76.0, 50.0, "Cổng Xuất Kích (Vào Trận)", new Color(80, 220, 255));
        this.interactionRadius = 60.0;
    }

    @Override
    public void update(double deltaTime) {
        animPulse += deltaTime * 4.0;
    }

    @Override
    public String getActionPrompt() {
        return "Bắt đầu chiến dịch Dungeon (Vào trận)";
    }

    @Override
    public void onInteract(HubRangeState hub) {
        hub.startCampaignFromHub();
    }

    @Override
    protected void renderStationContent(Graphics2D g, int sx, int sy, int halfW, int halfH, boolean playerNear) {
        // Cổng không gian phát quang màu Cyan / Xanh neon
        double pulse = Math.sin(animPulse) * 0.5 + 0.5; // [0, 1]

        // Vòm cổng
        g.setColor(new Color(30, 45, 60));
        g.fillRoundRect(sx - halfW + 4, sy - halfH + 4, (int) width - 8, (int) height - 8, 12, 12);

        // Hào quang portal trung tâm
        int glowAlpha = (int) (100 + pulse * 120);
        g.setColor(new Color(60, 210, 255, glowAlpha));
        g.fillOval(sx - 18, sy - 14, 36, 28);

        // Các vòng sóng năng lượng dịch chuyển
        g.setColor(new Color(255, 255, 255, (int) (140 + pulse * 115)));
        g.setStroke(new BasicStroke(2.0f));
        g.drawOval(sx - 12, sy - 9, 24, 18);

        // Đèn viền cổng
        g.setColor(playerNear ? new Color(100, 255, 220) : new Color(70, 180, 240));
        g.drawRoundRect(sx - halfW + 4, sy - halfH + 4, (int) width - 8, (int) height - 8, 12, 12);

        // Chữ "ENTER"
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.WHITE);
        g.drawString("VÀO TRẬN", sx - fm.stringWidth("VÀO TRẬN") / 2, sy + 18);
    }
}
