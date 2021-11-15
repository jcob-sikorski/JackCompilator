package com.example;

// holds info (name, type, kind, symbolTableIndex) about jack identifier
public class IdentifierInfo {
    public String name;
    public String type;
    public VARIABLE_IDENTIFIER kind;
    public Integer symbolTableIndex;

    public IdentifierInfo(String name, String type, VARIABLE_IDENTIFIER kind, Integer symbolTableIndex) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.symbolTableIndex = symbolTableIndex;
    }
}
