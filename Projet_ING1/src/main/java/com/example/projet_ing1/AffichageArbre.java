package com.example.projet_ing1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class AffichageArbre extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Titre
        Label title = new Label("Arbre Généalogique Pro++");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        // Conteneur à gauche pour le titre
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS); // prend tout l'espace à gauche

        // Boutons
        Button monArbreButton = createModernButton("Mon arbre");
        Button ajouterPersonneButton = createModernButton("Ajouter un proche");
        Button arbresToutLeMondeButton = createModernButton("Voir les différents arbres");
        Button deconnecterButton = createModernButton("Se Déconnecter");

        HBox buttonBox = new HBox(10, monArbreButton, ajouterPersonneButton, arbresToutLeMondeButton, deconnecterButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Barre de navigation complète
        HBox navbar = new HBox(30, titleBox, buttonBox);
        navbar.setPadding(new Insets(15));
        navbar.setStyle("-fx-background-color: #333;");

        // Layout principal
        VBox root = new VBox(navbar);
        root.setStyle("-fx-background-color: #f0f0f0;");

        // Scène
        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("Affichage Arbre Généalogique");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Méthode pour créer un bouton avec vrai relief
    private Button createModernButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setTextFill(Color.BLACK);
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #bbb;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(3);
        shadow.setOffsetY(3);
        shadow.setRadius(6);
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));
        button.setEffect(shadow);

        // Effet au survol pour renforcer l'interaction
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: #eaeaea;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #888;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        ));

        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #bbb;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        ));

        return button;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
