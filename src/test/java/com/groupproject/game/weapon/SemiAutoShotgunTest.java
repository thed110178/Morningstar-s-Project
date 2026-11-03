package com.groupproject.game.weapon;

import com.groupproject.game.core.GameSoundManager;
import com.groupproject.game.entity.buff.CharacterBuff;
import com.groupproject.game.entity.buff.PlayerBuffManager;
import com.groupproject.game.entity.item.AmmoInventory;
import com.groupproject.game.entity.item.AmmoType;
import com.groupproject.game.entity.projectile.Projectile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.entity.weapon.SemiAutoShotgun;
import com.groupproject.game.entity.weapon.WeaponSpriteManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử toàn diện dành cho súng săn bán tự động Semi-auto Shotgun (SAS).
 */
public class SemiAutoShotgunTest {

    @BeforeAll
    public static void initAudio() {
        GameSoundManager.setSettingsFilePath("target/test_audio_settings_sas.properties");
        GameSoundManager.init();
        GameSoundManager.setMuted(true);
    }

    @AfterAll
    public static void tearDownAudio() {
        GameSoundManager.setMuted(false);
        GameSoundManager.setSettingsFilePath("save/audio_settings.properties");
        GameSoundManager.loadSettings();
    }

    @Test
    @DisplayName("Khởi tạo đúng các thông số mặc định của Semi-auto Shotgun")
    public void testInitializationDefaults() {
        SemiAutoShotgun sas = new SemiAutoShotgun();

        assertEquals("Semi-auto Shotgun", sas.getRawName());
        assertEquals(SemiAutoShotgun.DEFAULT_DAMAGE, sas.getDamage());
        assertEquals(12, sas.getDamage());
        assertEquals(SemiAutoShotgun.FIRE_RATE, sas.getFireRateCooldown(), 0.001);
        assertEquals(0.28, sas.getFireRateCooldown(), 0.001);
        assertEquals(SemiAutoShotgun.MAG_CAPACITY, sas.getMagazineSize());
        assertEquals(5, sas.getMagazineSize());
        assertEquals(5, sas.getCurrentAmmo());
        assertEquals(SemiAutoShotgun.RELOAD_TIME, sas.getReloadDuration(), 0.001);
        assertEquals(2.2, sas.getReloadDuration(), 0.001);
        assertEquals(SemiAutoShotgun.SPREAD_CONE_ANGLE, sas.getBaseSpreadAngle(), 0.001);
        assertEquals(0.32, sas.getBaseSpreadAngle(), 0.001);
        assertEquals(SemiAutoShotgun.PELLETS_PER_SHOT, sas.getProjectilesPerShot());
        assertEquals(6, sas.getProjectilesPerShot());
        assertEquals(AmmoType.SHOTGUN, sas.getAmmoType());
        assertFalse(sas.isAutomatic(), "Semi-auto Shotgun phải là súng bán tự động (isAutomatic == false)");
        assertTrue(sas.getShortDisplayName().contains("SAS"), "Tên viết tắt hiển thị HUD phải chứa SAS");
    }

    @Test
    @DisplayName("Tái tính toán 3 chỉ số chính (Sát thương, Tốc bắn, Độ chính xác) theo cấp sao")
    public void testStatsRecalculationFromStars() {
        SemiAutoShotgun sas = new SemiAutoShotgun();

        // 1 sao cơ sở
        assertEquals(12, sas.getDamage());
        assertEquals(0.28, sas.getFireRateCooldown(), 0.001);
        assertEquals(0.32, sas.getCurrentSpreadAngle(), 0.001);

        // Nâng 2 sao sát thương (+20% damage) -> round(12 * 1.20) = 14
        sas.setStars(2, 1, 1);
        assertEquals(14, sas.getDamage());

        // Nâng 3 sao sát thương (+40% damage) -> round(12 * 1.40) = 17
        sas.setStars(3, 1, 1);
        assertEquals(17, sas.getDamage());

        // Nâng 2 sao tốc bắn (+15% RPM) -> 0.28 / 1.15 = 0.2435s
        sas.setStars(1, 2, 1);
        assertEquals(0.28 / 1.15, sas.getFireRateCooldown(), 0.001);

        // Nâng 2 sao chính xác -> 0.32 * 0.65 = 0.208 rad (gom chùm đạn)
        sas.setStars(1, 1, 2);
        assertEquals(0.32 * 0.65, sas.getCurrentSpreadAngle(), 0.001);
    }

