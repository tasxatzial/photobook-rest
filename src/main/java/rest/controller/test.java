package rest.controller;

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


    }

}
