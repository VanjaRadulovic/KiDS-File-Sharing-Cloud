package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;
import servent.message.PullResponseMessage;

public class PullResponseHandler implements MessageHandler {

	private final Message clientMessage;
	
	public PullResponseHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.PULL_RESPONSE) {
			PullResponseMessage pullResponseMessage = (PullResponseMessage) clientMessage;

			if (pullResponseMessage.getRequesterId() == AppConfig.myServentInfo.getChordId()) {//mi smo originalni pull request node
				AppConfig.chordState.addPulledFile(pullResponseMessage.getFileInfo());
			}
			else {//prosledi dalje
				Message pullResponse = new PullResponseMessage(pullResponseMessage.getSenderIpAddress(), pullResponseMessage.getSenderPort(),
						AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
						pullResponseMessage.getRequesterIpAddress(), pullResponseMessage.getRequesterId(), pullResponseMessage.getFileInfo());
				MessageUtil.sendMessage(pullResponse);
			}
		} else {
			AppConfig.timestampedErrorPrint("PullRespone  handler got a message that is not PULL_RESPONSe");
		}
	}

}
