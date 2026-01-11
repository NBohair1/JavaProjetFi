package metier;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

// Singleton pour gerer la connexion JPA/Hibernate
public class JPAUtil {

    private static final JPAUtil instance = new JPAUtil();
    private final EntityManagerFactory emf;

    private JPAUtil() {
        this.emf = Persistence.createEntityManagerFactory("repairPU");
    }

    public static JPAUtil getInstance() {
        return instance;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
	