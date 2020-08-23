package client;

import server.*;

import java.io.IOException;

public class ClientAppKlaus {
    public static void main(String[] args) {
        try {
            new Client(Server.PORT, "l4 p4 Klaus").start();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}