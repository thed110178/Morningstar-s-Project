import java.awt.Font;
import java.io.File;

public class TestSymbols {
    public static void main(String[] args) throws Exception {
        Font square = Font.createFont(Font.TRUETYPE_FONT, new File("Font/SVN-SQUARE.TTF"));
        Font dearpix = Font.createFont(Font.TRUETYPE_FONT, new File("Font/dearpix-1-94.ttf"));

        String testStr = "★*•+✚⚡☠∞✓▲▼|/[]():-";
        for (char c : testStr.toCharArray()) {
            System.out.printf("Char '%c' (\\u%04x): SVN-Square=%b, DeArPix=%b%n",
                    c, (int) c, square.canDisplay(c), dearpix.canDisplay(c));
        }
    }
}
