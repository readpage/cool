package com.undraw.service;

import com.undraw.domain.model.Employee;
import reactor.core.publisher.Flux;

/**
 * @author readpage
 * @date 2025-04-14 11:40
 */
public interface EmployeeService {

    /**
     * 返回用户列表的 Flux 流
     * @return
     */
    Flux<Employee> getAllEmployees();

}
