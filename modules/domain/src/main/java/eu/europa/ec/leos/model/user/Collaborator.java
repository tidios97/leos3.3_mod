package eu.europa.ec.leos.model.user;

import java.util.Objects;

public class Collaborator {

    private String login;

    private String entity;

    private String role;

    public Collaborator(String login, String role, String entity) {
        this.login = login;
        this.role = role;
        this.entity = entity;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Collaborator that = (Collaborator) o;
        return Objects.equals(login, that.login) &&
                Objects.equals(entity, that.entity) &&
                Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, entity, role);
    }
}
