package pharmacie.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@ToString
public class Dispensaire {

    @Id
    @Basic(optional = false)
    @NonNull
    @Size(min = 1, max = 5)
    @Column(nullable = false, length = 5)
    private String code;

    @Basic(optional = false)
    @NonNull
    @Size(min = 1, max = 40)
    @Column(nullable = false, length = 40)
    private String nom;

    @Size(max = 30)
    @Column(length = 30)
    private String contact;

    @Size(max = 30)
    @Column(length = 30)
    private String fonction;

    @Embedded
    private AdressePostale adresse;

    @Size(max = 24)
    @Column(length = 24)
    private String telephone;

    @Size(max = 24)
    @Column(length = 24)
    private String fax;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dispensaire")
    @ToString.Exclude
    @JsonIgnoreProperties({"dispensaire", "lignes"})
    private List<Commande> commandes = new ArrayList<>();

}
