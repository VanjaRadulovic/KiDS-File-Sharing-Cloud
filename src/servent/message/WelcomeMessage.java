package servent.message;

import app.file.FileInfo;

import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private final Map<String, FileInfo> cloudMap;
	
	public WelcomeMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, Map<String, FileInfo> cloudMap) {
		super(MessageType.WELCOME, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
		this.cloudMap = cloudMap;
	}

	public Map<String, FileInfo> getCloudMap() {
		return cloudMap;
	}

}
