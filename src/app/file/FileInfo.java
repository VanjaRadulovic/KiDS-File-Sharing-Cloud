package app.file;

import app.ChordState;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 463426265374700139L;

    private final String path;
    private final String content;
    private final boolean isDirectory;
    private final List<String> subFiles;
    private final int OriginalNode;

    public FileInfo(String path, boolean isDirectory, String content, List<String> subFiles, int originalNode) {
        this.path = path;
        this.isDirectory = isDirectory;
        this.content = content;
        this.subFiles = new ArrayList<>();
        if (subFiles != null) {
            this.subFiles.addAll(subFiles);
        }
        this.OriginalNode = originalNode;
    }

    public FileInfo(String path, String content, int originalNode) {
        this(path, false, content,  null, originalNode);
    }

    public FileInfo(String path, List<String> subFiles, int originalNode) {
        this (path, true, "", subFiles, originalNode);
    }

    public FileInfo(FileInfo fileInfo) {
        this(fileInfo.getPath(), fileInfo.isDirectory(), fileInfo.getContent(),  fileInfo.getSubFiles(), fileInfo.getOriginalNode());
    }

    public String getPath() {
        return path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public int getOriginalNode() {
        return OriginalNode;
    }

    public boolean isFile() {
        return !isDirectory;
    }

    public String getContent() {
        return content;
    }


    public List<String> getSubFiles() {
        return subFiles;
    }

    @Override
    public int hashCode() {
        return ChordState.chordHash(getPath());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FileInfo)return o.hashCode() == this.hashCode();
        return false;

    }

    @Override
    public String toString() {
        String toReturn;
        if (isDirectory) toReturn = "[" + getPath() + " {" + getSubFiles() + "}] - inside node: " + getOriginalNode();
        else toReturn = "[" + getPath() + "] - inside node: " + getOriginalNode();
        return toReturn;
    }

}
