package metier;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import dao.Boutique;
import dao.Client;
import dao.GenericDao;
import dao.Reparation;
import exception.ClientException;

public class ClientMetierImpl implements IClientMetier {

    private GenericDao dao = new GenericDao();

    @Override
    public Client creerClient(
            String nom,
            String prenom,
            String telephone,
            String adresse,
            byte[] image
    ) throws ClientException {

        EntityManager em = JPAUtil.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            if (nom == null || nom.trim().isEmpty())
                throw new ClientException("Nom du client requis (ne peut pas être vide)");
            
            if (prenom == null || prenom.trim().isEmpty())
                throw new ClientException("Prénom du client requis (ne peut pas être vide)");

            if (telephone == null || telephone.trim().isEmpty())
                throw new ClientException("Numéro de téléphone requis pour contacter le client");

            // Vérifier format téléphone basique
            String telNormalise = telephone.trim().replaceAll("[\\s-]", "");
            if (telNormalise.length() < 8) {
                throw new ClientException("Numéro de téléphone invalide (trop court): '" + telephone + "'");
            }

            // Vérifier si le téléphone existe déjà
            Client existant = dao.findOneByProperty(Client.class, "telephone", telephone);
            if (existant != null)
                throw new ClientException("Ce numéro de téléphone est déjà utilisé par le client: " + existant.getNom() + " " + existant.getPrenom());

            Client c = new Client();
            c.setNom(nom);
            c.setPrenom(prenom);
            c.setTelephone(telephone);
            c.setAdresse(adresse);
            c.setImage(image);
            c.setFidele(false);

            dao.save(c);
            
            tx.commit();
            return c;
        } catch (ClientException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new ClientException("Erreur lors de la création du client");
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public Client modifierClient(
            Client client,
            String nom,
            String prenom,
            String telephone,
            String adresse,
            byte[] image
    ) throws ClientException {

        EntityManager em = JPAUtil.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            if (client == null)
                throw new ClientException("Client obligatoire");

            if (nom != null && !nom.isEmpty())
                client.setNom(nom);
            
            if (prenom != null && !prenom.isEmpty())
                client.setPrenom(prenom);
            
            if (telephone != null && !telephone.isEmpty()) {
                // Vérifier si le nouveau téléphone n'est pas déjà utilisé par un autre client
                Client existant = dao.findOneByProperty(Client.class, "telephone", telephone);
                if (existant != null && !existant.getIdClient().equals(client.getIdClient()))
                    throw new ClientException("Ce numéro de téléphone est déjà utilisé");
                client.setTelephone(telephone);
            }
            
            if (adresse != null)
                client.setAdresse(adresse);
            
            if (image != null)
                client.setImage(image);

            Client updated = dao.update(client);
            
            tx.commit();
            return updated;
        } catch (ClientException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new ClientException("Erreur lors de la modification du client");
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public void supprimerClient(Client client) throws ClientException {

        EntityManager em = JPAUtil.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            if (client == null)
                throw new ClientException("Client obligatoire");

            // Récupérer le client géré par JPA
            Client clientManaged = em.find(Client.class, client.getIdClient());
            if (clientManaged == null) {
                throw new ClientException("Client non trouvé");
            }

            // Vérifier si le client a des réparations
            List<Reparation> reparations = em.createQuery(
                "SELECT r FROM Reparation r WHERE r.client.idClient = :clientId", Reparation.class)
                .setParameter("clientId", client.getIdClient())
                .getResultList();
            
            if (!reparations.isEmpty()) {
                // Vérifier si toutes les réparations sont terminées ou livrées
                boolean hasActiveRepairs = false;
                for (Reparation r : reparations) {
                    if (!"TERMINEE".equalsIgnoreCase(r.getEtat()) && 
                        !"LIVREE".equalsIgnoreCase(r.getEtat()) &&
                        !"ANNULEE".equalsIgnoreCase(r.getEtat())) {
                        hasActiveRepairs = true;
                        break;
                    }
                }
                
                if (hasActiveRepairs) {
                    throw new ClientException("Impossible de supprimer: le client a des réparations actives (EN_ATTENTE ou EN_COURS).\n" +
                        "Veuillez d'abord terminer, livrer ou annuler toutes ses réparations.");
                }
                
                // Message informatif si le client a des réparations terminées
                throw new ClientException("Impossible de supprimer: le client a un historique de " + 
                    reparations.size() + " réparation(s).\n" +
                    "La suppression est bloquée pour conserver l'historique.");
            }

            // Supprimer le client s'il n'a aucune réparation
            em.remove(clientManaged);
            
            tx.commit();
        } catch (ClientException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new ClientException("Impossible de supprimer le client");
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public Client rechercherClientParId(Long id) throws ClientException {
        if (id == null)
            throw new ClientException("ID obligatoire");
        
        return dao.findById(Client.class, id);
    }

    @Override
    public Client rechercherClientParTelephone(String telephone) throws ClientException {
        if (telephone == null || telephone.isEmpty())
            throw new ClientException("Téléphone obligatoire");
        
        return dao.findOneByProperty(Client.class, "telephone", telephone);
    }

    @Override
    public List<Client> listerTousLesClients() throws ClientException {
        return dao.findAll(Client.class);
    }

    @Override
    public List<Client> listerClientsParBoutique(Boutique boutique) throws ClientException {
        if (boutique == null) {
            throw new ClientException("Boutique obligatoire");
        }
        
        EntityManager em = JPAUtil.getInstance().getEntityManager();
        try {
            // Récupérer les clients qui ont des réparations dans cette boutique
            return em.createQuery(
                "SELECT DISTINCT c FROM Client c " +
                "JOIN c.reparations r " +
                "JOIN r.reparateur rep " +
                "WHERE rep.boutique = :boutique " +
                "ORDER BY c.nom, c.prenom",
                Client.class)
                .setParameter("boutique", boutique)
                .getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public void marquerClientFidele(Client client) throws ClientException {

        EntityManager em = JPAUtil.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            if (client == null)
                throw new ClientException("Client obligatoire");

            client.setFidele(true);
            dao.update(client);
            
            tx.commit();
        } catch (ClientException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new ClientException("Erreur lors du marquage de fidélité");
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<Client> listerClientsFideles() throws ClientException {
        return dao.findByProperty(Client.class, "fidele", true);
    }

    @Override
    public List<Reparation> historqueReparationsClient(Client client) throws ClientException {
        if (client == null)
            throw new ClientException("Client obligatoire");
        
        return dao.findByProperty(Reparation.class, "client", client);
    }

    // ===== SUIVI RÉPARATION PAR CODE =====
    @Override
    public Reparation suivreReparationParCode(String codeSuivi) throws ClientException {
        if (codeSuivi == null || codeSuivi.isEmpty())
            throw new ClientException("Code de suivi obligatoire");
        
        Reparation reparation = dao.findOneByProperty(Reparation.class, "codeSuivi", codeSuivi);
        
        if (reparation == null)
            throw new ClientException("Aucune réparation trouvée avec ce code de suivi");
        
        return reparation;
    }
}
