

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Created by Lucifer on 9/19/15.
 */

public class Server extends Thread{

    private static final String FILE_NAME = "/Users/Lucifer/IdeaProjects/ReservationSystems/testCase/server.txt";
    private ServerSocket serverSocket;
    private static final Semaphore semaphore = new Semaphore(1);

    public Server(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(100000);
    }

    public void run()
    {
        int i = 0;
        while(true)
        {
            try
            {
                System.out.println("Waiting for client on port " +
                        serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
                Thread t = new Thread(new sellRunnable(server));
                t.setName("Server: " + i++);
                t.start();

            }catch(SocketTimeoutException s)
            {
                System.out.println("Socket timed out!");
                break;
            }catch(IOException e)
            {
                e.printStackTrace();
                break;
            }
        }
    }

    public class sellRunnable implements Runnable{

        private Socket socket;

        public sellRunnable(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                sell(socket);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sell(Socket server) throws IOException, InterruptedException {

        while(!soldAll){
            Thread.sleep(1000);
            semaphore.acquire();
            if (leftTickets > 0) {
                System.out.println("Just connected to "
                        + server.getRemoteSocketAddress());
                DataInputStream in =
                        new DataInputStream(server.getInputStream());
                Thread t = Thread.currentThread();
                String tmp = in.readUTF();
                System.out.println(t.getName() + tmp);
                //System.out.println(in.readUTF());




                DataOutputStream out =
                        new DataOutputStream(server.getOutputStream());
                out.writeUTF("Thank you for connecting to "
                        + server.getLocalSocketAddress() + "\nGoodbye!");
            } else {
                soldAll = true;
                System.out.println("Sold out all tickets");
            }

            semaphore.release();
            server.close();
        }
    }

    public static void main(String[] args) {
        NameTable nameTable = new NameTable(FILE_NAME);
        for (int i = 0; i < nameTable.size(); i++) {
            int port = nameTable.getPort(0);
            try {
                Thread t = new Server(port);
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}