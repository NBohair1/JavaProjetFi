package dao;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Boutique de réparation
// Appartient à un propriétaire, emploie plusieurs réparateurs
@Entity  // JPA: table boutique
@Table(name = "boutique")
@Getter @Setter  // Lombok: getters/setters
@NoArgsConstructor  // Lombok: constructeur vide
@AllArgsConstructor  // Lombok: constructeur complet
public class Boutique {

    @Id  // Clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incrément
    private Long idBoutique;

    private String nom;
    private String adresse;
    private String numTelephone;
    private String numP;  // Numéro de patente

    @ManyToOne  // Plusieurs boutiques par propriétaire
    private Proprietaire proprietaire;

    // Liste des réparateurs (EAGER pour éviter LazyInitializationException)
    @OneToMany(mappedBy = "boutique", fetch = FetchType.EAGER)  // 1 boutique = N réparateurs
    private List<Reparateur> reparateurs;
}
