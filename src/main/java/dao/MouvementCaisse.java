package dao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// MouvementCaisse = historique des transactions
// Types: ENTREE | SORTIE | ALIMENTATION
@Entity  // JPA: table mouvement_caisse
@Table(name = "mouvement_caisse")
@Getter @Setter  // Lombok: getters/setters
@NoArgsConstructor  // Lombok: constructeur vide
@AllArgsConstructor  // Lombok: constructeur complet
public class MouvementCaisse {

    @Id  // Clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incrément
    private Long idMouvement;

    private float montant;
    private String typeMouvement;  // ENTREE | SORTIE | ALIMENTATION
    private String description;

    @Temporal(TemporalType.TIMESTAMP)  // Date + heure
    private Date dateMouvement;

    @ManyToOne  // Plusieurs mouvements par caisse
    @JoinColumn(name = "id_caisse")  // Clé étrangère
    private Caisse caisse;
    
    // Réparation associée (pour paiements clients)
    @ManyToOne
    @JoinColumn(name = "id_reparation")
    private Reparation reparation;
}
