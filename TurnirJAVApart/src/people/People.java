package people;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class People {
    enum Role { TEACHER, STUDENT, PARENT }
    
    private String firstName, lastName, aboutMe, email, password;
    private Date dateOfBirth;
    private Role role;

    public People(String firstName, String lastName, String aboutMe, Date dateOfBirth, String email, String password, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.aboutMe = aboutMe;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setAboutMe(String aboutMe) { this.aboutMe = aboutMe; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }
}

class PeopleManager {
    private static final String URL = "jdbc:mysql://localhost:3306/People";
    private static final String USER = "root";
    private static final String PASSWORD = "12345678";
    
    public static void register(String firstName, String lastName, String email, String password, People.Role role, People requester) {
        if (requester == null || requester.getRole() != People.Role.TEACHER) {
            throw new SecurityException("Only teachers can create new users!");
        }
        String query = "INSERT INTO users (first_name, last_name, email, password_hash, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, password);
            stmt.setString(5, role.name().toLowerCase());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    public static boolean logIn(String email, String password) {
        String query = "SELECT password_hash FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password_hash");
                    return storedPassword.equals(password); 
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при авторизації: " + e.getMessage());
        }
        return false;
    }
    
    
    public static People findUser(String column, String value) {
        Map<String, String> queries = Map.of(
            "email", "SELECT * FROM users WHERE email = ?",
            "first_name", "SELECT * FROM users WHERE first_name = ?",
            "last_name", "SELECT * FROM users WHERE last_name = ?",
            "date_of_birth", "SELECT * FROM users WHERE date_of_birth = ?",
            "role", "SELECT * FROM users WHERE role = ?"
        );
    
        if (!queries.containsKey(column)) {
            System.out.println("Invalid search parameter");
            return null;
        }
    
        String query = queries.get(column);
    
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            stmt.setString(1, value);
            ResultSet rs = stmt.executeQuery();
    
            if (rs.next()) {
                return new People(
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("about_me"),
                    rs.getDate("date_of_birth"),
                    rs.getString("email"),
                    null,
                    People.Role.valueOf(rs.getString("role").toUpperCase())
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static void updateProfile(String email, String firstName, String lastName, String aboutMe, People requester) {
        if (!requester.getRole().equals(People.Role.TEACHER) && !requester.getEmail().equals(email)) {
            throw new SecurityException("You do not have permission to edit this profile");
        }
        
        String query = "UPDATE users SET first_name = ?, last_name = ?, about_me = ? WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, aboutMe);
            stmt.setString(4, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
