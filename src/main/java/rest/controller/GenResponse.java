package rest.controller;

import com.google.gson.JsonElement;

public class GenResponse {
    private ResponseEnum status;
    private String message;
    private JsonElement data;

    public GenResponse(ResponseEnum status, String message, JsonElement data) {
        this.status = status;
        this.message = message;
        this.data = data;
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

    public enum ResponseEnum {
        SUCCESS, ERROR
    }

}
