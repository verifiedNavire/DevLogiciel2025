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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Profil extends Application {

    private static final String PROFIL_DIR = "src/main/resources/images/profils/";

    @Override
    public void start(Stage stage) {
        int userId = Session.getUserId();
        if (userId == -1) {
            alert("Non connecté", "Veuillez vous connecter.");
            return;
        }

        TextField nomField = new TextField();
        TextField prenomField = new TextField();
        TextField photoField = new TextField();
        ImageView imageView = new ImageView();

        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM personne WHERE id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                nomField.setText(rs.getString("nom"));
                prenomField.setText(rs.getString("prenom"));
                photoField.setText(rs.getString("photo"));

                String photo = rs.getString("photo");
                if (photo != null && !photo.isEmpty()) {
                    try {
                        Image img = new Image(getClass().getResource("/images/profils/" + photo).toExternalForm(), 100, 100, true, true);
                        imageView.setImage(img);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            alert("Erreur", "Impossible de charger le profil.");
            e.printStackTrace();
            return;
        }

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

                    // Copie réelle du fichier dans le dossier du projet
                    Files.copy(selectedFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    photoField.setText(nomFichier);
                    Image img = new Image(dest.toURI().toString(), 100, 100, true, true);
                    imageView.setImage(img);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    alert("Erreur", "Impossible de copier l'image.");
                }
            }
        });

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

        Button retourBtn = new Button("Retour");
        retourBtn.setOnAction(e -> stage.close());

        HBox boutons = new HBox(10, enregistrer, retourBtn);
        boutons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10,
                new Label("Nom :"), nomField,
                new Label("Prénom :"), prenomField,
                new Label("Nom du fichier photo :"), photoField,
                choisirPhotoBtn,
                imageView,
                boutons);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 400, 500);
        stage.setTitle("Mon Profil");
        stage.setScene(scene);
        stage.show();
    }

    private void alert(String titre, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}