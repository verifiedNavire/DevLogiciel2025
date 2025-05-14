package com.example.projet_ing1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.*;

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
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Boutons
        Button monArbreButton = createModernButton("Mon arbre");
        Button ajouterPersonneButton = createModernButton("Ajouter un proche");
        Button arbresToutLeMondeButton = createModernButton("Voir les différents arbres");
        Button profilButton = createModernButton("Mon profil");
        Button deconnecterButton = createModernButton("Se Déconnecter");

        profilButton.setOnAction(e -> {
            try {
                new Profil().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox buttonBox = new HBox(10, monArbreButton, ajouterPersonneButton, arbresToutLeMondeButton, profilButton, deconnecterButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Navigation
        HBox navbar = new HBox(30, titleBox, buttonBox);
        navbar.setPadding(new Insets(15));
        navbar.setStyle("-fx-background-color: #333;");

        // Zone d'affichage de l'arbre (Pane pour placer librement)
        Pane arbrePane = new Pane();
        arbrePane.setPrefHeight(500);

        // Charge l’arbre depuis la base
        afficherArbreDepuisBdd(arbrePane);

        // Layout principal
        VBox root = new VBox(navbar, arbrePane);
        root.setStyle("-fx-background-color: #f0f0f0;");
        VBox.setVgrow(arbrePane, Priority.ALWAYS);

        // Scène
        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("Affichage Arbre Généalogique");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void afficherArbreDepuisBdd(Pane pane) {
        ArbreDAO dao = new ArbreDAO();
        dao.chargerDepuisBase();

        double centerX = 500;  // point central horizontal
        double startY = 100;   // hauteur de départ
        double spacingY = 120; // espace vertical entre générations
        double spacingX = 150; // espace horizontal entre couples

        Map<Integer, Label> noeuds = new HashMap<>();
        Set<Integer> affiches = new HashSet<>();

        double x = centerX;

        for (Map.Entry<Integer, List<Integer>> entry : dao.relations.entrySet()) {
            int parentId = entry.getKey();
            List<Integer> enfantsIds = entry.getValue();

            Personne parent = dao.personnes.get(parentId);
            if (parent == null || affiches.contains(parentId)) continue;

            // Chercher s’il a un enfant avec un autre parent connu
            for (int enfantId : enfantsIds) {
                // chercher l'autre parent de l’enfant
                int autreParentId = dao.getAutreParent(enfantId, parentId);
                Personne autreParent = dao.personnes.get(autreParentId);

                // Afficher les parents côte à côte
                Label labelP1 = creerLabel(parent);
                Label labelP2 = autreParent != null ? creerLabel(autreParent) : null;

                double px1 = x - 60;
                double px2 = x + 60;

                labelP1.setLayoutX(px1);
                labelP1.setLayoutY(startY);
                pane.getChildren().add(labelP1);
                noeuds.put(parentId, labelP1);
                affiches.add(parentId);

                if (autreParent != null) {
                    labelP2.setLayoutX(px2);
                    labelP2.setLayoutY(startY);
                    pane.getChildren().add(labelP2);
                    noeuds.put(autreParentId, labelP2);
                    affiches.add(autreParentId);
                }

                // Enfants au centre
                Personne enfant = dao.personnes.get(enfantId);
                if (enfant != null && !affiches.contains(enfantId)) {
                    Label enfantLabel = creerLabel(enfant);
                    enfantLabel.setLayoutX(x - 50);
                    enfantLabel.setLayoutY(startY + spacingY);
                    pane.getChildren().add(enfantLabel);
                    noeuds.put(enfantId, enfantLabel);
                    affiches.add(enfantId);

                    // Lignes vers enfant
                    Line l1 = new Line(
                            px1 + 50, startY + 30,
                            enfantLabel.getLayoutX() + 50, enfantLabel.getLayoutY()
                    );
                    pane.getChildren().add(l1);

                    if (labelP2 != null) {
                        Line l2 = new Line(
                                px2 + 50, startY + 30,
                                enfantLabel.getLayoutX() + 50, enfantLabel.getLayoutY()
                        );
                        pane.getChildren().add(l2);
                    }
                }

                x += spacingX; // décale la prochaine famille
            }
        }
    }


    private Label creerLabel(Personne p) {
        Label label = new Label(p.getNomComplet());
        label.setPrefSize(100, 30);
        label.setStyle("-fx-border-color: black; -fx-alignment: center;");
        label.setTextFill(p.isInscrit() ? Color.GREEN : Color.RED);
        return label;
    }

    private Button createModernButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setTextFill(Color.BLACK);
        button.setStyle(
                "-fx-background-color: linear-gradient(#f9f9f9, #dcdcdc);" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #999;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 2, 2);"
        );

        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: linear-gradient(#ffffff, #e0e0e0);" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #777;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 7, 0.2, 2, 2);"
        ));

        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: linear-gradient(#f9f9f9, #dcdcdc);" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #999;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 2, 2);"
        ));

        return button;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
