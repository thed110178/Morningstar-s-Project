package com.groupproject.game.entity.item;

import com.groupproject.game.core.Camera;
import com.groupproject.game.entity.weapon.SniperRifle;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.entity.weapon.WeaponSpriteManager;
import com.groupproject.game.state.PlayingState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Hòm tiếp tế chiến lợi phẩm qua phòng (Tactical Reward Chest / Supply Crate).
 *
 * <p>WHY: Phần thưởng roguelite sau khi dọn sạch một phòng chiến đấu.
 * Hòm sẽ cung cấp 1 trong 2 loại tài nguyên chiến thuật:
 * 1. Tiền tệ (Credits): Tỉ lệ xuất hiện cao (~70%), thưởng từ 35 đến 65 Credits.
 * 2. Súng chính (Primary Weapon): Tỉ lệ xuất hiện thấp hơn (~30%), ngẫu nhiên AR, Shotgun hoặc SMG.
 * Khi người chơi tiến lại gần hòm, nắp hòm TỰ ĐỘNG BẬT MỞ và làm lộ diện chiến lợi phẩm
 * bên trong (nổi lơ lửng với hào quang rực rỡ). Người chơi bấm phím tương tác [F] để thu thập.
 */
public class WeaponChest {

    /**
     * Phân loại phần thưởng bên trong hòm tiếp tế.
     */
    public enum ChestRewardType {
        CREDITS,
        WEAPON
    }

    public static final int CREDITS_CHANCE_PERCENT = 75; // 75% ra tiền, 25% ra súng (tỉ lệ ra tiền cao hơn)

    private final double x;
    private final double y;
    private final double size = 38.0;
    private final boolean guaranteedWeapon; // Đảm bảo rơi súng (cho cây súng free đầu tiên ở Stage 1)
    private final int stage;

    private boolean opened = false;       // Đã bật nắp hòm (tự động khi lại gần)
    private boolean collected = false;    // Người chơi đã bấm [F] để thu thập
    private ChestRewardType rewardType;   // Loại phần thưởng
    private Weapon droppedWeapon;         // Vũ khí thưởng (nếu là WEAPON)
    private int creditsReward = 0;        // Số tiền thưởng (nếu là CREDITS)
    private double bobbingTimer = 0.0;

    /**
     * Khởi tạo hòm tiếp tế tại tọa độ thế giới với tỉ lệ ngẫu nhiên mặc định.
     */
    public WeaponChest(double x, double y) {
        this(x, y, false, 1);
    }

    /**
     * Khởi tạo hòm tiếp tế với tùy chọn đảm bảo rơi súng và chỉ định stage.
     *
     * @param x                Tọa độ X thế giới.
     * @param y                Tọa độ Y thế giới.
     * @param guaranteedWeapon true nếu bắt buộc rơi súng (cây súng free ở Stage 1).
     * @param stage            Stage hiện tại của hầm ngục.
     */
    public WeaponChest(double x, double y, boolean guaranteedWeapon, int stage) {
        this.x = x;
        this.y = y;
        this.guaranteedWeapon = guaranteedWeapon;
        this.stage = Math.max(1, stage);
    }

    /**
     * Factory tạo hòm thưởng chỉ định vũ khí (phục vụ unit test và sự kiện cố định).
     */
    public static WeaponChest createWeaponChest(double x, double y, Weapon weapon) {
        WeaponChest chest = new WeaponChest(x, y, true, 1);
        chest.rewardType = ChestRewardType.WEAPON;
        chest.droppedWeapon = weapon;
        return chest;
    }

    /**
     * Factory tạo hòm thưởng chỉ định số tiền Credits (phục vụ unit test và sự kiện cố định).
     */
    public static WeaponChest createCreditsChest(double x, double y, int credits) {
        WeaponChest chest = new WeaponChest(x, y, false, 1);
        chest.rewardType = ChestRewardType.CREDITS;
        chest.creditsReward = credits;
        return chest;
    }

