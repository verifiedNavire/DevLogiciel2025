package com.example.projet_ing1;

import javafx.scene.control.Button;

/**
 * Cette classe représente un utilisateur en attente de validation dans l'interface admin.
 * Elle est utilisée dans un TableView où chaque ligne correspond à un utilisateur à traiter.
 * Elle contient à la fois les données (nom, prénom, email) et les boutons d'action (valider/refuser).
 */
public class UtilisateurEnAttente {

    // Identifiant de l'utilisateur en base (sert à faire les actions en BDD lors d'une validation ou d'un refus)
    private int id;

    // Informations principales affichées dans le tableau
    private String nom;
    private String prenom;
    private String email;

    // Boutons affichés dans le tableau, colonnes d'action
    private Button validerBtn;
    private Button refuserBtn;

    /**
     * Constructeur principal permettant d'initialiser tous les champs d'un utilisateur.
     * id identifiant unique en base
     * nom nom de famille
     * prenom prénom
     * email adresse email
     * validerBtn bouton pour valider l'inscription
     * refuserBtn bouton pour refuser l'inscription
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

    // --- Getters utilisés automatiquement par le TableView JavaFX pour remplir les colonnes ---

    /**
     * @return le nom de l'utilisateur (affiché dans la colonne "Nom")
     */
    public String getNom() {
        return nom;
    }

    /**
     * @return le prénom de l'utilisateur (affiché dans la colonne "Prénom")
     */
    public String getPrenom() {
        return prenom;
    }

    /**
     * @return l'email de l'utilisateur (affiché dans la colonne "Email")
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return le bouton "Valider" associé à cette ligne (action personnalisée dans ValidationAdmin)
     */
    public Button getValiderBtn() {
        return validerBtn;
    }

    /**
     * @return le bouton "Refuser" associé à cette ligne (action personnalisée dans ValidationAdmin)
     */
    public Button getRefuserBtn() {
        return refuserBtn;
    }

    /**
     * @return l'identifiant interne de l'utilisateur (non visible dans l'IHM mais nécessaire pour les requêtes SQL)
     */
    public int getId() {
        return id;
    }
}
