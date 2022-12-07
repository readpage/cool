package cn.undraw.handler;

import cn.undraw.util.log.annotation.ErrorLog;
import cn.undraw.util.result.Result;
import cn.undraw.util.result.ResultEnum;
import cn.undraw.util.result.ResultUtils;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.ConnectException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.undraw.util.log.enums.OperateTypeEnum.WARN;

/**
 * @author readpage
 * @date 2022-10-27 19:02
 */
@ErrorLog
@RestControllerAdvice
public class GlobalDefaultExceptionHandler {
    @Value("${spring.servlet.multipart.max-file-size:1MB}")
    private String MaxFileSize;

    /**
     * 文件大小超出异常处理
     * @param req
     * @param e
     * @return
     */
    @ErrorLog(type = WARN)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> MaxUploadSizeExceededException(HttpServletRequest req, MaxUploadSizeExceededException e) {
        return ResultUtils.fail("文件大小超出" + MaxFileSize +  "限制, 请压缩或降低文件质量! ");
    }

    /**
     * 请求方式不支持异常信息
     * @param request
     * @param exception
     * @param response
     * @param e
     * @return
     */
    @ErrorLog(type = WARN)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> errorHandler(HttpServletRequest request, HttpRequestMethodNotSupportedException exception, HttpServletResponse response, Exception e) {
        return ResultUtils.fail("请求方式不支持异常");
    }

    /**
     * 忽略参数异常处理器
     * @param req
     * @param e
     * @return
     */
    @ErrorLog(type = WARN)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> parameterMissingException(HttpServletRequest req, MissingServletRequestParameterException e) {
        return ResultUtils.fail("请求参数 " + e.getParameterName() + " 不能为空");
    }

    /**
     * 参数效验异常处理器
     * @param req
     * @param e
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @ErrorLog(type = WARN)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> parameterException(HttpServletRequest req, MethodArgumentNotValidException e) throws InstantiationException, IllegalAccessException {
        // 获取异常信息
        BindingResult exceptions = e.getBindingResult();
        if (exceptions.hasErrors()) {
            List<ObjectError> errors = exceptions.getAllErrors();
            if (!errors.isEmpty()) {
                String[] msg = {"提交的数据不合规范"};
                Map<String, String> map = errors.stream().map(o -> {
                    FieldError f = (FieldError) o;
                    msg[0] = f.getDefaultMessage();
                    return f;
                }).collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
                ResultUtils.fail(e);
                return ResultUtils.fail(msg[0], map);
            }
        }
        return ResultUtils.fail(ResultEnum.BAD_REQUEST);
    }

    /**
     * Jackson参数绑定异常
     * @param req
     * @param e
     * @return
     */
    @ErrorLog(type = WARN)
    @ExceptionHandler(InvalidFormatException.class)
    public Result<?> handleInvalidFormatException(HttpServletRequest req, InvalidFormatException e) {
        return ResultUtils.fail("参数" + e.getValue() + "格式错误", e);
    }

    @ExceptionHandler(value = NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ErrorLog(type = WARN)
    public Result<?> errorHandler(HttpServletRequest request, NoHandlerFoundException exception, HttpServletResponse response,Exception e) {
        return ResultUtils.fail("服务不存在异常", e);
    }

    /**
     * 请求资源超时异常信息
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler({ResourceAccessException.class, ConnectException.class})
    public Result<?> resourceAccessExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) {
        return ResultUtils.fail("请求资源超时异常信息", e);
    }

}
