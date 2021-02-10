package rest.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;

public class UserPostContainer {
    private final Map<Integer, Post> _postsIDs = new HashMap<>();
    private final List<Post> _posts = new ArrayList<>();
    private final String _link;
    private final int _postsPerPage = 2;

    public UserPostContainer(User user) {
        _link = "/users/" + user.getUsername() + "/posts";
    }

    public void addPost(Post post) {
        _postsIDs.put(post.getPostID(), post);
        _posts.add(post);
        post.setUserLink(_link + "/" + post.getPostID());
    }

    public List<Post> getPostsList() {
        return Collections.unmodifiableList(_posts);
    }

    public void deletePost(Post post) {
        _postsIDs.remove(post.getPostID());
        for (int i = 0; i < _posts.size(); i++) {
            if (_posts.get(i) == post) {
                _posts.remove(i);
                break;
            }
        }
    }

    public void deletePosts() {
        _postsIDs.clear();
        _posts.clear();
    }

    public String getLink() {
        return _link;
    }

    public int getFirstPage() {
        return 1;
    }

    public int getLastPage() {
        if (_posts.size() % _postsPerPage == 0) {
            return Math.max(_posts.size() / _postsPerPage, 1);
        }
        return _posts.size() / _postsPerPage + 1;
    }

    private String getFirstPageLink() {
        return getPageLink(getFirstPage());
    }

    private String getLastPageLink() {
        return getPageLink(getLastPage());
    }

    public String getPageLink(int page) {
        return _link + "?page=" + page;
    }

    public static JsonArray getUserLinks(Post post) {
        JsonObject self = new JsonObject();
        self.addProperty("rel", "self");
        self.addProperty("resource", post.getUserLink());

        JsonArray links = new JsonArray();
        links.add(self);
        return links;
    }

    public static JsonArray getIDLink(Post post) {
        JsonObject self = new JsonObject();
        self.addProperty("rel", "self");
        self.addProperty("resource", post.getLink());

        JsonArray links = new JsonArray();
        links.add(self);
        return links;
    }

    private JsonArray getPosts(int start, int end) {
        JsonArray posts = new JsonArray();
        for (int i = start; i <= end; i++) {
            Post post = _posts.get(i);

            JsonObject postData = new JsonObject();
            postData.addProperty("postID", post.getPostID());

            JsonObject data = new JsonObject();
            data.add("links", getIDLink(post));
            data.add("data", postData);
            posts.add(data);
        }

        return posts;
    }

    public JsonArray getPosts() {
        int startPost = 0;
        int endPost = _posts.size() - 1;
        return getPosts(startPost, endPost);
    }

    public JsonArray getPage(int page) {
        int startPost = (page - 1) * _postsPerPage;
        int endPost = Math.min(startPost + _postsPerPage - 1, _posts.size() - 1);
        return getPosts(startPost, endPost);
    }

    public JsonArray getPageDefaultLinks() {
        JsonObject first = new JsonObject();
        first.addProperty("rel", "first");
        first.addProperty("resource", getFirstPageLink());

        JsonObject last = new JsonObject();
        last.addProperty("rel", "last");
        last.addProperty("resource", getLastPageLink());

        JsonArray links = new JsonArray();
        links.add(first);
        links.add(last);
        return links;
    }

    public JsonArray getPageLinks(int page) {
        JsonArray links = new JsonArray();

        JsonObject first = new JsonObject();
        int firstPage = getFirstPage();
        first.addProperty("rel", "first_page");
        first.addProperty("resource", getFirstPageLink());

        JsonObject last = new JsonObject();
        int lastPage = getLastPage();
        last.addProperty("rel", "last_page");
        last.addProperty("resource", getLastPageLink());

        JsonObject prev = new JsonObject();
        prev.addProperty("rel", "prev_page");

        JsonObject next = new JsonObject();
        next.addProperty("rel", "next_page");

        if (page <= firstPage || lastPage == 1) {
            prev.addProperty("resource", "");
        } else {
            prev.addProperty("resource", getPageLink(page - 1));
        }
        if (page >= lastPage || firstPage == lastPage) {
            next.addProperty("resource", "");
        } else {
            next.addProperty("resource", getPageLink(page + 1));
        }

        links.add(first);
        links.add(last);
        links.add(prev);
        links.add(next);
        return links;
    }
}
