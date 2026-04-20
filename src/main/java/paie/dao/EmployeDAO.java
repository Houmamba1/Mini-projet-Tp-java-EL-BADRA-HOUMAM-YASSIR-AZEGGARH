package paie.dao;

import paie.exception.InvalidWorkDataException;
import paie.model.Employe;
import paie.model.EmployeFixe;
import paie.model.EmployeHoraire;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * EmployeDAO — Couche d'accès aux données (pattern DAO).
 *
 * Responsabilités :
 *  - Persistance des objets Employe vers la table SQL.
 *  - Reconstruction des objets depuis la base (instanciation dynamique).
 *  - Requêtes d'agrégation (masse salariale par département).
 *  - Mise à jour massive des salaires.
 */
public class EmployeDAO {

    private final DatabaseConnection dbConn;

    public EmployeDAO() throws SQLException {
        this.dbConn = DatabaseConnection.getInstance();
    }

    // =======================================================
    // 1. SAVE — Insertion avec mapping LocalDate → java.sql.Date
    // =======================================================

    /**
     * Insère un employé en base de données.
     *
     * Mapping temporel :
     *   java.time.LocalDate  →  java.sql.Date.valueOf(localDate)
     *   Cette conversion est directe car LocalDate est sans fuseau horaire,
     *   tout comme le type DATE SQL. On évite ainsi toute dérive liée aux
     *   TimeZones que l'on aurait avec java.util.Date.
     *
     * @param e L'employé à persister (EmployeFixe ou EmployeHoraire).
     */
    public void save(Employe e) throws SQLException {
        String sql = """
            INSERT INTO employes
              (nom, email, departement, date_embauche, type,
               salaire_base, prime_perf, taux_horaire, heures_travail)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = dbConn.getConnection().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getNom());
            ps.setString(2, e.getEmail());
            ps.setString(3, e.getDepartement());

            // ---- Conversion LocalDate → java.sql.Date ----
            // java.sql.Date.valueOf() est la méthode idiomatique Java 8+
            // Elle préserve exactement l'année/mois/jour sans manipulation de timezone.
            ps.setDate(4, java.sql.Date.valueOf(e.getDateEmbauche()));

            // ---- Instanciation dynamique selon le type ----
            if (e instanceof EmployeFixe fixe) {
                ps.setString(5, "FIXE");
                ps.setDouble(6, fixe.getSalaireBase());
                ps.setDouble(7, fixe.getPrimePerformance());
                ps.setNull(8, Types.DOUBLE);
                ps.setNull(9, Types.DOUBLE);
            } else if (e instanceof EmployeHoraire horaire) {
                ps.setString(5, "HORAIRE");
                ps.setNull(6, Types.DOUBLE);
                ps.setNull(7, Types.DOUBLE);
                ps.setDouble(8, horaire.getTauxHoraire());
                ps.setDouble(9, horaire.getHeuresTravaillees());
            } else {
                throw new SQLException("Type d'employé inconnu : " + e.getClass().getName());
            }

            ps.executeUpdate();

            // Récupération de l'ID auto-généré
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    e.setId(keys.getInt(1));
                }
            }
            System.out.println("[DAO] Employé sauvegardé : " + e.getNom() + " (id=" + e.getId() + ")");
        }
    }

    // =======================================================
    // 2. FIND ALL — Récupération avec instanciation dynamique
    // =======================================================

    /**
     * Récupère tous les employés depuis la base de données.
     *
     * Instanciation dynamique :
     *   La colonne 'type' (FIXE | HORAIRE) pilote quelle sous-classe instancier.
     *   On utilise un switch sur la valeur String pour éviter tout instanceof.
     *   Le polymorphisme prend ensuite le relais : chaque objet retourné
     *   implémente IPaye via sa propre logique de calcul.
     *
     * @return Liste de tous les employés (mélange EmployeFixe et EmployeHoraire).
     */
    public List<Employe> findAll() throws SQLException {
        List<Employe> employes = new ArrayList<>();
        String sql = "SELECT * FROM employes ORDER BY id";

        try (Statement st = dbConn.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                try {
                    Employe emp = mapResultSet(rs);
                    employes.add(emp);
                } catch (InvalidWorkDataException e) {
                    System.err.println("[DAO] Données invalides pour l'employé id="
                        + rs.getInt("id") + " : " + e.getMessage());
                }
            }
        }
        return employes;
    }

    /**
     * Trouve un employé par son identifiant.
     */
    public Optional<Employe> findById(int id) throws SQLException {
        String sql = "SELECT * FROM employes WHERE id = ?";

        try (PreparedStatement ps = dbConn.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    try {
                        return Optional.of(mapResultSet(rs));
                    } catch (InvalidWorkDataException e) {
                        System.err.println("[DAO] Données corrompues : " + e.getMessage());
                    }
                }
            }
        }
        return Optional.empty();
    }

    // =======================================================
    // 3. MASSE SALARIALE PAR DÉPARTEMENT — Agrégation SQL
    // =======================================================

    /**
     * Retourne la somme des salaires nets groupée par département.
     *
     * Note technique :
     *   Le calcul du net est effectué en Java (car il dépend de la logique métier
     *   et de l'ancienneté). On récupère toutes les données et on agrège en mémoire
     *   après appel polymorphique de calculerNetAPayer().
     *
     *   Alternative pure SQL (si le calcul est encodé en base) :
     *   SELECT departement, SUM(salaire_net) FROM employes GROUP BY departement
     *
     * @return Map département → masse salariale nette totale.
     */
    public Map<String, Double> getMasseSalarialeParDept() throws SQLException {
        Map<String, Double> masse = new TreeMap<>();
        List<Employe> tous = findAll();

        for (Employe emp : tous) {
            masse.merge(
                emp.getDepartement(),
                emp.calculerNetAPayer(),    // appel polymorphique
                Double::sum
            );
        }
        return masse;
    }

    /**
     * Version SQL pure avec GROUP BY (si salaire_net est stocké en base).
     * Démonstration de la requête SQL d'agrégation demandée dans le projet.
     */
    public Map<String, Double> getMasseSalarialeParDeptSQL() throws SQLException {
        Map<String, Double> masse = new TreeMap<>();

        // Calcul du brut approximatif directement en SQL pour démonstration
        String sql = """
            SELECT departement,
                   SUM(
                     CASE type
                       WHEN 'FIXE'    THEN COALESCE(salaire_base,0) + COALESCE(prime_perf,0)
                       WHEN 'HORAIRE' THEN
                         CASE WHEN heures_travail <= 180
                              THEN heures_travail * taux_horaire
                              ELSE (180 * taux_horaire) + ((heures_travail - 180) * taux_horaire * 1.25)
                         END
                     END * 0.80
                   ) AS masse_nette
            FROM employes
            GROUP BY departement
            ORDER BY departement
        """;

        try (Statement st = dbConn.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                masse.put(rs.getString("departement"), rs.getDouble("masse_nette"));
            }
        }
        return masse;
    }

    // =======================================================
    // 4. AUGMENTATION MASSIVE DES SALAIRES FIXES
    // =======================================================

    /**
     * Augmente le salaire de base de tous les employés fixes d'un pourcentage donné.
     * Opération de mise à jour massive via une seule requête SQL UPDATE.
     *
     * @param pourcentage Ex: 0.05 pour 5% d'augmentation.
     */
    public int augmenterSalaireBase(double pourcentage) throws SQLException {
        String sql = "UPDATE employes SET salaire_base = salaire_base * ? WHERE type = 'FIXE'";

        try (PreparedStatement ps = dbConn.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, 1 + pourcentage);
            int lignesMaj = ps.executeUpdate();
            System.out.printf("[DAO] %d employé(s) fixe(s) augmenté(s) de %.1f%%\n",
                lignesMaj, pourcentage * 100);
            return lignesMaj;
        }
    }

    // =======================================================
    // 5. DELETE
    // =======================================================

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM employes WHERE id = ?";
        try (PreparedStatement ps = dbConn.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // =======================================================
    // MÉTHODE PRIVÉE : mapping ResultSet → objet Employe
    // =======================================================

    /**
     * Instanciation dynamique basée sur la colonne 'type'.
     *
     * Le switch sur la valeur String évite tout instanceof :
     *  - "FIXE"    → EmployeFixe    (salaire_base + prime_perf)
     *  - "HORAIRE" → EmployeHoraire (taux_horaire + heures_travail)
     *
     * Mapping temporel inverse :
     *   rs.getDate("date_embauche").toLocalDate()
     *   java.sql.Date.toLocalDate() est la conversion inverse idiomatique.
     */
    private Employe mapResultSet(ResultSet rs) throws SQLException, InvalidWorkDataException {
        int    id          = rs.getInt("id");
        String nom         = rs.getString("nom");
        String email       = rs.getString("email");
        String departement = rs.getString("departement");

        // ---- Conversion DATE SQL → LocalDate ----
        // rs.getDate() retourne un java.sql.Date.
        // .toLocalDate() donne un LocalDate sans information de timezone.
        LocalDate dateEmbauche = rs.getDate("date_embauche").toLocalDate();

        String type = rs.getString("type");

        // ---- Switch d'instanciation dynamique ----
        return switch (type) {
            case "FIXE" -> new EmployeFixe(
                id, nom, email, departement, dateEmbauche,
                rs.getDouble("salaire_base"),
                rs.getDouble("prime_perf")
            );
            case "HORAIRE" -> new EmployeHoraire(
                id, nom, email, departement, dateEmbauche,
                rs.getDouble("taux_horaire"),
                rs.getDouble("heures_travail")
            );
            default -> throw new SQLException("Type inconnu en base : " + type);
        };
    }
}
