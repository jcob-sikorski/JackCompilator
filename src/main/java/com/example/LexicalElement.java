package com.example;

class LexicalElement {
    private String sectionType;
    private String section;

    private TOKEN_TYPE tokenType;
    private String token;

    public LexicalElement(String sectionType, String section) {
        this.sectionType = sectionType;
        this.section = section;
    }

    public LexicalElement(TOKEN_TYPE tokenType, String token) {
        this.tokenType = tokenType;
        this.token = token;
    }

    public String sectionType() {
        return this.sectionType;
    }
    public String section() {
        return this.section;
    }

    public TOKEN_TYPE tokenType() {
        return this.tokenType;
    }
    public String token() {
        return this.token;
    }
};
