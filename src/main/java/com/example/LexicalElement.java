package com.example;

// holds info (tokenType, token) about serialized token
class LexicalElement {
    private String tokenType;
    private String token;

    public LexicalElement(String tokenType, String token) {
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
