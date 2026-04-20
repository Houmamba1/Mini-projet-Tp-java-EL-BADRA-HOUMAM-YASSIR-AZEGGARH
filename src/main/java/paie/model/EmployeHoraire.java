package paie.model;

import paie.exception.InvalidWorkDataException;

import java.time.LocalDate;

public class EmployeHoraire extends Employe {

    private static final double SEUIL_HEURES_NORMALES = 180.0;
    private static final double MAJORATION_SUP        = 1.25;
    private static final double PLAFOND_HEURES        = 240.0;

    private double tauxHoraire;
    private double heuresTravaillees;

    public EmployeHoraire(int id, String nom, String email, String departement,
                          LocalDate dateEmbauche, double tauxHoraire, double heuresTravaillees)
            throws InvalidWorkDataException {

        super(id, nom, email, departement, dateEmbauche);

        double salaireEstime = tauxHoraire * SEUIL_HEURES_NORMALES;
        validerSalaireBase(salaireEstime);  

        if (heuresTravaillees > PLAFOND_HEURES) {
            throw new InvalidWorkDataException(
                "heuresTravaillees",
                heuresTravaillees,
                "Le nombre d'heures (" + heuresTravaillees + "h) dépasse le plafond légal de 240h."
            );
        }

        this.tauxHoraire = tauxHoraire;
        this.heuresTravaillees = heuresTravaillees;
    }

    @Override
    public double calculerSalaireBrut() {
        if (heuresTravaillees <= SEUIL_HEURES_NORMALES) {
            return heuresTravaillees * tauxHoraire;
        } else {
            double heuresNormales = SEUIL_HEURES_NORMALES;
            double heuresSup      = heuresTravaillees - SEUIL_HEURES_NORMALES;
            return (heuresNormales * tauxHoraire) + (heuresSup * tauxHoraire * MAJORATION_SUP);
        }
    }

    public double getTauxHoraire()         { return tauxHoraire; }
    public void setTauxHoraire(double t)   { this.tauxHoraire = t; }

    public double getHeuresTravaillees()   { return heuresTravaillees; }
    public void setHeuresTravaillees(double h) throws InvalidWorkDataException {
        if (h > PLAFOND_HEURES) {
            throw new InvalidWorkDataException(
                "heuresTravaillees", h,
                "Plafond légal de 240h dépassé : " + h + "h."
            );
        }
        this.heuresTravaillees = h;
    }
}
