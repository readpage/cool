package cn.undraw.util.log.service;

import cn.undraw.util.log.vo.OperationLog;

/**
 * @author readpage
 * @date 2022-12-01 20:04
 */
public interface OperateLogService {

    /**
     * 创建操作日志
     * @param operationLog
     */
    void createLog(OperationLog operationLog);
}
