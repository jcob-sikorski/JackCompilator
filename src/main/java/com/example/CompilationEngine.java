package com.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;


class CompilationEngine { // each nested function call adds new tab to buffer, with end of function tab is deleted
    private ArrayList<Token> tokenArray;
    private ArrayList<LexicalElement> finalArray = new ArrayList<LexicalElement>();
    private int index = 0;

    private Set<String> op = new HashSet<String>() {{
        add("+"); add("-"); add("*"); add("/"); add("&"); add("|"); add("<"); add(">"); add("="); add("~");
    }};

    // prepares file to be written
    public CompilationEngine(ArrayList<Token> tokenArray) throws IOException, TransformerException, ParserConfigurationException, SAXException {  
        this.tokenArray = tokenArray;
        CompileClass();
    };

    private void CompileClass() { // problems start from index 143 (from token & in if statement in incSize())
        finalArray.add(new LexicalElement("class", ""));

        finalArray.add(getToken(index)); // class
        finalArray.add(getToken(index+1)); // className
        finalArray.add(getToken(index+2)); // {
        index += 3;
        CompileClassVarDec();
        while (!getToken(index).token().equals("}")) {
            CompileSubroutineDec();
        }
        finalArray.add(getToken(index)); // }
    }


    private void CompileClassVarDec() {
        while (tokenArray.get(index).token().equals("static") || tokenArray.get(index).token().equals("field")) {
            finalArray.add(new LexicalElement("classVarDec", ""));

            finalArray.add(getToken(index)); // static | field
            finalArray.add(getToken(index+1)); // type
            finalArray.add(getToken(index+2)); // varName
            index += 3;
            while (getToken(index).token().equals(",")) { // (',' varName)*
                finalArray.add(getToken(index));
                finalArray.add(getToken(index+1));
                index += 2;
            }
            finalArray.add(getToken(index)); // ;
            index += 1;
        }
    }


    private void CompileSubroutineDec() {
        if (tokenArray.get(index).token().equals("constructor") || tokenArray.get(index).token().equals("function") || tokenArray.get(index).token().equals("method")) {
            finalArray.add(new LexicalElement("subroutineDec", ""));

            finalArray.add(getToken(index)); // constructor | function | method
            finalArray.add(getToken(index+1)); // 'void' | type
            finalArray.add(getToken(index+2)); // subroutineName
            finalArray.add(getToken(index+3)); // (
            index += 4;
            compileParameterList();
            finalArray.add(getToken(index)); // )
            index += 1;
            compileSubroutineBody();
        }
    }


    private void compileParameterList() {
        finalArray.add(new LexicalElement("parameterList", ""));
        while (!getToken(index).token().equals(")")) {
            finalArray.add(getToken(index)); // type
            finalArray.add(getToken(index+1)); // varName
            index += 2;
            if (!getToken(index).token().equals(",")) {
                break;
            }
            else {
                finalArray.add(getToken(index)); // ,
                index += 1;
            }
        }
    }


    private void compileSubroutineBody() {
        finalArray.add(new LexicalElement("subroutineBody", ""));
        finalArray.add(getToken(index)); // {
        index += 1;
        compileVarDec();
        finalArray.add(new LexicalElement("statements", ""));
        compileStatements();
        finalArray.add(getToken(index)); // }
        index += 1;
    }


    private void compileVarDec() {
        while (tokenArray.get(index).token().equals("var")) {
            finalArray.add(new LexicalElement("varDec", ""));

            finalArray.add(getToken(index)); // var
            finalArray.add(getToken(index+1)); // type
            finalArray.add(getToken(index+2)); // varName
            index += 3;
            while (!getToken(index).token().equals(";")) {
                finalArray.add(getToken(index)); // type
                finalArray.add(getToken(index+1)); // varName
                index += 2;
                if (!getToken(index).token().equals(",")) {
                    break;
                }
                else {
                    finalArray.add(getToken(index)); // ,
                    index += 1;
                }
            }
            finalArray.add(getToken(index)); // ;
            index += 1;
        }
    }


    private void compileStatements() {
        switch (tokenArray.get(index).token()) { // serializer doesn't know about when the body of xml exists, we have to add it manually in compilation engine
            case "if":
                compileIf();
                compileStatements();
                break;
            case "let":
                compileLet();
                compileStatements();
                break;
            case "while":
                compileWhile();
                compileStatements();
                break;
            case "do":
                compileDo();
                compileStatements();
                break;
            case "return":
                compileReturn();
                break;
            default:
                return;
        }
    }


    private void compileLet() {
        finalArray.add(new LexicalElement("letStatement", ""));

        finalArray.add(getToken(index)); // let
        index += 1;
        if (getToken(index).token().equals("[")) {
            finalArray.add(getToken(index)); // [
            compileExpression();
            finalArray.add(getToken(index)); // ]
        }
        finalArray.add(getToken(index)); // varName
        finalArray.add(getToken(index+1)); // =
        index += 2;
        compileExpression();
        finalArray.add(getToken(index)); // ;
        index += 1;
    }


