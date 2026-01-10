package dao;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Réparation = cœur du système
// États: EN_ATTENTE → EN_COURS → TERMINEE → LIVREE (ou ANNULEE)
@Entity  // JPA: table reparation
@Table(name = "reparation")
@Getter @Setter  // Lombok: getters/setters
@NoArgsConstructor  // Lombok: constructeur vide
@AllArgsConstructor  // Lombok: constructeur complet
public class Reparation {

    @Id  // Clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incrément
    private Long idReparation;

    // Code unique pour le suivi client (ex: REP-20240115-0001)
    @Column(unique = true)  // Contrainte d'unicité
    private String codeSuivi;

    // État actuel: EN_ATTENTE | EN_COURS | TERMINEE | LIVREE | ANNULEE
    private String etat;
    private String commentaire;
    private float prixTotal;

    @Temporal(TemporalType.TIMESTAMP)  // Date + heure
    private Date dateDepot;
    
    // Date de livraison (renseignée quand état = LIVREE)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateLivraison;

    @ManyToOne  // Plusieurs réparations par client
    private Client client;

    @ManyToOne
    private Reparateur reparateur;

    // Appareils concernés par cette réparation
    @OneToMany(mappedBy = "reparation", cascade = CascadeType.ALL)
    private List<Appareil> appareils;

    // Composants utilisés pour la réparation
    @OneToMany(cascade = CascadeType.ALL)
    private List<Composant> composants;
    
    // Reçu de paiement
    @OneToOne(mappedBy = "reparation", cascade = CascadeType.ALL)
    private Recu recu;
}
