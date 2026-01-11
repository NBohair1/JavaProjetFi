package metier;

import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import dao.*;
import exception.AuthException;

public class AuthMetierImpl implements IAuthMetier {
    
    private GenericDao dao = new GenericDao();

    @Override
    public Proprietaire inscription(String nom, String prenom, String email, String motDePasse) throws AuthException {
        EntityManager em = dao.getEntityManager(); // Utilise le DAO
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (dao.findOneByProperty(Proprietaire.class, "email", email) != null) 
                throw new AuthException("Cet email est deja utilise");
                
            Proprietaire p = new Proprietaire();
            p.setNom(nom); p.setPrenom(prenom); p.setEmail(email); p.setMdp(motDePasse); p.setPourcentageGain(100f);
            
            // Créer une caisse pour le propriétaire
            Caisse caisse = new Caisse();
            caisse.setSoldeActuel(0);
            caisse.setDernierMouvement(new Date());
            em.persist(caisse);
            p.setCaisse(caisse);
            
            em.persist(p);
            tx.commit();
            return p;
        } catch (Exception e) {
            if(tx.isActive()) tx.rollback();
            throw new AuthException("Erreur lors de la creation du compte");
        } finally {
            em.close();
        }
    }

    @Override
    public User login(String email, String motDePasse) throws AuthException {
        EntityManager em = dao.getEntityManager();
        EntityTransaction tx = null;
        try {
            // D'abord chercher dans Proprietaire avec chargement EAGER des boutiques
            List<Proprietaire> proprietaires = em.createQuery(
                "SELECT DISTINCT p FROM Proprietaire p LEFT JOIN FETCH p.boutiques LEFT JOIN FETCH p.caisse WHERE p.email=:e", 
                Proprietaire.class)
                .setParameter("e", email).getResultList();
            
            if (!proprietaires.isEmpty() && proprietaires.get(0).getMdp().equals(motDePasse)) {
                Proprietaire p = proprietaires.get(0);
                
                // S'assurer que le propriétaire a une caisse
                if (p.getCaisse() == null) {
                    tx = em.getTransaction();
                    tx.begin();
                    Proprietaire pManaged = em.find(Proprietaire.class, p.getId());
                    Caisse caisse = new Caisse();
                    caisse.setSoldeActuel(0);
                    caisse.setDernierMouvement(new Date());
                    em.persist(caisse);
                    pManaged.setCaisse(caisse);
                    em.merge(pManaged);
                    tx.commit();
                    p.setCaisse(caisse);
                }
                
                return p;
            }
            
            // Sinon chercher dans Reparateur
            List<Reparateur> reparateurs = em.createQuery(
                "SELECT r FROM Reparateur r LEFT JOIN FETCH r.caisse WHERE r.email=:e", 
                Reparateur.class)
                .setParameter("e", email).getResultList();
            
            if (!reparateurs.isEmpty() && reparateurs.get(0).getMdp().equals(motDePasse)) {
                Reparateur r = reparateurs.get(0);
                
                // S'assurer que le réparateur a une caisse
                if (r.getCaisse() == null) {
                    tx = em.getTransaction();
                    tx.begin();
                    Reparateur rManaged = em.find(Reparateur.class, r.getId());
                    Caisse caisse = new Caisse();
                    caisse.setSoldeActuel(0);
                    caisse.setDernierMouvement(new Date());
                    em.persist(caisse);
                    rManaged.setCaisse(caisse);
                    em.merge(rManaged);
                    tx.commit();
                    r.setCaisse(caisse);
                }
                
                return r;
            }
            
            throw new AuthException("Email ou mot de passe incorrect");
            
        } catch (AuthException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new AuthException("Impossible de se connecter avec ces identifiants");
        } finally {
            em.close();
        }
    }

    @Override
    public Boutique creerBoutique(Proprietaire p, String nom, String adr, String tel, String pat) throws AuthException {
        EntityManager em = dao.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Boutique b = new Boutique();
            b.setNom(nom); b.setAdresse(adr); b.setNumTelephone(tel); b.setNumP(pat); b.setProprietaire(p);
            em.persist(b);
            
            Proprietaire pManaged = em.find(Proprietaire.class, p.getId());
            if(pManaged.getCaisse() == null) {
                Caisse c = new Caisse();
                c.setSoldeActuel(0);
                c.setDernierMouvement(new Date());
                em.persist(c);
                pManaged.setCaisse(c);
            }
            tx.commit();
            return b;
        } catch (Exception e) {
            if(tx.isActive()) tx.rollback();
            throw new AuthException("Impossible de créer la boutique");
        } finally {
            em.close();
        }
    }

    @Override
    public Caisse creerCaisseProprietaire(Proprietaire p) throws AuthException {
        return new Caisse(); 
    }
}