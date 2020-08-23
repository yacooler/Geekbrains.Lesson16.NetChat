package server;

import java.sql.*;


public class DatabaseAuthService implements AuthService {
    private Connection connection;

    public DatabaseAuthService(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Record findRecord(String login, String password) throws RuntimeException {

        String sql = "SELECT id, name FROM CHAT_USERS where login = ? and password = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, login);
            statement.setString(2, password);

            statement.execute();

            ResultSet resultSet = statement.getResultSet();

            if (resultSet.first()){
                int id = resultSet.getInt(1);
                String name = resultSet.getString(2);
                return new Record(id, name, login, password);
            }

        } catch (SQLException sqlException) {
            throw new RuntimeException("Не удалось прочитать запись о пользователе из БД!", sqlException);
        }

        return null;
    }
}
