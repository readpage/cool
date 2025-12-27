package com.undraw.service.impl;

import com.undraw.domain.model.Employee;
import com.undraw.service.EmployeeService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author readpage
 * @date 2025-04-14 11:41
 */

@Service
public class EmployeeServiceImpl implements EmployeeService {
    @Override
    public Flux<Employee> getAllEmployees() {
        List<Employee> list = Employee.employeeList;
        Flux<Employee> flux = Flux.create(sink -> {
            new Thread(() -> {
                try {
                    sink.next(list.get(0));
                    Thread.sleep(5000);
                    for (int i = 1; i < list.size(); i++) {
                        Thread.sleep((long) (Math.random() * 2000));
                        sink.next(list.get(i));
                    }
                    sink.complete();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        });

        return flux;
    }

}
