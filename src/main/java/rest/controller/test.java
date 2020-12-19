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

            JsonObject invalidRequestProps = userContainer.checkUserFields(jsonRequest, "", false);
            if (invalidRequestProps.size() == 0) {
                response.status(201);
                User user = new Gson().fromJson(request.body(), User.class);
                response.header("Location", "http://127.0.0.1:5677/" + userContainer.getResource(user));
                userContainer.addUser(user);
                return new Gson().
                        toJson(new UserContainerResponse(UserContainerResponse.ResponseEnum.SUCCESS,
                                "",
                                201,
                                userContainer.getResource(user),
                                new Gson().toJsonTree(user)));
            } else {
                response.status(400);
                return new Gson()
                        .toJson(new UserContainerResponse(UserContainerResponse.ResponseEnum.ERROR,
                                "Wrong user parameters",
                                400,
                                "",
                                new Gson().toJsonTree(invalidRequestProps)));
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

            String username = request.params(":username").trim().toLowerCase();
            User user = userContainer.getUser(username);
            if (user == null) {
                response.status(404);
                JsonObject data = new JsonObject();
                data.addProperty("username", username);
                return new Gson()
                        .toJson(new UserResponse(UserResponse.ResponseEnum.ERROR,
                                "Username not found",
                                404,
                                new Gson().toJsonTree(data)));
            } else {
                response.status(200);
                return new Gson()
                        .toJson(new UserResponse(UserResponse.ResponseEnum.SUCCESS,
                                "",
                                200,
                                new Gson().toJsonTree(user)));
            }
        });

        put("/users/:username", (request, response) -> {
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

            String username = request.params(":username").trim().toLowerCase();
            if (!userContainer.usernameExists(username)) {
                response.status(404);
                JsonObject data = new JsonObject();
                data.addProperty("username", username);
                return new Gson()
                        .toJson(new UserResponse(UserResponse.ResponseEnum.ERROR,
                                "Username not found",
                                404,
                                new Gson().toJsonTree(data)));
            } else {
                JsonObject invalidRequestProps = userContainer.checkUserFields(jsonRequest, username, true);
                if (invalidRequestProps.size() == 0) {
                    response.status(200);
                    User user = new Gson().fromJson(request.body(), User.class);
                    userContainer.addUser(user);
                    return new Gson().
                            toJson(new UserResponse(UserResponse.ResponseEnum.SUCCESS,
                                    "",
                                    200,
                                    new Gson().toJsonTree(user)));
                } else {
                    response.status(400);
                    return new Gson()
                            .toJson(new UserResponse(UserResponse.ResponseEnum.ERROR,
                                    "Wrong user parameters",
                                    400,
                                    new Gson().toJsonTree(invalidRequestProps)));
                }
            }
        });

        post("/users/:username", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new UserResponse(UserResponse.ResponseEnum.ERROR,
                            "POST not supported in non container URIs",
                            405,
                            new Gson().toJsonTree("")));
        });

    }

}
