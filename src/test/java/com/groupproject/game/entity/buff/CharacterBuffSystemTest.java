package com.groupproject.game.entity.buff;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.dungeon.Tile;
import com.groupproject.game.dungeon.TileType;
import com.groupproject.game.entity.player.Player;
import com.groupproject.game.entity.station.CharacterUpgradeStation;
import com.groupproject.game.entity.weapon.AssaultRifle;
import com.groupproject.game.entity.weapon.CombatKnife;
import com.groupproject.game.entity.weapon.Pistol;
import com.groupproject.game.entity.weapon.SMG;
import com.groupproject.game.entity.weapon.Shotgun;
import com.groupproject.game.entity.weapon.WeaponManager;
import com.groupproject.game.state.GameStateManager;
import com.groupproject.game.state.PlayingState;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động toàn diện cho Hệ thống Nâng Cấp & Buff Nhân Vật (Character Upgrade & Buffs).
 *
 * <p>WHY: Đảm bảo tính toán chính xác 13 loại Buff (6 buff cơ bản và 7 buff đặc biệt),
 * hành vi khiên chặn đòn 10s CD, diệt địch hồi 2.5 HP, mở rộng băng đạn cho AR/SMG/Lục,
 * giảm 30% hitbox, né xuyên vật cản, và logic mua bán tại 2 trạm riêng biệt.
 */
public class CharacterBuffSystemTest {

    private Player player;
    private PlayerBuffManager buffManager;
    private WeaponManager weaponManager;

    @BeforeEach
    void setUp() {
        player = new Player(100, 100);
        buffManager = player.getBuffManager();
        weaponManager = new WeaponManager();
        weaponManager.bindPlayerBuffManager(buffManager);
    }

    @Test
    @DisplayName("Kiểm tra 6 Buff chỉ số cơ bản: Máu, Tấn công, Tốc độ, Phòng thủ, CD Né, May mắn")
    void testBasicStatBuffsScalingAndCaps() {
        // 1. Máu tối đa (+20% mỗi cấp, tối đa 200% = 200 HP ở Cấp 5)
        assertEquals(1.0, buffManager.getMaxHealthMultiplier(), 0.001);
        assertEquals(100, player.getMaxHealth());
        for (int i = 0; i < 5; i++) {
            buffManager.applyBuff(CharacterBuff.MAX_HEALTH, player, weaponManager);
        }
        assertEquals(2.0, buffManager.getMaxHealthMultiplier(), 0.001);
        assertEquals(200, player.getMaxHealth(), "Max HP phải đạt 200 HP ở cấp 5");

        // 2. Tấn công (+10% mỗi cấp, tối đa 150% = 1.5x ở Cấp 5)
        assertEquals(1.0, buffManager.getAttackMultiplier(), 0.001);
        for (int i = 0; i < 5; i++) {
            buffManager.applyBuff(CharacterBuff.ATTACK_DAMAGE, player, weaponManager);
        }
        assertEquals(1.5, buffManager.getAttackMultiplier(), 0.001, "Sát thương tấn công phải đạt 1.5x ở cấp 5");

        // 3. Tốc độ (+5% mỗi cấp, tối đa 120% = 1.2x ở Cấp 4)
        assertEquals(1.0, buffManager.getSpeedMultiplier(), 0.001);
        for (int i = 0; i < 4; i++) {
            buffManager.applyBuff(CharacterBuff.MOVE_SPEED, player, weaponManager);
        }
        assertEquals(1.2, buffManager.getSpeedMultiplier(), 0.001, "Tốc độ phải đạt 1.2x ở cấp 4");

        // 4. Phòng thủ (+10% giảm sát thương mỗi cấp, tối đa 30% ở Cấp 3)
        assertEquals(0.0, buffManager.getDamageReduction(), 0.001);
        for (int i = 0; i < 3; i++) {
            buffManager.applyBuff(CharacterBuff.DEFENSE, player, weaponManager);
        }
        assertEquals(0.30, buffManager.getDamageReduction(), 0.001, "Phòng thủ phải giảm tối đa 30% sát thương");

        // 5. Cooldown né (nhanh hơn 1.5 lần ở Cấp 3)
        assertEquals(1.0, buffManager.getDodgeCooldownFactor(), 0.001);
        for (int i = 0; i < 3; i++) {
            buffManager.applyBuff(CharacterBuff.DODGE_COOLDOWN, player, weaponManager);
        }
        assertEquals(1.5, buffManager.getDodgeCooldownFactor(), 0.001, "Hệ số rút ngắn CD né phải là 1.5x");

        // 6. May mắn nhặt đồ (+25% mỗi cấp, tối đa cấp 3 = 0.75x)
        for (int i = 0; i < 3; i++) {
            buffManager.applyBuff(CharacterBuff.LOOT_LUCK, player, weaponManager);
        }
        assertEquals(0.75, buffManager.getLootLuckRatio(), 0.001);
    }

