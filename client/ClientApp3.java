package client;

import server.*;

import java.io.IOException;

public class ClientApp3 {
    public static void main(String[] args) {
        try {
            new Client(Server.PORT).start("l3","p3");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}