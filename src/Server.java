import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Lucifer on 9/19/15.
 */

public class Server extends Thread{

    private int id;
    private ServerSocket serverSocket;
    private Seat seat;
    private NameTable nameTable;
    private DirectClock clock;
    private static final Semaphore semaphore = new Semaphore(1);
    private int[] q;
    Logger logger = Logger.getLogger("serverLog");
    FileHandler fh;

    public Server(int id, NameTable nameTable, int seatNumber) throws IOException {
        this.id = id;
        this.nameTable = nameTable;
        this.seat = new Seat(seatNumber);
        serverSocket = new ServerSocket(nameTable.getPort(id));
        serverSocket.setSoTimeout(1000000);
        this.clock = new DirectClock(nameTable.size(), id);
        this.q = new int[nameTable.size()];
        Arrays.fill(q, Integer.MAX_VALUE);
        fh = new FileHandler(String.format("/Users/Lucifer/IdeaProjects/ReservationSystems/testCase/%s.log", id));
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);

    }

    public void run() {
        int i = 0;
        while(true)
        {
            try
            {
                logger.info("Waiting for client on port " +
                        serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
                Thread t = new Thread(new sellRunnable(server));
                t.setName("Server: " + i++);
                t.start();

            }catch(SocketTimeoutException s)
            {
                logger.info("Socket timed out!");
                break;
            }catch(IOException e)
            {
                e.printStackTrace();
                break;
            }
        }
    }

    public class sellRunnable implements Runnable {

        private Socket socket;

        public sellRunnable(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                logger.info("Successfully receive info. from client...");
                sell(socket);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadCast(Message.MessageType type, String buffer) throws IOException{
        logger.info("Broadcast begin...");
        String myId =  nameTable.getHost(id) + ":" + nameTable.getPort(id);
        logger.info("Clock send action.");
        clock.sendAction();
        for (int index = 0; index < nameTable.size(); ++index) {
            if (id != index) {
                logger.info("Server " + id + "send " + type + " to Server " + index + "with info: " + buffer);
                send(type, buffer, index, myId);
                logger.info("Send Completed.");
            }
        }
        logger.info("Broadcast completed...");
    }

    public void send(Message.MessageType type, String buffer, int index, String myId) throws IOException {
        String otherServerId = nameTable.getHost(index) + ":" + nameTable.getPort(index);
        logger.info("Get server " + index +  "'s id: " + otherServerId);
        Message reqMessage = new Message(myId, otherServerId, type, buffer);
        Socket client = new Socket(nameTable.getHost(index), nameTable.getPort(index));
        OutputStream outToOtherServer = client.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToOtherServer);
        logger.info("Send '" + reqMessage.toString() + "' to server " + index);
        out.writeUTF(reqMessage.toString());
    }

    public void sell(Socket server) throws IOException, InterruptedException {
        while (true) {
            logger.info("Just connected to " + server.getRemoteSocketAddress());
            DataInputStream in = new DataInputStream(server.getInputStream());
            Message message = null;
            try {
                message = Message.parseMessage(in.readUTF());
            } catch (EOFException e) {
                break;
            }
            logger.info("--------------------------------------------------");
            logger.info("Receive message from client: " + message.toString());
            if (message.getTag().equals(Message.MessageType.RESERVE)) {
                // Send CS request to other servers and update my own queue.
                logger.info("Message Type: RESERVE");
                semaphore.acquire();
                logger.info(id + " begin to broadcast REQUEST to other servers with 'timestamp = " + clock.getValue(id) + "'");
                q[id] = clock.getValue(id);
                broadCast(Message.MessageType.REQUEST, String.valueOf(id) + " " + String.valueOf(q[id]));
                semaphore.release();
                while (true) {
                    Thread.sleep(1000);
                    semaphore.acquire();
                    if (q[id] == findMin(q) && q[id] < findMin(clock.clock)) {
                        logger.info("Minimum timestamp in q is " + findMin(q) + ", and current timestamp of this request is " + q[id]);
                        logger.info("Minimum clock from the other server's timestamp is " + findMin(clock.clock) + ", my current timestamp is " + q[id]);
                        logger.info(id + " begin to broadcast RELEASE to other servers with 'timestamp = " + clock.getValue(id) + "'");
                        semaphore.release();
                        logger.info(message.getMsg());
                        String customer = message.getMsg().split(" ")[0];
                        int ticketNumber = Integer.parseInt(message.getMsg().split(" ")[1]);
                        broadCast(Message.MessageType.RELEASE, String.valueOf(id) + " " + clock.getValue(id));
                        semaphore.acquire();
                        q[id] = Integer.MAX_VALUE;
                        semaphore.release();
                        DataOutputStream out = new DataOutputStream(server.getOutputStream());
                        logger.info("Begin to check whether there is enough tickets");
                        if (seat.getLeftSeats() < ticketNumber) {
                            Message msg = new Message(message.getDestId(), message.getSrcId(), Message.MessageType.RESULT,
                                    String.format("Failed: only %d seats left but %d seats are requested", seat.getLeftSeats(), ticketNumber));
                            out.writeUTF(msg.toString());
                        } else if (!seat.search(customer).isEmpty()) {
                            Message msg = new Message(message.getDestId(), message.getSrcId(), Message.MessageType.RESULT,
                                    String.format("Failed %s has booked the following seats: %s", customer, seat.search(customer).toString()));
                            out.writeUTF(msg.toString());
                        } else {
                            Message msg = new Message(message.getDestId(), message.getSrcId(), Message.MessageType.RESULT,
                                    String.format("The seats have been reserved for %s: %s", customer, seat.reserve(customer, ticketNumber).toString()));
                            out.writeUTF(msg.toString());
                        }
                        break;
                    } else {
                        semaphore.release();
                    }
                }
            } else if (message.getTag() == Message.MessageType.SEARCH) {
                // Send CS request to other servers and update my own queue.
                logger.info("Message Type: SEARCH");
                semaphore.acquire();
                logger.info(id + " begin to broadcast REQUEST to other servers with 'timestamp = " + clock.getValue(id) + "'");
                q[id] = clock.getValue(id);
                broadCast(Message.MessageType.REQUEST, String.valueOf(id) + " " + String.valueOf(q[id]));
                semaphore.release();
                while (true) {
                    Thread.sleep(1000);
                    semaphore.acquire();
                    if (q[id] == findMin(q) && q[id] < findMin(clock.clock)) {
                        logger.info("Minimum timestamp in q is " + findMin(q) + ", and current timestamp of this request is " + q[id]);
                        logger.info("Minimum clock from the other server's timestamp is " + findMin(clock.clock) + ", my current timestamp is " + q[id]);
                        logger.info(id + " begin to broadcast RELEASE to other servers with 'timestamp = " + clock.getValue(id) + "'");
                        semaphore.release();
                        String customer = message.getMsg();
                        broadCast(Message.MessageType.RELEASE, String.valueOf(id) + " " + clock.getValue(id));
                        semaphore.acquire();
                        q[id] = Integer.MAX_VALUE;
                        semaphore.release();
                        DataOutputStream out = new DataOutputStream(server.getOutputStream());
                        if (seat.search(customer).isEmpty()) {
                            Message msg = new Message(message.getDestId(), message.getSrcId(), Message.MessageType.RESULT,
                                    String.format("Failed: no reservation is made by %s", customer));
                            out.writeUTF(msg.toString());
                        } else {
                            Message msg = new Message(message.getDestId(), message.getSrcId(), Message.MessageType.RESULT,
                                    seat.search(customer).toString());
                            out.writeUTF(msg.toString());
                        }
                        break;
                    } else {
                        semaphore.release();
                    }
                }
            } else if (message.getTag() == Message.MessageType.DELETE) {
                // Send CS request to other servers and update my own queue.
                logger.info("Message Type: DELETE");
                semaphore.acquire();
                logger.info(id + " begin to broadcast REQUEST to other servers with 'timestamp = " + clock.getValue(id) + "'");
                q[id] = clock.getValue(id);
                broadCast(Message.MessageType.REQUEST, String.valueOf(id) + " " + String.valueOf(q[id]));
                semaphore.release();
                while (true) {
                    Thread.sleep(1000);
                    semaphore.acquire();
                    if (q[id] == findMin(q) && q[id] < findMin(clock.clock)) {
                        logger.info("Minimum timestamp in q is " + findMin(q) + ", and current timestamp of this request is " + q[id]);
                        logger.info("Minimum clock from the other server's timestamp is " + findMin(clock.clock) + ", my current timestamp is " + q[id]);
                        logger.info(id + " begin to broadcast RELEASE to other servers with 'timestamp = " + clock.getValue(id) + "'");
                        semaphore.release();
                        String customer = message.getMsg();
                        broadCast(Message.MessageType.RELEASE, String.valueOf(id) + " " + clock.getValue(id));
                        semaphore.acquire();
                        q[id] = Integer.MAX_VALUE;
                        semaphore.release();
                        DataOutputStream out = new DataOutputStream(server.getOutputStream());
                        if (seat.search(customer).isEmpty()) {
                            Message msg = new Message(message.getDestId(), message.getSrcId(), Message.MessageType.RESULT,
                                    String.format("Failed: no reservation is made by %s", customer));
                            out.writeUTF(msg.toString());
                        } else {
                            Message msg = new Message(message.getDestId(), message.getSrcId(), Message.MessageType.RESULT,
                                    String.format("%d seats have been released. %d seats are now available.", seat.delete(customer).size(), seat.getLeftSeats()));
                            out.writeUTF(msg.toString());
                        }
                        break;
                    } else {
                        semaphore.release();
                    }
                }
            } else if (message.getTag() == Message.MessageType.REQUEST) {
                logger.info("Message Type: REQUEST");
                int sender = Integer.parseInt(message.getMsg().split(" ")[0]);
                int timeStamp = Integer.parseInt(message.getMsg().split(" ")[1]);
                semaphore.acquire();
                logger.info("Update timestamp because receive action.");
                clock.receiveAction(sender, timeStamp);
                q[sender] = timeStamp;
                logger.info("Update sender's clock to " + q[sender]);
                logger.info(id + " send ACK to sender server " + sender + "with 'timestamp = " + clock.getValue(id) + "'");
                send(Message.MessageType.ACK, String.valueOf(id) + " " + String.valueOf(clock.getValue(id)), sender, message.getDestId());
                semaphore.release();
            } else if (message.getTag() == Message.MessageType.RELEASE) {
                logger.info("Message Type: RELEASE");
                int sender = Integer.parseInt(message.getMsg().split(" ")[0]);
                int timeStamp = Integer.parseInt(message.getMsg().split(" ")[1]);
                semaphore.acquire();
                q[sender] = Integer.MAX_VALUE;
                clock.receiveAction(sender, timeStamp);
                logger.info("Update timestamp because receive RELEASE action.");
                logger.info("Update sender's clock to " + q[sender]);
                semaphore.release();
            } else if (message.getTag() == Message.MessageType.ACK) {
                logger.info("Message Type: ACK");
                int sender = Integer.parseInt(message.getMsg().split(" ")[0]);
                int timeStamp = Integer.parseInt(message.getMsg().split(" ")[1]);
                semaphore.acquire();
                clock.receiveAction(sender, timeStamp);
                logger.info("Update timestamp because receive ACK action.");
                semaphore.release();
            } else if (message.getTag() == Message.MessageType.RESULT) {
                int sender = Integer.parseInt(message.getMsg().split("#")[0]);
                int timeStamp = Integer.parseInt(message.getMsg().split("#")[1]);
                seat.setSeat(message.getMsg().split("#")[2]);
                semaphore.acquire();
                clock.receiveAction(sender, timeStamp);
                semaphore.release();
            } else if (message.getTag() == Message.MessageType.RECOVER) {
                semaphore.acquire();
                String myId = nameTable.getHost(id) + ":" + nameTable.getPort(id);
                String buffer = id + "#" + clock.getValue(id) + "#";
                semaphore.release();
                for (Map.Entry<Integer, String> entry : seat.getSeat().entrySet()) {
                    buffer += entry.getKey() + ":" + entry.getValue() + ",";
                }
                send(Message.MessageType.RESULT, buffer.substring(0, buffer.length() - 1), Integer.parseInt(message.getMsg()), myId);
                semaphore.acquire();
                clock.sendAction();
                semaphore.release();
            }
//            logger.info("Socket is closing.");
//            server.close();
//            logger.info("Socket closed...");
        }
    }

    public int findMin(int[] q) {
        int min = Integer.MAX_VALUE;
        for (int i : q) {
            if (min > i) min = i;
        }
        return min;
    }

}