package server;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

import database.Manager_db;
import universal.FormaterTerminalText;
import universal.managers.CommunicateManager;
import universal.ConverterClassToJson;
import universal.managers.FileManager;
import universal.FilePart;
import universal.FileStatusComparator;
import universal.announcements.*;
import universal.models.FileStatus;
import universal.models.NextSyncTime;
import universal.models.StateServer;
import universal.models.TypeOfFile;


public class UserHandling implements Runnable {
    private final Socket socket;
    private final String usersDirPath = "server\\";
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
            logInUser(loginData);
        }
        catch (NullPointerException e) {
            return;
        }


        FormaterTerminalText.printSucess("Poprawnie zalogowany użytkownik " + loginData.ID);
        communicateManager.sendCommunicate(StateServer.CONNECTED.toString());
        communicateManager.sendCommunicate(String.valueOf(loginData.ID));

        this.userHomePath = usersDirPath + loginData.ID + loginData.pathClientArchive.replace('/', '\\');
        this.fileManager = new FileManager(this.userHomePath);

        String filesListJsonFromClient =  communicateManager.receiveCommunicate();
        ListClientsFiles comunicate = ConverterClassToJson.restoreFileInformation(filesListJsonFromClient);
        System.out.println(comunicate);

        List<FileInformation> localFiles = this.fileManager.getListOfFilesInformation();
        List<FileInformation> neededChangesFiles = FileStatusComparator.compare(comunicate.filesInformation, localFiles);



        Map<FileStatus, List<FileInformation>> separated = classifyActionOnGivenFile(neededChangesFiles);

        List<FileInformation> listFilesToDelete = separated.get(FileStatus.DELETE);

        neededChangesFiles = separated.get(FileStatus.SEND);
        List<FileInformation> movedFile = searchMovedFiled(listFilesToDelete, neededChangesFiles);



        FormaterTerminalText.printServerComunicate("\nZmiany dla serwera: ");
        FormaterTerminalText.printNormal("\tPliki do przeniesienia: ");
        printList(movedFile);
        FormaterTerminalText.printNormal("\tPliki do usunięcia: ");
        printList(listFilesToDelete);
        deleteUnnecessaryFiles(listFilesToDelete);



        System.out.println("\nZmiany wysyłąne do klienta: ");
        printList(neededChangesFiles);

        comunicate.filesInformation = neededChangesFiles;
        String requestNewData = ConverterClassToJson.convert(comunicate);
        communicateManager.sendCommunicate(requestNewData);


        if( !comunicate.filesInformation.isEmpty()) {
            FormaterTerminalText.printNormal("Pobranie " + comunicate.filesInformation.size() + " plików od klienta: " );
            for(int i=0; i<comunicate.filesInformation.size(); i++) {
                TreeMap<Integer, FilePart> partsFile = communicateManager.downloadPartsOfFile();
                fileManager.saveFileFromParts(partsFile);
            }
            fileManager.updateModificationDates(neededChangesFiles);

        }

        communicateManager.sendCommunicate(StateServer.DONE.toString());

        FormaterTerminalText.printServerComunicate("wysyłam czas następnej synhronizacji");
        communicateManager.sendCommunicate(String.valueOf(NextSyncTime.sec_5.getMilliseconds()));

        FormaterTerminalText.printServerComunicate("//////  Wymiana danych z klientem zakończona ////////////\n\n");

        cleanUP();
    }

    private void deleteUnnecessaryFiles( List<FileInformation> list) {
        Thread cleanerFile = new Thread(() -> {
            List<String> dirsToDelete = list.stream()
                    .filter(fi -> FileStatus.DELETE == fi.fileStatus && TypeOfFile.DIR.name().equals(fi.fileType))
                    .map(fi -> fi.filePath)
                    .collect(Collectors.toList());

            List<FileInformation> toDelete = list.stream()
                    .filter(fi ->
                            FileStatus.DELETE == fi.fileStatus
                                    || dirsToDelete.stream().anyMatch(dir ->
                                    fi.filePath.equals(dir) || fi.filePath.startsWith(dir + File.separator)
                            )
                    )
                    .collect(Collectors.toList());

            for (FileInformation fi : toDelete) {
                boolean deleted = fileManager.deleteFile(fi.filePath);
                if (deleted) {
                    list.removeIf(item -> item.filePath.equals(fi.filePath));
                    System.out.println("\tUsunięto: " + fi.filePath);
                } else {
                    System.out.println("Nie udało się usunąć: " + fi.filePath);
                }
            }
        });
        cleanerFile.start();
    }

    private static Map<FileStatus, List<FileInformation>> classifyActionOnGivenFile(List<FileInformation> files) {
        List<FileInformation> deletes = new ArrayList<>();
        List<FileInformation> others = new ArrayList<>();


        for (FileInformation file : files) {
            if (FileStatus.DELETE == file.fileStatus) {
                deletes.add(file);
            } else if( !file.fileType.equals(TypeOfFile.DIR.name()) && file.fileStatus != FileStatus.WITHOUT_CHANGES && file.fileSize > 0) {
                others.add(file);
            }
        }

        Map<FileStatus, List<FileInformation>> result = new HashMap<>();
        result.put(FileStatus.DELETE, deletes);
        result.put(FileStatus.SEND, others);
        return result;
    }

    private List<FileInformation> searchMovedFiled(List<FileInformation> server, List<FileInformation> fileToSend) {
        List<FileInformation> result = fileToSend.stream()
                .filter(fi1 -> server.stream().anyMatch(fi2 ->
                        fi1.fileName.equals(fi2.fileName) &&
                                fi1.modfiferTime.equals(fi2.modfiferTime) &&
                                fi1.fileSize == fi2.fileSize
                ))
                .peek(fi -> fi.fileStatus = FileStatus.MOVE)
                .collect(Collectors.toList());

        fileToSend.removeIf(fi1 -> server.stream().anyMatch(fi2 ->
                fi1.fileName.equals(fi2.fileName) &&
                        fi1.modfiferTime.equals(fi2.modfiferTime) &&
                        fi1.fileSize == fi2.fileSize
        ));

        server.removeIf(fi2 -> result.stream().anyMatch(fi1 ->
                fi1.fileName.equals(fi2.fileName) &&
                        fi1.modfiferTime.equals(fi2.modfiferTime) &&
                        fi1.fileSize == fi2.fileSize
        ));

        return result;
    }

    private void logInUser(InitClientToServer loginData ) throws NullPointerException {
        String initJsonFromClient;
        while( !Manager_db.isCorrectUser(loginData.ID, loginData.pathClientArchive) ) {
            if (loginData.ID == -1 ) {
                this.createNewUser(loginData);
            }else {
                FormaterTerminalText.printFailure("Błąd logowania do ID: " + loginData.ID);
                communicateManager.sendCommunicate(StateServer.PERMISION_DENIED.toString());

                initJsonFromClient = communicateManager.receiveCommunicate();
                loginData = ConverterClassToJson.restoreInitClientToServer(initJsonFromClient);
            }
        }
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
        if( index == 0)
            System.out.println("Brak elementów do wyświetlneia");
    }


}
