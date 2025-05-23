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

public class AjoutPersonne extends Application {

    private Map<Integer, Personne> famille;

    @Override
    public void start(Stage stage) {
        Connection conn;
        try {
            conn = Database.getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
            alert("Erreur de base", "Impossible de se connecter à la base.");
            return;
        }

        int userId = Session.getUserId();
        famille = new ArbreDAO().getFamille(userId);

        Label cibleLabel = new Label("À qui ajouter un proche ?");
        ComboBox<Personne> cibleCombo = new ComboBox<>();
        cibleCombo.getItems().addAll(famille.values());

        Label lienLabel = new Label("Lien à ajouter :");
        ComboBox<String> lienBox = new ComboBox<>();
        lienBox.getItems().addAll("enfant", "pere", "mere");

        Label choixLabel = new Label("Méthode d’ajout :");
        ComboBox<String> choixBox = new ComboBox<>();
        choixBox.getItems().addAll("Inconnu", "Créer manuellement");

        TextField nomField = new TextField();
        TextField prenomField = new TextField();
        DatePicker datePicker = new DatePicker();
        VBox manuelBox = new VBox(5,
                new Label("Nom :"), nomField,
                new Label("Prénom :"), prenomField,
                new Label("Date de naissance :"), datePicker);
        manuelBox.setVisible(false);

        Button ajouterBtn = new Button("Ajouter");

        choixBox.setOnAction(e -> {
            String choix = choixBox.getValue();
            manuelBox.setVisible("Créer manuellement".equals(choix));
        });

        ajouterBtn.setOnAction(e -> {
            try {
                Personne cible = cibleCombo.getValue();
                String lien = lienBox.getValue();
                String choix = choixBox.getValue();

                if (cible == null || lien == null || choix == null) {
                    alert("Champs manquants", "Remplissez tous les champs.");
                    return;
                }

                int idCible = cible.getId();
                int idProche = -1;
                Personne prochePers = null;

                if (choix.equals("Inconnu")) {
                    prochePers = new Personne();
                    prochePers.setNom("Inconnu");
                    prochePers.setPrenom("inconnu");
                    prochePers.setDateNaissance(null);
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

                Personne ciblePers = ArbreDAO.getPersonneParId(idCible);

                if (ciblePers.getDateNaissance() != null && prochePers.getDateNaissance() != null) {
                    int anneeCible = ciblePers.getDateNaissance().toLocalDate().getYear();
                    int anneeProche = prochePers.getDateNaissance().toLocalDate().getYear();

                    if ("enfant".equals(lien) && anneeProche <= anneeCible + 10) {
                        alert("Âge incohérent", "Un enfant doit avoir au moins 10 ans de moins que vous.");
                        return;
                    }
                    if (!"enfant".equals(lien) && anneeProche >= anneeCible - 10) {
                        alert("Âge incohérent", "Un parent doit avoir au moins 10 ans de plus que vous.");
                        return;
                    }
                }

                List<Integer> parentsExistants = ArbreDAO.getParents("enfant".equals(lien) ? idProche : idCible);
                if (parentsExistants.size() >= 2) {
                    alert("Erreur", "Cette personne a déjà deux parents.");
                    return;
                }

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

                if (lien.equals("enfant")) {
                    int secondParentId;
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Deux parents");
                    confirm.setHeaderText("Souhaitez-vous ajouter le second parent ?");
                    confirm.setContentText("Sinon une personne inconnue sera ajoutée.");
                    ButtonType oui = new ButtonType("Oui");
                    ButtonType non = new ButtonType("Non");
                    confirm.getButtonTypes().setAll(oui, non);
                    Optional<ButtonType> rep = confirm.showAndWait();

                    if (rep.isPresent() && rep.get() == oui) {
                        secondParentId = selectionnerParent(conn, idProche, idCible);
                    } else {
                        secondParentId = ArbreDAO.ajouterPersonneInconnue("Inconnu", "inconnu");
                    }

                    ArbreDAO dao = new ArbreDAO();
                    Integer partenaireExistant = dao.getPartenaireExistant(idCible);
                    if (partenaireExistant != null && partenaireExistant != secondParentId) {
                        alert("Erreur", "Ce parent a déjà un partenaire pour ses enfants. Veuillez utiliser le même partenaire.");
                        return;
                    }

                    ArbreDAO.ajouterLienParent(idProche, idCible, "pere");
                    ArbreDAO.ajouterLienParent(idProche, secondParentId, "mere");

                } else {
                    ArbreDAO.ajouterLienParent(idCible, idProche, lien);
                    String autreType = lien.equals("pere") ? "mere" : "pere";

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

                    ArbreDAO.ajouterLienParent(idCible, secondId, autreType);
                }

                new ArbreDAO().mettreAJourNiveaux();

                VBox retourBox = new VBox(10);
                retourBox.setAlignment(Pos.CENTER);
                Button retourBtn = new Button("Retour à l'arbre");
                retourBtn.setOnAction(ev -> stage.close());
                retourBox.getChildren().add(retourBtn);
                stage.setScene(new Scene(retourBox, 300, 150));

            } catch (Exception ex) {
                ex.printStackTrace();
                alert("Erreur", ex.getMessage());
            }
        });

        VBox root = new VBox(10, cibleLabel, cibleCombo, lienLabel, lienBox, choixLabel, choixBox, manuelBox, ajouterBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 500, 600));
        stage.setTitle("Ajouter un proche");
        stage.show();
    }

