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
import exception.SoldeInsuffisantException;

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
                throw new EmpruntException("Réparateur requis pour effectuer un emprunt");

            if (montant <= 0)
                throw new EmpruntException("Montant d'emprunt invalide (doit être positif): " + montant + " DH");

            if (type == null || type.trim().isEmpty())
                throw new EmpruntException("Type d'emprunt requis (SORTIE ou ENTREE)");

            // Vérifier que le type est valide
            String typeNormalise = type.trim().toUpperCase();
            if (!"SORTIE".equals(typeNormalise) && !"ENTREE".equals(typeNormalise))
                throw new EmpruntException("Type d'emprunt invalide: '" + type + "'. Types autorisés: SORTIE, ENTREE");

            // IMPORTANT : Recharger le réparateur depuis la BD pour avoir les données à jour
            Reparateur reparateurManaged = em.find(Reparateur.class, reparateur.getId());
            if (reparateurManaged == null)
                throw new EmpruntException("Réparateur introuvable (ID: " + reparateur.getId() + ")");
            
            Caisse caisse = reparateurManaged.getCaisse();
            if (caisse == null)
                throw new EmpruntException("Caisse inexistante");
            
            // VALIDATION : Pour emprunt SORTANT (je prête), vérifier la caisse réelle
            if ("SORTIE".equalsIgnoreCase(type)) {
                // Recharger la caisse pour avoir le solde actuel
                Caisse caisseActuelle = em.find(Caisse.class, caisse.getIdCaisse());
                float caisseSysteme = caisseActuelle != null ? caisseActuelle.getSoldeActuel() : 0;
                
                // Calculer les emprunts SORTIE actifs
                List<Emprunt> emprunts = listerEmpruntsParReparateur(reparateurManaged);
                float empruntsSortieActifs = 0;
                for (Emprunt emp : emprunts) {
                    if (!emp.isRembourse() && "SORTIE".equalsIgnoreCase(emp.getType())) {
                        empruntsSortieActifs += emp.getMontant();
                    }
                }
                
                float caisseReelle = caisseSysteme - empruntsSortieActifs;
                
                if (montant > caisseReelle) {
                    throw new EmpruntException("Solde insuffisant : montant demandé (" + montant + 
                        " DH) dépasse la caisse réelle (" + caisseReelle + " DH)");
                }
            }

            Emprunt e = new Emprunt();
            e.setReparateur(reparateurManaged);
            e.setMontant(montant);
            e.setType(type);
            e.setCommentaire(commentaire);
            e.setDate(new Date());

            em.persist(e);

            // IMPORTANT : La caisse système (soldeActuel) N'EST PAS modifiée lors d'emprunts
            // Seuls les mouvements commerciaux modifient la caisse système
            // La caisse réelle se calcule dynamiquement : Caisse système - emprunts SORTIE actifs
            
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
            throw new EmpruntException("Réparateur obligatoire");

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
                throw new EmpruntException("Emprunt invalide");

            if (emprunt.isRembourse())
                throw new EmpruntException("Emprunt déjà remboursé");

            Reparateur reparateur = emprunt.getReparateur();
            if (reparateur == null)
                throw new EmpruntException("Réparateur invalide");

            Caisse caisse = reparateur.getCaisse();
            if (caisse == null)
                throw new EmpruntException("Caisse inexistante");

            // IMPORTANT : Le remboursement NE modifie PAS la caisse système (soldeActuel)
            // Il marque juste l'emprunt comme remboursé
            // La caisse réelle se recalcule automatiquement
            
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
        // Cette méthode calcule la caisse système (identique à soldeActuel)
        if (reparateur == null)
            throw new EmpruntException("Réparateur obligatoire");

        if (reparateur.getCaisse() != null) {
            return reparateur.getCaisse().getSoldeActuel();
        }
        return 0;
    }
    
    // Méthode privée pour calculer la caisse réelle
    private float calculerCaisseReelle(Reparateur reparateur) throws EmpruntException {
        // Caisse réelle = Caisse système - emprunts SORTIE non remboursés
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
