package eu.europa.ec.leos.domain.cmis.common;

import eu.europa.ec.leos.model.user.Collaborator;

import java.util.List;
import java.util.Objects;

public class SecurityData implements Securable {

    private final List<Collaborator> collaborators;

    public SecurityData(List<Collaborator> collaborators) {
        this.collaborators = collaborators;
    }

    @Override
    public List<Collaborator> getCollaborators() {
        return collaborators;
    }

    @Override
    public String toString() {
        return "SecurityData{" +
                "collaborators=" + collaborators +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityData that = (SecurityData) o;
        return Objects.equals(collaborators, that.collaborators);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collaborators);
    }
}
