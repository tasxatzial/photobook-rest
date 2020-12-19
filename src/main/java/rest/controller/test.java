package rest.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import rest.model.User;
import rest.model.UserContainer;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;

public class test {
    private static final UserContainer userContainer = new UserContainer();

    public static void main(String[] args) {
        port(5677);

        get("/users", (request, response) -> {
            response.type("application/json");
            response.status(200);
            return new Gson()
                    .toJson(new UserContainerResponse(UserContainerResponse.ResponseEnum.SUCCESS,
                            "",
                            200,
                            "",
                            new Gson().toJsonTree(userContainer.getUsers())));
        });

        post("/users", (request, response) -> {
            response.type("application/json");

            JsonObject jsonRequest = null;
            try {
                jsonRequest = JsonParser.parseString(request.body()).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                response.status(400);
                return new Gson()
                        .toJson(new UserContainerResponse(UserContainerResponse.ResponseEnum.ERROR,
                                "Malformed request",
                                400,
                                "",
                                new Gson().toJsonTree("")));
            }

            JsonObject jsonInvalidFields = userContainer.checkUserFields(jsonRequest);
            if (jsonInvalidFields.size() == 0) {
                response.status(201);
                User user = new Gson().fromJson(request.body(), User.class);
                response.header("Location", "http://127.0.0.1:5677/users/" + user.getUsername());
                userContainer.addUser(user);
                return new Gson().
                        toJson(new UserContainerResponse(UserContainerResponse.ResponseEnum.SUCCESS,
                                "",
                                201,
                                "users/" + user.getUsername(),
                                new Gson().toJsonTree(user)));
            } else {
                response.status(400);
                return new Gson()
                        .toJson(new UserContainerResponse(UserContainerResponse.ResponseEnum.ERROR,
                                "Wrong user parameters",
                                400,
                                "",
                                new Gson().toJsonTree(jsonInvalidFields)));
            }
        });

        get("/users/:username", (request, response) -> {
            response.type("application/json");

            JsonObject jsonRequest = null;
            try {
                jsonRequest = JsonParser.parseString(request.body()).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                response.status(400);
                return new Gson()
                        .toJson(new UserContainerResponse(UserContainerResponse.ResponseEnum.ERROR,
                                "Malformed request",
                                400,
                                "",
                                new Gson().toJsonTree("")));
            }

            User user = userContainer.getUser(request.params(":username"));
            if (user == null) {
                response.status(404);
                return new Gson()
                        .toJson(new UserResponse(UserResponse.ResponseEnum.ERROR,
                                "Invalid username",
                                404,
                                new Gson().toJsonTree("")));
            } else {
                response.status(200);
                return new Gson()
                        .toJson(new UserResponse(UserResponse.ResponseEnum.SUCCESS,
                                "",
                                200,
                                new Gson().toJsonTree(user)));
            }
        });
    }

}
