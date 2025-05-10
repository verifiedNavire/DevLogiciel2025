package com.example.projet_ing1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.*;
import javafx.scene.image.Image;

public class LoginApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Connexion - Arbre Généalogique Pro++");

        // Champs de saisie
        Label codeLabel = new Label("Code privé :");
        TextField codeField = new TextField();

        Label passwordLabel = new Label("Mot de passe :");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Se connecter");
        Button registerButton = new Button("S'inscrire");

        // Style des boutons
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px;");
        registerButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px;");

        // Gestion de l'événement de connexion
        loginButton.setOnAction(e -> {
            String code = codeField.getText();
            String password = passwordField.getText();

            if (code.equals("12345") && password.equals("prenom")) {
                showAlert(Alert.AlertType.INFORMATION, "Connexion réussie !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Code privé ou mot de passe incorrect.");
            }
        });

        // Gestion de l'événement d'inscription
        registerButton.setOnAction(e -> {
            showAlert(Alert.AlertType.INFORMATION, "Page d'inscription à implémenter.");
        });

        // Mise en page
        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Bienvenue sur Arbre Généalogique Pro++");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        grid.add(titleLabel, 0, 0, 2, 1);

        grid.add(codeLabel, 0, 1);
        grid.add(codeField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(loginButton, 1, 3);
        grid.add(registerButton, 1, 4);

        // Charger l'image avec un chemin relatif (stockée dans src/main/resources/images/wallpaper.jpg)
        Image backgroundImage = new Image(getClass().getResource("/images/wallpaper.jpg").toExternalForm());

        // Définir une image de fond qui s'étire
        BackgroundSize backgroundSize = new BackgroundSize(
                1.0, 1.0, true, true, false, false); // s'étire à 100% largeur/hauteur
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                backgroundSize);
        grid.setBackground(new Background(background));

        // Créer la scène
        Scene scene = new Scene(grid, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

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
