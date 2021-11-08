package com.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class VMWriter {
    private FileWriter fileWriter;

    public VMWriter(Path path) throws IOException {
        File file = new File(path.toString());
        this.fileWriter = new FileWriter(file, false);
    }

    public void writePush(SEGMENT segment, Integer index) throws IOException {
        fileWriter.write("push " + segment + " " + Integer.toString(index));
    }

    public void writePop(SEGMENT segment, Integer index) throws IOException {
        fileWriter.write("pop " + segment + " " + Integer.toString(index));
    }

    public void writeArithmetic(COMMAND command) throws IOException {
        fileWriter.write(command.toString().toLowerCase());
    }

    public void writeLabel(String label) throws IOException {
        fileWriter.write("label " + label);
    }

    public void writeGoto(String label) throws IOException {
        fileWriter.write("goto " + label);
    }

    public void writeIf(String label) throws IOException {
        fileWriter.write("if-goto " + label);
    }

    public void writeCall(String label, Integer nArgs) throws IOException {
        fileWriter.write("label " + label + " " + Integer.toString(nArgs));
    }

    public void writeFunction(String label, Integer nLocals) throws IOException {
        fileWriter.write("function " + label + " " + Integer.toString(nLocals));
    }

    public void writeReturn() throws IOException {
        fileWriter.write("return");
    }

    public void close() {
        // TODO is this function really necessary?
    }
}
