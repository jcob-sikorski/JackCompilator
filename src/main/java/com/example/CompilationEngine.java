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


class CompilationEngine {
    String filename;

    private ArrayList<LexicalElement> tokenArray = new ArrayList<LexicalElement>();
    private Integer index = 0;

    // holds unique label number
    // used for generating unique labels in .vm file
    private Integer labelNumber = 0;

    // name of compiled file
    private String className;

    // number of expressions between parentheses of called function
    private Integer nExpr;

    // number of variables declared inside subroutine
    private Integer nVars;

    // constructor | method | function
    private String subroutine;

    // void | int | String...
    private String subroutineType;

    private SymbolTable classSymbolTable = new SymbolTable();
    private SymbolTable subroutineSymbolTable = new SymbolTable();

    private Set<String> methodCollection;

    private Deque<String> operatorStack = new LinkedList<String>();

    private Deque<Integer> nOperatorsToPop = new LinkedList<Integer>();

    private VMWriter vmWriter;

    private Set<String> operators = new HashSet<String>() {{
        add("+"); add("-"); add("*"); add("/"); add("&"); add("|"); add("<"); add(">"); add("="); add("~");
    }};

    private HashMap<String, String> operatorInOS = new HashMap<String, String>() {{ // (!) except division and multiply
        put("+", "add"); put("-", "sub"); put("&", "and"); put("|", "or"); put("<", "lt"); put(">", "gt"); put("=", "eq"); put("~", "neg");
    }};

    // compiles .jack files to .vm files
    public CompilationEngine(SerializedFile serializedFile, Set<String> methodCollection) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        this.filename = serializedFile.getFilename(); 
        this.methodCollection = methodCollection;

        vmWriter = new VMWriter(serializedFile.getFile().getParent() + "/" + filename + ".vm");

        this.tokenArray = serializedFile.getTokenArray();

        CompileClass();

        // save file
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

    // Jack features four kinds of variables. 
    //     Static variables are defined at the class level and can be accessed by all the class subroutines.    // TODO static vars can be accessed by all the class subroutines.
    //     Field variables, also defined at the class level, are used to represent the properties of individual // TODO Field variables can be accessed by all the class constructors and methods. 
    // objects and can be accessed by all the class constructors and methods. 
    //     Local variables are used by subroutines for local computations, and 
    //     parameter variables represent the arguments that were passed to the subroutine by the caller. 
    // Local and parameter values are created just before the subroutine starts executing and are 
    // recycled when the subroutine returns. 

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

    private void CompileSubroutineDec() throws IOException {
        if (tokenArray.get(index).token().equals("constructor") ||
            tokenArray.get(index).token().equals("function") ||
            tokenArray.get(index).token().equals("method")) 
        {
            subroutineSymbolTable.reset(); // new subroutineTable for each declared subroutine
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
            else if (subroutine.equals("constructor")) {
                vmWriter.writePush(SEGMENT.CONSTANT, classSymbolTable.size());
                vmWriter.writeCall("Memory.alloc", 1);
                vmWriter.writePop(SEGMENT.POINTER, 0); // return this object
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
                subroutineSymbolTable.put("this", type, "argument"); // THIS = argument 0 (for methods)
            }
            subroutineSymbolTable.put(name, type, "argument"); // record argument 1

            index += 2;
        }

        while (!tokenArray.get(index).token().equals(")")) {
                        // ,
            index += 1;
            String type = tokenArray.get(index).token();   // type
            String name = tokenArray.get(index+1).token(); // varName
            
            subroutineSymbolTable.put(name, type, "argument"); // record argument n

            index += 2;
        }
    }


