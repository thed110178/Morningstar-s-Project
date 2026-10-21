package com.groupproject.game.dungeon;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.entity.player.Player;
import com.groupproject.game.ui.MinimapHUD;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động (Unit Test Suite) cho Hệ thống Bản Đồ Chiến Thuật Thu Nhỏ (Minimap / Tactical Radar)
 * và cơ chế sương mù chiến thuật (Progressive Room Discovery / Fog of War).
 */
public class MinimapSystemTest {

    private DungeonMap map;
    private MinimapHUD minimapHUD;
    private Player player;

    @BeforeEach
    void setUp() {
        map = new DungeonMap();
        minimapHUD = new MinimapHUD();
        player = new Player(100.0, 100.0);
    }

    @Test
    @DisplayName("Ban đầu Spawn Room phải được tự động đánh dấu đã khám phá, các phòng khác chưa khám phá")
    void testInitialSpawnRoomDiscovered() {
        assertNotNull(map.getSpawnRoom(), "Spawn Room không được null");
        assertTrue(map.getSpawnRoom().isDiscovered(), "Spawn Room phải được khám phá ngay khi bắt đầu nhiệm vụ");

        // Các phòng Combat ban đầu phải chưa khám phá
        for (Room cr : map.getCombatRooms()) {
            assertFalse(cr.isDiscovered(), "Combat Room " + cr.getName() + " ban đầu không được khám phá");
        }
    }

    @Test
    @DisplayName("Hành lang nối với phòng đã khám phá sẽ lập tức được hiển thị trên Minimap")
    void testCorridorsConnectedToDiscoveredRoom() {
        List<DungeonMap.RoomConnection> connections = map.getActiveConnections();
        assertFalse(connections.isEmpty(), "Bản đồ phải có các hành lang kết nối hoạt động");

        // Phải có ít nhất 1 hành lang nối trực tiếp với Spawn Room (đã khám phá)
        boolean hasConnectedToSpawn = false;
        for (DungeonMap.RoomConnection conn : connections) {
            if (conn.from == map.getSpawnRoom() || conn.to == map.getSpawnRoom()) {
                hasConnectedToSpawn = true;
                assertTrue(conn.from.isDiscovered() || conn.to.isDiscovered(),
                        "Hành lang nối với Spawn Room phải nhận diện là đã mở rộng hiển thị");
                assertNotNull(conn.getCorridorTileBounds(), "Bounding box ô tile hành lang không được null");
            }
        }
        assertTrue(hasConnectedToSpawn, "Phải có ít nhất một hành lang nối trực tiếp từ Spawn Room");
    }

    @Test
    @DisplayName("Khi người chơi di chuyển vào phòng mới, phòng đó chuyển sang đã khám phá và mở rộng hành lang")
    void testProgressiveRoomDiscoveryOnPlayerEnter() {
        Room combat1 = map.getCombatRooms().get(0);
        assertFalse(combat1.isDiscovered(), "Trước khi vào, phòng Combat 1 chưa được khám phá");

        // Mô phỏng người chơi bước vào tâm phòng Combat 1
        double cx = combat1.getCenterWorldX();
        double cy = combat1.getCenterWorldY();
        map.updateDiscovery(cx, cy);

        assertTrue(combat1.isDiscovered(), "Sau khi người chơi bước vào, phòng Combat 1 phải chuyển sang isDiscovered = true");

        // Kiểm tra tất cả các hành lang nối tới Combat 1 giờ đây đều đủ điều kiện hiển thị
        for (DungeonMap.RoomConnection conn : map.getActiveConnections()) {
            if (conn.from == combat1 || conn.to == combat1) {
                assertTrue(conn.from.isDiscovered() || conn.to.isDiscovered(),
                        "Toàn bộ hành lang thông tới Combat 1 phải được hiện lên Minimap");
            }
        }
    }

    @Test
    @DisplayName("Phòng kề cận chưa bước vào sẽ hiện dấu hỏi ?, phòng ở xa ẩn hoàn toàn trong Fog of War")
    void testAdjacentRoomDiscoveryIndicator() {
        Room spawn = map.getSpawnRoom();
        Room adjacentRoom = null;
        for (DungeonMap.RoomConnection conn : map.getActiveConnections()) {
            if (conn.from == spawn && !conn.to.isDiscovered()) {
                adjacentRoom = conn.to;
                break;
            } else if (conn.to == spawn && !conn.from.isDiscovered()) {
                adjacentRoom = conn.from;
                break;
            }
        }

        assertNotNull(adjacentRoom, "Phải tìm thấy ít nhất một phòng kề cạnh với Spawn Room");
        assertTrue(map.isRoomAdjacentToDiscovered(adjacentRoom),
                "Phòng kề cạnh phòng đã khám phá phải trả về isRoomAdjacentToDiscovered = true");
        assertFalse(adjacentRoom.isDiscovered(), "Phòng kề cạnh khi chưa bước vào vẫn là isDiscovered = false");
    }

    @Test
    @DisplayName("Stage Boss có đầy đủ kết nối hành lang và tự động khám phá Spawn Room")
    void testBossStageConnectionsAndDiscovery() {
        map.generateBossStage();
        assertNotNull(map.getSpawnRoom(), "Spawn Room trong Stage Boss không được null");
        assertTrue(map.getSpawnRoom().isDiscovered(), "Spawn Room trong Stage Boss phải được khám phá");

        List<DungeonMap.RoomConnection> connections = map.getActiveConnections();
        assertFalse(connections.isEmpty(), "Stage Boss phải có hành lang kết nối");

        // Đảm bảo có kết nối từ Spawn sang Boss Arena
        boolean hasBossConn = false;
        for (DungeonMap.RoomConnection conn : connections) {
            if ((conn.from == map.getSpawnRoom() && conn.to.getRoomType() == Room.RoomType.COMBAT)
                    || (conn.to == map.getSpawnRoom() && conn.from.getRoomType() == Room.RoomType.COMBAT)) {
                hasBossConn = true;
                break;
            }
        }
        assertTrue(hasBossConn, "Stage Boss phải có hành lang nối giữa Spawn Room và Grand Boss Arena");
    }

    @Test
    @DisplayName("Kết xuất MinimapHUD trên Graphics2D không ném bất kỳ ngoại lệ nào")
    void testMinimapHUDUpdateAndRenderNoCrash() {
        minimapHUD.update(0.016);
        minimapHUD.update(0.016);

        BufferedImage img = new BufferedImage(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        assertDoesNotThrow(() -> {
            minimapHUD.render(g, map, player, 1);
        }, "MinimapHUD render không được gây ra ngoại lệ");
        g.dispose();
    }
}
