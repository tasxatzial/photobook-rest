package rest.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GenContainerResponse {
    private ResponseEnum status;
    private String message;
    private JsonElement links;
    private JsonElement data;

    public GenContainerResponse(ResponseEnum status, String message, JsonElement links, JsonElement data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.links = links;
    }

    public ResponseEnum getStatus() {
        return status;
    }

    public void setStatus(ResponseEnum status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

    public JsonElement getLinks() {
        return links;
    }

    public void setLinks(JsonElement resource) {
        this.links = links;
    }

    public enum ResponseEnum {
        SUCCESS, ERROR
    }
}
