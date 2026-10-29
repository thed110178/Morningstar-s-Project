package com.groupproject.game.core;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Trình quản lý tập trung toàn bộ Font chữ (Typography) cho trò chơi DontKnow.
 *
 * <p>Cung cấp 2 hệ thống font chữ chuyên biệt:
 * <ul>
 *   <li><b>Primary Font (SansSerif)</b>: Font hệ thống chuẩn hiển thị rõ nét, hỗ trợ đầy đủ tiếng Việt,
 *       thích hợp cho tiêu đề lớn, banner chiến thuật (FIGHT!, CLEAR!), HUD đạn dược chính và tên vũ khí.</li>
 *   <li><b>Small Font (DeArPix)</b>: Font pixel chuyên dụng cho các thành phần UI nhỏ, phụ đề,
 *       chỉ số chi tiết, nhãn kho đạn (RIF, SMG, SG, SR), trạng thái né đòn và tooltip.</li>
 * </ul>
 *
 * <p>WHY: Hỗ trợ bộ đệm (Font Cache) hiệu năng cao, tránh cấp phát bộ nhớ mỗi frame,
 * và tích hợp bộ lọc ký tự {@link #sanitize(String)} giúp triệt tiêu hoàn toàn lỗi hiển thị
 * khối tác giả (.notdef watermark) khi gặp các ký tự Unicode đặc biệt (★, ✚, •, ⚡, ☠).
 */
public final class GameFontManager {

    private static Font primaryFont = null; // SansSerif (chữ to chuẩn nét)
    private static Font smallFont = null;   // DeArPix 1.94 (chữ nhỏ pixel)
    private static boolean initialized = false;

    // Bộ đệm lưu trữ Font theo key "style_size" để tối ưu hiệu năng kết xuất
    private static final Map<String, Font> PRIMARY_CACHE = new HashMap<>();
    private static final Map<String, Font> SMALL_CACHE = new HashMap<>();

    private GameFontManager() {
        // Utility class
    }

    /**
     * Khởi tạo và nạp toàn bộ font tùy chỉnh từ Classpath hoặc thư mục đĩa cứng.
     */
    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        // 1. Font chính (Chữ to): Quay trở lại dùng SansSerif mặc định như cũ theo yêu cầu
        // Loại bỏ hoàn toàn việc nạp SVN-Square do font này gặp nhiều vấn đề về watermark, ký tự và hiển thị
        primaryFont = new Font("SansSerif", Font.BOLD, 14);

        // 2. Nạp Font thành phần nhỏ: dearpix-1-94.ttf (giữ lại cho các nhãn bé, thông số chi tiết)
        smallFont = loadFontResource("/fonts/dearpix-1-94.ttf", "src/main/resources/fonts/dearpix-1-94.ttf", "DeArPix 1.94");
        if (smallFont != null) {
            ge.registerFont(smallFont);
        } else {
            smallFont = new Font("SansSerif", Font.PLAIN, 12);
        }
    }

    private static Font loadFontResource(String classpathRes, String diskPath, String fontLabel) {
        // Thử Classpath
        try (InputStream is = GameFontManager.class.getResourceAsStream(classpathRes)) {
            if (is != null) {
                Font f = Font.createFont(Font.TRUETYPE_FONT, is);
                System.out.println("[GameFontManager] Đã nạp thành công " + fontLabel + " từ Classpath: " + f.getName());
                return f;
            }
        } catch (Exception e) {
            System.err.println("[GameFontManager] Lỗi nạp " + fontLabel + " từ Classpath: " + e.getMessage());
        }

        // Thử Đĩa cứng
        File fDisk = new File(diskPath);
        if (fDisk.exists()) {
            try {
                Font f = Font.createFont(Font.TRUETYPE_FONT, fDisk);
                System.out.println("[GameFontManager] Đã nạp thành công " + fontLabel + " từ đĩa cứng: " + f.getName());
                return f;
            } catch (Exception e) {
                System.err.println("[GameFontManager] Lỗi nạp " + fontLabel + " từ đĩa: " + e.getMessage());
            }
        }

        System.out.println("[GameFontManager] Không tìm thấy " + fontLabel + ", chuyển sang fallback.");
        return null;
    }

    // =========================================================================
    // PRIMARY FONT (SansSerif): Dành cho Tiêu đề, Banner, Số đạn lớn, Tên súng (Khôi phục chuẩn)
    // =========================================================================

    public static Font getFont(float size) {
        return getFont(Font.PLAIN, size);
    }

    public static Font getBoldFont(float size) {
        return getFont(Font.BOLD, size);
    }

    public static Font getFont(int style, float size) {
        if (!initialized) init();

        String key = style + "_" + (int) size;
        Font cached = PRIMARY_CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        Font font = (primaryFont != null)
                ? primaryFont.deriveFont(style, size)
                : new Font("SansSerif", style, (int) size);

        PRIMARY_CACHE.put(key, font);
        return font;
    }

    // =========================================================================
    // SMALL PIXEL FONT (DeArPix): Dành cho các thành phần bé, nhãn, chi tiết, HUD phụ
    // =========================================================================

    public static Font getSmallFont(float size) {
        return getSmallFont(Font.PLAIN, size);
    }

    public static Font getSmallBoldFont(float size) {
        return getSmallFont(Font.BOLD, size);
    }

    public static Font getSmallFont(int style, float size) {
        if (!initialized) init();

        String key = style + "_" + (int) size;
        Font cached = SMALL_CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        Font font;
        if (smallFont != null) {
            font = smallFont.deriveFont(style, size);
        } else if (primaryFont != null) {
            font = primaryFont.deriveFont(style, size);
        } else {
            font = new Font("SansSerif", style, (int) size);
        }

        SMALL_CACHE.put(key, font);
        return font;
    }

    // =========================================================================
    // BỘ LỌC AN TOÀN GLYPH (.notdef Sanitation): Khử lỗi khối tác giả font
    // =========================================================================

    /**
     * Khử các ký tự Unicode đặc biệt (★, ✚, •, ⚡, ☠, v.v.) thành các ký tự ASCII chuẩn
     * nhằm triệt tiêu hoàn toàn lỗi hiển thị khối hình chữ nhật (.notdef watermark)
     * trong các font retro/pixel.
     */
    public static String sanitize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.replace('★', '*')
                   .replace('☆', '*')
                   .replace('•', '-')
                   .replace('✚', '+')
                   .replace('⚡', '!')
                   .replace('☠', 'X')
                   .replace('✓', 'v')
                   .replace('✔', 'v')
                   .replace('✖', 'x')
                   .replace('▲', '^')
                   .replace('▼', 'v');
    }

    /**
     * Vẽ chuỗi ký tự với cơ chế tự động làm sạch ký tự lạ, đảm bảo không bao giờ bị lỗi font.
     */
    public static void drawString(Graphics2D g, String text, int x, int y) {
        if (text == null || g == null) return;
        g.drawString(sanitize(text), x, y);
    }

    /**
     * Vẽ chuỗi ký tự với cơ chế tự động làm sạch ký tự lạ (tọa độ float).
     */
    public static void drawString(Graphics2D g, String text, float x, float y) {
        if (text == null || g == null) return;
        g.drawString(sanitize(text), x, y);
    }

    /**
     * Tính toán chiều rộng chuỗi text sau khi đã làm sạch ký tự.
     */
    public static int stringWidth(FontMetrics fm, String text) {
        if (fm == null || text == null) return 0;
        return fm.stringWidth(sanitize(text));
    }

    // =========================================================================
    // TRẠNG THÁI & THÔNG TIN FONT
    // =========================================================================

    public static boolean isCustomFontLoaded() {
        if (!initialized) init();
        return (smallFont != null && !"SansSerif".equalsIgnoreCase(smallFont.getFamily()))
                || (primaryFont != null && !"SansSerif".equalsIgnoreCase(primaryFont.getFamily()));
    }

    public static String getFontName() {
        if (!initialized) init();
        return (primaryFont != null) ? primaryFont.getName() : "SansSerif";
    }

    public static String getSmallFontName() {
        if (!initialized) init();
        return (smallFont != null) ? smallFont.getName() : "SansSerif";
    }
}
