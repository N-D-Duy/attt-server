package org.duynguyen.network;

import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;


@Getter
public class FileTransferSession {
    private final String transferId;
    private String fileName;
    private long fileSize;
    private final int senderId;
    private final int receiverId;
    private long bytesTransferred;
    private boolean completed;
    public final FileTransferHandler handler;

    private static final int DEFAULT_CHUNK_SIZE = 64 * 1024;
    private final ReentrantLock lock = new ReentrantLock();

    public FileTransferSession(int senderId, int receiverId) {
        this.transferId = UUID.randomUUID().toString();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.bytesTransferred = 0;
        this.completed = false;
        handler = new FileTransferHandler(this);
    }

    public void setFileInfo(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public void addBytes(long bytes) {
        lock.lock();
        try {
            this.bytesTransferred += bytes;
            if (this.bytesTransferred >= this.fileSize) {
                this.completed = true;
            }
        } finally {
            lock.unlock();
        }
    }

    public double getProgress() {
        lock.lock();
        try {
            return (double) bytesTransferred / fileSize;
        } finally {
            lock.unlock();
        }
    }
}
