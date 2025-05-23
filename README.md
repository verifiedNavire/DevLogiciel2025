# Projet Arbre Généalogique Pro++

## Description

Application JavaFX permettant aux utilisateurs de créer, visualiser et gérer leur arbre généalogique. Une interface d'administration permet de valider les demandes d'adhésion.

---

## Fonctionnalités implémentées

### Authentification & Inscription

* Inscription avec : nom, prénom, email, mot de passe, date de naissance, numéro de sécurité sociale, nationalité
* Upload d'une photo de profil + pièce d'identité (copie d'image ou PDF)
* Lors de l'inscription :

  * L'utilisateur est enregistré avec le statut `en_attente`
  * Un arbre privé est créé automatiquement
  * Le mot de passe est initialisé au prénom

### Administration

* Interface admin pour :

  * Visualiser les utilisateurs en attente
  * Valider ou refuser les demandes
  * En cas de validation :

    * Des codes public et privé sont générés
    * Envoi automatique d'un e-mail contenant les codes et les instructions de connexion

### Connexion

* Connexion par code privé + mot de passe
* Si le mot de passe correspond au prénom, redirection vers la page de changement de mot de passe
* Sinon :

  * Si admin → redirection vers l'interface d'administration
  * Sinon → affichage de l'arbre généalogique personnel

### Arbre généalogique

* Visualisation graphique des personnes dans l'arbre
* Bouton "Ajouter une personne"
* Bouton "Supprimer une personne"

### Envoi d'e-mails

* Configuration SMTP via Gmail (mot de passe d'application)
* Utilisation de Jakarta Mail (libs importées à la main)

---

## Bibliothèques

* JavaFX (UI principale)
* Jakarta Mail (e-mail SMTP)
* JDBC (connexion MySQL)

---

## Dépendances externes

Placer dans le dossier `/lib` :

* `jakarta.mail-api-2.1.2.jar`
* `jakarta.activation-2.0.1.jar`
* `javafx-base`, `javafx-controls`, `javafx-graphics` (si non fournis par l'IDE)

---

## Auteurs

Saidi, Besma
Paimba-Sail, Owen
Fowdar, Heeshaam
Cupessala, Alvariane 
Fowdar, Heeshaam
Ocloo, Leslie
