import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Utilisateur u = new Utilisateur("FOWDAR", "Alvariane", 10, "homme", LocalDate.of(2004, 9, 15), "ANgolais", "+56 69582358", "jesuisfatigué@gmail.com", "Chez moi", "String mdp");
        Utilisateur u2 = new Utilisateur("FOWDAR", "Alvariane", 20, "femme", LocalDate.of(2004, 9, 15), "ANgolais", "+56 69582358", "jesuis@gmail.com", "Chez moi", "String mdp");
        Utilisateur u1 = new Utilisateur("FOWDAR", "Alvariane", 52, "femme", "mort", LocalDate.of(2004, 9, 15), "ANgolais");
        Utilisateur.Inst();
        }
}