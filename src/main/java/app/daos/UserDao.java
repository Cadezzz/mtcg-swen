package app.daos;

import app.models.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserDao {

    private final Connection connection;

    public UserDao(Connection connection) {
        this.connection = connection;
    }

    public void create(User user) throws IllegalAccessException {
        String sql = "INSERT INTO users (username, password, display_name, bio, image, coins, elo, battles_won, battles_lost) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getDisplayName());
            stmt.setString(4, user.getBio());
            stmt.setString(5, user.getImage());
            stmt.setInt(6, 20); //initial coins are 20
            stmt.setInt(7, 100); //every player gets 100 elo points
            stmt.setInt(8, 0); //initial battles won are 0
            stmt.setInt(9, 0); //initial battles lost are 0

            stmt.executeUpdate();
        } catch (SQLException e){
            throw new IllegalAccessException("Username already exists");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User read(String username) {
        String sql = "SELECT * FROM users WHERE username = ?;";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            var result = stmt.executeQuery();
            if (result.next()) {
                return new User(
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("display_name"),
                        result.getString("bio"),
                        result.getString("image"),
                        result.getInt("coins"),
                        result.getInt("elo"),
                        result.getInt("battles_won"),
                        result.getInt("battles_lost")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<User> readAll() {
        String sql = "SELECT * FROM users;";
        try (var stmt = connection.prepareStatement(sql)) {
            var result = stmt.executeQuery();
            ArrayList<User> users = new ArrayList<>();
            while (result.next()) {
                users.add(new User(
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("display_name"),
                        result.getString("bio"),
                        result.getString("image"),
                        result.getInt("coins"),
                        result.getInt("elo"),
                        result.getInt("battles_won"),
                        result.getInt("battles_lost")
                ));
            }
            return users;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void update(User user) {
        String sql = "UPDATE users SET password = ?, display_name = ?, bio = ?, image = ?, coins = ?, elo = ?, battles_won = ?, battles_lost = ? WHERE username = ?;";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getPassword());
            stmt.setString(2, user.getDisplayName());
            stmt.setString(3, user.getBio());
            stmt.setString(4, user.getImage());
            stmt.setInt(5, user.getCoins());
            stmt.setInt(6, user.getElo());
            stmt.setInt(7, user.getBattlesWon());
            stmt.setInt(8, user.getBattlesLost());
            stmt.setString(9, user.getUsername());

            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(User user) {
        String sql = "DELETE FROM users WHERE username = ?;";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
