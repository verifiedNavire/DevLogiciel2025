package com.example.projet_ing1;

// Imports JavaFX pour les composants graphiques
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.Group;
import javafx.scene.transform.Scale;


import java.util.*;

/**
 * Classe principale d'affichage de l'arbre généalogique personnel.
 * Permet à l'utilisateur d'ajouter, supprimer, visualiser son arbre,
 * ou de consulter les arbres publics.
 */
public class AffichageArbre extends Application {
    private Scale arbreScale = new Scale(1, 1, 0, 0);

    @Override
    public void start(Stage primaryStage) {
        // Titre de la fenêtre
        Label title = new Label("Arbre Généalogique Pro++");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        // Conteneur du titre aligné à gauche
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Boutons du menu
        Button rafraichirButton = createModernButton("↻");
        Button monArbreButton = createModernButton("Mon arbre");
        Button ajouterPersonneButton = createModernButton("Ajouter un proche");
        Button supprimerPersonneButton = createModernButton("Supprimer une personne");
        Button arbresToutLeMondeButton = createModernButton("Voir les différents arbres");
        Button profilButton = createModernButton("Mon profil");
        Button deconnecterButton = createModernButton("Se Déconnecter");


	ComboBox<String> filtreCombo = new ComboBox<>();
        filtreCombo.getItems().addAll("Tous", "Enfants", "Grands-parents", "Frères/Sœurs", "Parents" , "Petits-enfants");
        filtreCombo.setValue("Tous");
        filtreCombo.setPrefHeight(40);        // hauteur similaire aux boutons
        filtreCombo.setPrefWidth(150);        // largeur fixe confortable
        filtreCombo.setStyle(
                "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #999;" +
                        "-fx-border-width: 1;" +
                        "-fx-background-color: linear-gradient(#f9f9f9, #dcdcdc);" +
                        "-fx-padding: 0 10 0 10;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;"
        );

// Bouton Rechercher
        Button rechercherButton = createModernButton("Rechercher");

        // Zone principale d'affichage de l'arbre
        Pane arbrePane = new Pane();
        arbrePane.setPrefHeight(500);	

	Slider zoomSlider = new Slider(0.5, 3.0, 1.0); // zoom de 0.5 à 3, valeur initiale 1
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setMajorTickUnit(0.5);
        zoomSlider.setBlockIncrement(0.1);
        zoomSlider.setPrefWidth(150);

	zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double scale = newVal.doubleValue();
            arbreScale.setX(scale);
            arbreScale.setY(scale);
        });

	 HBox zoomBox = new HBox(10, new Label("Zoom:"), zoomSlider);
        zoomBox.setAlignment(Pos.CENTER);
        zoomBox.setPadding(new Insets(10));


        // Affichage de tous les arbres accessibles
        arbresToutLeMondeButton.setOnAction(e -> {
            try {
                new AffichageTousArbres().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Réinitialise l'arbre affiché à l'écran
        rafraichirButton.setOnAction(e -> {
            arbrePane.getChildren().clear();
            afficherArbreDepuisBdd(arbrePane);
        });

        // Déconnexion de l'utilisateur
        deconnecterButton.setOnAction(e -> {
            Session.clear();
            ((Stage) deconnecterButton.getScene().getWindow()).close();
            try {
                new LoginApp().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Accès au profil de l'utilisateur
        profilButton.setOnAction(e -> {
            try {
                new Profil().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
	// Action sur le bouton rechercher
        rechercherButton.setOnAction(e -> {
            int idPersonne = Session.getUserId(); // Récupère l'ID courant (adapter si besoin)
            String filtre = filtreCombo.getValue();

            ArbreDAO dao = new ArbreDAO();
            List<Personne> resultat = new ArrayList<>();

            switch (filtre) {
                case "Enfants":
                    resultat = dao.getEnfantsPersonne(idPersonne);
                    break;
                case "Grands-parents":
                    resultat = dao.getGrandsParentsPersonne(idPersonne);
                    break;
                case "Frères/Sœurs":
                    resultat = dao.getFreresSoeursPersonne(idPersonne);
                    break;
                case "Parents":
                    resultat = dao.getParentsPersonne(idPersonne);
                    break;
                case "Petits-enfants":
                    resultat = dao.getPetitsEnfantsPersonne(idPersonne);
                    break;
                case "Tous":
                default:
                    dao.chargerFamillePourUtilisateur(idPersonne);
                    resultat = new ArrayList<>(dao.personnes.values());
                    break;
            }

            afficherResultatsFiltre(resultat, arbrePane);
        });

        // Ajout d’un proche à l’arbre
        ajouterPersonneButton.setOnAction(e -> {
            try {
                new AjoutPersonne().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Suppression d’une personne de l’arbre
        supprimerPersonneButton.setOnAction(e -> {
            try {
                new SupprimerPersonne().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Barre de boutons
        HBox buttonBox = new HBox(10, rafraichirButton, monArbreButton, ajouterPersonneButton, supprimerPersonneButton,filtreCombo,rechercherButton, arbresToutLeMondeButton, profilButton, deconnecterButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Barre supérieure avec le titre et les actions
        HBox navbar = new HBox(30, titleBox, buttonBox);
        navbar.setPadding(new Insets(15));
        navbar.setStyle("-fx-background-color: #333;");

        // Affichage initial de l'arbre au lancement
        afficherArbreDepuisBdd(arbrePane);

        // Conteneur global
        VBox root = new VBox(navbar, arbrePane,zoomBox);
        root.setStyle("-fx-background-color: #f0f0f0;");
        VBox.setVgrow(arbrePane, Priority.ALWAYS);

        // Construction et affichage de la scène
        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setTitle("Affichage Arbre Généalogique");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Charge les données de l'arbre depuis la base et les affiche graphiquement.
     */
    private void afficherArbreDepuisBdd(Pane pane) {
        // Chargement du fond visuel
        Image backgroundImage = new Image(getClass().getResource("/images/fond_parchemin.jpg").toExternalForm());
        BackgroundSize backgroundSize = new BackgroundSize(1.0, 1.0, true, true, false, false);
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
        pane.setBackground(new Background(background));

        ArbreDAO dao = new ArbreDAO();
        dao.mettreAJourNiveaux();
        dao.chargerFamillePourUtilisateur(Session.getUserId());

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.prefWidthProperty().bind(pane.widthProperty());
        scrollPane.prefHeightProperty().bind(pane.heightProperty());
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Group arbreGroup = new Group();
        arbreGroup.getTransforms().add(arbreScale);


        double spacingY = 180;
        double spacingX = 180;
        double nodeWidth = 120;
        double nodeHeight = 140;

        Map<Integer, VBox> noeuds = new HashMap<>();
        Map<Integer, List<Personne>> personnesParNiveau = new TreeMap<>();

        // Regroupe les personnes par niveau générationnel
        for (Personne p : dao.personnes.values()) {
            if (p.getNiveau() != null) {
                personnesParNiveau.computeIfAbsent(p.getNiveau(), k -> new ArrayList<>()).add(p);
            }
        }

        int minNiveau = personnesParNiveau.keySet().stream().min(Integer::compareTo).orElse(0);
        int maxNiveau = personnesParNiveau.keySet().stream().max(Integer::compareTo).orElse(0);

        // Positionne les nœuds dans le groupe en fonction de leur génération
        for (int niveau = minNiveau; niveau <= maxNiveau; niveau++) {
            List<Personne> personnes = personnesParNiveau.getOrDefault(niveau, Collections.emptyList());
            if (personnes.isEmpty()) continue;

            double totalWidth = personnes.size() * (nodeWidth + spacingX) - spacingX;
            double startX = (pane.getWidth() <= 0 ? 1200 : pane.getWidth() - totalWidth) / 2;
            double y = 50 + (niveau - minNiveau) * spacingY;
            double x = Math.max(50, startX);

            for (Personne p : personnes) {
                VBox label = creerLabel(p);
                appliquerStyleCarte(label);
                label.setLayoutX(x);
                label.setLayoutY(y);
                arbreGroup.getChildren().add(label);
                noeuds.put(p.getId(), label);
                x += nodeWidth + spacingX;
            }
        }

        Set<String> couplesDessines = new HashSet<>();

        // Création des lignes entre les couples et leurs enfants
        for (Map.Entry<Integer, List<Integer>> entry : dao.relations.entrySet()) {
            int parent1Id = entry.getKey();
            List<Integer> enfants = entry.getValue();
            VBox parent1Box = noeuds.get(parent1Id);
            if (parent1Box == null) continue;

            for (int enfantId : enfants) {
                VBox childBox = noeuds.get(enfantId);
                if (childBox == null) continue;

                int parent2Id = dao.getAutreParent(enfantId, parent1Id);
                VBox parent2Box = noeuds.get(parent2Id);

                String coupleKey = parent1Id < parent2Id ? parent1Id + "-" + parent2Id : parent2Id + "-" + parent1Id;
                if (parent2Box != null && couplesDessines.contains(coupleKey)) continue;

                List<Integer> enfantsCommuns = dao.getEnfantsCommun(parent1Id, parent2Id);

                double sommeX = 0;
                int compteur = 0;
                for (int eid : enfantsCommuns) {
                    VBox eb = noeuds.get(eid);
                    if (eb != null) {
                        sommeX += eb.getLayoutX() + nodeWidth / 2;
                        compteur++;
                    }
                }

                double centreX = compteur > 0 ? sommeX / compteur : parent1Box.getLayoutX() + nodeWidth / 2;
                double parentY = parent1Box.getLayoutY();

                double x1 = parent1Box.getLayoutX() + nodeWidth / 2;
                double x2 = parent2Box != null ? parent2Box.getLayoutX() + nodeWidth / 2 : x1;

                if (enfantsCommuns.size() < 1 || couplesDessines.contains(coupleKey)) continue;

                if (parent2Box != null) {
                    Line lienParents = new Line(x1, parentY + nodeHeight + 10, x2, parentY + nodeHeight + 10);
                    lienParents.setStroke(Color.DARKGRAY);
                    lienParents.setStrokeWidth(2);
                    arbreGroup.getChildren().add(lienParents);
                }

                double centreXCouple = parent2Box != null ? (x1 + x2) / 2 : x1;
                double centreY = parentY + nodeHeight + 10;

                Line ligneVersEnfants = new Line(centreXCouple, centreY, centreXCouple, centreY + 30);
                arbreGroup.getChildren().add(ligneVersEnfants);

                for (int eid : enfantsCommuns) {
                    VBox enfant = noeuds.get(eid);
                    if (enfant == null) continue;

                    double xe = enfant.getLayoutX() + nodeWidth / 2;
                    double ye = enfant.getLayoutY();

                    Line ligne = new Line(centreXCouple, centreY + 30, xe, ye);
                    ligne.setStroke(Color.DARKGRAY);
                    ligne.setStrokeWidth(2);
                    arbreGroup.getChildren().add(ligne);
                }

                couplesDessines.add(coupleKey);
            }
        }

        // Étend la surface scrollable pour éviter les coupures
        Region bounds = new Region();
        bounds.setMinSize(2000, (maxNiveau - minNiveau + 2) * spacingY);
        bounds.setMouseTransparent(true);
        arbreGroup.getChildren().add(bounds);

        scrollPane.setContent(arbreGroup);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        pane.getChildren().add(scrollPane);
    }

	 private void afficherResultatsFiltre(List<Personne> personnes, Pane pane) {
        pane.getChildren().clear();

        Group groupe = new Group();

        double spacingX = 180;
        double spacingY = 180;
        double nodeWidth = 120;
        double nodeHeight = 140;

        double startX = 50;
        double startY = 50;

        double x = startX;
        double y = startY;

        int count = 0;

        for (Personne p : personnes) {
            VBox node = creerLabel(p);
            appliquerStyleCarte(node);
            node.setLayoutX(x);
            node.setLayoutY(y);
            groupe.getChildren().add(node);

            x += nodeWidth + spacingX;
            count++;

            // Passer à la ligne suivante tous les 5 éléments (par ex)
            if (count % 5 == 0) {
                x = startX;
                y += nodeHeight + spacingY;
            }
        }

        pane.getChildren().add(groupe);
    }

    /**
     * Génère l'affichage d'une personne (nom, naissance, image) sous forme de boîte.
     */
    private VBox creerLabel(Personne p) {
        VBox box = new VBox(5);
        Label nomPrenom = new Label(p.getNomComplet());
        if (p.isInscrit()) {
            nomPrenom.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
        } else {
            nomPrenom.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
        }
        Label naissance = new Label(p.getDateNaissance() != null ? "Né le " + p.getDateNaissance() : "Date inconnue");
        naissance.setStyle("-fx-text-fill: black;");
        ImageView photoView = new ImageView();
        if (p.getPhoto() != null && !p.getPhoto().isEmpty()) {
            try {
                String imagePath = "/images/profils/" + p.getPhoto();
                Image img = new Image(getClass().getResource(imagePath).toExternalForm(), 80, 80, true, true);
                photoView.setImage(img);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        box.getChildren().addAll(photoView, nomPrenom, naissance);
        box.setPrefSize(120, 140);

        String fond = p.isInscrit()
                ? "rgba(173,255,47,0.9)"
                : "rgba(255,102,102,0.9)";

        box.setStyle("-fx-border-color: black; -fx-background-color: " + fond + "; -fx-padding: 5px; -fx-alignment: center;");
        return box;
    }

    /**
     * Applique un style visuel moderne à un nœud avec effet visuel.
     */
    private void appliquerStyleCarte(VBox box) {
        box.setStyle(""
                + "-fx-background-color: rgba(255,255,255,0.85);"
                + "-fx-background-radius: 15;"
                + "-fx-border-radius: 15;"
                + "-fx-border-color: #ffff;"
                + "-fx-border-width: 1.5;"
                + "-fx-padding: 10px;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 2, 2);"
                + "-fx-alignment: center;");
        FadeTransition ft = new FadeTransition(Duration.millis(500), box);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /**
     * Crée un bouton standardisé avec style uniforme.
     */
    private Button createModernButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setTextFill(Color.BLACK);
        button.setStyle("-fx-background-color: linear-gradient(#f9f9f9, #dcdcdc); -fx-background-radius: 10; -fx-border-color: #999; -fx-border-width: 1; -fx-border-radius: 10; -fx-padding: 10 20;");
        return button;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
