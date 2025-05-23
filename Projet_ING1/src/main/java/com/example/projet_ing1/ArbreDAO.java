package com.example.projet_ing1;

import java.sql.*;
import java.util.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ArbreDAO {

    // Map contenant toutes les personnes de l'arbre (clé = id, valeur = Personne)
    public Map<Integer, Personne> personnes = new HashMap<>();

    // Map des relations parent → liste des enfants
    public Map<Integer, List<Integer>> relations = new HashMap<>();

    // 🔄 Charge toutes les personnes de la base + les relations parents-enfants
    public void chargerDepuisBase() {
        try (Connection conn = Database.getConnection()) {
            // Récupère toutes les personnes
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

            // Récupère toutes les relations parent-enfant
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

    // 🔄 Charge uniquement les personnes et relations liées à un utilisateur
    public void chargerFamillePourUtilisateur(int userId) {
        personnes.clear();
        relations.clear();

        Set<Integer> ids = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        ids.add(userId);
        queue.add(userId);

        try (Connection conn = Database.getConnection()) {
            // BFS sur les relations (parents et enfants)
            while (!queue.isEmpty()) {
                int courant = queue.poll();

                // Ajoute les parents
                PreparedStatement ps = conn.prepareStatement("SELECT id_parent FROM lien_parent WHERE id_enfant = ?");
                ps.setInt(1, courant);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int parentId = rs.getInt("id_parent");
                    if (ids.add(parentId)) queue.add(parentId);
                }

                // Ajoute les enfants
                ps = conn.prepareStatement("SELECT id_enfant FROM lien_parent WHERE id_parent = ?");
                ps.setInt(1, courant);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int enfantId = rs.getInt("id_enfant");
                    if (ids.add(enfantId)) queue.add(enfantId);
                }
            }

            // Charge les personnes concernées
            if (ids.isEmpty()) return;
            String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM personne WHERE id IN (" + placeholders + ")");
            int i = 1;
            for (int id : ids) ps.setInt(i++, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Personne p = new Personne(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getDate("date_naissance"),
                        rs.getString("mot_de_passe"),
                        rs.getBoolean("inscrit"),
                        rs.getString("photo"),
                        rs.getObject("niveau", Integer.class)
                );
                personnes.put(p.getId(), p);
            }

            // Charge les liens parents-enfants uniquement sur ce sous-ensemble
            ps = conn.prepareStatement("SELECT * FROM lien_parent WHERE id_enfant IN (" + placeholders + ")");
            i = 1;
            for (int id : ids) ps.setInt(i++, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                int parentId = rs.getInt("id_parent");
                int enfantId = rs.getInt("id_enfant");
                relations.computeIfAbsent(parentId, k -> new ArrayList<>()).add(enfantId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔄 Met à jour les niveaux générationnels des personnes dans la base
    public void mettreAJourNiveaux() {
        try (Connection conn = Database.getConnection()) {

            // Réinitialise tous les niveaux
            PreparedStatement reset = conn.prepareStatement("UPDATE personne SET niveau = NULL");
            reset.executeUpdate();

            // Recherche les ancêtres (sans parents)
            PreparedStatement ancetres = conn.prepareStatement("""
                SELECT id FROM personne 
                WHERE id NOT IN (SELECT id_enfant FROM lien_parent)
            """);
            ResultSet rs = ancetres.executeQuery();

            Queue<Integer> queue = new LinkedList<>();
            Map<Integer, Integer> niveaux = new HashMap<>();

            while (rs.next()) {
                int id = rs.getInt("id");
                niveaux.put(id, 0); // niveau racine
                queue.add(id);
            }

            // Propagation descendante
            while (!queue.isEmpty()) {
                int parent = queue.poll();
                int niveauParent = niveaux.get(parent);

                PreparedStatement enfants = conn.prepareStatement("SELECT id_enfant FROM lien_parent WHERE id_parent = ?");
                enfants.setInt(1, parent);
                ResultSet enfantsRs = enfants.executeQuery();

                while (enfantsRs.next()) {
                    int enfant = enfantsRs.getInt("id_enfant");
                    int niveauEnfant = niveauParent + 1;

                    if (!niveaux.containsKey(enfant) || niveaux.get(enfant) < niveauEnfant) {
                        niveaux.put(enfant, niveauEnfant);
                        queue.add(enfant);
                    }
                }
            }

            // Propagation vers le haut pour corriger les incohérences
            boolean modifie;
            do {
                modifie = false;
                PreparedStatement allLiens = conn.prepareStatement("SELECT id_enfant, id_parent FROM lien_parent");
                rs = allLiens.executeQuery();
                while (rs.next()) {
                    int enfant = rs.getInt("id_enfant");
                    int parent = rs.getInt("id_parent");

                    if (!niveaux.containsKey(enfant)) continue;

                    int niveauEnfant = niveaux.get(enfant);
                    int niveauParentCandidat = niveauEnfant - 1;

                    if (!niveaux.containsKey(parent) || niveaux.get(parent) > niveauParentCandidat) {
                        niveaux.put(parent, niveauParentCandidat);
                        modifie = true;
                    }
                }
            } while (modifie);

            // Met les deux parents d’un même enfant au même niveau
            PreparedStatement parentsPair = conn.prepareStatement("""
                SELECT id_enfant FROM lien_parent 
                GROUP BY id_enfant 
                HAVING COUNT(*) = 2
            """);
            rs = parentsPair.executeQuery();
            while (rs.next()) {
                int enfant = rs.getInt("id_enfant");

                PreparedStatement ps = conn.prepareStatement("SELECT id_parent FROM lien_parent WHERE id_enfant = ?");
                ps.setInt(1, enfant);
                ResultSet prs = ps.executeQuery();

                List<Integer> parents = new ArrayList<>();
                while (prs.next()) parents.add(prs.getInt("id_parent"));
                if (parents.size() == 2 && niveaux.containsKey(parents.get(0))) {
                    int niveau = niveaux.get(parents.get(0));
                    niveaux.put(parents.get(1), niveau);
                }
            }

            // Mise à jour effective dans la base
            PreparedStatement update = conn.prepareStatement("UPDATE personne SET niveau = ? WHERE id = ?");
            for (Map.Entry<Integer, Integer> entry : niveaux.entrySet()) {
                update.setInt(1, entry.getValue());
                update.setInt(2, entry.getKey());
                update.addBatch();
            }
            update.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔍 Renvoie l'autre parent d’un enfant
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
        return -1;
    }

    // 🔍 Charge les données d'une personne depuis son ID
    public static Personne getPersonneParId(int id) {
        String query = "SELECT * FROM personne WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Personne p = new Personne();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setPrenom(rs.getString("prenom"));
                p.setDateNaissance(rs.getDate("date_naissance"));
                p.setMotDePasse(rs.getString("mot_de_passe"));
                p.setInscrit(rs.getBoolean("inscrit"));
                p.setPhoto(rs.getString("photo"));
                p.setNiveau(rs.getObject("niveau", Integer.class));
                return p;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 📦 Retourne la liste des enfants d’un parent
    public static List<Integer> getEnfants(int idParent) {
        List<Integer> enfants = new ArrayList<>();
        String query = "SELECT id_enfant FROM lien_parent WHERE id_parent = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idParent);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                enfants.add(rs.getInt("id_enfant"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return enfants;
    }

    // ➕ Crée une personne "inconnue" (non inscrite) dans la base
    public static int ajouterPersonneInconnue(String nomBase, String prenomBase) {
        int compteur = 1;
        String nom, prenom;

        try (Connection conn = Database.getConnection()) {
            // Cherche un nom/prenom libre
            while (true) {
                nom = nomBase + " " + compteur;
                prenom = prenomBase + " " + compteur;
                PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM personne WHERE nom = ? AND prenom = ?");
                check.setString(1, nom);
                check.setString(2, prenom);
                ResultSet rs = check.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) break; // nom disponible
                compteur++;
            }

            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO personne (nom, prenom, date_naissance, mot_de_passe, inscrit, photo) VALUES (?, ?, NULL, NULL, 0, 'defaut.png')",
                    Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, nom);
            stmt.setString(2, prenom);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


    public void chargerFamilleParIds(Set<Integer> ids) {
        personnes.clear();
        relations.clear();

        if (ids == null || ids.isEmpty()) return;

        try (Connection conn = Database.getConnection()) {
            String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM personne WHERE id IN (" + placeholders + ")");
            int i = 1;
            for (int id : ids) ps.setInt(i++, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Personne p = new Personne(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getDate("date_naissance"),
                        rs.getString("mot_de_passe"),
                        rs.getBoolean("inscrit"),
                        rs.getString("photo"),
                        rs.getObject("niveau", Integer.class)
                );
                personnes.put(p.getId(), p);
            }

            ps = conn.prepareStatement("SELECT * FROM lien_parent WHERE id_enfant IN (" + placeholders + ")");
            i = 1;
            for (int id : ids) ps.setInt(i++, id);
            rs = ps.executeQuery();

            while (rs.next()) {
                int parent = rs.getInt("id_parent");
                int enfant = rs.getInt("id_enfant");
                if (ids.contains(parent)) {
                    relations.computeIfAbsent(parent, k -> new ArrayList<>()).add(enfant);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    // ➕ Ajoute un lien parent-enfant en évitant les doublons
    public static void ajouterLienParent(int idEnfant, int idParent, String typeLien) {
        String checkQuery = "SELECT COUNT(*) FROM lien_parent WHERE id_enfant = ? AND type_lien = ?";
        String insertQuery = "INSERT INTO lien_parent (id_enfant, id_parent, type_lien) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setInt(1, idEnfant);
            checkStmt.setString(2, typeLien);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // Empêche d’ajouter un parent si un du même type existe
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Impossible d'ajouter");
                alert.setHeaderText("Parent déjà existant");
                alert.setContentText("L'enfant a déjà un(e) " + (typeLien.equals("pere") ? "père" : "mère") + ".");
                alert.showAndWait();
                return;
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, idEnfant);
                insertStmt.setInt(2, idParent);
                insertStmt.setString(3, typeLien);
                insertStmt.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public List<Integer> getEnfantsCommun(int parent1Id, int parent2Id) {
        List<Integer> enfantsCommuns = new ArrayList<>();
        List<Integer> enfantsParent1 = relations.getOrDefault(parent1Id, new ArrayList<>());
        for (int enfantId : enfantsParent1) {
            int autreParent = getAutreParent(enfantId, parent1Id);
            if (autreParent == parent2Id) {
                enfantsCommuns.add(enfantId);
            }
        }
        return enfantsCommuns;
    }

    public Map<Integer, Personne> getFamille(int userId) {
        Map<Integer, Personne> map = new HashMap<>();
        Set<Integer> ids = new HashSet<>();
        Queue<Integer> q = new LinkedList<>();
        ids.add(userId);
        q.add(userId);

        try (Connection conn = Database.getConnection()) {
            while (!q.isEmpty()) {
                int id = q.poll();

                PreparedStatement ps1 = conn.prepareStatement("SELECT id_parent FROM lien_parent WHERE id_enfant = ?");
                ps1.setInt(1, id);
                ResultSet r1 = ps1.executeQuery();
                while (r1.next()) {
                    int p = r1.getInt("id_parent");
                    if (ids.add(p)) q.add(p);
                }

                PreparedStatement ps2 = conn.prepareStatement("SELECT id_enfant FROM lien_parent WHERE id_parent = ?");
                ps2.setInt(1, id);
                ResultSet r2 = ps2.executeQuery();
                while (r2.next()) {
                    int e = r2.getInt("id_enfant");
                    if (ids.add(e)) q.add(e);
                }

            }

            if (!ids.isEmpty()) {
                String in = String.join(",", Collections.nCopies(ids.size(), "?"));
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM personne WHERE id IN (" + in + ")");
                int i = 1;
                for (int id : ids) ps.setInt(i++, id);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Personne p = new Personne(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getDate("date_naissance"),
                            rs.getString("mot_de_passe"),
                            rs.getBoolean("inscrit"),
                            rs.getString("photo"),
                            rs.getObject("niveau", Integer.class)
                    );
                    map.put(p.getId(), p);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    // 🔍 Si un parent a déjà un enfant, retourne l'autre parent associé à cet enfant
    public Integer getPartenaireExistant(int idParent) {
        String sql = """
        SELECT lp2.id_parent FROM lien_parent lp1
        JOIN lien_parent lp2 ON lp1.id_enfant = lp2.id_enfant
        WHERE lp1.id_parent = ? AND lp2.id_parent != ?
        LIMIT 1
    """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idParent);
            ps.setInt(2, idParent);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_parent");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // aucun partenaire existant
    }

    //Vérifie si un parent peut avoir un enfant avec un second partenaire
    public boolean estPartenaireCompatible(int parent1, int parent2) {
        Integer partenaireExistant = getPartenaireExistant(parent1);
        return partenaireExistant == null || partenaireExistant == parent2;
    }



    public static List<Integer> getAscendants(int id) {
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        List<Integer> ascendants = new ArrayList<>();

        try (Connection conn = Database.getConnection()) {
            queue.add(id);
            while (!queue.isEmpty()) {
                int current = queue.poll();
                PreparedStatement ps = conn.prepareStatement("SELECT id_parent FROM lien_parent WHERE id_enfant = ?");
                ps.setInt(1, current);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int parentId = rs.getInt("id_parent");
                    if (visited.add(parentId)) {
                        ascendants.add(parentId);
                        queue.add(parentId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ascendants;
    }

    public static List<Integer> getParents(int idEnfant) {
        List<Integer> parents = new ArrayList<>();
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT id_parent FROM lien_parent WHERE id_enfant = ?");
            ps.setInt(1, idEnfant);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                parents.add(rs.getInt("id_parent"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parents;
    }

    public void chargerFamillePourArbre(int idArbre) {
        personnes.clear();
        relations.clear();

        try (Connection conn = Database.getConnection()) {
            // 1. Charger toutes les personnes associées à l'id_arbre donné
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM personne WHERE id_arbre = ?");
            ps.setInt(1, idArbre);
            ResultSet rs = ps.executeQuery();

            Set<Integer> ids = new HashSet<>();
            while (rs.next()) {
                Personne p = new Personne(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getDate("date_naissance"),
                        rs.getString("mot_de_passe"),
                        rs.getBoolean("inscrit"),
                        rs.getString("photo"),
                        rs.getObject("niveau", Integer.class)
                );
                personnes.put(p.getId(), p);
                ids.add(p.getId());
            }

            // 2. Charger les relations parent-enfant entre ces personnes
            if (!ids.isEmpty()) {
                String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
                ps = conn.prepareStatement(
                        "SELECT * FROM lien_parent WHERE id_enfant IN (" + placeholders + ")"
                );
                int i = 1;
                for (int id : ids) {
                    ps.setInt(i++, id);
                }

                rs = ps.executeQuery();
                while (rs.next()) {
                    int parentId = rs.getInt("id_parent");
                    int enfantId = rs.getInt("id_enfant");

                    // Vérifie que le parent est aussi dans le même arbre (sécurité)
                    if (ids.contains(parentId)) {
                        relations.computeIfAbsent(parentId, k -> new ArrayList<>()).add(enfantId);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getTousLesIdArbres() {
        List<Integer> idArbres = new ArrayList<>();

        String query = """
        SELECT DISTINCT p.id_arbre
        FROM personne p
        JOIN utilisateur u ON u.id_personne = p.id
        WHERE p.id_arbre IS NOT NULL
          AND u.role = 'utilisateur'
          AND u.statut = 'valide'
    """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                idArbres.add(rs.getInt("id_arbre"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return idArbres;
    }


}