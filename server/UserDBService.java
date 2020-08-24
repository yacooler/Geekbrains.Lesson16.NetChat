package server;

import java.sql.*;

public class UserDBService {
    private final Connection connection;

    public UserDBService(Connection connection) {
        this.connection = connection;
    }


    public void setUserNameById(String name, int id) throws SQLException{
        String sql = "UPDATE CHAT_USERS SET name = ? where id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, name);
            statement.setInt(2, id);

            //Если не нашли пользователя = косяк, так быть не должно
            if (statement.executeUpdate() == 0){
                throw new RuntimeException(String.format("Не удалось найти пользователя с ID = %d", id));
            };


        }
    }

    public AuthService.Record getUserByLoginAndPassword(String login, String password) throws SQLException{

        String sql = "SELECT id, name FROM CHAT_USERS where login = ? and password = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, login);
            statement.setString(2, password);

            statement.execute();

            ResultSet resultSet = statement.getResultSet();

            if (resultSet.first()){
                int id = resultSet.getInt(1);
                String name = resultSet.getString(2);
                return new AuthService.Record(id, name, login, password);
            }
        }

        return null;
    }

    public AuthService.Record getUserByName(String name) throws SQLException{

        String sql = "SELECT id, login, password FROM CHAT_USERS where name = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, name);

            statement.execute();

            ResultSet resultSet = statement.getResultSet();

            if (resultSet.first()){
                int id = resultSet.getInt(1);
                String login = resultSet.getString(2);
                String password = resultSet.getString(3);
                return new AuthService.Record(id, name, login, password);
            }
        }

        return null;
    }

    /**
     * Создание каркаса базы. По идее тут ему не место, но не хочется делить на несколько классов работу с одной
     * таблицей в БД
     * @throws SQLException
     */
    public void initDatabase() throws SQLException{

        //try with resource - автоматически закроет statement
        try (Statement statement = connection.createStatement()) {

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
