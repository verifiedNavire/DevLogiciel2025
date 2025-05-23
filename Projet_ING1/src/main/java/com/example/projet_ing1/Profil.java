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

public class Profil extends Application {

    private static final String PROFIL_DIR = "src/main/resources/images/profils/";

    @Override
    public void start(Stage stage) {
        int userId = Session.getUserId();
        if (userId == -1) {
            alert("Non connecté", "Veuillez vous connecter.");
            return;
        }

        // Champs du formulaire
        TextField nomField = new TextField();
        nomField.setDisable(true);
        TextField prenomField = new TextField();
        prenomField.setDisable(true);

        TextField emailField = new TextField();
        TextField photoField = new TextField();
        photoField.setEditable(false);
        ImageView imageView = new ImageView();

        TextField codePriveField = new TextField();
        codePriveField.setDisable(true);

        TextField nationaliteField = new TextField();
        TextField numSecuField = new TextField();

        DatePicker dateNaissancePicker = new DatePicker();
        dateNaissancePicker.setDisable(true);

        ToggleGroup visibiliteGroup = new ToggleGroup();
        RadioButton privateRadio = new RadioButton("private");
        RadioButton publicRadio = new RadioButton("public");
        privateRadio.setToggleGroup(visibiliteGroup);
        publicRadio.setToggleGroup(visibiliteGroup);

        // Charger les données utilisateur depuis la BDD
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT p.*, u.email, u.code_prive FROM personne p LEFT JOIN utilisateur u ON p.id = u.id_personne WHERE p.id = ?"
            );
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                nomField.setText(rs.getString("nom"));
                prenomField.setText(rs.getString("prenom"));
                emailField.setText(rs.getString("email"));
                photoField.setText(rs.getString("photo"));
                codePriveField.setText(rs.getString("code_prive"));
                nationaliteField.setText(rs.getString("nationalite"));
                numSecuField.setText(rs.getString("securite_sociale"));
                if (rs.getDate("date_naissance") != null) {
                    dateNaissancePicker.setValue(rs.getDate("date_naissance").toLocalDate());
                }

                String visibilite = rs.getString("visibilite");
                if ("public".equalsIgnoreCase(visibilite)) {
                    publicRadio.setSelected(true);
                } else {
                    privateRadio.setSelected(true);
                }

                // Afficher la photo si elle existe
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

        // Bouton choisir photo
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

        // Bouton changer mot de passe
        Button changerMdpBtn = new Button("Changer mot de passe");
        changerMdpBtn.setOnAction(e -> {
            try {
                new ChangerMdp().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
                alert("Erreur", "Impossible d'ouvrir la fenêtre de changement de mot de passe.");
            }
        });

        // Bouton enregistrer
        Button enregistrer = new Button("Enregistrer");
        enregistrer.setOnAction(e -> {
            try (Connection conn = Database.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE personne SET photo = ?, visibilite = ?, nationalite = ?, securite_sociale = ? WHERE id = ?"
                );
                ps.setString(1, photoField.getText());
                String visibiliteChoisie = ((RadioButton) visibiliteGroup.getSelectedToggle()).getText().toLowerCase();
                ps.setString(2, visibiliteChoisie);
                ps.setString(3, nationaliteField.getText());
                ps.setString(4, numSecuField.getText());
                ps.setInt(5, userId);
                ps.executeUpdate();

                PreparedStatement psEmail = conn.prepareStatement(
                        "UPDATE utilisateur SET email = ? WHERE id_personne = ?"
                );
                psEmail.setString(1, emailField.getText());
                psEmail.setInt(2, userId);
                psEmail.executeUpdate();

                alert("Succès", "Profil mis à jour !");
                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                alert("Erreur", "Échec de la mise à jour.");
            }
        });

        // Bouton retour
        Button retourBtn = new Button("Retour");
        retourBtn.setOnAction(e -> stage.close());

        HBox boutons = new HBox(10, enregistrer, retourBtn);
        boutons.setAlignment(Pos.CENTER);

        // Layout du formulaire
        VBox formLayout = new VBox(10,
                new Label("Nom :"), nomField,
                new Label("Prénom :"), prenomField,
                new Label("Date de naissance :"), dateNaissancePicker,
                new Label("Code privé :"), codePriveField,
                new Label("Email :"), emailField,
                new Label("Nationalité :"), nationaliteField,
                new Label("Numéro de sécurité sociale :"), numSecuField,
                new Label("Nom du fichier photo :"), photoField,
                choisirPhotoBtn,
                imageView,
                new Label("Visibilité du profil :"),
                new HBox(10, privateRadio, publicRadio),
                changerMdpBtn,
                boutons
        );
        formLayout.setPadding(new Insets(20));
        formLayout.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane(formLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane, 450, 600);
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
