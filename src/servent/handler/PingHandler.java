package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PongMessage;
import servent.message.util.MessageUtil;

public class PingHandler implements MessageHandler{

    private Message clientMessage;

    public PingHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.PING) {

            Message pongMsg = new PongMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    clientMessage.getSenderIpAddress(), clientMessage.getSenderPort(), clientMessage.getMessageText());
            AppConfig.timestampedStandardPrint("Sending PONG message " + pongMsg);
            MessageUtil.sendMessage(pongMsg);

        }else {
            AppConfig.timestampedErrorPrint("PING handler got something that is not ping message.");
        }

    }
}
