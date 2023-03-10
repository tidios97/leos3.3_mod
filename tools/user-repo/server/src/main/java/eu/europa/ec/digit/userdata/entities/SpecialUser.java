/*
 * Copyright 2022 European Commission
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
package eu.europa.ec.digit.userdata.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@javax.persistence.Entity
@Table(name = "LEOS_SPECIAL_USER")
public class SpecialUser implements Serializable {

    private static final long serialVersionUID = -242509624358432413L;

    @Id
    @Column(name = "USER_LOGIN", nullable = false)
    private String login;

    @Column(name = "USER_PER_ID", nullable = false)
    private Long perId;

    @Column(name = "USER_LASTNAME", nullable = false)
    private String lastName;

    @Column(name = "USER_FIRSTNAME", nullable = false)
    private String firstName;

    @Column(name = "USER_EMAIL", nullable = false)
    private String email;

    @JsonIgnore
    @OneToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(name = "LEOS_SPECIAL_USER_ROLE", joinColumns = @JoinColumn(name = "USER_LOGIN"), inverseJoinColumns = @JoinColumn(name = "ROLE_NAME"))
    private List<Role> roleEntities;

    @OneToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(name = "LEOS_SPECIAL_USER_ENTITY", joinColumns = @JoinColumn(name = "USER_LOGIN"), inverseJoinColumns = @JoinColumn(name = "ENTITY_ID"))
    private List<Entity> entities;

    public SpecialUser() {
    }

    public SpecialUser(String login, Long perId, String lastName, String firstName,String email) {
        this.login = login;
        this.perId = perId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.roleEntities = roleEntities;
        this.entities = entities;
    }

    public String getLogin() {
        return login;
    }

    public Long getPerId() {
        return perId;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getEmail() {
        return email;
    }

    public List<Role> getRoleEntities() {
        roleEntities.add(new Role("USER","Default USER role"));
        return roleEntities;
    }

    public List<String> getRoles() {
        roleEntities.add(new Role("USER","Default USER role"));
        return roleEntities.stream().map(r -> r.getRole())
                .collect(Collectors.toList());
    }

    public List<Entity> getEntities() {
        return entities;
    }
}