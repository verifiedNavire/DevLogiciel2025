package com.example.projet_ing1;

// Imports JavaFX pour l'affichage
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

/**
 * Fenêtre JavaFX permettant à un utilisateur de supprimer une personne de sa famille dans l’arbre généalogique.
 * Seules les personnes liées à l’arbre de l'utilisateur sont affichées.
 */
public class SupprimerPersonne extends Application {

    // Map des personnes liées à l'utilisateur (famille étendue)
    private Map<Integer, Personne> famille;

    @Override
    public void start(Stage stage) {
        // Récupération de l'ID utilisateur actuellement connecté
        int userId = Session.getUserId();

        // Chargement de la famille de l'utilisateur à partir de la base
        ArbreDAO dao = new ArbreDAO();
        dao.chargerFamillePourUtilisateur(userId);
        famille = dao.personnes;

        // Tri alphabétique des personnes (par nom puis prénom)
        List<Personne> personnesTriees = new ArrayList<>(famille.values());
        personnesTriees.sort(Comparator.comparing(Personne::getNom).thenComparing(Personne::getPrenom));

        // Menu déroulant pour choisir la personne à supprimer
        ComboBox<Personne> personneCombo = new ComboBox<>();
        personneCombo.getItems().addAll(personnesTriees);

        // Label pour afficher les messages de retour
        Label message = new Label();

        // Bouton de suppression désactivé par défaut
        Button supprimerBtn = new Button("Supprimer");
        supprimerBtn.setDisable(true);

        // Activation du bouton si une personne est sélectionnée
        personneCombo.setOnAction(e -> {
            Personne selected = personneCombo.getValue();
            supprimerBtn.setDisable(selected == null);
        });

        // Action à effectuer lors du clic sur "Supprimer"
        supprimerBtn.setOnAction(e -> {
            Personne selected = personneCombo.getValue();
            if (selected == null) {
                message.setText("Veuillez sélectionner une personne à supprimer.");
                return;
            }

            // Empêche de supprimer soi-même
            if (selected.getId() == userId) {
                message.setText("Impossible de supprimer votre propre compte.");
                return;
            }

            // Confirmation utilisateur avant suppression
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer " + selected.getPrenom() + " " + selected.getNom());
            confirm.setContentText("Êtes-vous sûr ? Cette action est irréversible.");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try (Connection conn = Database.getConnection()) {

                    // Suppression des relations parentales
                    PreparedStatement deleteLiens = conn.prepareStatement(
                            "DELETE FROM lien_parent WHERE id_parent = ? OR id_enfant = ?");
                    deleteLiens.setInt(1, selected.getId());
                    deleteLiens.setInt(2, selected.getId());
                    deleteLiens.executeUpdate();

                    // Suppression de la personne dans la table `personne`
                    PreparedStatement deletePersonne = conn.prepareStatement(
                            "DELETE FROM personne WHERE id = ?");
                    deletePersonne.setInt(1, selected.getId());
                    int rowsDeleted = deletePersonne.executeUpdate();

                    if (rowsDeleted > 0) {
                        // Mise à jour de la liste locale et interface si succès
                        message.setText("Personne supprimée avec succès.");
                        famille.remove(selected.getId());
                        personneCombo.getItems().remove(selected);
                        personneCombo.setValue(null);
                        supprimerBtn.setDisable(true);

                        // Mise à jour des niveaux dans l’arbre
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

        // Mise en page de l'interface graphique
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
