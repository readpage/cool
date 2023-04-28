package com.undraw.service.impl;

import cn.undraw.util.ConvertUtils;
import cn.undraw.util.log.service.OperateLogService;
import cn.undraw.util.log.vo.OperationLog;
import com.fasterxml.jackson.core.type.TypeReference;
import com.undraw.entity.SystemLog;
import com.undraw.service.SystemLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author readpage
 * @date 2023-03-14 23:50
 */
@Service
public class OperateLogServiceImpl implements OperateLogService {
    @Resource
    private SystemLogService systemLogService;
    @Override
    public void createLog(OperationLog operationLog) {
        systemLogService.save(ConvertUtils.cloneDeep(operationLog, new TypeReference<SystemLog>() {}));
    }
}
