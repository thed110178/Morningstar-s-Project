package com.groupproject.game.entity.station;

import com.groupproject.game.core.Camera;
import com.groupproject.game.entity.buff.CharacterBuff;
import com.groupproject.game.entity.buff.PlayerBuffManager;
import com.groupproject.game.state.PlayingState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/**
 * Trạm Cường Hóa Công Nghệ Sinh Học đặt trong Phòng Nâng Cấp Nhân Vật.
 *
 * <p>WHY: Hiện thực hóa 2 ô tương tác riêng biệt theo yêu cầu của người chơi:
 * <ul>
 * <li>Ô Nâng Cấp Chỉ Số Cơ Bản (BASIC_STATS): Cung cấp 3 buff cơ bản ngẫu nhiên, mua bằng Credits giá vừa phải.</li>
 * <li>Ô Cường Hóa Kỹ Năng Đặc Biệt (SPECIAL_PERKS): Cung cấp 3 buff đặc biệt ngẫu nhiên, mua bằng Credits giá cao.</li>
 * </ul>
 */
public class CharacterUpgradeStation {

    public enum StationType {
        BASIC_STATS("NÂNG CẤP CHỈ SỐ CƠ BẢN", new Color(40, 240, 180), new Color(15, 60, 45)),
        SPECIAL_PERKS("CƯỜNG HÓA KỸ NĂNG ĐẶC BIỆT", new Color(255, 175, 40), new Color(70, 45, 10));

        public final String titleVi;
        public final Color accentColor;
        public final Color baseColor;

        StationType(String titleVi, Color accentColor, Color baseColor) {
            this.titleVi = titleVi;
            this.accentColor = accentColor;
            this.baseColor = baseColor;
        }
    }

    private final double x;
    private final double y;
    private final StationType type;
    private final double interactionRadius = 60.0;

    private List<CharacterBuff> offeredBuffs = new ArrayList<>();
    private double animTimer = 0.0;

