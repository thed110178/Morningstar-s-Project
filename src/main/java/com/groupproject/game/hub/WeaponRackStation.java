package com.groupproject.game.hub;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import com.groupproject.game.core.GameFontManager;
import com.groupproject.game.entity.item.WeaponChest;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.entity.weapon.WeaponSpriteManager;
import com.groupproject.game.input.KeyBindings;
import com.groupproject.game.input.KeyBindings.GameAction;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.util.function.Supplier;

/**
 * Giá trưng bày và cung ứng vũ khí (Tactical Weapon Showcase) trong The Range.
 *
 * <p>WHY: Thể hiện vũ khí nổi lơ lửng trên đĩa hào quang sáng ấm tương phản cao,
 * đồng bộ 100% với phong cách vũ khí rơi chiến lợi phẩm ngoài gameplay Dungeon chính.
 * Giúp người chơi dễ dàng quan sát ngoại hình súng, xem chi tiết 5 hàng chỉ số đo bằng sao
 * và nhặt thử nghiệm miễn phí trước khi bước vào trận chiến.
 */
public class WeaponRackStation extends HubStation {

    private final String weaponCode;
    private final Supplier<Weapon> weaponFactory;
    private final String spriteName;
    private final Weapon previewWeapon;
    private double bobbingTimer = 0.0;

    public WeaponRackStation(double x, double y, String weaponCode, String displayName,
                             String spriteName, Supplier<Weapon> weaponFactory) {
        super(x, y, 64.0, 64.0, displayName, new Color(255, 215, 60));
        this.weaponCode = weaponCode;
        this.spriteName = spriteName;
        this.weaponFactory = weaponFactory;
        this.previewWeapon = (weaponFactory != null) ? weaponFactory.get() : null;
    }

    @Override
    public String getActionPrompt() {
        String shortName = (previewWeapon != null) ? previewWeapon.getShortDisplayName() : title;
        return "Trang bị " + shortName + " (Free)";
    }

    @Override
    public void onInteract(HubRangeState hub) {
        Weapon newWp = weaponFactory.get();
        if (newWp == null) return;
        hub.equipWeaponFromRack(newWp);
    }

    @Override
    public void update(double deltaTime) {
        bobbingTimer += deltaTime * 2.8;
    }

