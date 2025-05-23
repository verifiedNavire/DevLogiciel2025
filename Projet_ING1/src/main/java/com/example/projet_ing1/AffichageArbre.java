package com.example.projet_ing1;

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

import java.util.*;

public class AffichageArbre extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label title = new Label("Arbre Généalogique Pro++");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button rafraichirButton = createModernButton("↻");
        Button monArbreButton = createModernButton("Mon arbre");
        Button ajouterPersonneButton = createModernButton("Ajouter un proche");
        Button supprimerPersonneButton = createModernButton("Supprimer une personne");
        Button arbresToutLeMondeButton = createModernButton("Voir les différents arbres");
        Button profilButton = createModernButton("Mon profil");
        Button deconnecterButton = createModernButton("Se Déconnecter");

        Pane arbrePane = new Pane();
        arbrePane.setPrefHeight(500);


        arbresToutLeMondeButton.setOnAction(e ->{
            try {
                new AffichageTousArbres().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        rafraichirButton.setOnAction(e -> {
            arbrePane.getChildren().clear();
            afficherArbreDepuisBdd(arbrePane);
        });

        deconnecterButton.setOnAction(e -> {
            Session.clear();
            ((Stage) deconnecterButton.getScene().getWindow()).close(); // ferme la fenêtre actuelle
            try {
                new LoginApp().start(new Stage()); // relance la page de connexion
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });



        profilButton.setOnAction(e -> {
            try {
                new Profil().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        ajouterPersonneButton.setOnAction(e -> {
            try {
                new AjoutPersonne().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        supprimerPersonneButton.setOnAction(e -> {
            try {
                new SupprimerPersonne().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });



        HBox buttonBox = new HBox(10, rafraichirButton, monArbreButton, ajouterPersonneButton, supprimerPersonneButton, arbresToutLeMondeButton, profilButton, deconnecterButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        HBox navbar = new HBox(30, titleBox, buttonBox);
        navbar.setPadding(new Insets(15));
        navbar.setStyle("-fx-background-color: #333;");

        afficherArbreDepuisBdd(arbrePane);

        VBox root = new VBox(navbar, arbrePane);
        root.setStyle("-fx-background-color: #f0f0f0;");
        VBox.setVgrow(arbrePane, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setTitle("Affichage Arbre Généalogique");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void afficherArbreDepuisBdd(Pane pane) {
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

        double spacingY = 180;
        double spacingX = 180;
        double nodeWidth = 120;
        double nodeHeight = 140;

        Map<Integer, VBox> noeuds = new HashMap<>();
        Map<Integer, List<Personne>> personnesParNiveau = new TreeMap<>();

        for (Personne p : dao.personnes.values()) {
            if (p.getNiveau() != null) {
                personnesParNiveau.computeIfAbsent(p.getNiveau(), k -> new ArrayList<>()).add(p);
            }
        }

        int minNiveau = personnesParNiveau.keySet().stream().min(Integer::compareTo).orElse(0);
        int maxNiveau = personnesParNiveau.keySet().stream().max(Integer::compareTo).orElse(0);

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

                // ✅ NE PAS TRACER le couple si c’est juste un lien artificiel
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

        // 🌟 Couleur de fond dynamique
        String fond = p.isInscrit()
                ? "rgba(173,255,47,0.9)"    // vert clair
                : "rgba(255,102,102,0.9)"; // rouge doux

        box.setStyle("-fx-border-color: black; -fx-background-color: " + fond + "; -fx-padding: 5px; -fx-alignment: center;");
        return box;
    }


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