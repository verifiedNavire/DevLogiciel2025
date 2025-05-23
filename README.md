# Projet Arbre Généalogique Pro++

## Description

Application JavaFX permettant aux utilisateurs de créer, visualiser et gérer leur arbre généalogique. Une interface d'administration permet de valider les demandes d'adhésion.

---

## Démarrage du projet

Le point d'entrée de l'application est la classe suivante :  
`src/main/java/com/example/projet_ing1/LoginApp.java`

Pour lancer l'application, exécutez **`LoginApp`**. C'est à partir de cette classe que s'affiche l'interface de connexion, permettant ensuite d'accéder à toutes les fonctionnalités selon le rôle de l'utilisateur (admin ou simple utilisateur).

---

## Structure des fichiers `.java`

Tous les fichiers sources se trouvent dans :  
`src/main/java/com/example/projet_ing1/`

Les classes principales sont :

- `LoginApp.java` : **point d’entrée** de l'application  
- `Inscription.java` : formulaire d’inscription complet avec upload d’image et de pièce d’identité  
- `AffichageArbre.java` : visualisation graphique de l’arbre pour l'utilisateur connecté  
- `AffichageTousArbres.java` : vue en lecture seule de tous les arbres validés  
- `AjoutPersonne.java` : formulaire dynamique pour ajouter un parent ou un enfant  
- `SupprimerPersonne.java` : suppression sécurisée d’une personne dans l’arbre  
- `ValidationAdmin.java` : interface pour que l'admin valide ou refuse les inscriptions  
- `EnvoiMail.java` : gestion de l’envoi automatique d'e-mails de validation via Gmail  
- `ChangerMdp.java` : formulaire pour forcer le changement du mot de passe initial  
- `Profil.java` : modification du nom, prénom, photo de profil de l’utilisateur  
- `Session.java` : stockage temporaire des données de session (ID utilisateur, rôle)  
- `Database.java` : gestion de la connexion à la base de données  
- `ArbreDAO.java` : interactions principales avec la base (liens, niveaux, familles)  
- `Personne.java` : modèle représentant une personne dans l’arbre  
- `UtilisateurEnAttente.java` : structure pour afficher les utilisateurs dans le tableau admin  

---

## Fonctionnalités implémentées

### Authentification & Inscription

- Inscription avec : nom, prénom, email, mot de passe, date de naissance, numéro de sécurité sociale, nationalité  
- Upload d'une photo de profil + pièce d'identité (copie d'image ou PDF)  
- Lors de l'inscription :
  - L'utilisateur est enregistré avec le statut `en_attente`
  - Un arbre privé est créé automatiquement
  - Le mot de passe est initialisé au prénom

### Administration

- Interface admin pour :
  - Visualiser les utilisateurs en attente
  - Valider ou refuser les demandes
  - En cas de validation :
    - Des codes public et privé sont générés
    - Envoi automatique d'un e-mail contenant les codes et les instructions de connexion

### Connexion

- Connexion par code privé + mot de passe
- Si le mot de passe correspond au prénom, redirection vers la page de changement de mot de passe
- Sinon :
  - Si admin → redirection vers l'interface d'administration
  - Sinon → affichage de l'arbre généalogique personnel

### Arbre généalogique

- Visualisation graphique des personnes dans l'arbre
- Bouton "Ajouter une personne"
- Bouton "Supprimer une personne"

### Envoi d'e-mails

- Configuration SMTP via Gmail (mot de passe d'application)
- Utilisation de Jakarta Mail (libs importées à la main)

---

## Fonctionnalités non implémentées

- Modification des relations déjà établies (changer un parent existant)
- Fusion de deux arbres ou rattachement à un arbre existant via demande
- Gestion des mariages ou partenariats avec dates
- Ajout de frères/sœurs directement (sans passer par les parents)
- Statistiques
- Système de messagerie ou notifications entre membres (Souvenirs)

---

## Bibliothèques

- JavaFX (UI principale)
- Jakarta Mail (e-mail SMTP)
- JDBC (connexion MySQL)

---

## Dépendances externes

Placer dans le dossier `/lib` :

- `jakarta.mail-api-2.1.2.jar`
- `jakarta.activation-2.0.1.jar`
- `javafx-base`, `javafx-controls`, `javafx-graphics` (si non fournis par l'IDE)

---

## Auteurs

- Saidi Besma  
- Paimba-Sail Owen  
- Fowdar Heeshaam  
- Cupessala Alvariane  
- Ocloo Leslie
