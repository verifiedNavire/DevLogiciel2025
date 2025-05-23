package com.example.projet_ing1;

import java.sql.*;
import java.util.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Classe d'accès aux données (DAO) liée à la gestion des arbres généalogiques.
 * Permet de charger les personnes, leurs relations, et de manipuler les arbres.
 */
public class ArbreDAO {

    // Contient toutes les personnes chargées en mémoire (clé = id de la personne)
    public Map<Integer, Personne> personnes = new HashMap<>();

    // Contient toutes les relations parent → enfants
    public Map<Integer, List<Integer>> relations = new HashMap<>();

    /**
     * Charge toutes les personnes et toutes les relations parent-enfant de la base.
     * Utile pour un affichage global sans restriction (admin par exemple).
     */
    public void chargerDepuisBase() {
        try (Connection conn = Database.getConnection()) {

            // Récupération de toutes les personnes dans la base
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

            // Récupération de tous les liens parent-enfant
            ps = conn.prepareStatement("SELECT id_parent, id_enfant FROM lien_parent");
            rs = ps.executeQuery();
            while (rs.next()) {
                int parentId = rs.getInt("id_parent");
                int enfantId = rs.getInt("id_enfant");

                // Pour chaque parent, ajoute l’enfant à la liste des enfants
                relations.computeIfAbsent(parentId, k -> new ArrayList<>()).add(enfantId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Charge uniquement la sous-famille (ancêtres + descendants) d’un utilisateur.
     * Utilise un parcours en largeur (BFS) pour explorer les parents et enfants.
     */
    public void chargerFamillePourUtilisateur(int userId) {
        personnes.clear();
        relations.clear();

        // Ensemble des ID rencontrés et file d’attente pour exploration
        Set<Integer> ids = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        ids.add(userId);
        queue.add(userId);

        try (Connection conn = Database.getConnection()) {

            // Parcours BFS : explore parents et enfants jusqu’à épuisement
            while (!queue.isEmpty()) {
                int courant = queue.poll();

                // Recherche les parents du noeud courant
                PreparedStatement ps = conn.prepareStatement("SELECT id_parent FROM lien_parent WHERE id_enfant = ?");
                ps.setInt(1, courant);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int parentId = rs.getInt("id_parent");
                    if (ids.add(parentId)) queue.add(parentId);
                }

                // Recherche les enfants du noeud courant
                ps = conn.prepareStatement("SELECT id_enfant FROM lien_parent WHERE id_parent = ?");
                ps.setInt(1, courant);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int enfantId = rs.getInt("id_enfant");
                    if (ids.add(enfantId)) queue.add(enfantId);
                }
            }

            // Une fois tous les ID collectés, on récupère leurs données
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

            // On ne charge que les relations entre personnes de cette sous-famille
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

    public void chargerFamillePourUtilisateurVisibilite(int userId) {
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
                        rs.getObject("niveau", Integer.class),
                        rs.getString("visibilite")
                );
                if(p.getVisibilite().equals("public")) {
                    personnes.put(p.getId(), p);
                }
                else{
                    p.setNom("Private");
                    p.setPrenom("Private");
                    personnes.put(p.getId(), p);
                }
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
    /**
     * Calcule et met à jour le niveau générationnel (entier) de chaque personne dans la base.
     * Le niveau 0 est attribué aux ancêtres (personnes sans parents).
     * Les enfants ont un niveau = niveau de leur parent + 1.
     * Une seconde phase remonte les niveaux si incohérence (correction ascendante).
     */
    public void mettreAJourNiveaux() {
        try (Connection conn = Database.getConnection()) {

            // Étape 1 : on réinitialise tous les niveaux à NULL
            PreparedStatement reset = conn.prepareStatement("UPDATE personne SET niveau = NULL");
            reset.executeUpdate();

            // Étape 2 : on identifie les racines (personnes sans parents)
            PreparedStatement ancetres = conn.prepareStatement("""
                SELECT id FROM personne 
                WHERE id NOT IN (SELECT id_enfant FROM lien_parent)
            """);
            ResultSet rs = ancetres.executeQuery();

            Queue<Integer> queue = new LinkedList<>();
            Map<Integer, Integer> niveaux = new HashMap<>();

            // Les racines sont au niveau 0
            while (rs.next()) {
                int id = rs.getInt("id");
                niveaux.put(id, 0);
                queue.add(id);
            }

            // Étape 3 : propagation descendante — BFS pour affecter les niveaux
            while (!queue.isEmpty()) {
                int parent = queue.poll();
                int niveauParent = niveaux.get(parent);

                PreparedStatement enfants = conn.prepareStatement("SELECT id_enfant FROM lien_parent WHERE id_parent = ?");
                enfants.setInt(1, parent);
                ResultSet enfantsRs = enfants.executeQuery();

                while (enfantsRs.next()) {
                    int enfant = enfantsRs.getInt("id_enfant");
                    int niveauEnfant = niveauParent + 1;

                    // Met à jour le niveau de l’enfant s’il est plus bas que ce qu’on avait
                    if (!niveaux.containsKey(enfant) || niveaux.get(enfant) < niveauEnfant) {
                        niveaux.put(enfant, niveauEnfant);
                        queue.add(enfant);
                    }
                }
            }

            // Étape 4 : propagation ascendante pour corriger les incohérences éventuelles
            boolean modifie;
            do {
                modifie = false;
                PreparedStatement allLiens = conn.prepareStatement("SELECT id_enfant, id_parent FROM lien_parent");
                rs = allLiens.executeQuery();
                while (rs.next()) {
                    int enfant = rs.getInt("id_enfant");
                    int parent = rs.getInt("id_parent");

                    // On doit s'assurer que le parent est au moins un niveau au-dessus
                    if (!niveaux.containsKey(enfant)) continue;

                    int niveauEnfant = niveaux.get(enfant);
                    int niveauParentCandidat = niveauEnfant - 1;

                    if (!niveaux.containsKey(parent) || niveaux.get(parent) > niveauParentCandidat) {
                        niveaux.put(parent, niveauParentCandidat);
                        modifie = true;
                    }
                }
            } while (modifie); // On répète tant qu’on fait des corrections

            // Étape 5 : égaliser les niveaux des deux parents d’un même enfant (si besoin)
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

            // Étape 6 : mise à jour effective dans la base de données
            PreparedStatement update = conn.prepareStatement("UPDATE personne SET niveau = ? WHERE id = ?");
            for (Map.Entry<Integer, Integer> entry : niveaux.entrySet()) {
                update.setInt(1, entry.getValue());
                update.setInt(2, entry.getKey());
                update.addBatch(); // prépare la requête
            }
            update.executeBatch(); // exécute toutes les mises à jour en une seule fois

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Retourne l'ID de l'autre parent d’un enfant, différent de celui fourni.
     * Utile pour retrouver les couples de parents.
     */
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
        return -1; // Aucun autre parent trouvé
    }

    /**
     * Charge les informations complètes d'une personne à partir de son identifiant.
     * Retourne un objet Personne rempli ou null si l’ID est introuvable.
     */
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

    /**
     * Retourne la liste des IDs des enfants d’un parent donné.
     */
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

    /**
     * Insère une personne "inconnue" dans la base de données.
     * Incrémente automatiquement un numéro pour éviter les doublons.
     */
    public static int ajouterPersonneInconnue(String nomBase, String prenomBase) {
        int compteur = 1;
        String nom, prenom;

        try (Connection conn = Database.getConnection()) {
            while (true) {
                nom = nomBase + " " + compteur;
                prenom = prenomBase + " " + compteur;

                // Vérifie que le nom/prénom ne sont pas déjà utilisés
                PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM personne WHERE nom = ? AND prenom = ?");
                check.setString(1, nom);
                check.setString(2, prenom);
                ResultSet rs = check.executeQuery();

                if (rs.next() && rs.getInt(1) == 0) break; // nom libre
                compteur++;
            }

            // Insertion de la nouvelle personne inconnue
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
        return -1; // Erreur
    }

    /**
     * Ajoute un lien parent-enfant dans la base s’il n’existe pas déjà.
     * Vérifie le type de lien (père ou mère) pour empêcher les doublons.
     */
    public static void ajouterLienParent(int idEnfant, int idParent, String typeLien) {
        String checkQuery = "SELECT COUNT(*) FROM lien_parent WHERE id_enfant = ? AND type_lien = ?";
        String insertQuery = "INSERT INTO lien_parent (id_enfant, id_parent, type_lien) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setInt(1, idEnfant);
            checkStmt.setString(2, typeLien);
            ResultSet rs = checkStmt.executeQuery();

            // Ne rien faire si un parent de ce type est déjà défini
            if (rs.next() && rs.getInt(1) > 0) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Impossible d'ajouter");
                alert.setHeaderText("Parent déjà existant");
                alert.setContentText("L'enfant a déjà un(e) " + (typeLien.equals("pere") ? "père" : "mère") + ".");
                alert.showAndWait();
                return;
            }

            // Sinon : insertion du lien
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
    /**
     * Retourne les enfants communs à deux parents (utilisé pour tracer un lien de couple).
     * On considère qu’un enfant est commun si l’autre parent correspond.
     */
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

    /**
     * Retourne l’ensemble des membres de la famille reliés à une personne donnée (BFS).
     * Utilisé pour afficher les membres d’un même arbre sans doublons.
     */
    public Map<Integer, Personne> getFamille(int userId) {
        Map<Integer, Personne> map = new HashMap<>();
        Set<Integer> ids = new HashSet<>();
        Queue<Integer> q = new LinkedList<>();
        ids.add(userId);
        q.add(userId);

        try (Connection conn = Database.getConnection()) {
            while (!q.isEmpty()) {
                int id = q.poll();

                // Ajout des parents
                PreparedStatement ps1 = conn.prepareStatement("SELECT id_parent FROM lien_parent WHERE id_enfant = ?");
                ps1.setInt(1, id);
                ResultSet r1 = ps1.executeQuery();
                while (r1.next()) {
                    int p = r1.getInt("id_parent");
                    if (ids.add(p)) q.add(p);
                }

                // Ajout des enfants
                PreparedStatement ps2 = conn.prepareStatement("SELECT id_enfant FROM lien_parent WHERE id_parent = ?");
                ps2.setInt(1, id);
                ResultSet r2 = ps2.executeQuery();
                while (r2.next()) {
                    int e = r2.getInt("id_enfant");
                    if (ids.add(e)) q.add(e);
                }
            }

            // Chargement des données complètes
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

    /**
     * Recherche un éventuel partenaire existant avec qui un parent a déjà un enfant.
     * Cela permet de faire respecter la monogamie (1 seul partenaire pour tous les enfants).
     */
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
                return rs.getInt("id_parent"); // retourne l’autre parent
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // aucun partenaire trouvé
    }

    /**
     * Vérifie si deux parents sont compatibles au regard des enfants existants.
     * Compatible = pas de partenaire existant ou partenaire actuel = second parent proposé.
     */
    public boolean estPartenaireCompatible(int parent1, int parent2) {
        Integer partenaireExistant = getPartenaireExistant(parent1);
        return partenaireExistant == null || partenaireExistant == parent2;
    }

    /**
     * Retourne les IDs des ascendants d’un individu, via une exploration ascendante récursive.
     * Permet d’étudier les lignées et contraintes d’héritage éventuelles.
     */
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

    /**
     * Retourne tous les parents d’un enfant (maximum 2).
     */
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

    /**
     * Charge toute la famille associée à un arbre spécifique (en lisant les `id_arbre` dans la table `personne`).
     * Utile pour afficher tous les arbres validés par des utilisateurs.
     */
    public void chargerFamillePourArbre(int idArbre) {
        personnes.clear();
        relations.clear();

        try (Connection conn = Database.getConnection()) {

            // Vérification de l'existence de l’arbre (optionnel mais propre)
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM arbre WHERE id = ?");
            ps.setInt(1, idArbre);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println("Arbre avec id " + idArbre + " non trouvé.");
                return;
            }

            // Pour chaque personne rattachée à cet arbre
            ps = conn.prepareStatement("SELECT id FROM personne WHERE id_arbre = ?");
            ps.setInt(1, idArbre);
            rs = ps.executeQuery();

            Set<Integer> dejaVus = new HashSet<>();
            while (rs.next()) {
                int personneId = rs.getInt("id");

                // Pour chaque membre, charge récursivement sa famille complète
                if (!dejaVus.contains(personneId)) {
                    chargerFamillePourUtilisateurVisibilite(personneId); // exploite la logique existante
                    dejaVus.addAll(personnes.keySet()); // évite les doublons entre sous-familles
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chargerFamillePourArbreAdmin(int idArbre) {
        personnes.clear();
        relations.clear();

        try (Connection conn = Database.getConnection()) {
            // Vérification de l'existence de l’arbre (optionnel mais propre)
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM arbre WHERE id = ?");
            ps.setInt(1, idArbre);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println("Arbre avec id " + idArbre + " non trouvé.");
                return;
            }

            // Pour chaque personne rattachée à cet arbre
            ps = conn.prepareStatement("SELECT id FROM personne WHERE id_arbre = ?");
            ps.setInt(1, idArbre);
            rs = ps.executeQuery();

            Set<Integer> dejaVus = new HashSet<>();
            while (rs.next()) {
                int personneId = rs.getInt("id");
                // Pour chaque membre, charge récursivement sa famille complète
                if (!dejaVus.contains(personneId)) {
                    chargerFamillePourUtilisateur(personneId); // exploite la logique existante
                    dejaVus.addAll(personnes.keySet()); // évite les doublons entre sous-familles
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère tous les identifiants d’arbres disponibles dans la base.
     * Ne retient que les arbres liés à des utilisateurs valides et approuvés.
     */
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
    public List<Personne> getEnfantsPersonne(int idParent) {
        List<Personne> enfants = new ArrayList<>();
        String query = """
        SELECT p.* FROM lien_parent lp
        JOIN personne p ON lp.id_enfant = p.id
        WHERE lp.id_parent = ?
    """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idParent);
            ResultSet rs = stmt.executeQuery();

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
                enfants.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return enfants;
    }

    public List<Personne> getGrandsParentsPersonne(int idPersonne) {
        List<Personne> grandsParents = new ArrayList<>();
        String query = """
        SELECT gp.* FROM lien_parent lp1
        JOIN lien_parent lp2 ON lp1.id_parent = lp2.id_enfant
        JOIN personne gp ON lp2.id_parent = gp.id
        WHERE lp1.id_enfant = ?
    """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idPersonne);
            ResultSet rs = stmt.executeQuery();

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
                grandsParents.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return grandsParents;
    }
    public List<Personne> getFreresSoeursPersonne(int idPersonne) {
        List<Personne> freresSoeurs = new ArrayList<>();
        String query = """
        SELECT DISTINCT p.* FROM lien_parent lp1
        JOIN lien_parent lp2 ON lp1.id_parent = lp2.id_parent
        JOIN personne p ON lp2.id_enfant = p.id
        WHERE lp1.id_enfant = ?
          AND lp2.id_enfant != ?
    """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idPersonne);
            stmt.setInt(2, idPersonne);
            ResultSet rs = stmt.executeQuery();

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
                freresSoeurs.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return freresSoeurs;
    }

    public List<Personne> getParentsPersonne(int idPersonne) {
        List<Personne> parents = new ArrayList<>();
        String query = """
        SELECT p.* FROM lien_parent lp
        JOIN personne p ON lp.id_parent = p.id
        WHERE lp.id_enfant = ?
    """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idPersonne);
            ResultSet rs = stmt.executeQuery();

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
                parents.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parents;


    }

    public List<Personne> getPetitsEnfantsPersonne(int idPersonne) {
        List<Personne> petitsEnfants = new ArrayList<>();
        String query = """
        SELECT pe.* FROM lien_parent lp1
        JOIN lien_parent lp2 ON lp1.id_enfant = lp2.id_parent
        JOIN personne pe ON lp2.id_enfant = pe.id
        WHERE lp1.id_parent = ?
    """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idPersonne);
            ResultSet rs = stmt.executeQuery();

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
                petitsEnfants.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return petitsEnfants;
    }

}
