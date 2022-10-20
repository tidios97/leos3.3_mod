package eu.europa.ec.leos.security;

public class AuthClient {
    
    private String name;
    private String clientId;
    private String secret;
    private boolean verified;
    
    public AuthClient() {
    }
    
    public AuthClient(String clientName, String clientId, String clientSecret) {
        this.name = clientName;
        this.clientId = clientId;
        this.secret = clientSecret;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public boolean isVerified() {
        return verified;
    }
    
    public void setVerified(boolean verified) {
        this.verified = verified;
    }
    
    @Override
    public String toString() {
        return "AuthClient [name=" + name + ", clientId=" + clientId + ", verified=" + verified + "]";
    }
}
