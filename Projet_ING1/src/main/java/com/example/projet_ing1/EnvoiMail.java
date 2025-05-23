package com.example.projet_ing1;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;

import java.util.Properties;

public class EnvoiMail {

    private static final String EXPEDITEUR = "arbreconfirmationvalidation@gmail.com\n"; // remplace par ton Gmail
    private static final String NOM_EXPEDITEUR = "Arbre Généalogique Pro++";
    private static final String MDP_APPLICATION = "vwcsehztenbxrjtw"; // sans les espaces

    public static void envoyerCodes(String destinataire, String prenom, String codePublic, String codePrive) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EXPEDITEUR, MDP_APPLICATION);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EXPEDITEUR, NOM_EXPEDITEUR));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject("Vos codes d’accès – Arbre Généalogique");

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

            message.setText(corps);
            Transport.send(message);
            System.out.println("✅ Mail envoyé à " + destinataire);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}