package dao;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Réparateur = employé d'une boutique
// Hérite de User, possède une caisse et un pourcentage de gain
@Entity  // JPA: table reparateur
@Table(name = "reparateur")  // Nom de la table en base
@Getter @Setter  // Lombok: getters/setters
@NoArgsConstructor  // Lombok: constructeur vide (requis JPA)
@AllArgsConstructor  // Lombok: constructeur complet
public class Reparateur extends User {

    // Pourcentage des gains attribué au réparateur (ex: 30%)
    private float pourcentageGain;

    // Boutique où travaille le réparateur
    @ManyToOne  // Plusieurs réparateurs par boutique
    @JoinColumn(name = "id_boutique")  // Clé étrangère
    private Boutique boutique;

    // Caisse personnelle du réparateur
    @OneToOne(cascade = CascadeType.ALL)  // 1 réparateur = 1 caisse
    @JoinColumn(name = "id_caisse")
    private Caisse caisse;
}
