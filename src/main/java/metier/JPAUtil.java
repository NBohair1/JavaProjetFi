package metier;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * ============================================================================
 * CLASSE JPAUTIL - UTILITAIRE DE CONNEXION JPA (SINGLETON)
 * ============================================================================
 * 
 * Cette classe utilitaire gère la création et la configuration de la connexion
 * JPA/Hibernate vers la base de données MySQL.
 * 
 * PATRON DE CONCEPTION: Singleton
 * - Une seule instance de JPAUtil existe dans toute l'application
 * - Le constructeur est privé pour empêcher l'instanciation directe
 * - Accès via getInstance() qui retourne toujours la même instance
 * 
 * AVANTAGES DU SINGLETON:
 * - Économie de ressources (une seule connexion)
 * - Point d'accès centralisé à la base de données
 * - Facilite la gestion du cycle de vie de l'EntityManagerFactory
 * 
 * CONFIGURATION:
 * - "repairPU" référence l'unité de persistance dans persistence.xml
 * - persistence.xml contient les paramètres de connexion MySQL
 * 
 * @author Équipe Repair Shop
 * @version 1.0
 */
public class JPAUtil {

    /** Instance unique du singleton (créée au chargement de la classe) */
    private static final JPAUtil instance = new JPAUtil();
    
    /** Factory JPA pour créer les EntityManagers */
    private final EntityManagerFactory emf;

    /**
     * Constructeur privé - empêche l'instanciation externe
     * Initialise la connexion à la base de données via JPA
     */
    private JPAUtil() {
        // Crée la factory à partir de la configuration persistence.xml
        this.emf = Persistence.createEntityManagerFactory("repairPU");
    }

    /**
     * Point d'accès unique à l'instance du singleton
     * @return L'instance unique de JPAUtil
     */
    public static JPAUtil getInstance() {
        return instance;
    }

    /**
     * Crée un nouvel EntityManager pour interagir avec la base
     * IMPORTANT: L'appelant doit fermer l'EntityManager après utilisation
     * @return Un EntityManager prêt à l'emploi
     */
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Ferme proprement l'EntityManagerFactory
     * À appeler à la fermeture de l'application pour libérer les ressources
     */
    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
