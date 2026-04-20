package paie.exception;

/**
 * Exception checked levée lorsque les données de travail sont invalides :
 * - heuresTravaillees > 240 (plafond légal)
 * - salaireBase < 3000 DH (SMIG marocain)
 */
public class InvalidWorkDataException extends Exception {

    private final String field;
    private final double value;

    public InvalidWorkDataException(String message) {
        super(message);
        this.field = "inconnu";
        this.value = 0;
    }

    public InvalidWorkDataException(String field, double value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "InvalidWorkDataException [champ=" + field + ", valeur=" + value + "] : " + getMessage();
    }
}
