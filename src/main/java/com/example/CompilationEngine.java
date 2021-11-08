package com.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


class CompilationEngine { // TODO use VMWriter and symbolTable to write .vm
    private File file;

    private Document document;

    private ArrayList<Token> tokenArray;
    private int index = 0;
    private boolean letStatementBool = false;

    private Set<String> op = new HashSet<String>() {{
        add("+"); add("-"); add("*"); add("/"); add("&"); add("|"); add("<"); add(">"); add("="); add("~");
    }};

    // prepares file to be written
    public CompilationEngine(File file, ArrayList<Token> tokenArray) throws IOException, TransformerException, ParserConfigurationException, SAXException {  
        // copy name of parsed file and 
        // set extension of written file to.xml
        int indexOfDot = file.getName().lastIndexOf('.');
        String name = file.getName().substring(0, indexOfDot);
        this.file = new File(file.getParent(), name + "EngineGenerated.xml"); // generates new file with previous name modified

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
        TransformerFactory tf = TransformerFactory.newInstance();  
        Transformer t = tf.newTransformer();
        t.setOutputProperty("omit-xml-declaration", "yes"); 
        this.tokenArray = tokenArray;

        Element root = this.document.createElement("class"); // create root of document

        CompileClass(root);

        t.transform(new DOMSource(root), new StreamResult(this.file)); // write XML file
    };

    private void CompileClass(Element root) {
        addChild(root, index); // class
        addChild(root, index+1); // className
        addChild(root, index+2); // {

        index += 3;
        CompileClassVarDec(root);
        while (!getToken(index).token().equals("}")) { // while its not the end of class
            CompileSubroutineDec(root);                // compile body of class
        }
        addChild(root, index); // }
        index += 1;
    }


    private void CompileClassVarDec(Element root) {
        while (tokenArray.get(index).token().equals("static") || 
               tokenArray.get(index).token().equals("field")) 
        {
            addSubroot(root, "classVarDec");
            Element classVarDec = getDirectChild(root, "classVarDec");
            
            addChild(classVarDec, index); // static | field
            addChild(classVarDec, index+1); // type
            addChild(classVarDec, index+2); // varName
            index += 3;

            while (getToken(index).token().equals(",")) { // (',' varName)*
                addChild(classVarDec, index); // ,
                addChild(classVarDec, index+1); // varName
                index += 2;
            }
            addChild(classVarDec, index); // ;
            index += 1;
        }
    }


    private void CompileSubroutineDec(Element root) {
        if (tokenArray.get(index).token().equals("constructor") ||
            tokenArray.get(index).token().equals("function") ||
            tokenArray.get(index).token().equals("method")) 
        {
            addSubroot(root, "subroutineDec");
            Element subroutineDec = getDirectChild(root, "subroutineDec");
            

            addChild(subroutineDec, index); // constructor | function | method
            addChild(subroutineDec, index+1); // 'void' | type
            addChild(subroutineDec, index+2); // subroutineName
            addChild(subroutineDec, index+3); // (
            index += 4;

            compileParameterList(subroutineDec);

            addChild(subroutineDec, index); // )
            index += 1;

            compileSubroutineBody(subroutineDec);
        }
    }


    private void compileParameterList(Element subroutineDec) {
        addSubroot(subroutineDec, "parameterList");
        Element parameterList = getDirectChild(subroutineDec, "parameterList");
        
        while (!getToken(index).token().equals(")")) {
            addChild(parameterList, index); // type
            addChild(parameterList, index+1); // varName
            index += 2;

            if (!getToken(index).token().equals(",")) {
                break;
            }
            else {
                addChild(parameterList, index); // ,
                index += 1;
            }
        }
    }


    private void compileSubroutineBody(Element subroutineDec) {
        addSubroot(subroutineDec, "subroutineBody");
        Element subroutineBody = getDirectChild(subroutineDec, "subroutineBody");
        
        addChild(subroutineBody, index); // {
        index += 1;

        compileVarDec(subroutineBody);

        addSubroot(subroutineBody, "statements");
        Element statements = getDirectChild(subroutineBody, "statements");

        compileStatements(statements);

        addChild(subroutineBody, index); // }
        index += 1;
    }


