package cn.undraw.handler.exception;

import cn.undraw.util.log.annotation.ErrorLog;
import cn.undraw.util.log.enums.OperateTypeEnum;
import cn.undraw.util.result.R;
import cn.undraw.util.result.ResultEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author readpage
 * @date 2022-10-27 19:02
 */
@ErrorLog(type = OperateTypeEnum.WARN)
@Order(value = 2)
@RestControllerAdvice
@Slf4j
public class GlobalWarnExceptionHandler {


    /**
     * 类型转换错误
     * @param e
     * @return cn.undraw.util.result.R
     */
    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    @ResponseBody
    public R requestParamFormatException(HttpServletRequest req, Exception e) {
        Pattern pattern = Pattern.compile("CustomerException: (.+?)$");
        Matcher matcher = pattern.matcher(e.getMessage());
        String msg = "类型转换错误";

        if (matcher.find()) {
            msg = matcher.group(1);
        }
        return R.bad(msg);
    }

    /**
     * 请求方式不支持异常信息
     * @param request
     * @param exception
     * @return cn.undraw.util.result.R<?>
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<?> errorHandler(HttpServletRequest request, HttpRequestMethodNotSupportedException exception) {
        return R.fail(ResultEnum.METHOD_NOT_ALLOWED);
    }


    /**
     * 参数效验异常处理器
     * @param req
     * @param e
     * @return cn.undraw.util.result.R<?>
     */
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public R<?> argumentValidationException(HttpServletRequest req, Exception e) {
        BindingResult bindingResult = null;
        if (e instanceof BindException) {
            bindingResult = ((BindException) e).getBindingResult();
        } else if (e instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException)e).getBindingResult();
        } else {
            return R.fail(e.getMessage());
        }
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            if (!errors.isEmpty()) {
                String[] msg = {"提交的数据不合规范"};
                Map<String, String> map = errors.stream().map(o -> {
                    FieldError f = (FieldError) o;
                    String message = f.getDefaultMessage();
                    String tar = "Exception: ";
                    if (message.contains(tar)) {
                        message = message.substring(message.lastIndexOf(tar)+tar.length());
                    }
                    msg[0] = message;
                    return f;
                }).collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (oldValue, newValue) -> (oldValue + "; " + newValue)));
                return R.fail(msg[0], map);
            }
        }
        return R.fail(ResultEnum.BAD_REQUEST);
    }

}
