package pharmacie.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter @Setter @NoArgsConstructor @RequiredArgsConstructor @ToString
@Table(uniqueConstraints = {
	@UniqueConstraint(columnNames = {"COMMANDE_NUMERO", "MEDICAMENT_REFERENCE"})
})
public class Ligne {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(nullable = false)
	@Setter(AccessLevel.NONE) // la clé est auto-générée par la BD, On ne veut pas de "setter"
	private Integer id;

	@JoinColumn(nullable = false)
	@ManyToOne(optional = false)
	@NonNull
	@JsonIgnoreProperties({"lignes", "dispensaire"})
	private Commande commande;

	@JoinColumn(nullable = false)
	@ManyToOne(optional = false)
	@NonNull
	@JsonIgnoreProperties({"lignes", "categorie"})
	private Medicament medicament;

	@Basic(optional = false)
	@Column(nullable = false)
	@NonNull
	private Integer quantite;

}
