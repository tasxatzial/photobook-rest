package rest.controller;

import com.google.gson.JsonElement;

public class UserContainerResponse {
    private ResponseEnum status;
    private String message;
    private String resource;
    private JsonElement data;

    public UserContainerResponse(ResponseEnum status, String message, String resource, JsonElement data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.resource = resource;
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

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public enum ResponseEnum {
        SUCCESS, ERROR
    }
}
