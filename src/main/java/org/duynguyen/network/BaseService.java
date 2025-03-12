package org.duynguyen.network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.duynguyen.protocol.Message;
import org.duynguyen.constants.CMD;
import org.duynguyen.server.Server;


public abstract class BaseService {
    public abstract void sendMessage(Message ms);
    public void serverDialog(String text) {
        try {
            Message ms = new Message(CMD.SERVER_DIALOG);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(text);
            ds.flush();
            sendMessage(ms);
            ms.cleanup();
        } catch (Exception ex) {
            Logger.getLogger(Provider.Service.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void serverMessage(String text) {
        try {
            Message ms = new Message(CMD.SERVER_MESSAGE);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(text);
            ds.flush();
            sendMessage(ms);
            ms.cleanup();
        } catch (Exception ex) {
            Logger.getLogger(Provider.Service.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void showAlert(String title, String text) {
        try {
            Message ms = new Message(CMD.ALERT_MESSAGE);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(title);
            ds.writeUTF(text);
            ds.flush();
            sendMessage(ms);
            ms.cleanup();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Message messageAuth(byte command) {
        Message ms = new Message(CMD.AUTH);
        try(DataOutputStream ds = ms.writer()) {
            ds.writeByte(command);
            return ms;
        } catch (Exception ex){
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Message messageNotAuth(byte command) {
        Message ms = new Message(CMD.NOT_AUTH);
        try(DataOutputStream ds = ms.writer()) {
            ds.writeByte(command);
            return ms;
        } catch (Exception ex){
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}

