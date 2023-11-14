package fr.totetmatt.blueskygephi.atproto.response;

/**
 *
 * @author totetmatt
 */
import fr.totetmatt.blueskygephi.atproto.response.common.Identity;
import java.util.List;

public class AppBskyGraphGetFollowers {

    private Identity subject;

    public Identity getSubject() {
        return subject;
    }

    public void setSubject(Identity subject) {
        this.subject = subject;
    }

    public List<Identity> getFollowers() {
        return followers;
    }

    public void setFollowers(List<Identity> followers) {
        this.followers = followers;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    private List<Identity> followers;
    private String cursor;
}
