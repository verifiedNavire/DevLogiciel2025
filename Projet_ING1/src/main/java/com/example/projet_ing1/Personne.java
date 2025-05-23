package com.example.projet_ing1;

import java.sql.Date;

/**
 * Classe représentant une personne dans l’arbre généalogique.
 * Elle peut être un simple nœud (non inscrit) ou un utilisateur inscrit avec compte.
 */
public class Personne {

    // === Attributs ===
    private int id;                    // Identifiant unique dans la base de données
    private String nom;                // Nom de famille de la personne
    private String prenom;             // Prénom de la personne
    private Date dateNaissance;        // Date de naissance (peut être null si inconnue)
    private String motDePasse;         // Mot de passe si la personne est un utilisateur
    private boolean inscrit;           // Vrai si la personne a un compte utilisateur
    private String photo;              // Nom du fichier image associé à la personne
    private Integer niveau;            // Niveau généalogique (0 = racine, 1 = enfants, etc.)

    // === Constructeurs ===

    /**
     * Constructeur complet avec tous les champs.
     */
    public Personne(int id, String nom, String prenom, Date dateNaissance, String motDePasse,
                    boolean inscrit, String photo, Integer niveau) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.motDePasse = motDePasse;
        this.inscrit = inscrit;
        this.photo = photo;
        this.niveau = niveau;
    }

    /**
     * Constructeur sans le niveau, utile lors d’une création initiale.
     */
    public Personne(int id, String nom, String prenom, Date dateNaissance,
                    String motDePasse, boolean inscrit, String photo) {
        this(id, nom, prenom, dateNaissance, motDePasse, inscrit, photo, null);
    }

    /**
     * Constructeur minimal pour afficher ou récupérer une personne simple.
     */
    public Personne(int id, String nom, String prenom, boolean inscrit) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.inscrit = inscrit;
    }

    /**
     * Constructeur vide, utile pour l’initialisation manuelle ou JDBC.
     */
    public Personne() {}

    // === Getters ===

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public boolean isInscrit() {
        return inscrit;
    }

    public String getPhoto() {
        return photo;
    }

    public Integer getNiveau() {
        return niveau;
    }

    /**
     * Retourne une version lisible du nom complet.
     * Format : "Prénom Nom"
     */
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    // === Setters ===

    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public void setInscrit(boolean inscrit) {
        this.inscrit = inscrit;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setNiveau(Integer niveau) {
        this.niveau = niveau;
    }

    /**
     * Représentation textuelle d’une personne (utilisée dans les ComboBox par exemple).
     * Affiche le prénom, le nom, et la date si disponible.
     */
    @Override
    public String toString() {
        return prenom + " " + nom + (dateNaissance != null ? " (" + dateNaissance + ")" : "");
    }
}
