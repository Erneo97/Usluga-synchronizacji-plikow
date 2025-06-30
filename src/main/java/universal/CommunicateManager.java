package universal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

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
        }
        catch (SocketException e) {
            System.out.println("Utracono połączenie");
            return null;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    public boolean sendCommunicate(String comunicate) {
        writer.println(comunicate);
        if (writer.checkError()) {
            return false;
        }
        return true;
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
