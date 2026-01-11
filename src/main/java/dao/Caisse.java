package dao;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Caisse = gestion financière d'un réparateur
// Caisse système = soldeActuel | Caisse réelle = solde - emprunts SORTIE
@Entity  // JPA: table caisse
@Table(name = "caisse")
@Getter @Setter  // Lombok: getters/setters
@NoArgsConstructor  // Lombok: constructeur vide
@AllArgsConstructor  // Lombok: constructeur complet
public class Caisse {

    @Id  // Clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incrément
    private Long idCaisse;

    // Solde actuel (caisse système)
    private float soldeActuel;

    @Temporal(TemporalType.TIMESTAMP)  // Date + heure dernier mouvement
    private Date dernierMouvement;
    
    // Historique des mouvements
    @OneToMany(mappedBy = "caisse", cascade = CascadeType.ALL)
    private List<MouvementCaisse> mouvements;
}
