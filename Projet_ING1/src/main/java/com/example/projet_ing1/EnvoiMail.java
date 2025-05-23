package com.example.projet_ing1;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;

import java.util.Properties;

/**
 * Classe utilitaire pour envoyer un e-mail de validation à un nouvel utilisateur.
 * Utilise Jakarta Mail pour configurer une session SMTP sécurisée avec Gmail.
 */
public class EnvoiMail {

    // Adresse e-mail utilisée comme expéditeur (doit être associée à un mot de passe d'application)
    private static final String EXPEDITEUR = "arbreconfirmationvalidation@gmail.com"; // à adapter
    private static final String NOM_EXPEDITEUR = "Arbre Généalogique Pro++";

    // Mot de passe d'application Gmail (généré dans les paramètres de sécurité Google)
    private static final String MDP_APPLICATION = "vwcsehztenbxrjtw";

    /**
     * Envoie un e-mail contenant les identifiants de connexion à un utilisateur nouvellement validé.
     *
     * @param destinataire l'adresse e-mail du destinataire
     * @param prenom       le prénom de l’utilisateur
     * @param codePublic   le code public d’identification
     * @param codePrive    le code privé (ID personnel ou confidentiel)
     */
    public static void envoyerCodes(String destinataire, String prenom, String codePublic, String codePrive) {
        // Configuration des propriétés SMTP pour Gmail
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true"); // authentification requise
        props.put("mail.smtp.starttls.enable", "true"); // chiffrement TLS
        props.put("mail.smtp.host", "smtp.gmail.com"); // serveur SMTP
        props.put("mail.smtp.port", "587"); // port SMTP TLS

        // Création de la session mail avec authentification
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EXPEDITEUR, MDP_APPLICATION);
            }
        });

        try {
            // Création du message mail
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EXPEDITEUR, NOM_EXPEDITEUR)); // nom d’expéditeur affiché
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject("Vos codes d’accès – Arbre Généalogique");

            // Corps du mail avec codes formatés
            String corps = """
                    Bonjour %s,

                    Votre inscription a été validée avec succès.

                    Voici vos identifiants :
                    - Code public : %s
                    - Code privé  : %s
                    - Mot de passe initial : %s

                    Vous pouvez vous connecter sur l'application.
                    Pensez à modifier votre mot de passe dès la première connexion.

                    Bien à vous,
                    L'équipe Arbre Généalogique Pro++
                    """.formatted(prenom, codePublic, codePrive, prenom);

            message.setText(corps); // contenu brut en texte

            // Envoi du message via le transport SMTP
            Transport.send(message);
            System.out.println("✅ Mail envoyé à " + destinataire);

        } catch (Exception e) {
            e.printStackTrace(); // en cas d'échec (connexion SMTP, erreur d'adresse, etc.)
        }
    }
}
