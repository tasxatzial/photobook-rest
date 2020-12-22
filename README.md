# Photobook REST API

The following methods have been implemented:

* get("/users")
* post("/users")
* options("/users")
* get("/users/:username")
* put("/users/:username")
* options("/users/:username")
* get("/posts")
* options("/posts")
* get("/posts/:postID")
* options("/posts/:postID")
* get("/users/:username/posts")
* post("/users/:username/posts")
* options("/users/:username/posts")
* get("/users/:username/posts/:postID")
* options("/users/:username/posts/:postID")

The following methods have been implemented and return "method not supported":

* delete("/users")
* put("/users")
* post("/users/:username")
* delete("/users/:username")
* post("/posts")
* delete("/posts")
* put("/posts")
* post("/posts/:postID")
* delete("/posts/:postID")
* put("/posts/:postID")
* delete("/users/:username/posts")
* put("/users/:username/posts")
* post("/users/:username/posts/:postID")
* delete("/users/:username/posts/:postID")
* put("/users/:username/posts/:postID")
