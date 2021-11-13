package com.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;
import java.io.File;


class CompilationEngine {
    private ArrayList<LexicalElement> tokenArray = new ArrayList<LexicalElement>();
    private Integer index = 0;

    private Integer labelNumber = 0;

    private String className;
    private Integer nExpr;
    private Integer nVars;
    private String subroutine;
    private String subroutineType;

    private SymbolTable classSymbolTable = new SymbolTable();
    private SymbolTable subroutineSymbolTable = new SymbolTable();

    private Deque<String> opStack = new LinkedList<String>();

    private Deque<Integer> nOperatorsToPop = new LinkedList<Integer>();

    private VMWriter vmWriter;

    private Set<String> op = new HashSet<String>() {{
        add("+"); add("-"); add("*"); add("/"); add("&"); add("|"); add("<"); add(">"); add("="); add("~");
    }};

    private HashMap<String, String> opFun = new HashMap<String, String>() {{ // (!) except division and multiply
        put("+", "add"); put("-", "sub"); put("&", "and"); put("|", "or"); put("<", "lt"); put(">", "gt"); put("=", "eq"); put("~", "neg");
    }};

    // prepares file to be written
    public CompilationEngine(File file, ArrayList<Token> tokenArray) throws IOException, TransformerException, ParserConfigurationException, SAXException { 
        int indexOfDot = file.getName().lastIndexOf('.');
        String name = file.getName().substring(0, indexOfDot);
        vmWriter = new VMWriter(file.getParent() + "/" + name + "EngineGenerated.vm");

        for (Token token : tokenArray) {
            LexicalElement lexicalElement = new LexicalElement(token.tokenType(), token.token());
            this.tokenArray.add(lexicalElement);
        }

        CompileClass();

        vmWriter.close();
    };

    private void CompileClass() throws IOException {
                                                     // class
        className = tokenArray.get(index+1).token(); // className
                                                     // {
        index += 3;

        classSymbolTable.reset();
        
        CompileClassVarDec();
        while (!tokenArray.get(index).token().equals("}")) { // while its not the end of class
            CompileSubroutineDec();                          // compile body of class
        }
                    // }
        index += 1;
    }


    private void CompileClassVarDec() {
        while (tokenArray.get(index).token().equals("static") || 
               tokenArray.get(index).token().equals("field")) 
        {
            String kind = tokenArray.get(index).token();   // static | field
            String type = tokenArray.get(index+1).token(); // type
            String name = tokenArray.get(index+2).token(); // varName
            classSymbolTable.put(name, type, kind);
            index += 3;


            while (tokenArray.get(index).token().equals(",")) { // (',' varName)*
                                                                // ,
                name = tokenArray.get(index+1).token();         // varName
                classSymbolTable.put(name, type, kind);
                index += 2;
            }
                        // ;
            index += 1;
        }
    }


    private void CompileSubroutineDec() throws IOException { // TODO write edge case for constructor
        if (tokenArray.get(index).token().equals("constructor") ||
            tokenArray.get(index).token().equals("function") ||
            tokenArray.get(index).token().equals("method")) 
        {
            subroutineSymbolTable.reset();
            nVars = 0;

            subroutine = tokenArray.get(index).token();       // constructor | function | method
            subroutineType = tokenArray.get(index+1).token();        // 'void' | type
            String subroutineName = tokenArray.get(index+2).token(); // subroutineName
                        // (
            index += 4;

            compileParameterList();
                        // )

                        // {
            index += 2;
            compileVarDec();

            vmWriter.writeFunction(className + "." + subroutineName, nVars);

            if (subroutine.equals("method")) {
                vmWriter.writePush(SEGMENT.ARGUMENT, 0);
                vmWriter.writePop(SEGMENT.POINTER, 0); // THIS = argument 0 (for methods)
            }

            compileStatements();
                        // }
            index += 1;
        }
    }


    private void compileParameterList() {
        if (!tokenArray.get(index).token().equals(")")) {
            String type = tokenArray.get(index).token();   // type
            String name = tokenArray.get(index+1).token(); // varName

            if (subroutine.equals("method")) {
                subroutineSymbolTable.put("this", type, "ARGUMENT"); // THIS = argument 0 (for methods)
            }
            subroutineSymbolTable.put(name, type, "ARGUMENT");

            index += 2;
        }

        while (!tokenArray.get(index).token().equals(")")) {
                        // ,
            index += 1;
            String type = tokenArray.get(index).token();   // type
            String name = tokenArray.get(index+1).token(); // varName
            
            subroutineSymbolTable.put(name, type, "ARGUMENT");

            index += 2;
        }
    }


