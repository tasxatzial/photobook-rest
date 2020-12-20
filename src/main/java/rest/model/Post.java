package rest.model;

public class Post {
    private String description;
    private String resourceURL;
    private String imageURL;
    private String imageBase64;
    private int latitude;
    private int longitude;

    public Post(String description, String resourceURL, String imageURL,
                String imageBase64, int latitude, int longitude) {
        this.description = description;
        this.resourceURL = resourceURL;
        this.imageURL = imageURL;
        this.imageBase64 = imageBase64;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getResourceURL() {
        return resourceURL;
    }

    public void setResourceURL(String resourceURL) {
        this.resourceURL = resourceURL;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }
}
