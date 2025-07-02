package universal.managers;

import universal.FilePart;
import universal.FormaterTerminalText;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class CommunicateManager {
    BufferedReader reader = null;
    PrintWriter writer;
    ObjectOutputStream oos;
    ObjectInputStream ois;

    public CommunicateManager(Socket socket) {
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.oos =new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());
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
            FormaterTerminalText.printFailure("Utracono połączenie");

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

    public boolean sendFile(File file, String filePath) {
        long fileSize = file.length();

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[FilePart.maxSizePart];
            int bytesRead;
            int partNumber = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] dataCopy = Arrays.copyOf(buffer, bytesRead);
                FilePart part = new FilePart(dataCopy, bytesRead, (int) fileSize, partNumber++, filePath);
                oos.writeObject(part);
            }
            System.out.println("Wysłano część #" + partNumber);
            oos.flush();
        } catch (SocketException e) {
            FormaterTerminalText.printFailure("Połączenie zostało zerwane");
            return false;
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public TreeMap<Integer, FilePart>  downloadPartsOfFile() throws SocketException {
        TreeMap<Integer, FilePart> parts = new TreeMap<>();
        int expectedSize = -1;

        try {
            while (true) {
                Object obj = ois.readObject();

                if (!(obj instanceof FilePart)) break;

                FilePart part = (FilePart) obj;
                parts.put(part.partNumber, part);
                expectedSize = part.fullSizeOfFile;

                if (getTotalSize(parts) >= expectedSize) break;
            }

        }
        catch (SocketException e) {
            throw e;
        }
        catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        }

        return parts;
    }



    private static int getTotalSize(Map<Integer, FilePart> parts) {
        return parts.values().stream().mapToInt(p -> p.partSize).sum();
    }

    public void cleanUp( ) {
        try {
            this.reader.close();
            this.writer.close();
            this.oos.close();
            this.ois.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

