package com.example.projet_ing1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;

/**
 * Fenêtre d'inscription pour les nouveaux utilisateurs.
 * Permet la saisie des données personnelles, l'import d'une photo et d'une pièce d'identité,
 * ainsi que la création d’un arbre généalogique privé et d’un compte utilisateur.
 */
public class Inscription extends Application {

    // Fichier image de profil sélectionné par l’utilisateur
    private File selectedImageFile = null;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Inscription");

        // Formulaire sous forme de grille
        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        // Champs texte pour les informations personnelles
        Label nomLabel = new Label("Nom :");
        TextField nomField = new TextField();

        Label prenomLabel = new Label("Prénom :");
        TextField prenomField = new TextField();

        Label emailLabel = new Label("Email :");
        TextField emailField = new TextField();

        Label mdpLabel = new Label("Mot de passe :");
        PasswordField mdpField = new PasswordField();

        Label dateLabel = new Label("Date de naissance (AAAA-MM-JJ) :");
        TextField dateField = new TextField();

        Label secuLabel = new Label("Numéro de sécurité sociale :");
        TextField secuField = new TextField();

        Label nationaliteLabel = new Label("Nationalité :");
        TextField nationaliteField = new TextField();

        // Gestion de la pièce d'identité
        Label idLabel = new Label("Copie de la carte d'identité :");
        Button idButton = new Button("Choisir un fichier");
        Label idFileNameLabel = new Label();

