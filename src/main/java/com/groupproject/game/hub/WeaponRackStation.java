package com.groupproject.game.hub;

import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.entity.weapon.WeaponSpriteManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.function.Supplier;

/**
 * Giá trưng bày vũ khí (Weapon Rack) trong The Range.
 *
 * <p>Cho phép người chơi lấy miễn phí bất kỳ loại vũ khí nào để thử nghiệm và mang vào trận.
 */
public class WeaponRackStation extends HubStation {

    private final String weaponCode;
    private final Supplier<Weapon> weaponFactory;
    private final String spriteName;

    public WeaponRackStation(double x, double y, String weaponCode, String displayName,
                             String spriteName, Supplier<Weapon> weaponFactory) {
        super(x, y, 48.0, 48.0, displayName, new Color(255, 205, 50));
        this.weaponCode = weaponCode;
        this.spriteName = spriteName;
        this.weaponFactory = weaponFactory;
    }

    @Override
    public String getActionPrompt() {
        return "Trang bị " + title + " (Free)";
    }

    @Override
    public void onInteract(HubRangeState hub) {
        Weapon newWp = weaponFactory.get();
        if (newWp == null) return;

        // Trang bị vũ khí vào Player trong Hub
        hub.equipWeaponFromRack(newWp);
    }

    @Override
    protected void renderStationContent(Graphics2D g, int sx, int sy, int halfW, int halfH, boolean playerNear) {
        // Vẽ icon vũ khí từ WeaponSpriteManager
        Image sprite = WeaponSpriteManager.getSprite(spriteName);
        if (sprite != null) {
            int iconSize = 32;
            g.drawImage(sprite, sx - iconSize / 2, sy - iconSize / 2, iconSize, iconSize, null);
        } else {
            // Vẽ vector thay thế
            g.setColor(new Color(200, 215, 230));
            g.fillRect(sx - 12, sy - 4, 24, 8);
        }
    }

    public String getWeaponCode() {
        return weaponCode;
    }
}
