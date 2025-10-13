package com.imd.db_service.service;

import com.imd.db_service.model.Employee;
import com.imd.db_service.repository.EmployeeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    
	@Autowired
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @CachePut
    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    // Obter todos os Employees
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // Obter Employee por ID
    @Cacheable(value = "employee", key = "#id")
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    // Atualizar Employee
    @CacheEvict(value = "employee", key = "#id")
    public Optional<Employee> updateEmployee(Long id, Employee employeeDetails) {
        return employeeRepository.findById(id).map(employee -> {
            employee.setName(employeeDetails.getName());
            employee.setPosition(employeeDetails.getPosition());
            employee.setSalary(employeeDetails.getSalary());
            return employeeRepository.save(employee);
        });
    }

    // Deletar Employee
    @CacheEvict(value = "employee", key = "#id")
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

    @CacheEvict(value = "employee", key = "#employeeId")
    public Employee saveReview(Long employeeId, String reviewText) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        employee.setAiReview(reviewText);
        return employeeRepository.save(employee);
    }
}
