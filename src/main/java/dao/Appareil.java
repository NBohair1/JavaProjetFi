package dao;

import lombok.*;
import javax.persistence.*;

// Appareil = téléphone/tablette à réparer
// Identifié par son IMEI unique
@Entity  // JPA: table appareil
@Table(name = "appareil")
@Getter @Setter  // Lombok: getters/setters
@NoArgsConstructor  // Lombok: constructeur vide
@AllArgsConstructor  // Lombok: constructeur complet
public class Appareil {

    @Id  // Clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incrément
    private Long idAppareil;

    @Column(unique = true)  // IMEI unique
    private String imei;

    private String marque;
    private String modele;
    private String typeAppareil;

    @ManyToOne  // Plusieurs appareils par réparation
    private Reparation reparation;
}
