package server;

import database.Manager_db;
import universal.FormaterTerminalText;
import universal.models.NextSyncTime;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

/**
 * Klasa {@code Server} odpowiada za uruchomienie serwera TCP,
 * przyjmowanie połączeń od klientów oraz delegowanie ich obsługi do osobnych wątków.
 *
 * Serwer zarządza kolejką klientów i wykonuje synchronizację plików
 * na podstawie częstotliwości ustalonej przez operatora.
 */
public class Server {
    /** Port nasłuchu serwera. */
    private final int port;

    /** Kolejka klientów oczekujących na obsługę. */
    private final BlockingQueue<UserHandling> usersWaiting;

    /** Flaga oznaczająca, czy serwer działa. */
    private boolean serverRunning;

    /**
     * Konstruktor klasy Server.
     *
     * @param port port, na którym serwer ma nasłuchiwać połączeń
     */
    public Server(int port) {
        Manager_db.initDatabase();
        usersWaiting = new LinkedBlockingQueue<>();
        this.port = port;
        this.serverRunning = true;
    }

    /**
     * Uruchamia osobny wątek do obsługi klientów pobranych z kolejki.
     * Każdy klient obsługiwany jest synchronicznie (jeden po drugim).
     */
    private void handleWatingUsers(long delay) {
        Thread handlerUsersThread = new Thread(() -> {
            while (serverRunning) {
                try {
                    UserHandling user = usersWaiting.take();
                    FormaterTerminalText.printServerComunicate("\t\tObsługuje nowego użytkownika");
                    user.run();
                    if( !user.isClientConnectef() ) {
                        user.cleanUP();
                    }
                    else {
                        Thread thread = new Thread(() -> {
                            try {
                                sleep(delay);
                                usersWaiting.put(user);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                        });
                        thread.start();
                    }

                } catch (InterruptedException  e) {
                    break;
                }
            }
        });
        handlerUsersThread.start();
    }

    /**
     * Główna metoda uruchamiająca serwer:
     * - Inicjalizuje gniazdo serwera,
     * - Prosi użytkownika o wybór częstotliwości synchronizacji,
     * - Oczekuje na połączenia od klientów,
     * - Dodaje ich do kolejki oczekujących.
     */
    void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            FormaterTerminalText.printServerComunicate("Serwer nasłuchuje na porcie " + port);



            // Lista dostępnych opcji synchronizacji
            NextSyncTime[] possibleTimeSync = {
                    NextSyncTime.sec_5,
                    NextSyncTime.sec_30,
                    NextSyncTime.m_5,
                    NextSyncTime.h_1
            };

            // Pobieranie wyboru synchronizacji od użytkownika
            FormaterTerminalText.printTextInputs("Podaj częstotliwość komunikacji:\n  0 - 5s\n  1 - 30s" +
                    "\n 2 - 5m\n  3 - 1h\nTwój wybór: ");
            Scanner scanner = new Scanner(System.in);
            int timIndex;
            while ((timIndex = scanner.nextInt()) < 0 || timIndex > 3) {
                FormaterTerminalText.printFailure("Nie poprawny zakres");
            }
            handleWatingUsers( possibleTimeSync[timIndex].getMilliseconds());
            FormaterTerminalText.printServerComunicate("Server został uruchomiony");

            // Główna pętla nasłuchiwania i dodawania użytkowników
            while (serverRunning) {
                Socket socket = serverSocket.accept();
                System.out.println("Połączono z klientem: " + socket.getInetAddress());

                UserHandling newUser = new UserHandling(socket, possibleTimeSync[timIndex].getMilliseconds());

                usersWaiting.put(newUser);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Zatrzymuje działanie serwera i przerywa główną pętlę nasłuchiwania.
     */
    public void shutdown() {
        serverRunning = false;
    }

    /**
     * Metoda uruchomieniowa. Oczekuje jednego argumentu - portu,
     * na którym ma działać serwer.
     *
     * @param args tablica argumentów (args[0] powinien zawierać numer portu)
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            FormaterTerminalText.printFailure("Neleży podać port w parametrach wywołania");
            return;
        }

        int port = Integer.parseInt(args[0]);
        Server server = new Server(port);

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

        server.start();
    }
}
