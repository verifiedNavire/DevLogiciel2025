import java.time.LocalDate;

public class Arbre{
    public Utilisateur personne;
    private Utilisateur [] parents;
    private Utilisateur [] fils;
    private Utilisateur [] partenaires;
    private static int nbvisiteur;
    private String visibilite;
    private static LocalDate datevisite;

    public Arbre(){
        nbvisiteur=0;
        visibilite="public";
    }

    public static void Visite(){ //Méthode qui ajoute le nombre de visite après chaque visiteur
        datevisite = LocalDate.now();
        nbvisiteur++;
    }

    public void ChangerVision(String visible){//Méthode qui change la visibilité d'un noeud
        String vision ="";
        if(visibilite.equalsIgnoreCase("Public")){
            vision = "Public";
        }
        else if(visibilite.equalsIgnoreCase("Private")){
            vision = "Private";
        }
        if(visible.equalsIgnoreCase(visibilite)){
            System.out.println("Cette visbilité est déjà celui de ce noeud");
        }
        else{
            visibilite = vision;
        }
    }

    public void suppPersonne(Utilisateur p){ // Suppression d'utilisateur
        p.supprimerUtilisateur(p.getNom(),p.getPrenom(),p.getNaissance());
    }
    public void setParents(Utilisateur[] parents) { // Définir/Redéfinir le ou les parent(s) de l'utilisateur
        this.parents = parents;
    }

    public void setFils(Utilisateur[] fils) { // Définir/Redéfinir le ou les fils de l'utilisateur
        this.fils = fils;
    }

    public void setPartenaires(Utilisateur[] partenaires) { // Définir/Redéfinir le ou les partenaire(s) de l'utilisateur
        this.partenaires = partenaires;
    }
}
