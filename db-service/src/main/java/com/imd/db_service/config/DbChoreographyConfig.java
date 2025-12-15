package com.imd.db_service.config;

import com.imd.common.events.*;
import com.imd.db_service.model.Employee;
import com.imd.db_service.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
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
    public Function<Flux<EmployeeEvent>, Flux<Message<EmployeeEvent>>> persistEmployee() {
        return flux -> flux.flatMap(event -> {
            if (event instanceof EquipmentAssigned req) {
                log.info("DB: Recebido pedido para persistir: {}", req.name());

                Mono<Employee> databaseAction;

                // --- POISON PILL (Simulação de Erro) ---
                // Se o nome tiver "Erro", forçamos uma falha antes de chamar o service
                if (req.name().contains("Erro")) {
                    log.error("DB: ⚠️ ATIVANDO POISON PILL!");
                    databaseAction = Mono.error(new RuntimeException("Simulacao de Queda do Banco"));
                } else {
                    // Fluxo normal: Prepara o objeto para UPDATE (pois o ID já existe no CRUD)
                    Employee entity = new Employee(
                            req.eventId(), req.name(), "Dev", req.salary(), "APPROVED", null
                    );
                    databaseAction = employeeService.createEmployee(entity);
                }

                return databaseAction
                        // --- CENÁRIO DE SUCESSO ---
                        .map(saved -> {
                            log.info("✅ DB: Salvo com sucesso! ID: {}", saved.getId());

                            return MessageBuilder.withPayload((EmployeeEvent) new EmployeeApproved(req.eventId(), saved.getId()))
                                    // AQUI ESTÁ A CORREÇÃO: Usamos a string direta
                                    .setHeader("my-routing-key", "employee.approved")
                                    .build();
                        })
                        // --- CENÁRIO DE ERRO (Compensação) ---
                        .onErrorResume(e -> {
                            log.error("❌ DB: Falha! Iniciando Compensação.", e);

                            return Mono.just(
                                    MessageBuilder.withPayload((EmployeeEvent) new EmployeePersistenceFailed(req.eventId(), "Falha DB: " + e.getMessage()))
                                            .setHeader("my-routing-key", "db.failed")
                                            .build()
                            );
                        });
            }
            return Mono.empty();
        });
    }
}