package com.example.projet_ing1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Interface de gestion du profil utilisateur (nom, prénom, photo).
 * Permet la visualisation et la mise à jour de ses informations personnelles.
 */
public class Profil extends Application {

    // Répertoire local où seront stockées les photos de profil
    private static final String PROFIL_DIR = "src/main/resources/images/profils/";

    @Override
    public void start(Stage stage) {
        // Récupération de l’ID utilisateur via la session
        int userId = Session.getUserId();
        if (userId == -1) {
            alert("Non connecté", "Veuillez vous connecter.");
            return;
        }

        // Champs du formulaire
        TextField nomField = new TextField();
        TextField prenomField = new TextField();
        TextField photoField = new TextField(); // contient uniquement le nom du fichier
        ImageView imageView = new ImageView(); // affiche la photo actuelle

        // Chargement des informations de l’utilisateur connecté
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM personne WHERE id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                nomField.setText(rs.getString("nom"));
                prenomField.setText(rs.getString("prenom"));
                photoField.setText(rs.getString("photo"));

                // Si une photo existe, on l’affiche dans l’imageView
                String photo = rs.getString("photo");
                if (photo != null && !photo.isEmpty()) {
                    try {
                        Image img = new Image(getClass().getResource("/images/profils/" + photo).toExternalForm(), 100, 100, true, true);
                        imageView.setImage(img);
                    } catch (Exception e) {
                        e.printStackTrace(); // gestion d’image invalide
                    }
                }
            }
        } catch (Exception e) {
            alert("Erreur", "Impossible de charger le profil.");
            e.printStackTrace();
            return;
        }

        // Bouton pour choisir une nouvelle photo
        Button choisirPhotoBtn = new Button("Choisir une photo");
        choisirPhotoBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
            );

            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                try {
                    String nomFichier = selectedFile.getName();
                    File dest = new File(PROFIL_DIR + nomFichier);

                    // Copie réelle du fichier dans le dossier ressources
                    Files.copy(selectedFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Mise à jour des champs UI
                    photoField.setText(nomFichier);
                    Image img = new Image(dest.toURI().toString(), 100, 100, true, true);
                    imageView.setImage(img);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    alert("Erreur", "Impossible de copier l'image.");
                }
            }
        });

        // Bouton pour sauvegarder les modifications dans la base
        Button enregistrer = new Button("Enregistrer");
        enregistrer.setOnAction(e -> {
            try (Connection conn = Database.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("UPDATE personne SET nom = ?, prenom = ?, photo = ? WHERE id = ?");
                ps.setString(1, nomField.getText());
                ps.setString(2, prenomField.getText());
                ps.setString(3, photoField.getText());
                ps.setInt(4, userId);
                ps.executeUpdate();

                alert("Succès", "Profil mis à jour !");
                stage.close();

            } catch (Exception ex) {
                ex.printStackTrace();
                alert("Erreur", "Échec de la mise à jour.");
            }
        });

        // Bouton pour fermer la fenêtre
        Button retourBtn = new Button("Retour");
        retourBtn.setOnAction(e -> stage.close());

        // Organisation des boutons horizontalement
        HBox boutons = new HBox(10, enregistrer, retourBtn);
        boutons.setAlignment(Pos.CENTER);

        // Layout principal de la fenêtre
        VBox layout = new VBox(10,
                new Label("Nom :"), nomField,
                new Label("Prénom :"), prenomField,
                new Label("Nom du fichier photo :"), photoField,
                choisirPhotoBtn,
                imageView,
                boutons);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        // Affichage de la scène
        Scene scene = new Scene(layout, 400, 500);
        stage.setTitle("Mon Profil");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Affiche une alerte JavaFX simple.
     */
    private void alert(String titre, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
