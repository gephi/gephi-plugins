package util;

public class HtmlUtils {
    public static final String NEW_LINE = "<br />";
    public static final String START = "<html>";
    public static final String END = "</html>";

    public static String putInTag(String text, String tagName) {
        return "<" + tagName + ">" + text + "</" + tagName + ">";
    }

    public static String bold(String text) {
        return putInTag(text, "b");
    }

    public static String paragraphWidthWidthStart(int width) {
        return "<p width=\"" + width + "\">";
    }

    public static String paragraphEnd() {
        return "</p>";
    }

}
