package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import client.gui.ChatFrame;
import client.gui.LoginFrame;
import server.ChatMessage;
import server.Server;

public class Client implements Authorizable {
    private int port;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ChatFrame chatFrame;
    private String userName;



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

        //Основное окно чата. Попробую лямбды
        chatFrame = new ChatFrame("Клиент чата " + userName, message -> sendMessage(message));
    }

    /**
     * Запуск клиента. Логин и пароль передаются для удобства отладки, ну или если мы их сохраним в реестре
     */
    public void start(String login, String password) throws IOException {

        init(login, password);

        //Получение сообщений от сервера
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    while (true) {

                        ChatMessage message = receiveChatMessage();

                        if (message.isEndMessage()) {
                            System.out.println("Сессия закрыта сервером!");
                            break;
                        }

                        if (message.isPrivateMessage()){

                            //Приватное сообщение бывает входящим и исходящим
                            if (!message.getSender().equals(userName)) {
                                chatFrame.prepareMessage(Server.WHISP_MESSAGE + message.getSender() + " ");
                                chatFrame.pushMessage(Server.PRIVATE_MESSAGE + " from " + message.getSender() + ":" + message.getMessage());
                            } else {
                                chatFrame.pushMessage(Server.PRIVATE_MESSAGE + " to " + message.getRecipient() + ":" + message.getMessage());
                            }

                        } else {
                            chatFrame.pushMessage(message.getSender() + ":" + message.getMessage());
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }

    /**
     * Метод проверки авторизации из интерфейса AutorizationMaker
     * Получает логин и пароль, соединяется с сервером и проверяет, существует ли
     * пользователь с указанным логином и паролем. Сервер возвращает имя текущего пользователя
     */
    @Override
    public boolean makeAuthorization(String login, String password) {
        try {

            sendChatMessage(Server.AUTH_MESSAGE + " " + login + " " + password);

            if (receiveChatMessage().getMessage().startsWith(Server.AUTH_DONE_MESSAGE)) {
                System.out.println("Authorized");
                userName = receiveChatMessage().getMessage();
                return true;
            }
        }
        catch (IOException exception){
            throw new RuntimeException("Ошибка авторизации", exception);
        }
        return false;
    }

    /**
     * Отправка сообщения на сервер с клиента
     */
    private void sendMessage(String message){
        if (message.isBlank()) return;

        try {
            sendChatMessage(new ChatMessage(userName, message));
        } catch (IOException ioException) {
            throw new RuntimeException("Sending message error", ioException);
        }

    }


    private void sendChatMessage(ChatMessage message) throws IOException{
        if (!message.isBlank()) {
            out.writeUTF(message.buildToSend());
        };
    }

    /**
     * Перегружена для системных вызовов
     */
    private void sendChatMessage(String message) throws IOException{
        sendChatMessage(new ChatMessage(message));
    }

    private ChatMessage receiveChatMessage() throws IOException{
        return new ChatMessage(in.readUTF());
    }

}
