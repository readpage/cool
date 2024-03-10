package com.undraw.service.impl;

import cn.undraw.util.StrUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.undraw.domain.entity.SystemLog;
import com.undraw.mapper.SystemLogMapper;
import com.undraw.service.SystemLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author readpage
 * @since 2023-05-24 10:37
 */
@Service
public class SystemLogServiceImpl extends ServiceImpl<SystemLogMapper, SystemLog> implements SystemLogService {


    @Async("async")
    public void _save(SystemLog systemLog) {
        systemLog.setResultMsg(StrUtils.substringByChar(systemLog.getResultMsg(), 65535));
        systemLog.setResultData(StrUtils.substringByChar(systemLog.getResultData(), 65535));
        this.save(systemLog);
    }


}
