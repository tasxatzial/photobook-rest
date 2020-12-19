package rest.controller;

import com.google.gson.JsonElement;

public class UserResponse {
    private ResponseEnum status;
    private String message;
    private int code;
    private JsonElement data;

    public UserResponse(ResponseEnum status, String message, int code, JsonElement data) {
        this.status = status;
        this.message = message;
        this.code = code;
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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public enum ResponseEnum {
        SUCCESS, ERROR
    }

}
