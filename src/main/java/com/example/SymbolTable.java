package com.example;

import java.util.HashMap;

public class SymbolTable {
    private HashMap<String, IdentifierInfo> classSymbolTable = new HashMap<String, IdentifierInfo>();
    private HashMap<String, IdentifierInfo> subroutineSymbolTable = new HashMap<String, IdentifierInfo>();

    private HashMap<VARIABLE_IDENTIFIER, Integer> kindLastIndex = new HashMap<VARIABLE_IDENTIFIER, Integer>();

    public void startSubroutine() {
        classSymbolTable.clear();
        subroutineSymbolTable.clear();
    }

    public void define(String name, String type, VARIABLE_IDENTIFIER kind) {
        Integer index = varCount(kind) + 1;
        IdentifierInfo identifierInfo = new IdentifierInfo(name, type, kind, index);

        if (kind.equals(VARIABLE_IDENTIFIER.STATIC) || kind.equals(VARIABLE_IDENTIFIER.FIELD)) {
            classSymbolTable.put(name, identifierInfo);
        }
        else {
            subroutineSymbolTable.put(name, identifierInfo);
        }
    }

    public Integer varCount(VARIABLE_IDENTIFIER kind) {
        return kindLastIndex.get(kind);
    }

    public VARIABLE_IDENTIFIER kindOf(String name) {
        if (classSymbolTable.containsKey(name)) {
            return classSymbolTable.get(name).kind;
        }
        else if (subroutineSymbolTable.containsKey(name)) {
            return subroutineSymbolTable.get(name).kind;
        }
        return VARIABLE_IDENTIFIER.NONE;
    }

    public String typeOf(String name) {
        try {
            return classSymbolTable.get(name).type;
        } catch (Exception e) {
            return subroutineSymbolTable.get(name).type;
        }
    }

    public Integer indexOf(String name) {
        try {
            return classSymbolTable.get(name).index;
        } catch (Exception e) {
            return subroutineSymbolTable.get(name).index;
        }
    }
}
