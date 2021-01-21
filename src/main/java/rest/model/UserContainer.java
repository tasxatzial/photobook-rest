package rest.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserContainer {
    Map<String, User> users = new HashMap<>();
    Set<String> emails = new HashSet<>();
    String[] userPropNames;
    Countries countries = new Countries();

    public UserContainer() throws IOException {
        userPropNames = new String[] {
                "username", "password", "passwordConfirm", "email",
                "firstName", "lastName", "birthDate", "country", "city",
                "address", "job", "gender", "interests", "about"
        };
    }

    public void addUser(User user) {
        users.put(user.getUsername(), user);
        emails.add(user.getEmail());
    }

    public void deleteUser(User user) {
        users.remove(user.getUsername());
        emails.remove(user.getEmail());
    }

    public JsonObject checkUserFields(JsonObject requestJson, String paramsUsername, boolean userUpdate) {
        JsonObject invalidRequestProps = new JsonObject();

        for (int i = 0; i < userPropNames.length; i++) {
            JsonElement requestProp = requestJson.get(userPropNames[i]);
            if (requestProp == null) {
                invalidRequestProps.addProperty(userPropNames[i], "MISSING_VALUE");
                continue;
            }
            String parsedRequestProp = parseRequestProp(requestProp);
            switch (userPropNames[i]) {
                case "username":
                    if (userUpdate) {
                        String requestUsername = parseRequestProp(requestJson.get("username")).trim().toLowerCase();
                        if (!requestUsername.equals(paramsUsername)) {
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
                        if (requestUsername.equals(paramsUsername)) {
                            String oldEmail = users.get(requestUsername).getEmail();
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
                        invalidRequestProps.addProperty(userPropNames[i], "INVALID_PATTERN");
                    }
                    break;
                case "country":
                    parsedRequestProp = parsedRequestProp.trim().toUpperCase();
                    if (!countries.containsCountry(parsedRequestProp)) {
                        invalidRequestProps.addProperty(userPropNames[i], "INVALID_CODE");
                    }
                    break;
                default:
                    if (!parsedRequestProp.matches(getRegexPattern(userPropNames[i]))) {
                        invalidRequestProps.addProperty(userPropNames[i], "INVALID_PATTERN");
                    }
                    break;
            }
        }

        return invalidRequestProps;
    }

    public static JsonArray getLinks(User user) {
        String username = user.getUsername();

        JsonObject self = new JsonObject();
        self.addProperty("rel", "self");
        self.addProperty("resource", "users/" + username);
        
        JsonObject posts = new JsonObject();
        posts.addProperty("rel", "posts");
        posts.addProperty("resource", "users/" + username + "/posts");

        JsonArray links = new JsonArray();
        links.add(self);
        links.add(posts);
        return links;
    }

    public static String getMainLink(User user) {
        return "users/" + user.getUsername();
    }

    public String parseRequestProp(JsonElement el) {
        return el.toString().substring(1, el.toString().length() - 1);
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public JsonArray getUsers() {
        JsonArray usersData = new JsonArray();
        for (Map.Entry<String, User> pair : users.entrySet()) {
            String username = pair.getKey();
            User user = pair.getValue();

            JsonObject data = new JsonObject();
            data.addProperty("username", username);
            data.add("links", getLinks(user));

            usersData.add(data);
        }

        return usersData;
    }

    public boolean usernameExists(String username) {
        return users.containsKey(username);
    }

    public boolean emailExists(String email) {
        return emails.contains(email);
    }

    /**
     * Checks whether the specified date conforms to the format yyyy-MM-dd.
     * @param date
     * @return
     */
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

    /**
     * Returns the regex pattern for the specified field request parameter.
     * @param field
     * @return
     */
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
