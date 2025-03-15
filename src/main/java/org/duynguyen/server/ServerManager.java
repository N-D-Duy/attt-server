package org.duynguyen.server;

import org.duynguyen.models.Client;
import org.duynguyen.network.FileTransferSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerManager {
    private static final ConcurrentHashMap<Integer, Client> clientsById = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Client> clientsByUsername = new ConcurrentHashMap<>();

    private static final ArrayList<String> ips = new ArrayList<>();
    private static final ReadWriteLock lockSession = new ReentrantReadWriteLock();
    public static final ConcurrentHashMap<String, FileTransferSession> activeSessions = new ConcurrentHashMap<>();

    public static void addSession(String sessionId, FileTransferSession session) {
        activeSessions.put(sessionId, session);
    }

    public static void removeSession(String sessionId) {
        activeSessions.remove(sessionId);
    }

    public static FileTransferSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    public static List<Client> getClients() {
        return new ArrayList<>(clientsById.values());
    }

    public static int getNumberOnline() {
        return clientsById.size();
    }

    public static int frequency(String ip) {
        lockSession.readLock().lock();
        try {
            return Collections.frequency(ips, ip);
        } finally {
            lockSession.readLock().unlock();
        }
    }

    public static void add(String ip) {
        lockSession.writeLock().lock();
        try {
            ips.add(ip);
        } finally {
            lockSession.writeLock().unlock();
        }
    }

    public static void remove(String ip) {
        lockSession.writeLock().lock();
        try {
            ips.remove(ip);
        } finally {
            lockSession.writeLock().unlock();
        }
    }

    public static Client findClientByID(int id) {
        return clientsById.get(id);
    }

    public static Client findClientByUsername(String username) {
        return clientsByUsername.get(username);
    }

    public static void addClient(Client client) {
        clientsById.put(client.id, client);
        clientsByUsername.put(client.username, client);
    }

    public static void removeClient(Client client) {
        clientsById.remove(client.id);
        clientsByUsername.remove(client.username);
    }
}