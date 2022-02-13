# Photobook REST API

A basic REST API for the [photobook](https://github.com/tasxatzial/photobook) project. Written with the Java [Spark](https://sparkjava.com/) framework.

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

## Compile

Requirements: Java (8 or 11) & Maven 3.6

From the command line switch to the root folder of the project and run:

    mvn dependency:copy-dependencies
    mvn package

The first command should copy all .jar dependecies in the 'target/dependency' folder. The second command should build the final executable jar file in the 'target' folder.

## Run

Switch to the 'target' folder and run:

    java -jar rest-1.0-SNAPSHOT.jar
