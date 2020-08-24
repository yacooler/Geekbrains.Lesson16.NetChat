package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
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

                    } catch (IOException|SQLException e) {
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

    public void doAuthorization() throws IOException, SQLException {
        while (true) {
            System.out.println("Waiting for auth...");
            ChatMessage message = receiveChatMessageFromClient();
            if  ( message.getContent() == ChatMessage.CONT_AUTH ){
                String[] credentials = message.getMessage().split("\\s");
                AuthService.Record possibleRecord = server.getAuthService().findRecord(credentials[0], credentials[1]);
                if (possibleRecord != null) {
                    if (!server.isOccupied(possibleRecord)) {
                        record = possibleRecord;
                        sendChatMessageToClient(new ChatMessage( ChatMessage.CONT_AUTH_DONE, ChatMessage.MESSAGE_AUTH_DONE));
                        sendChatMessageToClient(new ChatMessage( ChatMessage.CONT_RENAME, record.getName()));
                        server.handleMessage(new ChatMessage(ChatMessage.CONT_SERVER_MESSAGE, "New user logged in:" + record.getName()));
                        server.subscribe(this);
                        break;
                    } else {
                        sendChatMessageToClient(new ChatMessage( ChatMessage.CONT_ERROR, String.format("Current user [%s] is already occupied", possibleRecord.getName())));
                    }
                } else {
                    sendChatMessageToClient(new ChatMessage( ChatMessage.CONT_ERROR, "User no found"));
                }
            }
        }
    }

    /**
     * Обработка событий, пришедших от клиента на сервер
      * @throws IOException
     */
    public void handleMessages() throws IOException, SQLException {
        while (true) {
            ChatMessage message = receiveChatMessageFromClient();
            if (message.getContent() == ChatMessage.CONT_END) {
                return;
            }
            server.handleMessage(message);
        }
    }


    public void sendChatMessageToClient(ChatMessage message) throws IOException {
        out.writeUTF(message.buildToSend());
    }

    public ChatMessage receiveChatMessageFromClient() throws IOException {
        return new ChatMessage( in.readUTF() );
    }

    public void closeConnection(){
        try {
            server.unsubscribe(this);
            server.handleMessage(new ChatMessage(ChatMessage.CONT_SERVER_MESSAGE, "User has left the chat:" + record.getName()));
            in.close();
        } catch (IOException|SQLException e) {
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
