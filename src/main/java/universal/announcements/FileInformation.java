package universal.announcements;

import universal.models.FileStatus;

/**
 * Klasa {@code FileInformation} przechowuje szczegółowe informacje o pliku
 * w kontekście synchronizacji i komunikacji między klientem a serwerem.
 * <p>
 * Używana m.in. do wysyłania danych o plikach oraz określania ich statusu.
 */
public class FileInformation {

    /** Nazwa pliku. */
    public String fileName;

    /** Typ pliku (np. rozszerzenie lub "folder"). */
    public String fileType;

    /** Czas ostatniej modyfikacji pliku w formacie tekstowym. */
    public String modfiferTime;

    /** Rozmiar pliku w bajtach. */
    public int fileSize;

    /** Pełna ścieżka dostępu do pliku. */
    public String filePath;

    /** Status pliku określający jego aktualność lub potrzebę synchronizacji. */
    public FileStatus fileStatus;

    /**
     * Konstruktor domyślny.
     * Ustawia status pliku na {@code FileStatus.NO_INFORMATION}.
     */
    public FileInformation() {
        this.fileStatus = FileStatus.NO_INFORMATION;
    }

    /**
     * Zwraca reprezentację tekstową obiektu w formie jednej linii z wyrównanym formatowaniem.
     *
     * Format: [fileType]  [fileName]  [filePath]  [modfiferTime]  [fileSize] ([fileStatus])
     *
     * @return sformatowany opis pliku
     */
    @Override
    public String toString() {
        return String.format("%-4s", fileType) + "  " +
                String.format("%-25s", fileName) + "  " +
                String.format("%-35s", filePath) + "  " +
                modfiferTime + "  " +
                String.format("%10s", fileSize) + "  (" +
                fileStatus + ")";
    }
}
