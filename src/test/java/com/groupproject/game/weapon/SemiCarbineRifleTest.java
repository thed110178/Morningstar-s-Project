package com.groupproject.game.weapon;

import com.groupproject.game.entity.buff.CharacterBuff;
import com.groupproject.game.entity.buff.PlayerBuffManager;
import com.groupproject.game.entity.item.AmmoType;
import com.groupproject.game.entity.player.Player;
import com.groupproject.game.entity.projectile.Projectile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.entity.weapon.SemiCarbineRifle;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.entity.weapon.WeaponManager;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kiểm thử đơn vị cho vũ khí mới {@link SemiCarbineRifle} (SCR).
 *
 * <p>Kiểm tra toàn diện:
 * 1. Thuộc tính vũ khí cơ bản (băng 20 viên, sát thương cao 34 HP, sơ tốc 1150 px/s, đạn Rifle).
 * 2. Cơ chế bán tự động Semi-Automatic (không tự động bắn khi giữ chuột).
 * 3. Độ chính xác động: First-shot pinpoint accuracy (2 viên đầu cực chụm) và độ giật tăng mạnh khi bắn dồn dập (Spray Bloom).
 * 4. Cơ chế hồi tâm nhanh khi nhả nhịp bắn (Recoil Recovery).
 * 5. Tăng cấp và chỉ số sao (Sát thương, Tốc bắn, Chính xác).
 * 6. Tương tác với Buff Băng đạn mở rộng (tăng từ 20 lên 28 viên).
 */
public class SemiCarbineRifleTest {

    private SemiCarbineRifle scr;
    private ProjectileManager pm;

    @BeforeEach
    void setUp() {
        scr = new SemiCarbineRifle();
        pm = new ProjectileManager();
    }

    @Test
    @DisplayName("Kiểm tra các thuộc tính kỹ thuật cơ bản của Semi-Carbine Rifle")
    void testBasicAttributes() {
        assertEquals("Semi-Carbine Rifle (SCR)", scr.getName(), "Tên vũ khí phải đúng chuẩn SCR");
        assertEquals(20, scr.getMagazineSize(), "Băng đạn tiêu chuẩn phải là 20 viên");
        assertEquals(20, scr.getCurrentAmmo(), "Đạn khởi đầu phải đầy băng 20 viên");
        assertEquals(34, scr.getDamage(), "Sát thương cơ sở phải là 34 HP");
        assertEquals(AmmoType.AR, scr.getAmmoType(), "Loại đạn phải là đạn súng trường Rifle (AmmoType.AR)");
        assertEquals(1150.0, SemiCarbineRifle.BULLET_SPEED, 0.001, "Sơ tốc đạn phải đạt 1150 px/s (cao hơn AR 900 px/s)");
        assertTrue(SemiCarbineRifle.BULLET_SPEED > 900.0, "Sơ tốc đạn SCR phải vượt trội so với AR");
        assertFalse(scr.isAutomatic(), "Semi-Carbine Rifle phải là súng bán tự động (không tự sấy liên thanh)");
        assertEquals("SCR I", scr.getShortDisplayName(), "Tên hiển thị ngắn gọn cấp 1 phải là SCR I");
    }

