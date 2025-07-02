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
import java.util.regex.Pattern;

import universal.FormaterTerminalText;
import universal.ConverterClassToJson;
import universal.announcements.FileInformation;
import universal.announcements.InitClientToServer;
import universal.announcements.ListClientsFiles;
import universal.managers.CommunicateManager;
import universal.managers.FileManager;
import universal.models.StateServer;

import static java.lang.Thread.sleep;

/**
 * Główny klient odpowiedzialny za:
 * <ol>
 *     <li>Nawiązanie i utrzymanie połączenia z serwerem.</li>
 *     <li>Przesłanie danych logowania oraz listy plików.</li>
 *     <li>Obsługę wysyłki brakujących plików do serwera.</li>
 *     <li>Usypianie aplikacji na czas wyznaczony przez serwer i ponawianie synchronizacji.</li>
 * </ol>
 * <p>
 * Klasa korzysta z {@link CommunicateManager} do wymiany komunikatów oraz
 * z {@link FileManager} do operacji na lokalnym systemie plików.
 * </p>
 */
public class Client {
    /** Socket TCP używany do komunikacji z serwerem. */
    private Socket socket;
    /** Manager komunikatów JSON–>Java i Java–>JSON. */
    private CommunicateManager communicateManager;
    /** Konsola do wczytywania danych od użytkownika. */
    private final Scanner scanner = new Scanner(System.in);
    /** Flaga sterująca główną pętlą pracy klienta. */
    private volatile boolean clientRunning = true;

    /* -------------------------------------------------- */
    /*  CONNECTION & LOGIN                                */
    /* -------------------------------------------------- */

