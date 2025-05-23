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

public class ChangerMdp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Changement de mot de passe");

        PasswordField nouveauMdpField = new PasswordField();
        PasswordField confirmationField = new PasswordField();

        Button validerButton = new Button("Valider");

        validerButton.setOnAction(e -> {
            String nouveauMdp = nouveauMdpField.getText().trim();
            String confirmation = confirmationField.getText().trim();

            if (nouveauMdp.isEmpty() || confirmation.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Veuillez remplir les deux champs.");
                return;
            }

            if (!nouveauMdp.equals(confirmation)) {
                showAlert(Alert.AlertType.ERROR, "Les mots de passe ne correspondent pas.");
                return;
            }

            try (Connection conn = Database.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE utilisateur SET mot_de_passe = ? WHERE id_personne = ?"
                );
                ps.setString(1, nouveauMdp);
                ps.setInt(2, Session.getUserId());
                ps.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Mot de passe mis à jour !");
                stage.close();
                new AffichageArbre().start(new Stage());

            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur lors de la mise à jour.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        grid.add(new Label("Nouveau mot de passe :"), 0, 0);
        grid.add(nouveauMdpField, 1, 0);
        grid.add(new Label("Confirmer le mot de passe :"), 0, 1);
        grid.add(confirmationField, 1, 1);
        grid.add(validerButton, 1, 2);

        Scene scene = new Scene(grid, 400, 200);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}