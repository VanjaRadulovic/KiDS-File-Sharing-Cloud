package app;

import servent.handler.PingHandler;
import servent.message.Message;
import servent.message.PingMessage;
import servent.message.util.MessageUtil;

public class PingPong implements Runnable{

    private String node;
    public PingPong (String node){
        this.node = node;
    }
    @Override
    public void run() {

        if(node.equals("Pred")){
            Message pingMsgPred = new PingMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    AppConfig.chordState.getPredecessor().getIpAddress(),
                    AppConfig.chordState.getPredecessor().getListenerPort(), node);
            AppConfig.timestampedStandardPrint("Sending PING message to Predecessor" + pingMsgPred);
            MessageUtil.sendMessage(pingMsgPred);

            AppConfig.myServentInfo.setPingPred(true);

            try {
                Thread.sleep(AppConfig.WEAK_TIMEOUT);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if( AppConfig.myServentInfo.isPingPred()){
                AppConfig.timestampedStandardPrint("Sending PING message to Predecessor after WEAK_TIMEOUT" + pingMsgPred);
                MessageUtil.sendMessage(pingMsgPred);
                AppConfig.myServentInfo.setPingPred(true);

                try {
                    Thread.sleep(AppConfig.STRONG_TIMEOUT);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if(AppConfig.myServentInfo.isPingPred()){
                    //OTKAZAO NAM JE NODE MORAMO DA RADIMO BACKUP NJEGOV
                    AppConfig.timestampedStandardPrint("STRONG TIME OUT");
                }

            }
        }
        else{

            Message pingMsgSuc = new PingMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    AppConfig.chordState.getSuccessorTable()[0].getIpAddress(),
                    AppConfig.chordState.getSuccessorTable()[0].getListenerPort(), "Suc");
            AppConfig.timestampedStandardPrint("Sending PING message to Successor" + pingMsgSuc);
            MessageUtil.sendMessage(pingMsgSuc);

            AppConfig.myServentInfo.setPingSuc(true);

            try {
                Thread.sleep(AppConfig.WEAK_TIMEOUT);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if( AppConfig.myServentInfo.isPingSuc()){
                AppConfig.timestampedStandardPrint("Sending PING message to Successor after WEAK_TIMEOUT" + pingMsgSuc);
                MessageUtil.sendMessage(pingMsgSuc);
                AppConfig.myServentInfo.setPingSuc(true);

                try {
                    Thread.sleep(AppConfig.STRONG_TIMEOUT);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if(AppConfig.myServentInfo.isPingSuc()){
                    //OTKAZAO NAM JE NODE MORAMO DA RADIMO BACKUP NJEGOV
                    AppConfig.timestampedStandardPrint("STRONG TIME OUT");
                }
            }
        }

    }
}