    @Test
    @DisplayName("Kiểm tra Khiên Chặn Đòn 1 lần: chặn 100% sát thương và hồi sau 10 giây CD")
    void testOneHitBarrierBehavior() {
        assertFalse(buffManager.hasOneHitBarrier());
        assertFalse(buffManager.checkAndConsumeBarrier());

        // Kích hoạt buff
        buffManager.applyBuff(CharacterBuff.ONE_HIT_BARRIER, player, weaponManager);
        assertTrue(buffManager.hasOneHitBarrier());
        assertTrue(buffManager.isBarrierReady(), "Khiên phải sẵn sàng ngay sau khi nâng cấp");

        // Nhận đòn 100 sát thương: Khiên kích hoạt chặn hoàn toàn, máu không suy suyển
        int initialHp = player.getCurrentHealth();
        boolean damaged = player.takeDamage(100);
        assertFalse(damaged, "Đòn đánh phải bị khiên hấp thụ hoàn toàn");
        assertEquals(initialHp, player.getCurrentHealth(), "Máu không bị giảm khi có khiên");
        assertFalse(buffManager.isBarrierReady(), "Khiên phải bước vào trạng thái hồi chiêu");
        assertEquals(10.0, buffManager.getBarrierCooldownTimer(), 0.01);

        // Đòn đánh thứ hai khi khiên đang cooldown: Máu bị trừ bình thường
        boolean damagedAgain = player.takeDamage(20);
        assertTrue(damagedAgain);
        assertEquals(initialHp - 20, player.getCurrentHealth());

        // Cập nhật 5 giây: Khiên vẫn chưa hồi
        buffManager.update(5.0, player, weaponManager);
        assertFalse(buffManager.isBarrierReady());
        assertEquals(5.0, buffManager.getBarrierCooldownTimer(), 0.01);

        // Cập nhật thêm 5 giây: Khiên hồi phục thành công
        buffManager.update(5.0, player, weaponManager);
        assertTrue(buffManager.isBarrierReady(), "Khiên phải hồi phục sau 10 giây");
    }

    @Test
    @DisplayName("Kiểm tra Diệt địch hồi máu: hồi chính xác 2.5 HP mỗi mạng (tích lũy phần lẻ)")
    void testKillHealBuffAccumulator() {
        // Chưa có buff
        assertEquals(0, buffManager.onEnemyKilled());

        // Mua buff diệt địch hồi máu
        buffManager.applyBuff(CharacterBuff.KILL_HEAL, player, weaponManager);

        // Địch 1: 2.5 HP tích lũy -> trả về 2 HP (dư 0.5 HP)
        int heal1 = buffManager.onEnemyKilled();
        assertEquals(2, heal1);

        // Địch 2: +2.5 HP nữa = 3.0 HP tích lũy -> trả về 3 HP (dư 0.0 HP)
        int heal2 = buffManager.onEnemyKilled();
        assertEquals(3, heal2);

        // Tổng 2 mạng diệt hồi đúng 5.0 HP (2.5 * 2)
        assertEquals(5, heal1 + heal2);
    }

    @Test
    @DisplayName("Kiểm tra Thu nhỏ Hitbox: giảm đúng 30% kích thước hitbox (scale = 0.70x)")
    void testShrinkHitboxBuff() {
        assertEquals(1.0, buffManager.getHitboxScale(), 0.001);
        buffManager.applyBuff(CharacterBuff.SHRINK_HITBOX, player, weaponManager);
        assertTrue(buffManager.hasShrinkHitbox());
        assertEquals(0.70, buffManager.getHitboxScale(), 0.001, "Hitbox scale phải là 0.70x (giảm 30%)");
    }

