package cn.undraw.handler.exception;

import cn.undraw.util.log.annotation.ErrorLog;
import cn.undraw.util.result.R;
import cn.undraw.util.result.ResultEnum;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.FileNotFoundException;

/**
 *  基础异常抛出
 *  包含 类型转换异常  数组下表越界 文件没有找到  输入输出  空指针  除数为零
 * @author readpage
 * @date 2022-09-02 14:38
 */
@ErrorLog
@Order(value = 1)
@RestControllerAdvice
public class GlobalBaseExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public R<?> noResourceFoundException(HttpServletRequest req, NoResourceFoundException ex) {
        // 返回404状态码和自定义的错误信息
        return R.fail(ResultEnum.NOT_FOUND);
    }

    /**
     * 类转换异常
     * @param req
     * @param e
     * @return cn.undraw.util.result.R<?>
     */
    @ExceptionHandler(ClassCastException.class)
    public R<?> classCastException(HttpServletRequest req, ClassCastException e) {
        return R.error("类转换异常", e);
    }

    /**
     * 数组下表越界异常
     * @param req
     * @param e
     * @return cn.undraw.util.result.R<?>
     */
    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    public R<?> arrayIndexOutOfBoundsException(HttpServletRequest req, ArrayIndexOutOfBoundsException e) {
        return R.error("数组下表越界异常", e);
    }

    /**
     * 文件没有找到
     * @param req
     * @param e
     * @return cn.undraw.util.result.R<?>
     */
    @ExceptionHandler(FileNotFoundException.class)
    public R<?> fileNotFoundException(HttpServletRequest req, FileNotFoundException e) {
        return R.error("文件没有找到异常", e);
    }

    /**
     * 空指针异常
     * @param req
     * @param e
     * @return cn.undraw.util.result.R<?>
     */
    @ExceptionHandler(NullPointerException.class)
    public R<?> nullPointerException(HttpServletRequest req, NullPointerException e) {
        return R.error("空指针异常", e);
    }

   /**
    * 除数为零
    * @param req
    * @param e
    * @return cn.undraw.util.result.R<?>
    */
    @ExceptionHandler(ArithmeticException.class)
    public R<?> arithmeticException(HttpServletRequest req, ArithmeticException e) {
        return R.error("除数为零", e);
    }

}
