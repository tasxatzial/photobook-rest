package rest.model;

public class User {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String job;
    private String city;
    private String birthDate;
    private String country;
    private String interests;
    private String about;
    private String address;
    private Gender gender;
    private String link;
    private UserPostContainer postsContainer;

    public User(String username, String email, String password,
                String firstName, String lastName, String job, String city, String birthDate,
                String country, String interests, String about, String address, Gender gender) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.job = job;
        this.city = city;
        this.birthDate = birthDate;
        this.country = country;
        this.interests = interests;
        this.about = about;
        this.address = address;
        this.gender = gender;
    }

    public void init() {
        postsContainer = new UserPostContainer(this);
    }

    public UserPostContainer getPostsContainer() {
        return postsContainer;
    }

    public String getPostsLink() {
        return postsContainer.getLink();
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void updateUser(User newUser) {
        email = newUser.getEmail();
        password = newUser.getPassword();
        firstName = newUser.getFirstName();
        lastName = newUser.getLastName();
        job = newUser.getJob();
        city = newUser.getCity();
        birthDate = newUser.getBirthDate();
        country = newUser.getCountry();
        interests = newUser.getInterests();
        about = newUser.getAbout();
        address = newUser.getAddress();
        gender = newUser.getGender();
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getJob() {
        return job;
    }

    public String getCity() {
        return city;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getCountry() {
        return country;
    }

    public String getInterests() {
        return interests;
    }

    public String getAbout() {
        return about;
    }

    public String getAddress() {
        return address;
    }

    public Gender getGender() {
        return gender;
    }

    public enum Gender {
        M, F, U
    }
}
