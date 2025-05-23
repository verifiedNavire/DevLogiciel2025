package com.example.projet_ing1;

import javafx.scene.control.Button;

/**
 * Cette classe représente une ligne d'utilisateur dans le tableau d'administration affiché dans ValidationAdmin.java.
 * Chaque instance contient les infos d'un utilisateur en attente de validation.
 */
public class UtilisateurEnAttente {

    // Identifiant en base de données (peut être utile pour les actions)
    private int id;

    // Données de l'utilisateur à afficher
    private String nom;
    private String prenom;
    private String email;

    // Boutons d'action (affichés dans la même ligne du tableau)
    private Button validerBtn;
    private Button refuserBtn;

    /**
     * Constructeur complet avec les infos et les boutons d'action.
     */
    public UtilisateurEnAttente(int id, String nom, String prenom, String email,
                                Button validerBtn, Button refuserBtn) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.validerBtn = validerBtn;
        this.refuserBtn = refuserBtn;
    }

    // Getters appelés automatiquement par JavaFX pour alimenter le TableView

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public Button getValiderBtn() {
        return validerBtn;
    }

    public Button getRefuserBtn() {
        return refuserBtn;
    }

    // Getter d'ID (non affiché mais utile pour les actions internes)
    public int getId() {
        return id;
    }
}