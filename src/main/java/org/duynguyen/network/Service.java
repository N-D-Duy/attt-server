package org.duynguyen.network;

import org.duynguyen.models.Client;
import org.duynguyen.protocol.Message;

import org.duynguyen.constants.CMD;
import org.duynguyen.utils.Log;
import lombok.Getter;
import lombok.Setter;

import java.io.DataOutputStream;

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
}

