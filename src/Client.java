import java.net.*;
import java.io.*;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Xingyuan on 9/19/15.
 */

public class Client {
    private static final String FILE_NAME = "/Users/Xingyuan/Documents/GitHub/ReservationSystems/testCase/server.txt";

    public static String parseCommand(String cmd, Socket client) {
        String srcId = client.getLocalSocketAddress().toString();
        String destId = client.getRemoteSocketAddress().toString();
        String cmdType = cmd.split(" ")[0];
        String customer = cmd.split(" ")[1];
        /* Get rid of the unwanted first '/' */
        srcId = srcId.substring(1, srcId.length());
        destId = destId.substring(1, destId.length());
        if (cmdType.equals("reserve")) {
            String ticketNumber = cmd.split(" ")[2];
            Message msgToSend = new Message(srcId, destId, Message.MessageType.RESERVE, customer + " " + ticketNumber);
            return msgToSend.toString();
        } else if (cmdType.equals("search")) {
            Message msgToSend = new Message(srcId, destId, Message.MessageType.SEARCH, customer);
            return msgToSend.toString();
        } else if (cmdType.equals("delete")) {
            Message msgToSend = new Message(srcId, destId, Message.MessageType.DELETE, customer);
            return msgToSend.toString();
        } else {
            throw new IllegalArgumentException("Invalid Command! Please enter one of these:\n" +
                                                "(1) reserve <name> <count>\n" +
                                                "(2) search <name>\n" +
                                                "(3) delete <name>\n");
        }
    }

    public static void main(String [] args)
    {
        NameTable nameTable = new NameTable(FILE_NAME);
        Scanner scanner = new Scanner(System.in);
        Socket client = null;

        // Randomly choose a server
        Random rand = new Random();
        int randomNum = rand.nextInt(nameTable.size());

        while (true) {
            /* Read interactive command from terminal */
            String cmd = scanner.nextLine();
            while (true) {
                try {
                    /* Connect to a server in the server list */
                    String host = nameTable.getHost(randomNum);
                    int port = nameTable.getPort(randomNum);
                    System.out.println("Connecting to " + host + " on port " + port);
                    client = new Socket(host, port);
                    System.out.println("Just connected to " + client.getRemoteSocketAddress());
                    String msgToServer;
                    try {
                        msgToServer = parseCommand(cmd, client);
                    } catch (IllegalArgumentException e) {
                        System.out.print(e.toString());
                        continue;
                    }
                    OutputStream outToServer = client.getOutputStream();
                    DataOutputStream out = new DataOutputStream(outToServer);

                    InputStream inFromServer = client.getInputStream();
                    DataInputStream in = new DataInputStream(inFromServer);

                    /* Send the corresponding message to the server */
                    System.out.println("Sending message: " + msgToServer + " to server: " + client.getRemoteSocketAddress());
                    out.writeUTF(msgToServer);

                    /* Get the result back after server processes client's request */
                    String msgGetFromServer = in.readUTF();
                    Message message = Message.parseMessage(msgGetFromServer);
                    System.out.println("The result is:");
                    System.out.println(message.getMsg());
                    break;
                } catch (IOException e) {
                    randomNum = (randomNum + 1) % nameTable.size();
                    continue;
                }
            }
        }
    }
}