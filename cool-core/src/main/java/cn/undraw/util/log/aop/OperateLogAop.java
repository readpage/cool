package cn.undraw.util.log.aop;

import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.ConvertUtils;
import cn.undraw.util.DateUtils;
import cn.undraw.util.StrUtils;
import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.log.enums.OperateTypeEnum;
import cn.undraw.util.log.service.OperateLogWorkService;
import cn.undraw.util.log.vo.OperationLog;
import cn.undraw.util.result.R;
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
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author readpage
 * @date 2022-11-29 18:54
 */

@Component
@Aspect
@Slf4j
public class OperateLogAop {

    @Resource
    private OperateLogWorkService operateLogWorkService;

    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint pjp, OperateLog operateLog) throws Throwable {
        Instant start = Instant.now();

        Object result = null;
        try {
            result = pjp.proceed();
            record(pjp, operateLog, start, result);
            return result;
        } catch (CustomerException e) {
            R r = e.getR();
            record(pjp, operateLog, start, r);
            R res = ConvertUtils.copy(r, R.class);
            res.setData(null);
            return res;
        }
    }

    /**
     * 记录日志
     * @param pjp
     * @param operateLog
     * @param start
     * @param result
     * @return void
     */
    private void record(ProceedingJoinPoint pjp, OperateLog operateLog, Instant start, Object result) {
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

        R r = R.ok();

        if (result instanceof R) {
            r = ConvertUtils.copy(result, R.class);
        }
        // 设置结果信息
        setResultFields(operationLog, r);

        // 打印日志
        print(operationLog);
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
     * @return void
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
     * @param pjp
     * @return void
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
            try {
                // 构造参数组集合
                List<Object> argList = new ArrayList<>();
                for (Object arg : pjp.getArgs()) {
                    if (arg instanceof HttpServletRequest) {
                        argList.add("request");
                    } else if (arg instanceof HttpServletResponse) {
                        argList.add("response");
                    } else if (arg instanceof String) {
                        argList.add(arg);
                    } else {
                        argList.add(ConvertUtils.toJson(arg));
                    }
                }
                String s = ConvertUtils.toJson(argList);
                // 请求参数
                operationLog.setRequestParam(s);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 获取请求参数
     * @param pjp
     * @return java.lang.String
     */
    @Deprecated
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
     * @param operationLog
     * @param result
     * @return void
     */
    private void setResultFields(OperationLog operationLog, Object result) {
        R r = R.ok();
        if (result instanceof R) {
            r = (R) result;
        } else {
            result = r;
        }

        operationLog.setResultCode(r.getCode());
        operationLog.setResultMsg(r.getMsg());
        Object data = r.getData();
        operationLog.setResultData(ConvertUtils.toJson(data));
    }

    /**
     * 打印日志
     * @param operationLog
     * @return void
     */
    private static void print(OperationLog operationLog) {
        String str = "操作信息: " + operationLog.getModule() + "|" + operationLog.getName() + " " + OperateTypeEnum.getMsg(operationLog.getType()) +
                "; 开始时间:" + DateUtils.toString(operationLog.getStartTime()) + "|持续" + operationLog.getDuration() + "ms\n" +
                "请求信息:" + operationLog.getRequestMethod() + "|" + operationLog.getRequestUrl() + "\n方法名:" + operationLog.getOptMethod() +
                "\n请求参数:" + operationLog.getRequestParam() + "\n地理位置:" + operationLog.getUserIp()+ "(" + operationLog.getAddress() + ")" +
                "|" + operationLog.getUserAgent() + "\n操作结果:" +
                operationLog.getResultCode()  + "|" + operationLog.getResultMsg();


        String resultData = operationLog.getResultData();
        if (resultData != null && !resultData.equals("null")) {
            str += "\n----------------------------------------------------------------\n" +
                    operationLog.getResultData() + "\n";
        }

        if (R.ok().getCode() == operationLog.getResultCode()) {
            log.info(str);
        } else if (R.error().getCode() == operationLog.getResultCode()) {
            log.error(str);
        } else {
            log.warn(str);
        }
    }
}
