package com.undraw.service;

import com.undraw.domain.entity.SystemLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author readpage
 * @since 2023-03-15 18:08
 */
public interface SystemLogService extends IService<SystemLog> {
    void _save(SystemLog systemLog);
}
