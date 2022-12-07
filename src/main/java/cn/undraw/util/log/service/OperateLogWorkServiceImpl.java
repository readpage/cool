package cn.undraw.util.log.service;

import cn.undraw.util.ConvertUtils;
import cn.undraw.util.log.vo.OperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author readpage
 * @create 2022-12-01 22:02
 */
@Service
public class OperateLogWorkServiceImpl implements OperateLogWorkService {
    @Autowired(required = false)
    OperateLogService operateLogService;
    @Override
    public void createLog(OperationLog operationLog) {
        if (operateLogService != null) {
            operateLogService.createLog(ConvertUtils.cloneDeep(operationLog, OperationLog.class));
        }
    }
}
