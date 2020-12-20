package rest.controller;

import com.google.gson.*;
import rest.model.Post;
import rest.model.PostContainer;
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
    private static final PostContainer postContainer = new PostContainer();

    public static void main(String[] args) {
        port(5677);

        get("/users", (request, response) -> {
            response.type("application/json");
            response.status(200);
            return new Gson()
                    .toJson(new GenResponse(GenResponse.ResponseEnum.SUCCESS,
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
                        .toJson(new GenResponse(GenResponse.ResponseEnum.ERROR,
                                "Malformed request",
                                new Gson().toJsonTree("")));
            }

            JsonObject invalidRequestProps = userContainer.checkUserFields(jsonRequest, "", false);
            if (invalidRequestProps.size() == 0) {
                response.status(201);
                User user = new Gson().fromJson(request.body(), User.class);
                response.header("Location", "http://127.0.0.1:5677/" + userContainer.getMainLink(user));
                userContainer.addUser(user);
                return new Gson().
                        toJson(new GenContainerResponse(GenContainerResponse.ResponseEnum.SUCCESS,
                                "",
                                UserContainer.getLinks(user),
                                new Gson().toJsonTree(user)));
            } else {
                response.status(400);
                return new Gson()
                        .toJson(new GenContainerResponse(GenContainerResponse.ResponseEnum.ERROR,
                                "Wrong user parameters",
                                new JsonArray(),
                                new Gson().toJsonTree(invalidRequestProps)));
            }
        });

        delete("/users", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new GenResponse(GenResponse.ResponseEnum.ERROR,
                            "DELETE not supported",
                            new Gson().toJsonTree("")));
        });

        put("/users", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new GenResponse(GenResponse.ResponseEnum.ERROR,
                            "PUT not supported in container URIs",
                            new Gson().toJsonTree("")));
        });

        get("/users/:username", (request, response) -> {
            response.type("application/json");

            JsonObject jsonRequest = null;
            try {
                jsonRequest = JsonParser.parseString(request.body()).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                response.status(400);
                return new Gson()
                        .toJson(new GenResponse(GenResponse.ResponseEnum.ERROR,
                                "Malformed request",
                                new Gson().toJsonTree("")));
            }

            String username = request.params(":username").trim().toLowerCase();
            User user = userContainer.getUser(username);
            if (user == null) {
                response.status(404);
                JsonObject data = new JsonObject();
                data.addProperty("username", username);
                return new Gson()
                        .toJson(new GenResponse(GenResponse.ResponseEnum.ERROR,
                                "Username not found",
                                new Gson().toJsonTree(data)));
            } else {
                response.status(200);
                return new Gson()
                        .toJson(new GenResponse(GenResponse.ResponseEnum.SUCCESS,
                                "",
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
                        .toJson(new GenResponse(GenResponse.ResponseEnum.ERROR,
                                "Malformed request",
                                new Gson().toJsonTree("")));
            }

            String username = request.params(":username").trim().toLowerCase();
            if (!userContainer.usernameExists(username)) {
                response.status(404);
                JsonObject data = new JsonObject();
                data.addProperty("username", username);
                return new Gson()
                        .toJson(new GenResponse(GenResponse.ResponseEnum.ERROR,
                                "Username not found",
                                new Gson().toJsonTree(data)));
            } else {
                JsonObject invalidRequestProps = userContainer.checkUserFields(jsonRequest, username, true);
                if (invalidRequestProps.size() == 0) {
                    response.status(200);
                    User user = new Gson().fromJson(request.body(), User.class);
                    userContainer.addUser(user);
                    return new Gson().
                            toJson(new GenResponse(GenResponse.ResponseEnum.SUCCESS,
                                    "",
                                    new Gson().toJsonTree(user)));
                } else {
                    response.status(400);
                    return new Gson()
                            .toJson(new GenResponse(GenResponse.ResponseEnum.ERROR,
                                    "Wrong user parameters",
                                    new Gson().toJsonTree(invalidRequestProps)));
                }
            }
        });

        post("/users/:username", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new GenResponse(GenResponse.ResponseEnum.ERROR,
                            "POST not supported in non container URIs",
                            new Gson().toJsonTree("")));
        });

        delete("/users/:username", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new GenResponse(GenResponse.ResponseEnum.ERROR,
                            "DELETE not supported",
                            new Gson().toJsonTree("")));
        });

        post("/posts", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new GenResponse(GenResponse.ResponseEnum.ERROR,
                            "POST not supported",
                            new Gson().toJsonTree("")));
        });

        delete("/posts", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new GenResponse(GenResponse.ResponseEnum.ERROR,
                            "DELETE not supported",
                            new Gson().toJsonTree("")));
        });

        put("/posts", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new GenResponse(GenResponse.ResponseEnum.ERROR,
                            "PUT not supported in container URIs",
                            new Gson().toJsonTree("")));
        });

        get("/posts", (request, response) -> {
            response.type("application/json");
            response.status(200);
            return new Gson()
                    .toJson(new GenResponse(GenResponse.ResponseEnum.SUCCESS,
                            "",
                            new Gson().toJsonTree(postContainer.getPosts())));
        });

        get("/posts/:postID", (request, response) -> {
            response.type("application/json");

            JsonObject jsonRequest = null;
            try {
                jsonRequest = JsonParser.parseString(request.body()).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                response.status(400);
                return new Gson()
                        .toJson(new GenContainerResponse(GenContainerResponse.ResponseEnum.ERROR,
                                "Malformed request",
                                new JsonArray(),
                                new Gson().toJsonTree("")));
            }

            String str_postID = request.params(":postID");
            int postID = -1;
            try {
                postID = Integer.parseInt(str_postID);
            } catch (NumberFormatException e) {
                response.status(404);
                JsonObject data = new JsonObject();
                data.addProperty("postID", str_postID);
                return new Gson()
                        .toJson(new GenContainerResponse(GenContainerResponse.ResponseEnum.ERROR,
                                "Invalid post ID format",
                                new JsonArray(),
                                new Gson().toJsonTree(data)));
            }

            Post post = postContainer.getPost(postID);
            if (post == null) {
                response.status(404);
                JsonObject data = new JsonObject();
                data.addProperty("postID", postID);
                return new Gson()
                        .toJson(new GenContainerResponse(GenContainerResponse.ResponseEnum.ERROR,
                                "Post ID not found",
                                new JsonArray(),
                                new Gson().toJsonTree(data)));
            } else {
                response.status(200);
                return new Gson()
                        .toJson(new GenContainerResponse(GenContainerResponse.ResponseEnum.SUCCESS,
                                "",
                                PostContainer.getLinks(post),
                                new Gson().toJsonTree(post)));
            }
        });
    }
}
