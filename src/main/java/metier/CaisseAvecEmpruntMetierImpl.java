package metier;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import dao.CaisseAvecEmprunt;
import dao.Emprunt;
import dao.GenericDao;
import dao.Reparateur;
import exception.CaisseException;
import exception.EmpruntException;

/**
 * Implémentation de ICaisseAvecEmpruntMetier
 * Hérite de CaisseMetierImpl pour les opérations de base
 * et ajoute la gestion des entrées/sorties d'emprunt
 */
public class CaisseAvecEmpruntMetierImpl extends CaisseMetierImpl implements ICaisseAvecEmpruntMetier {
    
    private final GenericDao dao = new GenericDao();
    private final IEmpruntMetier empruntMetier = new EmpruntMetierImpl();
    
    @Override
    public Emprunt creerEmpruntSortie(Reparateur reparateur, float montant, String description) throws EmpruntException {
        return empruntMetier.creerEmprunt(reparateur, montant, "SORTIE", description);
    }
    
    @Override
    public Emprunt creerEmpruntEntree(Reparateur reparateur, float montant, String description) throws EmpruntException {
        return empruntMetier.creerEmprunt(reparateur, montant, "ENTREE", description);
    }
    
    @Override
    public void rembourserEmprunt(Long idEmprunt, Reparateur reparateur) throws EmpruntException {
        // Charger l'emprunt depuis la base de données
        EntityManager em = dao.getEntityManager();
        try {
            Emprunt emprunt = em.find(Emprunt.class, idEmprunt);
            if (emprunt == null) {
                throw new EmpruntException("Emprunt introuvable");
            }
            if (!emprunt.getReparateur().getId().equals(reparateur.getId())) {
                throw new EmpruntException("Cet emprunt n'appartient pas à ce réparateur");
            }
            empruntMetier.rembourserEmprunt(emprunt);
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Emprunt> listerEmpruntsActifs(Reparateur reparateur) throws CaisseException {
        try {
            return empruntMetier.listerEmpruntsParReparateur(reparateur).stream()
                    .filter(emp -> !emp.isRembourse())
                    .collect(Collectors.toList());
        } catch (EmpruntException e) {
            throw new CaisseException("Impossible de lister les emprunts actifs");
        }
    }
    
    @Override
    public List<Emprunt> listerTousEmprunts(Reparateur reparateur) throws CaisseException {
        try {
            return empruntMetier.listerEmpruntsParReparateur(reparateur);
        } catch (EmpruntException e) {
            throw new CaisseException("Impossible de lister les emprunts du réparateur");
        }
    }
    
    @Override
    public float calculerTotalEmpruntsSortieActifs(Reparateur reparateur) throws CaisseException {
        try {
            List<Emprunt> emprunts = empruntMetier.listerEmpruntsParReparateur(reparateur);
            return (float) emprunts.stream()
                    .filter(emp -> !emp.isRembourse() && "SORTIE".equalsIgnoreCase(emp.getType()))
                    .mapToDouble(Emprunt::getMontant)
                    .sum();
        } catch (EmpruntException e) {
            throw new CaisseException("Impossible de calculer les emprunts de sortie");
        }
    }
    
    @Override
    public float calculerTotalEmpruntsEntreeActifs(Reparateur reparateur) throws CaisseException {
        try {
            List<Emprunt> emprunts = empruntMetier.listerEmpruntsParReparateur(reparateur);
            return (float) emprunts.stream()
                    .filter(emp -> !emp.isRembourse() && "ENTREE".equalsIgnoreCase(emp.getType()))
                    .mapToDouble(Emprunt::getMontant)
                    .sum();
        } catch (EmpruntException e) {
            throw new CaisseException("Impossible de calculer les emprunts d'entrée");
        }
    }
    
    @Override
    public CaisseAvecEmprunt obtenirCaisseAvecEmprunts(Reparateur reparateur) throws CaisseException {
        if (reparateur == null || reparateur.getCaisse() == null) {
            throw new CaisseException("Réparateur sans caisse");
        }
        
        EntityManager em = dao.getEntityManager();
        try {
            // Charger la caisse avec les emprunts
            List<Emprunt> emprunts = em.createQuery(
                "SELECT e FROM Emprunt e WHERE e.caisse.idCaisse = :caisseId ORDER BY e.dateEmprunt DESC",
                Emprunt.class)
                .setParameter("caisseId", reparateur.getCaisse().getIdCaisse())
                .getResultList();
            
            return new CaisseAvecEmprunt(reparateur.getCaisse(), emprunts);
        } catch (Exception e) {
            throw new CaisseException("Impossible d'obtenir les informations de caisse avec emprunts");
        } finally {
            em.close();
        }
    }
}
