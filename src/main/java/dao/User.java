package dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Classe mère pour les utilisateurs (héritage JOINED)
// Héritiers: Reparateur, Proprietaire
@Entity  // JPA: classe persistante en base
@Inheritance(strategy = InheritanceType.JOINED)  // Héritage: une table par classe
@Getter @Setter  // Lombok: génère getters/setters
@NoArgsConstructor  // Lombok: constructeur vide
@AllArgsConstructor  // Lombok: constructeur avec tous les champs
public abstract class User {

    @Id  // Clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incrément
    protected Long id;

    protected String nom;
    protected String prenom;

    @Column(unique = true)  // Email unique pour login
    protected String email;

    protected String mdp;
}
