package paie.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton thread-safe pour la connexion JDBC.
 *
 * Configuration : modifier les constantes URL, USER, PASSWORD
 * selon votre environnement (MySQL, PostgreSQL, H2…).
 *
 * Pattern Singleton avec double-checked locking (thread-safe).
 */
public class DatabaseConnection {

    // -------------------------------------------------------
    // Paramètres de connexion — à adapter à votre SGBD
    // -------------------------------------------------------
    private static final String URL      = "jdbc:mysql://localhost:3306/paie_db?useSSL=false&serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "root";

    // Instance unique (volatile pour la visibilité entre threads)
    private static volatile DatabaseConnection instance = null;
    private Connection connection;

    // -------------------------------------------------------
    // Constructeur privé : charge le driver et ouvre la connexion
    // -------------------------------------------------------
    private DatabaseConnection() throws SQLException {
        try {
            // Chargement explicite du driver (inutile avec JDBC 4+ mais explicite ici)
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Connexion établie : " + URL);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC introuvable : " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Accès au Singleton (double-checked locking)
    // -------------------------------------------------------
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    // -------------------------------------------------------
    // Récupération de la connexion (re-connexion si fermée)
    // -------------------------------------------------------
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    // -------------------------------------------------------
    // Fermeture propre
    // -------------------------------------------------------
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connexion fermée.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Erreur lors de la fermeture : " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Script de création de la table (utilitaire)
    // -------------------------------------------------------
    public static String getCreateTableSQL() {
        return """
            CREATE TABLE IF NOT EXISTS employes (
                id              INT PRIMARY KEY AUTO_INCREMENT,
                nom             VARCHAR(100)   NOT NULL,
                email           VARCHAR(150)   NOT NULL UNIQUE,
                departement     VARCHAR(100)   NOT NULL,
                date_embauche   DATE           NOT NULL,
                type            ENUM('FIXE','HORAIRE') NOT NULL,
                salaire_base    DOUBLE,
                prime_perf      DOUBLE,
                taux_horaire    DOUBLE,
                heures_travail  DOUBLE
            );
        """;
    }
}
