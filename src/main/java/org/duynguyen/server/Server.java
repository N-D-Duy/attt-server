package org.duynguyen.server;


import org.duynguyen.models.Client;
import org.duynguyen.network.Session;
import org.duynguyen.utils.Config;
import org.duynguyen.utils.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Server {
    private static ServerSocket server;
    public static boolean start;

    public static boolean init(){
        start = false;
        return true;
    }
    public static void start(){
        try {
            int port = Config.getInstance().getPort();
            Log.info("Start socket port=" + port);
            server = new ServerSocket(port);
            start = true;
            int id = 0;
            Log.info("Start server Success!");
            while (start) {
                try {
                    Socket client = server.accept();
                    String ip = client.getInetAddress().getHostAddress();
                    int number = ServerManager.frequency(ip);
                    Log.info("IP: " + ip + " number: " + number);
                    if (number >= 2) {
                        client.close();
                        continue;
                    }
                    Session session = new Session(client, ++id);
                    session.IPAddress = ip;
                    ServerManager.add(ip);

                } catch (Exception e) {
                    Log.error("Can not accept client", e);
                }
            }
        } catch (IOException e) {
            Log.error("Can not start server", e);
        }
    }

    public static void close() {
        try {
            List<Client> clients = ServerManager.getClients();
            //disconnect client
            server.close();
            server = null;
            Log.info("End socket");
        } catch (IOException e) {
            Log.error("Can not close server", e);
        }
    }

}

