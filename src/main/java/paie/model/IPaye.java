package paie.model;

/**
 * Interface IPaye - Le Contrat Social
 * Définit les règles de calcul de paie pour tous les types d'employés.
 */
public interface IPaye {

    /**
     * Calcule le salaire brut selon le type de contrat.
     */
    double calculerSalaireBrut();

    /**
     * Calcule la prime d'ancienneté :
     * - 0%  si ancienneté < 2 ans
     * - 5%  si ancienneté ∈ [2, 5] ans
     * - 10% si ancienneté > 5 ans
     */
    double calculerPrimeAnciennete();

    /**
     * Calcule les charges sociales (CNSS/AMO) avec taux par défaut à 20%.
     * Peut être surchargée pour utiliser un taux différent.
     */
    default double calculerCharges(double taux) {
        return (calculerSalaireBrut() + calculerPrimeAnciennete()) * taux;
    }

    /**
     * Calcule le net à payer : (Brut + Prime) - Charges (à 20%).
     */
    default double calculerNetAPayer() {
        double brut = calculerSalaireBrut();
        double prime = calculerPrimeAnciennete();
        double charges = calculerCharges(0.20);
        return (brut + prime) - charges;
    }
}
