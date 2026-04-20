package paie.main;

import paie.exception.InvalidWorkDataException;
import paie.model.*;

import java.time.LocalDate;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║       SYSTÈME EXPERT DE GESTION DE PAIE — Demo POO           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        List<Employe> employes = new ArrayList<>();

        System.out.println("── Création des employés ──────────────────────────────────────");

        try {
            EmployeFixe ali = new EmployeFixe(
                1, "Ali Benali", "ali@uca.ma", "IT",
                LocalDate.now().minusYears(7),
                8000.0,   
                1500.0    
            );
            employes.add(ali);
            System.out.println("✔ Créé : " + ali.getNom());
        } catch (InvalidWorkDataException e) {
            System.err.println("✘ Erreur : " + e.getMessage());
        }

        try {
            EmployeFixe fatima = new EmployeFixe(
                2, "Fatima Zahra", "fatima@uca.ma", "RH",
                LocalDate.now().minusYears(3),
                5500.0,
                800.0
            );
            employes.add(fatima);
            System.out.println("✔ Créé : " + fatima.getNom());
        } catch (InvalidWorkDataException e) {
            System.err.println("✘ Erreur : " + e.getMessage());
        }

        try {
            EmployeHoraire youssef = new EmployeHoraire(
                3, "Youssef Tahiri", "youssef@uca.ma", "Logistique",
                LocalDate.now().minusYears(1),
                30.0,   
                200.0   
            );
            employes.add(youssef);
            System.out.println("✔ Créé : " + youssef.getNom());
        } catch (InvalidWorkDataException e) {
            System.err.println("✘ Erreur : " + e.getMessage());
        }

        try {
            EmployeHoraire karima = new EmployeHoraire(
                4, "Karima Idrissi", "karima@uca.ma", "IT",
                LocalDate.now().minusYears(5),
                25.0,
                180.0  
            );
            employes.add(karima);
            System.out.println("✔ Créé : " + karima.getNom());
        } catch (InvalidWorkDataException e) {
            System.err.println("✘ Erreur : " + e.getMessage());
        }

        System.out.println("\n── Tests de validation (erreurs intentionnelles) ──────────────");

        try {
            new EmployeFixe(99, "Test SMIG", "test@test.ma", "IT",
                LocalDate.now().minusYears(1), 2500.0, 0.0);  
        } catch (InvalidWorkDataException e) {
            System.out.println("✔ Erreur interceptée (SMIG) : " + e.getMessage());
        }

        try {
            new EmployeHoraire(99, "Test Heures", "h@test.ma", "IT",
                LocalDate.now().minusYears(1), 30.0, 250.0);  
        } catch (InvalidWorkDataException e) {
            System.out.println("✔ Erreur interceptée (heures) : " + e.getMessage());
        }

        System.out.println("\n── Rapport de paie (polymorphisme) ────────────────────────────");
        System.out.printf("%-25s %-12s %8s %8s %10s %10s %12s%n",
            "Nom", "Département", "Brut", "Ancien.", "Prime", "Charges", "NET");
        System.out.println("─".repeat(95));

        for (Employe emp : employes) {

            System.out.printf("%-25s %-12s %8.2f %7dan %9.2f %10.2f %12.2f DH%n",
                emp.getNom(),
                emp.getDepartement(),
                emp.calculerSalaireBrut(),
                emp.getAnciennete(),
                emp.calculerPrimeAnciennete(),
                emp.calculerCharges(0.20),
                emp.calculerNetAPayer()
            );
        }

        System.out.println("\n── Rapport décisionnel : Masse salariale par département ───────");

        Map<String, Double> masseSalariale = new TreeMap<>();
        for (Employe emp : employes) {
            masseSalariale.merge(emp.getDepartement(), emp.calculerNetAPayer(), Double::sum);
        }

        System.out.printf("%-20s %15s%n", "Département", "Masse nette (DH)");
        System.out.println("─".repeat(40));

        double totalGlobal = 0;
        for (Map.Entry<String, Double> entry : masseSalariale.entrySet()) {
            System.out.printf("%-20s %15.2f DH%n", entry.getKey(), entry.getValue());
            totalGlobal += entry.getValue();
        }
        System.out.println("─".repeat(40));
        System.out.printf("%-20s %15.2f DH%n", "TOTAL GLOBAL", totalGlobal);

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                     FIN DE LA DÉMONSTRATION                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
}
