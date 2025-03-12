package org.duynguyen.network;

import org.duynguyen.protocol.Message;

public interface IMessageHandler {

    void onMessage(Message message);

    void onConnectionFail();

    void onDisconnected();

    void onConnectOK();

    void newMessage(Message ms);

    void messageAuth(Message ms);

    void messageNotAuth(Message ms);
}
