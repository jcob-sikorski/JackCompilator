package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JackAnalyzer {

    public static void main(String... args) throws Exception, IOException {
        Path dir = Paths.get("/Users/jakubsiekiera/Desktop/nand2tetris/projects/10/ArrayTest");
        Files.walk(dir).forEach(path -> {
            File file = path.toFile();
            if (getFileExtension(file).equals("jack")) {
                loop(file);
            }
        });
    }
    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".") + 1;
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }
    public static void loop(File file) {
        try {
            JackTokenizer tokenizer = new JackTokenizer(file);
        }
        catch(IOException ie) {
            ie.printStackTrace();
        }
        // https://stackoverflow.com/questions/7373567/how-to-read-and-write-xml-files
        CompilationEngine engine = new CompilationEngine();

    }
}