package cn.undraw.util.log.aop;

import cn.undraw.handler.CustomerException;
import cn.undraw.util.DateTimeUtils;
import cn.undraw.util.ErrorUtils;
import cn.undraw.util.StrUtils;
import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.log.enums.OperateTypeEnum;
import cn.undraw.util.log.service.OperateLogWorkService;
import cn.undraw.util.log.vo.OperationLog;
import cn.undraw.util.result.Result;
import cn.undraw.util.result.ResultUtils;
import cn.undraw.util.servlet.IpUtils;
import cn.undraw.util.servlet.ServletUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * @author readpage
 * @create 2022-11-29 18:54
 */

@Component
@Aspect
@Slf4j
public class OperateLogAop {

    @Autowired
    private OperateLogWorkService operateLogWorkService;

    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint pjp, OperateLog operateLog) throws Throwable {
        Instant start = Instant.now();

        Object result = null;
        try {
            result = pjp.proceed();
            record(pjp, operateLog, start, result, null);
            return result;
        } catch (Throwable e) {
            record(pjp, operateLog, start, null, e);
            if (e instanceof CustomerException) {
                return ResultUtils.fail(e.getMessage(), e);
            }
            return ResultUtils.error(e);
        }
    }

    /**
     * 记录日志
     * @param pjp
     * @param operateLog
     * @param start
     * @param result
     */
    private void record(ProceedingJoinPoint pjp, OperateLog operateLog, Instant start, Object result, Throwable e) {
        Instant end = Instant.now();
        long time = Duration.between(start, end).toMillis();

        OperationLog operationLog = new OperationLog();
        operationLog.setStartTime(LocalDateTime.ofInstant(start, ZoneId.systemDefault()));

        // 设置模块信息
        setModuleFields(operationLog, pjp, operateLog);
        // 设置请求信息
        setRequestFields(operationLog, pjp);

        // 设置持续时间
        operationLog.setDuration(time);
        setResultFields(operationLog, result, e);

        // 打印日志
        print(operationLog, e);
        operateLogWorkService.createLog(operationLog);
    }

    public static <T extends Annotation> T getMethodAnnotation(JoinPoint joinPoint, Class<T> annotationClass) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(annotationClass);
    }

    public static <T extends Annotation> T getClassAnnotation(JoinPoint joinPoint, Class<T> annotationClass) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod().getDeclaringClass().getAnnotation(annotationClass);
    }

    /**
     * 设置模块信息
     * @param operationLog
     * @param pjp
     * @param operateLog
     */
    private void setModuleFields(OperationLog operationLog, ProceedingJoinPoint pjp, OperateLog operateLog) {
        // 获取操作
        Api api = getClassAnnotation(pjp, Api.class);
        ApiOperation apiOperation = getMethodAnnotation(pjp, ApiOperation.class);
        // 记录模板名
        if (api != null) {
            operationLog.setModule(api.tags()[0]);
        }
        // 记录name属性
        if (apiOperation != null) {
            operationLog.setName(apiOperation.value());
        }
        if (operateLog != null) {
            operationLog.setType(operateLog.type().getType());
            String module = operateLog.module();
            String name = operateLog.name();
            if (StrUtils.isNotEmpty(module)) {
                operationLog.setModule(module);
            }
            if (StrUtils.isNotEmpty(name)) {
                operationLog.setName(name);
            }
        }
    }

    /**
     * 设置请求信息
     * @param operationLog
     */
    private void setRequestFields(OperationLog operationLog, ProceedingJoinPoint pjp) {
        HttpServletRequest request = ServletUtils.getRequest();

        // 请求方式
        operationLog.setRequestMethod(request.getMethod());
        // 请求URL
        operationLog.setRequestUrl(request.getRequestURI());
        operationLog.setUserIp(IpUtils.getClientIP());
        operationLog.setUserAgent(ServletUtils.getUserAgent(request));
        operationLog.setAddress(IpUtils.getAddress());


        // 请求方法
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        operationLog.setOptMethod(methodSignature.toString());
        if (ServletUtils.isNotMultipart(request)) {
            // 请求参数
            operationLog.setRequestParam(getMethodArgs(pjp));
        }
    }

    /**
     * 获取请求参数
     * @param pjp
     * @return
     */
    private String getMethodArgs(ProceedingJoinPoint pjp) {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        String[] argNames = methodSignature.getParameterNames();
        Object[] argValues = pjp.getArgs();
        Map<String, Object> args = new HashMap<>();
        for (int i = 0; i < argNames.length; i++) {
            String argName = argNames[i];
            Object argValue = argValues[i];
            args.put(argName, argValue);
        }
        return args.toString();
    }

    /**
     * 设置结果信息
     */
    private void setResultFields(OperationLog operationLog, Object result, Throwable e) {
        String msg = "成功";
        Integer code = 200;

        // 异常处理
        if (e != null) {
            if (e instanceof CustomerException) {
                code = 401;
                msg = "错误";
            } else {
                code = 500;
                msg = "异常";
            }
            msg +=  "(" + e.getMessage() + ")";
            operationLog.setResultData(ErrorUtils.getStackTrace(e));
        } else {
            if (result instanceof Result) {
                Result r = (Result) result;
                code = r.getCode();
                msg = r.getMsg();
            }
        }
        operationLog.setResultCode(code);
        operationLog.setResultMsg(msg);
    }

    /**
     * 打印日志
     * @param operationLog
     * @param e
     */
    private static void print(OperationLog operationLog, Throwable e) {
        String str = "操作信息: " + operationLog.getModule() + "|" + operationLog.getName() + " " + OperateTypeEnum.getMsg(operationLog.getType()) +
                "; 开始时间:" + DateTimeUtils.toString(operationLog.getStartTime()) + "|持续" + operationLog.getDuration() + "ms\n" +
                "请求信息:" + operationLog.getRequestMethod() + "|" + operationLog.getRequestUrl() + "\n方法名:" + operationLog.getOptMethod() +
                "\n请求参数:" + operationLog.getRequestParam() + "\n地理位置:" + operationLog.getUserIp()+ "(" + operationLog.getAddress() + ")" +
                "|" + operationLog.getUserAgent() + "\n操作结果:" +
                operationLog.getResultMsg() + "|" + operationLog.getResultCode() + "||" + operationLog.getResultData();
        if (e == null) {
            log.info(str);
        } else {
            if (e instanceof CustomerException) {
                log.warn(str);
            } else {
                log.error(str);
            }
        }
    }
}
