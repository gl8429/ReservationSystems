import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Xingyuan on 9/20/15.
 */
public class Seat {
    private final Map<Integer, String> seat = new Hashtable<Integer, String>();

    public Seat(int total) {
        for(int i = 0; i < total; ++i) {
            seat.put(i + 1, " ");
        }
    }

    int getLeftSeats() {
        int count = 0;
        for(Map.Entry<Integer, String> entry : seat.entrySet()) {
            if(entry.getValue().equals(" ")) {
                ++count;
            }
        }
        return count;
    }

    ArrayList<Integer> reserve(String name, int num) {
        ArrayList<Integer> assignedSeats = new ArrayList<Integer>();
        for (Map.Entry<Integer, String> entry : seat.entrySet()) {
            if (entry.getValue().equals(" ")) {
                if (num == 0)
                    break;
                seat.put(entry.getKey(), name);
                assignedSeats.add(entry.getKey());
                --num;
            }
        }
        return assignedSeats;
    }

    ArrayList<Integer> delete(String name) {
        ArrayList<Integer> deletedSeats = new ArrayList<Integer>();
        for (Map.Entry<Integer, String> entry : seat.entrySet()) {
            if (entry.getValue().equals(name)) {
                deletedSeats.add(entry.getKey());
                seat.put(entry.getKey(), " ");
            }
        }
        return deletedSeats;
    }

    ArrayList<Integer> search(String name) {
        ArrayList<Integer> reservedSeats = new ArrayList<Integer>();
        for (Map.Entry<Integer, String> entry : seat.entrySet()) {
            if (entry.getValue().equals(name)) {
                reservedSeats.add(entry.getKey());
            }
        }
        return reservedSeats;
    }
}