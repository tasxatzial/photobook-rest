package rest.controller;

import com.google.gson.*;
import rest.model.*;

import java.io.IOException;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;

public class test {
    private static RootContainer rootContainer;
    private static UserContainer userContainer;
    private static PostContainer postContainer;

    static {
        try {
            rootContainer = new RootContainer();
            postContainer = rootContainer.getPostContainer();
            userContainer = rootContainer.getUserContainer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        port(5677);

        get("/", (request, response) -> {
            response.type("application/json");
            response.status(200);
            return new Gson()
                    .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                            "",
                            rootContainer.getLink(),
                            new JsonObject()));
        });

        get("/users", (request, response) -> {
            response.type("application/json");
            String pageParam = request.queryParams("page");
            if (pageParam != null) {
                int page = 0;
                try {
                    page = Integer.parseInt(pageParam);
                } catch (NumberFormatException ignored) {
                    response.status(400);
                    return new Gson()
                            .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                    "INVALID_REQUEST",
                                    new JsonObject()));
                }
                if (page < userContainer.getFirstPage() || page > userContainer.getLastPage()) {
                    response.status(404);
                    return new Gson()
                            .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                    "PAGE_NOT_FOUND",
                                    userContainer.getPageDefaultLinks(),
                                    new JsonArray()));
                } else {
                    response.status(200);
                    return new Gson()
                            .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                                    "",
                                    userContainer.getPageLinks(page),
                                    userContainer.getPage(page)));
                }
            } else {
                response.status(200);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                                "",
                                userContainer.getPageDefaultLinks(),
                                userContainer.getUsers()));
            }
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
                response.header("Location", "http://127.0.0.1:5677/" + user.getLink());
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
                                UserContainer.getLinks(user),
                                UserContainer.getData(user)));
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
                postContainer.deletePosts(user);
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
                    User newUser = new Gson().fromJson(request.body(), User.class);
                    User user = userContainer.getUser(username);
                    user.updateUser(newUser);
                    return new Gson().
                            toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                                    "",
                                    new JsonArray(),
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
            String pageParam = request.queryParams("page");
            if (pageParam != null) {
                int page = 0;
                try {
                    page = Integer.parseInt(pageParam);
                } catch (NumberFormatException ignored) {
                    response.status(400);
                    return new Gson()
                            .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                    "INVALID_REQUEST",
                                    new JsonObject()));
                }
                if (page < postContainer.getFirstPage() || page > postContainer.getLastPage()) {
                    response.status(400);
                    return new Gson()
                            .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                    "PAGE_NOT_FOUND",
                                    postContainer.getPageDefaultLinks(),
                                    new JsonArray()));
                } else {
                    response.status(200);
                    return new Gson()
                            .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                                    "",
                                    postContainer.getPageLinks(page),
                                    postContainer.getPage(page)));
                }
            } else {
                response.status(200);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                                "",
                                postContainer.getPageDefaultLinks(),
                                postContainer.getPosts()));
            }
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
                                PostContainer.getUserLinks(post),
                                PostContainer.getData(post)));
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
            }
            String pageParam = request.queryParams("page");
            if (pageParam != null) {
                int page = 0;
                try {
                    page = Integer.parseInt(pageParam);
                } catch (NumberFormatException ignored) {
                    response.status(400);
                    return new Gson()
                            .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                    "INVALID_REQUEST",
                                    new JsonObject()));
                }
                if (page < postContainer.getUserFirstPage(user) || page > postContainer.getUserLastPage(user)) {
                    response.status(400);
                    return new Gson()
                            .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.ERROR,
                                    "PAGE_NOT_FOUND",
                                    postContainer.getUserPageDefaultLinks(user),
                                    new JsonArray()));
                } else {
                    response.status(200);
                    return new Gson()
                            .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                                    "",
                                    postContainer.getUserPageLinks(page, user),
                                    postContainer.getUserPage(page, user)));
                }
            } else {
                response.status(200);
                return new Gson()
                        .toJson(new ApiResponse(ApiResponse.ApiResponseEnum.SUCCESS,
                                "",
                                postContainer.getUserPageDefaultLinks(user),
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
                    postContainer.addPost(post, user);
                    response.header("Location", "http://127.0.0.1:5677/" + post.getLink());
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
                                PostContainer.getIDLink(post),
                                PostContainer.getData(post)));
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
                postContainer.deletePost(post, user);
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
