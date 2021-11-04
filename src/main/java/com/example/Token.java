package com.example;

class Token {
    private String tokenType;
    private String token;

    public Token(String tokenType, String token) {
        this.tokenType = tokenType;
        this.token = token;
    }

    public String tokenType() {
        return this.tokenType;
    }
    public String token() {
        return this.token;
    }
};
