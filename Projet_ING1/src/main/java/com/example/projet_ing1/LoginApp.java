package com.example.projet_ing1;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

/**
 * Interface de connexion principale de l'application Arbre Généalogique Pro++.
 * Permet à un utilisateur de se connecter, de s'inscrire ou d'accéder à l'annuaire des utilisateurs inscrits.
 */
public class LoginApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Connexion - Arbre Généalogique Pro++");

        // === Création des champs du formulaire ===
        Label codeLabel = new Label("Code privé :");
        TextField codeField = new TextField();

        Label passwordLabel = new Label("Mot de passe :");
        PasswordField passwordField = new PasswordField();

        // Style des libellés
        codeLabel.setStyle("-fx-text-fill: white;-fx-font-weight: bold;-fx-font-size: 14px");
        passwordLabel.setStyle("-fx-text-fill: white;-fx-font-weight: bold;-fx-font-size: 14px");

        // === Boutons de navigation ===
        Button loginButton = new Button("Se connecter");
        Button registerButton = new Button("S'inscrire");
        Button annuaireButton = new Button("Voir annuaire");

        // Style personnalisé pour les boutons
        loginButton.setStyle("-fx-background-color: #dac290; -fx-text-fill: white; -fx-font-weight: bold;");
        registerButton.setStyle("-fx-background-color: #775b21; -fx-text-fill: white; -fx-font-weight: bold;");
        annuaireButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-weight: bold;");

        // === Action lors de la tentative de connexion ===
        loginButton.setOnAction(e -> {
            String codePrive = codeField.getText().trim();
            String password = passwordField.getText().trim();

            if (!checkLogin(codePrive, password)) {
                showAlert(Alert.AlertType.ERROR, "Code ou mot de passe incorrect.");
            }
        });

        // === Lien vers l’inscription ===
        registerButton.setOnAction(e -> {
            try {
                new Inscription().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // === Affichage de l'annuaire des personnes inscrites ===
        annuaireButton.setOnAction(e -> {
            try (Connection conn = Database.getConnection()) {
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

        // === Mise en page de la grille ===
        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        // Titre centré en haut
        Label titleLabel = new Label("Bienvenue sur Arbre Généalogique Pro++");
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");
        grid.add(titleLabel, 0, 0, 2, 1); // colonne 0 à 1 fusionnées

        // Placement des champs dans la grille
        grid.add(codeLabel, 0, 1);
        grid.add(codeField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(loginButton, 1, 3);
        grid.add(registerButton, 1, 4);
        grid.add(annuaireButton, 1, 5);

        // === Application d'une image de fond ===
        Image backgroundImage = new Image(getClass().getResource("/images/wallpaper.jpg").toExternalForm());
        BackgroundSize backgroundSize = new BackgroundSize(1.0, 1.0, true, true, false, false);
        BackgroundImage background = new BackgroundImage(
                backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, backgroundSize);
        grid.setBackground(new Background(background));

        // === Création de la scène ===
        Scene scene = new Scene(grid, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Vérifie les informations d’identification saisies dans la base de données.
     * Si la connexion est correcte, redirige l’utilisateur en fonction de son rôle.
     * @return false si aucun utilisateur n’a été trouvé, true sinon
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

                // Enregistre l’identifiant utilisateur et le rôle dans la session
                Session.setUserId(rs.getInt("id_personne"));
                Session.setUserRole(rs.getString("role"));

                // Si le mot de passe est encore le prénom, on force le changement
                if (motDePasse.equalsIgnoreCase(prenom)) {
                    new ChangerMdp().start(new Stage());
                } else if ("admin".equals(Session.getUserRole())) {
                    new AccueilAdmin().start(new Stage());
                } else {
                    new AffichageArbre().start(new Stage());
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Affiche une boîte de dialogue avec un message personnalisé.
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
