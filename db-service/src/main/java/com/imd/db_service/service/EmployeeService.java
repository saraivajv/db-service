package com.imd.db_service.service;

import com.imd.db_service.model.Employee;
import com.imd.db_service.repository.EmployeeRepository;
import org.redisson.api.RBucketReactive; // Importação importante
import org.redisson.api.RedissonReactiveClient; // Importação importante
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RedissonReactiveClient redissonClient;
    private final String CACHE_KEY_PREFIX = "employee:";

    public EmployeeService(EmployeeRepository employeeRepository, RedissonReactiveClient redissonClient) {
        this.employeeRepository = employeeRepository;
        this.redissonClient = redissonClient;
    }

    public Mono<Employee> createEmployee(Employee employee) {
        return employeeRepository.save(employee)
                .flatMap(savedEmployee ->
                        evictCache(savedEmployee.getId()).thenReturn(savedEmployee)
                );
    }

    public Mono<Employee> getEmployeeById(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        RBucketReactive<Employee> bucket = redissonClient.getBucket(cacheKey);

        return bucket.get()
                .switchIfEmpty(
                        employeeRepository.findById(id)
                                .flatMap(employeeFromDb ->
                                        bucket.set(employeeFromDb).thenReturn(employeeFromDb)
                                )
                );
    }

    public Mono<Employee> updateEmployee(Long id, Employee employeeDetails) {
        return employeeRepository.findById(id)
                .flatMap(employee -> {
                    employee.setName(employeeDetails.getName());
                    employee.setPosition(employeeDetails.getPosition());
                    employee.setSalary(employeeDetails.getSalary());
                    return employeeRepository.save(employee);
                })
                .flatMap(updatedEmployee ->
                        evictCache(updatedEmployee.getId()).thenReturn(updatedEmployee)
                );
    }

    public Mono<Void> deleteEmployee(Long id) {
        return employeeRepository.deleteById(id)
                .then(evictCache(id));
    }

    public Mono<Employee> saveReview(Long employeeId, String reviewText) {
        return employeeRepository.findById(employeeId)
                .flatMap(employee -> {
                    employee.setAiReview(reviewText);
                    return employeeRepository.save(employee);
                })
                .flatMap(savedEmployee ->
                        evictCache(savedEmployee.getId()).thenReturn(savedEmployee)
                )
                .switchIfEmpty(Mono.error(new RuntimeException("Employee not found with id: " + employeeId)));
    }

    public Flux<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    private Mono<Void> evictCache(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        RBucketReactive<Employee> bucket = redissonClient.getBucket(cacheKey);
        return bucket.delete().then();
    }
}

