package server;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
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
import universal.models.StateServer;
import universal.models.TypeOfFile;

/**
 * Klasa obsługująca pojedynczego użytkownika podłączonego do serwera.
 * Przeprowadza proces logowania, synchronizacji plików i kończy połączenie.
 */
public class UserHandling implements Runnable {
    private final Socket socket;
    private final String usersDirPath = "server\\";
    private final CommunicateManager communicateManager;
    private FileManager fileManager;
    private String userHomePath;
    private final long timeSync;
    private boolean isClientConnectef ;

    /**
     * Konstruktor klasy UserHandling
     *
     * @param socket   gniazdo klienta
     * @param timeSync czas synchronizacji między kolejnymi zapytaniami klienta (w ms)
     */
    UserHandling(Socket socket, long timeSync) {
        this.socket = socket;
        this.communicateManager = new CommunicateManager(socket);
        communicateManager.sendCommunicate(StateServer.BUSY.toString());
        this.timeSync = timeSync;
        this.isClientConnectef = true;
    }

    /**
     * Główna logika działania wątku obsługującego klienta.
     * Realizuje proces logowania, synchronizacji danych oraz zakończenia sesji.
     */
    @Override
    public void run(){
        try {
            communicateManager.sendCommunicate(StateServer.READY.toString());

            String initJsonFromClient = communicateManager.receiveCommunicate();
            InitClientToServer loginData = ConverterClassToJson.restoreInitClientToServer(initJsonFromClient);

            try {
                logInUser(loginData);
            } catch (NullPointerException e) {
                return;
            }

            FormaterTerminalText.printSucess("Poprawnie zalogowany użytkownik " + loginData.ID);
            communicateManager.sendCommunicate(StateServer.CONNECTED.toString());
            communicateManager.sendCommunicate(String.valueOf(loginData.ID));

            this.userHomePath = usersDirPath + loginData.ID + loginData.pathClientArchive.replace('/', '\\');
            this.fileManager = new FileManager(this.userHomePath);

            String filesListJsonFromClient = communicateManager.receiveCommunicate();
            ListClientsFiles comunicate = ConverterClassToJson.restoreFileInformation(filesListJsonFromClient);

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

            comunicate.filesInformation = neededChangesFiles;
            communicateManager.sendCommunicate(ConverterClassToJson.convert(comunicate));

            if (!comunicate.filesInformation.isEmpty()) {
                for (FileInformation fileInfo : neededChangesFiles) {
                    TreeMap<Integer, FilePart> partsFile;
                    try {
                        partsFile = communicateManager.downloadPartsOfFile();
                    } catch (SocketException | RuntimeException se) {
                        FormaterTerminalText.printFailure("Połączenie z klientem zostało zerwane");
                        cleanUP();
                        return;
                    }

                    fileManager.saveFileFromParts(partsFile);
                }
                fileManager.updateModificationDates(neededChangesFiles);
                communicateManager.sendCommunicate(StateServer.DONE.toString());
            }
            communicateManager.sendCommunicate(String.valueOf(this.timeSync));
            FormaterTerminalText.printServerComunicate("//////  Wymiana danych z klientem zakończona ////////////\n\n");

            String com = communicateManager.receiveCommunicate();
            if(com == null || com.equals(StateServer.CONTINUE.toString())) {
                this.isClientConnectef = false;
            }
        }
        catch (LostConnectExeption | RuntimeException ls) {
            this.isClientConnectef = false;
        }
    }

    /**
     * Po wywołaniu następuje dokończenie komunikacji i zakończenie jej
     */
    public boolean isClientConnectef() {
        return isClientConnectef;
    }

