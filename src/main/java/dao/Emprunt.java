package dao;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

// Emprunt = prêt accordé ou reçu
// Types: ENTREE (prêt reçu) | SORTIE (prêt accordé)
// N'affecte pas la caisse système, seulement la caisse réelle
@Entity  // JPA: table emprunt
@Data  // Lombok: @Getter + @Setter + @ToString + @EqualsAndHashCode
@NoArgsConstructor  // Lombok: constructeur vide
@AllArgsConstructor  // Lombok: constructeur complet
public class Emprunt {

    @Id  // Clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incrément
    private Long idEmprunt;

    private float montant;
    private String type;  // ENTREE | SORTIE
    private String commentaire;

    @Temporal(TemporalType.TIMESTAMP)  // Date création
    private Date date;

    @Temporal(TemporalType.TIMESTAMP)  // Date remboursement (null si non remboursé)
    private Date dateRemboursement;

    private boolean rembourse;

    @ManyToOne(optional = false)  // Obligatoire: chaque emprunt lié à un réparateur
    private Reparateur reparateur;
}

