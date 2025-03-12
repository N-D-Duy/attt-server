package org.duynguyen.network;

import org.duynguyen.constants.CMD;
import org.duynguyen.models.Client;
import org.duynguyen.protocol.Message;
import org.duynguyen.utils.Log;
import java.io.DataInputStream;
import java.io.IOException;

import lombok.Setter;

public class Controller implements IMessageHandler {

    public final Session session;
    @Setter public Service service;
    @Setter public Client client;

    public Controller(Session session) {
        this.session = session;
    }

    @Override
    public void onMessage(Message mss) {
        if (mss != null) {
            try {
                int command = mss.getCommand();
                if (command != CMD.NOT_AUTH && command != CMD.AUTH) {
                    if (client == null || client.isCleaned) {
                        return;
                    }
                }

                switch (command) {
                    case CMD.NOT_AUTH:
                        messageAuth(mss);
                        break;

                    case CMD.AUTH:
                        messageNotAuth(mss);
                        break;

                    default:
                        Log.info("CMD: " + mss.getCommand());
                        break;
                }
            } catch (Exception e) {
                Log.error("onMessage: " + e.getMessage());
            }
        } else {
            Log.info("message is null");
        }
    }

    @Override
    public void messageAuth(Message mss) {
        if (mss != null) {
            try (DataInputStream dis = mss.reader()) {
                byte command = dis.readByte();
                switch (command) {
                    case CMD.LOGIN:
                        session.login(mss);
                        break;
                    case CMD.CLIENT_OK:
                        session.clientOk();
                        break;
                    case CMD.REGISTER:
                        session.register(mss);
                        break;
                    case CMD.GET_SESSION_ID:
                        Log.info("Client " + session.id + ": GET_SESSION_ID");
                        break;
                    default:
                        Log.info(String.format("Client %d: messageNotLogin: %d", session.id, command));
                        break;
                }
            } catch (Exception e) {
                Log.error("messageNotLogin: " + e.getMessage());
            }
        }
    }

    @Override
    public void newMessage(Message ms) {}

    @Override
    public void onConnectionFail() {
        Log.info(String.format("Client %d: Kết nối thất bại!", session.id));
    }

    @Override
    public void onDisconnected() {
        Log.info(String.format("Client %d: Mất kết nối!", session.id));
    }

    @Override
    public void onConnectOK() {
        Log.info(String.format("Client %d: Kết nối thành công!", session.id));
    }

    @Override
    public void messageNotAuth(Message ms) {
        if (ms != null) {
            try (DataInputStream dis = ms.reader()) {
                if (client.isCleaned) {
                    return;
                }
                byte command = dis.readByte();
                switch (command) {
                    case CMD.GET_USERS:
                        //
                        break;
                    default:
                        Log.info(String.format("Client %d: messageInGame: %d", session.id, command));
                        break;
                }
            } catch (IOException ex) {
                Log.error("messageSubCommand: " + ex.getMessage());
            }
        }
    }
}
