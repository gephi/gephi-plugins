package fr.totetmatt.blueskygephi.atproto.response.common;

/**
 *
 * @author totetmatt
 */
public class Identity {

    private String did;
    private String handle;
    private String description;

    private String avatar;
    private String indexedAt;

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getIndexedAt() {
        return indexedAt;
    }

    public void setIndexedAt(String indexedAt) {
        this.indexedAt = indexedAt;
    }

    @Override
    public String toString() {
        return "Identity{"
                + "did='" + did + '\''
                + ", handle='" + handle + '\''
                + ", description='" + description + '\''
                + ", avatar='" + avatar + '\''
                + ", indexedAt='" + indexedAt + '\''
                + '}';
    }
}
