package com.example.projet_ing1;

// Import des classes JavaFX nécessaires pour l'interface graphique
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

/**
 * Classe représentant l'interface d'accueil de l'administrateur.
 * Elle permet de gérer les adhésions, de visualiser tous les arbres, et de se déconnecter.
 */
public class AccueilAdmin extends Application {

    @Override
    public void start(Stage stage) {
        // Définition du titre de la fenêtre
        stage.setTitle("Espace Administrateur - Arbre Généalogique Pro++");

        // Création du titre affiché en haut de la fenêtre
        Label title = new Label("Espace Administrateur - Arbre Généalogique Pro++");
        title.setTextFill(Color.WHITE); // Couleur du texte en blanc
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20)); // Police et taille

        // Conteneur horizontal pour le titre
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER_LEFT); // Alignement à gauche
        HBox.setHgrow(titleBox, Priority.ALWAYS); // Prend toute la largeur disponible

        // Barre de navigation en haut de l'interface
        HBox navbar = new HBox(30, titleBox); // Espacement de 30px
        navbar.setPadding(new Insets(15)); // Marge interne
        navbar.setStyle("-fx-background-color: #333;"); // Fond gris foncé

        // Création des boutons avec un style homogène
        Button btnValidation = createModernButton("Gérer les adhésions");
        Button btnVoirArbres = createModernButton("Voir tous les arbres");
        Button btnDeconnexion = createModernButton("Se déconnecter");

        // Action du bouton "Gérer les adhésions" : ouverture de la fenêtre ValidationAdmin
        btnValidation.setOnAction(e -> {
            try {
                new ValidationAdmin().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace(); // Affiche l'erreur dans la console si échec
            }
        });

        // Action du bouton "Voir tous les arbres" : ouverture de la fenêtre d'affichage global
        btnVoirArbres.setOnAction(e -> {
            try {
                new AffichageTousArbresAdmin().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Action du bouton "Se déconnecter" : efface la session et retourne à la page de login
        btnDeconnexion.setOnAction(e -> {
            Session.clear(); // Réinitialisation de la session
            stage.close(); // Fermeture de la fenêtre actuelle
            try {
                new LoginApp().start(new Stage()); // Retour à l'écran de connexion
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Conteneur vertical contenant les boutons centrés verticalement
        VBox content = new VBox(20, btnValidation, btnVoirArbres, btnDeconnexion);
        content.setAlignment(Pos.CENTER); // Centrage du contenu
        content.setPadding(new Insets(40)); // Marges internes

        // Conteneur principal de la fenêtre
        VBox root = new VBox(navbar, content);
        root.setStyle("-fx-background-color: #f0f0f0;"); // Fond gris clair
        VBox.setVgrow(content, Priority.ALWAYS); // Le contenu prend l'espace vertical restant

        // Création de la scène et affichage de la fenêtre
        Scene scene = new Scene(root, 600, 350);
        stage.setScene(scene);
        stage.show();
    }

     // Méthode  pour créer un bouton stylisé avec des paramètres uniformes.
    private Button createModernButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14)); // Police et taille
        button.setTextFill(Color.BLACK); // Couleur du texte
        button.setStyle("""
            -fx-background-color: linear-gradient(#f9f9f9, #dcdcdc);
            -fx-background-radius: 10;
            -fx-border-color: #999;
            -fx-border-width: 1;
            -fx-border-radius: 10;
            -fx-padding: 10 20;
        """); // Style CSS personnalisé
        return button;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
