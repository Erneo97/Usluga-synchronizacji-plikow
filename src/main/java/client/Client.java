package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;

import announcements.FileInformation;
import announcements.InitClientToServer;
import announcements.ListClientsFiles;
import announcements.StateServer;
import universal.CommunicateManager;
import universal.ConverterClassToJson;
import universal.FileManager;

import static java.lang.Thread.sleep;

public class Client {
    Socket socket = null;
    CommunicateManager communicateManager;

    public boolean connectToServer() {
        Scanner scanner = new Scanner(System.in);

        while (socket == null || !socket.isConnected()) {
            System.out.print("Podaj adres IP serwera: ");
            String serverIP = scanner.nextLine();

            System.out.print("Podaj port serwera: ");
            int port = scanner.nextInt();
            scanner.nextLine();

            // TODO: usunąć te dwie linie po zakonczeniu testów
            serverIP = "localhost";
            port = 1234;


            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(serverIP, port), 5000);
                communicateManager = new CommunicateManager(socket);
            } catch (IOException e) {
                System.out.println("Nie udało się połączyć w ciągu 5 sekund. Spróbuj ponownie.\n");
                return false;
            }
        }
        System.out.println("Połączono z serwerem.");
        return true;
    }

    private boolean loginToServer( ) {
        InitClientToServer initClientToServer = new InitClientToServer();

        System.out.print("Podaj swój ID: ");
        Scanner scanner = new Scanner(System.in);
        try {
            initClientToServer.ID = Long.parseLong(scanner.nextLine());
        }
        catch (NumberFormatException e) {
            return false;
        }


        System.out.print("Podaj scieżkę swojego katalogu: ");
        initClientToServer.pathClientArchive = scanner.nextLine();

        // TODO: usunąc po testach
        initClientToServer.ID  = 1;
        initClientToServer.pathClientArchive = "/";

        String loginDataJson = ConverterClassToJson.convert(initClientToServer);
        communicateManager.sendCommunicate(loginDataJson);


        return true;
    }

    private boolean isServerBUSY( ) {
        if (communicateManager.receiveCommunicate().equals( StateServer.BUSY.toString() )) {
            return true;
        }
        return false;
    }

    void waitToServerReady( ) {
        while (this.isServerBUSY( )) {
            System.out.println("Serwer zajęty czekam na jego dostępność...");
            try {
                sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();

        client.connectToServer();
        client.waitToServerReady();

        while (!client.loginToServer()) {
            System.out.println("\tBłędne dane logowania\n\tPonownie wprowadź dane");
        }
        System.out.println("\tPoprawnie zalogowano na serwer");



        FileManager fileManager = new FileManager("client_data");
        System.out.println("Odczytuje twój zbiór lokalny...");
        List<FileInformation> informationFiles = fileManager.getListOfFilesInformation();

        System.out.println("Twoja lista plików:\n");
        Client.printList(informationFiles);



        ListClientsFiles firstCommunicate = new ListClientsFiles();
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            firstCommunicate.IP  = localHost.getHostAddress();
            firstCommunicate.filesInformation = informationFiles;

            String jsonToSEND = ConverterClassToJson.convert(firstCommunicate);
            client.communicateManager.sendCommunicate(jsonToSEND);


        } catch (UnknownHostException e) {
            System.out.println("Nie można uzyskać adresu IP: " + e.getMessage());
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
