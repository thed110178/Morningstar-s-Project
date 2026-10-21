package com.groupproject.game.entity.buff;

/**
 * Định nghĩa danh mục toàn bộ các Buff Cường Hóa Nhân Vật (Character Upgrades & Perks).
 *
 * <p>WHY: Phân chia rõ ràng thành 2 nhóm theo phản hồi của người chơi:
 * <ul>
 * <li>Nhóm 1 - Chỉ số cơ bản: Máu, Công, Thủ, Tốc, CD Né, May Mắn (giá vừa phải, có thể nâng nhiều cấp tới mốc trần).</li>
 * <li>Nhóm 2 - Kỹ năng đặc biệt: Giáp chặn đòn, Diệt địch hồi máu, Né xuyên vật cản, Thu nhỏ hitbox,
 * Tiếp đạn chu kỳ, Băng đạn mở rộng, Bậc thầy chí mạng (giá cao, sở hữu hiệu ứng đặc biệt độc nhất).</li>
 * </ul>
 */
public enum CharacterBuff {

    // ==========================================
    // NHÓM 1: BUFF CHỈ SỐ CƠ BẢN (BASIC STATS)
    // ==========================================
    MAX_HEALTH(
            BuffCategory.BASIC_STAT,
            "TĂNG % MÁU TỐI ĐA",
            "+20% Máu tối đa mỗi cấp (Tối đa 200% = 200 HP ban đầu)",
            45,
            5
    ),
    ATTACK_DAMAGE(
            BuffCategory.BASIC_STAT,
            "TĂNG % TẤN CÔNG",
            "+10% Sát thương toàn bộ vũ khí & dao (Tối đa 150% = 1.5x)",
            50,
            5
    ),
    MOVE_SPEED(
            BuffCategory.BASIC_STAT,
            "TĂNG % TỐC ĐỘ",
            "+5% Tốc độ di chuyển nhân vật (Tối đa 120% = 1.2x = 252 px/s)",
            40,
            4
    ),
    DEFENSE(
            BuffCategory.BASIC_STAT,
            "TĂNG % PHÒNG THỦ",
            "Giảm 10% sát thương nhận vào mỗi cấp (Tối đa giảm 30% sát thương)",
            50,
            3
    ),
    DODGE_COOLDOWN(
            BuffCategory.BASIC_STAT,
            "GIẢM HỒI CHIÊU NÉ",
            "Rút ngắn hồi chiêu lướt né (Tối đa nhanh hơn 1.5 lần, 0.8s -> 0.533s)",
            45,
            3
    ),
    LOOT_LUCK(
            BuffCategory.BASIC_STAT,
            "MAY MẮN NHẶT ĐỒ",
            "+25% Tỉ lệ mở rương ra vũ khí nhiều sao (4-5★) và súng Đỏ Mythic",
            40,
            3
    ),

    // ==========================================
    // NHÓM 2: BUFF KỸ NĂNG ĐẶC BIỆT (SPECIALS)
    // ==========================================
    ONE_HIT_BARRIER(
            BuffCategory.SPECIAL,
            "GIÁP CHẶN ĐÒN HỒI PHỤC",
            "Tạo khiên chặn đứng 100% 1 lần bất kỳ sát thương. Hồi lại sau 10s.",
            135,
            1
    ),
    KILL_HEAL(
            BuffCategory.SPECIAL,
            "DIỆT ĐỊCH HỒI MÁU",
            "Hồi ngay +2.5 HP sinh lực với mỗi kẻ địch bị tiêu diệt.",
            120,
            1
    ),
    PHASE_DODGE(
            BuffCategory.SPECIAL,
            "NÉ XUYÊN VẬT CẢN",
            "Lướt né [SPACE] cho phép đi xuyên qua vật cản tạm thời và duy trì bất tử an toàn cho tới khi thoát ra.",
            140,
            1
    ),
    SHRINK_HITBOX(
            BuffCategory.SPECIAL,
            "THU NHỎ THÂN THỂ",
            "Giảm 30% kích thước hitbox nhân vật, luồn lách né đạn cực kỳ cơ động.",
            125,
            1
    ),
    PERIODIC_AMMO(
            BuffCategory.SPECIAL,
            "BĂNG ĐẠN TIẾP TẾ",
            "Cứ mỗi 30 giây tự động tiếp tế +2 băng đạn dự trữ cho vũ khí chính.",
            130,
            1
    ),
    EXTENDED_MAGAZINE(
            BuffCategory.SPECIAL,
            "BĂNG ĐẠN MỞ RỘNG",
            "Tăng đạn 1 băng: Rifle 40 viên, SMG 60 viên, Súng lục 12 viên.",
            135,
            1
    ),
    CRITICAL_MASTERY(
            BuffCategory.SPECIAL,
            "BẬC THẦY CHÍ MẠNG",
            "+15% Tỉ lệ chí mạng. Sát thương crit: Rifle 150%, SMG & Lục 120%, Dao găm 200%.",
            150,
            1
    );

