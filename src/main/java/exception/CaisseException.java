package exception;

// Exception pour les erreurs de gestion de caisse
public class CaisseException extends Exception {
    public CaisseException(String message) {
        super(message);
    }
}
