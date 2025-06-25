package database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Manager_db {
    private static final String DB_URL = "jdbc:sqlite:server/database/db.db";

    public static void addUser(String ip, String dirUser) {
        String sql = "INSERT INTO Users(IP, dir_user) VALUES(?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ip);
            pstmt.setString(2, dirUser);
            pstmt.executeUpdate();
            System.out.println("Użytkownik dodany.");

        } catch (SQLException e) {
            System.out.println("Błąd dodawania użytkownika: " + e.getMessage());
        }
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

    public static User getUserById(int id) {
        String sql = "SELECT * FROM Users WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
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

//        Manager_db.addUser("192.168.2.10", System.getProperty("user.dir") + "\\test");
//        Manager_db.addUser("192.168.2.10", System.getProperty("user.dir") + "\\test2");
        System.out.println("Wszyscy użytkownicy z bazy danych:");
        Manager_db.getAllUsers();

        User ret =  Manager_db.getUserById(2);
        System.out.println("Uzytkownik id=2 to  " + ret);


        List<User> users = Manager_db.getUserByIP("192.168.2.10");
        System.out.println("Uzytkownicy o IP 192.168.2.10 to  " + users);
    }
}