    private void compileVarDec() {
        if (subroutine.equals("method")) {
            subroutineSymbolTable.put("this", className, "argument");
        }

        while (tokenArray.get(index).token().equals("var")) {    
            String kind = tokenArray.get(index).token();   // var
            if (kind.equals("var")) {
                kind = "local";
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

        boolean array = false;

        if (tokenArray.get(index).token().equals("[")) {
                pushVariable(varName);
                            // [
                index += 1;

                compileExpression();

                vmWriter.writeArithmetic("add"); // push target address
                            // ]
                index += 1;

                array = true;
        }
                    // =
        index += 1;

        compileExpression();
                    // ;
        index += 1;

        if (array == false) {
            popVariable(varName);
        }
        else {
            vmWriter.writePop(SEGMENT.TEMP, 0);    // save value of computed expression
            vmWriter.writePop(SEGMENT.POINTER, 1); // store target address in THAT pointer (RAM[4])
            vmWriter.writePush(SEGMENT.TEMP, 0);   // push value of computed expression
            vmWriter.writePop(SEGMENT.THAT, 0);    // save it onto target address
        }
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

        if (!tokenArray.get(index).token().equals(";") && !subroutine.equals("constructor")) {
            compileExpression();
        }
        if (subroutineType.equals("void")) {
            vmWriter.writePush(SEGMENT.CONSTANT, 0);
        }
        else if (subroutine.equals("constructor")) {
            vmWriter.writePush(SEGMENT.POINTER, 0); // return this
                        // this
            index += 1;
        }
        vmWriter.writeReturn();
                    // ;
        index += 1;
    }

    private void compileExpression() throws IOException {

        nOperatorsToPop.add(0);

        compileTerm();  // write values in order
                        // add operands to stack

        Integer n = nOperatorsToPop.pollLast(); // pop n operators from operands stack
                                                // n - number of operators in current subexpression

        while (n > 0) { // write n operators in reversed order
            String operator = operatorStack.pollLast();

            if (operatorInOS.containsKey(operator)) {                 // convert jack operator to OS function
                vmWriter.writeArithmetic(operatorInOS.get(operator)); //
            }                                                         //
            else if (operator.equals("*")) {                          //
                vmWriter.writeCall("Math.multiply", 2);               //
            }                                                         //
            else if (operator.equals("/")) {                          //
                vmWriter.writeCall("Math.divide", 2);                 //
            }                                                         //
            n--;
        }
    }

    private void compileTerm() throws IOException {
        if (tokenArray.get(index).tokenType().equals("stringConstant")) {
            String token = tokenArray.get(index).token();

            Integer length = token.length();
            vmWriter.writePush(SEGMENT.CONSTANT, length);

            vmWriter.writeCall("String.new", 1);

            for (Integer ASCII_VALUE : token.codePoints().toArray()) {
                vmWriter.writePush(SEGMENT.CONSTANT, ASCII_VALUE);
                vmWriter.writeCall("String.appendChar", 2);
            }
            index += 1;
        }
        else if (tokenArray.get(index).tokenType().equals("integerConstant")) {
                vmWriter.writePush(SEGMENT.CONSTANT, Integer.parseInt(tokenArray.get(index).token()));
                index += 1;
                compileTerm(); // continue expression
        }
        else if (tokenArray.get(index).token().equals("this")) {
            vmWriter.writePush(SEGMENT.POINTER, 0);
            index += 1;
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

            if (operators.contains(tokenArray.get(index).token())) {
                compileTerm();
            }
        }
        else if (tokenArray.get(index).tokenType().equals("identifier")) {
            if (tokenArray.get(index+1).token().equals("[")) {

                            // varName
                pushVariable(tokenArray.get(index).token());
                            // [
                index += 2;

                compileExpression();

                vmWriter.writeArithmetic("add"); // push target address

                            // ]
                index += 1;

                vmWriter.writePop(SEGMENT.POINTER, 1); // store target address in THAT pointer (RAM[4])
                vmWriter.writePush(SEGMENT.THAT, 0); // push value from target address

                if (operators.contains(tokenArray.get(index).token())) {
                    compileTerm();
                }
            }
            else if (tokenArray.get(index+1).token().equals(".")) {
                String objectName = tokenArray.get(index).token();       // className | varName

                if (!subroutineSymbolTable.kindOf(objectName).equals(VARIABLE_IDENTIFIER.NONE)) {
                    pushVariable(objectName);
                    objectName = subroutineSymbolTable.typeOf(objectName);
                }
                else if (!classSymbolTable.kindOf(objectName).equals(VARIABLE_IDENTIFIER.NONE)) {
                    pushVariable(objectName);
                    objectName = classSymbolTable.typeOf(objectName);
                }

                                                                         // .
                String subroutineName = tokenArray.get(index+2).token(); // subroutineName

                                                             // (
                index += 4;

                compileExpressionList();

                                                            // )
                index += 1;

                boolean isMethod = methodCollection.contains(objectName + "." + subroutineName);

                if (isMethod) { // if called method has 0 expressions
                    nExpr++;                // take in account argument 0
                }
                vmWriter.writeCall(objectName + "." + subroutineName, nExpr);
            }
            else if (tokenArray.get(index+1).token().equals("(")) {
                vmWriter.writePush(SEGMENT.POINTER, 0); // this

                String subroutineName = tokenArray.get(index).token(); // subroutineName
                                                                       // (
                index += 2;

                compileExpressionList();
                
                                                                       // )
                index += 1;

                boolean isMethod = methodCollection.contains(filename + "." + subroutineName);
                
                if (isMethod) { // if called method has 0 expressions
                    nExpr++;                // take in account argument 0
                }
                vmWriter.writeCall(className + "." + subroutineName, nExpr);
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
                 !tokenArray.get(index-1).token().equals(")") &&
                 !tokenArray.get(index-1).tokenType().equals("integerConstant") &&
                 !tokenArray.get(index-1).tokenType().equals("identifier"))
        {
            index += 1;
            compileExpression();
            vmWriter.writeArithmetic("neg");
        } 
        else if (operators.contains(tokenArray.get(index).token())) {
                        // operators
            operatorStack.add(tokenArray.get(index).token());

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
    
    // pushes declared variable onto .vm stack
    private void pushVariable(String varName) throws IOException {
        VARIABLE_IDENTIFIER variableIdentifier;
        Integer indexOnRam;

        if (!subroutineSymbolTable.kindOf(varName).equals(VARIABLE_IDENTIFIER.NONE)) {
            variableIdentifier = subroutineSymbolTable.kindOf(varName);
            indexOnRam = subroutineSymbolTable.indexOf(varName);
        }
        else {
            variableIdentifier = classSymbolTable.kindOf(varName);
            indexOnRam = classSymbolTable.indexOf(varName);
        }

        if (variableIdentifier.equals(VARIABLE_IDENTIFIER.FIELD)) {
            vmWriter.writePush(SEGMENT.THIS, indexOnRam);
        }
        else {
            SEGMENT segment = SEGMENT.valueOf(variableIdentifier.toString());
            vmWriter.writePush(segment, indexOnRam);
        }
    }

    // pops declared variable onto .vm stack
    private void popVariable(String varName) throws IOException {
        VARIABLE_IDENTIFIER variableIdentifier;
        Integer indexOnRam;

        if (!subroutineSymbolTable.kindOf(varName).equals(VARIABLE_IDENTIFIER.NONE)) {
            variableIdentifier = subroutineSymbolTable.kindOf(varName);
            indexOnRam = subroutineSymbolTable.indexOf(varName);
        }
        else {
            variableIdentifier = classSymbolTable.kindOf(varName);
            indexOnRam = classSymbolTable.indexOf(varName);
        }

        if (variableIdentifier.equals(VARIABLE_IDENTIFIER.FIELD)) {
            vmWriter.writePop(SEGMENT.THIS, indexOnRam);
        }
        else {
            SEGMENT segment = SEGMENT.valueOf(variableIdentifier.toString());
            vmWriter.writePop(segment, indexOnRam);
        }
    }
};