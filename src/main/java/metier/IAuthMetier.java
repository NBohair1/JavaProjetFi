package metier;

import dao.Boutique;
import dao.Caisse;
import dao.Proprietaire;
import dao.User;
import exception.AuthException;

// Interface métier pour l'authentification
public interface IAuthMetier {
    // Inscription d'un nouveau propriétaire
    Proprietaire inscription(String nom, String prenom, String email, String motDePasse) throws AuthException;
    
    // Connexion utilisateur (retourne Proprietaire ou Reparateur)
    User login(String email, String motDePasse) throws AuthException;
    
    // Création d'une boutique pour un propriétaire
    Boutique creerBoutique(Proprietaire proprietaire, String nom, String adresse, String numTelephone, String numPatente) throws AuthException;
    
    // Création de la caisse du propriétaire
    Caisse creerCaisseProprietaire(Proprietaire proprietaire) throws AuthException;
}