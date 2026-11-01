package com.groupproject.game.enemy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.groupproject.game.core.Camera;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.entity.enemy.EnemyCorpse;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EnemyCorpseTest {

    @Test
    @DisplayName("Quái thường: Xác đầu lâu trượt văng và mờ dần rồi biến mất")
    void testNormalEnemyCorpseDecay() {
        DungeonMap map = new DungeonMap();
        map.generateProceduralDungeon();

        EnemyCorpse corpse = new EnemyCorpse(200.0, 200.0, 32.0, 0.0, 300.0, false);
        assertFalse(corpse.isBoss(), "Phải đánh dấu không phải Boss");
        assertFalse(corpse.isFinished(), "Xác mới sinh chưa biến mất");
        assertTrue(corpse.getAlpha() > 0.5f, "Độ mờ alpha ban đầu phải rõ nét");

        // Mô phỏng trôi qua 5.0 giây (vượt quá maxLifeTime = 4.0s)
        corpse.update(5.0, map);

        assertTrue(corpse.isFinished(), "Xác quái thường phải hoàn tất biến mất sau thời gian phân rã");
        assertEquals(0.0f, corpse.getAlpha(), 0.001f, "Alpha phải về 0 khi hết hạn");
    }

    @Test
    @DisplayName("Boss Tank: Xác xe tăng bọc thép cháy rụi tồn tại vĩnh viễn, không trôi dạt và không bao giờ biến mất")
    void testBossTankCorpsePermanent() {
        DungeonMap map = new DungeonMap();
        map.generateBossStage();

        double bossX = 400.0;
        double bossY = 300.0;
        double bossSize = 96.0;
        double chassisAngle = 1.25;

        EnemyCorpse bossCorpse = new EnemyCorpse(bossX, bossY, bossSize, chassisAngle, 0.0, true);

        assertTrue(bossCorpse.isBoss(), "Phải đánh dấu là Boss Corpse");
        assertEquals(bossSize, bossCorpse.getSize(), 0.01, "Kích thước xác Boss phải giữ nguyên kích thước thân xe");
        assertEquals(0.0, bossCorpse.getVx(), 0.001, "Xác xe tăng Boss không trượt văng theo đòn đánh");
        assertEquals(0.0, bossCorpse.getVy(), 0.001, "Xác xe tăng Boss không trượt văng theo đòn đánh");
        assertEquals(1.0f, bossCorpse.getAlpha(), 0.001f, "Alpha của xác Boss phải luôn đạt 1.0f tuyệt đối");
        assertFalse(bossCorpse.isFinished(), "Xác Boss không được phép biến mất");

        // Mô phỏng trôi qua 1000 giây trong game
        for (int i = 0; i < 100; i++) {
            bossCorpse.update(10.0, map);
        }

        assertFalse(bossCorpse.isFinished(), "Xác Boss Tank phải tồn tại mãi mãi (isFinished == false)");
        assertEquals(1.0f, bossCorpse.getAlpha(), 0.001f, "Alpha xác Boss không được mờ đi");
        assertEquals(bossX - bossSize / 2.0, bossCorpse.getX(), 0.001, "Tọa độ X không đổi");
        assertEquals(bossY - bossSize / 2.0, bossCorpse.getY(), 0.001, "Tọa độ Y không đổi");
    }

    @Test
    @DisplayName("Render xác Boss Tank không gây lỗi ngoại lệ khi vẽ trên Graphics2D")
    void testBossCorpseRenderingSafe() {
        EnemyCorpse bossCorpse = new EnemyCorpse(300.0, 300.0, 96.0, 0.5, 0.0, true);
        Camera camera = new Camera(800, 600);

        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        bossCorpse.render(g, camera);
        bossCorpse.update(0.5, new DungeonMap());
        bossCorpse.render(g, camera);

        g.dispose();
        assertFalse(bossCorpse.isFinished());
    }
}
