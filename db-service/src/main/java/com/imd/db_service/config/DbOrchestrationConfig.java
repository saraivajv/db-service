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
@Profile("orchestration")
public class DbOrchestrationConfig {

    private static final Logger log = LoggerFactory.getLogger(DbOrchestrationConfig.class);
    private final EmployeeService employeeService;

    public DbOrchestrationConfig(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Bean
    public Function<Flux<Message<SagaCommand>>, Flux<Message<SagaReply>>> handleSaveCommand() {
        return flux -> flux.flatMap(msg -> {
            SagaCommand command = msg.getPayload();

            if (command instanceof CompleteCreationCommand cmd) {
                log.info("DB: Recebido comando final para salvar: {}", cmd.name());
                if (cmd.name().contains("Erro")) {
                    log.error("DB: ⚠️ ERRO NO BANCO DETECTADO! Simulando queda.");
                    return Mono.just(createReply(new DbCompletionResult(
                            cmd.sagaId(), false, "Simulacao de Queda do Banco", null
                    )));
                }

                Employee entity = new Employee(
                        cmd.sagaId(), cmd.name(), cmd.position(), cmd.salary(),
                        "APPROVED", null
                );

                return employeeService.createEmployee(entity)
                        .map(saved -> {
                            log.info("✅ DB: Salvo! ID: {}", saved.getId());
                            // SUCESSO: Passa ID do banco e mensagem
                            return createReply(new DbCompletionResult(
                                    cmd.sagaId(),
                                    true,
                                    "Salvo com sucesso",
                                    saved.getId()
                            ));
                        })
                        .onErrorResume(e -> {
                            log.error("❌ DB: Falha ao salvar!", e);
                            // ERRO: Passa false, mensagem de erro e null no ID
                            return Mono.just(createReply(new DbCompletionResult(
                                    cmd.sagaId(),
                                    false,
                                    e.getMessage(),
                                    null
                            )));
                        });
            }
            return Mono.empty();
        });
    }

    private Message<SagaReply> createReply(SagaReply reply) {
        return MessageBuilder.withPayload(reply).build();
    }
}