    @Test
    @DisplayName("Kỹ năng buff Băng Đạn Mở Rộng tăng dung lượng băng đạn từ 5 lên 8 viên")
    public void testExtendedMagazineBuff() {
        SemiAutoShotgun sas = new SemiAutoShotgun();
        assertEquals(5, sas.getMagazineSize());

        PlayerBuffManager buffManager = new PlayerBuffManager();
        assertFalse(buffManager.hasExtendedMagazine());

        // Kích hoạt buff BĂNG ĐẠN MỞ RỘNG
        buffManager.applyBuff(CharacterBuff.EXTENDED_MAGAZINE, null, null);
        assertTrue(buffManager.hasExtendedMagazine());

        // Áp dụng lên vũ khí SAS
        buffManager.applyExtendedMagazineToWeapon(sas);
        assertEquals(8, sas.getMagazineSize(), "Băng đạn SAS phải mở rộng lên 8 viên");
    }

    @Test
    @DisplayName("Bắn súng sinh đúng 6 viên đạn chì, trừ đạn và kích hoạt hồi chiêu cooldown")
    public void testAttackMechanicsAndPellets() {
        SemiAutoShotgun sas = new SemiAutoShotgun();
        ProjectileManager pm = new ProjectileManager();

        boolean fired = sas.attack(200, 200, 0.0, pm, new ArrayList<>());
        assertTrue(fired, "Phát bắn đầu tiên phải thành công");
        assertEquals(4, sas.getCurrentAmmo(), "Đạn phải giảm từ 5 xuống 4");

        // Cập nhật ProjectileManager để tích hợp đạn từ pendingAdd
        pm.update(0.001, null);
        assertEquals(6, pm.getActiveProjectiles().size(), "Mỗi phát bắn phải phóng ra 6 viên đạn chì");

        // Kiểm tra đặc tính từng viên đạn chì
        for (Projectile p : pm.getActiveProjectiles()) {
            assertEquals(SemiAutoShotgun.PELLET_SPEED, p.getSpeed(), 0.01);
            assertEquals(SemiAutoShotgun.PELLET_LIFETIME, p.getLifetime(), 0.01);
            assertTrue(p.isPlayerBullet());
        }

        // Không thể bắn tiếp trong cùng frame do đang hồi chiêu (cooldown 0.28s)
        boolean spamFired = sas.attack(200, 200, 0.0, pm, new ArrayList<>());
        assertFalse(spamFired, "Không thể bắn liên tục khi cooldownTimer chưa hết");
    }

    @Test
    @DisplayName("Tiến trình nạp đạn hộp (Box Magazine) tiêu thụ đúng lượng đạn dự trữ SHOTGUN")
    public void testReloadCycleAndAmmoConsumption() {
        SemiAutoShotgun sas = new SemiAutoShotgun();
        AmmoInventory inv = new AmmoInventory();
        inv.addAmmo(AmmoType.SHOTGUN, 20);
        sas.bindAmmoInventory(inv);

        ProjectileManager pm = new ProjectileManager();
        // Bắn hết 5 viên
        for (int i = 0; i < 5; i++) {
            sas.setCooldownTimer(0.0);
            boolean shot = sas.attack(100, 100, 0.0, pm, new ArrayList<>());
            assertTrue(shot);
        }

        assertEquals(0, sas.getCurrentAmmo());
        assertTrue(sas.isReloading(), "Súng phải tự động chuyển sang trạng thái nạp đạn sau viên cuối cùng");

        // Trong lúc nạp không thể bắn
        assertFalse(sas.attack(100, 100, 0.0, pm, new ArrayList<>()));

        // Cập nhật nửa thời gian nạp đạn (1.1s / 2.2s)
        sas.update(1.1);
        assertTrue(sas.isReloading());
        assertEquals(0, sas.getCurrentAmmo());

        // Cập nhật nốt 1.1s còn lại để hoàn tất nạp đạn
        sas.update(1.2);
        assertFalse(sas.isReloading(), "Nạp đạn phải hoàn tất");
        assertEquals(5, sas.getCurrentAmmo(), "Băng đạn phải đầy 5 viên");
        assertEquals(15, inv.getAmmo(AmmoType.SHOTGUN), "Kho đạn phải bị trừ đúng 5 viên (20 - 5 = 15)");
    }

    @Test
    @DisplayName("Đăng ký Sprite vũ khí trong WeaponSpriteManager thành công")
    public void testWeaponSpriteRegistration() {
        SemiAutoShotgun sas = new SemiAutoShotgun();
        assertNotNull(WeaponSpriteManager.SPRITE_SEMI_AUTO_SHOTGUN);
        assertEquals("Semi-auto Shotgun.png", WeaponSpriteManager.SPRITE_SEMI_AUTO_SHOTGUN);

        java.awt.image.BufferedImage sprite = WeaponSpriteManager.getSprite(sas);
        assertNotNull(sprite, "Sprite của Semi-auto Shotgun phải được nạp thành công từ resources");
    }
}
