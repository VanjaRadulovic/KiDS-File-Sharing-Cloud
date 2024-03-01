package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import app.file.FileInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;
import servent.message.UpdateChordMessage;

public class UpdateChordHandler implements MessageHandler {

    private final Message clientMessage;

    public UpdateChordHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.UPDATE_CHORD) {
            UpdateChordMessage additionInfoMsg = (UpdateChordMessage) clientMessage;

            String requesterNode = additionInfoMsg.getReceiverIpAddress() + ":" + additionInfoMsg.getReceiverPort();
            int key = ChordState.chordHash(requesterNode);

            if (key == AppConfig.myServentInfo.getChordId()) {
                FileInfo fileInfo = additionInfoMsg.getFileInfo();
                AppConfig.chordState.addToCloud(fileInfo, additionInfoMsg.getSenderIpAddress(), additionInfoMsg.getSenderPort());
            }
            else {
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
                Message updateChordMsg = new UpdateChordMessage(
                        additionInfoMsg.getSenderIpAddress(), additionInfoMsg.getSenderPort(),
                        nextNode.getIpAddress(), nextNode.getListenerPort(),
                        additionInfoMsg.getRequesterIpAddress(), additionInfoMsg.getRequesterPort(),
                        additionInfoMsg.getFileInfo());
                MessageUtil.sendMessage(updateChordMsg);
            }
        } else {
            AppConfig.timestampedErrorPrint("Add success handler got message that's not of type ADD_SUCCESS.");
        }

    }

}
