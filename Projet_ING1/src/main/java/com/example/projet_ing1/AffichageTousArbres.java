package com.example.projet_ing1;

// Imports nécessaires pour construire l'interface graphique JavaFX
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

/**
 * Cette classe permet d’afficher en lecture seule tous les arbres généalogiques de la base de données.
 * Chaque arbre est affiché verticalement l’un en dessous de l’autre, avec une représentation graphique.
 */
public class AffichageTousArbres extends Application {

    @Override
    public void start(Stage stage) {
        VBox arbresBox = new VBox(40); // Conteneur principal des arbres, avec espacement vertical
        arbresBox.setPadding(new Insets(20)); // Marges internes
        arbresBox.setStyle("-fx-background-color: #f0f0f0;");

        ArbreDAO dao = new ArbreDAO();
        dao.mettreAJourNiveaux(); // Mise à jour des niveaux de toutes les personnes

        List<Integer> idArbres = dao.getTousLesIdArbres(); // Récupère tous les arbres uniques

        for (int idArbre : idArbres) {
            // Titre de l’arbre
            Label titre = new Label("Arbre ID : " + idArbre);
            titre.setFont(Font.font("Arial", FontWeight.BOLD, 18));

            Pane arbrePane = new Pane(); // Zone graphique de l’arbre
            arbrePane.setPrefHeight(600);

            afficherArbreDepuisBdd(idArbre, arbrePane); // Affichage personnalisé pour chaque arbre

            VBox arbreContainer = new VBox(10, titre, arbrePane); // Titre + graphique
            arbresBox.getChildren().add(arbreContainer); // Ajout à la liste globale
        }

        ScrollPane scrollPane = new ScrollPane(arbresBox); // Scroll général pour voir tous les arbres
        scrollPane.setFitToWidth(true); // S'étire horizontalement
        Scene scene = new Scene(scrollPane, 1200, 800);
        stage.setTitle("Tous les Arbres Généalogiques");
        stage.setScene(scene);
        stage.show(); // Affiche la scène
    }

    /**
     * Affiche graphiquement un arbre à partir de son ID et le place dans le pane fourni
     */
    private void afficherArbreDepuisBdd(int idArbre, Pane pane) {
        // Ajout d'un fond parchemin
        Image backgroundImage = new Image(getClass().getResource("/images/fond_parchemin.jpg").toExternalForm());
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, new BackgroundSize(1.0, 1.0, true, true, false, false));
        pane.setBackground(new Background(background));

        ArbreDAO dao = new ArbreDAO();
        dao.mettreAJourNiveaux();
        dao.chargerFamillePourArbre(idArbre); // Charge les membres et relations de l’arbre

        // Prépare un ScrollPane interne pour naviguer dans l’arbre
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.prefWidthProperty().bind(pane.widthProperty());
        scrollPane.prefHeightProperty().bind(pane.heightProperty());

        Group arbreGroup = new Group(); // Contient les nœuds et les liens

        // Paramètres d'affichage
        double spacingY = 180;
        double spacingX = 180;
        double nodeWidth = 120;
        double nodeHeight = 140;

        Map<Integer, VBox> noeuds = new HashMap<>(); // id → VBox
        Map<Integer, List<Personne>> personnesParNiveau = new TreeMap<>(); // génération → personnes

        // Classe les personnes par niveau générationnel
        for (Personne p : dao.personnes.values()) {
            if (p.getNiveau() != null) {
                personnesParNiveau.computeIfAbsent(p.getNiveau(), k -> new ArrayList<>()).add(p);
            }
        }

        // Détermine la plage des niveaux
        int minNiveau = personnesParNiveau.keySet().stream().min(Integer::compareTo).orElse(0);
        int maxNiveau = personnesParNiveau.keySet().stream().max(Integer::compareTo).orElse(0);

