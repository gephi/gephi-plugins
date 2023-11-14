package fr.totetmatt.gephi.twitter;

/**
 *
 * @author totetmatt
 */
final public class CredentialProperty {

    private String bearerToken = "";

    public CredentialProperty() {

    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String token) {
        this.bearerToken = token;
    }

}
