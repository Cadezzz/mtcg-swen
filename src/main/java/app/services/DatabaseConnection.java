package app.services;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    @Getter
    private final Connection connection;

    public DatabaseConnection() {
        try{
            this.connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/swe1db",
                "swe1user",
                "swe1pw"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
