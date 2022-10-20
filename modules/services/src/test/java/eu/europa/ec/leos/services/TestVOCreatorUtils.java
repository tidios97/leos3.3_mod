package eu.europa.ec.leos.services;

import eu.europa.ec.leos.model.user.Entity;
import eu.europa.ec.leos.model.user.User;

import java.util.ArrayList;
import java.util.List;

public class TestVOCreatorUtils {

    public static User getJohnTestUser() {
        return getTestUser("John", "SMITH", "smithj", "Ext");
    }

    public static User getJaneDigitUser() {
        return getTestUser("jane", "", "jane", "DIGIT");
    }


    public static User getJaneTestUser() {
        return getTestUser("jane", "demo", "jane", "Ext");
    }

    public static User getTestUser(String firstName, String lastName, String userLogin, String organizationName) {
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(new Entity("1", "A1", organizationName));
        List<String> roles = new ArrayList<String>();
        roles.add("ADMIN");
        User user1 = new User(1l, userLogin, lastName + " " + firstName, entities, firstName +"@gmail.com", roles);
        return user1;
    }
}
