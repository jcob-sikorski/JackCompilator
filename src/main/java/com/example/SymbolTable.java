package com.example;

import java.util.HashMap;

public class SymbolTable {
    private HashMap<String, IdentifierInfo> symbolTable = new HashMap<String, IdentifierInfo>();
    private HashMap<VARIABLE_IDENTIFIER, Integer> kindLastIndex = new HashMap<VARIABLE_IDENTIFIER, Integer>();

    public void reset() {
        symbolTable.clear();
        kindLastIndex.clear();
    }

    public void put(String name, String type, String kind) {
        VARIABLE_IDENTIFIER convertedKind = VARIABLE_IDENTIFIER.valueOf(kind.toUpperCase());
        
        Integer symbolTableIndex = varCount(convertedKind);

        if (kindLastIndex.containsKey(convertedKind)) {
            symbolTableIndex = kindLastIndex.get(convertedKind)+1;
            kindLastIndex.put(convertedKind, kindLastIndex.get(convertedKind) + 1);
        }
        else {
            kindLastIndex.put(convertedKind, 0);
            symbolTableIndex = 0;
        }

        IdentifierInfo identifierInfo = new IdentifierInfo(name, type, convertedKind, symbolTableIndex);

        symbolTable.put(name, identifierInfo);
    }

    public Integer varCount(VARIABLE_IDENTIFIER kind) {
        if (kindLastIndex.containsKey(kind)) {
            return kindLastIndex.get(kind);
        }
        else {
            return -1;
        }
    }

    public VARIABLE_IDENTIFIER kindOf(String name) {
        if (symbolTable.containsKey(name)) {
            return symbolTable.get(name).kind;
        }
        else {
            return VARIABLE_IDENTIFIER.NONE;
        }
    }

    public String typeOf(String name) {
        return symbolTable.get(name).type;
    }

    public Integer indexOf(String name) {
        return symbolTable.get(name).symbolTableIndex;
    }

    public Integer size() {
        return symbolTable.size();
    }
}
