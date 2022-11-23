package eu.europa.ec.leos.integration.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europa.ec.leos.security.LeosPermission;

import java.util.List;
import java.util.stream.Collectors;

public class AnnotatePermissionsJson {
    private final List<LeosPermission> permissions;

    public AnnotatePermissionsJson(final List<LeosPermission> permissions) {
        this.permissions = permissions;
    }

    @JsonProperty("permissions")
    public List<String> getPermissions() {
        return this.permissions.stream().map(Enum::name).collect(Collectors.toList());
    }
}
