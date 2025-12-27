package com.undraw.service.impl;

import cn.undraw.util.ConvertUtils;
import cn.undraw.util.log.service.OperateLogService;
import cn.undraw.util.log.vo.OperationLog;
import com.undraw.domain.entity.SystemLog;
import com.undraw.service.SystemLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * @author readpage
 * @date 2023-03-15 8:32
 */
@Service
@Slf4j
public class OperateLogServiceImpl implements OperateLogService {
    @Resource
    private SystemLogService systemLogService;


    @Override
    public void createLog(OperationLog operationLog, HttpServletRequest request) {
        SystemLog systemLog = ConvertUtils.cloneDeep(operationLog, SystemLog.class);
        systemLogService._save(systemLog);
    }
}
