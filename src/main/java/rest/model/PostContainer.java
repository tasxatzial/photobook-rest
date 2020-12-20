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

    public JsonObject getResources(Post post) {
        JsonArray resources = new JsonArray();
        int postID = post.getPostID();
        resources.add("post/" + postID);
        resources.add("users/" + post.getUsername() + "/posts/" + postID);

        JsonObject self = new JsonObject();
        self.addProperty("rel", "self");
        self.add("resource", resources);

        return self;
    }

    public JsonArray getPosts() {
        JsonArray postsData = new JsonArray();
        for (Map.Entry<String, UserPostContainer> _userPostContainer : posts.entrySet()) {
            String username = _userPostContainer.getKey();
            UserPostContainer userPostContainer = _userPostContainer.getValue();
            Map<Integer, Post> userPosts = userPostContainer.getUserPosts();

            for (Map.Entry<Integer, Post> _post : userPosts.entrySet()) {
                int postID = _post.getKey();
                Post post = _post.getValue();

                JsonArray links = new JsonArray();
                links.add(getResources(post));

                JsonObject data = new JsonObject();
                data.addProperty("ID", postID);
                data.addProperty("username", username);
                data.add("links", links);

                postsData.add(data);
            }
        }

        return postsData;
    }
}
