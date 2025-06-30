package universal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class CommunicateManager {
    BufferedReader reader = null;
    PrintWriter writer;

    public CommunicateManager(Socket socket) {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Nie udało się zainicjalizować wysyłania i odbierania komunikatów");

        }
    }
    public String receiveCommunicate() {
        String json ;
        try {
            json = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    public void sendCommunicate(String comunicate) {
        writer.println(comunicate);
    }


    public void cleanUp( ) {
        try {
            this.reader.close();
            this.writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
