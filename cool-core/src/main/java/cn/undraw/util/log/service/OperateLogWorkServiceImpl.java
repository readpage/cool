package cn.undraw.util.log.service;

import cn.undraw.util.log.vo.OperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

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
