package database;

/**
 * Klasa {@code User} reprezentuje użytkownika systemu.
 *
 * Przechowuje podstawowe informacje: adres IP, katalog użytkownika oraz identyfikator (ID).
 * Używana głównie w operacjach bazodanowych związanych z zarządzaniem użytkownikami. {@code Manager_db}
 */
public class User {

    /** Adres IP użytkownika. */
    private String ip;

    /** Ścieżka katalogu przypisanego użytkownikowi. */
    private String dir_user;

    /** Unikalny identyfikator użytkownika w bazie danych. */
    private int id;

    /**
     * Konstruktor inicjalizujący wszystkie pola użytkownika.
     *
     * @param ip adres IP użytkownika
     * @param dir_user katalog przypisany użytkownikowi
     * @param id identyfikator użytkownika w bazie danych
     */
    public User(String ip, String dir_user, int id) {
        this.ip = ip;
        this.dir_user = dir_user;
        this.id = id;
    }

    /**
     * Domyślny konstruktor bezparametrowy.
     * Używany np. podczas deserializacji.
     */
    public User() {}

    /**
     * Zwraca adres IP użytkownika.
     *
     * @return adres IP
     */
    public String getIp() {
        return ip;
    }

    /**
     * Zwraca ścieżkę katalogu użytkownika.
     *
     * @return katalog użytkownika
     */
    public String getDir_user() {
        return dir_user;
    }

    /**
     * Zwraca identyfikator użytkownika.
     *
     * @return ID użytkownika
     */
    public int getId() {
        return id;
    }

    /**
     * Zwraca reprezentację tekstową obiektu użytkownika w postaci:
     * {@code ID <tab> IP <tab> Katalog}.
     *
     * @return tekstowa reprezentacja użytkownika
     */
    @Override
    public String toString() {
        return this.id + "\t" + this.ip + "\t" + this.dir_user;
    }
}
