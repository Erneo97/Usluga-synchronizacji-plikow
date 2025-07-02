package client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import universal.FormaterTerminalText;
import universal.announcements.FileInformation;
import universal.announcements.InitClientToServer;
import universal.announcements.ListClientsFiles;
import universal.models.StateServer;
import universal.managers.CommunicateManager;
import universal.ConverterClassToJson;
import universal.managers.FileManager;

import static java.lang.Thread.sleep;
import java.util.regex.Pattern;

public class Client {
    Socket socket = null;
    CommunicateManager communicateManager;
    Scanner scanner = new Scanner(System.in);
    private boolean clientRuning = true;



    public boolean connectToServer() {
        while (socket == null || !socket.isConnected()) {
            String serverIP = inputIp();

            int port = inputPort();

            // TODO: usunąć te dwie linie po zakonczeniu testów
            serverIP = "localhost";
            port = 1234;


            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(serverIP, port), 5000);
                communicateManager = new CommunicateManager(socket);
            } catch (IOException e) {
                FormaterTerminalText.printFailure("Nie udało się połączyć w ciągu 5 sekund. Spróbuj ponownie.");
                return false;
            }
        }
        return true;
    }

    private String inputIp() {
        Pattern ipPattern = Pattern.compile(
                "^((25[0-5]|2[0-4]\\d|[0-1]?\\d{1,2})\\.){3}" +
                        "(25[0-5]|2[0-4]\\d|[0-1]?\\d{1,2})$"
        );

        String serverIP;
        do {
            FormaterTerminalText.printTextInputs("Podaj adres IP serwera: ");
            serverIP = scanner.nextLine();
            if (!ipPattern.matcher(serverIP).matches()) {
                FormaterTerminalText.printFailure("Niepoprawny adres IP. Spróbuj ponownie.");
            }
        } while (!ipPattern.matcher(serverIP).matches());
        return serverIP;
    }
    private int inputPort() {
        boolean correctNumber = false;
        int port = 0;
        do {
            FormaterTerminalText.printTextInputs("Podaj numer portu: ");
            try {
                port = scanner.nextInt();
                scanner.nextLine();
                correctNumber = true;
            } catch (InputMismatchException e) {
                FormaterTerminalText.printFailure("Błąd: podaj poprawny numer portu (liczbę całkowitą).");
                scanner.nextLine();
            }
        } while (!correctNumber);
        return port;
    }


    private boolean loginToServer( ) {
        FormaterTerminalText.printServerComunicate("Nawiązuje połączenie");
        InitClientToServer initClientToServer = new InitClientToServer();


        System.out.print("Podaj swój ID (Jeżeli go nie posiadasz wpisz -1): ");
        Scanner scanner = new Scanner(System.in);
        try {
            initClientToServer.ID = Long.parseLong(scanner.nextLine());
        }
        catch (NumberFormatException e) {
            return false;
        }


        System.out.print("Podaj scieżkę swojego katalogu: ");
        initClientToServer.pathClientArchive = scanner.nextLine();
        initClientToServer.pathClientArchive = initClientToServer.pathClientArchive.replace('\\', '/');

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            initClientToServer.IP = localHost.getHostAddress();
        } catch (UnknownHostException e) {
            FormaterTerminalText.printFailure("Nie można uzyskać adresu IP: " + e.getMessage());
        }


//        // TODO: usunąc po testach
//        initClientToServer.ID  = 10;
//        initClientToServer.pathClientArchive = "/ala/ma/kota";

        String loginDataJson = ConverterClassToJson.convert(initClientToServer);
        communicateManager.sendCommunicate(loginDataJson);

        String ret = communicateManager.receiveCommunicate();
        return ret != null && ret.equals(StateServer.CONNECTED.toString());
    }

    private boolean isServerBUSY( ) {
        String ret =communicateManager.receiveCommunicate();
        return ret != null && ret.equals(StateServer.BUSY.toString());
    }

    void waitToServerReady( ) {
        while (this.isServerBUSY( )) {
            FormaterTerminalText.printServerComunicate("Serwer zajęty czekam na jego dostępność...");

            try {
                sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void uspij(long ms) {
        try {
            sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Client client = new Client();

        FileManager fileManager = new FileManager("client_data");

        while( client.clientRuning ) {
            while (!client.connectToServer() ) {
                FormaterTerminalText.printNormal("Czekam na połączenie...");
            }
            FormaterTerminalText.printServerComunicate("Połączono z serwerem.");
            client.waitToServerReady();

            while (!client.loginToServer()) {
                FormaterTerminalText.printFailure("\tBłędne dane logowania\n\tPonownie wprowadź dane");
            }
            FormaterTerminalText.printSucess("\tPoprawnie zalogowano na serwer");
            String idFromServer = client.communicateManager.receiveCommunicate();


            FormaterTerminalText.printNormal("Odczytuje twój zbiór lokalny...");
            List<FileInformation> informationFiles = fileManager.getListOfFilesInformation();


            FormaterTerminalText.printNormal("\nTwoja lista plików:");
            Client.printList(informationFiles);


            client.sendInitialListToServer(informationFiles, idFromServer);


            String neededToSendJson = client.communicateManager.receiveCommunicate();
            ListClientsFiles neededToSend = ConverterClassToJson.restoreFileInformation(neededToSendJson);

            FormaterTerminalText.printServerComunicate("Prośba od serwera o wysłanie: ");
            printList(neededToSend.filesInformation);


            FormaterTerminalText.printNormal("Przesyłanie plików");
            client.sendAllFileToServer(fileManager, neededToSend.filesInformation);

            FormaterTerminalText.printSucess("Komunikacja zakończona - sukcesem");

            String timeNextSync = client.communicateManager.receiveCommunicate();
            FormaterTerminalText.printServerComunicate("Aplikacja zostanie uśpiona na " + timeNextSync + "s. do następnej synchronizacji" );
            client.uspij(Integer.parseInt(timeNextSync));
            client.cleanUP();
        }
    }

    void sendInitialListToServer( List<FileInformation> informationFiles, String idFromServer ) {
        ListClientsFiles firstCommunicate = new ListClientsFiles();
        firstCommunicate.filesInformation = informationFiles;
        firstCommunicate.ID = Integer.parseInt(idFromServer);
        String json = ConverterClassToJson.convert(firstCommunicate);

        this.communicateManager.sendCommunicate(json);
    }



    private static void printList(List<FileInformation> list) {
        int index = 0;
        for (FileInformation fileInformation : list) {
            System.out.println(index + ") " + fileInformation);
            index++;
        }
    }

    private void sendAllFileToServer(FileManager fileManager, List<FileInformation> neededToSend) {
        if(!neededToSend.isEmpty()) {
            for (FileInformation fileInformation : neededToSend) {
                File fileToSend = fileManager.getFile(fileInformation.filePath);
                this.communicateManager.sendFile(fileToSend, fileInformation.filePath) ;
            }

        }
        else {
            FormaterTerminalText.printServerComunicate("Braj plików do aktualizacji");
        }

        FormaterTerminalText.printNormal("Czekam na potwierdzenie");
        StateServer stateComunication;
        do {
            String com = this.communicateManager.receiveCommunicate();
            stateComunication = ConverterClassToJson.restoreStateServer(com);
        }while (stateComunication != StateServer.DONE);
    }


    private void cleanUP() {
        this.communicateManager.cleanUp();
        try {
            this.socket.close();
            this.socket = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        clientRuning = false;
    }
}