    public enum BuffCategory {
        BASIC_STAT,
        SPECIAL
    }

    private final BuffCategory category;
    private final String displayNameVi;
    private final String descriptionVi;
    private final int baseCost;
    private final int maxLevel;

    CharacterBuff(BuffCategory category, String displayNameVi, String descriptionVi, int baseCost, int maxLevel) {
        this.category = category;
        this.displayNameVi = displayNameVi;
        this.descriptionVi = descriptionVi;
        this.baseCost = baseCost;
        this.maxLevel = maxLevel;
    }

    public BuffCategory getCategory() {
        return category;
    }

    public String getDisplayNameVi() {
        return displayNameVi;
    }

    public String getDescriptionVi() {
        return descriptionVi;
    }

    public int getBaseCost() {
        return baseCost;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * Tính toán giá Credits cho cấp độ tiếp theo theo Stage hiện tại.
     *
     * <p>WHY: Cân bằng kinh tế tác chiến theo giai đoạn:
     * - Stage 1 (Khởi đầu): Giảm nhẹ giá để người chơi có thể mua 1-3 buff cơ bản hoặc tích lũy mua 1 buff đặc biệt.
     * - Stage 2: Tăng giá vừa phải để theo kịp lượng Credits rơi ra nhiều hơn từ quái và hòm.
     * - Stage 3+: Giá chuẩn và tăng theo cấp độ nâng để duy trì thử thách sinh tồn.
     *
     * @param currentLevel Cấp độ buff hiện tại của người chơi.
     * @param stage        Số thứ tự Stage hiện tại (1, 2, 3, 4...).
     * @return Chi phí Credits cần thiết, hoặc -1 nếu đã đạt cấp tối đa.
     */
    public int getCostForLevel(int currentLevel, int stage) {
        if (currentLevel >= maxLevel) return -1;
        int effectiveStage = Math.max(1, stage);
        int base;
        if (category == BuffCategory.BASIC_STAT) {
            if (effectiveStage == 1) {
                base = Math.max(25, (int) Math.round(baseCost * 0.60)); // 40 -> 25; 45 -> 27; 50 -> 30
            } else if (effectiveStage == 2) {
                base = Math.max(32, (int) Math.round(baseCost * 0.80)); // 40 -> 32; 45 -> 36; 50 -> 40
            } else {
                base = baseCost + (effectiveStage - 3) * 5;
            }
        } else {
            if (effectiveStage == 1) {
                base = Math.max(75, (int) Math.round(baseCost * 0.60)); // 120 -> 75; 135 -> 81; 150 -> 90
            } else if (effectiveStage == 2) {
                base = Math.max(98, (int) Math.round(baseCost * 0.80)); // 120 -> 98; 135 -> 108; 150 -> 120
            } else {
                base = baseCost + (effectiveStage - 3) * 15;
            }
        }
        return base + currentLevel * 15;
    }

    /**
     * Quá tải mặc định tính giá theo Stage 1 (phục vụ tương thích ngược).
     */
    public int getCostForLevel(int currentLevel) {
        return getCostForLevel(currentLevel, 1);
    }
}
