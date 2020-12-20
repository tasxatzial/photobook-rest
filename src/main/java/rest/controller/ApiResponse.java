package rest.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class ApiResponse {
    private ApiResponseEnum status;
    private String message;
    private JsonElement links;
    private JsonElement data;

    public ApiResponse(ApiResponseEnum status, String message, JsonElement data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.links = new JsonArray();
    }

    public ApiResponse(ApiResponseEnum status, String message, JsonElement links, JsonElement data) {
        this(status, message, data);
        this.links = links;
    }

    public ApiResponseEnum getStatus() {
        return status;
    }

    public void setStatus(ApiResponseEnum status) {
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

    public void setLinks(JsonElement links) {
        this.links = links;
    }

    public enum ApiResponseEnum {
        SUCCESS, ERROR
    }

}
