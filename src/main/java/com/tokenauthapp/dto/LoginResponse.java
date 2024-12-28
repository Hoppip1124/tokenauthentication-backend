package com.tokenauthapp.dto;

public class LoginResponse {
    private final String name;
    private final String token;
    private final String redirectUrl;
    private final String message;

    public LoginResponse(String name, String token, String redirectUrl, String message) {
        this.name = name;
        this.token = token;
        this.redirectUrl = redirectUrl;
        this.message = message;
    }

    public String getName() {return name;}
    public String getToken() {return token;}
    public String getRedirectUrl() {return redirectUrl;}
    public String getMessage() {return message;}
}
