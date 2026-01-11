package exception;

/*
 * Exception levée lorsque le solde de la caisse est insuffisant
 */
public class SoldeInsuffisantException extends Exception {

    public SoldeInsuffisantException(String message) {
        super(message);
    }
}
