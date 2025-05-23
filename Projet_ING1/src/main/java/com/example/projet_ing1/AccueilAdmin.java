package com.example.projet_ing1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class AccueilAdmin extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Espace Administrateur - Arbre Généalogique Pro++");

        // Barre de titre
        Label title = new Label("Espace Administrateur - Arbre Généalogique Pro++");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        HBox navbar = new HBox(30, titleBox);
        navbar.setPadding(new Insets(15));
        navbar.setStyle("-fx-background-color: #333;");

        // Boutons centraux
        Button btnValidation = createModernButton("Gérer les adhésions");
        Button btnVoirArbres = createModernButton("Voir tous les arbres");
        Button btnDeconnexion = createModernButton("Se déconnecter");

        btnValidation.setOnAction(e -> {
            try {
                new ValidationAdmin().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btnVoirArbres.setOnAction(e -> {
            try {
                new AffichageTousArbres().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btnDeconnexion.setOnAction(e -> {
            Session.clear();
            stage.close();
            try {
                new LoginApp().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox content = new VBox(20, btnValidation, btnVoirArbres, btnDeconnexion);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));

        VBox root = new VBox(navbar, content);
        root.setStyle("-fx-background-color: #f0f0f0;");
        VBox.setVgrow(content, Priority.ALWAYS);

        Scene scene = new Scene(root, 600, 350);
        stage.setScene(scene);
        stage.show();
    }

    private Button createModernButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setTextFill(Color.BLACK);
        button.setStyle("""
            -fx-background-color: linear-gradient(#f9f9f9, #dcdcdc);
            -fx-background-radius: 10;
            -fx-border-color: #999;
            -fx-border-width: 1;
            -fx-border-radius: 10;
            -fx-padding: 10 20;
        """);
        return button;
    }

    public static void main(String[] args) {
        launch(args);
    }
}