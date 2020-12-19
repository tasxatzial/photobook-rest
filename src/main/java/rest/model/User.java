package rest.model;

public class User {

    private String id;
    private String resource;

    public User(String desc, float price, String id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = id;
        resource = "users/" + id;
    }

    public String getResource() {
        return resource;
    }

    public String getId() {
        return id;
    }
}
