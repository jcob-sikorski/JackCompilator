package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

public class JackAnalyzer {

    public static void main(String... args) throws Exception, IOException {
        Path dir = Paths.get("/Users/jakubsiekiera/Desktop/nand2tetris/projects/11/ConvertToBin");

        // parse each file and serialize it to .xml file
        Files.walk(dir).forEach(path -> {
            File file = path.toFile();
            if (getFileExtension(file).equals("jack")) {
                try {
                    parseFile(file);
                } catch (TransformerException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".") + 1;
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf);
    }
    public static void parseFile(File file) throws TransformerException, ParserConfigurationException, SAXException {
        try {
            JackTokenizer tokenizer = new JackTokenizer();
            ArrayList<Token> tokenArray = tokenizer.JackTokenizer(file);

            CompilationEngine compilationEngine = new CompilationEngine(file, tokenArray);
        }
        catch(IOException ie) {
            ie.printStackTrace();
        }
    }
}