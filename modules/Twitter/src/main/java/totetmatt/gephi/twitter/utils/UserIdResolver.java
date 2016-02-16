package totetmatt.gephi.twitter.utils;

import java.io.IOException;
import java.util.Iterator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class UserIdResolver {

    static public long resolve(String userScreenName) throws IOException {
        Document doc;
        doc = Jsoup.connect(
                "http://www.twitter.com/" + userScreenName.toLowerCase()).get();
        Elements elems = doc.getElementsByAttribute("data-user-id");
        Iterator<Element> els = elems.iterator();
        while (els.hasNext()) {
            Element el = els.next();
            if (!el.attr("data-user-id").isEmpty()) {
                return Long.parseLong(el.attr("data-user-id"));
            }
        }

        return 0;
    }
}
