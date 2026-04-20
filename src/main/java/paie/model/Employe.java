package paie.model;

import paie.exception.InvalidWorkDataException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public abstract class Employe implements IPaye {

    protected int id;
    protected String nom;
    protected String email;
    protected String departement;
    protected LocalDate dateEmbauche;

    public Employe(int id, String nom, String email, String departement, LocalDate dateEmbauche) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.departement = departement;
        this.dateEmbauche = dateEmbauche;
    }

    public int getAnciennete() {
        return (int) ChronoUnit.YEARS.between(dateEmbauche, LocalDate.now());
    }

    @Override
    public double calculerPrimeAnciennete() {
        int anciennete = getAnciennete();
        double brut = calculerSalaireBrut();  

        if (anciennete < 2) {
            return 0.0;
        } else if (anciennete <= 5) {
            return brut * 0.05;
        } else {
            return brut * 0.10;
        }
    }

    protected static void validerSalaireBase(double salaireBase) throws InvalidWorkDataException {
        if (salaireBase < 3000) {
            throw new InvalidWorkDataException(
                "salaireBase",
                salaireBase,
                "Le salaire de base (" + salaireBase + " DH) est inférieur au SMIG de 3000 DH."
            );
        }
    }

    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }

    public String getNom()                   { return nom; }
    public void setNom(String nom)           { this.nom = nom; }

    public String getEmail()                 { return email; }
    public void setEmail(String email)       { this.email = email; }

    public String getDepartement()           { return departement; }
    public void setDepartement(String d)     { this.departement = d; }

    public LocalDate getDateEmbauche()             { return dateEmbauche; }
    public void setDateEmbauche(LocalDate date)    { this.dateEmbauche = date; }

    @Override
    public String toString() {
        return String.format(
            "[%s] id=%d | nom=%-20s | dept=%-12s | ancienneté=%d an(s) | net=%.2f DH",
            getClass().getSimpleName(), id, nom, departement, getAnciennete(), calculerNetAPayer()
        );
    }
}
