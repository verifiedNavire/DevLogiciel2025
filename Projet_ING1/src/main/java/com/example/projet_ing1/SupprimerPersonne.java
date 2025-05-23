package com.example.projet_ing1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class SupprimerPersonne extends Application {

    private Map<Integer, Personne> famille;

    @Override
    public void start(Stage stage) {
        int userId = Session.getUserId();
        ArbreDAO dao = new ArbreDAO();
        dao.chargerFamillePourUtilisateur(userId);
        famille = dao.personnes;

        List<Personne> personnesTriees = new ArrayList<>(famille.values());
        personnesTriees.sort(Comparator.comparing(Personne::getNom).thenComparing(Personne::getPrenom));

        ComboBox<Personne> personneCombo = new ComboBox<>();
        personneCombo.getItems().addAll(personnesTriees);

        Label message = new Label();
        Button supprimerBtn = new Button("Supprimer");
        supprimerBtn.setDisable(true);

        personneCombo.setOnAction(e -> {
            Personne selected = personneCombo.getValue();
            supprimerBtn.setDisable(selected == null);
        });

        supprimerBtn.setOnAction(e -> {
            Personne selected = personneCombo.getValue();
            if (selected == null) {
                message.setText("Veuillez sélectionner une personne à supprimer.");
                return;
            }

            if (selected.getId() == userId) {
                message.setText("Impossible de supprimer votre propre compte.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer " + selected.getPrenom() + " " + selected.getNom());
            confirm.setContentText("Êtes-vous sûr ? Cette action est irréversible.");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try (Connection conn = Database.getConnection()) {

                    // Supprimer tous les liens (parent/enfant) liés à cette personne
                    PreparedStatement deleteLiens = conn.prepareStatement(
                            "DELETE FROM lien_parent WHERE id_parent = ? OR id_enfant = ?");
                    deleteLiens.setInt(1, selected.getId());
                    deleteLiens.setInt(2, selected.getId());
                    deleteLiens.executeUpdate();

                    // Supprimer la personne
                    PreparedStatement deletePersonne = conn.prepareStatement(
                            "DELETE FROM personne WHERE id = ?");
                    deletePersonne.setInt(1, selected.getId());
                    int rowsDeleted = deletePersonne.executeUpdate();

                    if (rowsDeleted > 0) {
                        message.setText("Personne supprimée avec succès.");
                        famille.remove(selected.getId());
                        personneCombo.getItems().remove(selected);
                        personneCombo.setValue(null);
                        supprimerBtn.setDisable(true);
                        new ArbreDAO().mettreAJourNiveaux();
                    } else {
                        message.setText("Échec de la suppression.");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    message.setText("Erreur lors de la suppression.");
                }
            }
        });

        VBox root = new VBox(15,
                new Label("Sélectionner une personne à supprimer :"), personneCombo,
                supprimerBtn, message);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, 400, 300));
        stage.setTitle("Supprimer une personne");
        stage.show();
    }
}