    @Test
    @DisplayName("Kiểm tra Băng Đạn Mở Rộng: Rifle lên 40, SMG lên 60, Pistol lên 12 viên")
    void testExtendedMagazinesBuff() {
        AssaultRifle ar = new AssaultRifle();
        ar.setSlotIndex(1);
        SMG smg = new SMG();
        smg.setSlotIndex(2);
        Pistol pistol = new Pistol();

        // Ban đầu
        assertEquals(30, ar.getMagazineSize());
        assertEquals(25, smg.getMagazineSize());
        assertEquals(7, pistol.getMagazineSize());

        weaponManager.addWeapon(ar);
        weaponManager.addWeapon(smg);
        weaponManager.addWeapon(pistol);

        // Mua buff Băng đạn mở rộng
        buffManager.applyBuff(CharacterBuff.EXTENDED_MAGAZINE, player, weaponManager);
        buffManager.applyExtendedMagazines(weaponManager);

        assertEquals(40, ar.getMagazineSize(), "Assault Rifle phải mở rộng lên 40 viên");
        assertEquals(60, smg.getMagazineSize(), "SMG phải mở rộng lên 60 viên");
        assertEquals(12, pistol.getMagazineSize(), "Pistol phải mở rộng lên 12 viên");
    }

    @Test
    @DisplayName("Kiểm tra Chí mạng: +15% tỉ lệ chí mạng và hệ số sát thương chí mạng chuyên biệt")
    void testCriticalMasteryBuff() {
        AssaultRifle ar = new AssaultRifle();
        SMG smg = new SMG();
        Pistol pistol = new Pistol();
        CombatKnife knife = new CombatKnife();
        Shotgun shotgun = new Shotgun();

        assertEquals(0.0, buffManager.getCritChanceBonus(), 0.001);

        buffManager.applyBuff(CharacterBuff.CRITICAL_MASTERY, player, weaponManager);
        assertEquals(0.15, buffManager.getCritChanceBonus(), 0.001, "Phải cộng thêm đúng 15% tỉ lệ crit");

        assertEquals(1.50, buffManager.getCustomCritMultiplier(ar), 0.001, "Crit AR phải là 1.5x (150%)");
        assertEquals(1.20, buffManager.getCustomCritMultiplier(smg), 0.001, "Crit SMG phải là 1.2x (120%)");
        assertEquals(1.20, buffManager.getCustomCritMultiplier(pistol), 0.001, "Crit Pistol phải là 1.2x (120%)");
        assertEquals(2.00, buffManager.getCustomCritMultiplier(knife), 0.001, "Crit Dao găm phải là 2.0x (200%)");
        assertEquals(1.50, buffManager.getCustomCritMultiplier(shotgun), 0.001);
    }

    @Test
    @DisplayName("Kiểm tra Băng Đạn Tiếp Tế: Cấp 2 băng đạn vũ khí chính mỗi 30 giây")
    void testPeriodicAmmoSupply() {
        AssaultRifle ar = new AssaultRifle();
        ar.setSlotIndex(1);
        weaponManager.addWeapon(ar);
        weaponManager.selectSlot(1);

        int initialReserve = weaponManager.getAmmoInventory().getAmmo(ar.getAmmoType());

        buffManager.applyBuff(CharacterBuff.PERIODIC_AMMO, player, weaponManager);
        assertTrue(buffManager.hasPeriodicAmmo());
        assertEquals(30.0, buffManager.getPeriodicAmmoTimer(), 0.01);

        // Chạy 29 giây: Chưa hồi
        buffManager.update(29.0, player, weaponManager);
        assertEquals(initialReserve, weaponManager.getAmmoInventory().getAmmo(ar.getAmmoType()));

        // Chạy thêm 1.5 giây: Vượt ngưỡng 30s -> Cấp 2 băng đạn (+80 viên đạn vì AR đã có 40 viên hoặc +60 nếu 30 viên)
        buffManager.update(1.5, player, weaponManager);
        int expectedAdd = ar.getMagazineSize() * 2;
        assertEquals(initialReserve + expectedAdd, weaponManager.getAmmoInventory().getAmmo(ar.getAmmoType()));
        assertTrue(buffManager.pollAmmoSuppliedVisual());
    }

