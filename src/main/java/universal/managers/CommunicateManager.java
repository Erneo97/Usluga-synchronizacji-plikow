package universal.managers;

import universal.FilePart;
import universal.FormaterTerminalText;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

/**
 * Klasa {@code CommunicateManager} zarządza komunikacją sieciową między klientem a serwerem.
 * Umożliwia wysyłanie i odbieranie komunikatów tekstowych oraz przesyłanie plików podzielonych na części.
 * Jest wykorzystywana przez {@code UserHandling} jak i {@code Client}
 * <p>
 * Konstruktor przyjmuje połączenie {@link Socket} i inicjalizuje strumienie wejścia i wyjścia.
 */
public class CommunicateManager {
    BufferedReader reader = null;
    PrintWriter writer;
    ObjectOutputStream oos;
    ObjectInputStream ois;
    Socket socket;
    /**
     * Tworzy nowy obiekt CommunicateManager, inicjalizując strumienie komunikacyjne na podstawie gniazda sieciowego.
     *
     * @param socket połączenie sieciowe {@link Socket}
     */
    public CommunicateManager(Socket socket) {
        try {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Nie udało się zainicjalizować wysyłania i odbierania komunikatów");
        }
    }

    /**
     * Odbiera komunikat tekstowy (jako pojedynczy wiersz) od drugiej strony połączenia.
     *
     * @return odebrany komunikat w formie {@link String}, lub null jeśli połączenie zostało utracone
     */
    public String receiveCommunicate() {
        String json;
        try {
            json = reader.readLine();
        } catch (SocketException e) {
            FormaterTerminalText.printFailure("Utracono połączenie");
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    /**
     * Wysyła komunikat tekstowy do drugiej strony połączenia.
     *
     * @param comunicate komunikat do wysłania
     * @return true jeśli wysłanie się powiodło, false w przypadku błędu
     */
    public boolean sendCommunicate(String comunicate) {
        writer.println(comunicate);
        return !writer.checkError();
    }

    /**
     * Wysyła plik podzielony na części {@link FilePart} przez strumień obiektowy.
     *
     * @param file     plik do wysłania
     * @param filePath ścieżka pliku (używana w metadanych części)
     * @return true jeśli wysłanie się powiodło, false w przypadku błędu lub zerwania połączenia
     */
    public boolean sendFile(File file, String filePath) {
        long fileSize = file.length();

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[ FilePart.maxSizePart];
            int bytesRead;
            int partNumber = 0;
            double numberOfAllParts = Math.ceil(file.length() /FilePart.maxSizePart);
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] dataCopy = Arrays.copyOf(buffer, bytesRead);
                FilePart part = new FilePart(dataCopy, bytesRead, (int) fileSize, partNumber++, filePath);
                oos.writeObject(part);
                FormaterTerminalText.printprogressBar("Stan przesyłu "+ part.pathFile + ": ", partNumber+1, (int)numberOfAllParts-1);

                System.gc();
            }

            if (!this.socket.isClosed())
                oos.flush();
        } catch (SocketException e) {
            FormaterTerminalText.printFailure("Połączenie zostało zerwane");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Odbiera części pliku przesłane przez drugą stronę i łączy je w mapę uporządkowaną według numerów części.
     *
     * @return mapa z częściami pliku {@link FilePart} uporządkowana po numerze części, lub null w przypadku błędu
     * @throws SocketException jeśli połączenie zostanie zerwane
     */
    public TreeMap<Integer, FilePart> downloadPartsOfFile() throws SocketException {
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

        } catch (SocketException e) {
            throw e;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        }

        return parts;
    }

    /**
     * Oblicza łączny rozmiar wszystkich części pliku.
     *
     * @param parts mapa części pliku
     * @return łączny rozmiar w bajtach
     */
    private static int getTotalSize(Map<Integer, FilePart> parts) {
        return parts.values().stream().mapToInt(p -> p.partSize).sum();
    }

    /**
     * Zamyka wszystkie strumienie używane do komunikacji.
     */
    public void cleanUp() {
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
