package org.duynguyen.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.duynguyen.constants.CMD;
import org.duynguyen.models.Client;
import org.duynguyen.protocol.Message;
import org.duynguyen.server.ServerManager;
import org.duynguyen.utils.Log;

public class FileTransferHandler {
    public FileTransferSession session;
    public FileTransferHandler(FileTransferSession session){
        this.session = session;
    }
    public void handleFileChunk(Message message) {
        try (DataInputStream dis = message.reader()) {


            // Forward the chunk to the receiver
            int receiverId = session.getReceiverId();
            Client receiver = ServerManager.findClientByID(receiverId);

            if (receiver == null) {
                // Receiver is offline, cancel the transfer
                handleReceiverOffline(session);
                return;
            }

            // Read chunk size for progress tracking
            int chunkSize = dis.readInt();
            session.addBytes(chunkSize);

            // Forward the message
            receiver.session.sendMessage(message);

            Log.info("Forwarded chunk for file " + session.getFileName() + ": " +
                    String.format("%.2f%%", session.getProgress() * 100));

        } catch (IOException ex) {
            Log.error("Handle file chunk error: " + ex.getMessage());
        }
    }

    private boolean isSessionValid(Message message) {
        try(DataInputStream dis = message.reader()) {
            String transferId = dis.readUTF();
            FileTransferSession session = ServerManager.getSession(transferId);
            if (session == null) {
                Log.error("File transfer session not found: " + transferId);
                return true;
            }
            return false;
        } catch (IOException ex) {
            Log.error("Error checking session validity: " + ex.getMessage());
            return true;
        }
    }

    public void handleChunkAck(Message message) {
        try {
            if(isSessionValid(message)) {
                return;
            }
            // Forward the ACK to sender
            int senderId = session.getSenderId();
            Client sender = ServerManager.findClientByID(senderId);

            if (sender == null) {
                // Sender is offline, cancel the transfer
                handleSenderOffline(session);
                return;
            }

            // Forward the message
            sender.session.sendMessage(message);

        } catch (Exception ex) {
            Log.error("Handle chunk ACK error: " + ex.getMessage());
        }
    }

    public void handleChunkError(Message message) {
        try {
            if(isSessionValid(message)) {
                Log.error("File transfer session not found: " + session.getSenderId());
                return;
            }

            // Forward the error to sender
            int senderId = session.getSenderId();
            Client sender = ServerManager.findClientByID(senderId);

            if (sender == null) {
                // Sender is offline, cancel the transfer
                handleSenderOffline(session);
                return;
            }

            // Forward the message
            sender.session.sendMessage(message);

        } catch (Exception ex) {
            Log.error("Handle chunk error: " + ex.getMessage());
        }
    }

    public void handleTransferEnd(Message message) {
        try {
            if(isSessionValid(message)) {
                Log.error("File transfer session not found: " + session.getSenderId());
                return;
            }

            // Forward to receiver
            int receiverId = session.getReceiverId();
            Client receiver = ServerManager.findClientByID(receiverId);

            if (receiver != null) {
                receiver.session.sendMessage(message);
            }

            // Log completion
            Log.info("File transfer completed: " + session.getFileName() + " from " +
                    session.getSenderId() + " to " + session.getReceiverId());

            // Do not remove the session yet, wait for FILE_TRANSFER_COMPLETE
        } catch (Exception e) {
            Log.error("Handle transfer error: " + e.getMessage());
        }
    }

    public void handleTransferComplete(Message message) {
        try (DataInputStream dis = message.reader()) {
            String transferId = dis.readUTF();

            // Find the session
            FileTransferSession session = ServerManager.getSession(transferId);
            if (session == null) {
                Log.error("File transfer session not found: " + transferId);
                return;
            }

            // Forward to sender if needed
            int senderId = session.getSenderId();
            Client sender = ServerManager.findClientByID(senderId);

            if (sender != null) {
                sender.session.sendMessage(message);
            }

            // Remove the session
            ServerManager.activeSessions.remove(transferId);
            Log.info("File transfer session removed: " + transferId);

        } catch (IOException ex) {
            Log.error("Handle transfer complete error: " + ex.getMessage());
        }
    }

    public void handleTransferCancel(Message message) {
        try (DataInputStream dis = message.reader()) {
            String transferId = dis.readUTF();

            // Find the session
            FileTransferSession session = ServerManager.getSession(transferId);
            if (session == null) {
                Log.error("File transfer session not found: " + transferId);
                return;
            }

            // Forward to both parties
            int senderId = session.getSenderId();
            int receiverId = session.getReceiverId();

            Client sender = ServerManager.findClientByID(senderId);
            Client receiver = ServerManager.findClientByID(receiverId);

            if (sender != null) {
                sender.session.sendMessage(message);
            }

            if (receiver != null) {
                receiver.session.sendMessage(message);
            }

            // Remove the session
            ServerManager.activeSessions.remove(transferId);
            Log.info("File transfer cancelled: " + transferId);

        } catch (IOException ex) {
            Log.error("Handle transfer cancel error: " + ex.getMessage());
        }
    }

    // Helper methods
    public void handleSenderOffline(FileTransferSession session) {
        String transferId = session.getTransferId();

        // Create cancel message
        Message cancelMsg = new Message(CMD.FILE_TRANSFER_CANCEL);
        try (DataOutputStream dos = cancelMsg.writer()) {
            dos.writeUTF(transferId);
            dos.writeUTF("Sender is no longer online");

            // Send to receiver
            int receiverId = session.getReceiverId();
            Client receiver = ServerManager.findClientByID(receiverId);

            if (receiver != null) {
                receiver.session.sendMessage(cancelMsg);
            }

        } catch (IOException ex) {
            Log.error("Failed to send sender offline message: " + ex.getMessage());
        }

        // Remove the session
        ServerManager.removeSession(transferId);
        Log.info("File transfer cancelled due to sender offline: " + transferId);
    }

    public void handleReceiverOffline(FileTransferSession session) {
        String transferId = session.getTransferId();

        // Create cancel message
        Message cancelMsg = new Message(CMD.FILE_TRANSFER_CANCEL);
        try (DataOutputStream dos = cancelMsg.writer()) {
            dos.writeUTF(transferId);
            dos.writeUTF("Receiver is no longer online");

            // Send to sender
            int senderId = session.getSenderId();
            Client sender = ServerManager.findClientByID(senderId);

            if (sender != null) {
                sender.session.sendMessage(cancelMsg);
            }
        } catch (IOException ex) {
            Log.error("Failed to send receiver offline message: " + ex.getMessage());
        }

        // Remove the session
        ServerManager.removeSession(transferId);
        Log.info("File transfer cancelled due to receiver offline: " + transferId);
    }
}