package servent.handler;

import app.AppConfig;
import servent.message.Message;
import mutex.Token;
import servent.message.MessageType;

public class JoinedHandler implements MessageHandler {

    private final Message clientMessage;

    public JoinedHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.JOINED) {
            Token.unlock();//UNLOCK 1n
        } else {
            AppConfig.timestampedErrorPrint("Joined handler got message that's not of type JOINED.");
        }

    }

}
