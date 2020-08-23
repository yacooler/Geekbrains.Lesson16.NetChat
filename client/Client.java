package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import client.gui.ChatFrame;
import client.gui.LoginFrame;
import server.Server;

public class Client implements AuthorizationChecker{
    private int port;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ChatFrame chatFrame;



    public Client(int port) {
        this.port = port;
    }

    /**
     * Инициализация подключений, вызов диалогового окна с логином и общего окна
     */
    private void init(String login, String password) throws IOException {
        //Коннекты к серверу
        socket = new Socket("localhost", port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        //Диалоговое окно с логином и паролем. В качестве метода авторизации
        //используется перегруженный метод checkAuthorization из Client
        LoginFrame loginFrame = new LoginFrame(this, login, password);
        System.out.println(loginFrame.isAuthorized());
        loginFrame.dispose();

        //Основное окно чата
        chatFrame = new ChatFrame("Клиент чата " + login,
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

    /**
     * Запуск клиента. Логин и пароль передаются для удобства отладки, ну или если мы их сохраним в реестре
     */
    public void start(String login, String password) throws IOException {

        init(login, password);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

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

    /**
     * Метод проверки авторизации из интерфейса AutorizationChecker
     * Получает логин и пароль, соединяется с сервером и проверяет, существует ли
     * пользователь с указанным логином и паролем.
     *
     */
    @Override
    public boolean checkAuthorization(String login, String password) {
        try {
            out.writeUTF(Server.AUTH_MESSAGE + " " + login + " " + password);
            String message = in.readUTF();
            if (message.startsWith(Server.AUTH_DONE_MESSAGE)) {
                System.out.println("Authorized");
                return true;
            }
        }
        catch (IOException exception){
            throw new RuntimeException("Ошибка авторизации", exception);
        }
        return false;
    }
}
