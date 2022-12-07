package cn.undraw.handler;

import cn.undraw.util.log.annotation.ErrorLog;
import cn.undraw.util.result.Result;
import cn.undraw.util.result.ResultUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

import static cn.undraw.util.log.enums.OperateTypeEnum.WARN;


/**
 * @author readpage
 * @create 2022-08-24 17:06
 */
@ErrorLog
@RestControllerAdvice
public class GlobalErrorExceptionHandler {

    /**
     * 自定义异常
     * @param req
     * @param e
     * @return
     */
    @ErrorLog(type = WARN)
    @ExceptionHandler(CustomerException.class)
    public Result<?> CustomerException(HttpServletRequest req, CustomerException e) {
        return e.getResult();
    }


    /**
     * SQL 语法异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = BadSqlGrammarException.class)
    public Result<?> badSqlGrammarException(HttpServletRequest req, BadSqlGrammarException e) {
        return ResultUtils.error("SQL语法异常", e);
    }

    /**
     * SQL重复键异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler({ DuplicateKeyException.class})
    public Result<?> duplicateKeyException(HttpServletRequest req, DuplicateKeyException e) {
        return ResultUtils.error("SQL重复键异常", e);
    }

    /**
     * 其他异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public Result<?> otherException(HttpServletRequest req, Exception e) {
        return ResultUtils.error(e);
    }

}