    @Test
    @DisplayName("Kiểm tra độ chính xác viên đầu tiên cao và độ giật bloom khi bắn dồn dập")
    void testFirstShotAccuracyAndContinuousBloom() {
        double baseSpread = scr.getCurrentSpreadAngle();
        double initialSpread = scr.getInitialSpreadAngle();
        double maxSpread = scr.getMaxSpreadAngle();

        // 1. Viên đầu tiên có độ lệch cực nhỏ (10% cơ sở)
        assertEquals(baseSpread * SemiCarbineRifle.INITIAL_SPREAD_RATIO, initialSpread, 0.0001);
        assertTrue(initialSpread < baseSpread, "Độ lệch viên đầu phải nhỏ hơn rất nhiều so với độ lệch cơ sở");

        // 2. Chỉ duy nhất phát bắn đầu tiên duy trì độ chính xác tuyệt đối (Pinpoint Accuracy)
        assertEquals(initialSpread, scr.getCurrentDynamicSpreadAngle(), 0.0001);
        scr.attack(100, 100, 0.0, pm, new ArrayList<>());
        assertEquals(1, scr.getContinuousShots());
        assertEquals(initialSpread, scr.getCurrentDynamicSpreadAngle(), 0.0001);

        // 3. Ngay từ phát thứ 2 trở đi khi nhấp dồn dập, độ tản đạn tăng vọt rất dốc
        scr.update(0.20); // Xóa cooldown
        scr.attack(100, 100, 0.0, pm, new ArrayList<>());
        assertEquals(2, scr.getContinuousShots());
        assertTrue(scr.getCurrentDynamicSpreadAngle() > initialSpread, "Ngay từ phát thứ 2 độ tản đạn phải bắt đầu nở mạnh");

        // 4. Phát thứ 3 tiếp tục tăng độ lệch
        scr.update(0.20);
        scr.attack(100, 100, 0.0, pm, new ArrayList<>());
        assertEquals(3, scr.getContinuousShots());
        assertTrue(scr.getCurrentDynamicSpreadAngle() > initialSpread);

        // 5. Bắn dồn dập đến phát thứ 4 trở đi -> Đạt ngưỡng bloom tối đa gấp 8 lần
        scr.update(0.20);
        scr.attack(100, 100, 0.0, pm, new ArrayList<>());
        assertEquals(4, scr.getContinuousShots());
        assertEquals(maxSpread, scr.getCurrentDynamicSpreadAngle(), 0.0001, "Độ tản đạn đạt tối đa sau khi nhấp dồn dập 4 phát");
        assertEquals(baseSpread * SemiCarbineRifle.MAX_SPREAD_RATIO, maxSpread, 0.0001);
    }

    @Test
    @DisplayName("Kiểm tra cơ chế hồi tâm súng (Recoil Recovery) khi ngừng bắn")
    void testRecoilRecovery() {
        // Bắn 5 phát liên tiếp để tích lũy độ giật
        for (int i = 0; i < 5; i++) {
            scr.update(0.20);
            scr.attack(100, 100, 0.0, pm, new ArrayList<>());
        }
        assertEquals(5, scr.getContinuousShots());

        // Ngừng bắn trong thời gian ngắn (0.20s) chưa quá ngưỡng hồi tâm 0.45s -> chưa giảm độ giật
        scr.update(0.20);
        assertEquals(5, scr.getContinuousShots());

        // Ngừng bắn thêm 0.30s (tổng 0.50s > 0.45s) -> độ giật bắt đầu hồi phục giảm dần
        scr.update(0.30);
        assertTrue(scr.getContinuousShots() < 5, "Độ giật phải giảm dần khi ngừng bắn quá 0.45s");

        // Ngừng bắn thêm 0.30s (tổng 0.80s > 0.70s) -> độ giật reset hoàn toàn về viên đầu tiên
        scr.update(0.30);
        assertEquals(0, scr.getContinuousShots(), "Độ giật phải hồi phục hoàn toàn về 0 sau khi ngừng bắn");
        assertEquals(scr.getInitialSpreadAngle(), scr.getCurrentDynamicSpreadAngle(), 0.0001);
    }

    @Test
    @DisplayName("Kiểm tra bắn đạn tạo đúng Projectile mang sơ tốc 1150 px/s và tiêu hao đạn")
    void testAttackSpawnsProjectile() {
        boolean success = scr.attack(200, 300, 0.0, pm, new ArrayList<>());
        assertTrue(success, "Phát bắn đầu tiên phải thành công");
        assertEquals(19, scr.getCurrentAmmo(), "Đạn trong băng phải giảm từ 20 xuống 19");

        // Cập nhật ProjectileManager để tích hợp đạn từ pendingAdd
        pm.update(0.001, null);
        assertEquals(1, pm.getActiveProjectiles().size(), "Phải sinh ra đúng 1 viên đạn trong ProjectileManager");

        Projectile bullet = pm.getActiveProjectiles().get(0);
        assertNotNull(bullet);
        assertTrue(bullet.getDamage() >= 34, "Sát thương của viên đạn phải từ 34 HP trở lên");
    }

