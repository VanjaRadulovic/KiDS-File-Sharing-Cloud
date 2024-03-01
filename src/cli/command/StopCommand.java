package cli.command;

import app.AppConfig;
import app.BuddyService;
import cli.CLIParser;
import servent.SimpleServentListener;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class StopCommand implements CLICommand {

	private CLIParser parser;
	private SimpleServentListener listener;

	private BuddyService buddyService;
	
	public StopCommand(CLIParser parser, SimpleServentListener listener, BuddyService buddyService) {
		this.parser = parser;
		this.listener = listener;
		this.buddyService = buddyService;
	}
	
	@Override
	public String commandName() {
		return "stop";
	}

	@Override
	public void execute(String args) {
		AppConfig.timestampedStandardPrint("Stopping...");
		parser.stop();
		listener.stop();
		buddyService.stop();

//	todo vrati	PullCollector.stop();

		String bsAddress = AppConfig.BOOTSTRAP_ADDRESS;
		int bsPort = AppConfig.BOOTSTRAP_PORT;

		try {
			Socket bsSocket = new Socket(bsAddress, bsPort);

			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("Exit\n" + AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort() + "\n");
			bsWriter.flush();

			bsSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
