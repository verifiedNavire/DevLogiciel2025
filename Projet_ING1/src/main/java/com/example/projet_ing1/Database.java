package com.example.projet_ing1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/arbre_genealogique";
        return DriverManager.getConnection(url, "root", ""); // mot de passe si besoin
    }
}
