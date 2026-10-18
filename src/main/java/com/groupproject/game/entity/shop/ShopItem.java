package com.groupproject.game.entity.shop;

import com.groupproject.game.core.Camera;
import com.groupproject.game.entity.item.WeaponChest;
import com.groupproject.game.entity.weapon.AssaultRifle;
import com.groupproject.game.entity.weapon.SMG;
import com.groupproject.game.entity.weapon.Shotgun;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.state.PlayingState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Vật phẩm hoặc quầy hàng tương tác bên trong Phòng Cửa Hàng (Shop Room).
 *
 * <p>WHY: Đóng gói logic buôn bán, khấu trừ tiền tệ (Credits), trưng bày mô hình 2D
 * và phân loại các mặt hàng chiến thuật (Vũ khí chính, Tiếp tế đạn, Hồi máu).
 * Theo yêu cầu thiết kế, mặt hàng HP chưa phát triển hệ thống máu người chơi nên được ẩn
 * hoàn toàn (available = false), không render ra giao diện.
 */
public class ShopItem {

    /**
     * Phân loại các mặt hàng có mặt trong Tactical Shop.
     */
    public enum ShopItemType {
        PRIMARY_WEAPON,
        WEAPON_UPGRADE,
        AMMO,
        HEALTH
    }

    private final double x;
    private final double y;
    private final ShopItemType type;
    private final int price;
    private final Weapon weapon; // Chỉ dành cho quầy PRIMARY_WEAPON

    private boolean purchased = false;
    private final boolean available; // true = hiển thị và bán; false = chưa phát triển, ẩn hoàn toàn
    private double bobbingTimer = 0.0;

