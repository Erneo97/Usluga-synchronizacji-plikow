package server;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import announcements.FileInformation;
import announcements.InitClientToServer;
import announcements.ListClientsFiles;
import announcements.StateServer;
import database.Manager_db;
import universal.CommunicateManager;
import universal.ConverterClassToJson;
import universal.FileManager;
import universal.FileStatusComparator;


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

        try {
            while( !Manager_db.isCorrectUser(loginData.ID, loginData.pathClientArchive) ) {
                if (loginData.ID == -1 ) {
                    this.createNewUser(loginData);
                }else {
                    System.out.println("Błąd logowania do ID: " + loginData.ID);
                    communicateManager.sendCommunicate(StateServer.PERMISION_DENIED.toString());

                    initJsonFromClient = communicateManager.receiveCommunicate();
                    loginData = ConverterClassToJson.restoreInitClientToServer(initJsonFromClient);
                }
            }
        }
        catch (NullPointerException e) {
            return;
        }


        System.out.println("Poprawnie zalogowany użytkownik " + loginData.ID);
        communicateManager.sendCommunicate(StateServer.CONNECTED.toString());
        communicateManager.sendCommunicate(String.valueOf(loginData.ID));

        this.userHomePath = "server\\" + loginData.ID + loginData.pathClientArchive.replace('/', '\\');
        this.fileManager = new FileManager(this.userHomePath);

        String filesListJsonFromClient =  communicateManager.receiveCommunicate();
        ListClientsFiles comunicate = ConverterClassToJson.restoreFileInformation(filesListJsonFromClient);
        System.out.println(comunicate);

        List<FileInformation> localFiles = this.fileManager.getListOfFilesInformation();
        printList(localFiles);
        List<FileInformation> neededChangesFiles = FileStatusComparator.compare(comunicate.filesInformation, localFiles);

        System.out.println("Zmiany wysyłąne do klienta: \n");
        printList(neededChangesFiles);


        cleanUP();
        System.out.println("///////////////////////////////////////////\n\n" );
    }

    private void createNewUser(InitClientToServer loginData) {
        int id = Manager_db.addUser(loginData.IP, loginData.pathClientArchive);
        loginData.ID = id;
        this.userHomePath = "server/" + loginData.ID;
        fileManager.createDirectory(this.userHomePath + loginData.pathClientArchive);

        System.out.println("Nowy użytkownik utworzony o ID " + id);
    }

    private void cleanUP( ) {
        this.communicateManager.cleanUp();
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private static void printList(List<FileInformation> list) {
        int index = 0;
        for (FileInformation fileInformation : list) {
            System.out.println(index + ") " + fileInformation);
            index++;
        }
    }
}
