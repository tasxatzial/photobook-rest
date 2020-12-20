package rest.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class PostContainer {
    Map<Integer, Post> posts = new HashMap<>();
    int lastPostID;
    String[] postPropNames;

    public PostContainer() {
        postPropNames = new String[] {
          "description", "resourceURL", "imageURL", "imageBase64", "latitude", "longitude"
        };
    }

    public void addPost(Post post) {
        posts.put(post.getPostID(), post);
    }

    private void generatePostID(Post post) {
        post.setPostID(lastPostID++);
    }

    public JsonObject checkPostFields(JsonObject requestJson) {
        return null;
    }

    public String parseRequestProp(JsonElement el) {
        return el.toString().substring(1, el.toString().length() - 1);
    }

    public JsonArray getResource(Post post) {
        return null;
    }

    public Post getPost(int postID) {
        return null;
    }

    public JsonArray getPosts() {
        return null;
    }
}
