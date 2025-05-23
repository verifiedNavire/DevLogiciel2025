package com.example.projet_ing1;

import java.sql.Date;

public class Personne {
    // --- Attributs ---
    private int id;                    // Identifiant unique de la personne
    private String nom;                // Nom de famille
    private String prenom;             // Prénom
    private Date dateNaissance;        // Date de naissance
    private String motDePasse;         // Mot de passe (si utilisateur inscrit)
    private boolean inscrit;           // Indique si la personne a un compte actif (true) ou est juste un nœud dans l’arbre (false)
    private String photo;              // Nom du fichier image associé
    private Integer niveau;            // Niveau dans l’arbre (0 = racine, 1 = enfants, etc.)
    private int idArbre;
    private String securiteSociale;
    private String nationalite;
    private String fichierIdentite;  // peut être null
    private String visibilite;

    // --- Constructeurs ---

    // Constructeur complet avec tous les champs, y compris le niveau
    public Personne(int id, String nom, String prenom, Date dateNaissance, String motDePasse, boolean inscrit, String photo, Integer niveau) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.motDePasse = motDePasse;
        this.inscrit = inscrit;
        this.photo = photo;
        this.niveau = niveau;
    }

    // Constructeur complet avec tous les champs, y compris la visibilité de la personne dans l'arbre
    public Personne(int id, String nom, String prenom, Date dateNaissance, String motDePasse, boolean inscrit, String photo, Integer niveau, String visibilite) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.motDePasse = motDePasse;
        this.inscrit = inscrit;
        this.photo = photo;
        this.niveau = niveau;
        this.visibilite = visibilite;
    }

    // Constructeur sans le niveau (utile pour insertion initiale)
    public Personne(int id, String nom, String prenom, Date dateNaissance, String motDePasse, boolean inscrit, String photo) {
        this(id, nom, prenom, dateNaissance, motDePasse, inscrit, photo, null);
    }

    // Constructeur simplifié avec juste l’essentiel
    public Personne(int id, String nom, String prenom, boolean inscrit) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.inscrit = inscrit;
    }

        public Personne(int id, int idarbre) {
        this.id = id;
        this.idarbre = idarbre;
    }
    
    // Constructeur vide (utile pour setter manuels ou pour JDBC)
    public Personne() {}

    // --- Getters ---

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

    // Retourne le nom complet sous la forme "Prénom Nom"
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    // --- Setters ---

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

    @Override
    public String toString() {
        return prenom + " " + nom + (dateNaissance != null ? " (" + dateNaissance + ")" : "");
    }
    public int getIdArbre() { return idArbre; }
    public void setIdArbre(int idArbre) { this.idArbre = idArbre; }

    public String getSecuriteSociale() { return securiteSociale; }
    public void setSecuriteSociale(String securiteSociale) { this.securiteSociale = securiteSociale; }

    public String getNationalite() { return nationalite; }
    public void setNationalite(String nationalite) { this.nationalite = nationalite; }

    public String getFichierIdentite() { return fichierIdentite; }
    public void setFichierIdentite(String fichierIdentite) { this.fichierIdentite = fichierIdentite; }

    public String getVisibilite() { return visibilite; }
    public void setVisibilite(String visibilite) { this.visibilite = visibilite; }

}
