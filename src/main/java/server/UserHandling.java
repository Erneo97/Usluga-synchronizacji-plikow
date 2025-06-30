package server;

import java.io.IOException;
import java.net.Socket;

import announcements.ListClientsFiles;
import database.Manager_db;
import universal.CommunicateManager;
import universal.ConverterClassToJson;


public class UserHandling implements Runnable {
    private Socket socket;
    private Manager_db managerDatabse;
    private CommunicateManager communicateManager;

    UserHandling(Socket socket) {
        this.socket = socket;
        this.communicateManager = new CommunicateManager(socket);
    }

    @Override
    public void run() {
        System.out.println("UserHandling started o adrersie" + socket.getInetAddress());

        String initJsonFromClient = communicateManager.receiveCommunicate();
        System.out.println("Dane logowania klienta: " + initJsonFromClient);

        String filesListJsonFromClient =  communicateManager.receiveCommunicate();
        ListClientsFiles comunicate = ConverterClassToJson.restoreFileInformation(filesListJsonFromClient);

        System.out.println("Pierwszy komunikat:\n" + comunicate);

        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