    @Override
    public void render(Graphics2D g, Camera camera, boolean playerNear) {
        int screenX = camera.worldToScreenX(x);
        int screenY = camera.worldToScreenY(y);

        // Viewport Culling: Bỏ qua hoàn toàn nếu trạm nằm ngoài tầm nhìn camera
        if (screenX < -80 || screenX > GameConfig.SCREEN_WIDTH + 80 ||
            screenY < -80 || screenY > GameConfig.SCREEN_HEIGHT + 80) {
            return;
        }

        Stroke oldStroke = g.getStroke();
        Color oldColor = g.getColor();
        Font oldFont = g.getFont();

        // 1. Bóng đổ dã chiến mềm mại trên mặt sàn bên dưới
        g.setColor(new Color(0, 0, 0, 85));
        g.fillOval(screenX - 24, screenY + 22, 48, 14);

        // 2. Thân hòm tiếp tế quân sự đã mở nắp (Supply Chest Crate) - đồng bộ 100% với vũ khí rơi trong Dungeon
        int crateW = 42;
        int crateH = 24;
        int crateX = screenX - crateW / 2;
        int crateY = screenY + 4;

        // Thân hòm gỗ mở viền vàng kim
        g.setColor(new Color(110, 80, 25));
        g.fillRoundRect(crateX, crateY, crateW, crateH, 6, 6);
        g.setColor(new Color(180, 140, 50));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(crateX, crateY, crateW, crateH, 6, 6);

        // Nắp hòm mở lệch lên trên
        g.setColor(new Color(160, 120, 40));
        g.fillRect(crateX - 3, crateY - 5, crateW + 6, 7);
        g.setColor(new Color(255, 220, 100));
        g.drawRect(crateX - 3, crateY - 5, crateW + 6, 7);

        // 3. Vị trí tâm súng nhô cao hơn miệng hòm và lơ lửng dập dềnh
        int bobY = (int) (4 * Math.sin(bobbingTimer));
        int weaponCenterY = screenY - 14 + bobY;

        // 4. ĐĨA NỀN SÁNG TƯƠNG PHẢN CAO (High-contrast radiant aura):
        // Giúp thân súng màu tối nổi bần bật, không bị chìm vào nền đất/cỏ của The Range
        int basePulse = (int) (3 * Math.sin(bobbingTimer * 1.5));
        // 4a. Quầng hào quang khuếch tán bên ngoài
        g.setColor(new Color(255, 230, 110, 55));
        g.fillOval(screenX - 36 - basePulse, weaponCenterY - 26 - basePulse, (36 + basePulse) * 2, (26 + basePulse) * 2);
        // 4b. Đĩa sáng năng lượng vàng nhạt rực rỡ
        g.setColor(new Color(255, 245, 180, 160));
        g.fillOval(screenX - 28, weaponCenterY - 20, 56, 40);
        // 4c. Lõi ánh sáng trắng sáng tương phản trực tiếp sau lưng thân súng
        g.setColor(new Color(255, 255, 245, 230));
        g.fillOval(screenX - 20, weaponCenterY - 14, 40, 28);

        Weapon weaponToRender = (previewWeapon != null) ? previewWeapon : weaponFactory.get();

        // 5. HIỆU ỨNG ĐẶC BIỆT NẾU LÀ KHÍ TÀI HẠNG S (S-TIER LEGENDARY AURA)
        if (weaponToRender != null && weaponToRender.isSTier()) {
            int pulse = (int) (6 * Math.sin(bobbingTimer * 2.5));
            int auraRadius = 38 + pulse;
            g.setColor(new Color(255, 215, 0, Math.clamp(85 + pulse * 4, 40, 140)));
            g.fillOval(screenX - auraRadius, weaponCenterY - auraRadius, auraRadius * 2, auraRadius * 2);

            // Tia sáng năng lượng lấp lánh (Sparkles) 4 góc
            g.setColor(new Color(255, 255, 200, 220));
            int sparkDist = 28 + pulse / 2;
            g.fillRect(screenX - 1, weaponCenterY - sparkDist, 3, 7);
            g.fillRect(screenX - 1, weaponCenterY + sparkDist - 6, 3, 7);
            g.fillRect(screenX - sparkDist, weaponCenterY - 1, 7, 3);
            g.fillRect(screenX + sparkDist - 6, weaponCenterY - 1, 7, 3);

            // Ký hiệu huy hiệu [S] viễn tưởng bay lơ lửng góc trên súng
            int badgeX = screenX + 26;
            int badgeY = weaponCenterY - 24;
            g.setColor(new Color(30, 20, 5, 230));
            g.fillRoundRect(badgeX - 7, badgeY - 7, 15, 15, 4, 4);
            g.setColor(new Color(255, 215, 0));
            g.setStroke(new BasicStroke(1.2f));
            g.drawRoundRect(badgeX - 7, badgeY - 7, 15, 15, 4, 4);
            g.setFont(GameFontManager.getBoldFont(10.0f));
            g.drawString("S", badgeX - 3, badgeY + 4);
        }

        // 6. VẼ MÔ HÌNH VŨ KHÍ NỔI LƠ LỬNG: Phóng to lên 68x68 pixel to rõ, đẹp mắt
        if (weaponToRender != null) {
            WeaponSpriteManager.renderWeaponSprite(g, weaponToRender, screenX, weaponCenterY, 68, 68);
        } else {
            Image sprite = WeaponSpriteManager.getSprite(spriteName);
            if (sprite != null) {
                g.drawImage(sprite, screenX - 34, weaponCenterY - 34, 68, 68, null);
            }
        }

        // 7. HIỂN THỊ THẺ THÔNG TIN & HƯỚNG DẪN TƯƠNG TÁC
        if (playerNear) {
            // Khi người chơi lại gần: Hiện thẻ chỉ số chi tiết (Weapon Stat Card) y như khi nhặt súng trong hòm / quầy Shop
            String interactKey = KeyBindings.getKeyName(KeyBindings.getKeyCode(GameAction.INTERACT));
            String promptText = "[" + interactKey + "] TRANG BỊ " + (weaponToRender != null ? weaponToRender.getShortDisplayName().toUpperCase() : title.toUpperCase()) + " (FREE)";
            if (weaponToRender != null) {
                WeaponChest.renderWeaponStatCard(g, screenX, weaponCenterY - 38, weaponToRender, promptText);
            }
        } else {
            // Khi đứng xa: Nhãn mác nổi bo góc Sci-Fi với chữ vàng rực tên vũ khí
            String weaponTitle = (weaponToRender != null ? weaponToRender.getShortDisplayName().toUpperCase() : title.toUpperCase());
            renderFloatingTag(g, screenX, weaponCenterY - 32, weaponTitle, new Color(255, 220, 75));
        }

        g.setStroke(oldStroke);
        g.setColor(oldColor);
        g.setFont(oldFont);
    }

    private void renderFloatingTag(Graphics2D g, int centerX, int centerY, String text, Color textColor) {
        String clean = GameFontManager.sanitize(text);
        g.setFont(GameFontManager.getSmallBoldFont(10.0f));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(clean);
        int pad = 8;

        int boxW = textW + pad * 2;
        int boxH = 20;
        int boxX = centerX - boxW / 2;
        int boxY = centerY - boxH / 2;

        g.setColor(new Color(15, 22, 34, 215));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 6, 6);
        g.setColor(new Color(75, 100, 135));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 6, 6);

        g.setColor(textColor);
        g.drawString(clean, boxX + pad, boxY + 14);
    }

    @Override
    protected void renderStationContent(Graphics2D g, int sx, int sy, int halfW, int halfH, boolean playerNear) {
        // Đã vẽ toàn bộ trong render()
    }

    public String getWeaponCode() {
        return weaponCode;
    }

    public Weapon getPreviewWeapon() {
        return previewWeapon;
    }
}
