package rest.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

public class PostContainer {
    private static final String[] _postPropNames;
    private final List<Post> _posts = new ArrayList<>();
    private int _lastPostID;
    private final Map<Integer, Post> _postsIDs = new HashMap<>();
    private final Map<String, List<Post>> _userPosts = new HashMap<>();
    private final int _postsPerPage = 2;

    static {
        _postPropNames = new String[] {
                "description", "resourceURL", "imageURL", "imageBase64", "latitude", "longitude"
        };
    }

    public void addPost(Post post) {
        int postID = generatePostID(post);
        String username = post.getUsername();
        List<Post> userPosts = _userPosts.get(username);
        if (userPosts == null) {
            userPosts = new ArrayList<>();
            userPosts.add(post);
            _userPosts.put(username, userPosts);
        } else {
            userPosts.add(post);
        }

        _posts.add(post);
        _postsIDs.put(postID, post);
    }

    public void deletePost(Post post) {
        String username = post.getUsername();
        List<Post> userPosts = _userPosts.get(username);
        for (int i = 0; i < userPosts.size(); i++) {
            if (userPosts.get(i) == post) {
                userPosts.remove(i);
                break;
            }
        }
        _postsIDs.remove(post.getPostID());
        for (int i = 0; i < _posts.size(); i++) {
            if (_posts.get(i) == post) {
                _posts.remove(i);
                break;
            }
        }
    }

    public Post getPost(int postID) {
        return _postsIDs.get(postID);
    }

    public void deletePosts(User user) {
        String username = user.getUsername();
        List<Post> userPosts = _userPosts.get(username);
        if (userPosts != null) {
            for (Post userPost : userPosts) {
                _postsIDs.remove(userPost.getPostID());
                for (int i = 0; i < _posts.size(); i++) {
                    if (_posts.get(i) == userPost) {
                        _posts.remove(i);
                        break;
                    }
                }
            }
            _userPosts.remove(username);
        }
    }

    private int generatePostID(Post post) {
        post.setPostID(++_lastPostID);
        return _lastPostID;
    }

