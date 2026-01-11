package metier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import dao.Boutique;
import dao.Caisse;
import dao.GenericDao;
import dao.Proprietaire;
import dao.Reparateur;
import dao.Reparation;
import dao.StatistiquesFinancieres;
import exception.BoutiqueException;

public class BoutiqueMetierImpl implements IBoutiqueMetier {

    private final GenericDao dao = new GenericDao();

    @Override
    public Reparateur creerReparateur(
            Boutique boutique,
            String nom,
            String prenom,
            String email,
            String motDePasse,
            float pourcentageGain
    ) throws BoutiqueException {

        EntityManager em = JPAUtil.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            if (boutique == null)
                throw new BoutiqueException("Boutique non selectionnee");
            
            if (nom == null || nom.isEmpty())
                throw new BoutiqueException("Veuillez entrer le nom");
            
            if (prenom == null || prenom.isEmpty())
                throw new BoutiqueException("Veuillez entrer le prenom");

            if (email == null || email.isEmpty())
                throw new BoutiqueException("Veuillez entrer l'email");

            if (motDePasse == null || motDePasse.isEmpty())
                throw new BoutiqueException("Veuillez entrer le mot de passe");
            
            if (pourcentageGain < 0 || pourcentageGain > 100)
                throw new BoutiqueException("Pourcentage invalide (entre 0 et 100)");

            // Vérifier si l'email existe déjà
            Reparateur existant = dao.findOneByProperty(Reparateur.class, "email", email);
            if (existant != null)
                throw new BoutiqueException("Cet email est deja utilise");

            Reparateur r = new Reparateur();
            r.setNom(nom);
            r.setPrenom(prenom);
            r.setEmail(email);
            r.setMdp(motDePasse);
            r.setPourcentageGain(pourcentageGain);
            r.setBoutique(boutique);
            
            // Créer automatiquement une caisse pour le réparateur
            Caisse caisse = new Caisse();
            caisse.setSoldeActuel(0);
            caisse.setDernierMouvement(new Date());
            r.setCaisse(caisse);

            dao.save(r);
            
            tx.commit();
            return r;
        } catch (BoutiqueException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new BoutiqueException("Erreur lors de la creation du reparateur");
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<Reparateur> listerReparateurs(Boutique boutique) throws BoutiqueException {
        if (boutique == null)
            throw new BoutiqueException("Boutique non selectionnee");
        
        return dao.findByProperty(Reparateur.class, "boutique", boutique);
    }

    @Override
    public List<Boutique> listerBoutiques(Proprietaire proprietaire) throws BoutiqueException {
        if (proprietaire == null)
            throw new BoutiqueException("Proprietaire non selectionne");
        
        // Utiliser JOIN FETCH pour charger la collection reparateurs
        EntityManager em = JPAUtil.getInstance().getEntityManager();
        try {
            return em.createQuery(
                "SELECT DISTINCT b FROM Boutique b " +
                "LEFT JOIN FETCH b.reparateurs " +
                "WHERE b.proprietaire = :proprio",
                Boutique.class)
                .setParameter("proprio", proprietaire)
                .getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public Boutique modifierBoutique(
            Boutique boutique,
            String nom,
            String adresse,
            String numTelephone,
            String numPatente
    ) throws BoutiqueException {

        EntityManager em = JPAUtil.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            if (boutique == null)
                throw new BoutiqueException("Boutique non selectionnee");

            if (nom != null && !nom.isEmpty())
                boutique.setNom(nom);
            
            if (adresse != null && !adresse.isEmpty())
                boutique.setAdresse(adresse);
            
            if (numTelephone != null)
                boutique.setNumTelephone(numTelephone);
            
            if (numPatente != null)
                boutique.setNumP(numPatente);

            Boutique updated = dao.update(boutique);
            
            tx.commit();
            return updated;
        } catch (BoutiqueException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new BoutiqueException("Erreur lors de la modification de la boutique");
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public Caisse creerCaisseReparateur(Reparateur reparateur) throws BoutiqueException {

        EntityManager em = JPAUtil.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            if (reparateur == null)
                throw new BoutiqueException("Reparateur non selectionne");
            
            if (reparateur.getCaisse() != null)
                throw new BoutiqueException("Ce reparateur a deja une caisse");

            Caisse caisse = new Caisse();
            caisse.setSoldeActuel(0);
            caisse.setDernierMouvement(new Date());
            
            dao.save(caisse);
            
            reparateur.setCaisse(caisse);
            dao.update(reparateur);
            
            tx.commit();
            return caisse;
        } catch (BoutiqueException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new BoutiqueException("Erreur lors de la création de la caisse");
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public void modifierPourcentageGain(Reparateur reparateur, float nouveauPourcentage) 
            throws BoutiqueException {

        EntityManager em = JPAUtil.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            if (reparateur == null)
                throw new BoutiqueException("Reparateur non selectionne");
            
            if (nouveauPourcentage < 0 || nouveauPourcentage > 100)
                throw new BoutiqueException("Pourcentage invalide (entre 0 et 100)");

            reparateur.setPourcentageGain(nouveauPourcentage);
            dao.update(reparateur);
            
            tx.commit();
        } catch (BoutiqueException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new BoutiqueException("Erreur lors de la modification du pourcentage");
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    @Override
    public void modifierReparateur(Reparateur reparateur) throws BoutiqueException {
        EntityManager em = JPAUtil.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            if (reparateur == null)
                throw new BoutiqueException("Reparateur non selectionne");

            dao.update(reparateur);
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new BoutiqueException("Erreur lors de la modification");
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    @Override
    public List<Reparation> listerDernieresReparations(Proprietaire proprietaire, int limite) 
            throws BoutiqueException {
        if (proprietaire == null)
            throw new BoutiqueException("Proprietaire non selectionne");
        
        EntityManager em = JPAUtil.getInstance().getEntityManager();
        try {
            // Récupérer toutes les boutiques du propriétaire
            List<Boutique> boutiques = listerBoutiques(proprietaire);
            if (boutiques == null || boutiques.isEmpty())
                return new ArrayList<>();
            
            // Récupérer les dernières réparations de toutes les boutiques
            return em.createQuery(
                "SELECT DISTINCT r FROM Reparation r " +
                "LEFT JOIN FETCH r.client " +
                "LEFT JOIN FETCH r.reparateur rep " +
                "LEFT JOIN FETCH r.appareils " +
                "WHERE rep.boutique IN :boutiques " +
                "ORDER BY r.dateDepot DESC",
                Reparation.class)
                .setParameter("boutiques", boutiques)
                .setMaxResults(limite)
                .getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    @Override
    public List<Reparation> listerDernieresReparations(Boutique boutique, int limite) 
            throws BoutiqueException {
        if (boutique == null)
            throw new BoutiqueException("Boutique non selectionnee");
        
        EntityManager em = JPAUtil.getInstance().getEntityManager();
        try {
            // Récupérer les dernières réparations de la boutique spécifique
            return em.createQuery(
                "SELECT DISTINCT r FROM Reparation r " +
                "LEFT JOIN FETCH r.client " +
                "LEFT JOIN FETCH r.reparateur rep " +
                "LEFT JOIN FETCH r.appareils " +
                "WHERE rep.boutique = :boutique " +
                "ORDER BY r.dateDepot DESC",
                Reparation.class)
                .setParameter("boutique", boutique)
                .setMaxResults(limite)
                .getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    @Override
    public StatistiquesFinancieres obtenirStatistiquesFinancieres(Proprietaire proprietaire) 
            throws BoutiqueException {
        if (proprietaire == null)
            throw new BoutiqueException("Proprietaire non selectionne");
        
        EntityManager em = JPAUtil.getInstance().getEntityManager();
        try {
            StatistiquesFinancieres stats = new StatistiquesFinancieres();
            
            // Récupérer toutes les boutiques du propriétaire
            List<Boutique> boutiques = listerBoutiques(proprietaire);
            if (boutiques == null || boutiques.isEmpty())
                return stats;
            
            // Récupérer tous les réparateurs de ces boutiques
            List<Reparateur> reparateurs = em.createQuery(
                "SELECT r FROM Reparateur r WHERE r.boutique IN :boutiques",
                Reparateur.class)
                .setParameter("boutiques", boutiques)
                .getResultList();
            
            stats.setNombreReparateursActifs(reparateurs.size());
            
            float revenuTotal = 0;
            float totalCaisses = 0;
            int nombreTotalReparations = 0;
            
            // Calculer les statistiques pour chaque réparateur
            for (Reparateur rep : reparateurs) {
                if (rep.getCaisse() == null) continue;
                
                // Solde de la caisse
                float soldeCaisse = rep.getCaisse().getSoldeActuel();
                totalCaisses += soldeCaisse;
                
                // Revenus générés par ce réparateur
                Double revenus = em.createQuery(
                    "SELECT SUM(m.montant) FROM MouvementCaisse m " +
                    "WHERE m.caisse.idCaisse = :caisseId AND m.typeMouvement = 'ENTREE'",
                    Double.class)
                    .setParameter("caisseId", rep.getCaisse().getIdCaisse())
                    .getSingleResult();
                
                float revenusRep = (revenus != null ? revenus.floatValue() : 0);
                revenuTotal += revenusRep;
                
                // Nombre de réparations
                Long nbRep = em.createQuery(
                    "SELECT COUNT(r) FROM Reparation r WHERE r.reparateur.id = :repId",
                    Long.class)
                    .setParameter("repId", rep.getId())
                    .getSingleResult();
                
                int nombreRep = (nbRep != null ? nbRep.intValue() : 0);
                nombreTotalReparations += nombreRep;
                
                // Calculer le pourcentage gagné par le réparateur
                float pourcentageGagne = revenusRep * (rep.getPourcentageGain() / 100);
                
                // Ajouter aux statistiques
                stats.ajouterRevenusReparateur(rep.getId(), revenusRep);
                stats.ajouterPourcentageReparateur(rep.getId(), pourcentageGagne);
                stats.ajouterNombreReparationsReparateur(rep.getId(), nombreRep);
            }
            
            stats.setRevenuTotalBoutique(revenuTotal);
            stats.setTotalCaissesBoutique(totalCaisses);
            stats.setNombreTotalReparations(nombreTotalReparations);
            
            return stats;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    @Override
    public List<Caisse> listerCaissesReparateurs(Proprietaire proprietaire) 
            throws BoutiqueException {
        if (proprietaire == null)
            throw new BoutiqueException("Proprietaire non selectionne");
        
        EntityManager em = JPAUtil.getInstance().getEntityManager();
        try {
            // Récupérer toutes les boutiques du propriétaire
            List<Boutique> boutiques = listerBoutiques(proprietaire);
            if (boutiques == null || boutiques.isEmpty())
                return new ArrayList<>();
            
            // Récupérer toutes les caisses des réparateurs
            return em.createQuery(
                "SELECT c FROM Caisse c " +
                "JOIN Reparateur r ON r.caisse.idCaisse = c.idCaisse " +
                "WHERE r.boutique IN :boutiques",
                Caisse.class)
                .setParameter("boutiques", boutiques)
                .getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    @Override
    public List<Caisse> listerCaissesReparateurs(Boutique boutique) 
            throws BoutiqueException {
        if (boutique == null)
            throw new BoutiqueException("Boutique non selectionnee");
        
        EntityManager em = JPAUtil.getInstance().getEntityManager();
        try {
            // Récupérer les caisses des réparateurs de la boutique spécifique
            return em.createQuery(
                "SELECT c FROM Caisse c " +
                "JOIN Reparateur r ON r.caisse.idCaisse = c.idCaisse " +
                "WHERE r.boutique = :boutique",
                Caisse.class)
                .setParameter("boutique", boutique)
                .getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
