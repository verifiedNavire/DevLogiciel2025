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

/**
 * Interface réservée aux administrateurs pour gérer les demandes d’inscription en attente.
 * Les utilisateurs apparaissent dans un tableau avec deux options : Valider (avec envoi de mail) ou Refuser.
 */
public class ValidationAdmin extends Application {

    // Table JavaFX pour afficher les utilisateurs en attente
    private TableView<UtilisateurEnAttente> table = new TableView<>();

    // Liste observable qui sera affichée dans le tableau
    private ObservableList<UtilisateurEnAttente> data = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Validation des adhésions");

        // --- Colonnes du tableau ---
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

        // Ajout des colonnes à la table
        table.getColumns().addAll(nomCol, prenomCol, emailCol, validerCol, refuserCol);
        table.setItems(data); // liaison avec la liste observable

        // Charge les utilisateurs en attente depuis la base
        chargerUtilisateurs();

        // Layout principal avec padding
        VBox vbox = new VBox(10, new Label("Utilisateurs en attente de validation :"), table);
        vbox.setPadding(new Insets(20));

        stage.setScene(new Scene(vbox, 800, 400));
        stage.show();
    }

    /**
     * Charge les utilisateurs dont le statut est "en_attente" depuis la base de données.
     * Pour chacun, on crée les boutons d’action avec leur comportement.
     */
    private void chargerUtilisateurs() {
        data.clear(); // reset de la liste avant de recharger
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

                // Création des boutons avec leurs actions
                Button validerBtn = new Button("✅ Valider");
                Button refuserBtn = new Button("❌ Refuser");

                validerBtn.setOnAction(e -> validerUtilisateur(id, prenom, email));
                refuserBtn.setOnAction(e -> refuserUtilisateur(id));

                // Ajoute une ligne dans la liste observable
                data.add(new UtilisateurEnAttente(id, nom, prenom, email, validerBtn, refuserBtn));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lorsqu’un utilisateur est validé, on génère deux codes (public et privé), on met à jour la BDD
     * et on envoie un e-mail de confirmation.
     */
    private void validerUtilisateur(int userId, String prenom, String email) {
        try (Connection conn = Database.getConnection()) {
            // Génération de deux codes uniques
            String codePublic = "PUB-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            String codePrive = "PRV-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

            // Mise à jour en base
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE utilisateur SET statut = 'valide', code_public = ?, code_prive = ? WHERE id = ?"
            );
            ps.setString(1, codePublic);
            ps.setString(2, codePrive);
            ps.setInt(3, userId);
            ps.executeUpdate();

            // Envoi d’un mail à l’utilisateur avec ses codes
            EnvoiMail.envoyerCodes(email, prenom, codePublic, codePrive);

            showAlert("Succès", "Utilisateur validé et e-mail envoyé !");
            chargerUtilisateurs(); // recharge la liste
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de valider l'utilisateur.");
        }
    }

    /**
     * Refuse un utilisateur (statut mis à "refuse") et recharge la table.
     */
    private void refuserUtilisateur(int userId) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE utilisateur SET statut = 'refuse' WHERE id = ?"
            );
            ps.setInt(1, userId);
            ps.executeUpdate();

            showAlert("Refusé", "L'utilisateur a été refusé.");
            chargerUtilisateurs(); // mise à jour de la vue
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de refuser l'utilisateur.");
        }
    }

    /**
     * Affiche une alerte simple à l'écran avec un message.
     */
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
