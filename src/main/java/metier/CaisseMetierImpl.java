package metier;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import dao.Caisse;
import dao.Emprunt;
import dao.GenericDao;
import dao.MouvementCaisse;
import dao.Proprietaire;
import dao.Reparateur;
import dao.Reparation;
import dao.StatistiquesCaisse;
import exception.CaisseException;
import exception.EmpruntException;

public class CaisseMetierImpl implements ICaisseMetier {
    private final GenericDao dao = new GenericDao();
    private final IEmpruntMetier empruntMetier = new EmpruntMetierImpl();

    @Override
    public void alimenterCaisse(Reparateur r, float m, String desc) throws CaisseException {
        if (r == null) {
            throw new CaisseException("Reparateur non selectionne");
        }
        if (m <= 0) {
            throw new CaisseException("Le montant doit etre positif");
        }
        if (desc == null || desc.trim().isEmpty()) {
            desc = "Alimentation caisse " + r.getPrenom() + " " + r.getNom();
        }
        gererMouvement(r, m, "ALIMENTATION", desc, null);
    }

    @Override
    public void retirerCaisse(Reparateur r, float m, String desc) throws CaisseException {
        if (r == null) {
            throw new CaisseException("Reparateur non selectionne");
        }
        if (m <= 0) {
            throw new CaisseException("Le montant doit etre positif");
        }
        float soldeActuel = consulterSolde(r);
        if (soldeActuel < m) {
            throw new CaisseException("Solde insuffisant");
        }
        gererMouvement(r, -m, "SORTIE", desc, null);
    }

    @Override
    public void enregistrerPaiement(Reparateur r, float m, String desc, Reparation rep) throws CaisseException {
        if (r == null) {
            throw new CaisseException("Reparateur non selectionne");
        }
        if (m <= 0) {
            throw new CaisseException("Le montant doit etre positif");
        }
        if (rep == null) {
            throw new CaisseException("Reparation non selectionnee");
        }
        if (desc == null || desc.trim().isEmpty()) {
            desc = "Paiement reparation " + (rep.getCodeSuivi() != null ? rep.getCodeSuivi() : "#" + rep.getIdReparation());
        }
        
        // Repartition des gains entre reparateur et proprietaire
        float pourcentage = r.getPourcentageGain();
        float gainReparateur = m * (pourcentage / 100f);
        float gainProprietaire = m - gainReparateur;
        
        gererMouvement(r, gainReparateur, "ENTREE", 
            desc + " (" + pourcentage + "% de " + m + " DH)", rep);
        
        if (r.getBoutique() != null && r.getBoutique().getProprietaire() != null) {
            Proprietaire proprio = r.getBoutique().getProprietaire();
            if (proprio.getCaisse() != null) {
                gererMouvement(proprio, gainProprietaire, "ENTREE", 
                    desc + " (part boutique: " + (100 - pourcentage) + "% de " + m + " DH)", rep);
            }
        }
    }

