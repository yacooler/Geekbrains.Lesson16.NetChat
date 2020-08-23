package client;

import server.*;

import java.io.IOException;

public class ClientApp2 {
    public static void main(String[] args) {
        try {
            new Client(Server.PORT).start("l2","p2");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}