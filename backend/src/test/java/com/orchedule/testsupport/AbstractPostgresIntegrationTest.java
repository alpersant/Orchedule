package com.orchedule.testsupport;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/** Base de los tests de integración contra PostgreSQL real. Cada test parte de una BD vacía. */
@Tag("integration")
public abstract class AbstractPostgresIntegrationTest {

    protected static NamedParameterJdbcTemplate db;
    protected static TransactionTemplate tx;

    protected PlanningFixtures data;

    @BeforeAll
    static void startDatabase() {
        PostgresTestSupport.ensureStarted();
        db = PostgresTestSupport.jdbc();
        tx = PostgresTestSupport.transactions();
    }

    @BeforeEach
    void cleanDatabase() {
        PostgresTestSupport.resetData();
        data = new PlanningFixtures(db);
    }
}