    private void compileIf() {
        finalArray.add(new LexicalElement("ifStatement", ""));
        finalArray.add(getToken(index)); // if
        finalArray.add(getToken(index+1)); // (
        index += 2;
        compileExpression();
        finalArray.add(getToken(index)); // )
        finalArray.add(getToken(index+1)); // {
        index += 2;
        finalArray.add(new LexicalElement("statements", ""));
        compileStatements();
        finalArray.add(getToken(index)); // }
        index += 1;
        if (getToken(index).equals("else")) {
            finalArray.add(getToken(index)); // else
            finalArray.add(getToken(index+1)); // {
            index += 2;
            finalArray.add(new LexicalElement("statements", ""));
            compileStatements();
            finalArray.add(getToken(index)); // }
            index += 1;
        }

    }


    private void compileWhile() {
        finalArray.add(new LexicalElement("whileStatement", ""));

        finalArray.add(getToken(index)); // while
        finalArray.add(getToken(index+1)); // (
        index += 2;
        compileExpression();
        finalArray.add(getToken(index)); // )
        finalArray.add(getToken(index+1)); // {
        index += 2;
        finalArray.add(new LexicalElement("statements", ""));
        compileStatements();
        finalArray.add(getToken(index)); // }
        index += 1;
    }


    private void compileDo() {
        finalArray.add(new LexicalElement("doStatement", ""));

        finalArray.add(getToken(index)); // do
        index += 1;
        compileTerm();
        finalArray.add(getToken(index)); // ;
        index += 1;
    }


    private void compileReturn() {
        finalArray.add(new LexicalElement("returnStatement", ""));

        finalArray.add(getToken(index)); // return
        index += 1;
        if (!getToken(index).token().equals(";")) {
            compileExpression();
        }
        finalArray.add(getToken(index)); // ;
        index += 1;
    }

    private void compileExpression() {
        finalArray.add(new LexicalElement("expression", ""));
        compileTerm();
        while (op.contains(getToken(index).token())) {
            finalArray.add(new LexicalElement("term", ""));
            finalArray.add(getToken(index)); // op 
            index += 1; 
            compileTerm();
        }
    }

    private void compileTerm() {
        if (getToken(index).tokenType().equals(TOKEN_TYPE.INT_CONST) || getToken(index).tokenType().equals(TOKEN_TYPE.STRING_CONST) || getToken(index).tokenType().equals(TOKEN_TYPE.KEYWORD)) {
            finalArray.add(new LexicalElement("term", ""));
            finalArray.add(getToken(index)); // integerConsant | stringConstant | keywordConstant
            index += 1;
        }
        else if (getToken(index).token().equals("(")) {
            finalArray.add(new LexicalElement("term", ""));
            finalArray.add(getToken(index)); // (
            index += 1;
            compileExpression();
            finalArray.add(getToken(index)); // )
            index += 1;
        }
        else if (op.contains(getToken(index).token())) {
            finalArray.add(new LexicalElement("term", "")); // unaryOp
            finalArray.add(getToken(index));
            index += 1;
            compileTerm();
        }
        else if (getToken(index).tokenType().equals(TOKEN_TYPE.IDENTIFIER)) {
            if (getToken(index+1).token().equals("[")) {
                finalArray.add(new LexicalElement("term", ""));
                finalArray.add(getToken(index)); // varName
                finalArray.add(getToken(index+1)); // [
                index += 2;
                compileExpression();
                finalArray.add(getToken(index)); // ]
                index += 1;
            }
            else if (getToken(index+1).token().equals(".")) {
                finalArray.add(getToken(index)); // className | varName
                finalArray.add(getToken(index+1)); // .
                finalArray.add(getToken(index+2)); // subroutineName
                finalArray.add(getToken(index+3)); // (
                index += 4;
                compileExpressionList();
                finalArray.add(getToken(index));
                index += 1;
            }
            else if (getToken(index+1).token().equals("(")) {
                finalArray.add(getToken(index)); // subroutineName
                finalArray.add(getToken(index+1)); // (
                index += 2;
                compileExpressionList();
                finalArray.add(getToken(index)); // )
                index += 1;
            }
            else {
                finalArray.add(new LexicalElement("term", ""));
                finalArray.add(getToken(index)); // varName
                index += 1;
            }
        }
        else if (op.contains(getToken(index).token())) {
            finalArray.add(new LexicalElement("term", ""));
            finalArray.add(getToken(index)); // op
            index += 1;
            compileTerm();
        }
        else if (getToken(index).token().equals("true") || getToken(index).token().equals("false") || getToken(index).token().equals("null") || getToken(index).token().equals("this")) {
            finalArray.add(new LexicalElement("term", ""));
            finalArray.add(getToken(index));
            index += 1;
        }
    }

    private void compileExpressionList() {
        finalArray.add(new LexicalElement("expressionList", ""));
        while (!(getToken(index).token().equals(")"))) {
            compileExpression();
            if (getToken(index).token().equals(",")) {
                finalArray.add(getToken(index));
                index += 1;
            }
        }
    }

    private LexicalElement getToken(int index) {
        return new LexicalElement(tokenArray.get(index).tokenType(), tokenArray.get(index).token());
    }

    public ArrayList<LexicalElement> getOutput() {
        return finalArray;
    }
};