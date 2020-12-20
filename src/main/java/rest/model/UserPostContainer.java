package rest.model;

import java.util.HashMap;
import java.util.Map;

public class UserPostContainer {
    Map<Integer, Post> posts = new HashMap<>();
    String username;

    public UserPostContainer(String username) {
        this.username = username;
    }

    public Map<Integer, Post> getUserPosts() {
        return posts;
    }

    public void addPost(Post post) {
        posts.put(post.getPostID(), post);
    }
}