    /**
     * Mở nắp hòm và sinh phần thưởng:
     * - Ở Stage 1: Đảm bảo phòng đầu tiên luôn cho 1 cây súng free (guaranteedWeapon = true).
     * - Các phòng khác và các stage khác: Do tỉ lệ quyết định (tiền có tỉ lệ ra nhiều hơn súng ~75%).
     */
    public void open() {
        if (!opened) {
            opened = true;
            if (rewardType == null) {
                if (guaranteedWeapon) {
                    // Đảm bảo ở Stage 1 cho 1 cây súng free
                    setupWeaponReward();
                } else {
                    int roll = ThreadLocalRandom.current().nextInt(100);
                    if (roll < CREDITS_CHANCE_PERCENT) {
                        // Tỉ lệ ra tiền thưởng cao hơn (~75% cơ hội ra tiền tệ Credits)
                        setupCreditsReward();
                    } else {
                        // ~25% cơ hội rơi ra vũ khí chính
                        setupWeaponReward();
                    }
                }
            }
        }
    }

    private void setupWeaponReward() {
        rewardType = ChestRewardType.WEAPON;
        creditsReward = 0;
        // Quy tắc: Tỉ lệ chí mạng càng cao thì tỉ lệ ra càng ít (SMG 10% -> 50%, AR 20% -> 30%, Shotgun 30% -> 20%)
        droppedWeapon = Weapon.getRandomDropWeapon();
        // Quy tắc: Stage nào thì cho súng free khi clear room ở lv như thế (Stage 1 -> Lv 1; Stage 2 -> Lv 2; Stage 100 -> Lv 100)
        int weaponLevel = Math.max(1, stage);
        droppedWeapon.configureLevel(weaponLevel);
    }

    private void setupCreditsReward() {
        rewardType = ChestRewardType.CREDITS;
        // Phân tầng tiền thưởng hòm chiến lợi phẩm theo Stage:
        // Stage 1 giảm nhẹ (15-25 Credits) để cân bằng với các mặt hàng giảm giá; tăng dần qua các màn sau
        creditsReward = switch (stage) {
            case 1 -> ThreadLocalRandom.current().nextInt(15, 26); // 15 đến 25 Credits
            case 2 -> ThreadLocalRandom.current().nextInt(25, 41); // 25 đến 40 Credits
            default -> ThreadLocalRandom.current().nextInt(40, 61); // 40 đến 60 Credits
        };
        droppedWeapon = null;
    }

    /**
     * Cập nhật chuyển động nhấp nhô và tự động mở nắp khi người chơi tiếp cận.
     */
    public void update(double deltaTime, double playerX, double playerY, double playerSize) {
        bobbingTimer += deltaTime * 3.5;

        // Khi người chơi đi ra chỗ hòm: Hòm TỰ ĐỘNG MỞ và hiện ra phần thưởng bên trong
        if (!opened && isPlayerInRange(playerX, playerY, playerSize)) {
            open();
        }
    }

