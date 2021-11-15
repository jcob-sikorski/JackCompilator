package com.example;

import java.io.File;
import java.util.ArrayList;

// holds info (file, filename, tokenArray) about serialized file
public class SerializedFile {
    private File file;
    private String filename;
    private ArrayList<LexicalElement> tokenArray;
    
    public SerializedFile(File file, String filename, ArrayList<LexicalElement> tokenArray) {
        this.file = file;
        this.tokenArray = tokenArray;

        this.filename = filename;
    }

    public File getFile() {
        return file;
    }

    public String getFilename() {
        return filename;
    }

    public ArrayList<LexicalElement> getTokenArray() {
        return tokenArray;
    }
}
