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
        JsonArray data = new JsonArray();
        for (Integer postID : posts.keySet()) {
            JsonArray resources = new JsonArray();
            resources.add("post/" + postID);
            resources.add("users/" + posts.get(postID).getUsername() + "/posts/" + postID);

            JsonObject self = new JsonObject();
            self.addProperty("rel", "self");
            self.add("resource", resources);

            JsonArray links = new JsonArray();
            links.add(self);

            JsonObject post = new JsonObject();
            post.addProperty("ID", postID);
            post.add("links", links);

            data.add(post);
        }

        return data;
    }
}
