package app;

import java.io.Serializable;

/**
 * This is an immutable class that holds all the information for a servent.
 *
 * @author bmilojkovic
 */
public class ServentInfo implements Serializable {

	private static final long serialVersionUID = 4971234960037495511L;
	private final String ipAddress;
	private final int listenerPort;
	private final int chordId;

	private static volatile boolean pingPred = false;
	private static volatile boolean pingSuc = false;
	private static volatile boolean pong1 = false;

	public static boolean isPingPred() {
		return pingPred;
	}

	public static void setPingPred(boolean pingPred) {
		ServentInfo.pingPred = pingPred;
	}

	public static boolean isPingSuc() {
		return pingSuc;
	}

	public static void setPingSuc(boolean pingSuc) {
		ServentInfo.pingSuc = pingSuc;
	}

	public static boolean isPong1() {
		return pong1;
	}

	public static void setPong1(boolean pong1) {
		ServentInfo.pong1 = pong1;
	}

	public static boolean isPong2() {
		return pong2;
	}

	public static void setPong2(boolean pong2) {
		ServentInfo.pong2 = pong2;
	}

	private static volatile boolean pong2 = false;

	
	public ServentInfo(String ipAddress, int listenerPort) {
		this.ipAddress = ipAddress;
		this.listenerPort = listenerPort;
		this.chordId = ChordState.chordHash(ipAddress + ":" + listenerPort);
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public int getListenerPort() {
		return listenerPort;
	}

	public int getChordId() {
		return chordId;
	}
	
	@Override
	public String toString() {
		return "[cId: " + chordId + "|ipA: " + ipAddress + "|lP: " + listenerPort + "]";
	}

}
