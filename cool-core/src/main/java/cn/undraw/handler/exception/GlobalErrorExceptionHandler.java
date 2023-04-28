package cn.undraw.handler.exception;

import cn.undraw.util.log.annotation.ErrorLog;
import cn.undraw.util.result.R;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * @author readpage
 * @date 2022-08-24 17:06
 */
@ErrorLog
@Order(value = 5)
@RestControllerAdvice
public class GlobalErrorExceptionHandler {

    /**
     * SQL 语法异常
     * @param req
     * @param e
     * @return cn.undraw.util.result.R<?>
     */
    @ExceptionHandler(value = BadSqlGrammarException.class)
    public R<?> badSqlGrammarException(HttpServletRequest req, BadSqlGrammarException e) {
        return R.error("SQL语法异常", e);
    }

    /**
     * SQL重复键异常
     * @param req
     * @param e
     * @return cn.undraw.util.result.R<?>
     */
    @ExceptionHandler({ DuplicateKeyException.class})
    public R<?> duplicateKeyException(HttpServletRequest req, DuplicateKeyException e) {
        return R.error("SQL重复键异常", e);
    }

    /**
     * 其他异常
     * @param req
     * @param e
     * @return cn.undraw.util.result.R<?>
     */
    @ExceptionHandler(Exception.class)
    public R<?> otherException(HttpServletRequest req, Exception e) {
       return R.error(e.getMessage(), e);
    }

}
