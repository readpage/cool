package cn.undraw.util.log.service;

import cn.undraw.util.log.vo.OperationLog;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author readpage
 * @date 2022-12-01 22:02
 */
@Service
public class OperateLogWorkServiceImpl implements OperateLogWorkService {
    @Autowired(required = false)
    OperateLogService operateLogService;
    @Override
    public void createLog(OperationLog operationLog, HttpServletRequest request) {
        if (operateLogService != null) {
            operateLogService.createLog(operationLog, request);
        }
    }
}
