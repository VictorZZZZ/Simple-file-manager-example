package com.victorz.geekbrains.file.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfo {
    public static final String UP_TOKEN = "..";
    private String filename;
    private long size;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isDirectory(){
        return size == -1L;
    }
    public boolean isUpElement(){
        return size == -2L;
    }

    public FileInfo(String filename, long size){
        this.filename = filename;
        this.size = size;
    }

    public FileInfo(Path path){
        try {
            this.filename = path.getFileName().toString();
            if(Files.isDirectory(path)){
                this.size = -1L;
            } else {
                this.size = Files.size(path);
            }

        } catch (IOException e) {
                throw new RuntimeException("Something went wrong " + path.toAbsolutePath().toString());
        }
    }
}
