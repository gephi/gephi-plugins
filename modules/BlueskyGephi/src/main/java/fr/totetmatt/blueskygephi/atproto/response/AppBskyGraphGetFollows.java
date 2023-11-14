package fr.totetmatt.blueskygephi.atproto.response;

/**
 *
 * @author totetmatt
 */
import fr.totetmatt.blueskygephi.atproto.response.common.Identity;
import java.util.List;

public class AppBskyGraphGetFollows {

    private Identity subject;

    public Identity getSubject() {
        return subject;
    }

    public void setSubject(Identity subject) {
        this.subject = subject;
    }

    public List<Identity> getFollows() {
        return follows;
    }

    public void setFollows(List<Identity> follows) {
        this.follows = follows;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    private List<Identity> follows;
    private String cursor;
}