    private void compileVarDec(Element root) {
        while (tokenArray.get(index).token().equals("var")) {
            addSubroot(root, "varDec");
            Element varDec = getDirectChild(root, "varDec");
                
            addChild(varDec, index); // var       
            addChild(varDec, index+1); // type    
            addChild(varDec, index+2); // varName 
            index += 3;
    
            while (!getToken(index).token().equals(";")) {
                if (!getToken(index).token().equals(",")) {
                    break;
                }
                addChild(varDec, index); // ,
                addChild(varDec, index+1); // varName
                index += 2;
            }
            addChild(varDec, index); // ;
            index += 1;
        }
    }


    private void compileStatements(Element statements) {
        switch (tokenArray.get(index).token()) { // serializer doesn't know about when the body of xml exists, we have to add it manually in compilation engine
            case "if":
                
                compileIf(statements);
                compileStatements(statements);
                break;

            case "let":
                
                compileLet(statements);
                compileStatements(statements);
                break;

            case "while":
                
                compileWhile(statements);
                compileStatements(statements);
                break;

            case "do":
                
                compileDo(statements);
                compileStatements(statements);
                break;

            case "return":
                
                compileReturn(statements);
                break;

            default:
                return;
        }
    }


    private void compileLet(Element statements) {
        addSubroot(statements, "letStatement");
        Element letStatement = getDirectChild(statements, "letStatement");

        addChild(letStatement, index); // let
        addChild(letStatement, index+1); // varName
        index += 2;

        if (getToken(index).token().equals("[")) {
            addChild(letStatement, index); // [
            index += 1;

            compileExpression(letStatement);
            addChild(letStatement, index); // ]
            index += 1;
        }

        addChild(letStatement, index); // =
        index += 1;

        letStatementBool = true;
        compileExpression(letStatement);
        letStatementBool = false;
        addChild(letStatement, index); // ;
        index += 1;
    }


    private void compileIf(Element statements) {

        addSubroot(statements, "ifStatement");
        Element ifStatement = getDirectChild(statements, "ifStatement");
        
        addChild(ifStatement, index); // if
        addChild(ifStatement, index+1); // (
        index += 2;

        compileExpression(ifStatement);

        addChild(ifStatement, index); // )
        addChild(ifStatement, index+1); // {
        index += 2;

        addSubroot(ifStatement, "statements");
        Element statements1 = getDirectChild(ifStatement, "statements");
        
        compileStatements(statements1);
        
        addChild(ifStatement, index); // }
        index += 1;

        if (getToken(index).token().equals("else")) {
            addChild(ifStatement, index); // else
            addChild(ifStatement, index+1); // {
            index += 2;

            addSubroot(ifStatement, "statements");
            Element statements2 = getDirectChild(ifStatement, "statements");
            
            compileStatements(statements2);

            addChild(ifStatement, index); // }
            index += 1;
        }

    }


    private void compileWhile(Element statements) {

        addSubroot(statements, "whileStatement");
        Element whileStatement = getDirectChild(statements, "whileStatement");

        addChild(whileStatement, index); // while
        addChild(whileStatement, index+1); // (
        index += 2;

        
        compileExpression(whileStatement);

        addChild(whileStatement, index); // )
        addChild(whileStatement, index+1); // {
        index += 2;

        addSubroot(whileStatement, "statements");
        Element statements1 = getDirectChild(whileStatement, "statements");

        compileStatements(statements1);

        addChild(whileStatement, index); // }
        index += 1;
    }


    private void compileDo(Element statements) {
        addSubroot(statements, "doStatement");
        Element doStatement = getDirectChild(statements, "doStatement");

        addChild(doStatement, index); // do
        index += 1;


        compileTerm(doStatement);

        addChild(doStatement, index); // ;
        index += 1;
    }


    private void compileReturn(Element statements) {
        addSubroot(statements, "returnStatement");
        Element returnStatement = getDirectChild(statements, "returnStatement");

        addChild(returnStatement, index); // return
        index += 1;

        if (!getToken(index).token().equals(";")) {
            
            compileExpression(returnStatement);
        }
        addChild(returnStatement, index); // ;
        index += 1;
    }

    private void compileExpression(Element root) {
        addSubroot(root, "expression");
        Element expression = getDirectChild(root, "expression");

        compileTerm(expression);

        while (op.contains(getToken(index).token())) {
            addChild(expression, index); // op
            index += 1;

            compileTerm(expression);
        }
    }

