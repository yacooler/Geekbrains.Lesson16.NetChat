package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import server.Server;

public class Client {
    private int port;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ChatFrame chatFrame;
    private String logPass;


    public Client(int port, String logPass) {
        this.port = port;
        this.logPass = logPass;
    }

    private void init() throws IOException {
        socket = new Socket("localhost", port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        chatFrame = new ChatFrame("Клиент чата " + logPass,
            new MessageListener() {
                     @Override
                     public void messagePerformed(String message) {
                         try {
                             out.writeUTF(message);
                         } catch (IOException ioException) {
                             throw new RuntimeException("Sending message error", ioException);
                         }
                     }
                 });
    }

    public void start() throws IOException {

        init();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    doAuthorization();

                    while (true) {
                        String message = in.readUTF();
                        if (message.equals(Server.END_MESSAGE)) {
                            System.out.println("Session closed. Cau!");
                            break;
                        }

                        if (message.startsWith(Server.PRIVATE_MESSAGE)){
                            chatFrame.prepareMessage(Server.WHISP_MESSAGE + message.split(":")[0].split("\\s")[Server.PRIVATE_MESSAGE.split("\\s").length] + " ");
                        }

                        chatFrame.pushMessage(message);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }

    private void doAuthorization() throws IOException {
        out.writeUTF(Server.AUTH_MESSAGE + " " + logPass);
        while (true) {
            String message = in.readUTF();
            if (message.startsWith(Server.AUTH_DONE_MESSAGE)) {
                System.out.println("Authorized");
                break;
            }
        }
    }
}
