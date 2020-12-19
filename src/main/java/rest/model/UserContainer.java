package rest.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserContainer {

    Map<String, User> map = new HashMap<>();

    public boolean addUser(User user) {
        if ((user == null)) {
            return false;
        }

        String uniqueID = UUID.randomUUID().toString();
        user.setId(uniqueID);
        map.put(user.getId(), user);
        return true;
    }

    public Map<String, User> getUsers() {
        return map;
    }

    public User getUser(String id) {
        return map.get(id);
    }

    public User editUser(User user) {
        map.put(user.getId(), user);
        return user;
    }

    public void deleteUser(String id) {
        map.remove(id);
    }

    public boolean exists(String id) {
        return map.containsKey(id);
    }
}
