package client;

import server.*;

import java.io.IOException;

public class ClientAppBarboss {
    public static void main(String[] args) {
        try {
            new Client(Server.PORT, "l1 p1 Barboss").start();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
