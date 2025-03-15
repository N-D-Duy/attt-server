package org.duynguyen.network;

import org.duynguyen.constants.CMD;
import org.duynguyen.models.Client;
import org.duynguyen.protocol.Message;
import org.duynguyen.server.ServerManager;
import org.duynguyen.utils.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

import lombok.Setter;
import org.duynguyen.utils.Utils;

public class Controller implements IMessageHandler {

    public final Session session;
    @Setter public Service service;
    @Setter public Client client;
    public FileTransferSession fileTransferSession;

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
                        messageNotAuth(mss);
                        break;

                    case CMD.AUTH:
                        messageAuth(mss);
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
                    case CMD.HANDSHAKE_REQUEST:
                        handleHandshakeRequest(ms);
                        break;
                    case CMD.HANDSHAKE_ACCEPT:
                        handleHandshakeAccept(ms);
                        break;
                    case CMD.HANDSHAKE_REJECT:
                        handleHandshakeReject(ms);
                        break;
                    case CMD.FILE_INFO:
                        handleFileInfo(ms);
                        break;
                    case CMD.FILE_INFO_RECEIVED:
                        handleFileInfoReceived(ms);
                        break;
                    case CMD.FILE_CHUNK:
                        if(fileTransferSession == null) {
                            Log.error("File transfer session not found");
                            return;
                        }
                        fileTransferSession.handler.handleFileChunk(ms);
                        break;
                    case CMD.CHUNK_ACK:
                        if(fileTransferSession == null) {
                            Log.error("File transfer session not found");
                            return;
                        }
                        fileTransferSession.handler.handleChunkAck(ms);
                        break;
                    case CMD.CHUNK_ERROR:
                        if(fileTransferSession == null) {
                            Log.error("File transfer session not found");
                            return;
                        }
                        fileTransferSession.handler.handleChunkError(ms);
                        break;
                    case CMD.FILE_TRANSFER_COMPLETE:
                        if(fileTransferSession == null) {
                            Log.error("File transfer session not found");
                            return;
                        }
                        fileTransferSession.handler.handleTransferComplete(ms);
                        break;
                    case CMD.FILE_TRANSFER_END:
                        if(fileTransferSession == null) {
                            Log.error("File transfer session not found");
                            return;
                        }
                        fileTransferSession.handler.handleTransferEnd(ms);
                        break;
                    case CMD.FILE_TRANSFER_CANCEL:
                        if(fileTransferSession == null) {
                            Log.error("File transfer session not found");
                            return;
                        }
                        fileTransferSession.handler.handleTransferCancel(ms);
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


    public void handleHandshakeRequest(Message message) {
        try (DataInputStream dis = message.reader()) {
            int senderId = dis.readInt();
            int receiverId = dis.readInt();

            // Check if receiving client is online
            if (ServerManager.findClientByID(receiverId) == null) {
                // Send error message to sending client
                Message errorMsg = new Message(CMD.FILE_TRANSFER_CANCEL);
                try (DataOutputStream dos = errorMsg.writer()) {
                    dos.writeUTF("Recipient is not online");
                    Objects.requireNonNull(ServerManager.findClientByID(senderId)).session.sendMessage(errorMsg);
                } catch (IOException ex) {
                    Log.error("Failed to send error message to sending client: " + ex.getMessage());
                }
                return;
            }
            Objects.requireNonNull(ServerManager.findClientByID(receiverId)).session.sendMessage(message);
        } catch (IOException e) {
            Log.error("Handle handshake request error: " + e.getMessage());
        }
    }

    public void handleHandshakeAccept(Message message) {
        try (DataInputStream dis = message.reader()) {
            int senderId = dis.readInt();
            int receiverId = dis.readInt();
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();

            // Check if sending client is online
            if (ServerManager.findClientByID(senderId) == null) {
                // Send error message to receiving client
                Message errorMsg = new Message(CMD.HANDSHAKE_REJECT);
                try (DataOutputStream dos = errorMsg.writer()) {
                    dos.writeUTF("Sender is not online");
                    Objects.requireNonNull(ServerManager.findClientByID(receiverId)).session.sendMessage(errorMsg);
                } catch (IOException ex) {
                    Log.error("Failed to send error message to receiving client: " + ex.getMessage());
                }
                return;
            }

            // If accepted, create new transfer session, generate DES key, send to both clients
            FileTransferSession session = new FileTransferSession(senderId, receiverId);
            fileTransferSession = session;
            session.setFileInfo(fileName, fileSize);
            String transferId = session.getTransferId();
            byte[] desKey = Utils.generateDESKey();

            Message successMessage = new Message(CMD.HANDSHAKE_SUCCESS);
            try (DataOutputStream dos = successMessage.writer()) {
                dos.writeUTF(transferId);
                dos.writeInt(senderId);
                dos.writeInt(receiverId);
                dos.writeUTF(fileName);
                dos.writeLong(fileSize);
                dos.write(desKey);
            } catch (IOException ex) {
                Log.error("Failed to send success message: " + ex.getMessage());
            }

            Client sender = ServerManager.findClientByID(senderId);
            if (sender != null) {
                sender.session.sendMessage(successMessage);
            }

            Client receiver = ServerManager.findClientByID(receiverId);
            if (receiver != null) {
                receiver.session.sendMessage(successMessage);
            }
            // Add session to active sessions
            ServerManager.addSession(transferId, session);
        } catch (IOException ex) {
            Log.error("Handle handshake accept error: " + ex.getMessage());
        }
    }

    public void handleFileInfo(Message message) {
        try (DataInputStream dis = message.reader()) {
            String transferId = dis.readUTF();
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();

            // Find the session
            FileTransferSession session = ServerManager.getSession(transferId);
            if (session == null) {
                Log.error("File transfer session not found: " + transferId);
                return;
            }

            // Update file info in the session
            session.setFileInfo(fileName, fileSize);

            // Forward the message to receiver
            int receiverId = session.getReceiverId();
            Objects.requireNonNull(ServerManager.findClientByID(receiverId)).session.sendMessage(message);

        } catch (IOException ex) {
            Log.error("Handle file info error: " + ex.getMessage());
        }
    }

    public void handleFileInfoReceived(Message message) {
        try (DataInputStream dis = message.reader()) {
            String transferId = dis.readUTF();

            // Find the session
            FileTransferSession session = ServerManager.getSession(transferId);
            if (session == null) {
                Log.error("File transfer session not found: " + transferId);
                return;
            }

            // Forward the message to sender
            int senderId = session.getSenderId();
            Objects.requireNonNull(ServerManager.findClientByID(senderId)).session.sendMessage(message);

        } catch (IOException ex) {
            Log.error("Handle file info received error: " + ex.getMessage());
        }
    }

    public void handleHandshakeReject(Message message) {
        try (DataInputStream dis = message.reader()) {
            int senderId = dis.readInt();
            int receiverId = dis.readInt();

            // Check if sending client is online
            if (ServerManager.findClientByID(senderId) == null) {
                // Do nothing, the sender is offline anyway
                return;
            }

            Objects.requireNonNull(ServerManager.findClientByID(senderId)).session.sendMessage(message);
        } catch (IOException ex) {
            Log.error("Handle handshake reject error: " + ex.getMessage());
        }
    }
}
