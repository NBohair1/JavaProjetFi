package dao;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Reçu = justificatif de paiement généré à la livraison
@Entity  // JPA: table recu
@Table(name = "recu")
@Getter @Setter  // Lombok: getters/setters
@NoArgsConstructor  // Lombok: constructeur vide
@AllArgsConstructor  // Lombok: constructeur complet
public class Recu {

    @Id  // Clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incrément
    private Long idRecu;

    @Column(unique = true)  // Numéro reçu unique
    private String numeroRecu;

    private float montant;
    private String modePaiement;  // CASH uniquement

    @Temporal(TemporalType.TIMESTAMP)  // Date + heure
    private Date date;
    
    @OneToOne  // 1 reçu = 1 réparation
    @JoinColumn(name = "id_reparation")  // Clé étrangère
    private Reparation reparation;
    
    @ManyToOne
    @JoinColumn(name = "id_client")
    private Client client;
    
    @ManyToOne
    @JoinColumn(name = "id_reparateur")
    private Reparateur reparateur;
}
