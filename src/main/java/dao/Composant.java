package dao;

import lombok.*;
import javax.persistence.*;

// Composant = pièce détachée pour réparations
@Entity  // JPA: table composant
@Table(name = "composant")
@Getter @Setter  // Lombok: getters/setters
@NoArgsConstructor  // Lombok: constructeur vide
@AllArgsConstructor  // Lombok: constructeur complet
public class Composant {

    @Id  // Clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incrément
    private Long idComposant;

    @Column(nullable = false)  // Champ obligatoire
    private String nom;

    private float prix;      // Prix de vente
    private float prixAchat; // Prix d'achat
    private int quantite;    // Stock
    
    @Column(nullable = false)  // Champ obligatoire
    private String typeAppareil;  // Appareil compatible (ex: iPhone, Samsung, Universel)
}
