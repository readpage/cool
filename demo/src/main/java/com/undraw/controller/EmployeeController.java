package com.undraw.controller;

import com.undraw.domain.model.Employee;
import com.undraw.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author readpage
 * @date 2025-04-14 13:30
 */
@RestController
@RequestMapping("/employee")
@Tag(name = "人员管理")
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;

    // produces：指定控制器支持的响应媒体类型。
    @Operation(summary = "返回用户列表的Flux流")
    @GetMapping(value = "/getAllUsers", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

}
