package paie.model;

import paie.exception.InvalidWorkDataException;

import java.time.LocalDate;

public class EmployeFixe extends Employe {

    private double salaireBase;
    private double primePerformance;

    public EmployeFixe(int id, String nom, String email, String departement,
                       LocalDate dateEmbauche, double salaireBase, double primePerformance)
            throws InvalidWorkDataException {

        super(id, nom, email, departement, dateEmbauche);

        validerSalaireBase(salaireBase);

        this.salaireBase = salaireBase;
        this.primePerformance = primePerformance;
    }

    @Override
    public double calculerSalaireBrut() {
        return salaireBase + primePerformance;
    }

    public double getSalaireBase()                   { return salaireBase; }
    public void setSalaireBase(double s) throws InvalidWorkDataException {
        validerSalaireBase(s);
        this.salaireBase = s;
    }

    public double getPrimePerformance()              { return primePerformance; }
    public void setPrimePerformance(double p)        { this.primePerformance = p; }
}
