# Fault-Tolerant Ticket Reservation System

The goal of this programming assignment is to learn the knowledge about the fault-tolerance of servers that is based upon active replications. Specifically, you have to implement a ticket reservation system with fault-tolerance for a movie theatre.

Assume that the movie theatre has c total seats and there are n servers that keep the current reservation of the seats. The reservation system accepts the following requests from a client:

####(a) reserve &lt;name&gt; &lt;count&gt; – reserves the number, count, of seats for the name.

• If the theater does not have enough seats, then the request is rejected and the client shows this message: “Failed: only &lt;# seats left&gt; seats left but &lt;count&gt; seats are requested.”

• If a reservation has been made using the name, then the client shows the message: “Failed: &lt;name&gt; has booked the following seats: &lt;seat-number&gt;,...,&lt;seat-number&gt;.” Here, the seat numbers are the numbers that are previously reserved for the name.

• Otherwise, the specified seats are assigned to the name and the client shows the message: “The seats have been reversed for &lt;name&gt;: &lt;seat-number&gt;,...,&lt;seat-number&gt;.”

####(b) search &lt;name&gt; – returns the seat numbers that are reserved for the name. 
If no reservation is found for the name, then the client shows the message: “Failed: no reservation is made by &lt;name&gt;.” 

####(c) delete &lt;name&gt; – frees up the seats that are assigned to the name. 
The clients shows the message: “&lt;# seats released&gt; seats have been released. &lt;# seat left&gt; seats are now available.” If no reservation is found for the name, the client shows the message: “Failed: no reservation is made by &lt;name&gt;.”

The system should behave correctly in presence of multiple concurrent clients. In particular, your system has to ensure that the reservation is identical at all servers and any update to the system is done in a mutually exclusive fashion using Lamport’s algorithm. Since Lamport’s algorithm requires the messages between servers are delivered in FIFO order, your implementation should use TCP protocol. You may assume that all servers know the IP addresses of other servers; every server has a file that holds its IP address and port number as well as those of other servers.

The cluster of servers should give the user illusion of a single immortal server (so far as serving the requests are concerned). In this assignment, we assume that the system has “perfect” failure detection, i.e., a server does not respond in the timeout interval if and only if it is crashed. Choose timeout of 5 seconds. You may assume that there is at least one server is available. However, DO NOT assume that it is the same server that is always up. When a server comes up again, it would need to synchronize with existing servers to ensure the consistency of the data.

You also have to implement a client program that takes the commands (requests) from the user and communicates with servers using sockets. When a client is started, it first reads a file with the IP addresses and port numbers of all servers and randomly connects to one of the servers. If the chosen server is not available (i.e., after the timeout), the client contacts some other server.

During the demo of this assignment, each server and client will be started in a separate JVM process instead of a thread. So, DO NOT use a central process to create servers and clients in threads. Moreover, server processes may crash (e.g., aborted by ctrl+c command) at anytime.