        // Positionnement des boîtes représentant les personnes
        for (int niveau = minNiveau; niveau <= maxNiveau; niveau++) {
            List<Personne> personnes = personnesParNiveau.getOrDefault(niveau, Collections.emptyList());
            if (personnes.isEmpty()) continue;

            double totalWidth = personnes.size() * (nodeWidth + spacingX) - spacingX;
            double startX = (totalWidth > 0) ? (Math.max(pane.getWidth(), 1200) - totalWidth) / 2 : 50;
            double y = 50 + (niveau - minNiveau) * spacingY;
            double x = Math.max(50, startX);

            for (Personne p : personnes) {
                VBox label = creerLabel(p); // Création visuelle de la personne
                appliquerStyleCarte(label); // Application du style
                label.setLayoutX(x);
                label.setLayoutY(y);
                arbreGroup.getChildren().add(label); // Ajoute à la scène
                noeuds.put(p.getId(), label); // Stocke la position
                x += nodeWidth + spacingX;
            }
        }

        // Tracés des liens entre parents et enfants
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

                // Empêche les doublons de couple
                String coupleKey = parent1Id < parent2Id ? parent1Id + "-" + parent2Id : parent2Id + "-" + parent1Id;
                if (parent2Box != null && couplesDessines.contains(coupleKey)) continue;

                List<Integer> enfantsCommuns = dao.getEnfantsCommun(parent1Id, parent2Id);

                // Calcule la position moyenne des enfants
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

                // Ignore les couples sans enfants
                if (enfantsCommuns.size() < 1 || couplesDessines.contains(coupleKey)) continue;

                // Ligne horizontale entre les deux parents
                if (parent2Box != null) {
                    Line lienParents = new Line(x1, parentY + nodeHeight + 10, x2, parentY + nodeHeight + 10);
                    lienParents.setStroke(Color.DARKGRAY);
                    lienParents.setStrokeWidth(2);
                    arbreGroup.getChildren().add(lienParents);
                }

                double centreXCouple = parent2Box != null ? (x1 + x2) / 2 : x1;
                double centreY = parentY + nodeHeight + 10;

                // Trait descendant vers les enfants
                Line ligneVersEnfants = new Line(centreXCouple, centreY, centreXCouple, centreY + 30);
                arbreGroup.getChildren().add(ligneVersEnfants);

                // Branches vers chaque enfant
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

                couplesDessines.add(coupleKey); // Marque le couple comme traité
            }
        }

        // Étend la zone de scroll si besoin
        double totalHeight = (maxNiveau - minNiveau + 2) * spacingY;
        Region bounds = new Region();
        bounds.setMinSize(3000, totalHeight); // largeur généreuse
        bounds.setMouseTransparent(true);
        arbreGroup.getChildren().add(bounds);

        scrollPane.setContent(arbreGroup); // Ajoute tout dans le scroll interne
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        pane.getChildren().clear();
        pane.getChildren().add(scrollPane); // Intègre à la fenêtre
    }

    /**
     * Crée un nœud graphique représentant une personne (nom, photo, date)
     */
    private VBox creerLabel(Personne p) {
        VBox box = new VBox(5);
        Label nomPrenom = new Label(p.getNomComplet());
        nomPrenom.setStyle(p.isInscrit() ? "-fx-font-weight: bold;" : "-fx-font-weight: bold; -fx-text-fill: red;");
        Label naissance = new Label(p.getDateNaissance() != null ? "Né le " + p.getDateNaissance() : "Date inconnue");

        // 📷 Photo
        ImageView photoView = new ImageView();
        if (p.getPhoto() != null && !p.getPhoto().isEmpty()) {
            try {
                String imagePath = "/images/profils/" + p.getPhoto();
                Image img = new Image(getClass().getResource(imagePath).toExternalForm(), 80, 80, true, true);
                photoView.setImage(img);
            } catch (Exception e) {
                e.printStackTrace(); // En cas d'erreur de chargement
            }
        }

        box.getChildren().addAll(photoView, nomPrenom, naissance);
        box.setPrefSize(120, 140);
        return box;
    }

    /**
     * Applique un style visuel à une carte (VBox) représentant une personne
     */
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
