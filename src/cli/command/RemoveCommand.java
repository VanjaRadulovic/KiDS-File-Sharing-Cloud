package cli.command;

import app.AppConfig;
import mutex.Token;


public class RemoveCommand implements CLICommand {

    @Override
    public String commandName() {
        return "remove";
    }

    @Override
    public void execute(String args) {

        if (args == null || args.isEmpty()) {
            AppConfig.timestampedStandardPrint("Invalid argument for add command. Should be add path.");
            return;
        }


        String path = args.replace('/' , '\\');

        //proverimo da li postoji path, i proverimo da li smo mi node koji je originalno dodao file
        if (AppConfig.chordState.getCloudMap().containsKey(path)){
            if (AppConfig.chordState.getCloudMap().get(path).getOriginalNode() == AppConfig.myServentInfo.getChordId()){
                Token.lock();//LOCK 1r
                AppConfig.chordState.removeFileFromCloud(path);
                Token.unlock();//UNLOCK 1r
            } else AppConfig.timestampedErrorPrint("This node is not the main holder of the file, can not delete selected file");
        } else AppConfig.timestampedErrorPrint("Nonexistent path " + path);

    }

}
