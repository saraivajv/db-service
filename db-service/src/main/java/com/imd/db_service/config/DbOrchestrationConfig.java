package com.imd.db_service.config;

import com.imd.common.events.CompleteCreationCommand;
import com.imd.common.events.DbCompletionResult;
import com.imd.db_service.model.Employee;
import com.imd.db_service.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Configuration
@Profile("orchestration")
public class DbOrchestrationConfig {

    private static final Logger log = LoggerFactory.getLogger(DbOrchestrationConfig.class);
    private final EmployeeService employeeService;

    public DbOrchestrationConfig(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Bean
    public Function<CompleteCreationCommand, Mono<DbCompletionResult>> handleSaveCommand() {
        return cmd -> {
            log.info("ORQUESTRAÇÃO: Recebido comando para salvar: {}", cmd.name());

            // 1. Usar o construtor completo com UUID e Status
            Employee entity = new Employee(
                    cmd.sagaId(),
                    cmd.name(),
                    cmd.position(),
                    cmd.salary(),
                    "APPROVED",
                    null
            );

            // 2. Chama o Service
            return employeeService.createEmployee(entity)
                    .map(saved -> {
                        log.info("ORQUESTRAÇÃO: Comando executado. ID: {}", saved.getId());
                        // 3. Responde ao Maestro (Agora saved.getId() é UUID e o Record aceita UUID)
                        return new DbCompletionResult(cmd.sagaId(), saved.getId(), true);
                    })
                    .onErrorResume(e -> {
                        log.error("ORQUESTRAÇÃO: Erro ao salvar", e);
                        return Mono.just(new DbCompletionResult(cmd.sagaId(), null, false));
                    });
        };
    }
}