package com.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javafx.animation.Animation.Status;

class Test {

  private static String pathToFileExpected = "/Users/jakubsiekiera/Desktop/nand2tetris/projects/10/Square/SquareGame.xml";
  private static String pathToFileProduced = "/Users/jakubsiekiera/Desktop/nand2tetris/projects/10/Square/SquareGameEngineGenerated.xml";

  // private static ArrayList<String> outputLines = new ArrayList<String>();
  public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();

    // normalizeFile(pathToFileExpected);

    File expFile = Paths.get(pathToFileExpected).toFile();
    Document expected = db.parse(expFile);

    File prodFile = Paths.get(pathToFileProduced).toFile();
    Document produced = db.parse(prodFile);

    compare(produced, expected);
    // error:;
  }

  // private static void normalizeFile(String pathToFile) throws IOException {
  //   try (Stream<String> lines = Files.lines(Paths.get(pathToFileExpected), Charset.defaultCharset())) {
  //     lines.forEachOrdered(line -> removeNewLine(line));
  //   }
    
  //   try(FileWriter writer = new FileWriter(pathToFile, true) ){
  //     writer.write("");
  //       for (String line : outputLines) {
  //         writer.append(line);
  //       }
  //   } catch (IOException e) {}
  // }

  // private static void removeNewLine(String line) {
  //   String modifiedLine = line.replace("[\\n\\t ]", "");
  //   outputLines.add(modifiedLine);
  // }

  private static void compare(Document produced, Document expected) {
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

        if (rabbitHole.contains(prodNodeName) && rabbitHole.contains(prodNodeName)) {
          compareNodesList(prodNode.getChildNodes(), expNode.getChildNodes(), 0);
        }
        else if (rabbitHole.contains(prodNodeName)) {
          compareNodesList(prodNode.getChildNodes(), expNodes, 0);
        }
        else if (rabbitHole.contains(prodNodeName)) {
          compareNodesList(prodNodes, expNode.getChildNodes(), 0);
        }
        else {
          String prodNodeTextContent = prodNode.getTextContent();
          String expNodeTextContent = expNode.getTextContent().trim();
  
          if (prodNode.getNodeName().equals(expNode.getNodeName()) &&
              prodNode.getTextContent().trim().equals(expNode.getTextContent().trim())) {
                System.out.println("index " + i + ": " + "nodeName: " + expNode.getNodeName() + " textContent: " + expNode.getTextContent());
          }
          else {
            System.out.println("Files are not the same."); // 
          }
        }
      } 
    } catch (Exception e) {
      System.out.println("Files are incorrect.");
    }
    return i;
  }
};