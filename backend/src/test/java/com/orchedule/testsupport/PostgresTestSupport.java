package com.orchedule.testsupport;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Assumptions;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;

/**
 * Arranca una única instancia de PostgreSQL por JVM y aplica las migraciones Flyway reales.
 *
 * <p>Si Docker no está disponible, los tests de integración se omiten, salvo que se ejecute con
 * {@code -Dorchedule.it.requireDocker=true} (recomendado en CI), en cuyo caso fallan.</p>
 */
public final class PostgresTestSupport {

    private static final String IMAGE = "postgres:17";
    private static final String REQUIRE_DOCKER_PROPERTY = "orchedule.it.requireDocker";

    private static PostgreSQLContainer container;
    private static DataSource dataSource;
    private static NamedParameterJdbcTemplate jdbc;
    private static TransactionTemplate transactions;

    private PostgresTestSupport() {
    }

    public static synchronized void ensureStarted() {
        if (dataSource != null) {
            return;
        }

        boolean dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        if (!dockerAvailable && Boolean.getBoolean(REQUIRE_DOCKER_PROPERTY)) {
            throw new IllegalStateException("Docker es obligatorio para los tests de integración");
        }
        Assumptions.assumeTrue(dockerAvailable, "Docker no está disponible: tests de integración omitidos");

        container = new PostgreSQLContainer(IMAGE);
        container.start();

        DriverManagerDataSource ds = new DriverManagerDataSource(
                container.getJdbcUrl(), container.getUsername(), container.getPassword());

        Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .load()
                .migrate();

        dataSource = ds;
        jdbc = new NamedParameterJdbcTemplate(ds);
        transactions = new TransactionTemplate(new DataSourceTransactionManager(ds));
    }

    public static NamedParameterJdbcTemplate jdbc() {
        return jdbc;
    }

    public static TransactionTemplate transactions() {
        return transactions;
    }

    /** Vacía los datos de negocio conservando el esquema y el historial de Flyway. */
    public static void resetData() {
        jdbc.getJdbcTemplate().execute(
                "TRUNCATE TABLE availability_rule, competition, season, team, venue CASCADE");
    }
}
