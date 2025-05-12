// Déclaration du package dans lequel se trouve cette classe
package com.example.projet_ing1;

// Import des classes nécessaires de JavaFX
import javafx.application.Application; // Classe de base pour toute application JavaFX
import javafx.scene.Scene; // Représente la "scène" contenant tout le contenu affiché
import javafx.scene.control.*; // Contient les éléments graphiques comme Label, TextField, Button, etc.
import javafx.scene.layout.*; // Contient les layouts comme GridPane, VBox, etc.
import javafx.stage.Stage; // Représente la fenêtre principale
import javafx.geometry.*; // Pour la gestion de l’alignement, des marges, etc.
import javafx.scene.image.Image; // Pour charger une image (fond d'écran par exemple)

public class LoginApp extends Application { // Notre classe principale hérite de Application

    // Méthode principale de JavaFX qui sera appelée au lancement de l'application
    @Override
    public void start(Stage primaryStage) {
        // Titre de la fenêtre
        primaryStage.setTitle("Connexion - Arbre Généalogique Pro++");

        // Création du champ "Code privé"
        Label codeLabel = new Label("Code privé :");
        TextField codeField = new TextField(); // champ de texte classique

        // Création du champ "Mot de passe"
        Label passwordLabel = new Label("Mot de passe :");
        PasswordField passwordField = new PasswordField(); // champ masqué pour les mots de passe

        // Boutons de connexion et d'inscription
        Button loginButton = new Button("Se connecter");
        Button registerButton = new Button("S'inscrire");

        // Style CSS appliqué directement aux boutons
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px;");
        registerButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px;");

        // Action du bouton "Se connecter"
        loginButton.setOnAction(e -> {
            // Récupération des valeurs saisies
            String code = codeField.getText();
            String password = passwordField.getText();

            // Vérifie si le code et le mot de passe sont corrects
            if (code.equals("12345") && password.equals("prenom")) {
                // Affiche une alerte de succès
                showAlert(Alert.AlertType.INFORMATION, "Connexion réussie !");
            } else {
                // Affiche une alerte d'erreur
                showAlert(Alert.AlertType.ERROR, "Code privé ou mot de passe incorrect.");
            }
        });

        // Action du bouton "S'inscrire"
        registerButton.setOnAction(e -> {
            // Juste un message pour le moment
            showAlert(Alert.AlertType.INFORMATION, "Page d'inscription à implémenter.");
        });

        // Mise en page avec une grille
        GridPane grid = new GridPane(); // Grille de placement des éléments
        grid.setVgap(15); // Espace vertical entre les lignes
        grid.setHgap(10); // Espace horizontal entre les colonnes
        grid.setPadding(new Insets(20)); // Marge intérieure
        grid.setAlignment(Pos.CENTER); // Centre le contenu de la grille dans la fenêtre

        // Titre affiché en haut de la fenêtre
        Label titleLabel = new Label("Bienvenue sur Arbre Généalogique Pro++");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        grid.add(titleLabel, 0, 0, 2, 1); // colonne 0, ligne 0, largeur 2 colonnes, hauteur 1 ligne

        // Ajout des champs dans la grille
        grid.add(codeLabel, 0, 1);
        grid.add(codeField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(loginButton, 1, 3);
        grid.add(registerButton, 1, 4);

        // --- IMAGE DE FOND ---

        // Chargement de l'image (doit être placée dans src/main/resources/images/wallpaper.jpg)
        Image backgroundImage = new Image(getClass().getResource("/images/wallpaper.jpg").toExternalForm());

        // Taille de fond : largeur et hauteur à 100% de la scène
        BackgroundSize backgroundSize = new BackgroundSize(
                1.0, 1.0, true, true, false, false); // largeur 100%, hauteur 100%, valeurs relatives

        // Création d'un objet BackgroundImage avec l'image chargée
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT, // Ne pas répéter horizontalement
                BackgroundRepeat.NO_REPEAT, // Ne pas répéter verticalement
                BackgroundPosition.CENTER, // Centre l’image
                backgroundSize); // Taille configurée juste au-dessus

        // Appliquer ce fond à la grille
        grid.setBackground(new Background(background));

        // Création de la scène (zone visible à l'écran)
        Scene scene = new Scene(grid, 600, 400); // largeur 600px, hauteur 400px

        // Afficher la scène dans la fenêtre
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Méthode utilitaire pour afficher une boîte de dialogue (alerte)
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type); // Crée une alerte de type (INFORMATION, ERROR, etc.)
        alert.setTitle("Information"); // Titre de la boîte
        alert.setHeaderText(null); // Pas de titre secondaire
        alert.setContentText(message); // Le message affiché
        alert.showAndWait(); // Affiche la boîte et attend que l'utilisateur la ferme
    }

    // Méthode main standard en Java : point d'entrée du programme
    public static void main(String[] args) {
        launch(args); // Lance l’application JavaFX
    }
}
