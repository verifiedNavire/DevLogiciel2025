import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Souvenir {
    public String Publieur;
    public String Type;
    public File fichier;
    private static final Map<String, Souvenir> instances = new HashMap<>();

    public Souvenir(Utilisateur p, String type, File nomFichier){ // Constructeur de souvenir
        Publieur = p.getNom()+" "+p.getPrenom();
        Type = type;
        fichier = nomFichier;
        instances.put(Publieur, this);
    }

    public void Affichage(){ //Affichage
        for(String instance : instances.keySet()){
            String p = instance.toString();
            Souvenir s = instances.get(instance);
            System.out.println("Publieur : "+ p +" ; Type : "+ s.Type + " ; Nom du fichier :" + s.fichier.getName());
        }
    }

    public void supprimerSouvenir(String publieur, String nomfichier) {
        boolean supp=false;
        for(String instance : instances.keySet()){
            Souvenir f= instances.get(instance);
            if(f.Publieur==publieur&&f.fichier.getName()==nomfichier){
                instances.remove(f.Publieur,f);
                supp=true;
            }
        }
        if(supp){ System.out.println("La personne a été supprimé"); }
        else { System.err.println("La personne n'a pas été supprimé"); }
    }
}
