package dao;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Caisse avec gestion des emprunts
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaisseAvecEmprunt {

    private Caisse caisse;
    // List : on stocke les emprunts dans l'ordre d'insertion
    private List<Emprunt> emprunts;

    public float getSoldeSysteme() {
        return caisse != null ? caisse.getSoldeActuel() : 0;
    }

    // Solde reel = solde systeme - emprunts sortie en cours
    public float getSoldeReel() {
        float soldeSysteme = getSoldeSysteme();
        float totalEmpruntsSortie = getTotalEmpruntsSortieActifs();
        return soldeSysteme - totalEmpruntsSortie;
    }

    // stream() : filtre les emprunts SORTIE non rembourses puis additionne les montants
    public float getTotalEmpruntsSortieActifs() {
        if (emprunts == null || emprunts.isEmpty()) {
            return 0;
        }
        return emprunts.stream()
            .filter(e -> "SORTIE".equalsIgnoreCase(e.getType()) && !e.isRembourse())
            .map(Emprunt::getMontant)
            .reduce(0f, Float::sum);
    }

    // stream() : filtre les emprunts ENTREE non rembourses puis additionne
    public float getTotalEmpruntsEntreeActifs() {
        if (emprunts == null || emprunts.isEmpty()) {
            return 0;
        }
        return emprunts.stream()
            .filter(e -> "ENTREE".equalsIgnoreCase(e.getType()) && !e.isRembourse())
            .map(Emprunt::getMontant)
            .reduce(0f, Float::sum);
    }

    // collect(toList()) : transforme le stream filtre en nouvelle liste
    public List<Emprunt> getEmpruntsSortieActifs() {
        if (emprunts == null || emprunts.isEmpty()) {
            return List.of();
        }
        return emprunts.stream()
            .filter(e -> "SORTIE".equalsIgnoreCase(e.getType()) && !e.isRembourse())
            .collect(Collectors.toList());
    }

    public List<Emprunt> getEmpruntsEntreeActifs() {
        if (emprunts == null || emprunts.isEmpty()) {
            return List.of();
        }
        return emprunts.stream()
            .filter(e -> "ENTREE".equalsIgnoreCase(e.getType()) && !e.isRembourse())
            .collect(Collectors.toList());
    }

    public List<Emprunt> getEmpruntsActifs() {
        if (emprunts == null || emprunts.isEmpty()) {
            return List.of();
        }
        return emprunts.stream()
            .filter(e -> !e.isRembourse())
            .collect(Collectors.toList());
    }

    public boolean peutEmprunter(float montant) {
        return montant <= getSoldeReel() && montant > 0;
    }

    // count() : compte le nombre d'elements apres filtrage
    public int getNombreEmpruntsActifs() {
        if (emprunts == null) {
            return 0;
        }
        return (int) emprunts.stream().filter(e -> !e.isRembourse()).count();
    }
}
