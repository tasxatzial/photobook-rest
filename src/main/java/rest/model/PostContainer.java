package rest.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

public class PostContainer {
    private static final String[] _postProps;
    private final List<Post> _posts = new ArrayList<>();
    private int _lastPostID;
    private final Map<Integer, Post> _postsIDs = new HashMap<>();
    private final String _link;
    private final int _postsPerPage = 2;

    static {
        _postProps = new String[] {
                "description", "resourceURL", "imageURL", "imageBase64", "latitude", "longitude"
        };
    }

    public PostContainer() {
        _link = "/posts";
    }

    public String getLink() {
        return _link;
    }

    public void addPost(Post post, User user) {
        int postID = setPostID(post);
        user.getPostsContainer().addPost(post);
        _posts.add(post);
        _postsIDs.put(postID, post);
        post.setLink(_link + "/" + post.getPostID());
    }

    public void deletePost(Post post, User user) {
        user.getPostsContainer().deletePost(post);
        _postsIDs.remove(post.getPostID());
        for (int i = 0; i < _posts.size(); i++) {
            if (_posts.get(i) == post) {
                _posts.remove(i);
                break;
            }
        }
    }

    public void deletePosts(User user) {
        List<Post> posts = user.getPostsContainer().getPostsList();
        for (int i = 0; i < posts.size(); i++) {
            _postsIDs.remove(posts.get(i).getPostID());
            for (int j = 0; j < _posts.size(); j++) {
                if (_posts.get(j) == posts.get(i)) {
                    _posts.remove(j);
                    break;
                }
            }
        }
        user.getPostsContainer().deletePosts();
    }

    public Post getPost(int postID) {
        return _postsIDs.get(postID);
    }

    private int setPostID(Post post) {
        post.setPostID(++_lastPostID);
        return _lastPostID;
    }

    public JsonObject checkPostFields(JsonObject requestJson) {
        JsonObject invalidRequestProps = new JsonObject();
        boolean validImageURL = false;
        boolean validImageBase64 = false;

        for (int i = 0; i < _postProps.length; i++) {
            JsonElement requestProp = requestJson.get(_postProps[i]);
            if (requestProp == null) {
                invalidRequestProps.addProperty(_postProps[i], "MISSING_VALUE");
                continue;
            }
            String parsedRequestProp = parseRequestProp(requestProp);
            switch (_postProps[i]) {
                case "description":
                    if (!isValidDescription(parsedRequestProp)) {
                        invalidRequestProps.addProperty("description", "INVALID_PATTERN");
                    }
                    break;
                case "resourceURL":
                    if (isValidURL(parsedRequestProp)) {
                        invalidRequestProps.addProperty("resourceURL", "INVALID_PATTERN");
                    }
                    break;
                case "imageURL":
                    validImageURL = isValidURL(parsedRequestProp);
                    break;
                case "imageBase64":
                    validImageBase64 = isValidImageBase64(parsedRequestProp);
                    break;
                case "latitude":
                    if (isValidLocation(parsedRequestProp, "latitude")) {
                        invalidRequestProps.addProperty("resourceURL", "INVALID_PATTERN");
                    }
                    break;
                case "longitude":
                    if (isValidLocation(parsedRequestProp, "longitude")) {
                        invalidRequestProps.addProperty("resourceURL", "INVALID_PATTERN");
                    }
                    break;
                default:
                    break;
            }
        }

        if (requestJson.get("imageBase64") == null && requestJson.get("imageURL") != null && !validImageURL) {
            invalidRequestProps.addProperty("imageURL", "INVALID_PATTERN");
        } else if (requestJson.get("imageURL") == null && requestJson.get("imageBase64") != null && !validImageBase64) {
            invalidRequestProps.addProperty("imageBase64", "INVALID_PATTERN");
        } else if (requestJson.get("imageBase64") != null && requestJson.get("imageURL") != null &&
                !validImageURL && !validImageBase64) {
            invalidRequestProps.addProperty("imageBase64", "INVALID_PATTERN");
            invalidRequestProps.addProperty("imageURL", "INVALID_PATTERN");
        }

        return invalidRequestProps;
    }

