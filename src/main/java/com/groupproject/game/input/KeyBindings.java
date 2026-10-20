package com.groupproject.game.input;

import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.Map;

/**
 * Quản lý cấu hình phím điều khiển (Keybinding System) cho toàn bộ trò chơi.
 *
 * <p>WHY: Cho phép người chơi tự do tùy biến gán phím (Rebinding) cho các hành động tác chiến:
 * bắn súng (Chuột trái), cận chiến nhanh (Chuột phải), di chuyển WASD, né đòn [SPACE],
 * tương tác/nhặt đồ [F], nạp đạn [R], đổi vũ khí nhanh [Q], mở menu tạm dừng [ESC],
 * mở balo [TAB] và các phím số chọn trang bị [1-4].
 * Tách biệt khỏi các phím gán cứng (hardcoded), hỗ trợ lưu trữ tạm thời và khôi phục mặc định.
 */
public class KeyBindings {

    // Hằng số định danh nút chuột đặc biệt phục vụ gán phím
    public static final int MOUSE_LEFT = -101;
    public static final int MOUSE_RIGHT = -102;
    public static final int MOUSE_MIDDLE = -103;

    /**
     * Danh mục các hành động tác chiến có thể tùy chỉnh phím trong trò chơi.
     */
    public enum GameAction {
        // 1. Tác chiến hỏa lực & Cận chiến
        FIRE("Tấn công / Bắn", "Attack / Fire", MOUSE_LEFT),
        QUICK_MELEE("Cận chiến nhanh", "Quick Melee", MOUSE_RIGHT),

        // 2. Di chuyển 4 hướng
        MOVE_UP("Tiến lên", "Move Up", KeyEvent.VK_W),
        MOVE_DOWN("Lùi xuống", "Move Down", KeyEvent.VK_S),
        MOVE_LEFT("Sang trái", "Move Left", KeyEvent.VK_A),
        MOVE_RIGHT("Sang phải", "Move Right", KeyEvent.VK_D),

        // 3. Kỹ năng & Tương tác
        DODGE("Né đòn / Lướt", "Dodge Roll", KeyEvent.VK_SPACE),
        INTERACT("Tương tác / Nhặt đồ", "Interact", KeyEvent.VK_F),
        RELOAD("Nạp đạn thủ công", "Reload", KeyEvent.VK_R),
        QUICK_SWAP("Đổi vũ khí nhanh", "Quick Swap", KeyEvent.VK_Q),

        // 4. Hệ thống & Balo
        PAUSE_MENU("Tạm dừng / Menu", "Pause Menu", KeyEvent.VK_ESCAPE),
        BACKPACK("Mở Balo chiến thuật", "Backpack", KeyEvent.VK_TAB),

        // 5. Chọn Slot Trang bị (Slot 1 và 2 là vị trí vũ khí do người chơi tự do trang bị trong Balo)
        SLOT_1("Vũ khí Slot 1", "Equip Slot 1", KeyEvent.VK_1),
        SLOT_2("Vũ khí Slot 2", "Equip Slot 2", KeyEvent.VK_2),
        SLOT_3("Vũ khí Slot 3 (Súng lục)", "Equip Slot 3 (Pistol)", KeyEvent.VK_3),
        SLOT_4("Vũ khí Slot 4 (Dao găm)", "Equip Slot 4 (Knife)", KeyEvent.VK_4);

        private final String displayNameVi;
        private final String displayNameEn;
        private final int defaultKeyCode;

        GameAction(String vi, String en, int defaultKey) {
            this.displayNameVi = vi;
            this.displayNameEn = en;
            this.defaultKeyCode = defaultKey;
        }

        public String getDisplayNameVi() {
            return displayNameVi;
        }

        public String getDisplayNameEn() {
            return displayNameEn;
        }

        public int getDefaultKeyCode() {
            return defaultKeyCode;
        }
    }

    private static final Map<GameAction, Integer> bindings = new EnumMap<>(GameAction.class);

    static {
        resetToDefaults();
    }

    /**
     * Khôi phục toàn bộ bảng gán phím về trạng thái mặc định ban đầu.
     */
    public static void resetToDefaults() {
        for (GameAction action : GameAction.values()) {
            bindings.put(action, action.getDefaultKeyCode());
        }
    }

    /**
     * Lấy mã phím (keyCode) đang được gán cho hành động tương ứng.
     */
    public static int getKeyCode(GameAction action) {
        if (action == null) return -1;
        return bindings.getOrDefault(action, action.getDefaultKeyCode());
    }

    /**
     * Thiết lập mã phím mới cho hành động chỉ định (hỗ trợ cả phím bàn phím và nút chuột).
     */
    public static void setKeyCode(GameAction action, int keyCode) {
        if (action != null && keyCode != 0) {
            bindings.put(action, keyCode);
        }
    }

