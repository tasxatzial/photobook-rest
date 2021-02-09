package rest.model;

import java.util.ArrayList;
import java.util.List;

public class UserPostContainer {
    private List<Post> posts;
    private String link;

    public UserPostContainer(User user) {
        posts = new ArrayList<>();
        link = "users/" + user.getUsername() + "/posts";
    }

    public String getPostsLink() {
        return link;
    }
}
