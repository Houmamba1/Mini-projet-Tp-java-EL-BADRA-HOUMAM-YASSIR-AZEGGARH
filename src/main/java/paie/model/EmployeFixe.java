package paie.model;

import paie.exception.InvalidWorkDataException;

import java.time.LocalDate;

/**
 * Employé sous contrat fixe (CDI).
 *
 * Le salaire brut = salaireBase + primePerformance.
 *
 * Le polymorphisme est exploité ici : calculerSalaireBrut() est redéfini
 * pour intégrer la prime de performance propre à ce type de contrat.
 * L'appel à calculerNetAPayer() (défini dans IPaye) déclenche automatiquement
 * CETTE implémentation de calculerSalaireBrut() sans aucun instanceof.
 */
public class EmployeFixe extends Employe {

    private double salaireBase;
    private double primePerformance;

    // -------------------------------------------------------
    // Constructeur avec validation dès l'instanciation
    // -------------------------------------------------------

    public EmployeFixe(int id, String nom, String email, String departement,
                       LocalDate dateEmbauche, double salaireBase, double primePerformance)
            throws InvalidWorkDataException {

        super(id, nom, email, departement, dateEmbauche);

        // Validation immédiate : garantit SMIG dès la création de l'objet
        validerSalaireBase(salaireBase);

        this.salaireBase = salaireBase;
        this.primePerformance = primePerformance;
    }

    // -------------------------------------------------------
    // Implémentation de IPaye#calculerSalaireBrut()
    // -------------------------------------------------------

    /**
     * Brut = salaire de base + prime de performance.
     * Cette redéfinition est le cœur du polymorphisme :
     * calculerNetAPayer() de l'interface appellera CETTE méthode.
     */
    @Override
    public double calculerSalaireBrut() {
        return salaireBase + primePerformance;
    }

    // -------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------

    public double getSalaireBase()                   { return salaireBase; }
    public void setSalaireBase(double s) throws InvalidWorkDataException {
        validerSalaireBase(s);
        this.salaireBase = s;
    }

    public double getPrimePerformance()              { return primePerformance; }
    public void setPrimePerformance(double p)        { this.primePerformance = p; }
}
