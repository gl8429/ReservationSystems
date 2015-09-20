/**
 * Created by Xingyuan on 9/19/15.
 */


import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

import java.net.*;
import java.io.*;

public class Client {

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

    public static void main(String [] args)
    {
        Server server = new Server();
        NameTable nameTable = server.getNameTable();

        String serverName = args[0];
        int port = Integer.parseInt(args[1]);
        try
        {
            System.out.println("Connecting to " + serverName +
                    " on port " + port);
            Socket client = new Socket(serverName, port);
            System.out.println("Just connected to "
                    + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            out.writeUTF("Hello from "
                    + client.getLocalSocketAddress());
            InputStream inFromServer = client.getInputStream();
            DataInputStream in =
                    new DataInputStream(inFromServer);
            System.out.println("Server says " + in.readUTF());
            client.close();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}