package com.undraw.service.impl;

import cn.undraw.util.ConvertUtils;
import cn.undraw.util.log.service.OperateLogService;
import cn.undraw.util.log.vo.OperationLog;
import com.undraw.domain.entity.SystemLog;
import com.undraw.mapper.SystemLogMapper;
import com.undraw.service.SystemLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author readpage
 * @since 2023-03-15 18:08
 */
@Service
public class SystemLogServiceImpl extends ServiceImpl<SystemLogMapper, SystemLog> implements SystemLogService, OperateLogService {

    @Async("async")
    @Override
    public void createLog(OperationLog operationLog, HttpServletRequest request) {
        SystemLog systemLog = ConvertUtils.cloneDeep(operationLog, SystemLog.class);
        this.save(systemLog);
    }
}