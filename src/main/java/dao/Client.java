package dao;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Client = personne qui dépose un appareil pour réparation
@Entity  // JPA: table client
@Table(name = "client")
@Getter @Setter  // Lombok: getters/setters
@NoArgsConstructor  // Lombok: constructeur vide
@AllArgsConstructor  // Lombok: constructeur complet
public class Client {

    @Id  // Clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incrément
    private Long idClient;

    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    
    // Photo du client (stockée en binaire)
    @Lob  // Large Object pour stocker images
    @Column(columnDefinition = "LONGBLOB")  // Type MySQL pour gros binaires
    private byte[] image;
    
    // Client fidèle = avantages commerciaux
    private boolean fidele;

    // Historique des réparations
    @OneToMany(mappedBy = "client")  // 1 client = N réparations
    private List<Reparation> reparations;
    
    @Override
    public String toString() {
        return nom + " " + prenom;
    }
}
