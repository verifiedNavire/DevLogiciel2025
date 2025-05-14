package com.example.projet_ing1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Profil extends Application {

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // Titre
        Label titre = new Label("Mon Profil");
        titre.setFont(new Font("Arial", 24));

        // Exemple de champ (à modifier plus tard)
        Label infoPlaceholder = new Label("Ici s'afficheront les informations du profil (nom, prénom, mail...)");

        root.getChildren().addAll(titre, infoPlaceholder);

        Scene scene = new Scene(root, 500, 300);
        stage.setTitle("Mon Profil");
        stage.setScene(scene);
        stage.show();
    }
}
