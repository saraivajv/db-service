package com.imd.db_service.config;

import com.imd.common.events.*;
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
        return flux -> flux.flatMap(event -> {
            if (event instanceof EquipmentAssigned req) {

                // Cria usando o ID que veio do evento (req.eventId())
                Employee entity = new Employee(
                        req.eventId(),
                        req.name(),
                        "Dev",
                        req.salary(),
                        "APPROVED",
                        null
                );

                return employeeService.createEmployee(entity)
                        .map(saved -> (EmployeeEvent) new EmployeeApproved(
                                req.eventId(),
                                saved.getId()
                        ))
                        .onErrorResume(e -> {
                            log.error("DB: Erro fatal. Solicitando compensação ao Inventory.", e);
                            // Emite evento de falha para o Inventory devolver o item
                            return Mono.just(new EmployeePersistenceFailed(req.eventId(), e.getMessage()));
                        });
            }
            return Mono.empty();
        });
    }
}