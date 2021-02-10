package rest.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;

public class RootContainer {
    private final PostContainer postContainer;
    private final UserContainer userContainer;

    public RootContainer() throws IOException {
        postContainer = new PostContainer();
        userContainer = new UserContainer();
    }

    public PostContainer getPostContainer() {
        return postContainer;
    }

    public UserContainer getUserContainer() {
        return userContainer;
    }

    public JsonArray getLink() {
        JsonObject posts = new JsonObject();
        posts.addProperty("rel", "posts");
        posts.addProperty("resource", postContainer.getLink());

        JsonObject users = new JsonObject();
        users.addProperty("rel", "users");
        users.addProperty("resource", userContainer.getLink());

        JsonArray links = new JsonArray();
        links.add(users);
        links.add(posts);
        return links;
    }
}
