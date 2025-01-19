package com.imd.db_service.service;

import com.imd.db_service.model.Employee;
import com.imd.db_service.repository.EmployeeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    
	@Autowired
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // Criar um novo Employee
    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    // Obter todos os Employees
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // Obter Employee por ID
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    // Atualizar Employee
    public Optional<Employee> updateEmployee(Long id, Employee employeeDetails) {
        return employeeRepository.findById(id).map(employee -> {
            employee.setName(employeeDetails.getName());
            employee.setPosition(employeeDetails.getPosition());
            employee.setSalary(employeeDetails.getSalary());
            return employeeRepository.save(employee);
        });
    }

    // Deletar Employee
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}
