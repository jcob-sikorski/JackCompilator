package com.example;

class Token {
    private TOKEN_TYPE tokenType;
    private String token;

    public Token(TOKEN_TYPE tokenType, String token) {
        this.tokenType = tokenType;
        this.token = token;
    }

    public TOKEN_TYPE tokenType() {
        return this.tokenType;
    }
    public String token() {
        return this.token;
    }
};
