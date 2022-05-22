/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.totetmatt.gephi.twitter.networklogics.utils;

/**
 *
 * @author totetmatt
 */
public class Language {

    static public Language[] ALL
            = {
                new Language("ar", "Arabic"),
                new Language("eu", "Basque"),
                new Language("bn", "Bengali"),
                new Language("bg", "Bulgarian"),
                new Language("ca", "Catalan"),
                new Language("zh-cn", "Simplified Chinese"),
                new Language("zh-tw", "Traditional Chinese"),
                new Language("hr", "Croatian"),
                new Language("da", "Danish"),
                new Language("nl", "Dutch"),
                new Language("en", "English"),
                new Language("es", "Spanish"),
                new Language("fi", "Finnish"),
                new Language("fr", "French"),
                new Language("de", "German"),
                new Language("gu", "Gujarati"),
                new Language("el", "Greek"),
                new Language("he", "Hebrew"),
                new Language("hi", "Hindi"),
                new Language("hu", "Hungarian"),
                new Language("in", "Indonesian"),
                new Language("it", "Italian"),
                new Language("ja", "Japanese"),
                new Language("kn", "Kannada"),
                new Language("ko", "Korean"),
                new Language("mr", "Marathi"),
                new Language("no", "Norwegian"),
                new Language("ur", "Ourdou"),
                new Language("fa", "Persan"),
                new Language("pl", "Polish"),
                new Language("pt", "Portuguese"),
                new Language("ro", "Romanian"),
                new Language("ru", "Russian"),
                new Language("sr", "Serbian"),
                new Language("sk", "Slovak"),
                new Language("sv", "Swedish"),
                new Language("ta", "Tamoul"),
                new Language("cs", "Czech "),
                new Language("th", "Thai"),
                new Language("tr", "Turkish"),
                new Language("uk", "Ukrainian"),
                new Language("vi", "Vietnamese")
            };

    private final String label;
    private final String code;

    public Language(String code, String label) {
        this.label = label;
        this.code = code;
    }

    @Override
    public String toString() {
        return label + " (" + code + ")";
    }

    public String getLabel() {
        return label;
    }

    public String getCode() {
        return code;
    }

}