    public JsonObject checkPostFields(JsonObject requestJson) {
        JsonObject invalidRequestProps = new JsonObject();
        boolean validImageURL = false;
        boolean validImageBase64 = false;

        for (int i = 0; i < _postPropNames.length; i++) {
            JsonElement requestProp = requestJson.get(_postPropNames[i]);
            if (requestProp == null) {
                invalidRequestProps.addProperty(_postPropNames[i], "MISSING_VALUE");
                continue;
            }
            String parsedRequestProp = parseRequestProp(requestProp);
            switch (_postPropNames[i]) {
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

    public int getUserFirstPage() {
        return 1;
    }

    public int getLastPage() {
        if (_posts.size() % _postsPerPage == 0) {
            return Math.max(_posts.size() / _postsPerPage, 1);
        }
        return _posts.size() / _postsPerPage + 1;
    }

    public int getUserLastPage(User user) {
        List<Post> userPosts = _userPosts.get(user.getUsername());
        if (userPosts.size() % _postsPerPage == 0) {
            return Math.max(userPosts.size() / _postsPerPage, 1);
        }
        return userPosts.size() / _postsPerPage + 1;
    }

    private String getFirstPageLink() {
        return getPageLink(getFirstPage());
    }

    private String getUserFirstPageLink(User user) {
        return getUserPageLink(getUserFirstPage(), user);
    }

    private String getLastPageLink() {
        return getPageLink(getLastPage());
    }

    private String getUserLastPageLink(User user) {
        return getUserPageLink(getUserLastPage(user), user);
    }

    private static String getPageLink(int page) {
        return "posts?page=" + page;
    }

    private static String getUserPageLink(int page, User user) {
        return getUserPostsLink(user) + "?page=" + page;
    }

    private static String getUserPostLink(Post post) {
        return "users/" + post.getUsername() + "/posts/" + post.getPostID();
    }

    private static String getPostLink(Post post) {
        return "posts/" + post.getPostID();
    }

    public static String getUserPostsLink(User user) {
        return "users/" + user.getUsername() + "/posts";
    }

    public static JsonArray getUserPostLinks(Post post) {
        JsonObject self = new JsonObject();
        self.addProperty("rel", "self");
        self.addProperty("resource", getUserPostLink(post));

        JsonArray links = new JsonArray();
        links.add(self);
        return links;
    }

    public static JsonArray getPostIDLinks(Post post) {
        JsonObject self = new JsonObject();
        self.addProperty("rel", "self");
        self.addProperty("resource", getPostLink(post));

        JsonArray links = new JsonArray();
        links.add(self);
        return links;
    }

    public static JsonArray getLinks(Post post) {
        JsonObject self0 = new JsonObject();
        self0.addProperty("rel", "self");
        self0.addProperty("resource", getPostLink(post));

        JsonObject self1 = new JsonObject();
        self1.addProperty("rel", "self");
        self1.addProperty("resource", getUserPostLink(post));

        JsonArray links = new JsonArray();
        links.add(self0);
        links.add(self1);
        return links;
    }

    public JsonArray getPosts() {
        JsonArray posts = new JsonArray();
        for (Map.Entry<Integer, Post> pair : _postsIDs.entrySet()) {
            int postID = pair.getKey();
            Post post = pair.getValue();

            JsonObject postData = new JsonObject();
            postData.addProperty("postID", postID);

            JsonObject data = new JsonObject();
            data.add("links", getLinks(post));
            data.add("data", postData);
            posts.add(data);
        }

        return posts;
    }

    /* move to user */
    public JsonArray getUserPosts(User user) {
        JsonArray postsData = new JsonArray();
        String username = user.getUsername();
        List<Post> userPosts = _userPosts.get(username);
        if (userPosts != null) {
            for (Post userPost : userPosts) {
                JsonObject postData = new JsonObject();
                postData.addProperty("postID", userPost.getPostID());

                JsonObject data = new JsonObject();
                data.add("links", getLinks(userPost));
                data.add("data", postData);
                postsData.add(data);
            }
        }

        return postsData;
    }

    public JsonArray getPage(int page) {
        int startPost = (page - 1) * _postsPerPage;
        int endPost = Math.min(startPost + _postsPerPage - 1, _posts.size() - 1);
        JsonArray posts = new JsonArray();
        for (int i = startPost; i <= endPost; i++) {
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

    /* move to posts */
    public JsonArray getUserPage(int page, User user) {
        List<Post> userPosts = _userPosts.get(user.getUsername());
        int startPost = (page - 1) * _postsPerPage;
        int endPost = Math.min(startPost + _postsPerPage - 1, userPosts.size() - 1);
        JsonArray posts = new JsonArray();
        for (int i = startPost; i <= endPost; i++) {
            Post post = userPosts.get(i);

            JsonObject postData = new JsonObject();
            postData.addProperty("postID", post.getPostID());

            JsonObject data = new JsonObject();
            data.add("links", getLinks(post));
            data.add("data", postData);
            posts.add(data);
        }

        return posts;
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

    public JsonArray getUserPageDefaultLinks(User user) {
        JsonObject first = new JsonObject();
        first.addProperty("rel", "first");
        first.addProperty("resource", getUserFirstPageLink(user));

        JsonObject last = new JsonObject();
        last.addProperty("rel", "last");
        last.addProperty("resource", getUserLastPageLink(user));

        JsonArray links = new JsonArray();
        links.add(first);
        links.add(last);
        return links;
    }

    public JsonArray getPageLinks(int page) {
        JsonArray links = new JsonArray();

        JsonObject first = new JsonObject();
        int firstPage = getFirstPage();
        first.addProperty("rel", "first");
        first.addProperty("resource", getFirstPageLink());

        JsonObject last = new JsonObject();
        int lastPage = getLastPage();
        last.addProperty("rel", "last");
        last.addProperty("resource", getLastPageLink());

        JsonObject prev = new JsonObject();
        prev.addProperty("rel", "prev");

        JsonObject next = new JsonObject();
        next.addProperty("rel", "next");

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

    public JsonArray getUserPageLinks(int page, User user) {
        JsonArray links = new JsonArray();

        JsonObject first = new JsonObject();
        int firstPage = getFirstPage();
        first.addProperty("rel", "first");
        first.addProperty("resource", getUserFirstPageLink(user));

        JsonObject last = new JsonObject();
        int lastPage = getLastPage();
        last.addProperty("rel", "last");
        last.addProperty("resource", getUserLastPageLink(user));

        JsonObject prev = new JsonObject();
        prev.addProperty("rel", "prev");

        JsonObject next = new JsonObject();
        next.addProperty("rel", "next");

        if (page <= firstPage || lastPage == 1) {
            prev.addProperty("resource", "");
        } else {
            prev.addProperty("resource", getUserPageLink(page - 1, user));
        }
        if (page >= lastPage || firstPage == lastPage) {
            next.addProperty("resource", "");
        } else {
            next.addProperty("resource", getUserPageLink(page + 1, user));
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
