package rest.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserContainer {
    private final Map<String, User> _usernames = new HashMap<>();
    private final List<User> _users = new ArrayList<>();
    private final Set<String> _emails = new HashSet<>();
    private static final String[] _userProps;
    private final Countries _countries = new Countries();
    private final int _usersPerPage = 2;
    private String _link;

    static {
        _userProps = new String[] {
                "username", "password", "passwordConfirm", "email",
                "firstName", "lastName", "birthDate", "country", "city",
                "address", "job", "gender", "interests", "about"
        };
    }

    public UserContainer() throws IOException {
        _link = "/users";
    }

    public void addUser(User user) {
        user.setLink(_link + "/" + user.getUsername());
        user.init();
        _usernames.put(user.getUsername(), user);
        _users.add(user);
        _emails.add(user.getEmail());
    }

    public void deleteUser(User user) {
        String username = user.getUsername();
        _usernames.remove(username);
        _emails.remove(user.getEmail());
        for (int i = 0; i < _users.size(); i++) {
            if (_users.get(i).getUsername().equals(username)) {
                _users.remove(i);
                break;
            }
        }
    }

    public User getUser(String username) {
        return _usernames.get(username);
    }

    public JsonObject checkUserFields(JsonObject requestJson, String usernameParameter, boolean userUpdate) {
        JsonObject invalidRequestProps = new JsonObject();

        for (int i = 0; i < _userProps.length; i++) {
            JsonElement requestProp = requestJson.get(_userProps[i]);
            if (requestProp == null) {
                invalidRequestProps.addProperty(_userProps[i], "MISSING_VALUE");
                continue;
            }
            String parsedRequestProp = parseRequestProp(requestProp);
            switch (_userProps[i]) {
                case "username":
                    if (userUpdate) {
                        String requestUsername = parseRequestProp(requestJson.get("username")).trim().toLowerCase();
                        if (!requestUsername.equals(usernameParameter)) {
                            invalidRequestProps.addProperty("username", "MODIFY_FORBIDDEN");
                            return invalidRequestProps;
                        }
                    } else {
                        parsedRequestProp = parsedRequestProp.trim().toLowerCase();
                        if (usernameExists(parsedRequestProp)) {
                            invalidRequestProps.addProperty("username", "USERNAME_EXISTS");
                        } else if (!parsedRequestProp.matches(getRegexPattern("username"))) {
                            invalidRequestProps.addProperty("username", "INVALID_PATTERN");
                        }
                    }
                    break;
                case "email":
                    parsedRequestProp = parsedRequestProp.trim().toLowerCase();
                    boolean skipEmailCheck = false;
                    if (userUpdate) {
                        String requestUsername = parseRequestProp(requestJson.get("username")).trim().toLowerCase();
                        if (requestUsername.equals(usernameParameter)) {
                            String oldEmail = _usernames.get(requestUsername).getEmail();
                            if (oldEmail.equals(parsedRequestProp)) {
                                skipEmailCheck = true;
                            }
                        }
                    }
                    if (!skipEmailCheck) {
                        if (emailExists(parsedRequestProp)) {
                            invalidRequestProps.addProperty("email", "EMAIL_EXISTS");
                        } else if (!parsedRequestProp.matches(getRegexPattern("email"))) {
                            invalidRequestProps.addProperty("email", "INVALID_PATTERN");
                        }
                    }
                    break;
                case "passwordConfirm":
                    JsonElement password = requestJson.get("password");
                    if (password == null) {
                        break;
                    }
                    String parsedPassword = parseRequestProp(password);
                    if (parsedPassword.matches(getRegexPattern("password")) && !parsedPassword.equals(parsedRequestProp)) {
                        invalidRequestProps.addProperty("passwordConfirm","PASSWORD_MISMATCH");
                    }
                    break;
                case "birthDate":
                    if (!isValidDate(parsedRequestProp)) {
                        invalidRequestProps.addProperty("birthDate", "INVALID_PATTERN");
                    }
                    break;
                case "interests":
                    parsedRequestProp = parsedRequestProp.trim();
                    if (!parsedRequestProp.matches(getRegexPattern("interests")) || parsedRequestProp.length() > 100) {
                        invalidRequestProps.addProperty("interests", "INVALID_PATTERN");
                    }
                    break;
                case "about":
                    parsedRequestProp = parsedRequestProp.trim();
                    if (!parsedRequestProp.matches(getRegexPattern("about")) || parsedRequestProp.length() > 500) {
                        invalidRequestProps.addProperty("about", "INVALID_PATTERN");
                    }
                    break;
                case "gender":
                    parsedRequestProp = parsedRequestProp.trim();
                    if (!parsedRequestProp.matches(getRegexPattern("gender"))) {
                        invalidRequestProps.addProperty(_userProps[i], "INVALID_PATTERN");
                    }
                    break;
                case "country":
                    parsedRequestProp = parsedRequestProp.trim().toUpperCase();
                    if (!_countries.containsCountry(parsedRequestProp)) {
                        invalidRequestProps.addProperty(_userProps[i], "INVALID_CODE");
                    }
                    break;
                default:
                    if (!parsedRequestProp.matches(getRegexPattern(_userProps[i]))) {
                        invalidRequestProps.addProperty(_userProps[i], "INVALID_PATTERN");
                    }
                    break;
            }
        }

        return invalidRequestProps;
    }

    public String parseRequestProp(JsonElement el) {
        return el.toString().substring(1, el.toString().length() - 1);
    }

    public int getFirstPage() {
        return 1;
    }

    public int getLastPage() {
        return _users.size() / _usersPerPage + 1;
    }

    public String getLink() {
        return _link;
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

    public static JsonArray getLinks(User user) {
        JsonObject self = new JsonObject();
        self.addProperty("rel", "self");
        self.addProperty("resource", user.getLink());

        JsonObject posts = new JsonObject();
        posts.addProperty("rel", "posts");
        posts.addProperty("resource", user.getPostsLink());

        JsonArray links = new JsonArray();
        links.add(self);
        links.add(posts);
        return links;
    }

    public static JsonObject getData(User user) {
        JsonObject data = new JsonObject();
        data.addProperty("username", user.getUsername());
        data.addProperty("email", user.getEmail());
        data.addProperty("password", user.getPassword());
        data.addProperty("firstName", user.getFirstName());
        data.addProperty("lastName", user.getLastName());
        data.addProperty("job", user.getJob());
        data.addProperty("city", user.getCity());
        data.addProperty("birthDate", user.getBirthDate());
        data.addProperty("country", user.getCountry());
        data.addProperty("interests", user.getInterests());
        data.addProperty("about", user.getAbout());
        data.addProperty("address", user.getAddress());
        data.addProperty("gender", String.valueOf(user.getGender()));
        return data;
    }

    private JsonArray getUsers(int start, int end) {
        JsonArray users = new JsonArray();
        for (int i = start; i <= end; i++) {
            User user = _users.get(i);

            JsonObject userData = new JsonObject();
            userData.addProperty("username", user.getUsername());

            JsonObject data = new JsonObject();
            data.add("links", getLinks(user));
            data.add("data", userData);
            users.add(data);
        }

        return users;
    }

    public JsonArray getUsers() {
        int startUser = 0;
        int endUser = _users.size() - 1;
        return getUsers(startUser, endUser);
    }

    public JsonArray getPage(int page) {
        int startUser = (page - 1) * _usersPerPage;
        int endUser = Math.min(startUser + _usersPerPage - 1, _users.size() - 1);
        return getUsers(startUser, endUser);
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

    public boolean usernameExists(String username) {
        return _usernames.containsKey(username);
    }

    public boolean emailExists(String email) {
        return _emails.contains(email);
    }

    private boolean isValidDate(String date) {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(date);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    private String getRegexPattern(String field) {
        switch(field) {
            case "username":
                return "^[A-Za-z]{8,25}$";
            case "password":
                return "^[\\w0-9!#$%&'*+/=?^`{|}\\[\\]_\\\\~<>., -]{8,10}$";
            case "email":
                return "^[\\w!#$%&'*+/=?^`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^`{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?$";
            case "firstName":
            case "job":
            case "lastName":
                return "^[^0-9!#$%&'*+/=?^`{|}\\[\\]_\\\\~<>.,-]{3,15}$";
            case "city":
                return "^[^!#$%&'*+/=?^`{|}\\[\\]_\\\\~<>.,]{2,20}$";
            case "address":
            case "interests":
            case "about":
                return "^(?!.*<\\/?(\\s|\\S)*?>).*$";
            case "gender":
                return "^[MFU]$";
            default:
                return null;
        }
    }
}
