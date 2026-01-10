package metier;

import dao.Boutique;
import dao.Composant;
import dao.GenericDao;
import dao.Reparateur;
import exception.CaisseException;
import exception.ComposantException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public class ComposantMetierImpl implements IComposantMetier {

    private GenericDao dao = new GenericDao();
    private ICaisseMetier caisseMetier = new CaisseMetierImpl();

    @Override
    public void ajouterComposant(Composant composant, Reparateur reparateur) throws ComposantException {
        EntityManager em = dao.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            // Calculer coût total d'achat
            float coutTotal = composant.getPrixAchat() * composant.getQuantite();
            
            // Déduire de la caisse du réparateur
            if (reparateur != null && coutTotal > 0) {
                try {
                    caisseMetier.retirerCaisse(reparateur, coutTotal, 
                        "Achat composant: " + composant.getNom() + " (x" + composant.getQuantite() + ")");
                } catch (CaisseException e) {
                    throw new ComposantException("Caisse insuffisante pour acheter ce composant");
                }
            }
            
            em.persist(composant);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new ComposantException("Impossible d'ajouter le composant");
        } finally {
            em.close();
        }
    }

    @Override
    public void modifierComposant(Composant composant) throws ComposantException {
        EntityManager em = dao.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(composant);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new ComposantException("Impossible de modifier le composant");
        } finally {
            em.close();
        }
    }

    @Override
    public void supprimerComposant(Long id) throws ComposantException {
        EntityManager em = dao.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Composant c = em.find(Composant.class, id);
            if (c != null) {
                em.remove(c);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new ComposantException("Impossible de supprimer le composant");
        } finally {
            em.close();
        }
    }

    @Override
    public Composant chercherComposant(Long id) throws ComposantException {
        EntityManager em = dao.getEntityManager();
        try {
            return em.find(Composant.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Composant> listerComposants() throws ComposantException {
        EntityManager em = dao.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Composant c", Composant.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Composant> listerComposantsParBoutique(Boutique boutique) throws ComposantException {
        if (boutique == null) {
            throw new ComposantException("Boutique obligatoire");
        }
        
        EntityManager em = dao.getEntityManager();
        try {
            // Pour l'instant, retourner tous les composants car ils ne sont pas liés directement aux boutiques
            // Dans une implémentation plus avancée, on pourrait avoir un stock par boutique
            return em.createQuery("SELECT c FROM Composant c ORDER BY c.nom", Composant.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Composant> chercherComposantsParNom(String nom) throws ComposantException {
        EntityManager em = dao.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Composant c WHERE c.nom LIKE :nom", Composant.class)
                    .setParameter("nom", "%" + nom + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