    /**
     * Kiểm tra xem phím của hành động này có đang bị trùng với hành động khác không.
     *
     * @param targetAction Hành động đang muốn gán phím mới.
     * @param newKeyCode   Mã phím mới dự kiến gán.
     * @return GameAction bị trùng, hoặc null nếu không trùng phím nào.
     */
    public static GameAction findConflict(GameAction targetAction, int newKeyCode) {
        for (Map.Entry<GameAction, Integer> entry : bindings.entrySet()) {
            if (entry.getKey() != targetAction && entry.getValue() == newKeyCode) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Kiểm tra trạng thái đang giữ phím (Continuous held down) cho một hành động,
     * hỗ trợ đồng thời cả bàn phím và chuột.
     */
    public static boolean isActionDown(KeyboardHandler keyH, MouseHandler mouseH, GameAction action) {
        if (action == null) return false;
        int code = getKeyCode(action);

        if (code == MOUSE_LEFT) {
            return mouseH != null && mouseH.isLeftPressed();
        }
        if (code == MOUSE_RIGHT) {
            return mouseH != null && mouseH.isRightPressed();
        }

        return isActionDown(keyH, action);
    }

    /**
     * Kiểm tra trạng thái nhấn phím 1 lần (Single stroke press) cho một hành động,
     * hỗ trợ đồng thời cả bàn phím và chuột.
     */
    public static boolean isActionPressedOnce(KeyboardHandler keyH, MouseHandler mouseH, GameAction action) {
        if (action == null) return false;
        int code = getKeyCode(action);

        if (code == MOUSE_LEFT) {
            return mouseH != null && mouseH.isLeftClickedOnce();
        }
        if (code == MOUSE_RIGHT) {
            return mouseH != null && mouseH.isRightClickedOnce();
        }

        return isActionPressedOnce(keyH, action);
    }

    /**
     * Kiểm tra trạng thái đang giữ phím (Continuous held down) cho một hành động từ bàn phím.
     * Hỗ trợ kèm các phím phụ mở rộng tự nhiên (ví dụ phím mũi tên song song với WASD).
     */
    public static boolean isActionDown(KeyboardHandler keyH, GameAction action) {
        if (keyH == null || action == null) return false;
        int code = getKeyCode(action);
        boolean isDown = keyH.isKeyDown(code);

        // Hỗ trợ phím mũi tên phụ cho di chuyển
        if (!isDown) {
            if (action == GameAction.MOVE_UP && code == KeyEvent.VK_W) {
                isDown = keyH.isKeyDown(KeyEvent.VK_UP);
            } else if (action == GameAction.MOVE_DOWN && code == KeyEvent.VK_S) {
                isDown = keyH.isKeyDown(KeyEvent.VK_DOWN);
            } else if (action == GameAction.MOVE_LEFT && code == KeyEvent.VK_A) {
                isDown = keyH.isKeyDown(KeyEvent.VK_LEFT);
            } else if (action == GameAction.MOVE_RIGHT && code == KeyEvent.VK_D) {
                isDown = keyH.isKeyDown(KeyEvent.VK_RIGHT);
            } else if (action == GameAction.BACKPACK && code == KeyEvent.VK_TAB) {
                isDown = keyH.isKeyDown(KeyEvent.VK_B);
            }
        }
        return isDown;
    }

    /**
     * Kiểm tra trạng thái nhấn phím 1 lần (Single stroke press) cho một hành động từ bàn phím.
     */
    public static boolean isActionPressedOnce(KeyboardHandler keyH, GameAction action) {
        if (keyH == null || action == null) return false;
        int code = getKeyCode(action);
        boolean pressed = keyH.isKeyPressedOnce(code);

        if (!pressed) {
            if (action == GameAction.SLOT_1 && code == KeyEvent.VK_1) {
                pressed = keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD1);
            } else if (action == GameAction.SLOT_2 && code == KeyEvent.VK_2) {
                pressed = keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD2);
            } else if (action == GameAction.SLOT_3 && code == KeyEvent.VK_3) {
                pressed = keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD3);
            } else if (action == GameAction.SLOT_4 && code == KeyEvent.VK_4) {
                pressed = keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD4);
            } else if (action == GameAction.BACKPACK && code == KeyEvent.VK_TAB) {
                pressed = keyH.isKeyPressedOnce(KeyEvent.VK_B);
            }
        }
        return pressed;
    }

    /**
     * Chuyển đổi mã phím hoặc nút chuột thành chuỗi ký tự hiển thị thân thiện trên UI.
     */
    public static String getKeyName(int keyCode) {
        if (keyCode == MOUSE_LEFT) return "CHUỘT TRÁI";
        if (keyCode == MOUSE_RIGHT) return "CHUỘT PHẢI";
        if (keyCode == MOUSE_MIDDLE) return "CHUỘT GIỮA";
        if (keyCode == KeyEvent.VK_SPACE) return "SPACE";
        if (keyCode == KeyEvent.VK_ESCAPE) return "ESC";
        if (keyCode == KeyEvent.VK_ENTER) return "ENTER";
        if (keyCode == KeyEvent.VK_TAB) return "TAB";
        if (keyCode == KeyEvent.VK_CONTROL) return "CTRL";
        if (keyCode == KeyEvent.VK_SHIFT) return "SHIFT";
        if (keyCode == KeyEvent.VK_ALT) return "ALT";
        if (keyCode == KeyEvent.VK_UP) return "UP ARROW";
        if (keyCode == KeyEvent.VK_DOWN) return "DOWN ARROW";
        if (keyCode == KeyEvent.VK_LEFT) return "LEFT ARROW";
        if (keyCode == KeyEvent.VK_RIGHT) return "RIGHT ARROW";

        String text = KeyEvent.getKeyText(keyCode);
        if (text == null || text.isEmpty()) {
            return "KEY_" + keyCode;
        }
        return text.toUpperCase();
    }
}
