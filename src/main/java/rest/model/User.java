package rest.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class User {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String job;
    private String city;
    private String birthDate;
    private String country;
    private String interests;
    private String about;
    private String address;
    private Gender gender;
    private String link;
    private UserPostContainer postsContainer;

    public User(String username, String email, String password,
                String firstName, String lastName, String job, String city, String birthDate,
                String country, String interests, String about, String address, Gender gender) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.job = job;
        this.city = city;
        this.birthDate = birthDate;
        this.country = country;
        this.interests = interests;
        this.about = about;
        this.address = address;
        this.gender = gender;
    }

    public JsonArray getLinks() {
        JsonArray links = new JsonArray();

        JsonObject posts = new JsonObject();
        posts.addProperty("rel", "posts");
        posts.addProperty("resource", postsContainer.getPostsLink());

        links.add(posts);
        return links;
    }

    public JsonObject getData() {
        JsonObject data = new JsonObject();
        data.addProperty("username", username);
        data.addProperty("email", email);
        data.addProperty("password", password);
        data.addProperty("firstName", firstName);
        data.addProperty("lastName", lastName);
        data.addProperty("job", job);
        data.addProperty("city", city);
        data.addProperty("birthDate", birthDate);
        data.addProperty("country", country);
        data.addProperty("interests", interests);
        data.addProperty("about", about);
        data.addProperty("address", address);
        data.addProperty("gender", String.valueOf(gender));
        return data;
    }

    public void init() {
        postsContainer = new UserPostContainer(this);
    }

    public String getPostsLink() {
        return postsContainer.getPostsLink();
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public enum Gender {
        M, F, U
    }
}
