/*
 * Copyright 2017 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.web.model;

import eu.europa.ec.leos.model.user.Entity;
import eu.europa.ec.leos.model.user.User;

import java.util.Objects;

/*Note: this class has a natural ordering that is inconsistent with equals.*/
public class UserVO extends User implements Comparable {

    private Entity selectedEntity;

    public UserVO(User user) {
        super(user.getId(), user.getLogin(), user.getName(), user.getEntities(), user.getEmail(), user.getRoles());
    }

    public UserVO(User user, Entity selectedEntity) {
        super(user.getId(), user.getLogin(), user.getName(), user.getEntities(), user.getEmail(), user.getRoles());
        this.selectedEntity = selectedEntity;
    }

    public void setEntity(String entity){ //exposed for collaborator editing
    }

    public String getEntity() {
        if(selectedEntity != null) {
            return selectedEntity.getOrganizationName();
        } else {
            return getDefaultEntity() != null ? getDefaultEntity().getOrganizationName() : null;
        }
    }

    public Entity getSelectedEntity() {
        return selectedEntity;
    }

    public void setSelectedEntity(Entity entity) {
        this.selectedEntity = entity;
    }

    public String getNameWithOrg() {
        StringBuilder nameWithOrg = new StringBuilder(getName());
        if (getDefaultEntity() != null) {
            nameWithOrg.append(" (");
            if (this.selectedEntity != null) {
                nameWithOrg.append(this.selectedEntity.getOrganizationName());
            } else {
                nameWithOrg.append(getDefaultEntity().getOrganizationName());
            }
            nameWithOrg.append(")");
        }
        return nameWithOrg.toString();
    }

    /*Presence of  setLogin and setName is required as com.vaadin.server.JsonCodec.encodeObject() reads properties
    of beans using getter and setter methods via see om.vaadin.server.JsonCodec.MethodProperty.find(type).
    But it is not called. So keeping the implementation as blank for setLogin and setName*/
    public void setLogin(String login){ //exposed for editor extension
        throw new UnsupportedOperationException("Setting login is not supported!!!");
    }

    public void setName(String name){ //exposed for editor extension
        throw new UnsupportedOperationException("Setting name is not supported!!!");
    }

    @Override public int compareTo(Object o) {
        if (o == null) {
            return -1;
        }
        UserVO other = (UserVO) o;
        return getName().compareTo(other.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserVO)) return false;
        if (getLogin() == null) return false;
        if (getSelectedEntity() == null) return false;

        UserVO userVO = (UserVO) o;

        return getLogin().equals(userVO.getLogin()) && getSelectedEntity().equals(userVO.getSelectedEntity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLogin(), getSelectedEntity());
    }
}
