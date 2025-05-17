import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Utilisateur {
    private String nom;
    private String prenom;
    private int age;
    private String etat;
    private LocalDate naissance;
    private LocalDate mort;
    private String nationalite;
    private int codePublic;
    private int codePrive;
    private String numTel;
    private String mail;
    private String adresse;
    private String mdp;
    private Utilisateur [] parents;
    private Utilisateur [] fils;
    private Utilisateur [] partenaires;
    private boolean compte = false;
    private static final Map<String, Utilisateur> instances = new HashMap<>();

    Utilisateur(){
        etat = "vivant";
        naissance = LocalDate.now();
        compte = true;
    }

    Utilisateur(String nom, String prenom, int age, LocalDate naissance, String nationalite, String numTel, String mail, String adresse, String mdp) { //Constructeur de base lors de création de compte
        if(VerifEmail(mail)){
            System.err.println("Cet email est utilisé par un autre utilisateur, veuillez modifier votre email");
        }
        else if(VerifUtilisateur(nom,prenom,naissance)){
            System.err.println("Un utilisateur avec le nom, prénom et date de naissance que vous avez introduit existe déjà," +
                    "verifiez vos informations ou certainement vous avez déjà un compte utilisateur");
        }
        else {
            this.nom = nom;
            this.prenom = prenom;
            this.age = age;
            this.etat = "vivant";
            this.naissance = naissance;
            this.nationalite = nationalite;
            this.numTel = numTel;
            this.mail = mail;
            this.adresse = adresse;
            this.compte = true;
            this.mdp = mdp;
            instances.put(mail, this);
        }
    }

    Utilisateur(String nom, String prenom, int age, String etat, LocalDate naissance, LocalDate mort, String nationalite) { // Constructeur d'ajout d'un membre dans l'arbre généalogique avec date de mort
        if(VerifUtilisateur(nom,prenom,naissance)){
            System.out.println("L'utilisateur "+nom+" "+prenom+" existe");
        }
        else {
            this.nom = nom;
            this.prenom = prenom;
            if(age>122) {
                this.age = age;
            }
            else{
                System.err.println("L'âge de l'utilisateur "+nom+" "+prenom+"  introduit est supérieur à 122 et cela n'est pas possible");
                System.exit(0);
            }
            if(etat.equalsIgnoreCase("mort")) {
                this.etat = etat;
            }
            else{
                System.err.println("L'utilisateur "+nom+" "+prenom+" n'est pas mort, mais vous avez insérez une date de mort");
                System.exit(0);
            }
            this.naissance = naissance;
            this.nationalite = nationalite;
            this.mort = mort;   // la personne a une date de mort si seulement si elle est mort
            instances.put(" ", this);
        }
    }

    Utilisateur(String nom, String prenom, int age, String etat, LocalDate naissance, String nationalite) { // Constructeur d'ajout d'un membre dans l'arbre généalogique sans date de mort
        if(VerifUtilisateur(nom,prenom,naissance)){
            System.out.println("L'utilisateur "+nom+" "+prenom+" existe");
        }
        else {
            this.nom = nom;
            this.prenom = prenom;
            if(age>122) {
                this.age = age;
            }
            else{
                System.err.println("L'âge introduit est supérieur à 122 et cela n'est pas possible");
                System.exit(0);
            }
            if(etat.equalsIgnoreCase("mort")||etat.equalsIgnoreCase("vivant")) {
                this.etat = etat;
            }
            else{
                System.err.println("L'état de l'utilisateur "+nom+" "+prenom+" n'est pas vivant ni mort, réessayez");
                System.exit(0);
            }
            this.naissance = naissance;
            this.nationalite = nationalite;
            instances.put(" ", this);
        }
    }

    public static String Affichage(Utilisateur u){ // Affichage d'un utilisateur avec tous ces aspects
        return "Nom : " + u.nom + " ; Prénom : "+ u.prenom+" ; état de vie : "+u.etat+" ; Date de naissance : "+u.naissance+" ; Âge : "+u.age;
    }

    public static void Inst(){ // Affichage de toutes les instances de la classe
        int i=0;
        for(String instance : instances.keySet()){ // On parcours chaque instance
            i++;
            String email = instance.toString();
            Utilisateur u = instances.get(instance);
            System.out.println("Personne n°"+ i + ": Email : "+email + " ; " + Affichage(u));
        }
    }

    public static boolean VerifUtilisateur(String nom, String prenom, LocalDate naissance){ //Vérifie si un utilisateur existe
        for(String instance : instances.keySet()){
            Utilisateur u= instances.get(instance);
            if(u.nom==nom&&u.prenom==prenom&&u.naissance==naissance){
                return true;
            }
        }
        return false;
    }

    public static boolean VerifEmail(String email){ //Vérifie si l'email en paramètre est unique
        if (instances.containsKey(email)) {
            return true;
        }
        return false;
    }

    public void supprimerUtilisateur(String nom, String prenom, LocalDate naissance) { //Supprime un utilisateur
        boolean supp=false;
        for(String instance : instances.keySet()){
            Utilisateur u= instances.get(instance);
            if(u.nom==nom&&u.prenom==prenom&&u.naissance==naissance){
                instances.remove(u.mail,u);
                supp=true;

            }
        }
        if(supp){ System.out.println("La personne a été supprimé"); }
        else { System.err.println("La personne n'a pas été supprimé"); }
    }

    public String getNom() { //Renvoie le nom de l'utilisateur
        return this.nom;
    }

    public String getPrenom() { //Renvoie le prénom de l'utilisateur
        return this.prenom;
    }

    public int getAge() { //Renvoie l'âge de l'utilisateur
        return this.age;
    }

    public String getEtat() { //Renvoie l'état de vie de l'utilisateur
        return this.etat;
    }

    public LocalDate getNaissance() { //Renvoie la date de naissance de l'utilisateur
        return this.naissance;
    }

    public LocalDate getMort() { //Renvoie la date de mort de l'utilisateur
        return this.mort;
    }

    public String getNationalite() { //Renvoie la nationalité de l'utilisateur
        return this.nationalite;
    }

    public int getCodePublic() { //Renvoie le code public de l'utilisateur
        return this.codePublic;
    }

    public int getCodePrive() { //Renvoie le code privé de l'utilisateur
        return this.codePrive;
    }

    public String getNumTel() { //Renvoie le numéro téléphone de l'utilisateur
        return this.numTel;
    }

    public String getMail() { //Renvoie l'email de l'utilisateur
        return this.mail;
    }

    public String getAdresse() { //Renvoie l'adresse de l'utilisateur
        return this.adresse;
    }

    public String getMdp() { //Renvoie le mot de passe de l'utilisateur
        return this.mdp;
    }

    public void setNom(int age) { // Définir/Redéfinir le nom de l'utilisateur
        this.age = age;
    }

    public void setEtat(String etat) { // Définir/Redéfinir l'état de l'utilisateur
        if(etat.equalsIgnoreCase("mort")||etat.equalsIgnoreCase("vivant")) {
            this.etat = etat;
        }
        else{
            System.err.println("L'état de l'utilisateur "+nom+" "+prenom+" n'est pas vivant ni mort, réessayez");
        }
    }

    public void setNaissance(LocalDate naissance) { // Définir/Redéfinir la date de naissance de l'utilisateur
        this.naissance = naissance;
    }

    public void setMort(LocalDate mort) { // Définir/Redéfinir la date de mort de l'utilisateur
        this.mort = mort;
    }

    public void setNationalite(String nationalite) { // Définir/Redéfinir la date de naissance de l'utilisateur
        this.nationalite = nationalite;
    }

    public void setCodePublic(int codePublic) { // Définir/Redéfinir le code public de l'utilisateur
        this.codePublic = codePublic;
    }

    public void setCodePrive(int codePrive) { // Définir/Redéfinir le code privée de l'utilisateur
        this.codePrive = codePrive;
    }

    public void setNumTel(String numTel) { // Définir/Redéfinir le numéro téléphone de l'utilisateur
        this.numTel = numTel;
    }

    public void setMail(String mail) { // Définir/Redéfinir l'email de l'utilisateur
        this.mail = mail;
    }

    public void setAdresse(String adresse) { // Définir/Redéfinir l'adresse de l'utilisateur
        this.adresse = adresse;
    }

    public void setMdp(String mdp) { // Définir/Redéfinir le mot de passe de l'utilisateur
        this.mdp = mdp;
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