    @Test
    @DisplayName("Kiểm tra tự động nạp đạn khi bắn hết 20 viên")
    void testAutoReloadWhenEmpty() {
        for (int i = 0; i < 20; i++) {
            scr.update(0.20);
            scr.attack(100, 100, 0.0, pm, new ArrayList<>());
        }
        assertEquals(0, scr.getCurrentAmmo(), "Đạn phải về 0 sau 20 phát");

        // Phát bắn tiếp theo khi hết đạn sẽ kích hoạt nạp đạn
        scr.update(0.20);
        boolean fired = scr.attack(100, 100, 0.0, pm, new ArrayList<>());
        assertFalse(fired, "Không thể bắn khi đang bắt đầu nạp đạn");
        assertTrue(scr.isReloading(), "Súng phải ở trạng thái đang nạp đạn");
    }

    @Test
    @DisplayName("Kiểm tra nâng cấp sao sát thương, tốc bắn và độ chính xác")
    void testStarUpgrades() {
        int baseDmg = scr.getDamage();
        double baseCooldown = scr.getFireRateCooldown();
        double baseSpread = scr.getCurrentSpreadAngle();

        // 1. Tăng 1 sao sát thương (2 sao DMG, 1 sao FR, 1 sao ACC)
        scr.setStars(2, 1, 1);
        assertEquals((int) Math.round(baseDmg * 1.20), scr.getDamage(), "2 sao sát thương phải tăng +20% damage");

        // 2. Tăng 1 sao tốc bắn (+15% RPM -> cooldown / 1.15)
        scr.setStars(1, 2, 1);
        assertEquals(baseCooldown / 1.15, scr.getFireRateCooldown(), 0.0001, "2 sao tốc bắn phải giảm thời gian trễ");

        // 3. Tăng 1 sao độ chính xác (góc lệch x0.65)
        scr.setStars(1, 1, 2);
        assertEquals(baseSpread * 0.65, scr.getCurrentSpreadAngle(), 0.0001, "2 sao độ chính xác phải thu hẹp góc lệch");
        assertTrue(scr.getInitialSpreadAngle() < baseSpread * SemiCarbineRifle.INITIAL_SPREAD_RATIO,
                "Độ lệch viên đầu cũng được thu hẹp khi tăng sao chính xác");
    }

    @Test
    @DisplayName("Kiểm tra tương tác với Perk Băng đạn mở rộng (20 viên lên 28 viên)")
    void testExtendedMagazinePerk() {
        Player player = new Player(100, 100);
        WeaponManager wm = new WeaponManager();
        PlayerBuffManager bm = player.getBuffManager();
        wm.bindPlayerBuffManager(bm);

        scr.setSlotIndex(1);
        wm.addWeapon(scr);

        assertEquals(20, scr.getMagazineSize(), "Ban đầu băng đạn là 20 viên");

        bm.applyBuff(CharacterBuff.EXTENDED_MAGAZINE, player, wm);
        bm.applyExtendedMagazines(wm);

        assertEquals(28, scr.getMagazineSize(), "Sau khi áp dụng Perk, băng đạn SCR phải tăng lên 28 viên");
    }

    @Test
    @DisplayName("Kiểm tra vũ khí ngẫu nhiên có thể rơi ra Semi-Carbine Rifle")
    void testRandomDropCanIncludeSCR() {
        boolean foundSCR = false;
        for (int i = 0; i < 300; i++) {
            Weapon drop = Weapon.getRandomDropWeapon();
            if (drop instanceof SemiCarbineRifle) {
                foundSCR = true;
                break;
            }
        }
        assertTrue(foundSCR, "Trong 300 lần roll ngẫu nhiên phải xuất hiện SemiCarbineRifle");
    }
}
