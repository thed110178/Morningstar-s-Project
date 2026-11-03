package com.groupproject.game.ui;

import com.groupproject.game.core.GameFontManager;
import com.groupproject.game.core.GameSoundManager;
import com.groupproject.game.core.GameSoundManager.SoundEffect;
import com.groupproject.game.entity.buff.CharacterBuff;
import com.groupproject.game.entity.buff.PlayerBuffManager;
import com.groupproject.game.entity.player.Player;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.entity.weapon.WeaponManager;
import com.groupproject.game.entity.weapon.WeaponSpriteManager;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

/**
 * Giao diện Balo chiến thuật (Tactical Backpack / Tactical Field PDA Overlay).
 *
 * <p>WHY: Đóng gói toàn bộ giao diện quản lý trang bị và hồ sơ tác chiến của người chơi.
 * Hỗ trợ bố cục Chuyển Tab (Tabbed View) chuyển đổi linh hoạt qua phím tắt [Q / E] hoặc click chuột:
 * <ul>
 * <li>Tab 1 - Kho Khí Tài & Đạn Dược (WEAPONS): Quản lý 4 slot vũ khí chính/phụ, đổi vị trí Slot 1 & 2 [SPACE/S],
 *     hiển thị thông số sao, DPS, đạn và số lượng đạn dự trữ.</li>
 * <li>Tab 2 - Hồ Sơ Chiến Binh & Perks (CHARACTER_PERKS): Hiển thị chi tiết tình trạng sinh tồn (Máu, Công %, Tốc %,
 *     Giảm thương %, Hồi né %), toàn bộ 6 chỉ số nâng cấp cơ bản (Basic Stats) và danh mục 7 Kỹ năng đặc biệt (Special Perks)
 *     với trạng thái kích hoạt trực quan.</li>
 * </ul>
 */
public class BackpackUI {

    /**
     * Danh mục các Tab hiển thị trong Sổ tay tác chiến dã chiến.
     */
    public enum BackpackTab {
        WEAPONS,
        CHARACTER_PERKS
    }

    // Kích thước bảng Balo dã chiến
    private final int panelW = 740;
    private final int panelH = 490;

    // Quản lý Tab hiện tại
    private BackpackTab activeTab = BackpackTab.WEAPONS;

    // Các vùng nhấn chuột (Bounding boxes)
    private final Rectangle closeBtnBounds = new Rectangle();
    private final Rectangle tabWeaponsBounds = new Rectangle();
    private final Rectangle tabPerksBounds = new Rectangle();
    private final Rectangle swapBtnBounds = new Rectangle();
    private final Rectangle slot1Bounds = new Rectangle();
    private final Rectangle slot2Bounds = new Rectangle();
    private final Rectangle slot3Bounds = new Rectangle();
    private final Rectangle slot4Bounds = new Rectangle();

    // Hiệu ứng phản hồi tương tác
    private boolean isSwapHovered = false;
    private boolean isCloseHovered = false;
    private boolean isTabWeaponsHovered = false;
    private boolean isTabPerksHovered = false;
    private String feedbackMessage = "";
    private double feedbackTimer = 0.0;

    public BackpackTab getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(BackpackTab tab) {
        this.activeTab = (tab != null) ? tab : BackpackTab.WEAPONS;
    }

    public Rectangle getTabWeaponsBounds() {
        return tabWeaponsBounds;
    }

    public Rectangle getTabPerksBounds() {
        return tabPerksBounds;
    }

    /**
     * Cập nhật các tương tác chuột và bàn phím bên trong giao diện Balo (có truyền Player).
     *
     * @return {@code true} nếu người chơi yêu cầu đóng Balo (nhấn nút [X] hoặc ESC/TAB).
     */
    public boolean update(KeyboardHandler keyH, MouseHandler mouseH, WeaponManager weaponManager, Player player, double deltaTime) {
        if (feedbackTimer > 0) {
            feedbackTimer -= deltaTime;
            if (feedbackTimer <= 0) {
                feedbackMessage = "";
            }
        }

        int mouseX = mouseH.getScreenX();
        int mouseY = mouseH.getScreenY();

        isSwapHovered = swapBtnBounds.contains(mouseX, mouseY);
        isCloseHovered = closeBtnBounds.contains(mouseX, mouseY);
        isTabWeaponsHovered = tabWeaponsBounds.contains(mouseX, mouseY);
        isTabPerksHovered = tabPerksBounds.contains(mouseX, mouseY);

        // 0. Phím tắt chuyển đổi Tab: [Q] -> Khí Tài, [E] -> Hồ Sơ & Perks
        if (keyH.isKeyPressedOnce(KeyEvent.VK_Q)) {
            GameSoundManager.play(SoundEffect.UI_INTERACT);
            activeTab = BackpackTab.WEAPONS;
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_E)) {
            GameSoundManager.play(SoundEffect.UI_INTERACT);
            activeTab = BackpackTab.CHARACTER_PERKS;
        }

        // 1. Tương tác nhấn chuột trái
        if (mouseH.isLeftClickedOnce()) {
            if (isCloseHovered) {
                GameSoundManager.play(SoundEffect.UI_INTERACT);
                return true; // Yêu cầu đóng Balo
            }

            if (isTabWeaponsHovered) {
                GameSoundManager.play(SoundEffect.UI_INTERACT);
                activeTab = BackpackTab.WEAPONS;
                return false;
            }
            if (isTabPerksHovered) {
                GameSoundManager.play(SoundEffect.UI_INTERACT);
                activeTab = BackpackTab.CHARACTER_PERKS;
                return false;
            }

            // Chỉ xử lý tương tác vũ khí khi đang ở Tab WEAPONS
            if (activeTab == BackpackTab.WEAPONS) {
                if (isSwapHovered) {
                    triggerSwap(weaponManager);
                    return false;
                }

                if (slot1Bounds.contains(mouseX, mouseY)) {
                    GameSoundManager.play(SoundEffect.UI_INTERACT);
                    if (weaponManager.getWeaponInSlot(1) != null) weaponManager.selectSlot(1);
                } else if (slot2Bounds.contains(mouseX, mouseY)) {
                    GameSoundManager.play(SoundEffect.UI_INTERACT);
                    if (weaponManager.getWeaponInSlot(2) != null) weaponManager.selectSlot(2);
                } else if (slot3Bounds.contains(mouseX, mouseY)) {
                    GameSoundManager.play(SoundEffect.UI_INTERACT);
                    weaponManager.selectSlot(3);
                } else if (slot4Bounds.contains(mouseX, mouseY)) {
                    GameSoundManager.play(SoundEffect.UI_INTERACT);
                    weaponManager.selectSlot(4);
                }
            }
        }

