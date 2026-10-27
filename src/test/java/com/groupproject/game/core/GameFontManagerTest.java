package com.groupproject.game.core;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Font;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameFontManagerTest {

    @BeforeAll
    static void setUp() {
        GameFontManager.init();
    }

    @Test
    @DisplayName("Kiểm tra GameFontManager nạp thành công font chữ tùy chỉnh hoặc fallback")
    void testGameFontManagerLoad() {
        Font f1 = GameFontManager.getFont(14.0f);
        assertNotNull(f1, "Font trả về không được null");
        assertEquals(14, f1.getSize(), "Kích thước font phải là 14");

        Font f2 = GameFontManager.getBoldFont(20.0f);
        assertNotNull(f2, "Font in đậm không được null");
        assertEquals(20, f2.getSize(), "Kích thước font phải là 20");
        assertTrue(f2.isBold(), "Font phải có kiểu BOLD");

        // Kiểm tra bộ đệm Cache trả về cùng instance
        Font f1Cached = GameFontManager.getFont(14.0f);
        assertSame(f1, f1Cached, "Bộ đệm Cache phải trả về cùng đối tượng Font để tiết kiệm bộ nhớ");
    }

    @Test
    @DisplayName("Kiểm tra thông tin tên font và trạng thái nạp")
    void testFontInformation() {
        String name = GameFontManager.getFontName();
        assertNotNull(name);
        System.out.println("Font name in test: " + name);
        assertTrue(name.toLowerCase().contains("sansserif") || name.contains("SVN-Square"),
                "Tên font chữ to phải là SansSerif hoặc fallback tương thích");

        String smallName = GameFontManager.getSmallFontName();
        assertNotNull(smallName);
        System.out.println("Small Font name in test: " + smallName);
        assertTrue(smallName.contains("DeArPix") || smallName.toLowerCase().contains("sansserif"),
                "Tên font chữ nhỏ phải là DeArPix hoặc fallback");

        // Kiểm tra bộ lọc sanitize khử ký tự lạ
        assertEquals("*", GameFontManager.sanitize("★"));
        assertEquals("- HP", GameFontManager.sanitize("• HP"));
    }
}
