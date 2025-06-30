package database;
import announcements.InitClientToServer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Manager_db {
    private static final String DB_URL = "jdbc:sqlite:server/database/mydb.db";
    private static boolean isInitialized = false;


    public static void iniManager_db() {
        System.out.println( "Inicjalizacja!");
        if (!isInitialized) {
            try {
                Connection conn = DriverManager.getConnection(DB_URL);
                System.out.println( "Tworzę table ");
                createUsersTable();
            } catch (SQLException e) {

            }
        }
    }

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

    public static boolean isUserExistByID(Long ID ) {
        User userDataFromDB = getUserById(ID);
        if( userDataFromDB == null ) return false;

        return true;
    }


    public static boolean isCorrectUser(long id, String dirUser) {
        String sql = "SELECT COUNT(*) FROM Users WHERE ID = ? AND dir_user = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, (int)id);
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
                users.add(new User(rs.getString("IP") ,
                        rs.getString("dir_user"),
                        rs.getInt("id") ));

            }
            if (!found) {
                System.out.println("Brak użytkownika z IP: " + ip);
            }

        } catch (SQLException e) {
            System.out.println("Błąd podczas wyszukiwania: " + e.getMessage());
        }

        return users;
    }

    public static User getUserById(long id) {
        String sql = "SELECT * FROM Users WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, (int)id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
               return new User(rs.getString("IP") ,
                        rs.getString("dir_user"),
                        rs.getInt("id") );

            } else {
                System.out.println("Użytkownik o ID " + id + " nie istnieje.");
            }

        } catch (SQLException e) {
            System.out.println("Błąd podczas wyszukiwania: " + e.getMessage());
        }
        return null;
    }



    public static void main(String[] args) {
        Manager_db.iniManager_db();
//        Manager_db.addUser("192.168.2.10", System.getProperty("user.dir") + "\\test");
//        Manager_db.addUser("192.168.2.10", System.getProperty("user.dir") + "\\test2");
        System.out.println("Wszyscy użytkownicy z bazy danych:");
        Manager_db.getAllUsers();


        User ret =  Manager_db.getUserById(2);
        System.out.println("Uzytkownik id=2 to  " + ret);

        System.out.println(
                Manager_db.isCorrectUser(2L, "C:\\Users\\Michal Kaniewski\\Desktop\\Semestr 6\\Programowanie sieciowe 1\\Usluga-synchronizacji-plikow\\test2")
        ? "Istnieje użytkownika" : "Błąd logowania");

        List<User> users = Manager_db.getUserByIP("192.168.2.10");
        System.out.println("Uzytkownicy o IP 192.168.2.10 to  " + users);
    }
}