        // 2. Phím tắt thao tác trang bị vũ khí (khi đang ở Tab WEAPONS)
        if (activeTab == BackpackTab.WEAPONS) {
            if (keyH.isKeyPressedOnce(KeyEvent.VK_SPACE) || keyH.isKeyPressedOnce(KeyEvent.VK_S)) {
                triggerSwap(weaponManager);
            }
            if (keyH.isKeyPressedOnce(KeyEvent.VK_1) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD1)) {
                GameSoundManager.play(SoundEffect.UI_INTERACT);
                weaponManager.selectSlot(1);
            }
            if (keyH.isKeyPressedOnce(KeyEvent.VK_2) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD2)) {
                GameSoundManager.play(SoundEffect.UI_INTERACT);
                weaponManager.selectSlot(2);
            }
            if (keyH.isKeyPressedOnce(KeyEvent.VK_3) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD3)) {
                GameSoundManager.play(SoundEffect.UI_INTERACT);
                weaponManager.selectSlot(3);
            }
            if (keyH.isKeyPressedOnce(KeyEvent.VK_4) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD4)) {
                GameSoundManager.play(SoundEffect.UI_INTERACT);
                weaponManager.selectSlot(4);
            }
        }

        // Đóng balo khi bấm phím ESC (phím TAB được toggle trực tiếp từ PlayingState)
        if (keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
            GameSoundManager.play(SoundEffect.UI_INTERACT);
            return true;
        }

        return false;
    }

    /**
     * Overload tương thích ngược khi không truyền Player.
     */
    public boolean update(KeyboardHandler keyH, MouseHandler mouseH, WeaponManager weaponManager, double deltaTime) {
        return update(keyH, mouseH, weaponManager, null, deltaTime);
    }

    private void triggerSwap(WeaponManager weaponManager) {
        boolean success = weaponManager.swapPrimarySlots();
        if (success) {
            GameSoundManager.play(SoundEffect.UI_INTERACT);
            feedbackMessage = "★ ĐÃ HOÁN ĐỔI VỊ TRÍ VŨ KHÍ [1] ⇄ [2] THÀNH CÔNG ★";
        } else {
            GameSoundManager.play(SoundEffect.BUY_FAILED);
            feedbackMessage = "CẢ HAI Ô VŨ KHÍ CHÍNH ĐỀU TRỐNG (MỞ HÒM ĐỂ NHẶT SÚNG)";
        }
        feedbackTimer = 2.2;
    }

    /**
     * Vẽ giao diện Balo chiến thuật đầy đủ cả 2 Tab với phong cách Sci-Fi Tactical Glassmorphism.
     */
    public void render(Graphics2D g, WeaponManager weaponManager, Player player, int screenWidth, int screenHeight) {
        // 1. Phủ mờ toàn màn hình
        g.setColor(new Color(5, 10, 20, 215));
        g.fillRect(0, 0, screenWidth, screenHeight);

        // Tọa độ trung tâm bảng PDA
        int panelX = (screenWidth - panelW) / 2;
        int panelY = (screenHeight - panelH) / 2;

        // Cập nhật vùng nút đóng
        closeBtnBounds.setBounds(panelX + panelW - 44, panelY + 12, 30, 30);

        // Thân máy tính quân sự dã chiến
        g.setColor(new Color(18, 22, 20, 252));
        g.fillRoundRect(panelX, panelY, panelW, panelH, 16, 16);

        // Khung viền bọc cao su chịu lực và viền kim loại dã chiến
        g.setColor(new Color(65, 78, 64));
        g.setStroke(new BasicStroke(2.2f));
        g.drawRoundRect(panelX, panelY, panelW, panelH, 16, 16);
        g.setColor(new Color(185, 155, 65)); // Vàng hổ phách quân sự
        g.drawRoundRect(panelX + 3, panelY + 3, panelW - 6, panelH - 6, 14, 14);

        // 3. Header Tiêu đề Sổ tay Tác chiến
        g.setFont(GameFontManager.getBoldFont(15.0f));
        g.setColor(new Color(245, 215, 80));
        g.drawString("[ SỔ TAY TÁC CHIẾN DÃ CHIẾN // TACTICAL FIELD PDA ]", panelX + 25, panelY + 32);

        // Nút đóng [X]
        g.setColor(isCloseHovered ? new Color(255, 60, 60) : new Color(50, 65, 85));
        g.fillRoundRect(closeBtnBounds.x, closeBtnBounds.y, closeBtnBounds.width, closeBtnBounds.height, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(GameFontManager.getBoldFont(14.0f));
        g.drawString("X", closeBtnBounds.x + 9, closeBtnBounds.y + 21);

        // 3.5. THANH 2 TAB ĐIỀU HƯỚNG [Q / E]
        int tabY = panelY + 42;
        int tabH = 28;
        int tab1W = 200;
        int tab2W = 225;
        tabWeaponsBounds.setBounds(panelX + 25, tabY, tab1W, tabH);
        tabPerksBounds.setBounds(panelX + 235, tabY, tab2W, tabH);

        // Tab 1: KHO KHÍ TÀI & ĐẠN
        boolean isTab1Active = (activeTab == BackpackTab.WEAPONS);
        Color tab1Bg = isTab1Active ? new Color(30, 48, 70, 240) : (isTabWeaponsHovered ? new Color(25, 38, 55, 220) : new Color(18, 26, 35, 180));
        Color tab1Border = isTab1Active ? new Color(60, 220, 160) : new Color(50, 70, 95);
        g.setColor(tab1Bg);
        g.fillRoundRect(tabWeaponsBounds.x, tabWeaponsBounds.y, tabWeaponsBounds.width, tabWeaponsBounds.height, 6, 6);
        g.setColor(tab1Border);
        g.setStroke(new BasicStroke(isTab1Active ? 2.0f : 1.0f));
        g.drawRoundRect(tabWeaponsBounds.x, tabWeaponsBounds.y, tabWeaponsBounds.width, tabWeaponsBounds.height, 6, 6);
        g.setFont(GameFontManager.getSmallBoldFont(11.0f));
        g.setColor(isTab1Active ? new Color(255, 225, 80) : new Color(155, 175, 200));
        drawCenteredString(g, "[Q] KHO KHÍ TÀI & ĐẠN", tabWeaponsBounds.x + tabWeaponsBounds.width / 2, tabWeaponsBounds.y + 19);

        // Tab 2: HỒ SƠ CHIẾN BINH & PERKS
        boolean isTab2Active = (activeTab == BackpackTab.CHARACTER_PERKS);
        Color tab2Bg = isTab2Active ? new Color(35, 55, 45, 240) : (isTabPerksHovered ? new Color(28, 45, 38, 220) : new Color(18, 26, 35, 180));
        Color tab2Border = isTab2Active ? new Color(60, 220, 160) : new Color(50, 70, 95);
        g.setColor(tab2Bg);
        g.fillRoundRect(tabPerksBounds.x, tabPerksBounds.y, tabPerksBounds.width, tabPerksBounds.height, 6, 6);
        g.setColor(tab2Border);
        g.setStroke(new BasicStroke(isTab2Active ? 2.0f : 1.0f));
        g.drawRoundRect(tabPerksBounds.x, tabPerksBounds.y, tabPerksBounds.width, tabPerksBounds.height, 6, 6);
        g.setFont(GameFontManager.getSmallBoldFont(11.0f));
        g.setColor(isTab2Active ? new Color(255, 225, 80) : new Color(155, 175, 200));
        drawCenteredString(g, "[E] HỒ SƠ & PERKS", tabPerksBounds.x + tabPerksBounds.width / 2, tabPerksBounds.y + 19);

        // Đường gạch ngang phân cách header
        g.setColor(new Color(40, 60, 85));
        g.drawLine(panelX + 25, panelY + 76, panelX + panelW - 25, panelY + 76);

        // 4. VẼ NỘI DUNG THEO TAB ĐANG KÍCH HOẠT
        if (activeTab == BackpackTab.WEAPONS) {
            renderWeaponsTab(g, weaponManager, panelX, panelY);
        } else {
            renderCharacterPerksTab(g, player, panelX, panelY);
        }

        // 5. Chân bảng hướng dẫn thao tác (Footer Guide)
        g.setColor(new Color(40, 60, 85));
        g.drawLine(panelX + 25, panelY + 440, panelX + panelW - 25, panelY + 440);

        g.setFont(GameFontManager.getSmallFont(10.0f));
        g.setColor(new Color(160, 185, 215));
        if (activeTab == BackpackTab.WEAPONS) {
            drawCenteredString(g, "[TAB / ESC] Đóng Sổ Tay  |  [Q / E] Đổi Tab  |  [SPACE / Click Đổi] Đổi vị trí Slot 1 & 2  |  [1, 2, 3, 4] Cầm vũ khí",
                    panelX + panelW / 2, panelY + 465);
        } else {
            drawCenteredString(g, "[TAB / ESC] Đóng Sổ Tay  |  [Q / E] Đổi Tab  |  Ghé Phòng Nâng Cấp để mở khóa thêm Buff & Perk",
                    panelX + panelW / 2, panelY + 465);
        }

        g.setStroke(new BasicStroke(1.0f));
    }

    /**
     * Overload tương thích ngược khi không truyền Player.
     */
    public void render(Graphics2D g, WeaponManager weaponManager, int screenWidth, int screenHeight) {
        render(g, weaponManager, null, screenWidth, screenHeight);
    }

    /**
     * Render Nội dung Tab 1: KHO KHÍ TÀI & ĐẠN DƯỢC.
     */
    private void renderWeaponsTab(Graphics2D g, WeaponManager weaponManager, int panelX, int panelY) {
        // KHU VỰC VŨ KHÍ CHÍNH (SLOT 1 & SLOT 2)
        int slotW = 270;
        int slotH = 195;
        int slot1X = panelX + 35;
        int slot1Y = panelY + 86;
        int slot2X = panelX + panelW - 35 - slotW;
        int slot2Y = slot1Y;

        slot1Bounds.setBounds(slot1X, slot1Y, slotW, slotH);
        slot2Bounds.setBounds(slot2X, slot2Y, slotW, slotH);

        Weapon activeW = weaponManager.getActiveWeapon();
        Weapon w1 = weaponManager.getWeaponInSlot(1);
        Weapon w2 = weaponManager.getWeaponInSlot(2);

        renderWeaponCard(g, slot1X, slot1Y, slotW, slotH, 1, "VŨ KHÍ CHÍNH 1", w1, activeW == w1 && w1 != null);
        renderWeaponCard(g, slot2X, slot2Y, slotW, slotH, 2, "VŨ KHÍ CHÍNH 2", w2, activeW == w2 && w2 != null);

        // NÚT TRUNG TÂM HOÁN ĐỔI VỊ TRÍ [1] ⇄ [2]
        int swapW = 110;
        int swapH = 68;
        int swapX = panelX + (panelW - swapW) / 2;
        int swapY = slot1Y + (slotH - swapH) / 2;
        swapBtnBounds.setBounds(swapX, swapY, swapW, swapH);

        g.setColor(isSwapHovered ? new Color(255, 215, 60, 40) : new Color(25, 38, 55, 220));
        g.fillRoundRect(swapX, swapY, swapW, swapH, 10, 10);
        g.setColor(isSwapHovered ? new Color(255, 220, 70) : new Color(70, 100, 140));
        g.setStroke(new BasicStroke(isSwapHovered ? 2.5f : 1.5f));
        g.drawRoundRect(swapX, swapY, swapW, swapH, 10, 10);

        g.setFont(GameFontManager.getBoldFont(12.0f));
        g.setColor(isSwapHovered ? new Color(255, 225, 80) : Color.WHITE);
        drawCenteredString(g, "<-> ĐỔI VỊ TRÍ", swapX + swapW / 2, swapY + 28);
        g.setFont(GameFontManager.getSmallBoldFont(10.5f));
        g.setColor(isSwapHovered ? Color.WHITE : new Color(180, 200, 220));
        drawCenteredString(g, "[SPACE / S]", swapX + swapW / 2, swapY + 48);

        // KHU VỰC VŨ KHÍ PHỤ & CẬN CHIẾN (SLOT 3 & SLOT 4)
        int subSlotW = 325;
        int subSlotH = 95;
        int slot3X = panelX + 35;
        int slot3Y = panelY + 295;
        int slot4X = panelX + panelW - 35 - subSlotW;
        int slot4Y = slot3Y;

        slot3Bounds.setBounds(slot3X, slot3Y, subSlotW, subSlotH);
        slot4Bounds.setBounds(slot4X, slot4Y, subSlotW, subSlotH);

        Weapon w3 = weaponManager.getWeaponInSlot(3);
        Weapon w4 = weaponManager.getWeaponInSlot(4);

        renderSubCard(g, slot3X, slot3Y, subSlotW, subSlotH, 3, "SÚNG LỤC PHỤ (SIDEARM)", w3, activeW == w3);
        renderSubCard(g, slot4X, slot4Y, subSlotW, subSlotH, 4, "VŨ KHÍ CẬN CHIẾN (MELEE)", w4, activeW == w4);

        // KHU VỰC THỐNG KÊ KHO ĐẠN DỰ TRỮ (AMMO RESERVES HUD)
        com.groupproject.game.entity.item.AmmoInventory ammoInv = weaponManager.getAmmoInventory();
        int ammoBarY = panelY + 402;
        int barW = 155;
        int barH = 22;
        int badgeGap = 16;

        // 1. Rifle Ammo Badge (Đạn súng trường)
        int arX = panelX + 35;
        g.setColor(new Color(20, 35, 30, 220));
        g.fillRoundRect(arX, ammoBarY, barW, barH, 6, 6);
        g.setColor(new Color(60, 220, 120));
        g.drawRoundRect(arX, ammoBarY, barW, barH, 6, 6);
        g.setFont(GameFontManager.getSmallBoldFont(10.0f));
        g.drawString("- SÚNG TRƯỜNG: " + ammoInv.getAmmo(com.groupproject.game.entity.item.AmmoType.AR) + " / 150", arX + 8, ammoBarY + 15);

        // 2. SMG Ammo Badge
        int smgX = arX + barW + badgeGap;
        g.setColor(new Color(15, 30, 45, 220));
        g.fillRoundRect(smgX, ammoBarY, barW, barH, 6, 6);
        g.setColor(new Color(0, 210, 255));
        g.drawRoundRect(smgX, ammoBarY, barW, barH, 6, 6);
        g.drawString("- ĐẠN SMG: " + ammoInv.getAmmo(com.groupproject.game.entity.item.AmmoType.SMG) + " / 180", smgX + 12, ammoBarY + 15);

        // 3. Shotgun Ammo Badge
        int sgX = smgX + barW + badgeGap;
        g.setColor(new Color(40, 20, 20, 220));
        g.fillRoundRect(sgX, ammoBarY, barW, barH, 6, 6);
        g.setColor(new Color(255, 90, 70));
        g.drawRoundRect(sgX, ammoBarY, barW, barH, 6, 6);
        g.drawString("- ĐẠN SHOTGUN: " + ammoInv.getAmmo(com.groupproject.game.entity.item.AmmoType.SHOTGUN) + " / 50", sgX + 10, ammoBarY + 15);

        // 4. SR Sniper Ammo Badge
        int srX = sgX + barW + badgeGap;
        g.setColor(new Color(35, 18, 48, 220));
        g.fillRoundRect(srX, ammoBarY, barW, barH, 6, 6);
        g.setColor(new Color(185, 80, 255));
        g.drawRoundRect(srX, ammoBarY, barW, barH, 6, 6);
        g.drawString("- BẮN TỈA (SR): " + ammoInv.getAmmo(com.groupproject.game.entity.item.AmmoType.SR) + " / 30", srX + 10, ammoBarY + 15);

        // Thông báo phản hồi thao tác hoán đổi
        if (!feedbackMessage.isEmpty() && feedbackTimer > 0) {
            g.setFont(GameFontManager.getBoldFont(12.5f));
            g.setColor(new Color(255, 220, 80));
            drawCenteredString(g, feedbackMessage, panelX + panelW / 2, panelY + 395);
        }
    }

    /**
     * Render Nội dung Tab 2: HỒ SƠ CHIẾN BINH & PERKS (Hiển thị đầy đủ 6 chỉ số nâng cấp và 7 Kỹ năng đặc biệt).
     */
    private void renderCharacterPerksTab(Graphics2D g, Player player, int panelX, int panelY) {
        if (player == null) {
            g.setFont(GameFontManager.getSmallFont(12.0f));
            g.setColor(new Color(150, 175, 200));
            drawCenteredString(g, "[ Không có dữ liệu hồ sơ chiến binh ]", panelX + panelW / 2, panelY + 240);
            return;
        }

        PlayerBuffManager bm = player.getBuffManager();

        // 1. THANH TÌNH TRẠNG CHIẾN BINH TỔNG HỢP (OPERATIVE VITALS SUMMARY)
        int vitalsY = panelY + 84;
        g.setColor(new Color(16, 24, 30, 240));
        g.fillRoundRect(panelX + 25, vitalsY, panelW - 50, 32, 6, 6);
        g.setColor(new Color(45, 75, 65));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(panelX + 25, vitalsY, panelW - 50, 32, 6, 6);

        // Máu hiện tại / Máu tối đa kèm Mini Health Bar
        int hpX = panelX + 35;
        g.setFont(GameFontManager.getSmallBoldFont(10.5f));
        g.setColor(new Color(255, 95, 95));
        g.drawString("[HP] MÁU:", hpX, vitalsY + 20);

        int hpBarX = hpX + 56;
        int hpBarW = 60;
        int hpBarH = 10;
        g.setColor(new Color(50, 20, 20));
        g.fillRect(hpBarX, vitalsY + 11, hpBarW, hpBarH);
        double hpRatio = Math.clamp((double) player.getCurrentHealth() / Math.max(1, player.getMaxHealth()), 0.0, 1.0);
        g.setColor(new Color(60, 220, 120));
        g.fillRect(hpBarX, vitalsY + 11, (int) Math.round(hpBarW * hpRatio), hpBarH);
        g.setColor(new Color(160, 190, 180));
        g.drawRect(hpBarX, vitalsY + 11, hpBarW, hpBarH);
        g.setColor(Color.WHITE);
        g.setFont(GameFontManager.getSmallBoldFont(10.0f));
        g.drawString(player.getCurrentHealth() + "/" + player.getMaxHealth(), hpBarX + hpBarW + 6, vitalsY + 20);

        // Các chỉ số chiến thuật tính toán từ Buff
        int statX = hpBarX + hpBarW + 68;
        g.setFont(GameFontManager.getSmallBoldFont(10.0f));
        g.setColor(new Color(255, 215, 60));
        g.drawString("[ATK] +" + Math.round((bm.getAttackMultiplier() - 1.0) * 100) + "%", statX, vitalsY + 20);
        statX += 85;
        g.setColor(new Color(0, 225, 255));
        g.drawString("[SPD] +" + Math.round((bm.getSpeedMultiplier() - 1.0) * 100) + "%", statX, vitalsY + 20);
        statX += 85;
        g.setColor(new Color(140, 200, 255));
        g.drawString("[DEF] -" + Math.round(bm.getDamageReduction() * 100) + "%", statX, vitalsY + 20);
        statX += 85;
        g.setColor(new Color(255, 140, 90));
        g.drawString("[DODGE] +" + Math.round((bm.getDodgeCooldownFactor() - 1.0) * 100) + "%", statX, vitalsY + 20);
        statX += 100;
        g.setColor(new Color(255, 220, 75));
        g.drawString("[LUCK] +" + Math.round(bm.getLootLuckRatio() * 100) + "%", statX, vitalsY + 20);

        // 2. KHỐI 6 CHỈ SỐ CƠ BẢN (BASIC ATTRIBUTES)
        int basicY = panelY + 122;
        g.setFont(GameFontManager.getBoldFont(12.0f));
        g.setColor(new Color(255, 215, 60));
        g.drawString("CƯỜNG HÓA CHỈ SỐ CƠ BẢN (BASIC ATTRIBUTES)", panelX + 28, basicY + 11);

        g.setColor(new Color(16, 24, 30, 220));
        g.fillRoundRect(panelX + 25, basicY + 16, panelW - 50, 102, 8, 8);
        g.setColor(new Color(45, 65, 80));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(panelX + 25, basicY + 16, panelW - 50, 102, 8, 8);

        // Cột Trái (3 chỉ số: Máu, Tấn công, Tốc độ)
        int col1X = panelX + 38;
        int rowH = 30;
        int r1Y = basicY + 34;
        renderBasicStatRow(g, col1X, r1Y, CharacterBuff.MAX_HEALTH, "- MÁU TỐI ĐA (MAX HP)",
                bm.getLevel(CharacterBuff.MAX_HEALTH), CharacterBuff.MAX_HEALTH.getMaxLevel(),
                "+20% Máu/cấp (Hiện tại: +" + Math.round((bm.getMaxHealthMultiplier() - 1.0) * 100) + "% HP)");

        renderBasicStatRow(g, col1X, r1Y + rowH, CharacterBuff.ATTACK_DAMAGE, "- TẤN CÔNG (ATTACK)",
                bm.getLevel(CharacterBuff.ATTACK_DAMAGE), CharacterBuff.ATTACK_DAMAGE.getMaxLevel(),
                "+10% Sát thương/cấp (Hiện tại: +" + Math.round((bm.getAttackMultiplier() - 1.0) * 100) + "% DMG)");

        renderBasicStatRow(g, col1X, r1Y + rowH * 2, CharacterBuff.MOVE_SPEED, "- TỐC ĐỘ CHẠY (SPEED)",
                bm.getLevel(CharacterBuff.MOVE_SPEED), CharacterBuff.MOVE_SPEED.getMaxLevel(),
                "+5% Tốc độ/cấp (Hiện tại: +" + Math.round((bm.getSpeedMultiplier() - 1.0) * 100) + "% SPD)");

        // Cột Phải (3 chỉ số: Phòng thủ, Hồi chiêu né, May mắn)
        int col2X = panelX + 380;
        renderBasicStatRow(g, col2X, r1Y, CharacterBuff.DEFENSE, "- PHÒNG THỦ (DEFENSE)",
                bm.getLevel(CharacterBuff.DEFENSE), CharacterBuff.DEFENSE.getMaxLevel(),
                "Giảm 10% sát thương/cấp (Hiện tại: -" + Math.round(bm.getDamageReduction() * 100) + "% DMG)");

        renderBasicStatRow(g, col2X, r1Y + rowH, CharacterBuff.DODGE_COOLDOWN, "- HỒI CHIÊU NÉ (DODGE)",
                bm.getLevel(CharacterBuff.DODGE_COOLDOWN), CharacterBuff.DODGE_COOLDOWN.getMaxLevel(),
                "Nhanh hơn 1.5 lần (Hiện tại: +" + Math.round((bm.getDodgeCooldownFactor() - 1.0) * 100) + "% CD)");

        renderBasicStatRow(g, col2X, r1Y + rowH * 2, CharacterBuff.LOOT_LUCK, "- MAY MẮN NHẶT ĐỒ",
                bm.getLevel(CharacterBuff.LOOT_LUCK), CharacterBuff.LOOT_LUCK.getMaxLevel(),
                "+25% Tỉ lệ súng 4-5 Sao & Súng Đỏ (Hiện tại: +" + Math.round(bm.getLootLuckRatio() * 100) + "%)");

        // 3. KHỐI 7 KỸ NĂNG ĐẶC BIỆT & PERKS (SPECIAL PERKS)
        int perkY = panelY + 246;
        g.setFont(GameFontManager.getBoldFont(12.0f));
        g.setColor(new Color(60, 220, 160));
        g.drawString("KỸ NĂNG ĐẶC BIỆT & PERKS CHIẾN THUẬT (SPECIAL PERKS)", panelX + 28, perkY + 11);

        g.setColor(new Color(16, 24, 30, 220));
        g.fillRoundRect(panelX + 25, perkY + 16, panelW - 50, 168, 8, 8);
        g.setColor(new Color(40, 75, 65));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(panelX + 25, perkY + 16, panelW - 50, 168, 8, 8);

        // Lưới 7 Perks chia 2 cột:
        // Cột Trái (4 perks): Giáp chặn đòn, Diệt địch hồi máu, Né xuyên vật cản, Thu nhỏ thân thể
        int pCol1X = panelX + 35;
        int pColW = 330;
        int pRowH1 = 38;
        int pStart1Y = perkY + 22;

        renderSpecialPerkCard(g, pCol1X, pStart1Y, pColW, 34,
                "GIÁP CHẶN ĐÒN (BARRIER)", "Chặn 100% 1 lần sát thương bất kỳ, hồi lại sau 10s.",
                bm.hasOneHitBarrier());

        renderSpecialPerkCard(g, pCol1X, pStart1Y + pRowH1, pColW, 34,
                "DIỆT ĐỊCH HỒI MÁU (KILL HEAL)", "Hồi ngay +2.5 HP sinh lực với mỗi kẻ địch bị tiêu diệt.",
                bm.hasKillHeal());

        renderSpecialPerkCard(g, pCol1X, pStart1Y + pRowH1 * 2, pColW, 34,
                "NÉ XUYÊN VẬT CẢN (PHASE DODGE)", "Lướt né [SPACE] đi xuyên thùng gỗ và giữ I-frame an toàn.",
                bm.hasPhaseDodge());

        renderSpecialPerkCard(g, pCol1X, pStart1Y + pRowH1 * 3, pColW, 34,
                "THU NHỎ THÂN THỂ (SHRINK)", "Giảm 30% kích thước hitbox nhân vật, né đạn siêu cơ động.",
                bm.hasShrinkHitbox());

        // Cột Phải (3 perks): Băng đạn tiếp tế, Băng đạn mở rộng, Bậc thầy chí mạng
        int pCol2X = panelX + 375;
        int pRowH2 = 51;
        int pStart2Y = perkY + 22;

        renderSpecialPerkCard(g, pCol2X, pStart2Y, pColW, 46,
                "BĂNG ĐẠN TIẾP TẾ (PERIODIC AMMO)", "Cứ mỗi 30 giây tự động tiếp tế +2 băng đạn dự trữ cho vũ khí chính.",
                bm.hasPeriodicAmmo());

        renderSpecialPerkCard(g, pCol2X, pStart2Y + pRowH2, pColW, 46,
                "BĂNG ĐẠN MỞ RỘNG (EXTENDED MAG)", "Tăng đạn 1 băng: Rifle 40v, SCR 28v, SMG 60v, Súng ngắm 8v, Lục 12v.",
                bm.hasExtendedMagazine());

        renderSpecialPerkCard(g, pCol2X, pStart2Y + pRowH2 * 2, pColW, 46,
                "BẬC THẦY CHÍ MẠNG (CRIT MASTER)", "+15% Tỉ lệ crit. Sát thương crit: Rifle 150%, SMG & Lục 120%, Dao 200%.",
                bm.hasCriticalMastery());
    }

    /**
     * Vẽ một dòng hiển thị chỉ số cơ bản gồm tên, cấp độ badge, thanh ô vuông pips và mô tả chi tiết.
     */
    private void renderBasicStatRow(Graphics2D g, int x, int y, CharacterBuff buff, String title,
                                    int level, int maxLevel, String desc) {
        // Tên chỉ số
        g.setFont(GameFontManager.getSmallBoldFont(10.5f));
        g.setColor(level > 0 ? new Color(255, 230, 90) : new Color(200, 215, 230));
        g.drawString(title, x, y);

        // Badge cấp độ: Lv.X/Y
        int badgeX = x + 160;
        g.setFont(GameFontManager.getSmallBoldFont(10.0f));
        g.setColor(level >= maxLevel ? new Color(255, 215, 60) : (level > 0 ? new Color(60, 220, 160) : new Color(130, 145, 160)));
        g.drawString("Lv." + level + "/" + maxLevel, badgeX, y);

        // Thanh tiến trình Pips (ô vuông nhỏ 11x6px)
        int pipStartX = badgeX + 48;
        int pipW = 10;
        int pipH = 6;
        int pipGap = 3;
        for (int i = 1; i <= maxLevel; i++) {
            int px = pipStartX + (i - 1) * (pipW + pipGap);
            int py = y - 8;
            if (i <= level) {
                g.setColor(new Color(255, 215, 60));
                g.fillRect(px, py, pipW, pipH);
                g.setColor(new Color(255, 245, 150));
                g.drawRect(px, py, pipW, pipH);
            } else {
                g.setColor(new Color(30, 40, 50));
                g.fillRect(px, py, pipW, pipH);
                g.setColor(new Color(55, 70, 85));
                g.drawRect(px, py, pipW, pipH);
            }
        }

        // Mô tả chi tiết
        g.setFont(GameFontManager.getSmallFont(9.5f));
        g.setColor(level > 0 ? new Color(175, 210, 230) : new Color(130, 145, 165));
        g.drawString(desc, x + 8, y + 14);
    }

    /**
     * Vẽ một ô thẻ Kỹ Năng Đặc Biệt / Perk với viền dạ quang và trạng thái kích hoạt trực quan.
     */
    private void renderSpecialPerkCard(Graphics2D g, int x, int y, int w, int h, String name, String desc, boolean isUnlocked) {
        if (isUnlocked) {
            // ĐÃ KÍCH HOẠT: Nền xanh đen dạ quang
            g.setColor(new Color(15, 38, 28, 230));
            g.fillRoundRect(x, y, w, h, 6, 6);
            g.setColor(new Color(60, 220, 160));
            g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(x, y, w, h, 6, 6);

            // Tên Perk vàng kim nổi bật
            g.setFont(GameFontManager.getSmallBoldFont(10.0f));
            g.setColor(new Color(255, 225, 75));
            g.drawString("[+] " + name, x + 8, y + 13);

            // Badge ĐÃ KÍCH HOẠT
            g.setFont(GameFontManager.getSmallBoldFont(9.0f));
            g.setColor(new Color(60, 240, 140));
            FontMetrics fm = g.getFontMetrics();
            String badge = "[ĐÃ KÍCH HOẠT]";
            g.drawString(badge, x + w - fm.stringWidth(badge) - 8, y + 13);

            // Mô tả hiệu ứng
            g.setFont(GameFontManager.getSmallFont(9.0f));
            g.setColor(new Color(190, 230, 215));
            if (h > 40) {
                // Tách 2 dòng nếu mô tả dài
                drawWrappedDescription(g, desc, x + 8, y + 26, w - 16);
            } else {
                g.drawString(desc, x + 8, y + 26);
            }

        } else {
            // CHƯA SỞ HỮU: Nền tối viền xám mờ
            g.setColor(new Color(18, 22, 28, 190));
            g.fillRoundRect(x, y, w, h, 6, 6);
            g.setColor(new Color(45, 55, 68));
            g.setStroke(new BasicStroke(1.0f));
            g.drawRoundRect(x, y, w, h, 6, 6);

            // Tên Perk màu xám bạc
            g.setFont(GameFontManager.getSmallBoldFont(10.0f));
            g.setColor(new Color(140, 155, 170));
            g.drawString("[-] " + name, x + 8, y + 13);

            // Badge CHƯA CÓ
            g.setFont(GameFontManager.getSmallFont(9.0f));
            g.setColor(new Color(110, 125, 140));
            FontMetrics fm = g.getFontMetrics();
            String badge = "[CHƯA CÓ]";
            g.drawString(badge, x + w - fm.stringWidth(badge) - 8, y + 13);

            // Mô tả hiệu ứng mờ
            g.setFont(GameFontManager.getSmallFont(9.0f));
            g.setColor(new Color(105, 120, 135));
            if (h > 40) {
                drawWrappedDescription(g, desc, x + 8, y + 26, w - 16);
            } else {
                g.drawString(desc, x + 8, y + 26);
            }
        }
    }

    private void drawWrappedDescription(Graphics2D g, String text, int x, int y, int maxW) {
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(text) <= maxW) {
            g.drawString(text, x, y);
            return;
        }

        String[] words = text.split(" ");
        StringBuilder line1 = new StringBuilder();
        StringBuilder line2 = new StringBuilder();
        boolean line1Full = false;

        for (String word : words) {
            if (!line1Full) {
                if (fm.stringWidth(line1 + word + " ") <= maxW) {
                    line1.append(word).append(" ");
                } else {
                    line1Full = true;
                    line2.append(word).append(" ");
                }
            } else {
                line2.append(word).append(" ");
            }
        }

        g.drawString(line1.toString().trim(), x, y);
        g.drawString(line2.toString().trim(), x, y + 12);
    }

    private void renderWeaponCard(Graphics2D g, int x, int y, int w, int h, int slot, String slotTitle,
                                  Weapon weapon, boolean isEquipped) {
        // Nền thẻ
        g.setColor(new Color(20, 30, 45, 230));
        g.fillRoundRect(x, y, w, h, 12, 12);
        g.setColor(isEquipped ? new Color(60, 220, 160) : new Color(50, 75, 105));
        g.setStroke(new BasicStroke(isEquipped ? 2.2f : 1.2f));
        g.drawRoundRect(x, y, w, h, 12, 12);

        // Tiêu đề Slot
        g.setFont(GameFontManager.getBoldFont(12.0f));
        g.setColor(new Color(255, 200, 60));
        g.drawString(String.format("[%d] %s", slot, slotTitle), x + 15, y + 25);

        if (weapon != null) {
            // Tên vũ khí và cấp độ hiển thị (VD: AR I [0/3], SHOTGUN II [1/3])
            g.setFont(GameFontManager.getBoldFont(13.0f));
            g.setColor(new Color(255, 220, 75));
            String titleText = weapon.getShortDisplayName().toUpperCase();
            if (!weapon.isMelee()) {
                titleText += " [" + weapon.getUpgradeCount() + "/" + Weapon.MAX_UPGRADE_COUNT + "]";
            }
            g.drawString(titleText, x + 15, y + 48);

            // Minh họa hình dạng vũ khí thu nhỏ: Phóng to lên 56x56 pixel trong khung nền sáng tương phản 64x64
            int iconBoxSize = 64;
            int iconSize = 56;
            int iconCenterX = x + w - 46;
            int iconCenterY = y + 46;

            // TẤM NỀN SÁNG TƯƠNG PHẢN (LIGHT CONTRAST BACKDROP): Tôn vinh hình khối súng tối màu
            g.setColor(new Color(215, 230, 245, 240)); // Trắng bạc ánh xanh băng sáng rực rỡ
            g.fillRoundRect(iconCenterX - iconBoxSize / 2, iconCenterY - iconBoxSize / 2, iconBoxSize, iconBoxSize, 8, 8);
            g.setColor(new Color(255, 255, 255, 220)); // Quầng sáng trắng tâm
            g.fillOval(iconCenterX - 22, iconCenterY - 22, 44, 44);
            g.setColor(new Color(75, 130, 190));
            g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(iconCenterX - iconBoxSize / 2, iconCenterY - iconBoxSize / 2, iconBoxSize, iconBoxSize, 8, 8);

            WeaponSpriteManager.renderWeaponSprite(g, weapon, iconCenterX, iconCenterY, iconSize, iconSize);

            // Thông số chi tiết đo lường bằng sao, chí mạng và DPS
            boolean isSniper = (weapon instanceof com.groupproject.game.entity.weapon.SniperRifle);
            g.setFont(GameFontManager.getSmallFont(10.0f));
            g.setColor(new Color(185, 205, 230));
            g.drawString("- Sát thương:", x + 15, y + 66);
            g.drawString("- Tốc ra đạn:", x + 15, y + 80);
            g.drawString(isSniper ? "- Độ xuyên:" : "- Chính xác:", x + 15, y + 94);
            g.drawString("- Chí mạng:", x + 15, y + 108);
            g.drawString("- Chỉ số DPS:", x + 15, y + 122);
            String ammoText = "- Băng đạn: " + weapon.getCurrentAmmo() + " / " + weapon.getMagazineSize();
            if (weapon.getAmmoType() != null) {
                ammoText += " [Kho: " + weapon.getReserveAmmo() + "/" + weapon.getAmmoType().getMaxCapacity() + "]";
            }
            g.drawString(ammoText, x + 15, y + 137);

            // Các cột sao hiển thị đồ họa màu vàng kim và thông số thực tế
            g.setFont(GameFontManager.getSmallBoldFont(10.0f));
            g.setColor(new Color(255, 225, 75));

            int nextX1 = Weapon.renderStarIcons(g, x + 88, y + 66 - 4, weapon.getDamageStars());
            String dmgStr = (weapon.getProjectilesPerShot() > 1)
                    ? ("(" + weapon.getDamage() + "x" + weapon.getProjectilesPerShot() + " DMG)")
                    : ("(" + weapon.getDamage() + " DMG)");
            g.drawString(dmgStr, nextX1, y + 66);

            int nextX2 = Weapon.renderStarIcons(g, x + 88, y + 80 - 4, weapon.getFireRateStars());
            g.drawString("(" + weapon.getFormattedRPM() + ")", nextX2, y + 80);

            if (isSniper) {
                com.groupproject.game.entity.weapon.SniperRifle sr = (com.groupproject.game.entity.weapon.SniperRifle) weapon;
                int nextX3 = Weapon.renderStarIcons(g, x + 88, y + 94 - 4, sr.getPierceStars());
                g.drawString("(" + sr.getMaxPierces() + " mục tiêu | -" + sr.getPierceFalloffPercent() + "%)", nextX3, y + 94);
            } else if (weapon instanceof com.groupproject.game.entity.weapon.SemiCarbineRifle scr) {
                int nextX3 = Weapon.renderStarIcons(g, x + 88, y + 94 - 4, scr.getAccuracyStars());
                g.drawString("(Đầu " + scr.getFormattedInitialSpread() + " | Max " + scr.getFormattedSpread() + ")", nextX3, y + 94);
            } else {
                int nextX3 = Weapon.renderStarIcons(g, x + 88, y + 94 - 4, weapon.getAccuracyStars());
                g.drawString("(" + weapon.getFormattedSpread() + ")", nextX3, y + 94);
            }
            g.setColor(new Color(255, 130, 90));
            g.drawString(weapon.getCritChancePercent() + "% (x" + String.format("%.2f", weapon.getCritMultiplier()) + " DMG)", x + 88, y + 108);
            g.setColor(new Color(0, 225, 255));
            g.drawString(weapon.getNormalDPS() + " (Chí mạng: " + weapon.getCritDPS() + ")", x + 88, y + 122);

            // Nhãn trạng thái trang bị
            if (isEquipped) {
                g.setColor(new Color(25, 70, 50, 220));
                g.fillRoundRect(x + 15, y + 158, w - 30, 26, 6, 6);
                g.setColor(new Color(60, 220, 160));
                g.drawRoundRect(x + 15, y + 158, w - 30, 26, 6, 6);
                g.setFont(GameFontManager.getSmallBoldFont(11.0f));
                drawCenteredString(g, "[ ĐANG CẦM TRÊN TAY ]", x + w / 2, y + 175);
            } else {
                g.setColor(new Color(35, 50, 70));
                g.fillRoundRect(x + 15, y + 158, w - 30, 26, 6, 6);
                g.setColor(new Color(70, 105, 145));
                g.drawRoundRect(x + 15, y + 158, w - 30, 26, 6, 6);
                g.setFont(GameFontManager.getSmallBoldFont(10.5f));
                g.setColor(new Color(180, 210, 240));
                drawCenteredString(g, "[ CLICK / PHÍM " + slot + " ĐỂ CẦM ]", x + w / 2, y + 175);
            }
        } else {
            // Ô trống
            g.setFont(GameFontManager.getBoldFont(13.0f));
            g.setColor(new Color(120, 140, 165));
            drawCenteredString(g, "[ Ô TRỐNG ]", x + w / 2, y + 95);
            g.setFont(GameFontManager.getSmallFont(10.5f));
            drawCenteredString(g, "Mở hòm tiếp tế để nhặt súng", x + w / 2, y + 120);
        }
    }

    private void renderSubCard(Graphics2D g, int x, int y, int w, int h, int slot, String slotTitle,
                              Weapon weapon, boolean isEquipped) {
        g.setColor(new Color(20, 30, 45, 230));
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(isEquipped ? new Color(60, 220, 160) : new Color(50, 75, 105));
        g.setStroke(new BasicStroke(isEquipped ? 2.0f : 1.0f));
        g.drawRoundRect(x, y, w, h, 10, 10);

        // Dòng 1: Tiêu đề Slot
        g.setFont(GameFontManager.getBoldFont(11.0f));
        g.setColor(new Color(255, 200, 60));
        g.drawString(String.format("[%d] %s", slot, slotTitle), x + 12, y + 19);

        if (weapon != null) {
            // Dòng 2: Tên vũ khí và Badge CRIT tự động căn khoảng cách
            String nameText = weapon.getShortDisplayName().toUpperCase();
            g.setFont(GameFontManager.getBoldFont(12.0f));
            g.setColor(weapon.isMelee() ? Color.WHITE : new Color(255, 220, 75));
            g.drawString(nameText, x + 12, y + 36);

            int nameW = g.getFontMetrics().stringWidth(nameText);
            int badgeW = 75;
            int badgeH = 17;
            int badgeX = x + 12 + nameW + 8;
            int badgeY = y + 23;
            g.setColor(new Color(45, 20, 20, 220));
            g.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 5, 5);
            g.setColor(new Color(255, 95, 80));
            g.drawRoundRect(badgeX, badgeY, badgeW, badgeH, 5, 5);
            g.setFont(GameFontManager.getSmallBoldFont(9.5f));
            g.drawString("CRIT: " + weapon.getCritChancePercent() + "%", badgeX + 7, badgeY + 12);

            // Minh họa Sprite vũ khí phụ (Pistol / Knife): Khung nền sáng tương phản 58x58 ở góc phải
            int subBoxSize = 58;
            int subIconSize = 50;
            int subIconCenterX = x + w - 42;
            int subIconCenterY = y + 47;

            // TẤM NỀN SÁNG TƯƠNG PHẢN (LIGHT CONTRAST BACKDROP): Giúp Pistol và Dao găm tối màu nổi bần bật
            g.setColor(new Color(215, 230, 245, 240)); // Trắng bạc ánh xanh băng sáng rực
            g.fillRoundRect(subIconCenterX - subBoxSize / 2, subIconCenterY - subBoxSize / 2, subBoxSize, subBoxSize, 8, 8);
            g.setColor(new Color(255, 255, 255, 220)); // Quầng sáng trắng tâm
            g.fillOval(subIconCenterX - 18, subIconCenterY - 18, 36, 36);
            g.setColor(new Color(75, 130, 190));
            g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(subIconCenterX - subBoxSize / 2, subIconCenterY - subBoxSize / 2, subBoxSize, subBoxSize, 8, 8);

            WeaponSpriteManager.renderWeaponSprite(g, weapon, subIconCenterX, subIconCenterY, subIconSize, subIconSize);

            // Bố cục thông số chi tiết bên trái (gọn gàng, hoàn toàn không chạm vào khung súng bên phải)
            g.setFont(GameFontManager.getSmallFont(10.0f));
            g.setColor(new Color(185, 205, 230));

            // Dòng 3: Sát thương và DPS (loại bỏ lặp lại CRIT để tránh dài dòng đè vào khung ảnh)
            g.drawString(String.format("- Sát thương: %d HP   - DPS: %d (Crit: %d)",
                    weapon.getDamage(), weapon.getNormalDPS(), weapon.getCritDPS()), x + 12, y + 53);

            // Dòng 4: Băng đạn và Tốc bắn / Tốc vung
            if (weapon.isMelee()) {
                g.drawString(String.format("- Tốc vung: %s   - Loại: Cận chiến (--)",
                        weapon.getFormattedRPM()), x + 12, y + 69);
            } else {
                String subAmmo = weapon.hasInfiniteAmmo() ? " (--)" : "";
                g.drawString(String.format("- Băng đạn: %d/%d%s   - Tốc: %s",
                        weapon.getCurrentAmmo(), weapon.getMagazineSize(), subAmmo, weapon.getFormattedRPM()), x + 12, y + 69);
            }

            // Dòng 5: Trạng thái cầm
            if (isEquipped) {
                g.setColor(new Color(60, 220, 160));
                g.setFont(GameFontManager.getSmallBoldFont(10.0f));
                g.drawString("[ ĐANG CẦM ]", x + 12, y + 86);
            } else {
                g.setColor(new Color(130, 155, 185));
                g.setFont(GameFontManager.getSmallFont(9.5f));
                g.drawString("[Click hoặc nhấn " + slot + " để cầm]", x + 12, y + 86);
            }
        }
    }

    private void drawCenteredString(Graphics2D g, String text, int centerX, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(text);
        g.drawString(text, centerX - textW / 2, baselineY);
    }
}
