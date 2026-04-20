package paie.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql:
    private static final String USER     = "root";
    private static final String PASSWORD = "root";

    private static volatile DatabaseConnection instance = null;
    private Connection connection;

    private DatabaseConnection() throws SQLException {
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Connexion établie : " + URL);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC introuvable : " + e.getMessage());
        }
    }

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

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

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