    @Test
    @DisplayName("Kiểm tra Trạm Nâng Cấp: Trạm Cơ Bản và Trạm Đặc Biệt rút đúng danh mục buff tương ứng")
    void testUpgradeStationsSegregationAndRolling() {
        CharacterUpgradeStation basicStation = new CharacterUpgradeStation(200, 200, CharacterUpgradeStation.StationType.BASIC_STATS);
        CharacterUpgradeStation specialStation = new CharacterUpgradeStation(300, 300, CharacterUpgradeStation.StationType.SPECIAL_PERKS);

        basicStation.rollBuffs(buffManager);
        specialStation.rollBuffs(buffManager);

        assertEquals(3, basicStation.getOfferedBuffs().size(), "Trạm cơ bản phải rút đúng 3 buff");
        assertEquals(3, specialStation.getOfferedBuffs().size(), "Trạm đặc biệt phải rút đúng 3 buff");

        for (CharacterBuff b : basicStation.getOfferedBuffs()) {
            assertEquals(CharacterBuff.BuffCategory.BASIC_STAT, b.getCategory(), "Trạm cơ bản chỉ chứa buff BASIC_STAT");
        }

        for (CharacterBuff b : specialStation.getOfferedBuffs()) {
            assertEquals(CharacterBuff.BuffCategory.SPECIAL, b.getCategory(), "Trạm đặc biệt chỉ chứa buff SPECIAL");
        }
    }

    @Test
    @DisplayName("Kiểm tra Mua Buff tại trạm bằng Credits: trừ tiền chính xác và thăng cấp buff")
    void testBuffPurchaseWithCredits() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();
        state.setPlayerCredits(200);

        CharacterUpgradeStation basicStation = new CharacterUpgradeStation(100, 100, CharacterUpgradeStation.StationType.BASIC_STATS);
        basicStation.rollBuffs(state.getPlayer().getBuffManager());

        List<CharacterBuff> offered = basicStation.getOfferedBuffs();
        assertFalse(offered.isEmpty());
        CharacterBuff targetBuff = offered.get(0);
        int cost = targetBuff.getCostForLevel(0);

