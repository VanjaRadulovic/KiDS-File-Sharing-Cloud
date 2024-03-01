package mutex;

import app.AppConfig;
import servent.message.Message;
import servent.message.util.MessageUtil;
import servent.message.TokenMessage;

import java.util.concurrent.atomic.AtomicBoolean;

public class Token {

    private static volatile boolean haveToken = false;
    private static volatile boolean wantLock = false;

    public static void init() {
        haveToken = true;
    }

    public static void lock() {
        wantLock = true;

        int sleepTime = 100;
        while (!haveToken) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void unlock() {
        haveToken = false;
        wantLock = false;
        sendToken();
    }

    public static void receiveToken() {
        if (wantLock) {
            haveToken = true;
        } else {
            sendToken();
        }
    }

    private static void sendToken() {
        String nextNodeIp = AppConfig.chordState.getNextNodeIp();
        int nextNodePort = AppConfig.chordState.getNextNodePort();
        Message tokenMessage = new TokenMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextNodeIp, nextNodePort);
        MessageUtil.sendMessage(tokenMessage);
    }

}
