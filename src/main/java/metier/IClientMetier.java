package metier;

import java.util.List;

import dao.Boutique;
import dao.Client;
import dao.Reparation;
import exception.ClientException;

// Interface métier pour la gestion des clients
public interface IClientMetier {

    // Créer un nouveau client
    Client creerClient(
        String nom,
        String prenom,
        String telephone,
        String adresse,
        byte[] image
    ) throws ClientException;
    
    // Modifier un client existant
    Client modifierClient(
        Client client,
        String nom,
        String prenom,
        String telephone,
        String adresse,
        byte[] image
    ) throws ClientException;
    
    // Supprimer un client
    void supprimerClient(Client client) throws ClientException;
    
    // Recherche par ID
    Client rechercherClientParId(Long id) throws ClientException;
    
    // Recherche par téléphone
    Client rechercherClientParTelephone(String telephone) throws ClientException;
    
    // Lister tous les clients
    List<Client> listerTousLesClients() throws ClientException;
    
    // Lister les clients d'une boutique (via réparations)
    List<Client> listerClientsParBoutique(Boutique boutique) throws ClientException;
    
    // Marquer un client comme fidèle
    void marquerClientFidele(Client client) throws ClientException;
    
    // Lister les clients fidèles
    List<Client> listerClientsFideles() throws ClientException;
    
    // Historique des réparations d'un client
    List<Reparation> historqueReparationsClient(Client client) throws ClientException;
    
    // Suivi réparation par code (pour le client)
    Reparation suivreReparationParCode(String codeSuivi) throws ClientException;
}
