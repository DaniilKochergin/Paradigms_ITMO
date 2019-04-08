package md2html;

import java.io.IOException;

public class Md2Html {
    public static void main(String[] args) throws IOException {
        FileManager fileManager = new FileManager(args[0], args[1]);
        MdParser mdParser = new MdParser(fileManager);
        try {
            fileManager.write(mdParser.parse());
        } catch (IOException e){
            fileManager.close();
        }
        fileManager.close();
    }
}
