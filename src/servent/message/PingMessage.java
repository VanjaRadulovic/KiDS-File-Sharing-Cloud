package servent.message;

import java.io.Serial;


public class PingMessage extends BasicMessage{

    @Serial
    private static final long serialVersionUID = -3578851736688155676L;

    public PingMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, String messageText) {
        super(MessageType.PING, senderIpAddress, senderPort, receiverIpAddress, receiverPort , messageText);
    }


}
