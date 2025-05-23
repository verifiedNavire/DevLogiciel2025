package com.example.projet_ing1;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class LoginApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Connexion - Arbre Généalogique Pro++");

        // Champs et labels pour le formulaire de connexion
        Label codeLabel = new Label("Code privé :");
        TextField codeField = new TextField();


        Label passwordLabel = new Label("Mot de passe :");
        PasswordField passwordField = new PasswordField();

        // Style des labels
        codeLabel.setStyle("-fx-text-fill: white;-fx-font-weight: bold;-fx-font-size: 14px");
        passwordLabel.setStyle("-fx-text-fill: white;-fx-font-weight: bold;-fx-font-size: 14px");

        // Boutons principaux
        Button loginButton = new Button("Se connecter");
        Button registerButton = new Button("S'inscrire");
        Button annuaireButton = new Button("Voir annuaire");

        // Styles des boutons
        loginButton.setStyle("-fx-background-color: #dac290; -fx-text-fill: white; -fx-font-weight: bold;");
        registerButton.setStyle("-fx-background-color: #775b21; -fx-text-fill: white; -fx-font-weight: bold;");
        annuaireButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-weight: bold;");

        // --- Action bouton Connexion ---
        loginButton.setOnAction(e -> {
            String codePrive = codeField.getText().trim();
            String password = passwordField.getText().trim();

            if (!checkLogin(codePrive, password)) {
                showAlert(Alert.AlertType.ERROR, "Code ou mot de passe incorrect.");
            }

        });

        // --- Action bouton Inscription ---
        registerButton.setOnAction(e -> {
            try {
                new Inscription().start(new Stage()); // Lance l’inscription
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // --- Action bouton Annuaire ---
        annuaireButton.setOnAction(e -> {
            try (Connection conn = Database.getConnection()) {
                // On récupère toutes les personnes inscrites
                PreparedStatement ps = conn.prepareStatement("SELECT nom, prenom FROM personne WHERE inscrit = TRUE");
                ResultSet rs = ps.executeQuery();

                StringBuilder sb = new StringBuilder("Personnes inscrites :\n");
                while (rs.next()) {
                    sb.append("- ").append(rs.getString("prenom")).append(" ").append(rs.getString("nom")).append("\n");
                }
                showAlert(Alert.AlertType.INFORMATION, sb.toString());
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur lors de la récupération de l'annuaire.");
                ex.printStackTrace();
            }
        });

        // --- Layout graphique principal ---
        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        // Titre de l'application
        Label titleLabel = new Label("Bienvenue sur Arbre Généalogique Pro++");
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");
        grid.add(titleLabel, 0, 0, 2, 1);

        // Placement des composants dans la grille
        grid.add(codeLabel, 0, 1);
        grid.add(codeField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(loginButton, 1, 3);
        grid.add(registerButton, 1, 4);
        grid.add(annuaireButton, 1, 5);

        // Fond d’écran
        Image backgroundImage = new Image(getClass().getResource("/images/wallpaper.jpg").toExternalForm());
        BackgroundSize backgroundSize = new BackgroundSize(1.0, 1.0, true, true, false, false);
        BackgroundImage background = new BackgroundImage(backgroundImage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, backgroundSize);
        grid.setBackground(new Background(background));

        // Création de la scène
        Scene scene = new Scene(grid, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Vérifie les identifiants de connexion dans la base de données.
     */
    private boolean checkLogin(String codePrive, String password) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_personne, role, prenom, mot_de_passe FROM utilisateur WHERE code_prive = ? AND mot_de_passe = ?"
            );
            ps.setString(1, codePrive);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String prenom = rs.getString("prenom");
                String motDePasse = rs.getString("mot_de_passe");

                Session.setUserId(rs.getInt("id_personne"));
                Session.setUserRole(rs.getString("role"));

                if (motDePasse.equalsIgnoreCase(prenom)) {
                    new ChangerMdp().start(new Stage());
                } else if ("admin".equals(Session.getUserRole())) {
                    new AccueilAdmin().start(new Stage());
                } else {
                    new AffichageArbre().start(new Stage());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Affiche une alerte à l'utilisateur.
     */
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}