    private String parseRequestProp(JsonElement el) {
        return el.toString().substring(1, el.toString().length() - 1);
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

    private String getPageLink(int page) {
        return _link + "?page=" + page;
    }

    public static JsonArray getIDLink(Post post) {
        JsonObject self = new JsonObject();
        self.addProperty("rel", "self");
        self.addProperty("resource", post.getLink());

        JsonArray links = new JsonArray();
        links.add(self);
        return links;
    }

    public static JsonObject getData(Post post) {
        JsonObject data = new JsonObject();
        data.addProperty("postID", post.getPostID());
        data.addProperty("description", post.getDescription());
        data.addProperty("resourceURL", post.getResourceURL());
        data.addProperty("imageURL", post.getImageURL());
        data.addProperty("imageBase64", post.getImageBase64());
        data.addProperty("latitude", post.getLatitude());
        data.addProperty("longitude", post.getLongitude());

        return data;
    }

    public static JsonArray getLinks(Post post) {
        JsonObject self0 = new JsonObject();
        self0.addProperty("rel", "self");
        self0.addProperty("resource", post.getLink());

        JsonObject self1 = new JsonObject();
        self1.addProperty("rel", "self");
        self1.addProperty("resource", post.getUserLink());

        JsonArray links = new JsonArray();
        links.add(self0);
        links.add(self1);
        return links;
    }

    private JsonArray getPosts(int start, int end) {
        JsonArray posts = new JsonArray();
        for (int i = start; i <= end; i++) {
            Post post = _posts.get(i);

            JsonObject postData = new JsonObject();
            postData.addProperty("postID", post.getPostID());

            JsonObject data = new JsonObject();
            data.add("links", getLinks(post));
            data.add("data", postData);
            posts.add(data);
        }

        return posts;
    }

    public JsonArray getPage(int page) {
        int startPost = (page - 1) * _postsPerPage;
        int endPost = Math.min(startPost + _postsPerPage - 1, _posts.size() - 1);
        return getPosts(startPost, endPost);
    }

    public JsonArray getPosts() {
        int startPost = 0;
        int endPost = _posts.size() - 1;
        return getPosts(startPost, endPost);
    }

    public int getUserFirstPage(User user) {
        return user.getPostsContainer().getFirstPage();
    }

    public int getUserLastPage(User user) {
        return user.getPostsContainer().getLastPage();
    }

    public JsonArray getUserPageDefaultLinks(User user) {
        return user.getPostsContainer().getPageDefaultLinks();
    }

    public JsonArray getUserPageLinks(int page, User user) {
        return user.getPostsContainer().getPageLinks(page);
    }

    public JsonArray getUserPage(int page, User user) {
        return user.getPostsContainer().getPage(page);
    }

    public JsonArray getUserPosts(User user) {
        return user.getPostsContainer().getPosts();
    }

    public static JsonArray getUserLinks(Post post) {
        return UserPostContainer.getUserLinks(post);
    }

    public JsonArray getPageDefaultLinks() {
        JsonObject first = new JsonObject();
        first.addProperty("rel", "first_page");
        first.addProperty("resource", getFirstPageLink());

        JsonObject last = new JsonObject();
        last.addProperty("rel", "last_page");
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

    private boolean isValidDescription(String description) {
        return !description.trim().equals("");
    }

    private boolean isValidURL(String URL) {
        String trimmedURL = URL.trim();
        String http = "(?i)^http://.*";
        String https = "(?i)^https://.*";
        String www = "(?i)^www\\..*";
        return trimmedURL.matches(http) || trimmedURL.matches(https) || trimmedURL.matches(www);
    }

    private boolean isValidImageBase64(String image) {
        String trimmedImage = image.trim();
        if (!trimmedImage.equals("")) {
            String[] img = image.split(",");
            if (img[0].equals("data:image/jpeg;base64") || img[0].equals("data:image/png;base64")) {
                return img.length > 1 && !img[1].matches("[^A-Za-z0-9+/=]");
            }
        }
        return false;
    }

    private boolean isValidLocation(String value, String name) {
        if (value == null) {
            return false;
        }
        float f_value;
        try {
            f_value = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return false;
        }
        if (name.equals("latitude")) {
            return (f_value < 90 && f_value > -90);
        } else if (name.equals("longitude")){
            return (f_value < 180 && f_value > -180);
        } else {
            return false;
        }
    }
}
