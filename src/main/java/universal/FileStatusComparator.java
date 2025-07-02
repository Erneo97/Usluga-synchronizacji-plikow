package universal;

import universal.announcements.FileInformation;
import universal.models.FileStatus;

import java.util.*;

/**
 * Klasa pomocnicza do porównywania list informacji o plikach po stronie klienta i serwera.
 * Na podstawie porównania określa status każdego pliku, np. czy został zmodyfikowany, usunięty lub nie zmieniony.
 */
public class FileStatusComparator {

    /**
     * Porównuje listę plików klienta z listą plików serwera i zwraca nową listę
     * z aktualizowanymi statusami plików według następujących zasad:
     * <ul>
     *   <li>Jeśli plik z klienta nie istnieje na serwerze – status SEND (wysłać)</li>
     *   <li>Jeśli pliki mają ten sam czas modyfikacji – status WITHOUT_CHANGES (bez zmian)</li>
     *   <li>Jeśli pliki mają różny czas modyfikacji – status MODIFIED (zmodyfikowany)</li>
     *   <li>Pliki, które są na serwerze, ale nie ma ich na kliencie – status DELETE (usunąć)</li>
     * </ul>
     *
     * @param clientList lista plików po stronie klienta
     * @param serverList lista plików po stronie serwera (może być null)
     * @return lista plików z uaktualnionymi statusami
     */
    public static List<FileInformation> compare(List<FileInformation> clientList, List<FileInformation> serverList) {
        List<FileInformation> result = new ArrayList<>();

        if (serverList == null) {
            // Jeśli brak listy serwera, wszystkie pliki klienta oznacz jako do wysłania
            result.addAll(clientList);
            return result;
        }

        Map<String, FileInformation> serverMap = new HashMap<>();
        for (FileInformation file : serverList) {
            serverMap.put(file.filePath, file);
        }

        for (FileInformation clientFile : clientList) {
            FileInformation serverFile = serverMap.remove(clientFile.filePath);

            if (serverFile == null) {
                clientFile.fileStatus = FileStatus.SEND;
            } else if (clientFile.modfiferTime.equals(serverFile.modfiferTime)) {
                clientFile.fileStatus = FileStatus.WITHOUT_CHANGES;
            } else {
                clientFile.fileStatus = FileStatus.MODIFED;
            }

            result.add(clientFile);
        }

        // Pliki które są na serwerze, a nie ma ich na kliencie - oznacz jako DELETE
        for (FileInformation serverLeft : serverMap.values()) {
            serverLeft.fileStatus = FileStatus.DELETE;
            result.add(serverLeft);
        }

        return result;
    }

}
