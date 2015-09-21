/**
 * Created by Xingyuan on 9/19/15.
 */

import java.util.StringTokenizer;

public class Message {
    public enum MessageType {
        READ, WRITE
    }

    public enum MessageSender {
        SERVER, CLIENT
    }

    String srcId;
    String destId;
    MessageType msgType;
    MessageSender msgSender;
    String msgBuf;
    int count;

    public Message(String srcId, String destId, MessageType msgType, MessageSender msgSender, String msgBuf, int count) {
        this.srcId = srcId;
        this.destId = destId;
        this.msgType = msgType;
        this.msgSender = msgSender;
        this.msgBuf = msgBuf;
        this.count = count;
    }

    public String getSrcId() {
        return srcId;
    }

    public String getDestId() {
        return destId;
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public MessageSender getMsgSender() {
        return msgSender;
    }

    public String getMsgBuf() {
        return msgBuf;
    }

    public int getCount() {
        return count;
    }

    public static Message parseMessage(StringTokenizer st) {
        String srcId = st.nextToken();
        String destId = st.nextToken();
        MessageType msgType = MessageType.valueOf(st.nextToken()) ;
        MessageSender msgSender = MessageSender.valueOf(st.nextToken());
        String msgBuf = st.nextToken();
        Integer count = Integer.parseInt(st.nextToken());
        return new Message(srcId, destId, msgType, msgSender, msgBuf, count);
    }

    @Override
    public String toString() {
        String s =  srcId + " " +
                    destId + " " +
                    msgType.name() + " " +
                    msgSender.name() + " " +
                    msgBuf + " " +
                    String.valueOf(count) + "#";
        return s;
    }
}
