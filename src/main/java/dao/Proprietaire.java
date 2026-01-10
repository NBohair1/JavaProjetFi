package dao;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Propriétaire = admin qui possède une ou plusieurs boutiques
// Hérite de Reparateur (peut aussi réparer), 100% de gain
@Entity  // JPA: table proprietaire
@Table(name = "proprietaire")
@Getter @Setter  // Lombok: getters/setters
@NoArgsConstructor  // Lombok: constructeur vide
public class Proprietaire extends Reparateur {

    @OneToMany(mappedBy = "proprietaire", fetch = FetchType.EAGER)  // 1 proprio = N boutiques
    private List<Boutique> boutiques;
    
    // Callback JPA: exécuté avant INSERT
    @PrePersist
    public void initPourcentageGain() {
        if (this.getPourcentageGain() == 0) {
            this.setPourcentageGain(100f);
        }
    }
}
