package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

public class JackAnalyzer {
    // global collection of all methods across all files in Jack project
    private static Set<String> methodCollection = new HashSet<String>() {};
    private static ArrayList<SerializedFile> serializedFiles = new ArrayList<SerializedFile>();

    public static void main(String... args) throws Exception, IOException {
        Path dir = Paths.get("/Users/jakubsiekiera/Desktop/nand2tetris/projects/11/Square");

        // parse each file and serialize it to tokenArray
        Files.walk(dir).forEach(path -> {
            File file = path.toFile();
            if (getFileExtension(file).equals("jack")) {
                try {
                    serializeFile(file);
                } catch (TransformerException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }
        });
        compileFiles();
    }
    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".") + 1;
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf);
    }
    private static void serializeFile(File file) throws TransformerException, ParserConfigurationException, SAXException {
        try {
            int indexOfDash = file.getAbsolutePath().lastIndexOf('/');
            String filename = file.getAbsolutePath().substring(indexOfDash+1, file.getAbsolutePath().length()-5);

            JackTokenizer tokenizer = new JackTokenizer(filename, methodCollection);
            ArrayList<LexicalElement> tokenArray = tokenizer.serializeIntoTokens(file);
            methodCollection = tokenizer.updateMethodCollection();

            serializedFiles.add(new SerializedFile(file, filename, tokenArray));
        }
        catch(IOException ie) {
            ie.printStackTrace();
        }
    }
    public static void compileFiles() throws IOException, TransformerException, ParserConfigurationException, SAXException {
        for (SerializedFile serializedFile : serializedFiles) {
            new CompilationEngine(serializedFile, methodCollection);
        }
    }
}