package com.example;

import java.util.HashMap;

public class SymbolTable {
    private HashMap<String, IdentifierInfo> symbolTable = new HashMap<String, IdentifierInfo>();
    private HashMap<VARIABLE_IDENTIFIER, Integer> kindLastIndex = new HashMap<VARIABLE_IDENTIFIER, Integer>();

    public void reset() {
        symbolTable.clear();
    }

    public void put(String name, String type, String kind) {
        VARIABLE_IDENTIFIER convertedKind = VARIABLE_IDENTIFIER.valueOf(kind);
        
        Integer index = varCount(convertedKind) + 1;
        IdentifierInfo identifierInfo = new IdentifierInfo(name, type, convertedKind, index);

        symbolTable.put(name, identifierInfo);
    }

    public Integer varCount(VARIABLE_IDENTIFIER kind) {
        return kindLastIndex.get(kind);
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
        return symbolTable.get(name).index;
    }
}
