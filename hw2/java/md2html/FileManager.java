package md2html;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileManager {
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private String nextLine = new String();

    public FileManager(final String fileInput, final String fileOutput) throws IOException {
        reader = new BufferedReader(new FileReader(fileInput, StandardCharsets.UTF_8));
        writer = new BufferedWriter(new FileWriter(fileOutput, StandardCharsets.UTF_8));
    }

    private String getNextLine() throws IOException {
        int a;
        StringBuilder str = new StringBuilder();
        while ((a = reader.read()) != -1) {
            if (a == '\n') break;
            if (a == '\r') continue;
            str.append((char) a);
        }
        if (a == -1) {
            return null;
        }
        return str.toString();
    }

    public String paragraph() throws IOException {
        if (nextLine == null) {
            return "";
        }
        while (nextLine != null && nextLine.isEmpty()) {
            nextLine = getNextLine();
        }
        StringBuilder paragraph = new StringBuilder();
        paragraph.append(nextLine);
        String line;
        while ((line = getNextLine()) != null && !line.isEmpty()) {
            paragraph.append("\n");
            paragraph.append(new StringBuilder(line));
        }
        nextLine = reader.readLine();
        while (nextLine != null && nextLine.isEmpty()) {
            nextLine = getNextLine();
        }
        return paragraph.toString();
    }

    public void write(final String out) throws IOException {
        writer.write(out);
    }

    public void close() throws IOException {
        reader.close();
        writer.close();
    }
}
