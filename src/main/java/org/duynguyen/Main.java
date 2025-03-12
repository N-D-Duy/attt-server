package org.duynguyen;

import org.duynguyen.database.DbManager;
import org.duynguyen.server.Server;
import org.duynguyen.utils.Config;
import org.duynguyen.utils.Utils;
import org.duynguyen.utils.Log;

public class Main {
    public static boolean isStop = false;
    public static void main(String[] args) {
        if(Config.getInstance().load()){
            if (!DbManager.getInstance().start()) {
                return;
            }
            if (Utils.availablePort(Config.getInstance().getPort())) {
                new Main();
                if (!Server.init()) {
                    Log.error("Khoi tao that bai!");
                    return;
                }
                new Thread(Server::start).start();
            } else {
                Log.error("Port " + Config.getInstance().getPort() + " da duoc su dung!");
            }
        } else {
            Log.error("Khoi tao that bai!");
        }
    }
}