    public CharacterUpgradeStation(double x, double y, StationType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public StationType getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public List<CharacterBuff> getOfferedBuffs() {
        return offeredBuffs;
    }

    /**
     * Rút ngẫu nhiên 3 buff từ kho chỉ số của loại trạm này.
     */
    public void rollBuffs(PlayerBuffManager buffManager) {
        if (buffManager == null) return;
        CharacterBuff.BuffCategory cat = (type == StationType.BASIC_STATS)
                ? CharacterBuff.BuffCategory.BASIC_STAT
                : CharacterBuff.BuffCategory.SPECIAL;
        this.offeredBuffs = buffManager.getRandomOfferedBuffs(cat, 3);
    }

    /**
     * Đảm bảo trạm đã rút đủ 3 buff nếu chưa có hoặc danh sách đang trống.
     * Nếu đã có buff thì bảo lưu nguyên vẹn các lựa chọn hiện tại để người chơi không bị reroll lại khi đóng/mở UI.
     */
    public void ensureBuffsRolled(PlayerBuffManager buffManager) {
        if (offeredBuffs == null || offeredBuffs.isEmpty()) {
            rollBuffs(buffManager);
        }
    }

    public void update(double deltaTime) {
        animTimer += deltaTime * 2.5;
    }

    public boolean isPlayerInRange(double playerX, double playerY, double playerSize) {
        return Math.hypot(playerX - x, playerY - y) <= (interactionRadius + playerSize / 2.0);
    }

    /**
     * Mua một buff tại vị trí chỉ định (index 0, 1 hoặc 2 trong danh sách offeredBuffs).
     *
     * @return {@code true} nếu mua thành công và trừ Credits; {@code false} nếu thiếu tiền hoặc đã max.
     */
    public boolean purchaseBuff(int index, PlayingState state) {
        if (state == null || offeredBuffs == null || index < 0 || index >= offeredBuffs.size()) {
            return false;
        }

        CharacterBuff buff = offeredBuffs.get(index);
        PlayerBuffManager buffManager = state.getPlayer().getBuffManager();
        int currentLvl = buffManager.getLevel(buff);
        int cost = buff.getCostForLevel(currentLvl, state.getCurrentStage());

        if (cost <= 0 || state.getPlayerCredits() < cost || buffManager.isMaxLevel(buff)) {
            return false;
        }

        // Trừ tiền và áp dụng buff
        state.deductCredits(cost);
        boolean applied = buffManager.applyBuff(buff, state.getPlayer(), state.getWeaponManager());
        if (applied) {
            // Nếu buff đã max cấp sau khi mua, refresh lại ô đó nếu còn buff khác
            if (buffManager.isMaxLevel(buff)) {
                rollBuffs(buffManager);
            }
            return true;
        }
        return false;
    }

    /**
     * Kết xuất mô hình trạm nâng cấp 2D công nghệ cao trong không gian hầm ngục.
     */
    public void render(Graphics2D g, Camera camera, double playerX, double playerY, double playerSize) {
        int screenX = camera.worldToScreenX(x);
        int screenY = camera.worldToScreenY(y);

        // Viewport Culling: Bỏ qua nếu trạm nằm ngoài tầm nhìn camera
        if (screenX < -50 || screenX > com.groupproject.game.core.GameConfig.SCREEN_WIDTH + 50 ||
            screenY < -50 || screenY > com.groupproject.game.core.GameConfig.SCREEN_HEIGHT + 50) {
            return;
        }

        // 1. Bóng đổ dã chiến
        g.setColor(new Color(0, 0, 0, 110));
        g.fillOval(screenX - 26, screenY - 14, 52, 28);

        // 2. Vòng phát quang năng lượng dưới chân bệ
        float pulse = (float) (0.55 + 0.45 * Math.sin(animTimer));
        Color glow = new Color(
                type.accentColor.getRed(),
                type.accentColor.getGreen(),
                type.accentColor.getBlue(),
                (int) (90 * pulse)
        );
        g.setColor(glow);
        g.fillOval(screenX - 24, screenY - 24, 48, 48);

        // 3. Khung bệ máy bát giác dã chiến
        g.setColor(type.baseColor);
        g.fillRoundRect(screenX - 20, screenY - 20, 40, 40, 10, 10);
        g.setColor(type.accentColor);
        g.setStroke(new BasicStroke(2.0f));
        g.drawRoundRect(screenX - 20, screenY - 20, 40, 40, 10, 10);
        g.setStroke(new BasicStroke(1.0f));

        // 4. Lõi năng lượng / Biểu tượng trung tâm
        int coreRadius = 8;
        g.setColor(Color.WHITE);
        g.fillOval(screenX - coreRadius, screenY - coreRadius, coreRadius * 2, coreRadius * 2);
        g.setColor(type.accentColor);
        g.drawOval(screenX - coreRadius, screenY - coreRadius, coreRadius * 2, coreRadius * 2);

        // Biểu tượng chữ cái viết tắt (C: Cơ bản, Đ: Đặc biệt)
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.setColor(Color.BLACK);
        String label = (type == StationType.BASIC_STATS) ? "STAT" : "PERK";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, screenX - fm.stringWidth(label) / 2, screenY + 4);

        // 5. Gợi ý tương tác phím [F] khi người chơi bước vào bán kính kích hoạt
        boolean inRange = isPlayerInRange(playerX, playerY, playerSize);
        if (inRange) {
            renderTooltip(g, screenX, screenY);
        }
    }

    private void renderTooltip(Graphics2D g, int screenX, int screenY) {
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();

        String line1 = "[F] " + type.titleVi;
        String line2 = (type == StationType.BASIC_STATS)
                ? "3 Buff Chỉ Số (Giá vừa)"
                : "3 Buff Đặc Biệt (Giá cao)";

        int w1 = fm.stringWidth(line1);
        int w2 = fm.stringWidth(line2);
        int boxW = Math.max(w1, w2) + 24;
        int boxH = 46;
        int boxX = screenX - boxW / 2;
        int boxY = screenY - 58;

        // Khung nền tooltip
        g.setColor(new Color(15, 20, 32, 230));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setColor(type.accentColor);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setStroke(new BasicStroke(1.0f));

        // Chữ dòng 1
        g.setColor(Color.WHITE);
        g.drawString(line1, boxX + (boxW - w1) / 2, boxY + 18);

        // Chữ dòng 2
        g.setColor(type.accentColor);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics fm2 = g.getFontMetrics();
        g.drawString(line2, boxX + (boxW - fm2.stringWidth(line2)) / 2, boxY + 36);
    }
}
