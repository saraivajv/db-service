package com.imd.db_service.model;

// Importações corretas para Spring Data R2DBC
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

// A anotação @Entity do JPA é removida. @Table é opcional se o nome da tabela for igual ao da classe.
@Table("employee")
public class Employee implements Serializable {

    @Id // Anotação do Spring Data, não do JPA
    // A anotação @GeneratedValue é removida; o banco de dados gerencia o auto-incremento.
    private Long id;

    private String name;
    private String position;
    private Double salary;

    @Column("aireview") // Anotação do Spring Data R2DBC
    private String aiReview;

    public Employee() {}

    public Employee(String name, String position, Double salary) {
        this.name = name;
        this.position = position;
        this.salary = salary;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getAiReview() {
        return aiReview;
    }

    public void setAiReview(String aiReview) {
        this.aiReview = aiReview;
    }
}
