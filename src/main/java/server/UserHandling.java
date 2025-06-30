package server;

import java.io.IOException;
import java.net.Socket;

import announcements.ListClientsFiles;
import announcements.StateServer;
import database.Manager_db;
import universal.CommunicateManager;
import universal.ConverterClassToJson;

import static java.lang.Thread.sleep;


public class UserHandling implements Runnable {
    private Socket socket;
    private Manager_db managerDatabse;
    private CommunicateManager communicateManager;


    UserHandling(Socket socket) {
        this.socket = socket;
        this.communicateManager = new CommunicateManager(socket);
        communicateManager.sendCommunicate(StateServer.BUSY.toString());
    }



    @Override
    public void run() {
        communicateManager.sendCommunicate(StateServer.READY.toString());


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
