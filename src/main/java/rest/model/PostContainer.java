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
    Map<String, Set<Integer>> usernamePosts = new HashMap<>();

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
        JsonObject invalidRequestProps = new JsonObject();
        boolean validImageURL = false;
        boolean validImageBase64 = false;

        for (int i = 0; i < postPropNames.length; i++) {
            JsonElement requestProp = requestJson.get(postPropNames[i]);
            if (requestProp == null) {
                invalidRequestProps.addProperty(postPropNames[i], "MISSING_VALUE");
                continue;
            }
            String parsedRequestProp = parseRequestProp(requestProp);
            switch (postPropNames[i]) {
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

    private String parseRequestProp(JsonElement el) {
        return el.toString().substring(1, el.toString().length() - 1);
    }

    public Post getPost(int postID) {
        return posts.get(postID);
    }

    public void deletePost(Post post) {
        String username = post.getUsername();
        int postID = post.getPostID();
        Set<Integer> postIDs = usernamePosts.get(username);
        postIDs.remove(postID);
        posts.remove(postID);
    }

    public void addPost(Post post) {
        int postID = generatePostID(post);
        String username = post.getUsername();
        Set<Integer> postIDs = usernamePosts.get(username);
        if (postIDs == null) {
            postIDs = new HashSet<>();
            postIDs.add(postID);
            usernamePosts.put(username, postIDs);
        } else {
            postIDs.add(postID);
        }

        posts.put(post.getPostID(), post);
    }

    public static JsonArray getLinks(Post post) {
        int postID = post.getPostID();

        JsonObject self0 = new JsonObject();
        self0.addProperty("rel", "self");
        self0.addProperty("resource", "posts/" + postID);

        JsonObject self1 = new JsonObject();
        self1.addProperty("rel", "self");
        self1.addProperty("resource", "users/" + post.getUsername() + "/posts/" + postID);

        JsonArray links = new JsonArray();
        links.add(self0);
        links.add(self1);
        return links;
    }

    public static String getMainLink(Post post) {
        return "users/" + post.getUsername() + "/posts/" + post.getPostID();
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

    public JsonArray getUserPosts(User user) {
        JsonArray postsData = new JsonArray();
        String username = user.getUsername();
        Set<Integer> postIDs = usernamePosts.get(username);
        if (postIDs != null) {
            for (Integer postID : postIDs) {
                Post post = posts.get(postID);

                JsonObject data = new JsonObject();
                data.addProperty("postID", postID);
                data.add("links", getLinks(post));

                postsData.add(data);
            }
        }

        return postsData;
    }

    public void deleteUserPosts(User user) {
        String username = user.getUsername();
        Set<Integer> postIDs = usernamePosts.get(username);
        if (postIDs != null) {
            for (Integer postID : postIDs) {
                posts.remove(postID);
            }
            usernamePosts.remove(username);
        }
    }
}
