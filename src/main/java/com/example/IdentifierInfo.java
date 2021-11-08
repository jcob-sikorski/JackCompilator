package com.example;

public class IdentifierInfo {
    public String name;
    public String type;
    public VARIABLE_IDENTIFIER kind;
    public Integer index;

    public IdentifierInfo(String name, String type, VARIABLE_IDENTIFIER kind, Integer index) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.index = index;
    }
}
