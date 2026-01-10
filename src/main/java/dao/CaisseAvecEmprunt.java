package dao;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Classe qui encapsule une caisse avec ses informations d'emprunts.
 * Permet de gérer la distinction entre Caisse Système et Caisse Réelle.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaisseAvecEmprunt {

    private Caisse caisse;
    private List<Emprunt> emprunts;

    /**
     * Retourne le solde système (soldeActuel de la caisse).
     * Ce solde n'est jamais affecté par les emprunts.
     * @return Le solde système
     */
    public float getSoldeSysteme() {
        return caisse != null ? caisse.getSoldeActuel() : 0;
    }

    /**
     * Retourne le solde réel calculé.
     * Caisse Réelle = Caisse Système - Emprunts SORTIE actifs
     * @return Le solde réel
     */
    public float getSoldeReel() {
        float soldeSysteme = getSoldeSysteme();
        float totalEmpruntsSortie = getTotalEmpruntsSortieActifs();
        return soldeSysteme - totalEmpruntsSortie;
    }

    /**
     * Calcule le total des emprunts SORTIE non remboursés.
     * @return Le montant total des emprunts SORTIE actifs
     */
    public float getTotalEmpruntsSortieActifs() {
        if (emprunts == null || emprunts.isEmpty()) {
            return 0;
        }
        
        return emprunts.stream()
            .filter(e -> "SORTIE".equalsIgnoreCase(e.getType()) && !e.isRembourse())
            .map(Emprunt::getMontant)
            .reduce(0f, Float::sum);
    }

    /**
     * Calcule le total des emprunts ENTREE non remboursés.
     * @return Le montant total des emprunts ENTREE actifs
     */
    public float getTotalEmpruntsEntreeActifs() {
        if (emprunts == null || emprunts.isEmpty()) {
            return 0;
        }
        
        return emprunts.stream()
            .filter(e -> "ENTREE".equalsIgnoreCase(e.getType()) && !e.isRembourse())
            .map(Emprunt::getMontant)
            .reduce(0f, Float::sum);
    }

    /**
     * Retourne la liste des emprunts SORTIE actifs.
     * @return Liste des emprunts SORTIE non remboursés
     */
    public List<Emprunt> getEmpruntsSortieActifs() {
        if (emprunts == null || emprunts.isEmpty()) {
            return List.of();
        }
        
        return emprunts.stream()
            .filter(e -> "SORTIE".equalsIgnoreCase(e.getType()) && !e.isRembourse())
            .collect(Collectors.toList());
    }

    /**
     * Retourne la liste des emprunts ENTREE actifs.
     * @return Liste des emprunts ENTREE non remboursés
     */
    public List<Emprunt> getEmpruntsEntreeActifs() {
        if (emprunts == null || emprunts.isEmpty()) {
            return List.of();
        }
        
        return emprunts.stream()
            .filter(e -> "ENTREE".equalsIgnoreCase(e.getType()) && !e.isRembourse())
            .collect(Collectors.toList());
    }

    /**
     * Retourne tous les emprunts actifs (SORTIE et ENTREE).
     * @return Liste de tous les emprunts non remboursés
     */
    public List<Emprunt> getEmpruntsActifs() {
        if (emprunts == null || emprunts.isEmpty()) {
            return List.of();
        }
        
        return emprunts.stream()
            .filter(e -> !e.isRembourse())
            .collect(Collectors.toList());
    }

    /**
     * Vérifie si un montant peut être emprunté (SORTIE).
     * @param montant Le montant à emprunter
     * @return true si le montant est disponible dans la caisse réelle
     */
    public boolean peutEmprunter(float montant) {
        return montant <= getSoldeReel() && montant > 0;
    }

    /**
     * Retourne le nombre d'emprunts actifs.
     * @return Le nombre d'emprunts non remboursés
     */
    public int getNombreEmpruntsActifs() {
        if (emprunts == null) {
            return 0;
        }
        return (int) emprunts.stream().filter(e -> !e.isRembourse()).count();
    }
}
