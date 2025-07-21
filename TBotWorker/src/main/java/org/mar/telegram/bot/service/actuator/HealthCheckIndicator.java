package org.mar.telegram.bot.service.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up().build();
    }

}