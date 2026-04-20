package paie.model;

import paie.exception.InvalidWorkDataException;

import java.time.LocalDate;

/**
 * Employé rémunéré à l'heure (intérimaire, vacation…).
 *
 * Règle de dépassement : les heures au-delà de 180h sont majorées à 125%.
 * Brut = (heuresNormales × taux) + (heuresSup × taux × 1.25)
 *
 * Le polymorphisme est exploité ici : calculerSalaireBrut() redéfini
 * intègre la majoration heures sup. L'interface IPaye appellera
 * CETTE méthode sans qu'aucun instanceof soit nécessaire.
 */
public class EmployeHoraire extends Employe {

    private static final double SEUIL_HEURES_NORMALES = 180.0;
    private static final double MAJORATION_SUP        = 1.25;
    private static final double PLAFOND_HEURES        = 240.0;

    private double tauxHoraire;
    private double heuresTravaillees;

    // -------------------------------------------------------
    // Constructeur avec double validation dès l'instanciation
    // -------------------------------------------------------

    public EmployeHoraire(int id, String nom, String email, String departement,
                          LocalDate dateEmbauche, double tauxHoraire, double heuresTravaillees)
            throws InvalidWorkDataException {

        super(id, nom, email, departement, dateEmbauche);

        // Validation du taux horaire (assimilé au SMIG mensuel / 191h légales)
        double salaireEstime = tauxHoraire * SEUIL_HEURES_NORMALES;
        validerSalaireBase(salaireEstime);  // lève exception si < 3000 DH

        // Validation du plafond légal des heures
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

    // -------------------------------------------------------
    // Implémentation de IPaye#calculerSalaireBrut()
    // -------------------------------------------------------

    /**
     * Brut = heures normales au taux normal
     *      + heures supplémentaires majorées à 25%.
     *
     * Cette redéfinition fait que calculerNetAPayer() de l'interface
     * applique automatiquement les règles propres aux horaires
     * sans aucun cast ni instanceof.
     */
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

    // -------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------

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
