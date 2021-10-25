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


class  XMLEngine {
    private File file;

    private Document document;
    private Element rootElement;
    private Transformer transformer;

    // prepares file to be written
    public XMLEngine(File file) throws IOException, TransformerException, ParserConfigurationException {  

        // copy name of parsed file and 
        // set extension of written file to.xml
        int i = file.getName().lastIndexOf('.');
        String name = file.getName().substring(0,i);
        this.file = new File(file.getParent(), name + "EngineGenerated.xml");

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
        rootElement = this.document.createElement("tokens");            // 
        TransformerFactory tf = TransformerFactory.newInstance();       //    
        transformer = tf.newTransformer();                              // TODO genarate file with proper identations
                                                                        // 
        rootElement.appendChild(this.document.createTextNode("\n"));    // 

        // write XML file
        transformer.transform(new DOMSource(rootElement), new StreamResult(this.file));
    };

    // adds tokens to prepared .xml file
    public void writeXML(LexicalElement lexicalElement) throws TransformerException {
        // text element

        rootElement.appendChild(this.document.createTextNode("\n\t"));
        Element token;
        // create new element
        try {
            token = document.createElement(lexicalElement.tokenType().toString());
            token.appendChild(document.createTextNode(" " + lexicalElement.token() + " "));
        } catch (Exception e) {
            token = document.createElement(lexicalElement.sectionType().toString());
            token.appendChild(document.createTextNode(" " + lexicalElement.section() + " "));
        }

        rootElement.appendChild(token);
    }

    // beautifies end of file
    public void endFile() throws TransformerException {
        rootElement.appendChild(this.document.createTextNode("\n"));
        transformer.transform(new DOMSource(rootElement), new StreamResult(this.file));
    }
};