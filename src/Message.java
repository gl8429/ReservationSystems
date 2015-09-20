/**
 * Created by Lucifer on 9/19/15.
 */

import java.util.StringTokenizer;

public class Message {
    int srcId, destId;
    String tag;
    String msgBuf;

    public Message(int srcId, int destId, String msgType, String buf) {
        this.srcId = srcId;
        this.destId = destId;
        tag = msgType;
        msgBuf = buf;
    }

    public int getSrcId() {
        return srcId;
    }

    public int getDestId() {
        return destId;
    }

    public String getTag() {
        return tag;
    }

    public String getMessage() {
        return msgBuf;
    }

    public int getMessageInt() {
        StringTokenizer st = new StringTokenizer(msgBuf);
        return Integer.parseInt(st.nextToken());
    }

    public static Message parseMessage(StringTokenizer st) {
        int srcId = Integer.parseInt(st.nextToken());
        int destId = Integer.parseInt(st.nextToken());
        String tag = st.nextToken();
        String buf = st.nextToken();
        return new Message(srcId, destId, tag, buf);
    }

    public String toString() {
        String s = String.valueOf(srcId) + " " + String.valueOf(destId) + " " + tag + " " + msgBuf + "#";
        return s;
    }
}
