package app;

import app.file.FileInfo;
import servent.message.Message;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;
import servent.message.UpdateChordMessage;
import servent.message.PullRequestMessage;
import servent.message.RemoveMessage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;




public class ChordState {

    public static int CHORD_SIZE;

    public static int chordHash(String value) {
        int hashedString = value.hashCode();
        return 61 * hashedString % CHORD_SIZE;
    }

    private int chordLevel; //log_2(CHORD_SIZE)
    private ServentInfo[] successorTable;
    private ServentInfo predecessorInfo;
    private List<ServentInfo> allNodeInfo;

    private Map<String, FileInfo> cloudMap;
    public List<FileInfo> pulledFiles;

    public List<FileInfo> buddyFiles;
    public int amountToPull;
    public int amountPulled;

    public ChordState() {
        this.chordLevel = 1;
        int tmp = CHORD_SIZE;
        while (tmp != 2) {
            if (tmp % 2 != 0) { //not a power of 2
                throw new NumberFormatException();
            }
            tmp /= 2;
            this.chordLevel++;
        }

        successorTable = new ServentInfo[chordLevel];
        for (int i = 0; i < chordLevel; i++) {
            successorTable[i] = null;
        }
        predecessorInfo = null;
        allNodeInfo = new ArrayList<>();
        cloudMap = new ConcurrentHashMap<>();
        pulledFiles = new ArrayList<>();
        amountToPull = 0;
        amountPulled = 0;
    }

    public void addPulledFile(FileInfo fi){
        pulledFiles.add(fi);
        amountPulled++;
        if (amountPulled == amountToPull){
            printPulledFiles();
        }
    }


