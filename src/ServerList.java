import java.io.IOException;

/**
 * Created by Lucifer on 9/20/15.
 */
public class ServerList {

    private static final String FILE_NAME = "/Users/Lucifer/IdeaProjects/ReservationSystems/testCase/server.txt";
    private static final int SEAT_NUMBER = 100;

    public static void main(String[] args) {
        NameTable nameTable = new NameTable(FILE_NAME);
        for (int index = 0; index < nameTable.size(); index++) {
            try {
                Thread t = new Server(index, nameTable, SEAT_NUMBER);
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
