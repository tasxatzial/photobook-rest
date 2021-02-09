package rest.model;

public class Post {
    private int postID;
    private String username;
    private String description;
    private String resourceURL;
    private String imageURL;
    private String imageBase64;
    private int latitude;
    private int longitude;

    public Post(String username, int postID, String description, String resourceURL,
                String imageURL, String imageBase64, int latitude, int longitude) {
        this.username = username;
        this.postID = postID;
        this.description = description;
        this.resourceURL = resourceURL;
        this.imageURL = imageURL;
        this.imageBase64 = imageBase64;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPostID() {
        return postID;
    }

    public void setPostID(int postID) {
        this.postID = postID;
    }
}
