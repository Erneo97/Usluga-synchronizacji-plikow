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
import universal.CommunicateManager;
import universal.ConverterFilesIngormationToJson;
import universal.FileManager;

public class Client {
    Socket socket = null;
    CommunicateManager communicateManager;

    public boolean loginToServer() {
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


    public static void main(String[] args) {
        Client client = new Client();

        client.loginToServer();


        FileManager fileManager = new FileManager("client_data");
        System.out.println("Odczytuje twój zbiór lokalny...");
        List<FileInformation> informationFiles = fileManager.getListOfFilesInformation();

        System.out.println("Twoja lista plików:\n");
        Client.printList(informationFiles);

        InitClientToServer firstCommunicate = new InitClientToServer();
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            firstCommunicate.IP  = localHost.getHostAddress();
            firstCommunicate.filesInformation = informationFiles;

            String jsonToSEND = ConverterFilesIngormationToJson.convertInitClientToServer(firstCommunicate);
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
