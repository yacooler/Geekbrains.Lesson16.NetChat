package client;

public interface Authorizable {
    boolean makeAuthorization(String login, String password);
}
