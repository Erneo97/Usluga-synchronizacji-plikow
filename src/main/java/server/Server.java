package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import database.User;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



public class Server {
    private final ThreadPoolExecutor executor;
    private final int port;

    public Server(int port) {
        this.port = port;
        //    Init zarządzania wątkami
        executor = new ThreadPoolExecutor(
                0,                  // corePoolSize = 0 → wszystkie wątki mogą wygasać
                10,                 // maximumPoolSize = 10
                30,                 // keepAliveTime = 30
                TimeUnit.SECONDS,
                new SynchronousQueue<>() // bez kolejki – bezpośrednie przekazanie tasków
        );
        executor.allowCoreThreadTimeOut(true);

    }


    void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serwer nasłuchuje na porcie " + port);

            while (!executor.isShutdown()) {
                Socket socket = serverSocket.accept();
                System.out.println("Połączono z klientem: " + socket.getInetAddress());

                executor.execute(new UserHandling(socket));


            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void shutdown() {
        executor.shutdown();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Użycie: java Server <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        Server server = new Server(port);


        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

    }

    void test_mapowanie_json() {
        System.out.println("Rozpoczecie testu!!!");
        Gson gson = new Gson();

        User user = new User("192.168.2.1", "/", 234);
        User user2 = new User("192.168.2.2", "/", 2343);



        String json = gson.toJson(user);
        System.out.println(json);

        User userJson = gson.fromJson(json, User.class);
        System.out.println(userJson);




        System.out.println("test json Listy");
        List<User> users = Arrays.asList(user, user2, user);

        json = gson.toJson(users);
        System.out.println("Lista json: " + json);

        Type userListType = new TypeToken<List<User>>(){}.getType();
        List<User> usersRet = gson.fromJson(json, userListType);
        usersRet.forEach(u -> System.out.println(u));

    }

}
