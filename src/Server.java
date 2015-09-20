/**
 * Created by Lucifer on 9/19/15.
 */

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import java.io.*;

public class Server {
    private static final String FILE_NAME = "/Users/Lucifer/Documents/ds/server.txt";
    private NameTable nameTable;

    public NameTable getNameTable() {
        try {

            BufferedReader bf = new BufferedReader(new FileReader(new File(FILE_NAME)));
            String line;
            this.nameTable = new NameTable();

            Util.println("\nStart loading Server information...");
            while ((line = bf.readLine()) != null) {
                String[] serverInfo = line.split(" ");
                String serverName = serverInfo[0];
                String serverHost = serverInfo[1];
                int serverPort = Integer.parseInt(serverInfo[2]);
                if (this.nameTable.insert(serverName, serverHost, serverPort)){
                    Util.println(String.format("Successfully load server: %s", serverName));
                } else {
                    Util.println(String.format("%s has been loaded", serverName));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Util.println("End loading Server information...\n");
        return this.nameTable;
    }



}
