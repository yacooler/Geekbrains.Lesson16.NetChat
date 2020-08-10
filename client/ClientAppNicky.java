package client;

import server.*;

import java.io.IOException;

public class ClientAppNicky {
    public static void main(String[] args) {
        try {
            new Client(Server.PORT, "l3 p3 Nicky").start();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}