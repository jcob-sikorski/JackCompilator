package com.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    private FileWriter fileWriter;

    public VMWriter(String path) throws IOException {
        File file = new File(path);
        this.fileWriter = new FileWriter(file, false);
    }

    public void writePush(SEGMENT segment, Integer index) throws IOException {
        fileWriter.write("push " + segment.toString().toLowerCase() + " " + Integer.toString(index) + "\n");
    }

    public void writePop(SEGMENT segment, Integer index) throws IOException {
        fileWriter.write("pop " + segment.toString().toLowerCase() + " " + Integer.toString(index) + "\n");
    }

    public void writeArithmetic(String command) throws IOException {
        fileWriter.write(command + "\n");
    }

    public void writeLabel(String label) throws IOException {
        fileWriter.write("label " + label + "\n");
    }

    public void writeGoto(String label) throws IOException {
        fileWriter.write("goto " + label + "\n");
    }

    public void writeIf(String label) throws IOException {
        fileWriter.write("if-goto " + label + "\n");
    }

    public void writeCall(String label, Integer nArgs) throws IOException {
        fileWriter.write("call " + label + " " + Integer.toString(nArgs) + "\n");
    }

    public void writeFunction(String label, Integer nLocals) throws IOException {
        fileWriter.write("function " + label + " " + Integer.toString(nLocals) + "\n");
    }

    public void writeReturn() throws IOException {
        fileWriter.write("return" + "\n");
    }

    public void close() throws IOException {
        // TODO is this function really necessary?
        fileWriter.close();
    }
}
