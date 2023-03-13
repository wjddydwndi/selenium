package com.example.selenium.model;

import lombok.Data;


@Data
public class Response {
    private String code;         // Return Code
    private String message;      // Return Message
    private Object data;         // Output Object Json
    private String name;         // competitor
    private String country;

    public Response(String code, String message, Object data, String name) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.name = name;
    }

    public Response(String code, String message, String name) {
        this.code = code;
        this.message = message;
        this.name = name;
    }

    public Response(String code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Response(String code, String message, Object data, String name, String country) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.name = name;
        this.country = country;
    }

    public Response(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
