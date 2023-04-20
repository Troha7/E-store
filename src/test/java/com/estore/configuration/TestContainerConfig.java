package com.estore.configuration;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * {@link TestContainerConfig}
 *
 * @author Dmytro Trotsenko on 4/17/23
 */

public class TestContainerConfig implements BeforeAllCallback, AfterAllCallback {

    private final String POSTGRES_IMAGE = "postgres:14.6";

    @Override
    public void beforeAll(ExtensionContext context) {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(POSTGRES_IMAGE);
        container.start();

        String url = String.format(":postgresql://%s:%d/%s",
                container.getHost(),
                container.getFirstMappedPort(),
                container.getDatabaseName());

        // Test container PostgreSQL property
        System.setProperty("spring.r2dbc.url", "r2dbc" + url);
        System.setProperty("spring.r2dbc.username", container.getUsername());
        System.setProperty("spring.r2dbc.password", container.getPassword());

        // Flyway property
        System.setProperty("spring.flyway.url", "jdbc" + url);
        System.setProperty("spring.flyway.user", container.getUsername());
        System.setProperty("spring.flyway.password", container.getPassword());
    }

    @Override
    public void afterAll(ExtensionContext context) {
        // do nothing, Testcontainers handles container shutdown
    }

}
