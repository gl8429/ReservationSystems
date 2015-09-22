/**
 * Created by Xingyuan on 9/19/15.
 */

public class Message {
    public enum MessageType {
        RESERVE, SEARCH, DELETE, REQUEST, RELEASE, ACK, RESULT, RECOVER
    }

    private String srcId;
    private String destId;
    private MessageType tag;
    private String msg;

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

    public static Message parseMessage(String s) {

        String srcId = s.split("#")[0];
        String destId = s.split("#")[1];
        MessageType tag = MessageType.valueOf(s.split("#")[2]);
        String msg = s.split("#")[3];
        return new Message(srcId, destId, tag, msg);
    }

    @Override
    public String toString() {
        return srcId + "#" + destId + "#" + tag.name() + "#" + msg;
    }
}
