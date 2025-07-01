package server;

import universal.FormaterTerminalText;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;


public class Server {
    private final int port;
    private final BlockingQueue<UserHandling> usersWaiting;
    private boolean serverRunning;


    public Server(int port) {
        usersWaiting = new LinkedBlockingQueue<>();
        this.port = port;
        this.serverRunning = true;
    }

    private void handleWatingUsers( ) {
        Thread handlerUsersThread = new Thread(() -> {
            while(serverRunning) {
                try {
                    UserHandling user = usersWaiting.take();
                    FormaterTerminalText.printServerComunicate("\t\tObsługuje nowego użytkownika");
                    user.run();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        handlerUsersThread.start();
    }

    void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            FormaterTerminalText.printServerComunicate("Serwer nasłuchuje na porcie " + port);

            handleWatingUsers();

            while (serverRunning) {
                Socket socket = serverSocket.accept();
                System.out.println("Połączono z klientem: " + socket.getInetAddress());

                UserHandling newUser = new UserHandling(socket);
                usersWaiting.put(newUser);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        serverRunning= false;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            FormaterTerminalText.printFailure("Neleży podać port w parametrach wywołania");
            return;
        }

        int port = Integer.parseInt(args[0]);
        Server server = new Server(port);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

    }

}
