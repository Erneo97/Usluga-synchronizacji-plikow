package universal.announcements;

import java.util.List;

/**
 * Klasa {@code ListClientsFiles} reprezentuje listę plików przypisaną do konkretnego klienta.
 * <p>
 * Przechowuje identyfikator klienta oraz listę obiektów {@link FileInformation}
 * zawierających informacje o plikach tego klienta.
 */
public class ListClientsFiles {

    /** Unikalny identyfikator klienta. */
    public long ID;

    /** Lista informacji o plikach klienta. */
    public List<FileInformation> filesInformation;

    /**
     * Zwraca reprezentację tekstową listy plików klienta,
     * gdzie każdy plik jest wyświetlany wraz z indeksem.
     *
     * @return sformatowany łańcuch tekstowy zawierający ID klienta oraz listę plików
     */
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("\t" + this.ID + " :\n");
        int index = 0;
        for (FileInformation fileInformation : this.filesInformation) {
            ret.append(index).append(") ").append(fileInformation.toString()).append("\n");
            index++;
        }
        return ret.toString();
    }
}
