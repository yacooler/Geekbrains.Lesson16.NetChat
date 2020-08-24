package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class Server {


    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/netChat";
    static final String DB_USER = "sa";
    static final String DB_PASS = "";

    public static final int PORT = 8082;

    public static final String AUTH_MESSAGE = "/auth";
    public static final String AUTH_DONE_MESSAGE = "/authok";
    public static final String WHISP_MESSAGE = "/w ";
    public static final String END_MESSAGE = "/end";
    public static final String PRIVATE_MESSAGE = "PM ";

    private AuthService authService;
    private Set<ClientHandler> clientHandlers;

    private Connection dataBaseConnection = null;

    public Server() {
        this(PORT);
    }

    public Server(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            initSQLServer();
            initDatabase();

            authService = new DatabaseAuthService(dataBaseConnection);
            System.out.println("Auth is started up");

            clientHandlers = new HashSet<>();

            while (true) {
                System.out.println("Waiting for a connection...");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket);
                new ClientHandler(this, socket);
            }
        }

        catch (ClassNotFoundException | SQLException | IOException exception){
            //Все эксепшены в кучу, обрабатывать я их всё равно пока не умею
            exception.printStackTrace();
        }

        finally {
            //Закрываем коннект
            try {
                if(dataBaseConnection !=null) dataBaseConnection.close();
            } catch(SQLException se){
                se.printStackTrace();
            }
        }
    }


    public AuthService getAuthService() {
        return authService;
    }

    public synchronized boolean isOccupied(AuthService.Record record) {
        for (ClientHandler ch : clientHandlers) {
            if (ch.getRecord().equals(record)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void subscribe(ClientHandler ch) {
        clientHandlers.add(ch);
    }

    public synchronized void unsubscribe(ClientHandler ch) {
        clientHandlers.remove(ch);
    }

    public synchronized void sendMessage(ChatMessage message) throws IOException {
        if (message.isPrivateMessage()){
            sendPrivateMessage(message);
        } else {
            broadcastMessage(message);
        }
    }


    private void broadcastMessage(ChatMessage message) throws IOException{
        for (ClientHandler ch : clientHandlers){
            ch.sendChatMessageToClient(message);
        }
    }

    private void sendPrivateMessage(ChatMessage message)  throws IOException{
        ClientHandler sender = getClientHandlerByName(message.getSender());
        sender.sendChatMessageToClient(message);

        ClientHandler recipient = getClientHandlerByName(message.getRecipient());
        recipient.sendChatMessageToClient(message);
    }

    private ClientHandler getClientHandlerByName(String name){
        for (ClientHandler ch : clientHandlers){
            if (ch.getRecord().getName().toLowerCase().equals(name.toLowerCase())){
                return ch;
            }
        }
        return null;
    }

    public void initSQLServer() throws SQLException, ClassNotFoundException {
        Class.forName(Server.JDBC_DRIVER);
        dataBaseConnection = DriverManager.getConnection(Server.DB_URL,Server.DB_USER,Server.DB_PASS);
    }

    /**
     * Создаем таблицы в базе данных и наполняем юзерами, если их не было
     */
    public void initDatabase() throws SQLException{

        //try with resource - автоматически закроет statement
        try (Statement statement = dataBaseConnection.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS CHAT_USERS" +
                    "(id INTEGER IDENTITY," +
                    "name VARCHAR(255)," +
                    "login VARCHAR(255)," +
                    "password VARCHAR(255)," +
                    "PRIMARY KEY ( id ))";
            statement.executeUpdate(sql);

            sql = "SELECT COUNT(*) as cnt FROM CHAT_USERS";
            statement.execute(sql);

            ResultSet resultSet = statement.getResultSet();
            if (!resultSet.first()) {
                System.out.println("Ошибка получения количества записей в CHAT_USERS!");
                return;
            }
            if (resultSet.getInt("cnt") == 0) {

                sql = "INSERT INTO CHAT_USERS(name, login, password)" +
                        "VALUES('Barboss', 'l1', 'p1')" +
                        ",('Kelvin', 'l2', 'p2')" +
                        ",('Nicky', 'l3', 'p3')" +
                        ",('Klaus', 'l4', 'p4')";

                statement.executeUpdate(sql);
            }

        }
    }
}
