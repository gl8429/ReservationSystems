/**
 * Created by Lucifer on 9/19/15.
 */

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import java.io.*;

public class Server {
    private static final String FILE_NAME = "/Users/Lucifer/Documents/ds/server.txt";

    public static void main(String[] args) {
        NameTable nameTable = new NameTable(FILE_NAME);
        System.out.println(nameTable.size());
    }
}
