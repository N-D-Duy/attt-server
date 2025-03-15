package org.duynguyen.server;

import org.duynguyen.network.FileTransferSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class FileTransferManager {
    private static final ConcurrentHashMap<String, FileTransferSession> activeSessions = new ConcurrentHashMap<>();
    private static final ReentrantLock sessionLock = new ReentrantLock();

    public static String createSession(int senderId, int receiverId) {
        sessionLock.lock();
        try {
            FileTransferSession session = new FileTransferSession(senderId, receiverId);
            activeSessions.put(session.getTransferId(), session);
            return session.getTransferId();
        } finally {
            sessionLock.unlock();
        }
    }

    public static FileTransferSession getSession(String transferId) {
        return activeSessions.get(transferId);
    }

    public static void removeSession(String transferId) {
        sessionLock.lock();
        try {
            activeSessions.remove(transferId);
        } finally {
            sessionLock.unlock();
        }
    }
}

