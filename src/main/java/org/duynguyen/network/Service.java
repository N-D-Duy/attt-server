package org.duynguyen.network;

import org.duynguyen.models.Client;
import org.duynguyen.protocol.Message;
import org.duynguyen.server.ServerManager;

import org.duynguyen.constants.CMD;
import org.duynguyen.utils.Log;
import lombok.Getter;
import lombok.Setter;

import java.io.DataOutputStream;
import java.util.List;

public class Service extends BaseService{
    public final Session session;
    @Setter
    @Getter
    public Client client;

    public Service(Session session) {
        this.session = session;
    }

    @Override
    public void sendMessage(Message ms) {
        if (this.session != null) {
            this.session.sendMessage(ms);
        }
    }

    public void serverMessage(String text) {
        try {
            Log.info("Server message: " + text);
            Message ms = new Message(CMD.SERVER_MESSAGE);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(text);
            ds.flush();
            sendMessage(ms);
            ms.cleanup();
        } catch (Exception ex) {
            Log.error("Send server message error: " + ex.getMessage());
        }
    }

    public void loginOk() {
        try {
            Message ms = messageAuth(CMD.LOGIN_OK);
            DataOutputStream ds = ms.writer();
            List<Client> clientList  = ServerManager.getClients();
            ds.writeInt(clientList.size());
            for(Client c : clientList){
                ds.writeInt(c.id);
                ds.writeUTF(c.username);
            }
            ds.flush();
            sendMessage(ms);
            ms.cleanup();
        } catch (Exception ex) {
            Log.error("Send login OK error: " + ex.getMessage());
        }
    }

    public void sendUserList(Client cl) {
        try {
            Message ms = messageAuth(CMD.UPDATE_USER_LIST);
            DataOutputStream ds = ms.writer();
            List<Client> clientList  = ServerManager.getClients();
            ds.writeInt(clientList.size());
            for(Client c : clientList){
                ds.writeInt(c.id);
                ds.writeUTF(c.username);
            }
            ds.flush();
            cl.session.sendMessage(ms);
            ms.cleanup();
        } catch (Exception ex) {
            Log.error("Send user list error: " + ex.getMessage());
        }
    }


    public Message messageAuth(int command) {
        Message ms = new Message(CMD.AUTH);
        try (DataOutputStream ds = ms.writer()) {
            ds.writeByte(command);
            return ms;
        } catch (Exception ex) {
            Log.error(ex);
        }
        return null;
    }

    public Message messageNotAuth(int command) {
        Message ms = new Message(CMD.NOT_AUTH);
        try (DataOutputStream ds = ms.writer()) {
            ds.writeByte(command);
            return ms;
        } catch (Exception ex) {
            Log.error(ex);
        }
        return null;
    }
}

