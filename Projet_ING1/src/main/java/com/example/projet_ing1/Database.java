package com.example.projet_ing1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe pour obtenir une connexion à la base de données MySQL.
 * Utilise le driver JDBC pour se connecter à la base "arbre_genealogique" en local.
 */
public class Database {

    /**
     * Fournit une connexion à la base de données MySQL locale.
     */

    public static Connection getConnection() throws SQLException {
        // URL JDBC vers la base locale, port 3306, nom de la base : arbre_genealogique
        String url = "jdbc:mysql://localhost:3306/arbre_genealogique";

        // Connexion avec l’utilisateur root, sans mot de passe (à adapter si besoin)
        return DriverManager.getConnection(url, "root", "");
    }
}
