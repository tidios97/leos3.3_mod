package eu.europa.ec.leos.security;

import eu.europa.ec.leos.permissions.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class LeosPermissionAuthorityMapHelper {

    @Autowired
    LeosPermissionAuthorityMap leosPermissionAuthorityMap;


    public List<Role> getCollaboratorRoles() {
        List<Role> collaboratorRoles = new ArrayList<>();
        for (Role role : leosPermissionAuthorityMap.getAllRoles()) {
            if (role.isCollaborator()) {
                collaboratorRoles.add(role);
            }
        }
        return collaboratorRoles;
    }

    public String getRoleForDocCreation() {
        for (Role role : leosPermissionAuthorityMap.getAllRoles()) {
            if (role.isCollaborator() && role.isDefaultDocCreationRole()) {
                return role.getName();
            }
        }
        return null;
    }

    public Role getRoleFromListOfRoles(String roleName) {
        for (Role role : leosPermissionAuthorityMap.getAllRoles()) {
            if (role.getName().equals(roleName)) {
                return role;
            }
        }
        return null;
    }

    public String[] getPermissionsForRoles(List<String> authorities) {

        Set<LeosPermission> leosPermission = leosPermissionAuthorityMap.getPermissions(authorities);
        Set<String> leosPermissionValues = new HashSet<>();
        if (leosPermission != null) {
            for (LeosPermission permission : leosPermission) {
                leosPermissionValues.add(permission.name());
            }
        }
        return leosPermissionValues.toArray(new String[0]);

	}

}