    private void compileVarDec() {
        while (tokenArray.get(index).token().equals("var")) {    
            String kind = tokenArray.get(index).token();   // var
            if (kind.equals("var")) {
                kind = "LOCAL";
            }
            String type = tokenArray.get(index+1).token(); // type    
            String name = tokenArray.get(index+2).token(); // varName 
            subroutineSymbolTable.put(name, type, kind);
            index += 3;
            nVars++;

            while (!tokenArray.get(index).token().equals(";")) {
                if (!tokenArray.get(index).token().equals(",")) {
                    break;
                }
                            // ,
                name = tokenArray.get(index+1).token(); // varName 
                subroutineSymbolTable.put(name, type, kind);
                index += 2;
                nVars++;
            }
                        // ;
            index += 1;
        }
    }


    private void compileStatements() throws IOException {
        switch (tokenArray.get(index).token()) {
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


    private void compileLet() throws IOException {

                                                          // let
        String varName = tokenArray.get(index+1).token(); // varName
        index += 2;

        if (tokenArray.get(index).token().equals("[")) {
                        // [
            index += 1;

            compileExpression();
                        // ]
            index += 1;
        }
                    // =
        index += 1;

        compileExpression();
                    // ;
        index += 1;

        popVariable(varName);
    }


    private void compileIf() throws IOException {
                    // if
                    // (
        index += 2;

        compileExpression();

                    // )

        vmWriter.writeArithmetic("not");

        String l1 = "L" + String.valueOf(++labelNumber);

        vmWriter.writeIfGoto(l1);

                    // {
        index += 2;
        
        compileStatements();
        
                     // }
        index += 1;

        String l2 = "L" + String.valueOf(++labelNumber);

        vmWriter.writeGoto(l2);

        vmWriter.writeLabel(l1);

        if (tokenArray.get(index).token().equals("else")) {
                        // else
                        // {
            index += 2;
            
            compileStatements();

                        // }
            index += 1;
        }
        vmWriter.writeLabel(l2);
    }


    private void compileWhile() throws IOException {
        String l1 = "L" + String.valueOf(++labelNumber);

        vmWriter.writeLabel(l1);
                    // while
                    // (
        index += 2;

        
        compileExpression();

                    // )

        vmWriter.writeArithmetic("not");

        String l2 = "L" + String.valueOf(++labelNumber);
        vmWriter.writeIfGoto(l2);

                    // {
        index += 2;

        compileStatements();

                    // }
        index += 1;
        
        vmWriter.writeGoto(l1);

        vmWriter.writeLabel(l2);
    }


    private void compileDo() throws IOException {
                    // do
        index += 1;

        compileTerm();

                    // ;
        index += 1;

        vmWriter.writePop(SEGMENT.TEMP, 0);
    }


    private void compileReturn() throws IOException {
                    // return
        index += 1;

        if (!tokenArray.get(index).token().equals(";")) {
            compileExpression();
        }
                    // ;
        if (subroutineType.equals("void")) {
            // vmWriter.writePop(SEGMENT.TEMP, 0);
            vmWriter.writePush(SEGMENT.CONSTANT, 0);
        }
        vmWriter.writeReturn();
        index += 1;
    }

    private void compileExpression() throws IOException {

        nOperatorsToPop.add(0);

        compileTerm();  // write values in order
                        // add operands to stack

        Integer n = nOperatorsToPop.pollLast(); // pop n number of operands from operands stack

        while (n > 0) {
            String operator = opStack.pollLast();

            if (opFun.containsKey(operator)) {
                vmWriter.writeArithmetic(opFun.get(operator));
            }
            else if (operator.equals("*")) {
                vmWriter.writeCall("Math.multiply", 2);
            }
            else if (operator.equals("/")) {
                vmWriter.writeCall("Math.divide", 2);
            }
            n--;
        }
    }

    private void compileTerm() throws IOException { // TODO handle keyword "this"
        if (tokenArray.get(index).tokenType().equals("stringConstant")) {
            index += 1;
        }
        else if (tokenArray.get(index).tokenType().equals("integerConstant")) {
                vmWriter.writePush(SEGMENT.CONSTANT, Integer.parseInt(tokenArray.get(index).token()));
                index += 1;
                compileTerm(); // continue expression
        }
        else if (tokenArray.get(index).token().equals("true")) {
            vmWriter.writePush(SEGMENT.CONSTANT, 1);
            vmWriter.writeArithmetic("neg");
            index += 1;
        }
        else if (tokenArray.get(index).token().equals("false") ||
                tokenArray.get(index).token().equals("null"))
        {
            vmWriter.writePush(SEGMENT.CONSTANT, 0);
            index += 1;
        }
        else if (tokenArray.get(index).tokenType().equals("keyword")) {
            LexicalElement token = tokenArray.get(index);

            pushVariable(token.token());
            index += 1;

            compileTerm(); // continue expression
        }
        else if (tokenArray.get(index).token().equals("(")) {
                        // (
            index += 1;
            
            compileExpression();

                        // )
            index += 1;

            if (opFun.containsKey(tokenArray.get(index).token())) {
                compileTerm();
            }
        }
        // else if (op.contains(tokenArray.get(index).token())) { // shades other operations
        //                                                        // generating wrong result

        //                 // unaryOp           // TODO can't handle unaryOp now
        //     index += 1;

        //     compileTerm();
        // }
        else if (tokenArray.get(index).tokenType().equals("identifier")) {
            if (tokenArray.get(index+1).token().equals("[")) {

                            // varName
                            // [
                index += 2;

                compileExpression();

                            // ]
                index += 1;
            }
            else if (tokenArray.get(index+1).token().equals(".")) {
                String objectName = tokenArray.get(index).token();       // className | varName
                                                                         // .
                String subroutineName = tokenArray.get(index+2).token(); // subroutineName

                                                             // (
                index += 4;

                compileExpressionList();

                                                            // )
                index += 1;

                vmWriter.writeCall(objectName + "." + subroutineName, nExpr);
            }
            else if (tokenArray.get(index+1).token().equals("(")) {
                            // subroutineName
                            // (
                index += 2;

                compileExpressionList();
                
                            // )
                index += 1;
            }
            else {
                            // varName
                pushVariable(tokenArray.get(index).token());
                index += 1;

                compileTerm();
            }
        }
        else if (tokenArray.get(index).token().equals("~")) {
            index += 1;
            compileExpression();
            vmWriter.writeArithmetic("not");
        }
        else if (tokenArray.get(index).token().equals("-") &&
                 !tokenArray.get(index-1).tokenType().equals("integerConstant") &&
                 !tokenArray.get(index-1).tokenType().equals("identifier"))
        {
            index += 1;
            compileExpression();
            vmWriter.writeArithmetic("neg");
        } 
        else if (op.contains(tokenArray.get(index).token())) {
                        // op
            opStack.add(tokenArray.get(index).token());

            // update number of operands to pop in current scope
            Integer n = nOperatorsToPop.pollLast();
            nOperatorsToPop.add(n+1);
            index += 1;
                        // continue expression
            compileTerm();
        }
    }

    private void compileExpressionList() throws IOException {
        nExpr = 0;
        while (!(tokenArray.get(index).token().equals(")"))) { // (expression, (',' expression)*)?
            compileExpression();
            nExpr++;
            
            if (tokenArray.get(index).token().equals(",")) {
                            // ,
                index += 1;
            }
        }
    }

    private void pushVariable(String varName) throws IOException {
        VARIABLE_IDENTIFIER variableIdentifier;

        if (!subroutineSymbolTable.kindOf(varName).equals(VARIABLE_IDENTIFIER.NONE)) {
            variableIdentifier = subroutineSymbolTable.kindOf(varName);
        }
        else {
            variableIdentifier = classSymbolTable.kindOf(varName);
        }

        Integer indexOnRam = subroutineSymbolTable.indexOf(varName);

        if (variableIdentifier == VARIABLE_IDENTIFIER.FIELD) {
            vmWriter.writePush(SEGMENT.THIS, indexOnRam);
        }
        else {
            SEGMENT segment = SEGMENT.valueOf(variableIdentifier.toString());
            vmWriter.writePush(segment, indexOnRam);
        }
    }

    private void popVariable(String varName) throws IOException {
        VARIABLE_IDENTIFIER variableIdentifier;

        if (!subroutineSymbolTable.kindOf(varName).equals(VARIABLE_IDENTIFIER.NONE)) {
            variableIdentifier = subroutineSymbolTable.kindOf(varName);
        }
        else {
            variableIdentifier = classSymbolTable.kindOf(varName);
        }

        Integer indexOnRam = subroutineSymbolTable.indexOf(varName);

        if (variableIdentifier == VARIABLE_IDENTIFIER.FIELD) {
            vmWriter.writePop(SEGMENT.THIS, indexOnRam);
        }
        else {
            SEGMENT segment = SEGMENT.valueOf(variableIdentifier.toString());
            vmWriter.writePop(segment, indexOnRam);
        }
    }
};