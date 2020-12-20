package rest.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class UserPostContainer {
    Map<Integer, Post> posts = new HashMap<>();
    String username;

    public UserPostContainer(String username) {
        this.username = username;
    }

    public JsonArray getPosts() {
        JsonArray postsData = new JsonArray();
        for (Map.Entry<Integer, Post> pair : posts.entrySet()) {
            int postID = pair.getKey();
            Post post = pair.getValue();

            JsonArray links = new JsonArray();
            links.add(PostContainer.getResources(post));

            JsonObject data = new JsonObject();
            data.addProperty("ID", postID);
            data.add("links", links);

            postsData.add(data);
        }

        return postsData;
    }

    public void addPost(Post post) {
        posts.put(post.getPostID(), post);
    }
}
