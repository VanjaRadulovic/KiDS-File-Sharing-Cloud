package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import mutex.Token;

public class TokenHandler implements MessageHandler {

    private final Message clientMessage;

    public TokenHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.TOKEN) {
            Token.receiveToken();
        } else {
            AppConfig.timestampedErrorPrint("Token handler got message that's not of type TOKEN.");
        }

    }

}
