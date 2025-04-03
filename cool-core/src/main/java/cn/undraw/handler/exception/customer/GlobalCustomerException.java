package cn.undraw.handler.exception.customer;

import cn.undraw.util.log.annotation.ErrorLog;
import cn.undraw.util.log.enums.OperateTypeEnum;
import cn.undraw.util.result.R;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * @author readpage
 * @date 2023-03-29 23:56
 */
@ErrorLog
@RestControllerAdvice
@Order(value = 0)
public class GlobalCustomerException {

    @ErrorLog(type = OperateTypeEnum.WARN)
    @ExceptionHandler(CustomerException.class)
    public R<?> CustomerException(HttpServletRequest req, CustomerException e) {
        return e.getR();
    }
}
