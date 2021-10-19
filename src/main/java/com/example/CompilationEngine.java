package com.example;

import java.io.File;
import java.io.IOException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


class CompilationEngine {
    private File file;

    private Document document;
    private Element rootElement;
    private Transformer transformer;

    // prepares file to be written
    public CompilationEngine(File file) throws IOException, TransformerException, ParserConfigurationException {  

        // copy name of parsed file and 
        // set extension of written file to.xml
        int i = file.getName().lastIndexOf('.');
        String name = file.getName().substring(0,i);
        this.file = new File(file.getParent(), name + "Generated.xml");

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
        rootElement = this.document.createElement("tokens");
        TransformerFactory tf = TransformerFactory.newInstance();
        transformer = tf.newTransformer();

        rootElement.appendChild(this.document.createTextNode("\n"));

        // write XML file
        transformer.transform(new DOMSource(rootElement), new StreamResult(this.file));
    };

    // adds tokens to prepared .xml file
    public void writeXML(TOKEN_TYPE tp, String buffer) throws TransformerException {
        // text element

       rootElement.appendChild(this.document.createTextNode("\n\t"));
        
        // create new element 
        Element token = document.createElement(tp.toString().toLowerCase());
        token.appendChild(document.createTextNode(" " + buffer + " "));
        rootElement.appendChild(token);
    }

    // beautifies end of file
    public void endFile() throws TransformerException {
        rootElement.appendChild(this.document.createTextNode("\n"));
        transformer.transform(new DOMSource(rootElement), new StreamResult(this.file));
    }
};