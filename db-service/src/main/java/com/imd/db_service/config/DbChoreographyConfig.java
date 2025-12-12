package com.imd.db_service.config;

import com.imd.common.events.EmployeeApproved;
import com.imd.common.events.EmployeeEvent;
import com.imd.common.events.SalaryValidated;
import com.imd.db_service.model.Employee;
import com.imd.db_service.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Configuration
@Profile("choreography")
public class DbChoreographyConfig {

    private static final Logger log = LoggerFactory.getLogger(DbChoreographyConfig.class);
    private final EmployeeService employeeService;

    public DbChoreographyConfig(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Bean
    public Function<Flux<EmployeeEvent>, Flux<EmployeeEvent>> persistEmployee() {
        return flux -> flux
                .flatMap(event -> {
                    if (event instanceof SalaryValidated validatedEvent) {
                        log.info("DB: Recebido SalaryValidated para: {}", validatedEvent.name());

                        Employee entity = new Employee(
                                validatedEvent.name(),
                                validatedEvent.position(),
                                validatedEvent.salary()
                        );

                        return employeeService.createEmployee(entity)
                                .map(saved -> {
                                    log.info("DB: Salvo com sucesso! ID: {}", saved.getId());
                                    return (EmployeeEvent) new EmployeeApproved(
                                            validatedEvent.eventId(),
                                            saved.getId()
                                    );
                                })
                                .onErrorResume(e -> {
                                    log.error("DB: Erro ao salvar", e);
                                    return Mono.empty();
                                });
                    }
                    return Mono.empty();
                });
    }
}