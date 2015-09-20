/**
 * Created by Xingyuan on 9/19/15.
 */
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class NameTable {
    final int maxSize = 100;
    private final String[] names = new String[maxSize];
    private final String[] hosts = new String[maxSize];
    private final int[] ports = new int[maxSize];
    private int size = 0;

    NameTable(String fileName) {
        try {

            BufferedReader bf = new BufferedReader(new FileReader(new File(fileName)));
            String line;

            Util.println("\nStart loading server information...");
            while ((line = bf.readLine()) != null) {
                String[] serverInfo = line.split(" ");
                String serverName = serverInfo[0];
                String serverHost = serverInfo[1];
                int serverPort = Integer.parseInt(serverInfo[2]);
                names[size] = serverName;
                hosts[size] = serverHost;
                ports[size] = serverPort;
                ++size;
                Util.println(String.format("Successfully load server: %s", serverName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Util.println("End loading server information...\n");
    }

    public int search(String s) {
        for (int i = 0; i < size; ++i) {
            if (names[i].equals(s)) return i;
        }
        return -1;
    }

    public String getName(int index) {
        return names[index];
    }

    public String getHost(int index) {
        return hosts[index];
    }

    public int getPort(int index) {
        return ports[index];
    }

    public int size() {
        return size;
    }
}