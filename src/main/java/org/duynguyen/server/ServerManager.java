package org.duynguyen.server;

import org.duynguyen.models.Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;



public class ServerManager {
    public static final ArrayList<Client> clients = new ArrayList<>();
    private static final ArrayList<String> ips = new ArrayList<>();
    private static final ReadWriteLock lockSession = new ReentrantReadWriteLock();
    private static final ReadWriteLock lockClient = new ReentrantReadWriteLock();


    @SuppressWarnings("unchecked")
    public static List<Client> getClients() {
        return (List<Client>) clients.clone();
    }

    public static int getNumberOnline() {
        return clients.size();
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
        lockClient.readLock().lock();
        try {
            for (Client client : clients) {
                if (client.id == id) {
                    return client;
                }
            }
        } finally {
            lockClient.readLock().unlock();
        }
        return null;
    }

    public static Client findClientByUsername(String username) {
        lockClient.readLock().lock();
        try {
            for (Client client:clients) {
                if (client.username.equals(username)) {
                    return client;
                }
            }
        } finally {
            lockClient.readLock().unlock();
        }
        return null;

    }

    public static void addClient(Client client) {
        lockClient.writeLock().lock();
        try {
            clients.add(client);
        } finally {
            lockClient.writeLock().unlock();
        }
    }


    public static void removeClient(Client client) {
        lockClient.writeLock().lock();
        try {
            clients.removeIf(cl -> Objects.equals(cl.id, client.id));
        } finally {
            lockClient.writeLock().unlock();
        }
    }
}

