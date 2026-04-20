package paie.main;

import paie.exception.InvalidWorkDataException;
import paie.model.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Point d'entrée principal.
 * Démontre toutes les fonctionnalités sans nécessiter de base de données.
 * (Pour tester avec JDBC, décommenter la section DAO en bas.)
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║       SYSTÈME EXPERT DE GESTION DE PAIE — Demo POO           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        List<Employe> employes = new ArrayList<>();

        // =======================================================
        // 1. CRÉATION DES EMPLOYÉS (validation dès l'instanciation)
        // =======================================================

        System.out.println("── Création des employés ──────────────────────────────────────");

        // Employé fixe — embauché il y a 7 ans → prime 10%
        try {
            EmployeFixe ali = new EmployeFixe(
                1, "Ali Benali", "ali@uca.ma", "IT",
                LocalDate.now().minusYears(7),
                8000.0,   // salaire de base
                1500.0    // prime de performance
            );
            employes.add(ali);
            System.out.println("✔ Créé : " + ali.getNom());
        } catch (InvalidWorkDataException e) {
            System.err.println("✘ Erreur : " + e.getMessage());
        }

        // Employé fixe — embauché il y a 3 ans → prime 5%
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

        // Employé horaire — avec heures supplémentaires
        try {
            EmployeHoraire youssef = new EmployeHoraire(
                3, "Youssef Tahiri", "youssef@uca.ma", "Logistique",
                LocalDate.now().minusYears(1),
                30.0,   // taux horaire
                200.0   // 20h de sup par rapport à 180h normales
            );
            employes.add(youssef);
            System.out.println("✔ Créé : " + youssef.getNom());
        } catch (InvalidWorkDataException e) {
            System.err.println("✘ Erreur : " + e.getMessage());
        }

        // Employé horaire — embauché il y a 5 ans → prime 5%
        try {
            EmployeHoraire karima = new EmployeHoraire(
                4, "Karima Idrissi", "karima@uca.ma", "IT",
                LocalDate.now().minusYears(5),
                25.0,
                180.0  // exactement 180h — pas de sup
            );
            employes.add(karima);
            System.out.println("✔ Créé : " + karima.getNom());
        } catch (InvalidWorkDataException e) {
            System.err.println("✘ Erreur : " + e.getMessage());
        }

        // =======================================================
        // 2. TEST DE VALIDATION — Erreurs intentionnelles
        // =======================================================

        System.out.println("\n── Tests de validation (erreurs intentionnelles) ──────────────");

        // Test 1 : salaire sous le SMIG
        try {
            new EmployeFixe(99, "Test SMIG", "test@test.ma", "IT",
                LocalDate.now().minusYears(1), 2500.0, 0.0);  // < 3000 DH
        } catch (InvalidWorkDataException e) {
            System.out.println("✔ Erreur interceptée (SMIG) : " + e.getMessage());
        }

        // Test 2 : heures dépassant le plafond légal
        try {
            new EmployeHoraire(99, "Test Heures", "h@test.ma", "IT",
                LocalDate.now().minusYears(1), 30.0, 250.0);  // > 240h
        } catch (InvalidWorkDataException e) {
            System.out.println("✔ Erreur interceptée (heures) : " + e.getMessage());
        }

        // =======================================================
        // 3. RAPPORT DE PAIE — Polymorphisme en action
        // =======================================================

        System.out.println("\n── Rapport de paie (polymorphisme) ────────────────────────────");
        System.out.printf("%-25s %-12s %8s %8s %10s %10s %12s%n",
            "Nom", "Département", "Brut", "Ancien.", "Prime", "Charges", "NET");
        System.out.println("─".repeat(95));

        for (Employe emp : employes) {
            // calculerNetAPayer() est appelé polymorphiquement :
            // - EmployeFixe  → brut = base + perf
            // - EmployeHoraire → brut intègre les heures sup majorées
            // Aucun instanceof n'est utilisé ici.
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

        // =======================================================
        // 4. MASSE SALARIALE PAR DÉPARTEMENT
        // =======================================================

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

        // =======================================================
        // 5. SECTION JDBC (décommenter si base de données disponible)
        // =======================================================
        /*
        System.out.println("\n── Persistance JDBC ────────────────────────────────────────────");
        try {
            EmployeDAO dao = new EmployeDAO();

            // Création de la table
            dao.getConnection().createStatement()
               .execute(DatabaseConnection.getCreateTableSQL());

            // Sauvegarde des employés
            for (Employe emp : employes) {
                dao.save(emp);
            }

            // Récupération (instanciation dynamique selon colonne 'type')
            System.out.println("\nTous les employés depuis la BDD :");
            dao.findAll().forEach(System.out::println);

            // Rapport par département via SQL GROUP BY
            System.out.println("\nMasse salariale par département (SQL) :");
            dao.getMasseSalarialeParDeptSQL().forEach((dept, masse) ->
                System.out.printf("  %-15s → %10.2f DH%n", dept, masse));

            // Augmentation de 5% pour les fixes
            dao.augmenterSalaireBase(0.05);

        } catch (SQLException e) {
            System.err.println("[JDBC] Erreur de connexion : " + e.getMessage());
            System.err.println("Vérifiez que MySQL tourne sur localhost:3306 avec la base 'paie_db'.");
        }
        */

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                     FIN DE LA DÉMONSTRATION                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
}
