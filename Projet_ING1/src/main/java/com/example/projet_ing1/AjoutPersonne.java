package com.example.projet_ing1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Fenêtre JavaFX permettant d'ajouter un proche (parent ou enfant) à une personne de sa famille.
 * Deux modes sont proposés : ajout d'un proche inconnu ou création manuelle d'une nouvelle personne.
 * Le lien parental est automatiquement enregistré dans la base de données.
 */
public class AjoutPersonne extends Application {

    // Map stockant les membres de la famille de l'utilisateur courant (clé = id, valeur = Personne)
    private Map<Integer, Personne> famille;

    @Override
    public void start(Stage stage) {
        Connection conn;
        try {
            // Connexion à la base de données
            conn = Database.getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
            alert("Erreur de base", "Impossible de se connecter à la base.");
            return; // Arrêt si la base est inaccessible
        }

        int userId = Session.getUserId(); // Récupère l'utilisateur connecté
        famille = new ArbreDAO().getFamille(userId); // Récupère sa famille (tous les membres reliés)

        // Interface : sélection de la personne cible à laquelle on veut ajouter un proche
        Label cibleLabel = new Label("À qui ajouter un proche ?");
        ComboBox<Personne> cibleCombo = new ComboBox<>();
        cibleCombo.getItems().addAll(famille.values());

        // Interface : sélection du lien à ajouter
        Label lienLabel = new Label("Lien à ajouter :");
        ComboBox<String> lienBox = new ComboBox<>();
        lienBox.getItems().addAll("enfant", "pere", "mere");

        // Interface : méthode d'ajout du proche (inconnu ou création manuelle)
        Label choixLabel = new Label("Méthode d’ajout :");
        ComboBox<String> choixBox = new ComboBox<>();
        choixBox.getItems().addAll("Inconnu", "Créer manuellement");

        // Champs de saisie pour création manuelle
        TextField nomField = new TextField();
        TextField prenomField = new TextField();
        DatePicker datePicker = new DatePicker();
        VBox manuelBox = new VBox(5,
                new Label("Nom :"), nomField,
                new Label("Prénom :"), prenomField,
                new Label("Date de naissance :"), datePicker);
        manuelBox.setVisible(false); // Caché par défaut, affiché si "Créer manuellement" est sélectionné

        // Bouton final de validation
        Button ajouterBtn = new Button("Ajouter");

        // Affiche le formulaire de création manuelle si la bonne option est choisie
        choixBox.setOnAction(e -> {
            String choix = choixBox.getValue();
            manuelBox.setVisible("Créer manuellement".equals(choix));
        });

        // Logique déclenchée au clic sur "Ajouter"
        ajouterBtn.setOnAction(e -> {
            try {
                // Récupération des champs remplis par l’utilisateur
                Personne cible = cibleCombo.getValue();
                String lien = lienBox.getValue();
                String choix = choixBox.getValue();

                if (cible == null || lien == null || choix == null) {
                    alert("Champs manquants", "Remplissez tous les champs.");
                    return;
                }

                int idCible = cible.getId(); // ID de la personne à laquelle on ajoute le lien
                int idProche = -1;
                Personne prochePers = null; // Représente la personne qu'on va ajouter

                // Mode "Inconnu" : proche anonyme sans informations
                if (choix.equals("Inconnu")) {
                    prochePers = new Personne();
                    prochePers.setNom("Inconnu");
                    prochePers.setPrenom("inconnu");
                    prochePers.setDateNaissance(null);

                    // Mode "Créer manuellement" : on récupère les données saisies
                } else if (choix.equals("Créer manuellement")) {
                    if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() || datePicker.getValue() == null) {
                        alert("Champs manquants", "Remplissez nom, prénom et date.");
                        return;
                    }
                    prochePers = new Personne();
                    prochePers.setNom(nomField.getText());
                    prochePers.setPrenom(prenomField.getText());
                    prochePers.setDateNaissance(java.sql.Date.valueOf(datePicker.getValue()));
                }

                // Vérification de la cohérence des âges
                Personne ciblePers = ArbreDAO.getPersonneParId(idCible);

                if (ciblePers.getDateNaissance() != null && prochePers.getDateNaissance() != null) {
                    int anneeCible = ciblePers.getDateNaissance().toLocalDate().getYear();
                    int anneeProche = prochePers.getDateNaissance().toLocalDate().getYear();

                    // Règle : un enfant doit avoir au moins 10 ans de moins que le parent
                    if ("enfant".equals(lien) && anneeProche <= anneeCible + 10) {
                        alert("Âge incohérent", "Un enfant doit avoir au moins 10 ans de moins que vous.");
                        return;
                    }

                    // Règle : un parent doit avoir au moins 10 ans de plus que l’enfant
                    if (!"enfant".equals(lien) && anneeProche >= anneeCible - 10) {
                        alert("Âge incohérent", "Un parent doit avoir au moins 10 ans de plus que vous.");
                        return;
                    }
                }

                // Vérifie qu'on ne dépasse pas la limite de deux parents
                List<Integer> parentsExistants = ArbreDAO.getParents("enfant".equals(lien) ? idProche : idCible);
                if (parentsExistants.size() >= 2) {
                    alert("Erreur", "Cette personne a déjà deux parents.");
                    return;
                }

                // Insertion du proche dans la base
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO personne (nom, prenom, date_naissance, mot_de_passe, inscrit, photo) VALUES (?, ?, ?, NULL, 0, 'defaut.png')",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, prochePers.getNom());
                ps.setString(2, prochePers.getPrenom());
                ps.setDate(3, prochePers.getDateNaissance());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) idProche = rs.getInt(1);
                prochePers.setId(idProche);

                if (idProche == -1) {
                    alert("Erreur", "Impossible de créer le lien.");
                    return;
                }

                // Cas où on ajoute un enfant à la personne cible
                if (lien.equals("enfant")) {
                    int secondParentId;

                    // Fenêtre de confirmation : souhaite-t-on aussi ajouter le second parent ?
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Deux parents");
                    confirm.setHeaderText("Souhaitez-vous ajouter le second parent ?");
                    confirm.setContentText("Sinon une personne inconnue sera ajoutée.");
                    ButtonType oui = new ButtonType("Oui");
                    ButtonType non = new ButtonType("Non");
                    confirm.getButtonTypes().setAll(oui, non);
                    Optional<ButtonType> rep = confirm.showAndWait();

                    if (rep.isPresent() && rep.get() == oui) {
                        // L'utilisateur souhaite choisir ou créer un second parent
                        secondParentId = selectionnerParent(conn, idProche, idCible);
                    } else {
                        // Si non : on crée automatiquement une personne inconnue
                        secondParentId = ArbreDAO.ajouterPersonneInconnue("Inconnu", "inconnu");
                    }

                    ArbreDAO dao = new ArbreDAO();
                    Integer partenaireExistant = dao.getPartenaireExistant(idCible);

                    // Vérifie que le second parent est cohérent avec le partenaire déjà existant
                    if (partenaireExistant != null && partenaireExistant != secondParentId) {
                        alert("Erreur", "Ce parent a déjà un partenaire pour ses enfants. Veuillez utiliser le même partenaire.");
                        return;
                    }

                    // Ajout du lien "enfant → père" et "enfant → mère"
                    ArbreDAO.ajouterLienParent(idProche, idCible, "pere");
                    ArbreDAO.ajouterLienParent(idProche, secondParentId, "mere");

                } else {
                    // Cas inverse : on ajoute un parent à une personne
                    ArbreDAO.ajouterLienParent(idCible, idProche, lien);

                    // Déduction automatique de l’autre type de parent
                    String autreType = lien.equals("pere") ? "mere" : "pere";

                    // Confirmation : souhaite-t-on ajouter aussi l’autre parent ?
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Autre parent");
                    confirm.setHeaderText("Souhaitez-vous ajouter l'autre parent ?");
                    confirm.setContentText("Sinon une personne inconnue sera ajoutée.");
                    ButtonType oui = new ButtonType("Oui");
                    ButtonType non = new ButtonType("Non");
                    confirm.getButtonTypes().setAll(oui, non);
                    Optional<ButtonType> rep = confirm.showAndWait();

                    int secondId;
                    if (rep.isPresent() && rep.get() == oui) {
                        secondId = selectionnerParent(conn, idCible, idProche);
                    } else {
                        secondId = ArbreDAO.ajouterPersonneInconnue("Inconnu", "inconnu");
                    }

                    // Ajout de l’autre parent à la base
                    ArbreDAO.ajouterLienParent(idCible, secondId, autreType);
                }

                // Mise à jour des niveaux générationnels après l’ajout
                new ArbreDAO().mettreAJourNiveaux();

                // Affichage d’un bouton de retour une fois l’ajout terminé
                VBox retourBox = new VBox(10);
                retourBox.setAlignment(Pos.CENTER);
                Button retourBtn = new Button("Retour à l'arbre");
                retourBtn.setOnAction(ev -> stage.close());
                retourBox.getChildren().add(retourBtn);
                stage.setScene(new Scene(retourBox, 300, 150));

            } catch (Exception ex) {
                // Gestion des erreurs
                ex.printStackTrace();
                alert("Erreur", ex.getMessage());
            }
        });

        // Affichage du formulaire principal
        VBox root = new VBox(10, cibleLabel, cibleCombo, lienLabel, lienBox, choixLabel, choixBox, manuelBox, ajouterBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 500, 600));
        stage.setTitle("Ajouter un proche");
        stage.show();
    }

    /**
     * Affiche une alerte d’erreur bloquante avec un titre et un message
     */
    private void alert(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titre);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    /**
     * Permet de sélectionner ou créer le second parent lors de l’ajout d’un enfant ou d’un parent.
     * Propose 3 options : Inconnu, Créer manuellement, ou Choisir dans la famille (si applicable).
     * Retourne l’ID du parent sélectionné ou créé.
     */
    private int selectionnerParent(Connection conn, int idCible, int dejaAjoute) throws Exception {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL); // Rend la fenêtre modale (bloque les autres)
        dialog.setTitle("Ajout du second parent");

        ComboBox<String> methodeBox = new ComboBox<>();
        methodeBox.getItems().addAll("Inconnu", "Créer manuellement");

        // On charge toute la famille pour détecter un éventuel partenaire existant
        ArbreDAO dao = new ArbreDAO();
        dao.chargerFamillePourUtilisateur(Session.getUserId());

        // Si un partenaire fixe existe, on ajoute l’option "Choisir dans la famille"
        Integer partenaireFixe = dao.getPartenaireExistant(dejaAjoute);
        ComboBox<Personne> existantCombo = new ComboBox<>();
        boolean aUnPartenaire = false;

        if (partenaireFixe != null) {
            methodeBox.getItems().add("Choisir dans la famille"); // Option supplémentaire
            Map<Integer, Personne> famille = dao.getFamille(Session.getUserId());
            Personne partenaire = famille.get(partenaireFixe);
            if (partenaire != null) {
                existantCombo.getItems().add(partenaire);
                existantCombo.getSelectionModel().select(partenaire); // Sélection automatique
                aUnPartenaire = true;
            }
        }

        // Champs pour la création manuelle
        TextField nomField = new TextField();
        TextField prenomField = new TextField();
        DatePicker datePicker = new DatePicker();
        VBox manuelBox = new VBox(5,
                new Label("Nom :"), nomField,
                new Label("Prénom :"), prenomField,
                new Label("Date de naissance :"), datePicker);
        manuelBox.setVisible(false);
        existantCombo.setVisible(false);

        // On récupère la date de naissance de la personne cible pour vérifier les âges
        Personne cible = ArbreDAO.getPersonneParId(idCible);
        LocalDate dateCible = (cible.getDateNaissance() != null) ? cible.getDateNaissance().toLocalDate() : null;

        // Affiche dynamiquement la bonne interface selon le choix
        methodeBox.setOnAction(e -> {
            String choix = methodeBox.getValue();
            manuelBox.setVisible("Créer manuellement".equals(choix));
            existantCombo.setVisible("Choisir dans la famille".equals(choix));
        });

        Button valider = new Button("Valider");
        final int[] selectedId = {-1}; // Tableau à un élément pour capturer la valeur dans la lambda

        valider.setOnAction(e -> {
            try {
                String choix = methodeBox.getValue();
                if (choix == null) return;

                // Cas 1 : création automatique d’une personne inconnue
                if ("Inconnu".equals(choix)) {
                    selectedId[0] = ArbreDAO.ajouterPersonneInconnue("Inconnu", "inconnu");

                    // Cas 2 : création manuelle avec vérification d’âge
                } else if ("Créer manuellement".equals(choix)) {
                    if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() || datePicker.getValue() == null) {
                        alert("Champs manquants", "Remplissez nom, prénom et date.");
                        return;
                    }

                    LocalDate dateProche = datePicker.getValue();
                    if (dateCible != null && dateProche.isAfter(dateCible.minusYears(10))) {
                        alert("Âge incohérent", "Le second parent doit avoir au moins 10 ans de plus que l’enfant.");
                        return;
                    }

                    // Insertion en base
                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO personne (nom, prenom, date_naissance, mot_de_passe, inscrit, photo) VALUES (?, ?, ?, NULL, 0, 'defaut.png')",
                            Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, nomField.getText());
                    ps.setString(2, prenomField.getText());
                    ps.setDate(3, java.sql.Date.valueOf(dateProche));
                    ps.executeUpdate();
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) selectedId[0] = rs.getInt(1);

                    // Cas 3 : sélection dans la famille existante
                } else if ("Choisir dans la famille".equals(choix)) {
                    Personne p = existantCombo.getValue();
                    if (p == null) {
                        alert("Erreur", "Aucune personne sélectionnée.");
                        return;
                    }

                    if (p.getDateNaissance() != null && dateCible != null) {
                        LocalDate dateProche = p.getDateNaissance().toLocalDate();
                        if (dateProche.isAfter(dateCible.minusYears(10))) {
                            alert("Âge incohérent", "Le second parent doit avoir au moins 10 ans de plus que l’enfant.");
                            return;
                        }
                    }

                    selectedId[0] = p.getId(); // ID du parent existant
                }

                if (selectedId[0] != -1) dialog.close(); // Ferme la fenêtre si tout s'est bien passé

            } catch (Exception ex) {
                ex.printStackTrace();
                alert("Erreur interne", ex.getMessage());
            }
        });

        // Mise en page de la fenêtre
        VBox layout = new VBox(10,
                new Label("Méthode d’ajout du parent :"),
                methodeBox,
                manuelBox);
        if (aUnPartenaire) layout.getChildren().add(existantCombo);
        layout.getChildren().add(valider);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER);

        dialog.setScene(new Scene(layout, 400, 350));
        dialog.showAndWait();

        if (selectedId[0] == -1) throw new Exception("Aucun parent sélectionné");
        return selectedId[0];
    }

}

