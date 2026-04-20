package paie.model;

public interface IPaye {

    double calculerSalaireBrut();

    double calculerPrimeAnciennete();

    default double calculerCharges(double taux) {
        return (calculerSalaireBrut() + calculerPrimeAnciennete()) * taux;
    }

    default double calculerNetAPayer() {
        double brut = calculerSalaireBrut();
        double prime = calculerPrimeAnciennete();
        double charges = calculerCharges(0.20);
        return (brut + prime) - charges;
    }
}
