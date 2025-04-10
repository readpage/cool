package com.undraw.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.undraw.domain.entity.SystemLog;

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
