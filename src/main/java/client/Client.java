package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public boolean loginToServer() {
        Scanner scanner = new Scanner(System.in);

        Socket socket = null;

        while (socket == null || !socket.isConnected()) {
            System.out.print("Podaj adres IP serwera: ");
            String serverIP = scanner.nextLine();

            System.out.print("Podaj port serwera: ");
            int port = scanner.nextInt();
            scanner.nextLine();

            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(serverIP, port), 5000);
                System.out.println("Połączono z serwerem.");
            } catch (IOException e) {
                System.out.println("Nie udało się połączyć w ciągu 5 sekund. Spróbuj ponownie.\n");
            }
        }

        return true;
    }

    public static void main(String[] args) {
        Client client = new Client();

        client.loginToServer();

    }



}
