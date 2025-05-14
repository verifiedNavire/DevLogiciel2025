package com.example.projet_ing1;

import java.sql.*;
import java.util.*;

public class ArbreDAO {
    public Map<Integer, Personne> personnes = new HashMap<>();
    public Map<Integer, List<Integer>> relations = new HashMap<>();

    public void chargerDepuisBase() {
        try (Connection conn = Database.getConnection()) {
            // Charger toutes les personnes
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM personne");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Personne p = new Personne(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getBoolean("inscrit")
                );
                personnes.put(p.getId(), p);
            }

            // Charger les liens parent-enfant
            ps = conn.prepareStatement("SELECT id_parent, id_enfant FROM lien_parent");
            rs = ps.executeQuery();
            while (rs.next()) {
                int parentId = rs.getInt("id_parent");
                int enfantId = rs.getInt("id_enfant");

                relations.computeIfAbsent(parentId, k -> new ArrayList<>()).add(enfantId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retourne l'autre parent d’un enfant, différent de parentCourantId
    public int getAutreParent(int enfantId, int parentCourantId) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_parent FROM lien_parent WHERE id_enfant = ? AND id_parent != ?"
            );
            ps.setInt(1, enfantId);
            ps.setInt(2, parentCourantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_parent");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // pas trouvé
    }

}