    /**
     * Usuwa niepotrzebne pliki i katalogi z systemu serwera.
     *
     * @param list lista plików przeznaczonych do usunięcia
     */
    private void deleteUnnecessaryFiles(List<FileInformation> list) {
        Thread cleanerFile = new Thread(() -> {
            List<String> dirsToDelete = list.stream()
                    .filter(fi -> FileStatus.DELETE == fi.fileStatus && TypeOfFile.DIR.name().equals(fi.fileType))
                    .map(fi -> fi.filePath)
                    .collect(Collectors.toList());

            List<FileInformation> toDelete = list.stream()
                    .filter(fi ->
                            FileStatus.DELETE == fi.fileStatus
                                    || dirsToDelete.stream().anyMatch(dir ->
                                    fi.filePath.equals(dir) || fi.filePath.startsWith(dir + File.separator)))
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

    /**
     * Grupuje pliki według potrzebnej akcji (DELETE/SEND).
     *
     * @param files lista plików wymagających operacji
     * @return mapa z pogrupowanymi plikami
     */
    private static Map<FileStatus, List<FileInformation>> classifyActionOnGivenFile(List<FileInformation> files) {
        List<FileInformation> deletes = new ArrayList<>();
        List<FileInformation> others = new ArrayList<>();

        for (FileInformation file : files) {
            if (FileStatus.DELETE == file.fileStatus) {
                deletes.add(file);
            } else if (!file.fileType.equals(TypeOfFile.DIR.name()) &&
                    file.fileStatus != FileStatus.WITHOUT_CHANGES &&
                    file.fileSize > 0) {
                others.add(file);
            }
        }

        Map<FileStatus, List<FileInformation>> result = new HashMap<>();
        result.put(FileStatus.DELETE, deletes);
        result.put(FileStatus.SEND, others);
        return result;
    }

    /**
     * Wyszukuje pliki, które zostały przeniesione (MOVE) zamiast zmodyfikowane/usunięte.
     *
     * @param server     lista plików usuniętych po stronie serwera
     * @param fileToSend lista plików do przesłania
     * @return lista plików wykrytych jako przeniesione
     */
    private List<FileInformation> searchMovedFiled(List<FileInformation> server, List<FileInformation> fileToSend) {
        List<FileInformation> result = fileToSend.stream()
                .filter(fi1 -> server.stream().anyMatch(fi2 ->
                        fi1.fileName.equals(fi2.fileName) &&
                                fi1.modfiferTime.equals(fi2.modfiferTime) &&
                                fi1.fileSize == fi2.fileSize))
                .peek(fi -> fi.fileStatus = FileStatus.MOVE)
                .collect(Collectors.toList());

        fileToSend.removeIf(fi1 -> server.stream().anyMatch(fi2 ->
                fi1.fileName.equals(fi2.fileName) &&
                        fi1.modfiferTime.equals(fi2.modfiferTime) &&
                        fi1.fileSize == fi2.fileSize));

        server.removeIf(fi2 -> result.stream().anyMatch(fi1 ->
                fi1.fileName.equals(fi2.fileName) &&
                        fi1.modfiferTime.equals(fi2.modfiferTime) &&
                        fi1.fileSize == fi2.fileSize));

        return result;
    }

    /**
     * Przeprowadza proces logowania lub tworzenia nowego użytkownika.
     *
     * @param loginData dane logowania klienta
     * @throws NullPointerException jeśli dane logowania są błędne lub niekompletne
     */
    private void logInUser(InitClientToServer loginData) throws NullPointerException, LostConnectExeption{
        String initJsonFromClient;
        while (!Manager_db.isCorrectUser(loginData.ID, loginData.pathClientArchive)) {
            if (loginData.ID == -1) {
                this.createNewUser(loginData);
            } else {
                FormaterTerminalText.printFailure("Błąd logowania do ID: " + loginData.ID);
                communicateManager.sendCommunicate(StateServer.PERMISION_DENIED.toString());

                initJsonFromClient = communicateManager.receiveCommunicate();
                loginData = ConverterClassToJson.restoreInitClientToServer(initJsonFromClient);
            }
        }
    }

    /**
     * Tworzy nowego użytkownika w bazie danych i systemie plików.
     *
     * @param loginData dane nowego użytkownika
     */
    private void createNewUser(InitClientToServer loginData) {
        int id = Manager_db.addUser(loginData.IP, loginData.pathClientArchive);
        loginData.ID = id;
        this.userHomePath = "server/" + loginData.ID;
        fileManager.createDirectory(this.userHomePath + loginData.pathClientArchive);

        System.out.println("Nowy użytkownik utworzony o ID " + id);
    }

    /**
     * Czyści zasoby po zakończeniu komunikacji z klientem.
     */
    public void cleanUP() {
        this.communicateManager.cleanUp();
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wypisuje listę plików na konsolę serwera.
     *
     * @param list lista plików do wypisania
     */
    private static void printList(List<FileInformation> list) {
        int index = 0;
        for (FileInformation fileInformation : list) {
            System.out.println(index + ") " + fileInformation);
            index++;
        }
        if (index == 0)
            System.out.println("Brak elementów do wyświetlenia");
    }
}
