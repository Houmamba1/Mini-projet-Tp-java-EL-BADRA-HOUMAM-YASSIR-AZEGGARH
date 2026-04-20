package paie.dao;

import paie.exception.InvalidWorkDataException;
import paie.model.Employe;
import paie.model.EmployeFixe;
import paie.model.EmployeHoraire;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class EmployeDAO {

    private final DatabaseConnection dbConn;

    public EmployeDAO() throws SQLException {
        this.dbConn = DatabaseConnection.getInstance();
    }

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

            ps.setDate(4, java.sql.Date.valueOf(e.getDateEmbauche()));

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

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    e.setId(keys.getInt(1));
                }
            }
            System.out.println("[DAO] Employé sauvegardé : " + e.getNom() + " (id=" + e.getId() + ")");
        }
    }

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

    public Map<String, Double> getMasseSalarialeParDept() throws SQLException {
        Map<String, Double> masse = new TreeMap<>();
        List<Employe> tous = findAll();

        for (Employe emp : tous) {
            masse.merge(
                emp.getDepartement(),
                emp.calculerNetAPayer(),    
                Double::sum
            );
        }
        return masse;
    }

    public Map<String, Double> getMasseSalarialeParDeptSQL() throws SQLException {
        Map<String, Double> masse = new TreeMap<>();

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

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM employes WHERE id = ?";
        try (PreparedStatement ps = dbConn.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Employe mapResultSet(ResultSet rs) throws SQLException, InvalidWorkDataException {
        int    id          = rs.getInt("id");
        String nom         = rs.getString("nom");
        String email       = rs.getString("email");
        String departement = rs.getString("departement");

        LocalDate dateEmbauche = rs.getDate("date_embauche").toLocalDate();

        String type = rs.getString("type");

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
