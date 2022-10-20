package eu.europa.ec.leos.vo.token;

public class JsonTokenReponse {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String scope;
    private String state;

    public JsonTokenReponse(String accessToken, String tokenType, Long expiresIn, String scope, String state) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.scope = scope;
        this.state = state;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public String getScope() {
        return scope;
    }

    public String getState() {
        return state;
    }
}