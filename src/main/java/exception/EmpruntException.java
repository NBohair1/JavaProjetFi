package exception;

// Exception pour les erreurs de gestion des emprunts
public class EmpruntException extends Exception {
    public EmpruntException(String message) {
        super(message);
    }
}
