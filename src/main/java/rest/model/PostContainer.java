package rest.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PostContainer {
    String[] postPropNames;
    int lastPostID;
    Map<Integer, Post> posts = new HashMap<>();
    Map<String, Set<Integer>> userPosts = new HashMap<>();

    public PostContainer() {
        postPropNames = new String[] {
                "description", "resourceURL", "imageURL", "imageBase64", "latitude", "longitude"
        };
    }

    private int generatePostID(Post post) {
        post.setPostID(++lastPostID);
        return lastPostID;
    }

    public JsonObject checkPostFields(JsonObject requestJson) {
        return null;
    }

    public String parseRequestProp(JsonElement el) {
        return el.toString().substring(1, el.toString().length() - 1);
    }

    public Post getPost(int postID) {
        return posts.get(postID);
    }

    public void addPost(Post post) {
        int postID = generatePostID(post);
        String username = post.getUsername();
        Set<Integer> postIDs = userPosts.get(username);
        if (postIDs == null) {
            postIDs = new HashSet<>();
            postIDs.add(postID);
            userPosts.put(username, postIDs);
        } else {
            postIDs.add(postID);
        }

        posts.put(post.getPostID(), post);
    }

    public static JsonArray getLinks(Post post) {
        int postID = post.getPostID();

        JsonArray resources = new JsonArray();
        resources.add("post/" + postID);
        resources.add("users/" + post.getUsername() + "/posts/" + postID);

        JsonObject self = new JsonObject();
        self.addProperty("rel", "self");
        self.add("resource", resources);

        JsonArray links = new JsonArray();
        links.add(self);
        return links;
    }

    public JsonArray getPosts() {
        JsonArray postsData = new JsonArray();
        for (Map.Entry<Integer, Post> pair : posts.entrySet()) {
            int postID = pair.getKey();
            Post post = pair.getValue();

            JsonObject data = new JsonObject();
            data.addProperty("postID", postID);
            data.add("links", getLinks(post));

            postsData.add(data);
        }

        return postsData;
    }
}
