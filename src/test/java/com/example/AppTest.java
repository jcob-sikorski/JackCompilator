package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


class Test { // Compares two files.

  private static String pathToFileExpected = "/Users/jakubsiekiera/Desktop/nand2tetris/projects/10/ArrayTest/Main.xml";
  private static String pathToFileProduced = "/Users/jakubsiekiera/Desktop/nand2tetris/projects/10/ArrayTest/MainEngineGenerated.xml";

  public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();

    File expFile = Paths.get(pathToFileExpected).toFile();
    Document expected = db.parse(expFile);

    File prodFile = Paths.get(pathToFileProduced).toFile();
    Document produced = db.parse(prodFile);

    compareFiles(produced, expected);
  }

  private static void compareFiles(Document produced, Document expected) {
    Node prodRoot = produced.getElementsByTagName("class").item(0);
    Node expRoot = expected.getElementsByTagName("class").item(0);

    NodeList prodNodes = prodRoot.getChildNodes();
    NodeList expNodes = expRoot.getChildNodes();

    compareNodesList(prodNodes, expNodes, 0);
  }

  private static int compareNodesList(NodeList prodNodes, NodeList expNodes, int i) {
    
    Set<String> rabbitHole = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("classVarDec", "subroutineDec", "parameterList", "subroutineBody", "varDec", "letStatement",
                                    "ifStatement", "whileStatement", "doStatement", "returnStatement", "expression", "term", "expressionList", "statements")));

    try {
      for (; i < expNodes.getLength(); i++) {
        Node prodNode = prodNodes.item(i);
        Node expNode = expNodes.item(i);

        String prodNodeName = prodNode.getNodeName();
        String expNodeName = expNode.getNodeName();

        if (rabbitHole.contains(prodNodeName) && rabbitHole.contains(prodNodeName)) { // check if node has body
          compareNodesList(prodNode.getChildNodes(), expNode.getChildNodes(), 0);     // inspect body of node
        }
        else {
          String prodNodeTextContent = prodNode.getTextContent();
          String expNodeTextContent = expNode.getTextContent().trim();
  
          if (prodNode.getNodeName().equals(expNode.getNodeName()) &&                     // if nodes are the same
              prodNode.getTextContent().trim().equals(expNode.getTextContent().trim())) { // print info about them
                System.out.println("index " + i + ": " + "nodeName: " + expNode.getNodeName() + " textContent: " + expNode.getTextContent());
          }
          else {
            System.out.println("Files are not the same.");
          }
        }
      } 
    } catch (Exception e) {
      System.out.println("Files are incorrect.");
    }
    return i;
  }
};