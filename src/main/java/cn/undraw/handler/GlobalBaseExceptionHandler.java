package cn.undraw.handler;

import cn.undraw.util.log.annotation.ErrorLog;
import cn.undraw.util.result.Result;
import cn.undraw.util.result.ResultUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;

/**
 * @author readpage
 * @create 2022-09-02 14:38
 *  基础异常抛出
 *  包含 类型转换异常  数组下表越界 文件没有找到  输入输出  空指针  除数为零
 */
@ErrorLog
@RestControllerAdvice
public class GlobalBaseExceptionHandler {

    /**
     * 类转换异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(ClassCastException.class)
    public Result<?> classCastException(HttpServletRequest req, ClassCastException e) {
        return ResultUtils.error("类转换异常", e);
    }

    /**
     * 数组下表越界异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    public Result<?> arrayIndexOutOfBoundsException(HttpServletRequest req, ArrayIndexOutOfBoundsException e) {
        return ResultUtils.error("数组下表越界异常", e);
    }

    /**
     * 文件没有找到
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(FileNotFoundException.class)
    public Result<?> fileNotFoundException(HttpServletRequest req, FileNotFoundException e) {
        return ResultUtils.error("文件没有找到异常", e);
    }

//    /**
//     * 输入输出流异常
//     * @param e
//     * @return
//     */
//    @ExceptionHandler(IOException.class)
//    public Result<?> iOException(IOException e) {
//        return ResultUtils.error("输入输出流异常", e);
//    }

    /**
     * 空指针异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<?> nullPointerException(HttpServletRequest req, NullPointerException e) {
        return ResultUtils.error("空指针异常", e);
    }

    /**
     * 除数为零
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(ArithmeticException.class)
    public Result<?> arithmeticException(HttpServletRequest req, ArithmeticException e) {
        return ResultUtils.error("除数为零", e);
    }

}
