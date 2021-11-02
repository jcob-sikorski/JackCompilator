package com.example;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

// TODO do statement after index 124 is ignored
// bug in JackTokenizer

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


class CompilationEngine { // TODO after last child add tab
                          // considered function is intendTagEnd(parent)
    private File file;

    private Document document;

    private ArrayList<Token> tokenArray;
    private int index = 0;
    private int numberOfTabs = 0;
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
        this.file = new File(file.getParent(), name + "EngineGenerated.xml"); // generate new file with previous name modified

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
        TransformerFactory tf = TransformerFactory.newInstance();  
        Transformer t = tf.newTransformer();
        t.setOutputProperty("omit-xml-declaration", "yes"); 
        this.tokenArray = tokenArray;

        Element root = this.document.createElement("class"); // create root of document

        CompileClass(root);

        root.appendChild(this.document.createTextNode("\n")); // add tab to last node so </class> 
                                                                     // would be on next line

        t.transform(new DOMSource(root), new StreamResult(this.file)); // write XML file
    };

    private void CompileClass(Element root) { // TODO bug in jackTokenizer can't handle /* and /** comments
                                              //      debug SquareGame.jack (second interation of CompileClass)
        numberOfTabs++;
        
        addChild(root, index); // class
        addChild(root, index+1); // className
        addChild(root, index+2); // {

        index += 3;
        CompileClassVarDec(root);
        while (!getToken(index).token().equals("}")) {
            CompileSubroutineDec(root);
        }
        addChild(root, index); // }
        index += 1;

        numberOfTabs--;
    }


    private void CompileClassVarDec(Element root) { // TODO 
        while (tokenArray.get(index).token().equals("static") || 
               tokenArray.get(index).token().equals("field")) 
        {
            addSubroot(root, "classVarDec");
            Element classVarDec = getDirectChild(root, "classVarDec");
            numberOfTabs++;
            
            addChild(classVarDec, index); // static | field
            addChild(classVarDec, index+1); // type
            addChild(classVarDec, index+2); // varName
            index += 3;

            while (getToken(index).token().equals(",")) { // (',' varName)*
                addChild(classVarDec, index); // ,
                addChild(classVarDec, index+1); // varName
                index += 2;
            }
            addChild(classVarDec, index); // ; // TODO index or index+3?
            index += 1;

            numberOfTabs -= 2;
        }
    }


    private void CompileSubroutineDec(Element root) {
        if (tokenArray.get(index).token().equals("constructor") ||
            tokenArray.get(index).token().equals("function") ||
            tokenArray.get(index).token().equals("method")) 
        {
            numberOfTabs++;

            addSubroot(root, "subroutineDec");
            Element subroutineDec = getDirectChild(root, "subroutineDec");
            
            numberOfTabs++;

            addChild(subroutineDec, index); // constructor | function | method
            addChild(subroutineDec, index+1); // 'void' | type
            addChild(subroutineDec, index+2); // subroutineName
            addChild(subroutineDec, index+3); // (
            index += 4;

            compileParameterList(subroutineDec);
            addChild(subroutineDec, index); // )
            index += 1;

            compileSubroutineBody(subroutineDec);

            numberOfTabs -= 2;
        }
    }


    private void compileParameterList(Element subroutineDec) {
        addSubroot(subroutineDec, "parameterList");
        Element parameterList = getDirectChild(subroutineDec, "parameterList");
        
        while (!getToken(index).token().equals(")")) {
            numberOfTabs++;

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
            numberOfTabs--;
        }

        // numberOfTabs -= 2;
    }


    private void compileSubroutineBody(Element subroutineDec) {
        addSubroot(subroutineDec, "subroutineBody");
        Element subroutineBody = getDirectChild(subroutineDec, "subroutineBody");

        numberOfTabs++;
        
        addChild(subroutineBody, index); // {
        index += 1;

        compileVarDec(subroutineBody);

        addSubroot(subroutineBody, "statements");
        Element statements = getDirectChild(subroutineBody, "statements");

        compileStatements(statements);

        addChild(subroutineBody, index); // } // TODO
        index += 1;

        numberOfTabs--;
    }


    private void compileVarDec(Element root) { // TODO each new call generates new tag <classVarDec> </classVarDec>
        while (tokenArray.get(index).token().equals("var")) {
        // if (tokenArray.get(index).token().equals("var")) {
            numberOfTabs++;

            addSubroot(root, "varDec");
            Element varDec = getDirectChild(root, "varDec");
                
            numberOfTabs++;
                
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
    
            numberOfTabs -= 2;
            // compileVarDec(root);
        }
        // }
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
        numberOfTabs++;

        addSubroot(statements, "letStatement");
        Element letStatement = getDirectChild(statements, "letStatement");

        numberOfTabs++;

        addChild(letStatement, index); // let
        addChild(letStatement, index+1); // varName
        index += 2; // TODO index = 58

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

        numberOfTabs--;
    }


    private void compileIf(Element statements) {
        numberOfTabs++;

        addSubroot(statements, "ifStatement");
        Element ifStatement = getDirectChild(statements, "ifStatement");
        
        numberOfTabs++;
        
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

        numberOfTabs -= 2;

        if (getToken(index).token().equals("else")) {
            addChild(ifStatement, index); // else
            addChild(ifStatement, index+1); // {
            index += 2;

            numberOfTabs++;

            addSubroot(ifStatement, "statements");
            Element statements2 = getDirectChild(ifStatement, "statements");
            
            compileStatements(statements2);

            addChild(ifStatement, index); // }
            index += 1;

            numberOfTabs--;
        }

    }


    private void compileWhile(Element statements) {
        numberOfTabs++;

        addSubroot(statements, "whileStatement");
        Element whileStatement = getDirectChild(statements, "whileStatement");

        numberOfTabs++;

        addChild(whileStatement, index); // while
        addChild(whileStatement, index+1); // (
        index += 2;

        
        compileExpression(whileStatement);

        addChild(whileStatement, index); // )
        addChild(whileStatement, index+1); // {
        index += 2;

        addSubroot(statements, "statements");
        Element statements1 = getDirectChild(statements, "statements");

        compileStatements(statements1);

        addChild(whileStatement, index); // } // TODO
        // addChild(statements1, index); // } // TODO
        numberOfTabs -= 2;
        index += 1;
    }


    private void compileDo(Element statements) {
        numberOfTabs++;

        addSubroot(statements, "doStatement");
        Element doStatement = getDirectChild(statements, "doStatement");

        numberOfTabs++;

        addChild(doStatement, index); // do
        index += 1;


        compileTerm(doStatement);

        addChild(doStatement, index); // ;
        index += 1;
        
        numberOfTabs--;
    }


    private void compileReturn(Element statements) {
        numberOfTabs++;

        addSubroot(statements, "returnStatement");
        Element returnStatement = getDirectChild(statements, "returnStatement");

        numberOfTabs++;

        addChild(returnStatement, index); // return
        index += 1;

        if (!getToken(index).token().equals(";")) {
            
            compileExpression(returnStatement);
        }
        addChild(returnStatement, index); // ;
        index += 1;

        numberOfTabs--;
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
        root.appendChild(this.document.createTextNode("\n"));
    }

    private void compileTerm(Element root) {
        if (getToken(index).tokenType().equals(TOKEN_TYPE.INT_CONST) || 
            getToken(index).tokenType().equals(TOKEN_TYPE.STRING_CONST) || 
            getToken(index).tokenType().equals(TOKEN_TYPE.KEYWORD)) 
            {
            numberOfTabs++;

            addSubroot(root, "term");
            Element term = getDirectChild(root, "term");
            
            numberOfTabs++;

            addChild(term, index); // integerConsant | stringConstant | keywordConstant
            index += 1;

            numberOfTabs -= 2;
        }
        else if (getToken(index).token().equals("(")) {
            numberOfTabs++;

            addSubroot(root, "term");
            Element term = getDirectChild(root, "term");

            numberOfTabs++;

            addChild(term, index); // (
            index += 1;

            compileExpression(term);

            addChild(term, index); // )
            index += 1;

            numberOfTabs -= 2;
        }
        else if (op.contains(getToken(index).token())) {
            numberOfTabs++;

            addSubroot(root, "term");
            Element term = getDirectChild(root, "term");

            numberOfTabs++;

            addChild(term, index); // unaryOp
            index += 1;

            compileTerm(term);

            numberOfTabs -= 2;
        }
        else if (getToken(index).tokenType().equals(TOKEN_TYPE.IDENTIFIER)) {
            if (getToken(index+1).token().equals("[")) {
                numberOfTabs++;

                addSubroot(root, "term");
                Element term = getDirectChild(root, "term");

                numberOfTabs++;

                addChild(term, index); // varName
                addChild(term, index+1); // [
                index += 2;

                compileExpression(term);

                addChild(term, index); // ]
                index += 1;

                numberOfTabs -= 2;
            }
            else if (getToken(index+1).token().equals(".")) {
                numberOfTabs++;
                
                Element termRoot = root;
                if (letStatementBool == true) {
                    addSubroot(root, "term");                 // 
                    termRoot = getDirectChild(root, "term");  // it has to add term when let
                }

                numberOfTabs++;
                
                addChild(termRoot, index); // className | varName
                addChild(termRoot, index+1); // .
                addChild(termRoot, index+2); // subroutineName
                addChild(termRoot, index+3); // (
                index += 4;

                compileExpressionList(root);

                addChild(termRoot, index); // )
                index += 1;

                numberOfTabs -= 2;
                
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
                numberOfTabs++;

                addSubroot(root, "term");
                Element term = getDirectChild(root, "term");

                numberOfTabs++;

                addChild(term, index); // varName
                index += 1;

                numberOfTabs -= 2;
            }
        }
        else if (op.contains(getToken(index).token())) {
            numberOfTabs++;

            addSubroot(root, "term");
            Element term = getDirectChild(root, "term");

            numberOfTabs++;
            addChild(term, index); // op
            index += 1;

            compileTerm(term);

            numberOfTabs -= 2;
        }
        else if (getToken(index).token().equals("true") || 
                 getToken(index).token().equals("false") || 
                 getToken(index).token().equals("null") || 
                 getToken(index).token().equals("this")) 
        {
            numberOfTabs++;

            addSubroot(root, "term");
            Element term = getDirectChild(root, "term");

            numberOfTabs++;

            addChild(term, index);
            index += 1;

            numberOfTabs -= 2;
        }
    }

    private void compileExpressionList(Element root) {
        addSubroot(root, "expressionList");
        Element expressionList = getDirectChild(root, "expressionList");
        numberOfTabs++;
        while (!(getToken(index).token().equals(")"))) {
            compileExpression(expressionList);
            // expressionList.appendChild(this.document.createTextNode("\n"));
            if (getToken(index).token().equals(",")) {
                addChild(expressionList, index);
                index += 1;
            }
        }
        numberOfTabs--;
    }

    private LexicalElement getToken(int index) {
        return new LexicalElement(tokenArray.get(index).tokenType(), tokenArray.get(index).token());
    }

    private void addChild(Element parent, int index) {
        System.out.println(index);
        String tabs = String.format("%0" + numberOfTabs + "d", 0).replace("0", "  ");

        parent.appendChild(this.document.createTextNode("\n"+tabs)); // tokens are childs of root 
                                                                  // so they must be intended
        Element child = document.createElement(getToken(index).tokenType().toString().toLowerCase()); // create <tp> </tp>

        String description = " "+getToken(index).token()+" ";
        child.appendChild(document.createTextNode(description)); // add to it text -> <tp>text</tp>
        parent.appendChild(child); // add token to parent
    }

    private void addSubroot(Element parent, String description) {
        String tabs = "";
        try {
            tabs = String.format("%0" + numberOfTabs + "d", 0).replace("0", "  ");
        } catch (Exception e) {
            //TODO: handle exception
        }

        parent.appendChild(this.document.createTextNode("\n"+tabs)); // tokens are childs of root 
                                                                  // so they must be intended
        Element subRoot = document.createElement(description); // create <tp> </tp>
        parent.appendChild(subRoot); // add token to root (tokens)
    }

    private void intendEndTag(Element parent) { // TODO how to peel off end tag?
        String tabs = String.format("%0" + numberOfTabs + "d", 0).replace("0", "  ");

        parent.appendChild(this.document.createTextNode("\n"+tabs));
    }

    private static Element getDirectChild(Element parent, String tagName) { // TODO must return recently created element
        for(Node child = parent.getLastChild(); child != null; child = child.getParentNode()) {
            if (child instanceof Element && tagName.equals(child.getNodeName())) {
                return (Element) child;
            }
        }
        return null;
    }
};