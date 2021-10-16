package com.example;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


class JackTokenizer {
    private String buffer = "";
    private boolean stringConstant = false; // helps checking for string constant sequence
    private boolean integerConstant;
    private boolean keyword;
    private boolean symbol;
    private Set<String> keywords = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("class", "constructor", "function", "method", "field", "static",
                                    "var", "int", "char", "boolean", "void", "true", "false", "null", "this",
                                    "let", "do", "if", "else", "while", "return")));
    private Set<Character> symbols = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList('{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-', '*', '/', '&', '|','<', '>', '=', '~')));
    

    // on creation ignore all comments and whitespace in the input stream, 
    // and serialize it into Jack-language tokens. 
    // The token types are specified according to Jack grammar.
    public JackTokenizer(File file) throws IOException {        
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // process the line
                line = line.split("[/]")[0];
                if (!(line.equals(""))) {
                    String[] removedEntersTabsSpaces = line.split("[\s]");
                    for (String implicitToken : removedEntersTabsSpaces) {
                        if (!(implicitToken.equals(""))) {
                            Character[] charObjectArray = 
                                implicitToken.chars().mapToObj(c -> (char)c).toArray(Character[]::new); 
                            advance(charObjectArray);
                        }
                    }
                }
            }
        }
        catch(IOException ie) {
            ie.printStackTrace();
        }   
    }
    private boolean hasMoreTokens() { // are there more tokens in the file?
        return true;
    }
    private void advance(Character[] spelledToken) { // gets the next token from the input,
                             // and makes it the current token.
                             // This method should be called only if
                             // hasMoreTokens is true.
                             // Initially there is no current token.
        // while chars are same lexical elements:
        // add to string 
        for (Character s: spelledToken) { // TODO implement handling integerConstant
            if (stringConstant == true && !((int)'\"' == (int)s)) {
                buffer += s;
            }
            else if (Character.isLetter(s) || s.equals('_')) { // handle identifiers/keywords
                buffer += Character.toString(s);
            }
            else if (symbols.contains(s) && !(s.equals('/'))) { // handle symbols and comments
                if (!(buffer.equals(""))) {
                    writeXML(tokenType(buffer), buffer);
                }
                writeXML(TOKEN_TYPE.SYMBOL, s.toString());
                buffer = "";
            }
            else if ((int)'\"' == (int)s) { // handle stringConstant
                if (stringConstant == false) {
                    stringConstant = true;
                    buffer = "";
                }
                else {
                    stringConstant = false;
                }
            }
            else if (Character.isDigit(s)) {
                integerConstant= true;
                buffer += s;
            }
        }
        if (!(buffer.equals("")) && integerConstant == true) {
            writeXML(tokenType(buffer), buffer);
            buffer = "";
        }
        else if (!(buffer.equals("")) && stringConstant == true) { // if it's the end of the word 
            buffer += " ";                             // add space to buffer
        }
        else if (!(buffer.equals("")) && stringConstant == false) {
            writeXML(tokenType(buffer), buffer + " ");
            buffer = "";
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
        if (symbols.contains(token)) {
            return TOKEN_TYPE.SYMBOL;
        }
        if (token.chars().allMatch( Character::isDigit)) {
            Integer number = Integer.valueOf(token);
            if (number > 0 && number < 32767) {
                return TOKEN_TYPE.INT_CONST;
            }
            else {
                return TOKEN_TYPE.ERROR;
            }
        }
        if (token.chars().allMatch(c -> isIdentifier(c))) {
            return TOKEN_TYPE.IDENTIFIER;
        }
        return TOKEN_TYPE.SYMBOL;
    }

    private enum KEYWORD {
        CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR, VOID, VAR, STATIC, FIELD, LET, DO, IF, ELSE, WHILE, RETURN, TRUE, FALSE, NULL, THIS
    }
    private KEYWORD keyWord() { // Returns the keyword which is the current token, 
                                // as a constant.
                                // This method should be called only if tokenType is KEYWORD
        return KEYWORD.CLASS;
    }

    private char symbol() { // Returns the character which is the
                            // current token. Should be called only
                            // if tokenType is SYMBOL.
        return 'a';
    }
    private String identifier() { // Returns the identifier which is the
                                  // current token. Should be called only
                                  // if tokenType is IDENTIFIER.
        return "a";
    }
    private boolean isIdentifier(int intObj) {
        char c = (char)(intObj);
        return Character.isLetter(c) || (c == '_');
    }
    private int intVal() { // Returns the integer value of the
                           // current token. Should be called only
                           // if tokenType is INT_CONST.
        return 1;
    }
    private String stringVal() { // Returns the string value of the
                                 // current token. without the two
                                 // enclosing double quotes. Should be
                                 // called only if tokenType is
                                 // STRING_CONST.
        return "a";
    }
    private void writeXML(TOKEN_TYPE tp, String buffer) {
        System.out.println(tp + " - " + buffer);
    }
};