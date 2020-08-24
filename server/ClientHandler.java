package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class ClientHandler {
    private AuthService.Record record;
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Обмен авторизационными сообщениями
                        doAuthorization();

                        //Обработка сообщений от клиента
                        handleMessages();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        closeConnection();
                    }
                }
            })
                    .start();

        } catch (IOException e) {
           throw new RuntimeException("Client handler was not created");
        }
    }

    public AuthService.Record getRecord() {
        return record;
    }

    public void doAuthorization() throws IOException {
        while (true) {
            System.out.println("Waiting for auth...");
            String message = receiveChatMessageFromClient().getMessage();
            if (message.startsWith(Server.AUTH_MESSAGE)) {
                String[] credentials = message.split("\\s");
                AuthService.Record possibleRecord = server.getAuthService().findRecord(credentials[1], credentials[2]);
                if (possibleRecord != null) {
                    if (!server.isOccupied(possibleRecord)) {
                        record = possibleRecord;
                        sendChatMessageToClient(String.format(Server.AUTH_DONE_MESSAGE));
                        sendChatMessageToClient(record.getName());
                        server.sendMessage(new ChatMessage("System", "New user logged in:" + record.getName()));
                        server.subscribe(this);
                        break;
                    } else {
                        sendChatMessageToClient(String.format("Current user [%s] is already occupied", possibleRecord.getName()));
                    }
                } else {
                    sendChatMessageToClient(String.format("User no found"));
                }
            }
        }
    }

    public void handleMessages() throws IOException {
        while (true) {
            ChatMessage message = receiveChatMessageFromClient();
            if (message.isEndMessage()) {
                return;
            }
            server.sendMessage(message);
        }
    }


    public void sendChatMessageToClient(ChatMessage message) throws IOException {
        out.writeUTF(message.buildToSend());
    }

    /**
     * Перегружена для системных функций
     */
    public void sendChatMessageToClient(String message) throws IOException{
        sendChatMessageToClient(new ChatMessage(message));
    }

    public ChatMessage receiveChatMessageFromClient() throws IOException {
        return new ChatMessage( in.readUTF() );
    }

    public void closeConnection(){
        try {
            server.unsubscribe(this);
            server.sendMessage(new ChatMessage("System", " User has left the chat:" + record.getName()));
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientHandler that = (ClientHandler) o;
        return record.equals(that.record) &&
                server.equals(that.server) &&
                socket.equals(that.socket) &&
                in.equals(that.in) &&
                out.equals(that.out);
    }

    @Override
    public int hashCode() {
        return Objects.hash(record, server, socket, in, out);
    }
}
