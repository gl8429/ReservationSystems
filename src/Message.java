/**
 * Created by Xingyuan on 9/19/15.
 */

import java.util.StringTokenizer;

public class Message {
    public enum MessageType {
        RESERVE, SEARCH, DELETE, REQUEST, RELEASE, ACK, RESULT, AWAKE
    }

    String srcId;
    String destId;
    MessageType tag;
    String msg;

    public Message(String srcId, String destId, MessageType tag, String msg) {
        this.srcId = srcId;
        this.destId = destId;
        this.tag = tag;
        this.msg = msg;
    }

    public String getSrcId() {
        return srcId;
    }

    public String getDestId() {
        return destId;
    }

    public MessageType getTag() {
        return tag;
    }

    public String getMsg() {
        return msg;
    }

    public static Message parseMessage(StringTokenizer st) {
        String srcId = st.nextToken();
        String destId = st.nextToken();
        MessageType tag = MessageType.valueOf(st.nextToken()) ;
        String msg = st.nextToken("#");
        return new Message(srcId, destId, tag, msg);
    }

    @Override
    public String toString() {
        return srcId + " " + destId + " " + tag.name() + " " + msg + "#";
    }
}