    /**
     * Nawiązuje połączenie z serwerem. Pobiera adres IP i port od użytkownika,
     * waliduje je, a następnie próbuje połączyć się w ciągu 5 s.
     *
     * @return {@code true} gdy połączenie zostało ustanowione, w przeciwnym razie {@code false}
     */
    public boolean connectToServer() {
        while (socket == null || !socket.isConnected()) {
            String serverIP = inputIp();
            int port = inputPort();

//            // TODO: usunąć po zakończeniu testów (wartości wymuszone)
//            serverIP = "localhost";
//            port = 1234;

            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(serverIP, port), 5_000);
                communicateManager = new CommunicateManager(socket);
            } catch (IOException e) {
                FormaterTerminalText.printFailure("Nie udało się połączyć w ciągu 5 sekund. Spróbuj ponownie.");
                return false;
            }
        }
        return true;
    }

    /**
     * Pobiera od użytkownika poprawny adres IP zgodny z IPv4.
     *
     * @return zweryfikowany adres IP
     */
    private String inputIp() {
        Pattern ipPattern = Pattern.compile(
                "^((25[0-5]|2[0-4]\\d|[0-1]?\\d{1,2})\\.){3}(25[0-5]|2[0-4]\\d|[0-1]?\\d{1,2})$"
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

    /**
     * Pobiera od użytkownika numer portu.
     *
     * @return poprawny numer portu
     */
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

    /**
     * Wysyła dane logowania do serwera i oczekuje na potwierdzenie.
     *
     * @return {@code true} jeśli serwer zaakceptował połączenie, w przeciwnym razie {@code false}
     */
    private boolean loginToServer() {
        FormaterTerminalText.printServerComunicate("Nawiązuje połączenie");
        InitClientToServer init = new InitClientToServer();

        // Pobierz ID klienta
        System.out.print("Podaj swój ID (jeżeli go nie posiadasz wpisz -1): ");
        try {
            init.ID = Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            return false;
        }

        // Pobierz katalog archiwum
        System.out.print("Podaj ścieżkę swojego katalogu: ");
        init.pathClientArchive = scanner.nextLine().replace('\\', '/');

        // Ustal IP lokalne
        try {
            init.IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            FormaterTerminalText.printFailure("Nie można uzyskać adresu IP: " + e.getMessage());
        }

        String loginDataJson = ConverterClassToJson.convert(init);
        communicateManager.sendCommunicate(loginDataJson);

        String ret = communicateManager.receiveCommunicate();
        return ret != null && ret.equals(StateServer.CONNECTED.toString());
    }

    /* -------------------------------------------------- */
    /*  SERVER STATE HANDLING                             */
    /* -------------------------------------------------- */

    /**
     * Sprawdza, czy serwer zwraca status BUSY.
     */
    private boolean isServerBusy() {
        String ret = communicateManager.receiveCommunicate();
        return ret != null && ret.equals(StateServer.BUSY.toString());
    }

    /**
     * Pętla oczekująca na gotowość serwera.
     */
    void waitForServerReady() {
        while (isServerBusy()) {
            FormaterTerminalText.printServerComunicate("Serwer zajęty – czekam na jego dostępność...");
        }
    }

    /**
     * Usypia wątek na podaną liczbę milisekund.
     */
    void sleepSafely(long ms) {
        try {
            sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /* -------------------------------------------------- */
    /*  MAIN LOOP                                         */
    /* -------------------------------------------------- */


    public static void main(String[] args) {
        Client client = new Client();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.runOff();
        }));
        FileManager fileManager = new FileManager("client_data");

        while ( client.clientRunning ) {
            // 1. Połącz z serwerem
            while (!client.connectToServer()) {
                FormaterTerminalText.printNormal("Czekam na połączenie...");
            }
            FormaterTerminalText.printServerComunicate("Połączono z serwerem.");
            client.waitForServerReady();

            // 2. Zaloguj się
            while (!client.loginToServer()) {
                FormaterTerminalText.printFailure("Błędne dane logowania. Ponów próbę.");
            }
            FormaterTerminalText.printSucess("Poprawnie zalogowano na serwer.");
            String idFromServer = client.communicateManager.receiveCommunicate();

            // 3. Odczytaj listę lokalnych plików
            FormaterTerminalText.printNormal("Odczytuję twój zbiór lokalny...");
            List<FileInformation> localFiles = fileManager.getListOfFilesInformation();
            FormaterTerminalText.printNormal("Twoja lista plików:");
            printList(localFiles);

            // 4. Wyślij listę plików do serwera
            client.sendInitialListToServer(localFiles, idFromServer);

            // 5. Odbierz listę brakujących plików
            String neededJson = client.communicateManager.receiveCommunicate();
            ListClientsFiles needed = ConverterClassToJson.restoreFileInformation(neededJson);
            FormaterTerminalText.printServerComunicate("Brakujące pliki po stronie serwera:");
            printList(needed.filesInformation);

            // 6. Wyślij brakujące pliki
            if( !needed.filesInformation.isEmpty()) {
                FormaterTerminalText.printNormal("Przesyłanie plików...");
                client.sendAllFileToServer(fileManager, needed.filesInformation);
                FormaterTerminalText.printSucess("Synchronizacja zakończona sukcesem.");
            }
            else {
                FormaterTerminalText.printNormal("Brak plików do przesłania");
            }

            // 7. Odbierz czas kolejnej synchronizacji iuśpij aplikację
            String timeNextSync = client.communicateManager.receiveCommunicate();
            FormaterTerminalText.printServerComunicate(
                    "Uśpienie aplikacji na " + timeNextSync + "s. do kolejnej synchronizacji.");
            client.communicateManager.sendCommunicate(ConverterClassToJson.convert(StateServer.CONNECTED.toString()));

            client.sleepSafely(Integer.parseInt(timeNextSync));
        }
        client.cleanUp();
    }

    /* -------------------------------------------------- */
    /*  COMMUNICATION HELPERS                             */
    /* -------------------------------------------------- */

    /**
     * Wysyła do serwera inicjalną listę plików klienta.
     */
    void sendInitialListToServer(List<FileInformation> informationFiles, String idFromServer) {
        ListClientsFiles first = new ListClientsFiles();
        first.filesInformation = informationFiles;
        first.ID = Integer.parseInt(idFromServer);
        communicateManager.sendCommunicate(ConverterClassToJson.convert(first));
    }
    void runOff() {
        this.clientRunning = false;
        this.communicateManager.sendCommunicate(ConverterClassToJson.convert(StateServer.DONE.toString()));
    }
    /**
     * Wypisuje listę plików na konsolę.
     */
    private static void printList(List<FileInformation> list) {
        int index = 0;
        for (FileInformation fi : list) {
            System.out.println(index++ + ") " + fi);
        }
    }

    /**
     * Wysyła wszystkie wymagane pliki do serwera czeka na potwierdzenie DONE.
     */
    private void sendAllFileToServer(FileManager fm, List<FileInformation> needed) {
        if (needed.isEmpty()) {
            FormaterTerminalText.printServerComunicate("Brak plików do aktualizacji.");
        } else {
            for (FileInformation fi : needed) {
                File file = fm.getFile(fi.filePath);
                communicateManager.sendFile(file, fi.filePath);
            }
        }

        // Oczekiwanie na DONE
        StateServer state;
        do {
            String com = communicateManager.receiveCommunicate();
            state = ConverterClassToJson.restoreStateServer(com);
        } while (state != StateServer.DONE);
    }

    /* -------------------------------------------------- */
    /*  CLEANUP & SHUTDOWN                                */
    /* -------------------------------------------------- */

    /**
     * Czyści zasoby komunikacyjne.
     */
    private void cleanUp() {
        if (communicateManager != null) communicateManager.cleanUp();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            socket = null;
        }
    }

    /**
     * Zatrzymuje główną pętlę klienta (wywoływane np. przez shutdown hook).
     */
    public void shutdown() {
        clientRunning = false;
    }
}
