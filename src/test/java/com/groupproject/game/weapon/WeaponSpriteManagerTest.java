package com.groupproject.game.weapon;

import com.groupproject.game.entity.weapon.AssaultRifle;
import com.groupproject.game.entity.weapon.CombatKnife;
import com.groupproject.game.entity.weapon.Pistol;
import com.groupproject.game.entity.weapon.SMG;
import com.groupproject.game.entity.weapon.SemiCarbineRifle;
import com.groupproject.game.entity.weapon.Shotgun;
import com.groupproject.game.entity.weapon.SniperRifle;
import com.groupproject.game.entity.weapon.WeaponSpriteManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Kiểm thử đơn vị cho {@link WeaponSpriteManager}.
 *
 * <p>Đảm bảo:
 * 1. Toàn bộ 7 loại Sprite (AR, Shotgun, SMG, SR Bolt-Action, Pistol, Knife, Semi-Carbine) đều được tải thành công.
 * 2. Lấy Sprite theo đúng định danh đối tượng súng.
 * 3. Quá trình render sprite qua Graphics2D an toàn tuyệt đối, không phát sinh lỗi ngoại lệ kể cả khi súng hoặc đối tượng đồ họa có vấn đề.
 */
public class WeaponSpriteManagerTest {

    @Test
    @DisplayName("Nạp thành công 7 tệp sprite vũ khí từ thư mục tài nguyên hoặc Material")
    public void testSpritesLoadedSuccessfully() {
        assertNotNull(WeaponSpriteManager.getSprite(WeaponSpriteManager.SPRITE_AR), "Sprite AR không được null");
        assertNotNull(WeaponSpriteManager.getSprite(WeaponSpriteManager.SPRITE_SHOTGUN), "Sprite Shotgun không được null");
        assertNotNull(WeaponSpriteManager.getSprite(WeaponSpriteManager.SPRITE_SMG), "Sprite SMG không được null");
        assertNotNull(WeaponSpriteManager.getSprite(WeaponSpriteManager.SPRITE_SNIPER), "Sprite Sniper Rifle không được null");
        assertNotNull(WeaponSpriteManager.getSprite(WeaponSpriteManager.SPRITE_PISTOL), "Sprite Pistol không được null");
        assertNotNull(WeaponSpriteManager.getSprite(WeaponSpriteManager.SPRITE_KNIFE), "Sprite Knife không được null");
        assertNotNull(WeaponSpriteManager.getSprite(WeaponSpriteManager.SPRITE_SEMI_CARBINE), "Sprite SemiCarbineRifle không được null");
    }

    @Test
    @DisplayName("Lấy sprite tương ứng từ các thực thể vũ khí cụ thể")
    public void testGetSpriteByWeaponInstance() {
        AssaultRifle ar = new AssaultRifle();
        Shotgun shotgun = new Shotgun();
        SMG smg = new SMG();
        SniperRifle sniper = new SniperRifle();
        Pistol pistol = new Pistol();
        CombatKnife knife = new CombatKnife();
        SemiCarbineRifle scr = new SemiCarbineRifle();

        assertNotNull(WeaponSpriteManager.getSprite(ar), "Lấy sprite cho AssaultRifle phải thành công");
        assertNotNull(WeaponSpriteManager.getSprite(shotgun), "Lấy sprite cho Shotgun phải thành công");
        assertNotNull(WeaponSpriteManager.getSprite(smg), "Lấy sprite cho SMG phải thành công");
        assertNotNull(WeaponSpriteManager.getSprite(sniper), "Lấy sprite cho SniperRifle phải thành công");
        assertNotNull(WeaponSpriteManager.getSprite(pistol), "Lấy sprite cho Pistol phải thành công");
        assertNotNull(WeaponSpriteManager.getSprite(knife), "Lấy sprite cho CombatKnife phải thành công");
        assertNotNull(WeaponSpriteManager.getSprite(scr), "Lấy sprite cho SemiCarbineRifle phải thành công");
        assertNull(WeaponSpriteManager.getSprite((com.groupproject.game.entity.weapon.Weapon) null), "Weapon null phải trả về null");
    }

    @Test
    @DisplayName("Vẽ sprite qua Graphics2D hoạt động trơn tru không ném lỗi")
    public void testRenderWeaponSpriteDoesNotThrow() {
        BufferedImage canvas = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvas.createGraphics();

        AssaultRifle ar = new AssaultRifle();
        Shotgun shotgun = new Shotgun();
        SMG smg = new SMG();
        SniperRifle sniper = new SniperRifle();
        Pistol pistol = new Pistol();
        CombatKnife knife = new CombatKnife();
        SemiCarbineRifle scr = new SemiCarbineRifle();

        assertDoesNotThrow(() -> {
            WeaponSpriteManager.renderWeaponSprite(g2d, ar, 64, 64, 32, 32);
            WeaponSpriteManager.renderWeaponSprite(g2d, shotgun, 64, 64, 32, 32);
            WeaponSpriteManager.renderWeaponSprite(g2d, smg, 64, 64, 32, 32);
            WeaponSpriteManager.renderWeaponSprite(g2d, sniper, 64, 64, 32, 32);
            WeaponSpriteManager.renderWeaponSprite(g2d, pistol, 64, 64, 32, 32);
            WeaponSpriteManager.renderWeaponSprite(g2d, knife, 64, 64, 32, 32);
            WeaponSpriteManager.renderWeaponSprite(g2d, scr, 64, 64, 32, 32);
            WeaponSpriteManager.renderWeaponSprite(g2d, null, 64, 64, 32, 32);
            WeaponSpriteManager.renderWeaponSprite(null, ar, 64, 64, 32, 32);
        }, "Quá trình renderWeaponSprite không được ném bất kỳ ngoại lệ nào");

        g2d.dispose();
    }
}
