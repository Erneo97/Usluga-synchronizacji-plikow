package server;

import java.io.IOException;
import java.net.Socket;
import database.Manager_db;


public class UserHandling implements Runnable {
    private Socket socket;
    private Manager_db managerDatabse;

    UserHandling(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("UserHandling started o adrersie" + socket.getInetAddress());


        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
