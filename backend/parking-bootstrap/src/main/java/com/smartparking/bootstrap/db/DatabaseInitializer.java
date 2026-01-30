package com.smartparking.bootstrap.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Optionally initializes database schema on startup for local development.
 */
@Component
public class DatabaseInitializer {
    public DatabaseInitializer(DataSource dataSource,
                               ResourceLoader resourceLoader,
                               @Value("${db.initSchema:false}") boolean initSchema,
                               @Value("${db.schemaLocation:classpath:db/schema-mysql.sql}") String schemaLocation) {
        if (!initSchema) {
            return;
        }
        Resource schema = resourceLoader.getResource(schemaLocation);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(schema);
        populator.setContinueOnError(false);
        populator.execute(dataSource);
    }
}

