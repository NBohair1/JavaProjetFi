package exception;

// Exception pour les erreurs de gestion des réparations
public class ReparationException extends Exception {
    public ReparationException(String message) {
        super(message);
    }
}
