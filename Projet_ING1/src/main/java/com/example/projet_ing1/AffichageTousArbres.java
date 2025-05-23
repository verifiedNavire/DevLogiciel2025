package com.example.projet_ing1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.*;

public class AffichageTousArbres extends Application {

    @Override
    public void start(Stage stage) {
        VBox arbresBox = new VBox(40);
        arbresBox.setPadding(new Insets(20));
        arbresBox.setStyle("-fx-background-color: #f0f0f0;");

        ArbreDAO dao = new ArbreDAO();
        dao.mettreAJourNiveaux();

        // Récupère tous les ID uniques d'arbres
        List<Integer> idArbres = dao.getTousLesIdArbres();

        for (int idArbre : idArbres) {
            Label titre = new Label("Arbre ID : " + idArbre);
            titre.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            Pane arbrePane = new Pane();
            arbrePane.setPrefHeight(600);

            afficherArbreDepuisBdd(idArbre, arbrePane);

            VBox arbreContainer = new VBox(10, titre, arbrePane);
            arbresBox.getChildren().add(arbreContainer);
        }

        ScrollPane scrollPane = new ScrollPane(arbresBox);
        scrollPane.setFitToWidth(true);
        Scene scene = new Scene(scrollPane, 1200, 800);
        stage.setTitle("Tous les Arbres Généalogiques");
        stage.setScene(scene);
        stage.show();
    }

    private void afficherArbreDepuisBdd(int idArbre, Pane pane) {
        // Fond d'écran style parchemin
        Image backgroundImage = new Image(getClass().getResource("/images/fond_parchemin.jpg").toExternalForm());
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, new BackgroundSize(1.0, 1.0, true, true, false, false));
        pane.setBackground(new Background(background));

        ArbreDAO dao = new ArbreDAO();
        dao.chargerFamillePourArbre(idArbre);

        Map<Integer, VBox> noeuds = new HashMap<>();
        Map<Integer, List<Personne>> personnesParNiveau = new TreeMap<>();

        double spacingY = 180;
        double spacingX = 180;
        double nodeWidth = 120;
        double nodeHeight = 140;

        for (Personne p : dao.personnes.values()) {
            if (p.getNiveau() != null) {
                personnesParNiveau.computeIfAbsent(p.getNiveau(), k -> new ArrayList<>()).add(p);
            }
        }

        Group arbreGroup = new Group();
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

        // Lignes entre parents et enfants
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

                double x1 = centreX - 70;
                double x2 = centreX + 70;

                parent1Box.setLayoutX(x1 - nodeWidth / 2);
                parent1Box.setLayoutY(parentY);

                if (parent2Box != null) {
                    parent2Box.setLayoutX(x2 - nodeWidth / 2);
                    parent2Box.setLayoutY(parentY);
                }

                double y = parent1Box.getLayoutY() + nodeHeight;

                if (parent2Box != null) {
                    Line lienParents = new Line(x1, y + 10, x2, y + 10);
                    lienParents.setStroke(Color.DARKGRAY);
                    lienParents.setStrokeWidth(2);
                    arbreGroup.getChildren().add(lienParents);
                }

                double centreXCouple = parent2Box != null ? (x1 + x2) / 2 : x1;
                double centreY = y + 10;

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

        pane.getChildren().add(arbreGroup);
    }

    private VBox creerLabel(Personne p) {
        VBox box = new VBox(5);
        Label nomPrenom = new Label(p.getNomComplet());
        nomPrenom.setStyle(p.isInscrit() ? "-fx-font-weight: bold;" : "-fx-font-weight: bold; -fx-text-fill: red;");
        Label naissance = new Label(p.getDateNaissance() != null ? "Né le " + p.getDateNaissance() : "Date inconnue");
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
        return box;
    }

    private void appliquerStyleCarte(VBox box) {
        box.setStyle("""
            -fx-background-color: rgba(255,255,255,0.85);
            -fx-background-radius: 15;
            -fx-border-radius: 15;
            -fx-border-color: #ffff;
            -fx-border-width: 1.5;
            -fx-padding: 10px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 2, 2);
            -fx-alignment: center;
        """);
    }
}