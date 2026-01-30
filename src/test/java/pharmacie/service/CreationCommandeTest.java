package pharmacie.service;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import pharmacie.dao.DispensaireRepository;
import pharmacie.dao.LigneRepository;
import pharmacie.dao.MedicamentRepository;

@SpringBootTest
 // Ce test est basé sur le jeu de données dans "test_data.sql"
class CreationCommandeTest {
    private static final String ID_PETIT_CLIENT = "0COM";
    private static final String ID_GROS_CLIENT = "2COM";
    private static final BigDecimal REMISE_POUR_GROS_CLIENT = new BigDecimal("0.15");

    @Autowired
    private CommandeService service;
    @Autowired
    private DispensaireRepository daoClient;
    @Autowired
    private MedicamentRepository daoMedicament;

    @Autowired
    private LigneRepository ligneDao;

    @Test
    void testCreerCommandePourGrosClient() {
        var commande = service.creerCommande(ID_GROS_CLIENT);
        assertNotNull(commande.getNumero(), "On doit avoir la clé de la commande");
        assertEquals(REMISE_POUR_GROS_CLIENT, commande.getRemise(),
            "Une remise de 15% doit être appliquée pour les gros clients");
    }

    @Test
    void testCreerCommandePourPetitClient() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertNotNull(commande.getNumero());
        assertEquals(BigDecimal.ZERO, commande.getRemise(),
            "Aucune remise ne doit être appliquée pour les petits clients");
    }

    @Test
    void testCreerCommandeInitialiseAdresseLivraison() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        var client = daoClient.findById(ID_PETIT_CLIENT).orElseThrow();
        assertEquals(client.getAdresse(), commande.getAdresseLivraison(),
            "On doit recopier l'adresse du client dans l'adresse de livraison");
    }

    @Test
    void testCreerCommandePourClientInexistant() {
        try {
            service.creerCommande("XXX");
        } catch (java.util.NoSuchElementException ex) {
            // OK : on doit avoir une exception car le client n'existe pas
            return;
        }
        throw new AssertionError("On doit avoir une exception si le client n'existe pas");
    }

    @Test
    void testAjouterLigneCommande() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        int commandeNum = commande.getNumero();

        int medicamentRef = 93;
        int quantite = 10;

        var medicamentAvant = daoMedicament.findById(medicamentRef).orElseThrow();
        int unitesCommandeesAvant = medicamentAvant.getUnitesCommandees();

        var ligne = service.ajouterLigne(commandeNum, medicamentRef, quantite);

        assertNotNull(ligne, "La ligne de commande doit être créée");
        assertNotNull(ligne.getId(), "La ligne doit avoir un ID généré");
        assertEquals(quantite, ligne.getQuantite(), "La quantité doit être correcte");

        var medicamentApres = daoMedicament.findById(medicamentRef).orElseThrow();
        assertEquals(unitesCommandeesAvant + quantite, medicamentApres.getUnitesCommandees(),
                "Les unités commandées doivent être incrémentées de la quantité ajoutée");
    }

    @Test
    void testAjouterLigneMedicamentInexistant() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertThrows(NoSuchElementException.class, () -> service.ajouterLigne(commande.getNumero(), 999, 10),
                "Doit lancer NoSuchElementException si le médicament n'existe pas");
    }

    @Test
    void testAjouterLigneCommandeInexistante() {
        assertThrows(NoSuchElementException.class, () -> service.ajouterLigne(999999, 93, 10),
                "Doit lancer NoSuchElementException si la commande n'existe pas");
    }

    @Test
    void testAjouterLigneCommandeDejaEnvoyee() {
        // Utilise la commande 99999 qui est déjà envoyée
        assertThrows(IllegalStateException.class, () -> service.ajouterLigne(99999, 93, 10),
                "Doit lancer IllegalStateException si la commande est déjà envoyée");
    }

    @Test
    void testAjouterLigneStockInsuffisant() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        // Utilise le médicament 98 qui a unitesEnStock=26, unitesCommandees=20,
        // disponible=6
        assertThrows(IllegalStateException.class, () -> service.ajouterLigne(commande.getNumero(), 98, 10),
                "Doit lancer IllegalStateException si le stock est insuffisant");
    }

    @Test
    void testAjouterLigneMedicamentDejaPresent() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        int commandeNum = commande.getNumero();
        int medicamentRef = 93;
        int quantite1 = 5;
        int quantite2 = 3;

        service.ajouterLigne(commandeNum, medicamentRef, quantite1);
        service.ajouterLigne(commandeNum, medicamentRef, quantite2);

        var lignes = ligneDao.findByCommandeNumero(commandeNum);
        int totalQuantite = lignes.stream()
                .filter(l -> l.getMedicament().getReference().equals(medicamentRef))
                .mapToInt(l -> l.getQuantite())
                .sum();
        assertEquals(quantite1 + quantite2, totalQuantite,
                "Les quantités doivent être additionnées si le médicament est déjà présent");
    }

    @Test
    void testSupprimerLigneCommande() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        int commandeNum = commande.getNumero();
        int medicamentRef = 93;
        int quantite = 5;

        var ligne = service.ajouterLigne(commandeNum, medicamentRef, quantite);
        assertNotNull(ligne, "La ligne de commande doit être créée");

        service.supprimerLigne(ligne.getId());

        var lignes = ligneDao.findByCommandeNumero(commandeNum);
        boolean ligneExiste = lignes.stream()
                .anyMatch(l -> l.getId().equals(ligne.getId()));
        assertEquals(false, ligneExiste, "La ligne de commande doit être supprimée");
    }

    @Test
    void testSupprimerLigneCommandeInexistante() {
        assertThrows(NoSuchElementException.class, () -> service.supprimerLigne(999999),
                "Doit lancer NoSuchElementException si la ligne de commande n'existe pas");
    }

    @Test
    void testSupprimerLigneCommandeDejaEnvoyee() {
        var ligne = ligneDao.findByCommandeNumero(99999).get(0);
        assertThrows(IllegalStateException.class, () -> service.supprimerLigne(ligne.getId()),
                "Doit lancer IllegalStateException si la commande est déjà envoyée");
    }

    @Test
    void testSupprimerLigneCommandeDecrementeUnitesCommandees() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        int commandeNum = commande.getNumero();
        int medicamentRef = 93;
        int quantite = 7;

        var medicamentAvant = daoMedicament.findById(medicamentRef).orElseThrow();
        int unitesCommandeesAvant = medicamentAvant.getUnitesCommandees();

        var ligne = service.ajouterLigne(commandeNum, medicamentRef, quantite);
        assertNotNull(ligne, "La ligne de commande doit être créée");

        service.supprimerLigne(ligne.getId());

        var medicamentApres = daoMedicament.findById(medicamentRef).orElseThrow();
        assertEquals(unitesCommandeesAvant, medicamentApres.getUnitesCommandees(),
                "Les unités commandées doivent être décrémentées de la quantité supprimée");
    }

    @Test
    void testEnregistreExpeditionCommande() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        int commandeNum = commande.getNumero();
        service.enregistreExpedition(commandeNum);
        var commandeApres = service.getCommande(commandeNum);
        assertNotNull(commandeApres.getEnvoyeele(),
                "La date d'envoi doit être renseignée après l'expédition"); 
    }

    @Test
    void testEnregistreExpeditionCommandeDejaEnvoyee() {
        // Utilise la commande 99999 qui est déjà envoyée
        assertThrows(IllegalStateException.class, () -> service.enregistreExpedition(99999),
                "Doit lancer IllegalStateException si la commande est déjà envoyée");
    }

    @Test
    void testEnregistreExpeditionCommandeInexistante() {
        assertThrows(NoSuchElementException.class, () -> service.enregistreExpedition(999999),
                "Doit lancer NoSuchElementException si la commande n'existe pas");
    }

    @Test
    void testEnregistreExpeditionDecrementeStocks() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        int commandeNum = commande.getNumero();
        int medicamentRef = 93;
        int quantite = 8;

        var medicamentAvant = daoMedicament.findById(medicamentRef).orElseThrow();
        int unitesEnStockAvant = medicamentAvant.getUnitesEnStock();
        int unitesCommandeesAvant = medicamentAvant.getUnitesCommandees();

        service.ajouterLigne(commandeNum, medicamentRef, quantite);
        service.enregistreExpedition(commandeNum);

        var medicamentApres = daoMedicament.findById(medicamentRef).orElseThrow();
        assertEquals(unitesEnStockAvant - quantite, medicamentApres.getUnitesEnStock(),
                "Les unités en stock doivent être décrémentées de la quantité expédiée");
        assertEquals(unitesCommandeesAvant, medicamentApres.getUnitesCommandees(),
                "Les unités commandées doivent être décrémentées de la quantité expédiée");
    }


}
