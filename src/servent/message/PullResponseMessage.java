package servent.message;



import app.file.FileInfo;

import java.io.Serial;

public class PullResponseMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = -6213394344524749872L;

	private final String requesterIpAddress;
	private final int requesterId;
	private final FileInfo fileInfo;

	public PullResponseMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,
							   String requesterIpAddress, int requesterId, FileInfo fileInfo) {

		super(MessageType.PULL_RESPONSE, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

		this.requesterIpAddress = requesterIpAddress;
		this.requesterId = requesterId;
		this.fileInfo = fileInfo;

	}

	public String getRequesterIpAddress() {
		return requesterIpAddress;
	}

	public int getRequesterId() {
		return requesterId;
	}

	public FileInfo getFileInfo() {
		return fileInfo;
	}

}