        boolean bought = basicStation.purchaseBuff(0, state);
        assertTrue(bought, "Mua buff phải thành công khi đủ Credits");
        assertEquals(200 - cost, state.getPlayerCredits(), "Credits phải bị khấu trừ chính xác");
        assertEquals(1, state.getPlayer().getBuffManager().getLevel(targetBuff), "Cấp độ buff phải tăng lên 1");
    }

    @Test
    @DisplayName("Kiểm tra phân tầng giá Buff theo Stage: Stage 1 ưu đãi 1-3 đồ, Stage 2 và Stage 3 tăng dần độ khó")
    void testBuffPriceProgressionByStage() {
        // 1. Kiểm tra Buff cơ bản: Máu (gốc 45), Công (gốc 50), Tốc (gốc 40)
        int hpStage1 = CharacterBuff.MAX_HEALTH.getCostForLevel(0, 1);
        int hpStage2 = CharacterBuff.MAX_HEALTH.getCostForLevel(0, 2);
        int hpStage3 = CharacterBuff.MAX_HEALTH.getCostForLevel(0, 3);

        assertTrue(hpStage1 >= 25 && hpStage1 <= 30, "Giá Máu Stage 1 phải ưu đãi trong khoảng 25-30 Credits, got: " + hpStage1);
        assertTrue(hpStage2 > hpStage1, "Giá Máu Stage 2 (" + hpStage2 + ") phải đắt hơn Stage 1 (" + hpStage1 + ")");
        assertTrue(hpStage3 > hpStage2, "Giá Máu Stage 3 (" + hpStage3 + ") phải đắt hơn Stage 2 (" + hpStage2 + ")");

        // 2. Kiểm tra Buff đặc biệt: Giáp chặn đòn (gốc 135), Diệt địch hồi máu (gốc 120)
        int barrierStage1 = CharacterBuff.ONE_HIT_BARRIER.getCostForLevel(0, 1);
        int barrierStage2 = CharacterBuff.ONE_HIT_BARRIER.getCostForLevel(0, 2);
        int barrierStage3 = CharacterBuff.ONE_HIT_BARRIER.getCostForLevel(0, 3);

        assertTrue(barrierStage1 >= 75 && barrierStage1 <= 90, "Giá Giáp 1-hit Stage 1 phải từ 75-90 Credits, got: " + barrierStage1);
        assertTrue(barrierStage2 > barrierStage1, "Giá Giáp 1-hit Stage 2 phải đắt hơn Stage 1");
        assertTrue(barrierStage3 > barrierStage2, "Giá Giáp 1-hit Stage 3 phải đắt hơn Stage 2");

        // 3. Mua buff khi chuyển sang Stage 2 phải khấu trừ đúng giá Stage 2
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();
        state.setCurrentStage(2);
        state.setPlayerCredits(300);

        CharacterUpgradeStation station = new CharacterUpgradeStation(100, 100, CharacterUpgradeStation.StationType.BASIC_STATS);
        station.rollBuffs(state.getPlayer().getBuffManager());
        CharacterBuff b = station.getOfferedBuffs().get(0);
        int expectedStage2Cost = b.getCostForLevel(0, 2);

        boolean success = station.purchaseBuff(0, state);
        assertTrue(success);
        assertEquals(300 - expectedStage2Cost, state.getPlayerCredits(), "Khấu trừ phải theo đúng giá Stage 2");
    }

    @Test
    @DisplayName("Verify Phase Dodge only phases through temporary obstacles (DESTRUCTIBLE_COVER) and protects i-frames")
    void testPhaseDodgeTemporaryObstaclesAndAntiStuckProtection() {
        DungeonMap dungeonMap = new DungeonMap();
        Player p = new Player(100, 100);

        // 1. Chuẩn bị các ô địa hình kiểm thử:
        // Ô 1 (col=10, row=10): DESTRUCTIBLE_COVER (Vật cản tạm thời)
        // Ô 2 (col=11, row=10): WALL (Tường kiên cố)
        // Ô 3 (col=12, row=10): COVER (Cột kiên cố vĩnh viễn)
        int colDestructible = 10;
        int row = 10;
        dungeonMap.getTile(9, row).setType(TileType.FLOOR);
        Tile tDestructible = dungeonMap.getTile(colDestructible, row);
        tDestructible.setType(TileType.DESTRUCTIBLE_COVER);

        Tile tWall = dungeonMap.getTile(11, row);
        tWall.setType(TileType.WALL);

        Tile tCover = dungeonMap.getTile(12, row);
        tCover.setType(TileType.COVER);

        // 2. Khi KHÔNG CÓ buff Phase Dodge: Lướt né phải BỊ CHẶN trước DESTRUCTIBLE_COVER
        p.setX(colDestructible * GameConfig.TILE_SIZE - p.getSize() - 2);
        p.setY(row * GameConfig.TILE_SIZE + 5);
        double initialX = p.getX();
        p.triggerDodge(false, false, false, true); // Né sang phải
        assertTrue(p.isDodging());
        p.handleMovement(false, false, false, true, 0.05, dungeonMap);
        assertEquals(initialX, p.getX(), 0.1, "Without Phase Dodge, player cannot pass through destructible cover");

        // 3. Mua buff PHASE_DODGE
        p.getBuffManager().applyBuff(CharacterBuff.PHASE_DODGE, p, weaponManager);
        assertTrue(p.getBuffManager().hasPhaseDodge());

        // Lướt né có Phase Dodge -> Đi XUYÊN QUA thùng gỗ DESTRUCTIBLE_COVER
        p.handleMovement(false, false, false, true, 0.05, dungeonMap);
        assertTrue(p.getX() > initialX, "With Phase Dodge, player must pass through destructible cover");

        // 4. Kiểm tra PHASE_DODGE bị chặn đứng bởi WALL và COVER
        p.setX(11 * GameConfig.TILE_SIZE - p.getSize() - 0.5); // Sát tường WALL
        double wallStartX = p.getX();
        p.handleMovement(false, false, false, true, 0.05, dungeonMap);
        assertEquals(wallStartX, p.getX(), 0.01, "Phase Dodge must NOT pass through permanent WALL");

        p.setX(12 * GameConfig.TILE_SIZE - p.getSize() - 0.5); // Sát cột COVER
        double coverStartX = p.getX();
        p.handleMovement(false, false, false, true, 0.05, dungeonMap);
        assertEquals(coverStartX, p.getX(), 0.01, "Phase Dodge must NOT pass through permanent solid COVER");

        // 5. Kiểm tra phòng ngừa hết I-frame khi đang ở giữa vật cản tạm thời:
        // Đặt người chơi nằm trọn trong ô DESTRUCTIBLE_COVER
        p.setX(colDestructible * GameConfig.TILE_SIZE + 5);
        p.setY(row * GameConfig.TILE_SIZE + 5);
        assertTrue(p.isOverlappingDestructibleCover(dungeonMap), "Player should be overlapping destructible cover");

        p.setDodgeTimer(0.01); // Sắp hết thời gian né
        p.update(0.02, dungeonMap); // dodgeTimer giảm về <= 0 nhưng đang kẹt trong thùng

        // Hệ thống phòng ngừa phải tiếp tục duy trì dodging và i-frame an toàn!
        assertTrue(p.isDodging(), "Dodging must be extended to prevent dropping i-frames inside obstacle");
        assertTrue(p.isInvulnerable(), "I-frames must remain active while inside obstacle");

        // Di chuyển người chơi ra khỏi thùng gỗ sang ô sàn trống
        p.setX(colDestructible * GameConfig.TILE_SIZE - p.getSize() - 10);
        assertFalse(p.isOverlappingDestructibleCover(dungeonMap));
        p.update(0.01, dungeonMap);

        // Vừa thoát ra ngoài: kết thúc né và nhận thêm đệm an toàn I-frame
        assertFalse(p.isDodging(), "Dodge must end once safely outside obstacle");
        assertTrue(p.getInvulnerableTimer() >= 0.20, "Grace period i-frame must be granted after exiting obstacle");

        // 6a. Kiểm tra phòng ngừa: Kẹt trong thùng gỗ nhưng bị tường kiên cố chặn đà lướt
        tDestructible.setType(TileType.DESTRUCTIBLE_COVER);
        p.setDodging(true);
        p.setDodgeDir(1.0, 0.0); // Hướng sang phải (col 11 là WALL)
        p.setX(colDestructible * GameConfig.TILE_SIZE + 5);
        p.setY(row * GameConfig.TILE_SIZE + 5);
        p.setDodgeTimer(0.0); // Đã hết thời gian né chuẩn
        p.update(0.01, dungeonMap); // Kích hoạt pha gia hạn

        // Cố di chuyển tiếp nhưng bị tường cứng WALL chặn lại
        p.handleMovement(false, false, false, false, 0.05, dungeonMap);

        // Bị tường chặn -> Cơ chế phòng ngừa phá hủy ngay lập tức thùng gỗ để giải phóng người chơi
        assertEquals(TileType.FLOOR, tDestructible.getType(),
                "Obstacle must be destroyed immediately when blocked by solid wall during phase extension");
        assertFalse(p.isDodging(), "Dodge must finish immediately when blocked");
        assertTrue(p.getInvulnerableTimer() >= 0.25, "Safety i-frame must be active after destruction");

        // 6b. Kiểm tra phòng ngừa: Kẹt trong thùng gỗ và hết thời gian gia hạn tối đa MAX_PHASE_EXTENSION (0.20s)
        tDestructible.setType(TileType.DESTRUCTIBLE_COVER);
        p.setDodging(true);
        p.setDodgeDir(0.0, 0.0); // Đứng yên
        p.setX(colDestructible * GameConfig.TILE_SIZE + 5);
        p.setY(row * GameConfig.TILE_SIZE + 5);
        p.setDodgeTimer(0.0);

        p.update(0.10, dungeonMap); // 0.10s gia hạn
        assertTrue(p.isDodging(), "Still within max extension period");
        assertEquals(TileType.DESTRUCTIBLE_COVER, tDestructible.getType());

        p.update(0.15, dungeonMap); // Tổng 0.25s > MAX_PHASE_EXTENSION (0.20s)
        assertEquals(TileType.FLOOR, tDestructible.getType(),
                "Obstacle must be automatically destroyed after grace extension timeout");
        assertFalse(p.isDodging(), "Dodge must end after timeout destruction");
        assertTrue(p.getInvulnerableTimer() >= 0.25, "Safety i-frame must be active after timeout destruction");
    }
}