    private void gererMouvement(Reparateur r, float m, String type, String desc, Reparation rep) throws CaisseException {
        if (r == null) throw new CaisseException("Reparateur non selectionne");
        if (r.getCaisse() == null) {
            creerCaissePourReparateur(r);
            if (r.getCaisse() == null) throw new CaisseException("Impossible de creer la caisse");
        }
        EntityManager em = dao.getEntityManager(); // Via DAO
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Caisse c = em.find(Caisse.class, r.getCaisse().getIdCaisse());
            
            if(type.equals("SORTIE")) c.setSoldeActuel(c.getSoldeActuel() + m); // m est négatif
            else c.setSoldeActuel(c.getSoldeActuel() + m);
            
            c.setDernierMouvement(new Date());
            
            MouvementCaisse mv = new MouvementCaisse();
            mv.setMontant(Math.abs(m)); mv.setTypeMouvement(type); mv.setDescription(desc);
            mv.setDateMouvement(new Date()); mv.setCaisse(c); mv.setReparation(rep);
            
            em.persist(mv);
            em.merge(c);
            tx.commit();
        } catch(Exception e) { 
            if(tx.isActive()) tx.rollback(); 
            throw new CaisseException("Erreur sur la caisse");
        }
        finally { em.close(); }
    }

    @Override
    public float consulterSolde(Reparateur r) throws CaisseException {
        if(r == null) return 0;
        if(r.getCaisse() == null) {
            // Créer une caisse si elle n'existe pas
            creerCaissePourReparateur(r);
            if(r.getCaisse() == null) return 0;
        }
        EntityManager em = dao.getEntityManager();
        try {
            Caisse caisse = em.find(Caisse.class, r.getCaisse().getIdCaisse());
            return caisse != null ? caisse.getSoldeActuel() : 0;
        } finally {
            em.close();
        }
    }

    @Override
    public List<MouvementCaisse> consulterCaisseHebdomadaire(Reparateur r, Date d1, Date d2) throws CaisseException {
        return listerMouvements(r);
    }
    @Override
    public float consulterTotalCaissesBoutique(Proprietaire p) throws CaisseException { return 0; }
    @Override
    public List<MouvementCaisse> listerMouvements(Reparateur r) throws CaisseException {
        if(r == null) return new java.util.ArrayList<>();
        if(r.getCaisse() == null) {
            creerCaissePourReparateur(r);
            if(r.getCaisse() == null) return new java.util.ArrayList<>();
        }
        EntityManager em = dao.getEntityManager();
        try {
            return em.createQuery(
                "SELECT m FROM MouvementCaisse m " +
                "LEFT JOIN FETCH m.reparation " +
                "WHERE m.caisse.idCaisse = :caisseId " +
                "ORDER BY m.dateMouvement DESC",
                MouvementCaisse.class)
                .setParameter("caisseId", r.getCaisse().getIdCaisse())
                .getResultList();
        } finally {
            em.close();
        }
    }
    
    @Override
    public StatistiquesCaisse obtenirStatistiques(Reparateur r) throws CaisseException {
        StatistiquesCaisse stats = new StatistiquesCaisse();
        
        float caisseSysteme = consulterSolde(r);
        stats.setCaisse(caisseSysteme);
        
        try {
            List<Emprunt> emprunts = empruntMetier.listerEmpruntsParReparateur(r);
            float totalEmprunts = 0;
            float empruntsSortieActifs = 0;
            float empruntsEntreeActifs = 0;
            int nombreActifs = 0;
            
            for (Emprunt emp : emprunts) {
                if (!emp.isRembourse()) {
                    totalEmprunts += emp.getMontant();
                    nombreActifs++;
                    
                    if ("SORTIE".equalsIgnoreCase(emp.getType())) {
                        empruntsSortieActifs += emp.getMontant();
                    } else if ("ENTREE".equalsIgnoreCase(emp.getType())) {
                        empruntsEntreeActifs += emp.getMontant();
                    }
                }
            }
            
            float caisseReelle = caisseSysteme - empruntsSortieActifs + empruntsEntreeActifs;
            stats.setCaisseReelle(caisseReelle);
            stats.setTotalEmprunts(totalEmprunts);
            stats.setNombreEmpruntsActifs(nombreActifs);
        } catch (EmpruntException e) {
            stats.setCaisseReelle(caisseSysteme);
            stats.setTotalEmprunts(0);
            stats.setNombreEmpruntsActifs(0);
        }
        
        // Revenu total
        EntityManager em = dao.getEntityManager();
        try {
            if (r.getCaisse() != null) {
                Double revenuTotal = em.createQuery(
                    "SELECT SUM(m.montant) FROM MouvementCaisse m " +
                    "WHERE m.caisse.idCaisse = :caisseId AND m.typeMouvement = 'ENTREE'",
                    Double.class)
                    .setParameter("caisseId", r.getCaisse().getIdCaisse())
                    .getSingleResult();
                stats.setRevenuTotal(revenuTotal != null ? revenuTotal.floatValue() : 0);
            } else {
                stats.setRevenuTotal(0);
            }
        } finally {
            em.close();
        }
        
        // Revenus période (30 derniers jours)
        Date dateFin = new Date();
        Date dateDebut = new Date(dateFin.getTime() - 30L * 24 * 60 * 60 * 1000);
        stats.setRevenusPeriode(calculerRevenusPeriode(r, dateDebut, dateFin));
        
        // Nombre de réparations
        stats.setNombreReparations(calculerNombreReparations(r));
        
        // Nombre de réparations terminées
        em = dao.getEntityManager();
        try {
            Long nbTerminees = em.createQuery(
                "SELECT COUNT(r) FROM Reparation r " +
                "WHERE r.reparateur.id = :repId AND r.etat IN ('TERMINEE', 'LIVREE')",
                Long.class)
                .setParameter("repId", r.getId())
                .getSingleResult();
            stats.setNombreReparationsTerminees(nbTerminees != null ? nbTerminees.intValue() : 0);
        } finally {
            em.close();
        }
        
        return stats;
    }
    
    @Override
    public float calculerRevenusPeriode(Reparateur r, Date dateDebut, Date dateFin) throws CaisseException {
        if (r == null) return 0;
        if (r.getCaisse() == null) {
            creerCaissePourReparateur(r);
            if (r.getCaisse() == null) return 0;
        }
        
        EntityManager em = dao.getEntityManager();
        try {
            Double revenus = em.createQuery(
                "SELECT SUM(m.montant) FROM MouvementCaisse m " +
                "WHERE m.caisse.idCaisse = :caisseId " +
                "AND m.typeMouvement = 'ENTREE' " +
                "AND m.dateMouvement BETWEEN :debut AND :fin",
                Double.class)
                .setParameter("caisseId", r.getCaisse().getIdCaisse())
                .setParameter("debut", dateDebut)
                .setParameter("fin", dateFin)
                .getSingleResult();
            return revenus != null ? revenus.floatValue() : 0;
        } finally {
            em.close();
        }
    }
    
    @Override
    public int calculerNombreReparations(Reparateur r) throws CaisseException {
        EntityManager em = dao.getEntityManager();
        try {
            Long nombre = em.createQuery(
                "SELECT COUNT(r) FROM Reparation r WHERE r.reparateur.id = :repId",
                Long.class)
                .setParameter("repId", r.getId())
                .getSingleResult();
            return nombre != null ? nombre.intValue() : 0;
        } finally {
            em.close();
        }
    }
    
    @Override
    public float consulterCaisseReelle(Reparateur r) throws CaisseException {
        float caisseSysteme = consulterSolde(r);
        
        try {
            List<Emprunt> emprunts = empruntMetier.listerEmpruntsParReparateur(r);
            float empruntsSortieActifs = 0;
            
            for (Emprunt emp : emprunts) {
                if (!emp.isRembourse() && "SORTIE".equalsIgnoreCase(emp.getType())) {
                    empruntsSortieActifs += emp.getMontant();
                }
            }
            
            return caisseSysteme - empruntsSortieActifs;
        } catch (EmpruntException e) {
            throw new CaisseException("Impossible de calculer la caisse reelle");
        }
    }
    
    @Override
    public float consulterCaisseSysteme(Reparateur r) throws CaisseException {
        return consulterSolde(r);
    }
    
    private void creerCaissePourReparateur(Reparateur r) {
        if (r == null || r.getCaisse() != null) return;
        
        EntityManager em = dao.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            Reparateur reparateurManaged = em.find(Reparateur.class, r.getId());
            if (reparateurManaged != null && reparateurManaged.getCaisse() == null) {
                Caisse caisse = new Caisse();
                caisse.setSoldeActuel(0);
                caisse.setDernierMouvement(new Date());
                em.persist(caisse);
                
                reparateurManaged.setCaisse(caisse);
                em.merge(reparateurManaged);
                
                r.setCaisse(caisse);
            }
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}