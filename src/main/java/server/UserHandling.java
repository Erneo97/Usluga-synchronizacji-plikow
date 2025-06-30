package server;

import java.io.IOException;
import java.net.Socket;

import announcements.InitClientToServer;
import announcements.ListClientsFiles;
import announcements.StateServer;
import database.Manager_db;
import universal.CommunicateManager;
import universal.ConverterClassToJson;
import universal.FileManager;



public class UserHandling implements Runnable {
    private final Socket socket;
    private final CommunicateManager communicateManager;
    FileManager fileManager;
    String userHomePath;

    UserHandling(Socket socket) {
        this.socket = socket;
        this.communicateManager = new CommunicateManager(socket);
        communicateManager.sendCommunicate(StateServer.BUSY.toString());
    }



    @Override
    public void run() {
        communicateManager.sendCommunicate(StateServer.READY.toString());


        String initJsonFromClient = communicateManager.receiveCommunicate();
        InitClientToServer loginData = ConverterClassToJson.restoreInitClientToServer(initJsonFromClient);



        if (loginData.ID == -1 ) {
            this.createNewUser(loginData);
        }
        else if( !Manager_db.isCorrectUser(loginData.ID, loginData.pathClientArchive) ) {
            System.out.println("Błąd logowania do ID: " + loginData.ID);
        }
        else {
            System.out.println("Poprawnie zalogowany użytkownik " + loginData.ID);
        }


        String filesListJsonFromClient =  communicateManager.receiveCommunicate();
        ListClientsFiles comunicate = ConverterClassToJson.restoreFileInformation(filesListJsonFromClient);



        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("///////////////////////////////////////////\n\n" );
    }

    private void createNewUser(InitClientToServer loginData) {
        int id = Manager_db.addUser(loginData.IP, loginData.pathClientArchive);
        loginData.ID = id;
        this.userHomePath = "server/" + loginData.ID;
        this. fileManager = new FileManager(this.userHomePath);
        fileManager.createDirectory(this.userHomePath + loginData.pathClientArchive);

        System.out.println("Nowy użytkownik utworzony o ID " + id);
    }

}
