package com.imd.db_service.service;

import com.imd.db_service.model.Employee;
import com.imd.db_service.repository.EmployeeRepository;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

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

    public Flux<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    private Mono<Void> evictCache(UUID id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        RBucketReactive<Employee> bucket = redissonClient.getBucket(cacheKey);
        return bucket.delete().then();
    }
}