import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Semaphore;

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

    public Server(int id, NameTable nameTable, int seatNumber) throws IOException {
        this.id = id;
        this.nameTable = nameTable;
        this.seat = new Seat(seatNumber);
        serverSocket = new ServerSocket(nameTable.getPort(id));
        serverSocket.setSoTimeout(1000000);
        this.clock = new DirectClock(nameTable.size(), id);
        this.q = new int[nameTable.size()];
        Arrays.fill(q, Integer.MAX_VALUE);
    }

    public void run() {
        while(true)
        {
            try
            {

                Socket server = serverSocket.accept();
                Thread t = new Thread(new sellRunnable(server));
                t.start();

            }catch(SocketTimeoutException s)
            {
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

                sell(socket);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadCast(Message.MessageType type, String buffer) throws IOException{
        String myId =  nameTable.getHost(id) + ":" + nameTable.getPort(id);
        for (int index = 0; index < nameTable.size(); ++index) {
            if (id != index) {
                send(type, buffer, index, myId);
            }
        }
    }

    public void send(Message.MessageType type, String buffer, int index, String myId) throws IOException {
        String otherServerId = nameTable.getHost(index) + ":" + nameTable.getPort(index);

        Message reqMessage = new Message(myId, otherServerId, type, buffer);
        Socket client = new Socket(nameTable.getHost(index), nameTable.getPort(index));
        OutputStream outToOtherServer = client.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToOtherServer);

        out.writeUTF(reqMessage.toString());
    }

    public void sell(Socket server) throws IOException, InterruptedException {
        while (true) {

            DataInputStream in = new DataInputStream(server.getInputStream());

            Message message;
            try {
                message = Message.parseMessage(in.readUTF());
            } catch (EOFException e) {
                break;
            }


            if (message.getTag().equals(Message.MessageType.RESERVE)) {
                // Send CS request to other servers and update my own queue.

                int timeStamp;
                int requestTimeStamp;

                semaphore.acquire();
                timeStamp = clock.getValue(id);
                q[id] = timeStamp;
                clock.sendAction();
                semaphore.release();

                broadCast(Message.MessageType.REQUEST, id + " " + timeStamp);

                while (true) {

                    int[] serverCheckBefore;
                    semaphore.acquire();
                    serverCheckBefore = clock.clock;
                    semaphore.release();

                    Thread.sleep(5000);

                    int[] serverCheckAfter;
                    semaphore.acquire();
                    serverCheckAfter = clock.clock;
                    semaphore.release();

                    for (int serverIndex = 0; serverIndex < serverCheckBefore.length; serverIndex++) {
                        if (serverCheckBefore[serverIndex] == serverCheckAfter[serverIndex]) {
                            semaphore.acquire();
                            clock.clock[serverIndex] = Integer.MAX_VALUE;
                            semaphore.release();
                        }
                    }

                    semaphore.acquire();
                    requestTimeStamp = q[id];
                    int minInQ = findMin(q);
                    int minInV = findMin(clock.clock);
                    semaphore.release();

                    if (requestTimeStamp == minInQ && requestTimeStamp < minInV) {
                        String customer = message.getMsg().split(" ")[0];
                        int ticketNumber = Integer.parseInt(message.getMsg().split(" ")[1]);

                        semaphore.acquire();
                        timeStamp = clock.getValue(id);
                        q[id] = Integer.MAX_VALUE;
                        clock.sendAction();
                        semaphore.release();

                        DataOutputStream out = new DataOutputStream(server.getOutputStream());

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

                        broadCast(Message.MessageType.RELEASE, id + " " + timeStamp + " " + seat.seatToString());

                        break;
                    }
                }
            } else if (message.getTag() == Message.MessageType.SEARCH) {
                // Send CS request to other servers and update my own queue.
                int timeStamp;
                int requestTimeStamp;

                semaphore.acquire();
                timeStamp = clock.getValue(id);
                q[id] = timeStamp;
                clock.sendAction();
                semaphore.release();

                broadCast(Message.MessageType.REQUEST, id + " " + timeStamp);

                while (true) {

                    int[] serverCheckBefore;
                    semaphore.acquire();
                    serverCheckBefore = clock.clock;
                    semaphore.release();

                    Thread.sleep(5000);

                    int[] serverCheckAfter;
                    semaphore.acquire();
                    serverCheckAfter = clock.clock;
                    semaphore.release();

                    for (int serverIndex = 0; serverIndex < serverCheckBefore.length; serverIndex++) {
                        if (serverCheckBefore[serverIndex] == serverCheckAfter[serverIndex]) {
                            semaphore.acquire();
                            clock.clock[serverIndex] = Integer.MAX_VALUE;
                            semaphore.release();
                        }
                    }

                    semaphore.acquire();
                    requestTimeStamp = q[id];
                    int minInQ = findMin(q);
                    int minInV = findMin(clock.clock);
                    semaphore.release();

                    if (requestTimeStamp == minInQ && requestTimeStamp < minInV) {

                        String customer = message.getMsg().split(" ")[0];

                        semaphore.acquire();
                        timeStamp = clock.getValue(id);
                        q[id] = Integer.MAX_VALUE;
                        clock.sendAction();
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

                        broadCast(Message.MessageType.RELEASE, id + " " + timeStamp + " " + seat.seatToString());

                        break;
                    }
                }
            } else if (message.getTag() == Message.MessageType.DELETE) {
                // Send CS request to other servers and update my own queue.
                int timeStamp;
                int requestTimeStamp;

                semaphore.acquire();
                timeStamp = clock.getValue(id);
                q[id] = timeStamp;
                clock.sendAction();
                semaphore.release();

                broadCast(Message.MessageType.REQUEST, id + " " + timeStamp);

                while (true) {

                    int[] serverCheckBefore;
                    semaphore.acquire();
                    serverCheckBefore = clock.clock;
                    semaphore.release();

                    Thread.sleep(5000);

                    int[] serverCheckAfter;
                    semaphore.acquire();
                    serverCheckAfter = clock.clock;
                    semaphore.release();

                    for (int serverIndex = 0; serverIndex < serverCheckBefore.length; serverIndex++) {
                        if (serverCheckBefore[serverIndex] == serverCheckAfter[serverIndex]) {
                            semaphore.acquire();
                            clock.clock[serverIndex] = Integer.MAX_VALUE;
                            semaphore.release();
                        }
                    }

                    semaphore.acquire();
                    requestTimeStamp = q[id];
                    int minInQ = findMin(q);
                    int minInV = findMin(clock.clock);
                    semaphore.release();

                    if (requestTimeStamp == minInQ && requestTimeStamp < minInV) {

                        String customer = message.getMsg();

                        semaphore.acquire();
                        q[id] = Integer.MAX_VALUE;
                        clock.sendAction();
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

                        broadCast(Message.MessageType.RELEASE, id + " " + timeStamp + " " + seat.seatToString());

                        break;
                    }
                }
            } else if (message.getTag() == Message.MessageType.REQUEST) {

                int myTimeStamp;

                int sender = Integer.parseInt(message.getMsg().split(" ")[0]);
                int receiveTimeStamp = Integer.parseInt(message.getMsg().split(" ")[1]);

                semaphore.acquire();
                clock.receiveAction(sender, receiveTimeStamp);
                myTimeStamp = clock.getValue(id);
                q[sender] = receiveTimeStamp;
                semaphore.release();

                send(Message.MessageType.ACK, id + " " + myTimeStamp, sender, message.getDestId());

            } else if (message.getTag() == Message.MessageType.RELEASE) {

                int sender = Integer.parseInt(message.getMsg().split(" ")[0]);
                int timeStamp = Integer.parseInt(message.getMsg().split(" ")[1]);
                seat.setSeat(message.getMsg().split(" ")[2]);

                semaphore.acquire();
                q[sender] = Integer.MAX_VALUE;
                clock.receiveAction(sender, timeStamp);
                semaphore.release();

            } else if (message.getTag() == Message.MessageType.ACK) {

                int sender = Integer.parseInt(message.getMsg().split(" ")[0]);
                int timeStamp = Integer.parseInt(message.getMsg().split(" ")[1]);

                semaphore.acquire();
                clock.receiveAction(sender, timeStamp);
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