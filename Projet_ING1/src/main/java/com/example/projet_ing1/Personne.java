package com.example.projet_ing1;

public class Personne {
    private int id;
    private String nom;
    private String prenom;
    private boolean inscrit;

    public Personne(int id, String nom, String prenom, boolean inscrit) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.inscrit = inscrit;
    }

    public int getId() { return id; }
    public String getNomComplet() { return prenom + " " + nom; }
    public boolean isInscrit() { return inscrit; }
}
