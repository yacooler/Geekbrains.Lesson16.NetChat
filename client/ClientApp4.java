package client;

import server.*;

import java.io.IOException;

public class ClientApp4 {
    public static void main(String[] args) {
        try {
            new Client(Server.PORT).start("l4","p4");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}