package dao;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO pour les statistiques financières globales de la boutique
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesFinancieres {
    
    private float revenuTotalBoutique;
    private float totalCaissesBoutique;
    private int nombreTotalReparations;
    private int nombreReparateursActifs;
    
    private Map<Long, Float> revenusParReparateur = new HashMap<>();
    private Map<Long, Float> pourcentagesParReparateur = new HashMap<>();
    private Map<Long, Integer> nombreReparationsParReparateur = new HashMap<>();
    
    public void ajouterRevenusReparateur(Long reparateurId, float revenus) {
        revenusParReparateur.put(reparateurId, revenus);
    }
    
    public void ajouterPourcentageReparateur(Long reparateurId, float pourcentage) {
        pourcentagesParReparateur.put(reparateurId, pourcentage);
    }
    
    public void ajouterNombreReparationsReparateur(Long reparateurId, int nombre) {
        nombreReparationsParReparateur.put(reparateurId, nombre);
    }
}
