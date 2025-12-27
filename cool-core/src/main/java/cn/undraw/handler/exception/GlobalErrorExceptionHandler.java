package cn.undraw.handler.exception;

import cn.undraw.util.log.annotation.ErrorLog;
import cn.undraw.util.result.R;
import cn.undraw.util.result.ResultEnum;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.ConnectException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @ExceptionHandler(DuplicateKeyException.class)
    public R<?> duplicateKeyException(HttpServletRequest req, DuplicateKeyException e) {
        Pattern pattern = Pattern.compile("Duplicate entry '(.+?)' for key");
        Matcher matcher = pattern.matcher(e.getMessage());
        String res = "添加数据重复";

        if (matcher.find()) {
            String content = matcher.group(1);
            res += ": " + content;
        }
        return R.error(res, e);
    }

    /**
     * 完整型违规异常
     * @param req
     * @param e
     * @return cn.undraw.util.result.R<?>
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public R<?> dataIntegrityViolationException(HttpServletRequest req, DataIntegrityViolationException e) {
        return R.error("数据完整性违规异常", e);
    }

    /**
     * 其他异常
     * @param req
     * @param e
     * @return cn.undraw.util.result.R<?>
     */
    @ExceptionHandler(Exception.class)
    public R<?> otherException(HttpServletRequest req, Exception e) {
        if (e.getCause() instanceof ConnectException) {
            return ResultEnum.TIMEOUT.getR(e);
        }
        return ResultEnum.ERROR.getR(e);
    }

}
