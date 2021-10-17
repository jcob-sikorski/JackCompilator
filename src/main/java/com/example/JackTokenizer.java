package com.example;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


class JackTokenizer { // TODO write valid XML file
    private String buffer = "";

    // booleans help buffering specific sequences
    private boolean stringConstant = false; 
    private boolean integerConstant = false;
    private boolean identifierKeyword = false;
    private boolean comment = false;

    private Set<String> keywords = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("class", "constructor", "function", "method", "field", "static",
                                    "var", "int", "char", "boolean", "void", "true", "false", "null", "this",
                                    "let", "do", "if", "else", "while", "return")));

    private Set<Character> symbols = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList('{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-', '*', '/', '&', '|','<', '>', '=', '~')));
    

    // on initialization ignore all comments and whitespace in the input stream, 
    // and serialize it into Jack-language tokens. 
    // The token types are specified according to Jack grammar.
    public JackTokenizer(File file) throws IOException {        
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
        int c;
        while((c = reader.read()) != -1) {
            Character character = (char) c;
            advance(character);
        }
        reader.close();
    }


    private void advance(Character character) {
        if (comment == true) {
            if (!character.equals('\n')) { // ignore content of comment
                return;
            }
            else { // singal that it's the end of the comment
                comment = false;
                return;
            }
        }
        if (character.equals('/')) { // signal that comment starts
            comment = true;
            return;
        }


        if ((int)'\"' == (int)character) { // handle stringConstant
            if (stringConstant == false) { // open stringConsant buffer
                stringConstant = true;
                buffer = "";
            }
            else {
                writeXML(tokenType(buffer), buffer); // close stringConstant buffer
                stringConstant = false;
                buffer = "";
            }
            return;
        }
        if (stringConstant == true) { // add character to stringConstant buffer
            buffer += character;
            return;
        }


        if (Character.isDigit(character)) { // handle integerConstant
            integerConstant = true; // open integerConstant buffer
            buffer += character;
            return;
        }
        if (integerConstant == true) {
            writeXML(tokenType(buffer), buffer); // close integerConstant buffer
            buffer = "";
            integerConstant = false; 
        }


        if (character.equals(' ') || character.equals('\n')) { // ignore whitespace and enters
            if (identifierKeyword == true) {
                writeXML(tokenType(buffer), buffer); // close identifier/keyword buffer
                buffer = "";
                identifierKeyword = false;
            }
            return;
        }


        if (Character.isLetter(character) || character.equals('_')) { // handle identifiers/keywords
            identifierKeyword = true; // open identifier/keyword buffer
            buffer += character;
            return;
        }


        if (symbols.contains(character)) { // handle symbols
            if (identifierKeyword == true) { // close identifier/keyword buffer
                writeXML(tokenType(buffer), buffer);
                buffer = "";
                identifierKeyword = false;
            }
            buffer = "";
            String sCharacter = character.toString(); // simultaneously write symbol
            writeXML(tokenType(sCharacter), sCharacter);
            return;
        }
    }


    private enum TOKEN_TYPE {
        KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST, ERROR
    }

    private TOKEN_TYPE tokenType(String token) { // Returns the type of current token, 
                                                 // as a constant.
        if (keywords.contains(token)) {
            return TOKEN_TYPE.KEYWORD;
        }
        if (token != "") {
            if (symbols.contains(token.charAt(0))) { // handle symbol
                return TOKEN_TYPE.SYMBOL;
            }
            if (token.chars().allMatch( Character::isDigit)) { // handle integerConstant
                int number = Integer.parseInt(token);
                if (number >= 0 && number <= 32767) { // check if integerConstant is in valid range
                    return TOKEN_TYPE.INT_CONST;
                }
            }
            if (token.chars().allMatch(c -> isIdentifier(c))) { // handle indentifier
                return TOKEN_TYPE.IDENTIFIER;
            }
        }
        if (stringConstant == true) { // handle stringConstant
            return TOKEN_TYPE.STRING_CONST;
        }
        return TOKEN_TYPE.ERROR;
    }


    private enum KEYWORD {
        CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR, VOID, VAR, STATIC, FIELD, LET, DO, IF, ELSE, WHILE, RETURN, TRUE, FALSE, NULL, THIS
    }


    private boolean isIdentifier(int intObj) {
        char c = (char)(intObj);
        return Character.isLetter(c) || (c == '_');
    }


    private void writeXML(TOKEN_TYPE tp, String buffer) {
        System.out.println(tp + " - " + buffer);
    }
};