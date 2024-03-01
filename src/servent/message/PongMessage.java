package servent.message;

import java.io.Serial;

public class PongMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -3578851736688155676L;

    public PongMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, String messageText) {
        super(MessageType.PONG, senderIpAddress, senderPort, receiverIpAddress, receiverPort, messageText);
    }
}