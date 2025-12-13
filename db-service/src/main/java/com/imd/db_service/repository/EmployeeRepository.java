package com.imd.db_service.repository;

import com.imd.db_service.model.Employee;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmployeeRepository extends R2dbcRepository<Employee, UUID> {
}
