package com.imd.db_service.config;

import com.imd.common.events.CompleteCreationCommand;
import com.imd.common.events.DbCompletionResult;
import com.imd.common.events.EmployeeApproved;
import com.imd.common.events.SalaryValidated;
import com.imd.db_service.model.Employee;
import com.imd.db_service.service.EmployeeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Configuration
public class StreamConfig {
    private final EmployeeService employeeService;
    public StreamConfig(EmployeeService s) { this.employeeService = s; }

    @Bean
    @Profile("choreography")
    public Function<SalaryValidated, Mono<EmployeeApproved>> persistEmployee() {
        return event -> {
            // Converte Evento -> Entidade
            Employee emp = new Employee(event.name(), event.position(), event.salary());
            return employeeService.createEmployee(emp)
                    .map(saved -> new EmployeeApproved(event.eventId(), saved.getId()));
        };
    }

    @Bean @Profile("orchestration")
    public Function<CompleteCreationCommand, Mono<DbCompletionResult>> handleSaveCommand() {
        return cmd -> {
            Employee emp = new Employee(cmd.name(), cmd.position(), cmd.salary());
            return employeeService.createEmployee(emp)
                    .map(saved -> new DbCompletionResult(cmd.sagaId(), saved.getId(), true));
        };
    }
}
