package com.groupproject.game.entity.shop;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameSoundManager;
import com.groupproject.game.core.GameSoundManager.SoundEffect;
import com.groupproject.game.entity.item.WeaponChest;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.entity.weapon.WeaponSpriteManager;
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
     * Factory tạo quầy Cứu thương Nanite (hồi phục +50 HP).
     */
    public static ShopItem createHealthItem(double x, double y, int price) {
        return new ShopItem(x, y, ShopItemType.HEALTH, price, null, true);
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
                GameSoundManager.play(SoundEffect.BUY_FAILED);
                state.showTacticalBanner("VŨ KHÍ NÀY KHÔNG THỂ NÂNG CẤP! HÃY CHỌN SÚNG CHÍNH HOẶC SÚNG LỤC",
                        new Color(255, 75, 75), 2.5);
                return false;
            }
            if (!activeW.canUpgrade()) {
                GameSoundManager.play(SoundEffect.BUY_FAILED);
                state.showTacticalBanner("VŨ KHÍ ĐÃ ĐẠT GIỚI HẠN NÂNG CẤP (" + Weapon.MAX_UPGRADE_COUNT + "/" + Weapon.MAX_UPGRADE_COUNT + " LẦN)! KHÔNG THỂ NÂNG THÊM",
                        new Color(255, 180, 50), 2.5);
                return false;
            }
            if (state.getPlayerCredits() < price) {
                GameSoundManager.play(SoundEffect.BUY_FAILED);
                state.showTacticalBanner("KHÔNG ĐỦ CREDITS! (CẦN " + price + " CREDITS - HIỆN CÓ: " + state.getPlayerCredits() + ")",
                        new Color(255, 75, 75), 2.2);
                return false;
            }

            // Mở Modal Nâng Cấp Vũ Khí cho phép người chơi chủ động chọn 1 trong 3 chỉ số (DMG, RPM, ACC)
            GameSoundManager.play(SoundEffect.INTERACT);
            state.openWeaponUpgradeModal(activeW, price, false, selectedStat -> {
                if (state.getPlayerCredits() < price) {
                    GameSoundManager.play(SoundEffect.BUY_FAILED);
                    state.showTacticalBanner("KHÔNG ĐỦ CREDITS ĐỂ HOÀN TẤT NÂNG CẤP!", new Color(255, 75, 75), 2.0);
                    return;
                }
                state.deductCredits(price);
                String upgradedStatName = activeW.upgradeChosenStat(selectedStat);
                GameSoundManager.play(SoundEffect.BUY_COMPLETE);
                String statLabel = upgradedStatName != null ? upgradedStatName : activeW.getStatDisplayName(selectedStat);
                state.showTacticalBanner("★ NÂNG CẤP THÀNH CÔNG: " + activeW.getShortDisplayName() +
                        " [NÂNG: " + activeW.getUpgradeCount() + "/" + Weapon.MAX_UPGRADE_COUNT + "] (+1★ " + statLabel + ") (-" + price + "C) ★",
                        new Color(60, 240, 140), 3.5);
            });
            return true;
        }

        // 2. Kiểm tra điều kiện máu trước khi mua Cứu Thương
        if (type == ShopItemType.HEALTH && state.getPlayerCurrentHealth() >= state.getPlayerMaxHealth()) {
            GameSoundManager.play(SoundEffect.BUY_FAILED);
            state.showTacticalBanner("MÁU ĐÃ ĐẠT TỐI ĐA (" + state.getPlayerMaxHealth() + "/" + state.getPlayerMaxHealth() + " HP)! KHÔNG CẦN CỨU THƯƠNG",
                    new Color(255, 180, 50), 2.2);
            return false;
        }

        // 3. Xử lý các quầy hàng mua đứt: Kiểm tra đủ tiền
        if (state.getPlayerCredits() < price) {
            GameSoundManager.play(SoundEffect.BUY_FAILED);
            state.showTacticalBanner("KHÔNG ĐỦ CREDITS! (CẦN " + price + " CREDITS - HIỆN CÓ: " + state.getPlayerCredits() + ")",
                    new Color(255, 75, 75), 2.2);
            return false;
        }

        // Khấu trừ tiền tệ và phát âm thanh hoàn tất giao dịch
        state.deductCredits(price);
        GameSoundManager.play(SoundEffect.BUY_COMPLETE);

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
            state.showTacticalBanner("★ ĐÃ TIẾP TẾ +2 STACK ĐẠN CHO TẤT CẢ VŨ KHÍ! (-" + price + " CREDITS) ★",
                    new Color(255, 215, 60), 2.5);
            return true;

        } else if (type == ShopItemType.HEALTH) {
            state.healPlayer(50);
            purchased = true;
            state.showTacticalBanner("★ ĐÃ HỒI PHỤC +50 HP TẠI TRẠM CỨU THƯƠNG NANO! (-" + price + " CREDITS) ★",
                    new Color(60, 240, 140), 2.5);
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

        // Viewport Culling: Bỏ qua nếu quầy hàng nằm ngoài tầm nhìn camera
        if (screenX < -60 || screenX > com.groupproject.game.core.GameConfig.SCREEN_WIDTH + 60 ||
            screenY < -60 || screenY > com.groupproject.game.core.GameConfig.SCREEN_HEIGHT + 60) {
            return;
        }

        // Hiệu ứng nhấp nhô lơ lửng
        int bobOffset = (int) (Math.sin(bobbingTimer) * 4.0);
        int itemY = (type == ShopItemType.PRIMARY_WEAPON) ? (screenY - 26 + bobOffset) : (screenY - 14 + bobOffset);

        // 1. Vẽ bệ đứng trưng bày công nghệ cao (Pedestal Base)
        int baseRadiusX = (type == ShopItemType.PRIMARY_WEAPON) ? 38 : 24;
        int baseRadiusY = (type == ShopItemType.PRIMARY_WEAPON) ? 12 : 9;
        g.setColor(new Color(25, 32, 45, 230));
        g.fillOval(screenX - baseRadiusX, screenY - baseRadiusY + 3, baseRadiusX * 2, baseRadiusY * 2);
        g.setColor(purchased ? new Color(70, 80, 95) : new Color(255, 215, 60, 180));
        g.setStroke(new BasicStroke(1.8f));
        g.drawOval(screenX - baseRadiusX, screenY - baseRadiusY + 3, baseRadiusX * 2, baseRadiusY * 2);

        // Hào quang chiếu từ bệ lên
        if (!purchased) {
            if (type == ShopItemType.PRIMARY_WEAPON) {
                // ĐĨA NỀN SÁNG TƯƠNG PHẢN CAO (LIGHT BACKDROP): Giúp thân súng màu tối nổi bần bật, không bị chìm vào sàn phòng shop
                // 1. Quầng hào quang vàng ấm khuếch tán bên ngoài
                g.setColor(new Color(255, 220, 90, 50));
                g.fillOval(screenX - 44, itemY - 26, 88, 72);
                // 2. Đĩa sáng năng lượng vàng nhạt rực rỡ
                g.setColor(new Color(255, 245, 180, 160));
                g.fillOval(screenX - 35, itemY - 20, 70, 56);
                // 3. Lõi ánh sáng trắng sáng tương phản trực tiếp sau lưng thân súng
                g.setColor(new Color(255, 255, 245, 230));
                g.fillOval(screenX - 25, itemY - 15, 50, 42);
            } else {
                g.setColor(new Color(255, 215, 60, 45));
                g.fillOval(screenX - 18, itemY - 6, 36, 26);
            }
        }

        Color accentColor = new Color(255, 215, 60);

        // 2. Vẽ mô hình vật phẩm tương ứng
        if (type == ShopItemType.PRIMARY_WEAPON && weapon != null) {
            accentColor = new Color(255, 215, 60);
            if (!purchased) {
                // Mô hình súng nổi lơ lửng trên bệ: Phóng to lên 76x76 pixel to rõ, hoành tráng
                int weaponDisplaySize = 76;
                WeaponSpriteManager.renderWeaponSprite(g, weapon, screenX, itemY, weaponDisplaySize, weaponDisplaySize);

                // Khi lại gần: Hiển thị thẻ chỉ số vũ khí chi tiết
                if (showPrompt) {
                    WeaponChest.renderWeaponStatCard(g, screenX, itemY - 38, weapon,
                            "MUA " + weapon.getShortDisplayName().toUpperCase() + " (" + price + "C)");
                }
            } else {
                // Đã mua xong (Sold out)
                g.setColor(new Color(80, 90, 105));
                g.fillRect(screenX - 20, itemY - 2, 40, 4);
            }

        } else if (type == ShopItemType.WEAPON_UPGRADE) {
            accentColor = new Color(0, 230, 255);
            // Mô hình Bàn nâng cấp vũ khí công nghệ cao: Vòng hào quang Cyan và ký hiệu thăng cấp ▲
            g.setColor(new Color(0, 200, 255, 90));
            g.fillOval(screenX - 16, itemY - 12, 32, 24);
            g.setColor(new Color(0, 230, 255));
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(screenX - 16, itemY - 12, 32, 24);

            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.setColor(new Color(255, 225, 75));
            g.drawString("▲", screenX - 7, itemY + 5);

            // Khi lại gần: Hiển thị bảng thông tin nâng cấp
            if (showPrompt) {
                renderUpgradeTooltip(g, screenX, itemY - 18, price);
            }

        } else if (type == ShopItemType.AMMO) {
            accentColor = new Color(120, 240, 140);
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

                // Khi lại gần: Hiển thị tooltip gói tiếp tế đạn
                if (showPrompt) {
                    renderAmmoTooltip(g, screenX, itemY - 18, price);
                }
            } else {
                // Hộp đạn đã mở
                g.setColor(new Color(45, 55, 40));
                g.fillRoundRect(screenX - 14, itemY - 5, 28, 12, 4, 4);
            }

        } else if (type == ShopItemType.HEALTH) {
            accentColor = new Color(60, 230, 140);
            if (!purchased) {
                // Mô hình Trạm Cứu Thương Nano (Hộp kim loại trắng ngà viền xanh ngọc)
                g.setColor(new Color(240, 245, 248));
                g.fillRoundRect(screenX - 14, itemY - 9, 28, 18, 4, 4);
                g.setColor(new Color(60, 230, 140));
                g.drawRoundRect(screenX - 14, itemY - 9, 28, 18, 4, 4);

                // Biểu tượng dấu thập y tế xanh ngọc phát quang
                g.setColor(new Color(40, 210, 95));
                g.fillRect(screenX - 5, itemY - 2, 10, 4);
                g.fillRect(screenX - 2, itemY - 5, 4, 10);

                // Tooltip cứu thương khi lại gần
                if (showPrompt) {
                    renderHealthTooltip(g, screenX, itemY - 18, price);
                }
            } else {
                // Đã sử dụng: Khung kim loại xám
                g.setColor(new Color(55, 65, 60));
                g.fillRoundRect(screenX - 14, itemY - 5, 28, 12, 4, 4);
            }
        }

        // 3. Hiển thị giá cả ngay dưới bệ vật phẩm bày bán (theo yêu cầu người dùng)
        renderPriceTag(g, screenX, screenY + 16, price, purchased, accentColor);

        g.setStroke(new BasicStroke(1.0f));
    }

    /**
     * Hiển thị bảng giá tiền gọn gàng ngay dưới bệ vật phẩm bày bán.
     * Khi đã mua xong, chuyển sang nhãn [ ĐÃ BÁN ] màu xám công nghệ.
     */
    private void renderPriceTag(Graphics2D g, int centerX, int centerY, int price, boolean isPurchased, Color accentColor) {
        g.setFont(com.groupproject.game.core.GameFontManager.getSmallBoldFont(10.0f));
        FontMetrics fm = g.getFontMetrics();

        String text = isPurchased ? "[ ĐÃ BÁN ]" : price + " CREDITS";
        int textW = fm.stringWidth(text);
        int pad = 8;
        int boxW = textW + pad * 2;
        int boxH = 18;
        int boxX = centerX - boxW / 2;
        int boxY = centerY;

        // Nền tối công nghệ cao
        g.setColor(new Color(12, 18, 26, 235));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 6, 6);

        // Viền và văn bản
        if (isPurchased) {
            g.setColor(new Color(65, 75, 90));
            g.drawRoundRect(boxX, boxY, boxW, boxH, 6, 6);
            g.setColor(new Color(130, 140, 155));
        } else {
            g.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 180));
            g.drawRoundRect(boxX, boxY, boxW, boxH, 6, 6);
            g.setColor(accentColor);
        }
        g.drawString(text, boxX + pad, boxY + 13);
    }

    /**
     * Hiển thị bảng thông tin chi tiết gói tiếp tế đạn dược khi người chơi bước lại gần.
     */
    private void renderAmmoTooltip(Graphics2D g, int centerX, int bottomY, int price) {
        int boxW = 224;
        int boxH = 92;
        int boxX = centerX - boxW / 2;
        int boxY = bottomY - boxH;

        g.setColor(new Color(15, 26, 20, 245));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setColor(new Color(120, 240, 140));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        g.setFont(com.groupproject.game.core.GameFontManager.getBoldFont(11.0f));
        g.setColor(new Color(120, 240, 140));
        String title = "TIẾP TẾ ĐẠN DƯỢC (+2 STACK)";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, centerX - fm.stringWidth(title) / 2, boxY + 16);

        g.setColor(new Color(35, 75, 50));
        g.drawLine(boxX + 12, boxY + 22, boxX + boxW - 12, boxY + 22);

        g.setFont(com.groupproject.game.core.GameFontManager.getSmallFont(10.0f));
        g.setColor(new Color(210, 245, 220));
        g.drawString("- AR: +60 viên | SMG: +70 viên", boxX + 14, boxY + 38);
        g.drawString("- Shotgun: +20 viên | Nạp đầy băng", boxX + 14, boxY + 54);

        g.setColor(new Color(20, 60, 35, 230));
        g.fillRoundRect(boxX + 10, boxY + 66, boxW - 20, 18, 4, 4);
        g.setColor(new Color(120, 240, 140));
        g.drawRoundRect(boxX + 10, boxY + 66, boxW - 20, 18, 4, 4);

        g.setFont(com.groupproject.game.core.GameFontManager.getSmallBoldFont(10.0f));
        g.setColor(new Color(255, 220, 60));
        g.drawString("[F]", boxX + 16, boxY + 79);
        g.setColor(Color.WHITE);
        g.drawString("MUA TIẾP TẾ (" + price + " CREDITS)", boxX + 38, boxY + 79);

        g.setStroke(new BasicStroke(1.0f));
    }

    /**
     * Hiển thị bảng thông tin chi tiết Trạm cứu thương Nano khi người chơi bước lại gần.
     */
    private void renderHealthTooltip(Graphics2D g, int centerX, int bottomY, int price) {
        int boxW = 224;
        int boxH = 92;
        int boxX = centerX - boxW / 2;
        int boxY = bottomY - boxH;

        g.setColor(new Color(15, 28, 22, 245));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setColor(new Color(60, 230, 140));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        g.setFont(com.groupproject.game.core.GameFontManager.getBoldFont(11.0f));
        g.setColor(new Color(60, 230, 140));
        String title = "TRẠM CỨU THƯƠNG NANO (+50 HP)";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, centerX - fm.stringWidth(title) / 2, boxY + 16);

        g.setColor(new Color(30, 80, 55));
        g.drawLine(boxX + 12, boxY + 22, boxX + boxW - 12, boxY + 22);

        g.setFont(com.groupproject.game.core.GameFontManager.getSmallFont(10.0f));
        g.setColor(new Color(210, 250, 225));
        g.drawString("- Hồi phục ngay lập tức +50 HP", boxX + 14, boxY + 38);
        g.drawString("- Tối đa không vượt quá 100 HP", boxX + 14, boxY + 54);

        g.setColor(new Color(20, 65, 40, 230));
        g.fillRoundRect(boxX + 10, boxY + 66, boxW - 20, 18, 4, 4);
        g.setColor(new Color(60, 230, 140));
        g.drawRoundRect(boxX + 10, boxY + 66, boxW - 20, 18, 4, 4);

        g.setFont(com.groupproject.game.core.GameFontManager.getSmallBoldFont(10.0f));
        g.setColor(new Color(255, 220, 60));
        g.drawString("[F]", boxX + 16, boxY + 79);
        g.setColor(Color.WHITE);
        g.drawString("MUA CỨU THƯƠNG (" + price + " CREDITS)", boxX + 38, boxY + 79);

        g.setStroke(new BasicStroke(1.0f));
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

        g.setFont(com.groupproject.game.core.GameFontManager.getBoldFont(11.0f));
        g.setColor(new Color(0, 230, 255));
        String title = "BÀN NÂNG CẤP VŨ KHÍ (+1 CẤP)";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, centerX - fm.stringWidth(title) / 2, boxY + 16);

        g.setColor(new Color(50, 85, 115));
        g.drawLine(boxX + 12, boxY + 22, boxX + boxW - 12, boxY + 22);

        g.setFont(com.groupproject.game.core.GameFontManager.getSmallFont(10.0f));
        g.setColor(new Color(200, 230, 255));
        g.drawString("- Nâng cấp súng đang cầm +1 Cấp", boxX + 14, boxY + 38);
        g.drawString("- +1 Sao ngẫu nhiên (Tối đa 3 lần/súng)", boxX + 14, boxY + 54);

        g.setColor(new Color(20, 55, 65, 230));
        g.fillRoundRect(boxX + 10, boxY + 64, boxW - 20, 18, 4, 4);
        g.setColor(new Color(0, 230, 255));
        g.drawRoundRect(boxX + 10, boxY + 64, boxW - 20, 18, 4, 4);

        g.setFont(com.groupproject.game.core.GameFontManager.getSmallBoldFont(10.0f));
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