    private void alert(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titre);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private int selectionnerParent(Connection conn, int idCible, int dejaAjoute) throws Exception {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Ajout du second parent");

        ComboBox<String> methodeBox = new ComboBox<>();
        methodeBox.getItems().addAll("Inconnu", "Créer manuellement");

        // Si un partenaire existe déjà : on ajoute "Choisir dans la famille"
        ArbreDAO dao = new ArbreDAO();
        dao.chargerFamillePourUtilisateur(Session.getUserId());

        Integer partenaireFixe = dao.getPartenaireExistant(dejaAjoute);
        ComboBox<Personne> existantCombo = new ComboBox<>();
        boolean aUnPartenaire = false;

        if (partenaireFixe != null) {
            methodeBox.getItems().add("Choisir dans la famille");
            Map<Integer, Personne> famille = dao.getFamille(Session.getUserId());
            Personne partenaire = famille.get(partenaireFixe);
            if (partenaire != null) {
                existantCombo.getItems().add(partenaire);
                existantCombo.getSelectionModel().select(partenaire); // Pré-sélection
                aUnPartenaire = true;
            }
        }

        // Création manuelle
        TextField nomField = new TextField();
        TextField prenomField = new TextField();
        DatePicker datePicker = new DatePicker();
        VBox manuelBox = new VBox(5,
                new Label("Nom :"), nomField,
                new Label("Prénom :"), prenomField,
                new Label("Date de naissance :"), datePicker);
        manuelBox.setVisible(false);
        existantCombo.setVisible(false);

        Personne cible = ArbreDAO.getPersonneParId(idCible);
        LocalDate dateCible = (cible.getDateNaissance() != null) ? cible.getDateNaissance().toLocalDate() : null;

        methodeBox.setOnAction(e -> {
            String choix = methodeBox.getValue();
            manuelBox.setVisible("Créer manuellement".equals(choix));
            existantCombo.setVisible("Choisir dans la famille".equals(choix));
        });

        Button valider = new Button("Valider");
        final int[] selectedId = {-1};

        valider.setOnAction(e -> {
            try {
                String choix = methodeBox.getValue();
                if (choix == null) return;

                if ("Inconnu".equals(choix)) {
                    selectedId[0] = ArbreDAO.ajouterPersonneInconnue("Inconnu", "inconnu");

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

                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO personne (nom, prenom, date_naissance, mot_de_passe, inscrit, photo) VALUES (?, ?, ?, NULL, 0, 'defaut.png')",
                            Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, nomField.getText());
                    ps.setString(2, prenomField.getText());
                    ps.setDate(3, java.sql.Date.valueOf(dateProche));
                    ps.executeUpdate();
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) selectedId[0] = rs.getInt(1);

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

                    selectedId[0] = p.getId();
                }

                if (selectedId[0] != -1) dialog.close();

            } catch (Exception ex) {
                ex.printStackTrace();
                alert("Erreur interne", ex.getMessage());
            }
        });

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