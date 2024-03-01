package cli.command;

import app.AppConfig;
import app.file.FileInfo;
import app.file.FileUtil;
import mutex.Token;
import java.util.List;

public class AddCommand implements CLICommand {

    @Override
    public String commandName() {
        return "add";
    }

    @Override
    public void execute(String args) {

        if (args == null || args.isEmpty()) {
            AppConfig.timestampedStandardPrint("Invalid argument for add command. Should be add path.");
            return;
        }

        String path = args.replace('/' , '\\');

        Token.lock();//LOCK 1a

        if (FileUtil.isPathFile(AppConfig.ROOT_DIR, path)) {
            FileInfo fileInfo = FileUtil.getFileInfoFromPath(AppConfig.ROOT_DIR, path);
            if (fileInfo != null) {
                AppConfig.chordState.addToCloud(fileInfo, AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort());
            }
        }
        else {
            List<FileInfo> fileInfoList = FileUtil.getDirectoryInfoFromPath(AppConfig.ROOT_DIR, path);
            if (!fileInfoList.isEmpty()) {
                for (FileInfo fileInfo : fileInfoList) {
                    AppConfig.chordState.addToCloud(fileInfo, AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort());
                }
            }
        }

        Token.unlock(); //UNLOCK 1a
    }
}
