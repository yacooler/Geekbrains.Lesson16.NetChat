package client;

import server.*;

import java.io.IOException;

public class ClientApp1 {
    public static void main(String[] args) {
        try {
            new Client(Server.PORT).start("l1","p1");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
