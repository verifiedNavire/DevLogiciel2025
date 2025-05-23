package com.example.projet_ing1;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Fenêtre permettant à un utilisateur connecté de changer son mot de passe.
 * Comporte deux champs pour le nouveau mot de passe et sa confirmation.
 */
public class ChangerMdp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Changement de mot de passe");

        // Champs de saisie du mot de passe
        PasswordField nouveauMdpField = new PasswordField();
        PasswordField confirmationField = new PasswordField();

        // Bouton de validation du changement
        Button validerButton = new Button("Valider");

        // Action exécutée lors du clic sur le bouton "Valider"
        validerButton.setOnAction(e -> {
            String nouveauMdp = nouveauMdpField.getText().trim();
            String confirmation = confirmationField.getText().trim();

            // Vérification que les deux champs sont remplis
            if (nouveauMdp.isEmpty() || confirmation.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Veuillez remplir les deux champs.");
                return;
            }

            // Vérifie que les deux mots de passe sont identiques
            if (!nouveauMdp.equals(confirmation)) {
                showAlert(Alert.AlertType.ERROR, "Les mots de passe ne correspondent pas.");
                return;
            }

            // Mise à jour dans la base de données
            try (Connection conn = Database.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE utilisateur SET mot_de_passe = ? WHERE id_personne = ?"
                );
                ps.setString(1, nouveauMdp);
                ps.setInt(2, Session.getUserId());
                ps.executeUpdate();

                // Affiche un message de succès et ferme la fenêtre
                showAlert(Alert.AlertType.INFORMATION, "Mot de passe mis à jour !");
                stage.close();

                // Retour à l'affichage de l'arbre principal
                new AffichageArbre().start(new Stage());

            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur lors de la mise à jour.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Mise en page du formulaire avec un GridPane
        GridPane grid = new GridPane();
        grid.setVgap(15); // espacement vertical
        grid.setHgap(10); // espacement horizontal
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        // Ajout des éléments dans la grille
        grid.add(new Label("Nouveau mot de passe :"), 0, 0);
        grid.add(nouveauMdpField, 1, 0);
        grid.add(new Label("Confirmer le mot de passe :"), 0, 1);
        grid.add(confirmationField, 1, 1);
        grid.add(validerButton, 1, 2);

        // Scène finale
        Scene scene = new Scene(grid, 400, 200);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Affiche une alerte standard (erreur ou information) avec un message.
     */
    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
