package eu.europa.ec.leos.services.request;

public class CollaboratorRequest {

    private String userId;
    private String roleName;
    private String connectedDG;

    public CollaboratorRequest() {
    }

    public CollaboratorRequest(String userId, String roleName, String connectedDG) {
        this.userId = userId;
        this.roleName = roleName;
        this.connectedDG = connectedDG;
    }

    public String getUserId() {
        return userId;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getConnectedDG() {
        return connectedDG;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setConnectedDG(String connectedDG) {
        this.connectedDG = connectedDG;
    }
}
