package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa {@code Manager_db} odpowiada za zarządzanie bazą danych SQLite dla serwera.
 *
 * Obsługuje połączenie z bazą danych, tworzenie tabel, dodawanie, wyszukiwanie i walidację użytkowników.
 */
public class Manager_db {
    /** Ścieżka do pliku bazy danych SQLite. */
    private static final String DB_URL = "jdbc:sqlite:server/database/mydb.db";

    /** Flaga oznaczająca, czy baza danych została zainicjalizowana. */
    private static boolean isInitialized = false;

    /**
     * Inicjalizuje połączenie z bazą danych oraz tworzy tabelę `Users`, jeśli jeszcze nie istnieje.
     */
    public static void initDatabase() {
        System.out.println("Inicjalizacja - bazy danych");
        if (!isInitialized) {
            try {
                DriverManager.getConnection(DB_URL);
                System.out.println("Tworzę tabelę");
                createUsersTable();
                isInitialized = true;
            } catch (SQLException e) {
                // Obsługa błędu połączenia z bazą
            }
        }
    }

    /**
     * Tworzy tabelę `Users` w bazie danych, jeśli nie istnieje.
     * Tabela zawiera kolumny: ID (PK), IP, dir_user.
     */
    private static void createUsersTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS Users (
                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                IP TEXT NOT NULL,
                dir_user TEXT NOT NULL
            );
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela Users została utworzona (lub już istnieje).");
        } catch (SQLException e) {
            System.out.println("Błąd przy tworzeniu tabeli Users: " + e.getMessage());
        }
    }

    /**
     * Dodaje nowego użytkownika do bazy danych.
     *
     * @param ip adres IP użytkownika
     * @param dirUser ścieżka katalogu użytkownika
     * @return ID nowo dodanego użytkownika lub -1 jeśli dodanie się nie powiodło
     */
    public static int addUser(String ip, String dirUser) {
        String sql = "INSERT INTO Users(IP, dir_user) VALUES(?, ?)";
        int generatedId = -1;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, ip);
            pstmt.setString(2, dirUser);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Dodanie użytkownika nie powiodło się, brak zmienionych wierszy.");
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                }
            }

            System.out.println("Użytkownik dodany. ID = " + generatedId);

        } catch (SQLException e) {
            System.out.println("Błąd dodawania użytkownika: " + e.getMessage());
        }

        return generatedId;
    }

    /**
     * Sprawdza, czy użytkownik o podanym ID istnieje w bazie danych.
     *
     * @param ID identyfikator użytkownika
     * @return true jeśli użytkownik istnieje, false w przeciwnym wypadku
     */
    public static boolean isUserExistByID(Long ID) {
        return getUserById(ID) != null;
    }

    /**
     * Sprawdza, czy użytkownik o danym ID i ścieżce katalogu istnieje.
     *
     * @param id identyfikator użytkownika
     * @param dirUser katalog przypisany użytkownikowi
     * @return true jeśli użytkownik istnieje i dane się zgadzają, false w przeciwnym wypadku
     */
    public static boolean isCorrectUser(long id, String dirUser) {
        String sql = "SELECT COUNT(*) FROM Users WHERE ID = ? AND dir_user = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, (int) id);
            pstmt.setString(2, dirUser);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }

        } catch (SQLException e) {
            System.out.println("Błąd podczas sprawdzania użytkownika: " + e.getMessage());
        }

        return false;
    }

    /**
     * Wypisuje wszystkich użytkowników znajdujących się w bazie danych na konsolę.
     */
    public static void getAllUsers() {
        String sql = "SELECT * FROM Users";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("ID: " + rs.getInt("id") +
                        ", IP: " + rs.getString("IP") +
                        ", dir_user: " + rs.getString("dir_user"));
            }

            if (!found) {
                System.out.println("Brak użytkowników w bazie.");
            }

        } catch (SQLException e) {
            System.out.println("Błąd podczas pobierania użytkowników: " + e.getMessage());
        }
    }

    /**
     * Zwraca listę użytkowników posiadających podany adres IP.
     *
     * @param ip adres IP
     * @return lista użytkowników pasujących do IP
     */
    public static List<User> getUserByIP(String ip) {
        String sql = "SELECT * FROM Users WHERE IP = ?";
        List<User> users = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ip);
            ResultSet rs = pstmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                users.add(new User(
                        rs.getString("IP"),
                        rs.getString("dir_user"),
                        rs.getInt("id")
                ));
            }

            if (!found) {
                System.out.println("Brak użytkownika z IP: " + ip);
            }

        } catch (SQLException e) {
            System.out.println("Błąd podczas wyszukiwania: " + e.getMessage());
        }

        return users;
    }

    /**
     * Zwraca użytkownika na podstawie jego ID.
     *
     * @param id identyfikator użytkownika
     * @return obiekt {@code User}, jeśli istnieje, w przeciwnym razie {@code null}
     */
    public static User getUserById(long id) {
        String sql = "SELECT * FROM Users WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, (int) id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getString("IP"),
                        rs.getString("dir_user"),
                        rs.getInt("id")
                );
            } else {
                System.out.println("Użytkownik o ID " + id + " nie istnieje.");
            }

        } catch (SQLException e) {
            System.out.println("Błąd podczas wyszukiwania: " + e.getMessage());
        }

        return null;
    }

    /**
     * Metoda testowa: inicjalizuje bazę danych i wykonuje przykładowe operacje.
     * Można ją wykorzystać do szybkiego sprawdzenia działania klasy.
     */
    public static void main(String[] args) {
        Manager_db.initDatabase();

        System.out.println("Wszyscy użytkownicy z bazy danych:");
        Manager_db.getAllUsers();

        User ret = Manager_db.getUserById(2);
        System.out.println("Uzytkownik id=2 to  " + ret);

        System.out.println(
                Manager_db.isCorrectUser(2L, "C:\\Users\\Michal Kaniewski\\Desktop\\Semestr 6\\Programowanie sieciowe 1\\Usluga-synchronizacji-plikow\\test2")
                        ? "Istnieje użytkownik" : "Błąd logowania");

        List<User> users = Manager_db.getUserByIP("192.168.2.10");
        System.out.println("Użytkownicy o IP 192.168.2.10 to  " + users);
    }
}
