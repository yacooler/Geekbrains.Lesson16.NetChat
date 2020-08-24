package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import client.gui.ChatFrame;
import client.gui.LoginFrame;
import server.ChatMessage;

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
        chatFrame = new ChatFrame("Клиент чата " + userName, message -> sendChatMessage(new ChatMessage(userName, message)));
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

                        if (message.getContent()==ChatMessage.CONT_END) {
                            System.out.println("Сессия закрыта сервером!");
                            break;
                        }

                        if (message.getContent()==ChatMessage.CONT_RENAME_DONE){
                            userName = message.getMessage();
                            chatFrame.setTitle("Клиент чата " + userName);
                            continue;
                        }

                        if (message.getContent()==ChatMessage.CONT_WISP_MESSAGE){
                            //Приватное сообщение бывает входящим и исходящим
                            if (!message.getSender().equals(userName)) {
                                chatFrame.prepareMessage(ChatMessage.MESSAGE_WISP + " " + message.getSender() + " ");
                                chatFrame.pushMessage("PM from " + message.getSender() + ":" + message.getMessage());
                            } else {
                                chatFrame.pushMessage("PM to " + message.getRecipient() + ":" + message.getMessage());
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

            sendChatMessage(new ChatMessage(ChatMessage.CONT_AUTH, login + " " + password) );

            if (receiveChatMessage().getContent() == ChatMessage.CONT_AUTH_DONE) {
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


    private void sendChatMessage(ChatMessage message){
        if (!message.isBlank()) {
            try {
                out.writeUTF(message.buildToSend());
            } catch (IOException ioException) {
                throw new RuntimeException("Ошибка отправки сообщения", ioException);
            }
        };
    }


    private ChatMessage receiveChatMessage() throws IOException{
        return new ChatMessage(in.readUTF());
    }

}
