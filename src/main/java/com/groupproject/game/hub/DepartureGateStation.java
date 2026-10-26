package com.groupproject.game.hub;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Cầu Thang Xuất Kích Vào Trận (Combat Deployment Staircase / Bunker Hatch) trong The Range.
 *
 * <p>WHY: Thay thế cổng không gian viễn tưởng bằng cầu thang bộ dã chiến kiên cố có chiều sâu 3D,
 * tạo cảm giác Operative bước xuống đường hầm bí mật để thâm nhập vào chiến trường hầm ngục Dungeon.
 */
public class DepartureGateStation extends HubStation {

    private double animPulse = 0.0;

    public DepartureGateStation(double x, double y) {
        super(x, y, 80.0, 54.0, "Cầu Thang Xuất Kích (Vào Trận)", new Color(255, 205, 55));
        this.interactionRadius = 60.0;
    }

    @Override
    public void update(double deltaTime) {
        animPulse += deltaTime * 3.5;
    }

    @Override
    public String getActionPrompt() {
        return "Bước xuống cầu thang (Vào trận)";
    }

    @Override
    public void onInteract(HubRangeState hub) {
        hub.startCampaignFromHub();
    }

    @Override
    protected void renderStationContent(Graphics2D g, int sx, int sy, int halfW, int halfH, boolean playerNear) {
        // 1. Khung miệng hầm cầu thang kim loại dã chiến (Heavy-duty bunker hatch frame)
        int hatchW = (int) width - 10;
        int hatchH = (int) height - 10;
        int hx = sx - hatchW / 2;
        int hy = sy - hatchH / 2;

        // Viền bê tông cốt thép màu xám đen bao quanh miệng hầm
        g.setColor(new Color(25, 28, 35));
        g.fillRoundRect(hx - 2, hy - 2, hatchW + 4, hatchH + 4, 6, 6);

        // Khung thép màu xám kim loại dã chiến
        g.setColor(new Color(48, 55, 68));
        g.fillRoundRect(hx, hy, hatchW, hatchH, 4, 4);

        // 2. Dải mép trên: Sọc cảnh báo quân sự Vàng - Đen (Military Hazard Stripes)
        int stripH = 6;
        g.setColor(new Color(230, 185, 30));
        g.fillRect(hx + 2, hy + 2, hatchW - 4, stripH);
        g.setColor(new Color(30, 30, 30));
        for (int stripeX = hx + 4; stripeX < hx + hatchW - 6; stripeX += 8) {
            int[] xs = { stripeX, stripeX + 4, stripeX + 2, stripeX - 2 };
            int[] ys = { hy + 2, hy + 2, hy + 2 + stripH, hy + 2 + stripH };
            g.fillPolygon(xs, ys, 4);
        }

        // 3. Lòng hầm cầu thang bộ dốc sâu xuống dưới (Descending 3D Stair Steps)
        int pitX = hx + 8;
        int pitY = hy + stripH + 3;
        int pitW = hatchW - 16;
        int pitH = hatchH - stripH - 7;

        // Nền lòng hầm đen sâu
        g.setColor(new Color(10, 12, 16));
        g.fillRect(pitX, pitY, pitW, pitH);

        // 5 bậc thang kim loại đổ dốc từ mép trên xuống sâu bên dưới
        int stepCount = 5;
        double stepH = pitH / (double) stepCount;

        for (int i = 0; i < stepCount; i++) {
            int stepTopY = (int) (pitY + i * stepH);
            int stepBottomY = (int) (pitY + (i + 1) * stepH);
            int sH = Math.max(1, stepBottomY - stepTopY);

            // Bậc càng ở trên càng sáng, càng xuống sâu càng tối dần vào bóng đêm
            int brightness = Math.max(20, 125 - i * 22);
            g.setColor(new Color(brightness, brightness + 5, brightness + 15));
            g.fillRect(pitX + 2, stepTopY, pitW - 4, sH);

            // Gờ mép bậc thang (Tread nosing) highlight sáng
            g.setColor(new Color(Math.min(255, brightness + 45), Math.min(255, brightness + 50), Math.min(255, brightness + 60)));
            g.drawLine(pitX + 2, stepTopY, pitX + pitW - 3, stepTopY);

            // Rãnh chống trượt trên mặt bậc thang
            if (sH >= 3) {
                g.setColor(new Color(Math.max(10, brightness - 30), Math.max(10, brightness - 30), Math.max(10, brightness - 25)));
                g.drawLine(pitX + 6, stepTopY + 1, pitX + pitW - 7, stepTopY + 1);
            }
        }

        // 4. Lan can tay vịn kim loại (Steel Handrails) hai bên mép cầu thang
        g.setColor(new Color(180, 190, 205));
        g.setStroke(new BasicStroke(1.8f));
        // Lan can trái
        g.drawLine(pitX + 1, pitY, pitX + 1, pitY + pitH);
        // Lan can phải
        g.drawLine(pitX + pitW - 2, pitY, pitX + pitW - 2, pitY + pitH);

        // Các mấu gắn lan can vào thành tường
        g.setColor(new Color(220, 230, 245));
        g.fillRect(pitX, pitY + 3, 3, 2);
        g.fillRect(pitX, pitY + pitH - 5, 3, 2);
        g.fillRect(pitX + pitW - 3, pitY + 3, 3, 2);
        g.fillRect(pitX + pitW - 3, pitY + pitH - 5, 3, 2);

        // 5. Mũi tên dạ quang chỉ hướng xuất kích trượt dần xuống bậc thang theo nhịp thở
        double pulsePos = (animPulse % 2.5) / 2.5; // [0.0 -> 1.0]
        int arrowY = (int) (pitY + 2 + pulsePos * (pitH - 8));
        int arrowAlpha = (int) (140 + 100 * Math.sin(pulsePos * Math.PI));
        g.setColor(playerNear ? new Color(80, 255, 160, Math.clamp(arrowAlpha, 50, 255))
                              : new Color(255, 215, 60, Math.clamp(arrowAlpha, 40, 240)));
        g.setStroke(new BasicStroke(1.5f));
        int arrX = sx;
        g.drawLine(arrX - 6, arrowY, arrX, arrowY + 4);
        g.drawLine(arrX, arrowY + 4, arrX + 6, arrowY);

        // 6. Bu lông tán đinh ở 4 góc khung thép
        g.setColor(new Color(160, 175, 195));
        g.fillRect(hx + 2, hy + 2, 2, 2);
        g.fillRect(hx + hatchW - 4, hy + 2, 2, 2);
        g.fillRect(hx + 2, hy + hatchH - 4, 2, 2);
        g.fillRect(hx + hatchW - 4, hy + hatchH - 4, 2, 2);

        // 7. Nhãn chữ "XUẤT KÍCH" nhỏ phía dưới cầu thang
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(playerNear ? new Color(120, 255, 190) : new Color(255, 230, 130));
        String label = "XUẤT KÍCH";
        g.drawString(label, sx - fm.stringWidth(label) / 2, hy + hatchH - 2);
    }
}