    /**
     * Thu thập phần thưởng vào túi đồ hoặc ví tiền khi người chơi ấn phím tương tác [F].
     *
     * @param state Trạng thái gameplay chứa ví Credits và quản lý vũ khí.
     * @return true nếu thu thập thành công; false nếu đã thu thập trước đó.
     */
    public boolean collectReward(PlayingState state) {
        if (!opened) {
            open();
        }
        if (!collected) {
            collected = true;
            if (rewardType == ChestRewardType.CREDITS) {
                state.addCredits(creditsReward);
                state.showTacticalBanner("★ ĐÃ NHẶT: +" + creditsReward + " CREDITS CHIẾN LỢI PHẨM ★",
                        new Color(255, 215, 60), 3.5);
                return true;
            } else if (rewardType == ChestRewardType.WEAPON && droppedWeapon != null) {
                Weapon toEquip = droppedWeapon;
                Weapon replaced = state.getWeaponManager().equipWeapon(toEquip);
                if (replaced != null) {
                    // Cả 2 slot chính đã đầy: hoán đổi vũ khí bị thay thế vào hòm dưới đất để người chơi nhặt lại nếu bấm nhầm
                    this.droppedWeapon = replaced;
                    this.collected = false;
                    state.showTacticalBanner("★ ĐÃ TRANG BỊ: " + toEquip.getShortDisplayName()
                            + " | VŨ KHÍ CŨ (" + replaced.getShortDisplayName() + ") ĐÃ ĐẶT VÀO HÒM DƯỚI ĐẤT ★",
                            new Color(255, 215, 60), 3.5);
                } else {
                    this.collected = true;
                    this.droppedWeapon = null;
                    state.showTacticalBanner("★ ĐÃ TRANG BỊ: " + toEquip.getName().toUpperCase()
                            + " (" + toEquip.getShortDisplayName() + ") [SLOT " + toEquip.getSlotIndex() + "] ★",
                            new Color(255, 215, 60), 3.5);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Thu thập vũ khí vào túi đồ khi người chơi ấn phím tương tác chính [F] (Tương thích ngược).
     */
    public Weapon collect() {
        if (!opened) {
            open();
        }
        if (!collected) {
            collected = true;
            return droppedWeapon;
        }
        return null;
    }

    /**
     * Kiểm tra người chơi có đang đứng trong phạm vi tương tác (bán kính ~55px).
     */
    public boolean isPlayerInRange(double px, double py, double pSize) {
        double chestCenterX = x + size / 2.0;
        double chestCenterY = y + size / 2.0;
        double playerCenterX = px + pSize / 2.0;
        double playerCenterY = py + pSize / 2.0;

        double distSq = Math.pow(chestCenterX - playerCenterX, 2) + Math.pow(chestCenterY - playerCenterY, 2);
        return distSq <= 58.0 * 58.0;
    }

    /**
     * Vẽ mô hình hòm tiếp tế, hiệu ứng phát quang, vũ khí nổi lơ lửng và lời nhắc [F].
     */
    public void render(Graphics2D g, Camera camera, boolean showPrompt) {
        int screenX = camera.worldToScreenX(x);
        int screenY = camera.worldToScreenY(y);

        // Viewport Culling: Bỏ qua nếu hòm tiếp tế nằm ngoài tầm nhìn camera
        if (screenX < -50 || screenX > com.groupproject.game.core.GameConfig.SCREEN_WIDTH + 50 ||
            screenY < -50 || screenY > com.groupproject.game.core.GameConfig.SCREEN_HEIGHT + 50) {
            return;
        }

        // Hiệu ứng nhấp nhô nhẹ lơ lửng của vật phẩm thưởng
        double floatOffset = Math.sin(bobbingTimer) * 4.0;
        int renderY = (int) (screenY + floatOffset);

        if (!opened) {
            // --- 1. TRẠNG THÁI CHƯA MỞ (Chờ người chơi tiếp cận) ---
            // Hào quang vàng kim dưới chân
            g.setColor(new Color(255, 215, 60, 45));
            g.fillOval(screenX - 8, renderY + 22, (int) size + 16, 16);

            // Thân hòm quân sự mạ vàng
            g.setColor(new Color(180, 130, 40));
            g.fillRoundRect(screenX, renderY, (int) size, (int) size, 6, 6);

            // Viền kim loại vàng bóng
            g.setColor(new Color(255, 225, 100));
            g.setStroke(new BasicStroke(2.0f));
            g.drawRoundRect(screenX, renderY, (int) size, (int) size, 6, 6);

            // Khóa nắp hòm chính diện
            g.setColor(new Color(50, 40, 20));
            g.fillRect(screenX + (int) size / 2 - 4, renderY + (int) size / 2 - 4, 8, 8);
            g.setColor(new Color(255, 255, 200));
            g.drawRect(screenX + (int) size / 2 - 4, renderY + (int) size / 2 - 4, 8, 8);

        } else if (!collected) {
            // --- 2. TRẠNG THÁI ĐÃ TỰ ĐỘNG MỞ & LỘ DIỆN KHẨU SÚNG BÊN TRONG ---
            // Hào quang xung quanh miệng hòm
            g.setColor(new Color(255, 220, 80, 70));
            g.fillOval(screenX - 12, renderY - 14, (int) size + 24, 30);

            // Đáy hòm mở
            g.setColor(new Color(110, 85, 30));
            g.fillRoundRect(screenX, renderY + 10, (int) size, (int) size - 10, 6, 6);
            g.setColor(new Color(60, 50, 25));
            g.drawRoundRect(screenX, renderY + 10, (int) size, (int) size - 10, 6, 6);

            // Nắp hòm mở lật lên trên
            g.setColor(new Color(160, 120, 40));
            g.fillRect(screenX - 3, renderY + 2, (int) size + 6, 8);
            g.setColor(new Color(255, 220, 100));
            g.drawRect(screenX - 3, renderY + 2, (int) size + 6, 8);

            int rewardCenterY = renderY - 16;
            int rewardCenterX = screenX + (int) size / 2;

            if (rewardType == ChestRewardType.CREDITS) {
                // VẼ MÔ HÌNH TIỀN TỆ CREDITS NỔI LƠ LỬNG TRÊN MIỆNG HÒM
                // Hào quang vàng rực rỡ
                g.setColor(new Color(255, 230, 80, 90));
                g.fillOval(rewardCenterX - 18, rewardCenterY - 18, 36, 36);

                // 2 đồng xu vàng phía sau tạo hiệu ứng đống tiền
                g.setColor(new Color(210, 160, 20));
                g.fillOval(rewardCenterX - 13, rewardCenterY - 4, 14, 14);
                g.setColor(new Color(255, 215, 60));
                g.fillOval(rewardCenterX + 1, rewardCenterY - 4, 14, 14);

                // Đồng xu trung tâm lớn với chữ C
                g.setColor(new Color(255, 225, 70));
                g.fillOval(rewardCenterX - 10, rewardCenterY - 10, 20, 20);
                g.setColor(new Color(180, 130, 15));
                g.setStroke(new BasicStroke(1.5f));
                g.drawOval(rewardCenterX - 10, rewardCenterY - 10, 20, 20);

                g.setFont(new Font("SansSerif", Font.BOLD, 12));
                g.setColor(new Color(50, 35, 10));
                g.drawString("C", rewardCenterX - 4, rewardCenterY + 4);

                // Nhãn tiền nổi
                String creditTitle = "★ +" + creditsReward + " CREDITS ★";
                renderTag(g, rewardCenterX, rewardCenterY - 18, creditTitle, new Color(255, 220, 60));

                if (showPrompt) {
                    String promptText = "[F] NHẶT +" + creditsReward + " CREDITS";
                    renderPrompt(g, rewardCenterX, rewardCenterY - 40, promptText);
                }

            } else if (droppedWeapon != null) {
                // Vị trí tâm súng nhô cao hơn miệng hòm để hiển thị kích thước lớn rõ nét
                int weaponDisplayY = rewardCenterY - 8;

                // ĐĨA NỀN SÁNG TƯƠNG PHẢN CAO: Giúp thân súng màu tối nổi bần bật, không bị chìm vào sàn đá tối của hầm ngục
                int basePulse = (int) (3 * Math.sin(bobbingTimer * 2.0));
                // 1. Quầng hào quang khuếch tán bên ngoài
                g.setColor(new Color(255, 230, 110, 60));
                g.fillOval(rewardCenterX - 34 - basePulse, weaponDisplayY - 34 - basePulse, (34 + basePulse) * 2, (34 + basePulse) * 2);
                // 2. Đĩa sáng ấm rực rỡ
                g.setColor(new Color(255, 245, 185, 175));
                g.fillOval(rewardCenterX - 26, weaponDisplayY - 26, 52, 52);
                // 3. Lõi trắng sáng tương phản trực tiếp sau thân súng
                g.setColor(new Color(255, 255, 250, 235));
                g.fillOval(rewardCenterX - 18, weaponDisplayY - 18, 36, 36);

                // VẼ HIỆU ỨNG ĐẶC BIỆT NẾU LÀ KHÍ TÀI HẠNG S (S-TIER LEGENDARY AURA)
                if (droppedWeapon.isSTier()) {
                    // 1. Quầng hào quang vàng kim hoàng gia nhấp nhô theo nhịp thở
                    int pulse = (int) (6 * Math.sin(bobbingTimer * 2.5));
                    int auraRadius = 38 + pulse;
                    g.setColor(new Color(255, 215, 0, Math.clamp(85 + pulse * 4, 40, 140)));
                    g.fillOval(rewardCenterX - auraRadius, weaponDisplayY - auraRadius, auraRadius * 2, auraRadius * 2);

                    // 2. Tia sáng năng lượng lấp lánh (Sparkles) 4 góc
                    g.setColor(new Color(255, 255, 200, 220));
                    int sparkDist = 28 + pulse / 2;
                    g.fillRect(rewardCenterX - 1, weaponDisplayY - sparkDist, 3, 7);
                    g.fillRect(rewardCenterX - 1, weaponDisplayY + sparkDist - 6, 3, 7);
                    g.fillRect(rewardCenterX - sparkDist, weaponDisplayY - 1, 7, 3);
                    g.fillRect(rewardCenterX + sparkDist - 6, weaponDisplayY - 1, 7, 3);

                    // 3. Ký hiệu huy hiệu [S] viễn tưởng bay lơ lửng góc trên súng
                    int badgeX = rewardCenterX + 26;
                    int badgeY = weaponDisplayY - 24;
                    g.setColor(new Color(30, 20, 5, 230));
                    g.fillRoundRect(badgeX - 7, badgeY - 7, 15, 15, 4, 4);
                    g.setColor(new Color(255, 215, 0));
                    g.setStroke(new BasicStroke(1.2f));
                    g.drawRoundRect(badgeX - 7, badgeY - 7, 15, 15, 4, 4);
                    g.setFont(new Font("SansSerif", Font.BOLD, 10));
                    g.drawString("S", badgeX - 3, badgeY + 4);
                }

                // VẼ MÔ HÌNH VŨ KHÍ NỔI LƠ LỬNG TRÊN MIỆNG HÒM: Phóng to lên 58x58 pixel to rõ, đẹp mắt
                WeaponSpriteManager.renderWeaponSprite(g, droppedWeapon, rewardCenterX, weaponDisplayY, 58, 58);

                if (showPrompt) {
                    // Khi người chơi tiến vào phạm vi: Hiện ô UI Tooltip hiển thị chi tiết 3 hàng sao và thông số
                    renderWeaponStatCard(g, rewardCenterX, weaponDisplayY - 26, droppedWeapon, "NHẶT TRANG BỊ");
                } else {
                    // Khi đứng xa: Chỉ hiện nhãn tên súng và cấp độ cơ bản (Có viền vàng rực nếu là S-Tier)
                    String weaponTitle = "★ " + droppedWeapon.getShortDisplayName().toUpperCase() + " ★";
                    Color tagColor = droppedWeapon.isSTier() ? new Color(255, 215, 0) : new Color(255, 215, 60);
                    renderTag(g, rewardCenterX, weaponDisplayY - 24, weaponTitle, tagColor);
                }
            }

        } else {
            // --- 3. TRẠNG THÁI ĐÃ NHẶT XONG (Hòm rỗng nắp mở) ---
            g.setColor(new Color(90, 70, 25));
            g.fillRoundRect(screenX, renderY + 10, (int) size, (int) size - 10, 6, 6);
            g.setColor(new Color(50, 40, 20));
            g.drawRoundRect(screenX, renderY + 10, (int) size, (int) size - 10, 6, 6);

            // Nắp mở
            g.setColor(new Color(130, 95, 30));
            g.fillRect(screenX - 3, renderY + 2, (int) size + 6, 8);
        }

        g.setStroke(new BasicStroke(1.0f));
    }

    private void renderTag(Graphics2D g, int centerX, int centerY, String text, Color textColor) {
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(text);
        int pad = 6;

        int boxW = textW + pad * 2;
        int boxH = 18;
        int boxX = centerX - boxW / 2;
        int boxY = centerY - boxH / 2;

        g.setColor(new Color(15, 20, 30, 210));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 4, 4);
        g.setColor(new Color(60, 80, 110));
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

        g.setColor(new Color(15, 20, 30, 230));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 6, 6);
        g.setColor(new Color(60, 220, 160)); // Viền dạ quang tương tác chính [F]
        g.drawRoundRect(boxX, boxY, boxW, boxH, 6, 6);

        // Phím [F] nổi bật màu vàng
        g.setColor(new Color(255, 220, 60));
        g.drawString("[F]", boxX + pad, boxY + 15);

        // Chữ nội dung
        g.setColor(Color.WHITE);
        g.drawString(text.substring(3), boxX + pad + fm.stringWidth("[F] "), boxY + 15);
    }

    /**
     * Vẽ ô UI Tooltip hiển thị bảng 3 hàng sao và thông số vũ khí chi tiết khi người chơi tiến lại gần.
     */
    public static void renderWeaponStatCard(Graphics2D g, int centerX, int bottomY, Weapon weapon, String actionPrompt) {
        if (weapon == null) return;

        boolean isSTier = weapon.isSTier();
        int boxW = 236;
        int boxH = isSTier ? 148 : 130;
        int boxX = centerX - boxW / 2;
        int boxY = bottomY - boxH;

        // 1. Nền Card bo góc bán trong suốt phong cách Tactical HUD
        g.setColor(new Color(15, 22, 35, 240));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        Color borderColor = isSTier ? new Color(255, 215, 0) : new Color(60, 220, 160);
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(isSTier ? 2.0f : 1.5f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        // 2. Tiêu đề tên súng kèm cấp độ và số lần nâng cấp (VD: ★ [S] AR I [0/3] ★)
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(isSTier ? new Color(255, 225, 60) : new Color(255, 215, 60));
        String title = "★ " + weapon.getShortDisplayName().toUpperCase() + " [" + weapon.getUpgradeCount() + "/" + Weapon.MAX_UPGRADE_COUNT + "] ★";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, centerX - fm.stringWidth(title) / 2, boxY + 16);

        // Đường gạch mảnh phân cách
        g.setColor(isSTier ? new Color(120, 95, 30) : new Color(50, 75, 100));
        g.drawLine(boxX + 12, boxY + 22, boxX + boxW - 12, boxY + 22);

        // 3. 5 hàng chỉ số đo bằng sao, chí mạng và DPS
        boolean isSniper = weapon instanceof SniperRifle;
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.setColor(new Color(200, 220, 240));
        g.drawString("• Sát thương:", boxX + 14, boxY + 36);
        g.drawString("• Tốc ra đạn:", boxX + 14, boxY + 51);
        g.drawString(isSniper ? "• Sức xuyên:" : "• Chính xác:", boxX + 14, boxY + 66);
        g.drawString("• Chí mạng:", boxX + 14, boxY + 81);
        g.drawString("• Chỉ số DPS:", boxX + 14, boxY + 96);

        // Giá trị sao màu vàng kim và thông số
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.setColor(new Color(255, 225, 75));
        g.drawString(Weapon.formatStars(weapon.getDamageStars()) + " (" + weapon.getDamage() + " DMG)", boxX + 88, boxY + 36);
        g.drawString(Weapon.formatStars(weapon.getFireRateStars()) + " (" + weapon.getFormattedRPM() + ")", boxX + 88, boxY + 51);
        if (isSniper) {
            SniperRifle sr = (SniperRifle) weapon;
            g.drawString(Weapon.formatStars(sr.getPierceStars()) + " (" + sr.getMaxPierces() + " mục tiêu | -" + sr.getPierceFalloffPercent() + "%)", boxX + 88, boxY + 66);
        } else {
            g.drawString(Weapon.formatStars(weapon.getAccuracyStars()) + " (" + weapon.getFormattedSpread() + ")", boxX + 88, boxY + 66);
        }
        g.setColor(new Color(255, 130, 90));
        g.drawString(weapon.getCritChancePercent() + "% (x" + String.format("%.2f", weapon.getCritMultiplier()) + " DMG)", boxX + 88, boxY + 81);
        g.setColor(new Color(0, 225, 255));
        g.drawString(weapon.getNormalDPS() + " (Chí mạng: " + weapon.getCritDPS() + ")", boxX + 88, boxY + 96);

        int promptY = boxY + 106;
        if (isSTier) {
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.setColor(new Color(255, 215, 0));
            g.drawString("★ ĐẶC QUYỀN S: +1 Cấp mỗi khi qua Stage", boxX + 14, boxY + 112);
            promptY = boxY + 122;
        }

        // 4. Thanh lời nhắc phím bấm [F]
        g.setColor(new Color(25, 65, 45, 230));
        g.fillRoundRect(boxX + 10, promptY, boxW - 20, 16, 4, 4);
        g.setColor(new Color(60, 220, 160));
        g.drawRoundRect(boxX + 10, promptY, boxW - 20, 16, 4, 4);

        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.setColor(new Color(255, 220, 60));
        g.drawString("[F] " + actionPrompt, boxX + 16, promptY + 12);

        g.setStroke(new BasicStroke(1.0f));
    }

    public boolean isOpened() {
        return opened;
    }

    public boolean isCollected() {
        return collected;
    }

    public Weapon getDroppedWeapon() {
        return droppedWeapon;
    }

    public ChestRewardType getRewardType() {
        return rewardType;
    }

    public int getCreditsReward() {
        return creditsReward;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isGuaranteedWeapon() {
        return guaranteedWeapon;
    }

    public int getStage() {
        return stage;
    }
}
