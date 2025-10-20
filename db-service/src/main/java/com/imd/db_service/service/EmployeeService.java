package com.imd.db_service.service;

import com.imd.db_service.model.Employee;
import com.imd.db_service.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Mono<Employee> createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Flux<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Mono<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public Mono<Employee> updateEmployee(Long id, Employee employeeDetails) {
        return employeeRepository.findById(id)
                .flatMap(employee -> {
                    employee.setName(employeeDetails.getName());
                    employee.setPosition(employeeDetails.getPosition());
                    employee.setSalary(employeeDetails.getSalary());
                    return employeeRepository.save(employee);
                });
    }

    public Mono<Void> deleteEmployee(Long id) {
        return employeeRepository.deleteById(id);
    }

    public Mono<Employee> saveReview(Long employeeId, String reviewText) {
        return employeeRepository.findById(employeeId)
                .flatMap(employee -> {
                    employee.setAiReview(reviewText);
                    return employeeRepository.save(employee);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Employee not found with id: " + employeeId)));
    }
}
