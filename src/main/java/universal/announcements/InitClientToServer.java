package universal.announcements;

import java.io.Serializable;

/**
 * Klasa {@code InitClientToServer} reprezentuje podstawowe dane
 * wysyłane z klienta do serwera podczas inicjalizacji połączenia.
 * <p>
 * Obiekt jest serializowalny, co umożliwia przesyłanie go przez sieć.
 */
public class InitClientToServer implements Serializable {

    /** Adres IP klienta. */
    public String IP;

    /** Ścieżka do archiwum klienta (lokalizacja katalogu do synchronizacji). */
    public String pathClientArchive;

    /** Unikalny identyfikator klienta. */
    public long ID;

    /**
     * Zwraca tekstową reprezentację obiektu w postaci "ID - pathClientArchive".
     *
     * @return reprezentacja tekstowa obiektu
     */
    @Override
    public String toString() {
        return this.ID + " - " + this.pathClientArchive;
    }
}
