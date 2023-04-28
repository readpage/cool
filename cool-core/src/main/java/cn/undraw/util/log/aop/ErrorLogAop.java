package cn.undraw.util.log.aop;

import cn.undraw.util.ConvertUtils;
import cn.undraw.util.DateUtils;
import cn.undraw.util.ErrorUtils;
import cn.undraw.util.log.annotation.ErrorLog;
import cn.undraw.util.log.service.OperateLogWorkService;
import cn.undraw.util.log.vo.OperationLog;
import cn.undraw.util.result.R;
import cn.undraw.util.servlet.IpUtils;
import cn.undraw.util.servlet.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author readpage
 * @date 2022-12-02 15:31
 */

@Component
@Aspect
@Slf4j
public class ErrorLogAop {

    @Resource
    private OperateLogWorkService operateLogWorkService;

    @Around("@within(errorLog) || @annotation(errorLog)")
    public Object around(ProceedingJoinPoint pjp, ErrorLog errorLog) throws Throwable {
        OperationLog operationLog = new OperationLog();
        HttpServletRequest req = null;
        Exception e = null;

        Object result = pjp.proceed();

        if (errorLog == null) {
            errorLog = OperateLogAop.getClassAnnotation(pjp, ErrorLog.class);
        }
        operationLog.setType(errorLog.type().getType());

        Object[] args = pjp.getArgs();
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest) {
                req = (HttpServletRequest) arg;
            } else if (arg instanceof Exception) {
                e = (Exception) arg;
            }
        }
        String method = ((MethodSignature) pjp.getSignature()).toString();
        if (req == null) {
            log.error("OperateLog注解: " + (MethodSignature)pjp.getSignature() + "方法的参数HttpServletRequest为null");
        } else if (e == null) {
            log.error("OperateLog注解: " + (MethodSignature)pjp.getSignature() + "方法的参数Exception为null");
        } else {
            initExceptionLog(operationLog, req, result, e);
        }
        R res = ConvertUtils.copy(result, R.class);
        res.setData(null);
        return res;
    }


    public void initExceptionLog(OperationLog operationLog, HttpServletRequest request, Object result, Throwable e) {
        //设置请求信息
        setRequestFields(operationLog, request);

        setRFields(operationLog, result, e);

        // 打印日志
        print(operationLog);
        operateLogWorkService.createLog(operationLog);
    }

    /**
     * 设置结果信息
     * @param operationLog
     * @param e
     * @return void
     */
    private void setRFields(OperationLog operationLog, Object result, Throwable e) {
        R r = null;
        if (result instanceof R) {
            r = ConvertUtils.copy(result, R.class);
        } else {
            r = R.error(e);
        }

        operationLog.setResultCode(r.getCode());
        operationLog.setResultMsg(r.getMsg());
        operationLog.setResultData(ConvertUtils.toJson(r.getData()));
        // 设置异常方法名称
        operationLog.setOptMethod(ErrorUtils.getErrorMethod(e));
    }

    /**
     * 设置请求信息
     * @param operationLog
     * @param request
     * @return void
     */
    private void setRequestFields(OperationLog operationLog, HttpServletRequest request) {
        if (ServletUtils.isNotMultipart(request)) {
            Map<String, Object> params = new HashMap<>();
            params.put("query", ServletUtils.getParams(request));
            params.put("body", ServletUtils.getBody(request));
            operationLog.setRequestParam(params.toString());
        }
        operationLog.setRequestUrl(request.getRequestURI());
        operationLog.setRequestMethod(request.getMethod());
        operationLog.setUserAgent(ServletUtils.getUserAgent(request));
        operationLog.setUserIp(IpUtils.getClientIP(request));
        operationLog.setAddress(IpUtils.getAddress());
        operationLog.setStartTime(LocalDateTime.now());
    }


    /**
     * 打印日志
     * @param operationLog
     * @return void
     */
    private void print(OperationLog operationLog) {
        String str = "请求信息:" + operationLog.getRequestMethod() + "|" + operationLog.getRequestUrl() + "; 方法名:" + operationLog.getOptMethod() +
                "; 开始时间:" + DateUtils.toString(operationLog.getStartTime()) +
                "\n请求参数:" + operationLog.getRequestParam() + "\n地理位置:" + operationLog.getUserIp()+ "(" + operationLog.getAddress() + ")" +
                "|" + operationLog.getUserAgent() + "\n操作结果:" +
                operationLog.getResultCode()  + "|" + operationLog.getResultMsg();

        String resultData = operationLog.getResultData();
        if (resultData != null && !resultData.equals("null")) {
            str += "\n----------------------------------------------------------------\n" +
                    operationLog.getResultData() + "\n";
        }

        if (R.error().getCode() == operationLog.getResultCode()) {
            log.error(str);
        } else {
            log.warn(str);
        }
    }
}
