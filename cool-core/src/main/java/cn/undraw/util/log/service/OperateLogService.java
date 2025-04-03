package cn.undraw.util.log.service;

import cn.undraw.util.log.vo.OperationLog;
import jakarta.servlet.http.HttpServletRequest;


/**
 * @author readpage
 * @date 2022-12-01 20:04
 */
public interface OperateLogService {

    /**
     * 创建操作日志
     * @param operationLog
     * @return void
     */
    void createLog(OperationLog operationLog, HttpServletRequest request);
}
