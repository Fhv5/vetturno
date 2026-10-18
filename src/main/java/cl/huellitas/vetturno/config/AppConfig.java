package cl.huellitas.vetturno.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class AppConfig {
    @Bean
    Clock clock(@Value("${app.time-zone}") String zone) {
        return Clock.system(ZoneId.of(zone));
    }

    @Bean
    LocalValidatorFactoryBean validator(Clock clock) {
        var validator = new LocalValidatorFactoryBean();
        validator.setConfigurationInitializer(config -> config.clockProvider(() -> clock));
        return validator;
    }
}
