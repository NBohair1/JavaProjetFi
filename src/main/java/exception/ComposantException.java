package exception;

/**
 * Exception levée lors d'erreurs liées à la gestion des composants
 * (stock insuffisant, création, modification, suppression de composants)
 * 
 * @author Repair Shop Team
 */
public class ComposantException extends Exception {

    /**
     * Constructeur avec message d'erreur
     * @param message Description claire de l'erreur pour l'utilisateur
     */
    public ComposantException(String message) {
        super(message);
    }
}

