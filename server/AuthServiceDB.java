package server;

import java.sql.*;


public class AuthServiceDB implements AuthService {
    private final Connection connection;
    private final UserDBService userDBService;

    public AuthServiceDB(Connection connection) {
        userDBService = new UserDBService(connection);
        this.connection = connection;
    }

    @Override
    public Record findRecord(String login, String password) {
        try {
            return userDBService.getUserByLoginAndPassword(login, password);
        } catch (SQLException sqlException) {
            throw new RuntimeException("Не удалось прочитать запись о пользователе из БД!", sqlException);
        }
    }
}
