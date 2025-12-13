package com.imd.db_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("employee")
public class Employee implements Persistable<UUID> {

    @Id
    private UUID id;

    private String name;
    private String position;
    private Double salary;
    private String status;

    @Column("reason_msg")
    private String reasonMsg;

    // Campo auxiliar para o Spring Data R2DBC saber se é INSERT ou UPDATE
    @Transient // Importante: Avisa que isso NÃO é uma coluna no banco
    private boolean newEmployee = false;

    public Employee() {}

    public Employee(UUID id, String name, String position, Double salary, String status, String reasonMsg) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.salary = salary;
        this.status = status;
        this.reasonMsg = reasonMsg;
        this.newEmployee = false;
    }

    // --- Métodos da Interface Persistable ---

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return newEmployee;
    }

    // --- Getters e Setters Padrão ---

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReasonMsg() {
        return reasonMsg;
    }

    public void setReasonMsg(String reasonMsg) {
        this.reasonMsg = reasonMsg;
    }
}