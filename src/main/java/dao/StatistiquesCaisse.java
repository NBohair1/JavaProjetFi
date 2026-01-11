package dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO pour les statistiques de caisse d'un réparateur
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesCaisse {
    
    private float caisse;          // Caisse système
    private float caisseReelle;    // Caisse réelle (- emprunts)
    private float totalEmprunts;
    private int nombreEmpruntsActifs;
    private float revenuTotal;
    private float revenusPeriode;
    private int nombreReparations;
    private int nombreReparationsTerminees;
    
    @Override
    public String toString() {
        return String.format(
            "Caisse: %.2f DH | Réelle: %.2f DH | Emprunts: %.2f DH",
            caisse, caisseReelle, totalEmprunts
        );
    }
}
