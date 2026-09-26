package fr.totetmatt.blueskygephi.atproto.response;

/**
 *
 * @author totetmatt
 */
public class ComAtprotoServerCreateSession {

    private String did;
    private String handle;
    private String email;
    private String accessJwt;
    private String refreshJwt;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccessJwt() {
        return accessJwt;
    }

    public void setAccessJwt(String accessJwt) {
        this.accessJwt = accessJwt;
    }

    public String getRefreshJwt() {
        return refreshJwt;
    }

    public void setRefreshJwt(String refreshJwt) {
        this.refreshJwt = refreshJwt;
    }

    @Override
    public String toString() {
        return "GetSessionResponse{"
                + "did='" + did + '\''
                + ", handle='" + handle + '\''
                + ", email='" + email + '\''
                + ", accessJwt='" + accessJwt + '\''
                + ", refreshJwt='" + refreshJwt + '\''
                + '}';
    }
}
