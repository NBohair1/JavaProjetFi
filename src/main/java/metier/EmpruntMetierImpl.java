package metier;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import dao.Caisse;
import dao.Emprunt;
import dao.GenericDao;
import dao.Reparateur;
import exception.EmpruntException;

public class EmpruntMetierImpl implements IEmpruntMetier {

    private GenericDao dao = new GenericDao();

    @Override
    public Emprunt creerEmprunt(
            Reparateur reparateur,
            float montant,
            String type,
            String commentaire
    ) throws EmpruntException {

        EntityManager em = JPAUtil.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            if (reparateur == null)
                throw new EmpruntException("Reparateur non selectionne");

            if (montant <= 0)
                throw new EmpruntException("Le montant doit etre positif");

            if (type == null || type.trim().isEmpty())
                throw new EmpruntException("Type d'emprunt requis");

            String typeNormalise = type.trim().toUpperCase();
            if (!"SORTIE".equals(typeNormalise) && !"ENTREE".equals(typeNormalise))
                throw new EmpruntException("Type invalide: utilisez SORTIE ou ENTREE");

            Reparateur reparateurManaged = em.find(Reparateur.class, reparateur.getId());
            if (reparateurManaged == null)
                throw new EmpruntException("Reparateur introuvable");
            
            Caisse caisse = reparateurManaged.getCaisse();
            if (caisse == null)
                throw new EmpruntException("Caisse inexistante");
            
            // Verifier le solde pour emprunt SORTIE
            if ("SORTIE".equalsIgnoreCase(type)) {
                Caisse caisseActuelle = em.find(Caisse.class, caisse.getIdCaisse());
                float caisseSysteme = caisseActuelle != null ? caisseActuelle.getSoldeActuel() : 0;
                
                List<Emprunt> emprunts = listerEmpruntsParReparateur(reparateurManaged);
                float empruntsSortieActifs = 0;
                for (Emprunt emp : emprunts) {
                    if (!emp.isRembourse() && "SORTIE".equalsIgnoreCase(emp.getType())) {
                        empruntsSortieActifs += emp.getMontant();
                    }
                }
                
                float caisseReelle = caisseSysteme - empruntsSortieActifs;
                
                if (montant > caisseReelle) {
                    throw new EmpruntException("Solde insuffisant");
                }
            }

            Emprunt e = new Emprunt();
            e.setReparateur(reparateurManaged);
            e.setMontant(montant);
            e.setType(type);
            e.setCommentaire(commentaire);
            e.setDate(new Date());

            em.persist(e);

            // La caisse systeme n'est pas modifiee par les emprunts
            
            tx.commit();
            return e;
        } catch (EmpruntException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new EmpruntException("Erreur lors de la création de l'emprunt");
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<Emprunt> listerEmpruntsParReparateur(Reparateur reparateur)
            throws EmpruntException {

        if (reparateur == null)
            throw new EmpruntException("Reparateur non selectionne");

        return dao.findByProperty(Emprunt.class, "reparateur", reparateur);
    }
    @Override
    public void rembourserEmprunt(Emprunt emprunt)
            throws EmpruntException {

        EntityManager em = JPAUtil.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            if (emprunt == null)
                throw new EmpruntException("Emprunt non selectionne");

            if (emprunt.isRembourse())
                throw new EmpruntException("Cet emprunt est deja rembourse");

            Reparateur reparateur = emprunt.getReparateur();
            if (reparateur == null)
                throw new EmpruntException("Reparateur introuvable");

            Caisse caisse = reparateur.getCaisse();
            if (caisse == null)
                throw new EmpruntException("Caisse inexistante");
            
            emprunt.setRembourse(true);
            emprunt.setDateRemboursement(new Date());

            dao.update(emprunt);
            
            tx.commit();
        } catch (EmpruntException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new EmpruntException("Erreur lors du remboursement de l'emprunt");
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public float calculerSoldeAvecEmprunts(Reparateur reparateur)
            throws EmpruntException {
        if (reparateur == null)
            throw new EmpruntException("Reparateur non selectionne");

        if (reparateur.getCaisse() != null) {
            return reparateur.getCaisse().getSoldeActuel();
        }
        return 0;
    }
    
    // Calcul caisse reelle = systeme - emprunts SORTIE actifs
    private float calculerCaisseReelle(Reparateur reparateur) throws EmpruntException {
        float caisseSysteme = 0;
        if (reparateur.getCaisse() != null) {
            caisseSysteme = reparateur.getCaisse().getSoldeActuel();
        }
        
        List<Emprunt> emprunts = listerEmpruntsParReparateur(reparateur);
        float empruntsSortieActifs = 0;
        
        for (Emprunt e : emprunts) {
            if (!e.isRembourse() && "SORTIE".equalsIgnoreCase(e.getType())) {
                empruntsSortieActifs += e.getMontant();
            }
        }
        
        return caisseSysteme - empruntsSortieActifs;
    }
}
