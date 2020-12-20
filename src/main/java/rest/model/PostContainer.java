package rest.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class PostContainer {
    String[] postPropNames;
    int lastPostID;
    Map<String, UserPostContainer> posts = new HashMap<>();

    public PostContainer() {
        postPropNames = new String[] {
                "description", "resourceURL", "imageURL", "imageBase64", "latitude", "longitude"
        };
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

    public void addPost(Post post) {
        UserPostContainer userPostContainer = posts.get(post.getUsername());
        generatePostID(post);
        userPostContainer.addPost(post);
    }

    public static JsonObject getResources(Post post) {
        int postID = post.getPostID();

        JsonArray resources = new JsonArray();
        resources.add("post/" + postID);
        resources.add("users/" + post.getUsername() + "/posts/" + postID);

        JsonObject self = new JsonObject();
        self.addProperty("rel", "self");
        self.add("resource", resources);

        return self;
    }

    public JsonArray getPosts() {
        JsonArray postsData = new JsonArray();
        for (Map.Entry<String, UserPostContainer> pair : posts.entrySet()) {
            UserPostContainer userPostContainer = pair.getValue();
            JsonArray userPosts = userPostContainer.getPosts();
            for (int i = 0; i < userPosts.size(); i++) {
                postsData.add(userPosts.get(i));
            }
        }

        return postsData;
    }
}
