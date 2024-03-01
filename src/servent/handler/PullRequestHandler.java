package servent.handler;

import app.AppConfig;
import app.file.FileInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;
import servent.message.PullRequestMessage;
import servent.message.PullResponseMessage;


public class PullRequestHandler implements MessageHandler {

	private final Message clientMessage;
	
	public PullRequestHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.PULL_REQUEST) {
			PullRequestMessage request = (PullRequestMessage) clientMessage;

			if (request.getFileInfo().getOriginalNode() == AppConfig.myServentInfo.getChordId()){//ako smo mi od koga treba da povucemo
				FileInfo fileToSendBack = AppConfig.chordState.getCloudMap().get(request.getFileInfo().getPath());
				if (fileToSendBack != null) {
					Message responseMessage = new PullResponseMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
							AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
							request.getSenderIpAddress(), request.getRequesterId(), fileToSendBack);
					MessageUtil.sendMessage(responseMessage);
				}
			}
			else {
				Message pullRequest = new PullRequestMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
						AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),request.getRequesterId(), request.getFileInfo());
				MessageUtil.sendMessage(pullRequest);
			}

		} else {
			AppConfig.timestampedErrorPrint("Pull get handler got a message that is not PULL_REQUEST");
		}
	}

}