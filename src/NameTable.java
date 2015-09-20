/**
 * Created by Xingyuan on 9/19/15.
 */

public class NameTable {
    final int maxSize = 100;
    private final String[] names = new String[maxSize];
    private final String[] hosts = new String[maxSize];
    private final int[] ports = new int[maxSize];
    private int size = 0;

    public int search(String s) {
        for (int i = 0; i < size; ++i) {
            if (names[i].equals(s)) return i;
        }
        return -1;
    }

    public boolean insert(String s, String hostName, int portNumber) {
        int oldIndex = search(s);   // Is it already there
        if (oldIndex == -1 && size < maxSize) {
            names[size] = s;
            hosts[size] = hostName;
            ports[size] = portNumber;
            ++size;
            return true;
        } else {
            return false;
        }
    }

    public int getPort(int index) {
        return ports[index];
    }

    public String getHostName(int index) {
        return hosts[index];
    }

    public int getSize() {
        return size;
    }
}