    /**
     * Khởi tạo một quầy vật phẩm trong Shop.
     *
     * @param x         Tọa độ X thế giới của quầy.
     * @param y         Tọa độ Y thế giới của quầy.
     * @param type      Loại mặt hàng.
     * @param price     Giá bán tính bằng đơn vị Credits.
     * @param weapon    Vũ khí trưng bày (nếu là PRIMARY_WEAPON, ngược lại null).
     * @param available Trạng thái khả dụng (false sẽ ẩn hoàn toàn không render).
     */
    public ShopItem(double x, double y, ShopItemType type, int price, Weapon weapon, boolean available) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.price = price;
        this.weapon = weapon;
        this.available = available;
    }

    /**
     * Factory tạo quầy bán Súng Chính ngẫu nhiên (AR, Shotgun, hoặc SMG).
     */
    public static ShopItem createWeaponItem(double x, double y, Weapon weapon, int price) {
        return new ShopItem(x, y, ShopItemType.PRIMARY_WEAPON, price, weapon, true);
    }

    /**
     * Factory tạo bàn nâng cấp vũ khí (Weapon Upgrade Terminal).
     */
    public static ShopItem createUpgradeStation(double x, double y, int price) {
        return new ShopItem(x, y, ShopItemType.WEAPON_UPGRADE, price, null, true);
    }

    /**
     * Factory tạo quầy Tiếp tế đạn dược.
     */
    public static ShopItem createAmmoItem(double x, double y, int price) {
        return new ShopItem(x, y, ShopItemType.AMMO, price, null, true);
    }

    /**
     * Factory tạo quầy HP (chưa phát triển nên available = false).
     */
    public static ShopItem createHealthItem(double x, double y, int price) {
        return new ShopItem(x, y, ShopItemType.HEALTH, price, null, false);
    }

    public void update(double deltaTime) {
        bobbingTimer += deltaTime * 3.5;
    }

    /**
     * Kiểm tra người chơi có đang đứng trong phạm vi tương tác (bán kính ~55px).
     */
    public boolean isPlayerInRange(double playerX, double playerY, double playerSize) {
        if (!available) return false;
        double centerX = x;
        double centerY = y;
        double pCenterX = playerX + playerSize / 2.0;
        double pCenterY = playerY + playerSize / 2.0;
        double distSq = (centerX - pCenterX) * (centerX - pCenterX) + (centerY - pCenterY) * (centerY - pCenterY);
        return distSq <= 55.0 * 55.0;
    }

    /**
     * Xử lý giao dịch mua hàng hoặc nâng cấp vũ khí khi người chơi bấm phím tương tác [F].
     *
     * @param state Trạng thái gameplay chứa ví Credits và kho vũ khí của người chơi.
     * @return {@code true} nếu giao dịch thành công; {@code false} nếu không đủ điều kiện.
     */
    public boolean purchase(PlayingState state) {
        if (!available || purchased) {
            return false;
        }

        // 1. Xử lý Quầy Nâng Cấp Vũ Khí (Weapon Upgrade Terminal)
        if (type == ShopItemType.WEAPON_UPGRADE) {
            Weapon activeW = state.getWeaponManager().getActiveWeapon();
            if (activeW == null || activeW.isMelee()) {
                state.showTacticalBanner("VŨ KHÍ NÀY KHÔNG THỂ NÂNG CẤP! HÃY CHỌN SÚNG CHÍNH HOẶC SÚNG LỤC",
                        new Color(255, 75, 75), 2.5);
                return false;
            }
            if (!activeW.canUpgrade()) {
                state.showTacticalBanner("VŨ KHÍ ĐÃ ĐẠT GIỚI HẠN NÂNG CẤP (" + Weapon.MAX_UPGRADE_COUNT + "/" + Weapon.MAX_UPGRADE_COUNT + " LẦN)! KHÔNG THỂ NÂNG THÊM",
                        new Color(255, 180, 50), 2.5);
                return false;
            }
            if (state.getPlayerCredits() < price) {
                state.showTacticalBanner("KHÔNG ĐỦ CREDITS! (CẦN " + price + " CREDITS - HIỆN CÓ: " + state.getPlayerCredits() + ")",
                        new Color(255, 75, 75), 2.2);
                return false;
            }

            // Trừ tiền và thăng cấp +1 sao ngẫu nhiên
            state.deductCredits(price);
            String boostedStat = activeW.upgradeLevel();
            state.showTacticalBanner("★ NÂNG CẤP THÀNH CÔNG: " + activeW.getShortDisplayName() +
                    " [NÂNG: " + activeW.getUpgradeCount() + "/" + Weapon.MAX_UPGRADE_COUNT + "] (+1★ " + boostedStat + ") (-" + price + "C) ★",
                    new Color(60, 240, 140), 3.5);
            return true;
        }

        // 2. Xử lý các quầy hàng mua đứt
        if (state.getPlayerCredits() < price) {
            state.showTacticalBanner("KHÔNG ĐỦ CREDITS! (CẦN " + price + " CREDITS - HIỆN CÓ: " + state.getPlayerCredits() + ")",
                    new Color(255, 75, 75), 2.2);
            return false;
        }

        // Khấu trừ tiền tệ
        state.deductCredits(price);

        if (type == ShopItemType.PRIMARY_WEAPON && weapon != null) {
            Weapon replaced = state.getWeaponManager().equipWeapon(weapon);
            purchased = true;
            if (replaced != null) {
                state.dropWeaponOnGround(replaced, state.getPlayerX(), state.getPlayerY());
                state.showTacticalBanner("★ ĐÃ MUA: " + weapon.getShortDisplayName()
                        + " | VŨ KHÍ CŨ (" + replaced.getShortDisplayName() + ") ĐÃ RƠI XUỐNG ĐẤT (-" + price + "C) ★",
                        new Color(60, 220, 160), 3.5);
            } else {
                state.showTacticalBanner("MUA THÀNH CÔNG: " + weapon.getShortDisplayName().toUpperCase() + " (-" + price + " CREDITS)",
                        new Color(60, 220, 160), 2.5);
            }
            return true;

        } else if (type == ShopItemType.AMMO) {
            state.getWeaponManager().resupplyAllWeapons();
            purchased = true;
            state.showTacticalBanner("ĐÃ NẠP ĐẦY TOÀN BỘ ĐẠN DƯỢC! (-" + price + " CREDITS)",
                    new Color(255, 215, 60), 2.5);
            return true;
        }

        return false;
    }

    /**
     * Hiển thị quầy hàng, mô hình vật phẩm và bảng giá tương tác.
     */
    public void render(Graphics2D g, Camera camera, boolean showPrompt) {
        // Nếu mặt hàng chưa phát triển (như HP), hoàn toàn KHÔNG SHOW ra màn hình
        if (!available) {
            return;
        }

        int screenX = camera.worldToScreenX(x);
        int screenY = camera.worldToScreenY(y);

        // Hiệu ứng nhấp nhô lơ lửng
        int bobOffset = (int) (Math.sin(bobbingTimer) * 4.0);
        int itemY = screenY - 14 + bobOffset;

        // 1. Vẽ bệ đứng trưng bày công nghệ cao (Pedestal Base)
        g.setColor(new Color(25, 32, 45, 230));
        g.fillOval(screenX - 24, screenY - 6, 48, 18);
        g.setColor(purchased ? new Color(70, 80, 95) : new Color(255, 215, 60, 180));
        g.setStroke(new BasicStroke(1.8f));
        g.drawOval(screenX - 24, screenY - 6, 48, 18);

        // Hào quang chiếu từ bệ lên
        if (!purchased) {
            g.setColor(new Color(255, 215, 60, 45));
            g.fillOval(screenX - 18, itemY - 6, 36, 26);
        }

        // 2. Vẽ mô hình vật phẩm tương ứng
        if (type == ShopItemType.PRIMARY_WEAPON && weapon != null) {
            if (!purchased) {
                // Mô hình súng nổi lơ lửng
                if (weapon instanceof AssaultRifle) {
                    g.setColor(new Color(45, 55, 65));
                    g.fillRect(screenX - 18, itemY - 4, 34, 7);
                    g.setColor(new Color(25, 30, 35));
                    g.fillRect(screenX + 8, itemY - 2, 12, 3);
                    g.fillRect(screenX - 6, itemY + 3, 6, 9);
                    g.setColor(new Color(255, 215, 60));
                    g.fillRect(screenX + 19, itemY - 3, 3, 5);
                } else if (weapon instanceof Shotgun) {
                    g.setColor(new Color(139, 69, 19));
                    g.fillRect(screenX - 14, itemY - 4, 12, 8);
                    g.setColor(new Color(55, 60, 70));
                    g.fillRect(screenX - 2, itemY - 5, 24, 9);
                    g.setColor(new Color(255, 140, 30));
                    g.fillRect(screenX + 21, itemY - 4, 3, 7);
                } else if (weapon instanceof SMG) {
                    g.setColor(new Color(40, 50, 62));
                    g.fillRect(screenX - 14, itemY - 4, 24, 7);
                    g.setColor(new Color(20, 26, 32));
                    g.fillRect(screenX - 4, itemY + 3, 5, 10);
                    g.setColor(new Color(25, 32, 40));
                    g.fillRect(screenX + 10, itemY - 2, 7, 3);
                    g.setColor(new Color(0, 230, 255));
                    g.fillRect(screenX + 16, itemY - 3, 3, 5);
                }

                if (showPrompt) {
                    WeaponChest.renderWeaponStatCard(g, screenX, itemY - 18, weapon,
                            "MUA " + weapon.getShortDisplayName().toUpperCase() + " (" + price + "C)");
                } else {
                    // Nhãn tên và giá tiền khi đứng xa
                    String title = weapon.getShortDisplayName().toUpperCase() + " • " + price + " CREDITS";
                    renderBadge(g, screenX, itemY - 18, title, new Color(255, 215, 60));
                }
            } else {
                // Đã mua xong (Sold out)
                g.setColor(new Color(80, 90, 105));
                g.fillRect(screenX - 12, itemY - 2, 24, 4);
                renderBadge(g, screenX, itemY - 14, "[ SOLD OUT / ĐÃ BÁN ]", new Color(150, 160, 175));
            }

        } else if (type == ShopItemType.WEAPON_UPGRADE) {
            // Mô hình Bàn nâng cấp vũ khí công nghệ cao: Vòng hào quang Cyan và ký hiệu thăng cấp ▲
            g.setColor(new Color(0, 200, 255, 90));
            g.fillOval(screenX - 16, itemY - 12, 32, 24);
            g.setColor(new Color(0, 230, 255));
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(screenX - 16, itemY - 12, 32, 24);

            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.setColor(new Color(255, 225, 75));
            g.drawString("▲", screenX - 7, itemY + 5);

            if (showPrompt) {
                renderUpgradeTooltip(g, screenX, itemY - 18, price);
            } else {
                String title = "NÂNG CẤP VŨ KHÍ • " + price + " CREDITS";
                renderBadge(g, screenX, itemY - 18, title, new Color(0, 230, 255));
            }

        } else if (type == ShopItemType.AMMO) {
            if (!purchased) {
                // Mô hình Hộp đạn quân sự xanh ô-liu với nẹp vàng
                g.setColor(new Color(55, 80, 45));
                g.fillRoundRect(screenX - 14, itemY - 8, 28, 18, 4, 4);
                g.setColor(new Color(35, 55, 28));
                g.drawRoundRect(screenX - 14, itemY - 8, 28, 18, 4, 4);
                // Biểu tượng đạn vàng
                g.setColor(new Color(255, 215, 60));
                g.fillRect(screenX - 6, itemY - 3, 12, 6);
                g.setColor(new Color(200, 160, 30));
                g.drawRect(screenX - 6, itemY - 3, 12, 6);

                String title = "HỘP TIẾP TẾ ĐẠN • " + price + " CREDITS";
                renderBadge(g, screenX, itemY - 18, title, new Color(120, 240, 140));

                if (showPrompt) {
                    renderPrompt(g, screenX, itemY - 40, "[F] MUA ĐẠN DƯỢC (" + price + "C)");
                }
            } else {
                // Hộp đạn đã mở
                g.setColor(new Color(45, 55, 40));
                g.fillRoundRect(screenX - 14, itemY - 5, 28, 12, 4, 4);
                renderBadge(g, screenX, itemY - 14, "[ ĐÃ TIẾP TẾ ]", new Color(140, 155, 140));
            }
        }

        g.setStroke(new BasicStroke(1.0f));
    }

    private void renderBadge(Graphics2D g, int centerX, int centerY, String text, Color textColor) {
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(text);
        int pad = 6;
        int boxW = textW + pad * 2;
        int boxH = 18;
        int boxX = centerX - boxW / 2;
        int boxY = centerY - boxH / 2;

        g.setColor(new Color(15, 22, 32, 220));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 4, 4);
        g.setColor(new Color(70, 95, 130));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 4, 4);

        g.setColor(textColor);
        g.drawString(text, boxX + pad, boxY + 13);
    }

    private void renderPrompt(Graphics2D g, int centerX, int centerY, String text) {
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(text);
        int pad = 8;
        int boxW = textW + pad * 2;
        int boxH = 22;
        int boxX = centerX - boxW / 2;
        int boxY = centerY - boxH / 2;

        g.setColor(new Color(15, 20, 30, 235));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 6, 6);
        g.setColor(new Color(255, 215, 60)); // Viền vàng kim tương tác mua hàng
        g.drawRoundRect(boxX, boxY, boxW, boxH, 6, 6);

        // Phím [F] nổi bật màu vàng
        g.setColor(new Color(255, 220, 60));
        g.drawString("[F]", boxX + pad, boxY + 15);

        // Chữ nội dung
        g.setColor(Color.WHITE);
        g.drawString(text.substring(3), boxX + pad + fm.stringWidth("[F] "), boxY + 15);
    }

    private void renderUpgradeTooltip(Graphics2D g, int centerX, int bottomY, int price) {
        int boxW = 220;
        int boxH = 88;
        int boxX = centerX - boxW / 2;
        int boxY = bottomY - boxH;

        g.setColor(new Color(15, 25, 40, 240));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setColor(new Color(0, 230, 255));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(new Color(0, 230, 255));
        String title = "★ BÀN NÂNG CẤP VŨ KHÍ (+1★) ★";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, centerX - fm.stringWidth(title) / 2, boxY + 16);

        g.setColor(new Color(50, 85, 115));
        g.drawLine(boxX + 12, boxY + 22, boxX + boxW - 12, boxY + 22);

        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(200, 230, 255));
        g.drawString("• Nâng cấp súng đang cầm +1 Cấp", boxX + 14, boxY + 38);
        g.drawString("• +1★ ngẫu nhiên (Tối đa 3 lần/súng)", boxX + 14, boxY + 54);

        g.setColor(new Color(20, 55, 65, 230));
        g.fillRoundRect(boxX + 10, boxY + 64, boxW - 20, 18, 4, 4);
        g.setColor(new Color(0, 230, 255));
        g.drawRoundRect(boxX + 10, boxY + 64, boxW - 20, 18, 4, 4);

        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.setColor(new Color(255, 220, 60));
        g.drawString("[F] NÂNG CẤP (" + price + " CREDITS)", boxX + 16, boxY + 77);

        g.setStroke(new BasicStroke(1.0f));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public ShopItemType getType() {
        return type;
    }

    public int getPrice() {
        return price;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public boolean isAvailable() {
        return available;
    }

    public Weapon getWeapon() {
        return weapon;
    }
}
