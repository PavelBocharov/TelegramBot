package com.mar.telegram.db.conf;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return (flywayOld) -> {
            Flyway flyway = Flyway.configure()
                    .configuration(flywayOld.getConfiguration())
//                    .callbacks(new FlywayDatabaseSeeder())
                    .load();

            if (!"local".equalsIgnoreCase(activeProfile)) {
                flyway.migrate();
            }

        };
    }

}
