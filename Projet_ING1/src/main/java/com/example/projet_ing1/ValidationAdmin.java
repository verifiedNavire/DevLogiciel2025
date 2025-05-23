package com.example.projet_ing1;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class ValidationAdmin extends Application {

    private TableView<UtilisateurEnAttente> table = new TableView<>();
    private ObservableList<UtilisateurEnAttente> data = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        // creation d un tableau avec la liste des personnes en attente
        stage.setTitle("Validation des adhésions");

        TableColumn<UtilisateurEnAttente, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<UtilisateurEnAttente, String> prenomCol = new TableColumn<>("Prénom");
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        TableColumn<UtilisateurEnAttente, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<UtilisateurEnAttente, Button> validerCol = new TableColumn<>("Valider");
        validerCol.setCellValueFactory(new PropertyValueFactory<>("validerBtn"));

        TableColumn<UtilisateurEnAttente, Button> refuserCol = new TableColumn<>("Refuser");
        refuserCol.setCellValueFactory(new PropertyValueFactory<>("refuserBtn"));

        table.getColumns().addAll(nomCol, prenomCol, emailCol, validerCol, refuserCol);
        table.setItems(data);

        chargerUtilisateurs();

        VBox vbox = new VBox(10, new Label("Utilisateurs en attente de validation :"), table);
        vbox.setPadding(new Insets(20));

        stage.setScene(new Scene(vbox, 800, 400));
        stage.show();
    }

    private void chargerUtilisateurs() {
        data.clear();
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM utilisateur WHERE statut = 'en_attente'"
            );
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String email = rs.getString("email");

                Button validerBtn = new Button("✅ Valider");
                Button refuserBtn = new Button("❌ Refuser");

                validerBtn.setOnAction(e -> validerUtilisateur(id, prenom, email));
                refuserBtn.setOnAction(e -> refuserUtilisateur(id));

                data.add(new UtilisateurEnAttente(id, nom, prenom, email, validerBtn, refuserBtn));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void validerUtilisateur(int userId, String prenom, String email) {
        try (Connection conn = Database.getConnection()) {
            String codePublic = "PUB-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            String codePrive = "PRV-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE utilisateur SET statut = 'valide', code_public = ?, code_prive = ? WHERE id = ?"
            );
            ps.setString(1, codePublic);
            ps.setString(2, codePrive);
            ps.setInt(3, userId);
            ps.executeUpdate();

            // ✉️ Envoi du mail
            EnvoiMail.envoyerCodes(email, prenom, codePublic, codePrive);

            showAlert("Succès", "Utilisateur validé et e-mail envoyé !");
            chargerUtilisateurs();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de valider l'utilisateur.");
        }
    }

    private void refuserUtilisateur(int userId) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE utilisateur SET statut = 'refuse' WHERE id = ?"
            );
            ps.setInt(1, userId);
            ps.executeUpdate();

            showAlert("Refusé", "L'utilisateur a été refusé.");
            chargerUtilisateurs();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de refuser l'utilisateur.");
        }
    }

    private void showAlert(String titre, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}