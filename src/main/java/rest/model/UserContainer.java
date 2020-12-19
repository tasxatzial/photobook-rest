package rest.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserContainer {
    Map<String, User> users = new HashMap<>();
    Set<String> emails = new HashSet<>();
    String[] userFieldNames;

    public UserContainer() {
        userFieldNames = new String[] {
                "username", "password", "passwordConfirm", "email",
                "firstName", "lastName", "birthDate", "country", "city",
                "address", "job", "gender", "interests", "about" };
    }

    public void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("null data");
        }
        users.put(user.getUsername(), user);
        emails.add(user.getEmail());
    }

    public JsonObject checkUserFields(JsonObject requestJson) {
        if (requestJson == null) {
            throw new IllegalArgumentException("null data");
        }
        JsonObject jsonInvalidFields = new JsonObject();

        for (int i = 0; i < userFieldNames.length; i++) {
            JsonElement field = requestJson.get(userFieldNames[i]);
            if (field == null) {
                jsonInvalidFields.addProperty(userFieldNames[i], "[Missing value]");
                continue;
            }
            String parsedField = parseJsonElement(field);
            switch (userFieldNames[i]) {
                case "username":
                    parsedField = parsedField.trim().toLowerCase();
                    if (usernameExists(parsedField)) {
                        jsonInvalidFields.addProperty("username", parsedField + " [Already taken]");
                    } else if (!parsedField.matches(getRegexPattern("username"))) {
                        jsonInvalidFields.addProperty("username", parsedField + " [Invalid pattern]");
                    }
                    break;
                case "email":
                    parsedField = parsedField.trim().toLowerCase();
                    if (emailExists(parsedField)) {
                        jsonInvalidFields.addProperty("email", parsedField + " [Already taken]");
                    } else if (!parsedField.matches(getRegexPattern("email"))) {
                        jsonInvalidFields.addProperty("email", parsedField + " [Invalid pattern]");
                    }
                    break;
                case "passwordConfirm":
                    JsonElement password = requestJson.get("password");
                    if (password == null) {
                        break;
                    }
                    String parsedPassword = parseJsonElement(password);
                    if (parsedPassword.matches(getRegexPattern("password")) &&  !parsedPassword.equals(parsedField)) {
                        jsonInvalidFields.addProperty("passwordConfirm", parsedField + " [Mismatch]");
                    }
                    break;
                case "birthDate":
                    if (!isValidDate(parsedField)) {
                        jsonInvalidFields.addProperty("birthDate", parsedField + " [Invalid pattern]");
                    }
                    break;
                case "interests":
                    parsedField = parsedField.trim();
                    if (!parsedField.matches(getRegexPattern("interests")) && parsedField.length() > 100) {
                        jsonInvalidFields.addProperty("interests", parsedField + " [Invalid pattern]");
                    }
                    break;
                case "about":
                    parsedField = parsedField.trim();
                    if (!parsedField.matches(getRegexPattern("about")) && parsedField.length() > 500) {
                        jsonInvalidFields.addProperty("about", parsedField + " [Invalid pattern]");
                    }
                    break;
                case "gender":
                    parsedField = parsedField.trim();
                    if (!parsedField.matches(getRegexPattern("gender"))) {
                        jsonInvalidFields.addProperty(userFieldNames[i], parsedField + " [Invalid pattern]");
                    }
                    break;
                case "country":
                    parsedField = parsedField.trim().toUpperCase();
                    if (!Countries.containsCountry(parsedField)) {
                        jsonInvalidFields.addProperty(userFieldNames[i], parsedField + " [Invalid code]");
                    }
                    break;
                default:
                    if (!parsedField.matches(getRegexPattern(userFieldNames[i]))) {
                        jsonInvalidFields.addProperty(userFieldNames[i], parsedField + " [Invalid pattern]");
                    }
                    break;
            }
        }

        return jsonInvalidFields;
    }

    public String getResource(User user) {
        if (user == null) {
            throw new IllegalArgumentException("null data");
        }
        return "users/" + user.getUsername();
    }

    public String parseJsonElement(JsonElement el) {
        return el.toString().substring(1, el.toString().length() - 1);
    }

    public JsonArray getUsers() {
        JsonArray data = new JsonArray();
        for (String username : users.keySet()) {
            JsonObject self = new JsonObject();
            self.addProperty("rel", "self");
            self.addProperty("resource", "users/" + username);

            JsonArray links = new JsonArray();
            links.add(self);

            JsonObject user = new JsonObject();
            user.addProperty("username", username);
            user.add("links", links);

            data.add(user);
        }

        return data;
    }

    public boolean usernameExists(String username) {
        return users.containsKey(username);
    }

    public boolean emailExists(String email) {
        return emails.contains(email);
    }

    public User getUser(String username) {
        return users.get(username);
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
