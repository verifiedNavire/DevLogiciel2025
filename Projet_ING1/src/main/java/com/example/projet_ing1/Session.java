package com.example.projet_ing1;

/**
 * Classe utilitaire pour gérer les informations de session de l'utilisateur actuellement connecté.
 * Cette classe contient uniquement des membres statiques (singleton implicite).
 */
public class Session {

    // ID de la personne actuellement connectée (référence à la table `personne`)
    private static int userId;

    /**
     * Définit l'identifiant de l'utilisateur connecté.
     * @param id identifiant dans la base de données
     */
    public static void setUserId(int id) {
        userId = id;
    }

    /**
     * Récupère l'identifiant de l'utilisateur actuellement connecté.
     * @return identifiant utilisateur
     */
    public static int getUserId() {
        return userId;
    }

    /**
     * Réinitialise la session (déconnexion).
     */
    public static void clear() {
        userId = -1; // valeur conventionnelle pour signifier "aucun utilisateur connecté"
    }

    // Rôle de l'utilisateur (ex : "admin" ou "utilisateur")
    private static String role;

    /**
     * Définit le rôle de l'utilisateur actuellement connecté.
     * @param r chaîne représentant le rôle (ex : "admin")
     */
    public static void setUserRole(String r) {
        role = r;
    }

    /**
     * Récupère le rôle de l'utilisateur connecté.
     * @return rôle (ex : "admin" ou "utilisateur")
     */
    public static String getUserRole() {
        return role;
    }
}