    public void init(WelcomeMessage welcomeMsg) {
        //set a temporary pointer to next node, for sending of update message
        successorTable[0] = new ServentInfo(welcomeMsg.getSenderIpAddress(), welcomeMsg.getSenderPort());
        this.cloudMap = new ConcurrentHashMap<>(welcomeMsg.getCloudMap());

        System.out.println("- MY PREDECESSOR IS " + predecessorInfo);
        System.out.println("- MY SUCCESSORS ARE " + Arrays.toString(successorTable));

        //tell bootstrap this node is not a collider
        try {
            Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);

            PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
            bsWriter.write("New\n" + AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort() + "\n");

            bsWriter.flush();
            bsSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Returns true if we are the owner of the specified key.
     */
    public boolean isKeyMine(int key) {
        if (predecessorInfo == null) {
            return true;
        }
        int predecessorChordId = predecessorInfo.getChordId();
        int myChordId = AppConfig.myServentInfo.getChordId();
        if (predecessorChordId < myChordId) { //no overflow
            if (key <= myChordId && key > predecessorChordId) {
                return true;
            }
        } else { //overflow
            if (key <= myChordId || key > predecessorChordId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Main chord operation - find the nearest node to hop to to find a specific key.
     * We have to take a value that is smaller than required to make sure we don't overshoot.
     * We can only be certain we have found the required node when it is our first next node.
     */
    public ServentInfo getNextNodeForKey(int key) {
        if (isKeyMine(key)) {
            return AppConfig.myServentInfo;
        }

        //normally we start the search from our first successor
        int startInd = 0;

        //if the key is smaller than us, and we are not the owner,
        //then all nodes up to CHORD_SIZE will never be the owner,
        //so we start the search from the first item in our table after CHORD_SIZE
        //we know that such a node must exist, because otherwise we would own this key
        if (key < AppConfig.myServentInfo.getChordId()) {
            int skip = 1;
            while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
                startInd++;
                skip++;
            }
        }

        int previousId = successorTable[startInd].getChordId();

        for (int i = startInd + 1; i < successorTable.length; i++) {
            if (successorTable[i] == null) {
                AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
                break;
            }

            int successorId = successorTable[i].getChordId();

            if (successorId >= key) {
                return successorTable[i - 1];
            }
            if (key > previousId && successorId < previousId) { //overflow
                return successorTable[i - 1];
            }
            previousId = successorId;
        }
        //if we have only one node in all slots in the table, we might get here
        //then we can return any item
        return successorTable[0];
    }

    private void updateSuccessorTable() {
        //first node after me has to be successorTable[0]

        int currentNodeIndex = 0;
        ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
        successorTable[0] = currentNode;

        int currentIncrement = 2;

        ServentInfo previousNode = AppConfig.myServentInfo;

        //i is successorTable index
        for (int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
            //we are looking for the node that has larger chordId than this
            int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;

            int currentId = currentNode.getChordId();
            int previousId = previousNode.getChordId();

            //this loop needs to skip all nodes that have smaller chordId than currentValue
            while (true) {
                if (currentValue > currentId) {
                    //before skipping, check for overflow
                    if (currentId > previousId || currentValue < previousId) {
                        //try same value with the next node
                        previousId = currentId;
                        currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
                        currentNode = allNodeInfo.get(currentNodeIndex);
                        currentId = currentNode.getChordId();
                    } else {
                        successorTable[i] = currentNode;
                        break;
                    }
                } else { //node id is larger
                    ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
                    int nextNodeId = nextNode.getChordId();
                    //check for overflow
                    if (nextNodeId < currentId && currentValue <= nextNodeId) {
                        //try same value with the next node
                        previousId = currentId;
                        currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
                        currentNode = allNodeInfo.get(currentNodeIndex);
                        currentId = currentNode.getChordId();
                    } else {
                        successorTable[i] = currentNode;
                        break;
                    }
                }
            }
        }
    }

    /**
     * This method constructs an ordered list of all nodes. They are ordered by chordId, starting from this node.
     * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do the rest of the work.
     */
    public void addNodes(List<ServentInfo> newNodes) {
        allNodeInfo.addAll(newNodes);

        allNodeInfo.sort(new Comparator<ServentInfo>() {
            @Override
            public int compare(ServentInfo o1, ServentInfo o2) {
                return o1.getChordId() - o2.getChordId();
            }
        });

        List<ServentInfo> newList = new ArrayList<>();
        List<ServentInfo> newList2 = new ArrayList<>();

        int myId = AppConfig.myServentInfo.getChordId();
        for (ServentInfo serventInfo : allNodeInfo) {
            if (serventInfo.getChordId() < myId) {
                newList2.add(serventInfo);
            } else {
                newList.add(serventInfo);
            }
        }

        allNodeInfo.clear();
        allNodeInfo.addAll(newList);
        allNodeInfo.addAll(newList2);

        if (newList2.size() > 0) {
            predecessorInfo = newList2.get(newList2.size() - 1);
        } else {
            predecessorInfo = newList.get(newList.size() - 1);
        }

        updateSuccessorTable();
    }

    public void addToCloud(FileInfo fileInfo, String requesterIp, int requesterPort) {
        if (!cloudMap.containsKey(fileInfo.getPath())) { //Proverimo da li vec imamo fajl, ako nemamo ododaj podatke
            cloudMap.put(fileInfo.getPath(), new FileInfo(fileInfo));
            AppConfig.timestampedStandardPrint("File " + fileInfo.getPath() + " stored successfully.");

            String nextNodeIp = AppConfig.chordState.getNextNodeIp();
            int nextNodePort = AppConfig.chordState.getNextNodePort();

            Message updateChordMsg = new UpdateChordMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    nextNodeIp, nextNodePort, requesterIp, requesterPort, fileInfo);
            AppConfig.timestampedStandardPrint("Sending inform message " + updateChordMsg);
            MessageUtil.sendMessage(updateChordMsg);

            System.out.println("- STANJE NA CLOUD-u");
            int i = 1;
            for (Map.Entry<String, FileInfo> map: cloudMap.entrySet()) {
                System.out.println(+i+":" + map.getKey() + " -- " + map.getValue());
                i++;
            }
            System.out.println("\n");
        }
        else {
            AppConfig.timestampedStandardPrint("We already have " + fileInfo.getPath());
        }
    }

    public void pullFile (String path){

        List<FileInfo> filesToPull = pullFromCloud(path);

        if (filesToPull == null) {
            AppConfig.timestampedErrorPrint("Bad pull path - " + path);
            return;
        }

        if (filesToPull.isEmpty()) {
            AppConfig.timestampedErrorPrint("No files found to pull - " + path);
            return;
        }

        pulledFiles.clear();
        amountPulled = 0;

        for (FileInfo fileToPull : filesToPull) {
            Message pullMessage = new PullRequestMessage( AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    getNextNodeIp(), getNextNodePort(), AppConfig.myServentInfo.getChordId(), fileToPull);
            MessageUtil.sendMessage(pullMessage);
            AppConfig.timestampedErrorPrint("sent pull msg " + pullMessage);
        }
        amountToPull = filesToPull.size();
    }

    public void printPulledFiles(){
        AppConfig.timestampedStandardPrint("Printing pulled files");
        for (FileInfo pulledFile: pulledFiles){
            System.out.println("\nLocation: " + pulledFile.getPath() );
            System.out.println("File name: " + pulledFile.getContent());
        }
    }

    public List<FileInfo> pullFromCloud(String path){

        if (cloudMap.containsKey(path)){
            List<FileInfo> filesToReturn = new ArrayList<>();

            FileInfo requestedFileInfo = cloudMap.get(path); //file ili dir

            if (!requestedFileInfo.isDirectory()) {//ako je file samo ga vrati
                filesToReturn.add(requestedFileInfo);
                return filesToReturn;
            }

            //ako je dir onda iskopaj sve filove koji su nam unutar dira
            List<String> allDirSubFilePaths = getAllFilesFromDir(requestedFileInfo);

            for (String pathKey: allDirSubFilePaths){
                if (cloudMap.containsKey(pathKey))
                    filesToReturn.add(cloudMap.get(pathKey));
            }


            return filesToReturn;
        }
        else return null;

    }

    private List<String> getAllFilesFromDir(FileInfo dirInfo){
        List<String> filePaths = new ArrayList<>();
        for (String path: dirInfo.getSubFiles()) {
            if (path.contains(".")) filePaths.add(path);//ako ima tacku znaci da je neki file
            else filePaths.addAll(getAllFilesFromDir(cloudMap.get(path))); //ako nema znaci da je dir i daj mi njegove subdirove
        }
        return filePaths;
    }

    public void removeFileFromCloud(String path){
        if (!cloudMap.containsKey(path)) AppConfig.timestampedErrorPrint("File already removed - " + path);

        FileInfo fileToRemove = cloudMap.get(path);
        if (fileToRemove.isFile()){
            cloudMap.remove(path);
            AppConfig.timestampedStandardPrint("Removing file" + path + " from virtual memory");
            Message removeMessage = new RemoveMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    getNextNodeIp(), getNextNodePort(), path);
            MessageUtil.sendMessage(removeMessage);
        }
        else {
            for (String dirFileToRemove: getAllFilesFromDir(fileToRemove)) {
                cloudMap.remove(dirFileToRemove);
                Message removeMessage = new RemoveMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                        getNextNodeIp(), getNextNodePort(), path);
                MessageUtil.sendMessage(removeMessage);
            }
        }
    }



    public ServentInfo[] getSuccessorTable() {
        return successorTable;
    }

    public void setCloudMap(Map<String, FileInfo> cloudMap) {
        this.cloudMap = cloudMap;
    }

    public Map<String, FileInfo> getCloudMap() {
        return cloudMap;
    }

    public int getNextNodePort() {
        return successorTable[0].getListenerPort();
    }

    public String getNextNodeIp() {
        return successorTable[0].getIpAddress();
    }

    public ServentInfo getPredecessor() {
        return predecessorInfo;
    }

    public void setPredecessor(ServentInfo newNodeInfo) {
        this.predecessorInfo = newNodeInfo;
    }

    public List<FileInfo> getPulledFiles() {
        return pulledFiles;
    }

    public void setPulledFiles(List<FileInfo> pulledFiles) {
        this.pulledFiles = pulledFiles;
    }

    public boolean isCollision(int chordId) {
        if (chordId == AppConfig.myServentInfo.getChordId()) {
            return true;
        }
        for (ServentInfo serventInfo : allNodeInfo) {
            if (serventInfo.getChordId() == chordId) {
                return true;
            }
        }
        return false;
    }

}
