package exception;

// Exception pour les erreurs de gestion des clients
public class ClientException extends Exception {
    public ClientException(String message) {
        super(message);
    }
}

