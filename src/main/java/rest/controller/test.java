package rest.controller;

import com.google.gson.*;
import rest.model.Post;
import rest.model.PostContainer;
import rest.model.User;
import rest.model.UserContainer;

import java.io.IOException;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;

public class test {
    private static UserContainer userContainer;
    private static final PostContainer postContainer = new PostContainer();

    static {
        try {
            userContainer = new UserContainer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        port(5677);

        get("/users", (request, response) -> {
            response.type("application/json");
            String pageParam = request.queryParams("offset");
            int page = -1;
            if (pageParam != null) {
                try {
                    page = Integer.parseInt(pageParam);
                } catch (NumberFormatException ignored) { }
            }

            response.status(200);
            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                            "",
                            userContainer.getLinks(page),
                            userContainer.getUsers(page)));
        });

        post("/users", (request, response) -> {
            response.type("application/json");

            JsonObject jsonRequest = null;
            try {
                jsonRequest = JsonParser.parseString(request.body()).getAsJsonObject();
            } catch (JsonSyntaxException | IllegalStateException e) {
                response.status(400);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "INVALID_REQUEST",
                                new JsonObject()));
            }

            JsonObject invalidRequestProps = userContainer.checkUserFields(jsonRequest, "", false);
            if (invalidRequestProps.size() == 0) {
                response.status(201);
                User user = new Gson().fromJson(request.body(), User.class);
                response.header("Location", "http://127.0.0.1:5677/" + UserContainer.getMainLink(user));
                userContainer.addUser(user);
                return new Gson().
                        toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                                "",
                                UserContainer.getLinks(user),
                                new JsonObject()));
            } else {
                response.status(400);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "INVALID_PARAMETERS",
                                new JsonArray(),
                                invalidRequestProps));
            }
        });

        delete("/users", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                            "DELETE_NOT_SUPPORTED",
                            new JsonObject()));
        });

        put("/users", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                            "PUT_NOT_SUPPORTED",
                            new JsonObject()));
        });

        options("/users", (request, response) -> {
            response.type("application/json");
            response.status(200);
            response.header("Allow", "GET,POST");
            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                            "",
                            new JsonObject()));
        });

        get("/users/:username", (request, response) -> {
            response.type("application/json");

            String username = request.params(":username").trim().toLowerCase();
            User user = userContainer.getUser(username);
            if (user == null) {
                response.status(404);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "USERNAME_NOT_FOUND",
                                new JsonArray(),
                                new JsonObject()));
            } else {
                response.status(200);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                                "",
                                UserContainer.getPostsLinks(user),
                                new Gson().toJsonTree(user)));
            }
        });

        post("/users/:username", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                            "POST_NOT_SUPPORTED",
                            new JsonObject()));
        });

        delete("/users/:username", (request, response) -> {
            response.type("application/json");

            String username = request.params(":username").trim().toLowerCase();
            if (!userContainer.usernameExists(username)) {
                response.status(404);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "USERNAME_NOT_FOUND",
                                new JsonArray(),
                                new JsonObject()));
            } else {
                response.status(200);
                User user = userContainer.getUser(username);
                postContainer.deleteUserPosts(user);
                userContainer.deleteUser(user);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                                "",
                                new JsonArray(),
                                new JsonObject()));
            }
        });

        put("/users/:username", (request, response) -> {
            response.type("application/json");

            JsonObject jsonRequest = null;
            try {
                jsonRequest = JsonParser.parseString(request.body()).getAsJsonObject();
            } catch (JsonSyntaxException | IllegalStateException e) {
                response.status(400);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "INVALID_REQUEST",
                                new JsonObject()));
            }

            String username = request.params(":username").trim().toLowerCase();
            if (!userContainer.usernameExists(username)) {
                response.status(404);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "USERNAME_NOT_FOUND",
                                new JsonArray(),
                                new JsonObject()));
            } else {
                JsonObject invalidRequestProps = userContainer.checkUserFields(jsonRequest, username, true);
                if (invalidRequestProps.size() == 0) {
                    response.status(200);
                    User user = new Gson().fromJson(request.body(), User.class);
                    userContainer.addUser(user);
                    return new Gson().
                            toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                                    "",
                                    UserContainer.getPostsLinks(user),
                                    new JsonObject()));
                } else {
                    response.status(400);
                    return new Gson()
                            .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                    "INVALID_PARAMETERS",
                                    invalidRequestProps));
                }
            }
        });

        options("/users/:username", (request, response) -> {
            response.type("application/json");
            response.status(200);
            response.header("Allow", "GET,PUT,DELETE");
            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                            "",
                            new JsonObject()));
        });

        get("/posts", (request, response) -> {
            response.type("application/json");
            response.status(200);
            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                            "",
                            new JsonArray(),
                            postContainer.getPosts()));
        });

        post("/posts", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                            "POST_NOT_SUPPORTED",
                            new JsonObject()));
        });

        delete("/posts", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                            "DELETE_NOT_SUPPORTED",
                            new JsonObject()));
        });

        put("/posts", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                            "PUT_NOT_SUPPORTED",
                            new JsonObject()));
        });

        options("/posts", (request, response) -> {
            response.type("application/json");
            response.status(200);
            response.header("Allow", "GET");
            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                            "",
                            new JsonObject()));
        });

        get("/posts/:postID", (request, response) -> {
            response.type("application/json");

            String str_postID = request.params(":postID");
            int postID = -1;
            try {
                postID = Integer.parseInt(str_postID);
            } catch (NumberFormatException e) {
                response.status(400);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "POSTID_INVALID",
                                new JsonObject()));
            }

            Post post = postContainer.getPost(postID);
            if (post == null) {
                response.status(404);
                JsonObject data = new JsonObject();
                data.addProperty("postID", "");
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "POSTID_NOT_FOUND",
                                new JsonArray(),
                                new JsonObject()));
            } else {
                response.status(200);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                                "",
                                PostContainer.getNamedLinks(post),
                                new Gson().toJsonTree(post)));
            }
        });

        post("/posts/:postID", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                            "POST_NOT_SUPPORTED",
                            new JsonObject()));
        });

        delete("/posts/:postID", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                            "DELETE_NOT_SUPPORTED",
                            new JsonObject()));
        });

        put("/posts/:postID", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                            "PUT_NOT_SUPPORTED",
                            new JsonObject()));
        });

        options("/posts/:postID", (request, response) -> {
            response.type("application/json");
            response.status(200);
            response.header("Allow", "GET");
            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                            "",
                            new JsonObject()));
        });

        get("/users/:username/posts", (request, response) -> {
            response.type("application/json");

            String username = request.params(":username").trim().toLowerCase();
            User user = userContainer.getUser(username);
            if (user == null) {
                response.status(404);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "USERNAME_NOT_FOUND",
                                new JsonArray(),
                                new JsonArray()));
            } else {
                response.status(200);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                                "",
                                new JsonArray(),
                                postContainer.getUserPosts(user)));
            }
        });

        post("/users/:username/posts", (request, response) -> {
            response.type("application/json");

            JsonObject jsonRequest = null;
            try {
                jsonRequest = JsonParser.parseString(request.body()).getAsJsonObject();
            } catch (JsonSyntaxException | IllegalStateException e) {
                response.status(400);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "INVALID_REQUEST",
                                new JsonObject()));
            }

            String username = request.params(":username").trim().toLowerCase();
            User user = userContainer.getUser(username);
            if (user == null) {
                response.status(404);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "USERNAME_NOT_FOUND",
                                new JsonArray(),
                                new JsonObject()));
            } else {
                JsonObject invalidRequestProps = postContainer.checkPostFields(jsonRequest);
                if (invalidRequestProps.size() == 0) {
                    response.status(201);
                    Post post = new Gson().fromJson(request.body(), Post.class);
                    post.setUsername(username);
                    postContainer.addPost(post);
                    response.header("Location", "http://127.0.0.1:5677/" + PostContainer.getMainLink(post));
                    return new Gson().
                            toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                                    "",
                                    PostContainer.getLinks(post),
                                    new JsonObject()));
                } else {
                    response.status(400);
                    return new Gson()
                            .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                    "INVALID_PARAMETERS",
                                    new JsonArray(),
                                    invalidRequestProps));
                }
            }
        });

        delete("/users/:username/posts", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                            "DELETE_NOT_SUPPORTED",
                            new JsonObject()));
        });

        put("/users/:username/posts", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                            "PUT_NOT_SUPPORTED",
                            new JsonObject()));
        });

        options("/users/:username/posts", (request, response) -> {
            response.type("application/json");
            response.status(200);
            response.header("Allow", "GET,POST");
            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                            "",
                            new JsonObject()));
        });

        get("/users/:username/posts/:postID", (request, response) -> {
            response.type("application/json");

            String username = request.params(":username").trim().toLowerCase();
            User user = userContainer.getUser(username);
            if (user == null) {
                response.status(404);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "USERNAME_NOT_FOUND",
                                new JsonArray(),
                                new JsonObject()));
            }

            String str_postID = request.params(":postID");
            int postID = -1;
            try {
                postID = Integer.parseInt(str_postID);
            } catch (NumberFormatException e) {
                response.status(400);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "POSTID_INVALID",
                                new JsonObject()));
            }

            Post post = postContainer.getPost(postID);
            if (post == null) {
                response.status(404);
                JsonObject data = new JsonObject();
                data.addProperty("postID", "");
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "POSTID_NOT_FOUND",
                                new JsonArray(),
                                new JsonObject()));
            } else {
                response.status(200);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                                "",
                                PostContainer.getIDLinks(post),
                                new Gson().toJsonTree(post)));
            }
        });

        post("/users/:username/posts/:postID", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                            "POST_NOT_SUPPORTED",
                            new JsonObject()));
        });

        delete("/users/:username/posts/:postID", (request, response) -> {
            response.type("application/json");

            String username = request.params(":username").trim().toLowerCase();
            User user = userContainer.getUser(username);
            if (user == null) {
                response.status(404);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "USERNAME_NOT_FOUND",
                                new JsonArray(),
                                new JsonObject()));
            }

            String str_postID = request.params(":postID");
            int postID = -1;
            try {
                postID = Integer.parseInt(str_postID);
            } catch (NumberFormatException e) {
                response.status(400);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "POSTID_INVALID",
                                new JsonObject()));
            }

            Post post = postContainer.getPost(postID);
            if (post == null) {
                response.status(404);
                JsonObject data = new JsonObject();
                data.addProperty("postID", "");
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                "POSTID_NOT_FOUND",
                                new JsonArray(),
                                new JsonObject()));
            } else {
                response.status(200);
                postContainer.deletePost(post);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                                "",
                                new JsonArray(),
                                new JsonObject()));
            }
        });

        put("/users/:username/posts/:postID", (request, response) -> {
            response.type("application/json");
            response.status(405);

            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                            "PUT_NOT_SUPPORTED",
                            new JsonObject()));
        });

        options("/users/:username/posts/:postID", (request, response) -> {
            response.type("application/json");
            response.status(200);
            response.header("Allow", "GET,DELETE");
            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                            "",
                            new JsonObject()));
        });
    }
}
