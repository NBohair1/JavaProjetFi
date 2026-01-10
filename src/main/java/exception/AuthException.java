package exception;

// Exception pour les erreurs d'authentification (login, inscription)
public class AuthException extends Exception {
    public AuthException(String message) {
        super(message);
    }
}
