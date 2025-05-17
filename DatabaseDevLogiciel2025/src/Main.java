import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Utilisateur u = new Utilisateur("FOWDAR", "Alvariane", 552, LocalDate.of(2004, 9, 15), "ANgolais", "+56 69582358", "jesuisfatigué@gmail.com", "Chez moi", "String mdp");
        Utilisateur u2 = new Utilisateur("FOWDAR", "Alvariane", 552, LocalDate.of(2004, 9, 15), "ANgolais", "+56 69582358", "jesuisfatigué@gmail.com", "Chez moi", "String mdp");
        Utilisateur u1 = new Utilisateur("FOWDAR", "Alvariane", 52, "mort", LocalDate.of(2004, 9, 15), "ANgolais");
        Utilisateur.Inst();
        }
}