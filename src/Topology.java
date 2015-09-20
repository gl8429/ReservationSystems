/**
 * Created by Lucifer on 9/19/15.
 */

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Topology {
    /* read the topology of the underlying network*/
    public static void readNeighbors(int myId, int N, LinkedList<Integer> neighbors) {
        Util.println("Reading topology");
        try {
            BufferedReader dIn = new BufferedReader(new FileReader("topology" + myId));
            StringTokenizer st = new StringTokenizer(dIn.readLine());
            while (st.hasMoreTokens()) {
                int neighbor = Integer.parseInt(st.nextToken());
                neighbors.add(neighbor);
            }
        } catch (FileNotFoundException e) {
            for (int j = 0; j < N; ++j) {
                if (j != myId) neighbors.add(j);
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        Util.println(neighbors.toString());
    }
}