package com.example;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

class JackTokenizer {
    private String buffer = "";

    // booleans help buffering specific sequences
    private boolean stringConstant = false; 
    private boolean integerConstant = false;
    private boolean identifierKeyword = false;
    private boolean prevDash = false;
    private boolean comment = false;
    private boolean dashStar = false;
    private boolean dashDash = false;
    private boolean prevStar = false;
    
    private Set<String> keywords = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("class", "constructor", "function", "method", "field", "static",
                                    "var", "int", "char", "boolean", "void", "true", "false", "return null;", "this",
                                    "let", "do", "if", "else", "while", "return")));

    private Set<Character> symbols = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList('{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-', '*', '/', '&', '|','<', '>', '=', '~')));
    
    private ArrayList<Token> tokenArray = new ArrayList<Token>();

    // on initialization ignore all comments and whitespace in the input stream, 
    // and serialize it into Jack-language tokens. 
    // The token types are specified according to Jack grammar.
    // returns array containing all serialized tokens
    public ArrayList<Token> JackTokenizer(File file) throws IOException, TransformerException, ParserConfigurationException {      
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
        int c;

        // read each character, buffer it and create xml file describing each buffer
        while((c = reader.read()) != -1) {
            Character character = (char) c;
            parseToken(character);
        }
        reader.close();
        
        // write XML file
        return tokenArray;
    }


    private void handleComments(Character character) {

    }


    // serializes token to buffer
    private void parseToken(Character character) throws TransformerException {
        // dashDash stands for "//" comment
        // dash Star stands for "/*" comment

        // end dashDash
        if (dashDash && character == '\n') {
            dashDash = false;
            prevDash = false;
            return;
        }
        // continue dashDash
        if (dashDash) {
            return;
        }
        // start starDash
        if (prevDash && character == '/') {
            dashDash = true;
            prevDash = false;
            return;
        }
        // start starDash
        if (prevDash && character == '*') {
            dashStar = true;
            prevDash = false;
            return;
        }
        // end starDash
        if (prevStar && character == '/') {
            dashStar = false;
            prevStar = false;
            return;
        }
        // singal occurence of '*'
        if (character == '*') {
            prevStar = true;
            return;
        }
        // continue starDash
        if (dashStar) {
            prevStar = false;
            return;
        }
        // signal occurence of '/'
        if (character == '/') {
            prevDash = true;
            return;
        }
        // it was divide symbol!
        if (prevDash && character != '/') {
            prevDash = false;
            tokenArray.add(new Token(tokenType("/"), "/"));
        }
        // it was times symbol!
        if (prevStar && character != '*') {
            prevStar = false;
            tokenArray.add(new Token(tokenType("*"), "*"));
        }


        if ((int)'\"' == (int)character) { // handle stringConstant
            
            if (stringConstant == false) { // open stringConsant buffer
                stringConstant = true;
                buffer = "";
            }
            else {
                tokenArray.add(new Token(tokenType(buffer), buffer));

                stringConstant = false;
                buffer = "";
            }
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
        if (integerConstant == true) { // close integerConstant buffer
            tokenArray.add(new Token(tokenType(buffer), buffer));
            buffer = "";
            integerConstant = false; 
        }


        if (character.equals(' ') || character.equals('\n')) { // ignore whitespace and enters
            if (identifierKeyword == true) {
                tokenArray.add(new Token(tokenType(buffer), buffer));
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
                tokenArray.add(new Token(tokenType(buffer), buffer));
                buffer = "";
                identifierKeyword = false;
            }
            buffer = "";
            String sCharacter = character.toString(); // simultaneously write symbol
            tokenArray.add(new Token(tokenType(sCharacter), sCharacter));
        }
        return;
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


    private boolean isIdentifier(int intObj) {
        char c = (char)(intObj);
        return Character.isLetter(c) || (c == '_');
    }
};