    private void compileTerm(Element root) {
        if (getToken(index).tokenType().equals("integerConstant") || 
            getToken(index).tokenType().equals("stringConstant") || 
            getToken(index).tokenType().equals("keyword")) 
            {

            addSubroot(root, "term");
            Element term = getDirectChild(root, "term");

            addChild(term, index); // integerConsant | stringConstant | keywordConstant
            index += 1;
        }
        else if (getToken(index).token().equals("(")) {

            addSubroot(root, "term");
            Element term = getDirectChild(root, "term");

            addChild(term, index); // (
            index += 1;

            compileExpression(term);

            addChild(term, index); // )
            index += 1;
        }
        else if (op.contains(getToken(index).token())) {

            addSubroot(root, "term");
            Element term = getDirectChild(root, "term");

            addChild(term, index); // unaryOp
            index += 1;

            compileTerm(term);
        }
        else if (getToken(index).tokenType().equals("identifier")) {
            if (getToken(index+1).token().equals("[")) {

                addSubroot(root, "term");
                Element term = getDirectChild(root, "term");

                addChild(term, index); // varName
                addChild(term, index+1); // [
                index += 2;

                compileExpression(term);

                addChild(term, index); // ]
                index += 1;
            }
            else if (getToken(index+1).token().equals(".")) {
                
                Element termRoot = root;
                if (letStatementBool == true) {
                    addSubroot(root, "term");
                    termRoot = getDirectChild(root, "term");
                }

                addChild(termRoot, index); // className | varName
                addChild(termRoot, index+1); // .
                addChild(termRoot, index+2); // subroutineName
                addChild(termRoot, index+3); // (
                index += 4;

                compileExpressionList(termRoot);

                addChild(termRoot, index); // )
                index += 1;
                
            }
            else if (getToken(index+1).token().equals("(")) {
                addChild(root, index); // subroutineName
                addChild(root, index+1); // (
                index += 2;

                compileExpressionList(root);
                
                addChild(root, index); // )
                index += 1;
            }
            else {
                addSubroot(root, "term");
                Element term = getDirectChild(root, "term");

                addChild(term, index); // varName
                index += 1;
            }
        }
        else if (op.contains(getToken(index).token())) {

            addSubroot(root, "term");
            Element term = getDirectChild(root, "term");
            addChild(term, index); // op
            index += 1;

            compileTerm(term);
        }
        else if (getToken(index).token().equals("true") || 
                 getToken(index).token().equals("false") || 
                 getToken(index).token().equals("null") || 
                 getToken(index).token().equals("this")) 
        {

            addSubroot(root, "term");
            Element term = getDirectChild(root, "term");

            addChild(term, index); // true | false | null | this
            index += 1;
        }
    }

    private void compileExpressionList(Element root) {
        addSubroot(root, "expressionList");
        Element expressionList = getDirectChild(root, "expressionList");

        while (!(getToken(index).token().equals(")"))) { // (expression, (',' expression)*)?
            compileExpression(expressionList);
            
            if (getToken(index).token().equals(",")) {
                addChild(expressionList, index);
                index += 1;
            }
        }
    }

    private LexicalElement getToken(int index) {
        return new LexicalElement(tokenArray.get(index).tokenType(), tokenArray.get(index).token());
    }

    private void addChild(Element parent, int index) {
        Element child = document.createElement(getToken(index).tokenType()); // create <tp> </tp>

        // String description = " "+getToken(index).token()+" ";
        String description = getToken(index).token();

        System.out.println(description);

        child.appendChild(document.createTextNode(description)); // add to it text -> <tp>text</tp>
        parent.appendChild(child); // add token to parent
    }

    private void addSubroot(Element parent, String description) {
        Element subRoot = document.createElement(description); // create <tp> </tp>
        parent.appendChild(subRoot); // add token to root (tokens)
    }

    private static Element getDirectChild(Element parent, String tagName) { // gets last created child by tagName
        for(Node child = parent.getLastChild(); child != null; child = child.getParentNode()) {
            if (child instanceof Element && tagName.equals(child.getNodeName())) {
                return (Element) child;
            }
        }
        return null;
    }
};