        FileChooser idChooser = new FileChooser();
        idChooser.setTitle("Choisir la carte d'identité");
        idChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF ou Image", "*.pdf", "*.png", "*.jpg", "*.jpeg")
        );
        final File[] selectedIdFile = new File[1];

        idButton.setOnAction(e -> {
            File file = idChooser.showOpenDialog(stage);
            if (file != null) {
                selectedIdFile[0] = file;
                idFileNameLabel.setText(file.getName());
            }
        });

        // Choix de la photo de profil
        Button photoButton = new Button("Choisir une photo");
        Label photoNameLabel = new Label();

        photoButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
            );
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                selectedImageFile = file;
                photoNameLabel.setText(file.getName());
            }
        });

        // Bouton final de validation
        Button validerButton = new Button("Valider");

        validerButton.setOnAction(e -> {
            // Récupération des valeurs saisies
            String nom = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            String email = emailField.getText().trim();
            String mdp = mdpField.getText().trim();
            String dateNaissance = dateField.getText().trim();
            String nomImage = null;
            String secu = secuField.getText().trim();
            String nationalite = nationaliteField.getText().trim();
            String fichierIdentite = null;

            // Copie du fichier d'identité dans le répertoire de stockage
            if (selectedIdFile[0] != null) {
                fichierIdentite = selectedIdFile[0].getName();
                File destDir = new File("src/main/resources/documents/identites/");
                destDir.mkdirs();
                try {
                    Path dest = Paths.get(destDir.getAbsolutePath(), fichierIdentite);
                    Files.copy(selectedIdFile[0].toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur lors de la copie de la pièce d'identité.");
                    return;
                }
            }

            // Vérification des champs obligatoires
            if (nom.isEmpty() || prenom.isEmpty() || mdp.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Tous les champs obligatoires doivent être remplis.");
                return;
            }
            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                showAlert(Alert.AlertType.ERROR, "Veuillez entrer une adresse e-mail valide.");
                return;
            }
            if (dateNaissance.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Veuillez entrer votre date de naissance.");
                return;
            }

            // Copie de l’image de profil dans le dossier dédié
            if (selectedImageFile != null) {
                nomImage = selectedImageFile.getName();
                File destDir = new File("src/main/resources/images/profils/");
                destDir.mkdirs();
                try {
                    Path dest = Paths.get(destDir.getAbsolutePath(), nomImage);
                    Files.copy(selectedImageFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur lors de la copie de l'image.");
                    return;
                }
            }

            // Traitement principal de l’inscription avec gestion SQL
            try (Connection conn = Database.getConnection()) {
                conn.setAutoCommit(false); // Démarre une transaction

                String nomImageFinale = nomImage != null ? nomImage : "defaut.png";
                int personneId = -1;
                int arbreId = -1;
                int utilisateurId = -1;

                // Si une personne non inscrite existe déjà, on met à jour ses données
                PreparedStatement check = conn.prepareStatement(
                        "SELECT id FROM personne WHERE nom = ? AND prenom = ? AND date_naissance = ? AND inscrit = FALSE"
                );
                check.setString(1, nom);
                check.setString(2, prenom);
                check.setString(3, dateNaissance);
                ResultSet rs = check.executeQuery();

                if (rs.next()) {
                    personneId = rs.getInt("id");

                    PreparedStatement update = conn.prepareStatement(
                            "UPDATE personne SET date_naissance = ?, mot_de_passe = ?, photo = ?, inscrit = TRUE, securite_sociale = ?, nationalite = ?, fichier_identite = ? WHERE id = ?"
                    );
                    update.setString(1, dateNaissance);
                    update.setString(2, mdp);
                    update.setString(3, nomImageFinale);
                    update.setString(4, secu);
                    update.setString(5, nationalite);
                    update.setString(6, fichierIdentite);
                    update.setInt(7, personneId);
                    update.executeUpdate();

                    // Récupération de l’arbre associé à la personne
                    PreparedStatement arbreReq = conn.prepareStatement("SELECT id_arbre FROM personne WHERE id = ?");
                    arbreReq.setInt(1, personneId);
                    ResultSet arbreRs = arbreReq.executeQuery();
                    if (arbreRs.next()) arbreId = arbreRs.getInt("id_arbre");

                } else {
                    // Création d’un nouvel arbre privé
                    PreparedStatement insertArbre = conn.prepareStatement(
                            "INSERT INTO arbre (visibilite) VALUES ('prive')", Statement.RETURN_GENERATED_KEYS);
                    insertArbre.executeUpdate();
                    ResultSet arbreKeys = insertArbre.getGeneratedKeys();
                    if (arbreKeys.next()) arbreId = arbreKeys.getInt(1);

                    // Insertion de la personne
                    PreparedStatement insertPersonne = conn.prepareStatement(
                            "INSERT INTO personne (nom, prenom, date_naissance, mot_de_passe, photo, inscrit, id_arbre, securite_sociale, nationalite, fichier_identite) VALUES (?, ?, ?, ?, ?, TRUE, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS);
                    insertPersonne.setString(1, nom);
                    insertPersonne.setString(2, prenom);
                    insertPersonne.setString(3, dateNaissance);
                    insertPersonne.setString(4, mdp);
                    insertPersonne.setString(5, nomImageFinale);
                    insertPersonne.setInt(6, arbreId);
                    insertPersonne.setString(7, secu);
                    insertPersonne.setString(8, nationalite);
                    insertPersonne.setString(9, fichierIdentite);
                    insertPersonne.executeUpdate();

                    ResultSet newPersonneKeys = insertPersonne.getGeneratedKeys();
                    if (newPersonneKeys.next()) personneId = newPersonneKeys.getInt(1);
                }

                // Création du compte utilisateur associé
                PreparedStatement insertUser = conn.prepareStatement(
                        "INSERT INTO utilisateur (email, mot_de_passe, nom, prenom, id_personne, statut, code_public, code_prive) VALUES (?, ?, ?, ?, ?, 'en_attente', NULL, NULL)",
                        Statement.RETURN_GENERATED_KEYS);
                insertUser.setString(1, email);
                insertUser.setString(2, prenom); // mot de passe initial = prénom
                insertUser.setString(3, nom);
                insertUser.setString(4, prenom);
                insertUser.setInt(5, personneId);
                insertUser.executeUpdate();

                ResultSet userKeys = insertUser.getGeneratedKeys();
                if (userKeys.next()) utilisateurId = userKeys.getInt(1);

                // Mise à jour de l’arbre avec le bon id_utilisateur
                PreparedStatement majArbre = conn.prepareStatement(
                        "UPDATE arbre SET id_utilisateur = ? WHERE id = ?");
                majArbre.setInt(1, utilisateurId);
                majArbre.setInt(2, arbreId);
                majArbre.executeUpdate();

                conn.commit(); // valide la transaction complète

                Session.setUserId(personneId); // session utilisateur
                showAlert(Alert.AlertType.INFORMATION, "Inscription réussie !");
                stage.close();

            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur SQL : " + ex.getMessage());
            }
        });

        // Placement des composants dans la grille
        grid.add(nomLabel, 0, 0);     grid.add(nomField, 1, 0);
        grid.add(prenomLabel, 0, 1);  grid.add(prenomField, 1, 1);
        grid.add(emailLabel, 0, 2);   grid.add(emailField, 1, 2);
        grid.add(mdpLabel, 0, 3);     grid.add(mdpField, 1, 3);
        grid.add(dateLabel, 0, 4);    grid.add(dateField, 1, 4);
        grid.add(photoButton, 0, 5);  grid.add(photoNameLabel, 1, 5);
        grid.add(secuLabel, 0, 6);    grid.add(secuField, 1, 6);
        grid.add(nationaliteLabel, 0, 7); grid.add(nationaliteField, 1, 7);
        grid.add(idLabel, 0, 8);      grid.add(idButton, 1, 8);
        grid.add(idFileNameLabel, 1, 9);
        grid.add(validerButton, 1, 10);

        Scene scene = new Scene(grid, 600, 550);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Méthode pour afficher des alertes (erreur ou confirmation).
     */
    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
