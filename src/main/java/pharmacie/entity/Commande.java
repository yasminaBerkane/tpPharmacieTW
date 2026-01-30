package pharmacie.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
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
public class Commande {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(nullable = false)
	@Setter(AccessLevel.NONE) // la clé est auto-générée par la BD, On ne veut pas de "setter"
	private Integer numero;

	@Basic(optional = false)
	@Column(nullable = false)
	@ToString.Exclude
	private LocalDate saisiele = LocalDate.now();

	@Basic(optional = true)
	private LocalDate envoyeele = null;

	// @Max(value=?) @Min(value=?)//if you know range of your decimal fields
	// consider using these annotations to enforce field validation
	@Column(precision = 18, scale = 2)
	@ToString.Exclude
	private BigDecimal port;

	@Size(max = 40)
	@Column(length = 40)
	private String destinataire;

	@Embedded
	private AdressePostale adresseLivraison;

	@Basic(optional = false)
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal remise = BigDecimal.ZERO;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "commande", orphanRemoval = true)
	@JsonIgnoreProperties({"commande"})
	private List<Ligne> lignes = new LinkedList<>();

	@ManyToOne(optional = false)
	@NonNull
	@JsonIgnoreProperties({"commandes"})
	private Dispensaire dispensaire;

}
