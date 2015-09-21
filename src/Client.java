/**
 * Created by Xingyuan on 9/19/15.
 */
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import java.net.*;
import java.io.*;
import java.util.Random;

public class Client {
    private static final String FILE_NAME = "/Users/Xingyuan/Documents/GitHub/ReservationSystems/testCase/server.txt";

    public static void main(String [] args)
    {
        NameTable nameTable = new NameTable(FILE_NAME);

        // Randomly choose a server
        Random rand = new Random();
        int randomNum = rand.nextInt(nameTable.size());
        String host = nameTable.getHost(randomNum);
        int port = nameTable.getPort(randomNum);

        try
        {
            System.out.println("Connecting to " + host + " on port " + port);
            Socket client = new Socket(host, port);
            System.out.println("Just connected to " + client.getRemoteSocketAddress());
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